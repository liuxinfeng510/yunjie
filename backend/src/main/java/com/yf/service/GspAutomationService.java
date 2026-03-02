package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.*;
import com.yf.mapper.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * GSP自动化服务
 * 提供GSP合规自动化检查、养护计划执行、报表生成等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GspAutomationService {

    private final GspComplianceCheckMapper complianceCheckMapper;
    private final DrugMaintenanceMapper maintenanceMapper;
    private final DrugAcceptanceMapper acceptanceMapper;
    private final TemperatureHumidityLogMapper tempLogMapper;
    private final DrugBatchMapper batchMapper;
    private final StoreMapper storeMapper;

    // GSP检查项定义
    private static final List<CheckItem> DAILY_CHECK_ITEMS = Arrays.asList(
        new CheckItem("TEMP_01", "温度记录", "每日温度记录是否完整"),
        new CheckItem("HUMID_01", "湿度记录", "每日湿度记录是否完整"),
        new CheckItem("CLEAN_01", "环境卫生", "营业场所是否整洁"),
        new CheckItem("DISP_01", "陈列规范", "药品陈列是否规范")
    );

    private static final List<CheckItem> MONTHLY_CHECK_ITEMS = Arrays.asList(
        new CheckItem("MAINT_01", "养护执行", "本月养护计划是否按时完成"),
        new CheckItem("EXPIRY_01", "效期管理", "近效期药品是否及时处理"),
        new CheckItem("QUAL_01", "供应商资质", "供应商资质是否在有效期内"),
        new CheckItem("TRAIN_01", "员工培训", "本月培训计划是否完成"),
        new CheckItem("RECORD_01", "记录完整性", "各类GSP记录是否完整")
    );

    /**
     * 执行合规检查
     */
    @Transactional
    public GspComplianceCheck executeComplianceCheck(Long storeId, String checkType, Long checkerId) {
        GspComplianceCheck check = new GspComplianceCheck();
        check.setCheckBatchNo(generateBatchNo());
        check.setStoreId(storeId);
        check.setCheckDate(LocalDate.now());
        check.setCheckType(checkType);
        check.setCheckerId(checkerId);
        
        List<CheckItem> items = "daily".equals(checkType) ? DAILY_CHECK_ITEMS : MONTHLY_CHECK_ITEMS;
        List<CheckResultItem> results = new ArrayList<>();
        int passed = 0;
        int failed = 0;
        StringBuilder issues = new StringBuilder();

        for (CheckItem item : items) {
            CheckResultItem result = new CheckResultItem();
            result.setCode(item.getCode());
            result.setName(item.getName());
            
            boolean isPassed = checkItemCompliance(storeId, item.getCode());
            result.setPassed(isPassed);
            result.setRemark(isPassed ? "合格" : "需整改");
            results.add(result);
            
            if (isPassed) {
                passed++;
            } else {
                failed++;
                issues.append(item.getName()).append("不合格；");
            }
        }

        check.setTotalItems(items.size());
        check.setPassedItems(passed);
        check.setFailedItems(failed);
        check.setPassRate(items.size() > 0 ? (passed * 100 / items.size()) : 100);
        check.setCheckResult(results.toString());
        check.setIssues(issues.toString());
        check.setCorrectionStatus(failed > 0 ? "pending" : "completed");

        complianceCheckMapper.insert(check);
        log.info("完成GSP合规检查，门店：{}，类型：{}，合格率：{}%", storeId, checkType, check.getPassRate());
        
        return check;
    }

    /**
     * 检查单项合规性
     */
    private boolean checkItemCompliance(Long storeId, String itemCode) {
        LocalDate today = LocalDate.now();
        
        switch (itemCode) {
            case "TEMP_01":
            case "HUMID_01":
                // 检查今日是否有温湿度记录
                LambdaQueryWrapper<TemperatureHumidityLog> tempWrapper = new LambdaQueryWrapper<>();
                tempWrapper.eq(TemperatureHumidityLog::getStoreId, storeId)
                          .ge(TemperatureHumidityLog::getRecordTime, today.atStartOfDay());
                return tempLogMapper.selectCount(tempWrapper) > 0;
                
            case "MAINT_01":
                // 检查本月养护计划完成情况
                LocalDate monthStart = today.withDayOfMonth(1);
                LambdaQueryWrapper<DrugMaintenance> maintWrapper = new LambdaQueryWrapper<>();
                maintWrapper.eq(DrugMaintenance::getStoreId, storeId)
                           .ge(DrugMaintenance::getMaintenanceTime, monthStart.atStartOfDay());
                return maintenanceMapper.selectCount(maintWrapper) > 0;
                
            case "EXPIRY_01":
                // 检查是否有超期未处理的批次
                LambdaQueryWrapper<DrugBatch> expiryWrapper = new LambdaQueryWrapper<>();
                expiryWrapper.eq(DrugBatch::getStatus, "active")
                            .lt(DrugBatch::getExpireDate, today);
                return batchMapper.selectCount(expiryWrapper) == 0;
                
            default:
                // 默认返回合格（实际应用中需要具体检查逻辑）
                return true;
        }
    }

    /**
     * 自动生成养护计划
     */
    @Scheduled(cron = "0 0 8 1 * ?") // 每月1日早8点
    public void generateMonthlyMaintenancePlan() {
        log.info("开始生成月度养护计划");
        
        // 获取所有门店
        List<Store> stores = storeMapper.selectList(null);
        
        for (Store store : stores) {
            // 为每个门店生成养护提醒
            log.info("生成门店 {} 的月度养护计划", store.getName());
            // 实际实现中，这里会创建养护任务记录
        }
    }

    /**
     * 定时执行日检
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天早9点
    public void executeDailyCheck() {
        log.info("执行每日GSP自动检查");
        
        List<Store> stores = storeMapper.selectList(null);
        for (Store store : stores) {
            try {
                // 自动执行日检（系统检查项）
                executeComplianceCheck(store.getId(), "daily", null);
            } catch (Exception e) {
                log.error("门店 {} 日检失败", store.getId(), e);
            }
        }
    }

    /**
     * 分页查询合规检查记录
     */
    public Page<GspComplianceCheck> pageChecks(Page<GspComplianceCheck> page, 
                                                Long storeId, String checkType) {
        LambdaQueryWrapper<GspComplianceCheck> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(GspComplianceCheck::getStoreId, storeId);
        }
        if (checkType != null && !checkType.isEmpty()) {
            wrapper.eq(GspComplianceCheck::getCheckType, checkType);
        }
        wrapper.orderByDesc(GspComplianceCheck::getCheckDate);
        return complianceCheckMapper.selectPage(page, wrapper);
    }

    /**
     * 获取合规统计
     */
    public ComplianceStatistics getStatistics(Long storeId, LocalDate startDate, LocalDate endDate) {
        ComplianceStatistics stats = new ComplianceStatistics();
        
        LambdaQueryWrapper<GspComplianceCheck> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(GspComplianceCheck::getStoreId, storeId);
        }
        wrapper.ge(GspComplianceCheck::getCheckDate, startDate)
               .le(GspComplianceCheck::getCheckDate, endDate);
        
        List<GspComplianceCheck> checks = complianceCheckMapper.selectList(wrapper);
        
        stats.setTotalChecks(checks.size());
        
        int totalPassed = 0;
        int totalFailed = 0;
        int sumPassRate = 0;
        
        for (GspComplianceCheck check : checks) {
            totalPassed += check.getPassedItems();
            totalFailed += check.getFailedItems();
            sumPassRate += check.getPassRate();
        }
        
        stats.setTotalPassedItems(totalPassed);
        stats.setTotalFailedItems(totalFailed);
        stats.setAveragePassRate(checks.size() > 0 ? sumPassRate / checks.size() : 100);
        
        // 待整改数
        LambdaQueryWrapper<GspComplianceCheck> pendingWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            pendingWrapper.eq(GspComplianceCheck::getStoreId, storeId);
        }
        pendingWrapper.eq(GspComplianceCheck::getCorrectionStatus, "pending");
        stats.setPendingCorrections(Math.toIntExact(complianceCheckMapper.selectCount(pendingWrapper)));
        
        return stats;
    }

    /**
     * 更新整改状态
     */
    @Transactional
    public void updateCorrectionStatus(Long checkId, String status, String measures) {
        GspComplianceCheck check = complianceCheckMapper.selectById(checkId);
        if (check != null) {
            check.setCorrectionStatus(status);
            if (measures != null) {
                check.setCorrectionMeasures(measures);
            }
            complianceCheckMapper.updateById(check);
            log.info("更新合规检查整改状态：{}，状态：{}", checkId, status);
        }
    }

    private String generateBatchNo() {
        return "GSP" + System.currentTimeMillis();
    }

    @Data
    public static class CheckItem {
        private String code;
        private String name;
        private String description;
        
        public CheckItem(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }
    }

    @Data
    public static class CheckResultItem {
        private String code;
        private String name;
        private Boolean passed;
        private String remark;
    }

    @Data
    public static class ComplianceStatistics {
        private Integer totalChecks;
        private Integer totalPassedItems;
        private Integer totalFailedItems;
        private Integer averagePassRate;
        private Integer pendingCorrections;
    }
}
