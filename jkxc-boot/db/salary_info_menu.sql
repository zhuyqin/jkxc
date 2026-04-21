-- ============================================
-- 工资发放-信息维护菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @salary_management_menu_id = 'salarymanagement20250112';
SET @salary_info_menu_id = 'salaryinfo20250112';
SET @salary_payment_menu_id = 'salarypayment20250112';

-- ============================================
-- 第一部分：创建工资发放一级菜单
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
    @salary_management_menu_id,
    '',
    UNHEX('E5B7A5E8B584E58F91E694BE'),
    '/salary-management',
    'layouts/RouteView',
    'salary-management',
    '/salary-management/info',
    0,
    NULL,
    '1',
    8.0,
    1,
    'pay-circle',
    1,
    0,
    0,
    0,
    0,
    UNHEX('E5B7A5E8B584E58F91E694BEE6A8A1E59D97'),
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
    name = UNHEX('E5B7A5E8B584E58F91E694BE'),
    url = '/salary-management',
    component = 'layouts/RouteView',
    component_name = 'salary-management',
    redirect = '/salary-management/info',
    sort_no = 8.0,
    icon = 'pay-circle',
    description = UNHEX('E5B7A5E8B584E58F91E694BEE6A8A1E59D97'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建工资发放-信息维护子菜单
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
    @salary_info_menu_id,
    @salary_management_menu_id,
    UNHEX('E4BFA1E681AFE7BBB4E68AA4'),
    '/salary-management/info',
    'salary/SalaryInfoList',
    'SalaryInfoList',
    NULL,
    1,
    'salary:info:manage',
    '1',
    1.0,
    0,
    'user',
    1,
    1,
    0,
    0,
    UNHEX('E5B7A5E8B584E4BFA1E681AFE7BBB4E68AA4'),
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
    name = UNHEX('E4BFA1E681AFE7BBB4E68AA4'),
    url = '/salary-management/info',
    component = 'salary/SalaryInfoList',
    component_name = 'SalaryInfoList',
    perms = 'salary:info:manage',
    sort_no = 1.0,
    icon = 'user',
    description = UNHEX('E5B7A5E8B584E4BFA1E681AFE7BBB4E68AA4'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：创建工资发放-工资发放子菜单
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
    @salary_payment_menu_id,
    @salary_management_menu_id,
    UNHEX('E5B7A5E8B584E58F91E694BE'),
    '/salary-management/payment',
    'salary/SalaryPaymentList',
    'SalaryPaymentList',
    NULL,
    1,
    'salary:payment:manage',
    '1',
    2.0,
    0,
    'dollar',
    1,
    1,
    0,
    0,
    UNHEX('E5B7A5E8B584E58F91E694BEE7AEA1E79086'),
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
    name = UNHEX('E5B7A5E8B584E58F91E694BE'),
    url = '/salary-management/payment',
    component = 'salary/SalaryPaymentList',
    component_name = 'SalaryPaymentList',
    perms = 'salary:payment:manage',
    sort_no = 2.0,
    icon = 'dollar',
    description = UNHEX('E5B7A5E8B584E58F91E694BEE7AEA1E79086'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第四部分：为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @salary_management_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @salary_management_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @salary_management_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @salary_info_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @salary_info_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @salary_info_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @salary_payment_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @salary_payment_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @salary_payment_menu_id
);

