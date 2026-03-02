package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Supplier;
import com.yf.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 供应商服务类
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierMapper supplierMapper;

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
        return supplierMapper.insert(supplier);
    }

    /**
     * 更新供应商
     *
     * @param supplier 供应商信息
     * @return 更新成功的记录数
     */
    public int update(Supplier supplier) {
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
}
