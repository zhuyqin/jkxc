-- ============================================
-- 工资信息表创建SQL脚本
-- 表名: gh_salary_info
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_salary_info` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '员工ID（关联sys_user表）',
  `user_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余字段，方便查询）',
  `current_position` varchar(100) DEFAULT NULL COMMENT '当前职位',
  `current_star_level` varchar(20) DEFAULT NULL COMMENT '当前星级',
  `base_salary` decimal(18,2) DEFAULT NULL COMMENT '基本工资',
  `performance_salary` decimal(18,2) DEFAULT NULL COMMENT '绩效工资',
  `housing_fund_subsidy` decimal(18,2) DEFAULT NULL COMMENT '公积金补贴',
  `high_temperature_subsidy` decimal(18,2) DEFAULT NULL COMMENT '高温补贴',
  `full_attendance_bonus` decimal(18,2) DEFAULT NULL COMMENT '全勤奖金',
  `company_social_security` decimal(18,2) DEFAULT NULL COMMENT '公司社保',
  `personal_social_security` decimal(18,2) DEFAULT NULL COMMENT '个人社保',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_name` (`user_name`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工资信息表';

