-- 修复审核流程配置（仅配置，不改历史任务）
-- 目的：把“第3步=财税顾问”在流程配置里写错的 role_id/nextStepRoleId 纠正为正确的财税顾问 roleId
--
-- 背景：
-- 线上出现 sys_audit_step.role_name='财税顾问' 但 role_id 指向了“财税主管”(role_code=cwgw) 的情况
-- 导致后续生成的第3步待审任务 current_role_id 错配，财税顾问人员看不见
--
-- 本脚本只修：
-- - sys_audit_step（流程步骤表）
-- - sys_audit_form_indicator（next_auditor 指标配置里 roleId/nextStepRoleId）
--
-- 不修：
-- - sys_audit_task（已生成的任务）。如需修历史任务请用 fix_task_center_step3_invisible_batch_20260128.sql
--
-- 注意：库里存在 utf8mb4_general_ci / utf8mb4_0900_ai_ci 混用，字符串比较易触发 1267
-- 这里统一在比较表达式里强制 COLLATE 到 utf8mb4_general_ci

START TRANSACTION;

-- ====== 参数（如你们财税顾问想用另一个 roleId，可在此修改）======
SET @WRONG_ROLE_ID := '2007320114263695362';      -- 实际是“财税主管”（sys_role.role_code=cwgw）
SET @ADVISOR_ROLE_ID := '2008778829643956225';   -- 标准财税顾问（sys_role.role_code=cszl）

-- ====== 修复前检查：找出“步骤名像财税顾问，但 role_id=错误”的配置 ======
SELECT
  s.id,
  s.process_id,
  s.step_order,
  s.role_id,
  s.role_name
FROM sys_audit_step s
WHERE s.step_order = 3
  AND CONVERT(s.role_id USING utf8mb4) COLLATE utf8mb4_general_ci =
      CONVERT(@WRONG_ROLE_ID USING utf8mb4) COLLATE utf8mb4_general_ci;

-- ====== 1) 修复 sys_audit_step：把第3步错误 role_id 纠正为财税顾问 ======
UPDATE sys_audit_step s
SET s.role_id = @ADVISOR_ROLE_ID
WHERE s.step_order = 3
  AND CONVERT(s.role_id USING utf8mb4) COLLATE utf8mb4_general_ci =
      CONVERT(@WRONG_ROLE_ID USING utf8mb4) COLLATE utf8mb4_general_ci;

-- 回填步骤角色名，统一从 sys_role 回填（避免历史脏数据/乱码）
UPDATE sys_audit_step s
JOIN sys_role r
  ON CONVERT(r.id USING utf8mb4) COLLATE utf8mb4_general_ci =
     CONVERT(s.role_id USING utf8mb4) COLLATE utf8mb4_general_ci
SET s.role_name = CONVERT(r.role_name USING utf8mb4)
WHERE s.step_order = 3
  AND CONVERT(s.role_id USING utf8mb4) COLLATE utf8mb4_general_ci =
      CONVERT(@ADVISOR_ROLE_ID USING utf8mb4) COLLATE utf8mb4_general_ci;

-- ====== 2) 修复 next_auditor 指标配置：indicator_config 里 roleId/nextStepRoleId 的错误引用 ======
-- 仅对 indicator_type='next_auditor' 生效，避免误伤其他配置

-- 修复 nextStepRoleId（常见：nextStepOrder=3，但 nextStepRoleId 被写成主管 roleId）
UPDATE sys_audit_form_indicator
SET indicator_config = REPLACE(indicator_config,
  CONCAT('\"nextStepRoleId\":\"', @WRONG_ROLE_ID, '\"'),
  CONCAT('\"nextStepRoleId\":\"', @ADVISOR_ROLE_ID, '\"')
)
WHERE indicator_type = 'next_auditor'
  AND CONVERT(indicator_config USING utf8mb4) COLLATE utf8mb4_general_ci LIKE
      CONVERT(CONCAT('%\"nextStepRoleId\":\"', @WRONG_ROLE_ID, '\"%') USING utf8mb4) COLLATE utf8mb4_general_ci;

-- 修复 roleId（如果指标本身限定的可选用户角色也写错）
UPDATE sys_audit_form_indicator
SET indicator_config = REPLACE(indicator_config,
  CONCAT('\"roleId\":\"', @WRONG_ROLE_ID, '\"'),
  CONCAT('\"roleId\":\"', @ADVISOR_ROLE_ID, '\"')
)
WHERE indicator_type = 'next_auditor'
  AND CONVERT(indicator_config USING utf8mb4) COLLATE utf8mb4_general_ci LIKE
      CONVERT(CONCAT('%\"roleId\":\"', @WRONG_ROLE_ID, '\"%') USING utf8mb4) COLLATE utf8mb4_general_ci;

COMMIT;

-- ====== 修复后检查：确认第3步不再引用错误 roleId ======
SELECT
  COUNT(*) AS step3_wrong_role_id_cnt_after
FROM sys_audit_step s
WHERE s.step_order = 3
  AND CONVERT(s.role_id USING utf8mb4) COLLATE utf8mb4_general_ci =
      CONVERT(@WRONG_ROLE_ID USING utf8mb4) COLLATE utf8mb4_general_ci;


