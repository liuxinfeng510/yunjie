-- 药品表增加原系统编号字段（用于数据迁移时关联库存）
ALTER TABLE drug ADD COLUMN original_code VARCHAR(50) NULL COMMENT '原系统编号' AFTER barcode;
CREATE INDEX idx_drug_original_code ON drug(original_code);
