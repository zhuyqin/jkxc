-- ============================================
-- 客户名称同步更新解决方案（完整版）
-- ============================================
-- 功能说明：
-- 1. 当在gh_customer表中修改客户名称(corporate_name)时
-- 2. 自动同步更新所有关联表中的公司名称(company_name)字段
-- 3. 包含触发器、历史数据修复脚本和数据一致性检查
-- 
-- 涉及的表：
-- - gh_order (订单表)
-- - gh_accounting_contract (代账合同表)
-- - gh_accounting_handover (代账交接表)
-- - gh_business_task (业务任务表)
-- - gh_address_center (地址中心表)
-- - gh_address_renew (地址续费表)
-- - gh_bank_diary (银行日记账表)
-- - gh_reimbursement (报销表)
-- - gh_opportunity (商机表 - corporate_name字段)
-- 
-- 执行前注意事项：
-- ⚠️ 务必先备份数据库！
-- ⚠️ 建议在测试环境先执行验证！
-- ⚠️ 选择业务低峰期执行！
-- ============================================

-- ============================================
-- 第一部分：创建触发器（自动同步更新）
-- ============================================

-- 删除旧触发器（如果存在）
DROP TRIGGER IF EXISTS `trg_customer_name_update`;

DELIMITER $$

-- 创建触发器：当客户名称更新时，同步更新所有关联表
CREATE TRIGGER `trg_customer_name_update`
AFTER UPDATE ON `gh_customer`
FOR EACH ROW
BEGIN
    -- 只有当客户名称发生变化时才执行更新
    IF OLD.corporate_name != NEW.corporate_name THEN
        
        -- 1. 更新订单表 (gh_order)
        UPDATE `gh_order`
        SET `company_name` = NEW.corporate_name,
            `update_time` = NOW()
        WHERE `company_id` = NEW.id
          AND `del_flag` = 0;
        
        -- 2. 更新代账合同表 (gh_accounting_contract)
        -- 通过订单ID关联更新
        UPDATE `gh_accounting_contract` ac
        INNER JOIN `gh_order` o ON ac.order_id = o.id
        SET ac.company_name = NEW.corporate_name,
            ac.update_time = NOW()
        WHERE o.company_id = NEW.id
          AND ac.del_flag = 0;
        
        -- 3. 更新代账交接表 (gh_accounting_handover)
        UPDATE `gh_accounting_handover` ah
        INNER JOIN `gh_order` o ON ah.order_id = o.id
        SET ah.company_name = NEW.corporate_name,
            ah.update_time = NOW()
        WHERE o.company_id = NEW.id
          AND ah.del_flag = 0;
        
        -- 4. 更新业务任务表 (gh_business_task)
        UPDATE `gh_business_task` bt
        INNER JOIN `gh_order` o ON bt.order_id = o.id
        SET bt.company_name = NEW.corporate_name,
            bt.update_time = NOW()
        WHERE o.company_id = NEW.id
          AND bt.del_flag = 0;
        
        -- 5. 更新地址中心表 (gh_address_center)
        UPDATE `gh_address_center`
        SET `company_name` = NEW.corporate_name,
            `update_time` = NOW()
        WHERE `data_id` = NEW.id
          AND `del_flag` = 0;
        
        -- 6. 更新地址续费表 (gh_address_renew)
        UPDATE `gh_address_renew`
        SET `company_name` = NEW.corporate_name
        WHERE `company_id` = NEW.id;
        
        -- 7. 更新银行日记账表 (gh_bank_diary)
        UPDATE `gh_bank_diary`
        SET `company_name` = NEW.corporate_name,
            `update_time` = NOW()
        WHERE `company_id` = NEW.id
          AND `del_flag` = 0;
        
        -- 8. 更新报销表 (gh_reimbursement)
        UPDATE `gh_reimbursement`
        SET `company_name` = NEW.corporate_name,
            `update_time` = NOW()
        WHERE `company_id` = NEW.id
          AND `del_flag` = 0;
        
        -- 9. 更新供应商表 (gh_supplier)
        -- 注意：供应商表的company_name可能是供应商自己的公司名，需要根据业务逻辑判断
        -- 如果供应商表的company_name是关联客户的，则取消下面的注释
        /*
        UPDATE `gh_supplier`
        SET `company_name` = NEW.corporate_name,
            `update_time` = NOW()
        WHERE `company_id` = NEW.id
          AND `del_flag` = 0;
        */
        
        -- 10. 更新商机表 (gh_opportunity) - 如果存在
        UPDATE `gh_opportunity`
        SET `corporate_name` = NEW.corporate_name,
            `update_time` = NOW()
        WHERE `id` IN (
            SELECT `opportunity_id` 
            FROM `gh_customer_relation` 
            WHERE `customer_id` = NEW.id
        )
        AND `del_flag` = 0;
        
    END IF;
END$$

DELIMITER ;

-- ============================================
-- 第二部分：历史数据修复脚本
-- ============================================

-- 说明：此脚本用于修复历史数据中客户名称不一致的问题
-- 执行前请先备份数据库！

-- 1. 修复订单表中的公司名称
UPDATE `gh_order` o
INNER JOIN `gh_customer` c ON o.company_id = c.id
SET o.company_name = c.corporate_name,
    o.update_time = NOW(),
    o.update_by = 'system_sync'
WHERE o.company_name != c.corporate_name
  AND o.del_flag = 0
  AND c.del_flag = 0;

-- 2. 修复代账合同表中的公司名称
UPDATE `gh_accounting_contract` ac
INNER JOIN `gh_order` o ON ac.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
SET ac.company_name = c.corporate_name,
    ac.update_time = NOW(),
    ac.update_by = 'system_sync'
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0;

