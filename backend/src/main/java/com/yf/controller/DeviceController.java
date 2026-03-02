package com.yf.controller;

import com.yf.device.CameraDevice;
import com.yf.device.DeviceManager;
import com.yf.device.ScaleDevice;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceManager deviceManager;

    /**
     * 获取所有设备状态
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> listDevices() {
        return ApiResponse.success(deviceManager.getDeviceSummary());
    }

    /**
     * 连接模拟电子秤
     */
    @PostMapping("/scale/connect-sim")
    public ApiResponse<Map<String, Object>> connectSimulatedScale(@RequestBody(required = false) Map<String, String> config) {
        String portConfig = config != null ? config.getOrDefault("config", "SIM") : "SIM";
        ScaleDevice device = deviceManager.connectSimulatedScale(portConfig);
        return ApiResponse.success(Map.of(
                "deviceId", device.getDeviceId(),
                "status", device.getStatus().name()
        ));
    }

    /**
     * 连接模拟摄像头
     */
    @PostMapping("/camera/connect-sim")
    public ApiResponse<Map<String, Object>> connectSimulatedCamera(@RequestBody(required = false) Map<String, String> config) {
        String camConfig = config != null ? config.getOrDefault("config", "SIM") : "SIM";
        CameraDevice device = deviceManager.connectSimulatedCamera(camConfig);
        return ApiResponse.success(Map.of(
                "deviceId", device.getDeviceId(),
                "status", device.getStatus().name()
        ));
    }

    /**
     * 断开设备
     */
    @PostMapping("/disconnect/{deviceId}")
    public ApiResponse<Void> disconnectDevice(@PathVariable String deviceId) {
        deviceManager.disconnectDevice(deviceId);
        return ApiResponse.success();
    }
}
