package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Inventory;
import com.yf.exception.BusinessException;
import com.yf.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 库存服务
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryMapper inventoryMapper;
    
    /**
     * 分页查询库存
     */
    public Page<Inventory> page(Long storeId, Long drugId, Boolean lowStock, int pageNum, int pageSize) {
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
        return inventoryMapper.selectPage(page, wrapper);
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
