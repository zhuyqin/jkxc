-- ============================================
-- 可配置化审批流程系统 - 数据库表结构
-- 执行前请备份数据库！
-- ============================================

-- 1. 流程审批业务类型绑定表（支持一个流程绑定多个业务类型）
CREATE TABLE IF NOT EXISTS `sys_audit_process_binding` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `process_id` VARCHAR(32) NOT NULL COMMENT '流程ID（关联sys_audit_process表）',
  `business_type_id` VARCHAR(32) NOT NULL COMMENT '业务类型ID（关联sys_category表，必须是最底层节点）',
  `business_type_code` VARCHAR(50) DEFAULT NULL COMMENT '业务类型编码（冗余字段，方便查询）',
  `business_type_name` VARCHAR(100) DEFAULT NULL COMMENT '业务类型名称（冗余字段，方便查询）',
  `task_type` VARCHAR(20) NOT NULL DEFAULT 'once' COMMENT '任务类型：once-一次性任务，recurring-周期任务',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_business_type` (`process_id`, `business_type_id`),
  KEY `idx_process_id` (`process_id`),
  KEY `idx_business_type_id` (`business_type_id`),
  KEY `idx_task_type` (`task_type`),
  CONSTRAINT `fk_audit_process_binding_process` FOREIGN KEY (`process_id`) REFERENCES `sys_audit_process` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程审批业务类型绑定表';

-- 2. 审批步骤表单配置表（每个步骤可以配置独立的审批表单）
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
  CONSTRAINT `fk_audit_step_form_step` FOREIGN KEY (`step_id`) REFERENCES `sys_audit_step` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审批步骤表单配置表';

-- 3. 审批表单特殊指标配置表（下个流程审批人员选择、成本填写等）
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
  KEY `idx_indicator_type` (`indicator_type`),
  CONSTRAINT `fk_audit_form_indicator_step_form` FOREIGN KEY (`step_form_id`) REFERENCES `sys_audit_step_form` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审批表单特殊指标配置表';

-- 指标配置JSON说明：
-- 1. next_auditor（下个流程审批人员选择）：
--    {
--      "roleId": "角色ID",
--      "roleName": "角色名称",
--      "nextStepOrder": 2,  // 下个流程环节的stepOrder
--      "nextStepId": "步骤ID"  // 下个流程环节的步骤ID（可选）
--    }
-- 2. cost_input（成本填写）：
--    {
--      "expenseName": "报销名称",
--      "categoryIds": ["类目ID1", "类目ID2"],  // 关联sys_category表
--      "categoryNames": ["类目名称1", "类目名称2"],
--      "items": [  // 多个报销项
--        {
--          "name": "报销项名称",
--          "categoryId": "类目ID",
--          "categoryName": "类目名称"
--        }
--      ]
--    }

-- 4. 任务表（一次性任务和周期任务）
CREATE TABLE IF NOT EXISTS `sys_audit_task` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID（关联gh_order表）',
  `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单编号（冗余字段）',
  `process_id` VARCHAR(32) NOT NULL COMMENT '流程ID（关联sys_audit_process表）',
  `step_id` VARCHAR(32) NOT NULL COMMENT '当前步骤ID（关联sys_audit_step表）',
  `step_order` INT(11) NOT NULL COMMENT '当前步骤顺序',
  `task_type` VARCHAR(20) NOT NULL COMMENT '任务类型：once-一次性任务，recurring-周期任务',
  `task_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态：pending-待审核，approved-已审核，rejected-已驳回',
  `assigned_user_id` VARCHAR(32) DEFAULT NULL COMMENT '指定审核人ID（如果配置了下个流程审批人员选择）',
  `assigned_user_name` VARCHAR(100) DEFAULT NULL COMMENT '指定审核人姓名（冗余字段）',
  `current_role_id` VARCHAR(32) NOT NULL COMMENT '当前审核角色ID',
  `current_role_name` VARCHAR(100) DEFAULT NULL COMMENT '当前审核角色名称（冗余字段）',
  `audit_data` TEXT COMMENT '审核数据JSON（包含审核表单填写的数据）',
  `audit_result` VARCHAR(20) DEFAULT NULL COMMENT '审核结果：approved-通过，rejected-不通过，returned-驳回',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因（驳回时填写）',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_user_id` VARCHAR(32) DEFAULT NULL COMMENT '审核人ID',
  `audit_user_name` VARCHAR(100) DEFAULT NULL COMMENT '审核人姓名',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_process_id` (`process_id`),
  KEY `idx_step_id` (`step_id`),
  KEY `idx_task_type` (`task_type`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_assigned_user_id` (`assigned_user_id`),
  KEY `idx_current_role_id` (`current_role_id`),
  KEY `idx_audit_user_id` (`audit_user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审批任务表';

-- 5. 审批任务成本记录表（存储成本填写指标的数据）
CREATE TABLE IF NOT EXISTS `sys_audit_task_cost` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `task_id` VARCHAR(32) NOT NULL COMMENT '任务ID（关联sys_audit_task表）',
  `indicator_id` VARCHAR(32) DEFAULT NULL COMMENT '指标ID（关联sys_audit_form_indicator表）',
  `expense_name` VARCHAR(100) DEFAULT NULL COMMENT '报销名称',
  `category_id` VARCHAR(32) DEFAULT NULL COMMENT '报销类目ID（关联sys_category表）',
  `category_name` VARCHAR(100) DEFAULT NULL COMMENT '报销类目名称（冗余字段）',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序顺序',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_indicator_id` (`indicator_id`),
  KEY `idx_category_id` (`category_id`),
  CONSTRAINT `fk_audit_task_cost_task` FOREIGN KEY (`task_id`) REFERENCES `sys_audit_task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审批任务成本记录表';

-- 6. 扩展订单审核记录表，增加驳回重新提交相关字段
ALTER TABLE `gh_order_audit` 
ADD COLUMN IF NOT EXISTS `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因' AFTER `audit_remark`,
ADD COLUMN IF NOT EXISTS `is_resubmit` TINYINT(1) DEFAULT 0 COMMENT '是否重新提交：0-否，1-是' AFTER `reject_reason`,
ADD COLUMN IF NOT EXISTS `resubmit_time` DATETIME DEFAULT NULL COMMENT '重新提交时间' AFTER `is_resubmit`,
ADD COLUMN IF NOT EXISTS `parent_audit_id` VARCHAR(32) DEFAULT NULL COMMENT '父审核记录ID（驳回后重新提交时关联原审核记录）' AFTER `resubmit_time`;

-- 7. 扩展订单表，移除不需要的字段（保留字段但标记为废弃，实际删除需要数据迁移）
-- 注意：这里只是添加注释说明，实际删除字段需要谨慎处理
ALTER TABLE `gh_order` 
MODIFY COLUMN `audit_process_id` VARCHAR(32) DEFAULT NULL COMMENT '审核流程ID（已废弃，由业务类型自动匹配）',
MODIFY COLUMN `delivery_method` VARCHAR(20) DEFAULT NULL COMMENT '交单方式（已废弃，由流程配置决定）',
MODIFY COLUMN `is_recurring` VARCHAR(10) DEFAULT '0' COMMENT '周期业务（已废弃，由流程配置决定）';

-- 8. 添加订单业务类型自动匹配流程的字段
ALTER TABLE `gh_order` 
ADD COLUMN IF NOT EXISTS `matched_process_id` VARCHAR(32) DEFAULT NULL COMMENT '自动匹配的流程ID（根据业务类型自动匹配）' AFTER `audit_process_id`,
ADD COLUMN IF NOT EXISTS `task_type` VARCHAR(20) DEFAULT NULL COMMENT '任务类型（从流程配置中获取：once-一次性任务，recurring-周期任务）' AFTER `matched_process_id`;

-- 创建索引
CREATE INDEX IF NOT EXISTS `idx_matched_process_id` ON `gh_order` (`matched_process_id`);
CREATE INDEX IF NOT EXISTS `idx_task_type` ON `gh_order` (`task_type`);

