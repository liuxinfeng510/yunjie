package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug")
public class Drug extends BaseEntity {

    /**
     * 药品分类ID
     */
    private Long categoryId;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 条形码
     */
    private String barcode;

    /**
     * 通用名
     */
    private String genericName;

    /**
     * 商品名
     */
    private String tradeName;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 拼音简码
     */
    private String pinyinShort;

    /**
     * 批准文号
     */
    private String approvalNo;

    /**
     * 剂型
     */
    private String dosageForm;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 生产企业ID（关联drug_manufacturer表）
     */
    private Long manufacturerId;

    /**
     * 上市许可持有人
     */
    private String marketingAuthHolder;

    /**
     * OTC类型 (甲类/乙类/处方药)
     */
    private String otcType;

    /**
     * 存储条件
     */
    private String storageCondition;

    /**
     * 有效期（月）
     */
    private Integer validPeriod;

    /**
     * 采购价
     */
    private BigDecimal purchasePrice;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

    /**
     * 是否抗生素
     */
    private Boolean isAntibiotics;

    /**
     * 是否精神药品
     */
    private Boolean isPsychotropic;

    /**
     * 是否麻醉药品
     */
    private Boolean isNarcotic;

    /**
     * 医保类型
     */
    private String medicalInsurance;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 状态 (启用/停用)
     */
    private String status;

    /**
     * 是否拆零销售
     */
    private Boolean isSplit;

    /**
     * 拆零比例（大包装:小包装）
     */
    private Integer splitRatio;

    /**
     * 拆零优先级（1=优先拆零销售）
     */
    private Integer splitPriority;

    /**
     * 是否重点养护品种
     */
    private Boolean isKeyMaintenance;

    /**
     * 是否进口药品
     */
    private Boolean isImported;
}
