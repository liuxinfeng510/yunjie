package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbPrescription;
import com.yf.entity.HerbPrescriptionItem;
import com.yf.mapper.HerbPrescriptionItemMapper;
import com.yf.mapper.HerbPrescriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中药处方服务类
 */
@Service
@RequiredArgsConstructor
public class HerbPrescriptionService {
    
    private final HerbPrescriptionMapper prescriptionMapper;
    private final HerbPrescriptionItemMapper prescriptionItemMapper;
    
    /**
     * 分页查询
     */
    public Page<HerbPrescription> page(Page<HerbPrescription> page) {
        return prescriptionMapper.selectPage(page, null);
    }
    
    /**
     * 根据ID查询（包含明细）
     */
    public Map<String, Object> getById(Long id) {
        HerbPrescription prescription = prescriptionMapper.selectById(id);
        if (prescription == null) {
            return null;
        }
        
        LambdaQueryWrapper<HerbPrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbPrescriptionItem::getPrescriptionId, id)
               .orderByAsc(HerbPrescriptionItem::getId);
        List<HerbPrescriptionItem> items = prescriptionItemMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("prescription", prescription);
        result.put("items", items);
        return result;
    }
    
    /**
     * 创建处方（含明细）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createWithItems(HerbPrescription prescription, List<HerbPrescriptionItem> items) {
        // 插入主表
        if (prescriptionMapper.insert(prescription) <= 0) {
            return false;
        }
        
        // 插入明细
        if (items != null && !items.isEmpty()) {
            for (HerbPrescriptionItem item : items) {
                item.setPrescriptionId(prescription.getId());
                prescriptionItemMapper.insert(item);
            }
        }
        
        return true;
    }
    
    /**
     * 更新状态
     */
    public boolean updateStatus(Long id, String status) {
        HerbPrescription prescription = new HerbPrescription();
        prescription.setId(id);
        prescription.setStatus(status);
        return prescriptionMapper.updateById(prescription) > 0;
    }
}
