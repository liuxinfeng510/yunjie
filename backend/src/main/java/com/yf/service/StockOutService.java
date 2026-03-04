package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.StockOut;
import com.yf.entity.StockOutDetail;
import com.yf.entity.Store;
import com.yf.exception.BusinessException;
import com.yf.mapper.StockOutDetailMapper;
import com.yf.mapper.StockOutMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockOutService {

    private final StockOutMapper stockOutMapper;
    private final StockOutDetailMapper stockOutDetailMapper;
    private final InventoryService inventoryService;
    private final StoreService storeService;

    public Page<StockOut> page(Long storeId, String type, String status, int pageNum, int pageSize) {
        Page<StockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();

        if (storeId != null) {
            wrapper.eq(StockOut::getStoreId, storeId);
        }
        if (type != null) {
            wrapper.eq(StockOut::getType, type);
        }
        if (status != null) {
            wrapper.eq(StockOut::getStatus, status);
        }

        wrapper.orderByDesc(StockOut::getCreatedAt);
        return stockOutMapper.selectPage(page, wrapper);
    }

    public StockOut getById(Long id) {
        return stockOutMapper.selectById(id);
    }

    public List<StockOutDetail> getDetails(Long stockOutId) {
        LambdaQueryWrapper<StockOutDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOutDetail::getStockOutId, stockOutId);
        return stockOutDetailMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public StockOut create(StockOut stockOut, List<StockOutDetail> details) {
        String orderNo = "SO" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        stockOut.setOrderNo(orderNo);
        stockOut.setStatus("待审核");

        // 自动填充门店ID
        if (stockOut.getStoreId() == null) {
            Store hq = storeService.getHeadquarter();
            if (hq != null) {
                stockOut.setStoreId(hq.getId());
            } else {
                java.util.List<Store> stores = storeService.listAll();
                if (!stores.isEmpty()) {
                    stockOut.setStoreId(stores.get(0).getId());
                }
            }
        }

        BigDecimal totalAmount = details.stream()
                .map(StockOutDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stockOut.setTotalAmount(totalAmount);

        stockOutMapper.insert(stockOut);

        for (StockOutDetail detail : details) {
            detail.setStockOutId(stockOut.getId());
            stockOutDetailMapper.insert(detail);
        }

        return stockOut;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException("出库单不存在");
        }
        if (!"待审核".equals(stockOut.getStatus())) {
            throw new BusinessException("出库单状态不正确");
        }

        stockOut.setStatus("已审核");
        stockOutMapper.updateById(stockOut);
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException("出库单不存在");
        }
        if (!"已审核".equals(stockOut.getStatus())) {
            throw new BusinessException("出库单未审核");
        }

        List<StockOutDetail> details = getDetails(id);
        for (StockOutDetail detail : details) {
            inventoryService.adjustStock(stockOut.getStoreId(), detail.getDrugId(),
                    detail.getBatchId(), detail.getQuantity(), "OUT");
        }

        stockOut.setStatus("已出库");
        stockOutMapper.updateById(stockOut);
    }
}
