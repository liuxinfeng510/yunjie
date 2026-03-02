package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.TemperatureHumidityLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 温湿度记录Mapper
 */
@Mapper
public interface TemperatureHumidityLogMapper extends BaseMapper<TemperatureHumidityLog> {
}
