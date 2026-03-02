package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DefectiveDrug;
import com.yf.entity.DrugDestruction;
import com.yf.mapper.DefectiveDrugMapper;
import com.yf.mapper.DrugDestructionMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品销毁管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugDestructionService {

    private final DrugDestructionMapper destructionMapper;
    private final DefectiveDrugMapper defectiveMapper;

    /**
     * 创建销毁申请
     */
    @Transactional
    public DrugDestruction createApplication(DrugDestruction destruction) {
        destruction.setDestructionNo(generateNo());
        destruction.setApplyTime(LocalDateTime.now());
        destruction.setStatus("pending");
        destructionMapper.insert(destruction);
        log.info("创建销毁申请：{}", destruction.getDestructionNo());
        return destruction;
    }

    /**
     * 审批销毁申请
     */
    @Transactional
    public void approve(Long id, Long approverId, String approverName, boolean approved, String opinion) {
        DrugDestruction destruction = destructionMapper.selectById(id);
        if (destruction == null) {
            throw new RuntimeException("销毁记录不存在");
        }
        
        destruction.setApproverId(approverId);
        destruction.setApproverName(approverName);
        destruction.setApproveTime(LocalDateTime.now());
        destruction.setApproveOpinion(opinion);
        destruction.setStatus(approved ? "approved" : "rejected");
        destructionMapper.updateById(destruction);
        
        log.info("审批销毁申请：{}，结果：{}", id, approved ? "通过" : "拒绝");
    }

    /**
     * 执行销毁
     */
    @Transactional
    public void execute(Long id, Long executorId, String executorName, 
                       Long supervisorId, String supervisorName, String images) {
        DrugDestruction destruction = destructionMapper.selectById(id);
        if (destruction == null || !"approved".equals(destruction.getStatus())) {
            throw new RuntimeException("销毁记录不存在或未审批");
        }
        
        destruction.setExecutorId(executorId);
        destruction.setExecutorName(executorName);
        destruction.setSupervisorId(supervisorId);
        destruction.setSupervisorName(supervisorName);
        destruction.setExecuteTime(LocalDateTime.now());
        destruction.setImages(images);
        destruction.setStatus("completed");
        destruction.setDestructionDate(LocalDate.now());
        destructionMapper.updateById(destruction);
        
        // 更新关联的不良品记录状态
        LambdaQueryWrapper<DefectiveDrug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DefectiveDrug::getDestructionId, id);
        List<DefectiveDrug> defectives = defectiveMapper.selectList(wrapper);
        for (DefectiveDrug defective : defectives) {
            defective.setStatus("completed");
            defectiveMapper.updateById(defective);
        }
        
        log.info("执行销毁：{}，执行人：{}，监督人：{}", id, executorName, supervisorName);
    }

    /**
     * 分页查询
     */
    public Page<DrugDestruction> page(Page<DrugDestruction> page, Long storeId, String status) {
        LambdaQueryWrapper<DrugDestruction> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(DrugDestruction::getStoreId, storeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DrugDestruction::getStatus, status);
        }
        wrapper.orderByDesc(DrugDestruction::getApplyTime);
        return destructionMapper.selectPage(page, wrapper);
    }

    /**
     * 获取详情
     */
    public DrugDestruction getById(Long id) {
        return destructionMapper.selectById(id);
    }

    /**
     * 获取统计
     */
    public DestructionStatistics getStatistics(Long storeId, LocalDate startDate, LocalDate endDate) {
        DestructionStatistics stats = new DestructionStatistics();
        
        LambdaQueryWrapper<DrugDestruction> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(DrugDestruction::getStoreId, storeId);
        }
        if (startDate != null) {
            wrapper.ge(DrugDestruction::getDestructionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(DrugDestruction::getDestructionDate, endDate);
        }
        wrapper.eq(DrugDestruction::getStatus, "completed");
        
        List<DrugDestruction> list = destructionMapper.selectList(wrapper);
        stats.setTotalCount(list.size());
        stats.setTotalAmount(list.stream()
                .map(d -> d.getTotalAmount() != null ? d.getTotalAmount().doubleValue() : 0.0)
                .reduce(0.0, Double::sum));
        
        return stats;
    }

    private String generateNo() {
        return "XH" + System.currentTimeMillis();
    }

    @Data
    public static class DestructionStatistics {
        private Integer totalCount;
        private Double totalAmount;
    }
}
