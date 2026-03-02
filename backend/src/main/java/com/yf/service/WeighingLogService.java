package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.WeighingLog;
import com.yf.mapper.WeighingLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 称重记录服务
 */
@Service
@RequiredArgsConstructor
public class WeighingLogService {

    private final WeighingLogMapper weighingLogMapper;

    public Page<WeighingLog> page(Long deviceId, Long herbId, String operationType,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   int pageNum, int pageSize) {
        Page<WeighingLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WeighingLog> wrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            wrapper.eq(WeighingLog::getDeviceId, deviceId);
        }
        if (herbId != null) {
            wrapper.eq(WeighingLog::getHerbId, herbId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(WeighingLog::getOperationType, operationType);
        }
        if (startTime != null) {
            wrapper.ge(WeighingLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(WeighingLog::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(WeighingLog::getCreatedAt);
        return weighingLogMapper.selectPage(page, wrapper);
    }

    public WeighingLog getById(Long id) {
        return weighingLogMapper.selectById(id);
    }

    public WeighingLog create(WeighingLog log) {
        weighingLogMapper.insert(log);
        return log;
    }

    /**
     * 记录AI识别称重
     */
    public WeighingLog recordAiWeighing(Long deviceId, Long herbId, String herbName,
                                         BigDecimal weight, String operationType,
                                         BigDecimal confidence, Long operatorId) {
        WeighingLog log = new WeighingLog();
        log.setDeviceId(deviceId);
        log.setHerbId(herbId);
        log.setHerbName(herbName);
        log.setWeight(weight);
        log.setOperationType(operationType);
        log.setRecognitionMethod("ai");
        log.setConfidence(confidence);
        log.setOperatorId(operatorId);
        weighingLogMapper.insert(log);
        return log;
    }

    /**
     * 记录手动称重
     */
    public WeighingLog recordManualWeighing(Long deviceId, Long herbId, String herbName,
                                             BigDecimal weight, String operationType,
                                             Long operatorId) {
        WeighingLog log = new WeighingLog();
        log.setDeviceId(deviceId);
        log.setHerbId(herbId);
        log.setHerbName(herbName);
        log.setWeight(weight);
        log.setOperationType(operationType);
        log.setRecognitionMethod("manual");
        log.setOperatorId(operatorId);
        weighingLogMapper.insert(log);
        return log;
    }

    /**
     * 关联处方
     */
    public void linkToPrescription(Long logId, Long prescriptionId, Long prescriptionItemId) {
        WeighingLog log = new WeighingLog();
        log.setId(logId);
        log.setPrescriptionId(prescriptionId);
        log.setPrescriptionItemId(prescriptionItemId);
        weighingLogMapper.updateById(log);
    }

    /**
     * 获取处方相关称重记录
     */
    public List<WeighingLog> getByPrescription(Long prescriptionId) {
        LambdaQueryWrapper<WeighingLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeighingLog::getPrescriptionId, prescriptionId)
               .orderByAsc(WeighingLog::getCreatedAt);
        return weighingLogMapper.selectList(wrapper);
    }

    /**
     * 统计中药称重数据
     */
    public BigDecimal sumWeightByHerb(Long herbId, String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<WeighingLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeighingLog::getHerbId, herbId);
        if (operationType != null) {
            wrapper.eq(WeighingLog::getOperationType, operationType);
        }
        if (startTime != null) {
            wrapper.ge(WeighingLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(WeighingLog::getCreatedAt, endTime);
        }

        List<WeighingLog> logs = weighingLogMapper.selectList(wrapper);
        return logs.stream()
                .map(WeighingLog::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
