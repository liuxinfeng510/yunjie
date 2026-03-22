package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbCleanLog;
import com.yf.service.HerbCleanLogService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/herb-clean-log")
@RequiredArgsConstructor
public class HerbCleanLogController {

    private final HerbCleanLogService herbCleanLogService;

    @GetMapping("/page")
    public ApiResponse<Page<HerbCleanLog>> page(
            @RequestParam(required = false) String drugName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(herbCleanLogService.page(drugName, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/list")
    public ApiResponse<List<HerbCleanLog>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(herbCleanLogService.listByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        herbCleanLogService.updateReviewStatus(id, body.get("reviewStatus"));
        return ApiResponse.success();
    }
}
