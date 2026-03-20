package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Member;
import com.yf.entity.SaleOrder;
import com.yf.entity.SaleOrderDetail;
import com.yf.entity.SysUser;
import com.yf.mapper.MemberMapper;
import com.yf.mapper.SaleOrderDetailMapper;
import com.yf.mapper.SaleOrderMapper;
import com.yf.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final MemberMapper memberMapper;
    private final SysUserMapper sysUserMapper;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 支付方式映射（数据库值 -> 前端展示值）
     */
    private static final Map<String, String> PAY_METHOD_MAP = Map.of(
            "现金", "CASH", "CASH", "CASH",
            "微信", "WECHAT", "WECHAT", "WECHAT",
            "支付宝", "ALIPAY", "ALIPAY", "ALIPAY",
            "医保", "MEDICAL_INSURANCE", "MEDICAL_INSURANCE", "MEDICAL_INSURANCE"
    );
    
    /**
     * 分页查询销售订单（带关联信息）
     */
    public Page<SaleOrder> pageWithDetails(Long storeId, String status, String orderNo,
                                            String memberName, String paymentMethod,
                                            LocalDateTime startTime, LocalDateTime endTime,
                                            int pageNum, int pageSize) {
        Page<SaleOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(SaleOrder::getStoreId, storeId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SaleOrder::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(SaleOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(paymentMethod)) {
            // 前端传的是 CASH/WECHAT 等，需要查找数据库中匹配的值
            List<String> dbValues = PAY_METHOD_MAP.entrySet().stream()
                    .filter(e -> e.getValue().equals(paymentMethod))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (!dbValues.isEmpty()) {
                wrapper.in(SaleOrder::getPayMethod, dbValues);
            }
        }
        if (startTime != null) {
            wrapper.ge(SaleOrder::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(SaleOrder::getCreatedAt, endTime);
        }
        
        // 如果按会员姓名搜索，先查出匹配的会员ID
        if (StringUtils.hasText(memberName)) {
            LambdaQueryWrapper<Member> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.like(Member::getName, memberName);
            List<Member> members = memberMapper.selectList(memberWrapper);
            if (members.isEmpty()) {
                // 没找到匹配的会员，返回空结果
                return page;
            }
            List<Long> memberIds = members.stream().map(Member::getId).collect(Collectors.toList());
            wrapper.in(SaleOrder::getMemberId, memberIds);
        }
        
        wrapper.orderByDesc(SaleOrder::getCreatedAt);
        Page<SaleOrder> result = saleOrderMapper.selectPage(page, wrapper);
        
        // 填充关联信息
        fillOrderDetails(result.getRecords());
        
        return result;
    }

    /**
     * 根据ID获取订单（带关联信息）
     */
    public SaleOrder getByIdWithDetails(Long id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order != null) {
            fillOrderDetails(List.of(order));
        }
        return order;
    }

    /**
     * 批量填充订单的关联信息（会员名、收银员名、支付方式、时间）
     */
    private void fillOrderDetails(List<SaleOrder> orders) {
        if (orders == null || orders.isEmpty()) return;
        
        // 收集所有需要查询的 memberId 和 cashierId
        Set<Long> memberIds = orders.stream()
                .map(SaleOrder::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> userIds = orders.stream()
                .map(SaleOrder::getCashierId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // 批量查询会员
        Map<Long, String> memberNameMap = new HashMap<>();
        if (!memberIds.isEmpty()) {
            List<Member> members = memberMapper.selectBatchIds(memberIds);
            members.forEach(m -> memberNameMap.put(m.getId(), m.getName()));
        }
        
        // 批量查询用户（收银员）
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            users.forEach(u -> userNameMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }
        
        // 填充字段
        for (SaleOrder order : orders) {
            if (order.getMemberId() != null) {
                order.setMemberName(memberNameMap.getOrDefault(order.getMemberId(), null));
            }
            if (order.getCashierId() != null) {
                order.setCashierName(userNameMap.getOrDefault(order.getCashierId(), null));
            }
            // payMethod -> paymentMethod
            String pm = order.getPayMethod();
            order.setPaymentMethod(pm != null ? PAY_METHOD_MAP.getOrDefault(pm, pm) : null);
            // createdAt -> createTime
            if (order.getCreatedAt() != null) {
                order.setCreateTime(order.getCreatedAt().format(TIME_FMT));
            }
        }
    }

    /**
     * 分页查询销售订单（旧版兼容）
     */
    public Page<SaleOrder> page(Long storeId, String status, LocalDateTime startTime, 
                                 LocalDateTime endTime, int pageNum, int pageSize) {
        return pageWithDetails(storeId, status, null, null, null, startTime, endTime, pageNum, pageSize);
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
        // 自动设置收银员ID（从当前登录用户获取）
        if (saleOrder.getCashierId() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                saleOrder.setCashierId(userId);
            }
        }

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
