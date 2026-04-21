-- 修复 gh_reimbursement 表字段备注乱码问题
-- 执行前请确保数据库连接使用 utf8mb4 编码

SET NAMES utf8mb4;

-- 修复乱码字段备注
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `reimbursement_time` datetime COMMENT '报销时间';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `reimbursement_person` varchar(50) COMMENT '报销人员';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `team_id` varchar(32) COMMENT '所属团队';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `reimbursement_basis` varchar(500) COMMENT '报销依据';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `reimbursement_account_id` varchar(32) COMMENT '报销账号ID（用于三联级选择）';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `payment_basis` varchar(500) COMMENT '支付依据';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `payment_bank_name` varchar(200) COMMENT '支付银行开户行';
ALTER TABLE `gh_reimbursement` MODIFY COLUMN `payment_bank_account` varchar(100) COMMENT '支付银行卡号';

-- 添加 del_flag 字段（如果不存在）
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

-- 添加 payment_bank_account_id 字段（如果不存在）
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

-- 添加 update_time 字段（如果不存在）
SET @columnname = 'update_time';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `', @columnname, '` DATETIME DEFAULT NULL COMMENT ''更新时间''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 update_by 字段（如果不存在）
SET @columnname = 'update_by';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `', @columnname, '` VARCHAR(50) DEFAULT NULL COMMENT ''更新人''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加索引
ALTER TABLE `gh_reimbursement` ADD INDEX `idx_del_flag` (`del_flag`);


