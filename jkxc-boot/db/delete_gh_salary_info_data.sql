-- ============================================
-- 删除工资发放-信息维护表数据
-- 表名: gh_salary_info
-- 警告：此操作将删除所有工资信息数据，请谨慎执行！
-- 执行前请备份数据库！
-- ============================================

-- 物理删除所有数据（彻底清空表）
-- 注意：TRUNCATE 会重置自增ID，但此表使用varchar主键，不受影响
TRUNCATE TABLE `gh_salary_info`;

-- 验证删除结果
SELECT COUNT(*) AS remaining_count FROM `gh_salary_info`;

