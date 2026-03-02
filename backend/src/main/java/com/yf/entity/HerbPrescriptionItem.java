package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 中药处方明细实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_prescription_item")
public class HerbPrescriptionItem extends BaseEntity {
    
    /**
     * 处方ID
     */
    private Long prescriptionId;
    
    /**
     * 药材ID
     */
    private Long herbId;
    
    /**
     * 药材名称
     */
    private String herbName;
    
    /**
     * 每剂用量
     */
    private BigDecimal dosePerDay;
    
    /**
     * 特殊炮制要求
     */
    private String specialProcess;
    
    /**
     * 斗格位置
     */
    private String cellLocation;
}
