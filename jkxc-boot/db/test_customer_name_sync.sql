-- ============================================
-- 客户名称同步更新 - 测试脚本
-- ============================================
-- 用途：在测试环境验证触发器是否正常工作
-- 注意：此脚本会创建测试数据，请勿在生产环境执行！
-- ============================================

-- ============================================
-- 第一部分：准备测试数据
-- ============================================

-- 1. 创建测试客户
INSERT INTO `gh_customer` (
    `id`,
    `corporate_name`,
    `contacts`,
    `contact_information`,
    `region`,
    `create_by`,
    `create_time`,
    `del_flag`
) VALUES (
    'TEST_CUSTOMER_001',
    '测试公司A',
    '张三',
    '13800138000',
    '北京市',
    'test_user',
    NOW(),
    0
);

-- 2. 创建测试订单
INSERT INTO `gh_order` (
    `id`,
    `order_no`,
    `company_id`,
    `company_name`,
    `salesman`,
    `business_type`,
    `create_by`,
    `create_time`,
    `del_flag`
) VALUES (
    'TEST_ORDER_001',
    'ORD-TEST-001',
    'TEST_CUSTOMER_001',
    '测试公司A',
    '李四',
    '代账服务',
    'test_user',
    NOW(),
    0
);

-- 3. 创建测试代账合同
INSERT INTO `gh_accounting_contract` (
    `id`,
    `contract_no`,
    `order_id`,
    `order_no`,
    `company_name`,
    `contract_amount`,
    `sign_date`,
    `create_by`,
    `create_time`,
    `del_flag`
) VALUES (
    'TEST_CONTRACT_001',
    'CON-TEST-001',
    'TEST_ORDER_001',
    'ORD-TEST-001',
    '测试公司A',
    10000.00,
    CURDATE(),
    'test_user',
    NOW(),
    0
);

-- 4. 创建测试地址中心记录
INSERT INTO `gh_address_center` (
    `id`,
    `company_name`,
    `data_id`,
    `salesman`,
    `create_time`,
    `del_flag`
) VALUES (
    'TEST_ADDRESS_001',
    '测试公司A',
    'TEST_CUSTOMER_001',
    '王五',
    NOW(),
    0
);

-- 5. 创建测试银行日记账
INSERT INTO `gh_bank_diary` (
    `id`,
    `company_id`,
    `company_name`,
    `transaction_date`,
    `amount`,
    `create_by`,
    `create_time`,
    `del_flag`
) VALUES (
    'TEST_DIARY_001',
    'TEST_CUSTOMER_001',
    '测试公司A',
    CURDATE(),
    5000.00,
    'test_user',
    NOW(),
    0
);

-- 显示初始数据
SELECT '=== 初始测试数据 ===' AS info;

SELECT 
    'gh_customer' AS table_name,
    id,
    corporate_name AS name
FROM gh_customer 
WHERE id = 'TEST_CUSTOMER_001'

UNION ALL

SELECT 
    'gh_order' AS table_name,
    id,
    company_name AS name
FROM gh_order 
WHERE id = 'TEST_ORDER_001'

UNION ALL

SELECT 
    'gh_accounting_contract' AS table_name,
    id,
    company_name AS name
FROM gh_accounting_contract 
WHERE id = 'TEST_CONTRACT_001'

UNION ALL

SELECT 
    'gh_address_center' AS table_name,
    id,
    company_name AS name
FROM gh_address_center 
WHERE id = 'TEST_ADDRESS_001'

UNION ALL

SELECT 
    'gh_bank_diary' AS table_name,
    id,
    company_name AS name
FROM gh_bank_diary 
WHERE id = 'TEST_DIARY_001';

-- ============================================
-- 第二部分：测试触发器
-- ============================================

SELECT '=== 开始测试触发器 ===' AS info;

-- 修改客户名称
UPDATE `gh_customer`
SET `corporate_name` = '测试公司A（已改名）',
    `update_by` = 'test_trigger',
    `update_time` = NOW()
WHERE `id` = 'TEST_CUSTOMER_001';

-- 等待1秒（确保触发器执行完成）
SELECT SLEEP(1);

-- 显示更新后的数据
SELECT '=== 触发器执行后的数据 ===' AS info;

SELECT 
    'gh_customer' AS table_name,
    id,
    corporate_name AS name,
    '主表' AS note
FROM gh_customer 
WHERE id = 'TEST_CUSTOMER_001'

UNION ALL

SELECT 
    'gh_order' AS table_name,
    id,
    company_name AS name,
    CASE 
        WHEN company_name = '测试公司A（已改名）' THEN '✓ 已同步'
        ELSE '✗ 未同步'
    END AS note
FROM gh_order 
WHERE id = 'TEST_ORDER_001'

UNION ALL

SELECT 
    'gh_accounting_contract' AS table_name,
    id,
    company_name AS name,
    CASE 
        WHEN company_name = '测试公司A（已改名）' THEN '✓ 已同步'
        ELSE '✗ 未同步'
    END AS note
FROM gh_accounting_contract 
WHERE id = 'TEST_CONTRACT_001'

UNION ALL

SELECT 
    'gh_address_center' AS table_name,
    id,
    company_name AS name,
    CASE 
        WHEN company_name = '测试公司A（已改名）' THEN '✓ 已同步'
        ELSE '✗ 未同步'
    END AS note
FROM gh_address_center 
WHERE id = 'TEST_ADDRESS_001'

UNION ALL

