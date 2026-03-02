package com.yf.controller;

import com.yf.annotation.Auditable;
import com.yf.service.HerbIncompatibilityService.IncompatibilityCheckResult;
import com.yf.service.SmartWeighingService;
import com.yf.service.SmartWeighingService.SmartWeighingRequest;
import com.yf.service.SmartWeighingService.SmartWeighingResult;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能称重控制器
 */
@RestController
@RequestMapping("/smart-weighing")
@RequiredArgsConstructor
public class SmartWeighingController {

    private final SmartWeighingService smartWeighingService;

    /**
     * 执行智能称重
     */
    @PostMapping("/process")
    @Auditable(module = "智能称重", operation = "称重识别")
    public ApiResponse<SmartWeighingResult> process(@RequestBody SmartWeighingRequest request) {
        // 自动填充操作人
        if (request.getOperatorId() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                request.setOperatorId(userId);
            }
        }
        return ApiResponse.success(smartWeighingService.process(request));
    }

    /**
     * 处方配伍检查
     */
    @PostMapping("/check-compatibility")
    public ApiResponse<IncompatibilityCheckResult> checkCompatibility(@RequestBody Map<String, List<String>> request) {
        List<String> herbs = request.get("herbs");
        return ApiResponse.success(smartWeighingService.checkPrescriptionCompatibility(herbs));
    }
}
