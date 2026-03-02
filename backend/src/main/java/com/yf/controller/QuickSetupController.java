package com.yf.controller;

import com.yf.dto.QuickSetupRequest;
import com.yf.service.QuickSetupService;
import com.yf.service.SysConfigService;
import com.yf.vo.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 快速配置控制器 - 单体/连锁模式一键初始化
 */
@RestController
@RequestMapping("/setup")
@RequiredArgsConstructor
public class QuickSetupController {

    private final QuickSetupService quickSetupService;
    private final SysConfigService sysConfigService;

    /**
     * 检查是否已完成初始化配置
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getSetupStatus() {
        Map<String, Object> status = new HashMap<>();
        String completed = sysConfigService.getValue("system.setup_completed", "false");
        String businessMode = sysConfigService.getValue("system.business_mode", "");
        String companyName = sysConfigService.getValue("system.company_name", "");

        status.put("setupCompleted", "true".equals(completed));
        status.put("businessMode", businessMode);
        status.put("companyName", companyName);
        return ApiResponse.success(status);
    }

    /**
     * 执行快速配置
     */
    @PostMapping
    public ApiResponse<Void> setup(@Valid @RequestBody QuickSetupRequest request) {
        quickSetupService.setup(request);
        return ApiResponse.success();
    }

    /**
     * 获取功能开关配置
     */
    @GetMapping("/features")
    public ApiResponse<Map<String, String>> getFeatures() {
        return ApiResponse.success(sysConfigService.getByGroup("feature"));
    }

    /**
     * 更新功能开关
     */
    @PutMapping("/features")
    public ApiResponse<Void> updateFeatures(@RequestBody Map<String, String> features) {
        sysConfigService.batchUpdate(features);
        return ApiResponse.success();
    }
}
