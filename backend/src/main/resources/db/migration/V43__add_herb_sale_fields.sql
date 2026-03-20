-- 销售订单明细：中药饮片处方字段
ALTER TABLE sale_order_detail ADD COLUMN is_herb TINYINT(1) DEFAULT 0 COMMENT '是否中药饮片';
ALTER TABLE sale_order_detail ADD COLUMN dose_per_gram DECIMAL(8,1) NULL COMMENT '每剂克数';
ALTER TABLE sale_order_detail ADD COLUMN dose_count INT NULL COMMENT '副数';

-- 销售订单：中药副数
ALTER TABLE sale_order ADD COLUMN herb_dose_count INT NULL COMMENT '中药副数';
