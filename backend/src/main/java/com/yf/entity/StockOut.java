package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 出库单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_out")
public class StockOut extends BaseEntity {
    
    /**
     * 出库单号
     */
    private String orderNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 出库类型（销售出库、调拨出库、报损出库等）
     */
    private String type;
    
    /**
     * 目标门店ID（调拨时使用）
     */
    private Long targetStoreId;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 状态（待审核、已审核、已出库）
     */
    private String status;
    
    /**
     * 备注
     */
    private String remark;
}
