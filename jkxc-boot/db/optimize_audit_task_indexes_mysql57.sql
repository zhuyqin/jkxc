-- ============================================
-- 性能优化：审批任务列表（MySQL 5.7）
-- 说明：MySQL 5.7 不支持 CREATE INDEX IF NOT EXISTS，这里用 information_schema + 动态SQL
-- 执行前请备份数据库
-- ============================================

SET @db = DATABASE();

-- ---------- sys_audit_task：支持列表过滤 + 排序 ----------
-- 待审核：task_type + task_status + del_flag + create_time
SET @idx = 'idx_task_type_status_del_createtime';
SET @tbl = 'sys_audit_task';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (task_type, task_status, del_flag, create_time)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 已审核/已驳回：task_type + task_status + del_flag + audit_time
SET @idx = 'idx_task_type_status_del_audittime';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (task_type, task_status, del_flag, audit_time)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 个人模式：audit_user_id + 任务过滤
SET @idx = 'idx_audit_user_type_status_del_audittime';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (audit_user_id, task_type, task_status, del_flag, audit_time)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 团队/角色过滤：current_role_id + 任务过滤
SET @idx = 'idx_role_type_status_del_createtime';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (current_role_id, task_type, task_status, del_flag, create_time)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- gh_order_expense：支持按 order_id 聚合 / group_concat 排序 ----------
SET @tbl = 'gh_order_expense';
SET @idx = 'idx_order_del_expensetime_createtime';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (order_id, del_flag, expense_time, create_time)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- sys_audit_step：支持 total_steps 统计 ----------
SET @tbl = 'sys_audit_step';
SET @idx = 'idx_process_steporder';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (process_id, step_order)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- sys_user：支持 salesmen_team（realname -> team_id） ----------
SET @tbl = 'sys_user';
SET @idx = 'idx_realname_del_status_team';
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema=@db AND table_name=@tbl AND index_name=@idx) > 0,
  CONCAT('SELECT ''', @idx, ' exists'' AS result;'),
  CONCAT('CREATE INDEX ', @idx, ' ON ', @tbl, ' (realname, del_flag, status, team_id)')
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'done' AS result;


