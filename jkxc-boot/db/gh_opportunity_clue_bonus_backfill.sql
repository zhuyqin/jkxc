-- =============================================================================
-- 数据修复：将现有线索奖金按配置表回填入库
-- 口径：
--   gh_opportunity.opportunity_name = gh_opportunity_bonus_config.opportunity_name
-- 策略：
--   仅回填 gh_opportunity.clue_bonus IS NULL 的记录，不覆盖已有奖金
-- =============================================================================

-- 1) 回填前预览：将被更新的记录数
SELECT COUNT(1) AS will_update_count
FROM gh_opportunity o
JOIN gh_opportunity_bonus_config c
  ON TRIM(o.opportunity_name) = TRIM(c.opportunity_name)
WHERE o.del_flag = 0
  AND c.del_flag = 0
  AND o.clue_bonus IS NULL
  AND c.bonus_money IS NOT NULL;

-- 2) 执行回填（只更新奖金为空的线索）
UPDATE gh_opportunity o
JOIN gh_opportunity_bonus_config c
  ON TRIM(o.opportunity_name) = TRIM(c.opportunity_name)
SET
  o.clue_bonus = c.bonus_money,
  o.update_time = NOW()
WHERE o.del_flag = 0
  AND c.del_flag = 0
  AND o.clue_bonus IS NULL
  AND c.bonus_money IS NOT NULL;

-- 3) 回填后校验：仍为空但可匹配配置的数量（理论应为0）
SELECT COUNT(1) AS remain_null_but_matchable
FROM gh_opportunity o
JOIN gh_opportunity_bonus_config c
  ON TRIM(o.opportunity_name) = TRIM(c.opportunity_name)
WHERE o.del_flag = 0
  AND c.del_flag = 0
  AND o.clue_bonus IS NULL
  AND c.bonus_money IS NOT NULL;

