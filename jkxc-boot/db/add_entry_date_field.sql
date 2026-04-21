-- ============================================
-- 添加入职时间字段SQL脚本
-- ============================================

-- 添加入职时间字段
ALTER TABLE `sys_user` 
ADD COLUMN `entry_date` DATE DEFAULT NULL COMMENT '入职时间' AFTER `emergency_phone`;

-- 为入职时间字段添加索引（可选，用于提高查询性能）
CREATE INDEX `idx_entry_date` ON `sys_user` (`entry_date`);

