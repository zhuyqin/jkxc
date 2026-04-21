-- ============================================
-- 所属团队管理功能SQL脚本
-- ============================================

-- 创建团队表
CREATE TABLE IF NOT EXISTS `sys_team` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `team_name` VARCHAR(100) NOT NULL COMMENT '团队名称',
  `team_code` VARCHAR(50) DEFAULT NULL COMMENT '团队编码',
  `team_leader` VARCHAR(32) DEFAULT NULL COMMENT '团队负责人ID（关联sys_user.id）',
  `team_leader_name` VARCHAR(50) DEFAULT NULL COMMENT '团队负责人姓名',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '团队描述',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序号',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_code` (`team_code`),
  KEY `idx_team_name` (`team_name`),
  KEY `idx_status` (`status`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队管理表';

-- 在用户表中添加团队ID字段
ALTER TABLE `sys_user` 
ADD COLUMN IF NOT EXISTS `team_id` VARCHAR(32) DEFAULT NULL COMMENT '所属团队ID（关联sys_team.id）' AFTER `rel_tenant_ids`,
ADD KEY `idx_team_id` (`team_id`);

