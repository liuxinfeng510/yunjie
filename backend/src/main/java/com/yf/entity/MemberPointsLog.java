package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员积分日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_points_log")
public class MemberPointsLog extends BaseEntity {
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 积分变动（正数增加，负数减少）
     */
    private Integer points;
    
    /**
     * 类型（消费获得、兑换消耗、过期扣除、手动调整）
     */
    private String type;
    
    /**
     * 关联订单ID
     */
    private Long relatedOrderId;
    
    /**
     * 变动后余额
     */
    private Integer balance;
    
    /**
     * 备注
     */
    private String remark;
}
