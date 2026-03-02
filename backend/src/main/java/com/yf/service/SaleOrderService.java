package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.SaleOrder;
import com.yf.entity.SaleOrderDetail;
import com.yf.exception.BusinessException;
import com.yf.mapper.SaleOrderDetailMapper;
import com.yf.mapper.SaleOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售订单服务
 */
@Service
@RequiredArgsConstructor
public class SaleOrderService {
    
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderDetailMapper saleOrderDetailMapper;
    private final InventoryService inventoryService;
    private final MemberService memberService;
    
    /**
     * 分页查询销售订单
     */
    public Page<SaleOrder> page(Long storeId, String status, LocalDateTime startTime, 
                                 LocalDateTime endTime, int pageNum, int pageSize) {
        Page<SaleOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(SaleOrder::getStoreId, storeId);
        }
        if (status != null) {
            wrapper.eq(SaleOrder::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(SaleOrder::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(SaleOrder::getCreatedAt, endTime);
        }
        
        wrapper.orderByDesc(SaleOrder::getCreatedAt);
        return saleOrderMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID获取销售订单
     */
    public SaleOrder getById(Long id) {
        return saleOrderMapper.selectById(id);
    }
    
    /**
     * 获取订单明细
     */
    public List<SaleOrderDetail> getDetails(Long saleOrderId) {
        LambdaQueryWrapper<SaleOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderDetail::getSaleOrderId, saleOrderId);
        return saleOrderDetailMapper.selectList(wrapper);
    }
    
    /**
     * 创建销售订单（自动扣减库存）
     */
    @Transactional(rollbackFor = Exception.class)
    public SaleOrder create(SaleOrder saleOrder, List<SaleOrderDetail> details) {
        // 生成订单号
        String orderNo = "SO" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        saleOrder.setOrderNo(orderNo);
        saleOrder.setStatus("已完成");
        
        // 计算总金额
        BigDecimal totalAmount = details.stream()
                .map(SaleOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        saleOrder.setTotalAmount(totalAmount);
        
        // 计算实付金额（总金额 - 折扣）
        BigDecimal payAmount = totalAmount.subtract(
                saleOrder.getDiscountAmount() != null ? saleOrder.getDiscountAmount() : BigDecimal.ZERO);
        saleOrder.setPayAmount(payAmount);
        
        // 保存订单
        saleOrderMapper.insert(saleOrder);
        
        // 保存明细并扣减库存
        for (SaleOrderDetail detail : details) {
            detail.setSaleOrderId(saleOrder.getId());
            saleOrderDetailMapper.insert(detail);
            
            // 扣减库存
            inventoryService.adjustStock(saleOrder.getStoreId(), detail.getDrugId(), 
                    detail.getBatchId(), detail.getQuantity(), "OUT");
        }
        
        // 如果有会员，增加积分和消费金额
        if (saleOrder.getMemberId() != null && saleOrder.getPointsEarned() != null) {
            memberService.addPoints(saleOrder.getMemberId(), saleOrder.getPointsEarned(), 
                    "消费获得", saleOrder.getId());
        }
        
        return saleOrder;
    }
    
    /**
     * 每日销售统计
     */
    public Map<String, Object> dailyStats(Long storeId, LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrder::getStoreId, storeId)
               .ge(SaleOrder::getCreatedAt, startTime)
               .lt(SaleOrder::getCreatedAt, endTime)
               .eq(SaleOrder::getStatus, "已完成");
        
        List<SaleOrder> orders = saleOrderMapper.selectList(wrapper);
        
        // 统计数据
        long orderCount = orders.size();
        BigDecimal totalAmount = orders.stream()
                .map(SaleOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payAmount = orders.stream()
                .map(SaleOrder::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("date", date);
        stats.put("orderCount", orderCount);
        stats.put("totalAmount", totalAmount);
        stats.put("payAmount", payAmount);
        stats.put("discountAmount", totalAmount.subtract(payAmount));
        
        return stats;
    }
}
