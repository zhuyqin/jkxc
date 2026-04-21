-- ============================================
-- 清理孤立的绑定记录（找不到对应审批流程的绑定记录）
-- 执行前请备份数据库！
-- ============================================

-- 1. 先查看有多少条孤立的绑定记录
SELECT 
    b.id,
    b.process_id,
    b.business_type_id,
    b.business_type_name,
    b.task_type,
    b.create_time
FROM sys_audit_process_binding b
LEFT JOIN sys_audit_process p ON b.process_id = p.id
WHERE p.id IS NULL;

-- 2. 删除孤立的绑定记录
DELETE b
FROM sys_audit_process_binding b
LEFT JOIN sys_audit_process p ON b.process_id = p.id
WHERE p.id IS NULL;

-- 3. 验证删除结果（应该返回0条记录）
SELECT COUNT(*) AS orphan_count
FROM sys_audit_process_binding b
LEFT JOIN sys_audit_process p ON b.process_id = p.id
WHERE p.id IS NULL;

