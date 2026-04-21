-- ============================================
-- 任务分析：表格统计 vs 弹窗查询 SQL对比
-- ============================================
-- 参数说明：
-- @year: 年份，例如 2026
-- @month: 月份，例如 1
-- @salesman: 业务员，例如 '巴冰雪'
-- @businessType: 业务类型（可选），例如 'dljz'
-- @taskType: 任务类型（可选），例如 'recurring'
-- ============================================

-- ============================================
-- 1. 表格统计SQL（getTaskSalesmanMonthlyDetail）
-- 统计逻辑：遍历所有审核任务，按订单创建时间分组，每个订单只统计一次
-- ============================================

-- 步骤1：查询审核任务（当年）
SELECT 
    t.id AS task_id,
    t.order_id,
    t.task_type,
    t.create_time AS task_create_time
FROM sys_audit_task t
WHERE t.del_flag = 0
  AND t.create_time >= CONCAT(@year, '-01-01 00:00:00')
  AND t.create_time <= CONCAT(@year, '-12-31 23:59:59')
  AND (@taskType IS NULL OR t.task_type = @taskType)
  AND t.order_id IS NOT NULL
  AND t.order_id != '';

-- 步骤2：查询订单（批量查询，通过order_id关联）
SELECT 
    o.id AS order_id,
    o.order_no,
    o.company_name,
    o.salesman,
    o.business_type,
    o.create_time AS order_create_time,
    o.del_flag,
    COALESCE(o.order_amount, o.contract_amount, 0) AS amount
FROM gh_order o
WHERE o.id IN (
    SELECT DISTINCT t.order_id 
    FROM sys_audit_task t
    WHERE t.del_flag = 0
      AND t.create_time >= CONCAT(@year, '-01-01 00:00:00')
      AND t.create_time <= CONCAT(@year, '-12-31 23:59:59')
      AND (@taskType IS NULL OR t.task_type = @taskType)
      AND t.order_id IS NOT NULL
      AND t.order_id != ''
);

-- 步骤3：统计查询（表格统计的最终SQL等价查询）
-- 注意：这个查询模拟了Java代码的逻辑，按订单创建时间分组，每个订单只统计一次
SELECT 
    o.salesman,
    MONTH(o.create_time) AS order_month,
    COUNT(DISTINCT o.id) AS order_count,
    SUM(COALESCE(o.order_amount, o.contract_amount, 0)) AS total_amount
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag = 0
  AND t.create_time >= CONCAT(@year, '-01-01 00:00:00')
  AND t.create_time <= CONCAT(@year, '-12-31 23:59:59')
  AND YEAR(o.create_time) = @year  -- 只统计订单创建时间在当年的订单
  AND (@taskType IS NULL OR t.task_type = @taskType)
  AND (@businessType IS NULL OR o.business_type = @businessType)
  AND t.order_id IS NOT NULL
  AND t.order_id != ''
  AND o.salesman = @salesman  -- 指定业务员
  AND MONTH(o.create_time) = @month  -- 指定月份
GROUP BY o.salesman, MONTH(o.create_time)
ORDER BY o.salesman, order_month;

-- ============================================
-- 2. 弹窗查询SQL（getTaskSalesmanMonthlyTasks）
-- 查询逻辑：与表格统计完全一致，遍历所有审核任务，按订单创建时间分组
-- ============================================

-- 弹窗查询的最终SQL（与表格统计逻辑一致）
SELECT DISTINCT
    o.id AS order_id,
    o.order_no,
    o.company_name,
    o.business_type,
    o.salesman,
    o.create_time AS order_create_time,
    COALESCE(o.order_amount, o.contract_amount, 0) AS cost_amount,
    COALESCE(o.audit_status, 'pending') AS task_status
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag = 0
  AND t.create_time >= CONCAT(@year, '-01-01 00:00:00')
  AND t.create_time <= CONCAT(@year, '-12-31 23:59:59')
  AND YEAR(o.create_time) = @year  -- 只统计订单创建时间在当年的订单
  AND MONTH(o.create_time) = @month  -- 指定月份
  AND o.salesman = @salesman  -- 指定业务员
  AND (@taskType IS NULL OR t.task_type = @taskType)
  AND (@businessType IS NULL OR o.business_type = @businessType)
  AND t.order_id IS NOT NULL
  AND t.order_id != ''
ORDER BY o.create_time DESC
LIMIT @pageSize OFFSET ((@pageNo - 1) * @pageSize);

