package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员等级实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_level")
public class MemberLevel extends BaseEntity {
    
    /**
     * 等级名称
     */
    private String name;
    
    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;
    
    /**
     * 折扣率
     */
    private BigDecimal discount;
    
    /**
     * 积分倍率
     */
    private BigDecimal pointsRate;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
}
