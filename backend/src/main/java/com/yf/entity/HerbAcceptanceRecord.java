package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中药验收记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_acceptance_record")
public class HerbAcceptanceRecord extends BaseEntity {
    
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
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 数量
     */
    private BigDecimal quantity;
    
    /**
     * 性状检查
     */
    private String appearanceCheck;
    
    /**
     * 气味检查
     */
    private String odorCheck;
    
    /**
     * 质地检查
     */
    private String textureCheck;
    
    /**
     * 专项检查
     */
    private String specialTest;
    
    /**
     * 产地核对
     */
    private String originCheck;
    
    /**
     * 包装检查
     */
    private String packageCheck;
    
    /**
     * 总体结果
     */
    private String overallResult;
    
    /**
     * 拒收原因
     */
    private String rejectReason;
    
    /**
     * 图片（JSON数组）
     */
    private String images;
    
    /**
     * 验收人ID
     */
    private Long acceptorId;
    
    /**
     * 验收时间
     */
    private LocalDateTime acceptTime;
    
    /**
     * 药师签名
     */
    private String pharmacistSign;
}
