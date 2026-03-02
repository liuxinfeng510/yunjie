package com.yf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 注意：审计日志不继承BaseEntity，因为它本身就是日志表，不需要逻辑删除和审计字段
 */
@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long userId;

    private String username;

    /** 业务模块 */
    private String module;

    /** 操作类型 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求参数 */
    private String params;

    /** 请求IP */
    private String ip;

    /** 执行结果：success/fail */
    private String result;

    /** 错误信息 */
    private String errorMsg;

    /** 执行时长(ms) */
    private Long duration;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
