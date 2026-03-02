package com.yf.device;

import java.math.BigDecimal;

/**
 * 电子秤设备接口
 */
public interface ScaleDevice {

    /**
     * 连接设备
     */
    boolean connect(String portOrConfig);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 读取当前重量(g)
     */
    BigDecimal readWeight();

    /**
     * 去皮/置零
     */
    void tare();

    /**
     * 获取设备状态
     */
    DeviceStatus getStatus();

    /**
     * 获取设备ID
     */
    String getDeviceId();

    /**
     * 设备状态枚举
     */
    enum DeviceStatus {
        ONLINE, OFFLINE, ERROR, CALIBRATING
    }
}
