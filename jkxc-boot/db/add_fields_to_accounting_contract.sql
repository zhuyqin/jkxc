-- ============================================
-- 合同管理表 - 添加字段
-- 添加：到期时间(expire_date)、服务人员(service_person)、流失标志(loss_flag)
-- 注意：如果字段已存在，执行会报错，请手动检查后再执行
-- ============================================

-- 添加到期时间字段
ALTER TABLE `gh_accounting_contract` 
ADD COLUMN `expire_date` DATE DEFAULT NULL COMMENT '到期时间' AFTER `sign_date`;

-- 添加服务人员字段
ALTER TABLE `gh_accounting_contract` 
ADD COLUMN `service_person` VARCHAR(50) DEFAULT NULL COMMENT '服务人员' AFTER `company_name`;

-- 添加流失标志字段
ALTER TABLE `gh_accounting_contract` 
ADD COLUMN `loss_flag` TINYINT(1) DEFAULT '0' COMMENT '流失标志：0-正常，1-已流失' AFTER `contract_status`;

-- 添加索引
ALTER TABLE `gh_accounting_contract` 
ADD INDEX `idx_expire_date` (`expire_date`);

ALTER TABLE `gh_accounting_contract` 
ADD INDEX `idx_service_person` (`service_person`);

ALTER TABLE `gh_accounting_contract` 
ADD INDEX `idx_loss_flag` (`loss_flag`);

