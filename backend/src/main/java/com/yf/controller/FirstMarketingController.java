package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.FirstMarketingDrug;
import com.yf.entity.FirstMarketingSupplier;
import com.yf.service.FirstMarketingDrugService;
import com.yf.service.FirstMarketingSupplierService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/first-marketing")
@RequiredArgsConstructor
public class FirstMarketingController {

    private final FirstMarketingSupplierService supplierService;
    private final FirstMarketingDrugService drugService;

    // ========== 首营企业 ==========

    @GetMapping("/supplier/page")
    public ApiResponse<Page<FirstMarketingSupplier>> supplierPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(supplierService.page(current, size, supplierName, status));
    }

    @GetMapping("/supplier/{id}")
    public ApiResponse<FirstMarketingSupplier> getSupplier(@PathVariable Long id) {
        return ApiResponse.success(supplierService.getById(id));
    }

    @PostMapping("/supplier")
    public ApiResponse<FirstMarketingSupplier> createSupplier(@RequestBody FirstMarketingSupplier entity) {
        return ApiResponse.success(supplierService.create(entity));
    }

    @PutMapping("/supplier/{id}")
    public ApiResponse<Void> updateSupplier(@PathVariable Long id, @RequestBody FirstMarketingSupplier entity) {
        supplierService.update(id, entity);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/supplier/{id}")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/supplier/{id}/submit")
    public ApiResponse<Void> submitSupplier(@PathVariable Long id) {
        supplierService.submit(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/supplier/{id}/approve-first")
    public ApiResponse<Void> approveSupplierFirst(@PathVariable Long id, @RequestBody ApproveRequest req) {
        supplierService.approveFirst(id, req.getApproverId(), req.getApproverName(), req.getApproved(), req.getOpinion());
        return ApiResponse.success(null);
    }

    @PostMapping("/supplier/{id}/approve-second")
    public ApiResponse<Void> approveSupplierSecond(@PathVariable Long id, @RequestBody ApproveRequest req) {
        supplierService.approveSecond(id, req.getApproverId(), req.getApproverName(), req.getApproved(), req.getOpinion());
        return ApiResponse.success(null);
    }

    @GetMapping("/supplier/approval-level")
    public ApiResponse<String> getApprovalLevel() {
        return ApiResponse.success(supplierService.getApprovalLevel());
    }

    // ========== 首营品种 ==========

    @GetMapping("/drug/page")
    public ApiResponse<Page<FirstMarketingDrug>> drugPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String genericName,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(drugService.page(current, size, genericName, supplierName, status));
    }

    @GetMapping("/drug/{id}")
    public ApiResponse<FirstMarketingDrug> getDrug(@PathVariable Long id) {
        return ApiResponse.success(drugService.getById(id));
    }

    @PostMapping("/drug")
    public ApiResponse<FirstMarketingDrug> createDrug(@RequestBody FirstMarketingDrug entity) {
        return ApiResponse.success(drugService.create(entity));
    }

    @PutMapping("/drug/{id}")
    public ApiResponse<Void> updateDrug(@PathVariable Long id, @RequestBody FirstMarketingDrug entity) {
        drugService.update(id, entity);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/drug/{id}")
    public ApiResponse<Void> deleteDrug(@PathVariable Long id) {
        drugService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/drug/{id}/submit")
    public ApiResponse<Void> submitDrug(@PathVariable Long id) {
        drugService.submit(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/drug/{id}/approve-first")
    public ApiResponse<Void> approveDrugFirst(@PathVariable Long id, @RequestBody ApproveRequest req) {
        drugService.approveFirst(id, req.getApproverId(), req.getApproverName(), req.getApproved(), req.getOpinion());
        return ApiResponse.success(null);
    }

    @PostMapping("/drug/{id}/approve-second")
    public ApiResponse<Void> approveDrugSecond(@PathVariable Long id, @RequestBody ApproveRequest req) {
        drugService.approveSecond(id, req.getApproverId(), req.getApproverName(), req.getApproved(), req.getOpinion());
        return ApiResponse.success(null);
    }

    @Data
    public static class ApproveRequest {
        private Long approverId;
        private String approverName;
        private Boolean approved;
        private String opinion;
    }
}
