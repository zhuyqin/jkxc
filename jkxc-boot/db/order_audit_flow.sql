-- ============================================
-- 订单审核流程集成SQL脚本
-- 用于订单审核流程与工商任务流程的集成
-- ============================================

-- 创建订单审核记录表（用于记录审核流程的每一步审核）
CREATE TABLE IF NOT EXISTS `gh_order_audit` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `audit_process_id` VARCHAR(32) DEFAULT NULL COMMENT '审核流程ID（关联sys_audit_process表）',
  `step_order` INT(11) NOT NULL COMMENT '审核步骤顺序（从1开始）',
  `role_id` VARCHAR(32) NOT NULL COMMENT '审核角色ID（关联sys_role表）',
  `role_name` VARCHAR(100) DEFAULT NULL COMMENT '审核角色名称',
  `audit_status` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核,approved-已通过,rejected-已驳回',
  `audit_user_id` VARCHAR(32) DEFAULT NULL COMMENT '审核人ID',
  `audit_user_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_audit_process_id` (`audit_process_id`),
  KEY `idx_step_order` (`order_id`, `step_order`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_order_audit_order` FOREIGN KEY (`order_id`) REFERENCES `gh_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单审核记录表';

-- 为订单表添加审核流程相关字段（如果不存在）
ALTER TABLE `gh_order` 
ADD COLUMN IF NOT EXISTS `audit_process_id` VARCHAR(32) DEFAULT NULL COMMENT '审核流程ID（关联sys_audit_process表）' AFTER `auditProcessId`,
ADD COLUMN IF NOT EXISTS `current_audit_step` INT(11) DEFAULT 1 COMMENT '当前审核步骤顺序' AFTER `audit_process_id`,
ADD COLUMN IF NOT EXISTS `audit_status` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核,approved-已通过,rejected-已驳回' AFTER `current_audit_step`;

-- 创建索引
CREATE INDEX IF NOT EXISTS `idx_audit_process_id` ON `gh_order` (`audit_process_id`);
CREATE INDEX IF NOT EXISTS `idx_audit_status` ON `gh_order` (`audit_status`);

