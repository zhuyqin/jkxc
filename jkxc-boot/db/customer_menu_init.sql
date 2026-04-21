-- 客户管理菜单初始化脚本
SET @customer_menu_id = 'customermanagement202501';
SET @customer_list_menu_id = CONCAT(@customer_menu_id, '001');

-- 1. 插入客户管理一级菜单
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
    `hide_tab`,
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
    @customer_menu_id,
    NULL,
    UNHEX('E5AEA2E688B7E7AEA1E79086'),
    '/customer',
    'layouts/RouteView',
    NULL,
    '/customer/list',
    0,
    NULL,
    '1',
    4.0,
    1,
    'team',
    1,
    0,
    0,
    0,
    0,
    '客户管理模块',
    '1',
    0,
    0,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0
)
ON DUPLICATE KEY UPDATE 
    `name` = UNHEX('E5AEA2E688B7E7AEA1E79086'),
    `url` = '/customer',
    `component` = 'layouts/RouteView',
    `redirect` = '/customer/list',
    `menu_type` = 0,
    `sort_no` = 4.0,
    `always_show` = 1,
    `icon` = 'team',
    `is_route` = 1,
    `is_leaf` = 0,
    `keep_alive` = 0,
    `hidden` = 0,
    `description` = '客户管理模块',
    `status` = '1',
    `del_flag` = 0,
    `rule_flag` = 0,
    `update_time` = NOW();

-- 2. 插入客户列表子菜单
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
    `hide_tab`,
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
    @customer_list_menu_id,
    @customer_menu_id,
    UNHEX('E5AEA2E688B7E58897E8A1A8'),
    '/customer/list',
    'customer/CustomerList',
    NULL,
    NULL,
    1,
    NULL,
    '1',
    1.0,
    0,
    'user',
    1,
    1,
    0,
    0,
    0,
    '客户列表管理',
    '1',
    0,
    0,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0
)
ON DUPLICATE KEY UPDATE 
    `name` = UNHEX('E5AEA2E688B7E58897E8A1A8'),
    `parent_id` = @customer_menu_id,
    `url` = '/customer/list',
    `component` = 'customer/CustomerList',
    `menu_type` = 1,
    `sort_no` = 1.0,
    `always_show` = 0,
    `icon` = 'user',
    `is_route` = 1,
    `is_leaf` = 1,
    `keep_alive` = 0,
    `hidden` = 0,
    `description` = '客户列表管理',
    `status` = '1',
    `del_flag` = 0,
    `rule_flag` = 0,
    `update_time` = NOW();

-- 3. 为管理员角色分配权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('r_p_', @customer_menu_id, '_', r.id) as id,
    r.id as role_id,
    @customer_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE `operate_date` = NOW();

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('r_p_', @customer_list_menu_id, '_', r.id) as id,
    r.id as role_id,
    @customer_list_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE `operate_date` = NOW();

