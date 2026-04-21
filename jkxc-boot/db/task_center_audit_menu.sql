-- ============================================
-- 任务中心审核任务菜单配置SQL脚本
-- 包含：一次性任务、周期任务
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @task_center_menu_id = 'taskcenter20250101';  -- 任务中心一级菜单ID（如果已存在）
SET @one_time_task_menu_id = 'onetimetask20250104';
SET @recurring_task_menu_id = 'recurringtask20250104';

-- ============================================
-- 第一部分：确保任务中心一级菜单存在
-- ============================================

-- 如果任务中心菜单不存在，则创建（如果已存在则跳过）
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
) 
SELECT 
    @task_center_menu_id,
    '',
    '任务中心',
    '/task-center',
    'layouts/RouteView',
    'task-center',
    '/task-center/one-time-task',
    0,
    NULL,
    '1',
    4.0,
    1,
    'box-plot',
    1,
    0,
    0,
    0,
    '任务中心模块',
    '1',
    0,
    0,
    'admin',
    NOW(),
    NULL,
    NULL,
    0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_permission` WHERE id = @task_center_menu_id
);

-- ============================================
-- 第二部分：创建一次性任务菜单
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
    @one_time_task_menu_id,
    @task_center_menu_id,
    '一次性任务',
    '/task-center/one-time-task',
    'system/OneTimeTaskList',
    'OneTimeTaskList',
    NULL,
    1,  -- 菜单类型：1-菜单
    'task:center:one:time:task',
    '1',
    2.0,  -- 排序号，放在工商任务之后
    0,
    'file-text',
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    '一次性任务审核，包含待审核、已审核、已驳回三个tab页',
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
    name = '一次性任务',
    url = '/task-center/one-time-task',
    component = 'system/OneTimeTaskList',
    component_name = 'OneTimeTaskList',
    perms = 'task:center:one:time:task',
    sort_no = 2.0,
    icon = 'file-text',
    description = '一次性任务审核，包含待审核、已审核、已驳回三个tab页',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：创建周期任务菜单
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
    @recurring_task_menu_id,
    @task_center_menu_id,
    '周期任务',
    '/task-center/recurring-task',
    'system/RecurringTaskList',
    'RecurringTaskList',
    NULL,
    1,  -- 菜单类型：1-菜单
    'task:center:recurring:task',
    '1',
    3.0,  -- 排序号，放在一次性任务之后
    0,
    'reload',
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    '周期任务审核，包含待审核、已审核、已驳回三个tab页',
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
    name = '周期任务',
    url = '/task-center/recurring-task',
    component = 'system/RecurringTaskList',
    component_name = 'RecurringTaskList',
    perms = 'task:center:recurring:task',
    sort_no = 3.0,
    icon = 'reload',
    description = '周期任务审核，包含待审核、已审核、已驳回三个tab页',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第四部分：为管理员角色分配菜单权限
-- ============================================

-- 一次性任务菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @one_time_task_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID，如果不同请修改
    @one_time_task_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @one_time_task_menu_id
);

-- 周期任务菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @recurring_task_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID，如果不同请修改
    @recurring_task_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @recurring_task_menu_id
);

-- ============================================
-- 验证脚本执行结果
-- ============================================

SELECT 
    id,
    name,
    url,
    component,
    parent_id,
    sort_no,
    status,
    del_flag
FROM `sys_permission`
WHERE id IN (@one_time_task_menu_id, @recurring_task_menu_id)
ORDER BY sort_no;

