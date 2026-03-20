package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 销售订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_order")
public class SaleOrder extends BaseEntity {
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 支付方式（现金、微信、支付宝、刷卡等）
     */
    private String payMethod;
    
    /**
     * 获得积分
     */
    private Integer pointsEarned;
    
    /**
     * 使用积分
     */
    private Integer pointsUsed;
    
    /**
     * 收银员ID
     */
    private Long cashierId;
    
    /**
     * 驻店药师ID
     */
    private Long pharmacistId;
    
    /**
     * 状态（待支付、已完成、已退款）
     */
    private String status;
    
    /**
     * 备注
     */
    private String remark;

    /**
     * 中药副数
     */
    private Integer herbDoseCount;

    // ========== 非数据库字段 ==========

    /** 会员名称 */
    @TableField(exist = false)
    private String memberName;

    /** 收银员名称 */
    @TableField(exist = false)
    private String cashierName;

    /** 支付方式（前端展示用） */
    @TableField(exist = false)
    private String paymentMethod;

    /** 创建时间（前端展示用） */
    @TableField(exist = false)
    private String createTime;
}
