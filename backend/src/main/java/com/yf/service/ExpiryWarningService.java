package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.Drug;
import com.yf.entity.DrugBatch;
import com.yf.entity.Inventory;
import com.yf.mapper.DrugBatchMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 效期预警服务
 * 定时检查近效期和已过期药品，生成预警信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryWarningService {

    private final DrugBatchMapper batchMapper;
    private final DrugMapper drugMapper;
    private final InventoryMapper inventoryMapper;

    /** 近效期预警天数（默认90天） */
    private static final int NEAR_EXPIRY_DAYS = 90;
    /** 紧急预警天数（默认30天） */
    private static final int URGENT_EXPIRY_DAYS = 30;

    /**
     * 获取效期预警汇总
     */
    public ExpiryWarningSummary getWarningSummary(Long storeId) {
        ExpiryWarningSummary summary = new ExpiryWarningSummary();

        // 已过期
        List<DrugBatch> expiredBatches = getExpiredBatches();
        summary.setExpiredCount(expiredBatches.size());
        summary.setExpiredBatches(buildWarningItems(expiredBatches, "expired", storeId));

        // 紧急（30天内）
        List<DrugBatch> urgentBatches = getNearExpiryBatches(URGENT_EXPIRY_DAYS);
        summary.setUrgentCount(urgentBatches.size());
        summary.setUrgentBatches(buildWarningItems(urgentBatches, "urgent", storeId));

        // 近效期（90天内，排除紧急）
        List<DrugBatch> nearExpiryBatches = getNearExpiryBatches(NEAR_EXPIRY_DAYS);
        nearExpiryBatches.removeIf(b -> urgentBatches.stream()
                .anyMatch(u -> u.getId().equals(b.getId())));
        summary.setNearExpiryCount(nearExpiryBatches.size());
        summary.setNearExpiryBatches(buildWarningItems(nearExpiryBatches, "near_expiry", storeId));

        return summary;
    }

    /**
     * 获取近效期批次
     */
    public List<DrugBatch> getNearExpiryBatches(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getStatus, "active")
               .le(DrugBatch::getExpireDate, deadline)
               .gt(DrugBatch::getExpireDate, LocalDate.now())
               .orderByAsc(DrugBatch::getExpireDate);
        return batchMapper.selectList(wrapper);
    }

    /**
     * 获取已过期批次
     */
    public List<DrugBatch> getExpiredBatches() {
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getStatus, "active")
               .le(DrugBatch::getExpireDate, LocalDate.now())
               .orderByAsc(DrugBatch::getExpireDate);
        return batchMapper.selectList(wrapper);
    }

    /**
     * 构建预警条目
     */
    private List<ExpiryWarningItem> buildWarningItems(List<DrugBatch> batches, String level, Long storeId) {
        List<ExpiryWarningItem> items = new ArrayList<>();
        for (DrugBatch batch : batches) {
            ExpiryWarningItem item = new ExpiryWarningItem();
            item.setBatchId(batch.getId());
            item.setBatchNo(batch.getBatchNo());
            item.setDrugId(batch.getDrugId());
            item.setExpireDate(batch.getExpireDate());
            item.setLevel(level);

            // 计算剩余天数
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpireDate());
            item.setDaysRemaining((int) daysRemaining);

            // 获取商品名称
            Drug drug = drugMapper.selectById(batch.getDrugId());
            if (drug != null) {
                item.setDrugName(drug.getGenericName());
                item.setSpecification(drug.getSpecification());
            }

            // 获取库存数量
            if (storeId != null) {
                LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
                invWrapper.eq(Inventory::getStoreId, storeId)
                         .eq(Inventory::getDrugId, batch.getDrugId())
                         .eq(Inventory::getBatchId, batch.getId());
                Inventory inv = inventoryMapper.selectOne(invWrapper);
                if (inv != null) {
                    item.setStockQuantity(inv.getQuantity());
                }
            }

            items.add(item);
        }
        return items;
    }

    /**
     * 定时任务：每天凌晨检查效期并自动标记过期批次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiryDaily() {
        log.info("开始执行效期检查定时任务");

        // 将已过期的批次状态改为expired
        List<DrugBatch> expiredBatches = getExpiredBatches();
        for (DrugBatch batch : expiredBatches) {
            batch.setStatus("expired");
            batchMapper.updateById(batch);
            log.info("批次 {} 已过期，状态已更新", batch.getBatchNo());
        }

        log.info("效期检查完成，共处理 {} 个过期批次", expiredBatches.size());
    }

    @Data
    public static class ExpiryWarningSummary {
        private int expiredCount;
        private int urgentCount;
        private int nearExpiryCount;
        private List<ExpiryWarningItem> expiredBatches;
        private List<ExpiryWarningItem> urgentBatches;
        private List<ExpiryWarningItem> nearExpiryBatches;
    }

    @Data
    public static class ExpiryWarningItem {
        private Long batchId;
        private String batchNo;
        private Long drugId;
        private String drugName;
        private String specification;
        private LocalDate expireDate;
        private int daysRemaining;
        private BigDecimal stockQuantity;
        private String level; // expired/urgent/near_expiry
    }
}
