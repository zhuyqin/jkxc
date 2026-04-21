-- =============================================================================
-- 补丁：商机 / 地址 / 代账 导入导出按钮权限
-- 说明：
-- 1) 仅创建按钮权限(menu_type=2)并绑定到对应列表菜单下
-- 2) 同步给 admin 角色授权
-- 3) 若你环境的父菜单ID与下方不同，请先改变量再执行
-- =============================================================================

SET @admin_role_id = 'f6817f48af4fb3af11b9e8bf182f618b';

-- 父菜单ID（从数据库自动查询；避免不同环境ID不一致）
SET @opportunity_list_menu_id = (
  SELECT id FROM sys_permission
  WHERE del_flag=0 AND menu_type=1
    AND (url='/opportunity/list' OR component='opportunity/OpportunityList')
  ORDER BY update_time DESC, create_time DESC
  LIMIT 1
);

SET @address_list_menu_id = (
  SELECT id FROM sys_permission
  WHERE del_flag=0 AND menu_type=1
    AND (url='/business-management/address' OR component='address/GhAddressCenterList')
  ORDER BY update_time DESC, create_time DESC
  LIMIT 1
);

SET @accounting_list_menu_id = (
  SELECT id FROM sys_permission
  WHERE del_flag=0 AND menu_type=1
    AND (url='/accounting-management/contract' OR component='accounting/AccountingContractList')
  ORDER BY update_time DESC, create_time DESC
  LIMIT 1
);

-- 防呆：若没查到父菜单，直接报错（避免插到 parent_id 为空）
SELECT
  IF(@opportunity_list_menu_id IS NULL, 'ERR: 未找到 线索管理(opportunity_list) 父菜单', 'OK') AS opportunity_parent,
  IF(@address_list_menu_id IS NULL, 'ERR: 未找到 地址管理(address) 父菜单', 'OK') AS address_parent,
  IF(@accounting_list_menu_id IS NULL, 'ERR: 未找到 代账管理(accounting contract) 父菜单', 'OK') AS accounting_parent;

-- ----------------------------
-- 商机：导出 / 导入
-- ----------------------------
INSERT INTO `sys_permission` (
  `id`,`parent_id`,`name`,`url`,`component`,`component_name`,`redirect`,
  `menu_type`,`perms`,`perms_type`,`sort_no`,`always_show`,`icon`,
  `is_route`,`is_leaf`,`keep_alive`,`hidden`,`description`,
  `status`,`del_flag`,`rule_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`internal_or_external`
) VALUES
  ('opp_export_20260409', @opportunity_list_menu_id, '导出', NULL, NULL, NULL, NULL, 2, 'opportunity:export', '1', 90, 0, NULL, 0, 1, 0, 0, '线索管理导出权限', '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
  ('opp_import_20260409', @opportunity_list_menu_id, '导入', NULL, NULL, NULL, NULL, 2, 'opportunity:import', '1', 91, 0, NULL, 0, 1, 0, 0, '线索管理导入权限', '1', 0, 0, 'admin', NOW(), NULL, NULL, 0)
ON DUPLICATE KEY UPDATE
  `parent_id`=VALUES(`parent_id`),
  `name`=VALUES(`name`),
  `menu_type`=2,
  `perms`=VALUES(`perms`),
  `sort_no`=VALUES(`sort_no`),
  `description`=VALUES(`description`),
  `status`='1',
  `del_flag`=0,
  `update_time`=NOW();

-- ----------------------------
-- 地址：导出 / 导入
-- ----------------------------
INSERT INTO `sys_permission` (
  `id`,`parent_id`,`name`,`url`,`component`,`component_name`,`redirect`,
  `menu_type`,`perms`,`perms_type`,`sort_no`,`always_show`,`icon`,
  `is_route`,`is_leaf`,`keep_alive`,`hidden`,`description`,
  `status`,`del_flag`,`rule_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`internal_or_external`
) VALUES
  ('addr_export_20260409', @address_list_menu_id, '导出', NULL, NULL, NULL, NULL, 2, 'address:export', '1', 90, 0, NULL, 0, 1, 0, 0, '地址管理导出权限', '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
  ('addr_import_20260409', @address_list_menu_id, '导入', NULL, NULL, NULL, NULL, 2, 'address:import', '1', 91, 0, NULL, 0, 1, 0, 0, '地址管理导入权限', '1', 0, 0, 'admin', NOW(), NULL, NULL, 0)
ON DUPLICATE KEY UPDATE
  `parent_id`=VALUES(`parent_id`),
  `name`=VALUES(`name`),
  `menu_type`=2,
  `perms`=VALUES(`perms`),
  `sort_no`=VALUES(`sort_no`),
  `description`=VALUES(`description`),
  `status`='1',
  `del_flag`=0,
  `update_time`=NOW();

-- ----------------------------
-- 代账：导出 / 导入
-- ----------------------------
INSERT INTO `sys_permission` (
  `id`,`parent_id`,`name`,`url`,`component`,`component_name`,`redirect`,
  `menu_type`,`perms`,`perms_type`,`sort_no`,`always_show`,`icon`,
  `is_route`,`is_leaf`,`keep_alive`,`hidden`,`description`,
  `status`,`del_flag`,`rule_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`internal_or_external`
) VALUES
  ('acct_export_20260409', @accounting_list_menu_id, '导出', NULL, NULL, NULL, NULL, 2, 'accountingContract:export', '1', 90, 0, NULL, 0, 1, 0, 0, '代账管理导出权限', '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
  ('acct_import_20260409', @accounting_list_menu_id, '导入', NULL, NULL, NULL, NULL, 2, 'accountingContract:import', '1', 91, 0, NULL, 0, 1, 0, 0, '代账管理导入权限', '1', 0, 0, 'admin', NOW(), NULL, NULL, 0)
ON DUPLICATE KEY UPDATE
  `parent_id`=VALUES(`parent_id`),
  `name`=VALUES(`name`),
  `menu_type`=2,
  `perms`=VALUES(`perms`),
  `sort_no`=VALUES(`sort_no`),
  `description`=VALUES(`description`),
  `status`='1',
  `del_flag`=0,
  `update_time`=NOW();

-- admin 角色授权（幂等）
INSERT INTO `sys_role_permission` (`id`,`role_id`,`permission_id`,`data_rule_ids`,`operate_date`,`operate_ip`)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, p.id, NULL, NOW(), NULL
FROM `sys_permission` p
WHERE p.id IN (
  'opp_export_20260409','opp_import_20260409',
  'addr_export_20260409','addr_import_20260409',
  'acct_export_20260409','acct_import_20260409'
)
AND NOT EXISTS (
  SELECT 1
  FROM `sys_role_permission` rp
  WHERE rp.role_id = @admin_role_id
    AND rp.permission_id = p.id
);

