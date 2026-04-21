-- ============================================
-- 地址商表创建SQL脚本
-- 表名: gh_supplier
-- ============================================

CREATE TABLE IF NOT EXISTS `gh_supplier` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `supplier_company` varchar(200) DEFAULT NULL COMMENT '供应商公司',
  `supplier_contact` varchar(100) DEFAULT NULL COMMENT '供应商联系人',
  `supplier_contact_number` varchar(50) DEFAULT NULL COMMENT '供应商联系电话',
  `used` varchar(10) DEFAULT NULL COMMENT '是否被用',
  `used_company` varchar(200) DEFAULT NULL COMMENT '被用公司',
  `legal_person` varchar(100) DEFAULT NULL COMMENT '法人姓名',
  `legal_tel` varchar(50) DEFAULT NULL COMMENT '法人电话',
  `address_requirements` varchar(500) DEFAULT NULL COMMENT '地址要求',
  `pic_id` varchar(500) DEFAULT NULL COMMENT '图片凭证',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `bx_account` varchar(100) DEFAULT NULL COMMENT '报销账号',
  `bx_bank` varchar(100) DEFAULT NULL COMMENT '报销银行',
  `del_flag` int(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_company` (`supplier_company`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址商信息表';

