-- V36: drug 表增加 stock_quantity 字段（库存数量参考值）
ALTER TABLE drug ADD COLUMN stock_quantity DECIMAL(10,2) NULL COMMENT '库存数量(参考值)' AFTER is_imported;
