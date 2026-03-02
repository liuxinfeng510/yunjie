package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 中药处方实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_prescription")
public class HerbPrescription extends BaseEntity {
    
    /**
     * 处方编号
     */
    private String prescriptionNo;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 患者姓名
     */
    private String patientName;
    
    /**
     * 患者性别
     */
    private String patientGender;
    
    /**
     * 患者年龄
     */
    private Integer patientAge;
    
    /**
     * 开方医生
     */
    private String prescriber;
    
    /**
     * 医院
     */
    private String hospital;
    
    /**
     * 诊断
     */
    private String diagnosis;
    
    /**
     * 总剂数
     */
    private Integer totalDose;
    
    /**
     * 煎煮方法
     */
    private String decoctionMethod;
    
    /**
     * 配药人ID
     */
    private Long dispenserId;
    
    /**
     * 审核人ID
     */
    private Long checkerId;
    
    /**
     * 状态
     */
    private String status;
}
