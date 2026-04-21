-- 商机管理表
CREATE TABLE IF NOT EXISTS `gh_opportunity` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `corporate_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `opportunity_name` varchar(200) DEFAULT NULL COMMENT '商机名称',
  `charge_person` varchar(100) DEFAULT NULL COMMENT '负责人',
  `contacts` varchar(100) DEFAULT NULL COMMENT '联系人',
  `contact_information` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `state` varchar(50) DEFAULT NULL COMMENT '状态',
  `opportunity_source` varchar(50) DEFAULT NULL COMMENT '商机来源',
  `founder` varchar(100) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modi_time` datetime DEFAULT NULL COMMENT '修改时间',
  `remarks` text COMMENT '备注',
  `customer_id` varchar(36) DEFAULT NULL COMMENT '客户编号',
  `region` varchar(200) DEFAULT NULL COMMENT '所属区域',
  `address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int(1) DEFAULT '0' COMMENT '删除标志',
  `clue_bonus` decimal(18, 2) DEFAULT NULL COMMENT '线索奖金',
  `clue_bonus_awarded` tinyint(1) NOT NULL DEFAULT 0 COMMENT '线索奖金已发放标识',
  `clue_bonus_awarded_time` datetime DEFAULT NULL COMMENT '线索奖金发放时间',
  PRIMARY KEY (`id`),
  KEY `idx_corporate_name` (`corporate_name`),
  KEY `idx_state` (`state`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机管理表';

-- 跟进记录表
CREATE TABLE IF NOT EXISTS `gh_followup_detail` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `opport_id` varchar(36) DEFAULT NULL COMMENT '商机ID',
  `followup_content` text COMMENT '跟进内容',
  `followup_way` varchar(50) DEFAULT NULL COMMENT '跟进方式',
  `followup_person` varchar(100) DEFAULT NULL COMMENT '跟进人员',
  `followup_next_time` date DEFAULT NULL COMMENT '下次跟进时间',
  `create_date` datetime DEFAULT NULL COMMENT '创建日期',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_opport_id` (`opport_id`),
  KEY `idx_create_date` (`create_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录表';

-- 商机管理菜单（使用十六进制编码避免乱码问题）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`) VALUES
('opportunity_menu', NULL, 0xE59586E69CBAE7AEA1E79086, '/opportunity', 'layouts/RouteView', NULL, NULL, 0, NULL, '1', 3.00, 0, 'fund', 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
('opportunity_list', 'opportunity_menu', 0xE59586E69CBAE58897E8A1A8, '/opportunity/list', 'opportunity/OpportunityList', NULL, NULL, 1, NULL, '1', 1.00, 0, NULL, 1, 1, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
('opp_add', 'opportunity_list', 0xE696B0E5A29EE59586E69CBA, NULL, NULL, NULL, NULL, 2, 'opportunity:add', '1', 1.00, 0, NULL, 1, 1, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
('opp_edit', 'opportunity_list', 0xE7BC96E8BE91E59586E69CBA, NULL, NULL, NULL, NULL, 2, 'opportunity:edit', '1', 2.00, 0, NULL, 1, 1, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
('opp_delete', 'opportunity_list', 0xE588A0E999A4E59586E69CBA, NULL, NULL, NULL, NULL, 2, 'opportunity:delete', '1', 3.00, 0, NULL, 1, 1, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0),
('opp_view', 'opportunity_list', 0xE69FA5E79C8BE59586E69CBA, NULL, NULL, NULL, NULL, 2, 'opportunity:view', '1', 4.00, 0, NULL, 1, 1, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 给admin角色分配权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) 
SELECT REPLACE(UUID(), '-', ''), 'f6817f48af4fb3af11b9e8bf182f618b', `id`, NULL, NOW(), '127.0.0.1' FROM `sys_permission` WHERE `id` IN ('opportunity_menu', 'opportunity_list', 'opp_add', 'opp_edit', 'opp_delete', 'opp_view');

