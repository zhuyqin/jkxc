-- ============================================
-- 测试SQL：模拟查询已审核任务列表的SQL
-- 用于测试MySQL 5.7.44对audit_data字段的兼容性
-- ============================================

-- 测试查询1：简化版本，测试audit_data字段是否能正常查询
SELECT 
    t.id,
    t.order_id,
    t.audit_data,
    JSON_UNQUOTE(JSON_EXTRACT(t.audit_data, '$.startMonth')) AS start_month,
    JSON_UNQUOTE(JSON_EXTRACT(t.audit_data, '$.endMonth')) AS end_month
FROM sys_audit_task t
WHERE t.del_flag = 0
LIMIT 5;

-- 测试查询2：测试子查询中引用外部表字段（模拟service_user_name的提取）
-- 方法1：使用相关子查询在WHERE子句中
SELECT 
    t.id,
    t.order_id,
    (SELECT GROUP_CONCAT(
        COALESCE(su.realname, username_part, '')
        SEPARATOR '; '
    )
    FROM (
        SELECT 
            TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
                COALESCE(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')), ''),
                ';', 
                numbers.n
            ), ';', -1)) AS username_part
        FROM (
            SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        ) AS numbers
        WHERE JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')) IS NOT NULL 
            AND JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')) != ''
            AND (
                (LOCATE(';', JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName'))) = 0 AND numbers.n = 1)
                OR (LOCATE(';', JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName'))) > 0 
                    AND numbers.n <= (CHAR_LENGTH(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName'))) - CHAR_LENGTH(REPLACE(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')), ';', '')) + 1))
            )
    ) AS username_list
    LEFT JOIN sys_user su ON su.username = username_list.username_part AND su.del_flag = 0 AND (su.status = 1 OR su.status IS NULL)
    WHERE username_list.username_part IS NOT NULL AND username_list.username_part != ''
    ) AS service_user_name
FROM sys_audit_task t
WHERE t.del_flag = 0
LIMIT 5;

-- 测试查询2-2：简化版本，先提取audit_data值
SELECT 
    t.id,
    t.order_id,
    t.audit_data,
    (SELECT GROUP_CONCAT(
        COALESCE(su.realname, username_part, '')
        SEPARATOR '; '
    )
    FROM (
        SELECT 
            TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
                COALESCE(service_user_name_str, ''),
                ';', 
                numbers.n
            ), ';', -1)) AS username_part
        FROM (
            SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        ) AS numbers
        CROSS JOIN (
            SELECT COALESCE(JSON_UNQUOTE(JSON_EXTRACT(t.audit_data, '$.serviceUserName')), '') AS service_user_name_str
        ) AS audit_data_val
        WHERE service_user_name_str IS NOT NULL 
            AND service_user_name_str != ''
            AND (
                (LOCATE(';', service_user_name_str) = 0 AND numbers.n = 1)
                OR (LOCATE(';', service_user_name_str) > 0 
                    AND numbers.n <= (CHAR_LENGTH(service_user_name_str) - CHAR_LENGTH(REPLACE(service_user_name_str, ';', '')) + 1))
            )
    ) AS username_list
    LEFT JOIN sys_user su ON su.username = username_list.username_part AND su.del_flag = 0 AND (su.status = 1 OR su.status IS NULL)
    WHERE username_list.username_part IS NOT NULL AND username_list.username_part != ''
    ) AS service_user_name
FROM sys_audit_task t
WHERE t.del_flag = 0
LIMIT 5;

-- 测试查询3：完整模拟getApprovedTasks查询（简化版）
SELECT 
    t.id, 
    t.order_id, 
    t.order_no, 
    t.task_type, 
    t.task_status,
    t.audit_data,
    o.company_name, 
    o.business_type,
    (SELECT name FROM sys_category c WHERE c.id = o.business_type) AS business_type_name,
    o.salesman,
    COALESCE(u.realname, t.audit_user_name) AS assigned_user_name,
    (SELECT GROUP_CONCAT(
        COALESCE(su.realname, username_part, '')
        SEPARATOR '; '
    )
    FROM (
        SELECT 
            TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
                COALESCE(service_user_name_val, ''),
                ';', 
                numbers.n
            ), ';', -1)) AS username_part
        FROM (
            SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        ) AS numbers
        CROSS JOIN (
            SELECT COALESCE(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')), '') AS service_user_name_val
        ) AS audit_data_sub
        WHERE service_user_name_val IS NOT NULL 
            AND service_user_name_val != ''
            AND (
                (LOCATE(';', service_user_name_val) = 0 AND numbers.n = 1)
                OR (LOCATE(';', service_user_name_val) > 0 
                    AND numbers.n <= (CHAR_LENGTH(service_user_name_val) - CHAR_LENGTH(REPLACE(service_user_name_val, ';', '')) + 1))
            )
    ) AS username_list
    LEFT JOIN sys_user su ON su.username = username_list.username_part AND su.del_flag = 0 AND (su.status = 1 OR su.status IS NULL)
    WHERE username_list.username_part IS NOT NULL AND username_list.username_part != ''
    ) AS service_user_name,
    JSON_UNQUOTE(JSON_EXTRACT(t.audit_data, '$.startMonth')) AS start_month,
    JSON_UNQUOTE(JSON_EXTRACT(t.audit_data, '$.endMonth')) AS end_month
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
LEFT JOIN sys_user u ON t.assigned_user_id = u.id
WHERE t.task_type = 'once'
    AND t.task_status = 'approved'
    AND t.del_flag = 0
    AND o.del_flag = 0
ORDER BY t.audit_time DESC
LIMIT 10;

-- 测试查询4：如果上面的查询失败，尝试这个更简单的版本（不使用CROSS JOIN）
SELECT 
    t.id, 
    t.order_id,
    t.audit_data,
    (SELECT GROUP_CONCAT(
        COALESCE(su.realname, username_part, '')
        SEPARATOR '; '
    )
    FROM (
        SELECT 
            TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
                COALESCE(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')), ''),
                ';', 
                numbers.n
            ), ';', -1)) AS username_part
        FROM (
            SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        ) AS numbers
        WHERE JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')) IS NOT NULL 
            AND JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')) != ''
            AND (
                (LOCATE(';', JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName'))) = 0 AND numbers.n = 1)
                OR (LOCATE(';', JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName'))) > 0 
                    AND numbers.n <= (CHAR_LENGTH(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName'))) - CHAR_LENGTH(REPLACE(JSON_UNQUOTE(JSON_EXTRACT((SELECT audit_data FROM sys_audit_task WHERE id = t.id LIMIT 1), '$.serviceUserName')), ';', '')) + 1))
            )
    ) AS username_list
    LEFT JOIN sys_user su ON su.username = username_list.username_part AND su.del_flag = 0 AND (su.status = 1 OR su.status IS NULL)
    WHERE username_list.username_part IS NOT NULL AND username_list.username_part != ''
    ) AS service_user_name
FROM sys_audit_task t
WHERE t.del_flag = 0
LIMIT 5;

