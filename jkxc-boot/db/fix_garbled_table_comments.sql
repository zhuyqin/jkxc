-- ============================================
-- 修复数据库中表备注乱码问题
-- 请在MySQL客户端中直接执行此脚本
-- 确保MySQL客户端使用UTF-8编码
-- ============================================

SET NAMES utf8mb4;

-- 修复已知乱码的表备注
ALTER TABLE `sys_audit_form_indicator` COMMENT = '审批表单特殊指标配置表';
ALTER TABLE `sys_audit_process_binding` COMMENT = '流程审批业务类型绑定表';
ALTER TABLE `sys_audit_step_form` COMMENT = '审批步骤表单配置表';
ALTER TABLE `sys_audit_task` COMMENT = '审批任务表';
ALTER TABLE `sys_audit_task_cost` COMMENT = '审批任务成本记录表';

-- 验证修复结果
SELECT TABLE_NAME, TABLE_COMMENT 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME IN (
    'sys_audit_form_indicator',
    'sys_audit_process_binding', 
    'sys_audit_step_form',
    'sys_audit_task',
    'sys_audit_task_cost'
  );

