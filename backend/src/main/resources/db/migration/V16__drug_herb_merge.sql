-- =====================================================
-- V16: 将中药饮片字段合并到 drug 表 + 初始化中药分类
-- =====================================================

-- 给 drug 表添加中药饮片特有字段
ALTER TABLE drug
    ADD COLUMN is_herb TINYINT(1) DEFAULT 0 COMMENT '是否中药饮片',
    ADD COLUMN alias VARCHAR(200) COMMENT '别名',
    ADD COLUMN nature VARCHAR(20) COMMENT '性(寒/热/温/凉/平)',
    ADD COLUMN flavor VARCHAR(50) COMMENT '味(酸/苦/甘/辛/咸)',
    ADD COLUMN meridian VARCHAR(100) COMMENT '归经',
    ADD COLUMN efficacy VARCHAR(500) COMMENT '功效',
    ADD COLUMN origin VARCHAR(100) COMMENT '产地',
    ADD COLUMN processing_method VARCHAR(100) COMMENT '炮制方法',
    ADD COLUMN dosage_min DECIMAL(6,1) COMMENT '最小用量(g)',
    ADD COLUMN dosage_max DECIMAL(6,1) COMMENT '最大用量(g)',
    ADD COLUMN is_toxic TINYINT(1) DEFAULT 0 COMMENT '是否有毒',
    ADD COLUMN toxic_level VARCHAR(20) COMMENT '毒性等级(大毒/有毒/小毒)',
    ADD COLUMN is_precious TINYINT(1) DEFAULT 0 COMMENT '是否贵细药材';

ALTER TABLE drug ADD INDEX idx_is_herb (is_herb);

-- 为 tenant_id=1 插入中药饮片分类树
INSERT INTO drug_category (tenant_id, name, parent_id, sort_order, created_at, updated_at, deleted) VALUES
(1, '中药饮片', 0, 100, NOW(), NOW(), 0);

SET @herb_cat_id = LAST_INSERT_ID();

INSERT INTO drug_category (tenant_id, name, parent_id, sort_order, created_at, updated_at, deleted) VALUES
(1, '解表药', @herb_cat_id, 1, NOW(), NOW(), 0),
(1, '清热药', @herb_cat_id, 2, NOW(), NOW(), 0),
(1, '补虚药', @herb_cat_id, 3, NOW(), NOW(), 0),
(1, '理气药', @herb_cat_id, 4, NOW(), NOW(), 0),
(1, '活血化瘀药', @herb_cat_id, 5, NOW(), NOW(), 0),
(1, '止血药', @herb_cat_id, 6, NOW(), NOW(), 0),
(1, '化痰止咳平喘药', @herb_cat_id, 7, NOW(), NOW(), 0),
(1, '安神药', @herb_cat_id, 8, NOW(), NOW(), 0),
(1, '平肝息风药', @herb_cat_id, 9, NOW(), NOW(), 0),
(1, '开窍药', @herb_cat_id, 10, NOW(), NOW(), 0),
(1, '其他', @herb_cat_id, 99, NOW(), NOW(), 0);
