-- 线索状态字典：意向a+、意向a、意向b、意向c、无效客户
-- 删除旧的字典数据（如果存在）
DELETE FROM sys_dict_item WHERE dict_id IN (SELECT id FROM sys_dict WHERE dict_code = 'opportunity_status');
DELETE FROM sys_dict WHERE dict_code = 'opportunity_status';

-- 插入字典类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
VALUES ('opp_status_dict_001', '线索状态', 'opportunity_status', '线索状态：意向a+、意向a、意向b、意向c、无效客户', 0, 'admin', NOW(), NULL, NULL, 0);

-- 插入字典项（直接使用子查询获取字典ID）
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'opp_status_item_001', id, '意向a+', 'intention_a_plus', '意向a+', 1, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'opportunity_status' LIMIT 1;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'opp_status_item_002', id, '意向a', 'intention_a', '意向a', 2, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'opportunity_status' LIMIT 1;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'opp_status_item_003', id, '意向b', 'intention_b', '意向b', 3, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'opportunity_status' LIMIT 1;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'opp_status_item_004', id, '意向c', 'intention_c', '意向c', 4, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'opportunity_status' LIMIT 1;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'opp_status_item_005', id, '无效客户', 'invalid', '无效客户', 5, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'opportunity_status' LIMIT 1;

-- 归属类型字典：公海、个人、无效
DELETE FROM sys_dict_item WHERE dict_id IN (SELECT id FROM sys_dict WHERE dict_code = 'owner_type');
DELETE FROM sys_dict WHERE dict_code = 'owner_type';

INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
VALUES ('owner_type_dict_001', '归属类型', 'owner_type', '归属类型：公海、个人、无效', 0, 'admin', NOW(), NULL, NULL, 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'owner_type_item_001', id, '公海', 'public', '公海线索', 1, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'owner_type' LIMIT 1;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'owner_type_item_002', id, '个人', 'personal', '个人线索', 2, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'owner_type' LIMIT 1;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
SELECT 
  'owner_type_item_003', id, '无效', 'invalid', '无效线索', 3, 1, 'admin', NOW()
FROM sys_dict WHERE dict_code = 'owner_type' LIMIT 1;

