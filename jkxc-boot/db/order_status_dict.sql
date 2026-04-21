-- 订单状态字典配置
-- 执行前请备份数据库！

-- 1. 创建订单状态字典（如果不存在）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
SELECT 
  'order_status_dict_001',
  '订单状态',
  'order_status',
  '订单状态字典：进行中、已完成',
  0,
  'admin',
  NOW(),
  'admin',
  NOW(),
  0
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'order_status');

-- 2. 删除旧的字典项（如果存在）
DELETE FROM sys_dict_item WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'order_status');

-- 3. 插入新的字典项
-- 进行中
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'order_status_item_001',
  (SELECT id FROM sys_dict WHERE dict_code = 'order_status'),
  '进行中',
  '1',
  '订单进行中',
  1,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'order_status');

-- 已完成
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'order_status_item_002',
  (SELECT id FROM sys_dict WHERE dict_code = 'order_status'),
  '已完成',
  '2',
  '订单已完成',
  2,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'order_status');

