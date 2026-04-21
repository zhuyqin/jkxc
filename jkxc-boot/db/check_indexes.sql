-- 检查客户画像和价值分析相关的索引是否存在

SELECT 
    '========== gh_customer 表索引 ==========' AS info;

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    INDEX_COMMENT
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'gh_customer'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

SELECT 
    '========== gh_order 表索引 ==========' AS info;

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    INDEX_COMMENT
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'gh_order'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 检查是否缺少关键索引
SELECT 
    '========== 索引检查结果 ==========' AS info;

SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.STATISTICS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'gh_customer' 
            AND INDEX_NAME = 'idx_create_time_del_flag'
        ) THEN '✓ gh_customer.idx_create_time_del_flag 已存在'
        ELSE '✗ gh_customer.idx_create_time_del_flag 缺失'
    END AS status
UNION ALL
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.STATISTICS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'gh_order' 
            AND INDEX_NAME = 'idx_company_id_create_time'
        ) THEN '✓ gh_order.idx_company_id_create_time 已存在'
        ELSE '✗ gh_order.idx_company_id_create_time 缺失 (最重要)'
    END AS status
UNION ALL
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.STATISTICS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'gh_order' 
            AND INDEX_NAME = 'idx_business_type_del_flag'
        ) THEN '✓ gh_order.idx_business_type_del_flag 已存在'
        ELSE '✗ gh_order.idx_business_type_del_flag 缺失'
    END AS status
UNION ALL
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.STATISTICS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'gh_order' 
            AND INDEX_NAME = 'idx_salesman_del_flag'
        ) THEN '✓ gh_order.idx_salesman_del_flag 已存在'
        ELSE '✗ gh_order.idx_salesman_del_flag 缺失'
    END AS status
UNION ALL
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.STATISTICS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'gh_order' 
            AND INDEX_NAME = 'idx_salesman_create_time'
        ) THEN '✓ gh_order.idx_salesman_create_time 已存在'
        ELSE '✗ gh_order.idx_salesman_create_time 缺失'
    END AS status;
