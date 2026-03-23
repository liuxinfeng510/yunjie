package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.Drug;
import com.yf.entity.DrugBarcode;
import com.yf.mapper.DrugBarcodeMapper;
import com.yf.mapper.DrugMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 药品条形码服务
 */
@Service
@RequiredArgsConstructor
public class DrugBarcodeService {

    private final DrugBarcodeMapper barcodeMapper;
    private final DrugMapper drugMapper;

    /**
     * 根据药品ID查询条形码列表
     */
    public List<DrugBarcode> listByDrugId(Long drugId) {
        LambdaQueryWrapper<DrugBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBarcode::getDrugId, drugId)
               .orderByDesc(DrugBarcode::getIsPrimary)
               .orderByAsc(DrugBarcode::getId);
        return barcodeMapper.selectList(wrapper);
    }

    /**
     * 根据条形码查询
     */
    public DrugBarcode findByBarcode(String barcode) {
        LambdaQueryWrapper<DrugBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBarcode::getBarcode, barcode)
               .last("LIMIT 1");
        return barcodeMapper.selectOne(wrapper);
    }

    /**
     * 检查条形码是否重复，返回重复的药品信息
     * @param barcode 条形码
     * @param excludeDrugId 排除的药品ID（编辑时排除当前药品）
     * @return null表示不重复，否则返回 {drugId, drugCode, tradeName}
     */
    public Map<String, Object> checkDuplicate(String barcode, Long excludeDrugId) {
        LambdaQueryWrapper<DrugBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBarcode::getBarcode, barcode);
        if (excludeDrugId != null) {
            wrapper.ne(DrugBarcode::getDrugId, excludeDrugId);
        }
        wrapper.last("LIMIT 1");
        DrugBarcode existing = barcodeMapper.selectOne(wrapper);
        
        if (existing == null) {
            return null; // 不重复
        }
        
        // 查询药品信息
        Drug drug = drugMapper.selectById(existing.getDrugId());
        if (drug == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("drugId", drug.getId());
        result.put("drugCode", drug.getDrugCode());
        result.put("tradeName", drug.getTradeName());
        result.put("genericName", drug.getGenericName());
        return result;
    }

    /**
     * 保存药品的条形码列表（先删旧再插新）
     */
    @Transactional
    public void saveAll(Long drugId, Long tenantId, List<DrugBarcode> barcodes) {
        // 删除旧的条形码
        LambdaQueryWrapper<DrugBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBarcode::getDrugId, drugId);
        barcodeMapper.delete(wrapper);
        
        // 插入新的条形码
        if (barcodes != null && !barcodes.isEmpty()) {
            for (DrugBarcode barcode : barcodes) {
                barcode.setId(null);
                barcode.setDrugId(drugId);
                barcode.setTenantId(tenantId);
                barcodeMapper.insert(barcode);
            }
        }
    }

    /**
     * 新增单个条形码
     */
    public void create(DrugBarcode barcode) {
        barcodeMapper.insert(barcode);
    }

    /**
     * 删除单个条形码
     */
    public void delete(Long id) {
        barcodeMapper.deleteById(id);
    }
}
