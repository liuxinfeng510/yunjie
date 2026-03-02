package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 中药饮片实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb")
public class Herb extends BaseEntity {
    
    /**
     * 药材编码
     */
    private String herbCode;
    
    /**
     * 药材名称
     */
    private String name;
    
    /**
     * 别名
     */
    private String alias;
    
    /**
     * 拼音
     */
    private String pinyin;
    
    /**
     * 拼音简写
     */
    private String pinyinShort;
    
    /**
     * 类别
     */
    private String category;
    
    /**
     * 性味
     */
    private String nature;
    
    /**
     * 味道
     */
    private String flavor;
    
    /**
     * 归经
     */
    private String meridian;
    
    /**
     * 功效
     */
    private String efficacy;
    
    /**
     * 产地
     */
    private String origin;
    
    /**
     * 炮制方法
     */
    private String processingMethod;
    
    /**
     * 最小剂量
     */
    private BigDecimal dosageMin;
    
    /**
     * 最大剂量
     */
    private BigDecimal dosageMax;
    
    /**
     * 储存条件
     */
    private String storage;
    
    /**
     * 是否有毒
     */
    private Boolean isToxic;
    
    /**
     * 毒性等级
     */
    private String toxicLevel;
    
    /**
     * 是否贵细药材
     */
    private Boolean isPrecious;
    
    /**
     * 进货价
     */
    private BigDecimal purchasePrice;
    
    /**
     * 零售价
     */
    private BigDecimal retailPrice;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 状态
     */
    private String status;
}
