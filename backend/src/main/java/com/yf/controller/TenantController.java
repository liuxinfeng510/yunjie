package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Tenant;
import com.yf.service.TenantService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理控制器 - 仅超级管理员可访问
 */
@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/page")
    public ApiResponse<Page<Tenant>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(tenantService.page(new Page<>(current, size), name, status));
    }

    @GetMapping("/list")
    public ApiResponse<List<Tenant>> list() {
        return ApiResponse.success(tenantService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Tenant> getById(@PathVariable Long id) {
        return ApiResponse.success(tenantService.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Tenant tenant) {
        tenantService.create(tenant);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Tenant tenant) {
        tenant.setId(id);
        tenantService.update(tenant);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        tenantService.enable(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        tenantService.disable(id);
        return ApiResponse.success();
    }
}
