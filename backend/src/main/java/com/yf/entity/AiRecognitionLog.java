package com.yf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI识别记录实体
 */
@Data
@TableName("ai_recognition_log")
public class AiRecognitionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** 识别类型: herb_identify/ocr/sales_assist/vision_checkout */
    private String recognitionType;

    /** 输入类型: image/text */
    private String inputType;

    /** 输入内容 */
    private String inputData;

    /** AI返回结果JSON */
    private String resultData;

    /** 置信度 */
    private BigDecimal confidence;

    /** 使用的AI模型 */
    private String modelName;

    /** Token消耗量 */
    private Integer tokenUsage;

    /** 响应耗时(ms) */
    private Long duration;

    /** 操作人ID */
    private Long operatorId;

    /** 关联业务类型 */
    private String relatedType;

    /** 关联业务ID */
    private Long relatedId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
