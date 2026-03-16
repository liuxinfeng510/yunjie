package com.yf.controller;

import com.yf.entity.DrugCategory;
import com.yf.service.DrugCategoryService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药品分类控制器
 */
@RestController
@RequestMapping("/drug-category")
@RequiredArgsConstructor
public class DrugCategoryController {

    private final DrugCategoryService drugCategoryService;

    /**
     * 获取分类树
     *
     * @return 树形结构的分类列表
     */
    @GetMapping("/tree")
    public ApiResponse getTree() {
        List<DrugCategory> tree = drugCategoryService.getTree();
        return ApiResponse.success(tree);
    }

    /**
     * 初始化中药饮片分类（幂等）
     */
    @PostMapping("/init-herb")
    public ApiResponse initHerbCategories() {
        drugCategoryService.initHerbCategories();
        return ApiResponse.success();
    }

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        DrugCategory category = drugCategoryService.getById(id);
        if (category == null) {
            return ApiResponse.error("分类不存在");
        }
        return ApiResponse.success(category);
    }

    /**
     * 创建分类
     *
     * @param category 分类信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody DrugCategory category) {
        try {
            int result = drugCategoryService.create(category);
            if (result > 0) {
                return ApiResponse.success();
            }
            return ApiResponse.error("创建失败");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新分类
     *
     * @param id       分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody DrugCategory category) {
        category.setId(id);
        try {
            int result = drugCategoryService.update(category);
            if (result > 0) {
                return ApiResponse.success();
            }
            return ApiResponse.error("更新失败");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        try {
            int result = drugCategoryService.delete(id);
            if (result > 0) {
                return ApiResponse.success();
            }
            return ApiResponse.error("删除失败");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
