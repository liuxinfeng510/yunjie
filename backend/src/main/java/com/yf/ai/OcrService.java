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
 * OCR 识别服务
 * 识别处方单、发票、药品包装等文档信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final BaseAiService baseAiService;
    private final ObjectMapper objectMapper;

    private static final String PRESCRIPTION_PROMPT = """
        你是一位专业的处方识别助手。请识别图片中的中药处方信息。
        
        请按以下JSON格式返回：
        {
            "patientName": "患者姓名",
            "patientAge": "年龄",
            "patientGender": "性别",
            "diagnosis": "诊断",
            "doctorName": "医生姓名",
            "prescriptionDate": "处方日期",
            "herbs": [
                {"name": "药材名", "dosage": "剂量(g)", "notes": "备注"}
            ],
            "dosage": "剂数",
            "usage": "用法",
            "notes": "其他备注"
        }
        
        如果某些字段无法识别，使用null。只返回JSON。
        """;

    private static final String INVOICE_PROMPT = """
        请识别图片中的发票信息。
        
        返回JSON格式：
        {
            "invoiceType": "发票类型",
            "invoiceCode": "发票代码",
            "invoiceNumber": "发票号码",
            "invoiceDate": "开票日期",
            "sellerName": "销方名称",
            "buyerName": "购方名称",
            "items": [{"name": "项目名", "quantity": 数量, "unitPrice": 单价, "amount": 金额}],
            "totalAmount": 总金额,
            "taxAmount": 税额
        }
        
        只返回JSON。
        """;

    private static final String DRUG_PACKAGE_PROMPT = """
        请识别图片中的药品包装信息。
        
        返回JSON格式：
        {
            "drugName": "商品名称",
            "genericName": "通用名",
            "specification": "规格",
            "manufacturer": "生产厂家",
            "approvalNumber": "批准文号",
            "batchNumber": "批号",
            "productionDate": "生产日期",
            "expiryDate": "有效期至",
            "barcode": "条形码"
        }
        
        只返回JSON。
        """;

    /**
     * 识别处方单
     */
    public AiRecognitionResult recognizePrescription(String base64Image) {
        return recognize(base64Image, PRESCRIPTION_PROMPT, "ocr_prescription");
    }

    /**
     * 识别发票
     */
    public AiRecognitionResult recognizeInvoice(String base64Image) {
        return recognize(base64Image, INVOICE_PROMPT, "ocr_invoice");
    }

    /**
     * 识别药品包装
     */
    public AiRecognitionResult recognizeDrugPackage(String base64Image) {
        return recognize(base64Image, DRUG_PACKAGE_PROMPT, "ocr_drug_package");
    }

    /**
     * 通用文字识别
     */
    public AiRecognitionResult recognizeText(String base64Image, String customPrompt) {
        String prompt = customPrompt != null ? customPrompt : "请识别图片中的所有文字，按原格式返回。";
        return recognize(base64Image, prompt, "ocr_text");
    }

    private AiRecognitionResult recognize(String base64Image, String prompt, String type) {
        long startTime = System.currentTimeMillis();
        try {
            String response = baseAiService.visionBase64("你是一个专业的OCR识别助手。", prompt, base64Image);
            return parseResponse(response, type, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("OCR识别失败: {}", type, e);
            return AiRecognitionResult.error(type, e.getMessage());
        }
    }

    private AiRecognitionResult parseResponse(String response, String type, long duration) {
        try {
            String cleanJson = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            JsonNode json = objectMapper.readTree(cleanJson);

            AiRecognitionResult result = new AiRecognitionResult();
            result.setRecognitionType(type);
            result.setSuccess(true);
            result.setDuration(duration);
            result.setRawResponse(response);
            result.setConfidence(85.0); // OCR通常有较高置信度

            // 根据类型设置主要结果
            if ("ocr_prescription".equals(type)) {
                result.setPrimaryResult(json.path("diagnosis").asText("处方识别成功"));
            } else if ("ocr_invoice".equals(type)) {
                result.setPrimaryResult("发票号: " + json.path("invoiceNumber").asText());
            } else if ("ocr_drug_package".equals(type)) {
                result.setPrimaryResult(json.path("drugName").asText("药品包装识别成功"));
            } else {
                result.setPrimaryResult("文字识别成功");
            }

            // 将JSON转为items
            List<AiRecognitionResult.RecognitionItem> items = new ArrayList<>();
            AiRecognitionResult.RecognitionItem item = new AiRecognitionResult.RecognitionItem();
            item.setName(type);
            item.setConfidence(85.0);
            item.setAttributes(objectMapper.convertValue(json, Map.class));
            items.add(item);
            result.setItems(items);

            return result;
        } catch (Exception e) {
            log.warn("解析OCR响应失败", e);
            // 如果不是JSON，直接作为文本返回
            AiRecognitionResult result = new AiRecognitionResult();
            result.setRecognitionType(type);
            result.setSuccess(true);
            result.setDuration(duration);
            result.setPrimaryResult(response);
            result.setRawResponse(response);
            return result;
        }
    }
}
