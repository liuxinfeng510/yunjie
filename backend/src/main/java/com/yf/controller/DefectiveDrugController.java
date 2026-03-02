package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DefectiveDrug;
import com.yf.service.DefectiveDrugService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        defectiveService.dispose(id, request.getDisposalMethod(), request.getRemark());
        return ApiResponse.success(null);
    }

    @GetMapping("/page")
    public ApiResponse<Page<DefectiveDrug>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status) {
        Page<DefectiveDrug> page = new Page<>(current, size);
        return ApiResponse.success(defectiveService.page(page, storeId, status));
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
    }
}
