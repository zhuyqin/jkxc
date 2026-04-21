-- ============================================
-- 续费审核菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @finance_management_menu_id = 'financemanagement20250110';
SET @renewal_audit_menu_id = 'renewalaudit20250115';

-- ============================================
-- 创建财务管理-续费审核子菜单
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
    @renewal_audit_menu_id,
    @finance_management_menu_id,
    UNHEX('E7BBADE8B4B9E5AEA1E6A0B8'),
    '/finance-management/renewal-audit',
    'finance/RenewalAuditList',
    'RenewalAuditList',
    NULL,
    1,
    'finance:renewal:audit',
    '1',
    3.0,
    0,
    'audit',
    1,
    1,
    0,
    0,
    UNHEX('E7BBADE8B4B9E5AEA1E6A0B8E7AEA1E79086'),
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
    name = UNHEX('E7BBADE8B4B9E5AEA1E6A0B8'),
    url = '/finance-management/renewal-audit',
    component = 'finance/RenewalAuditList',
    component_name = 'RenewalAuditList',
    perms = 'finance:renewal:audit',
    sort_no = 3.0,
    icon = 'audit',
    description = UNHEX('E7BBADE8B4B9E5AEA1E6A0B8E7AEA1E79086'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT(@renewal_audit_menu_id, '_', r.id) as id,
    r.id as role_id,
    @renewal_audit_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE
    `operate_date` = NOW();

