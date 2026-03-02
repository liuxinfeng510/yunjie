package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Promotion;
import com.yf.service.PromotionService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 促销活动控制器
 */
@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * 创建促销活动
     */
    @PostMapping
    public ApiResponse<Promotion> create(@RequestBody Promotion promotion) {
        return ApiResponse.success(promotionService.create(promotion));
    }

    /**
     * 更新促销活动
     */
    @PutMapping("/{id}")
    public ApiResponse<Promotion> update(@PathVariable Long id, @RequestBody Promotion promotion) {
        promotion.setId(id);
        return ApiResponse.success(promotionService.update(promotion));
    }

    /**
     * 启用促销活动
     */
    @PostMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        promotionService.activate(id);
        return ApiResponse.success(null);
    }

    /**
     * 暂停促销活动
     */
    @PostMapping("/{id}/pause")
    public ApiResponse<Void> pause(@PathVariable Long id) {
        promotionService.pause(id);
        return ApiResponse.success(null);
    }

    /**
     * 结束促销活动
     */
    @PostMapping("/{id}/end")
    public ApiResponse<Void> end(@PathVariable Long id) {
        promotionService.end(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Promotion> getById(@PathVariable Long id) {
        return ApiResponse.success(promotionService.getById(id));
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<Promotion>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(promotionService.page(storeId, status, type, pageNum, pageSize));
    }

    /**
     * 获取当前有效促销
     */
    @GetMapping("/active")
    public ApiResponse<List<Promotion>> getActivePromotions(
            @RequestParam(required = false) Long storeId) {
        return ApiResponse.success(promotionService.getActivePromotions(storeId));
    }

    /**
     * 计算药品优惠
     */
    @PostMapping("/calculate")
    public ApiResponse<PromotionService.PromotionResult> calculate(@RequestBody CalculateRequest req) {
        return ApiResponse.success(promotionService.calculatePromotion(
                req.getStoreId(), req.getDrugId(), req.getCategoryId(),
                req.getOriginalPrice(), req.getQuantity(), req.getMemberLevelId()));
    }

    @Data
    public static class CalculateRequest {
        private Long storeId;
        private Long drugId;
        private Long categoryId;
        private BigDecimal originalPrice;
        private Integer quantity;
        private Long memberLevelId;
    }
}
