package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 供应商资质实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_qualification")
public class SupplierQualification extends BaseEntity {

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 证照类型
     */
    private String licenseType;

    /**
     * 证照编号
     */
    private String licenseNo;

    /**
     * 证照图片
     */
    private String licenseImage;

    /**
     * 有效期起
     */
    private LocalDate validFrom;

    /**
     * 有效期止
     */
    private LocalDate validUntil;

    /**
     * 审核状态
     */
    private String verifyStatus;

    /**
     * 审批状态
     */
    private String approvalStatus;
}
