-- 为线索备注增加图片字段
ALTER TABLE `gh_opportunity`
  ADD COLUMN `remark_images` text NULL COMMENT '备注图片（多图，逗号分隔或JSON数组）' AFTER `remarks`;

