-- ============================================
-- 报销管理菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @reimbursement_management_menu_id = 'reimbursementmanagement20250112';
SET @reimbursement_list_menu_id = 'reimbursementlist20250112';

-- ============================================
-- 第一部分：创建报销管理一级菜单
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
    @reimbursement_management_menu_id,
    '',
    UNHEX('E68AA5E99480E7AEA1E79086'),
    '/reimbursement-management',
    'layouts/RouteView',
    'reimbursement-management',
    '/reimbursement-management/list',
    0,
    NULL,
    '1',
    7.0,
    1,
    'file-text',
    1,
    0,
    0,
    0,
    0,
    UNHEX('E68AA5E99480E7AEA1E79086E6A8A1E59D97'),
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
    name = UNHEX('E68AA5E99480E7AEA1E79086'),
    url = '/reimbursement-management',
    component = 'layouts/RouteView',
    component_name = 'reimbursement-management',
    redirect = '/reimbursement-management/list',
    sort_no = 7.0,
    icon = 'file-text',
    description = UNHEX('E68AA5E99480E7AEA1E79086E6A8A1E59D97'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建报销管理-报销列表子菜单
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
    @reimbursement_list_menu_id,
    @reimbursement_management_menu_id,
    UNHEX('E68AA5E99480E58897E8A1A8'),
    '/reimbursement-management/list',
    'reimbursement/ReimbursementList',
    'ReimbursementList',
    NULL,
    1,
    'reimbursement:list',
    '1',
    1.0,
    0,
    'file-text',
    1,
    1,
    0,
    0,
    UNHEX('E68AA5E99480E7AEA1E79086E68AA5E99480E58897E8A1A8'),
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
    name = UNHEX('E68AA5E99480E58897E8A1A8'),
    url = '/reimbursement-management/list',
    component = 'reimbursement/ReimbursementList',
    component_name = 'ReimbursementList',
    perms = 'reimbursement:list',
    sort_no = 1.0,
    icon = 'file-text',
    description = UNHEX('E68AA5E99480E7AEA1E79086E68AA5E99480E58897E8A1A8'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @reimbursement_management_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @reimbursement_management_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @reimbursement_management_menu_id
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @reimbursement_list_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @reimbursement_list_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @reimbursement_list_menu_id
);

