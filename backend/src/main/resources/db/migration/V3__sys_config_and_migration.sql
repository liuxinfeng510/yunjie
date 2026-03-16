-- =====================================================
-- V3: 系统配置表 + 数据迁移任务表
-- =====================================================

-- 升级 sys_config 表（V1 已建基础结构，此处补充字段）
ALTER TABLE sys_config
    ADD COLUMN config_group VARCHAR(50) NOT NULL DEFAULT '' COMMENT '配置分组' AFTER tenant_id,
    ADD COLUMN value_type VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '值类型: string/number/boolean/json' AFTER config_value,
    ADD COLUMN sort_order INT DEFAULT 0 COMMENT '排序号' AFTER description,
    MODIFY COLUMN description VARCHAR(255) COMMENT '配置说明',
    MODIFY COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;

ALTER TABLE sys_config ADD INDEX idx_config_tenant (tenant_id);
ALTER TABLE sys_config ADD INDEX idx_config_group (config_group);
ALTER TABLE sys_config DROP INDEX uk_key;
ALTER TABLE sys_config ADD UNIQUE INDEX uk_config_key_tenant (tenant_id, config_key, deleted);

-- 数据迁移任务表
CREATE TABLE IF NOT EXISTS data_migration_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    source_type VARCHAR(20) NOT NULL COMMENT '数据来源: excel/csv/api/database',
    target_module VARCHAR(50) NOT NULL COMMENT '目标模块: drug/herb/member/supplier/inventory',
    file_path VARCHAR(500) COMMENT '上传文件路径',
    original_file_name VARCHAR(200) COMMENT '原始文件名',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/completed/failed',
    total_count INT DEFAULT 0 COMMENT '总记录数',
    success_count INT DEFAULT 0 COMMENT '成功数',
    fail_count INT DEFAULT 0 COMMENT '失败数',
    skip_count INT DEFAULT 0 COMMENT '跳过数',
    error_log TEXT COMMENT '错误日志',
    start_time DATETIME COMMENT '开始时间',
    finish_time DATETIME COMMENT '完成时间',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_migration_tenant (tenant_id),
    INDEX idx_migration_module (target_module),
    INDEX idx_migration_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据迁移任务表';

-- =====================================================
-- 插入默认系统配置（租户ID=1）
-- =====================================================
INSERT INTO sys_config (tenant_id, config_group, config_key, config_value, value_type, description, sort_order) VALUES
-- 基础配置
(1, 'basic', 'system.business_mode', 'single_store', 'string', '经营模式: single_store/chain_store', 1),
(1, 'basic', 'system.company_name', '药房管理系统', 'string', '企业/药店名称', 2),
(1, 'basic', 'system.setup_completed', 'false', 'boolean', '是否已完成初始化配置', 3),

-- GSP配置
(1, 'gsp', 'gsp.acceptance_required', 'true', 'boolean', '入库是否需要验收', 1),
(1, 'gsp', 'gsp.near_expiry_days', '180', 'number', '近效期预警天数', 2),
(1, 'gsp', 'gsp.temp_check_interval', '120', 'number', '温湿度记录间隔（分钟）', 3),
(1, 'gsp', 'gsp.maintenance_cycle', '30', 'number', '养护周期（天）', 4),

-- 库存配置
(1, 'inventory', 'inventory.low_stock_threshold', '10', 'number', '库存不足预警阈值', 1),
(1, 'inventory', 'inventory.auto_reorder', 'false', 'boolean', '是否自动生成补货单', 2),
(1, 'inventory', 'inventory.batch_tracking', 'true', 'boolean', '是否启用批号追踪', 3),

-- 销售配置
(1, 'sale', 'sale.allow_credit', 'false', 'boolean', '是否允许赊账', 1),
(1, 'sale', 'sale.receipt_printer', 'false', 'boolean', '是否启用小票打印', 2),
(1, 'sale', 'sale.member_points_ratio', '1', 'number', '会员积分比例（消费1元=N积分）', 3),

-- 功能开关
(1, 'feature', 'feature.herb', 'true', 'boolean', '中药管理', 1),
(1, 'feature', 'feature.member', 'true', 'boolean', '会员管理', 2),
(1, 'feature', 'feature.gsp', 'true', 'boolean', 'GSP管理', 3),
(1, 'feature', 'feature.prescription', 'true', 'boolean', '处方管理', 4),
(1, 'feature', 'feature.scale', 'false', 'boolean', '电子秤', 5),
(1, 'feature', 'feature.temp_humidity', 'true', 'boolean', '温湿度监控', 6),
(1, 'feature', 'feature.analytics', 'true', 'boolean', '数据分析', 7),
(1, 'feature', 'feature.central_purchase', 'false', 'boolean', '总部统一采购（连锁模式）', 8),
(1, 'feature', 'feature.cross_transfer', 'false', 'boolean', '跨店调拨（连锁模式）', 9);
