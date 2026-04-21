-- ============================================
-- 为地址续费表添加收款时间字段
-- 表名: gh_address_renew
-- 执行前请备份数据库！
-- ============================================

-- 检查字段是否存在，如果不存在则添加
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
  'SELECT 1', -- 字段已存在，不执行任何操作
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` datetime DEFAULT NULL COMMENT ''收款时间'' AFTER `amount_received`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 或者使用更简单的方式（如果MySQL版本支持）
-- ALTER TABLE `gh_address_renew` 
-- ADD COLUMN IF NOT EXISTS `payment_time` datetime DEFAULT NULL COMMENT '收款时间' AFTER `amount_received`;

SELECT 'gh_address_renew表payment_time字段检查/添加完成！' AS result;

