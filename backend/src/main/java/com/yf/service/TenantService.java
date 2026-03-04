package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Tenant;
import com.yf.exception.BusinessException;
import com.yf.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;

    public Page<Tenant> page(Page<Tenant> page, String name, String status) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Tenant::getName, name)
               .eq(StringUtils.hasText(status), Tenant::getStatus, status)
               .orderByDesc(Tenant::getCreatedAt);
        return tenantMapper.selectPage(page, wrapper);
    }

    public List<Tenant> listAll() {
        return tenantMapper.selectList(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, "active")
                .orderByAsc(Tenant::getName));
    }

    public Tenant getById(Long id) {
        return tenantMapper.selectById(id);
    }

    @Transactional
    public void create(Tenant tenant) {
        // 检查租户编码是否重复
        Long count = tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, tenant.getCode()));
        if (count > 0) {
            throw new BusinessException("租户编码已存在");
        }
        tenant.setStatus("active");
        tenantMapper.insert(tenant);
    }

    @Transactional
    public void update(Tenant tenant) {
        Tenant existing = tenantMapper.selectById(tenant.getId());
        if (existing == null) {
            throw new BusinessException("租户不存在");
        }
        // 检查编码是否被其他租户使用
        Long count = tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, tenant.getCode())
                .ne(Tenant::getId, tenant.getId()));
        if (count > 0) {
            throw new BusinessException("租户编码已被使用");
        }
        tenantMapper.updateById(tenant);
    }

    @Transactional
    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }

    @Transactional
    public void enable(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        tenant.setStatus("active");
        tenantMapper.updateById(tenant);
    }

    @Transactional
    public void disable(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        tenant.setStatus("inactive");
        tenantMapper.updateById(tenant);
    }
}
