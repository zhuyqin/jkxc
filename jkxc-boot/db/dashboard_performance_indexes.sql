-- 首页/工作台统计相关索引（执行前请确认无同名索引，重复执行会因同名报错可忽略）
-- 库名：jkxc-cg（与 application-dev.yml 一致）

USE jkxc-cg;

-- gh_order：按创建时间范围统计、最近订单排序
CREATE INDEX idx_gh_order_del_flag_create_time ON gh_order (del_flag, create_time);

-- gh_order：按业务员统计去重客户
CREATE INDEX idx_gh_order_del_salesman_company ON gh_order (del_flag, salesman, company_id);

-- sys_audit_task：按年统计完成率
CREATE INDEX idx_audit_task_del_create ON sys_audit_task (del_flag, create_time);

-- gh_opportunity：线索奖金按月聚合
CREATE INDEX idx_gh_opp_bonus_month ON gh_opportunity (del_flag, clue_bonus_awarded, clue_bonus_awarded_time);
