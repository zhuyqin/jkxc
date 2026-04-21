-- =====================================================
-- 客户双向关联关系修复脚本
-- =====================================================
-- 功能: 修复数据库中不对称的客户关联关系
-- 说明: 将单向关联转换为双向关联
-- 执行前: 建议先备份 gh_customer 表
-- =====================================================

-- 1. 查看当前不对称的关联关系
-- =====================================================
SELECT 
    '不对称关联检查' as check_type,
    COUNT(*) as count
FROM (
    SELECT 
        a.id as customer_a_id,
        a.corporate_name as customer_a_name,
        SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1) as related_id
    FROM gh_customer a
    CROSS JOIN (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers
    WHERE a.del_flag = 0
      AND a.related_company_id IS NOT NULL
      AND a.related_company_id != ''
      AND CHAR_LENGTH(a.related_company_id) - CHAR_LENGTH(REPLACE(a.related_company_id, ',', '')) >= numbers.n - 1
) a_relations
JOIN gh_customer b ON b.id = a_relations.related_id AND b.del_flag = 0
WHERE (b.related_company_id IS NULL 
       OR b.related_company_id = '' 
       OR FIND_IN_SET(a_relations.customer_a_id, b.related_company_id) = 0);

-- 2. 详细列出不对称的关联关系
-- =====================================================
SELECT 
    a.id as customer_a_id,
    a.corporate_name as customer_a_name,
    a.related_company_id as a_related_ids,
    b.id as customer_b_id,
    b.corporate_name as customer_b_name,
    b.related_company_id as b_related_ids,
    CASE 
        WHEN b.related_company_id IS NULL OR b.related_company_id = '' THEN 'B没有关联'
        WHEN FIND_IN_SET(a.id, b.related_company_id) = 0 THEN 'B未关联A'
        ELSE '正常'
    END as status
FROM (
    SELECT 
        a.id,
        a.corporate_name,
        a.related_company_id,
        SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1) as related_id
    FROM gh_customer a
    CROSS JOIN (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers
    WHERE a.del_flag = 0
      AND a.related_company_id IS NOT NULL
      AND a.related_company_id != ''
      AND CHAR_LENGTH(a.related_company_id) - CHAR_LENGTH(REPLACE(a.related_company_id, ',', '')) >= numbers.n - 1
) a
JOIN gh_customer b ON b.id = a.related_id AND b.del_flag = 0
WHERE (b.related_company_id IS NULL 
       OR b.related_company_id = '' 
       OR FIND_IN_SET(a.id, b.related_company_id) = 0)
ORDER BY a.id, b.id;

-- 3. 查找包含无效客户ID的关联关系
-- =====================================================
SELECT 
    c.id,
    c.corporate_name,
    c.related_company_id,
    '包含无效ID' as issue
FROM gh_customer c
WHERE c.del_flag = 0
  AND c.related_company_id IS NOT NULL
  AND c.related_company_id != ''
  AND EXISTS (
    SELECT 1
    FROM (
        SELECT 
            SUBSTRING_INDEX(SUBSTRING_INDEX(c.related_company_id, ',', numbers.n), ',', -1) as related_id
        FROM (
            SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        ) numbers
        WHERE CHAR_LENGTH(c.related_company_id) - CHAR_LENGTH(REPLACE(c.related_company_id, ',', '')) >= numbers.n - 1
    ) ids
    WHERE NOT EXISTS (
        SELECT 1 FROM gh_customer c2 
        WHERE c2.id = ids.related_id AND c2.del_flag = 0
    )
  );

-- 4. 查找自己关联自己的异常数据
-- =====================================================
SELECT 
    id,
    corporate_name,
    related_company_id,
    '自己关联自己' as issue
FROM gh_customer
WHERE del_flag = 0
  AND related_company_id IS NOT NULL
  AND related_company_id != ''
  AND FIND_IN_SET(id, related_company_id) > 0;

-- =====================================================
-- 修复脚本（需要根据实际情况选择执行）
-- =====================================================

-- 方案1: 建立双向关联（推荐）
-- =====================================================
-- 说明: 为所有单向关联建立反向关联
-- 注意: 此脚本需要在MySQL 5.7+环境中执行，且需要分批执行避免锁表

-- 创建临时表存储需要修复的关联关系
DROP TEMPORARY TABLE IF EXISTS temp_fix_relations;
CREATE TEMPORARY TABLE temp_fix_relations (
    customer_a_id VARCHAR(32),
    customer_b_id VARCHAR(32),
    INDEX idx_a (customer_a_id),
    INDEX idx_b (customer_b_id)
);

-- 插入需要修复的关联关系
INSERT INTO temp_fix_relations (customer_a_id, customer_b_id)
SELECT DISTINCT
    a.id as customer_a_id,
    SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1) as customer_b_id
