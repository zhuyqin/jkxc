-- ============================================
-- 删除所有业务数据SQL脚本
-- 包括：线索、客户、订单、收入记录、支出记录、日记账、报销、地址管理、代账管理、任务记录
-- 执行前请备份数据库！
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- 禁用外键检查（临时）
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一步：删除审批任务相关数据（需要先删除，因为可能关联订单）
-- ============================================

-- 删除审批任务成本记录
DELETE FROM `sys_audit_task_cost`;

-- 删除审批任务
DELETE FROM `sys_audit_task`;

-- ============================================
-- 第二步：删除报销数据
-- ============================================

-- 删除报销记录
DELETE FROM `gh_reimbursement`;

-- ============================================
-- 第三步：删除日记账数据
-- ============================================

-- 删除银行日记账
DELETE FROM `gh_bank_diary`;

-- ============================================
-- 第四步：删除订单相关数据
-- ============================================

-- 删除订单表单数据
DELETE FROM `gh_order_form_data`;

-- 删除订单操作日志
DELETE FROM `gh_order_operation_log`;

-- 删除订单审核记录
DELETE FROM `gh_order_audit`;

-- 删除订单支出记录
DELETE FROM `gh_order_expense`;

-- 删除订单收入记录（订单收费记录）
DELETE FROM `gh_order_payment`;

-- 删除订单步骤记录
DELETE FROM `gh_order_step`;

-- 删除订单主表
DELETE FROM `gh_order`;

-- ============================================
-- 第五步：删除代账管理相关数据
-- ============================================

-- 删除代账交接审核记录
DELETE FROM `gh_accounting_handover_audit`;

-- 删除代账交接
DELETE FROM `gh_accounting_handover`;

-- 删除代账合同
DELETE FROM `gh_accounting_contract`;

-- ============================================
-- 第六步：删除工商任务数据
-- ============================================

-- 删除工商任务
DELETE FROM `gh_business_task`;

-- ============================================
-- 第七步：删除地址管理数据
-- ============================================

-- 删除地址中心
DELETE FROM `gh_address_center`;

-- ============================================
-- 第八步：删除线索相关数据
-- ============================================

-- 删除跟进记录
DELETE FROM `gh_followup_detail`;

-- 删除线索（商机）
DELETE FROM `gh_opportunity`;

-- ============================================
-- 第九步：删除客户数据
-- ============================================

-- 删除客户
DELETE FROM `gh_customer`;

-- ============================================
-- 恢复外键检查
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 验证删除结果
-- ============================================

SELECT 
    'gh_opportunity' AS table_name, 
    COUNT(*) AS remaining_count 
FROM gh_opportunity
UNION ALL
SELECT 
    'gh_followup_detail', 
    COUNT(*) 
FROM gh_followup_detail
UNION ALL
SELECT 
    'gh_customer', 
    COUNT(*) 
FROM gh_customer
UNION ALL
SELECT 
    'gh_order', 
    COUNT(*) 
FROM gh_order
UNION ALL
SELECT 
    'gh_order_payment', 
    COUNT(*) 
FROM gh_order_payment
UNION ALL
SELECT 
    'gh_order_expense', 
    COUNT(*) 
FROM gh_order_expense
UNION ALL
SELECT 
    'gh_bank_diary', 
    COUNT(*) 
FROM gh_bank_diary
UNION ALL
SELECT 
    'gh_reimbursement', 
    COUNT(*) 
FROM gh_reimbursement
UNION ALL
SELECT 
    'gh_address_center', 
    COUNT(*) 
FROM gh_address_center
UNION ALL
SELECT 
    'gh_accounting_contract', 
    COUNT(*) 
FROM gh_accounting_contract
UNION ALL
SELECT 
    'gh_accounting_handover', 
    COUNT(*) 
FROM gh_accounting_handover
UNION ALL
SELECT 
    'gh_accounting_handover_audit', 
    COUNT(*) 
FROM gh_accounting_handover_audit
UNION ALL
SELECT 
    'gh_business_task', 
    COUNT(*) 
FROM gh_business_task
UNION ALL
SELECT 
    'sys_audit_task', 
    COUNT(*) 
FROM sys_audit_task
UNION ALL
SELECT 
    'sys_audit_task_cost', 
    COUNT(*) 
FROM sys_audit_task_cost;

