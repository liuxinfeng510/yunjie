package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品条形码实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_barcode")
public class DrugBarcode extends BaseEntity {
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 条形码
     */
    private String barcode;
    
    /**
     * 条码类型：EAN13/EAN8/CODE128/QR
     */
    private String barcodeType;
    
    /**
     * 是否主条码
     */
    private Boolean isPrimary;
}
