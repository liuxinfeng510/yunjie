package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 挂单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("suspended_order")
public class SuspendedOrder extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 挂单号
     */
    private String orderNo;
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 会员姓名
     */
    private String memberName;
    
    /**
     * 商品明细JSON
     */
    private String items;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 挂单人ID
     */
    private Long suspendedBy;
    
    /**
     * 挂单人姓名
     */
    private String suspendedByName;
    
    /**
     * 挂单时间
     */
    private LocalDateTime suspendedAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireAt;
    
    /**
     * 状态：1-有效 0-已取单 -1-已过期
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}
