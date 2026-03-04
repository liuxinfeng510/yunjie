package com.yf.controller;

import com.yf.entity.SysDictItem;
import com.yf.service.SysDictItemService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统字典控制器
 */
@RestController
@RequestMapping("/sys-dict")
@RequiredArgsConstructor
public class SysDictItemController {

    private final SysDictItemService sysDictItemService;

    /**
     * 按类型查询字典项列表
     *
     * @param type 字典类型（dosage_form/drug_unit/storage_condition）
     * @return 字典项列表
     */
    @GetMapping("/{type}/list")
    public ApiResponse listByType(@PathVariable String type) {
        List<SysDictItem> list = sysDictItemService.listByType(type);
        return ApiResponse.success(list);
    }

    /**
     * 根据ID查询字典项
     *
     * @param id 字典项ID
     * @return 字典项信息
     */
    @GetMapping("/item/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        SysDictItem item = sysDictItemService.getById(id);
        if (item == null) {
            return ApiResponse.error("字典项不存在");
        }
        return ApiResponse.success(item);
    }

    /**
     * 创建字典项
     *
     * @param item 字典项信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody SysDictItem item) {
        try {
            SysDictItem created = sysDictItemService.create(item);
            return ApiResponse.success(created);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新字典项
     *
     * @param id   字典项ID
     * @param item 字典项信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody SysDictItem item) {
        try {
            item.setId(id);
            sysDictItemService.update(item);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        try {
            sysDictItemService.delete(id);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
