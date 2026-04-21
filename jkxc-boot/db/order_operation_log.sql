-- 订单操作记录表
CREATE TABLE IF NOT EXISTS `gh_order_operation_log` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：create-创建,update-修改,approve-审批,reject-驳回,delete-删除等',
  `operation_desc` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `operator_id` VARCHAR(32) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `before_data` TEXT DEFAULT NULL COMMENT '操作前数据（JSON格式）',
  `after_data` TEXT DEFAULT NULL COMMENT '操作后数据（JSON格式）',
  `changed_fields` VARCHAR(1000) DEFAULT NULL COMMENT '变更字段（多个字段用逗号分隔）',
  `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_operation_type` (`operation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作记录表';