FROM gh_customer a
CROSS JOIN (
    SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) numbers
WHERE a.del_flag = 0
  AND a.related_company_id IS NOT NULL
  AND a.related_company_id != ''
  AND CHAR_LENGTH(a.related_company_id) - CHAR_LENGTH(REPLACE(a.related_company_id, ',', '')) >= numbers.n - 1
  AND EXISTS (
      SELECT 1 FROM gh_customer b 
      WHERE b.id = SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1)
        AND b.del_flag = 0
        AND (b.related_company_id IS NULL 
             OR b.related_company_id = '' 
             OR FIND_IN_SET(a.id, b.related_company_id) = 0)
  );

-- 查看需要修复的记录数
SELECT COUNT(*) as need_fix_count FROM temp_fix_relations;

-- 执行修复（为B添加对A的关联）
-- 注意: 此操作会修改数据，请确保已备份
UPDATE gh_customer b
JOIN temp_fix_relations t ON b.id = t.customer_b_id
SET b.related_company_id = CASE
    WHEN b.related_company_id IS NULL OR b.related_company_id = '' THEN t.customer_a_id
    WHEN FIND_IN_SET(t.customer_a_id, b.related_company_id) = 0 THEN CONCAT(b.related_company_id, ',', t.customer_a_id)
    ELSE b.related_company_id
END,
b.update_time = NOW()
WHERE b.del_flag = 0;

-- 验证修复结果
SELECT 
    '修复后检查' as check_type,
    COUNT(*) as remaining_issues
FROM (
    SELECT 
        a.id as customer_a_id,
        SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1) as related_id
    FROM gh_customer a
    CROSS JOIN (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers
    WHERE a.del_flag = 0
      AND a.related_company_id IS NOT NULL
      AND a.related_company_id != ''
      AND CHAR_LENGTH(a.related_company_id) - CHAR_LENGTH(REPLACE(a.related_company_id, ',', '')) >= numbers.n - 1
) a_relations
JOIN gh_customer b ON b.id = a_relations.related_id AND b.del_flag = 0
WHERE (b.related_company_id IS NULL 
       OR b.related_company_id = '' 
       OR FIND_IN_SET(a_relations.customer_a_id, b.related_company_id) = 0);

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_fix_relations;

-- =====================================================
-- 方案2: 清理无效的关联ID
-- =====================================================
-- 说明: 从关联列表中移除已删除或不存在的客户ID
-- 注意: 此操作会修改数据，请确保已备份

-- 创建临时表存储清理后的关联ID
DROP TEMPORARY TABLE IF EXISTS temp_clean_relations;
CREATE TEMPORARY TABLE temp_clean_relations (
    customer_id VARCHAR(32),
    old_related_ids TEXT,
    new_related_ids TEXT,
    PRIMARY KEY (customer_id)
);

-- 计算清理后的关联ID列表
INSERT INTO temp_clean_relations (customer_id, old_related_ids, new_related_ids)
SELECT 
    c.id,
    c.related_company_id as old_related_ids,
    GROUP_CONCAT(valid_ids.related_id SEPARATOR ',') as new_related_ids
FROM gh_customer c
CROSS JOIN (
    SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) numbers
