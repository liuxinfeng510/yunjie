package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * GSP合规检查记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gsp_compliance_check")
public class GspComplianceCheck extends BaseEntity {
    
    /**
     * 检查批次号
     */
    private String checkBatchNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 检查日期
     */
    private LocalDate checkDate;
    
    /**
     * 检查类型：daily/weekly/monthly/quarterly/annual
     */
    private String checkType;
    
    /**
     * 检查项目总数
     */
    private Integer totalItems;
    
    /**
     * 合格项数
     */
    private Integer passedItems;
    
    /**
     * 不合格项数
     */
    private Integer failedItems;
    
    /**
     * 合格率
     */
    private Integer passRate;
    
    /**
     * 检查结果JSON（详细检查项）
     */
    private String checkResult;
    
    /**
     * 问题描述
     */
    private String issues;
    
    /**
     * 整改措施
     */
    private String correctionMeasures;
    
    /**
     * 整改期限
     */
    private LocalDate correctionDeadline;
    
    /**
     * 整改状态：pending/in_progress/completed
     */
    private String correctionStatus;
    
    /**
     * 检查人ID
     */
    private Long checkerId;
    
    /**
     * 检查人姓名
     */
    private String checkerName;
    
    /**
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核人姓名
     */
    private String reviewerName;
    
    /**
     * 审核时间
     */
    private LocalDate reviewDate;
    
    /**
     * 备注
     */
    private String remark;
}
