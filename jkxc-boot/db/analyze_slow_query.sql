-- 分析客户画像和价值分析的慢查询问题

-- 1. 检查表的数据量
SELECT 
    '========== 表数据量统计 ==========' AS info;

SELECT 
    'gh_customer' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN del_flag = 0 THEN 1 END) AS active_rows,
    COUNT(CASE WHEN del_flag = 1 THEN 1 END) AS deleted_rows
FROM gh_customer
UNION ALL
SELECT 
    'gh_order' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN del_flag = 0 THEN 1 END) AS active_rows,
    COUNT(CASE WHEN del_flag = 1 THEN 1 END) AS deleted_rows
FROM gh_order;

-- 2. 检查索引使用情况
SELECT 
    '========== 索引统计 ==========' AS info;

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    NULLABLE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('gh_customer', 'gh_order')
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME;

-- 3. 分析查询计划 - 客户查询
SELECT 
    '========== 客户查询分析 ==========' AS info;

EXPLAIN SELECT * FROM gh_customer 
WHERE del_flag = 0 
  AND create_time >= '2026-01-01 00:00:00' 
  AND create_time <= '2026-12-31 23:59:59';

-- 4. 分析查询计划 - 订单查询（按公司ID）
SELECT 
    '========== 订单查询分析（按公司ID）==========' AS info;

EXPLAIN SELECT * FROM gh_order 
WHERE del_flag = 0 
  AND company_id IN (
    SELECT id FROM gh_customer WHERE del_flag = 0 LIMIT 10
  );

-- 5. 检查是否有锁表
SELECT 
    '========== 锁表检查 ==========' AS info;

SHOW OPEN TABLES WHERE In_use > 0;

-- 6. 检查慢查询日志状态
SELECT 
    '========== 慢查询配置 ==========' AS info;

SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 7. 统计订单按客户分布
SELECT 
    '========== 订单分布统计 ==========' AS info;

SELECT 
    '订单总数' AS metric,
    COUNT(*) AS value
FROM gh_order WHERE del_flag = 0
UNION ALL
SELECT 
    '有订单的客户数' AS metric,
    COUNT(DISTINCT company_id) AS value
FROM gh_order WHERE del_flag = 0
UNION ALL
SELECT 
    '平均每客户订单数' AS metric,
    ROUND(COUNT(*) / COUNT(DISTINCT company_id), 2) AS value
FROM gh_order WHERE del_flag = 0;

-- 8. 找出订单最多的客户（可能导致性能问题）
SELECT 
    '========== 订单最多的前10个客户 ==========' AS info;

SELECT 
    c.corporate_name,
    COUNT(o.id) AS order_count,
    SUM(COALESCE(o.contract_amount, o.order_amount, 0)) AS total_amount
FROM gh_customer c
LEFT JOIN gh_order o ON c.id = o.company_id AND o.del_flag = 0
WHERE c.del_flag = 0
GROUP BY c.id, c.corporate_name
ORDER BY order_count DESC
LIMIT 10;
