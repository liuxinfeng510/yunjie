package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 销售订单明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_order_detail")
public class SaleOrderDetail extends BaseEntity {
    
    /**
     * 销售订单ID
     */
    private Long saleOrderId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 药品名称
     */
    private String drugName;
    
    /**
     * 规格
     */
    private String specification;
    
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
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 折扣
     */
    private BigDecimal discount;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 成本价
     */
    private BigDecimal costPrice;
    
    /**
     * 追溯码
     */
    private String traceCode;
}
