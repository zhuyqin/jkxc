-- ============================================
-- 地址商管理菜单配置SQL脚本
-- 在地址管理下添加地址商管理子菜单
-- 执行前请备份数据库！
-- ============================================

-- 获取地址管理菜单ID（业务管理-地址管理）
SET @address_menu_id = (SELECT id FROM sys_permission WHERE url = '/business-management/address' AND del_flag = 0 LIMIT 1);

-- 如果地址管理菜单不存在，使用默认ID
SET @address_menu_id = IFNULL(@address_menu_id, 'bizaddr20250110');

-- 地址商管理菜单ID
SET @supplier_menu_id = 'supplier20250118';

-- ============================================
-- 创建地址商管理菜单
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
    @supplier_menu_id,
    @address_menu_id,
    '地址商管理',
    '/business-management/supplier',
    'supplier/SupplierList',
    'SupplierList',
    NULL,
    1,
    'business:supplier:list',
    '1',
    1.0,
    0,
    'shop',
    1,
    1,
    0,
    0,
    '地址商信息维护',
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
    name = '地址商管理',
    url = '/business-management/supplier',
    component = 'supplier/SupplierList',
    component_name = 'SupplierList',
    perms = 'business:supplier:list',
    sort_no = 1.0,
    icon = 'shop',
    description = '地址商信息维护',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @supplier_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @supplier_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_permission 
    WHERE role_id = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND permission_id = @supplier_menu_id
);

