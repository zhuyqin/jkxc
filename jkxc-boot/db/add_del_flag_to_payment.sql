-- ============================================
-- 订单收费记录表 - 添加删除标志字段
-- ============================================
-- 注意：如果字段已存在，执行会报错，可以忽略

-- 添加删除标志字段
ALTER TABLE `gh_order_payment` 
ADD COLUMN `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除' AFTER `update_time`;

-- 为现有记录设置默认值
UPDATE `gh_order_payment` SET `del_flag` = 0 WHERE `del_flag` IS NULL;

