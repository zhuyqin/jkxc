-- ============================================
-- 为 gh_bank_diary 表添加 category 字段
-- 表名: gh_bank_diary
-- ============================================

-- 添加 category 字段（业务类型，支出时的报销类目）
ALTER TABLE `gh_bank_diary` 
ADD COLUMN `category` VARCHAR(32) DEFAULT NULL COMMENT '业务类型（支出时的报销类目ID，关联sys_category表）' AFTER `fee_detail`;

