-- ============================================
-- 会计管理菜单配置SQL脚本
-- 包含：会计管理-代账交接、会计管理-合同管理
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @accounting_management_menu_id = 'accountingmanagement20250102';
SET @accounting_handover_menu_id = 'accountinghandover20250102';
SET @accounting_contract_menu_id = 'accountingcontract20250102';

-- ============================================
-- 第一部分：创建会计管理一级菜单
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
    @accounting_management_menu_id,
    '',
    '会计管理',
    '/accounting-management',
    'layouts/RouteView',
    'accounting-management',
    '/accounting-management/handover',
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
    0,
    '会计管理模块',
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
    name = '会计管理',
    url = '/accounting-management',
    component = 'layouts/RouteView',
    component_name = 'accounting-management',
    redirect = '/accounting-management/handover',
    sort_no = 5.0,
    icon = 'calculator',
    description = '会计管理模块',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建会计管理-代账交接子菜单
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
    @accounting_handover_menu_id,
    @accounting_management_menu_id,
    '代账交接',
    '/accounting-management/handover',
    'accounting/AccountingHandoverList',
    'AccountingHandoverList',
    NULL,
    1,
    'accounting:handover:list',
    '1',
    1.0,
    0,
    'file-text',
    1,
    1,
    0,
    0,
    '会计管理-代账交接',
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
    name = '代账交接',
    url = '/accounting-management/handover',
    component = 'accounting/AccountingHandoverList',
    component_name = 'AccountingHandoverList',
    perms = 'accounting:handover:list',
    sort_no = 1.0,
    icon = 'file-text',
    description = '会计管理-代账交接',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：创建会计管理-合同管理子菜单
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
    @accounting_contract_menu_id,
    @accounting_management_menu_id,
    '合同管理',
    '/accounting-management/contract',
    'accounting/AccountingContractList',
    'AccountingContractList',
    NULL,
    1,
    'accounting:contract:list',
    '1',
    2.0,
    0,
    'file-protect',
    1,
    1,
    0,
    0,
    '会计管理-合同管理',
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
    name = '合同管理',
    url = '/accounting-management/contract',
    component = 'accounting/AccountingContractList',
    component_name = 'AccountingContractList',
    perms = 'accounting:contract:list',
    sort_no = 2.0,
    icon = 'file-protect',
    description = '会计管理-合同管理',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第四部分：为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @accounting_management_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @accounting_management_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @accounting_management_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @accounting_handover_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @accounting_handover_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @accounting_handover_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @accounting_contract_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @accounting_contract_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @accounting_contract_menu_id
);