SELECT 
    'gh_bank_diary' AS table_name,
    id,
    company_name AS name,
    CASE 
        WHEN company_name = '测试公司A（已改名）' THEN '✓ 已同步'
        ELSE '✗ 未同步'
    END AS note
FROM gh_bank_diary 
WHERE id = 'TEST_DIARY_001';

-- ============================================
-- 第三部分：验证结果
-- ============================================

SELECT '=== 测试结果验证 ===' AS info;

-- 统计同步成功的表数量
SELECT 
    COUNT(*) AS synced_tables,
    CASE 
        WHEN COUNT(*) = 4 THEN '✓ 测试通过：所有表都已同步'
        ELSE '✗ 测试失败：部分表未同步'
    END AS test_result
FROM (
    SELECT 1 FROM gh_order 
    WHERE id = 'TEST_ORDER_001' 
    AND company_name = '测试公司A（已改名）'
    
    UNION ALL
    
    SELECT 1 FROM gh_accounting_contract 
    WHERE id = 'TEST_CONTRACT_001' 
    AND company_name = '测试公司A（已改名）'
    
    UNION ALL
    
    SELECT 1 FROM gh_address_center 
    WHERE id = 'TEST_ADDRESS_001' 
    AND company_name = '测试公司A（已改名）'
    
    UNION ALL
    
    SELECT 1 FROM gh_bank_diary 
    WHERE id = 'TEST_DIARY_001' 
    AND company_name = '测试公司A（已改名）'
) AS synced;

-- ============================================
-- 第四部分：清理测试数据
-- ============================================

SELECT '=== 清理测试数据 ===' AS info;

-- 删除测试数据（按依赖关系倒序删除）
DELETE FROM `gh_bank_diary` WHERE id = 'TEST_DIARY_001';
DELETE FROM `gh_address_center` WHERE id = 'TEST_ADDRESS_001';
DELETE FROM `gh_accounting_contract` WHERE id = 'TEST_CONTRACT_001';
DELETE FROM `gh_order` WHERE id = 'TEST_ORDER_001';
DELETE FROM `gh_customer` WHERE id = 'TEST_CUSTOMER_001';

SELECT '测试数据已清理' AS info;

-- ============================================
-- 第五部分：性能测试（可选）
-- ============================================

/*
-- 如果需要测试性能，取消下面的注释

SELECT '=== 性能测试开始 ===' AS info;

-- 创建100个测试客户和相关数据
DELIMITER $$

CREATE PROCEDURE test_performance()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE customer_id VARCHAR(32);
    DECLARE order_id VARCHAR(32);
    
    WHILE i <= 100 DO
        SET customer_id = CONCAT('PERF_CUSTOMER_', LPAD(i, 3, '0'));
        SET order_id = CONCAT('PERF_ORDER_', LPAD(i, 3, '0'));
        
        -- 创建客户
        INSERT INTO gh_customer (
            id, corporate_name, contacts, contact_information,
            create_by, create_time, del_flag
        ) VALUES (
            customer_id,
            CONCAT('性能测试公司', i),
            '测试',
            '13800138000',
            'perf_test',
            NOW(),
            0
        );
        
        -- 创建订单
        INSERT INTO gh_order (
            id, order_no, company_id, company_name,
            salesman, create_by, create_time, del_flag
        ) VALUES (
            order_id,
            CONCAT('PERF-ORD-', LPAD(i, 3, '0')),
            customer_id,
            CONCAT('性能测试公司', i),
            '测试员',
            'perf_test',
            NOW(),
            0
        );
        
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- 执行性能测试
CALL test_performance();

-- 测试批量更新性能
SET @start_time = NOW(6);

UPDATE gh_customer
SET corporate_name = CONCAT(corporate_name, '（已更新）'),
    update_time = NOW()
WHERE id LIKE 'PERF_CUSTOMER_%';

SET @end_time = NOW(6);

SELECT 
    '性能测试结果' AS info,
    TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) / 1000 AS execution_time_ms,
    100 AS updated_customers,
    100 AS updated_orders;

-- 清理性能测试数据
DELETE FROM gh_order WHERE id LIKE 'PERF_ORDER_%';
DELETE FROM gh_customer WHERE id LIKE 'PERF_CUSTOMER_%';
DROP PROCEDURE IF EXISTS test_performance;

SELECT '性能测试数据已清理' AS info;
*/

-- ============================================
-- 测试完成
-- ============================================

SELECT '=== 测试完成 ===' AS info;
SELECT '如果所有表都显示"✓ 已同步"，说明触发器工作正常' AS note;

-- ============================================
-- 使用说明
-- ============================================
/*
1. 执行方式：
   mysql -u root -p jkxc-cg < test_customer_name_sync.sql

2. 预期结果：
   - 初始数据：所有表的公司名称都是"测试公司A"
   - 更新后：所有表的公司名称都变成"测试公司A（已改名）"
   - 验证结果：显示"✓ 测试通过：所有表都已同步"

3. 如果测试失败：
   - 检查触发器是否创建成功：SHOW TRIGGERS;
   - 查看MySQL错误日志
   - 检查表结构是否正确
   - 确认字段名称和关联关系

4. 性能测试：
   - 取消"第五部分"的注释
   - 会创建100个测试客户和订单
   - 测量批量更新的执行时间
   - 自动清理测试数据

5. 注意事项：
   - 此脚本仅用于测试环境
   - 会自动清理测试数据
   - 如果中途失败，可能需要手动清理
*/
