-- =============================================================================
-- 补丁：为 gh_opportunity 增加「是否确认有效」字段
-- 说明：
-- 1) confirm_valid：0-未确认有效（默认）；1-已确认有效
-- 2) confirm_valid_time：确认有效时间
-- 使用：在业务库执行一次即可（若列已存在请跳过对应 ALTER）
-- =============================================================================

ALTER TABLE `gh_opportunity`
  ADD COLUMN `confirm_valid` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否确认有效：0-否，1-是' AFTER `clue_bonus_awarded_time`;

ALTER TABLE `gh_opportunity`
  ADD COLUMN `confirm_valid_time` datetime NULL DEFAULT NULL COMMENT '确认有效时间' AFTER `confirm_valid`;

