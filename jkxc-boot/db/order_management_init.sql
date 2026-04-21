-- 订单管理功能初始化SQL脚本
-- 此脚本会自动执行，无需手动执行

-- ============================================
-- 第一部分：创建订单管理菜单
-- ============================================

-- 菜单ID（使用32位字符串）
SET @order_menu_id = 'ordermanagement20250101';
SET @order_list_menu_id = 'orderlist20250101';

-- 1. 插入订单管理一级菜单（如果不存在）
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
    @order_menu_id,
    '',  -- 一级菜单，parent_id为空
    '订单管理',
    '/order',
    'layouts/RouteView',
    'order-management',
    '/order/list',
    0,  -- 菜单类型：0-一级菜单
    NULL,
    '1',
    3.0,  -- 排序号
    1,  -- always_show
    'shopping',
    1,  -- 是否路由
    0,  -- 是否叶子节点（一级菜单不是叶子节点）
    0,  -- 是否缓存
    0,  -- 是否隐藏
    '订单管理模块',
    '1',  -- 状态：1-启用
    0,  -- 删除标志：0-正常
    0,  -- 是否配置数据权限
    'admin',
    NOW(),
    NULL,
    NULL,
    0  -- 是否外链
)
ON DUPLICATE KEY UPDATE
    name = '订单管理',
    url = '/order',
    component = 'layouts/RouteView',
    component_name = 'order-management',
    redirect = '/order/list',
    sort_no = 3.0,
    icon = 'shopping',
    description = '订单管理模块',
    status = '1',
    del_flag = 0;

-- 2. 插入订单列表子菜单（如果不存在）
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
    @order_list_menu_id,
    @order_menu_id,  -- 父菜单ID
    '订单列表',
    '/order/list',
    'order/OrderList',
    'order-list',
    NULL,
    1,  -- 菜单类型：1-子菜单
    'order:list',
    '1',
    1.0,  -- 排序号
    0,
    'ordered-list',
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    '订单列表管理',
    '1',  -- 状态：1-启用
    0,  -- 删除标志：0-正常
    0,  -- 是否配置数据权限
    'admin',
    NOW(),
    NULL,
    NULL,
    0  -- 是否外链
)
ON DUPLICATE KEY UPDATE
    name = '订单列表',
    url = '/order/list',
    component = 'order/OrderList',
    component_name = 'order-list',
    perms = 'order:list',
    sort_no = 1.0,
    icon = 'ordered-list',
    description = '订单列表管理',
    status = '1',
    del_flag = 0;

-- ============================================
-- 第二部分：为管理员角色分配菜单权限
-- ============================================

-- 为默认管理员角色分配订单管理菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_order_menu_admin_', @order_menu_id),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID
    @order_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @order_menu_id
);

-- 为默认管理员角色分配订单列表菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_order_list_admin_', @order_list_menu_id),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID
    @order_list_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @order_list_menu_id
);

-- ============================================
-- 第三部分：修复菜单名称编码问题（如果出现乱码）
-- ============================================

-- 修复订单管理菜单名称编码
UPDATE sys_permission SET name = UNHEX('E8AEA2E58D95E7AEA1E79086') WHERE id = @order_menu_id;

-- 修复订单列表菜单名称编码
UPDATE sys_permission SET name = UNHEX('E8AEA2E58D95E58897E8A1A8') WHERE id = @order_list_menu_id;

-- ============================================
-- 验证脚本执行结果
-- ============================================

SELECT 
    id,
    name,
    parent_id,
    url,
    component,
    sort_no,
    status,
    del_flag
FROM `sys_permission`
WHERE id IN (@order_menu_id, @order_list_menu_id)
ORDER BY sort_no;

