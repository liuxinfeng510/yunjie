package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统字典项实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {
    
    /**
     * 字典类型：dosage_form/drug_unit/storage_condition
     */
    private String dictType;
    
    /**
     * 字典值
     */
    private String itemValue;
    
    /**
     * 拼音
     */
    private String pinyin;
    
    /**
     * 拼音首字母
     */
    private String pinyinShort;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 是否预置数据
     */
    private Boolean isPreset;
    
    /**
     * 状态：active/disabled
     */
    private String status;
}
