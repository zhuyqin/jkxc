-- ============================================
-- 财务管理菜单配置SQL脚本
-- 包含：财务管理-未收订单
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @finance_management_menu_id = 'financemanagement20250110';
SET @unreceived_order_menu_id = 'unreceivedorder20250110';

-- ============================================
-- 第一部分：创建财务管理一级菜单
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
    @finance_management_menu_id,
    '',
    UNHEX('E8B4A2E58AA1E7AEA1E79086'),
    '/finance-management',
    'layouts/RouteView',
    'finance-management',
    '/finance-management/unreceived-order',
    0,
    NULL,
    '1',
    6.0,
    1,
    'dollar',
    1,
    0,
    0,
    0,
    0,
    UNHEX('E8B4A2E58AA1E7AEA1E79086E6A8A1E59D97'),
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
    name = UNHEX('E8B4A2E58AA1E7AEA1E79086'),
    url = '/finance-management',
    component = 'layouts/RouteView',
    component_name = 'finance-management',
    redirect = '/finance-management/unreceived-order',
    sort_no = 6.0,
    icon = 'dollar',
    description = UNHEX('E8B4A2E58AA1E7AEA1E79086E6A8A1E59D97'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建财务管理-未收订单子菜单
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
    @unreceived_order_menu_id,
    @finance_management_menu_id,
    UNHEX('E69CAAE694B6E8AEA2E58D95'),
    '/finance-management/unreceived-order',
    'order/UnreceivedOrderList',
    'UnreceivedOrderList',
    NULL,
    1,
    'finance:unreceived:order',
    '1',
    1.0,
    0,
    'file-text',
    1,
    1,
    0,
    0,
    UNHEX('E8B4A2E58AA1E7AEA1E79086E69CAAE694B6E8AEA2E58D95'),
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
    name = UNHEX('E69CAAE694B6E8AEA2E58D95'),
    url = '/finance-management/unreceived-order',
    component = 'order/UnreceivedOrderList',
    component_name = 'UnreceivedOrderList',
    perms = 'finance:unreceived:order',
    sort_no = 1.0,
    icon = 'file-text',
    description = UNHEX('E8B4A2E58AA1E7AEA1E79086E69CAAE694B6E8AEA2E58D95'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @finance_management_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @finance_management_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @finance_management_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @unreceived_order_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @unreceived_order_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @unreceived_order_menu_id
);

