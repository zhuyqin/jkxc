-- 为 gh_reimbursement 表添加 del_flag 和 payment_bank_account_id 字段
-- 执行前请确保数据库连接使用 utf8mb4 编码

SET NAMES utf8mb4;

-- 检查并添加 del_flag 字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'gh_reimbursement';
SET @columnname = 'del_flag';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `', @columnname, '` INT(1) DEFAULT 0 COMMENT ''删除标志（0-正常，1-已删除）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 payment_bank_account_id 字段（如果不存在）
SET @columnname = 'payment_bank_account_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `', @columnname, '` VARCHAR(32) DEFAULT NULL COMMENT ''支付单位ID（银行账户ID）'' AFTER `payment_bank_account`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加索引
ALTER TABLE `gh_reimbursement` ADD INDEX `idx_del_flag` (`del_flag`);

