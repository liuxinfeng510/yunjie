package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.DrugCategory;
import com.yf.mapper.DrugCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 药品分类服务类
 */
@Service
@RequiredArgsConstructor
public class DrugCategoryService {

    private final DrugCategoryMapper drugCategoryMapper;

    /**
     * 获取分类树
     *
     * @return 树形结构的分类列表
     */
    public List<DrugCategory> getTree() {
        // 查询所有分类
        LambdaQueryWrapper<DrugCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DrugCategory::getSortOrder);
        List<DrugCategory> allCategories = drugCategoryMapper.selectList(wrapper);

        // 构建树形结构
        return buildTree(allCategories, null);
    }

    /**
     * 递归构建树形结构
     *
     * @param allCategories 所有分类列表
     * @param parentId      父级ID
     * @return 树形结构
     */
    private List<DrugCategory> buildTree(List<DrugCategory> allCategories, Long parentId) {
        List<DrugCategory> children = new ArrayList<>();

        for (DrugCategory category : allCategories) {
            Long categoryParentId = category.getParentId();

            // 判断是否为当前父节点的子节点
            if ((parentId == null && categoryParentId == null) ||
                    (parentId != null && parentId.equals(categoryParentId))) {
                children.add(category);
            }
        }

        return children;
    }

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    public DrugCategory getById(Long id) {
        return drugCategoryMapper.selectById(id);
    }

    /**
     * 创建分类
     *
     * @param category 分类信息
     * @return 创建成功的记录数
     */
    public int create(DrugCategory category) {
        return drugCategoryMapper.insert(category);
    }

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 更新成功的记录数
     */
    public int update(DrugCategory category) {
        return drugCategoryMapper.updateById(category);
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除成功的记录数
     */
    public int delete(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<DrugCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugCategory::getParentId, id);
        Long count = drugCategoryMapper.selectCount(wrapper);

        if (count > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }

        return drugCategoryMapper.deleteById(id);
    }
}
