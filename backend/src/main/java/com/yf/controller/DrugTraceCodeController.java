package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.annotation.Auditable;
import com.yf.entity.DrugTraceCode;
import com.yf.service.DrugTraceCodeService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 药品追溯码控制器
 */
@RestController
@RequestMapping("/trace-code")
@RequiredArgsConstructor
public class DrugTraceCodeController {

    private final DrugTraceCodeService traceCodeService;

    @GetMapping("/page")
    public ApiResponse<Page<DrugTraceCode>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(traceCodeService.page(drugId, batchNo, status, pageNum, pageSize));
    }

    @GetMapping("/query")
    public ApiResponse<DrugTraceCode> query(@RequestParam String traceCode) {
        DrugTraceCode tc = traceCodeService.getByTraceCode(traceCode);
        if (tc == null) {
            return ApiResponse.error("追溯码不存在");
        }
        return ApiResponse.success(tc);
    }

    @GetMapping("/trace")
    public ApiResponse<DrugTraceCodeService.TraceInfo> trace(@RequestParam String traceCode) {
        DrugTraceCodeService.TraceInfo info = traceCodeService.trace(traceCode);
        if (info == null) {
            return ApiResponse.error("追溯码不存在");
        }
        return ApiResponse.success(info);
    }

    @PostMapping
    @Auditable(module = "追溯码", operation = "新增追溯码")
    public ApiResponse<DrugTraceCode> create(@RequestBody DrugTraceCode traceCode) {
        return ApiResponse.success(traceCodeService.create(traceCode));
    }

    @PostMapping("/batch")
    @Auditable(module = "追溯码", operation = "批量新增追溯码")
    public ApiResponse<Void> batchCreate(@RequestBody List<DrugTraceCode> traceCodes) {
        traceCodeService.batchCreate(traceCodes);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @Auditable(module = "追溯码", operation = "更新追溯码状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        traceCodeService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PostMapping("/bind-sale")
    @Auditable(module = "追溯码", operation = "绑定销售订单")
    public ApiResponse<Void> bindSaleOrder(@RequestBody Map<String, Object> request) {
        String traceCode = (String) request.get("traceCode");
        Long saleOrderId = Long.valueOf(request.get("saleOrderId").toString());
        traceCodeService.bindSaleOrder(traceCode, saleOrderId);
        return ApiResponse.success();
    }

    @GetMapping("/drug/{drugId}")
    public ApiResponse<List<DrugTraceCode>> getByDrug(@PathVariable Long drugId) {
        return ApiResponse.success(traceCodeService.getByDrugId(drugId));
    }

    @GetMapping("/batch/{batchId}")
    public ApiResponse<List<DrugTraceCode>> getByBatch(@PathVariable Long batchId) {
        return ApiResponse.success(traceCodeService.getByBatchId(batchId));
    }
}
