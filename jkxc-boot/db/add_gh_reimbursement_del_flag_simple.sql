-- 为 gh_reimbursement 表添加缺失的字段
-- 如果字段已存在，执行会报错，可以忽略

-- 添加 del_flag 字段（删除标志：0-正常，1-已删除）
ALTER TABLE `gh_reimbursement` 
ADD COLUMN `del_flag` INT(1) DEFAULT 0 COMMENT '删除标志（0-正常，1-已删除）';

-- 添加 payment_bank_account_id 字段（支付单位ID）
ALTER TABLE `gh_reimbursement` 
ADD COLUMN `payment_bank_account_id` VARCHAR(32) DEFAULT NULL COMMENT '支付单位ID（银行账户ID）' AFTER `payment_bank_account`;

-- 添加 update_time 字段（更新时间）
ALTER TABLE `gh_reimbursement` 
ADD COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';

-- 添加 update_by 字段（更新人）
ALTER TABLE `gh_reimbursement` 
ADD COLUMN `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人';

-- 添加索引
ALTER TABLE `gh_reimbursement` ADD INDEX `idx_del_flag` (`del_flag`);

