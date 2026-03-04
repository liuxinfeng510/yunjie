package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产企业实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_manufacturer")
public class DrugManufacturer extends BaseEntity {
    
    /**
     * 企业名称
     */
    private String name;
    
    /**
     * 简称
     */
    private String shortName;
    
    /**
     * 拼音
     */
    private String pinyin;
    
    /**
     * 拼音首字母
     */
    private String pinyinShort;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 状态：active/disabled
     */
    private String status;
}
