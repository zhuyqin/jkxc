-- ============================================
-- 诊断：检查sys_audit_task表的audit_data字段
-- 用于排查字段存在但查询报错的问题
-- ============================================

-- 1. 检查字段是否存在（不区分大小写）
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_SET_NAME,
    COLLATION_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'sys_audit_task' 
    AND LOWER(COLUMN_NAME) = 'audit_data';

-- 2. 检查所有字段名称（查看是否有大小写问题）
SELECT 
    COLUMN_NAME,
    DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'sys_audit_task'
ORDER BY ORDINAL_POSITION;

-- 3. 测试直接查询字段（如果字段存在）
-- SELECT audit_data FROM sys_audit_task LIMIT 1;

-- 4. 测试使用表别名查询字段
-- SELECT t.audit_data FROM sys_audit_task t LIMIT 1;

-- 5. 检查表的字符集和排序规则
SELECT 
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_audit_task';

