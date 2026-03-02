package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促销活动实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion")
public class Promotion extends BaseEntity {
    
    /**
     * 门店ID（null表示全部门店）
     */
    private Long storeId;
    
    /**
     * 促销名称
     */
    private String name;
    
    /**
     * 促销类型：discount-折扣, reduction-满减, gift-赠品, bundle-组合优惠
     */
    private String type;
    
    /**
     * 适用范围：all-全场, category-分类, product-指定商品
     */
    private String scope;
    
    /**
     * 适用对象ID列表（JSON数组，存储药品ID或分类ID）
     */
    private String targetIds;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 折扣率（折扣类型使用，如0.8表示8折）
     */
    private BigDecimal discountRate;
    
    /**
     * 满足金额（满减类型使用）
     */
    private BigDecimal thresholdAmount;
    
    /**
     * 减免金额（满减类型使用）
     */
    private BigDecimal reductionAmount;
    
    /**
     * 赠品药品ID（赠品类型使用）
     */
    private Long giftDrugId;
    
    /**
     * 赠品数量
     */
    private Integer giftQuantity;
    
    /**
     * 组合价格（组合优惠类型使用）
     */
    private BigDecimal bundlePrice;
    
    /**
     * 会员等级限制（JSON数组，空表示不限制）
     */
    private String memberLevelIds;
    
    /**
     * 每人限购次数（0表示不限制）
     */
    private Integer limitPerMember;
    
    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 状态：draft-草稿, active-生效中, paused-已暂停, ended-已结束
     */
    private String status;
    
    /**
     * 促销描述
     */
    private String description;
}
