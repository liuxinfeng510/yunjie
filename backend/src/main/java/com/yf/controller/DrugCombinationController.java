package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugCombination;
import com.yf.service.DrugCombinationService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 联合用药推荐控制器
 */
@RestController
@RequestMapping("/drug-combination")
@RequiredArgsConstructor
public class DrugCombinationController {

    private final DrugCombinationService combinationService;

    /**
     * 创建联合用药推荐
     */
    @PostMapping
    public ApiResponse<DrugCombination> create(@RequestBody DrugCombination combination) {
        return ApiResponse.success(combinationService.create(combination));
    }

    /**
     * 批量创建
     */
    @PostMapping("/batch")
    public ApiResponse<Void> batchCreate(@RequestBody List<DrugCombination> combinations) {
        combinationService.batchCreate(combinations);
        return ApiResponse.success(null);
    }

    /**
     * 更新联合用药推荐
     */
    @PutMapping("/{id}")
    public ApiResponse<DrugCombination> update(@PathVariable Long id, @RequestBody DrugCombination combination) {
        combination.setId(id);
        return ApiResponse.success(combinationService.update(combination));
    }

    /**
     * 删除联合用药推荐
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        combinationService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 启用/停用
     */
    @PostMapping("/{id}/toggle")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id) {
        combinationService.toggleStatus(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DrugCombination> getById(@PathVariable Long id) {
        return ApiResponse.success(combinationService.getById(id));
    }

    /**
     * 获取药品的推荐联合用药
     */
    @GetMapping("/recommend/{drugId}")
    public ApiResponse<List<DrugCombination>> getRecommendations(@PathVariable Long drugId) {
        return ApiResponse.success(combinationService.getRecommendations(drugId));
    }

    /**
     * 按类型获取推荐
     */
    @GetMapping("/recommend/{drugId}/{type}")
    public ApiResponse<List<DrugCombination>> getRecommendationsByType(
            @PathVariable Long drugId, @PathVariable String type) {
        return ApiResponse.success(combinationService.getRecommendationsByType(drugId, type));
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<DrugCombination>> page(
            @RequestParam(required = false) Long primaryDrugId,
            @RequestParam(required = false) String recommendType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(combinationService.page(primaryDrugId, recommendType, status, pageNum, pageSize));
    }
}
