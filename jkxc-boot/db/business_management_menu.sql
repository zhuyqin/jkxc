-- ============================================
-- 业务管理菜单配置SQL脚本
-- 包含：业务管理-代账管理、业务管理-地址管理
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @business_management_menu_id = 'businessmgt20250110';
SET @business_accounting_menu_id = 'bizacctg20250110';
SET @business_address_menu_id = 'bizaddr20250110';

-- ============================================
-- 第一部分：创建业务管理一级菜单
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
    @business_management_menu_id,
    '',
    UNHEX('E4B89AE58AA1E7AEA1E79086'),
    '/business-management',
    'layouts/RouteView',
    'business-management',
    '/business-management/accounting',
    0,
    NULL,
    '1',
    6.0,
    1,
    'appstore',
    1,
    0,
    0,
    0,
    0,
    UNHEX('E4B89AE58AA1E7AEA1E79086E6A8A1E59D97'),
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
    name = UNHEX('E4B89AE58AA1E7AEA1E79086'),
    url = '/business-management',
    component = 'layouts/RouteView',
    component_name = 'business-management',
    redirect = '/business-management/accounting',
    sort_no = 6.0,
    icon = 'appstore',
    description = UNHEX('E4B89AE58AA1E7AEA1E79086E6A8A1E59D97'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建业务管理-代账管理子菜单
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
    @business_accounting_menu_id,
    @business_management_menu_id,
    UNHEX('E4BBA3E8B4A6E7AEA1E79086'),
    '/business-management/accounting',
    'accounting/AccountingContractList',
    'AccountingContractList',
    NULL,
    1,
    'business:accounting:list',
    '1',
    1.0,
    0,
    'file-protect',
    1,
    1,
    0,
    0,
    UNHEX('E4B89AE58AA1E7AEA1E79086E4BBA3E8B4A6E7AEA1E79086'),
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
    name = UNHEX('E4BBA3E8B4A6E7AEA1E79086'),
    url = '/business-management/accounting',
    component = 'accounting/AccountingContractList',
    component_name = 'AccountingContractList',
    perms = 'business:accounting:list',
    sort_no = 1.0,
    icon = 'file-protect',
    description = UNHEX('E4B89AE58AA1E7AEA1E79086E4BBA3E8B4A6E7AEA1E79086'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：创建业务管理-地址管理子菜单
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
    @business_address_menu_id,
    @business_management_menu_id,
    UNHEX('E59CB0E59D80E7AEA1E79086'),
    '/business-management/address',
    'address/GhAddressCenterList',
    'GhAddressCenterList',
    NULL,
    1,
    'business:address:list',
    '1',
    2.0,
    0,
    'environment',
    1,
    1,
    0,
    0,
    UNHEX('E4B89AE58AA1E7AEA1E79086E59CB0E59D80E7AEA1E79086'),
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
    name = UNHEX('E59CB0E59D80E7AEA1E79086'),
    url = '/business-management/address',
    component = 'address/GhAddressCenterList',
    component_name = 'GhAddressCenterList',
    perms = 'business:address:list',
    sort_no = 2.0,
    icon = 'environment',
    description = UNHEX('E4B89AE58AA1E7AEA1E79086E59CB0E59D80E7AEA1E79086'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第四部分：为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @business_management_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @business_management_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @business_management_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @business_accounting_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @business_accounting_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @business_accounting_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @business_address_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @business_address_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @business_address_menu_id
);

