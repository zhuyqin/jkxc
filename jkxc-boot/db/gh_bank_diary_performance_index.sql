-- ============================================
-- 银行日记表性能优化索引
-- 用于提升查询性能
-- 注意：如果索引已存在，执行时会报错，可以忽略或先检查索引是否存在
-- ============================================

-- 1. 复合索引：bank_account_id + create_time（用于按账户查询时按创建时间排序）
-- 这是最常用的查询场景，在计算结余金额时也需要用到
CREATE INDEX `idx_bank_account_create_time` ON `gh_bank_diary` (`bank_account_id`, `create_time`);

-- 2. 复合索引：del_flag + create_time（用于列表查询时的排序）
CREATE INDEX `idx_del_flag_create_time` ON `gh_bank_diary` (`del_flag`, `create_time`);

-- 3. 复合索引：bank_account_id + del_flag + create_time（综合查询场景）
CREATE INDEX `idx_bank_account_del_create` ON `gh_bank_diary` (`bank_account_id`, `del_flag`, `create_time`);

-- 说明：
-- - idx_bank_account_create_time: 优化按账户ID查询并按创建时间排序的场景
-- - idx_del_flag_create_time: 优化列表查询（过滤已删除记录并排序）的场景
-- - idx_bank_account_del_create: 优化按账户查询且过滤已删除记录的复合场景
