package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Inventory;
import com.yf.entity.StockIn;
import com.yf.entity.StockInDetail;
import com.yf.exception.BusinessException;
import com.yf.mapper.StockInDetailMapper;
import com.yf.mapper.StockInMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单服务
 */
@Service
@RequiredArgsConstructor
public class StockInService {
    
    private final StockInMapper stockInMapper;
    private final StockInDetailMapper stockInDetailMapper;
    private final InventoryService inventoryService;
    
    /**
     * 分页查询入库单
     */
    public Page<StockIn> page(Long storeId, String status, int pageNum, int pageSize) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(StockIn::getStoreId, storeId);
        }
        if (status != null) {
            wrapper.eq(StockIn::getStatus, status);
        }
        
        wrapper.orderByDesc(StockIn::getCreatedAt);
        return stockInMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID获取入库单
     */
    public StockIn getById(Long id) {
        return stockInMapper.selectById(id);
    }
    
    /**
     * 获取入库单明细
     */
    public List<StockInDetail> getDetails(Long stockInId) {
        LambdaQueryWrapper<StockInDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockInDetail::getStockInId, stockInId);
        return stockInDetailMapper.selectList(wrapper);
    }
    
    /**
     * 创建入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public StockIn create(StockIn stockIn, List<StockInDetail> details) {
        // 生成入库单号
        String orderNo = "SI" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        stockIn.setOrderNo(orderNo);
        stockIn.setStatus("待审核");
        
        // 计算总金额
        BigDecimal totalAmount = details.stream()
                .map(StockInDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stockIn.setTotalAmount(totalAmount);
        
        stockInMapper.insert(stockIn);
        
        // 保存明细
        for (StockInDetail detail : details) {
            detail.setStockInId(stockIn.getId());
            stockInDetailMapper.insert(detail);
        }
        
        return stockIn;
    }
    
    /**
     * 审核入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long approvedBy) {
        StockIn stockIn = stockInMapper.selectById(id);
        if (stockIn == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"待审核".equals(stockIn.getStatus())) {
            throw new BusinessException("入库单状态不正确");
        }
        
        stockIn.setStatus("已审核");
        stockIn.setApprovedBy(approvedBy);
        stockIn.setApprovedAt(LocalDateTime.now());
        stockInMapper.updateById(stockIn);
    }
    
    /**
     * 完成入库（更新库存）
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        StockIn stockIn = stockInMapper.selectById(id);
        if (stockIn == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"已审核".equals(stockIn.getStatus())) {
            throw new BusinessException("入库单未审核");
        }
        
        // 获取明细并更新库存
        List<StockInDetail> details = getDetails(id);
        for (StockInDetail detail : details) {
            Inventory inventory = inventoryService.getByStoreAndDrug(
                    stockIn.getStoreId(), detail.getDrugId(), null);
            
            if (inventory == null) {
                // 创建新的库存记录
                inventory = new Inventory();
                inventory.setStoreId(stockIn.getStoreId());
                inventory.setDrugId(detail.getDrugId());
                inventory.setBatchNo(detail.getBatchNo());
                inventory.setQuantity(detail.getQuantity());
                inventory.setUnit(detail.getUnit());
                inventory.setCostPrice(detail.getPurchasePrice());
                inventoryService.create(inventory);
            } else {
                // 更新库存数量
                inventoryService.adjustStock(stockIn.getStoreId(), detail.getDrugId(), 
                        inventory.getBatchId(), detail.getQuantity(), "IN");
            }
        }
        
        stockIn.setStatus("已入库");
        stockInMapper.updateById(stockIn);
    }
}
