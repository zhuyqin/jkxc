-- 可配置表单系统完整初始化SQL脚本
-- 包含数据库表创建和菜单配置
-- 执行此脚本即可完成所有初始化

-- ============================================
-- 第一部分：创建数据库表
-- ============================================

-- 1. 创建表单主表
CREATE TABLE IF NOT EXISTS `sys_dynamic_form` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `form_name` VARCHAR(100) NOT NULL COMMENT '表单名称',
  `form_code` VARCHAR(50) NOT NULL COMMENT '表单编码（唯一标识）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '表单描述',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `current_version` INT(11) DEFAULT 1 COMMENT '当前版本号',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_code` (`form_code`),
  KEY `idx_status` (`status`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表单主表';

-- 2. 创建表单版本表
CREATE TABLE IF NOT EXISTS `sys_dynamic_form_version` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `form_id` VARCHAR(32) NOT NULL COMMENT '表单ID',
  `version` INT(11) NOT NULL COMMENT '版本号',
  `form_config` TEXT COMMENT '表单配置JSON（字段定义、布局等）',
  `version_desc` VARCHAR(500) DEFAULT NULL COMMENT '版本描述',
  `is_current` TINYINT(1) DEFAULT 0 COMMENT '是否当前版本：0-否，1-是',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_version` (`form_id`, `version`),
  KEY `idx_form_id` (`form_id`),
  KEY `idx_is_current` (`is_current`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表单版本表';

-- 3. 创建业务类型绑定表
CREATE TABLE IF NOT EXISTS `sys_dynamic_form_binding` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `business_type` VARCHAR(100) NOT NULL COMMENT '业务类型（sys_category的code或id）',
  `form_id` VARCHAR(32) NOT NULL COMMENT '表单ID',
  `version_id` VARCHAR(32) NOT NULL COMMENT '版本ID（绑定时的版本）',
  `version` INT(11) NOT NULL COMMENT '版本号（冗余字段，方便查询）',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_type` (`business_type`),
  KEY `idx_form_id` (`form_id`),
  KEY `idx_version_id` (`version_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务类型表单绑定表';

-- 4. 创建订单表单数据表
CREATE TABLE IF NOT EXISTS `gh_order_form_data` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `form_id` VARCHAR(32) NOT NULL COMMENT '表单ID',
  `version_id` VARCHAR(32) NOT NULL COMMENT '版本ID（订单创建时的版本）',
  `version` INT(11) NOT NULL COMMENT '版本号（冗余字段）',
  `form_data` TEXT COMMENT '表单数据JSON（用户填写的值）',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_form_id` (`form_id`),
  KEY `idx_version_id` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表单数据表';

-- ============================================
-- 第二部分：创建菜单配置
-- ============================================

SET @menu_id = 'dynamicform202501';
SET @parent_id = (SELECT id FROM sys_permission WHERE name = '系统管理' AND del_flag = 0 LIMIT 1);

-- 插入菜单（如果不存在）
INSERT INTO sys_permission (
  id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external
) VALUES (
  @menu_id,
  @parent_id,
  '动态表单管理',
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
  name = '动态表单管理',
  url = '/system/dynamicform',
  component = 'system/DynamicFormList',
  update_time = NOW();

-- 为管理员角色分配权限
INSERT INTO sys_role_permission (
  id, role_id, permission_id, data_rule_ids, operate_date, operate_ip
) 
SELECT 
  CONCAT('r_p_', @menu_id, '_', r.id) as id,
  r.id as role_id,
  @menu_id as permission_id,
  NULL as data_rule_ids,
  NOW() as operate_date,
  '127.0.0.1' as operate_ip
FROM sys_role r
WHERE r.role_code = 'admin'
ON DUPLICATE KEY UPDATE
  operate_date = NOW();

