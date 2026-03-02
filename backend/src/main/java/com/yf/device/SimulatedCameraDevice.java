package com.yf.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 模拟摄像头设备
 * 用于开发和测试环境，返回预置的测试图片数据
 */
@Slf4j
@Component
public class SimulatedCameraDevice implements CameraDevice {

    private boolean opened = false;
    private String deviceId;

    @Override
    public boolean open(String deviceConfig) {
        this.deviceId = "SIM-CAM-" + System.currentTimeMillis();
        this.opened = true;
        log.info("模拟摄像头已打开: {}", deviceId);
        return true;
    }

    @Override
    public void close() {
        this.opened = false;
        log.info("模拟摄像头已关闭: {}", deviceId);
    }

    @Override
    public String captureImage() {
        if (!opened) {
            return null;
        }
        // 返回1x1像素的透明PNG作为占位
        log.info("模拟摄像头拍照: {}", deviceId);
        return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    @Override
    public DeviceStatus getStatus() {
        return opened ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }
}
