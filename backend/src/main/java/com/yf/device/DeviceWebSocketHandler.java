package com.yf.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 设备 WebSocket 消息处理器
 * 处理前端通过 STOMP 发送的设备控制消息
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DeviceWebSocketHandler {

    private final DeviceManager deviceManager;

    /**
     * 读取电子秤重量
     */
    @MessageMapping("/scale/{deviceId}/read")
    @SendTo("/topic/scale/{deviceId}")
    public Map<String, Object> readScale(@DestinationVariable String deviceId) {
        ScaleDevice device = deviceManager.getScaleDevice(deviceId);
        if (device == null) {
            return Map.of("error", "设备未找到", "deviceId", deviceId);
        }
        BigDecimal weight = device.readWeight();
        return Map.of(
                "deviceId", deviceId,
                "weight", weight,
                "status", device.getStatus().name(),
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 电子秤去皮
     */
    @MessageMapping("/scale/{deviceId}/tare")
    @SendTo("/topic/scale/{deviceId}")
    public Map<String, Object> tareScale(@DestinationVariable String deviceId) {
        ScaleDevice device = deviceManager.getScaleDevice(deviceId);
        if (device == null) {
            return Map.of("error", "设备未找到", "deviceId", deviceId);
        }
        device.tare();
        return Map.of(
                "deviceId", deviceId,
                "action", "tare",
                "weight", device.readWeight(),
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 摄像头拍照
     */
    @MessageMapping("/camera/{deviceId}/capture")
    @SendTo("/topic/camera/{deviceId}")
    public Map<String, Object> captureImage(@DestinationVariable String deviceId) {
        CameraDevice device = deviceManager.getCameraDevice(deviceId);
        if (device == null) {
            return Map.of("error", "设备未找到", "deviceId", deviceId);
        }
        String image = device.captureImage();
        return Map.of(
                "deviceId", deviceId,
                "image", image != null ? image : "",
                "timestamp", System.currentTimeMillis()
        );
    }
}
