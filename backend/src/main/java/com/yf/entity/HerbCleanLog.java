package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 清斗记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("herb_clean_log")
public class HerbCleanLog extends BaseEntity {

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

    /** 剩余数量 */
    private BigDecimal remainingQuantity;

    /** 清斗日期 */
    private LocalDate cleanDate;

    /** 产地 */
    private String origin;

    /** 生产企业 */
    private String manufacturer;

    /** 状态(已复核/未复核) */
    private String reviewStatus;

    /** 清斗人 */
    private String cleanPerson;
}
