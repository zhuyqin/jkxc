-- ============================================
-- 客户名称同步更新 - 日常维护脚本
-- ============================================
-- 用途：用于日常维护和数据一致性检查
-- 执行频率：建议每周或每月执行一次
-- ============================================

-- ============================================
-- 1. 数据一致性详细检查
-- ============================================

-- 1.1 检查订单表中公司名称不一致的记录（详细信息）
SELECT 
    '订单表(gh_order)' AS table_name,
    o.id AS record_id,
    o.order_no AS order_no,
    o.company_name AS current_name,
    c.corporate_name AS correct_name,
    o.create_time AS create_time
FROM `gh_order` o
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE o.company_name != c.corporate_name
  AND o.del_flag = 0
  AND c.del_flag = 0
ORDER BY o.create_time DESC
LIMIT 100;

-- 1.2 检查代账合同表中公司名称不一致的记录
SELECT 
    '代账合同表(gh_accounting_contract)' AS table_name,
    ac.id AS record_id,
    ac.contract_no AS contract_no,
    ac.company_name AS current_name,
    c.corporate_name AS correct_name,
    ac.create_time AS create_time
FROM `gh_accounting_contract` ac
INNER JOIN `gh_order` o ON ac.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0
ORDER BY ac.create_time DESC
LIMIT 100;

-- 1.3 检查代账交接表中公司名称不一致的记录
SELECT 
    '代账交接表(gh_accounting_handover)' AS table_name,
    ah.id AS record_id,
    ah.order_no AS order_no,
    ah.company_name AS current_name,
    c.corporate_name AS correct_name,
    ah.create_time AS create_time
FROM `gh_accounting_handover` ah
INNER JOIN `gh_order` o ON ah.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE ah.company_name != c.corporate_name
  AND ah.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0
ORDER BY ah.create_time DESC
LIMIT 100;

-- 1.4 检查业务任务表中公司名称不一致的记录
SELECT 
    '业务任务表(gh_business_task)' AS table_name,
    bt.id AS record_id,
    bt.order_no AS order_no,
    bt.company_name AS current_name,
    c.corporate_name AS correct_name,
    bt.create_time AS create_time
FROM `gh_business_task` bt
INNER JOIN `gh_order` o ON bt.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE bt.company_name != c.corporate_name
  AND bt.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0
ORDER BY bt.create_time DESC
LIMIT 100;

-- 1.5 检查地址中心表中公司名称不一致的记录
SELECT 
    '地址中心表(gh_address_center)' AS table_name,
    ac.id AS record_id,
    ac.company_name AS current_name,
    c.corporate_name AS correct_name,
    ac.create_time AS create_time
FROM `gh_address_center` ac
INNER JOIN `gh_customer` c ON ac.data_id = c.id
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND c.del_flag = 0
ORDER BY ac.create_time DESC
LIMIT 100;

-- 1.6 检查地址续费表中公司名称不一致的记录
SELECT 
    '地址续费表(gh_address_renew)' AS table_name,
    ar.id AS record_id,
    ar.company_name AS current_name,
    c.corporate_name AS correct_name,
    ar.create_time AS create_time
FROM `gh_address_renew` ar
INNER JOIN `gh_customer` c ON ar.company_id = c.id
WHERE ar.company_name != c.corporate_name
  AND c.del_flag = 0
ORDER BY ar.create_time DESC
LIMIT 100;

-- 1.7 检查银行日记账表中公司名称不一致的记录
SELECT 
    '银行日记账表(gh_bank_diary)' AS table_name,
    bd.id AS record_id,
    bd.company_name AS current_name,
    c.corporate_name AS correct_name,
    bd.create_time AS create_time
FROM `gh_bank_diary` bd
INNER JOIN `gh_customer` c ON bd.company_id = c.id
WHERE bd.company_name != c.corporate_name
  AND bd.del_flag = 0
  AND c.del_flag = 0
ORDER BY bd.create_time DESC
LIMIT 100;

-- 1.8 检查报销表中公司名称不一致的记录
SELECT 
    '报销表(gh_reimbursement)' AS table_name,
    r.id AS record_id,
    r.company_name AS current_name,
    c.corporate_name AS correct_name,
    r.create_time AS create_time
FROM `gh_reimbursement` r
INNER JOIN `gh_customer` c ON r.company_id = c.id
WHERE r.company_name != c.corporate_name
  AND r.del_flag = 0
  AND c.del_flag = 0
