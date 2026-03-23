package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.entity.DrugBatch;
import com.yf.entity.Inventory;
import com.yf.entity.Store;
import com.yf.exception.BusinessException;
import com.yf.mapper.DrugBatchMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import com.yf.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryMapper inventoryMapper;
    private final DrugMapper drugMapper;
    private final DrugBatchMapper drugBatchMapper;
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
                    
                    // 计算缺口数量
                    BigDecimal qty = inv.getQuantity() != null ? inv.getQuantity() : BigDecimal.ZERO;
                    BigDecimal safe = inv.getSafeStock() != null ? inv.getSafeStock() : BigDecimal.ZERO;
                    BigDecimal shortage = safe.subtract(qty);
                    map.put("shortage", shortage.compareTo(BigDecimal.ZERO) > 0 ? shortage : BigDecimal.ZERO);
                    
                    // 药品信息
                    Drug drug = finalDrugMap.get(inv.getDrugId());
                    if (drug != null) {
                        map.put("drugName", drug.getGenericName());
                        map.put("specification", drug.getSpecification());
                        map.put("manufacturer", drug.getManufacturer());
                        map.put("approvalNo", drug.getApprovalNo());
                    } else {
                        map.put("drugName", "");
                        map.put("specification", "");
                        map.put("manufacturer", "");
                        map.put("approvalNo", "");
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
        
        // 使用 selectList + LIMIT 1 避免多结果异常
        wrapper.last("LIMIT 1");
        return inventoryMapper.selectOne(wrapper);
    }
    
    /**
     * 渐进式查找库存记录（用于出库/入库时定位库存行）
     * 查找顺序：精确匹配 -> storeId+drugId -> drugId+batchId -> drugId
     */
    private Inventory findInventoryForAdjust(Long storeId, Long drugId, Long batchId) {
        // 1. 精确匹配：storeId + drugId + batchId
        if (batchId != null && storeId != null) {
            LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
            w.eq(Inventory::getStoreId, storeId)
             .eq(Inventory::getDrugId, drugId)
             .eq(Inventory::getBatchId, batchId)
             .last("LIMIT 1");
            Inventory inv = inventoryMapper.selectOne(w);
            if (inv != null) return inv;
        }
        
        // 2. storeId + drugId（忽略batchId），取有库存的第一条
        if (storeId != null) {
            LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
            w.eq(Inventory::getStoreId, storeId)
             .eq(Inventory::getDrugId, drugId)
             .gt(Inventory::getQuantity, 0)
             .last("LIMIT 1");
            Inventory inv = inventoryMapper.selectOne(w);
            if (inv != null) return inv;
            
            // 2b. storeId + drugId，含零库存（用于入库场景）
            LambdaQueryWrapper<Inventory> w2 = new LambdaQueryWrapper<>();
            w2.eq(Inventory::getStoreId, storeId)
              .eq(Inventory::getDrugId, drugId)
              .last("LIMIT 1");
            inv = inventoryMapper.selectOne(w2);
            if (inv != null) return inv;
        }
        
        // 3. drugId + batchId（storeId不匹配时降级查找）
        if (batchId != null) {
            LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
            w.eq(Inventory::getDrugId, drugId)
             .eq(Inventory::getBatchId, batchId)
             .last("LIMIT 1");
            Inventory inv = inventoryMapper.selectOne(w);
            if (inv != null) return inv;
        }
        
        // 4. 仅drugId，取有库存的第一条
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
        w.eq(Inventory::getDrugId, drugId)
         .gt(Inventory::getQuantity, 0)
         .last("LIMIT 1");
        Inventory inv = inventoryMapper.selectOne(w);
        if (inv != null) return inv;
        
        // 5. 仅drugId，任意一条（包含零库存）
        LambdaQueryWrapper<Inventory> w2 = new LambdaQueryWrapper<>();
        w2.eq(Inventory::getDrugId, drugId)
          .last("LIMIT 1");
        return inventoryMapper.selectOne(w2);
    }
    
    /**
     * 调整库存（出库时支持跨批次 FEFO 扣减）
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long storeId, Long drugId, Long batchId, BigDecimal quantity, String type) {
        if ("IN".equals(type)) {
            // 入库：直接加到指定记录
            Inventory inventory = findInventoryForAdjust(storeId, drugId, batchId);
            if (inventory == null) {
                log.warn("库存记录不存在: storeId={}, drugId={}, batchId={}", storeId, drugId, batchId);
                throw new BusinessException("库存记录不存在(drugId=" + drugId + ")");
            }
            inventory.setQuantity(inventory.getQuantity().add(quantity));
            inventoryMapper.updateById(inventory);
            return;
        }

        if (!"OUT".equals(type)) {
            throw new BusinessException("无效的调整类型");
        }

        // 出库：先尝试从指定批次扣
        Inventory primary = findInventoryForAdjust(storeId, drugId, batchId);
        if (primary != null && primary.getQuantity().compareTo(quantity) >= 0) {
            // 指定批次足够，直接扣
            primary.setQuantity(primary.getQuantity().subtract(quantity));
            inventoryMapper.updateById(primary);
            return;
        }

        // 指定批次不够，按 FEFO 顺序从所有批次扣减
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
        w.eq(Inventory::getDrugId, drugId)
         .gt(Inventory::getQuantity, 0);
        if (storeId != null) {
            w.eq(Inventory::getStoreId, storeId);
        }
        List<Inventory> invList = inventoryMapper.selectList(w);

        // 按到期日排序（FEFO），无批次的排最后
        Map<Long, DrugBatch> batchCache = new HashMap<>();
        if (!invList.isEmpty()) {
            List<Long> bIds = invList.stream().map(Inventory::getBatchId)
                    .filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
            if (!bIds.isEmpty()) {
                for (DrugBatch b : drugBatchMapper.selectBatchIds(bIds)) {
                    batchCache.put(b.getId(), b);
                }
            }
        }
        invList.sort((a, b) -> {
            java.time.LocalDate ea = a.getBatchId() != null && batchCache.containsKey(a.getBatchId())
                    ? batchCache.get(a.getBatchId()).getExpireDate() : null;
            java.time.LocalDate eb = b.getBatchId() != null && batchCache.containsKey(b.getBatchId())
                    ? batchCache.get(b.getBatchId()).getExpireDate() : null;
            if (ea == null && eb == null) return 0;
            if (ea == null) return 1;
            if (eb == null) return -1;
            return ea.compareTo(eb);
        });

        BigDecimal totalAvailable = invList.stream()
                .map(Inventory::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailable.compareTo(quantity) < 0) {
            log.warn("库存不足(FEFO): storeId={}, drugId={}, 请求={}, 总可用={}", storeId, drugId, quantity, totalAvailable);
            throw new BusinessException("库存不足(drugId=" + drugId + ",需要" + quantity + ",现有" + totalAvailable + ")");
        }

        BigDecimal remaining = quantity;
        for (Inventory inv : invList) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal available = inv.getQuantity();
            if (available.compareTo(remaining) >= 0) {
                inv.setQuantity(available.subtract(remaining));
                remaining = BigDecimal.ZERO;
            } else {
                remaining = remaining.subtract(available);
                inv.setQuantity(BigDecimal.ZERO);
            }
            inventoryMapper.updateById(inv);
        }
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
