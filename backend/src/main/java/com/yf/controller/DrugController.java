package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.dto.DrugRequest;
import com.yf.entity.Drug;
import com.yf.entity.DrugBarcode;
import com.yf.service.DrugBarcodeService;
import com.yf.service.DrugService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 药品控制器
 */
@RestController
@RequestMapping("/drug")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;
    private final DrugBarcodeService drugBarcodeService;
    private final com.yf.service.DrugCategoryService drugCategoryService;

    /**
     * 分页查询药品
     *
     * @param name       商品名称
     * @param keyword    关键词(兼容POS搜索)
     * @param categoryId 分类ID
     * @param otcType    OTC类型
     * @param status     状态
     * @param pageNum    页码
     * @param current    页码(兼容)
     * @param pageSize   每页大小
     * @param size       每页大小(兼容)
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String otcType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) Long size
    ) {
        // 兼容不同参数名
        String searchKey = keyword != null ? keyword : name;
        long pn = current != null ? current : (pageNum != null ? pageNum : 1);
        long ps = size != null ? size : (pageSize != null ? pageSize : 10);
        
        Page<Drug> page = new Page<>(pn, ps);
        var result = drugService.pageWithStock(page, searchKey, categoryId, otcType, status);
        return ApiResponse.success(result);
    }

    /**
     * 获取药品列表(用于下拉选择)
     */
    @GetMapping("/list")
    public ApiResponse<java.util.List<Drug>> list() {
        java.util.List<Drug> result = drugService.list();
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询药品（包含条形码列表）
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
        // 查询条形码列表
        List<DrugBarcode> barcodes = drugBarcodeService.listByDrugId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("drug", drug);
        result.put("barcodes", barcodes);
        return ApiResponse.success(result);
    }

    /**
     * 创建药品（支持条形码列表）
     *
     * @param request 药品请求（含条形码列表）
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody DrugRequest request) {
        try {
            Drug drug = request.getDrug();
            drug.setIsHerb(drugCategoryService.isHerbCategory(drug.getCategoryId()));
            drug = drugService.createWithBarcodes(drug, request.getBarcodes());
            return ApiResponse.success(drug);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新药品（支持条形码列表）
     *
     * @param id      药品ID
     * @param request 药品请求（含条形码列表）
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody DrugRequest request) {
        try {
            Drug drug = request.getDrug();
            drug.setId(id);
            drug.setIsHerb(drugCategoryService.isHerbCategory(drug.getCategoryId()));
            drugService.updateWithBarcodes(drug, request.getBarcodes());
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
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
