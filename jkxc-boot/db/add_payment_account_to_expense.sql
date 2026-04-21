-- ============================================
-- 订单支出记录表 - 添加支付账户字段
-- ============================================

-- 添加支付账户字段
ALTER TABLE `gh_order_expense` 
ADD COLUMN `payment_account` VARCHAR(100) DEFAULT NULL COMMENT '支付账户' AFTER `category`;

