-- =============================================================================
-- 数据修复：将历史线索全部设为“已确认有效”
-- =============================================================================
-- 约定：
-- confirm_valid：0-未确认有效；1-已确认有效
-- confirm_valid_time：确认有效时间
--
-- 使用：上线 confirm_valid 字段后执行一次
-- =============================================================================

UPDATE `gh_opportunity`
SET
  `confirm_valid` = 1,
  `confirm_valid_time` = COALESCE(`confirm_valid_time`, `update_time`, `modi_time`, `create_time`, NOW())
WHERE `del_flag` = 0;

