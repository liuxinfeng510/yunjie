package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 退货明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_order_detail")
public class RefundOrderDetail extends BaseEntity {
    
    /**
     * 退款订单ID
     */
    private Long refundOrderId;
    
    /**
     * 原销售明细ID
     */
    private Long saleOrderDetailId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 商品名称
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
     * 批号
     */
    private String batchNo;
    
    /**
     * 退货数量
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
     * 退款金额
     */
    private BigDecimal refundAmount;
}
