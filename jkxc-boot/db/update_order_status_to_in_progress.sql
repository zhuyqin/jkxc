-- 将所有订单状态设置为"进行中"（1）
-- 执行前请备份数据库！

-- 更新所有未删除的订单状态为"1"（进行中）
UPDATE `gh_order` 
SET 
  `order_status` = '1',
  `update_time` = NOW()
WHERE 
  `del_flag` = 0
  AND (`order_status` IS NULL OR `order_status` != '1');

-- 查看更新结果
SELECT 
  COUNT(*) as total_orders,
  SUM(CASE WHEN order_status = '1' THEN 1 ELSE 0 END) as in_progress_count,
  SUM(CASE WHEN order_status = '2' THEN 1 ELSE 0 END) as completed_count,
  SUM(CASE WHEN order_status IS NULL OR order_status NOT IN ('1', '2') THEN 1 ELSE 0 END) as other_status_count
FROM `gh_order`
WHERE `del_flag` = 0;

