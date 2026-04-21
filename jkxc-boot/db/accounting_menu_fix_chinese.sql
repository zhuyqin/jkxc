-- ============================================
-- 会计管理菜单中文名称修复SQL脚本
-- 由于编码问题，需要手动执行此脚本更新中文名称
-- 请在支持utf8mb4的MySQL客户端中执行
-- ============================================

-- 更新会计管理一级菜单名称
UPDATE `sys_permission` 
SET `name` = '会计管理' 
WHERE `id` = 'accountingmanagement20250102';

-- 更新代账交接子菜单名称
UPDATE `sys_permission` 
SET `name` = '代账交接' 
WHERE `id` = 'accountinghandover20250102';

-- 更新合同管理子菜单名称
UPDATE `sys_permission` 
SET `name` = '合同管理' 
WHERE `id` = 'accountingcontract20250102';

-- ============================================
-- 为管理员角色分配菜单权限（如果还没有分配）
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_accountingmanagement20250102_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    'accountingmanagement20250102',
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = 'accountingmanagement20250102'
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_accountinghandover20250102_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    'accountinghandover20250102',
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = 'accountinghandover20250102'
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_accountingcontract20250102_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    'accountingcontract20250102',
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = 'accountingcontract20250102'
);

