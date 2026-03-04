-- V10: 为suspended_order表的store_id设置默认值
ALTER TABLE suspended_order 
    MODIFY COLUMN store_id BIGINT NOT NULL DEFAULT 1 COMMENT '门店ID';
