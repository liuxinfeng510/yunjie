package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.StaffTraining;
import com.yf.mapper.StaffTrainingMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工培训管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffTrainingService {

    private final StaffTrainingMapper trainingMapper;

    /**
     * 创建培训计划
     */
    @Transactional
    public StaffTraining create(StaffTraining training) {
        training.setStatus("planned");
        trainingMapper.insert(training);
        log.info("创建培训计划：{}", training.getTitle());
        return training;
    }

    /**
     * 更新培训
     */
    @Transactional
    public StaffTraining update(StaffTraining training) {
        trainingMapper.updateById(training);
        return training;
    }

    /**
     * 完成培训
     */
    @Transactional
    public void complete(Long id, String assessmentResults, String signInImage, String images) {
        StaffTraining training = trainingMapper.selectById(id);
        if (training != null) {
            training.setStatus("completed");
            training.setAssessmentResults(assessmentResults);
            training.setSignInImage(signInImage);
            training.setImages(images);
            trainingMapper.updateById(training);
            log.info("完成培训：{}", training.getTitle());
        }
    }

    /**
     * 取消培训
     */
    @Transactional
    public void cancel(Long id, String reason) {
        StaffTraining training = trainingMapper.selectById(id);
        if (training != null) {
            training.setStatus("cancelled");
            training.setRemark(reason);
            trainingMapper.updateById(training);
            log.info("取消培训：{}", training.getTitle());
        }
    }

    /**
     * 分页查询
     */
    public Page<StaffTraining> page(Page<StaffTraining> page, Long storeId, 
                                     String trainingType, String status) {
        LambdaQueryWrapper<StaffTraining> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(StaffTraining::getStoreId, storeId);
        }
        if (trainingType != null && !trainingType.isEmpty()) {
            wrapper.eq(StaffTraining::getTrainingType, trainingType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(StaffTraining::getStatus, status);
        }
        wrapper.orderByDesc(StaffTraining::getTrainingDate);
        return trainingMapper.selectPage(page, wrapper);
    }

    /**
     * 获取详情
     */
    public StaffTraining getById(Long id) {
        return trainingMapper.selectById(id);
    }

    /**
     * 获取近期培训计划
     */
    public List<StaffTraining> getUpcoming(Long storeId, int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<StaffTraining> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(StaffTraining::getStoreId, storeId);
        }
        wrapper.eq(StaffTraining::getStatus, "planned")
               .ge(StaffTraining::getTrainingDate, LocalDate.now())
               .le(StaffTraining::getTrainingDate, endDate)
               .orderByAsc(StaffTraining::getTrainingDate);
        return trainingMapper.selectList(wrapper);
    }

    /**
     * 获取培训统计
     */
    public TrainingStatistics getStatistics(Long storeId, LocalDate startDate, LocalDate endDate) {
        TrainingStatistics stats = new TrainingStatistics();
        
        LambdaQueryWrapper<StaffTraining> baseWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            baseWrapper.eq(StaffTraining::getStoreId, storeId);
        }
        if (startDate != null) {
            baseWrapper.ge(StaffTraining::getTrainingDate, startDate);
        }
        if (endDate != null) {
            baseWrapper.le(StaffTraining::getTrainingDate, endDate);
        }
        
        List<StaffTraining> all = trainingMapper.selectList(baseWrapper);
        stats.setTotalCount(all.size());
        
        stats.setCompletedCount((int) all.stream()
                .filter(t -> "completed".equals(t.getStatus())).count());
        stats.setPlannedCount((int) all.stream()
                .filter(t -> "planned".equals(t.getStatus())).count());
        stats.setTotalHours(all.stream()
                .filter(t -> "completed".equals(t.getStatus()))
                .mapToInt(t -> t.getDuration() != null ? t.getDuration() : 0)
                .sum());
        stats.setTotalAttendees(all.stream()
                .filter(t -> "completed".equals(t.getStatus()))
                .mapToInt(t -> t.getAttendeeCount() != null ? t.getAttendeeCount() : 0)
                .sum());
        
        return stats;
    }

    @Data
    public static class TrainingStatistics {
        private Integer totalCount;
        private Integer completedCount;
        private Integer plannedCount;
        private Integer totalHours;
        private Integer totalAttendees;
    }
}
