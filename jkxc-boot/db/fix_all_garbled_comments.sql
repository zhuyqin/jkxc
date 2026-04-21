-- ============================================
-- 修复数据库中所有表备注和字段备注的乱码问题
-- 使用UNHEX函数确保UTF-8编码正确
-- 执行前请备份数据库！
-- ============================================

SET NAMES utf8mb4;

-- ============================================
-- 修复表备注乱码
-- ============================================

-- sys_audit_form_indicator: 瀹℃壒琛ㄥ崟鐗规畩鎸囨爣閰嶇疆琛? -> 审批表单特殊指标配置表
ALTER TABLE `sys_audit_form_indicator` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E8A1A8E58D95E789B9E6AE8AE68C87E6A087E9858DE7BDAEE8A1A8') USING utf8mb4);

-- sys_audit_process_binding: 娴佺▼瀹℃壒涓氬姟绫诲瀷缁戝畾琛? -> 流程审批业务类型绑定表
ALTER TABLE `sys_audit_process_binding` COMMENT = CONVERT(UNHEX('E6B581E7A88BE5AEA1E689B9E4B89AE58AA1E7B1BBE59E8BE7BB91E5AE9AE8A1A8') USING utf8mb4);

-- sys_audit_step_form: 瀹℃壒姝ラ?よ〃鍗曢厤缃?琛? -> 审批步骤表单配置表
ALTER TABLE `sys_audit_step_form` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E6ADA5E9AAA4E8A1A8E58D95E9858DE7BDAEE8A1A8') USING utf8mb4);

-- sys_audit_task: 瀹℃壒浠诲姟琛? -> 审批任务表
ALTER TABLE `sys_audit_task` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E4BBBBE58AA1E8A1A8') USING utf8mb4);

-- sys_audit_task_cost: 瀹℃壒浠诲姟鎴愭湰璁板綍琛? -> 审批任务成本记录表
ALTER TABLE `sys_audit_task_cost` COMMENT = CONVERT(UNHEX('E5AEA1E689B9E4BBBBE58AA1E68890E69CACE8AEB0E5BD95E8A1A8') USING utf8mb4);

-- ============================================
-- 查询并修复所有字段备注乱码
-- 注意：由于字段备注可能很多，这里只修复已知的乱码字段
-- 如果需要修复所有字段，请使用Python脚本
-- ============================================

-- 如果发现字段备注有乱码，可以使用以下格式修复：
-- ALTER TABLE `表名` MODIFY COLUMN `字段名` 字段类型 COMMENT = UNHEX('十六进制编码的UTF-8字符串');

-- 示例：修复某个字段的备注
-- ALTER TABLE `sys_audit_form_indicator` MODIFY COLUMN `id` VARCHAR(32) NOT NULL COMMENT = UNHEX('E4B8BBE994AEE494');

-- ============================================
-- 说明：
-- 1. UNHEX函数将十六进制字符串转换为二进制，然后MySQL会将其解释为UTF-8字符串
-- 2. 可以使用Python的 encode('utf-8').hex() 来获取中文字符串的十六进制编码
-- 3. 执行此脚本后，请验证表备注是否正确显示
-- ============================================

