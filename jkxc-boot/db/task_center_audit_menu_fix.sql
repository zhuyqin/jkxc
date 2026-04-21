-- ============================================
-- 任务中心审核任务菜单名称修复SQL脚本
-- 如果菜单名称显示乱码，请执行此脚本修复
-- ============================================

-- 首先确保表字符集为utf8mb4（如果还没有转换）
ALTER TABLE `sys_permission` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修复一次性任务菜单名称（使用十六进制避免编码问题）
UPDATE `sys_permission` 
SET `name` = 0xE4B880E6ACA1E680A7E4BBBBE58AA1,
    `description` = 0xE4B880E6ACA1E680A7E4BBBBE58AA1E5AEA1E6A0B8EFBC8CE58C85E590ABE5BE85E5AEA1E6A0B8E38081E5B7B2E5AEA1E6A0B8E38081E5B7B2E9A9B3E59B9EE4B889E4B8AAtabE9A1B5
WHERE `id` = 'onetimetask20250104';

-- 修复周期任务菜单名称（使用十六进制避免编码问题）
UPDATE `sys_permission` 
SET `name` = 0xE591A8E69C9FE4BBBBE58AA1,
    `description` = 0xE591A8E69C9FE4BBBBE58AA1E5AEA1E6A0B8EFBC8CE58C85E590ABE5BE85E5AEA1E6A0B8E38081E5B7B2E5AEA1E6A0B8E38081E5B7B2E9A9B3E59B9EE4B889E4B8AAtabE9A1B5
WHERE `id` = 'recurringtask20250104';

-- 验证修复结果
SELECT 
    id,
    name,
    url,
    component,
    parent_id,
    sort_no,
    status,
    del_flag
FROM `sys_permission`
WHERE id IN ('onetimetask20250104', 'recurringtask20250104')
ORDER BY sort_no;

-- 注意：如果UPDATE后名称仍然是乱码，可能是数据库字符集问题
-- 请检查数据库和表的字符集：
-- SHOW CREATE DATABASE your_database_name;
-- SHOW CREATE TABLE sys_permission;
-- 
-- 如果字符集不是utf8mb4，需要修改：
-- ALTER DATABASE your_database_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ALTER TABLE sys_permission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

