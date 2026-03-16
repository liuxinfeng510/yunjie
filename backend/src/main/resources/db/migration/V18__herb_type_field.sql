-- =============================================
-- V18: 中药饮片子分类移至药品表字段 herb_type
-- 删除分类树中的子分类，改为药品表上的"饮片类别"字段
-- =============================================

-- 1. drug 表新增 herb_type 列
ALTER TABLE drug ADD COLUMN herb_type VARCHAR(30) DEFAULT NULL COMMENT '饮片类别(解表药/清热药/补虚药等)' AFTER is_herb;

-- 2. 迁移：已有药品的 category_id 如果指向中药饮片子分类，
--    将 herb_type 设为子分类名称，category_id 改为中药饮片根分类
SET @herb_root = (SELECT id FROM drug_category WHERE tenant_id = 0 AND is_system = 1 AND name = '中药饮片' AND parent_id = 0 LIMIT 1);

UPDATE drug d
  JOIN drug_category sc ON d.category_id = sc.id AND sc.parent_id = @herb_root
SET d.herb_type = sc.name, d.category_id = @herb_root
WHERE d.deleted = 0;

-- 3. 软删除中药饮片子分类
UPDATE drug_category SET deleted = 1
WHERE parent_id = @herb_root AND is_system = 1 AND tenant_id = 0;
