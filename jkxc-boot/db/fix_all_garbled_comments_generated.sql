-- ============================================
-- 修复数据库中所有表备注和字段备注乱码问题
-- 执行前请备份数据库！
-- 注意：请确保MySQL客户端使用UTF-8编码执行此脚本
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;

-- ============================================
-- 修复表备注乱码
-- ============================================

-- sys_audit_form_indicator: 瀹℃壒琛ㄥ崟鐗规畩鎸囨爣閰嶇疆琛? -> 审批表单特殊指标配置表
ALTER TABLE `sys_audit_form_indicator` COMMENT = '审批表单特殊指标配置表';

-- sys_audit_process_binding: 娴佺▼瀹℃壒涓氬姟绫诲瀷缁戝畾琛? -> 流程审批业务类型绑定表
ALTER TABLE `sys_audit_process_binding` COMMENT = '流程审批业务类型绑定表';

-- sys_audit_step_form: 瀹℃壒姝ラ?よ〃鍗曢厤缃?琛? -> 审批步骤表单配置表
ALTER TABLE `sys_audit_step_form` COMMENT = '审批步骤表单配置表';

-- sys_audit_task: 瀹℃壒浠诲姟琛? -> 审批任务表
ALTER TABLE `sys_audit_task` COMMENT = '审批任务表';

-- sys_audit_task_cost: 瀹℃壒浠诲姟鎴愭湰璁板綍琛? -> 审批任务成本记录表
ALTER TABLE `sys_audit_task_cost` COMMENT = '审批任务成本记录表';

-- ============================================
-- 说明：
-- 1. 此SQL文件直接使用UTF-8编码的中文字符串
-- 2. 执行前请确保MySQL客户端使用UTF-8编码（已通过SET NAMES utf8mb4设置）
-- 3. 如果使用命令行执行，请使用：mysql -u用户名 -p --default-character-set=utf8mb4 数据库名 < 此文件
-- 4. 如果使用MySQL Workbench等工具，请确保连接字符集为utf8mb4
-- 5. 执行此脚本后，请验证表备注是否正确显示
-- 6. 如果发现字段备注也有乱码，请使用类似的方式修复：
--    ALTER TABLE `表名` MODIFY COLUMN `字段名` 字段类型 COMMENT = '正确的中文备注';
-- ============================================

