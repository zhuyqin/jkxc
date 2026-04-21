-- ============================================
-- 菜单修复完整SQL脚本
-- 修复：菜单乱码、FD核算-新签订单菜单、BusinessProcessList路径
-- 执行前请备份数据库！
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- 菜单ID定义
SET @task_center_menu_id = 'taskcenter20250101';
SET @task_center_business_menu_id = 'taskcenterbusiness20250101';
SET @fd_accounting_menu_id = 'fdaccounting20250101';
SET @fd_new_order_menu_id = 'fdneworder20250101';
SET @business_process_menu_id = 'businessprocess202501';

-- ============================================
-- 第一部分：删除旧菜单（如果存在）
-- ============================================

-- 删除旧的菜单权限关联
DELETE FROM `sys_role_permission` WHERE `permission_id` IN (
    @task_center_menu_id, 
    @task_center_business_menu_id, 
    @fd_accounting_menu_id, 
    @fd_new_order_menu_id,
    @business_process_menu_id
);

-- 删除旧菜单
DELETE FROM `sys_permission` WHERE `id` IN (
    @task_center_menu_id, 
    @task_center_business_menu_id, 
    @fd_accounting_menu_id, 
    @fd_new_order_menu_id,
    @business_process_menu_id
);

-- ============================================
-- 第二部分：创建任务中心一级菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`,
    `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
    `is_route`, `is_leaf`, `keep_alive`, `hidden`, `description`, `status`,
    `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`
) VALUES (
    @task_center_menu_id, '', '任务中心', '/task-center', 'layouts/RouteView', 'task-center', '/task-center/business-task',
    0, NULL, '1', 4.0, 1, 'box-plot',
    1, 0, 0, 0, '任务中心模块', '1',
    0, 0, 'admin', NOW(), NULL, NULL, 0
);

-- ============================================
-- 第三部分：创建任务中心-工商任务子菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`,
    `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
    `is_route`, `is_leaf`, `keep_alive`, `hidden`, `description`, `status`,
    `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`
) VALUES (
    @task_center_business_menu_id, @task_center_menu_id, '工商任务', '/task-center/business-task', 'order/BusinessTaskList', 'BusinessTaskList', NULL,
    1, 'task:center:business:task', '1', 1.0, 0, 'box-plot',
    1, 1, 0, 0, '任务中心-工商任务', '1',
    0, 0, 'admin', NOW(), NULL, NULL, 0
);

-- ============================================
-- 第四部分：创建FD核算一级菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`,
    `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
    `is_route`, `is_leaf`, `keep_alive`, `hidden`, `description`, `status`,
    `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`
) VALUES (
    @fd_accounting_menu_id, '', 'FD核算', '/fd-accounting', 'layouts/RouteView', 'fd-accounting', '/fd-accounting/new-order',
    0, NULL, '1', 5.0, 1, 'calculator',
    1, 0, 0, 0, 'FD核算模块', '1',
    0, 0, 'admin', NOW(), NULL, NULL, 0
);

-- ============================================
-- 第五部分：创建FD核算-新签订单子菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`,
    `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
    `is_route`, `is_leaf`, `keep_alive`, `hidden`, `description`, `status`,
    `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`
) VALUES (
    @fd_new_order_menu_id, @fd_accounting_menu_id, '新签订单', '/fd-accounting/new-order', 'order/FdNewOrderList', 'FdNewOrderList', NULL,
    1, 'fd:accounting:new:order', '1', 1.0, 0, 'file-text',
    1, 1, 0, 0, 'FD核算-新签订单，用于出纳审核订单', '1',
    0, 0, 'admin', NOW(), NULL, NULL, 0
);

-- ============================================
-- 第六部分：创建工商流程管理菜单（修复路径）
-- ============================================

INSERT INTO `sys_permission` (
    `id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`,
    `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
    `is_route`, `is_leaf`, `keep_alive`, `hidden`, `description`, `status`,
    `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`
) VALUES (
    @business_process_menu_id, 'd7d6e2e4e2934f2c9385a623fd98c6f3', '工商流程管理', '/isystem/businessProcess', 'system/BusinessProcessList', 'business-process-list', NULL,
    1, NULL, '1', 8.0, 0, 'file-text',
    1, 1, 0, 0, '工商流程管理，配置工商相关业务流程', '1',
    0, 0, 'admin', NOW(), NULL, NULL, 0
);

-- ============================================
-- 第七部分：为管理员角色分配菜单权限
-- ============================================

-- 任务中心菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT CONCAT('rp_', @task_center_menu_id, '_admin'), 'f6817f48af4fb3af11b9e8bf182f618b', @task_center_menu_id, NULL, NOW(), NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' AND `permission_id` = @task_center_menu_id
);

-- 任务中心-工商任务菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT CONCAT('rp_', @task_center_business_menu_id, '_admin'), 'f6817f48af4fb3af11b9e8bf182f618b', @task_center_business_menu_id, NULL, NOW(), NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' AND `permission_id` = @task_center_business_menu_id
);

-- FD核算菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT CONCAT('rp_', @fd_accounting_menu_id, '_admin'), 'f6817f48af4fb3af11b9e8bf182f618b', @fd_accounting_menu_id, NULL, NOW(), NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' AND `permission_id` = @fd_accounting_menu_id
);

-- FD核算-新签订单菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT CONCAT('rp_', @fd_new_order_menu_id, '_admin'), 'f6817f48af4fb3af11b9e8bf182f618b', @fd_new_order_menu_id, NULL, NOW(), NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' AND `permission_id` = @fd_new_order_menu_id
);

-- 工商流程管理菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT CONCAT('rp_', @business_process_menu_id, '_admin'), 'f6817f48af4fb3af11b9e8bf182f618b', @business_process_menu_id, NULL, NOW(), NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' AND `permission_id` = @business_process_menu_id
);

-- ============================================
-- 验证脚本执行结果
-- ============================================

SELECT 
    id,
    name,
    url,
    component,
    parent_id,
    sort_no,
    status,
    del_flag
FROM `sys_permission`
WHERE id IN (@task_center_menu_id, @task_center_business_menu_id, @fd_accounting_menu_id, @fd_new_order_menu_id, @business_process_menu_id)
ORDER BY sort_no;

