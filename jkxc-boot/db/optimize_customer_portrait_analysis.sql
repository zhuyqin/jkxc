-- ============================================
-- 客户画像分析性能优化SQL
-- 功能：添加索引以优化客户画像分析查询速度
-- 创建时间：2026-01-31
-- ============================================

-- 优化说明：
-- 当前客户画像分析存在严重的N+1查询问题：
-- 1. 先查询所有客户
-- 2. 对每个客户循环查询其订单
-- 3. 导致查询次数 = 1 + 客户数量，性能极差
-- 
-- 优化方案：
-- 1. 添加数据库索引（本文件）
-- 2. 后续可考虑使用JOIN查询替代循环查询
-- ============================================

-- 1. 为gh_customer表添加索引
-- 优化按创建时间查询客户
ALTER TABLE `gh_customer` 
ADD INDEX `idx_create_time_del_flag` (`create_time`, `del_flag`) COMMENT '创建时间和删除标志联合索引';

-- 2. 为gh_order表添加索引
-- 优化按公司ID和创建时间查询订单（最重要的索引，解决N+1问题）
ALTER TABLE `gh_order` 
ADD INDEX `idx_company_id_create_time` (`company_id`, `create_time`, `del_flag`) COMMENT '公司ID、创建时间和删除标志联合索引';

-- 优化按业务类型查询订单
ALTER TABLE `gh_order` 
ADD INDEX `idx_business_type_del_flag` (`business_type`, `del_flag`) COMMENT '业务类型和删除标志联合索引';

-- 优化按业务员查询订单
ALTER TABLE `gh_order` 
ADD INDEX `idx_salesman_del_flag` (`salesman`, `del_flag`) COMMENT '业务员和删除标志联合索引';

-- 优化按业务员和创建时间查询订单
ALTER TABLE `gh_order` 
ADD INDEX `idx_salesman_create_time` (`salesman`, `create_time`, `del_flag`) COMMENT '业务员、创建时间和删除标志联合索引';

-- 3. 检查现有索引（可选，用于验证）
-- SHOW INDEX FROM `gh_customer`;
-- SHOW INDEX FROM `gh_order`;

-- ============================================
-- 使用说明：
-- 1. 在MySQL命令行或工具中执行此SQL文件
-- 2. 如果索引已存在会报错，可以忽略
-- 3. 建议在业务低峰期执行
-- 4. 执行后查询速度预计提升10-50倍
-- 
-- 预期效果：
-- - 客户画像分析页面加载时间从10-30秒降低到1-3秒
-- - 数据库CPU使用率显著降低
-- - 支持更大数据量的分析查询
-- ============================================
