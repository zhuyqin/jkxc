-- ============================================
-- 为线索管理添加删除权限按钮
-- 权限标识: opportunity:delete
-- 挂载在: 线索管理菜单（opportunity_list）下
-- ============================================

-- 检查并添加删除权限按钮（如果不存在）
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
    'opp_delete',
    'opportunity_list',  -- 父级菜单：线索管理
    '线索删除',
    NULL,
    NULL,
    NULL,
    NULL,
    2,  -- 菜单类型：2-按钮
    'opportunity:delete',  -- 权限标识
    '1',
    3.00,
    0,
    NULL,
    1,
    1,
    0,
    0,
    '线索删除权限',
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
    `parent_id` = 'opportunity_list',
    `name` = '线索删除',
    `perms` = 'opportunity:delete',
    `menu_type` = 2,
    `status` = '1',
    `del_flag` = 0,
    `update_time` = NOW();

-- 检查并添加批量删除权限按钮（如果不存在）
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
    'opp_deleteBatch',
    'opportunity_list',  -- 父级菜单：线索管理
    '线索批量删除',
    NULL,
    NULL,
    NULL,
    NULL,
    2,  -- 菜单类型：2-按钮
    'opportunity:deleteBatch',  -- 权限标识
    '1',
    4.00,
    0,
    NULL,
    1,
    1,
    0,
    0,
    '线索批量删除权限',
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
    `parent_id` = 'opportunity_list',
    `name` = '线索批量删除',
    `perms` = 'opportunity:deleteBatch',
    `menu_type` = 2,
    `status` = '1',
    `del_flag` = 0,
    `update_time` = NOW();

