-- ============================================
-- 银行日记菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @finance_management_menu_id = 'financemanagement20250110';
SET @bank_diary_menu_id = 'bankdiary20250112';

-- ============================================
-- 创建财务管理-银行日记子菜单
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
    @bank_diary_menu_id,
    @finance_management_menu_id,
    UNHEX('E993B6E8A18CE697A5E8AEB0'),
    '/finance-management/bank-diary',
    'finance/BankDiaryList',
    'BankDiaryList',
    NULL,
    1,
    'finance:bank:diary',
    '1',
    2.0,
    0,
    'account-book',
    1,
    1,
    0,
    0,
    UNHEX('E993B6E8A18CE697A5E8AEB0E7AEA1E79086'),
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
    name = UNHEX('E993B6E8A18CE697A5E8AEB0'),
    url = '/finance-management/bank-diary',
    component = 'finance/BankDiaryList',
    component_name = 'BankDiaryList',
    perms = 'finance:bank:diary',
    sort_no = 2.0,
    icon = 'account-book',
    description = UNHEX('E993B6E8A18CE697A5E8AEB0E7AEA1E79086'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 为管理员角色分配菜单权限
-- ============================================

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @bank_diary_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @bank_diary_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @bank_diary_menu_id
);

