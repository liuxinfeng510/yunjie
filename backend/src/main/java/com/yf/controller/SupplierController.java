package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Supplier;
import com.yf.service.SupplierService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商控制器
 */
@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * 分页查询供应商
     *
     * @param name     供应商名称
     * @param status   状态
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        Page<Supplier> result = supplierService.page(page, name, status);
        return ApiResponse.success(result);
    }

    /**
     * 搜索供应商（名称/拼音模糊匹配）
     *
     * @param keyword 搜索关键字
     * @return 匹配的供应商列表
     */
    @GetMapping("/search")
    public ApiResponse search(@RequestParam(required = false) String keyword) {
        java.util.List<Supplier> list = supplierService.search(keyword);
        return ApiResponse.success(list);
    }

    /**
     * 获取供应商列表(用于下拉选择)
     */
    @GetMapping("/list")
    public ApiResponse<java.util.List<Supplier>> list() {
        java.util.List<Supplier> result = supplierService.list();
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询供应商
     *
     * @param id 供应商ID
     * @return 供应商信息
     */
    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        if (supplier == null) {
            return ApiResponse.error("供应商不存在");
        }
        return ApiResponse.success(supplier);
    }

    /**
     * 创建供应商
     *
     * @param supplier 供应商信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody Supplier supplier) {
        int result = supplierService.create(supplier);
        if (result > 0) {
            return ApiResponse.success();
        }
        return ApiResponse.error("创建失败");
    }

    /**
     * 更新供应商
     *
     * @param id       供应商ID
     * @param supplier 供应商信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        int result = supplierService.update(supplier);
        if (result > 0) {
            return ApiResponse.success();
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 批量补全拼音码
     */
    @PostMapping("/rebuild-pinyin")
    public ApiResponse rebuildPinyin() {
        int count = supplierService.rebuildAllPinyin();
        return ApiResponse.success("已补全 " + count + " 条供应商拼音码");
    }

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        int result = supplierService.delete(id);
        if (result > 0) {
            return ApiResponse.success();
        }
        return ApiResponse.error("删除失败");
    }
}
