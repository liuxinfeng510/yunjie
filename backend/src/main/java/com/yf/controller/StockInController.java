package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.StockIn;
import com.yf.entity.StockInDetail;
import com.yf.service.StockInService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入库单控制器
 */
@RestController
@RequestMapping("/stock-in")
@RequiredArgsConstructor
public class StockInController {
    
    private final StockInService stockInService;
    
    /**
     * 分页查询入库单
     */
    @GetMapping("/page")
    public ApiResponse<Page<StockIn>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<StockIn> result = stockInService.page(storeId, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取入库单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        StockIn stockIn = stockInService.getById(id);
        List<StockInDetail> details = stockInService.getDetails(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("stockIn", stockIn);
        result.put("details", details);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建入库单
     */
    @PostMapping("/")
    public ApiResponse<StockIn> create(@RequestBody StockInRequest request) {
        StockIn stockIn = stockInService.create(request.getStockIn(), request.getDetails());
        return ApiResponse.success(stockIn);
    }
    
    /**
     * 审核入库单
     */
    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long approvedBy = request.get("approvedBy");
        stockInService.approve(id, approvedBy);
        return ApiResponse.success();
    }
    
    /**
     * 完成入库（更新库存）
     */
    @PutMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        stockInService.complete(id);
        return ApiResponse.success();
    }
    
    @Data
    public static class StockInRequest {
        private StockIn stockIn;
        private List<StockInDetail> details;
    }
}
