# 首页使用指南

## 一、已创建的文件

### 后端文件
1. `DashboardController.java` - 首页数据统计控制器
   - 路径：`jkxc-boot/jeecg-boot-module-system/src/main/java/org/jeecg/modules/dashboard/controller/DashboardController.java`
   - 接口：
     - `GET /dashboard/getStatistics` - 获取数据统计页数据
     - `GET /dashboard/getWorkbench` - 获取工作台数据

### 前端文件
1. `Dashboard.vue` - 数据统计页（给领导看）
   - 路径：`jkxc-vue/src/views/dashboard/Dashboard.vue`
   - 功能：展示关键业务指标、趋势图表、数据表格

2. `Workbench.vue` - 工作台（给员工看）
   - 路径：`jkxc-vue/src/views/dashboard/Workbench.vue`
   - 功能：展示待办任务、快捷操作、我的统计

### 配置文件
1. `RoleIndexConfigEnum.java` - 角色首页配置
   - 已添加：
     - `LEADER("leader", "dashboard/Dashboard")` - 领导角色
     - `MANAGER("manager", "dashboard/Dashboard")` - 经理角色
     - `EMPLOYEE("employee", "dashboard/Workbench")` - 员工角色

## 二、配置步骤

### 步骤1：配置角色首页映射

在系统角色管理中，确保角色编码与 `RoleIndexConfigEnum` 中的配置一致：

- **领导角色**：角色编码设置为 `leader` → 自动跳转到数据统计页
- **经理角色**：角色编码设置为 `manager` → 自动跳转到数据统计页
- **员工角色**：角色编码设置为 `employee` → 自动跳转到工作台

**注意**：如果您的角色编码不同，需要修改 `RoleIndexConfigEnum.java` 中的配置。

### 步骤2：执行菜单配置SQL（可选）

如果需要将这两个页面添加到菜单中，执行：
```sql
-- 执行 jkxc-boot/db/add_homepage_menus.sql
```

### 步骤3：重启服务

1. 重启后端服务
2. 重启前端服务（如果需要）

## 三、功能说明

### 数据统计页（Dashboard）

**访问路径**：`/dashboard/statistics` 或通过角色自动跳转

**展示内容**：
1. **关键指标卡片（6个）**
   - 今日订单数/金额
   - 本月订单数/金额
   - 本年订单数/金额
   - 客户总数
   - 任务完成率
   - 业务员数量

2. **月度订单趋势图**
   - 折线图展示全年12个月的订单数和金额趋势

3. **业务类型分布图**
   - 饼图展示各业务类型的订单数量分布

4. **业务员排名图**
   - 横向柱状图展示Top 10业务员的订单数排名

5. **数据表格**
   - 最近订单列表（Top 10）
   - 高价值客户列表（Top 10）

### 工作台（Workbench）

**访问路径**：`/dashboard/workbench` 或通过角色自动跳转

**展示内容**：
1. **欢迎信息**
   - 根据时间显示问候语（早上好/上午好/下午好等）
   - 显示当前日期

2. **我的统计卡片（4个）**
   - 待办任务数
   - 我的客户数
   - 我的订单数（本月）
   - 我的业绩（本月）

3. **待办任务列表**
   - 显示待处理的审核任务
   - 支持点击"处理"按钮跳转到任务详情

4. **快捷操作**
   - 创建订单
   - 添加客户
   - 创建任务
   - 查看报表

5. **待处理订单列表**
   - 显示我的待处理订单
   - 支持点击"查看"按钮跳转到订单详情

6. **系统通知**
   - 显示系统通知消息

## 四、自定义配置

### 修改角色映射

如果需要修改角色与首页的映射关系，编辑 `RoleIndexConfigEnum.java`：

```java
// 例如：将"业务员"角色映射到工作台
SALESMAN("salesman", "dashboard/Workbench"),

// 例如：将"财务"角色映射到数据统计页
FINANCE("finance", "dashboard/Dashboard"),
```

### 修改快捷操作

在工作台页面中，快捷操作列表由后端接口返回。可以修改 `DashboardController.java` 中的 `getWorkbench` 方法：

```java
quickActions.add(createQuickAction("操作名称", "图标", "/路由路径"));
```

### 修改统计指标

在数据统计页中，可以修改 `DashboardController.java` 中的 `getStatistics` 方法，添加或删除统计指标。

## 五、注意事项

1. **权限控制**：确保用户有权限访问相关数据
2. **性能优化**：首页数据量较大，建议添加缓存
3. **数据准确性**：所有统计数据都过滤了已删除的记录（del_flag = 0）
4. **角色编码**：确保数据库中的角色编码与 `RoleIndexConfigEnum` 中的配置一致

## 六、常见问题

### Q1: 登录后没有自动跳转到对应的首页？
**A**: 检查：
1. 用户角色编码是否与 `RoleIndexConfigEnum` 中的配置一致
2. 角色编码是否正确设置
3. 前端路由配置是否正确

### Q2: 数据统计页显示的数据不准确？
**A**: 检查：
1. 后端接口是否正确过滤了已删除的记录
2. 时间范围是否正确
3. 数据库中的数据是否完整

### Q3: 工作台显示的待办任务为空？
**A**: 检查：
1. 数据库中是否有待处理的任务
2. 任务状态字段是否正确
3. 后端接口的查询条件是否正确

## 七、后续优化建议

1. **数据缓存**：首页数据可以缓存5-10分钟，减少数据库压力
2. **实时更新**：使用WebSocket实现数据实时更新
3. **自定义首页**：允许用户自定义首页布局和内容
4. **移动端适配**：开发移动端首页视图
5. **数据导出**：支持导出统计数据为Excel

