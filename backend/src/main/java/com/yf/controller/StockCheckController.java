package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.StockCheck;
import com.yf.entity.StockCheckDetail;
import com.yf.service.StockCheckService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock-check")
@RequiredArgsConstructor
public class StockCheckController {

    private final StockCheckService stockCheckService;

    @GetMapping("/page")
    public ApiResponse<Page<StockCheck>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<StockCheck> result = stockCheckService.page(storeId, type, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        StockCheck stockCheck = stockCheckService.getById(id);
        List<StockCheckDetail> details = stockCheckService.getDetails(id);

        Map<String, Object> result = new HashMap<>();
        result.put("stockCheck", stockCheck);
        result.put("details", details);

        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<StockCheck> create(@RequestBody StockCheck stockCheck) {
        StockCheck created = stockCheckService.create(stockCheck);
        return ApiResponse.success(created);
    }

    @PutMapping("/detail/{detailId}")
    public ApiResponse<Void> updateDetail(
            @PathVariable Long detailId,
            @RequestParam BigDecimal actualQuantity,
            @RequestParam(required = false) String diffReason) {
        stockCheckService.updateDetail(detailId, actualQuantity, diffReason);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        stockCheckService.complete(id);
        return ApiResponse.success();
    }
}
