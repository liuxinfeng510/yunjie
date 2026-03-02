package com.yf.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.annotation.Auditable;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.AuditLog;
import com.yf.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志切面
 * 拦截标注了 @Auditable 的方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();

        AuditLog auditLog = new AuditLog();
        auditLog.setModule(auditable.module());
        auditLog.setOperation(auditable.operation());
        auditLog.setTenantId(TenantContext.getTenantId());
        auditLog.setCreatedAt(LocalDateTime.now());

        // 获取当前用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            auditLog.setUserId(userId);
        }

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            auditLog.setIp(getClientIp(request));
        }

        // 方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        auditLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

        // 请求参数（截取前2000字符）
        try {
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            auditLog.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
        } catch (Exception e) {
            auditLog.setParams("[序列化失败]");
        }

        Object result;
        try {
            result = joinPoint.proceed();
            auditLog.setResult("success");
        } catch (Throwable e) {
            auditLog.setResult("fail");
            String errorMsg = e.getMessage();
            auditLog.setErrorMsg(errorMsg != null && errorMsg.length() > 2000 ? errorMsg.substring(0, 2000) : errorMsg);
            throw e;
        } finally {
            auditLog.setDuration(System.currentTimeMillis() - startTime);
            // 异步保存日志
            try {
                auditLogService.saveAsync(auditLog);
            } catch (Exception e) {
                log.error("保存审计日志失败", e);
            }
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
