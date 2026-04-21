-- ============================================
-- 为地址中心表添加缺失字段
-- ============================================

-- 添加服务状态字段
ALTER TABLE `gh_address_center` 
ADD COLUMN `service_status` varchar(20) DEFAULT '1' COMMENT '服务状态：1-服务中，2-已终止' 
AFTER `address_status`;

-- 添加流失标志字段
ALTER TABLE `gh_address_center` 
ADD COLUMN `loss_flag` int(1) DEFAULT '0' COMMENT '流失标志：0-正常，1-已流失' 
AFTER `service_status`;

-- 添加索引以提高查询性能
ALTER TABLE `gh_address_center` 
ADD INDEX `idx_service_status` (`service_status`);

ALTER TABLE `gh_address_center` 
ADD INDEX `idx_loss_flag` (`loss_flag`);

-- 根据现有数据初始化服务状态
-- 如果终止日期已过，设置为已终止；否则设置为服务中
UPDATE `gh_address_center` 
SET `service_status` = CASE 
    WHEN `termination_date` IS NOT NULL AND `termination_date` < CURDATE() THEN '2'
    ELSE '1'
END
WHERE `service_status` IS NULL OR `service_status` = '';

-- ============================================
-- 添加服务状态字典数据
-- ============================================

-- 检查并插入服务状态字典类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
SELECT * FROM (
  SELECT 
    REPLACE(UUID(), '-', '') as id,
    '服务状态' as dict_name,
    'service_status' as dict_code,
    '地址服务状态：1-服务中，2-已终止' as description,
    0 as del_flag,
    'admin' as create_by,
    NOW() as create_time,
    NULL as update_by,
    NULL as update_time,
    0 as type
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM sys_dict WHERE dict_code = 'service_status'
);

-- 获取字典ID（用于插入字典项）
SET @dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'service_status');

-- 插入服务状态字典项：服务中
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT * FROM (
  SELECT 
    REPLACE(UUID(), '-', '') as id,
    @dict_id as dict_id,
    '服务中' as item_text,
    '1' as item_value,
    '地址正在服务中' as description,
    1 as sort_order,
    1 as status,
    'admin' as create_by,
    NOW() as create_time,
    NULL as update_by,
    NULL as update_time
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM sys_dict_item WHERE dict_id = @dict_id AND item_value = '1'
);

-- 插入服务状态字典项：已终止
INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT * FROM (
  SELECT 
    REPLACE(UUID(), '-', '') as id,
    @dict_id as dict_id,
    '已终止' as item_text,
    '2' as item_value,
    '地址服务已终止' as description,
    2 as sort_order,
    1 as status,
    'admin' as create_by,
    NOW() as create_time,
    NULL as update_by,
    NULL as update_time
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM sys_dict_item WHERE dict_id = @dict_id AND item_value = '2'
);
