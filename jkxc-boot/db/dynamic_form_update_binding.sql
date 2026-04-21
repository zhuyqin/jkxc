-- 修改绑定表，允许一个表单绑定多个业务类型
-- 删除business_type的唯一约束

ALTER TABLE `sys_dynamic_form_binding` DROP INDEX `uk_business_type`;

-- 添加form_id和business_type的联合唯一索引（一个业务类型只能绑定一个表单，但一个表单可以绑定多个业务类型）
ALTER TABLE `sys_dynamic_form_binding` ADD UNIQUE KEY `uk_form_business` (`form_id`, `business_type`);

