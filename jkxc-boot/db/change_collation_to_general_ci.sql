-- 将数据库排序规则从 utf8mb4_0900_ai_ci 改为 utf8mb4_general_ci
-- 执行前请备份数据库

-- 1. 修改数据库默认排序规则
ALTER DATABASE `jkxc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2. 修改所有表的排序规则
-- 注意：这会修改表及其所有字段的排序规则
-- 如果有外键约束，需要先删除外键，修改后再重新创建

-- 2.1 处理有外键约束的表（sys_audit_step）
ALTER TABLE `sys_audit_step` DROP FOREIGN KEY `fk_audit_step_process`;

-- 2.2 修改所有表的排序规则
ALTER TABLE `gh_accounting_contract` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_accounting_handover` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_accounting_handover_audit` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_business_task` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_customer` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_followup_detail` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_opportunity` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order_audit` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order_expense` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order_form_data` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order_operation_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order_payment` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `gh_order_step` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `sys_audit_process` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `sys_audit_step` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `sys_dynamic_form` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `sys_dynamic_form_binding` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `sys_dynamic_form_version` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `sys_team` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2.3 重新创建外键约束
ALTER TABLE `sys_audit_step` ADD CONSTRAINT `fk_audit_step_process` FOREIGN KEY (`process_id`) REFERENCES `sys_audit_process` (`id`) ON DELETE CASCADE;

-- 3. 验证：检查是否还有使用 utf8mb4_0900_ai_ci 的表
-- SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_COLLATION = 'utf8mb4_0900_ai_ci';

