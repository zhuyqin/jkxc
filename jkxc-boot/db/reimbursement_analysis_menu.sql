-- ============================================
-- 报销管理 - 数据分析 子菜单（需在 reimbursement_menu.sql 已执行后运行）
-- ============================================

SET @reimbursement_management_menu_id = 'reimbursementmanagement20250112';
SET @reimbursement_analysis_menu_id = 'reimbursementanalysis20250406';

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
    @reimbursement_analysis_menu_id,
    @reimbursement_management_menu_id,
    UNHEX('E695B0E68DAEE58886E69E90'),
    '/reimbursement-management/analysis',
    'reimbursement/ReimbursementAnalysis',
    'ReimbursementAnalysis',
    NULL,
    1,
    'reimbursement:list',
    '1',
    2.0,
    0,
    'bar-chart',
    1,
    1,
    0,
    0,
    UNHEX('E68AA5E99480E695B0E68DAEE58886E69E90'),
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
    name = UNHEX('E695B0E68DAEE58886E69E90'),
    url = '/reimbursement-management/analysis',
    component = 'reimbursement/ReimbursementAnalysis',
    component_name = 'ReimbursementAnalysis',
    perms = 'reimbursement:list',
    sort_no = 2.0,
    icon = 'bar-chart',
    description = UNHEX('E68AA5E99480E695B0E68DAEE58886E69E90'),
    status = '1',
    del_flag = 0,
    update_time = NOW();

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT
    'rp_reimb_analysis20250406',
    'f6817f48af4fb3af11b9e8bf182f618b',
    @reimbursement_analysis_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission`
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b'
    AND `permission_id` = @reimbursement_analysis_menu_id
);
