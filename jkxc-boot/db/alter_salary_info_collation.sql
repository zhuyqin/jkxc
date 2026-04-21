-- ============================================
-- 修改工资信息表(gh_salary_info)的排序规则为utf8mb4_general_ci
-- 包括表级别的排序规则和所有字符类型列的排序规则
-- ============================================

-- 步骤1：修改表的默认排序规则
ALTER TABLE `gh_salary_info` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

-- 步骤2：修改所有字符类型(varchar, char, text等)列的排序规则
ALTER TABLE `gh_salary_info` 
MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `user_id` varchar(32) NOT NULL COMMENT '员工ID（关联sys_user表）' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `user_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余字段，方便查询）' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `current_position` varchar(100) DEFAULT NULL COMMENT '当前职位' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `current_star_level` varchar(20) DEFAULT NULL COMMENT '当前星级' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `remarks` varchar(500) DEFAULT NULL COMMENT '备注' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人' COLLATE utf8mb4_general_ci;

-- 步骤3：验证修改结果（执行后可以运行这个查询确认）
-- SELECT 
--     TABLE_COLLATION as table_collation
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = DATABASE()
-- AND TABLE_NAME = 'gh_salary_info';

-- SELECT 
--     COLUMN_NAME,
--     COLLATION_NAME as column_collation
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
-- AND TABLE_NAME = 'gh_salary_info'
-- AND COLLATION_NAME IS NOT NULL
-- ORDER BY ORDINAL_POSITION;

