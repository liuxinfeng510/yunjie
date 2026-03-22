package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品养护记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_maintenance")
public class DrugMaintenance extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 商品名称
     */
    private String drugName;
    
    /**
     * 批号
     */
    private String batchNo;
    
    /**
     * 规格
     */
    private String specification;
    
    /**
     * 生产企业
     */
    private String manufacturer;
    
    /**
     * 是否重点养护品种
     */
    private Boolean isKeyDrug;
    
    /**
     * 养护类型（日常养护、重点养护、特殊养护）
     */
    private String maintenanceType;
    
    /**
     * 外观检查（正常/异常）
     */
    private String appearanceCheck;
    
    /**
     * 包装检查（正常/异常）
     */
    private String packageCheck;
    
    /**
     * 效期检查（正常/异常）
     */
    private String expireCheck;
    
    /**
     * 储存条件检查（正常/异常）
     */
    private String storageCheck;
    
    /**
     * 综合结果（正常/异常）
     */
    private String overallResult;
    
    /**
     * 异常描述
     */
    private String abnormalDesc;
    
    /**
     * 处理措施
     */
    private String treatment;
    
    /**
     * 养护图片（多张图片URL，逗号分隔）
     */
    private String images;
    
    /**
     * 养护人ID
     */
    private Long operatorId;
    
    /**
     * 养护人姓名（快照）
     */
    private String operatorName;
    
    /**
     * 养护时间
     */
    private LocalDateTime maintenanceTime;
    
    /**
     * 下次养护日期
     */
    private LocalDate nextMaintenance;
    
    /**
     * 签名图片
     */
    private String signImage;
}
