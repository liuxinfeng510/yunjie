package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DefectiveDrug;
import com.yf.entity.DrugBatch;
import com.yf.entity.Inventory;
import com.yf.mapper.DefectiveDrugMapper;
import com.yf.mapper.DrugBatchMapper;
import com.yf.mapper.InventoryMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 不良品管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefectiveDrugService {

    private final DefectiveDrugMapper defectiveMapper;
    private final DrugBatchMapper batchMapper;
    private final InventoryMapper inventoryMapper;

    /**
     * 登记不良品
     */
    @Transactional
    public DefectiveDrug register(DefectiveDrug defective) {
        defective.setDiscoveryDate(LocalDate.now());
        defective.setStatus("locked");
        defectiveMapper.insert(defective);
        
        // 锁定库存（标记为不可销售）
        if (defective.getBatchId() != null) {
            DrugBatch batch = batchMapper.selectById(defective.getBatchId());
            if (batch != null) {
                batch.setStatus("locked");
                batchMapper.updateById(batch);
            }
        }
        
        log.info("登记不良品，药品：{}，原因：{}", defective.getDrugName(), defective.getDefectReason());
        return defective;
    }

    /**
     * 处置不良品
     */
    @Transactional
    public void dispose(Long id, String disposalMethod, String remark) {
        DefectiveDrug defective = defectiveMapper.selectById(id);
        if (defective == null) {
            throw new RuntimeException("不良品记录不存在");
        }
        
        defective.setDisposalMethod(disposalMethod);
        defective.setStatus("processing");
        defective.setRemark(remark);
        defectiveMapper.updateById(defective);
        
        log.info("处置不良品：{}，方式：{}", id, disposalMethod);
    }

    /**
     * 完成处置
     */
    @Transactional
    public void complete(Long id, Long destructionId) {
        DefectiveDrug defective = defectiveMapper.selectById(id);
        if (defective != null) {
            defective.setStatus("completed");
            defective.setDestructionId(destructionId);
            defectiveMapper.updateById(defective);
            log.info("完成不良品处置：{}", id);
        }
    }

    /**
     * 分页查询
     */
    public Page<DefectiveDrug> page(Page<DefectiveDrug> page, Long storeId, String status) {
        LambdaQueryWrapper<DefectiveDrug> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(DefectiveDrug::getStoreId, storeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DefectiveDrug::getStatus, status);
        }
        wrapper.orderByDesc(DefectiveDrug::getDiscoveryDate);
        return defectiveMapper.selectPage(page, wrapper);
    }

    /**
     * 获取待处理数量
     */
    public int getPendingCount(Long storeId) {
        LambdaQueryWrapper<DefectiveDrug> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(DefectiveDrug::getStoreId, storeId);
        }
        wrapper.eq(DefectiveDrug::getStatus, "locked");
        return Math.toIntExact(defectiveMapper.selectCount(wrapper));
    }

    /**
     * 获取统计
     */
    public DefectiveStatistics getStatistics(Long storeId) {
        DefectiveStatistics stats = new DefectiveStatistics();
        
        LambdaQueryWrapper<DefectiveDrug> baseWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            baseWrapper.eq(DefectiveDrug::getStoreId, storeId);
        }
        
        stats.setTotalCount(Math.toIntExact(defectiveMapper.selectCount(baseWrapper)));
        
        LambdaQueryWrapper<DefectiveDrug> lockedWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) lockedWrapper.eq(DefectiveDrug::getStoreId, storeId);
        lockedWrapper.eq(DefectiveDrug::getStatus, "locked");
        stats.setLockedCount(Math.toIntExact(defectiveMapper.selectCount(lockedWrapper)));
        
        LambdaQueryWrapper<DefectiveDrug> processingWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) processingWrapper.eq(DefectiveDrug::getStoreId, storeId);
        processingWrapper.eq(DefectiveDrug::getStatus, "processing");
        stats.setProcessingCount(Math.toIntExact(defectiveMapper.selectCount(processingWrapper)));
        
        LambdaQueryWrapper<DefectiveDrug> completedWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) completedWrapper.eq(DefectiveDrug::getStoreId, storeId);
        completedWrapper.eq(DefectiveDrug::getStatus, "completed");
        stats.setCompletedCount(Math.toIntExact(defectiveMapper.selectCount(completedWrapper)));
        
        return stats;
    }

    @Data
    public static class DefectiveStatistics {
        private Integer totalCount;
        private Integer lockedCount;
        private Integer processingCount;
        private Integer completedCount;
    }
}
