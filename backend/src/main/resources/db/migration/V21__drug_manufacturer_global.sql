-- ============================================================
-- V21: 生产企业全局化
-- 将 drug_manufacturer 从租户隔离改为全局共享
-- ============================================================

-- 步骤1: 创建临时映射表，记录需要合并的重复记录
CREATE TEMPORARY TABLE tmp_mfr_merge (
    old_id BIGINT NOT NULL,
    new_id BIGINT NOT NULL
);

-- 步骤2: 找出每个名称的存活ID(MIN(id))，非存活的记录写入映射表
INSERT INTO tmp_mfr_merge (old_id, new_id)
SELECT m.id AS old_id, survivor.min_id AS new_id
FROM drug_manufacturer m
JOIN (
    SELECT TRIM(name) AS trimmed_name, MIN(id) AS min_id
    FROM drug_manufacturer
    WHERE deleted = 0
    GROUP BY TRIM(name)
) survivor ON TRIM(m.name) = survivor.trimmed_name
WHERE m.deleted = 0 AND m.id != survivor.min_id;

-- 步骤3: 更新 drug 表的 manufacturer_id 外键，指向存活记录
UPDATE drug d
JOIN tmp_mfr_merge t ON d.manufacturer_id = t.old_id
SET d.manufacturer_id = t.new_id
WHERE d.deleted = 0;

-- 步骤4: 软删除被合并的重复记录
UPDATE drug_manufacturer
SET deleted = 1
WHERE id IN (SELECT old_id FROM tmp_mfr_merge);

-- 步骤5: 所有存活记录的 tenant_id 设为 0（全局共享）
UPDATE drug_manufacturer
SET tenant_id = 0
WHERE deleted = 0;

-- 步骤6: 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_mfr_merge;
