package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药品批次实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_batch")
public class DrugBatch extends BaseEntity {

    /**
     * 药品ID
     */
    private Long drugId;

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
     * 采购价
     */
    private BigDecimal purchasePrice;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 状态
     */
    private String status;
}
