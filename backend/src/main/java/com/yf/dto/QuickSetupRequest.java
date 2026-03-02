package com.yf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 快速配置请求 - 单体/连锁模式一键初始化
 */
@Data
public class QuickSetupRequest {

    /** 经营模式: single_store / chain_store */
    @NotBlank(message = "经营模式不能为空")
    private String businessMode;

    /** 企业/药店名称 */
    @NotBlank(message = "企业名称不能为空")
    private String companyName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 总店信息（连锁模式下为总部） */
    @NotNull(message = "门店信息不能为空")
    private StoreInfo mainStore;

    /** 分店列表（仅连锁模式） */
    private List<StoreInfo> branchStores;

    /** 功能开关配置 */
    private FeatureConfig features;

    @Data
    public static class StoreInfo {
        @NotBlank(message = "门店名称不能为空")
        private String name;
        private String province;
        private String city;
        private String district;
        private String address;
        private String phone;
        private String licenseNo;
        private String gspCertNo;
        private Boolean hasWarehouse;
    }

    @Data
    public static class FeatureConfig {
        /** 是否启用中药管理 */
        private Boolean enableHerb = true;
        /** 是否启用会员管理 */
        private Boolean enableMember = true;
        /** 是否启用GSP管理 */
        private Boolean enableGsp = true;
        /** 是否启用处方管理 */
        private Boolean enablePrescription = true;
        /** 是否启用电子秤 */
        private Boolean enableScale = false;
        /** 是否启用温湿度监控 */
        private Boolean enableTempHumidity = true;
        /** 是否启用数据分析 */
        private Boolean enableAnalytics = true;
        /** 是否启用总部统一采购（连锁模式） */
        private Boolean enableCentralPurchase = false;
        /** 是否启用跨店调拨（连锁模式） */
        private Boolean enableCrossTransfer = false;
    }
}
