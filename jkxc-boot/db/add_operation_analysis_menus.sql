-- ============================================
-- 运营分析-流失分析、任务分析、客户画像、客户价值菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @operation_analysis_menu_id = 'operationanalysis20250128';
SET @loss_analysis_menu_id = 'lossanalysis20250131';
SET @task_analysis_menu_id = 'taskanalysis20250131';
SET @customer_portrait_menu_id = 'customerportrait20250131';
SET @customer_value_menu_id = 'customervalue20250131';

-- ============================================
-- 第一部分：确保运营分析一级菜单存在
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
-- 第二部分：创建流失分析子菜单
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
    @loss_analysis_menu_id,
    @operation_analysis_menu_id,
    '流失分析',
    '/operation-analysis/loss-analysis',
    'operation/LossAnalysis',
    'LossAnalysis',
    NULL,
    1,
    'operation:analysis:loss',
    '1',
    3.0,
    0,
    'fall',
    1,
    1,
    0,
    0,
    '客户流失分析，包含流失趋势、业务类型分布、业务员排名等',
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
    name = '流失分析',
    url = '/operation-analysis/loss-analysis',
    component = 'operation/LossAnalysis',
    component_name = 'LossAnalysis',
    perms = 'operation:analysis:loss',
    sort_no = 3.0,
    icon = 'fall',
    description = '客户流失分析，包含流失趋势、业务类型分布、业务员排名等',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：创建任务分析子菜单
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
    @task_analysis_menu_id,
    @operation_analysis_menu_id,
    '任务分析',
    '/operation-analysis/task-analysis',
    'operation/TaskCenterAnalysis',
    'TaskCenterAnalysis',
    NULL,
    1,
    'operation:analysis:task',
    '1',
    4.0,
    0,
    'check-circle',
    1,
    1,
    0,
    0,
    '任务中心分析，包含任务趋势、业务类型分布、业务员排名等',
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
    name = '任务分析',
    url = '/operation-analysis/task-analysis',
    component = 'operation/TaskCenterAnalysis',
    component_name = 'TaskCenterAnalysis',
    perms = 'operation:analysis:task',
    sort_no = 4.0,
    icon = 'check-circle',
    description = '任务中心分析，包含任务趋势、业务类型分布、业务员排名等',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第四部分：创建客户画像分析子菜单
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
    @customer_portrait_menu_id,
    @operation_analysis_menu_id,
    '客户画像',
    '/operation-analysis/customer-portrait',
    'operation/CustomerPortraitAnalysis',
    'CustomerPortraitAnalysis',
    NULL,
    1,
    'operation:analysis:customer:portrait',
    '1',
    5.0,
    0,
    'user',
    1,
    1,
    0,
    0,
    '客户画像分析，包含客户分布、业务类型、业务员排名等',
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
    name = '客户画像',
    url = '/operation-analysis/customer-portrait',
    component = 'operation/CustomerPortraitAnalysis',
    component_name = 'CustomerPortraitAnalysis',
    perms = 'operation:analysis:customer:portrait',
    sort_no = 5.0,
    icon = 'user',
    description = '客户画像分析，包含客户分布、业务类型、业务员排名等',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第五部分：创建客户价值分析子菜单
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
    @customer_value_menu_id,
    @operation_analysis_menu_id,
    '客户价值',
    '/operation-analysis/customer-value',
    'operation/CustomerValueAnalysis',
    'CustomerValueAnalysis',
    NULL,
    1,
    'operation:analysis:customer:value',
    '1',
    6.0,
    0,
    'dollar',
    1,
    1,
    0,
    0,
    '客户价值分析，包含高价值客户分布、业务类型、业务员排名等',
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
    name = '客户价值',
    url = '/operation-analysis/customer-value',
    component = 'operation/CustomerValueAnalysis',
    component_name = 'CustomerValueAnalysis',
    perms = 'operation:analysis:customer:value',
    sort_no = 6.0,
    icon = 'dollar',
    description = '客户价值分析，包含高价值客户分布、业务类型、业务员排名等',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第六部分：为管理员角色分配菜单权限
-- ============================================

-- 流失分析菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @loss_analysis_menu_id, '_admin'),
    r.id as role_id,
    @loss_analysis_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE
    `operate_date` = NOW();

-- 任务分析菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @task_analysis_menu_id, '_admin'),
    r.id as role_id,
    @task_analysis_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE
    `operate_date` = NOW();

-- 客户画像菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @customer_portrait_menu_id, '_admin'),
    r.id as role_id,
    @customer_portrait_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE
    `operate_date` = NOW();

-- 客户价值菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @customer_value_menu_id, '_admin'),
    r.id as role_id,
    @customer_value_menu_id as permission_id,
    NULL as data_rule_ids,
    NOW() as operate_date,
    '127.0.0.1' as operate_ip
FROM `sys_role` r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE
    `operate_date` = NOW();

SELECT '运营分析-流失分析、任务分析、客户画像、客户价值菜单配置完成！' AS result;

