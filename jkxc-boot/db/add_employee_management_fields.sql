-- ============================================
-- 员工管理字段扩展SQL
-- 将用户管理改造成员工管理，添加新字段
-- ============================================

-- 添加直属上级字段（关联sys_user.id）
ALTER TABLE `sys_user` 
ADD COLUMN `direct_supervisor` VARCHAR(32) DEFAULT NULL COMMENT '直属上级（关联sys_user.id）' AFTER `team_id`;

-- 添加紧急联系人姓名字段
ALTER TABLE `sys_user` 
ADD COLUMN `emergency_contact` VARCHAR(100) DEFAULT NULL COMMENT '紧急联系人姓名' AFTER `direct_supervisor`;

-- 添加紧急联系电话字段
ALTER TABLE `sys_user` 
ADD COLUMN `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话' AFTER `emergency_contact`;

-- 为直属上级字段添加索引（可选，用于提高查询性能）
CREATE INDEX `idx_direct_supervisor` ON `sys_user` (`direct_supervisor`);

-- 为紧急电话字段添加索引（可选，用于提高查询性能）
CREATE INDEX `idx_emergency_phone` ON `sys_user` (`emergency_phone`);

