-- 删除租户管理、部门管理、职务管理、统计报表、报表设计、在线开发相关菜单
-- 执行前请备份数据库！

-- 1. 删除租户管理菜单及其子菜单
DELETE FROM sys_role_permission WHERE permission_id = '1280350452934307841';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1280350452934307841';
DELETE FROM sys_permission WHERE id = '1280350452934307841';

-- 2. 删除部门管理菜单及其子菜单
DELETE FROM sys_role_permission WHERE permission_id = '45c966826eeff4c99b8f8ebfe74511fc';
DELETE FROM sys_permission_data_rule WHERE permission_id = '45c966826eeff4c99b8f8ebfe74511fc';
DELETE FROM sys_permission WHERE id = '45c966826eeff4c99b8f8ebfe74511fc';

-- 3. 删除我的部门菜单
DELETE FROM sys_role_permission WHERE permission_id = '5c2f42277948043026b7a14692456828';
DELETE FROM sys_permission_data_rule WHERE permission_id = '5c2f42277948043026b7a14692456828';
DELETE FROM sys_permission WHERE id = '5c2f42277948043026b7a14692456828';

-- 4. 删除职务管理菜单及其子菜单
DELETE FROM sys_role_permission WHERE permission_id = '1174506953255182338';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1174506953255182338';
DELETE FROM sys_permission WHERE id = '1174506953255182338';

-- 5. 删除在线开发菜单及其所有子菜单
-- 先删除所有子菜单的权限关联（在线开发的所有子菜单ID）
DELETE FROM sys_role_permission WHERE permission_id IN (
    '109c78a583d4693ce2f16551b7786786', -- Online报表配置
    '1192318987661234177', -- 系统编码规则
    '1209731624921534465', -- 多数据源管理
    '1224641973866467330', -- 系统校验规则
    '1229674163694841857', -- AUTO在线表单ERP
    '1235823781053313025', -- AUTO在线内嵌子表
    '1304032910990495745', -- AUTO在线表单TAB
    '22d6a3d39a59dd7ea9a30acfa6bfb0a5', -- AUTO动态表单
    '54097c6a3cf50fad0793a34beff1efdf', -- AUTO在线表单
    '8d4683aacaa997ab86b966b464360338', -- Online表单开发
    '9fe26464838de2ea5e90f2367e35efa0', -- AUTO在线报表
    'f2849d3814fc97993bfc519ae6bbf049', -- AUTO复制表单
    'fba41089766888023411a978d13c0aa4'  -- AUTO树表单列表
);
DELETE FROM sys_permission_data_rule WHERE permission_id IN (
    '109c78a583d4693ce2f16551b7786786',
    '1192318987661234177',
    '1209731624921534465',
    '1224641973866467330',
    '1229674163694841857',
    '1235823781053313025',
    '1304032910990495745',
    '22d6a3d39a59dd7ea9a30acfa6bfb0a5',
    '54097c6a3cf50fad0793a34beff1efdf',
    '8d4683aacaa997ab86b966b464360338',
    '9fe26464838de2ea5e90f2367e35efa0',
    'f2849d3814fc97993bfc519ae6bbf049',
    'fba41089766888023411a978d13c0aa4'
);
-- 删除所有子菜单
DELETE FROM sys_permission WHERE id IN (
    '109c78a583d4693ce2f16551b7786786',
    '1192318987661234177',
    '1209731624921534465',
    '1224641973866467330',
    '1229674163694841857',
    '1235823781053313025',
    '1304032910990495745',
    '22d6a3d39a59dd7ea9a30acfa6bfb0a5',
    '54097c6a3cf50fad0793a34beff1efdf',
    '8d4683aacaa997ab86b966b464360338',
    '9fe26464838de2ea5e90f2367e35efa0',
    'f2849d3814fc97993bfc519ae6bbf049',
    'fba41089766888023411a978d13c0aa4'
);
-- 删除在线开发主菜单的权限关联
DELETE FROM sys_role_permission WHERE permission_id = 'e41b69c57a941a3bbcce45032fe57605';
DELETE FROM sys_permission_data_rule WHERE permission_id = 'e41b69c57a941a3bbcce45032fe57605';
-- 删除在线开发主菜单
DELETE FROM sys_permission WHERE id = 'e41b69c57a941a3bbcce45032fe57605';

-- 6. 删除报表设计菜单及其所有子菜单
-- 先删除所有子菜单的权限关联（报表设计的所有子菜单ID）
DELETE FROM sys_role_permission WHERE permission_id IN (
    '1205098241075453953', -- 生产销售监控
    '1205306106780364802', -- 智慧物流监控
    '1335960713267093506'  -- 积木报表设计
);
DELETE FROM sys_permission_data_rule WHERE permission_id IN (
    '1205098241075453953',
    '1205306106780364802',
    '1335960713267093506'
);
-- 删除所有子菜单
DELETE FROM sys_permission WHERE id IN (
    '1205098241075453953',
    '1205306106780364802',
    '1335960713267093506'
);
-- 删除报表设计主菜单的权限关联
DELETE FROM sys_role_permission WHERE permission_id = '1205097455226462210';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1205097455226462210';
-- 删除报表设计主菜单
DELETE FROM sys_permission WHERE id = '1205097455226462210';

-- 7. 删除其他使用在线开发组件的菜单（如果存在）
-- Online报表示例
DELETE FROM sys_role_permission WHERE permission_id = '1232123780958064642';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1232123780958064642';
DELETE FROM sys_permission WHERE id = '1232123780958064642';

-- online订单管理
DELETE FROM sys_role_permission WHERE permission_id = '1260922988733255681';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1260922988733255681';
DELETE FROM sys_permission WHERE id = '1260922988733255681';

-- online用户报表
DELETE FROM sys_role_permission WHERE permission_id = '1260923256208216065';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1260923256208216065';
DELETE FROM sys_permission WHERE id = '1260923256208216065';

-- 业绩汇总（使用在线报表组件）
DELETE FROM sys_role_permission WHERE permission_id = '1509165928773820417';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1509165928773820417';
DELETE FROM sys_permission WHERE id = '1509165928773820417';

-- 总收入（使用在线报表组件）
DELETE FROM sys_role_permission WHERE permission_id = '1512061519153528834';
DELETE FROM sys_permission_data_rule WHERE permission_id = '1512061519153528834';
DELETE FROM sys_permission WHERE id = '1512061519153528834';

-- 8. 删除统计报表相关的菜单（ViserChartDemo等）
DELETE FROM sys_role_permission WHERE permission_id = '020b06793e4de2eee0007f603000c769';
DELETE FROM sys_permission_data_rule WHERE permission_id = '020b06793e4de2eee0007f603000c769';
DELETE FROM sys_permission WHERE id = '020b06793e4de2eee0007f603000c769';

-- 注意：如果还有其他菜单使用了这些组件，需要手动检查并删除

