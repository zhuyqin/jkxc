# 客户画像分析性能优化方案

## 问题诊断

当前代码存在严重的N+1查询问题，导致查询速度极慢：

### 问题代码位置
`GhCustomerController.getCustomerStatistics()` 方法中：

1. **第95-108行**：循环查询每个客户的订单（总体统计）
2. **第263-275行**：月度趋势中循环查询每个客户的订单
3. **第297-305行**：业务类型分布中循环查询每个客户的订单
4. **第351-359行**：业务员排名中循环查询每个客户的订单
5. **第405-417行**：每日趋势中循环查询每个客户的订单

### 性能影响
- 如果有100个客户，会执行 **500次** 订单查询
- 如果有1000个客户，会执行 **5000次** 订单查询
- 即使有索引，查询次数过多仍然导致性能极差

## 优化方案

### 方案1：批量查询（推荐）

将循环查询改为一次性批量查询所有订单，然后在内存中分组：

```java
// 一次性查询所有相关订单
List<String> customerIds = customers.stream()
    .map(GhCustomer::getId)
    .collect(Collectors.toList());

QueryWrapper<GhOrder> orderWrapper = new QueryWrapper<>();
orderWrapper.eq("del_flag", 0);
orderWrapper.in("company_id", customerIds);
List<GhOrder> allOrders = ghOrderService.list(orderWrapper);

// 按客户ID分组
Map<String, List<GhOrder>> ordersByCustomer = allOrders.stream()
    .collect(Collectors.groupingBy(GhOrder::getCompanyId));

// 然后在内存中处理
for (GhCustomer customer : customers) {
    List<GhOrder> orders = ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
    // 处理订单...
}
```

### 方案2：使用数据库聚合查询（最优）

直接在数据库层面进行聚合计算，避免在Java层面循环：

```sql
-- 总体统计
SELECT 
    COUNT(DISTINCT c.id) as totalCount,
    COALESCE(SUM(o.contract_amount), 0) as totalAmount
FROM gh_customer c
LEFT JOIN gh_order o ON c.id = o.company_id AND o.del_flag = 0
WHERE c.del_flag = 0
    AND c.create_time >= ?
    AND c.create_time <= ?
```

## 实施步骤

### 步骤1：执行索引SQL（已完成）
```bash
mysql -u用户名 -p密码 数据库名 < jkxc-boot/db/optimize_customer_portrait_analysis.sql
```

### 步骤2：修改代码使用批量查询

修改 `GhCustomerController.getCustomerStatistics()` 方法：

1. 在方法开始处，一次性查询所有订单
2. 将订单按客户ID分组到Map中
3. 后续所有循环都从Map中获取订单，而不是查询数据库

### 步骤3：修改业务人员月度明细方法

同样修改 `getCustomerSalesmanMonthlyDetail()` 方法，使用批量查询。

## 预期效果

- **查询次数**：从500次降低到1次
- **查询速度**：提升50-100倍
- **页面加载时间**：从10-30秒降低到0.5-2秒
- **数据库负载**：降低99%

## 代码示例

完整的优化代码示例见下方。

