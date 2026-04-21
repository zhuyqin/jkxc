-- ============================================
-- 为报销管理添加审核权限按钮
-- 权限标识: reimbursement:audit
-- 挂载在: 报销列表菜单（reimbursementlist20250112）下
-- ============================================

-- 检查并添加报销审核权限按钮（如果不存在）
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
    'reimbursement_audit',
    'reimbursementlist20250112',  -- 父级菜单：报销列表
    '报销审核',
    NULL,
    NULL,
    NULL,
    NULL,
    2,  -- 菜单类型：2-按钮
    'reimbursement:audit',  -- 权限标识
    '1',
    2.00,
    0,
    NULL,
    1,
    1,
    0,
    0,
    '报销审核权限',
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
    `parent_id` = 'reimbursementlist20250112',
    `name` = '报销审核',
    `perms` = 'reimbursement:audit',
    `menu_type` = 2,
    `status` = '1',
    `del_flag` = 0,
    `update_time` = NOW();

