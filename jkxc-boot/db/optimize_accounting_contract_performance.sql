-- ============================================
-- 代账管理列表查询性能优化SQL
-- ============================================

-- 1. 为gh_accounting_contract表添加缺失的索引
ALTER TABLE `gh_accounting_contract` 
ADD INDEX `idx_service_person` (`service_person`),
ADD INDEX `idx_expire_date` (`expire_date`),
ADD INDEX `idx_loss_flag` (`loss_flag`),
ADD INDEX `idx_company_name` (`company_name`(100));

-- 2. 为gh_accounting_contract表添加复合索引，优化常用查询组合
-- 服务中tab：del_flag + loss_flag + create_time
ALTER TABLE `gh_accounting_contract` 
ADD INDEX `idx_serving_query` (`del_flag`, `loss_flag`, `create_time`);

-- 当前续费tab：del_flag + expire_date + contract_status
ALTER TABLE `gh_accounting_contract` 
ADD INDEX `idx_renewal_query` (`del_flag`, `expire_date`, `contract_status`);

-- 3. 为gh_order表添加索引（如果不存在）
-- 注意：如果索引已存在，执行时会报错，可以忽略或先检查
-- 检查并添加company_id索引
ALTER TABLE `gh_order` 
ADD INDEX `idx_company_id` (`company_id`);

-- 检查并添加company_name索引
ALTER TABLE `gh_order` 
ADD INDEX `idx_company_name` (`company_name`(100));

-- 检查并添加del_flag + company_id复合索引
ALTER TABLE `gh_order` 
ADD INDEX `idx_del_company` (`del_flag`, `company_id`);

-- 4. 为sys_audit_task表添加索引（优化NOT EXISTS子查询）
ALTER TABLE `sys_audit_task` 
ADD INDEX `idx_order_task_type` (`order_id`, `task_type`, `task_status`, `del_flag`);

-- 5. 查看当前索引情况
SHOW INDEX FROM `gh_accounting_contract`;
SHOW INDEX FROM `gh_order`;
SHOW INDEX FROM `sys_audit_task`;
