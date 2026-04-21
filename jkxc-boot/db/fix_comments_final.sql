-- ============================================
-- 修复数据库表注释和字段注释乱码问题
-- 请在MySQL客户端中直接执行此脚本
-- 确保MySQL客户端使用UTF-8编码
-- ============================================

-- 设置连接字符集
SET NAMES utf8mb4;

-- 修复 gh_clue 表注释
ALTER TABLE `gh_clue` COMMENT = '线索管理表';

-- 修复 gh_clue 表字段注释
ALTER TABLE `gh_clue` MODIFY COLUMN `id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID';
ALTER TABLE `gh_clue` MODIFY COLUMN `clue_no` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '线索编号';
ALTER TABLE `gh_clue` MODIFY COLUMN `business_person` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务人员';
ALTER TABLE `gh_clue` MODIFY COLUMN `customer_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户名称';
ALTER TABLE `gh_clue` MODIFY COLUMN `region` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属区域';
ALTER TABLE `gh_clue` MODIFY COLUMN `customer_source` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户来源';
ALTER TABLE `gh_clue` MODIFY COLUMN `current_status` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '当前状态（意向a+、意向a、意向b、意向c、无效客户）';
ALTER TABLE `gh_clue` MODIFY COLUMN `follow_record` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '跟进记录';
ALTER TABLE `gh_clue` MODIFY COLUMN `assigned_user_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分配给的用户ID（个人线索）';
ALTER TABLE `gh_clue` MODIFY COLUMN `assigned_user_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分配给的用户名称';
ALTER TABLE `gh_clue` MODIFY COLUMN `is_public` TINYINT(1) DEFAULT 0 COMMENT '是否公海线索（0-否，1-是）';
ALTER TABLE `gh_clue` MODIFY COLUMN `follow_time` DATETIME DEFAULT NULL COMMENT '最后跟进时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `next_follow_time` DATETIME DEFAULT NULL COMMENT '下次跟进时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `remarks` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注';
ALTER TABLE `gh_clue` MODIFY COLUMN `create_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人';
ALTER TABLE `gh_clue` MODIFY COLUMN `create_time` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `update_by` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人';
ALTER TABLE `gh_clue` MODIFY COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `gh_clue` MODIFY COLUMN `del_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）';

-- 修复审核相关表的注释（表注释和字段注释已在 fix_comment_encoding_utf8mb4.sql 中修复）
-- 如果需要，可以执行 jkxc-boot/db/fix_comment_encoding_utf8mb4.sql 脚本

