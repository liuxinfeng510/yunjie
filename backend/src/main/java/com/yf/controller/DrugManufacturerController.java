package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugManufacturer;
import com.yf.service.DrugManufacturerService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生产企业控制器
 */
@RestController
@RequestMapping("/drug-manufacturer")
@RequiredArgsConstructor
public class DrugManufacturerController {

    private final DrugManufacturerService manufacturerService;

    /**
     * 搜索生产企业（名称/拼音模糊匹配，最多返回20条）
     *
     * @param keyword 搜索关键字
     * @return 匹配的生产企业列表
     */
    @GetMapping("/search")
    public ApiResponse search(@RequestParam(required = false) String keyword) {
        List<DrugManufacturer> list = manufacturerService.search(keyword);
        return ApiResponse.success(list);
    }

    /**
     * 分页查询生产企业
     *
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<DrugManufacturer> page = manufacturerService.page(keyword, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    /**
     * 根据ID查询生产企业
     *
     * @param id 企业ID
     * @return 企业信息
     */
    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        DrugManufacturer manufacturer = manufacturerService.getById(id);
        if (manufacturer == null) {
            return ApiResponse.error("生产企业不存在");
        }
        return ApiResponse.success(manufacturer);
    }

    /**
     * 按名称查找或创建
     *
     * @param name 企业名称
     * @return 企业信息
     */
    @PostMapping("/get-or-create")
    public ApiResponse getOrCreate(@RequestParam String name) {
        DrugManufacturer manufacturer = manufacturerService.getOrCreate(name);
        return ApiResponse.success(manufacturer);
    }

    /**
     * 创建生产企业
     *
     * @param manufacturer 企业信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody DrugManufacturer manufacturer) {
        try {
            DrugManufacturer created = manufacturerService.create(manufacturer);
            return ApiResponse.success(created);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新生产企业
     *
     * @param id           企业ID
     * @param manufacturer 企业信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody DrugManufacturer manufacturer) {
        try {
            manufacturer.setId(id);
            manufacturerService.update(manufacturer);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除生产企业
     *
     * @param id 企业ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        try {
            manufacturerService.delete(id);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
