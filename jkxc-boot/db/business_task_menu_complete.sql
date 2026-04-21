-- ============================================
-- 工商任务完整菜单配置SQL脚本
-- 包含：任务中心-工商任务、FD核算-新签订单
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @task_center_menu_id = 'taskcenter20250101';
SET @task_center_business_menu_id = 'taskcenterbusiness20250101';
SET @fd_accounting_menu_id = 'fdaccounting20250101';
SET @fd_new_order_menu_id = 'fdneworder20250101';

-- ============================================
-- 第一部分：创建任务中心一级菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`,
    `parent_id`,
    `name`,
    `url`,
    `component`,
    `component_name`,
    `redirect`,
    `menu_type`,
    `perms`,
    `perms_type`,
    `sort_no`,
    `always_show`,
    `icon`,
    `is_route`,
    `is_leaf`,
    `keep_alive`,
    `hidden`,
    `description`,
    `status`,
    `del_flag`,
    `rule_flag`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `internal_or_external`
) VALUES (
    @task_center_menu_id,
    '',
    '任务中心',
    '/task-center',
    'layouts/RouteView',
    'task-center',
    '/task-center/business-task',
    0,
    NULL,
    '1',
    4.0,
    1,
    'box-plot',
    1,
    0,
    0,
    0,
    '任务中心模块',
    '1',
    0,
    0,
    'admin',
    NOW(),
    NULL,
    NULL,
    0
)
ON DUPLICATE KEY UPDATE
    name = '任务中心',
    url = '/task-center',
    component = 'layouts/RouteView',
    component_name = 'task-center',
    redirect = '/task-center/business-task',
    sort_no = 4.0,
    icon = 'box-plot',
    description = '任务中心模块',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建任务中心-工商任务子菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`,
    `parent_id`,
    `name`,
    `url`,
    `component`,
    `component_name`,
    `redirect`,
    `menu_type`,
    `perms`,
    `perms_type`,
    `sort_no`,
    `always_show`,
    `icon`,
    `is_route`,
    `is_leaf`,
    `keep_alive`,
    `hidden`,
    `description`,
    `status`,
    `del_flag`,
    `rule_flag`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `internal_or_external`
) VALUES (
    @task_center_business_menu_id,
    @task_center_menu_id,
    '工商任务',
    '/task-center/business-task',
    'order/BusinessTaskList',
    'BusinessTaskList',
    NULL,
    1,
    'task:center:business:task',
    '1',
    1.0,
    0,
    'box-plot',
    1,
    1,
    0,
    0,
    '任务中心-工商任务，包含公海待接收、待本人接收、任务、交接、已完单、问题任务、回收站、工商经理审核等tab页',
    '1',
    0,
    0,
    'admin',
    NOW(),
    NULL,
    NULL,
    0
)
ON DUPLICATE KEY UPDATE
    name = '工商任务',
    url = '/task-center/business-task',
    component = 'order/BusinessTaskList',
    component_name = 'BusinessTaskList',
    perms = 'task:center:business:task',
    sort_no = 1.0,
    icon = 'box-plot',
    description = '任务中心-工商任务，包含公海待接收、待本人接收、任务、交接、已完单、问题任务、回收站、工商经理审核等tab页',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：创建FD核算一级菜单
-- ============================================

INSERT INTO `sys_permission` (
    `id`,
    `parent_id`,
    `name`,
    `url`,
    `component`,
    `component_name`,
    `redirect`,
    `menu_type`,
    `perms`,
    `perms_type`,
    `sort_no`,
    `always_show`,
    `icon`,
    `is_route`,
    `is_leaf`,
    `keep_alive`,
    `hidden`,
    `description`,
    `status`,
    `del_flag`,
    `rule_flag`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `internal_or_external`
) VALUES (
    @fd_accounting_menu_id,
    '',
    'FD核算',
    '/fd-accounting',
    'layouts/RouteView',
    'fd-accounting',
    '/fd-accounting/new-order',
    0,
    NULL,
    '1',
    5.0,
    1,
    'calculator',
    1,
    0,
    0,
    0,
    'FD核算模块',
    '1',
    0,
    0,
    'admin',
    NOW(),
    NULL,
    NULL,
    0
)
ON DUPLICATE KEY UPDATE
    name = 'FD核算',
    url = '/fd-accounting',
    component = 'layouts/RouteView',
    component_name = 'fd-accounting',
    redirect = '/fd-accounting/new-order',
    sort_no = 5.0,
    icon = 'calculator',
    description = 'FD核算模块',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第四部分：创建FD核算-新签订单子菜单（出纳审核）
-- ============================================

INSERT INTO `sys_permission` (
    `id`,
    `parent_id`,
    `name`,
    `url`,
    `component`,
    `component_name`,
    `redirect`,
    `menu_type`,
    `perms`,
    `perms_type`,
    `sort_no`,
    `always_show`,
    `icon`,
    `is_route`,
    `is_leaf`,
    `keep_alive`,
    `hidden`,
    `description`,
    `status`,
    `del_flag`,
    `rule_flag`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `internal_or_external`
) VALUES (
    @fd_new_order_menu_id,
    @fd_accounting_menu_id,
    '新签订单',
    '/fd-accounting/new-order',
    'order/FdNewOrderList',
    'FdNewOrderList',
    NULL,
    1,
    'fd:accounting:new:order',
    '1',
    1.0,
    0,
    'file-text',
    1,
    1,
    0,
    0,
    'FD核算-新签订单，用于出纳审核订单',
    '1',
    0,
    0,
    'admin',
    NOW(),
    NULL,
    NULL,
    0
)
ON DUPLICATE KEY UPDATE
    name = '新签订单',
    url = '/fd-accounting/new-order',
    component = 'order/FdNewOrderList',
    component_name = 'FdNewOrderList',
    perms = 'fd:accounting:new:order',
    sort_no = 1.0,
    icon = 'file-text',
    description = 'FD核算-新签订单，用于出纳审核订单',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第五部分：为管理员角色分配菜单权限
-- ============================================

-- 任务中心菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @task_center_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @task_center_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @task_center_menu_id
);

-- 任务中心-工商任务菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @task_center_business_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @task_center_business_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @task_center_business_menu_id
);

-- FD核算菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @fd_accounting_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @fd_accounting_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @fd_accounting_menu_id
);

-- FD核算-新签订单菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @fd_new_order_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @fd_new_order_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @fd_new_order_menu_id
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
WHERE id IN (@task_center_menu_id, @task_center_business_menu_id, @fd_accounting_menu_id, @fd_new_order_menu_id)
ORDER BY sort_no;

