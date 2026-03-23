-- 药柜起始列号（跨柜连续编号）
ALTER TABLE herb_cabinet ADD COLUMN column_start_number INT NOT NULL DEFAULT 1 COMMENT '起始列号(默认1，第二组柜可设为5使列号连续)';
