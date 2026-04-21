-- ============================================
-- 首页菜单配置SQL脚本
-- 包含：数据统计页（Dashboard）、工作台（Workbench）
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @dashboard_menu_id = 'dashboard20250201';
SET @workbench_menu_id = 'workbench20250201';

-- ============================================
-- 第一部分：创建数据统计页菜单（如果不存在）
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
    @dashboard_menu_id,
    '',
    '数据统计',
    '/dashboard/statistics',
    'dashboard/Dashboard',
    'Dashboard',
    NULL,
    1,
    'dashboard:statistics',
    '1',
    0.1,
    0,
    'dashboard',
    1,
    1,
    0,
    0,
    '数据统计页，展示关键业务指标和趋势图表',
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
    name = '数据统计',
    url = '/dashboard/statistics',
    component = 'dashboard/Dashboard',
    component_name = 'Dashboard',
    perms = 'dashboard:statistics',
    icon = 'dashboard',
    description = '数据统计页，展示关键业务指标和趋势图表',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建工作台菜单（如果不存在）
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
    @workbench_menu_id,
    '',
    '工作台',
    '/dashboard/workbench',
    'dashboard/Workbench',
    'Workbench',
    NULL,
    1,
    'dashboard:workbench',
    '1',
    0.2,
    0,
    'appstore',
    1,
    1,
    0,
    0,
    '工作台，展示待办任务和快捷操作',
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
    name = '工作台',
    url = '/dashboard/workbench',
    component = 'dashboard/Workbench',
    component_name = 'Workbench',
    perms = 'dashboard:workbench',
    icon = 'appstore',
    description = '工作台，展示待办任务和快捷操作',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 说明
-- ============================================
-- 1. 这两个菜单可以作为首页使用
-- 2. 需要在 RoleIndexConfigEnum 中配置角色映射
-- 3. 建议：
--    - 领导角色 → dashboard/Dashboard（数据统计页）
--    - 员工角色 → dashboard/Workbench（工作台）
-- 4. 也可以让用户手动切换首页视图

