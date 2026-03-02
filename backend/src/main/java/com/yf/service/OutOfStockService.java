package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.OutOfStockRequest;
import com.yf.mapper.OutOfStockRequestMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 缺药登记服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutOfStockService {

    private final OutOfStockRequestMapper requestMapper;

    /**
     * 创建缺药登记
     */
    @Transactional
    public OutOfStockRequest create(OutOfStockRequest request) {
        request.setStatus("pending");
        request.setNotified(false);
        requestMapper.insert(request);
        log.info("缺药登记已创建: drugName={}, phone={}", request.getDrugName(), request.getPhone());
        return request;
    }

    /**
     * 开始处理
     */
    @Transactional
    public void startProcess(Long id, Long handlerId, String handlerName) {
        OutOfStockRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("登记记录不存在");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("当前状态不允许处理");
        }
        request.setStatus("processing");
        request.setHandlerId(handlerId);
        request.setHandlerName(handlerName);
        request.setHandleTime(LocalDateTime.now());
        requestMapper.updateById(request);
    }

    /**
     * 完成处理
     */
    @Transactional
    public void resolve(Long id, String handleResult) {
        OutOfStockRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("登记记录不存在");
        }
        request.setStatus("resolved");
        request.setHandleResult(handleResult);
        requestMapper.updateById(request);
        log.info("缺药登记已解决: id={}", id);
    }

    /**
     * 取消登记
     */
    @Transactional
    public void cancel(Long id, String reason) {
        OutOfStockRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("登记记录不存在");
        }
        request.setStatus("cancelled");
        request.setHandleResult(reason);
        requestMapper.updateById(request);
    }

    /**
     * 标记已通知客户
     */
    @Transactional
    public void markNotified(Long id) {
        OutOfStockRequest request = requestMapper.selectById(id);
        if (request != null) {
            request.setNotified(true);
            request.setNotifyTime(LocalDateTime.now());
            requestMapper.updateById(request);
        }
    }

    /**
     * 根据ID查询
     */
    public OutOfStockRequest getById(Long id) {
        return requestMapper.selectById(id);
    }

    /**
     * 分页查询
     */
    public Page<OutOfStockRequest> page(Long storeId, String status, String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<OutOfStockRequest> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(OutOfStockRequest::getStoreId, storeId);
        if (status != null && !status.isEmpty()) wrapper.eq(OutOfStockRequest::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(OutOfStockRequest::getDrugName, keyword)
                    .or().like(OutOfStockRequest::getMemberName, keyword)
                    .or().like(OutOfStockRequest::getPhone, keyword));
        }
        wrapper.orderByDesc(OutOfStockRequest::getCreatedAt);
        return requestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 统计信息
     */
    public OutOfStockStatistics getStatistics(Long storeId) {
        OutOfStockStatistics stats = new OutOfStockStatistics();
        LambdaQueryWrapper<OutOfStockRequest> base = new LambdaQueryWrapper<>();
        if (storeId != null) base.eq(OutOfStockRequest::getStoreId, storeId);
        
        stats.setTotal(requestMapper.selectCount(base));
        
        LambdaQueryWrapper<OutOfStockRequest> pending = new LambdaQueryWrapper<>();
        if (storeId != null) pending.eq(OutOfStockRequest::getStoreId, storeId);
        pending.eq(OutOfStockRequest::getStatus, "pending");
        stats.setPendingCount(requestMapper.selectCount(pending));
        
        LambdaQueryWrapper<OutOfStockRequest> processing = new LambdaQueryWrapper<>();
        if (storeId != null) processing.eq(OutOfStockRequest::getStoreId, storeId);
        processing.eq(OutOfStockRequest::getStatus, "processing");
        stats.setProcessingCount(requestMapper.selectCount(processing));
        
        LambdaQueryWrapper<OutOfStockRequest> resolved = new LambdaQueryWrapper<>();
        if (storeId != null) resolved.eq(OutOfStockRequest::getStoreId, storeId);
        resolved.eq(OutOfStockRequest::getStatus, "resolved");
        stats.setResolvedCount(requestMapper.selectCount(resolved));
        
        return stats;
    }

    @Data
    public static class OutOfStockStatistics {
        private Long total;
        private Long pendingCount;
        private Long processingCount;
        private Long resolvedCount;
    }
}
