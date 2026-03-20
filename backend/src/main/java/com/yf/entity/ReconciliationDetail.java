package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 对账明细实体（按支付方式拆分）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reconciliation_detail")
public class ReconciliationDetail extends BaseEntity {

    /** 对账单ID */
    private Long reconciliationId;

    /** 支付方式 */
    private String payMethod;

    /** 订单数量 */
    private Integer orderCount;

    /** 系统金额 */
    private BigDecimal systemAmount;

    /** 实际金额 */
    private BigDecimal actualAmount;

    /** 差额 */
    private BigDecimal difference;

    /** 备注 */
    private String remark;
}
