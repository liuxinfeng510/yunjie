-- 药品表新增上市许可持有人字段
ALTER TABLE drug ADD COLUMN marketing_auth_holder VARCHAR(200) COMMENT '上市许可持有人' AFTER manufacturer_id;
