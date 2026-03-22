package com.yf.service;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Supplier;
import com.yf.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 供应商服务类
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierMapper supplierMapper;

    /**
     * 搜索供应商（名称/拼音模糊匹配，最多返回20条）
     */
    public List<Supplier> search(String keyword) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getStatus, "启用");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Supplier::getName, keyword)
                .or().like(Supplier::getShortName, keyword)
                .or().like(Supplier::getPinyin, keyword)
                .or().like(Supplier::getPinyinShort, keyword)
            );
        }
        wrapper.orderByDesc(Supplier::getCreatedAt)
               .last("LIMIT 20");
        return supplierMapper.selectList(wrapper);
    }

    /**
     * 分页查询供应商
     *
     * @param page   分页对象
     * @param name   供应商名称
     * @param status 状态
     * @return 分页结果
     */
    public Page<Supplier> page(Page<Supplier> page, String name, String status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();

        // 名称模糊查询
        if (StringUtils.hasText(name)) {
            wrapper.like(Supplier::getName, name)
                    .or().like(Supplier::getShortName, name);
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(Supplier::getStatus, status);
        }

        // 按创建时间降序
        wrapper.orderByDesc(Supplier::getCreatedAt);

        return supplierMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询供应商
     *
     * @param id 供应商ID
     * @return 供应商信息
     */
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    /**
     * 创建供应商
     *
     * @param supplier 供应商信息
     * @return 创建成功的记录数
     */
    public int create(Supplier supplier) {
        fillPinyin(supplier);
        return supplierMapper.insert(supplier);
    }

    /**
     * 更新供应商
     *
     * @param supplier 供应商信息
     * @return 更新成功的记录数
     */
    public int update(Supplier supplier) {
        fillPinyin(supplier);
        return supplierMapper.updateById(supplier);
    }

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     * @return 删除成功的记录数
     */
    public int delete(Long id) {
        return supplierMapper.deleteById(id);
    }

    /**
     * 获取所有有效供应商列表（用于下拉选择）
     *
     * @return 供应商列表
     */
    public List<Supplier> list() {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getStatus, "启用")
               .orderByAsc(Supplier::getName);
        return supplierMapper.selectList(wrapper);
    }

    /**
     * 自动填充拼音
     */
    private void fillPinyin(Supplier supplier) {
        if (StringUtils.hasText(supplier.getName())) {
            supplier.setPinyin(PinyinUtil.getPinyin(supplier.getName(), ""));
            supplier.setPinyinShort(PinyinUtil.getFirstLetter(supplier.getName(), ""));
        }
    }
}
