package com.yf.controller;

import com.yf.entity.ElectronicSignature;
import com.yf.service.ElectronicSignatureService;
import com.yf.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子签名控制器
 */
@RestController
@RequestMapping("/signature")
@RequiredArgsConstructor
public class ElectronicSignatureController {

    private final ElectronicSignatureService signatureService;

    /**
     * 创建电子签名
     */
    @PostMapping
    public ApiResponse<ElectronicSignature> create(
            @RequestBody ElectronicSignatureService.SignatureRequest request,
            HttpServletRequest httpRequest) {
        // 获取当前用户ID
        if (request.getSignerId() == null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Long) {
                request.setSignerId((Long) principal);
            }
        }
        request.setIpAddress(httpRequest.getRemoteAddr());
        ElectronicSignature signature = signatureService.createSignature(request);
        return ApiResponse.success(signature);
    }

    /**
     * 验证签名
     */
    @GetMapping("/{id}/verify")
    public ApiResponse<Boolean> verify(@PathVariable Long id) {
        boolean valid = signatureService.verifySignature(id);
        return ApiResponse.success(valid);
    }

    /**
     * 撤销签名
     */
    @PostMapping("/{id}/revoke")
    public ApiResponse<Void> revoke(@PathVariable Long id, @RequestParam String reason) {
        signatureService.revokeSignature(id, reason);
        return ApiResponse.success(null);
    }

    /**
     * 获取业务签名
     */
    @GetMapping("/business")
    public ApiResponse<List<ElectronicSignature>> getBusinessSignatures(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        List<ElectronicSignature> signatures = signatureService.getBusinessSignatures(businessType, businessId);
        return ApiResponse.success(signatures);
    }

    /**
     * 检查是否已签名
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkSigned(
            @RequestParam String businessType,
            @RequestParam Long businessId,
            @RequestParam Long signerId) {
        boolean signed = signatureService.hasSignature(businessType, businessId, signerId);
        return ApiResponse.success(signed);
    }
}
