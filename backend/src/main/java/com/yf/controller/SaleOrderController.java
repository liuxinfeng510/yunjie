package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.SaleOrder;
import com.yf.entity.SaleOrderDetail;
import com.yf.service.SaleOrderService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售订单控制器
 */
@RestController
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleOrderController {
    
    private final SaleOrderService saleOrderService;
    
    /**
     * 分页查询销售订单
     */
    @GetMapping("/page")
    public ApiResponse<Page<SaleOrder>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<SaleOrder> result = saleOrderService.page(storeId, status, startTime, endTime, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取销售订单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        SaleOrder saleOrder = saleOrderService.getById(id);
        List<SaleOrderDetail> details = saleOrderService.getDetails(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("saleOrder", saleOrder);
        result.put("details", details);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建销售订单
     */
    @PostMapping("/")
    public ApiResponse<SaleOrder> create(@RequestBody SaleOrderRequest request) {
        SaleOrder saleOrder = saleOrderService.create(request.getSaleOrder(), request.getDetails());
        return ApiResponse.success(saleOrder);
    }
    
    /**
     * 每日销售统计
     */
    @GetMapping("/daily-stats")
    public ApiResponse<Map<String, Object>> dailyStats(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Map<String, Object> stats = saleOrderService.dailyStats(storeId, date);
        return ApiResponse.success(stats);
    }
    
    @Data
    public static class SaleOrderRequest {
        private SaleOrder saleOrder;
        private List<SaleOrderDetail> details;
    }
}
