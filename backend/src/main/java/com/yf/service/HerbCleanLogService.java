package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbCleanLog;
import com.yf.mapper.HerbCleanLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HerbCleanLogService {

    private final HerbCleanLogMapper herbCleanLogMapper;

    /**
     * 分页查询
     */
    public Page<HerbCleanLog> page(String drugName, LocalDate startDate, LocalDate endDate,
                                    int pageNum, int pageSize) {
        Page<HerbCleanLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HerbCleanLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(drugName)) {
            wrapper.like(HerbCleanLog::getDrugName, drugName);
        }
        if (startDate != null) {
            wrapper.ge(HerbCleanLog::getCleanDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(HerbCleanLog::getCleanDate, endDate);
        }
        wrapper.orderByDesc(HerbCleanLog::getCleanDate);
        return herbCleanLogMapper.selectPage(page, wrapper);
    }

    /**
     * 按日期范围查询列表（打印用）
     */
    public List<HerbCleanLog> listByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<HerbCleanLog> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(HerbCleanLog::getCleanDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(HerbCleanLog::getCleanDate, endDate);
        }
        wrapper.orderByDesc(HerbCleanLog::getCleanDate);
        return herbCleanLogMapper.selectList(wrapper);
    }

    /**
     * 批量创建
     */
    public void batchCreate(List<HerbCleanLog> logs) {
        for (HerbCleanLog log : logs) {
            herbCleanLogMapper.insert(log);
        }
    }

    /**
     * 更新复核状态
     */
    public void updateReviewStatus(Long id, String reviewStatus) {
        HerbCleanLog log = herbCleanLogMapper.selectById(id);
        if (log != null) {
            log.setReviewStatus(reviewStatus);
            herbCleanLogMapper.updateById(log);
        }
    }
}
