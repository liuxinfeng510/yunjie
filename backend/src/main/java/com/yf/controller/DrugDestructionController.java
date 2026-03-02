package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugDestruction;
import com.yf.service.DrugDestructionService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/drug-destruction")
@RequiredArgsConstructor
public class DrugDestructionController {

    private final DrugDestructionService destructionService;

    @PostMapping
    public ApiResponse<DrugDestruction> create(@RequestBody DrugDestruction destruction) {
        return ApiResponse.success(destructionService.createApplication(destruction));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody ApproveRequest request) {
        destructionService.approve(id, request.getApproverId(), request.getApproverName(),
                request.getApproved(), request.getOpinion());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<Void> execute(@PathVariable Long id, @RequestBody ExecuteRequest request) {
        destructionService.execute(id, request.getExecutorId(), request.getExecutorName(),
                request.getSupervisorId(), request.getSupervisorName(), request.getImages());
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<DrugDestruction> getById(@PathVariable Long id) {
        return ApiResponse.success(destructionService.getById(id));
    }

    @GetMapping("/page")
    public ApiResponse<Page<DrugDestruction>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status) {
        Page<DrugDestruction> page = new Page<>(current, size);
        return ApiResponse.success(destructionService.page(page, storeId, status));
    }

    @GetMapping("/statistics")
    public ApiResponse<DrugDestructionService.DestructionStatistics> statistics(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(destructionService.getStatistics(storeId, startDate, endDate));
    }

    @Data
    public static class ApproveRequest {
        private Long approverId;
        private String approverName;
        private Boolean approved;
        private String opinion;
    }

    @Data
    public static class ExecuteRequest {
        private Long executorId;
        private String executorName;
        private Long supervisorId;
        private String supervisorName;
        private String images;
    }
}