-- ============================================
-- 3. 统计总数SQL（用于弹窗分页）
-- ============================================
SELECT COUNT(DISTINCT o.id) AS total_count
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag = 0
  AND t.create_time >= CONCAT(@year, '-01-01 00:00:00')
  AND t.create_time <= CONCAT(@year, '-12-31 23:59:59')
  AND YEAR(o.create_time) = @year
  AND MONTH(o.create_time) = @month
  AND o.salesman = @salesman
  AND (@taskType IS NULL OR t.task_type = @taskType)
  AND (@businessType IS NULL OR o.business_type = @businessType)
  AND t.order_id IS NOT NULL
  AND t.order_id != '';

-- ============================================
-- 4. 实际使用示例（替换参数）
-- ============================================
-- 示例：查询巴冰雪在2026年1月的周期任务
-- SET @year = 2026;
-- SET @month = 1;
-- SET @salesman = '巴冰雪';
-- SET @businessType = NULL;  -- 或者 'dljz'
-- SET @taskType = 'recurring';
-- SET @pageNo = 1;
-- SET @pageSize = 10;

-- 执行统计查询（应该返回78）
SELECT 
    o.salesman,
    MONTH(o.create_time) AS order_month,
    COUNT(DISTINCT o.id) AS order_count
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag = 0
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != ''
  AND o.salesman = '巴冰雪'
  AND MONTH(o.create_time) = 1
GROUP BY o.salesman, MONTH(o.create_time);

-- 执行弹窗查询总数（应该也返回78）
SELECT COUNT(DISTINCT o.id) AS total_count
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag = 0
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND MONTH(o.create_time) = 1
  AND o.salesman = '巴冰雪'
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != '';

-- ============================================
-- 5. 调试查询：查看差异订单
-- ============================================
-- 这个查询可以帮助找出为什么统计是78但查询是76
-- 可能的原因：
-- 1. 某些订单在统计时被计入，但在查询时被过滤掉（del_flag=1的订单）
-- 2. 某些订单有多个审核任务，导致去重逻辑不一致
-- 3. 订单的del_flag或salesman字段在统计和查询时不一致

-- 查看所有符合条件的订单（包括已删除的，不去重）
SELECT 
    t.id AS task_id,
    t.order_id,
    o.id AS order_id_check,
    o.order_no,
    o.salesman,
    o.business_type,
    o.create_time AS order_create_time,
    o.del_flag AS order_del_flag,
    t.create_time AS task_create_time,
    t.task_type,
    t.del_flag AS task_del_flag
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND MONTH(o.create_time) = 1
  AND o.salesman = '巴冰雪'
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != ''
ORDER BY o.create_time DESC;

-- 查看已删除的订单（这些订单会被listByIds返回，但SQL查询会过滤掉）
-- 这个查询应该返回2条（78-76=2），这些就是差异订单
SELECT 
    t.id AS task_id,
    t.order_id,
    o.id AS order_id_check,
    o.order_no,
    o.salesman,
    o.del_flag AS order_del_flag,
    o.create_time AS order_create_time
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND (o.del_flag IS NULL OR o.del_flag != 0)  -- 已删除的订单或del_flag为null的订单
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND MONTH(o.create_time) = 1
  AND o.salesman = '巴冰雪'
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != '';

-- 查看del_flag为null的订单（这些订单在SQL中会被过滤，但在Java中可能被统计）
SELECT 
    t.id AS task_id,
    t.order_id,
    o.id AS order_id_check,
    o.order_no,
    o.salesman,
    o.del_flag AS order_del_flag,
    o.create_time AS order_create_time
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag IS NULL  -- del_flag为null的订单
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND MONTH(o.create_time) = 1
  AND o.salesman = '巴冰雪'
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != '';

-- 查看所有符合条件的订单（不去重）
SELECT 
    t.id AS task_id,
    t.order_id,
    o.id AS order_id_check,
    o.order_no,
    o.salesman,
    o.business_type,
    o.create_time AS order_create_time,
    o.del_flag AS order_del_flag,
    t.create_time AS task_create_time,
    t.task_type,
    t.del_flag AS task_del_flag
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND MONTH(o.create_time) = 1
  AND o.salesman = '巴冰雪'
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != ''
ORDER BY o.create_time DESC;

-- 查看去重后的订单（应该返回78）
SELECT DISTINCT
    o.id AS order_id,
    o.order_no,
    o.salesman,
    o.create_time AS order_create_time
FROM sys_audit_task t
INNER JOIN gh_order o ON t.order_id = o.id
WHERE t.del_flag = 0
  AND o.del_flag = 0
  AND t.create_time >= '2026-01-01 00:00:00'
  AND t.create_time <= '2026-12-31 23:59:59'
  AND YEAR(o.create_time) = 2026
  AND MONTH(o.create_time) = 1
  AND o.salesman = '巴冰雪'
  AND t.task_type = 'recurring'
  AND t.order_id IS NOT NULL
  AND t.order_id != ''
ORDER BY o.create_time DESC;

