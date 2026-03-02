package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 温湿度记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("temperature_humidity_log")
public class TemperatureHumidityLog extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 位置（仓库、阴凉柜、冷藏柜等）
     */
    private String location;
    
    /**
     * 温度（℃）
     */
    private BigDecimal temperature;
    
    /**
     * 湿度（%）
     */
    private BigDecimal humidity;
    
    /**
     * 记录时间
     */
    private LocalDateTime recordTime;
    
    /**
     * 是否异常
     */
    private Boolean isAbnormal;
    
    /**
     * 是否已发送告警
     */
    private Boolean alarmSent;
    
    /**
     * 处理状态（未处理、已处理）
     */
    private String handleStatus;
}
