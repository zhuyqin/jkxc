-- ============================================
-- 订单收费记录表 - 添加凭证附件字段
-- ============================================

-- 添加凭证附件字段
ALTER TABLE `gh_order_payment` 
ADD COLUMN `image_voucher` TEXT DEFAULT NULL COMMENT '凭证附件（JSON数组）' AFTER `remarks`;

