-- 角色管理树结构改造SQL脚本
-- 执行前请备份数据库！

-- 1. 添加父级角色ID字段（支持树结构）
ALTER TABLE sys_role ADD COLUMN parent_id VARCHAR(32) NULL COMMENT '父级角色ID' AFTER id;

-- 2. 添加角色类型字段（1-操作角色，2-数据角色）
ALTER TABLE sys_role ADD COLUMN role_type TINYINT(1) DEFAULT 1 COMMENT '角色类型：1-操作角色，2-数据角色' AFTER role_code;

-- 3. 添加角色排序字段
ALTER TABLE sys_role ADD COLUMN role_order INT(11) DEFAULT 0 COMMENT '角色排序' AFTER role_type;

-- 4. 添加角色状态字段（0-禁用，1-启用）
ALTER TABLE sys_role ADD COLUMN status TINYINT(1) DEFAULT 1 COMMENT '角色状态：0-禁用，1-启用' AFTER role_order;

-- 5. 为parent_id添加索引
CREATE INDEX idx_parent_id ON sys_role(parent_id);

-- 6. 为role_type添加索引
CREATE INDEX idx_role_type ON sys_role(role_type);

-- 7. 更新现有数据：将所有现有角色设置为操作角色（默认）
UPDATE sys_role SET role_type = 1 WHERE role_type IS NULL;

-- 8. 更新现有数据：将所有现有角色设置为启用状态
UPDATE sys_role SET status = 1 WHERE status IS NULL;

