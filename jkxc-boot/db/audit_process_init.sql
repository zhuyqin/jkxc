-- ============================================
-- 审核流程管理功能初始化SQL脚本
-- 执行前请备份数据库！
-- ============================================

-- 第一部分：创建数据库表结构
-- ============================================

-- 1. 创建审核流程表
CREATE TABLE IF NOT EXISTS `sys_audit_process` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `process_name` VARCHAR(100) NOT NULL COMMENT '流程名称（如：订单审批、会计合同审批）',
  `process_code` VARCHAR(50) NOT NULL COMMENT '流程编码（唯一标识）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核流程表';

-- 2. 创建审核步骤表
CREATE TABLE IF NOT EXISTS `sys_audit_step` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `process_id` VARCHAR(32) NOT NULL COMMENT '流程ID',
  `step_order` INT(11) NOT NULL COMMENT '步骤顺序（从1开始）',
  `role_id` VARCHAR(32) NOT NULL COMMENT '审核角色ID（关联sys_role表）',
  `role_name` VARCHAR(100) DEFAULT NULL COMMENT '审核角色名称（冗余字段，方便查询）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '步骤描述',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_id` (`process_id`),
  KEY `idx_step_order` (`process_id`, `step_order`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_audit_step_process` FOREIGN KEY (`process_id`) REFERENCES `sys_audit_process` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核步骤表';

-- 第二部分：创建菜单配置
-- ============================================

-- 菜单ID（30位字符串）
SET @menu_id = 'auditprocessmanagement202501';

-- 检查菜单是否已存在，如果存在则先删除
DELETE FROM `sys_role_permission` WHERE `permission_id` = @menu_id;
DELETE FROM `sys_permission_data_rule` WHERE `permission_id` = @menu_id;
DELETE FROM `sys_permission` WHERE `id` = @menu_id;

-- 插入审核流程管理菜单（放在系统管理下）
INSERT INTO `sys_permission` (
    `id`,
    `parent_id`,
    `name`,
    `url`,
    `component`,
    `component_name`,
    `redirect`,
    `menu_type`,
    `perms`,
    `perms_type`,
    `sort_no`,
    `always_show`,
    `icon`,
    `is_route`,
    `is_leaf`,
    `keep_alive`,
    `hidden`,
    `description`,
    `status`,
    `del_flag`,
    `rule_flag`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `internal_or_external`
) VALUES (
    @menu_id,
    'd7d6e2e4e2934f2c9385a623fd98c6f3',  -- 系统管理的parent_id
    '审核流程管理',
    '/isystem/auditProcess',
    'system/AuditProcessList',
    'audit-process-list',
    NULL,
    1,  -- 菜单类型：1-菜单
    NULL,
    '1',
    7.0,  -- 排序号，放在系统通告之后
    0,
    'audit',
    1,  -- 是否路由
    1,  -- 是否叶子节点
    0,  -- 是否缓存
    0,  -- 是否隐藏
    '配置审核流程，支持多步骤审核',
    '1',  -- 状态：1-启用
    0,  -- 删除标志：0-正常
    0,  -- 是否配置数据权限
    'admin',
    NOW(),
    NULL,
    NULL,
    0  -- 是否外链
);

-- 第三部分：为管理员角色分配菜单权限
-- ============================================

-- 为默认管理员角色分配菜单权限
-- 注意：如果您的系统中管理员角色ID不同，请修改下面的角色ID
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT 
    'rp_auditprocess202501_admin',
    'f6817f48af4fb3af11b9e8bf182f618b',  -- 默认管理员角色ID
    @menu_id,
    NULL,
    NOW(),
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` 
    WHERE `role_id` = 'f6817f48af4fb3af11b9e8bf182f618b' 
    AND `permission_id` = 'auditprocessmanagement202501'
);

-- ============================================
-- 验证脚本执行结果
-- ============================================

-- 验证菜单是否创建成功
SELECT 
    id,
    name,
    url,
    component,
    sort_no,
    status,
    del_flag
FROM `sys_permission`
WHERE id = 'auditprocessmanagement202501';

-- 验证表是否创建成功
SHOW TABLES LIKE 'sys_audit%';

-- ============================================
-- 执行完成！
-- 请重启应用服务以使菜单生效
-- ============================================

