package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.DrugCategory;
import com.yf.mapper.DrugCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 药品分类服务类
 * drug_category 已从租户自动过滤中排除，所有查询需手动处理租户逻辑
 * 系统分类 (tenant_id=0, is_system=true) 全局可见，不可修改删除
 * 租户自定义分类 (tenant_id=N, is_system=false) 仅本租户可见
 */
@Service
@RequiredArgsConstructor
public class DrugCategoryService {

    private static final String HERB_CATEGORY_NAME = "中药饮片";

    private final DrugCategoryMapper drugCategoryMapper;

    /**
     * 获取分类树（系统分类 + 当前租户自定义分类）
     */
    public List<DrugCategory> getTree() {
        Long tenantId = TenantContext.getTenantId();
        LambdaQueryWrapper<DrugCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DrugCategory::getTenantId, Arrays.asList(0L, tenantId != null ? tenantId : 0L))
               .orderByAsc(DrugCategory::getSortOrder);
        List<DrugCategory> allCategories = drugCategoryMapper.selectList(wrapper);
        return buildTree(allCategories, 0L);
    }

    /**
     * 递归构建树形结构
     */
    private List<DrugCategory> buildTree(List<DrugCategory> allCategories, Long parentId) {
        List<DrugCategory> children = new ArrayList<>();
        for (DrugCategory category : allCategories) {
            Long pid = category.getParentId();
            if ((parentId == null && pid == null) ||
                    (parentId != null && parentId.equals(pid))) {
                category.setChildren(buildTree(allCategories, category.getId()));
                children.add(category);
            }
        }
        return children;
    }

    /**
     * 判断某个分类ID是否属于"中药饮片"及其子分类
     */
    public boolean isHerbCategory(Long categoryId) {
        if (categoryId == null) return false;
        LambdaQueryWrapper<DrugCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugCategory::getName, HERB_CATEGORY_NAME)
               .eq(DrugCategory::getParentId, 0L)
               .eq(DrugCategory::getTenantId, 0L)
               .eq(DrugCategory::getIsSystem, true);
        DrugCategory herbRoot = drugCategoryMapper.selectOne(wrapper);
        if (herbRoot == null) return false;
        return herbRoot.getId().equals(categoryId);
    }

    /**
     * 初始化中药饮片分类 — 全局分类已由迁移脚本创建，此方法保留兼容
     */
    public void initHerbCategories() {
        // 全局分类已在 V17 迁移脚本中创建，无需再初始化
    }

    /**
     * 根据ID查询分类（仅允许查看系统分类或本租户分类）
     */
    public DrugCategory getById(Long id) {
        DrugCategory category = drugCategoryMapper.selectById(id);
        if (category == null) return null;
        Long tenantId = TenantContext.getTenantId();
        if (category.getTenantId() == 0L || category.getTenantId().equals(tenantId)) {
            return category;
        }
        return null; // 不允许跨租户读取
    }

    /**
     * 创建分类（租户自定义，手动设置 tenantId）
     */
    public int create(DrugCategory category) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId == 0L) {
            throw new RuntimeException("无法确定当前租户");
        }
        category.setTenantId(tenantId);
        category.setIsSystem(false);

        // 校验 parentId 合法性
        if (category.getParentId() != null && category.getParentId() != 0L) {
            DrugCategory parent = drugCategoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new RuntimeException("上级分类不存在");
            }
            // parentId 必须是系统分类或本租户分类
            if (parent.getTenantId() != 0L && !parent.getTenantId().equals(tenantId)) {
                throw new RuntimeException("无权在此分类下创建子分类");
            }
        }
        return drugCategoryMapper.insert(category);
    }

    /**
     * 更新分类（系统分类不可修改）
     */
    public int update(DrugCategory category) {
        DrugCategory existing = drugCategoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new RuntimeException("系统预置分类不允许修改");
        }
        Long tenantId = TenantContext.getTenantId();
        if (!existing.getTenantId().equals(tenantId)) {
            throw new RuntimeException("无权修改此分类");
        }
        return drugCategoryMapper.updateById(category);
    }

    /**
     * 删除分类（系统分类不可删除）
     */
    public int delete(Long id) {
        DrugCategory existing = drugCategoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new RuntimeException("系统预置分类不允许删除");
        }
        Long tenantId = TenantContext.getTenantId();
        if (!existing.getTenantId().equals(tenantId)) {
            throw new RuntimeException("无权删除此分类");
        }
        // 检查子分类
        LambdaQueryWrapper<DrugCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugCategory::getParentId, id);
        Long count = drugCategoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        return drugCategoryMapper.deleteById(id);
    }
}
