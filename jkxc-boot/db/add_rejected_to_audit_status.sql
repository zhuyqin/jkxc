-- 为审核状态字典添加"已拒绝"选项
-- 字典代码: audit_status
-- 执行方式: 在MySQL客户端中执行，确保使用utf8mb4编码

SET NAMES utf8mb4;

-- 获取字典ID
SET @dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'audit_status' LIMIT 1);

-- 检查是否已存在"已拒绝"选项（item_value = '2'）
SELECT COUNT(*) INTO @exists FROM sys_dict_item WHERE dict_id = @dict_id AND item_value = '2';

-- 如果不存在，则添加"已拒绝"选项
INSERT INTO sys_dict_item (
    id,
    dict_id,
    item_text,
    item_value,
    description,
    sort_order,
    status,
    create_by,
    create_time
)
SELECT 
    REPLACE(UUID(), '-', '') as id,
    @dict_id as dict_id,
    '已拒绝' as item_text,
    '2' as item_value,
    '已拒绝' as description,
    3 as sort_order,
    1 as status,
    'admin' as create_by,
    NOW() as create_time
WHERE @exists = 0;

-- 如果已存在，则更新
UPDATE sys_dict_item 
SET 
    item_text = '已拒绝',
    description = '已拒绝',
    sort_order = 3,
    status = 1,
    update_time = NOW()
WHERE dict_id = @dict_id AND item_value = '2';

