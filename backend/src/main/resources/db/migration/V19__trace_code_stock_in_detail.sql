-- V19: 追溯码关联入库明细行
ALTER TABLE drug_trace_code ADD COLUMN stock_in_detail_id BIGINT COMMENT '入库明细行ID';
ALTER TABLE drug_trace_code ADD INDEX idx_stock_in_detail_id (stock_in_detail_id);
ALTER TABLE drug_trace_code ADD INDEX idx_purchase_order_id (purchase_order_id);
