package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_in")
public class StockIn extends BaseEntity {
    
    /**
     * 入库单号
     */
    private String orderNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 入库类型（采购入库、退货入库、调拨入库等）
     */
    private String type;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 状态（待审核、已审核、已入库）
     */
    private String status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 审核人ID
     */
    private Long approvedBy;
    
    /**
     * 审核时间
     */
    private LocalDateTime approvedAt;
    
    /**
     * 供应商名称（非数据库字段，查询时填充）
     */
    @TableField(exist = false)
    private String supplierName;
}
