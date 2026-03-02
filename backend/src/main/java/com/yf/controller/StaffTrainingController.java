package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.StaffTraining;
import com.yf.service.StaffTrainingService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/staff-training")
@RequiredArgsConstructor
public class StaffTrainingController {

    private final StaffTrainingService trainingService;

    @PostMapping
    public ApiResponse<StaffTraining> create(@RequestBody StaffTraining training) {
        return ApiResponse.success(trainingService.create(training));
    }

    @PutMapping
    public ApiResponse<StaffTraining> update(@RequestBody StaffTraining training) {
        return ApiResponse.success(trainingService.update(training));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id, @RequestBody CompleteRequest request) {
        trainingService.complete(id, request.getAssessmentResults(), 
                request.getSignInImage(), request.getImages());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestParam String reason) {
        trainingService.cancel(id, reason);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<StaffTraining> getById(@PathVariable Long id) {
        return ApiResponse.success(trainingService.getById(id));
    }

    @GetMapping("/page")
    public ApiResponse<Page<StaffTraining>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String trainingType,
            @RequestParam(required = false) String status) {
        Page<StaffTraining> page = new Page<>(current, size);
        return ApiResponse.success(trainingService.page(page, storeId, trainingType, status));
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<StaffTraining>> getUpcoming(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "7") Integer days) {
        return ApiResponse.success(trainingService.getUpcoming(storeId, days));
    }

    @GetMapping("/statistics")
    public ApiResponse<StaffTrainingService.TrainingStatistics> statistics(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(trainingService.getStatistics(storeId, startDate, endDate));
    }

    @Data
    public static class CompleteRequest {
        private String assessmentResults;
        private String signInImage;
        private String images;
    }
}
