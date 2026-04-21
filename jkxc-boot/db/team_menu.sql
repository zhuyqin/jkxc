-- ============================================
-- 所属团队管理菜单SQL脚本
-- ============================================

-- 删除已存在的菜单（如果存在）
DELETE FROM `sys_permission` WHERE `id` = 'team_management_20250103';

-- 插入所属团队管理菜单
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
  `hide_tab`,
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
  'team_management_20250103',
  '2f467bcf-a42c-4b1c-8c62-1e16d1cd4d60',
  '所属团队管理',
  '/system/team',
  'system/TeamList',
  'TeamList',
  NULL,
  1,
  NULL,
  '1',
  10,
  0,
  'team',
  1,
  1,
  0,
  0,
  0,
  '所属团队管理',
  '1',
  0,
  0,
  'admin',
  NOW(),
  NULL,
  NULL,
  0
);

-- 为管理员角色分配菜单权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 'f6817f48-4dda-4d50-aa11-7c5b2246f3f0', 'team_management_20250103'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_permission` 
  WHERE `role_id` = 'f6817f48-4dda-4d50-aa11-7c5b2246f3f0' 
  AND `permission_id` = 'team_management_20250103'
);

