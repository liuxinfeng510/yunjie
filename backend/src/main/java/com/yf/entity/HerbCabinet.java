package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 中药斗柜实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_cabinet")
public class HerbCabinet extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 斗柜名称
     */
    private String name;
    
    /**
     * 行数
     */
    private Integer rowCount;
    
    /**
     * 列数
     */
    private Integer columnCount;
    
    /**
     * 起始列号(默认1，多柜连续编号时设为上一柜列数+1)
     */
    private Integer columnStartNumber;
    
    /**
     * 位置
     */
    private String location;
    
    /**
     * 每行子格数配置JSON，如{"1":3,"2":3,"3":2}
     */
    private String rowCellConfig;
    
    /**
     * 状态
     */
    private String status;
}
