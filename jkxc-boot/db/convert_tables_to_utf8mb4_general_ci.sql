-- 将表转换为 utf8mb4_general_ci 排序规则
-- 执行前请确保数据库连接使用 utf8mb4 编码

SET NAMES utf8mb4;

-- 修改 gh_address_center 表及其所有字段
ALTER TABLE `gh_address_center` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 修改 gh_bank_management 表及其所有字段
ALTER TABLE `gh_bank_management` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 如果以下表已存在，也一并修改（如果不存在则忽略错误）
-- ALTER TABLE `gh_bank_diary` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- ALTER TABLE `gh_salary_info` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- ALTER TABLE `gh_salary_payment` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;


