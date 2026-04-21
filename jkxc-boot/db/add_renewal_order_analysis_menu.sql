-- ============================================
-- 运营分析-续费订单分析菜单配置SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 菜单ID定义
SET @operation_analysis_menu_id = 'operationanalysis20250128';
SET @renewal_order_analysis_menu_id = 'renewalorderanalysis20250128';

-- ============================================
-- 第一部分：确保运营分析一级菜单存在
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
    @operation_analysis_menu_id,
    '',
    '运营分析',
    '/operation-analysis',
    'layouts/RouteView',
    'operation-analysis',
    '/operation-analysis/new-order-analysis',
    0,
    NULL,
    '1',
    5.0,
    1,
    'bar-chart',
    1,
    0,
    0,
    0,
    '运营分析模块',
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
    name = '运营分析',
    url = '/operation-analysis',
    component = 'layouts/RouteView',
    component_name = 'operation-analysis',
    redirect = '/operation-analysis/new-order-analysis',
    sort_no = 5.0,
    icon = 'bar-chart',
    description = '运营分析模块',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第二部分：创建续费订单分析子菜单
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
    @renewal_order_analysis_menu_id,
    @operation_analysis_menu_id,
    '续费订单分析',
    '/operation-analysis/renewal-order-analysis',
    'operation/RenewalOrderAnalysis',
    'RenewalOrderAnalysis',
    NULL,
    1,
    'operation:analysis:renewal:order',
    '1',
    2.0,
    0,
    'line-chart',
    1,
    1,
    0,
    0,
    '续费订单统计分析，包含代账续费和地址续费两个标签页，使用ECharts图表展示',
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
    name = '续费订单分析',
    url = '/operation-analysis/renewal-order-analysis',
    component = 'operation/RenewalOrderAnalysis',
    component_name = 'RenewalOrderAnalysis',
    perms = 'operation:analysis:renewal:order',
    sort_no = 2.0,
    icon = 'line-chart',
    description = '续费订单统计分析，包含代账续费和地址续费两个标签页，使用ECharts图表展示',
    status = '1',
    del_flag = 0,
    update_time = NOW();

-- ============================================
-- 第三部分：为管理员角色分配菜单权限
-- ============================================

-- 续费订单分析菜单权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    CONCAT('rp_', @renewal_order_analysis_menu_id, '_admin'),
    'f6817f48af4fb3af11b9e8bf182f618b',
    @renewal_order_analysis_menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = @renewal_order_analysis_menu_id
);

-- ============================================
-- 第四部分：确保payment_time字段存在（如果不存在则添加）
-- ============================================

-- 检查字段是否存在，如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'gh_address_renew';
SET @columnname = 'payment_time';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1', -- 字段已存在，不执行任何操作
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` datetime DEFAULT NULL COMMENT ''收款时间'' AFTER `amount_received`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 完成
-- ============================================
SELECT '运营分析-续费订单分析菜单配置完成！' AS result;

