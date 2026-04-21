-- ============================================
-- 修复菜单中文名称SQL脚本
-- 由于MCP执行时字符编码问题，需要手动执行此脚本修复中文名称
-- 请在支持utf8mb4的MySQL客户端中执行
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- 修复任务中心菜单名称
UPDATE `sys_permission` SET `name` = '任务中心', `description` = '任务中心模块' WHERE `id` = 'taskcenter20250101';

-- 修复工商任务菜单名称
UPDATE `sys_permission` SET `name` = '工商任务', `description` = '任务中心-工商任务' WHERE `id` = 'taskcenterbusiness20250101';

-- 修复新签订单菜单名称
UPDATE `sys_permission` SET `name` = '新签订单', `description` = 'FD核算-新签订单，用于出纳审核订单' WHERE `id` = 'fdneworder20250101';

-- 修复工商流程管理菜单名称
UPDATE `sys_permission` SET `name` = '工商流程管理', `description` = '工商流程管理，配置工商相关业务流程' WHERE `id` = 'businessprocess202501';

-- 验证修复结果
SELECT id, name, url, component, parent_id, sort_no, status, del_flag 
FROM `sys_permission`
WHERE id IN ('taskcenter20250101', 'taskcenterbusiness20250101', 'fdaccounting20250101', 'fdneworder20250101', 'businessprocess202501')
ORDER BY sort_no;

