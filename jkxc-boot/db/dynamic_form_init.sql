-- 可配置表单系统初始化SQL脚本
-- 此脚本会自动执行，无需手动执行

-- ============================================
-- 第一部分：创建表单主表
-- ============================================

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

-- ============================================
-- 第二部分：创建表单版本表
-- ============================================

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

-- ============================================
-- 第三部分：创建业务类型绑定表
-- ============================================

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

-- ============================================
-- 第四部分：创建订单表单数据表
-- ============================================

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

