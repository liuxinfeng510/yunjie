package com.yf.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbMaintenanceRecord;
import com.yf.mapper.HerbMaintenanceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 中药养护记录服务类
 */
@Service
@RequiredArgsConstructor
public class HerbMaintenanceRecordService {
    
    private final HerbMaintenanceRecordMapper maintenanceRecordMapper;
    
    /**
     * 创建养护记录
     */
    public boolean createMaintenanceRecord(HerbMaintenanceRecord record) {
        return maintenanceRecordMapper.insert(record) > 0;
    }
    
    /**
     * 分页查询
     */
    public Page<HerbMaintenanceRecord> page(Page<HerbMaintenanceRecord> page) {
        return maintenanceRecordMapper.selectPage(page, null);
    }
}
