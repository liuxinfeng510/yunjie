package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("first_marketing_supplier")
public class FirstMarketingSupplier extends BaseEntity {

    private String applyNo;
    private Long supplierId;
    private String supplierName;
    private String creditCode;
    private String legalPerson;
    private String registeredCapital;
    private String businessScope;
    private String companyAddress;
    private String contactName;
    private String contactPhone;

    // 营业执照
    private String businessLicenseNo;
    private String businessLicenseImage;
    private LocalDate businessLicenseValidFrom;
    private LocalDate businessLicenseValidUntil;

    // 药品经营许可证
    private String drugLicenseNo;
    private String drugLicenseImage;
    private LocalDate drugLicenseValidFrom;
    private LocalDate drugLicenseValidUntil;

    // GSP认证证书
    private String gspCertNo;
    private String gspCertImage;
    private LocalDate gspCertValidFrom;
    private LocalDate gspCertValidUntil;

    // 委托授权
    private String legalAuthImage;
    private String salesPersonName;
    private String salesPersonIdImage;
    private String salesAuthImage;

    // 开票信息
    private String billingName;
    private String billingTaxNo;
    private String billingAddress;
    private String billingPhone;
    private String billingBankName;
    private String billingBankAccount;

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
