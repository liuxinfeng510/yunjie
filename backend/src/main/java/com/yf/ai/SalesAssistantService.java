package com.yf.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售智能助手服务
 * 提供用药建议、药品推荐、症状分析等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesAssistantService {

    private final BaseAiService baseAiService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        你是一位专业的药房销售顾问，具有丰富的药学知识。你的职责是：
        1. 根据顾客描述的症状，提供合理的用药建议
        2. 推荐合适的药品组合
        3. 提醒用药注意事项和禁忌
        4. 回答关于药品的相关问题
        
        注意事项：
        - 始终建议顾客在用药前咨询医生或药师
        - 对于严重症状，建议就医而非自行用药
        - 考虑药物相互作用和禁忌症
        - 给出的建议应该专业、安全、负责任
        
        请使用通俗易懂的语言回答问题。
        """;

    private static final String RECOMMENDATION_PROMPT = """
        你是药房销售顾问。根据顾客的需求推荐药品。
        
        请按JSON格式返回推荐结果：
        {
            "analysis": "症状/需求分析",
            "recommendations": [
                {
                    "drugName": "商品名称",
                    "type": "西药/中成药/中药饮片",
                    "reason": "推荐理由",
                    "dosage": "用法用量",
                    "precautions": "注意事项"
                }
            ],
            "advice": "综合建议",
            "warnings": "重要提醒(如需就医等)"
        }
        
        只返回JSON。
        """;

    /**
     * 智能问答
     */
    public String chat(String question) {
        try {
            return baseAiService.chat(SYSTEM_PROMPT, question);
        } catch (Exception e) {
            log.error("销售助手对话失败", e);
            return "抱歉，服务暂时不可用，请稍后再试或咨询药师。";
        }
    }

    /**
     * 症状分析和药品推荐
     */
    public AiRecognitionResult recommendBySymptoms(String symptoms, String patientInfo) {
        long startTime = System.currentTimeMillis();
        try {
            String userMessage = String.format("顾客信息：%s\n症状描述：%s\n请给出用药建议。",
                    patientInfo != null ? patientInfo : "未提供",
                    symptoms);

            String response = baseAiService.chat(RECOMMENDATION_PROMPT, userMessage);
            return parseRecommendation(response, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("药品推荐失败", e);
            return AiRecognitionResult.error("sales_assist", e.getMessage());
        }
    }

    /**
     * 药品搭配建议
     */
    public AiRecognitionResult suggestCombination(List<String> selectedDrugs, String symptoms) {
        long startTime = System.currentTimeMillis();
        try {
            String prompt = """
                顾客已选药品：%s
                症状：%s
                
                请分析：
                1. 已选药品是否合理
                2. 是否有药物相互作用风险
                3. 是否需要补充其他药品
                4. 用药注意事项
                
                按JSON返回：
                {
                    "compatibility": "药品配伍分析",
                    "risks": ["风险点"],
                    "suggestions": ["补充建议"],
                    "usage": "综合用药建议"
                }
                """.formatted(String.join("、", selectedDrugs), symptoms);

            String response = baseAiService.chat("你是专业药师。", prompt);
            return parseRecommendation(response, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("药品搭配建议失败", e);
            return AiRecognitionResult.error("sales_assist", e.getMessage());
        }
    }

    /**
     * 用药指导
     */
    public String getMedicationGuidance(String drugName, String patientCondition) {
        try {
            String prompt = String.format(
                    "请提供%s的详细用药指导。患者情况：%s。包括：用法用量、注意事项、不良反应、禁忌症、存储方法。",
                    drugName, patientCondition != null ? patientCondition : "普通成年人");
            return baseAiService.chat(SYSTEM_PROMPT, prompt);
        } catch (Exception e) {
            log.error("获取用药指导失败", e);
            return "暂时无法获取用药指导，请咨询药师。";
        }
    }

    private AiRecognitionResult parseRecommendation(String response, long duration) {
        try {
            String cleanJson = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            JsonNode json = objectMapper.readTree(cleanJson);

            AiRecognitionResult result = new AiRecognitionResult();
            result.setRecognitionType("sales_assist");
            result.setSuccess(true);
            result.setDuration(duration);
            result.setRawResponse(response);
            result.setConfidence(80.0);
            result.setPrimaryResult(json.path("analysis").asText(json.path("compatibility").asText("分析完成")));

            List<AiRecognitionResult.RecognitionItem> items = new ArrayList<>();

            // 如果有recommendations数组
            if (json.has("recommendations")) {
                for (JsonNode rec : json.get("recommendations")) {
                    AiRecognitionResult.RecognitionItem item = new AiRecognitionResult.RecognitionItem();
                    item.setName(rec.path("drugName").asText());
                    item.setConfidence(80.0);
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("type", rec.path("type").asText());
                    attrs.put("reason", rec.path("reason").asText());
                    attrs.put("dosage", rec.path("dosage").asText());
                    attrs.put("precautions", rec.path("precautions").asText());
                    item.setAttributes(attrs);
                    items.add(item);
                }
            }

            result.setItems(items);
            return result;
        } catch (Exception e) {
            log.warn("解析推荐响应失败", e);
            AiRecognitionResult result = new AiRecognitionResult();
            result.setRecognitionType("sales_assist");
            result.setSuccess(true);
            result.setDuration(duration);
            result.setPrimaryResult(response);
            result.setRawResponse(response);
            return result;
        }
    }
}
