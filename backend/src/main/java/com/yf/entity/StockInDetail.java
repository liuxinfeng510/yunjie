package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 入库单明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_in_detail")
public class StockInDetail extends BaseEntity {
    
    /**
     * 入库单ID
     */
    private Long stockInId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 生产日期
     */
    private LocalDate produceDate;
    
    /**
     * 有效期至
     */
    private LocalDate expireDate;
    
    /**
     * 数量
     */
    private BigDecimal quantity;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 进货价
     */
    private BigDecimal purchasePrice;
    
    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 药品通用名（快照）
     */
    private String drugName;
    
    /**
     * 规格（快照）
     */
    private String specification;
    
    /**
     * 生产企业（快照）
     */
    private String manufacturer;

    /**
     * 追溯码列表（非数据库字段，API传输用）
     */
    @TableField(exist = false)
    private List<String> traceCodes;

    /**
     * 追溯码数量（非数据库字段，查询回显用）
     */
    @TableField(exist = false)
    private Integer traceCodeCount;
}
