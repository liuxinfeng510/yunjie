package com.yf.device;

/**
 * 摄像头设备接口
 * 用于中药识别、视觉结算等场景
 */
public interface CameraDevice {

    /**
     * 打开摄像头
     */
    boolean open(String deviceConfig);

    /**
     * 关闭摄像头
     */
    void close();

    /**
     * 拍照获取图片（Base64编码）
     */
    String captureImage();

    /**
     * 获取设备状态
     */
    DeviceStatus getStatus();

    /**
     * 获取设备ID
     */
    String getDeviceId();

    enum DeviceStatus {
        ONLINE, OFFLINE, ERROR
    }
}
