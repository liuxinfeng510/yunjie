package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中药斗柜清斗记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_cabinet_clean_record")
public class HerbCabinetCleanRecord extends BaseEntity {
    
    /**
     * 斗柜ID
     */
    private Long cabinetId;
    
    /**
     * 斗格ID
     */
    private Long cellId;
    
    /**
     * 斗格位置
     */
    private String cellLocation;
    
    /**
     * 原药材ID
     */
    private Long originalHerbId;
    
    /**
     * 原药材名称
     */
    private String originalHerbName;
    
    /**
     * 剩余重量
     */
    private BigDecimal remainingWeight;
    
    /**
     * 剩余药材处理方式
     */
    private String remainingDisposal;
    
    /**
     * 清斗原因
     */
    private String cleanReason;
    
    /**
     * 清斗方法
     */
    private String cleanMethod;
    
    /**
     * 清斗后斗格状态
     */
    private String cellStatusAfter;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 操作员ID
     */
    private Long operatorId;
    
    /**
     * 清斗时间
     */
    private LocalDateTime cleanTime;
    
    /**
     * 下一次装斗药材ID
     */
    private Long nextHerbId;
}
