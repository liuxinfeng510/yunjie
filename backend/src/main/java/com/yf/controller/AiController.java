package com.yf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.ai.*;
import com.yf.annotation.Auditable;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.AiRecognitionLog;
import com.yf.mapper.AiRecognitionLogMapper;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final HerbRecognitionService herbRecognitionService;
    private final OcrService ocrService;
    private final SalesAssistantService salesAssistantService;
    private final VisionCheckoutService visionCheckoutService;
    private final FirstMarketingOcrService firstMarketingOcrService;
    private final AiRecognitionLogMapper aiRecognitionLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 中药图片识别
     */
    @PostMapping("/herb/recognize")
    @Auditable(module = "AI", operation = "中药识别")
    public ApiResponse<AiRecognitionResult> recognizeHerb(@RequestBody Map<String, String> request) {
        String imageBase64 = request.get("image");
        String imageUrl = request.get("imageUrl");

        AiRecognitionResult result;
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            result = herbRecognitionService.recognizeByBase64(imageBase64);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            result = herbRecognitionService.recognizeByUrl(imageUrl);
        } else {
            return ApiResponse.error("请提供图片数据(image)或图片URL(imageUrl)");
        }

        saveLog("herb_identify", "image", imageBase64 != null ? "[base64]" : imageUrl, result);
        return ApiResponse.success(result);
    }

    /**
     * 中药称重识别
     */
    @PostMapping("/herb/recognize-weighing")
    @Auditable(module = "AI", operation = "称重场景中药识别")
    public ApiResponse<AiRecognitionResult> recognizeHerbWeighing(@RequestBody Map<String, Object> request) {
        String imageBase64 = (String) request.get("image");
        Double weight = request.get("weight") != null ? Double.valueOf(request.get("weight").toString()) : 0.0;

        if (imageBase64 == null || imageBase64.isEmpty()) {
            return ApiResponse.error("请提供图片数据");
        }

        AiRecognitionResult result = herbRecognitionService.recognizeForWeighing(imageBase64, weight);
        saveLog("herb_identify", "image", "[base64]", result);
        return ApiResponse.success(result);
    }

    /**
     * 处方单OCR识别
     */
    @PostMapping("/ocr/prescription")
    @Auditable(module = "AI", operation = "处方单OCR")
    public ApiResponse<AiRecognitionResult> ocrPrescription(@RequestBody Map<String, String> request) {
        String imageBase64 = request.get("image");
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return ApiResponse.error("请提供图片数据");
        }

        AiRecognitionResult result = ocrService.recognizePrescription(imageBase64);
        saveLog("ocr_prescription", "image", "[base64]", result);
        return ApiResponse.success(result);
    }

    /**
     * 发票OCR识别
     */
    @PostMapping("/ocr/invoice")
    @Auditable(module = "AI", operation = "发票OCR")
    public ApiResponse<AiRecognitionResult> ocrInvoice(@RequestBody Map<String, String> request) {
        String imageBase64 = request.get("image");
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return ApiResponse.error("请提供图片数据");
        }

        AiRecognitionResult result = ocrService.recognizeInvoice(imageBase64);
        saveLog("ocr_invoice", "image", "[base64]", result);
        return ApiResponse.success(result);
    }

    /**
     * 药品包装OCR识别
     */
    @PostMapping("/ocr/drug-package")
    @Auditable(module = "AI", operation = "药品包装OCR")
    public ApiResponse<AiRecognitionResult> ocrDrugPackage(@RequestBody Map<String, String> request) {
        String imageBase64 = request.get("image");
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return ApiResponse.error("请提供图片数据");
        }

        AiRecognitionResult result = ocrService.recognizeDrugPackage(imageBase64);
        saveLog("ocr_drug_package", "image", "[base64]", result);
        return ApiResponse.success(result);
    }

    /**
     * 销售助手对话
     */
    @PostMapping("/sales-assistant/chat")
    public ApiResponse<String> salesChat(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isEmpty()) {
            return ApiResponse.error("请提供问题");
        }

        String answer = salesAssistantService.chat(question);
        return ApiResponse.success(answer);
    }

    /**
     * 症状药品推荐
     */
    @PostMapping("/sales-assistant/recommend")
    @Auditable(module = "AI", operation = "症状药品推荐")
    public ApiResponse<AiRecognitionResult> recommendBySymptoms(@RequestBody Map<String, String> request) {
        String symptoms = request.get("symptoms");
        String patientInfo = request.get("patientInfo");

        if (symptoms == null || symptoms.isEmpty()) {
            return ApiResponse.error("请提供症状描述");
        }

        AiRecognitionResult result = salesAssistantService.recommendBySymptoms(symptoms, patientInfo);
        saveLog("sales_assist", "text", symptoms, result);
        return ApiResponse.success(result);
    }

    /**
     * 药品搭配建议
     */
    @PostMapping("/sales-assistant/combination")
    @Auditable(module = "AI", operation = "药品搭配建议")
    public ApiResponse<AiRecognitionResult> suggestCombination(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> drugs = (List<String>) request.get("drugs");
        String symptoms = (String) request.get("symptoms");

        if (drugs == null || drugs.isEmpty()) {
            return ApiResponse.error("请提供已选药品列表");
        }

        AiRecognitionResult result = salesAssistantService.suggestCombination(drugs, symptoms);
        saveLog("sales_assist", "text", String.join(",", drugs), result);
        return ApiResponse.success(result);
    }

    /**
     * 用药指导
     */
    @PostMapping("/sales-assistant/guidance")
    public ApiResponse<String> getMedicationGuidance(@RequestBody Map<String, String> request) {
        String drugName = request.get("drugName");
        String patientCondition = request.get("patientCondition");

        if (drugName == null || drugName.isEmpty()) {
            return ApiResponse.error("请提供商品名称");
        }

        String guidance = salesAssistantService.getMedicationGuidance(drugName, patientCondition);
        return ApiResponse.success(guidance);
    }

    /**
     * 视觉结算识别
     */
    @PostMapping("/vision-checkout/recognize")
    @Auditable(module = "AI", operation = "视觉结算识别")
    public ApiResponse<AiRecognitionResult> visionCheckout(@RequestBody Map<String, String> request) {
        String imageBase64 = request.get("image");
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return ApiResponse.error("请提供图片数据");
        }

        AiRecognitionResult result = visionCheckoutService.recognizeCheckoutItems(imageBase64);
        saveLog("vision_checkout", "image", "[base64]", result);
        return ApiResponse.success(result);
    }

    /**
     * 首营企业证照AI识别
     * 接收多个文件路径和类型，逐个识别并合并结果
     */
    @PostMapping("/first-marketing/recognize")
    @Auditable(module = "AI", operation = "首营企业证照识别")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> recognizeFirstMarketing(@RequestBody Map<String, Object> request) {
        List<Map<String, String>> files = (List<Map<String, String>>) request.get("files");
        if (files == null || files.isEmpty()) {
            return ApiResponse.error("请提供待识别的文件列表");
        }

        Map<String, Object> result = firstMarketingOcrService.recognizeDocuments(files);

        // 记录审计日志
        try {
            AiRecognitionLog aiLog = new AiRecognitionLog();
            aiLog.setTenantId(TenantContext.getTenantId());
            aiLog.setRecognitionType("first_marketing_ocr");
            aiLog.setInputType("files");
            aiLog.setInputData(objectMapper.writeValueAsString(files));
            aiLog.setResultData(objectMapper.writeValueAsString(result));
            aiLog.setConfidence(BigDecimal.valueOf(85.0));
            aiLog.setDuration((Long) result.get("duration"));
            aiLog.setCreatedAt(LocalDateTime.now());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                aiLog.setOperatorId(userId);
            }
            aiRecognitionLogMapper.insert(aiLog);
        } catch (Exception e) {
            log.error("保存AI识别日志失败", e);
        }

        return ApiResponse.success(result);
    }

    /**
     * 保存AI识别日志
     */
    private void saveLog(String type, String inputType, String inputData, AiRecognitionResult result) {
        try {
            AiRecognitionLog log = new AiRecognitionLog();
            log.setTenantId(TenantContext.getTenantId());
            log.setRecognitionType(type);
            log.setInputType(inputType);
            log.setInputData(inputData != null && inputData.length() > 2000 ? inputData.substring(0, 2000) : inputData);
            log.setResultData(objectMapper.writeValueAsString(result));
            log.setConfidence(result.getConfidence() != null ? BigDecimal.valueOf(result.getConfidence()) : null);
            log.setDuration(result.getDuration());
            log.setCreatedAt(LocalDateTime.now());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                log.setOperatorId(userId);
            }

            aiRecognitionLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存AI识别日志失败", e);
        }
    }
}
