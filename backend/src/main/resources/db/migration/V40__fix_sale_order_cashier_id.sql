-- 回填历史订单的 cashier_id（使用 created_by 作为收银员ID）
UPDATE sale_order SET cashier_id = created_by WHERE cashier_id IS NULL AND created_by IS NOT NULL AND deleted = 0;
