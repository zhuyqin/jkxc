-- ============================================
-- 通讯名录菜单配置SQL脚本（更新版）
-- 1. 删除旧的通讯录菜单
-- 2. 创建新的通讯名录菜单
-- 3. 为管理员角色分配权限
-- ============================================

-- 1. 删除旧的通讯录菜单及其权限关联
DELETE FROM sys_role_permission WHERE permission_id = '1174590283938041857';
DELETE FROM sys_permission WHERE id = '1174590283938041857';

-- 2. 创建新的通讯名录菜单（放在系统管理下）
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
    'contactlist20250107',
    'd7d6e2e4e2934f2c9385a623fd98c6f3',  -- 系统管理的parent_id
    UNHEX('E9809AE8AEAFE5908DE5BD95'),  -- 通讯名录（UTF-8编码）
    '/isystem/contactList',
    'system/ContactList',
    'contact-list',
    NULL,
    1,  -- 菜单类型：1-菜单
    NULL,
    '1',
    9.0,  -- 排序号，放在员工管理之后
    0,
    'contacts',  -- 图标
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    UNHEX('E59198E5B7A5E9809AE8AEAFE5908DE5BD95EFBC8CE69FA5E79C8BE59198E5B7A5E88194E7B3BBE696B9E5BC8F'),  -- 员工通讯名录，查看员工联系方式
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
    name = UNHEX('E9809AE8AEAFE5908DE5BD95'),
    url = '/isystem/contactList',
    component = 'system/ContactList',
    component_name = 'contact-list',
    sort_no = 9.0,
    icon = 'contacts',
    description = UNHEX('E59198E5B7A5E9809AE8AEAFE5908DE5BD95EFBC8CE69FA5E79C8BE59198E5B7A5E88194E7B3BBE696B9E5BC8F'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- 3. 为管理员角色分配菜单权限
-- 注意：使用默认的管理员角色ID，如果您的系统中管理员角色ID不同，请修改
INSERT INTO `sys_role_permission` (
    `id`,
    `role_id`,
    `permission_id`,
    `data_rule_ids`,
    `operate_date`,
    `operate_ip`
) VALUES (
    CONCAT('rp_contactlist20250107_', 'f6817f48af4fb3af11b9e8bf182f618b'),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID
    'contactlist20250107',
    NULL,
    NOW(),
    NULL
)
ON DUPLICATE KEY UPDATE
    operate_date = NOW();

-- 说明：
-- - 已删除旧的通讯录菜单（ID: 1174590283938041857）
-- - 已创建新的通讯名录菜单（ID: contactlist20250107）
-- - 菜单路径：/isystem/contactList
-- - 组件：system/ContactList
-- - 显示字段：员工姓名、工作号码、私人号码、紧急联系、紧急电话、所属团队、当前状态

