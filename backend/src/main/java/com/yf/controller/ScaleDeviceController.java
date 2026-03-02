package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.annotation.Auditable;
import com.yf.entity.ScaleDevice;
import com.yf.entity.WeighingLog;
import com.yf.service.ScaleDeviceService;
import com.yf.service.WeighingLogService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子秤设备控制器
 */
@RestController
@RequestMapping("/scale")
@RequiredArgsConstructor
public class ScaleDeviceController {

    private final ScaleDeviceService scaleDeviceService;
    private final WeighingLogService weighingLogService;

    @GetMapping("/device/page")
    public ApiResponse<Page<ScaleDevice>> pageDevices(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(scaleDeviceService.page(storeId, status, pageNum, pageSize));
    }

    @GetMapping("/device/list")
    public ApiResponse<List<ScaleDevice>> listDevices(@RequestParam Long storeId) {
        return ApiResponse.success(scaleDeviceService.listByStore(storeId));
    }

    @GetMapping("/device/{id}")
    public ApiResponse<ScaleDevice> getDevice(@PathVariable Long id) {
        return ApiResponse.success(scaleDeviceService.getById(id));
    }

    @PostMapping("/device")
    @Auditable(module = "电子秤", operation = "新增设备")
    public ApiResponse<ScaleDevice> createDevice(@RequestBody ScaleDevice device) {
        return ApiResponse.success(scaleDeviceService.create(device));
    }

    @PutMapping("/device/{id}")
    @Auditable(module = "电子秤", operation = "更新设备")
    public ApiResponse<Void> updateDevice(@PathVariable Long id, @RequestBody ScaleDevice device) {
        device.setId(id);
        scaleDeviceService.update(device);
        return ApiResponse.success();
    }

    @DeleteMapping("/device/{id}")
    @Auditable(module = "电子秤", operation = "删除设备")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        scaleDeviceService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/device/{id}/status")
    public ApiResponse<Void> updateDeviceStatus(@PathVariable Long id, @RequestParam String status) {
        scaleDeviceService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PostMapping("/device/{id}/heartbeat")
    public ApiResponse<Void> heartbeat(@PathVariable Long id) {
        scaleDeviceService.heartbeat(id);
        return ApiResponse.success();
    }

    @GetMapping("/device/online")
    public ApiResponse<List<ScaleDevice>> getOnlineDevices(@RequestParam Long storeId) {
        return ApiResponse.success(scaleDeviceService.getOnlineDevices(storeId));
    }

    // ========== 称重记录 ==========

    @GetMapping("/log/page")
    public ApiResponse<Page<WeighingLog>> pageLogs(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long herbId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(weighingLogService.page(deviceId, herbId, operationType, startTime, endTime, pageNum, pageSize));
    }

    @GetMapping("/log/{id}")
    public ApiResponse<WeighingLog> getLog(@PathVariable Long id) {
        return ApiResponse.success(weighingLogService.getById(id));
    }

    @PostMapping("/log")
    public ApiResponse<WeighingLog> createLog(@RequestBody WeighingLog log) {
        return ApiResponse.success(weighingLogService.create(log));
    }

    @GetMapping("/log/prescription/{prescriptionId}")
    public ApiResponse<List<WeighingLog>> getLogsByPrescription(@PathVariable Long prescriptionId) {
        return ApiResponse.success(weighingLogService.getByPrescription(prescriptionId));
    }
}
