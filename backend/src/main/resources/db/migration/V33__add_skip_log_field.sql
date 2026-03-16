-- 数据迁移任务表增加跳过日志字段
ALTER TABLE data_migration_task ADD COLUMN skip_log TEXT NULL COMMENT '跳过日志（记录跳过原因）' AFTER error_log;
