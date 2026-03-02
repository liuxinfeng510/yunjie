package com.yf.controller;

import com.yf.entity.SysConfig;
import com.yf.service.SysConfigService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 获取所有配置（按分组）
     */
    @GetMapping
    public ApiResponse<Map<String, List<SysConfig>>> getAllGrouped() {
        return ApiResponse.success(sysConfigService.getAllGrouped());
    }

    /**
     * 按分组获取配置
     */
    @GetMapping("/group/{group}")
    public ApiResponse<Map<String, String>> getByGroup(@PathVariable String group) {
        return ApiResponse.success(sysConfigService.getByGroup(group));
    }

    /**
     * 获取单个配置值
     */
    @GetMapping("/key/{key}")
    public ApiResponse<String> getValue(@PathVariable String key) {
        return ApiResponse.success(sysConfigService.getValue(key));
    }

    /**
     * 设置单个配置
     */
    @PostMapping
    public ApiResponse<Void> setValue(@RequestParam String group,
                                      @RequestParam String key,
                                      @RequestParam String value,
                                      @RequestParam(required = false) String valueType,
                                      @RequestParam(required = false) String description) {
        sysConfigService.setValue(group, key, value, valueType, description);
        return ApiResponse.success();
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    public ApiResponse<Void> batchUpdate(@RequestBody Map<String, String> configs) {
        sysConfigService.batchUpdate(configs);
        return ApiResponse.success();
    }
}
