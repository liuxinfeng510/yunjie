package com.yf.ai;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * AI 识别结果 DTO
 */
@Data
public class AiRecognitionResult {

    /** 识别类型 */
    private String recognitionType;

    /** 是否成功 */
    private boolean success;

    /** 置信度 (0-100) */
    private Double confidence;

    /** 主要结果 */
    private String primaryResult;

    /** 详细结果列表 */
    private List<RecognitionItem> items;

    /** 原始响应 */
    private String rawResponse;

    /** Token 消耗 */
    private Integer tokenUsage;

    /** 耗时(ms) */
    private Long duration;

    /** 错误信息 */
    private String errorMessage;

    @Data
    public static class RecognitionItem {
        private String name;
        private Double confidence;
        private Map<String, Object> attributes;
    }

    public static AiRecognitionResult success(String type, String result, Double confidence) {
        AiRecognitionResult r = new AiRecognitionResult();
        r.setRecognitionType(type);
        r.setSuccess(true);
        r.setPrimaryResult(result);
        r.setConfidence(confidence);
        return r;
    }

    public static AiRecognitionResult error(String type, String errorMessage) {
        AiRecognitionResult r = new AiRecognitionResult();
        r.setRecognitionType(type);
        r.setSuccess(false);
        r.setErrorMessage(errorMessage);
        return r;
    }
}
