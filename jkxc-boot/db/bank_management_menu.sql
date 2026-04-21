-- 收款账号管理菜单初始化脚本
-- 执行前请备份数据库！

-- 菜单ID（使用32位字符串）
SET @bank_menu_id = 'bankmanagement20250101';

-- 1. 插入收款账号管理菜单（放在系统管理下）
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
    @bank_menu_id,
    'd7d6e2e4e2934f2c9385a623fd98c6f3',  -- 系统管理的parent_id
    UNHEX('E694B6E6ACBEE8B4A6E58FB7E7AEA1E79086'),
    '/isystem/bank',
    'bank/BankManagementList',
    'bank-management-list',
    NULL,
    1,  -- 菜单类型：1-菜单
    NULL,
    '1',
    8.0,  -- 排序号，放在审核流程管理之后
    0,
    'account',
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    UNHEX('E694B6E6ACBEE8B4A6E58FB7E7AEA1E79086E6A8A1E59D97'),
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
    name = UNHEX('E694B6E6ACBEE8B4A6E58FB7E7AEA1E79086'),
    url = '/isystem/bank',
    component = 'bank/BankManagementList',
    component_name = 'bank-management-list',
    sort_no = 8.0,
    icon = 'account',
    description = UNHEX('E694B6E6ACBEE8B4A6E58FB7E7AEA1E79086E6A8A1E59D97'),
    status = '1',
    del_flag = 0;

-- 2. 为管理员角色分配菜单权限（如果需要自动分配权限给管理员角色）
-- 注意：这里使用默认的管理员角色ID，如果您的系统中管理员角色ID不同，请修改
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @bank_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID，如果不同请修改
    @bank_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @bank_menu_id
);

