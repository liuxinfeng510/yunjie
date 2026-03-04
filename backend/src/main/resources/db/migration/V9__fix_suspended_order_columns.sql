-- V9: 修复suspended_order表字段名
-- 将 create_by 改为 created_by, update_by 改为 updated_by 以匹配 BaseEntity

ALTER TABLE suspended_order 
    CHANGE COLUMN create_by created_by BIGINT,
    CHANGE COLUMN update_by updated_by BIGINT;
