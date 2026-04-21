-- 审核流程管理表结构
-- 执行前请备份数据库！

-- 1. 创建审核流程表
CREATE TABLE IF NOT EXISTS `sys_audit_process` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `process_name` VARCHAR(100) NOT NULL COMMENT '流程名称（如：订单审批、会计合同审批）',
  `process_code` VARCHAR(50) NOT NULL COMMENT '流程编码（唯一标识）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核流程表';

-- 2. 创建审核步骤表
CREATE TABLE IF NOT EXISTS `sys_audit_step` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `process_id` VARCHAR(32) NOT NULL COMMENT '流程ID',
  `step_order` INT(11) NOT NULL COMMENT '步骤顺序（从1开始）',
  `role_id` VARCHAR(32) NOT NULL COMMENT '审核角色ID（关联sys_role表）',
  `role_name` VARCHAR(100) DEFAULT NULL COMMENT '审核角色名称（冗余字段，方便查询）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '步骤描述',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_id` (`process_id`),
  KEY `idx_step_order` (`process_id`, `step_order`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_audit_step_process` FOREIGN KEY (`process_id`) REFERENCES `sys_audit_process` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核步骤表';

