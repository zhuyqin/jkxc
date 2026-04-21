-- 客户关联表（多对多关系）
-- 用于存储客户与企业的关联关系
CREATE TABLE IF NOT EXISTS `gh_customer_relation` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `customer_id` VARCHAR(32) NOT NULL COMMENT '客户ID',
  `related_customer_id` VARCHAR(32) NOT NULL COMMENT '关联客户ID',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` INT(1) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_relation` (`customer_id`, `related_customer_id`, `del_flag`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_related_customer_id` (`related_customer_id`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户关联表';

