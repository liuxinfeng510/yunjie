package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 慢病记录实体
 * 记录会员的慢性病诊断和管理信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chronic_disease_record")
public class ChronicDiseaseRecord extends BaseEntity {
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 疾病类型：diabetes/hypertension/hyperlipidemia/coronary_heart/asthma/other
     */
    private String diseaseType;
    
    /**
     * 疾病名称
     */
    private String diseaseName;
    
    /**
     * 诊断日期
     */
    private LocalDate diagnosisDate;
    
    /**
     * 诊断医院
     */
    private String diagnosisHospital;
    
    /**
     * 病情等级：mild/moderate/severe
     */
    private String severityLevel;
    
    /**
     * 当前用药方案（JSON格式）
     */
    private String medicationPlan;
    
    /**
     * 目标指标（如血糖目标值JSON）
     */
    private String targetIndicators;
    
    /**
     * 最近检查日期
     */
    private LocalDate lastCheckDate;
    
    /**
     * 最近检查结果（JSON）
     */
    private String lastCheckResult;
    
    /**
     * 下次复查日期
     */
    private LocalDate nextCheckDate;
    
    /**
     * 管理状态：active/stable/improved/worsened
     */
    private String managementStatus;
    
    /**
     * 医嘱记录
     */
    private String doctorAdvice;
    
    /**
     * 备注
     */
    private String remark;
}
