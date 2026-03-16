package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品追溯码实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_trace_code")
public class DrugTraceCode extends BaseEntity {

    /**
     * 追溯码
     */
    private String traceCode;

    /**
     * 药品ID
     */
    private Long drugId;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 生产日期
     */
    private LocalDate produceDate;

    /**
     * 失效日期
     */
    private LocalDate expireDate;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 采购订单ID
     */
    private Long purchaseOrderId;

    /**
     * 入库明细行ID
     */
    private Long stockInDetailId;

    /**
     * 销售订单ID
     */
    private Long saleOrderId;

    /**
     * 状态
     */
    private String status;

    /**
     * 追溯时间
     */
    private LocalDateTime traceTime;
}
