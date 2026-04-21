-- 修复任务中心：流程步骤角色ID/角色名不一致导致“下一个审核人看不见/按角色查不到人”
-- case: 订单号 20260128017（周期任务 step_order=3）
--
-- 核心问题：
-- - sys_audit_step(step_order=3) 的 role_id 错误指向了“财税主管”，但 role_name 写成“财税顾问”
-- - 已生成的 sys_audit_task.current_role_id 也随之错误，导致拥有“财税顾问”角色的用户看不到待审核任务
-- - next_auditor 指标的 indicator_config.nextStepRoleId 也错误，进一步造成“按角色查询拥有该角色的人”不一致
--
-- 目标修复：
-- - 将 step_order=3 角色ID修正为：2008778829643956225（sys_role.role_code=cszl，财税顾问）
-- - 将该订单的 pending 任务 current_role_id 同步修正
-- - 角色名称统一从 sys_role 回填，避免字符集/乱码问题

START TRANSACTION;

-- 1) 修复流程步骤（step_id=2011632970292576257，step_order=3）
UPDATE sys_audit_step
SET role_id = '2008778829643956225'
WHERE id = '2011632970292576257';

-- 角色名从 sys_role 回填（避免乱码/字符集问题）
UPDATE sys_audit_step s
JOIN sys_role r ON r.id = s.role_id
SET s.role_name = r.role_name
WHERE s.id = '2011632970292576257';

-- 2) 修复“下个流程审批人员选择”指标配置（indicator id=2011632970963664897）
-- 将 nextStepRoleId 纠正为财税顾问 roleId
UPDATE sys_audit_form_indicator
SET indicator_config = '{"roleId":"2008778829643956225","roleName":"财税顾问","nextStepOrder":3,"nextStepRoleId":"2008778829643956225"}'
WHERE id = '2011632970963664897';

-- 3) 修复已生成的待审核任务（订单号 20260128017，step_order=3）
UPDATE sys_audit_task
SET current_role_id = '2008778829643956225'
WHERE order_no = '20260128017'
  AND task_status = 'pending'
  AND step_order = 3;

-- 角色名从 sys_role 回填（避免乱码/字符集问题）
UPDATE sys_audit_task t
JOIN sys_role r ON r.id = t.current_role_id
SET t.current_role_name = r.role_name
WHERE t.order_no = '20260128017'
  AND t.task_status = 'pending'
  AND t.step_order = 3;

COMMIT;


