-- V13: 药品管理模块增强
-- 1. 字典表、2. 生产企业表、3. 多条形码表、4. Drug新增字段、5. 预置数据

-- ================================
-- 1. 创建通用字典表
-- ================================
CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID，0为系统预置',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型：dosage_form/drug_unit/storage_condition',
    item_value VARCHAR(100) NOT NULL COMMENT '字典值',
    pinyin VARCHAR(100) COMMENT '拼音',
    pinyin_short VARCHAR(50) COMMENT '拼音首字母',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    is_preset TINYINT(1) DEFAULT 0 COMMENT '是否预置数据',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_dict_type (dict_type),
    INDEX idx_tenant_type (tenant_id, dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- ================================
-- 2. 创建生产企业表
-- ================================
CREATE TABLE IF NOT EXISTS drug_manufacturer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    name VARCHAR(200) NOT NULL COMMENT '企业名称',
    short_name VARCHAR(100) COMMENT '简称',
    pinyin VARCHAR(200) COMMENT '拼音',
    pinyin_short VARCHAR(50) COMMENT '拼音首字母',
    address VARCHAR(300) COMMENT '地址',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_pinyin_short (pinyin_short),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产企业表';

-- ================================
-- 3. 创建多条形码表
-- ================================
CREATE TABLE IF NOT EXISTS drug_barcode (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    drug_id BIGINT NOT NULL COMMENT '药品ID',
    barcode VARCHAR(50) NOT NULL COMMENT '条形码',
    barcode_type VARCHAR(30) COMMENT '条码类型：EAN13/EAN8/CODE128/QR',
    is_primary TINYINT(1) DEFAULT 0 COMMENT '是否主条码',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_barcode (barcode),
    INDEX idx_drug_id (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品条形码表';

-- ================================
-- 4. Drug表新增字段
-- ================================
ALTER TABLE drug ADD COLUMN manufacturer_id BIGINT COMMENT '生产企业ID';
ALTER TABLE drug ADD COLUMN is_split TINYINT(1) DEFAULT 0 COMMENT '是否拆零';
ALTER TABLE drug ADD COLUMN split_ratio INT COMMENT '拆零系数';
ALTER TABLE drug ADD COLUMN split_priority TINYINT(1) DEFAULT 0 COMMENT '拆零优先';
ALTER TABLE drug ADD COLUMN is_key_maintenance TINYINT(1) DEFAULT 0 COMMENT '是否重点养护';
ALTER TABLE drug ADD COLUMN is_imported TINYINT(1) DEFAULT 0 COMMENT '是否进口品种';

-- ================================
-- 5. 预置剂型数据（20条）
-- ================================
INSERT INTO sys_dict_item (tenant_id, dict_type, item_value, pinyin, pinyin_short, sort_order, is_preset, status) VALUES
(0, 'dosage_form', '片剂', 'pianji', 'pj', 1, 1, 'active'),
(0, 'dosage_form', '胶囊剂', 'jiaonangji', 'jnj', 2, 1, 'active'),
(0, 'dosage_form', '颗粒剂', 'keliji', 'klj', 3, 1, 'active'),
(0, 'dosage_form', '口服液', 'koufuye', 'kfy', 4, 1, 'active'),
(0, 'dosage_form', '糖浆剂', 'tangjiangji', 'tjj', 5, 1, 'active'),
(0, 'dosage_form', '注射剂', 'zhusheji', 'zsj', 6, 1, 'active'),
(0, 'dosage_form', '软膏剂', 'ruangaoji', 'rgj', 7, 1, 'active'),
(0, 'dosage_form', '乳膏剂', 'rugaoji', 'rgj', 8, 1, 'active'),
(0, 'dosage_form', '栓剂', 'shuanji', 'sj', 9, 1, 'active'),
(0, 'dosage_form', '滴眼液', 'diyanye', 'dyy', 10, 1, 'active'),
(0, 'dosage_form', '滴耳液', 'dierye', 'dey', 11, 1, 'active'),
(0, 'dosage_form', '滴鼻液', 'dibiye', 'dby', 12, 1, 'active'),
(0, 'dosage_form', '气雾剂', 'qiwuji', 'qwj', 13, 1, 'active'),
(0, 'dosage_form', '喷雾剂', 'penwuji', 'pwj', 14, 1, 'active'),
(0, 'dosage_form', '贴剂', 'tieji', 'tj', 15, 1, 'active'),
(0, 'dosage_form', '散剂', 'sanji', 'sj', 16, 1, 'active'),
(0, 'dosage_form', '丸剂', 'wanji', 'wj', 17, 1, 'active'),
(0, 'dosage_form', '膏剂', 'gaoji', 'gj', 18, 1, 'active'),
(0, 'dosage_form', '合剂', 'heji', 'hj', 19, 1, 'active'),
(0, 'dosage_form', '酊剂', 'dingji', 'dj', 20, 1, 'active');

-- ================================
-- 6. 预置单位数据（10条）
-- ================================
INSERT INTO sys_dict_item (tenant_id, dict_type, item_value, pinyin, pinyin_short, sort_order, is_preset, status) VALUES
(0, 'drug_unit', '盒', 'he', 'h', 1, 1, 'active'),
(0, 'drug_unit', '瓶', 'ping', 'p', 2, 1, 'active'),
(0, 'drug_unit', '袋', 'dai', 'd', 3, 1, 'active'),
(0, 'drug_unit', '支', 'zhi', 'z', 4, 1, 'active'),
(0, 'drug_unit', '片', 'pian', 'p', 5, 1, 'active'),
(0, 'drug_unit', '粒', 'li', 'l', 6, 1, 'active'),
(0, 'drug_unit', '板', 'ban', 'b', 7, 1, 'active'),
(0, 'drug_unit', '条', 'tiao', 't', 8, 1, 'active'),
(0, 'drug_unit', '罐', 'guan', 'g', 9, 1, 'active'),
(0, 'drug_unit', '克', 'ke', 'k', 10, 1, 'active');

-- ================================
-- 7. 预置储存条件数据（8条）
-- ================================
INSERT INTO sys_dict_item (tenant_id, dict_type, item_value, pinyin, pinyin_short, sort_order, is_preset, status) VALUES
(0, 'storage_condition', '密封阴凉干燥处保存', 'mifengyinliangganzaochubaocu', 'mfylgzcbc', 1, 1, 'active'),
(0, 'storage_condition', '密封常温保存', 'mifengchangwenbaocu', 'mfcwbc', 2, 1, 'active'),
(0, 'storage_condition', '冷藏保存(2-8℃)', 'lengcangbaocun', 'lcbc', 3, 1, 'active'),
(0, 'storage_condition', '冷冻保存(-20℃以下)', 'lengdongbaocun', 'ldbc', 4, 1, 'active'),
(0, 'storage_condition', '遮光密封保存', 'zheguangmifengbaocun', 'zgmfbc', 5, 1, 'active'),
(0, 'storage_condition', '遮光密闭阴凉处保存', 'zheguangmibiyinliangchubaocun', 'zgmbylcbc', 6, 1, 'active'),
(0, 'storage_condition', '阴凉处保存', 'yinliangchubaocun', 'ylcbc', 7, 1, 'active'),
(0, 'storage_condition', '密闭干燥处保存', 'mibiganzaochubaocun', 'mbgzcbc', 8, 1, 'active');

-- ================================
-- 8. 迁移现有barcode数据到drug_barcode表
-- ================================
INSERT INTO drug_barcode (tenant_id, drug_id, barcode, is_primary, created_at, updated_at, deleted)
SELECT tenant_id, id, barcode, 1, NOW(), NOW(), 0 
FROM drug 
WHERE barcode IS NOT NULL AND barcode != '' AND deleted = 0;
