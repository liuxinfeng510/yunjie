package com.yf.service;

import com.yf.ai.AiRecognitionResult;
import com.yf.ai.HerbRecognitionService;
import com.yf.entity.WeighingLog;
import com.yf.service.HerbIncompatibilityService.IncompatibilityCheckResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 智能称重服务
 * 整合AI识别、称重记录、配伍检查的完整工作流
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartWeighingService {

    private final HerbRecognitionService herbRecognitionService;
    private final WeighingLogService weighingLogService;
    private final HerbIncompatibilityService incompatibilityService;
    private final HerbService herbService;

    /**
     * 智能称重工作流
     * 1. AI识别图片中的中药
     * 2. 记录称重数据
     * 3. 检查与已有药材的配伍
     */
    public SmartWeighingResult process(SmartWeighingRequest request) {
        SmartWeighingResult result = new SmartWeighingResult();
        result.setWeight(request.getWeight());

        // 1. AI识别
        AiRecognitionResult aiResult = null;
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            aiResult = herbRecognitionService.recognizeForWeighing(
                    request.getImageBase64(), 
                    request.getWeight().doubleValue());
            result.setAiRecognition(aiResult);

            if (aiResult.isSuccess()) {
                result.setRecognizedHerbName(aiResult.getPrimaryResult());
                result.setConfidence(aiResult.getConfidence());
                result.setRecognitionMethod("ai");

                // 尝试匹配系统中的中药
                var herbs = herbService.searchByName(aiResult.getPrimaryResult());
                if (!herbs.isEmpty()) {
                    result.setMatchedHerbId(herbs.get(0).getId());
                    result.setMatchedHerbName(herbs.get(0).getName());
                }
            }
        }

        // 如果AI未识别或手动指定
        if (request.getManualHerbId() != null) {
            var herb = herbService.getById(request.getManualHerbId());
            if (herb != null) {
                result.setMatchedHerbId(herb.getId());
                result.setMatchedHerbName(herb.getName());
                result.setRecognitionMethod("manual");
            }
        }

        // 2. 配伍检查
        if (request.getExistingHerbs() != null && !request.getExistingHerbs().isEmpty() 
                && result.getMatchedHerbName() != null) {
            IncompatibilityCheckResult checkResult = incompatibilityService.checkNewHerb(
                    result.getMatchedHerbName(), 
                    request.getExistingHerbs());
            result.setIncompatibilityCheck(checkResult);
            result.setHasIncompatibility(!checkResult.isSafe());
        }

        // 3. 记录称重日志
        if (request.isSaveLog() && result.getMatchedHerbId() != null) {
            WeighingLog log;
            if ("ai".equals(result.getRecognitionMethod())) {
                log = weighingLogService.recordAiWeighing(
                        request.getDeviceId(),
                        result.getMatchedHerbId(),
                        result.getMatchedHerbName(),
                        request.getWeight(),
                        request.getOperationType(),
                        BigDecimal.valueOf(result.getConfidence()),
                        request.getOperatorId());
            } else {
                log = weighingLogService.recordManualWeighing(
                        request.getDeviceId(),
                        result.getMatchedHerbId(),
                        result.getMatchedHerbName(),
                        request.getWeight(),
                        request.getOperationType(),
                        request.getOperatorId());
            }
            result.setWeighingLogId(log.getId());

            // 关联处方
            if (request.getPrescriptionId() != null) {
                weighingLogService.linkToPrescription(
                        log.getId(),
                        request.getPrescriptionId(),
                        request.getPrescriptionItemId());
            }
        }

        result.setSuccess(true);
        return result;
    }

    /**
     * 批量配伍检查
     */
    public IncompatibilityCheckResult checkPrescriptionCompatibility(List<String> herbNames) {
        return incompatibilityService.check(herbNames);
    }

    @Data
    public static class SmartWeighingRequest {
        /** 设备ID */
        private Long deviceId;
        /** 称重值(g) */
        private BigDecimal weight;
        /** 图片Base64 */
        private String imageBase64;
        /** 手动指定的中药ID（优先级高于AI识别） */
        private Long manualHerbId;
        /** 操作类型: stock_in/stock_out/dispense/check */
        private String operationType;
        /** 操作人ID */
        private Long operatorId;
        /** 已有药材列表（用于配伍检查） */
        private List<String> existingHerbs;
        /** 是否保存称重记录 */
        private boolean saveLog = true;
        /** 关联处方ID */
        private Long prescriptionId;
        /** 关联处方明细ID */
        private Long prescriptionItemId;
    }

    @Data
    public static class SmartWeighingResult {
        private boolean success;
        private BigDecimal weight;
        /** AI识别结果 */
        private AiRecognitionResult aiRecognition;
        /** 识别的药材名称 */
        private String recognizedHerbName;
        /** AI置信度 */
        private Double confidence;
        /** 识别方式: ai/manual */
        private String recognitionMethod;
        /** 匹配的系统中药ID */
        private Long matchedHerbId;
        /** 匹配的系统中药名称 */
        private String matchedHerbName;
        /** 配伍检查结果 */
        private IncompatibilityCheckResult incompatibilityCheck;
        /** 是否存在配伍禁忌 */
        private boolean hasIncompatibility;
        /** 称重记录ID */
        private Long weighingLogId;
    }
}
