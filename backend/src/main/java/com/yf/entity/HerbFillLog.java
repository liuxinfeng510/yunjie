package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 装斗记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_fill_log")
public class HerbFillLog extends BaseEntity {

    /** 编号 */
    private String recordNo;

    /** 关联入库单ID */
    private Long stockInId;

    /** 药品ID */
    private Long drugId;

    /** 药品名称(快照) */
    private String drugName;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 批号 */
    private String batchNo;

    /** 装斗数量 */
    private BigDecimal fillQuantity;

    /** 装斗日期 */
    private LocalDate fillDate;

    /** 产地 */
    private String origin;

    /** 生产企业 */
    private String manufacturer;

    /** 购进企业(供应商) */
    private String supplierName;

    /** 质量状况(合格/不合格) */
    private String qualityStatus;

    /** 验收结论(合格/不合格) */
    private String acceptanceResult;

    /** 装斗人 */
    private String fillPerson;

    /** 复核人 */
    private String reviewer;
}
