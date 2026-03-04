package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.entity.SuspendedOrder;
import com.yf.entity.SysConfig;
import com.yf.mapper.SuspendedOrderMapper;
import com.yf.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 挂单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuspendedOrderService {

    private final SuspendedOrderMapper suspendedOrderMapper;
    private final SysConfigMapper sysConfigMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建挂单
     */
    @Transactional
    public SuspendedOrder create(SuspendedOrder order) {
        // 生成挂单号
        order.setOrderNo(generateOrderNo());
        order.setSuspendedAt(LocalDateTime.now());
        order.setStatus(1);
        
        // 计算过期时间
        int expireMinutes = getExpireMinutes();
        order.setExpireAt(LocalDateTime.now().plusMinutes(expireMinutes));
        
        suspendedOrderMapper.insert(order);
        return order;
    }

    /**
     * 获取有效挂单列表
     */
    public List<SuspendedOrder> listActive(Long storeId) {
        LambdaQueryWrapper<SuspendedOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuspendedOrder::getStatus, 1)
               .gt(SuspendedOrder::getExpireAt, LocalDateTime.now())
               .orderByDesc(SuspendedOrder::getSuspendedAt);
        if (storeId != null) {
            wrapper.eq(SuspendedOrder::getStoreId, storeId);
        }
        return suspendedOrderMapper.selectList(wrapper);
    }

    /**
     * 获取挂单详情
     */
    public SuspendedOrder getById(Long id) {
        return suspendedOrderMapper.selectById(id);
    }

    /**
     * 取单（更新状态为已取单）
     */
    @Transactional
    public SuspendedOrder retrieve(Long id) {
        SuspendedOrder order = suspendedOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("挂单不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("挂单已被取走或已过期");
        }
        if (order.getExpireAt().isBefore(LocalDateTime.now())) {
            order.setStatus(-1);
            suspendedOrderMapper.updateById(order);
            throw new RuntimeException("挂单已过期");
        }
        
        order.setStatus(0);
        suspendedOrderMapper.updateById(order);
        return order;
    }

    /**
     * 删除挂单
     */
    public void delete(Long id) {
        suspendedOrderMapper.deleteById(id);
    }

    /**
     * 获取挂单过期时间配置（分钟）
     */
    public int getExpireMinutes() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, "suspended_order_expire_minutes");
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                log.warn("挂单过期时间配置值无效，使用默认值60分钟");
            }
        }
        return 60; // 默认60分钟
    }

    /**
     * 更新挂单过期时间配置
     */
    @Transactional
    public void updateExpireMinutes(int minutes) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, "suspended_order_expire_minutes");
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        if (config != null) {
            config.setConfigValue(String.valueOf(minutes));
            sysConfigMapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey("suspended_order_expire_minutes");
            config.setConfigValue(String.valueOf(minutes));
            config.setDescription("挂单过期时间(分钟)");
            config.setConfigGroup("sale");
            sysConfigMapper.insert(config);
        }
    }

    /**
     * 生成挂单号
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "GD" + dateStr + random;
    }

    /**
     * 定时任务：清理过期挂单
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cleanExpiredOrders() {
        LambdaQueryWrapper<SuspendedOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuspendedOrder::getStatus, 1)
               .lt(SuspendedOrder::getExpireAt, LocalDateTime.now());
        
        List<SuspendedOrder> expiredOrders = suspendedOrderMapper.selectList(wrapper);
        for (SuspendedOrder order : expiredOrders) {
            order.setStatus(-1);
            suspendedOrderMapper.updateById(order);
        }
        
        if (!expiredOrders.isEmpty()) {
            log.info("已清理{}个过期挂单", expiredOrders.size());
        }
    }
}
