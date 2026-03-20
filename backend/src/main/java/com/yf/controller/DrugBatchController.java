package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.annotation.Auditable;
import com.yf.entity.DrugBatch;
import com.yf.service.DrugBatchService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 药品批次控制器
 */
@RestController
@RequestMapping("/drug-batch")
@RequiredArgsConstructor
public class DrugBatchController {

    private final DrugBatchService batchService;

    @GetMapping("/page")
    public ApiResponse<Page<Map<String, Object>>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String drugName,
            @RequestParam(required = false) String batchNo,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(batchService.pageWithDetail(drugId, supplierId, status, drugName, batchNo, current, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<DrugBatch> getById(@PathVariable Long id) {
        return ApiResponse.success(batchService.getById(id));
    }

    @GetMapping("/drug/{drugId}")
    public ApiResponse<List<DrugBatch>> getByDrug(@PathVariable Long drugId) {
        return ApiResponse.success(batchService.getByDrugId(drugId));
    }

    @GetMapping("/drug/{drugId}/valid")
    public ApiResponse<List<DrugBatch>> getValidBatches(@PathVariable Long drugId) {
        return ApiResponse.success(batchService.getValidBatches(drugId));
    }

    @PostMapping
    @Auditable(module = "批次管理", operation = "新增批次")
    public ApiResponse<DrugBatch> create(@RequestBody DrugBatch batch) {
        return ApiResponse.success(batchService.create(batch));
    }

    @PutMapping("/{id}")
    @Auditable(module = "批次管理", operation = "更新批次")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody DrugBatch batch) {
        batch.setId(id);
        batchService.update(batch);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @Auditable(module = "批次管理", operation = "更新批次状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        batchService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/lock")
    @Auditable(module = "批次管理", operation = "锁定批次")
    public ApiResponse<Void> lockBatch(@PathVariable Long id) {
        batchService.updateStatus(id, "locked");
        return ApiResponse.success();
    }

    @PutMapping("/{id}/unlock")
    @Auditable(module = "批次管理", operation = "解锁批次")
    public ApiResponse<Void> unlockBatch(@PathVariable Long id) {
        batchService.updateStatus(id, "active");
        return ApiResponse.success();
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        return ApiResponse.success(batchService.getStatistics());
    }

    @GetMapping("/near-expiry")
    public ApiResponse<List<DrugBatch>> getNearExpiry(
            @RequestParam(defaultValue = "90") int days) {
        return ApiResponse.success(batchService.getNearExpiry(days));
    }

    @GetMapping("/expired")
    public ApiResponse<List<DrugBatch>> getExpired() {
        return ApiResponse.success(batchService.getExpired());
    }
}
