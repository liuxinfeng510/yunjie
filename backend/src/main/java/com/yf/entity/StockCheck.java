package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存盘点实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check")
public class StockCheck extends BaseEntity {
    
    /**
     * 盘点单号
     */
    private String orderNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 盘点类型（全盘、抽盘、动态盘）
     */
    private String type;
    
    /**
     * 状态（盘点中、已完成）
     */
    private String status;
    
    /**
     * 备注
     */
    private String remark;
}
