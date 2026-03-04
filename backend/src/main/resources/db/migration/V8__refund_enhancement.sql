-- V8: 退货功能增强 - 支持单品退货
-- 添加退货类型字段到退款订单表
ALTER TABLE refund_order ADD COLUMN refund_type VARCHAR(20) DEFAULT 'full' COMMENT '退货类型：full-整单退 partial-单品退' AFTER refund_amount;

-- 创建退货明细表
CREATE TABLE IF NOT EXISTS refund_order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    refund_order_id BIGINT NOT NULL COMMENT '退款订单ID',
    sale_order_detail_id BIGINT COMMENT '原销售明细ID',
    drug_id BIGINT NOT NULL COMMENT '药品ID',
    drug_name VARCHAR(200) COMMENT '商品名称',
    specification VARCHAR(100) COMMENT '规格',
    batch_id BIGINT COMMENT '批次ID',
    batch_no VARCHAR(50) COMMENT '批号',
    quantity DECIMAL(12,2) NOT NULL COMMENT '退货数量',
    unit VARCHAR(20) COMMENT '单位',
    unit_price DECIMAL(12,2) COMMENT '单价',
    refund_amount DECIMAL(12,2) COMMENT '退款金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_refund_order_id (refund_order_id),
    INDEX idx_drug_id (drug_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货明细表';
