-- 对账单主表
CREATE TABLE reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    reconciliation_no VARCHAR(30) NOT NULL,
    store_id BIGINT NOT NULL,
    cashier_id BIGINT NOT NULL,
    reconcile_date DATE NOT NULL,
    order_count INT NOT NULL DEFAULT 0,
    system_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    actual_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    difference DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'balanced' COMMENT 'balanced/surplus/shortage',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_reconciliation_no (reconciliation_no, tenant_id),
    INDEX idx_cashier_date (cashier_id, reconcile_date),
    INDEX idx_store_date (store_id, reconcile_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账单';

-- 对账明细表（按支付方式拆分）
CREATE TABLE reconciliation_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    reconciliation_id BIGINT NOT NULL,
    pay_method VARCHAR(30) NOT NULL COMMENT '支付方式',
    order_count INT DEFAULT 0,
    system_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    actual_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    difference DECIMAL(12,2) NOT NULL DEFAULT 0,
    remark VARCHAR(200),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_reconciliation_id (reconciliation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账明细';
