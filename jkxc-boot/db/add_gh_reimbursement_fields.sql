-- 为 gh_reimbursement 表添加缺失的字段
-- 执行前请确保数据库连接使用 utf8mb4 编码

SET NAMES utf8mb4;

-- 添加 del_flag 字段（删除标志：0-正常，1-已删除）
ALTER TABLE `gh_reimbursement` 
ADD COLUMN IF NOT EXISTS `del_flag` INT(1) DEFAULT 0 COMMENT '删除标志（0-正常，1-已删除）';

-- 添加 payment_bank_account_id 字段（支付单位ID）
ALTER TABLE `gh_reimbursement` 
ADD COLUMN IF NOT EXISTS `payment_bank_account_id` VARCHAR(32) DEFAULT NULL COMMENT '支付单位ID（银行账户ID）' AFTER `payment_bank_account`;

-- 添加索引
ALTER TABLE `gh_reimbursement` ADD INDEX IF NOT EXISTS `idx_del_flag` (`del_flag`);

