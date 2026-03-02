package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 不良品记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("defective_drug")
public class DefectiveDrug extends BaseEntity {
    
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
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 批号
     */
    private String batchNo;
    
    /**
     * 不良原因：expired/damaged/quality/recall/other
     */
    private String defectReason;
    
    /**
     * 不良描述
     */
    private String defectDescription;
    
    /**
     * 数量
     */
    private BigDecimal quantity;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 发现日期
     */
    private LocalDate discoveryDate;
    
    /**
     * 发现人ID
     */
    private Long discovererId;
    
    /**
     * 发现人姓名
     */
    private String discovererName;
    
    /**
     * 图片（多张，逗号分隔）
     */
    private String images;
    
    /**
     * 处置方式：pending/return/destroy/other
     */
    private String disposalMethod;
    
    /**
     * 处置状态：locked/processing/completed
     */
    private String status;
    
    /**
     * 关联销毁记录ID
     */
    private Long destructionId;
    
    /**
     * 备注
     */
    private String remark;
}
