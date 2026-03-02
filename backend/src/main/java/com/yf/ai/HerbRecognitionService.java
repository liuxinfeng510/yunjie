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
 * 中药智能识别服务
 * 通过图片识别中药材种类、品质等信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HerbRecognitionService {

    private final BaseAiService baseAiService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        你是一位经验丰富的中药鉴定专家。请根据用户提供的中药图片进行识别和鉴定。
        
        请按照以下JSON格式返回识别结果：
        {
            "herbName": "中药名称",
            "confidence": 置信度(0-100的数字),
            "quality": "品质等级(优/良/中/差)",
            "origin": "可能的产地",
            "characteristics": "外观特征描述",
            "storageAdvice": "存储建议",
            "warnings": "注意事项(如有)"
        }
        
        如果无法识别，请返回：
        {
            "herbName": "未知",
            "confidence": 0,
            "error": "无法识别的原因"
        }
        
        只返回JSON，不要其他内容。
        """;

    /**
     * 通过图片URL识别中药
     */
    public AiRecognitionResult recognizeByUrl(String imageUrl) {
        long startTime = System.currentTimeMillis();
        try {
            String response = baseAiService.vision(SYSTEM_PROMPT,
                    "请识别这张图片中的中药材，并进行品质鉴定。", imageUrl);

            return parseResponse(response, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("中药识别失败", e);
            return AiRecognitionResult.error("herb_identify", e.getMessage());
        }
    }

    /**
     * 通过Base64图片识别中药
     */
    public AiRecognitionResult recognizeByBase64(String base64Image) {
        long startTime = System.currentTimeMillis();
        try {
            String response = baseAiService.visionBase64(SYSTEM_PROMPT,
                    "请识别这张图片中的中药材，并进行品质鉴定。", base64Image);

            return parseResponse(response, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("中药识别失败", e);
            return AiRecognitionResult.error("herb_identify", e.getMessage());
        }
    }

    /**
     * 批量识别（称重场景）
     */
    public AiRecognitionResult recognizeForWeighing(String base64Image, Double weight) {
        long startTime = System.currentTimeMillis();
        try {
            String prompt = String.format(
                    "请识别这张图片中的中药材。当前称重显示 %.2f 克。请判断药材种类和品质，并评估该重量是否符合常见包装规格。",
                    weight);

            String response = baseAiService.visionBase64(SYSTEM_PROMPT, prompt, base64Image);
            return parseResponse(response, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("称重场景中药识别失败", e);
            return AiRecognitionResult.error("herb_identify", e.getMessage());
        }
    }

    private AiRecognitionResult parseResponse(String response, long duration) {
        try {
            // 清理可能的 markdown 代码块标记
            String cleanJson = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            JsonNode json = objectMapper.readTree(cleanJson);

            AiRecognitionResult result = new AiRecognitionResult();
            result.setRecognitionType("herb_identify");
            result.setDuration(duration);
            result.setRawResponse(response);

            String herbName = json.path("herbName").asText("未知");
            double confidence = json.path("confidence").asDouble(0);

            if ("未知".equals(herbName) || confidence < 50) {
                result.setSuccess(false);
                result.setErrorMessage(json.path("error").asText("识别置信度过低"));
            } else {
                result.setSuccess(true);
                result.setPrimaryResult(herbName);
                result.setConfidence(confidence);

                // 构建详细结果
                List<AiRecognitionResult.RecognitionItem> items = new ArrayList<>();
                AiRecognitionResult.RecognitionItem item = new AiRecognitionResult.RecognitionItem();
                item.setName(herbName);
                item.setConfidence(confidence);

                Map<String, Object> attrs = new HashMap<>();
                if (json.has("quality")) attrs.put("quality", json.get("quality").asText());
                if (json.has("origin")) attrs.put("origin", json.get("origin").asText());
                if (json.has("characteristics")) attrs.put("characteristics", json.get("characteristics").asText());
                if (json.has("storageAdvice")) attrs.put("storageAdvice", json.get("storageAdvice").asText());
                if (json.has("warnings")) attrs.put("warnings", json.get("warnings").asText());
                item.setAttributes(attrs);

                items.add(item);
                result.setItems(items);
            }

            return result;
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", response, e);
            AiRecognitionResult result = AiRecognitionResult.error("herb_identify", "响应解析失败");
            result.setRawResponse(response);
            result.setDuration(duration);
            return result;
        }
    }
}
