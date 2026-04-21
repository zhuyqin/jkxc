-- ============================================
-- 添加退单订单列表菜单
-- 菜单标识: refund_order_list
-- 挂载在: 订单管理菜单（order_list）下，或作为独立菜单
-- ============================================

-- 检查并添加退单订单列表菜单（如果不存在）
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
    'refund_order_list',
    'order_list',  -- 父级菜单：订单列表（如果菜单ID不同，请修改此处）
    '退单订单列表',
    '/order/refundOrderList',
    'order/RefundOrderList',
    'RefundOrderList',
    NULL,
    1,  -- 菜单类型：1-菜单
    NULL,  -- 权限标识（菜单本身不需要权限标识）
    '1',
    6.00,  -- 排序号
    0,
    'rollback',  -- 图标
    1,
    1,
    0,
    0,
    '退单订单列表',
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
    `name` = '退单订单列表',
    `url` = '/order/refundOrderList',
    `component` = 'order/RefundOrderList',
    `component_name` = 'RefundOrderList',
    `menu_type` = 1,
    `sort_no` = 6.00,
    `icon` = 'rollback',
    `status` = '1',
    `del_flag` = 0,
    `update_time` = NOW();

