-- ============================================
-- 修复：为gh_address_renew表添加payment_time字段
-- 如果字段已存在则跳过，不会报错
-- ============================================

-- 方法1：使用动态SQL检查字段是否存在（推荐）
SET @dbname = DATABASE();
SET @tablename = 'gh_address_renew';
SET @columnname = 'payment_time';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT ''字段payment_time已存在，跳过添加'' AS result;', -- 字段已存在
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` datetime DEFAULT NULL COMMENT ''收款时间'' AFTER `amount_received`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 方法2：如果MySQL版本支持（MySQL 8.0.19+），可以直接使用：
-- ALTER TABLE `gh_address_renew` 
-- ADD COLUMN IF NOT EXISTS `payment_time` datetime DEFAULT NULL COMMENT '收款时间' AFTER `amount_received`;

SELECT 'payment_time字段检查/添加完成！' AS result;

