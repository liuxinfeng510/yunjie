package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbFillLog;
import com.yf.mapper.HerbFillLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HerbFillLogService {

    private final HerbFillLogMapper herbFillLogMapper;

    /**
     * 分页查询
     */
    public Page<HerbFillLog> page(String drugName, LocalDate startDate, LocalDate endDate,
                                   int pageNum, int pageSize) {
        Page<HerbFillLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HerbFillLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(drugName)) {
            wrapper.like(HerbFillLog::getDrugName, drugName);
        }
        if (startDate != null) {
            wrapper.ge(HerbFillLog::getFillDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(HerbFillLog::getFillDate, endDate);
        }
        wrapper.orderByDesc(HerbFillLog::getFillDate);
        return herbFillLogMapper.selectPage(page, wrapper);
    }

    /**
     * 按日期范围查询列表（打印用，不分页）
     */
    public List<HerbFillLog> listByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<HerbFillLog> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(HerbFillLog::getFillDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(HerbFillLog::getFillDate, endDate);
        }
        wrapper.orderByDesc(HerbFillLog::getFillDate);
        return herbFillLogMapper.selectList(wrapper);
    }

    /**
     * 批量创建
     */
    public void batchCreate(List<HerbFillLog> logs) {
        for (HerbFillLog log : logs) {
            herbFillLogMapper.insert(log);
        }
    }

    /**
     * 更新质量状况和验收结论
     */
    public void updateStatus(Long id, String qualityStatus, String acceptanceResult) {
        HerbFillLog log = herbFillLogMapper.selectById(id);
        if (log != null) {
            if (StringUtils.hasText(qualityStatus)) {
                log.setQualityStatus(qualityStatus);
            }
            if (StringUtils.hasText(acceptanceResult)) {
                log.setAcceptanceResult(acceptanceResult);
            }
            herbFillLogMapper.updateById(log);
        }
    }
}
