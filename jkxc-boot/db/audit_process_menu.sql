-- 审核流程管理菜单配置SQL脚本
-- 执行前请备份数据库！

-- 菜单ID（使用32位字符串，类似UUID格式）
SET @menu_id = 'auditprocessmanagement20250101';

-- 1. 插入审核流程管理菜单（放在系统管理下）
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
    @menu_id,
    'd7d6e2e4e2934f2c9385a623fd98c6f3',  -- 系统管理的parent_id
    '审核流程管理',
    '/isystem/auditProcess',
    'system/AuditProcessList',
    'audit-process-list',
    NULL,
    1,  -- 菜单类型：1-菜单
    NULL,
    '1',
    7.0,  -- 排序号，放在系统通告之后
    0,
    'audit',
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    '配置审核流程，支持多步骤审核',
    '1',  -- 状态：1-启用
    0,  -- 删除标志：0-正常
    0,  -- 是否配置数据权限
    'admin',
    NOW(),
    NULL,
    NULL,
    0  -- 是否外链
);

-- 2. 为管理员角色分配菜单权限（可选，如果需要自动分配权限给管理员角色）
-- 注意：这里使用默认的管理员角色ID，如果您的系统中管理员角色ID不同，请修改
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID，如果不同请修改
    @menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @menu_id
);

-- 验证插入结果
SELECT 
    id,
    name,
    url,
    component,
    sort_no,
    status,
    del_flag
FROM `sys_permission`
WHERE id = @menu_id;

