package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DefectiveDrug;
import com.yf.entity.Drug;
import com.yf.entity.DrugBatch;
import com.yf.entity.Inventory;
import com.yf.mapper.DefectiveDrugMapper;
import com.yf.mapper.DrugBatchMapper;
import com.yf.mapper.DrugMapper;
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
    private final DrugMapper drugMapper;

    /**
     * 登记不良品（自动生成台帐编号、填充药品快照）
     */
    @Transactional
    public DefectiveDrug register(DefectiveDrug defective) {
        defective.setDiscoveryDate(LocalDate.now());
        defective.setStatus("locked");

        // 自动生成台帐编号: BHG-yyyyMM-NNN
        String monthStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "BHG-" + monthStr + "-";
        LambdaQueryWrapper<DefectiveDrug> noWrapper = new LambdaQueryWrapper<>();
        noWrapper.likeRight(DefectiveDrug::getRegisterNo, prefix);
        noWrapper.orderByDesc(DefectiveDrug::getRegisterNo);
        noWrapper.last("LIMIT 1");
        DefectiveDrug last = defectiveMapper.selectOne(noWrapper);
        int seq = 1;
        if (last != null && last.getRegisterNo() != null) {
            String lastNo = last.getRegisterNo();
            String seqStr = lastNo.substring(prefix.length());
            try { seq = Integer.parseInt(seqStr) + 1; } catch (NumberFormatException ignored) {}
        }
        defective.setRegisterNo(prefix + String.format("%03d", seq));

        // 自动填充药品快照字段
        if (defective.getDrugId() != null) {
            Drug drug = drugMapper.selectById(defective.getDrugId());
            if (drug != null) {
                if (defective.getSpecification() == null) defective.setSpecification(drug.getSpecification());
                if (defective.getManufacturer() == null) defective.setManufacturer(drug.getManufacturer());
                if (defective.getDrugName() == null) defective.setDrugName(drug.getGenericName());
                if (defective.getUnit() == null) defective.setUnit(drug.getUnit());
            }
        }
        // 自动填充批次信息
        if (defective.getBatchId() != null) {
            DrugBatch batch = batchMapper.selectById(defective.getBatchId());
            if (batch != null) {
                if (defective.getExpireDate() == null) defective.setExpireDate(batch.getExpireDate());
                if (defective.getBatchNo() == null) defective.setBatchNo(batch.getBatchNo());
                // 锁定库存
                batch.setStatus("locked");
                batchMapper.updateById(batch);
            }
        }

        defectiveMapper.insert(defective);
        log.info("登记不良品，台帐编号：{}，药品：{}，原因：{}", defective.getRegisterNo(), defective.getDrugName(), defective.getDefectReason());
        return defective;
    }

    /**
     * 处置不良品（设置处置日期、处置人）
     */
    @Transactional
    public void dispose(Long id, String disposalMethod, String remark, String disposalHandlerName) {
        DefectiveDrug defective = defectiveMapper.selectById(id);
        if (defective == null) {
            throw new RuntimeException("不良品记录不存在");
        }
        
        defective.setDisposalMethod(disposalMethod);
        defective.setDisposalDate(LocalDate.now());
        defective.setDisposalHandlerName(disposalHandlerName);
        defective.setStatus("processing");
        defective.setRemark(remark);
        defectiveMapper.updateById(defective);
        
        log.info("处置不良品：{}，方式：{}，处置人：{}", id, disposalMethod, disposalHandlerName);
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
     * 分页查询（支持药品名称、日期范围筛选）
     */
    public Page<DefectiveDrug> page(Page<DefectiveDrug> page, Long storeId, String status,
                                     String drugName, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DefectiveDrug> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(DefectiveDrug::getStoreId, storeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DefectiveDrug::getStatus, status);
        }
        if (drugName != null && !drugName.isEmpty()) {
            wrapper.like(DefectiveDrug::getDrugName, drugName);
        }
        if (startDate != null) {
            wrapper.ge(DefectiveDrug::getDiscoveryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(DefectiveDrug::getDiscoveryDate, endDate);
        }
        wrapper.orderByDesc(DefectiveDrug::getDiscoveryDate);
        return defectiveMapper.selectPage(page, wrapper);
    }

    /**
     * 按日期范围查询（非分页，用于打印台帐）
     */
    public List<DefectiveDrug> listByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DefectiveDrug> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(DefectiveDrug::getDiscoveryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(DefectiveDrug::getDiscoveryDate, endDate);
        }
        wrapper.orderByDesc(DefectiveDrug::getDiscoveryDate);
        return defectiveMapper.selectList(wrapper);
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
