-- 收款账号管理表初始化脚本
-- 此脚本会自动执行，无需手动执行

CREATE TABLE IF NOT EXISTS `gh_bank_management` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `payee_person` VARCHAR(200) DEFAULT NULL COMMENT '收款单位/人',
  `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '收款方式',
  `collection_account` VARCHAR(200) DEFAULT NULL COMMENT '收款账户',
  `account_notes` VARCHAR(500) DEFAULT NULL COMMENT '账户备注',
  `collection_remarks` VARCHAR(500) DEFAULT NULL COMMENT '收款备注',
  `pic` VARCHAR(500) DEFAULT NULL COMMENT '收款码',
  `hidden` VARCHAR(1) DEFAULT '0' COMMENT '是否隐藏（0-显示，1-隐藏）',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` INT(1) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_hidden` (`hidden`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款账号管理表';

