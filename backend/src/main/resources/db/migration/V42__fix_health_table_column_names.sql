-- V42: 修复健康画像相关表的列名，使其与BaseEntity字段映射一致
-- BaseEntity字段: createdBy->created_by, createdAt->created_at, updatedBy->updated_by, updatedAt->updated_at
-- V5创建的表使用了 create_by/update_by/create_time/update_time，需要统一

-- 修复 member_health_profile
ALTER TABLE member_health_profile
    CHANGE COLUMN create_time created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHANGE COLUMN update_time updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHANGE COLUMN create_by created_by BIGINT,
    CHANGE COLUMN update_by updated_by BIGINT;

-- 修复 chronic_disease_record
ALTER TABLE chronic_disease_record
    CHANGE COLUMN create_time created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHANGE COLUMN update_time updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHANGE COLUMN create_by created_by BIGINT,
    CHANGE COLUMN update_by updated_by BIGINT;

-- 修复 medication_reminder
ALTER TABLE medication_reminder
    CHANGE COLUMN create_time created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHANGE COLUMN update_time updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHANGE COLUMN create_by created_by BIGINT,
    CHANGE COLUMN update_by updated_by BIGINT;

-- 修复 refund_order_detail (V8创建，已有created_at/updated_at，只需修复create_by/update_by)
ALTER TABLE refund_order_detail
    CHANGE COLUMN create_by created_by BIGINT,
    CHANGE COLUMN update_by updated_by BIGINT;
