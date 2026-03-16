package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("first_marketing_drug")
public class FirstMarketingDrug extends BaseEntity {

    private String applyNo;
    private Long firstSupplierId;
    private String firstSupplierName;
    private Long supplierId;
    private String supplierName;
    private Long drugId;
    private String genericName;
    private String tradeName;
    private String dosageForm;
    private String specification;
    private String unit;
    private String manufacturer;
    private String approvalNo;

    // 证照资料
    private String registrationCertNo;
    private String registrationCertImage;
    private LocalDate registrationCertValidFrom;
    private LocalDate registrationCertValidUntil;
    private String qualityStandard;
    private String qualityStandardImage;
    private String inspectionReportImage;
    private String instructionImage;
    private String packagingImage;

    // 审批流程
    private Long applicantId;
    private String applicantName;
    private LocalDateTime applyTime;
    private Long firstApproverId;
    private String firstApproverName;
    private LocalDateTime firstApproveTime;
    private String firstApproveOpinion;
    private String firstApproveResult;
    private Long secondApproverId;
    private String secondApproverName;
    private LocalDateTime secondApproveTime;
    private String secondApproveOpinion;
    private String secondApproveResult;
    private String status;
    private String remark;
}
