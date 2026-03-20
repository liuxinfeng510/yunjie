package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 对账单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reconciliation")
public class Reconciliation extends BaseEntity {

    /** 对账单号 */
    private String reconciliationNo;

    /** 门店ID */
    private Long storeId;

    /** 收银员ID */
    private Long cashierId;

    /** 对账日期 */
    private LocalDate reconcileDate;

    /** 订单数量 */
    private Integer orderCount;

    /** 系统总额 */
    private BigDecimal systemTotal;

    /** 实际总额 */
    private BigDecimal actualTotal;

    /** 差额 */
    private BigDecimal difference;

    /** 状态：balanced/surplus/shortage */
    private String status;

    /** 备注 */
    private String remark;

    // ========== 非数据库字段 ==========

    /** 收银员名称 */
    @TableField(exist = false)
    private String cashierName;

    /** 门店名称 */
    @TableField(exist = false)
    private String storeName;
}
