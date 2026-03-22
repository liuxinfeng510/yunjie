package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DefectiveDrug;
import com.yf.service.DefectiveDrugService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/defective-drug")
@RequiredArgsConstructor
public class DefectiveDrugController {

    private final DefectiveDrugService defectiveService;

    @PostMapping
    public ApiResponse<DefectiveDrug> register(@RequestBody DefectiveDrug defective) {
        return ApiResponse.success(defectiveService.register(defective));
    }

    @PostMapping("/{id}/dispose")
    public ApiResponse<Void> dispose(@PathVariable Long id, @RequestBody DisposeRequest request) {
        defectiveService.dispose(id, request.getDisposalMethod(), request.getRemark(), request.getDisposalHandlerName());
        return ApiResponse.success(null);
    }

    @GetMapping("/page")
    public ApiResponse<Page<DefectiveDrug>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String drugName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<DefectiveDrug> page = new Page<>(current, size);
        return ApiResponse.success(defectiveService.page(page, storeId, status, drugName, startDate, endDate));
    }

    @GetMapping("/list")
    public ApiResponse<List<DefectiveDrug>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(defectiveService.listByDateRange(startDate, endDate));
    }

    @GetMapping("/statistics")
    public ApiResponse<DefectiveDrugService.DefectiveStatistics> statistics(
            @RequestParam(required = false) Long storeId) {
        return ApiResponse.success(defectiveService.getStatistics(storeId));
    }

    @Data
    public static class DisposeRequest {
        private String disposalMethod;
        private String remark;
        private String disposalHandlerName;
    }
}
