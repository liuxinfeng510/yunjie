package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存盘点明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check_detail")
public class StockCheckDetail extends BaseEntity {
    
    /**
     * 盘点单ID
     */
    private Long stockCheckId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 系统数量
     */
    private BigDecimal systemQuantity;
    
    /**
     * 实际数量
     */
    private BigDecimal actualQuantity;
    
    /**
     * 差异数量
     */
    private BigDecimal diffQuantity;
    
    /**
     * 差异原因
     */
    private String diffReason;
}
