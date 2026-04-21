-- ============================================
-- 修复数据库中所有表备注和字段备注乱码问题
-- 使用UNHEX函数将中文转换为十六进制编码
-- 执行前请备份数据库！
-- ============================================

SET NAMES utf8mb4;

-- ============================================
-- 修复表备注乱码
-- ============================================

-- sys_audit_form_indicator: 审批表单特殊指标配置表
ALTER TABLE `sys_audit_form_indicator` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E8A1A8E58D95E789B9E6AE8AE68C87E6A087E9858DE7BDAEE8A1A8') USING utf8mb4);

-- sys_audit_process_binding: 流程审批业务类型绑定表
ALTER TABLE `sys_audit_process_binding` COMMENT = CONVERT(UNHEX('E6B581E7A88BE5AEA1E689B9E4B89AE58AA1E7B1BBE59E8BE7BB91E5AE9AE8A1A8') USING utf8mb4);

-- sys_audit_step_form: 审批步骤表单配置表
ALTER TABLE `sys_audit_step_form` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E6ADA5E9AAA4E8A1A8E58D95E9858DE7BDAEE8A1A8') USING utf8mb4);

-- sys_audit_task: 审批任务表
ALTER TABLE `sys_audit_task` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E4BBBBE58AA1E8A1A8') USING utf8mb4);

-- sys_audit_task_cost: 审批任务成本记录表
ALTER TABLE `sys_audit_task_cost` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E4BBBBE58AA1E68890E69CACE8AEB0E5BD95E8A1A8') USING utf8mb4);

-- ============================================
-- 说明：
-- 1. 此SQL文件使用UNHEX函数将UTF-8编码的中文字符串转换为二进制，然后使用CONVERT转换为utf8mb4字符集
-- 2. 十六进制编码是通过Python的 encode('utf-8').hex() 生成的
-- 3. 执行此脚本后，请验证表备注是否正确显示
-- ============================================

