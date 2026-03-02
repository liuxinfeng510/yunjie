package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 中药养护记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_maintenance_record")
public class HerbMaintenanceRecord extends BaseEntity {
    
    /**
     * 斗柜ID
     */
    private Long cabinetId;
    
    /**
     * 斗格ID
     */
    private Long cellId;
    
    /**
     * 药材ID
     */
    private Long herbId;
    
    /**
     * 药材名称
     */
    private String herbName;
    
    /**
     * 养护类型
     */
    private String maintenanceType;
    
    /**
     * 外观检查结果
     */
    private String appearanceResult;
    
    /**
     * 虫害检查结果
     */
    private String pestResult;
    
    /**
     * 霉变检查结果
     */
    private String moldResult;
    
    /**
     * 气味检查结果
     */
    private String odorResult;
    
    /**
     * 储存条件
     */
    private String storageCondition;
    
    /**
     * 总体结果
     */
    private String overallResult;
    
    /**
     * 异常描述
     */
    private String abnormalDesc;
    
    /**
     * 处理措施
     */
    private String treatment;
    
    /**
     * 图片（JSON数组）
     */
    private String images;
    
    /**
     * 操作员ID
     */
    private Long operatorId;
    
    /**
     * 养护时间
     */
    private LocalDateTime maintenanceTime;
    
    /**
     * 下次养护日期
     */
    private LocalDate nextMaintenance;
}
