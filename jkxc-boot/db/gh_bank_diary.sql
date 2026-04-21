-- ============================================
-- 银行日记表创建SQL脚本
-- 表名: gh_bank_diary
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_bank_diary` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `order_date` date DEFAULT NULL COMMENT '下单日期',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型（reimbursement-报销，order-订单，manual-手动）',
  `business_id` varchar(32) DEFAULT NULL COMMENT '业务ID（报销ID或订单ID）',
  `fee_detail` varchar(500) DEFAULT NULL COMMENT '费用详情',
  `income_amount` decimal(18,2) DEFAULT NULL COMMENT '收入金额',
  `expense_amount` decimal(18,2) DEFAULT NULL COMMENT '支出金额',
  `balance_amount` decimal(18,2) DEFAULT NULL COMMENT '结余金额',
  `bank_account_id` varchar(32) DEFAULT NULL COMMENT '银行账户ID',
  `bank_account_name` varchar(200) DEFAULT NULL COMMENT '银行账户名称（冗余字段，方便查询）',
  `operator_id` varchar(32) DEFAULT NULL COMMENT '操作人员ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人员姓名（冗余字段）',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `vouchers` varchar(1000) DEFAULT NULL COMMENT '凭证附件（JSON数组）',
  `company_id` varchar(32) DEFAULT NULL COMMENT '公司ID',
  `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `reimbursement_id` varchar(32) DEFAULT NULL COMMENT '关联报销ID',
  `order_id` varchar(32) DEFAULT NULL COMMENT '关联订单ID',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_business_id` (`business_id`),
  KEY `idx_bank_account_id` (`bank_account_id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_reimbursement_id` (`reimbursement_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='银行日记表';

