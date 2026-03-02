package com.yf.service;

import com.yf.entity.HerbCabinetCleanRecord;
import com.yf.mapper.HerbCabinetCleanRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 中药斗柜清斗记录服务类
 */
@Service
@RequiredArgsConstructor
public class HerbCabinetCleanRecordService {
    
    private final HerbCabinetCleanRecordMapper cleanRecordMapper;
    
    /**
     * 创建清斗记录
     */
    public boolean createCleanRecord(HerbCabinetCleanRecord record) {
        return cleanRecordMapper.insert(record) > 0;
    }
}
