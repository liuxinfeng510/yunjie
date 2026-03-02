package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.service.DrugService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 药品控制器
 */
@RestController
@RequestMapping("/drug")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    /**
     * 分页查询药品
     *
     * @param name       药品名称
     * @param categoryId 分类ID
     * @param otcType    OTC类型
     * @param status     状态
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String otcType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        Page<Drug> page = new Page<>(pageNum, pageSize);
        Page<Drug> result = drugService.page(page, name, categoryId, otcType, status);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询药品
     *
     * @param id 药品ID
     * @return 药品信息
     */
    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        Drug drug = drugService.getById(id);
        if (drug == null) {
            return ApiResponse.error("药品不存在");
        }
        return ApiResponse.success(drug);
    }

    /**
     * 创建药品
     *
     * @param drug 药品信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody Drug drug) {
        int result = drugService.create(drug);
        if (result > 0) {
            return ApiResponse.success();
        }
        return ApiResponse.error("创建失败");
    }

    /**
     * 更新药品
     *
     * @param id   药品ID
     * @param drug 药品信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody Drug drug) {
        drug.setId(id);
        int result = drugService.update(drug);
        if (result > 0) {
            return ApiResponse.success();
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 删除药品
     *
     * @param id 药品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        int result = drugService.delete(id);
        if (result > 0) {
            return ApiResponse.success();
        }
        return ApiResponse.error("删除失败");
    }
}
