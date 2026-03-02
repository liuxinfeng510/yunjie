package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品销毁记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_destruction")
public class DrugDestruction extends BaseEntity {
    
    /**
     * 销毁单号
     */
    private String destructionNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 销毁日期
     */
    private LocalDate destructionDate;
    
    /**
     * 销毁原因
     */
    private String reason;
    
    /**
     * 销毁方式：burn/bury/chemical/entrust
     */
    private String destructionMethod;
    
    /**
     * 销毁地点
     */
    private String destructionPlace;
    
    /**
     * 销毁金额（成本）
     */
    private BigDecimal totalAmount;
    
    /**
     * 销毁明细JSON
     */
    private String items;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 审批意见
     */
    private String approveOpinion;
    
    /**
     * 执行人ID
     */
    private Long executorId;
    
    /**
     * 执行人姓名
     */
    private String executorName;
    
    /**
     * 执行时间
     */
    private LocalDateTime executeTime;
    
    /**
     * 监督人ID
     */
    private Long supervisorId;
    
    /**
     * 监督人姓名
     */
    private String supervisorName;
    
    /**
     * 销毁照片
     */
    private String images;
    
    /**
     * 状态：pending/approved/rejected/executing/completed
     */
    private String status;
    
    /**
     * 备注
     */
    private String remark;
}
