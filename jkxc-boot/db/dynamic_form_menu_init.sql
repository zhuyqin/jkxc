-- 动态表单管理菜单初始化SQL脚本
-- 此脚本会自动执行，无需手动执行

SET @menu_id = 'dynamicform202501';
SET @parent_id = (SELECT id FROM sys_permission WHERE name = '系统管理' AND del_flag = 0 LIMIT 1);

-- 插入一级菜单（如果不存在）
INSERT INTO sys_permission (
  id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external
) VALUES (
  @menu_id,
  @parent_id,
  UNHEX('E58AA8E68081E8A1A8E58D95E7AEA1E79086'),
  '/system/dynamicform',
  'system/DynamicFormList',
  'DynamicFormList',
  NULL,
  1,
  NULL,
  NULL,
  10,
  0,
  'form',
  1,
  1,
  0,
  0,
  0,
  '动态表单管理',
  1,
  0,
  0,
  'admin',
  NOW(),
  NULL,
  NULL,
  0
) ON DUPLICATE KEY UPDATE
  name = UNHEX('E58AA8E68081E8A1A8E58D95E7AEA1E79086'),
  url = '/system/dynamicform',
  component = 'system/DynamicFormList',
  update_time = NOW();

-- 为管理员角色分配权限
INSERT INTO sys_role_permission (
  id, role_id, permission_id, data_rule_ids, operate_date, operate_ip
) VALUES (
  CONCAT(@menu_id, '_admin'),
  (SELECT id FROM sys_role WHERE role_code = 'admin' LIMIT 1),
  @menu_id,
  NULL,
  NOW(),
  '127.0.0.1'
) ON DUPLICATE KEY UPDATE
  operate_date = NOW();

