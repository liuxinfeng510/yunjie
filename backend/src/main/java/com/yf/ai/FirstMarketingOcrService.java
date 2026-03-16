package com.yf.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 首营企业证照AI识别服务
 * 使用 DashScope qwen-vl-max 视觉模型识别各类证照并提取结构化信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirstMarketingOcrService {

    private final BaseAiService baseAiService;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final String SYSTEM_PROMPT = "你是一个专业的证照OCR识别助手，擅长识别各类企业证照和商业文件。";

    private static final String BUSINESS_LICENSE_PROMPT = """
        识别图片中的营业执照信息。返回JSON格式：
        {
            "supplierName": "企业名称/公司名称",
            "creditCode": "统一社会信用代码",
            "legalPerson": "法定代表人",
            "registeredCapital": "注册资本(如：100万元人民币)",
            "companyAddress": "住所/经营场所地址",
            "businessScope": "经营范围",
            "businessLicenseNo": "营业执照注册号(如有单独显示，否则用统一社会信用代码)",
            "businessLicenseValidFrom": "成立日期(YYYY-MM-DD格式)",
            "businessLicenseValidUntil": "营业期限至(YYYY-MM-DD格式，长期/永久则为null)"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    private static final String DRUG_LICENSE_PROMPT = """
        识别图片中的药品经营许可证信息。返回JSON格式：
        {
            "supplierName": "企业名称",
            "drugLicenseNo": "许可证编号",
            "legalPerson": "法定代表人/企业负责人",
            "companyAddress": "经营地址/注册地址",
            "businessScope": "经营范围",
            "drugLicenseValidFrom": "发证日期(YYYY-MM-DD格式)",
            "drugLicenseValidUntil": "有效期至(YYYY-MM-DD格式)"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    private static final String GSP_CERT_PROMPT = """
        识别图片中的GSP认证证书（药品经营质量管理规范认证证书）信息。返回JSON格式：
        {
            "supplierName": "企业名称",
            "gspCertNo": "证书编号",
            "gspCertValidFrom": "发证日期(YYYY-MM-DD格式)",
            "gspCertValidUntil": "有效期至(YYYY-MM-DD格式)"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    private static final String LEGAL_AUTH_PROMPT = """
        识别图片中的法人委托书/授权委托书信息。返回JSON格式：
        {
            "supplierName": "委托方/公司名称",
            "legalPerson": "法定代表人/委托人",
            "salesPersonName": "被委托人/受托人姓名",
            "contactPhone": "联系电话(如有)"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    private static final String ID_CARD_PROMPT = """
        识别图片中的身份证信息。返回JSON格式：
        {
            "salesPersonName": "姓名"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    private static final String SALES_AUTH_PROMPT = """
        识别图片中的销售人员授权委托书信息。返回JSON格式：
        {
            "supplierName": "授权公司名称",
            "salesPersonName": "被授权人/销售人员姓名",
            "contactPhone": "联系电话(如有)"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    private static final String BILLING_INFO_PROMPT = """
        识别图片中的开票资料/银行开户许可证/一般纳税人证明信息。返回JSON格式：
        {
            "billingName": "企业名称/开户名称",
            "billingTaxNo": "纳税人识别号/统一社会信用代码",
            "billingAddress": "地址",
            "billingPhone": "电话",
            "billingBankName": "开户银行",
            "billingBankAccount": "银行账号"
        }
        无法识别的字段用null。只返回JSON，不要任何其他文字。
        """;

    // docType → Image字段名 的映射
    private static final Map<String, String> DOC_TYPE_TO_IMAGE_FIELD = Map.of(
            "business_license", "businessLicenseImage",
            "drug_license", "drugLicenseImage",
            "gsp_cert", "gspCertImage",
            "legal_auth", "legalAuthImage",
            "id_card", "salesPersonIdImage",
            "sales_auth", "salesAuthImage"
    );

    /**
     * 批量识别证照文件
     * @param files 文件列表，每项包含 filePath 和 docType
     * @return 合并后的表单数据和识别详情
     */
    public Map<String, Object> recognizeDocuments(List<Map<String, String>> files) {
        long totalStart = System.currentTimeMillis();
        Map<String, Object> mergedFormData = new LinkedHashMap<>();
        List<Map<String, Object>> fileResults = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Map<String, String> fileItem : files) {
            String filePath = fileItem.get("filePath");
            String docType = fileItem.get("docType");
            Map<String, Object> fileResult = new LinkedHashMap<>();
            fileResult.put("filePath", filePath);
            fileResult.put("docType", docType);

            try {
                // 1. 读取文件为base64
                String base64 = readFileAsBase64(filePath);

                // 2. 按类型选prompt并调用AI
                Map<String, Object> extracted = recognizeSingle(base64, docType);
                fileResult.put("success", true);
                fileResult.put("fields", extracted);

                // 3. 合并到总结果（先到先得，不覆盖已有非null值）
                for (Map.Entry<String, Object> entry : extracted.entrySet()) {
                    if (entry.getValue() != null && !mergedFormData.containsKey(entry.getKey())) {
                        mergedFormData.put(entry.getKey(), entry.getValue());
                    }
                }

                // 4. 图片路径映射
                String imageField = DOC_TYPE_TO_IMAGE_FIELD.get(docType);
                if (imageField != null && filePath != null) {
                    mergedFormData.put(imageField, filePath);
                }

            } catch (Exception e) {
                log.error("识别文件失败: {} ({})", filePath, docType, e);
                fileResult.put("success", false);
                fileResult.put("error", e.getMessage());
                warnings.add(docType + " 识别失败: " + e.getMessage());
            }

            fileResults.add(fileResult);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formData", mergedFormData);
        result.put("fileResults", fileResults);
        result.put("warnings", warnings);
        result.put("duration", System.currentTimeMillis() - totalStart);
        return result;
    }

    /**
     * 读取文件为base64字符串
     * PDF文件渲染首页为图片
     */
    private String readFileAsBase64(String filePath) throws IOException {
        // 安全校验：确保路径在上传目录内
        String relativePath = filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length()) : filePath;
        Path fullPath = Path.of(uploadDir, relativePath).normalize();
        if (!fullPath.startsWith(Path.of(uploadDir).normalize())) {
            throw new IllegalArgumentException("非法文件路径");
        }

        File file = fullPath.toFile();
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        if (filePath.toLowerCase().endsWith(".pdf")) {
            return renderPdfFirstPage(file);
        } else {
            byte[] bytes = Files.readAllBytes(fullPath);
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * 使用PDFBox渲染PDF首页为JPEG base64
     */
    private String renderPdfFirstPage(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "JPEG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 识别单个文件
     */
    private Map<String, Object> recognizeSingle(String base64, String docType) {
        String prompt = getPromptByDocType(docType);
        String response = baseAiService.visionBase64(SYSTEM_PROMPT, prompt, base64);
        return parseJsonResponse(response);
    }

    private String getPromptByDocType(String docType) {
        return switch (docType) {
            case "business_license" -> BUSINESS_LICENSE_PROMPT;
            case "drug_license" -> DRUG_LICENSE_PROMPT;
            case "gsp_cert" -> GSP_CERT_PROMPT;
            case "legal_auth" -> LEGAL_AUTH_PROMPT;
            case "id_card" -> ID_CARD_PROMPT;
            case "sales_auth" -> SALES_AUTH_PROMPT;
            case "billing_info" -> BILLING_INFO_PROMPT;
            default -> throw new IllegalArgumentException("不支持的文件类型: " + docType);
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String response) {
        try {
            String cleanJson = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            JsonNode json = objectMapper.readTree(cleanJson);
            Map<String, Object> map = objectMapper.convertValue(json, Map.class);
            // 移除null值
            map.values().removeIf(v -> v == null || "null".equals(v));
            return map;
        } catch (Exception e) {
            log.warn("解析AI响应JSON失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
