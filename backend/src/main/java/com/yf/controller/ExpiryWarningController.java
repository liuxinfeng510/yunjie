package com.yf.controller;

import com.yf.entity.DrugBatch;
import com.yf.service.ExpiryWarningService;
import com.yf.service.ExpiryWarningService.ExpiryWarningSummary;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 效期预警控制器
 */
@RestController
@RequestMapping("/expiry-warning")
@RequiredArgsConstructor
public class ExpiryWarningController {

    private final ExpiryWarningService expiryWarningService;

    /**
     * 获取效期预警汇总
     */
    @GetMapping("/summary")
    public ApiResponse<ExpiryWarningSummary> getSummary(
            @RequestParam(required = false) Long storeId) {
        return ApiResponse.success(expiryWarningService.getWarningSummary(storeId));
    }

    /**
     * 获取近效期批次
     */
    @GetMapping("/near-expiry")
    public ApiResponse<List<DrugBatch>> getNearExpiry(
            @RequestParam(defaultValue = "90") int days) {
        return ApiResponse.success(expiryWarningService.getNearExpiryBatches(days));
    }

    /**
     * 获取已过期批次
     */
    @GetMapping("/expired")
    public ApiResponse<List<DrugBatch>> getExpired() {
        return ApiResponse.success(expiryWarningService.getExpiredBatches());
    }

    /**
     * 手动触发效期检查
     */
    @PostMapping("/check")
    public ApiResponse<Void> triggerCheck() {
        expiryWarningService.checkExpiryDaily();
        return ApiResponse.success();
    }
}
