package com.yf.service;

import com.yf.entity.HerbCabinetCell;
import com.yf.entity.HerbCabinetFillRecord;
import com.yf.mapper.HerbCabinetCellMapper;
import com.yf.mapper.HerbCabinetFillRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 中药斗柜装斗记录服务类
 */
@Service
@RequiredArgsConstructor
public class HerbCabinetFillRecordService {
    
    private final HerbCabinetFillRecordMapper fillRecordMapper;
    private final HerbCabinetCellMapper cellMapper;
    
    /**
     * 创建装斗记录并更新斗格库存
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createFillRecord(HerbCabinetFillRecord record) {
        // 插入装斗记录
        if (fillRecordMapper.insert(record) <= 0) {
            return false;
        }
        
        // 更新斗格库存
        HerbCabinetCell cell = cellMapper.selectById(record.getTargetCellId());
        if (cell != null) {
            BigDecimal newStock = (cell.getCurrentStock() != null ? cell.getCurrentStock() : BigDecimal.ZERO)
                    .add(record.getFillWeight());
            cell.setCurrentStock(newStock);
            cell.setHerbId(record.getHerbId());
            cellMapper.updateById(cell);
        }
        
        return true;
    }
}
