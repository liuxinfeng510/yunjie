package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中药配药记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_dispensing_record")
public class HerbDispensingRecord extends BaseEntity {
    
    /**
     * 处方ID
     */
    private Long prescriptionId;
    
    /**
     * 明细序号
     */
    private Integer itemIndex;
    
    /**
     * 药材ID
     */
    private Long herbId;
    
    /**
     * 药材名称
     */
    private String herbName;
    
    /**
     * 目标重量
     */
    private BigDecimal targetWeight;
    
    /**
     * 实际重量
     */
    private BigDecimal actualWeight;
    
    /**
     * 重量偏差
     */
    private BigDecimal weightDeviation;
    
    /**
     * 视觉检查结果
     */
    private String visionCheckResult;
    
    /**
     * 视觉识别置信度
     */
    private BigDecimal visionConfidence;
    
    /**
     * 斗格位置
     */
    private String cellLocation;
    
    /**
     * 配药人ID
     */
    private Long dispenserId;
    
    /**
     * 配药时间
     */
    private LocalDateTime dispenseTime;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 状态
     */
    private String status;
}
