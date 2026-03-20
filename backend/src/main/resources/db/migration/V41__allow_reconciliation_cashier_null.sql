-- 允许对账单的 cashier_id 为空（表示"全部收银员"对账）
ALTER TABLE reconciliation MODIFY COLUMN cashier_id BIGINT NULL;
