package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.GspComplianceCheck;
import com.yf.service.GspAutomationService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * GSP自动化控制器
 */
@RestController
@RequestMapping("/gsp-automation")
@RequiredArgsConstructor
public class GspAutomationController {

    private final GspAutomationService gspAutomationService;

    /**
     * 执行合规检查
     */
    @PostMapping("/check")
    public ApiResponse<GspComplianceCheck> executeCheck(@RequestBody CheckRequest request) {
        Long checkerId = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            checkerId = (Long) principal;
        }
        GspComplianceCheck check = gspAutomationService.executeComplianceCheck(
            request.getStoreId(), request.getCheckType(), checkerId);
        return ApiResponse.success(check);
    }

    /**
     * 分页查询检查记录
     */
    @GetMapping("/checks")
    public ApiResponse<Page<GspComplianceCheck>> pageChecks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String checkType) {
        Page<GspComplianceCheck> page = new Page<>(current, size);
        Page<GspComplianceCheck> result = gspAutomationService.pageChecks(page, storeId, checkType);
        return ApiResponse.success(result);
    }

    /**
     * 获取合规统计
     */
    @GetMapping("/statistics")
    public ApiResponse<GspAutomationService.ComplianceStatistics> getStatistics(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GspAutomationService.ComplianceStatistics stats = 
            gspAutomationService.getStatistics(storeId, startDate, endDate);
        return ApiResponse.success(stats);
    }

    /**
     * 更新整改状态
     */
    @PostMapping("/checks/{id}/correction")
    public ApiResponse<Void> updateCorrection(
            @PathVariable Long id,
            @RequestBody CorrectionRequest request) {
        gspAutomationService.updateCorrectionStatus(id, request.getStatus(), request.getMeasures());
        return ApiResponse.success(null);
    }

    @Data
    public static class CheckRequest {
        private Long storeId;
        private String checkType;
    }

    @Data
    public static class CorrectionRequest {
        private String status;
        private String measures;
    }
}
