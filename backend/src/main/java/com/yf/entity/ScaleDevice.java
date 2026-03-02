package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电子秤设备实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scale_device")
public class ScaleDevice extends BaseEntity {

    private Long storeId;

    private String deviceName;

    /** 设备型号 */
    private String model;

    /** 设备序列号 */
    private String serialNo;

    /** 连接类型: serial/usb/bluetooth/simulated */
    private String connectionType;

    /** 端口配置 */
    private String portConfig;

    /** 精度(g) */
    private BigDecimal precisionG;

    /** 最大量程(g) */
    private BigDecimal maxWeight;

    /** 状态: online/offline/error/calibrating */
    private String status;

    /** 最后心跳时间 */
    private LocalDateTime lastHeartbeat;

    /** 上次校准日期 */
    private LocalDate calibrationDate;

    /** 下次校准截止日期 */
    private LocalDate calibrationDueDate;
}
