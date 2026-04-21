-- 修复数据库表和字段注释的乱码问题
-- 执行前请确保数据库连接使用 utf8mb4 编码

-- 修复表注释
ALTER TABLE `gh_accounting_contract` COMMENT = '合同管理表';
ALTER TABLE `gh_accounting_handover` COMMENT = '代账交接表';
ALTER TABLE `gh_accounting_handover_audit` COMMENT = '代账交接审核记录表';
ALTER TABLE `gh_business_task` COMMENT = '工商任务表';
ALTER TABLE `gh_customer` COMMENT = '客户表';
ALTER TABLE `gh_followup_detail` COMMENT = '跟进记录表';
ALTER TABLE `gh_opportunity` COMMENT = '商机管理表';
ALTER TABLE `gh_order` COMMENT = '订单主表';
ALTER TABLE `gh_order_audit` COMMENT = '订单审核记录表';
ALTER TABLE `gh_order_expense` COMMENT = '订单支出记录表';
ALTER TABLE `gh_order_form_data` COMMENT = '订单表单数据表';
ALTER TABLE `gh_order_operation_log` COMMENT = '订单操作记录表';
ALTER TABLE `gh_order_payment` COMMENT = '订单收费记录表';
ALTER TABLE `gh_order_step` COMMENT = '订单流程步骤表';
ALTER TABLE `sys_audit_process` COMMENT = '审核流程表';
ALTER TABLE `sys_audit_step` COMMENT = '审核步骤表';
ALTER TABLE `sys_dynamic_form` COMMENT = '动态表单主表';
ALTER TABLE `sys_dynamic_form_binding` COMMENT = '业务类型表单绑定表';
ALTER TABLE `sys_dynamic_form_version` COMMENT = '动态表单版本表';
ALTER TABLE `sys_team` COMMENT = '团队管理表';