ORDER BY r.create_time DESC
LIMIT 100;

-- 1.9 检查商机表中公司名称不一致的记录
SELECT 
    '商机表(gh_opportunity)' AS table_name,
    opp.id AS record_id,
    opp.opportunity_no AS opportunity_no,
    opp.corporate_name AS current_name,
    c.corporate_name AS correct_name,
    opp.create_time AS create_time
FROM `gh_opportunity` opp
INNER JOIN `gh_customer_relation` cr ON opp.id = cr.opportunity_id
INNER JOIN `gh_customer` c ON cr.customer_id = c.id
WHERE opp.corporate_name != c.corporate_name
  AND opp.del_flag = 0
  AND c.del_flag = 0
ORDER BY opp.create_time DESC
LIMIT 100;

-- ============================================
-- 2. 统计汇总报告
-- ============================================

SELECT 
    '数据一致性统计报告' AS report_title,
    NOW() AS check_time;

SELECT 
    '订单表(gh_order)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过company_id关联' AS relation_type
FROM `gh_order` o
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE o.company_name != c.corporate_name
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '代账合同表(gh_accounting_contract)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过order_id关联' AS relation_type
FROM `gh_accounting_contract` ac
INNER JOIN `gh_order` o ON ac.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '代账交接表(gh_accounting_handover)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过order_id关联' AS relation_type
FROM `gh_accounting_handover` ah
INNER JOIN `gh_order` o ON ah.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE ah.company_name != c.corporate_name
  AND ah.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '业务任务表(gh_business_task)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过order_id关联' AS relation_type
FROM `gh_business_task` bt
INNER JOIN `gh_order` o ON bt.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE bt.company_name != c.corporate_name
  AND bt.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '地址中心表(gh_address_center)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过data_id关联' AS relation_type
FROM `gh_address_center` ac
INNER JOIN `gh_customer` c ON ac.data_id = c.id
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '地址续费表(gh_address_renew)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过company_id关联' AS relation_type
FROM `gh_address_renew` ar
INNER JOIN `gh_customer` c ON ar.company_id = c.id
WHERE ar.company_name != c.corporate_name
  AND c.del_flag = 0

UNION ALL

SELECT 
    '银行日记账表(gh_bank_diary)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过company_id关联' AS relation_type
FROM `gh_bank_diary` bd
INNER JOIN `gh_customer` c ON bd.company_id = c.id
WHERE bd.company_name != c.corporate_name
  AND bd.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '报销表(gh_reimbursement)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过company_id关联' AS relation_type
FROM `gh_reimbursement` r
INNER JOIN `gh_customer` c ON r.company_id = c.id
WHERE r.company_name != c.corporate_name
  AND r.del_flag = 0
  AND c.del_flag = 0

UNION ALL

SELECT 
    '商机表(gh_opportunity)' AS table_name,
    COUNT(*) AS inconsistent_count,
    '通过gh_customer_relation关联' AS relation_type
FROM `gh_opportunity` opp
INNER JOIN `gh_customer_relation` cr ON opp.id = cr.opportunity_id
INNER JOIN `gh_customer` c ON cr.customer_id = c.id
WHERE opp.corporate_name != c.corporate_name
  AND opp.del_flag = 0
  AND c.del_flag = 0;

-- ============================================
-- 3. 手动修复单个客户的数据
-- ============================================
-- 使用说明：将 'YOUR_CUSTOMER_ID' 替换为实际的客户ID

