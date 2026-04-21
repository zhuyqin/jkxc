-- ============================================
-- 代账管理列表查询性能优化SQL（安全版本）
-- 先检查索引是否存在，避免重复创建
-- ============================================

-- 查看当前索引情况
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('gh_accounting_contract', 'gh_order', 'sys_audit_task')
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME, INDEX_NAME;

-- ============================================
-- 1. gh_accounting_contract表索引
-- ============================================

-- 添加service_person索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_accounting_contract' 
    AND INDEX_NAME = 'idx_service_person');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_service_person已存在" AS message',
    'ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_service_person` (`service_person`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加expire_date索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_accounting_contract' 
    AND INDEX_NAME = 'idx_expire_date');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_expire_date已存在" AS message',
    'ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_expire_date` (`expire_date`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加loss_flag索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_accounting_contract' 
    AND INDEX_NAME = 'idx_loss_flag');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_loss_flag已存在" AS message',
    'ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_loss_flag` (`loss_flag`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加company_name索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_accounting_contract' 
    AND INDEX_NAME = 'idx_company_name');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_company_name已存在" AS message',
    'ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_company_name` (`company_name`(100))');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加服务中查询复合索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_accounting_contract' 
    AND INDEX_NAME = 'idx_serving_query');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_serving_query已存在" AS message',
    'ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_serving_query` (`del_flag`, `loss_flag`, `create_time`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加续费查询复合索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_accounting_contract' 
    AND INDEX_NAME = 'idx_renewal_query');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_renewal_query已存在" AS message',
    'ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_renewal_query` (`del_flag`, `expire_date`, `contract_status`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 2. gh_order表索引
-- ============================================

-- 添加company_id索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_order' 
    AND INDEX_NAME = 'idx_company_id');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_company_id已存在" AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_company_id` (`company_id`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加company_name索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_order' 
    AND INDEX_NAME = 'idx_company_name');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_company_name已存在" AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_company_name` (`company_name`(100))');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加del_flag + company_id复合索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'gh_order' 
    AND INDEX_NAME = 'idx_del_company');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_del_company已存在" AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_del_company` (`del_flag`, `company_id`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 3. sys_audit_task表索引
-- ============================================

-- 添加order_id + task_type + task_status + del_flag复合索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'sys_audit_task' 
    AND INDEX_NAME = 'idx_order_task_type');
SET @sqlstmt := IF(@exist > 0, 
    'SELECT "索引idx_order_task_type已存在" AS message',
    'ALTER TABLE `sys_audit_task` ADD INDEX `idx_order_task_type` (`order_id`, `task_type`, `task_status`, `del_flag`)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 查看优化后的索引情况
-- ============================================
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('gh_accounting_contract', 'gh_order', 'sys_audit_task')
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY TABLE_NAME, INDEX_NAME;
