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
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 兼容 current/size 和 pageNum/pageSize 两种分页参数
        int actualPage = current != null && current > 1 ? current : pageNum;
        int actualSize = size != null && size > 1 ? size : pageSize;
        
        // 兼容 startDate/endDate 和 startTime/endTime 两种日期参数
        if (startTime == null && startDate != null && !startDate.isEmpty()) {
            startTime = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endTime == null && endDate != null && !endDate.isEmpty()) {
            endTime = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        }
        
        Page<SaleOrder> result = saleOrderService.pageWithDetails(
                storeId, status, orderNo, memberName, paymentMethod,
                startTime, endTime, actualPage, actualSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取销售订单详情（包含明细和关联名称）
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        SaleOrder saleOrder = saleOrderService.getByIdWithDetails(id);
        List<SaleOrderDetail> details = saleOrderService.getDetails(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("saleOrder", saleOrder);
        result.put("details", details);
        result.put("items", details);
        result.put("orderNo", saleOrder.getOrderNo());
        result.put("memberName", saleOrder.getMemberName());
        result.put("totalAmount", saleOrder.getTotalAmount());
        result.put("discountAmount", saleOrder.getDiscountAmount());
        result.put("payAmount", saleOrder.getPayAmount());
        result.put("paymentMethod", saleOrder.getPaymentMethod());
        result.put("cashierName", saleOrder.getCashierName());
        result.put("createTime", saleOrder.getCreateTime());
        result.put("status", saleOrder.getStatus());
        result.put("remark", saleOrder.getRemark());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建销售订单
     */
    @PostMapping
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
