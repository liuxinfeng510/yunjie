package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.ChronicDiseaseRecord;
import com.yf.entity.Member;
import com.yf.entity.MedicationReminder;
import com.yf.mapper.ChronicDiseaseRecordMapper;
import com.yf.mapper.MemberMapper;
import com.yf.mapper.MedicationReminderMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 慢病管理服务
 * 管理会员慢性病记录，提供慢病随访和复查提醒
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChronicDiseaseService {

    private final ChronicDiseaseRecordMapper diseaseMapper;
    private final MemberMapper memberMapper;
    private final MedicationReminderMapper reminderMapper;

    /**
     * 创建慢病记录
     */
    @Transactional
    public ChronicDiseaseRecord createRecord(ChronicDiseaseRecord record) {
        record.setManagementStatus("active");
        diseaseMapper.insert(record);
        log.info("创建慢病记录，会员ID：{}，疾病：{}", record.getMemberId(), record.getDiseaseName());
        return record;
    }

    /**
     * 更新慢病记录
     */
    @Transactional
    public ChronicDiseaseRecord updateRecord(ChronicDiseaseRecord record) {
        diseaseMapper.updateById(record);
        return record;
    }

    /**
     * 获取会员的慢病记录列表
     */
    public List<ChronicDiseaseRecord> getMemberDiseases(Long memberId) {
        LambdaQueryWrapper<ChronicDiseaseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChronicDiseaseRecord::getMemberId, memberId)
               .orderByDesc(ChronicDiseaseRecord::getCreatedAt);
        return diseaseMapper.selectList(wrapper);
    }

    /**
     * 分页查询慢病记录
     */
    public Page<ChronicDiseaseRecord> pageRecords(Page<ChronicDiseaseRecord> page, 
                                                   String diseaseType, 
                                                   String managementStatus) {
        LambdaQueryWrapper<ChronicDiseaseRecord> wrapper = new LambdaQueryWrapper<>();
        if (diseaseType != null && !diseaseType.isEmpty()) {
            wrapper.eq(ChronicDiseaseRecord::getDiseaseType, diseaseType);
        }
        if (managementStatus != null && !managementStatus.isEmpty()) {
            wrapper.eq(ChronicDiseaseRecord::getManagementStatus, managementStatus);
        }
        wrapper.orderByDesc(ChronicDiseaseRecord::getCreatedAt);
        return diseaseMapper.selectPage(page, wrapper);
    }

    /**
     * 获取需要复查的记录（7天内需要复查）
     */
    public List<FollowUpReminder> getPendingFollowUps(int days) {
        List<FollowUpReminder> reminders = new ArrayList<>();
        LocalDate deadline = LocalDate.now().plusDays(days);
        
        LambdaQueryWrapper<ChronicDiseaseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChronicDiseaseRecord::getManagementStatus, "active")
               .le(ChronicDiseaseRecord::getNextCheckDate, deadline)
               .ge(ChronicDiseaseRecord::getNextCheckDate, LocalDate.now())
               .orderByAsc(ChronicDiseaseRecord::getNextCheckDate);
        
        List<ChronicDiseaseRecord> records = diseaseMapper.selectList(wrapper);
        
        for (ChronicDiseaseRecord record : records) {
            FollowUpReminder reminder = new FollowUpReminder();
            reminder.setRecordId(record.getId());
            reminder.setMemberId(record.getMemberId());
            reminder.setDiseaseName(record.getDiseaseName());
            reminder.setNextCheckDate(record.getNextCheckDate());
            
            // 获取会员信息
            Member member = memberMapper.selectById(record.getMemberId());
            if (member != null) {
                reminder.setMemberName(member.getName());
                reminder.setPhone(member.getPhone());
            }
            
            // 计算剩余天数
            long daysRemaining = record.getNextCheckDate().toEpochDay() - LocalDate.now().toEpochDay();
            reminder.setDaysRemaining((int) daysRemaining);
            
            reminders.add(reminder);
        }
        
        return reminders;
    }

    /**
     * 记录检查结果
     */
    @Transactional
    public ChronicDiseaseRecord recordCheckResult(Long recordId, String checkResult, 
                                                   LocalDate nextCheckDate, String doctorAdvice) {
        ChronicDiseaseRecord record = diseaseMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("慢病记录不存在");
        }
        
        record.setLastCheckDate(LocalDate.now());
        record.setLastCheckResult(checkResult);
        record.setNextCheckDate(nextCheckDate);
        if (doctorAdvice != null) {
            record.setDoctorAdvice(doctorAdvice);
        }
        
        diseaseMapper.updateById(record);
        log.info("记录检查结果，记录ID：{}", recordId);
        return record;
    }

    /**
     * 获取慢病统计
     */
    public DiseaseStatistics getStatistics() {
        DiseaseStatistics stats = new DiseaseStatistics();
        
        // 总记录数
        stats.setTotalRecords(Math.toIntExact(diseaseMapper.selectCount(null)));
        
        // 活跃记录数
        LambdaQueryWrapper<ChronicDiseaseRecord> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(ChronicDiseaseRecord::getManagementStatus, "active");
        stats.setActiveRecords(Math.toIntExact(diseaseMapper.selectCount(activeWrapper)));
        
        // 各类型统计
        stats.setDiabetesCount(countByType("diabetes"));
        stats.setHypertensionCount(countByType("hypertension"));
        stats.setHyperlipidemiaCount(countByType("hyperlipidemia"));
        stats.setCoronaryHeartCount(countByType("coronary_heart"));
        stats.setAsthmaCount(countByType("asthma"));
        
        // 待复查数（7天内）
        stats.setPendingFollowUps(getPendingFollowUps(7).size());
        
        return stats;
    }

    private int countByType(String type) {
        LambdaQueryWrapper<ChronicDiseaseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChronicDiseaseRecord::getDiseaseType, type)
               .eq(ChronicDiseaseRecord::getManagementStatus, "active");
        return Math.toIntExact(diseaseMapper.selectCount(wrapper));
    }

    /**
     * 根据慢病记录创建用药提醒
     */
    @Transactional
    public MedicationReminder createReminderFromDisease(Long diseaseRecordId, 
                                                         MedicationReminder reminder) {
        ChronicDiseaseRecord record = diseaseMapper.selectById(diseaseRecordId);
        if (record == null) {
            throw new RuntimeException("慢病记录不存在");
        }
        
        reminder.setMemberId(record.getMemberId());
        reminder.setDiseaseRecordId(diseaseRecordId);
        reminder.setStatus("active");
        reminderMapper.insert(reminder);
        
        log.info("创建用药提醒，关联慢病记录：{}", diseaseRecordId);
        return reminder;
    }

    @Data
    public static class FollowUpReminder {
        private Long recordId;
        private Long memberId;
        private String memberName;
        private String phone;
        private String diseaseName;
        private LocalDate nextCheckDate;
        private Integer daysRemaining;
    }

    @Data
    public static class DiseaseStatistics {
        private Integer totalRecords;
        private Integer activeRecords;
        private Integer diabetesCount;
        private Integer hypertensionCount;
        private Integer hyperlipidemiaCount;
        private Integer coronaryHeartCount;
        private Integer asthmaCount;
        private Integer pendingFollowUps;
    }
}
