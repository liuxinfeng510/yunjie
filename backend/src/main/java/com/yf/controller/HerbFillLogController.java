package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbFillLog;
import com.yf.service.HerbFillLogService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/herb-fill-log")
@RequiredArgsConstructor
public class HerbFillLogController {

    private final HerbFillLogService herbFillLogService;

    @GetMapping("/page")
    public ApiResponse<Page<HerbFillLog>> page(
            @RequestParam(required = false) String drugName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(herbFillLogService.page(drugName, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/list")
    public ApiResponse<List<HerbFillLog>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(herbFillLogService.listByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        herbFillLogService.updateStatus(id, body.get("qualityStatus"), body.get("acceptanceResult"));
        return ApiResponse.success();
    }
}
