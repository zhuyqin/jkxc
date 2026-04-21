-- ============================================
-- 运营分析-新签订单分析菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @operation_analysis_menu_id = 'operationanalysis20250128';
SET @new_order_analysis_menu_id = 'neworderanalysis20250128';

-- ============================================
-- 第一部分：创建运营分析一级菜单（如果不存在）
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
    @operation_analysis_menu_id,
    '',
    '运营分析',
    '/operation-analysis',
    'layouts/RouteView',
    'operation-analysis',
    '/operation-analysis/new-order-analysis',
    0,
    NULL,
    '1',
    5.0,
    1,
    'bar-chart',
    1,
    0,
    0,
    0,
    '运营分析模块',
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
    name = '运营分析',
    url = '/operation-analysis',
    component = 'layouts/RouteView',
    component_name = 'operation-analysis',
    redirect = '/operation-analysis/new-order-analysis',
    sort_no = 5.0,
    icon = 'bar-chart',
    description = '运营分析模块',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建新签订单分析子菜单
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
    @new_order_analysis_menu_id,
    @operation_analysis_menu_id,
    '新签订单分析',
    '/operation-analysis/new-order-analysis',
    'operation/NewOrderAnalysis',
    'NewOrderAnalysis',
    NULL,
    1,
    'operation:analysis:new:order',
    '1',
    1.0,
    0,
    'line-chart',
    1,
    1,
    0,
    0,
    '新签订单统计分析，使用ECharts图表展示',
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
    name = '新签订单分析',
    url = '/operation-analysis/new-order-analysis',
    component = 'operation/NewOrderAnalysis',
    component_name = 'NewOrderAnalysis',
    perms = 'operation:analysis:new:order',
    sort_no = 1.0,
    icon = 'line-chart',
    description = '新签订单统计分析，使用ECharts图表展示',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：为管理员角色分配菜单权限
-- ============================================

-- 运营分析菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @operation_analysis_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @operation_analysis_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @operation_analysis_menu_id
);

-- 新签订单分析菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @new_order_analysis_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @new_order_analysis_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @new_order_analysis_menu_id
);

-- ============================================
-- 完成
-- ============================================
SELECT '运营分析-新签订单分析菜单配置完成！' AS result;

