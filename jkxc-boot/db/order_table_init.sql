-- 订单管理表结构初始化SQL脚本
-- 此脚本会自动执行，无需手动执行

-- ============================================
-- 第一部分：创建订单主表
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_order` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `company_id` VARCHAR(32) DEFAULT NULL COMMENT '公司ID',
  `company_name` VARCHAR(200) DEFAULT NULL COMMENT '公司名称',
  `salesman` VARCHAR(50) DEFAULT NULL COMMENT '业务员',
  `business_type` VARCHAR(100) DEFAULT NULL COMMENT '业务类型',
  `order_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '订单金额',
  `order_status` VARCHAR(10) DEFAULT '0' COMMENT '订单状态：0-待审核，1-审核通过，2-处理中，3-已完成，4-已驳回',
  `operation_type` VARCHAR(20) DEFAULT 'default' COMMENT '操作类型',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ============================================
-- 第二部分：创建订单流程步骤表
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_order_step` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `step_type` VARCHAR(20) NOT NULL COMMENT '步骤类型：0-订单创建，1-经理审核，2-出纳审核，3-任务分配，4-任务处理，5-任务完成，6-订单完成，7-订单驳回',
  `step_order` INT(11) NOT NULL COMMENT '步骤顺序（从1开始）',
  `operator_id` VARCHAR(32) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态：0-待处理，1-已完成，2-处理中，3-已驳回，4-已取消',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_step_order` (`order_id`, `step_order`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_step_order` FOREIGN KEY (`order_id`) REFERENCES `gh_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单流程步骤表';

-- ============================================
-- 第三部分：创建订单收费记录表
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_order_payment` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `payment_time` DATETIME DEFAULT NULL COMMENT '收费时间',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '收费金额',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '收款方式',
  `account_number` VARCHAR(100) DEFAULT NULL COMMENT '收款账号',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态：0-待确认，1-已确认',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_payment_time` (`payment_time`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_payment_order` FOREIGN KEY (`order_id`) REFERENCES `gh_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单收费记录表';

-- ============================================
-- 第四部分：创建订单支出记录表
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_order_expense` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `expense_time` DATETIME DEFAULT NULL COMMENT '支出时间',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '支出金额',
  `category` VARCHAR(100) DEFAULT NULL COMMENT '支出类目',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_expense_time` (`expense_time`),
  CONSTRAINT `fk_order_expense_order` FOREIGN KEY (`order_id`) REFERENCES `gh_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单支出记录表';

-- ============================================
-- 验证脚本执行结果
-- ============================================

SELECT 
    TABLE_NAME,
    TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('gh_order', 'gh_order_step', 'gh_order_payment', 'gh_order_expense')
ORDER BY TABLE_NAME;

