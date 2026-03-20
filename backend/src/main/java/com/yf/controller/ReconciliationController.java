package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.dto.ReconciliationSubmitDTO;
import com.yf.entity.Reconciliation;
import com.yf.service.ReconciliationService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 对账单控制器
 */
@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    /**
     * 预览对账数据（根据日期和门店汇总当日销售）
     */
    @GetMapping("/preview")
    public ApiResponse<Map<String, Object>> preview(
            @RequestParam(required = false) Long storeId,
            @RequestParam String reconcileDate,
            @RequestParam(required = false) Long cashierId) {
        return ApiResponse.success(reconciliationService.preview(storeId, reconcileDate, cashierId));
    }

    /**
     * 提交对账单
     */
    @PostMapping
    public ApiResponse<Reconciliation> submit(@RequestBody ReconciliationSubmitDTO dto) {
        return ApiResponse.success(reconciliationService.submit(dto));
    }

    /**
     * 分页查询对账记录
     */
    @GetMapping("/page")
    public ApiResponse<Page<Reconciliation>> page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(reconciliationService.pageList(storeId, cashierId, status, startDate, endDate, current, size));
    }

    /**
     * 获取对账单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> detail = reconciliationService.getDetail(id);
        if (detail == null) {
            return ApiResponse.error("对账单不存在");
        }
        return ApiResponse.success(detail);
    }
}
