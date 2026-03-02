package com.yf.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备管理器
 * 管理所有智能设备（电子秤、摄像头）的生命周期和数据推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceManager {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimulatedScaleDevice simulatedScaleDevice;
    private final SimulatedCameraDevice simulatedCameraDevice;

    /** 已注册的电子秤设备 */
    private final Map<String, ScaleDevice> scaleDevices = new ConcurrentHashMap<>();
    /** 已注册的摄像头设备 */
    private final Map<String, CameraDevice> cameraDevices = new ConcurrentHashMap<>();

    /**
     * 注册并连接模拟电子秤
     */
    public ScaleDevice connectSimulatedScale(String config) {
        simulatedScaleDevice.connect(config);
        String id = simulatedScaleDevice.getDeviceId();
        scaleDevices.put(id, simulatedScaleDevice);
        log.info("注册模拟电子秤: {}", id);
        return simulatedScaleDevice;
    }

    /**
     * 注册并连接模拟摄像头
     */
    public CameraDevice connectSimulatedCamera(String config) {
        simulatedCameraDevice.open(config);
        String id = simulatedCameraDevice.getDeviceId();
        cameraDevices.put(id, simulatedCameraDevice);
        log.info("注册模拟摄像头: {}", id);
        return simulatedCameraDevice;
    }

    /**
     * 获取电子秤设备
     */
    public ScaleDevice getScaleDevice(String deviceId) {
        return scaleDevices.get(deviceId);
    }

    /**
     * 获取摄像头设备
     */
    public CameraDevice getCameraDevice(String deviceId) {
        return cameraDevices.get(deviceId);
    }

    /**
     * 推送称重数据到前端
     */
    public void pushWeightData(String deviceId, BigDecimal weight) {
        Map<String, Object> data = Map.of(
                "deviceId", deviceId,
                "weight", weight,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/scale/" + deviceId, data);
    }

    /**
     * 推送设备状态变更
     */
    public void pushDeviceStatus(String deviceId, String type, String status) {
        Map<String, Object> data = Map.of(
                "deviceId", deviceId,
                "type", type,
                "status", status,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/device/status", data);
    }

    /**
     * 获取所有已注册设备摘要
     */
    public Map<String, Object> getDeviceSummary() {
        Map<String, Object> summary = new ConcurrentHashMap<>();
        scaleDevices.forEach((id, device) -> summary.put(id, Map.of(
                "type", "scale",
                "status", device.getStatus().name()
        )));
        cameraDevices.forEach((id, device) -> summary.put(id, Map.of(
                "type", "camera",
                "status", device.getStatus().name()
        )));
        return summary;
    }

    /**
     * 断开并移除设备
     */
    public void disconnectDevice(String deviceId) {
        ScaleDevice scale = scaleDevices.remove(deviceId);
        if (scale != null) {
            scale.disconnect();
            pushDeviceStatus(deviceId, "scale", "OFFLINE");
            return;
        }
        CameraDevice camera = cameraDevices.remove(deviceId);
        if (camera != null) {
            camera.close();
            pushDeviceStatus(deviceId, "camera", "OFFLINE");
        }
    }
}
