-- ============================================
-- 修复代账交接第一步审核记录状态SQL脚本
-- 对于已经创建的代账交接，如果订单的第一步审核已完成，
-- 则自动将代账交接的第一步审核记录标记为已完成
-- ============================================

-- 更新代账交接的第一步审核记录状态
-- 如果订单的第一步审核已完成，则代账交接的第一步也应该标记为已完成
UPDATE gh_accounting_handover_audit ha
INNER JOIN gh_accounting_handover h ON ha.handover_id = h.id
INNER JOIN (
    -- 查询订单第一步审核是否已完成
    SELECT 
        order_id,
        CASE 
            WHEN COUNT(*) > 0 AND SUM(CASE WHEN audit_status = 'approved' THEN 1 ELSE 0 END) = COUNT(*) 
            THEN 1 
            ELSE 0 
        END AS first_step_completed
    FROM gh_order_audit
    WHERE step_order = 1
    GROUP BY order_id
) oa ON h.order_id = oa.order_id
SET 
    ha.audit_status = 'approved',
    ha.audit_time = NOW(),
    ha.audit_remark = 'Order first step audit completed, auto approved',
    ha.update_time = NOW()
WHERE 
    ha.step_order = 1
    AND ha.audit_status = 'pending'
    AND oa.first_step_completed = 1;

-- 验证修复结果
SELECT 
    h.id AS handover_id,
    h.order_no,
    h.company_name,
    ha.step_order,
    ha.role_name,
    ha.audit_status,
    ha.audit_time,
    ha.audit_remark
FROM gh_accounting_handover h
INNER JOIN gh_accounting_handover_audit ha ON h.id = ha.handover_id
WHERE ha.step_order = 1
ORDER BY h.create_time DESC
LIMIT 20;

