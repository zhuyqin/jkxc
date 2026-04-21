-- ============================================
-- 第一步：检查当前索引情况
-- ============================================

SELECT 
    '当前gh_accounting_contract表的索引：' AS info;

SELECT 
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'gh_accounting_contract'
GROUP BY INDEX_NAME
ORDER BY INDEX_NAME;

SELECT 
    '当前gh_order表的索引：' AS info;

SELECT 
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'gh_order'
GROUP BY INDEX_NAME
ORDER BY INDEX_NAME;

SELECT 
    '当前sys_audit_task表的索引：' AS info;

SELECT 
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'sys_audit_task'
GROUP BY INDEX_NAME
ORDER BY INDEX_NAME;

-- ============================================
-- 第二步：根据上面的结果，手动执行需要的索引
-- 如果索引已存在，跳过对应的语句
-- ============================================

-- gh_accounting_contract表索引（逐条执行，如果报错说明已存在，继续下一条）

-- 如果idx_service_person不存在，执行：
-- ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_service_person` (`service_person`);

-- 如果idx_expire_date不存在，执行：
-- ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_expire_date` (`expire_date`);

-- 如果idx_loss_flag不存在，执行：
-- ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_loss_flag` (`loss_flag`);

-- 如果idx_company_name不存在，执行：
-- ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_company_name` (`company_name`(100));

-- 如果idx_serving_query不存在，执行：
-- ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_serving_query` (`del_flag`, `loss_flag`, `create_time`);

-- 如果idx_renewal_query不存在，执行：
-- ALTER TABLE `gh_accounting_contract` ADD INDEX `idx_renewal_query` (`del_flag`, `expire_date`, `contract_status`);

-- gh_order表索引（逐条执行）

-- 如果idx_company_id不存在，执行：
-- ALTER TABLE `gh_order` ADD INDEX `idx_company_id` (`company_id`);

-- 如果idx_company_name不存在，执行：
-- ALTER TABLE `gh_order` ADD INDEX `idx_company_name` (`company_name`(100));

-- 如果idx_del_company不存在，执行：
-- ALTER TABLE `gh_order` ADD INDEX `idx_del_company` (`del_flag`, `company_id`);

-- sys_audit_task表索引（逐条执行）

-- 如果idx_order_task_type不存在，执行：
-- ALTER TABLE `sys_audit_task` ADD INDEX `idx_order_task_type` (`order_id`, `task_type`, `task_status`, `del_flag`);