-- 3. 修复代账交接表中的公司名称
UPDATE `gh_accounting_handover` ah
INNER JOIN `gh_order` o ON ah.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
SET ah.company_name = c.corporate_name,
    ah.update_time = NOW(),
    ah.update_by = 'system_sync'
WHERE ah.company_name != c.corporate_name
  AND ah.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0;

-- 4. 修复业务任务表中的公司名称
UPDATE `gh_business_task` bt
INNER JOIN `gh_order` o ON bt.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
SET bt.company_name = c.corporate_name,
    bt.update_time = NOW(),
    bt.update_by = 'system_sync'
WHERE bt.company_name != c.corporate_name
  AND bt.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0;

-- 5. 修复地址中心表中的公司名称
UPDATE `gh_address_center` ac
INNER JOIN `gh_customer` c ON ac.data_id = c.id
SET ac.company_name = c.corporate_name,
    ac.update_time = NOW(),
    ac.update_by = 'system_sync'
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND c.del_flag = 0;

-- 6. 修复地址续费表中的公司名称
UPDATE `gh_address_renew` ar
INNER JOIN `gh_customer` c ON ar.company_id = c.id
SET ar.company_name = c.corporate_name
WHERE ar.company_name != c.corporate_name
  AND c.del_flag = 0;

-- 7. 修复银行日记账表中的公司名称
UPDATE `gh_bank_diary` bd
INNER JOIN `gh_customer` c ON bd.company_id = c.id
SET bd.company_name = c.corporate_name,
    bd.update_time = NOW(),
    bd.update_by = 'system_sync'
WHERE bd.company_name != c.corporate_name
  AND bd.del_flag = 0
  AND c.del_flag = 0;

-- 8. 修复报销表中的公司名称
UPDATE `gh_reimbursement` r
INNER JOIN `gh_customer` c ON r.company_id = c.id
SET r.company_name = c.corporate_name,
    r.update_time = NOW(),
    r.update_by = 'system_sync'
WHERE r.company_name != c.corporate_name
  AND r.del_flag = 0
  AND c.del_flag = 0;

-- 9. 修复商机表中的公司名称
UPDATE `gh_opportunity` opp
INNER JOIN `gh_customer_relation` cr ON opp.id = cr.opportunity_id
INNER JOIN `gh_customer` c ON cr.customer_id = c.id
SET opp.corporate_name = c.corporate_name,
    opp.update_time = NOW(),
    opp.update_by = 'system_sync'
WHERE opp.corporate_name != c.corporate_name
  AND opp.del_flag = 0
  AND c.del_flag = 0;

-- ============================================
-- 第三部分：数据一致性检查
-- ============================================

-- 检查订单表中公司名称不一致的记录
SELECT 
    '订单表(gh_order)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_order` o
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE o.company_name != c.corporate_name
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

-- 检查代账合同表中公司名称不一致的记录
SELECT 
    '代账合同表(gh_accounting_contract)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_accounting_contract` ac
INNER JOIN `gh_order` o ON ac.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

-- 检查代账交接表中公司名称不一致的记录
SELECT 
    '代账交接表(gh_accounting_handover)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_accounting_handover` ah
INNER JOIN `gh_order` o ON ah.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE ah.company_name != c.corporate_name
  AND ah.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

-- 检查业务任务表中公司名称不一致的记录
SELECT 
    '业务任务表(gh_business_task)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_business_task` bt
INNER JOIN `gh_order` o ON bt.order_id = o.id
INNER JOIN `gh_customer` c ON o.company_id = c.id
WHERE bt.company_name != c.corporate_name
  AND bt.del_flag = 0
  AND o.del_flag = 0
  AND c.del_flag = 0

UNION ALL

-- 检查地址中心表中公司名称不一致的记录
SELECT 
    '地址中心表(gh_address_center)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_address_center` ac
INNER JOIN `gh_customer` c ON ac.data_id = c.id
WHERE ac.company_name != c.corporate_name
  AND ac.del_flag = 0
  AND c.del_flag = 0

UNION ALL

-- 检查银行日记账表中公司名称不一致的记录
SELECT 
    '银行日记账表(gh_bank_diary)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_bank_diary` bd
INNER JOIN `gh_customer` c ON bd.company_id = c.id
WHERE bd.company_name != c.corporate_name
  AND bd.del_flag = 0
  AND c.del_flag = 0

UNION ALL

-- 检查报销表中公司名称不一致的记录
SELECT 
    '报销表(gh_reimbursement)' AS table_name,
    COUNT(*) AS inconsistent_count
FROM `gh_reimbursement` r
INNER JOIN `gh_customer` c ON r.company_id = c.id
WHERE r.company_name != c.corporate_name
  AND r.del_flag = 0
  AND c.del_flag = 0;

-- ============================================
-- 第四部分：查看触发器状态
-- ============================================

-- 查看触发器是否创建成功
SHOW TRIGGERS WHERE `Trigger` = 'trg_customer_name_update';

-- ============================================
-- 使用说明
-- ============================================
/*
1. 执行顺序：
   - 首先执行"第一部分"创建触发器
   - 然后执行"第二部分"修复历史数据
   - 最后执行"第三部分"检查数据一致性

2. 注意事项：
   - 执行前请务必备份数据库
   - 建议在测试环境先执行验证
   - 触发器会在每次更新客户名称时自动执行
   - 历史数据修复脚本只需执行一次

3. 如何删除触发器：
   DROP TRIGGER IF EXISTS `trg_customer_name_update`;

4. 性能考虑：
   - 触发器会在更新客户名称时执行多个UPDATE语句
   - 如果客户关联的数据量很大，可能会影响性能
   - 建议在业务低峰期执行历史数据修复

5. 后续维护：
   - 如果新增了包含company_name字段的表，需要更新触发器
   - 定期执行数据一致性检查脚本
*/
