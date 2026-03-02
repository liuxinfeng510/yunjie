package com.yf.service;

import com.yf.config.tenant.TenantContext;
import com.yf.dto.QuickSetupRequest;
import com.yf.entity.Store;
import com.yf.entity.Tenant;
import com.yf.exception.BusinessException;
import com.yf.mapper.StoreMapper;
import com.yf.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 快速配置服务 - 单体/连锁模式一键初始化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuickSetupService {

    private final TenantMapper tenantMapper;
    private final StoreMapper storeMapper;
    private final SysConfigService sysConfigService;

    /**
     * 执行快速配置
     */
    @Transactional
    public void setup(QuickSetupRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未找到租户信息");
        }

        String mode = request.getBusinessMode();
        if (!"single_store".equals(mode) && !"chain_store".equals(mode)) {
            throw new BusinessException("经营模式无效，请选择 single_store 或 chain_store");
        }

        log.info("开始快速配置，租户ID={}，模式={}", tenantId, mode);

        // 1. 更新租户信息
        updateTenant(tenantId, request);

        // 2. 初始化门店
        initStores(request);

        // 3. 初始化系统配置
        initSystemConfig(request);

        // 4. 初始化功能开关
        initFeatureConfig(request);

        log.info("快速配置完成，租户ID={}", tenantId);
    }

    /**
     * 更新租户基本信息
     */
    private void updateTenant(Long tenantId, QuickSetupRequest request) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        tenant.setName(request.getCompanyName());
        tenant.setCreditCode(request.getCreditCode());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setBusinessMode(request.getBusinessMode());
        tenantMapper.updateById(tenant);
    }

    /**
     * 初始化门店
     */
    private void initStores(QuickSetupRequest request) {
        QuickSetupRequest.StoreInfo mainInfo = request.getMainStore();
        boolean isChain = "chain_store".equals(request.getBusinessMode());

        // 创建主店/总部
        Store mainStore = new Store();
        mainStore.setName(mainInfo.getName());
        mainStore.setCode("HQ001");
        mainStore.setType(isChain ? "headquarter" : "headquarter");
        mainStore.setProvince(mainInfo.getProvince());
        mainStore.setCity(mainInfo.getCity());
        mainStore.setDistrict(mainInfo.getDistrict());
        mainStore.setAddress(mainInfo.getAddress());
        mainStore.setPhone(mainInfo.getPhone());
        mainStore.setLicenseNo(mainInfo.getLicenseNo());
        mainStore.setGspCertNo(mainInfo.getGspCertNo());
        mainStore.setHasWarehouse(mainInfo.getHasWarehouse() != null ? mainInfo.getHasWarehouse() : true);
        mainStore.setStatus("active");
        storeMapper.insert(mainStore);

        // 连锁模式：创建分店
        if (isChain && request.getBranchStores() != null) {
            List<QuickSetupRequest.StoreInfo> branches = request.getBranchStores();
            for (int i = 0; i < branches.size(); i++) {
                QuickSetupRequest.StoreInfo branchInfo = branches.get(i);
                Store branch = new Store();
                branch.setName(branchInfo.getName());
                branch.setCode(String.format("BR%03d", i + 1));
                branch.setType("store");
                branch.setProvince(branchInfo.getProvince());
                branch.setCity(branchInfo.getCity());
                branch.setDistrict(branchInfo.getDistrict());
                branch.setAddress(branchInfo.getAddress());
                branch.setPhone(branchInfo.getPhone());
                branch.setLicenseNo(branchInfo.getLicenseNo());
                branch.setGspCertNo(branchInfo.getGspCertNo());
                branch.setHasWarehouse(branchInfo.getHasWarehouse() != null ? branchInfo.getHasWarehouse() : false);
                branch.setStatus("active");
                storeMapper.insert(branch);
            }
        }
    }

    /**
     * 初始化系统配置
     */
    private void initSystemConfig(QuickSetupRequest request) {
        boolean isChain = "chain_store".equals(request.getBusinessMode());

        // 基础配置
        sysConfigService.setValue("basic", "system.business_mode", request.getBusinessMode(),
                "string", "经营模式");
        sysConfigService.setValue("basic", "system.company_name", request.getCompanyName(),
                "string", "企业名称");
        sysConfigService.setValue("basic", "system.setup_completed", "true",
                "boolean", "是否已完成初始化配置");

        // GSP配置
        sysConfigService.setValue("gsp", "gsp.acceptance_required", "true",
                "boolean", "入库是否需要验收");
        sysConfigService.setValue("gsp", "gsp.near_expiry_days", "180",
                "number", "近效期预警天数");
        sysConfigService.setValue("gsp", "gsp.temp_check_interval", "120",
                "number", "温湿度记录间隔（分钟）");
        sysConfigService.setValue("gsp", "gsp.maintenance_cycle", "30",
                "number", "养护周期（天）");

        // 库存配置
        sysConfigService.setValue("inventory", "inventory.low_stock_threshold", "10",
                "number", "库存不足预警阈值");
        sysConfigService.setValue("inventory", "inventory.auto_reorder", "false",
                "boolean", "是否自动生成补货单");
        sysConfigService.setValue("inventory", "inventory.batch_tracking", "true",
                "boolean", "是否启用批号追踪");

        // 销售配置
        sysConfigService.setValue("sale", "sale.allow_credit", "false",
                "boolean", "是否允许赊账");
        sysConfigService.setValue("sale", "sale.receipt_printer", "false",
                "boolean", "是否启用小票打印");
        sysConfigService.setValue("sale", "sale.member_points_ratio", "1",
                "number", "会员积分比例（消费1元=N积分）");

        // 连锁模式专属配置
        if (isChain) {
            sysConfigService.setValue("chain", "chain.central_purchase", "true",
                    "boolean", "是否启用总部统一采购");
            sysConfigService.setValue("chain", "chain.cross_transfer", "true",
                    "boolean", "是否允许跨店调拨");
            sysConfigService.setValue("chain", "chain.unified_pricing", "true",
                    "boolean", "是否统一定价");
            sysConfigService.setValue("chain", "chain.branch_stock_independent", "true",
                    "boolean", "分店库存是否独立管理");
            sysConfigService.setValue("chain", "chain.report_consolidation", "true",
                    "boolean", "是否合并报表");
        }
    }

    /**
     * 初始化功能开关
     */
    private void initFeatureConfig(QuickSetupRequest request) {
        QuickSetupRequest.FeatureConfig features = request.getFeatures();
        boolean isChain = "chain_store".equals(request.getBusinessMode());

        if (features == null) {
            sysConfigService.initFeatureDefaults(isChain);
            return;
        }

        sysConfigService.setValue("feature", "feature.herb",
                String.valueOf(features.getEnableHerb()), "boolean", "中药管理");
        sysConfigService.setValue("feature", "feature.member",
                String.valueOf(features.getEnableMember()), "boolean", "会员管理");
        sysConfigService.setValue("feature", "feature.gsp",
                String.valueOf(features.getEnableGsp()), "boolean", "GSP管理");
        sysConfigService.setValue("feature", "feature.prescription",
                String.valueOf(features.getEnablePrescription()), "boolean", "处方管理");
        sysConfigService.setValue("feature", "feature.scale",
                String.valueOf(features.getEnableScale()), "boolean", "电子秤");
        sysConfigService.setValue("feature", "feature.temp_humidity",
                String.valueOf(features.getEnableTempHumidity()), "boolean", "温湿度监控");
        sysConfigService.setValue("feature", "feature.analytics",
                String.valueOf(features.getEnableAnalytics()), "boolean", "数据分析");
        sysConfigService.setValue("feature", "feature.central_purchase",
                String.valueOf(isChain && Boolean.TRUE.equals(features.getEnableCentralPurchase())),
                "boolean", "总部统一采购");
        sysConfigService.setValue("feature", "feature.cross_transfer",
                String.valueOf(isChain && Boolean.TRUE.equals(features.getEnableCrossTransfer())),
                "boolean", "跨店调拨");
    }
}
