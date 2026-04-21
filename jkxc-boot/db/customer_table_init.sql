-- 客户表初始化脚本
-- 创建客户表
CREATE TABLE IF NOT EXISTS `gh_customer` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `corporate_name` VARCHAR(200) NOT NULL COMMENT '公司名称',
  `contacts` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_information` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  `region` VARCHAR(100) DEFAULT NULL COMMENT '所属区域',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '详细地址',
  `legal_person_name` VARCHAR(50) DEFAULT NULL COMMENT '法人姓名',
  `legal_person_phone` VARCHAR(50) DEFAULT NULL COMMENT '法人电话',
  `duty_paragraph` VARCHAR(50) DEFAULT NULL COMMENT '税号',
  `registered_address` VARCHAR(500) DEFAULT NULL COMMENT '注册地址',
  `actual_address` VARCHAR(500) DEFAULT NULL COMMENT '实际地址',
  `registered_capital` VARCHAR(50) DEFAULT NULL COMMENT '注册资金',
  `business_scope` TEXT DEFAULT NULL COMMENT '经营范围',
  `status` VARCHAR(1) DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` INT(1) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_corporate_name` (`corporate_name`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

