package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.RefundOrder;
import com.yf.entity.RefundOrderDetail;
import com.yf.entity.SaleOrderDetail;
import com.yf.service.RefundOrderService;
import com.yf.service.SaleOrderService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundOrderController {

    private final RefundOrderService refundOrderService;
    private final SaleOrderService saleOrderService;

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

    /**
     * 获取退货明细
     */
    @GetMapping("/{id}/details")
    public ApiResponse<List<RefundOrderDetail>> getDetails(@PathVariable Long id) {
        List<RefundOrderDetail> details = refundOrderService.getDetails(id);
        return ApiResponse.success(details);
    }

    /**
     * 获取原订单明细（用于单品退货选择）
     */
    @GetMapping("/order/{saleOrderId}/details")
    public ApiResponse<List<SaleOrderDetail>> getOrderDetails(@PathVariable Long saleOrderId) {
        List<SaleOrderDetail> details = saleOrderService.getDetails(saleOrderId);
        return ApiResponse.success(details);
    }

    /**
     * 创建整单退货
     */
    @PostMapping
    public ApiResponse<RefundOrder> create(@RequestBody RefundOrder refundOrder) {
        RefundOrder created = refundOrderService.create(refundOrder);
        return ApiResponse.success(created);
    }

    /**
     * 创建单品退货
     */
    @PostMapping("/partial")
    public ApiResponse<RefundOrder> createPartial(@RequestBody Map<String, Object> request) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setSaleOrderId(Long.valueOf(request.get("saleOrderId").toString()));
        refundOrder.setReason((String) request.get("reason"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
        List<RefundOrderDetail> details = itemsData.stream().map(item -> {
            RefundOrderDetail detail = new RefundOrderDetail();
            detail.setSaleOrderDetailId(item.get("saleOrderDetailId") != null ? 
                    Long.valueOf(item.get("saleOrderDetailId").toString()) : null);
            detail.setDrugId(Long.valueOf(item.get("drugId").toString()));
            detail.setDrugName((String) item.get("drugName"));
            detail.setSpecification((String) item.get("specification"));
            detail.setBatchId(item.get("batchId") != null ? 
                    Long.valueOf(item.get("batchId").toString()) : null);
            detail.setBatchNo((String) item.get("batchNo"));
            detail.setQuantity(new java.math.BigDecimal(item.get("quantity").toString()));
            detail.setUnit((String) item.get("unit"));
            detail.setUnitPrice(new java.math.BigDecimal(item.get("unitPrice").toString()));
            return detail;
        }).toList();

        RefundOrder created = refundOrderService.createPartial(refundOrder, details);
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
