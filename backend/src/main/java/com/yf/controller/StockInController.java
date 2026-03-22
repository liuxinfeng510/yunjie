package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.dto.SmartStockInResult;
import com.yf.entity.StockIn;
import com.yf.entity.StockInDetail;
import com.yf.service.SmartStockInService;
import com.yf.service.SmartStockInService.FileItem;
import com.yf.service.StockInService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SmartStockInService smartStockInService;
    
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
        List<StockInDetail> details = stockInService.getDetailsWithTraceCodes(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("stockIn", stockIn);
        result.put("details", details);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建入库单
     */
    @PostMapping
    public ApiResponse<StockIn> create(@RequestBody StockInRequest request) {
        StockIn stockIn = stockInService.create(request.getStockIn(), request.getDetails());
        return ApiResponse.success(stockIn);
    }
    
    /**
     * 更新入库单（仅待审核状态可编辑）
     */
    @PutMapping("/{id}")
    public ApiResponse<StockIn> update(@PathVariable Long id, @RequestBody StockInRequest request) {
        StockIn stockIn = stockInService.update(id, request.getStockIn(), request.getDetails());
        return ApiResponse.success(stockIn);
    }

    /**
     * 审核入库单
     */
    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        Long approvedBy = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

    /**
     * 驳回入库单
     */
    @PutMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        stockInService.reject(id, reason);
        return ApiResponse.success();
    }

    /**
     * 智能入库 - 第一步：解析多张单据（只识别和匹配，不创建数据）
     */
    @PostMapping("/smart-parse")
    public ApiResponse<SmartStockInResult> smartParse(@RequestBody SmartParseRequest request) {
        SmartStockInResult result = smartStockInService.parseMultiple(request.getFiles());
        return ApiResponse.success(result);
    }

    /**
     * 智能入库 - 第二步：批量创建（用户确认后，创建供应商+药品+入库单）
     */
    @PostMapping("/smart-create")
    public ApiResponse<SmartStockInResult> smartCreate(@RequestBody SmartStockInResult parseResult) {
        SmartStockInResult result = smartStockInService.batchCreateOrders(parseResult);
        return ApiResponse.success(result);
    }
    
    @Data
    public static class StockInRequest {
        private StockIn stockIn;
        private List<StockInDetail> details;
    }

    @Data
    public static class SmartParseRequest {
        private List<FileItem> files;
    }
}
