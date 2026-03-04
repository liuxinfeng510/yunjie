package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_order")
public class RefundOrder extends BaseEntity {
    
    /**
     * 退款单号
     */
    private String refundNo;
    
    /**
     * 销售订单ID
     */
    private Long saleOrderId;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退货类型：full-整单退 partial-单品退
     */
    private String refundType;
    
    /**
     * 退款原因
     */
    private String reason;
    
    /**
     * 状态（待审核、已退款、已拒绝）
     */
    private String status;
    
    /**
     * 审核人ID
     */
    private Long approvedBy;
    
    /**
     * 审核时间
     */
    private LocalDateTime approvedAt;
}
