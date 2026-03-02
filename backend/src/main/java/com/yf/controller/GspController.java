package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugAcceptance;
import com.yf.entity.DrugMaintenance;
import com.yf.entity.TemperatureHumidityLog;
import com.yf.mapper.TemperatureHumidityLogMapper;
import com.yf.service.DrugAcceptanceService;
import com.yf.service.DrugMaintenanceService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;

/**
 * GSP合规控制器
 */
@RestController
@RequestMapping("/gsp")
@RequiredArgsConstructor
public class GspController {
    
    private final DrugAcceptanceService drugAcceptanceService;
    private final DrugMaintenanceService drugMaintenanceService;
    private final TemperatureHumidityLogMapper temperatureHumidityLogMapper;
    
    /**
     * 创建验收记录
     */
    @PostMapping("/acceptance")
    public ApiResponse<DrugAcceptance> createAcceptance(@RequestBody DrugAcceptance acceptance) {
        DrugAcceptance result = drugAcceptanceService.create(acceptance);
        return ApiResponse.success(result);
    }
    
    /**
     * 分页查询验收记录
     */
    @GetMapping("/acceptances")
    public ApiResponse<Page<DrugAcceptance>> getAcceptances(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String overallResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<DrugAcceptance> result = drugAcceptanceService.page(storeId, overallResult, 
                startTime, endTime, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 创建养护记录
     */
    @PostMapping("/maintenance")
    public ApiResponse<DrugMaintenance> createMaintenance(@RequestBody DrugMaintenance maintenance) {
        DrugMaintenance result = drugMaintenanceService.create(maintenance);
        return ApiResponse.success(result);
    }
    
    /**
     * 分页查询养护记录
     */
    @GetMapping("/maintenances")
    public ApiResponse<Page<DrugMaintenance>> getMaintenances(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String maintenanceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<DrugMaintenance> result = drugMaintenanceService.page(storeId, maintenanceType, 
                startTime, endTime, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 查询温湿度记录
     */
    @GetMapping("/temp-humidity-logs")
    public ApiResponse<Page<TemperatureHumidityLog>> getTempHumidityLogs(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean isAbnormal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<TemperatureHumidityLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TemperatureHumidityLog> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(TemperatureHumidityLog::getStoreId, storeId);
        }
        if (location != null) {
            wrapper.eq(TemperatureHumidityLog::getLocation, location);
        }
        if (isAbnormal != null) {
            wrapper.eq(TemperatureHumidityLog::getIsAbnormal, isAbnormal);
        }
        if (startTime != null) {
            wrapper.ge(TemperatureHumidityLog::getRecordTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(TemperatureHumidityLog::getRecordTime, endTime);
        }
        
        wrapper.orderByDesc(TemperatureHumidityLog::getRecordTime);
        Page<TemperatureHumidityLog> result = temperatureHumidityLogMapper.selectPage(page, wrapper);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建温湿度记录
     */
    @PostMapping("/temp-humidity-log")
    public ApiResponse<TemperatureHumidityLog> createTempHumidityLog(@RequestBody TemperatureHumidityLog log) {
        log.setRecordTime(LocalDateTime.now());
        temperatureHumidityLogMapper.insert(log);
        return ApiResponse.success(log);
    }
}
