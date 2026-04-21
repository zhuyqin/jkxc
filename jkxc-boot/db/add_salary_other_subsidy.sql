-- ============================================
-- 为工资信息表和工资发放表添加其他补贴字段
-- ============================================

-- 为 gh_salary_info 表添加 other_subsidy 字段
ALTER TABLE `gh_salary_info` 
ADD COLUMN `other_subsidy` DECIMAL(18,2) DEFAULT NULL COMMENT '其他补贴' AFTER `high_temperature_subsidy`;

-- 为 gh_salary_payment 表添加 other_subsidy 字段
ALTER TABLE `gh_salary_payment` 
ADD COLUMN `other_subsidy` DECIMAL(18,2) DEFAULT NULL COMMENT '其他补贴' AFTER `high_temperature_subsidy`;

