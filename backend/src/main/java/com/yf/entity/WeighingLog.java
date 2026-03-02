package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 称重记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("weighing_log")
public class WeighingLog extends BaseEntity {

    private Long deviceId;

    private Long herbId;

    private String herbName;

    /** 称重值(g) */
    private BigDecimal weight;

    /** 操作类型: stock_in/stock_out/dispense/check */
    private String operationType;

    /** 识别方式: ai/manual */
    private String recognitionMethod;

    /** AI识别置信度 */
    private BigDecimal confidence;

    /** 操作人ID */
    private Long operatorId;

    /** 关联订单ID */
    private Long relatedOrderId;

    /** 关联处方ID */
    private Long prescriptionId;

    /** 关联处方明细ID */
    private Long prescriptionItemId;
}
