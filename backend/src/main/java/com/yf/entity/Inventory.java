package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 库存数量
     */
    private BigDecimal quantity;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 成本价
     */
    private BigDecimal costPrice;
    
    /**
     * 货位
     */
    private String location;
    
    /**
     * 安全库存
     */
    private BigDecimal safeStock;
    
    /**
     * 最大库存
     */
    private BigDecimal maxStock;
}
