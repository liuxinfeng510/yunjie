package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.ChronicDiseaseRecord;
import com.yf.entity.MedicationReminder;
import com.yf.service.ChronicDiseaseService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 慢病管理控制器
 */
@RestController
@RequestMapping("/chronic-disease")
@RequiredArgsConstructor
public class ChronicDiseaseController {

    private final ChronicDiseaseService chronicDiseaseService;

    /**
     * 创建慢病记录
     */
    @PostMapping
    public ApiResponse<ChronicDiseaseRecord> create(@RequestBody ChronicDiseaseRecord record) {
        ChronicDiseaseRecord created = chronicDiseaseService.createRecord(record);
        return ApiResponse.success(created);
    }

    /**
     * 更新慢病记录
     */
    @PutMapping
    public ApiResponse<ChronicDiseaseRecord> update(@RequestBody ChronicDiseaseRecord record) {
        ChronicDiseaseRecord updated = chronicDiseaseService.updateRecord(record);
        return ApiResponse.success(updated);
    }

    /**
     * 获取会员慢病列表
     */
    @GetMapping("/member/{memberId}")
    public ApiResponse<List<ChronicDiseaseRecord>> getMemberDiseases(@PathVariable Long memberId) {
        List<ChronicDiseaseRecord> records = chronicDiseaseService.getMemberDiseases(memberId);
        return ApiResponse.success(records);
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<ChronicDiseaseRecord>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String diseaseType,
            @RequestParam(required = false) String managementStatus) {
        Page<ChronicDiseaseRecord> page = new Page<>(current, size);
        Page<ChronicDiseaseRecord> result = chronicDiseaseService.pageRecords(page, diseaseType, managementStatus);
        return ApiResponse.success(result);
    }

    /**
     * 获取待复查列表
     */
    @GetMapping("/pending-followups")
    public ApiResponse<List<ChronicDiseaseService.FollowUpReminder>> getPendingFollowUps(
            @RequestParam(defaultValue = "7") Integer days) {
        List<ChronicDiseaseService.FollowUpReminder> reminders = 
            chronicDiseaseService.getPendingFollowUps(days);
        return ApiResponse.success(reminders);
    }

    /**
     * 记录检查结果
     */
    @PostMapping("/{recordId}/check-result")
    public ApiResponse<ChronicDiseaseRecord> recordCheckResult(
            @PathVariable Long recordId,
            @RequestBody CheckResultRequest request) {
        ChronicDiseaseRecord record = chronicDiseaseService.recordCheckResult(
            recordId, 
            request.getCheckResult(), 
            request.getNextCheckDate(),
            request.getDoctorAdvice()
        );
        return ApiResponse.success(record);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<ChronicDiseaseService.DiseaseStatistics> getStatistics() {
        ChronicDiseaseService.DiseaseStatistics stats = chronicDiseaseService.getStatistics();
        return ApiResponse.success(stats);
    }

    /**
     * 为慢病记录创建用药提醒
     */
    @PostMapping("/{recordId}/reminder")
    public ApiResponse<MedicationReminder> createReminder(
            @PathVariable Long recordId,
            @RequestBody MedicationReminder reminder) {
        MedicationReminder created = chronicDiseaseService.createReminderFromDisease(recordId, reminder);
        return ApiResponse.success(created);
    }

    @Data
    public static class CheckResultRequest {
        private String checkResult;
        private LocalDate nextCheckDate;
        private String doctorAdvice;
    }
}
