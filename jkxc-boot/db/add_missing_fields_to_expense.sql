-- ============================================
-- 订单支出记录表 - 添加缺失的字段
-- ============================================
-- 注意：如果字段已存在，执行会报错，可以忽略

-- 1. 添加审核状态字段
ALTER TABLE `gh_order_expense` 
ADD COLUMN `audit_status` VARCHAR(10) DEFAULT '0' COMMENT '审核状态：0-待审核，1-已审核' AFTER `category`;

-- 2. 添加支付账户字段
ALTER TABLE `gh_order_expense` 
ADD COLUMN `payment_account` VARCHAR(100) DEFAULT NULL COMMENT '支付账户' AFTER `audit_status`;

-- 3. 添加删除标志字段
ALTER TABLE `gh_order_expense` 
ADD COLUMN `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除' AFTER `update_time`;

-- 为现有记录设置默认值
UPDATE `gh_order_expense` SET `audit_status` = '1' WHERE `audit_status` IS NULL;
UPDATE `gh_order_expense` SET `del_flag` = 0 WHERE `del_flag` IS NULL;

