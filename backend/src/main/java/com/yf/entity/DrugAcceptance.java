package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品验收记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_acceptance")
public class DrugAcceptance extends BaseEntity {
    
    /**
     * 入库单ID
     */
    private Long stockInId;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 药品名称
     */
    private String drugName;
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 验收数量
     */
    private BigDecimal quantity;
    
    /**
     * 外观检查（合格/不合格）
     */
    private String appearanceCheck;
    
    /**
     * 包装检查（合格/不合格）
     */
    private String packageCheck;
    
    /**
     * 标签检查（合格/不合格）
     */
    private String labelCheck;
    
    /**
     * 效期检查（合格/不合格）
     */
    private String expireCheck;
    
    /**
     * 综合结果（合格/不合格）
     */
    private String overallResult;
    
    /**
     * 不合格原因
     */
    private String rejectReason;
    
    /**
     * 验收图片（多张图片URL，逗号分隔）
     */
    private String images;
    
    /**
     * 验收人ID
     */
    private Long acceptorId;
    
    /**
     * 验收时间
     */
    private LocalDateTime acceptTime;
    
    /**
     * 签名图片
     */
    private String signImage;
}
