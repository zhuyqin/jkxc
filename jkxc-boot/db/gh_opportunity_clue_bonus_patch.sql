-- =============================================================================
-- 补丁：为 gh_opportunity 增加「线索奖金」相关字段
-- =============================================================================
-- 现象：Dashboard 等接口查询 clue_bonus 时报 Unknown column 'clue_bonus'
-- 原因：库表由旧版脚本创建，未包含下列列（完整定义见 jkxc-cg.sql）
-- 使用：在业务库中执行本脚本一次（若某列已存在，删除对应行后执行其余 ALTER）
-- =============================================================================

ALTER TABLE `gh_opportunity`
  ADD COLUMN `clue_bonus` decimal(18, 2) NULL DEFAULT NULL COMMENT '线索奖金' AFTER `del_flag`;

ALTER TABLE `gh_opportunity`
  ADD COLUMN `clue_bonus_awarded` tinyint(1) NOT NULL DEFAULT 0 COMMENT '线索奖金已发放标识' AFTER `clue_bonus`;

ALTER TABLE `gh_opportunity`
  ADD COLUMN `clue_bonus_awarded_time` datetime NULL DEFAULT NULL COMMENT '线索奖金发放时间' AFTER `clue_bonus_awarded`;
