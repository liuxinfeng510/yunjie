-- 小票打印配置
-- tenant_id=1
INSERT INTO sys_config (tenant_id, config_group, config_key, config_value, value_type, description, sort_order, deleted)
VALUES
(1, 'sale', 'sale.receipt_paper_width', '58', 'string', '小票纸张宽度(58/80mm)', 4, 0),
(1, 'sale', 'sale.receipt_shop_name', '', 'string', '小票店名(空则取企业名称)', 5, 0),
(1, 'sale', 'sale.receipt_footer', '感谢您的光临！\n如有问题请保留此小票', 'string', '小票页脚文本', 6, 0),
(1, 'sale', 'sale.receipt_fields', '{"header":{"shopName":true,"tenantName":true,"subtitle":true},"orderInfo":{"orderNo":true,"dateTime":true,"cashier":true},"memberInfo":{"memberName":true,"memberPhone":true},"itemDetail":{"drugName":true,"specification":true,"batchNo":true,"manufacturer":true,"unitPrice":true},"summary":{"itemCount":true,"totalAmount":true,"memberDiscount":true,"wholeDiscount":true,"manualDiscount":true,"payMethod":true,"cashInfo":true},"footer":{"footerText":true}}', 'json', '小票打印字段配置', 7, 0);

-- tenant_id=2
INSERT INTO sys_config (tenant_id, config_group, config_key, config_value, value_type, description, sort_order, deleted)
VALUES
(2, 'sale', 'sale.receipt_paper_width', '58', 'string', '小票纸张宽度(58/80mm)', 4, 0),
(2, 'sale', 'sale.receipt_shop_name', '', 'string', '小票店名(空则取企业名称)', 5, 0),
(2, 'sale', 'sale.receipt_footer', '感谢您的光临！\n如有问题请保留此小票', 'string', '小票页脚文本', 6, 0),
(2, 'sale', 'sale.receipt_fields', '{"header":{"shopName":true,"tenantName":true,"subtitle":true},"orderInfo":{"orderNo":true,"dateTime":true,"cashier":true},"memberInfo":{"memberName":true,"memberPhone":true},"itemDetail":{"drugName":true,"specification":true,"batchNo":true,"manufacturer":true,"unitPrice":true},"summary":{"itemCount":true,"totalAmount":true,"memberDiscount":true,"wholeDiscount":true,"manualDiscount":true,"payMethod":true,"cashInfo":true},"footer":{"footerText":true}}', 'json', '小票打印字段配置', 7, 0);
