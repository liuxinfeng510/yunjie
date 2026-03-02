package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.MedicationReminder;
import com.yf.service.MedicationReminderService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用药提醒控制器
 */
@RestController
@RequestMapping("/medication-reminder")
@RequiredArgsConstructor
public class MedicationReminderController {

    private final MedicationReminderService reminderService;

    /**
     * 创建用药提醒
     */
    @PostMapping
    public ApiResponse<MedicationReminder> create(@RequestBody MedicationReminder reminder) {
        MedicationReminder created = reminderService.createReminder(reminder);
        return ApiResponse.success(created);
    }

    /**
     * 更新用药提醒
     */
    @PutMapping
    public ApiResponse<MedicationReminder> update(@RequestBody MedicationReminder reminder) {
        MedicationReminder updated = reminderService.updateReminder(reminder);
        return ApiResponse.success(updated);
    }

    /**
     * 暂停提醒
     */
    @PostMapping("/{id}/pause")
    public ApiResponse<Void> pause(@PathVariable Long id) {
        reminderService.pauseReminder(id);
        return ApiResponse.success(null);
    }

    /**
     * 恢复提醒
     */
    @PostMapping("/{id}/resume")
    public ApiResponse<Void> resume(@PathVariable Long id) {
        reminderService.resumeReminder(id);
        return ApiResponse.success(null);
    }

    /**
     * 完成提醒
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        reminderService.completeReminder(id);
        return ApiResponse.success(null);
    }

    /**
     * 获取会员的用药提醒
     */
    @GetMapping("/member/{memberId}")
    public ApiResponse<List<MedicationReminder>> getMemberReminders(
            @PathVariable Long memberId,
            @RequestParam(required = false) String status) {
        List<MedicationReminder> reminders = reminderService.getMemberReminders(memberId, status);
        return ApiResponse.success(reminders);
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<MedicationReminder>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<MedicationReminder> page = new Page<>(current, size);
        Page<MedicationReminder> result = reminderService.pageReminders(page, status);
        return ApiResponse.success(result);
    }

    /**
     * 获取待发送的提醒
     */
    @GetMapping("/pending")
    public ApiResponse<List<MedicationReminderService.PendingNotification>> getPending() {
        List<MedicationReminderService.PendingNotification> notifications = 
            reminderService.getPendingNotifications();
        return ApiResponse.success(notifications);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<MedicationReminderService.ReminderStatistics> getStatistics() {
        MedicationReminderService.ReminderStatistics stats = reminderService.getStatistics();
        return ApiResponse.success(stats);
    }

    /**
     * 手动触发提醒发送
     */
    @PostMapping("/send-now")
    public ApiResponse<Void> sendNow() {
        reminderService.sendRemindersTask();
        return ApiResponse.success(null);
    }
}
