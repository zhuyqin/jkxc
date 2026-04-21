-- ============================================
-- 创建缺失的审批流程相关表
-- 如果表已存在，此脚本会报错，可以忽略
-- ============================================

-- 1. 审批步骤表单配置表（每个步骤可以配置独立的审批表单）
CREATE TABLE IF NOT EXISTS `sys_audit_step_form` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `step_id` VARCHAR(32) NOT NULL COMMENT '步骤ID（关联sys_audit_step表）',
  `form_name` VARCHAR(100) DEFAULT NULL COMMENT '表单名称',
  `form_config` TEXT COMMENT '表单配置JSON（包含普通表单组件和特殊指标）',
  `remark_required` TINYINT(1) DEFAULT 1 COMMENT '是否必须填写备注：0-否，1-是',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_step_id` (`step_id`),
  KEY `idx_step_id` (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审批步骤表单配置表';

-- 注意：如果 sys_audit_step 表存在，可以添加外键约束
-- ALTER TABLE `sys_audit_step_form` 
-- ADD CONSTRAINT `fk_audit_step_form_step` FOREIGN KEY (`step_id`) REFERENCES `sys_audit_step` (`id`) ON DELETE CASCADE;

-- 2. 审批表单特殊指标配置表（下个流程审批人员选择、成本填写等）
CREATE TABLE IF NOT EXISTS `sys_audit_form_indicator` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `step_form_id` VARCHAR(32) NOT NULL COMMENT '步骤表单ID（关联sys_audit_step_form表）',
  `indicator_type` VARCHAR(50) NOT NULL COMMENT '指标类型：next_auditor-下个流程审批人员选择，cost_input-成本填写',
  `indicator_name` VARCHAR(100) DEFAULT NULL COMMENT '指标名称（显示名称）',
  `indicator_config` TEXT COMMENT '指标配置JSON（根据不同类型存储不同配置）',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序顺序',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_step_form_id` (`step_form_id`),
  KEY `idx_indicator_type` (`indicator_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审批表单特殊指标配置表';

-- 注意：如果 sys_audit_step_form 表存在，可以添加外键约束
-- ALTER TABLE `sys_audit_form_indicator` 
-- ADD CONSTRAINT `fk_audit_form_indicator_step_form` FOREIGN KEY (`step_form_id`) REFERENCES `sys_audit_step_form` (`id`) ON DELETE CASCADE;

