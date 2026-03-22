package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 中药斗格实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_cabinet_cell")
public class HerbCabinetCell extends BaseEntity {
    
    /**
     * 斗柜ID
     */
    private Long cabinetId;
    
    /**
     * 行号
     */
    private Integer rowNum;
    
    /**
     * 列号
     */
    private Integer columnNum;
    
    /**
     * 子格序号(1=A,2=B,3=C,4=D)
     */
    private Integer subIndex;
    
    /**
     * 标签
     */
    private String label;
    
    /**
     * 药材ID
     */
    private Long herbId;
    
    /**
     * 当前库存
     */
    private BigDecimal currentStock;
    
    /**
     * 最小库存
     */
    private BigDecimal minStock;
    
    /**
     * 状态
     */
    private String status;
}
