package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 出库单明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_out_detail")
public class StockOutDetail extends BaseEntity {
    
    /**
     * 出库单ID
     */
    private Long stockOutId;
    
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
     * 数量
     */
    private BigDecimal quantity;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 成本价
     */
    private BigDecimal costPrice;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 药品通用名（快照）
     */
    private String drugName;
    
    /**
     * 规格（快照）
     */
    private String specification;
    
    /**
     * 生产企业（快照）
     */
    private String manufacturer;
}
