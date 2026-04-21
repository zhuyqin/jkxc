-- ============================================
-- 检查重复角色代码SQL脚本
-- 用于排查角色代码重复问题
-- ============================================

-- 1. 查询所有角色代码及其出现次数
SELECT 
    role_code,
    COUNT(*) as count,
    GROUP_CONCAT(id ORDER BY id) as role_ids,
    GROUP_CONCAT(role_name ORDER BY id) as role_names
FROM sys_role
WHERE del_flag = 0
GROUP BY role_code
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- 2. 查询特定角色代码 'xsjl' 的详细信息
SELECT 
    id,
    role_name,
    role_code,
    role_type,
    status,
    create_by,
    create_time,
    update_time
FROM sys_role
WHERE role_code = 'xsjl'
  AND del_flag = 0
ORDER BY create_time DESC;

-- 3. 如果确认需要删除重复的角色（请谨慎操作！）
-- 注意：删除前请先检查该角色是否被用户使用
-- 查询使用该角色的用户数量
SELECT 
    r.role_code,
    r.role_name,
    COUNT(ur.user_id) as user_count
FROM sys_role r
LEFT JOIN sys_user_role ur ON r.id = ur.role_id
WHERE r.role_code = 'xsjl'
  AND r.del_flag = 0
GROUP BY r.id, r.role_code, r.role_name;

-- 4. 如果确认要删除某个重复的角色（示例：删除较早创建的，保留最新的）
-- 请先备份数据，然后执行以下SQL（请根据实际情况修改）
-- DELETE FROM sys_role WHERE id = '要删除的角色ID' AND role_code = 'xsjl';

-- 5. 查询所有角色代码（用于检查是否有其他重复）
SELECT 
    role_code,
    COUNT(*) as count
FROM sys_role
WHERE del_flag = 0
GROUP BY role_code
ORDER BY count DESC, role_code;

