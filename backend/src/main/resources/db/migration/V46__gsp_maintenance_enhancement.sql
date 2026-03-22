-- 1. 近效期药品催销记录表
CREATE TABLE IF NOT EXISTS near_expiry_sale_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  store_id BIGINT,
  report_month VARCHAR(7) COMMENT '催销月份 2026-03',
  drug_id BIGINT,
  drug_name VARCHAR(200),
  specification VARCHAR(100),
  manufacturer VARCHAR(200),
  batch_id BIGINT,
  batch_no VARCHAR(50),
  expire_date DATE,
  remaining_days INT,
  stock_quantity DECIMAL(12,2),
  unit VARCHAR(20),
  sale_measure VARCHAR(30) COMMENT 'promotion/return_supplier/markdown/report_loss',
  measure_detail VARCHAR(500),
  status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/processing/completed',
  handler_name VARCHAR(50),
  complete_time DATETIME,
  result_remark VARCHAR(500),
  created_by BIGINT,
  created_at DATETIME,
  updated_by BIGINT,
  updated_at DATETIME,
  deleted INT DEFAULT 0,
  INDEX idx_report_month(report_month),
  INDEX idx_batch_month(batch_id, report_month)
);

-- 2. drug_maintenance 表增加字段（忽略已存在的列）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='drug_maintenance' AND COLUMN_NAME='batch_no');
SET @sql := IF(@exist=0, 'ALTER TABLE drug_maintenance ADD COLUMN batch_no VARCHAR(50) AFTER drug_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='drug_maintenance' AND COLUMN_NAME='specification');
SET @sql := IF(@exist=0, 'ALTER TABLE drug_maintenance ADD COLUMN specification VARCHAR(100) AFTER batch_no', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='drug_maintenance' AND COLUMN_NAME='manufacturer');
SET @sql := IF(@exist=0, 'ALTER TABLE drug_maintenance ADD COLUMN manufacturer VARCHAR(200) AFTER specification', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='drug_maintenance' AND COLUMN_NAME='is_key_drug');
SET @sql := IF(@exist=0, 'ALTER TABLE drug_maintenance ADD COLUMN is_key_drug TINYINT(1) DEFAULT 0 AFTER next_maintenance', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. defective_drug 表（含全部字段）
CREATE TABLE IF NOT EXISTS defective_drug (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  store_id BIGINT,
  register_no VARCHAR(50),
  drug_id BIGINT,
  drug_name VARCHAR(200),
  specification VARCHAR(100),
  manufacturer VARCHAR(200),
  batch_id BIGINT,
  batch_no VARCHAR(50),
  expire_date DATE,
  defect_reason VARCHAR(30),
  defect_description VARCHAR(500),
  quantity DECIMAL(12,2),
  unit VARCHAR(20),
  discovery_date DATE,
  discoverer_id BIGINT,
  discoverer_name VARCHAR(50),
  images TEXT,
  isolation_location VARCHAR(100),
  disposal_method VARCHAR(30),
  disposal_date DATE,
  disposal_handler_name VARCHAR(50),
  status VARCHAR(20) DEFAULT 'locked',
  destruction_id BIGINT,
  remark VARCHAR(500),
  created_by BIGINT,
  created_at DATETIME,
  updated_by BIGINT,
  updated_at DATETIME,
  deleted INT DEFAULT 0,
  INDEX idx_store(store_id),
  INDEX idx_status(status),
  INDEX idx_register_no(register_no)
);