JOIN (
    SELECT 
        c2.id as customer_id,
        SUBSTRING_INDEX(SUBSTRING_INDEX(c2.related_company_id, ',', numbers2.n), ',', -1) as related_id
    FROM gh_customer c2
    CROSS JOIN (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers2
    WHERE c2.del_flag = 0
      AND c2.related_company_id IS NOT NULL
      AND c2.related_company_id != ''
      AND CHAR_LENGTH(c2.related_company_id) - CHAR_LENGTH(REPLACE(c2.related_company_id, ',', '')) >= numbers2.n - 1
) valid_ids ON valid_ids.customer_id = c.id
WHERE c.del_flag = 0
  AND c.related_company_id IS NOT NULL
  AND c.related_company_id != ''
  AND EXISTS (
      SELECT 1 FROM gh_customer c3 
      WHERE c3.id = valid_ids.related_id 
        AND c3.del_flag = 0
        AND c3.id != c.id  -- 排除自己关联自己
  )
GROUP BY c.id, c.related_company_id;

-- 查看需要清理的记录
SELECT * FROM temp_clean_relations
WHERE old_related_ids != new_related_ids OR new_related_ids IS NULL;

-- 执行清理（更新为有效的关联ID列表）
-- 注意: 此操作会修改数据，请确保已备份
UPDATE gh_customer c
JOIN temp_clean_relations t ON c.id = t.customer_id
SET c.related_company_id = IFNULL(t.new_related_ids, ''),
    c.update_time = NOW()
WHERE t.old_related_ids != IFNULL(t.new_related_ids, '');

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_clean_relations;

-- =====================================================
-- 方案3: 清理自己关联自己的异常数据
-- =====================================================
-- 说明: 从关联列表中移除自己的ID
-- 注意: 此操作会修改数据，请确保已备份

UPDATE gh_customer c
SET c.related_company_id = TRIM(BOTH ',' FROM 
    REPLACE(CONCAT(',', c.related_company_id, ','), CONCAT(',', c.id, ','), ',')
),
c.update_time = NOW()
WHERE c.del_flag = 0
  AND c.related_company_id IS NOT NULL
  AND c.related_company_id != ''
  AND FIND_IN_SET(c.id, c.related_company_id) > 0;

-- 清理可能产生的空值或多余逗号
UPDATE gh_customer
SET related_company_id = ''
WHERE del_flag = 0
  AND (related_company_id = ',' OR related_company_id IS NULL);

-- =====================================================
-- 验证脚本
-- =====================================================

-- 验证1: 检查是否还有不对称关联
SELECT 
    '不对称关联' as check_item,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END as status
FROM (
    SELECT 
        a.id as customer_a_id,
        SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1) as related_id
    FROM gh_customer a
    CROSS JOIN (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers
    WHERE a.del_flag = 0
      AND a.related_company_id IS NOT NULL
      AND a.related_company_id != ''
      AND CHAR_LENGTH(a.related_company_id) - CHAR_LENGTH(REPLACE(a.related_company_id, ',', '')) >= numbers.n - 1
) a_relations
JOIN gh_customer b ON b.id = a_relations.related_id AND b.del_flag = 0
WHERE (b.related_company_id IS NULL 
       OR b.related_company_id = '' 
       OR FIND_IN_SET(a_relations.customer_a_id, b.related_company_id) = 0);

-- 验证2: 检查是否还有无效ID
SELECT 
    '无效关联ID' as check_item,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END as status
FROM gh_customer c
WHERE c.del_flag = 0
  AND c.related_company_id IS NOT NULL
  AND c.related_company_id != ''
  AND EXISTS (
    SELECT 1
    FROM (
        SELECT 
            SUBSTRING_INDEX(SUBSTRING_INDEX(c.related_company_id, ',', numbers.n), ',', -1) as related_id
        FROM (
            SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
        ) numbers
        WHERE CHAR_LENGTH(c.related_company_id) - CHAR_LENGTH(REPLACE(c.related_company_id, ',', '')) >= numbers.n - 1
    ) ids
    WHERE NOT EXISTS (
        SELECT 1 FROM gh_customer c2 
        WHERE c2.id = ids.related_id AND c2.del_flag = 0
    )
  );

-- 验证3: 检查是否还有自己关联自己
SELECT 
    '自己关联自己' as check_item,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END as status
FROM gh_customer
WHERE del_flag = 0
  AND related_company_id IS NOT NULL
  AND related_company_id != ''
  AND FIND_IN_SET(id, related_company_id) > 0;

-- =====================================================
-- 统计信息
-- =====================================================

-- 统计有关联关系的客户数量
SELECT 
    '有关联关系的客户' as stat_item,
    COUNT(*) as count
FROM gh_customer
WHERE del_flag = 0
  AND related_company_id IS NOT NULL
  AND related_company_id != '';

-- 统计关联关系总数（单向计数）
SELECT 
    '关联关系总数' as stat_item,
    COUNT(*) as count
FROM (
    SELECT 
        a.id,
        SUBSTRING_INDEX(SUBSTRING_INDEX(a.related_company_id, ',', numbers.n), ',', -1) as related_id
    FROM gh_customer a
    CROSS JOIN (
        SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
        UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    ) numbers
    WHERE a.del_flag = 0
      AND a.related_company_id IS NOT NULL
      AND a.related_company_id != ''
      AND CHAR_LENGTH(a.related_company_id) - CHAR_LENGTH(REPLACE(a.related_company_id, ',', '')) >= numbers.n - 1
) relations;

-- 统计平均关联数量
SELECT 
    '平均关联数量' as stat_item,
    AVG(relation_count) as avg_count
FROM (
    SELECT 
        id,
        CHAR_LENGTH(related_company_id) - CHAR_LENGTH(REPLACE(related_company_id, ',', '')) + 1 as relation_count
    FROM gh_customer
    WHERE del_flag = 0
      AND related_company_id IS NOT NULL
      AND related_company_id != ''
) counts;

-- =====================================================
-- 执行说明
-- =====================================================
-- 1. 首先执行查询脚本（1-4），了解当前数据状况
-- 2. 根据实际情况选择执行修复方案：
--    - 方案1: 建立双向关联（推荐，保留所有关联关系）
--    - 方案2: 清理无效ID（清理已删除的客户）
--    - 方案3: 清理自己关联自己（修复异常数据）
-- 3. 执行修复后，运行验证脚本确认修复效果
-- 4. 查看统计信息，了解修复后的数据状况
-- =====================================================
