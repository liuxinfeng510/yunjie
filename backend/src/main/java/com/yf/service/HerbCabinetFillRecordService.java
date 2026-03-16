package com.yf.service;

import com.yf.entity.HerbCabinet;
import com.yf.entity.HerbCabinetCell;
import com.yf.entity.HerbCabinetFillRecord;
import com.yf.entity.SysUser;
import com.yf.mapper.HerbCabinetCellMapper;
import com.yf.mapper.HerbCabinetFillRecordMapper;
import com.yf.mapper.HerbCabinetMapper;
import com.yf.mapper.SysUserMapper;
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
    private final HerbCabinetMapper cabinetMapper;
    private final SysUserMapper sysUserMapper;
    
    /**
     * 创建装斗记录并更新斗格库存
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createFillRecord(HerbCabinetFillRecord record) {
        // 快照：填充斗柜名称
        if (record.getTargetCabinetId() != null) {
            HerbCabinet cabinet = cabinetMapper.selectById(record.getTargetCabinetId());
            if (cabinet != null) {
                record.setTargetCabinetName(cabinet.getName());
            }
        }
        // 快照：填充斗格标签
        if (record.getTargetCellId() != null) {
            HerbCabinetCell preCell = cellMapper.selectById(record.getTargetCellId());
            if (preCell != null) {
                record.setTargetCellLabel(preCell.getLabel());
            }
        }
        // 快照：填充操作员姓名
        if (record.getOperatorId() != null) {
            SysUser user = sysUserMapper.selectById(record.getOperatorId());
            if (user != null) {
                record.setOperatorName(user.getRealName());
            }
        }

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
