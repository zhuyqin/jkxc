-- 为订单支出记录表添加审核状态字段
-- 审核状态：0-待审核，1-已审核

ALTER TABLE `gh_order_expense` 
ADD COLUMN `audit_status` VARCHAR(10) DEFAULT '0' COMMENT '审核状态：0-待审核，1-已审核' AFTER `category`;

-- 为现有记录设置默认值（已存在的记录视为已审核）
UPDATE `gh_order_expense` SET `audit_status` = '1' WHERE `audit_status` IS NULL;

