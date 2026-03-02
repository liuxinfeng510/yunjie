package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.annotation.Auditable;
import com.yf.entity.DrugBatch;
import com.yf.service.DrugBatchService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药品批次控制器
 */
@RestController
@RequestMapping("/drug-batch")
@RequiredArgsConstructor
public class DrugBatchController {

    private final DrugBatchService batchService;

    @GetMapping("/page")
    public ApiResponse<Page<DrugBatch>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(batchService.page(drugId, supplierId, status, pageNum, pageSize));
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
