-- 流失原因字典配置（正常流失 + 非正常流失）
-- 执行前请备份数据库！

-- 一、正常流失原因：dict_code = 'lossReason'

-- 1. 创建正常流失原因字典（如果不存在）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
SELECT 
  'loss_reason_dict_001',
  '正常流失原因',
  'lossReason',
  '正常流失原因：转零申报、我司转让、其他原因-代办注销',
  0,
  'admin',
  NOW(),
  'admin',
  NOW(),
  0
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossReason');

-- 2. 删除旧的字典项（如果存在）
DELETE FROM sys_dict_item WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'lossReason');

-- 3. 插入新的字典项（正常流失）

-- 转零申报
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_reason_item_001',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossReason'),
  '转零申报',
  'zero_declare',
  '正常流失：转零申报',
  1,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossReason');

-- 我司转让
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_reason_item_002',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossReason'),
  '我司转让',
  'our_transfer',
  '正常流失：我司转让',
  2,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossReason');

-- 其他原因-代办注销
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_reason_item_003',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossReason'),
  '其他原因-代办注销',
  'other_agent_cancel',
  '正常流失：其他原因（代办注销）',
  3,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossReason');


-- 二、非正常流失原因：dict_code = 'lossNoNormalReason'

-- 1. 创建非正常流失原因字典（如果不存在）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
SELECT 
  'loss_abnormal_reason_dict_001',
  '非正常流失原因',
  'lossNoNormalReason',
  '非正常流失原因：自己注销、价格原因、服务需求不能满足、其他原因',
  0,
  'admin',
  NOW(),
  'admin',
  NOW(),
  0
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossNoNormalReason');

-- 2. 删除旧的字典项（如果存在）
DELETE FROM sys_dict_item WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'lossNoNormalReason');

-- 3. 插入新的字典项（非正常流失）

-- 自己注销
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_abnormal_reason_item_001',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossNoNormalReason'),
  '自己注销',
  'self_cancel',
  '非正常流失：自己注销',
  1,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossNoNormalReason');

-- 价格原因
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_abnormal_reason_item_002',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossNoNormalReason'),
  '价格原因',
  'price_reason',
  '非正常流失：价格原因',
  2,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossNoNormalReason');

-- 服务需求不能满足（服务原因）
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_abnormal_reason_item_003',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossNoNormalReason'),
  '服务需求不能满足',
  'service_not_satisfied',
  '非正常流失：服务原因-服务需求不能满足',
  3,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossNoNormalReason');

-- 其他原因
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT 
  'loss_abnormal_reason_item_004',
  (SELECT id FROM sys_dict WHERE dict_code = 'lossNoNormalReason'),
  '其他原因',
  'other_reason',
  '非正常流失：其他原因',
  4,
  1,
  'admin',
  NOW(),
  'admin',
  NOW()
WHERE EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'lossNoNormalReason');


