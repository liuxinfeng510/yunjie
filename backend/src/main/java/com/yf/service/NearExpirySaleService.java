package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.entity.DrugBatch;
import com.yf.entity.Inventory;
import com.yf.entity.NearExpirySaleRecord;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import com.yf.mapper.NearExpirySaleRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NearExpirySaleService {

    private final NearExpirySaleRecordMapper recordMapper;
    private final ExpiryWarningService expiryWarningService;
    private final DrugMapper drugMapper;
    private final InventoryMapper inventoryMapper;
    private final SysConfigService sysConfigService;

    /**
     * 生成月度催销表
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateMonthly(String month) {
        List<DrugBatch> batches = expiryWarningService.getNearExpiryBatches(180);
        int count = 0;
        for (DrugBatch batch : batches) {
            // 去重：同 batch_id + report_month 不重复插入
            LambdaQueryWrapper<NearExpirySaleRecord> dup = new LambdaQueryWrapper<>();
            dup.eq(NearExpirySaleRecord::getBatchId, batch.getId())
               .eq(NearExpirySaleRecord::getReportMonth, month);
            if (recordMapper.selectCount(dup) > 0) {
                continue;
            }

            Drug drug = drugMapper.selectById(batch.getDrugId());
            if (drug == null) continue;

            NearExpirySaleRecord record = new NearExpirySaleRecord();
            record.setReportMonth(month);
            record.setDrugId(batch.getDrugId());
            record.setDrugName(drug.getGenericName());
            record.setSpecification(drug.getSpecification());
            record.setManufacturer(drug.getManufacturer());
            record.setBatchId(batch.getId());
            record.setBatchNo(batch.getBatchNo());
            record.setExpireDate(batch.getExpireDate());
            record.setRemainingDays((int) ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpireDate()));
            record.setUnit(drug.getUnit());
            record.setSaleMeasure("accelerate");
            record.setStatus("processing");

            // 查库存数量（同一药品可能多条库存记录，汇总）
            LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
            invWrapper.eq(Inventory::getDrugId, batch.getDrugId());
            List<Inventory> invList = inventoryMapper.selectList(invWrapper);
            BigDecimal totalQty = invList.stream()
                    .map(Inventory::getQuantity)
                    .filter(q -> q != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            record.setStockQuantity(totalQty);

            recordMapper.insert(record);
            count++;
        }
        return count;
    }

    public Page<NearExpirySaleRecord> page(String month, String status, int pageNum, int pageSize) {
        Page<NearExpirySaleRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NearExpirySaleRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(month)) {
            wrapper.eq(NearExpirySaleRecord::getReportMonth, month);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(NearExpirySaleRecord::getStatus, status);
        }
        wrapper.orderByAsc(NearExpirySaleRecord::getRemainingDays);
        return recordMapper.selectPage(page, wrapper);
    }

    public List<NearExpirySaleRecord> listByMonth(String month) {
        LambdaQueryWrapper<NearExpirySaleRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(month)) {
            wrapper.eq(NearExpirySaleRecord::getReportMonth, month);
        }
        wrapper.orderByAsc(NearExpirySaleRecord::getRemainingDays);
        return recordMapper.selectList(wrapper);
    }

    public void updateMeasure(Long id, String saleMeasure, String measureDetail) {
        NearExpirySaleRecord record = recordMapper.selectById(id);
        if (record == null) return;
        record.setSaleMeasure(saleMeasure);
        record.setMeasureDetail(measureDetail);
        record.setStatus("processing");
        recordMapper.updateById(record);
    }

    public void complete(Long id, String resultRemark) {
        NearExpirySaleRecord record = recordMapper.selectById(id);
        if (record == null) return;
        record.setStatus("completed");
        record.setResultRemark(resultRemark);
        record.setCompleteTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    /**
     * 定时任务：每天早8点检查是否需要自动生成催销表
     * 根据 sys_config 中 near_expiry_auto_gen_day 配置的日期执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledAutoGenerate() {
        try {
            String dayStr = sysConfigService.getValue("near_expiry_auto_gen_day", "0");
            int autoGenDay = Integer.parseInt(dayStr);
            if (autoGenDay <= 0) {
                return;
            }

            int today = LocalDate.now().getDayOfMonth();
            if (today != autoGenDay) {
                return;
            }

            String currentMonth = LocalDate.now().getYear() + "-"
                    + String.format("%02d", LocalDate.now().getMonthValue());

            log.info("定时自动生成近效期催销表，月份: {}", currentMonth);
            int count = generateMonthly(currentMonth);
            log.info("自动生成近效期催销表完成，新增 {} 条记录", count);
        } catch (Exception e) {
            log.error("定时自动生成近效期催销表失败", e);
        }
    }
}
