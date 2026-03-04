package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.SupplierQualification;
import com.yf.mapper.SupplierQualificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 供应商资质服务类
 */
@Service
@RequiredArgsConstructor
public class SupplierQualificationService {

    private final SupplierQualificationMapper supplierQualificationMapper;

    /**
     * 分页查询供应商资质
     */
    public Page<SupplierQualification> page(Page<SupplierQualification> page, Long supplierId, String licenseType) {
        LambdaQueryWrapper<SupplierQualification> wrapper = new LambdaQueryWrapper<>();
        
        if (supplierId != null) {
            wrapper.eq(SupplierQualification::getSupplierId, supplierId);
        }
        
        if (StringUtils.hasText(licenseType)) {
            wrapper.eq(SupplierQualification::getLicenseType, licenseType);
        }
        
        wrapper.orderByDesc(SupplierQualification::getCreatedAt);
        
        return supplierQualificationMapper.selectPage(page, wrapper);
    }

    /**
     * 根据供应商ID查询资质列表
     */
    public List<SupplierQualification> listBySupplierId(Long supplierId) {
        LambdaQueryWrapper<SupplierQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierQualification::getSupplierId, supplierId)
               .orderByAsc(SupplierQualification::getLicenseType);
        return supplierQualificationMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询资质
     */
    public SupplierQualification getById(Long id) {
        return supplierQualificationMapper.selectById(id);
    }

    /**
     * 创建资质
     */
    public int create(SupplierQualification qualification) {
        return supplierQualificationMapper.insert(qualification);
    }

    /**
     * 更新资质
     */
    public int update(SupplierQualification qualification) {
        return supplierQualificationMapper.updateById(qualification);
    }

    /**
     * 删除资质
     */
    public int delete(Long id) {
        return supplierQualificationMapper.deleteById(id);
    }
}
