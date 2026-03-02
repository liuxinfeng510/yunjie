package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.OutOfStockRequest;
import com.yf.service.OutOfStockService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 缺药登记控制器
 */
@RestController
@RequestMapping("/out-of-stock")
@RequiredArgsConstructor
public class OutOfStockController {

    private final OutOfStockService outOfStockService;

    /**
     * 创建缺药登记
     */
    @PostMapping
    public ApiResponse<OutOfStockRequest> create(@RequestBody OutOfStockRequest request) {
        return ApiResponse.success(outOfStockService.create(request));
    }

    /**
     * 开始处理
     */
    @PostMapping("/{id}/process")
    public ApiResponse<Void> startProcess(@PathVariable Long id, @RequestBody ProcessRequest req) {
        outOfStockService.startProcess(id, req.getHandlerId(), req.getHandlerName());
        return ApiResponse.success(null);
    }

    /**
     * 完成处理
     */
    @PostMapping("/{id}/resolve")
    public ApiResponse<Void> resolve(@PathVariable Long id, @RequestBody ResolveRequest req) {
        outOfStockService.resolve(id, req.getHandleResult());
        return ApiResponse.success(null);
    }

    /**
     * 取消登记
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestBody CancelRequest req) {
        outOfStockService.cancel(id, req.getReason());
        return ApiResponse.success(null);
    }

    /**
     * 标记已通知客户
     */
    @PostMapping("/{id}/notify")
    public ApiResponse<Void> markNotified(@PathVariable Long id) {
        outOfStockService.markNotified(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public ApiResponse<OutOfStockRequest> getById(@PathVariable Long id) {
        return ApiResponse.success(outOfStockService.getById(id));
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<OutOfStockRequest>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(outOfStockService.page(storeId, status, keyword, pageNum, pageSize));
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<OutOfStockService.OutOfStockStatistics> getStatistics(
            @RequestParam(required = false) Long storeId) {
        return ApiResponse.success(outOfStockService.getStatistics(storeId));
    }

    @Data
    public static class ProcessRequest {
        private Long handlerId;
        private String handlerName;
    }

    @Data
    public static class ResolveRequest {
        private String handleResult;
    }

    @Data
    public static class CancelRequest {
        private String reason;
    }
}
