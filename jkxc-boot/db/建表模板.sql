-- ============================================
-- 数据库建表标准模板
-- 请严格按照此模板创建新表
-- ============================================

-- 表名：使用小写字母和下划线，具有描述性
CREATE TABLE IF NOT EXISTS `table_name` (
  -- 主键ID（标准格式）
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  
  -- 业务字段（根据实际需求调整）
  `name` VARCHAR(100) DEFAULT NULL COMMENT '名称',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '编码',
  `description` TEXT COMMENT '描述',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  
  -- 标准审计字段
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-正常，1-已删除',
  
  -- 主键约束
  PRIMARY KEY (`id`),
  
  -- 唯一索引
  UNIQUE KEY `uk_code` (`code`),
  
  -- 普通索引
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_del_flag` (`del_flag`)
  
  -- ⚠️ 重要：必须明确指定字符集和排序规则
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='表说明';

-- ============================================
-- 字段类型说明
-- ============================================
-- 字符串类型：
--   VARCHAR(长度)    - 可变长度字符串，最大65535字节
--   CHAR(长度)       - 固定长度字符串
--   TEXT             - 文本类型，最大65535字节
--   LONGTEXT         - 长文本类型，最大4GB
--
-- 数值类型：
--   TINYINT(1)       - 布尔值或状态标志（0/1）
--   INT(11)          - 整数
--   BIGINT(20)       - 大整数
--   DECIMAL(10,2)    - 精确小数
--
-- 日期时间类型：
--   DATETIME         - 日期时间
--   DATE             - 日期
--   TIME             - 时间
--   TIMESTAMP        - 时间戳（自动更新）

-- ============================================
-- 索引命名规范
-- ============================================
-- 主键：PRIMARY KEY (`id`)
-- 唯一索引：UNIQUE KEY `uk_字段名` (`字段名`)
-- 普通索引：KEY `idx_字段名` (`字段名`)
-- 组合索引：KEY `idx_字段1_字段2` (`字段1`, `字段2`)

-- ============================================
-- 外键约束示例（可选）
-- ============================================
-- ALTER TABLE `table_name` 
-- ADD CONSTRAINT `fk_table_name_foreign_key` 
-- FOREIGN KEY (`foreign_key_id`) REFERENCES `foreign_table` (`id`) 
-- ON DELETE CASCADE ON UPDATE CASCADE;

-- ============================================
-- 修改现有表的字符集（如果需要）
-- ============================================
-- ALTER TABLE `table_name` 
-- CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

