package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.config.security.JwtUtil;
import com.yf.config.tenant.TenantContext;
import com.yf.dto.LoginRequest;
import com.yf.dto.LoginResponse;
import com.yf.entity.SysUser;
import com.yf.entity.Tenant;
import com.yf.exception.BusinessException;
import com.yf.mapper.SysUserMapper;
import com.yf.mapper.TenantMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final TenantMapper tenantMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper userMapper, TenantMapper tenantMapper,
                       JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        // 查询时暂时忽略租户过滤
        TenantContext.setTenantId(0L);
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
                        .eq(SysUser::getStatus, "active")
        );
        TenantContext.clear();

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        Tenant tenant = tenantMapper.selectById(user.getTenantId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getTenantId());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .storeId(user.getStoreId())
                .businessMode(tenant != null ? tenant.getBusinessMode() : "single_store")
                .build();
    }
}
