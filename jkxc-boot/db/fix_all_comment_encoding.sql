-- ============================================
-- 修复所有表注释和字段注释乱码问题
-- 统一字符集为 utf8mb4，排序规则为 utf8mb4_general_ci
-- 执行前请备份数据库！
-- ============================================

-- 设置连接字符集
SET NAMES utf8mb4;

-- 1. 修复 gh_clue 表注释和字段注释
ALTER TABLE `gh_clue` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '线索管理表';

ALTER TABLE `gh_clue` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `gh_clue` MODIFY COLUMN `clue_no` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '线索编号';
ALTER TABLE `gh_clue` MODIFY COLUMN `business_person` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务人员';
ALTER TABLE `gh_clue` MODIFY COLUMN `customer_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户名称';
ALTER TABLE `gh_clue` MODIFY COLUMN `region` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属区域';
ALTER TABLE `gh_clue` MODIFY COLUMN `customer_source` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户来源';
ALTER TABLE `gh_clue` MODIFY COLUMN `current_status` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '当前状态（意向a+、意向a、意向b、意向c、无效客户）';
ALTER TABLE `gh_clue` MODIFY COLUMN `follow_record` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '跟进记录';
ALTER TABLE `gh_clue` MODIFY COLUMN `assigned_user_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分配给的用户ID（个人线索）';
ALTER TABLE `gh_clue` MODIFY COLUMN `assigned_user_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分配给的用户名称';
ALTER TABLE `gh_clue` MODIFY COLUMN `is_public` TINYINT(1) DEFAULT 0 COMMENT '是否公海线索（0-否，1-是）';
ALTER TABLE `gh_clue` MODIFY COLUMN `follow_time` DATETIME DEFAULT NULL COMMENT '最后跟进时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `next_follow_time` DATETIME DEFAULT NULL COMMENT '下次跟进时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `remarks` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注';
ALTER TABLE `gh_clue` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `gh_clue` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `update_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人';
ALTER TABLE `gh_clue` MODIFY COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）';

-- 2. 修复审核相关表的注释和字段注释
ALTER TABLE `sys_audit_form_indicator` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '审批表单特殊指标配置表';
ALTER TABLE `sys_audit_process_binding` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '流程审批业务类型绑定表';
ALTER TABLE `sys_audit_step_form` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '审批步骤表单配置表';
ALTER TABLE `sys_audit_task` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '审批任务表';
ALTER TABLE `sys_audit_task_cost` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '审批任务成本记录表';

-- 修复 sys_audit_form_indicator 表字段备注
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `step_form_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤表单ID（关联sys_audit_step_form表）';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `indicator_type` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指标类型：next_auditor-下个流程审批人员选择，cost_input-成本填写';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `indicator_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指标名称（显示名称）';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `indicator_config` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '指标配置JSON（根据不同类型存储不同配置）';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `sort_order` INT(11) DEFAULT 0 COMMENT '排序顺序';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `update_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';

-- 修复 sys_audit_process_binding 表字段备注
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `process_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程ID（关联sys_audit_process表）';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `business_type_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型ID（关联sys_category表，必须是最底层节点）';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `business_type_code` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务类型编码（冗余字段，方便查询）';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `business_type_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务类型名称（冗余字段，方便查询）';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `task_type` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'once' COMMENT '任务类型：once-一次性任务，recurring-周期任务';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `update_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_audit_process_binding` MODIFY COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';

-- 修复 sys_audit_step_form 表字段备注
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `step_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '步骤ID（关联sys_audit_step表）';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `form_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表单名称';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `form_config` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '表单配置JSON（包含普通表单组件和特殊指标）';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `remark_required` TINYINT(1) DEFAULT 1 COMMENT '是否必须填写备注：0-否，1-是';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `update_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_audit_step_form` MODIFY COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';

-- 修复 sys_audit_task 表字段备注
ALTER TABLE `sys_audit_task` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `order_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID（关联gh_order表）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `order_no` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单编号（冗余字段）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `process_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程ID（关联sys_audit_process表）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `step_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前步骤ID（关联sys_audit_step表）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `step_order` INT(11) NOT NULL COMMENT '当前步骤顺序';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `task_type` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务类型：once-一次性任务，recurring-周期任务';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `task_status` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '任务状态：pending-待审核，approved-已审核，rejected-已驳回';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `assigned_user_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指定审核人ID（如果配置了下个流程审批人员选择）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `assigned_user_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指定审核人姓名（冗余字段）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `current_role_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前审核角色ID';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `current_role_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '当前审核角色名称（冗余字段）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `audit_data` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '审核数据JSON（包含审核表单填写的数据）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `audit_result` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核结果：approved-通过，rejected-不通过，returned-驳回';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `audit_remark` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核备注';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `reject_reason` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '驳回原因（驳回时填写）';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `audit_user_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核人ID';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `audit_user_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核人姓名';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `update_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `sys_audit_task` MODIFY COLUMN `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除';

-- 修复 sys_audit_task_cost 表字段备注
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `task_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务ID（关联sys_audit_task表）';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `indicator_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指标ID（关联sys_audit_form_indicator表）';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `expense_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '报销名称';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `category_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '报销类目ID（关联sys_category表）';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `category_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '报销类目名称（冗余字段）';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '金额';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `remark` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `sort_order` INT(11) DEFAULT 0 COMMENT '排序顺序';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_task_cost` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';