-- 修复 gh_accounting_contract 表字段注释
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键ID';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `contract_no` varchar(50) NOT NULL COMMENT '合同编号';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `order_id` varchar(32) NOT NULL COMMENT '订单ID';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `order_no` varchar(50) DEFAULT NULL COMMENT '订单编号（冗余字段）';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称（冗余字段）';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `contract_amount` decimal(18,2) DEFAULT NULL COMMENT '合同金额';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `sign_date` date DEFAULT NULL COMMENT '签约日期';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `contract_status` varchar(20) DEFAULT 'draft' COMMENT '合同状态：draft-草稿,signed-已签约,executing-执行中,completed-已完成,terminated-已终止';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `remarks` varchar(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `gh_accounting_contract` MODIFY COLUMN `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除';

-- 修复 gh_business_task 表字段注释
ALTER TABLE `gh_business_task` MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键ID';
ALTER TABLE `gh_business_task` MODIFY COLUMN `order_id` varchar(32) NOT NULL COMMENT '订单ID';
ALTER TABLE `gh_business_task` MODIFY COLUMN `order_no` varchar(50) DEFAULT NULL COMMENT '订单编号（冗余字段）';
ALTER TABLE `gh_business_task` MODIFY COLUMN `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称（冗余字段）';
ALTER TABLE `gh_business_task` MODIFY COLUMN `task_status` varchar(50) DEFAULT NULL COMMENT '任务状态：pending_manager_audit-待工商经理审核,public_sea-公海待接收,assigned_to_me-待本人接收,task-任务,handover-交接,completed-已完成,problem_task-问题任务,recycle_bin-回收站';
ALTER TABLE `gh_business_task` MODIFY COLUMN `manager_audit_status` varchar(20) DEFAULT NULL COMMENT '工商经理审核状态：pending-待审核,approved-已通过,rejected-已驳回';
ALTER TABLE `gh_business_task` MODIFY COLUMN `manager_audit_time` datetime DEFAULT NULL COMMENT '工商经理审核时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `manager_audit_user_id` varchar(32) DEFAULT NULL COMMENT '工商经理审核用户ID';
ALTER TABLE `gh_business_task` MODIFY COLUMN `manager_audit_user_name` varchar(50) DEFAULT NULL COMMENT '工商经理审核人姓名';
ALTER TABLE `gh_business_task` MODIFY COLUMN `manager_audit_remark` varchar(500) DEFAULT NULL COMMENT '工商经理审核备注';
ALTER TABLE `gh_business_task` MODIFY COLUMN `assign_type` varchar(20) DEFAULT NULL COMMENT '分配类型：public_sea-放入公海,assign_user-指定工商人员';
ALTER TABLE `gh_business_task` MODIFY COLUMN `assigned_user_id` varchar(32) DEFAULT NULL COMMENT '分配的工商人员ID';
ALTER TABLE `gh_business_task` MODIFY COLUMN `assigned_user_name` varchar(50) DEFAULT NULL COMMENT '分配的工商人员姓名';
ALTER TABLE `gh_business_task` MODIFY COLUMN `assigned_time` datetime DEFAULT NULL COMMENT '分配时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `received_user_id` varchar(32) DEFAULT NULL COMMENT '接收人ID';
ALTER TABLE `gh_business_task` MODIFY COLUMN `received_user_name` varchar(50) DEFAULT NULL COMMENT '接收人姓名';
ALTER TABLE `gh_business_task` MODIFY COLUMN `received_time` datetime DEFAULT NULL COMMENT '接收时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `cost_category` varchar(100) DEFAULT NULL COMMENT '成本类目';
ALTER TABLE `gh_business_task` MODIFY COLUMN `cost_amount` decimal(18,2) DEFAULT NULL COMMENT '成本金额';
ALTER TABLE `gh_business_task` MODIFY COLUMN `handover_user_id` varchar(32) DEFAULT NULL COMMENT '交接人ID（工商人员）';
ALTER TABLE `gh_business_task` MODIFY COLUMN `handover_user_name` varchar(50) DEFAULT NULL COMMENT '交接人姓名';
ALTER TABLE `gh_business_task` MODIFY COLUMN `handover_time` datetime DEFAULT NULL COMMENT '交接时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `handover_status` varchar(20) DEFAULT NULL COMMENT '交接状态：pending-待交接,handovered-已交接,completed-交接完成';
ALTER TABLE `gh_business_task` MODIFY COLUMN `business_user_id` varchar(32) DEFAULT NULL COMMENT '业务人员ID';
ALTER TABLE `gh_business_task` MODIFY COLUMN `business_user_name` varchar(50) DEFAULT NULL COMMENT '业务人员姓名';
ALTER TABLE `gh_business_task` MODIFY COLUMN `complete_time` datetime DEFAULT NULL COMMENT '完成时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `problem_reason` varchar(500) DEFAULT NULL COMMENT '问题原因';
ALTER TABLE `gh_business_task` MODIFY COLUMN `recycle_reason` varchar(500) DEFAULT NULL COMMENT '回收原因';
ALTER TABLE `gh_business_task` MODIFY COLUMN `remarks` varchar(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `gh_business_task` MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人';
ALTER TABLE `gh_business_task` MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `gh_business_task` MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `gh_business_task` MODIFY COLUMN `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除';

-- 修复 gh_order 表字段注释
ALTER TABLE `gh_order` MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键ID';
ALTER TABLE `gh_order` MODIFY COLUMN `order_no` varchar(50) NOT NULL COMMENT '订单编号';
ALTER TABLE `gh_order` MODIFY COLUMN `company_id` varchar(32) DEFAULT NULL COMMENT '公司ID';
ALTER TABLE `gh_order` MODIFY COLUMN `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称';
ALTER TABLE `gh_order` MODIFY COLUMN `salesman` varchar(50) DEFAULT NULL COMMENT '业务员';
ALTER TABLE `gh_order` MODIFY COLUMN `business_type` varchar(100) DEFAULT NULL COMMENT '业务类型';
ALTER TABLE `gh_order` MODIFY COLUMN `region` varchar(200) DEFAULT NULL COMMENT '所属区域';
ALTER TABLE `gh_order` MODIFY COLUMN `contacts` varchar(100) DEFAULT NULL COMMENT '联系人员';
ALTER TABLE `gh_order` MODIFY COLUMN `address` varchar(500) DEFAULT NULL COMMENT '详细地址';
ALTER TABLE `gh_order` MODIFY COLUMN `contact_information` varchar(100) DEFAULT NULL COMMENT '联系方式';
ALTER TABLE `gh_order` MODIFY COLUMN `opportunity_source` varchar(50) DEFAULT NULL COMMENT '商机来源';
ALTER TABLE `gh_order` MODIFY COLUMN `delivery_method` varchar(50) DEFAULT NULL COMMENT '交单方式';
ALTER TABLE `gh_order` MODIFY COLUMN `audit_process_id` varchar(32) DEFAULT NULL COMMENT '流程审批ID（关联sys_audit_process表）';
ALTER TABLE `gh_order` MODIFY COLUMN `current_audit_step` int DEFAULT '1' COMMENT '当前审核步骤顺序';
ALTER TABLE `gh_order` MODIFY COLUMN `audit_status` varchar(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核,approved-已通过,rejected-已驳回';
ALTER TABLE `gh_order` MODIFY COLUMN `collection_time` datetime DEFAULT NULL COMMENT '收款时间';
ALTER TABLE `gh_order` MODIFY COLUMN `payment_method` varchar(50) DEFAULT NULL COMMENT '收款方式';
ALTER TABLE `gh_order` MODIFY COLUMN `collection_account_number` varchar(100) DEFAULT NULL COMMENT '收款账号';
ALTER TABLE `gh_order` MODIFY COLUMN `is_recurring` varchar(10) DEFAULT '0' COMMENT '周期业务：0-否，1-是';
ALTER TABLE `gh_order` MODIFY COLUMN `image_voucher` text COMMENT '图片凭证（JSON数组）';
ALTER TABLE `gh_order` MODIFY COLUMN `order_amount` decimal(18,2) DEFAULT '0.00' COMMENT '订单金额';
ALTER TABLE `gh_order` MODIFY COLUMN `contract_amount` decimal(18,2) DEFAULT '0.00' COMMENT '合同金额';
ALTER TABLE `gh_order` MODIFY COLUMN `final_payment_amount` decimal(18,2) DEFAULT '0.00' COMMENT '尾款金额';
ALTER TABLE `gh_order` MODIFY COLUMN `received_amount` decimal(18,2) DEFAULT '0.00' COMMENT '收款金额';
ALTER TABLE `gh_order` MODIFY COLUMN `order_status` varchar(10) DEFAULT '0' COMMENT '订单状态：0-待审核，1-审核通过，2-处理中，3-已完成，4-已驳回';
ALTER TABLE `gh_order` MODIFY COLUMN `operation_type` varchar(20) DEFAULT 'default' COMMENT '操作类型';
ALTER TABLE `gh_order` MODIFY COLUMN `remarks` varchar(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `gh_order` MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人';
ALTER TABLE `gh_order` MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `gh_order` MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `gh_order` MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `gh_order` MODIFY COLUMN `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除';

-- 修复 sys_audit_process 表字段注释
ALTER TABLE `sys_audit_process` MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `process_name` varchar(100) NOT NULL COMMENT '流程名称';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `process_code` varchar(50) NOT NULL COMMENT '流程编码';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `description` varchar(500) DEFAULT NULL COMMENT '流程描述';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_audit_process` MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

-- 修复 sys_audit_step 表字段注释
ALTER TABLE `sys_audit_step` MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `process_id` varchar(32) NOT NULL COMMENT '流程ID（关联sys_audit_process.id）';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `step_order` int NOT NULL COMMENT '步骤顺序';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `role_id` varchar(500) DEFAULT NULL COMMENT '审核角色ID列表（多个用逗号分隔）';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `role_name` varchar(500) DEFAULT NULL COMMENT '审核角色名称列表（多个用逗号分隔）';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `description` varchar(500) DEFAULT NULL COMMENT '步骤描述';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_audit_step` MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

-- 修复 sys_team 表字段注释
ALTER TABLE `sys_team` MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键ID';
ALTER TABLE `sys_team` MODIFY COLUMN `team_name` varchar(100) NOT NULL COMMENT '团队名称';
ALTER TABLE `sys_team` MODIFY COLUMN `team_code` varchar(50) DEFAULT NULL COMMENT '团队编码';
ALTER TABLE `sys_team` MODIFY COLUMN `team_leader` varchar(32) DEFAULT NULL COMMENT '团队负责人ID（关联sys_user.id）';
ALTER TABLE `sys_team` MODIFY COLUMN `team_leader_name` varchar(50) DEFAULT NULL COMMENT '团队负责人姓名';
ALTER TABLE `sys_team` MODIFY COLUMN `description` varchar(500) DEFAULT NULL COMMENT '团队描述';
ALTER TABLE `sys_team` MODIFY COLUMN `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用';
ALTER TABLE `sys_team` MODIFY COLUMN `sort_order` int DEFAULT '0' COMMENT '排序号';
ALTER TABLE `sys_team` MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人';
ALTER TABLE `sys_team` MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `sys_team` MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `sys_team` MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `sys_team` MODIFY COLUMN `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除';

