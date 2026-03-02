package com.yf.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * 模拟电子秤设备
 * 用于开发和测试环境，生成模拟称重数据
 */
@Slf4j
@Component
public class SimulatedScaleDevice implements ScaleDevice {

    private boolean connected = false;
    private BigDecimal currentWeight = BigDecimal.ZERO;
    private BigDecimal tareWeight = BigDecimal.ZERO;
    private final Random random = new Random();
    private String deviceId;

    @Override
    public boolean connect(String portOrConfig) {
        this.deviceId = "SIM-SCALE-" + System.currentTimeMillis();
        this.connected = true;
        log.info("模拟电子秤已连接: {}", deviceId);
        return true;
    }

    @Override
    public void disconnect() {
        this.connected = false;
        log.info("模拟电子秤已断开: {}", deviceId);
    }

    @Override
    public BigDecimal readWeight() {
        if (!connected) {
            return BigDecimal.ZERO;
        }
        // 模拟称重数据：在当前重量基础上产生微小波动
        double fluctuation = (random.nextDouble() - 0.5) * 0.1;
        currentWeight = currentWeight.add(BigDecimal.valueOf(fluctuation))
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        return currentWeight.subtract(tareWeight).max(BigDecimal.ZERO);
    }

    @Override
    public void tare() {
        this.tareWeight = this.currentWeight;
        log.info("模拟电子秤去皮: tareWeight={}", tareWeight);
    }

    @Override
    public DeviceStatus getStatus() {
        return connected ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置模拟重量（用于测试）
     */
    public void setSimulatedWeight(BigDecimal weight) {
        this.currentWeight = weight;
    }
}
