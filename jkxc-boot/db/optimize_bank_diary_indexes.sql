-- 优化银行日记表的查询性能，添加复合索引
-- 注意：基础索引（idx_bank_account_id, idx_del_flag, idx_create_time）在表创建时已存在

-- 创建复合索引：bank_account_id + del_flag + create_time（用于最常见的查询场景）
-- 这个复合索引可以大幅提升按账户查询并排序的性能
-- 查询条件：WHERE bank_account_id = ? AND del_flag = 0 ORDER BY create_time DESC
-- 这个索引可以覆盖整个查询，避免回表查询，大幅提升性能

-- 检查索引是否存在，如果不存在则创建
-- MySQL 5.7+ 不支持 IF NOT EXISTS，需要手动检查
-- 可以先执行：SHOW INDEX FROM gh_bank_diary WHERE Key_name = 'idx_bank_account_del_flag_create_time';

-- 如果索引不存在，执行以下SQL创建
CREATE INDEX `idx_bank_account_del_flag_create_time` ON `gh_bank_diary` (`bank_account_id`, `del_flag`, `create_time` DESC);

-- 如果上面的语句报错（索引已存在），可以忽略错误
-- 或者先删除再创建（谨慎操作）：
-- DROP INDEX `idx_bank_account_del_flag_create_time` ON `gh_bank_diary`;
-- CREATE INDEX `idx_bank_account_del_flag_create_time` ON `gh_bank_diary` (`bank_account_id`, `del_flag`, `create_time` DESC);
