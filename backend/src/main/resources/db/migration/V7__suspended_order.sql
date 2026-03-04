-- V7: 挂单功能
-- 创建挂单表
CREATE TABLE IF NOT EXISTS suspended_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(32) UNIQUE COMMENT '挂单号',
    member_id BIGINT COMMENT '会员ID',
    member_name VARCHAR(50) COMMENT '会员姓名',
    items JSON NOT NULL COMMENT '商品明细JSON',
    total_amount DECIMAL(12,2) COMMENT '总金额',
    suspended_by BIGINT COMMENT '挂单人ID',
    suspended_by_name VARCHAR(50) COMMENT '挂单人姓名',
    suspended_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '挂单时间',
    expire_at DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效 0-已取单 -1-已过期',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_store_id (store_id),
    INDEX idx_status (status),
    INDEX idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂单表';

-- 添加挂单过期时间配置
INSERT INTO sys_config (tenant_id, config_group, config_key, config_value, description, created_at, updated_at, deleted)
VALUES (1, 'sale', 'suspended_order_expire_minutes', '60', '挂单过期时间(分钟)', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
