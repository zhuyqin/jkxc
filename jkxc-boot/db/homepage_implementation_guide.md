# 首页实现指南

## 一、实现方案总结

### 推荐方案：基于角色的自动首页跳转 + 手动切换

**优点**：
1. 用户体验好：登录后自动跳转到合适的首页
2. 灵活性强：用户可以手动切换
3. 易于维护：通过角色配置管理

## 二、实施步骤

### 步骤1：配置角色首页映射

修改 `RoleIndexConfigEnum.java`，添加角色配置：

```java
// 在 RoleIndexConfigEnum 中添加
LEADER("leader", "dashboard/Dashboard"),      // 领导角色 → 数据统计页
EMPLOYEE("employee", "dashboard/Workbench"),  // 员工角色 → 工作台
MANAGER("manager", "dashboard/Dashboard"),    // 经理角色 → 数据统计页
```

### 步骤2：创建后端接口

创建 `DashboardController.java`，提供两个接口：
- `/dashboard/getStatistics` - 数据统计页数据
- `/dashboard/getWorkbench` - 工作台数据

### 步骤3：创建前端页面

创建两个Vue组件：
- `dashboard/Dashboard.vue` - 数据统计页
- `dashboard/Workbench.vue` - 工作台

### 步骤4：配置菜单（可选）

执行 `add_homepage_menus.sql` 创建菜单项，方便用户手动访问。

## 三、功能设计细节

### 数据统计页（Dashboard）功能

#### 1. 关键指标卡片（6个）
- 今日订单数/金额
- 本月订单数/金额
- 本年订单数/金额
- 客户总数
- 任务完成率
- 业务员数量

#### 2. 趋势图表
- 月度订单趋势（折线图）
- 业务类型分布（饼图）
- 业务员排名（柱状图）

#### 3. 数据表格
- 最近订单列表（Top 10）
- 高价值客户列表（Top 10）

### 工作台（Workbench）功能

#### 1. 我的统计卡片（4个）
- 我的待办任务数
- 我的客户数
- 我的订单数
- 我的业绩（本月）

#### 2. 待办任务列表
- 待审核任务
- 待处理订单
- 待跟进客户

#### 3. 快捷操作
- 创建订单
- 添加客户
- 创建任务
- 查看报表

#### 4. 系统通知
- 最新消息
- 待处理事项提醒

## 四、数据接口设计

### 1. 数据统计接口

**接口路径**：`GET /dashboard/getStatistics`

**返回数据**：
```json
{
  "success": true,
  "result": {
    "statistics": {
      "todayOrders": 10,
      "todayAmount": 50000,
      "monthOrders": 200,
      "monthAmount": 1000000,
      "yearOrders": 2000,
      "yearAmount": 10000000,
      "totalCustomers": 500,
      "taskCompletionRate": 85.5,
      "salesmanCount": 20
    },
    "monthlyTrend": [
      {"month": "1月", "orders": 150, "amount": 800000},
      ...
    ],
    "businessTypeDistribution": [
      {"name": "代理记账", "count": 100, "amount": 500000},
      ...
    ],
    "salesmanRank": [
      {"salesman": "张三", "orders": 50, "amount": 250000},
      ...
    ],
    "recentOrders": [...],
    "topCustomers": [...]
  }
}
```

### 2. 工作台接口

**接口路径**：`GET /dashboard/getWorkbench`

**返回数据**：
```json
{
  "success": true,
  "result": {
    "myStatistics": {
      "pendingTasks": 5,
      "myCustomers": 30,
      "myOrders": 50,
      "myPerformance": 200000
    },
    "pendingTasks": [
      {"id": "1", "title": "审核订单", "type": "order", "time": "2025-02-01"},
      ...
    ],
    "pendingOrders": [...],
    "quickActions": [
      {"name": "创建订单", "icon": "plus", "path": "/order/add"},
      ...
    ],
    "notifications": [...]
  }
}
```

## 五、前端页面结构

### Dashboard.vue 结构
```vue
<template>
  <div class="dashboard">
    <!-- 关键指标卡片 -->
    <a-row :gutter="16">
      <a-col :span="4" v-for="stat in statistics" :key="stat.key">
        <a-card>
          <a-statistic :title="stat.title" :value="stat.value" />
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 趋势图表 -->
    <a-row :gutter="16">
      <a-col :span="24">
        <a-card title="月度订单趋势">
          <div ref="trendChart"></div>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 业务类型分布和业务员排名 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="业务类型分布">
          <div ref="businessTypeChart"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="业务员排名">
          <div ref="salesmanRankChart"></div>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 数据表格 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="最近订单">
          <a-table :dataSource="recentOrders" :columns="orderColumns" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="高价值客户">
          <a-table :dataSource="topCustomers" :columns="customerColumns" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
```

### Workbench.vue 结构
```vue
<template>
  <div class="workbench">
    <!-- 欢迎信息和统计卡片 -->
    <a-row :gutter="16">
      <a-col :span="24">
        <a-card>
          <h2>{{ timeFix }}，{{ username }}！</h2>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="6" v-for="stat in myStatistics" :key="stat.key">
        <a-card>
          <a-statistic :title="stat.title" :value="stat.value" />
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 待办任务和快捷操作 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="待办任务">
          <a-list :dataSource="pendingTasks">
            <a-list-item slot="renderItem" slot-scope="item">
              <a-list-item-meta>
                <a slot="title">{{ item.title }}</a>
                <span slot="description">{{ item.time }}</span>
              </a-list-item-meta>
              <a-button type="link">处理</a-button>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="快捷操作">
          <a-row :gutter="16">
            <a-col :span="12" v-for="action in quickActions" :key="action.name">
              <a-button type="primary" block @click="handleQuickAction(action)">
                <a-icon :type="action.icon" />
                {{ action.name }}
              </a-button>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 待处理订单和系统通知 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="待处理订单">
          <a-table :dataSource="pendingOrders" :columns="orderColumns" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="系统通知">
          <a-list :dataSource="notifications">
            <a-list-item slot="renderItem" slot-scope="item">
              {{ item.content }}
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
```

## 六、使用建议

1. **角色配置**：根据实际业务角色配置首页映射
2. **数据缓存**：首页数据可以适当缓存，减少数据库压力
3. **权限控制**：确保用户只能看到有权限的数据
4. **响应式设计**：确保在不同屏幕尺寸下都能正常显示
5. **性能优化**：使用懒加载、分页等技术优化性能

## 七、后续扩展

1. **自定义首页**：允许用户自定义首页布局和内容
2. **数据导出**：支持导出统计数据为Excel
3. **实时更新**：使用WebSocket实现数据实时更新
4. **移动端适配**：开发移动端首页视图

