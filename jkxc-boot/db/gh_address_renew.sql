-- ============================================
-- 地址中心续费信息表创建SQL脚本
-- 表名: gh_address_renew
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_address_renew` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `address_id` varchar(32) DEFAULT NULL COMMENT '地址id',
  `renewal_time` date DEFAULT NULL COMMENT '续费时间',
  `original_due_date` date DEFAULT NULL COMMENT '原到期日期',
  `post_expiration_date` date DEFAULT NULL COMMENT '后到期日期',
  `enewa_amount` varchar(50) DEFAULT NULL COMMENT '应续费金额',
  `amount_received` varchar(50) DEFAULT NULL COMMENT '到款金额',
  `financial_audit` varchar(20) DEFAULT NULL COMMENT '财务审核',
  `audit_status` varchar(20) DEFAULT NULL COMMENT '审核状态：0-待审核，1-已通过，2-已驳回',
  `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `company_id` varchar(32) DEFAULT NULL COMMENT '公司ID',
  `amounts` varchar(50) DEFAULT NULL COMMENT '应续费金额',
  `detail_type` varchar(50) DEFAULT NULL COMMENT '收款类型（续费类型）',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `charge_amount` decimal(18,2) DEFAULT NULL COMMENT '收费金额',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '收款方式',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `vouchers` varchar(500) DEFAULT NULL COMMENT '凭证',
  `collection_account_number` varchar(100) DEFAULT NULL COMMENT '收款账号',
  `contract_no` varchar(100) DEFAULT NULL COMMENT '合同编号',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `ht_id` varchar(32) DEFAULT NULL COMMENT '合同ID',
  `bj_flag` varchar(10) DEFAULT NULL COMMENT '标记',
  `sale_man` varchar(50) DEFAULT NULL COMMENT '签单员',
  `creator` varchar(50) DEFAULT NULL COMMENT '创建人',
  `address_audit` varchar(20) DEFAULT NULL COMMENT '地址审核',
  PRIMARY KEY (`id`),
  KEY `idx_address_id` (`address_id`),  
  KEY `idx_company_id` (`company_id`),
  KEY `idx_ht_id` (`ht_id`),
  KEY `idx_contract_no` (`contract_no`),
  KEY `idx_renewal_time` (`renewal_time`),
  KEY `idx_detail_type` (`detail_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址中心续费信息';

