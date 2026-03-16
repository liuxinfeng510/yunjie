-- V32: 为下拉框选择的关联数据添加名称快照字段，保证历史记录不受关联数据变更影响
-- 使用存储过程使 ALTER TABLE 幂等（列存在则跳过）

DELIMITER //

DROP PROCEDURE IF EXISTS safe_add_column //

CREATE PROCEDURE safe_add_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_def VARCHAR(500))
BEGIN
    SET @db = DATABASE();
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- ========== 1. 创建 promotion 表（如不存在） ==========

CREATE TABLE IF NOT EXISTS promotion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NULL,
    store_id BIGINT NULL COMMENT '门店ID（null表示全部门店）',
    store_name VARCHAR(100) NULL COMMENT '门店名称（快照）',
    name VARCHAR(200) NOT NULL COMMENT '促销名称',
    type VARCHAR(50) NOT NULL COMMENT '促销类型：discount/reduction/gift/bundle',
    scope VARCHAR(50) NULL COMMENT '适用范围：all/category/product',
    target_ids TEXT NULL COMMENT '适用对象ID列表（JSON）',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    discount_rate DECIMAL(5,2) NULL COMMENT '折扣率',
    threshold_amount DECIMAL(12,2) NULL COMMENT '满足金额',
    reduction_amount DECIMAL(12,2) NULL COMMENT '减免金额',
    gift_drug_id BIGINT NULL COMMENT '赠品药品ID',
    gift_drug_name VARCHAR(200) NULL COMMENT '赠品药品名称（快照）',
    gift_quantity INT NULL COMMENT '赠品数量',
    bundle_price DECIMAL(12,2) NULL COMMENT '组合价格',
    member_level_ids VARCHAR(500) NULL COMMENT '会员等级限制（JSON）',
    limit_per_member INT NULL DEFAULT 0 COMMENT '每人限购次数',
    priority INT NULL DEFAULT 0 COMMENT '优先级',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态',
    description TEXT NULL COMMENT '促销描述',
    created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    deleted INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_promotion_store (store_id),
    KEY idx_promotion_status (status),
    KEY idx_promotion_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

-- ========== 2. 安全添加快照列 ==========

-- stock_in
CALL safe_add_column('stock_in', 'supplier_name', "VARCHAR(200) NULL COMMENT '供应商名称（快照）'");
CALL safe_add_column('stock_in', 'store_name', "VARCHAR(100) NULL COMMENT '门店名称（快照）'");

-- stock_in_detail
CALL safe_add_column('stock_in_detail', 'drug_name', "VARCHAR(200) NULL COMMENT '药品通用名（快照）'");
CALL safe_add_column('stock_in_detail', 'specification', "VARCHAR(100) NULL COMMENT '规格（快照）'");
CALL safe_add_column('stock_in_detail', 'manufacturer', "VARCHAR(200) NULL COMMENT '生产企业（快照）'");

-- stock_out
CALL safe_add_column('stock_out', 'store_name', "VARCHAR(100) NULL COMMENT '门店名称（快照）'");
CALL safe_add_column('stock_out', 'target_store_name', "VARCHAR(100) NULL COMMENT '目标门店名称（快照）'");

-- stock_out_detail
CALL safe_add_column('stock_out_detail', 'drug_name', "VARCHAR(200) NULL COMMENT '药品通用名（快照）'");
CALL safe_add_column('stock_out_detail', 'specification', "VARCHAR(100) NULL COMMENT '规格（快照）'");
CALL safe_add_column('stock_out_detail', 'manufacturer', "VARCHAR(200) NULL COMMENT '生产企业（快照）'");

-- stock_check_detail
CALL safe_add_column('stock_check_detail', 'drug_name', "VARCHAR(200) NULL COMMENT '药品通用名（快照）'");
CALL safe_add_column('stock_check_detail', 'specification', "VARCHAR(100) NULL COMMENT '规格（快照）'");
CALL safe_add_column('stock_check_detail', 'batch_no', "VARCHAR(50) NULL COMMENT '批次号（快照）'");
CALL safe_add_column('stock_check_detail', 'unit', "VARCHAR(20) NULL COMMENT '单位（快照）'");

-- drug_acceptance
CALL safe_add_column('drug_acceptance', 'supplier_name', "VARCHAR(200) NULL COMMENT '供应商名称（快照）'");

-- drug_maintenance
CALL safe_add_column('drug_maintenance', 'operator_name', "VARCHAR(50) NULL COMMENT '养护人姓名（快照）'");

-- promotion (如果已通过 CREATE TABLE 创建则跳过)
CALL safe_add_column('promotion', 'store_name', "VARCHAR(100) NULL COMMENT '门店名称（快照）'");
CALL safe_add_column('promotion', 'gift_drug_name', "VARCHAR(200) NULL COMMENT '赠品药品名称（快照）'");

-- herb_cabinet_fill_record
CALL safe_add_column('herb_cabinet_fill_record', 'target_cabinet_name', "VARCHAR(100) NULL COMMENT '目标斗柜名称（快照）'");
CALL safe_add_column('herb_cabinet_fill_record', 'target_cell_label', "VARCHAR(50) NULL COMMENT '目标斗格标签（快照）'");
CALL safe_add_column('herb_cabinet_fill_record', 'operator_name', "VARCHAR(50) NULL COMMENT '操作员姓名（快照）'");

-- herb_dispensing_record
CALL safe_add_column('herb_dispensing_record', 'dispenser_name', "VARCHAR(50) NULL COMMENT '配药人姓名（快照）'");

-- first_marketing_drug
CALL safe_add_column('first_marketing_drug', 'first_supplier_name', "VARCHAR(200) NULL COMMENT '首营供应商名称（快照）'");

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS safe_add_column;

