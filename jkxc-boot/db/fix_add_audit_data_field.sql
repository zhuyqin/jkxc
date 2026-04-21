-- ============================================
-- 修复：为sys_audit_task表添加audit_data字段
-- 如果字段已存在则跳过，不会报错
-- 执行时间：2025-01-28
-- ============================================

-- 方法1：使用动态SQL检查字段是否存在（推荐）
SET @dbname = DATABASE();
SET @tablename = 'sys_audit_task';
SET @columnname = 'audit_data';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT ''字段audit_data已存在，跳过添加'' AS result;', -- 字段已存在
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT ''审核数据JSON（包含审核表单填写的数据）'' AFTER `current_role_name`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 如果字段已存在，确保字符集和注释正确
SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE `', @tablename, '` MODIFY COLUMN `', @columnname, '` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT ''审核数据JSON（包含审核表单填写的数据）'';'),
  'SELECT ''字段不存在，无需修改'' AS result;'
));
PREPARE modifyIfExists FROM @preparedStatement2;
EXECUTE modifyIfExists;
DEALLOCATE PREPARE modifyIfExists;

SELECT 'audit_data字段检查/添加完成！' AS result;

