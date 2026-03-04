package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.SupplierQualification;
import com.yf.service.SupplierQualificationService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商资质控制器
 */
@RestController
@RequestMapping("/supplier-qualification")
@RequiredArgsConstructor
public class SupplierQualificationController {

    private final SupplierQualificationService supplierQualificationService;

    /**
     * 分页查询供应商资质
     */
    @GetMapping("/page")
    public ApiResponse<Page<SupplierQualification>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String licenseType) {
        Page<SupplierQualification> page = new Page<>(current, size);
        Page<SupplierQualification> result = supplierQualificationService.page(page, supplierId, licenseType);
        return ApiResponse.success(result);
    }

    /**
     * 根据供应商ID查询资质列表
     */
    @GetMapping("/list")
    public ApiResponse<List<SupplierQualification>> listBySupplierId(@RequestParam Long supplierId) {
        List<SupplierQualification> result = supplierQualificationService.listBySupplierId(supplierId);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询资质详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SupplierQualification> getById(@PathVariable Long id) {
        SupplierQualification result = supplierQualificationService.getById(id);
        return ApiResponse.success(result);
    }

    /**
     * 创建供应商资质
     */
    @PostMapping
    public ApiResponse<Void> create(@RequestBody SupplierQualification qualification) {
        supplierQualificationService.create(qualification);
        return ApiResponse.success(null);
    }

    /**
     * 更新供应商资质
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SupplierQualification qualification) {
        qualification.setId(id);
        supplierQualificationService.update(qualification);
        return ApiResponse.success(null);
    }

    /**
     * 删除供应商资质
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierQualificationService.delete(id);
        return ApiResponse.success(null);
    }
}
