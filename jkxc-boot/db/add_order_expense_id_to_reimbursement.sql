-- 为报销表添加支出记录ID字段
ALTER TABLE `gh_reimbursement` 
ADD COLUMN `order_expense_id` VARCHAR(32) DEFAULT NULL COMMENT '订单支出记录ID' AFTER `payment_bank_account_id`;

-- 添加索引以便查询
CREATE INDEX `idx_order_expense_id` ON `gh_reimbursement` (`order_expense_id`);

