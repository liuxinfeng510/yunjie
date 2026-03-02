package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Store;
import com.yf.service.StoreService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店管理控制器
 */
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 分页查询门店
     */
    @GetMapping("/page")
    public ApiResponse<Page<Store>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(storeService.page(new Page<>(current, size), name, type, status));
    }

    /**
     * 获取所有门店列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Store>> list() {
        return ApiResponse.success(storeService.listAll());
    }

    /**
     * 获取门店详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Store> getById(@PathVariable Long id) {
        return ApiResponse.success(storeService.getById(id));
    }

    /**
     * 创建门店
     */
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Store store) {
        storeService.create(store);
        return ApiResponse.success();
    }

    /**
     * 更新门店
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Store store) {
        store.setId(id);
        storeService.update(store);
        return ApiResponse.success();
    }

    /**
     * 删除门店
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return ApiResponse.success();
    }
}
