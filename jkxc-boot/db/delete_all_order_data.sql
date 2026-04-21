-- ============================================
-- 删除所有订单相关数据SQL脚本
-- 包括订单主表及所有关联的业务数据
-- 执行前请备份数据库！
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- 禁用外键检查（临时）
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一步：删除代账交接相关数据
-- ============================================

-- 删除代账交接审核记录
DELETE FROM `gh_accounting_handover_audit`;

-- 删除代账交接
DELETE FROM `gh_accounting_handover`;

-- ============================================
-- 第二步：删除合同数据
-- ============================================

-- 删除合同
DELETE FROM `gh_accounting_contract`;

-- ============================================
-- 第三步：删除工商任务数据
-- ============================================

-- 删除工商任务
DELETE FROM `gh_business_task`;

-- ============================================
-- 第四步：删除订单审核相关数据
-- ============================================

-- 删除订单审核记录
DELETE FROM `gh_order_audit`;

-- ============================================
-- 第五步：删除订单操作日志
-- ============================================

-- 删除订单操作日志
DELETE FROM `gh_order_operation_log`;

-- ============================================
-- 第六步：删除订单表单数据
-- ============================================

-- 删除订单表单数据
DELETE FROM `gh_order_form_data`;

-- ============================================
-- 第七步：删除订单支出数据
-- ============================================

-- 删除订单支出记录
DELETE FROM `gh_order_expense`;

-- ============================================
-- 第八步：删除订单支付数据
-- ============================================

-- 删除订单支付记录
DELETE FROM `gh_order_payment`;

-- ============================================
-- 第九步：删除订单步骤数据
-- ============================================

-- 删除订单步骤记录
DELETE FROM `gh_order_step`;

-- ============================================
-- 第十步：删除订单主表数据
-- ============================================

-- 删除订单主表
DELETE FROM `gh_order`;

-- ============================================
-- 恢复外键检查
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 验证删除结果
-- ============================================

SELECT 
    'gh_order' AS table_name, 
    COUNT(*) AS remaining_count 
FROM gh_order
UNION ALL
SELECT 
    'gh_order_audit', 
    COUNT(*) 
FROM gh_order_audit
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
    'gh_order_form_data', 
    COUNT(*) 
FROM gh_order_form_data
UNION ALL
SELECT 
    'gh_order_operation_log', 
    COUNT(*) 
FROM gh_order_operation_log
UNION ALL
SELECT 
    'gh_order_step', 
    COUNT(*) 
FROM gh_order_step
UNION ALL
SELECT 
    'gh_business_task', 
    COUNT(*) 
FROM gh_business_task
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
    'gh_accounting_contract', 
    COUNT(*) 
FROM gh_accounting_contract;

