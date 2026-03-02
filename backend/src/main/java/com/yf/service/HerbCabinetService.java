package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.Herb;
import com.yf.entity.HerbCabinet;
import com.yf.entity.HerbCabinetCell;
import com.yf.mapper.HerbCabinetCellMapper;
import com.yf.mapper.HerbCabinetMapper;
import com.yf.mapper.HerbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 中药斗柜服务类
 */
@Service
@RequiredArgsConstructor
public class HerbCabinetService {
    
    private final HerbCabinetMapper herbCabinetMapper;
    private final HerbCabinetCellMapper herbCabinetCellMapper;
    private final HerbMapper herbMapper;
    
    /**
     * 查询列表（按门店）
     */
    public List<HerbCabinet> listByStoreId(Long storeId) {
        LambdaQueryWrapper<HerbCabinet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinet::getStoreId, storeId)
               .orderByAsc(HerbCabinet::getName);
        return herbCabinetMapper.selectList(wrapper);
    }
    
    /**
     * 根据ID查询
     */
    public HerbCabinet getById(Long id) {
        return herbCabinetMapper.selectById(id);
    }
    
    /**
     * 获取斗柜所有斗格及药材信息
     */
    public List<Map<String, Object>> getCellsByShelfId(Long cabinetId) {
        LambdaQueryWrapper<HerbCabinetCell> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinetCell::getCabinetId, cabinetId)
               .orderByAsc(HerbCabinetCell::getRowNum, HerbCabinetCell::getColumnNum);
        List<HerbCabinetCell> cells = herbCabinetCellMapper.selectList(wrapper);
        
        // 批量查询药材信息
        List<Long> herbIds = cells.stream()
                .map(HerbCabinetCell::getHerbId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Herb> herbMap = new HashMap<>();
        if (!herbIds.isEmpty()) {
            List<Herb> herbs = herbMapper.selectBatchIds(herbIds);
            herbMap = herbs.stream().collect(Collectors.toMap(Herb::getId, h -> h));
        }
        
        // 组装结果
        final Map<Long, Herb> finalHerbMap = herbMap;
        return cells.stream().map(cell -> {
            Map<String, Object> result = new HashMap<>();
            result.put("cell", cell);
            if (cell.getHerbId() != null) {
                result.put("herb", finalHerbMap.get(cell.getHerbId()));
            }
            return result;
        }).collect(Collectors.toList());
    }
    
    /**
     * 新增
     */
    public boolean save(HerbCabinet cabinet) {
        return herbCabinetMapper.insert(cabinet) > 0;
    }
    
    /**
     * 更新
     */
    public boolean updateById(HerbCabinet cabinet) {
        return herbCabinetMapper.updateById(cabinet) > 0;
    }
    
    /**
     * 删除
     */
    public boolean deleteById(Long id) {
        return herbCabinetMapper.deleteById(id) > 0;
    }
}
