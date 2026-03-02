package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据迁移任务表 - 记录从旧系统导入数据的任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_migration_task")
public class DataMigrationTask extends BaseEntity {
    /** 任务名称 */
    private String taskName;
    /** 数据来源类型: excel / csv / api / database */
    private String sourceType;
    /** 目标模块: drug / herb / member / supplier / inventory */
    private String targetModule;
    /** 上传文件路径 */
    private String filePath;
    /** 原始文件名 */
    private String originalFileName;
    /** 任务状态: pending / processing / completed / failed */
    private String status;
    /** 总记录数 */
    private Integer totalCount;
    /** 成功数 */
    private Integer successCount;
    /** 失败数 */
    private Integer failCount;
    /** 跳过数（重复记录） */
    private Integer skipCount;
    /** 错误日志 */
    private String errorLog;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 完成时间 */
    private LocalDateTime finishTime;
}
