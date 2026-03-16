package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Tenant;
import com.yf.entity.Store;
import com.yf.entity.SysUser;
import com.yf.exception.BusinessException;
import com.yf.mapper.TenantMapper;
import com.yf.mapper.StoreMapper;
import com.yf.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;
    private final StoreMapper storeMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("tenantCount", tenantMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.put("activeTenants", tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, "active")));
        stats.put("storeCount", storeMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.put("userCount", sysUserMapper.selectCount(new LambdaQueryWrapper<>()));
        return stats;
    }

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
    public Map<String, String> create(Tenant tenant, String adminRealName, String contactPhone) {
        // 自动生成租户编码: YF + 年月日 + 4位随机数
        String code = generateTenantCode();
        tenant.setCode(code);
        tenant.setStatus("active");
        tenantMapper.insert(tenant);

        // 自动生成管理员账号和密码
        String adminUsername = "admin_" + code.toLowerCase();
        String rawPassword = generatePassword(8);

        SysUser admin = new SysUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRealName(StringUtils.hasText(adminRealName) ? adminRealName : tenant.getContactName());
        admin.setPhone(StringUtils.hasText(contactPhone) ? contactPhone : tenant.getContactPhone());
        admin.setRole("admin");
        admin.setStatus("active");
        admin.setTenantId(tenant.getId());
        sysUserMapper.insert(admin);

        Map<String, String> result = new HashMap<>();
        result.put("tenantCode", code);
        result.put("adminUsername", adminUsername);
        result.put("adminPassword", rawPassword);
        return result;
    }

    private String generateTenantCode() {
        String prefix = "YF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 10; i++) {
            String code = prefix + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            Long count = tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>().eq(Tenant::getCode, code));
            if (count == 0) {
                return code;
            }
        }
        throw new BusinessException("生成租户编码失败，请重试");
    }

    private String generatePassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
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
