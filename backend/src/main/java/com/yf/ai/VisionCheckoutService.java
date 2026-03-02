package com.yf.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视觉结算服务
 * 通过摄像头图片识别商品，实现快速结算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisionCheckoutService {

    private final BaseAiService baseAiService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        你是一个专业的药品视觉识别系统。请识别图片中的药品/商品。
        
        请按JSON格式返回识别结果：
        {
            "items": [
                {
                    "name": "商品名称",
                    "specification": "规格",
                    "quantity": 数量,
                    "confidence": 置信度(0-100),
                    "barcode": "条形码(如能识别)",
                    "category": "类别(西药/中成药/保健品/医疗器械/其他)"
                }
            ],
            "totalItems": 总件数,
            "notes": "备注(如有遮挡或不清晰的情况)"
        }
        
        注意：
        - 仔细观察每件商品
        - 如果有多件相同商品，合并为一条记录并标注数量
        - 尽可能识别商品规格
        - 只返回JSON
        """;

    /**
     * 识别结算台商品
     */
    public AiRecognitionResult recognizeCheckoutItems(String base64Image) {
        long startTime = System.currentTimeMillis();
        try {
            String response = baseAiService.visionBase64(SYSTEM_PROMPT,
                    "请识别图片中的所有药品/商品，用于收银结算。", base64Image);

            return parseCheckoutResult(response, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("视觉结算识别失败", e);
            return AiRecognitionResult.error("vision_checkout", e.getMessage());
        }
    }

    /**
     * 识别并匹配库存
     * 返回识别结果及建议匹配的库存商品
     */
    public Map<String, Object> recognizeAndMatch(String base64Image, List<Map<String, Object>> inventoryItems) {
        AiRecognitionResult recognition = recognizeCheckoutItems(base64Image);

        Map<String, Object> result = new HashMap<>();
        result.put("recognition", recognition);

        if (recognition.isSuccess() && recognition.getItems() != null) {
            List<Map<String, Object>> matchedItems = new ArrayList<>();

            for (AiRecognitionResult.RecognitionItem item : recognition.getItems()) {
                Map<String, Object> matched = new HashMap<>();
                matched.put("recognized", item);

                // 简单的名称匹配逻辑（实际应用中可以使用更复杂的匹配算法）
                Map<String, Object> bestMatch = null;
                double bestScore = 0;

                for (Map<String, Object> inv : inventoryItems) {
                    String invName = String.valueOf(inv.getOrDefault("name", ""));
                    double score = calculateSimilarity(item.getName(), invName);
                    if (score > bestScore && score > 0.5) {
                        bestScore = score;
                        bestMatch = inv;
                    }
                }

                if (bestMatch != null) {
                    matched.put("inventory", bestMatch);
                    matched.put("matchScore", bestScore);
                }

                matchedItems.add(matched);
            }

            result.put("matchedItems", matchedItems);
        }

        return result;
    }

    private AiRecognitionResult parseCheckoutResult(String response, long duration) {
        try {
            String cleanJson = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            JsonNode json = objectMapper.readTree(cleanJson);

            AiRecognitionResult result = new AiRecognitionResult();
            result.setRecognitionType("vision_checkout");
            result.setSuccess(true);
            result.setDuration(duration);
            result.setRawResponse(response);

            int totalItems = json.path("totalItems").asInt(0);
            result.setPrimaryResult("识别到 " + totalItems + " 件商品");

            List<AiRecognitionResult.RecognitionItem> items = new ArrayList<>();
            double totalConfidence = 0;

            if (json.has("items")) {
                for (JsonNode itemNode : json.get("items")) {
                    AiRecognitionResult.RecognitionItem item = new AiRecognitionResult.RecognitionItem();
                    item.setName(itemNode.path("name").asText());
                    double conf = itemNode.path("confidence").asDouble(70);
                    item.setConfidence(conf);
                    totalConfidence += conf;

                    Map<String, Object> attrs = new HashMap<>();
                    if (itemNode.has("specification")) attrs.put("specification", itemNode.get("specification").asText());
                    if (itemNode.has("quantity")) attrs.put("quantity", itemNode.get("quantity").asInt(1));
                    if (itemNode.has("barcode")) attrs.put("barcode", itemNode.get("barcode").asText());
                    if (itemNode.has("category")) attrs.put("category", itemNode.get("category").asText());
                    item.setAttributes(attrs);

                    items.add(item);
                }
            }

            result.setItems(items);
            result.setConfidence(items.isEmpty() ? 0 : totalConfidence / items.size());

            return result;
        } catch (Exception e) {
            log.warn("解析视觉结算响应失败", e);
            AiRecognitionResult result = AiRecognitionResult.error("vision_checkout", "响应解析失败");
            result.setRawResponse(response);
            result.setDuration(duration);
            return result;
        }
    }

    /**
     * 简单的字符串相似度计算（Jaccard）
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        if (s1.equals(s2)) return 1.0;
        if (s1.contains(s2) || s2.contains(s1)) return 0.8;

        // 简单的字符重叠计算
        int common = 0;
        for (char c : s1.toCharArray()) {
            if (s2.indexOf(c) >= 0) common++;
        }
        return (double) common / Math.max(s1.length(), s2.length());
    }
}
