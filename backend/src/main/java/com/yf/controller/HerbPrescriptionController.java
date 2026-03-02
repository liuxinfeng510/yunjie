package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbPrescription;
import com.yf.entity.HerbPrescriptionItem;
import com.yf.service.HerbPrescriptionService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 中药处方控制器
 */
@RestController
@RequestMapping("/herb-prescription")
@RequiredArgsConstructor
public class HerbPrescriptionController {
    
    private final HerbPrescriptionService prescriptionService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<HerbPrescription>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HerbPrescription> page = new Page<>(current, size);
        Page<HerbPrescription> result = prescriptionService.page(page);
        return ApiResponse.success(result);
    }
    
    /**
     * 根据ID查询（包含明细）
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = prescriptionService.getById(id);
        return ApiResponse.success(result);
    }
    
    /**
     * 创建处方（含明细）
     */
    @PostMapping
    public ApiResponse<Void> create(@RequestBody PrescriptionRequest request) {
        boolean success = prescriptionService.createWithItems(
                request.getPrescription(), 
                request.getItems()
        );
        return success ? ApiResponse.success() : ApiResponse.error("创建处方失败");
    }
    
    /**
     * 更新状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        boolean success = prescriptionService.updateStatus(id, status);
        return success ? ApiResponse.success() : ApiResponse.error("更新状态失败");
    }
    
    /**
     * 创建处方请求DTO
     */
    @Data
    public static class PrescriptionRequest {
        private HerbPrescription prescription;
        private List<HerbPrescriptionItem> items;
    }
}
