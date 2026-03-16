package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 中药斗柜装斗记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_cabinet_fill_record")
public class HerbCabinetFillRecord extends BaseEntity {
    
    /**
     * 药材ID
     */
    private Long herbId;
    
    /**
     * 药材名称
     */
    private String herbName;
    
    /**
     * 批号
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
     * 装斗重量
     */
    private BigDecimal fillWeight;
    
    /**
     * 源包装ID
     */
    private Long sourcePackageId;
    
    /**
     * 目标斗柜ID
     */
    private Long targetCabinetId;
    
    /**
     * 目标斗柜名称（快照）
     */
    private String targetCabinetName;
    
    /**
     * 目标斗格ID
     */
    private Long targetCellId;
    
    /**
     * 目标斗格标签（快照）
     */
    private String targetCellLabel;
    
    /**
     * 装斗前检查结果
     */
    private String preCheckResult;
    
    /**
     * 性状描述
     */
    private String appearanceDesc;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 操作员ID
     */
    private Long operatorId;
    
    /**
     * 操作员姓名（快照）
     */
    private String operatorName;
    
    /**
     * 装斗时间
     */
    private LocalDateTime fillTime;
    
    /**
     * 状态
     */
    private String status;
}
