package com.yf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.SysUser;
import com.yf.mapper.SysUserMapper;
import com.yf.vo.ApiResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/page")
    public ApiResponse<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        
        return ApiResponse.success(userMapper.selectPage(page, wrapper));
    }

    @GetMapping("/list")
    public ApiResponse<List<SysUser>> list(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        wrapper.eq(SysUser::getStatus, "active");
        return ApiResponse.success(userMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        return ApiResponse.success(userMapper.selectById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> data) {
        SysUser user = new SysUser();
        user.setUsername((String) data.get("username"));
        user.setRealName((String) data.get("realName"));
        user.setPhone((String) data.get("phone"));
        user.setRole((String) data.get("role"));
        user.setStatus("active");
        user.setTenantId(TenantContext.getTenantId());
        
        // 处理门店ID
        Object storeIdObj = data.get("storeId");
        if (storeIdObj != null) {
            user.setStoreId(Long.valueOf(storeIdObj.toString()));
        }
        
        // 加密密码
        String password = (String) data.get("password");
        if (StringUtils.hasText(password)) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        
        userMapper.insert(user);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        
        if (data.containsKey("realName")) {
            user.setRealName((String) data.get("realName"));
        }
        if (data.containsKey("phone")) {
            user.setPhone((String) data.get("phone"));
        }
        if (data.containsKey("role")) {
            user.setRole((String) data.get("role"));
        }
        if (data.containsKey("storeId")) {
            Object storeIdObj = data.get("storeId");
            user.setStoreId(storeIdObj != null ? Long.valueOf(storeIdObj.toString()) : null);
        }
        
        userMapper.updateById(user);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> data) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        user.setStatus(data.get("status"));
        userMapper.updateById(user);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        String newPassword = "123456";
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return ApiResponse.success(newPassword);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return ApiResponse.success(null);
    }
}
