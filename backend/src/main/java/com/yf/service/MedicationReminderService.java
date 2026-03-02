package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Member;
import com.yf.entity.MedicationReminder;
import com.yf.mapper.MedicationReminderMapper;
import com.yf.mapper.MemberMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 用药提醒服务
 * 管理会员用药提醒，定时发送提醒通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationReminderService {

    private final MedicationReminderMapper reminderMapper;
    private final MemberMapper memberMapper;

    /**
     * 创建用药提醒
     */
    @Transactional
    public MedicationReminder createReminder(MedicationReminder reminder) {
        reminder.setStatus("active");
        reminderMapper.insert(reminder);
        log.info("创建用药提醒，会员ID：{}，药品：{}", reminder.getMemberId(), reminder.getDrugName());
        return reminder;
    }

    /**
     * 更新用药提醒
     */
    @Transactional
    public MedicationReminder updateReminder(MedicationReminder reminder) {
        reminderMapper.updateById(reminder);
        return reminder;
    }

    /**
     * 暂停提醒
     */
    @Transactional
    public void pauseReminder(Long reminderId) {
        MedicationReminder reminder = reminderMapper.selectById(reminderId);
        if (reminder != null) {
            reminder.setStatus("paused");
            reminderMapper.updateById(reminder);
            log.info("暂停用药提醒：{}", reminderId);
        }
    }

    /**
     * 恢复提醒
     */
    @Transactional
    public void resumeReminder(Long reminderId) {
        MedicationReminder reminder = reminderMapper.selectById(reminderId);
        if (reminder != null) {
            reminder.setStatus("active");
            reminderMapper.updateById(reminder);
            log.info("恢复用药提醒：{}", reminderId);
        }
    }

    /**
     * 完成提醒（结束用药）
     */
    @Transactional
    public void completeReminder(Long reminderId) {
        MedicationReminder reminder = reminderMapper.selectById(reminderId);
        if (reminder != null) {
            reminder.setStatus("completed");
            reminderMapper.updateById(reminder);
            log.info("完成用药提醒：{}", reminderId);
        }
    }

    /**
     * 获取会员的用药提醒列表
     */
    public List<MedicationReminder> getMemberReminders(Long memberId, String status) {
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getMemberId, memberId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MedicationReminder::getStatus, status);
        }
        wrapper.orderByDesc(MedicationReminder::getCreatedAt);
        return reminderMapper.selectList(wrapper);
    }

    /**
     * 分页查询提醒
     */
    public Page<MedicationReminder> pageReminders(Page<MedicationReminder> page, String status) {
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MedicationReminder::getStatus, status);
        }
        wrapper.orderByDesc(MedicationReminder::getCreatedAt);
        return reminderMapper.selectPage(page, wrapper);
    }

    /**
     * 获取当前需要发送的提醒
     */
    public List<PendingNotification> getPendingNotifications() {
        List<PendingNotification> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        String currentHour = currentTime.format(DateTimeFormatter.ofPattern("HH:00"));
        
        // 查询活跃的提醒
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getStatus, "active")
               .le(MedicationReminder::getStartDate, today)
               .and(w -> w.isNull(MedicationReminder::getEndDate)
                        .or().ge(MedicationReminder::getEndDate, today));
        
        List<MedicationReminder> reminders = reminderMapper.selectList(wrapper);
        
        for (MedicationReminder reminder : reminders) {
            // 检查今天是否已发送
            if (reminder.getLastNotifyDate() != null && reminder.getLastNotifyDate().equals(today)) {
                continue;
            }
            
            // 检查提醒时间（简化处理：检查JSON中是否包含当前小时）
            String times = reminder.getReminderTimes();
            if (times != null && times.contains(currentHour)) {
                PendingNotification notification = new PendingNotification();
                notification.setReminderId(reminder.getId());
                notification.setMemberId(reminder.getMemberId());
                notification.setDrugName(reminder.getDrugName());
                notification.setDosage(reminder.getDosage());
                notification.setUsageMethod(reminder.getUsageMethod());
                notification.setPrecautions(reminder.getPrecautions());
                notification.setNotifyMethod(reminder.getNotifyMethod());
                
                // 获取会员信息
                Member member = memberMapper.selectById(reminder.getMemberId());
                if (member != null) {
                    notification.setMemberName(member.getName());
                    notification.setPhone(member.getPhone());
                    notification.setWechatOpenid(member.getWechatOpenid());
                }
                
                notifications.add(notification);
            }
        }
        
        return notifications;
    }

    /**
     * 定时任务：每小时检查并发送用药提醒
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void sendRemindersTask() {
        log.info("开始执行用药提醒定时任务");
        
        List<PendingNotification> notifications = getPendingNotifications();
        
        for (PendingNotification notification : notifications) {
            try {
                // 发送提醒（根据通知方式）
                sendNotification(notification);
                
                // 更新最后提醒时间
                MedicationReminder reminder = reminderMapper.selectById(notification.getReminderId());
                if (reminder != null) {
                    reminder.setLastNotifyDate(LocalDate.now());
                    reminderMapper.updateById(reminder);
                }
            } catch (Exception e) {
                log.error("发送用药提醒失败，提醒ID：{}", notification.getReminderId(), e);
            }
        }
        
        log.info("用药提醒任务完成，共发送 {} 条提醒", notifications.size());
    }

    /**
     * 发送通知（预留接口，实际需对接短信/微信等渠道）
     */
    private void sendNotification(PendingNotification notification) {
        String method = notification.getNotifyMethod();
        
        // 构建消息内容
        String message = String.format("【用药提醒】%s，请按时服用%s，用量：%s。%s",
                notification.getMemberName(),
                notification.getDrugName(),
                notification.getDosage(),
                notification.getPrecautions() != null ? "注意：" + notification.getPrecautions() : "");
        
        switch (method) {
            case "sms":
                // TODO: 对接短信服务
                log.info("发送短信提醒到 {}：{}", notification.getPhone(), message);
                break;
            case "wechat":
                // TODO: 对接微信模板消息
                log.info("发送微信提醒到 {}：{}", notification.getWechatOpenid(), message);
                break;
            case "app":
                // TODO: 对接APP推送
                log.info("发送APP推送到会员 {}：{}", notification.getMemberId(), message);
                break;
            case "all":
                log.info("发送全渠道提醒：{}", message);
                break;
            default:
                log.info("模拟发送提醒：{}", message);
        }
    }

    /**
     * 检查并自动完成已结束的提醒
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredReminders() {
        log.info("检查过期的用药提醒");
        
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getStatus, "active")
               .lt(MedicationReminder::getEndDate, LocalDate.now());
        
        List<MedicationReminder> expiredReminders = reminderMapper.selectList(wrapper);
        
        for (MedicationReminder reminder : expiredReminders) {
            reminder.setStatus("completed");
            reminderMapper.updateById(reminder);
            log.info("自动完成过期提醒：{}", reminder.getId());
        }
        
        log.info("过期提醒处理完成，共处理 {} 条", expiredReminders.size());
    }

    /**
     * 获取提醒统计
     */
    public ReminderStatistics getStatistics() {
        ReminderStatistics stats = new ReminderStatistics();
        
        stats.setTotalReminders(Math.toIntExact(reminderMapper.selectCount(null)));
        
        LambdaQueryWrapper<MedicationReminder> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(MedicationReminder::getStatus, "active");
        stats.setActiveReminders(Math.toIntExact(reminderMapper.selectCount(activeWrapper)));
        
        LambdaQueryWrapper<MedicationReminder> pausedWrapper = new LambdaQueryWrapper<>();
        pausedWrapper.eq(MedicationReminder::getStatus, "paused");
        stats.setPausedReminders(Math.toIntExact(reminderMapper.selectCount(pausedWrapper)));
        
        stats.setTodayPending(getPendingNotifications().size());
        
        return stats;
    }

    @Data
    public static class PendingNotification {
        private Long reminderId;
        private Long memberId;
        private String memberName;
        private String phone;
        private String wechatOpenid;
        private String drugName;
        private String dosage;
        private String usageMethod;
        private String precautions;
        private String notifyMethod;
    }

    @Data
    public static class ReminderStatistics {
        private Integer totalReminders;
        private Integer activeReminders;
        private Integer pausedReminders;
        private Integer todayPending;
    }
}
