-- ============================================
-- 修复客户画像和价值分析超时问题
-- 功能：添加必要的数据库索引以提升查询性能
-- 创建时间：2026-02-03
-- ============================================

-- 检查并创建gh_customer表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'gh_customer' 
               AND index_name = 'idx_create_time_del_flag');

SET @sqlstmt := IF(@exist > 0, 
    'SELECT ''索引 idx_create_time_del_flag 已存在'' AS message',
    'ALTER TABLE `gh_customer` ADD INDEX `idx_create_time_del_flag` (`create_time`, `del_flag`) COMMENT ''创建时间和删除标志联合索引''');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建gh_order表索引 - company_id
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'gh_order' 
               AND index_name = 'idx_company_id_create_time');

SET @sqlstmt := IF(@exist > 0, 
    'SELECT ''索引 idx_company_id_create_time 已存在'' AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_company_id_create_time` (`company_id`, `create_time`, `del_flag`) COMMENT ''公司ID、创建时间和删除标志联合索引''');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建gh_order表索引 - business_type
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'gh_order' 
               AND index_name = 'idx_business_type_del_flag');

SET @sqlstmt := IF(@exist > 0, 
    'SELECT ''索引 idx_business_type_del_flag 已存在'' AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_business_type_del_flag` (`business_type`, `del_flag`) COMMENT ''业务类型和删除标志联合索引''');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建gh_order表索引 - salesman
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'gh_order' 
               AND index_name = 'idx_salesman_del_flag');

SET @sqlstmt := IF(@exist > 0, 
    'SELECT ''索引 idx_salesman_del_flag 已存在'' AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_salesman_del_flag` (`salesman`, `del_flag`) COMMENT ''业务员和删除标志联合索引''');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建gh_order表索引 - salesman_create_time
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'gh_order' 
               AND index_name = 'idx_salesman_create_time');

SET @sqlstmt := IF(@exist > 0, 
    'SELECT ''索引 idx_salesman_create_time 已存在'' AS message',
    'ALTER TABLE `gh_order` ADD INDEX `idx_salesman_create_time` (`salesman`, `create_time`, `del_flag`) COMMENT ''业务员、创建时间和删除标志联合索引''');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示创建的索引
SELECT '索引创建完成，以下是gh_customer表的索引：' AS message;
SHOW INDEX FROM gh_customer WHERE Key_name LIKE 'idx_%';

SELECT '以下是gh_order表的索引：' AS message;
SHOW INDEX FROM gh_order WHERE Key_name LIKE 'idx_%';

-- ============================================
-- 使用说明：
-- 1. 连接到数据库：mysql -u用户名 -p密码 数据库名
-- 2. 执行此脚本：source jkxc-boot/db/fix_customer_analysis_timeout.sql
-- 3. 或者直接执行：mysql -u用户名 -p密码 数据库名 < jkxc-boot/db/fix_customer_analysis_timeout.sql
-- 
-- 预期效果：
-- - 客户画像分析页面加载时间从30秒以上降低到3秒以内
-- - 客户价值分析页面加载时间从30秒以上降低到3秒以内
-- - 不会再出现超时错误
-- ============================================
