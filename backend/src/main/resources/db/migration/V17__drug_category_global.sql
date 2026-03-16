-- =============================================
-- V17: 药品分类改为全局表 + 系统预置保护
-- 系统分类 tenant_id=0, is_system=1 全局可见
-- 租户自定义分类 tenant_id=N, is_system=0 仅本租户可见
-- =============================================

-- 1. 添加 is_system 列
ALTER TABLE drug_category ADD COLUMN is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统预置分类';
ALTER TABLE drug_category ADD INDEX idx_tenant_system (tenant_id, is_system);

-- 2. 插入全局系统根分类 (tenant_id=0, is_system=1)
INSERT INTO drug_category (tenant_id, name, parent_id, sort_order, is_system, created_at, deleted) VALUES
(0, '西药',     0, 1,  1, NOW(), 0),
(0, '中成药',   0, 2,  1, NOW(), 0),
(0, '保健品',   0, 3,  1, NOW(), 0),
(0, '医疗器械', 0, 4,  1, NOW(), 0),
(0, '中药饮片', 0, 5,  1, NOW(), 0),
(0, '其他',     0, 99, 1, NOW(), 0);

-- 3. 获取中药饮片全局分类 ID，插入子分类
SET @herb_root_id = (SELECT id FROM drug_category WHERE tenant_id = 0 AND is_system = 1 AND name = '中药饮片' AND parent_id = 0 LIMIT 1);

INSERT INTO drug_category (tenant_id, name, parent_id, sort_order, is_system, created_at, deleted) VALUES
(0, '解表药',         @herb_root_id, 1,  1, NOW(), 0),
(0, '清热药',         @herb_root_id, 2,  1, NOW(), 0),
(0, '补虚药',         @herb_root_id, 3,  1, NOW(), 0),
(0, '理气药',         @herb_root_id, 4,  1, NOW(), 0),
(0, '活血化瘀药',     @herb_root_id, 5,  1, NOW(), 0),
(0, '止血药',         @herb_root_id, 6,  1, NOW(), 0),
(0, '化痰止咳平喘药', @herb_root_id, 7,  1, NOW(), 0),
(0, '安神药',         @herb_root_id, 8,  1, NOW(), 0),
(0, '平肝息风药',     @herb_root_id, 9,  1, NOW(), 0),
(0, '开窍药',         @herb_root_id, 10, 1, NOW(), 0),
(0, '其他',           @herb_root_id, 99, 1, NOW(), 0);

-- 4. 迁移 drug.category_id: 将旧的租户分类 ID 映射到新的全局分类 ID
--    根分类：按名称匹配
UPDATE drug d
  JOIN drug_category old_cat ON d.category_id = old_cat.id AND old_cat.tenant_id != 0
  JOIN drug_category new_cat ON old_cat.name = new_cat.name
    AND new_cat.tenant_id = 0 AND new_cat.is_system = 1
    AND new_cat.parent_id = 0 AND old_cat.parent_id = 0
SET d.category_id = new_cat.id
WHERE d.deleted = 0;

--    子分类：按名称+父级名称匹配
UPDATE drug d
  JOIN drug_category old_cat ON d.category_id = old_cat.id AND old_cat.tenant_id != 0 AND old_cat.parent_id != 0
  JOIN drug_category old_parent ON old_cat.parent_id = old_parent.id
  JOIN drug_category new_parent ON old_parent.name = new_parent.name
    AND new_parent.tenant_id = 0 AND new_parent.is_system = 1 AND new_parent.parent_id = 0
  JOIN drug_category new_cat ON old_cat.name = new_cat.name
    AND new_cat.tenant_id = 0 AND new_cat.is_system = 1
    AND new_cat.parent_id = new_parent.id
SET d.category_id = new_cat.id
WHERE d.deleted = 0;

-- 5. 软删除旧的租户专属系统分类副本（与全局分类同名的）
--    根分类
UPDATE drug_category oc
  JOIN drug_category nc ON oc.name = nc.name
    AND nc.tenant_id = 0 AND nc.is_system = 1
    AND nc.parent_id = 0 AND oc.parent_id = 0
SET oc.deleted = 1
WHERE oc.tenant_id != 0 AND oc.is_system = 0;

--    子分类（中药饮片子分类）
UPDATE drug_category oc
  JOIN drug_category old_parent ON oc.parent_id = old_parent.id
  JOIN drug_category new_parent ON old_parent.name = new_parent.name
    AND new_parent.tenant_id = 0 AND new_parent.is_system = 1 AND new_parent.parent_id = 0
  JOIN drug_category nc ON oc.name = nc.name
    AND nc.tenant_id = 0 AND nc.is_system = 1
    AND nc.parent_id = new_parent.id
SET oc.deleted = 1
WHERE oc.tenant_id != 0 AND oc.is_system = 0 AND oc.parent_id != 0;
