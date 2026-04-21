-- 线索类别广告费用表（投放分析）
CREATE TABLE IF NOT EXISTS `gh_opportunity_ad_cost` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `stat_year` int NOT NULL COMMENT '统计年',
  `stat_month` int NOT NULL DEFAULT 0 COMMENT '统计月：1-12，0表示全年',
  `opportunity_name_key` varchar(64) NOT NULL COMMENT '线索类别键值（优先sys_category.id）',
  `opportunity_name` varchar(200) DEFAULT NULL COMMENT '线索类别展示名',
  `ad_cost` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '广告费用',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` int NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_opp_ad_cost` (`stat_year`,`stat_month`,`opportunity_name_key`,`del_flag`),
  KEY `idx_opp_ad_cost_ym` (`stat_year`,`stat_month`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索类别广告费用';

