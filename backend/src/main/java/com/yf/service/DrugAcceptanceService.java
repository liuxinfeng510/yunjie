package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugAcceptance;
import com.yf.mapper.DrugAcceptanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 药品验收服务
 */
@Service
@RequiredArgsConstructor
public class DrugAcceptanceService {
    
    private final DrugAcceptanceMapper drugAcceptanceMapper;
    
    /**
     * 分页查询验收记录
     */
    public Page<DrugAcceptance> page(Long storeId, String overallResult, 
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      int pageNum, int pageSize) {
        Page<DrugAcceptance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugAcceptance> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(DrugAcceptance::getStoreId, storeId);
        }
        if (overallResult != null) {
            wrapper.eq(DrugAcceptance::getOverallResult, overallResult);
        }
        if (startTime != null) {
            wrapper.ge(DrugAcceptance::getAcceptTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DrugAcceptance::getAcceptTime, endTime);
        }
        
        wrapper.orderByDesc(DrugAcceptance::getAcceptTime);
        return drugAcceptanceMapper.selectPage(page, wrapper);
    }
    
    /**
     * 创建验收记录
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugAcceptance create(DrugAcceptance acceptance) {
        acceptance.setAcceptTime(LocalDateTime.now());
        drugAcceptanceMapper.insert(acceptance);
        return acceptance;
    }
    
    /**
     * 根据入库单ID获取验收记录
     */
    public DrugAcceptance getByStockInId(Long stockInId) {
        LambdaQueryWrapper<DrugAcceptance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugAcceptance::getStockInId, stockInId);
        return drugAcceptanceMapper.selectOne(wrapper);
    }
}
