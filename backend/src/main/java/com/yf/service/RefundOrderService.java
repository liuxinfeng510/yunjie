package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.RefundOrder;
import com.yf.entity.RefundOrderDetail;
import com.yf.entity.SaleOrder;
import com.yf.entity.SaleOrderDetail;
import com.yf.exception.BusinessException;
import com.yf.mapper.RefundOrderDetailMapper;
import com.yf.mapper.RefundOrderMapper;
import com.yf.mapper.SaleOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundOrderService {

    private final RefundOrderMapper refundOrderMapper;
    private final RefundOrderDetailMapper refundOrderDetailMapper;
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

    /**
     * 获取退货明细
     */
    public List<RefundOrderDetail> getDetails(Long refundOrderId) {
        LambdaQueryWrapper<RefundOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundOrderDetail::getRefundOrderId, refundOrderId);
        return refundOrderDetailMapper.selectList(wrapper);
    }

    /**
     * 创建整单退货
     */
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
        refundOrder.setRefundType("full");

        refundOrderMapper.insert(refundOrder);
        return refundOrder;
    }

    /**
     * 创建单品退货
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundOrder createPartial(RefundOrder refundOrder, List<RefundOrderDetail> details) {
        SaleOrder saleOrder = saleOrderMapper.selectById(refundOrder.getSaleOrderId());
        if (saleOrder == null) {
            throw new BusinessException("原销售订单不存在");
        }

        String refundNo = "RF" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        refundOrder.setRefundNo(refundNo);
        refundOrder.setStoreId(saleOrder.getStoreId());
        refundOrder.setStatus("待审核");
        refundOrder.setRefundType("partial");

        // 计算退款总金额
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (RefundOrderDetail detail : details) {
            BigDecimal itemAmount = detail.getUnitPrice().multiply(detail.getQuantity());
            detail.setRefundAmount(itemAmount);
            totalRefund = totalRefund.add(itemAmount);
        }
        refundOrder.setRefundAmount(totalRefund);

        refundOrderMapper.insert(refundOrder);

        // 保存退货明细
        for (RefundOrderDetail detail : details) {
            detail.setRefundOrderId(refundOrder.getId());
            refundOrderDetailMapper.insert(detail);
        }

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

        SaleOrder saleOrder = saleOrderMapper.selectById(refundOrder.getSaleOrderId());

        // 根据退货类型处理库存回滚
        if ("partial".equals(refundOrder.getRefundType())) {
            // 单品退货：只退还指定商品
            List<RefundOrderDetail> details = getDetails(id);
            for (RefundOrderDetail detail : details) {
                inventoryService.adjustStock(saleOrder.getStoreId(), detail.getDrugId(),
                        detail.getBatchId(), detail.getQuantity(), "IN");
            }
        } else {
            // 整单退货：退还所有商品
            List<SaleOrderDetail> details = saleOrderService.getDetails(refundOrder.getSaleOrderId());
            for (SaleOrderDetail detail : details) {
                inventoryService.adjustStock(saleOrder.getStoreId(), detail.getDrugId(),
                        detail.getBatchId(), detail.getQuantity(), "IN");
            }
            // 更新原订单状态
            saleOrder.setStatus("已退款");
            saleOrderMapper.updateById(saleOrder);
        }

        // 更新退款单
        refundOrder.setStatus("已退款");
        refundOrder.setApprovedBy(approvedBy);
        refundOrder.setApprovedAt(LocalDateTime.now());
        refundOrderMapper.updateById(refundOrder);
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
