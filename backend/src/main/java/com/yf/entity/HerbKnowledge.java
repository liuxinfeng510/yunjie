package com.yf.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中药知识库实体
 * 全局共享表，不需要租户隔离
 */
@Data
@TableName("herb_knowledge")
public class HerbKnowledge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String herbCode;

    private String herbName;

    private String alias;

    private String pinyin;

    private String latinName;

    private String source;

    private String origin;

    private String nature;

    private String flavor;

    private String meridian;

    private String efficacy;

    private String indication;

    private String dosageUsage;

    private BigDecimal dosageMin;

    private BigDecimal dosageMax;

    private String dosageNote;

    private String processingMethod;

    private String contraindication;

    private String precaution;

    private String incompatibility;

    private String storage;

    private String identification;

    private Boolean isToxic;

    private String toxicLevel;

    private Boolean isPrecious;

    private String medicalInsurance;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    private Integer deleted;
}
