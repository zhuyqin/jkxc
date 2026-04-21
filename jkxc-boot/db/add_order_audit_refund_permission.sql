-- ============================================
-- 为订单管理添加退单审核权限按钮
-- 权限标识: order:auditRefund
-- 挂载在: 订单列表菜单（order_list）下
-- ============================================

-- 检查并添加退单审核权限按钮（如果不存在）
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
    'order_audit_refund',
    'order_list',  -- 父级菜单：订单列表（如果菜单ID不同，请修改此处）
    '退单审核',
    NULL,
    NULL,
    NULL,
    NULL,
    2,  -- 菜单类型：2-按钮
    'order:auditRefund',  -- 权限标识
    '1',
    5.00,
    0,
    NULL,
    1,
    1,
    0,
    0,
    '退单审核权限',
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
    `parent_id` = 'order_list',
    `name` = '退单审核',
    `perms` = 'order:auditRefund',
    `menu_type` = 2,
    `status` = '1',
    `del_flag` = 0,
    `update_time` = NOW();

