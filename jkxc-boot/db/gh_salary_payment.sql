-- ============================================
-- 工资发放记录表创建SQL脚本
-- 表名: gh_salary_payment
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_salary_payment` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '员工ID（关联sys_user表）',
  `user_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余字段，方便查询）',
  `payment_month` varchar(7) NOT NULL COMMENT '发放月份（格式：YYYY-MM）',
  `base_salary` decimal(18,2) DEFAULT NULL COMMENT '基本工资',
  `performance_salary` decimal(18,2) DEFAULT NULL COMMENT '绩效工资',
  `housing_fund_subsidy` decimal(18,2) DEFAULT NULL COMMENT '公积金补贴',
  `high_temperature_subsidy` decimal(18,2) DEFAULT NULL COMMENT '高温补贴',
  `full_attendance_bonus` decimal(18,2) DEFAULT NULL COMMENT '全勤奖金',
  `company_social_security` decimal(18,2) DEFAULT NULL COMMENT '公司社保',
  `personal_social_security` decimal(18,2) DEFAULT NULL COMMENT '个人社保',
  `total_amount` decimal(18,2) DEFAULT NULL COMMENT '应发总额（基本工资+绩效工资+公积金补贴+高温补贴+全勤奖金）',
  `deduction_amount` decimal(18,2) DEFAULT NULL COMMENT '扣除总额（公司社保+个人社保）',
  `actual_amount` decimal(18,2) DEFAULT NULL COMMENT '实发金额（应发总额-扣除总额）',
  `payment_status` varchar(20) DEFAULT NULL COMMENT '发放状态（pending-待发放，paid-已发放，cancelled-已取消）',
  `payment_time` datetime DEFAULT NULL COMMENT '发放时间',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '发放方式（bank_transfer-银行转账，cash-现金，other-其他）',
  `payment_account_id` varchar(32) DEFAULT NULL COMMENT '发放账号ID（关联gh_bank_management表）',
  `payment_account` varchar(200) DEFAULT NULL COMMENT '发放账号（冗余字段）',
  `vouchers` varchar(1000) DEFAULT NULL COMMENT '凭证附件（JSON数组）',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_month` (`payment_month`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_create_time` (`create_time`),
  UNIQUE KEY `uk_user_month` (`user_id`, `payment_month`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工资发放记录表';