/*
-- 示例：修复指定客户的所有关联数据
SET @customer_id = 'YOUR_CUSTOMER_ID';
SET @new_name = (SELECT corporate_name FROM gh_customer WHERE id = @customer_id);

-- 更新订单表
UPDATE gh_order 
SET company_name = @new_name, update_time = NOW(), update_by = 'manual_fix'
WHERE company_id = @customer_id AND del_flag = 0;

-- 更新代账合同表
UPDATE gh_accounting_contract ac
INNER JOIN gh_order o ON ac.order_id = o.id
SET ac.company_name = @new_name, ac.update_time = NOW(), ac.update_by = 'manual_fix'
WHERE o.company_id = @customer_id AND ac.del_flag = 0;

-- 更新代账交接表
UPDATE gh_accounting_handover ah
INNER JOIN gh_order o ON ah.order_id = o.id
SET ah.company_name = @new_name, ah.update_time = NOW(), ah.update_by = 'manual_fix'
WHERE o.company_id = @customer_id AND ah.del_flag = 0;

-- 更新业务任务表
UPDATE gh_business_task bt
INNER JOIN gh_order o ON bt.order_id = o.id
SET bt.company_name = @new_name, bt.update_time = NOW(), bt.update_by = 'manual_fix'
WHERE o.company_id = @customer_id AND bt.del_flag = 0;

-- 更新地址中心表
UPDATE gh_address_center
SET company_name = @new_name, update_time = NOW(), update_by = 'manual_fix'
WHERE data_id = @customer_id AND del_flag = 0;

-- 更新地址续费表
UPDATE gh_address_renew
SET company_name = @new_name
WHERE company_id = @customer_id;

-- 更新银行日记账表
UPDATE gh_bank_diary
SET company_name = @new_name, update_time = NOW(), update_by = 'manual_fix'
WHERE company_id = @customer_id AND del_flag = 0;

-- 更新报销表
UPDATE gh_reimbursement
SET company_name = @new_name, update_time = NOW(), update_by = 'manual_fix'
WHERE company_id = @customer_id AND del_flag = 0;

-- 更新商机表
UPDATE gh_opportunity opp
INNER JOIN gh_customer_relation cr ON opp.id = cr.opportunity_id
SET opp.corporate_name = @new_name, opp.update_time = NOW(), opp.update_by = 'manual_fix'
WHERE cr.customer_id = @customer_id AND opp.del_flag = 0;

SELECT CONCAT('客户 ', @customer_id, ' 的数据已修复完成') AS result;
*/

-- ============================================
-- 4. 查看触发器状态
-- ============================================

SHOW TRIGGERS WHERE `Trigger` = 'trg_customer_name_update';

-- ============================================
-- 5. 性能监控查询
-- ============================================

-- 5.1 查看各表的数据量
SELECT 
    'gh_customer' AS table_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN del_flag = 0 THEN 1 ELSE 0 END) AS active_count
FROM gh_customer

UNION ALL

SELECT 
    'gh_order' AS table_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN del_flag = 0 THEN 1 ELSE 0 END) AS active_count
FROM gh_order

UNION ALL

SELECT 
    'gh_accounting_contract' AS table_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN del_flag = 0 THEN 1 ELSE 0 END) AS active_count
FROM gh_accounting_contract

UNION ALL

SELECT 
    'gh_address_center' AS table_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN del_flag = 0 THEN 1 ELSE 0 END) AS active_count
FROM gh_address_center

UNION ALL

SELECT 
    'gh_bank_diary' AS table_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN del_flag = 0 THEN 1 ELSE 0 END) AS active_count
FROM gh_bank_diary

UNION ALL

SELECT 
    'gh_reimbursement' AS table_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN del_flag = 0 THEN 1 ELSE 0 END) AS active_count
FROM gh_reimbursement;

-- 5.2 查看最近修改的客户名称（可能触发了触发器）
SELECT 
    id,
    corporate_name,
    update_by,
    update_time,
    create_time
FROM gh_customer
WHERE del_flag = 0
  AND update_time IS NOT NULL
ORDER BY update_time DESC
LIMIT 20;

-- ============================================
-- 6. 索引检查
-- ============================================

-- 检查关键索引是否存在
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN (
    'gh_customer',
    'gh_order',
    'gh_accounting_contract',
    'gh_accounting_handover',
    'gh_business_task',
    'gh_address_center',
    'gh_address_renew',
    'gh_bank_diary',
    'gh_reimbursement',
    'gh_opportunity'
  )
  AND COLUMN_NAME IN ('company_id', 'data_id', 'order_id', 'company_name', 'corporate_name')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 使用说明
-- ============================================
/*
1. 日常检查流程：
   - 执行"第1部分"查看详细的不一致记录
   - 执行"第2部分"查看统计汇总
   - 如果发现不一致，执行 sync_customer_name_update.sql 中的修复脚本

2. 单个客户修复：
   - 使用"第3部分"的脚本，替换客户ID后执行

3. 性能监控：
   - 定期执行"第5部分"查看数据量和最近更新情况
   - 如果数据量很大，考虑优化触发器或改用异步更新

4. 索引检查：
   - 执行"第6部分"确保所有关键字段都有索引
   - 如果缺少索引，会严重影响触发器性能

5. 建议执行频率：
   - 数据一致性检查：每周一次
   - 性能监控：每月一次
   - 索引检查：每季度一次或数据库升级后
*/
