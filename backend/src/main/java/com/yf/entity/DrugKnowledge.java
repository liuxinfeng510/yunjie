package com.yf.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品知识库实体
 * 全局共享表，不需要租户隔离
 */
@Data
@TableName("drug_knowledge")
public class DrugKnowledge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String drugCode;

    private String genericName;

    private String tradeName;

    private String englishName;

    private String pinyin;

    private String pinyinShort;

    private String approvalNo;

    private String dosageForm;

    private String specification;

    private String manufacturer;

    private String category;

    private String otcType;

    private String storageCondition;

    private Integer validPeriod;

    private String indication;

    private String dosage;

    private String adverseReaction;

    private String contraindication;

    private String precaution;

    private String interaction;

    private String pregnancyCategory;

    private Boolean isAntibiotics;

    private Boolean isPsychotropic;

    private Boolean isNarcotic;

    private String medicalInsurance;

    private BigDecimal referencePrice;

    private String barcode;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    private Integer deleted;
}
