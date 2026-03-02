package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.StockOut;
import com.yf.entity.StockOutDetail;
import com.yf.service.StockOutService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock-out")
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutService stockOutService;

    @GetMapping("/page")
    public ApiResponse<Page<StockOut>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<StockOut> result = stockOutService.page(storeId, type, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        StockOut stockOut = stockOutService.getById(id);
        List<StockOutDetail> details = stockOutService.getDetails(id);

        Map<String, Object> result = new HashMap<>();
        result.put("stockOut", stockOut);
        result.put("details", details);

        return ApiResponse.success(result);
    }

    @PostMapping("/")
    public ApiResponse<StockOut> create(@RequestBody StockOutRequest request) {
        StockOut stockOut = stockOutService.create(request.getStockOut(), request.getDetails());
        return ApiResponse.success(stockOut);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        stockOutService.approve(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        stockOutService.complete(id);
        return ApiResponse.success();
    }

    @Data
    public static class StockOutRequest {
        private StockOut stockOut;
        private List<StockOutDetail> details;
    }
}
