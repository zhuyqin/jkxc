-- ============================================
-- 代账交接表结构SQL脚本
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_accounting_handover` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单编号（冗余字段）',
  `company_name` VARCHAR(200) DEFAULT NULL COMMENT '公司名称（冗余字段）',
  `handover_status` VARCHAR(50) DEFAULT 'pending' COMMENT '交接状态：pending-待审核,approved-审核通过,rejected-审核驳回,completed-已完成',
  `audit_process_id` VARCHAR(32) DEFAULT NULL COMMENT '审核流程ID（关联sys_audit_process表）',
  `current_audit_step` INT(11) DEFAULT 1 COMMENT '当前审核步骤顺序',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_handover_status` (`handover_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代账交接表';

-- 创建代账交接审核记录表（类似于订单审核记录表）
CREATE TABLE IF NOT EXISTS `gh_accounting_handover_audit` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `handover_id` VARCHAR(32) NOT NULL COMMENT '代账交接ID',
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
  KEY `idx_handover_id` (`handover_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_audit_process_id` (`audit_process_id`),
  KEY `idx_step_order` (`handover_id`, `step_order`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_handover_audit_handover` FOREIGN KEY (`handover_id`) REFERENCES `gh_accounting_handover` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代账交接审核记录表';

