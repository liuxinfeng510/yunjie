package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Inventory;
import com.yf.entity.StockCheck;
import com.yf.entity.StockCheckDetail;
import com.yf.exception.BusinessException;
import com.yf.mapper.InventoryMapper;
import com.yf.mapper.StockCheckDetailMapper;
import com.yf.mapper.StockCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCheckService {

    private final StockCheckMapper stockCheckMapper;
    private final StockCheckDetailMapper stockCheckDetailMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryService inventoryService;

    public Page<StockCheck> page(Long storeId, String type, String status, int pageNum, int pageSize) {
        Page<StockCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();

        if (storeId != null) {
            wrapper.eq(StockCheck::getStoreId, storeId);
        }
        if (type != null) {
            wrapper.eq(StockCheck::getType, type);
        }
        if (status != null) {
            wrapper.eq(StockCheck::getStatus, status);
        }

        wrapper.orderByDesc(StockCheck::getCreatedAt);
        return stockCheckMapper.selectPage(page, wrapper);
    }

    public StockCheck getById(Long id) {
        return stockCheckMapper.selectById(id);
    }

    public List<StockCheckDetail> getDetails(Long stockCheckId) {
        LambdaQueryWrapper<StockCheckDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockCheckDetail::getStockCheckId, stockCheckId);
        return stockCheckDetailMapper.selectList(wrapper);
    }

    /**
     * 创建盘点单，自动填充系统库存数量
     */
    @Transactional(rollbackFor = Exception.class)
    public StockCheck create(StockCheck stockCheck) {
        String orderNo = "SC" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        stockCheck.setOrderNo(orderNo);
        stockCheck.setStatus("盘点中");

        stockCheckMapper.insert(stockCheck);

        // 查询该门店所有库存，自动生成盘点明细
        LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
        invWrapper.eq(Inventory::getStoreId, stockCheck.getStoreId());
        if ("抽盘".equals(stockCheck.getType())) {
            // 抽盘只取库存量>0的
            invWrapper.gt(Inventory::getQuantity, BigDecimal.ZERO);
        }

        List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
        for (Inventory inv : inventories) {
            StockCheckDetail detail = new StockCheckDetail();
            detail.setStockCheckId(stockCheck.getId());
            detail.setDrugId(inv.getDrugId());
            detail.setBatchId(inv.getBatchId());
            detail.setSystemQuantity(inv.getQuantity());
            detail.setActualQuantity(null); // 待盘点
            detail.setDiffQuantity(BigDecimal.ZERO);
            stockCheckDetailMapper.insert(detail);
        }

        return stockCheck;
    }

    /**
     * 更新盘点明细（录入实际数量）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(Long detailId, BigDecimal actualQuantity, String diffReason) {
        StockCheckDetail detail = stockCheckDetailMapper.selectById(detailId);
        if (detail == null) {
            throw new BusinessException("盘点明细不存在");
        }

        detail.setActualQuantity(actualQuantity);
        detail.setDiffQuantity(actualQuantity.subtract(detail.getSystemQuantity()));
        detail.setDiffReason(diffReason);
        stockCheckDetailMapper.updateById(detail);
    }

    /**
     * 完成盘点，将差异同步到库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        StockCheck stockCheck = stockCheckMapper.selectById(id);
        if (stockCheck == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (!"盘点中".equals(stockCheck.getStatus())) {
            throw new BusinessException("盘点单状态不正确");
        }

        List<StockCheckDetail> details = getDetails(id);
        for (StockCheckDetail detail : details) {
            if (detail.getActualQuantity() == null) {
                throw new BusinessException("存在未盘点的明细");
            }

            BigDecimal diff = detail.getDiffQuantity();
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                // 盘盈用IN，盘亏用OUT
                String type = diff.compareTo(BigDecimal.ZERO) > 0 ? "IN" : "OUT";
                inventoryService.adjustStock(stockCheck.getStoreId(), detail.getDrugId(),
                        detail.getBatchId(), diff.abs(), type);
            }
        }

        stockCheck.setStatus("已完成");
        stockCheckMapper.updateById(stockCheck);
    }
}
