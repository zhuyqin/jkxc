-- ============================================
-- 删除工资信息表(gh_salary_info)中的重复记录
-- 规则：对于同一个员工姓名(user_name)的重复记录，保留id最大的记录，删除其他重复记录
-- ============================================

-- ============================================
-- 方式1：删除重复记录，保留id最大的记录（最简单可靠，推荐使用）
-- ============================================

-- 先查看重复记录（执行前可以先执行这个查询确认）
-- SELECT 
--     user_name,
--     COUNT(*) as duplicate_count,
--     GROUP_CONCAT(id ORDER BY id DESC) as ids,
--     GROUP_CONCAT(create_time ORDER BY create_time DESC) as create_times
-- FROM gh_salary_info
-- WHERE del_flag = 0
-- AND user_name IS NOT NULL
-- AND user_name != ''
-- GROUP BY user_name
-- HAVING COUNT(*) > 1;

-- 删除重复记录，保留id最大的记录（根据员工姓名user_name判断）
DELETE t1 FROM gh_salary_info t1
INNER JOIN gh_salary_info t2
WHERE t1.user_name = t2.user_name
AND t1.user_name IS NOT NULL
AND t1.user_name != ''
AND t2.user_name IS NOT NULL
AND t2.user_name != ''
AND t1.del_flag = 0
AND t2.del_flag = 0
AND t1.id < t2.id;

-- ============================================
-- 方式2：如果上面的SQL执行有问题，可以使用下面的方式（更安全，先标记再删除）
-- ============================================

-- 步骤1：先查看会被删除的记录（确认后再执行删除）
-- SELECT 
--     t1.id,
--     t1.user_id,
--     t1.user_name,
--     t1.create_time,
--     '将被删除' as action
-- FROM gh_salary_info t1
-- INNER JOIN (
--     SELECT 
--         user_name,
--         MAX(id) as max_id
--     FROM gh_salary_info
--     WHERE del_flag = 0
--     AND user_name IS NOT NULL
--     AND user_name != ''
--     GROUP BY user_name
--     HAVING COUNT(*) > 1
-- ) t2 ON t1.user_name = t2.user_name
-- WHERE t1.del_flag = 0
-- AND t1.user_name IS NOT NULL
-- AND t1.user_name != ''
-- AND t1.id < t2.max_id;

-- 步骤2：确认无误后执行删除（保留id最大的一条记录）
-- DELETE t1 FROM gh_salary_info t1
-- INNER JOIN (
--     SELECT 
--         user_name,
--         MAX(id) as max_id
--     FROM gh_salary_info
--     WHERE del_flag = 0
--     AND user_name IS NOT NULL
--     AND user_name != ''
--     GROUP BY user_name
--     HAVING COUNT(*) > 1
-- ) t2 ON t1.user_name = t2.user_name
-- WHERE t1.del_flag = 0
-- AND t1.user_name IS NOT NULL
-- AND t1.user_name != ''
-- AND t1.id < t2.max_id;

-- ============================================
-- 方式3：根据create_time删除重复记录（如果需要保留最新创建时间的记录）
-- 注意：如果create_time可能为NULL，建议使用方式1
-- ============================================

-- 先查看重复记录（执行前可以先执行这个查询确认）
-- SELECT 
--     user_name,
--     COUNT(*) as duplicate_count,
--     GROUP_CONCAT(id ORDER BY create_time DESC) as ids,
--     GROUP_CONCAT(create_time ORDER BY create_time DESC) as create_times
-- FROM gh_salary_info
-- WHERE del_flag = 0
-- AND user_name IS NOT NULL
-- AND user_name != ''
-- GROUP BY user_name
-- HAVING COUNT(*) > 1;

-- 步骤1：先查看会被删除的记录（确认后再执行删除）
-- SELECT 
--     t1.id,
--     t1.user_id,
--     t1.user_name,
--     t1.create_time,
--     '将被删除' as action
-- FROM gh_salary_info t1
-- INNER JOIN (
--     SELECT 
--         user_name,
--         MAX(create_time) as max_create_time
--     FROM gh_salary_info
--     WHERE del_flag = 0
--     AND user_name IS NOT NULL
--     AND user_name != ''
--     GROUP BY user_name
--     HAVING COUNT(*) > 1
-- ) t2 ON t1.user_name = t2.user_name
-- WHERE t1.del_flag = 0
-- AND t1.user_name IS NOT NULL
-- AND t1.user_name != ''
-- AND (t1.create_time < t2.max_create_time OR (t1.create_time = t2.max_create_time AND t1.id < (
--     SELECT id FROM gh_salary_info t3 
--     WHERE t3.user_name = t1.user_name 
--     AND t3.del_flag = 0 
--     AND t3.create_time = t2.max_create_time 
--     ORDER BY t3.id DESC 
--     LIMIT 1
-- )));

-- 步骤2：确认无误后执行删除（保留最新创建时间的一条记录）
-- DELETE t1 FROM gh_salary_info t1
-- INNER JOIN (
--     SELECT 
--         user_name,
--         MAX(create_time) as max_create_time
--     FROM gh_salary_info
--     WHERE del_flag = 0
--     AND user_name IS NOT NULL
--     AND user_name != ''
--     GROUP BY user_name
--     HAVING COUNT(*) > 1
-- ) t2 ON t1.user_name = t2.user_name
-- WHERE t1.del_flag = 0
-- AND t1.user_name IS NOT NULL
-- AND t1.user_name != ''
-- AND (t1.create_time < t2.max_create_time OR (t1.create_time = t2.max_create_time AND t1.id < (
--     SELECT id FROM gh_salary_info t3 
--     WHERE t3.user_name = t1.user_name 
--     AND t3.del_flag = 0 
--     AND t3.create_time = t2.max_create_time 
--     ORDER BY t3.id DESC 
--     LIMIT 1
-- )));

-- ============================================
-- 执行后的验证查询：确认是否还有重复记录
-- ============================================

-- 执行删除后，运行这个查询确认是否还有重复记录（应该返回空结果）
-- SELECT 
--     user_name,
--     COUNT(*) as duplicate_count
-- FROM gh_salary_info
-- WHERE del_flag = 0
-- AND user_name IS NOT NULL
-- AND user_name != ''
-- GROUP BY user_name
-- HAVING COUNT(*) > 1;

