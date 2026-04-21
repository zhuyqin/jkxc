-- ============================================
-- 修复：为sys_audit_task表添加audit_data字段（简化版）
-- 执行时间：2025-01-28
-- 说明：如果字段已存在会报错，可以忽略错误继续执行
-- ============================================

-- 方法1：直接添加字段（如果字段不存在）
-- 如果报错 "Duplicate column name 'audit_data'"，说明字段已存在，可以忽略
ALTER TABLE `sys_audit_task` 
ADD COLUMN `audit_data` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '审核数据JSON（包含审核表单填写的数据）' 
AFTER `current_role_name`;

-- 方法2：如果字段已存在，使用下面的语句确保字符集和注释正确
ALTER TABLE `sys_audit_task` 
MODIFY COLUMN `audit_data` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '审核数据JSON（包含审核表单填写的数据）';

-- 验证字段是否存在
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_SET_NAME, 
    COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'sys_audit_task' 
    AND COLUMN_NAME = 'audit_data';

