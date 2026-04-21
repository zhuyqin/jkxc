-- ============================================
-- 修改地址商表(gh_supplier)的排序规则为utf8mb4_general_ci
-- 包括表级别的排序规则和所有字符类型列的排序规则
-- ============================================

-- 步骤1：修改表的默认排序规则
ALTER TABLE `gh_supplier` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

-- 步骤2：修改所有字符类型(varchar, char, text等)列的排序规则
ALTER TABLE `gh_supplier` 
MODIFY COLUMN `id` varchar(32) NOT NULL COMMENT '主键' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '更新人' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `supplier_company` varchar(200) DEFAULT NULL COMMENT '供应商公司' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `supplier_contact` varchar(100) DEFAULT NULL COMMENT '供应商联系人' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `supplier_contact_number` varchar(50) DEFAULT NULL COMMENT '供应商联系电话' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `used` varchar(10) DEFAULT NULL COMMENT '是否被用' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `used_company` varchar(200) DEFAULT NULL COMMENT '被用公司' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `legal_person` varchar(100) DEFAULT NULL COMMENT '法人姓名' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `legal_tel` varchar(50) DEFAULT NULL COMMENT '法人电话' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `address_requirements` varchar(500) DEFAULT NULL COMMENT '地址要求' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `pic_id` varchar(500) DEFAULT NULL COMMENT '图片凭证' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `remarks` varchar(500) DEFAULT NULL COMMENT '备注' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `bx_account` varchar(100) DEFAULT NULL COMMENT '报销账号' COLLATE utf8mb4_general_ci,
MODIFY COLUMN `bx_bank` varchar(100) DEFAULT NULL COMMENT '报销银行' COLLATE utf8mb4_general_ci;

-- 步骤3：验证修改结果（执行后可以运行这个查询确认）
-- SELECT 
--     TABLE_COLLATION as table_collation
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = DATABASE()
-- AND TABLE_NAME = 'gh_supplier';

-- SELECT 
--     COLUMN_NAME,
--     COLLATION_NAME as column_collation
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
-- AND TABLE_NAME = 'gh_supplier'
-- AND COLLATION_NAME IS NOT NULL
-- ORDER BY ORDINAL_POSITION;

