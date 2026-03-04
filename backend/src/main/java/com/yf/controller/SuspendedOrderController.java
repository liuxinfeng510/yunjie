package com.yf.controller;

import com.yf.entity.SuspendedOrder;
import com.yf.service.SuspendedOrderService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 挂单控制器
 */
@RestController
@RequestMapping("/suspended-order")
@RequiredArgsConstructor
public class SuspendedOrderController {

    private final SuspendedOrderService suspendedOrderService;

    /**
     * 创建挂单
     */
    @PostMapping
    public ApiResponse<SuspendedOrder> create(@RequestBody SuspendedOrder order) {
        SuspendedOrder result = suspendedOrderService.create(order);
        return ApiResponse.success(result);
    }

    /**
     * 获取有效挂单列表
     */
    @GetMapping("/list")
    public ApiResponse<List<SuspendedOrder>> list(@RequestParam(required = false) Long storeId) {
        List<SuspendedOrder> list = suspendedOrderService.listActive(storeId);
        return ApiResponse.success(list);
    }

    /**
     * 获取挂单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SuspendedOrder> getById(@PathVariable Long id) {
        SuspendedOrder order = suspendedOrderService.getById(id);
        if (order == null) {
            return ApiResponse.error("挂单不存在");
        }
        return ApiResponse.success(order);
    }

    /**
     * 取单
     */
    @PutMapping("/{id}/retrieve")
    public ApiResponse<SuspendedOrder> retrieve(@PathVariable Long id) {
        try {
            SuspendedOrder order = suspendedOrderService.retrieve(id);
            return ApiResponse.success(order);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除挂单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        suspendedOrderService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 获取挂单过期时间配置
     */
    @GetMapping("/config/expire-minutes")
    public ApiResponse<Integer> getExpireMinutes() {
        int minutes = suspendedOrderService.getExpireMinutes();
        return ApiResponse.success(minutes);
    }

    /**
     * 更新挂单过期时间配置
     */
    @PutMapping("/config/expire-minutes")
    public ApiResponse<Void> updateExpireMinutes(@RequestBody Map<String, Integer> request) {
        Integer minutes = request.get("minutes");
        if (minutes == null || minutes < 1) {
            return ApiResponse.error("过期时间必须大于0");
        }
        suspendedOrderService.updateExpireMinutes(minutes);
        return ApiResponse.success();
    }
}
