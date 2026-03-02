package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.AuditLog;
import com.yf.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志服务
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    /**
     * 异步保存审计日志，避免影响主业务性能
     */
    @Async
    public void saveAsync(AuditLog auditLog) {
        if (auditLog.getCreatedAt() == null) {
            auditLog.setCreatedAt(LocalDateTime.now());
        }
        auditLogMapper.insert(auditLog);
    }

    /**
     * 同步保存审计日志
     */
    public void save(AuditLog auditLog) {
        if (auditLog.getCreatedAt() == null) {
            auditLog.setCreatedAt(LocalDateTime.now());
        }
        auditLogMapper.insert(auditLog);
    }

    /**
     * 分页查询审计日志
     */
    public Page<AuditLog> page(String module, String username, LocalDateTime startTime,
                                LocalDateTime endTime, int pageNum, int pageSize) {
        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        if (module != null && !module.isEmpty()) {
            wrapper.eq(AuditLog::getModule, module);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(AuditLog::getUsername, username);
        }
        if (startTime != null) {
            wrapper.ge(AuditLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(AuditLog::getCreatedAt);
        return auditLogMapper.selectPage(page, wrapper);
    }
}
