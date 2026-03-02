package com.yf.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbAcceptanceRecord;
import com.yf.mapper.HerbAcceptanceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 中药验收记录服务类
 */
@Service
@RequiredArgsConstructor
public class HerbAcceptanceRecordService {
    
    private final HerbAcceptanceRecordMapper acceptanceRecordMapper;
    
    /**
     * 创建验收记录
     */
    public boolean createAcceptanceRecord(HerbAcceptanceRecord record) {
        return acceptanceRecordMapper.insert(record) > 0;
    }
    
    /**
     * 分页查询
     */
    public Page<HerbAcceptanceRecord> page(Page<HerbAcceptanceRecord> page) {
        return acceptanceRecordMapper.selectPage(page, null);
    }
}
