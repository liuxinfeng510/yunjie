package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbAcceptanceRecord;
import com.yf.entity.HerbCabinetCleanRecord;
import com.yf.entity.HerbCabinetFillRecord;
import com.yf.entity.HerbMaintenanceRecord;
import com.yf.service.HerbAcceptanceRecordService;
import com.yf.service.HerbCabinetCleanRecordService;
import com.yf.service.HerbCabinetFillRecordService;
import com.yf.service.HerbMaintenanceRecordService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 中药GSP管理控制器
 */
@RestController
@RequestMapping("/herb-gsp")
@RequiredArgsConstructor
public class HerbGspController {
    
    private final HerbCabinetFillRecordService fillRecordService;
    private final HerbCabinetCleanRecordService cleanRecordService;
    private final HerbAcceptanceRecordService acceptanceRecordService;
    private final HerbMaintenanceRecordService maintenanceRecordService;
    
    /**
     * 创建装斗记录
     */
    @PostMapping("/fill-record")
    public ApiResponse<Void> createFillRecord(@RequestBody HerbCabinetFillRecord record) {
        boolean success = fillRecordService.createFillRecord(record);
        return success ? ApiResponse.success() : ApiResponse.error("创建装斗记录失败");
    }
    
    /**
     * 分页查询装斗记录
     */
    @GetMapping("/fill-records")
    public ApiResponse<Page<HerbCabinetFillRecord>> getFillRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 简化版，实际应该添加查询条件
        return ApiResponse.success(new Page<>(current, size));
    }
    
    /**
     * 创建清斗记录
     */
    @PostMapping("/clean-record")
    public ApiResponse<Void> createCleanRecord(@RequestBody HerbCabinetCleanRecord record) {
        boolean success = cleanRecordService.createCleanRecord(record);
        return success ? ApiResponse.success() : ApiResponse.error("创建清斗记录失败");
    }
    
    /**
     * 分页查询清斗记录
     */
    @GetMapping("/clean-records")
    public ApiResponse<Page<HerbCabinetCleanRecord>> getCleanRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 简化版，实际应该添加查询条件
        return ApiResponse.success(new Page<>(current, size));
    }
    
    /**
     * 创建验收记录
     */
    @PostMapping("/acceptance")
    public ApiResponse<Void> createAcceptance(@RequestBody HerbAcceptanceRecord record) {
        boolean success = acceptanceRecordService.createAcceptanceRecord(record);
        return success ? ApiResponse.success() : ApiResponse.error("创建验收记录失败");
    }
    
    /**
     * 分页查询验收记录
     */
    @GetMapping("/acceptances")
    public ApiResponse<Page<HerbAcceptanceRecord>> getAcceptances(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HerbAcceptanceRecord> page = new Page<>(current, size);
        Page<HerbAcceptanceRecord> result = acceptanceRecordService.page(page);
        return ApiResponse.success(result);
    }
    
    /**
     * 创建养护记录
     */
    @PostMapping("/maintenance")
    public ApiResponse<Void> createMaintenance(@RequestBody HerbMaintenanceRecord record) {
        boolean success = maintenanceRecordService.createMaintenanceRecord(record);
        return success ? ApiResponse.success() : ApiResponse.error("创建养护记录失败");
    }
    
    /**
     * 分页查询养护记录
     */
    @GetMapping("/maintenances")
    public ApiResponse<Page<HerbMaintenanceRecord>> getMaintenances(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HerbMaintenanceRecord> page = new Page<>(current, size);
        Page<HerbMaintenanceRecord> result = maintenanceRecordService.page(page);
        return ApiResponse.success(result);
    }
}