-- ========== 3. 存量数据回填 ==========

-- stock_in.supplier_name
UPDATE stock_in a LEFT JOIN supplier b ON a.supplier_id = b.id AND b.deleted = 0
SET a.supplier_name = b.name
WHERE a.supplier_name IS NULL AND a.supplier_id IS NOT NULL AND a.deleted = 0;

-- stock_in.store_name
UPDATE stock_in a LEFT JOIN store b ON a.store_id = b.id AND b.deleted = 0
SET a.store_name = b.name
WHERE a.store_name IS NULL AND a.store_id IS NOT NULL AND a.deleted = 0;

-- stock_in_detail (drug_name, specification, manufacturer)
UPDATE stock_in_detail a LEFT JOIN drug b ON a.drug_id = b.id AND b.deleted = 0
SET a.drug_name = b.generic_name, a.specification = b.specification, a.manufacturer = b.manufacturer
WHERE a.drug_name IS NULL AND a.drug_id IS NOT NULL AND a.deleted = 0;

-- stock_out.store_name
UPDATE stock_out a LEFT JOIN store b ON a.store_id = b.id AND b.deleted = 0
SET a.store_name = b.name
WHERE a.store_name IS NULL AND a.store_id IS NOT NULL AND a.deleted = 0;

-- stock_out.target_store_name
UPDATE stock_out a LEFT JOIN store b ON a.target_store_id = b.id AND b.deleted = 0
SET a.target_store_name = b.name
WHERE a.target_store_name IS NULL AND a.target_store_id IS NOT NULL AND a.deleted = 0;

-- stock_out_detail (drug_name, specification, manufacturer)
UPDATE stock_out_detail a LEFT JOIN drug b ON a.drug_id = b.id AND b.deleted = 0
SET a.drug_name = b.generic_name, a.specification = b.specification, a.manufacturer = b.manufacturer
WHERE a.drug_name IS NULL AND a.drug_id IS NOT NULL AND a.deleted = 0;

-- stock_check_detail (drug_name, specification, unit)
UPDATE stock_check_detail a LEFT JOIN drug b ON a.drug_id = b.id AND b.deleted = 0
SET a.drug_name = b.generic_name, a.specification = b.specification, a.unit = b.unit
WHERE a.drug_name IS NULL AND a.drug_id IS NOT NULL AND a.deleted = 0;

-- stock_check_detail.batch_no
UPDATE stock_check_detail a LEFT JOIN drug_batch b ON a.batch_id = b.id AND b.deleted = 0
SET a.batch_no = b.batch_no
WHERE a.batch_no IS NULL AND a.batch_id IS NOT NULL AND a.deleted = 0;

-- drug_acceptance.supplier_name
UPDATE drug_acceptance a LEFT JOIN supplier b ON a.supplier_id = b.id AND b.deleted = 0
SET a.supplier_name = b.name
WHERE a.supplier_name IS NULL AND a.supplier_id IS NOT NULL AND a.deleted = 0;

-- drug_maintenance.operator_name
UPDATE drug_maintenance a LEFT JOIN sys_user b ON a.operator_id = b.id AND b.deleted = 0
SET a.operator_name = b.real_name
WHERE a.operator_name IS NULL AND a.operator_id IS NOT NULL AND a.deleted = 0;

-- promotion.store_name
UPDATE promotion a LEFT JOIN store b ON a.store_id = b.id AND b.deleted = 0
SET a.store_name = b.name
WHERE a.store_name IS NULL AND a.store_id IS NOT NULL AND a.deleted = 0;

-- promotion.gift_drug_name
UPDATE promotion a LEFT JOIN drug b ON a.gift_drug_id = b.id AND b.deleted = 0
SET a.gift_drug_name = b.generic_name
WHERE a.gift_drug_name IS NULL AND a.gift_drug_id IS NOT NULL AND a.deleted = 0;

-- herb_cabinet_fill_record.target_cabinet_name
UPDATE herb_cabinet_fill_record a LEFT JOIN herb_cabinet b ON a.target_cabinet_id = b.id AND b.deleted = 0
SET a.target_cabinet_name = b.name
WHERE a.target_cabinet_name IS NULL AND a.target_cabinet_id IS NOT NULL AND a.deleted = 0;

-- herb_cabinet_fill_record.target_cell_label
UPDATE herb_cabinet_fill_record a LEFT JOIN herb_cabinet_cell b ON a.target_cell_id = b.id AND b.deleted = 0
SET a.target_cell_label = b.label
WHERE a.target_cell_label IS NULL AND a.target_cell_id IS NOT NULL AND a.deleted = 0;

-- herb_cabinet_fill_record.operator_name
UPDATE herb_cabinet_fill_record a LEFT JOIN sys_user b ON a.operator_id = b.id AND b.deleted = 0
SET a.operator_name = b.real_name
WHERE a.operator_name IS NULL AND a.operator_id IS NOT NULL AND a.deleted = 0;

-- herb_dispensing_record.dispenser_name
UPDATE herb_dispensing_record a LEFT JOIN sys_user b ON a.dispenser_id = b.id AND b.deleted = 0
SET a.dispenser_name = b.real_name
WHERE a.dispenser_name IS NULL AND a.dispenser_id IS NOT NULL AND a.deleted = 0;

-- first_marketing_drug.first_supplier_name
UPDATE first_marketing_drug a LEFT JOIN first_marketing_supplier b ON a.first_supplier_id = b.id AND b.deleted = 0
SET a.first_supplier_name = b.supplier_name
WHERE a.first_supplier_name IS NULL AND a.first_supplier_id IS NOT NULL AND a.deleted = 0;
