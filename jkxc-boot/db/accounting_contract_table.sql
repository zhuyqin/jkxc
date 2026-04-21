-- ============================================
-- 合同管理表结构SQL脚本
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_accounting_contract` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `contract_no` VARCHAR(50) NOT NULL COMMENT '合同编号',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单编号（冗余字段）',
  `company_name` VARCHAR(200) DEFAULT NULL COMMENT '公司名称（冗余字段）',
  `contract_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '合同金额',
  `sign_date` DATE DEFAULT NULL COMMENT '签订日期',
  `contract_status` VARCHAR(20) DEFAULT 'draft' COMMENT '合同状态：draft-草稿,signed-已签订,executing-执行中,completed-已完成,terminated-已终止',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_contract_status` (`contract_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同管理表';

