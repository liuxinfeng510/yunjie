package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.RefundOrder;
import com.yf.entity.SaleOrder;
import com.yf.entity.SaleOrderDetail;
import com.yf.exception.BusinessException;
import com.yf.mapper.RefundOrderMapper;
import com.yf.mapper.SaleOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundOrderService {

    private final RefundOrderMapper refundOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderService saleOrderService;
    private final InventoryService inventoryService;

    public Page<RefundOrder> page(Long storeId, String status, int pageNum, int pageSize) {
        Page<RefundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RefundOrder> wrapper = new LambdaQueryWrapper<>();

        if (storeId != null) {
            wrapper.eq(RefundOrder::getStoreId, storeId);
        }
        if (status != null) {
            wrapper.eq(RefundOrder::getStatus, status);
        }

        wrapper.orderByDesc(RefundOrder::getCreatedAt);
        return refundOrderMapper.selectPage(page, wrapper);
    }

    public RefundOrder getById(Long id) {
        return refundOrderMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefundOrder create(RefundOrder refundOrder) {
        SaleOrder saleOrder = saleOrderMapper.selectById(refundOrder.getSaleOrderId());
        if (saleOrder == null) {
            throw new BusinessException("原销售订单不存在");
        }
        if ("已退款".equals(saleOrder.getStatus())) {
            throw new BusinessException("该订单已退款");
        }

        String refundNo = "RF" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        refundOrder.setRefundNo(refundNo);
        refundOrder.setStoreId(saleOrder.getStoreId());
        refundOrder.setStatus("待审核");

        refundOrderMapper.insert(refundOrder);
        return refundOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long approvedBy) {
        RefundOrder refundOrder = refundOrderMapper.selectById(id);
        if (refundOrder == null) {
            throw new BusinessException("退款单不存在");
        }
        if (!"待审核".equals(refundOrder.getStatus())) {
            throw new BusinessException("退款单状态不正确");
        }

        // 退货入库：将原订单明细的药品退回库存
        SaleOrder saleOrder = saleOrderMapper.selectById(refundOrder.getSaleOrderId());
        List<SaleOrderDetail> details = saleOrderService.getDetails(refundOrder.getSaleOrderId());
        for (SaleOrderDetail detail : details) {
            inventoryService.adjustStock(saleOrder.getStoreId(), detail.getDrugId(),
                    detail.getBatchId(), detail.getQuantity(), "IN");
        }

        // 更新退款单
        refundOrder.setStatus("已退款");
        refundOrder.setApprovedBy(approvedBy);
        refundOrder.setApprovedAt(LocalDateTime.now());
        refundOrderMapper.updateById(refundOrder);

        // 更新原订单状态
        saleOrder.setStatus("已退款");
        saleOrderMapper.updateById(saleOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, Long approvedBy) {
        RefundOrder refundOrder = refundOrderMapper.selectById(id);
        if (refundOrder == null) {
            throw new BusinessException("退款单不存在");
        }
        if (!"待审核".equals(refundOrder.getStatus())) {
            throw new BusinessException("退款单状态不正确");
        }

        refundOrder.setStatus("已拒绝");
        refundOrder.setApprovedBy(approvedBy);
        refundOrder.setApprovedAt(LocalDateTime.now());
        refundOrderMapper.updateById(refundOrder);
    }
}
