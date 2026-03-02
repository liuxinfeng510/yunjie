package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.RefundOrder;
import com.yf.service.RefundOrderService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundOrderController {

    private final RefundOrderService refundOrderService;

    @GetMapping("/page")
    public ApiResponse<Page<RefundOrder>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<RefundOrder> result = refundOrderService.page(storeId, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<RefundOrder> getById(@PathVariable Long id) {
        RefundOrder refundOrder = refundOrderService.getById(id);
        return ApiResponse.success(refundOrder);
    }

    @PostMapping("/")
    public ApiResponse<RefundOrder> create(@RequestBody RefundOrder refundOrder) {
        RefundOrder created = refundOrderService.create(refundOrder);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long approvedBy = request.get("approvedBy");
        refundOrderService.approve(id, approvedBy);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long approvedBy = request.get("approvedBy");
        refundOrderService.reject(id, approvedBy);
        return ApiResponse.success();
    }
}
