package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.entity.Inventory;
import com.yf.entity.Store;
import com.yf.exception.BusinessException;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import com.yf.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存服务
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryMapper inventoryMapper;
    private final DrugMapper drugMapper;
    private final StoreMapper storeMapper;
    
    /**
     * 分页查询库存（带药品信息）
     */
    public Page<Map<String, Object>> page(Long storeId, Long drugId, Boolean lowStock, int pageNum, int pageSize) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(Inventory::getStoreId, storeId);
        }
        if (drugId != null) {
            wrapper.eq(Inventory::getDrugId, drugId);
        }
        if (lowStock != null && lowStock) {
            wrapper.apply("quantity <= safe_stock");
        }
        
        wrapper.orderByDesc(Inventory::getUpdatedAt);
        Page<Inventory> inventoryPage = inventoryMapper.selectPage(page, wrapper);
        
        // 获取药品ID列表和门店ID列表
        List<Long> drugIds = inventoryPage.getRecords().stream()
                .map(Inventory::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> storeIds = inventoryPage.getRecords().stream()
                .map(Inventory::getStoreId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询药品信息
        Map<Long, Drug> drugMap = new HashMap<>();
        if (!drugIds.isEmpty()) {
            List<Drug> drugs = drugMapper.selectBatchIds(drugIds);
            drugMap = drugs.stream().collect(Collectors.toMap(Drug::getId, d -> d));
        }
        
        // 查询门店信息
        Map<Long, Store> storeMap = new HashMap<>();
        if (!storeIds.isEmpty()) {
            List<Store> stores = storeMapper.selectBatchIds(storeIds);
            storeMap = stores.stream().collect(Collectors.toMap(Store::getId, s -> s));
        }
        
        // 构建返回结果
        Map<Long, Drug> finalDrugMap = drugMap;
        Map<Long, Store> finalStoreMap = storeMap;
        List<Map<String, Object>> records = inventoryPage.getRecords().stream()
                .map(inv -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", inv.getId());
                    map.put("storeId", inv.getStoreId());
                    map.put("drugId", inv.getDrugId());
                    map.put("batchId", inv.getBatchId());
                    map.put("batchNo", inv.getBatchNo());
                    map.put("quantity", inv.getQuantity());
                    map.put("unit", inv.getUnit());
                    map.put("costPrice", inv.getCostPrice());
                    map.put("location", inv.getLocation());
                    map.put("safeStock", inv.getSafeStock());
                    map.put("maxStock", inv.getMaxStock());
                    map.put("updateTime", inv.getUpdatedAt());
                    
                    // 药品信息
                    Drug drug = finalDrugMap.get(inv.getDrugId());
                    if (drug != null) {
                        map.put("drugName", drug.getGenericName());
                        map.put("specification", drug.getSpecification());
                        map.put("manufacturer", drug.getManufacturer());
                    } else {
                        map.put("drugName", "");
                        map.put("specification", "");
                        map.put("manufacturer", "");
                    }
                    
                    // 门店信息
                    Store store = finalStoreMap.get(inv.getStoreId());
                    map.put("storeName", store != null ? store.getName() : "");
                    
                    return map;
                })
                .collect(Collectors.toList());
        
        Page<Map<String, Object>> resultPage = new Page<>(inventoryPage.getCurrent(), inventoryPage.getSize(), inventoryPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }
    
    /**
     * 根据门店和药品获取库存
     */
    public Inventory getByStoreAndDrug(Long storeId, Long drugId, Long batchId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getStoreId, storeId)
               .eq(Inventory::getDrugId, drugId);
        
        if (batchId != null) {
            wrapper.eq(Inventory::getBatchId, batchId);
        }
        
        return inventoryMapper.selectOne(wrapper);
    }
    
    /**
     * 调整库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long storeId, Long drugId, Long batchId, BigDecimal quantity, String type) {
        Inventory inventory = getByStoreAndDrug(storeId, drugId, batchId);
        
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }
        
        BigDecimal newQuantity;
        if ("IN".equals(type)) {
            // 入库
            newQuantity = inventory.getQuantity().add(quantity);
        } else if ("OUT".equals(type)) {
            // 出库
            newQuantity = inventory.getQuantity().subtract(quantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("库存不足");
            }
        } else {
            throw new BusinessException("无效的调整类型");
        }
        
        inventory.setQuantity(newQuantity);
        inventoryMapper.updateById(inventory);
    }
    
    /**
     * 创建库存记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(Inventory inventory) {
        inventoryMapper.insert(inventory);
    }
    
    /**
     * 更新库存记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Inventory inventory) {
        inventoryMapper.updateById(inventory);
    }
}
