# 客户名称同步更新 - Java代码实现说明

## 📋 概述

本文档说明了在Java代码层面实现客户名称同步更新的逻辑，与数据库触发器方案互补，提供双重保障。

## 📦 实现文件

### 1. 核心服务接口
**文件：** `ICustomerNameSyncService.java`

**功能：**
- 定义客户名称同步服务接口
- 定义SyncResult结果类，包含各表更新记录数

**关键方法：**
```java
SyncResult syncCustomerNameToRelatedTables(String customerId, String newCorporateName);
```

---

### 2. 服务实现类
**文件：** `CustomerNameSyncServiceImpl.java`

**功能：**
- 实现客户名称同步逻辑
- 更新9个关联表的公司名称
- 记录详细的更新日志
- 支持事务回滚

**更新的表：**
1. gh_order（订单表）
2. gh_accounting_contract（代账合同表）
3. gh_accounting_handover（代账交接表）
4. gh_business_task（业务任务表）
5. gh_address_center（地址中心表）
6. gh_address_renew（地址续费表）
7. gh_bank_diary（银行日记账表）
8. gh_reimbursement（报销表）
9. gh_opportunity（商机表）

**关键特性：**
- 使用 `@Transactional` 保证事务一致性
- 使用 `UpdateWrapper` 构建更新条件
- 自动记录更新时间和更新人
- 详细的日志记录

---

### 3. 客户服务实现类（增强）
**文件：** `GhCustomerServiceImpl.java`

**功能：**
- 重写 `updateById` 方法
- 检测客户名称变化
- 自动调用同步服务

**核心逻辑：**
```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean updateById(GhCustomer entity) {
    // 1. 获取更新前的客户信息
    GhCustomer oldCustomer = this.getById(entity.getId());
    
    // 2. 执行客户信息更新
    boolean result = super.updateById(entity);
    
    // 3. 如果名称变化，同步更新关联表
    if (result && oldCustomer != null && 
        !entity.getCorporateName().equals(oldCustomer.getCorporateName())) {
        customerNameSyncService.syncCustomerNameToRelatedTables(
            entity.getId(), 
            entity.getCorporateName()
        );
    }
    
    return result;
}
```

---

### 4. Mapper接口
**文件：** `CustomerNameSyncMapper.java`

**功能：**
- 处理复杂的JOIN UPDATE操作
- 提供数据一致性检查方法

**关键方法：**
- `updateOpportunityNameByCustomerId()` - 更新商机表
- `checkOrderConsistency()` - 检查订单表一致性
- `checkContractConsistency()` - 检查合同表一致性
- `countInconsistentRecords()` - 统计不一致记录

---

### 5. Mapper XML配置
**文件：** `CustomerNameSyncMapper.xml`

**功能：**
- 定义复杂的SQL语句
- 实现JOIN UPDATE操作
- 提供数据一致性检查SQL

**示例SQL：**
```xml
<update id="updateOpportunityNameByCustomerId">
    UPDATE gh_opportunity opp
    INNER JOIN gh_customer_relation cr ON opp.id = cr.opportunity_id
    SET opp.corporate_name = #{newCorporateName},
        opp.update_time = NOW(),
        opp.update_by = 'customer_name_sync'
    WHERE cr.customer_id = #{customerId}
      AND opp.del_flag = 0
</update>
```

---

## 🔄 工作流程

### 1. 用户修改客户名称
```
用户在前端修改客户名称
    ↓
调用 GhCustomerController.edit()
    ↓
调用 GhCustomerService.updateById()
```

### 2. 检测名称变化
```
GhCustomerServiceImpl.updateById()
    ↓
获取旧客户信息
    ↓
执行更新操作
    ↓
比较新旧名称
```

### 3. 同步更新关联表
```
检测到名称变化
    ↓
调用 CustomerNameSyncService.syncCustomerNameToRelatedTables()
    ↓
依次更新9个关联表
    ↓
记录更新结果
```

### 4. 返回结果
```
返回 SyncResult
    ↓
包含各表更新记录数
    ↓
记录详细日志
```

---

## 🎯 关键特性

### 1. 事务管理
```java
@Transactional(rollbackFor = Exception.class)
public SyncResult syncCustomerNameToRelatedTables(...) {
    // 所有更新操作在同一事务中
    // 任何失败都会回滚
}
```

### 2. 智能检测
```java
// 只在名称真正变化时才同步
if (!entity.getCorporateName().equals(oldCustomer.getCorporateName())) {
    // 执行同步
}
```

### 3. 详细日志
```java
log.info("开始同步客户名称：customerId={}, newName={}", customerId, newCorporateName);
log.info("更新订单表：{} 条记录", orderCount);
log.info("客户名称同步完成：{}", result);
```

### 4. 容错处理
```java
try {
    // 同步逻辑
} catch (Exception e) {
    log.error("客户名称同步失败", e);
    // 可选择抛出异常回滚，或继续执行
}
```

### 5. 依赖注入保护
```java
@Autowired(required = false)
private GhOrderMapper ghOrderMapper;

if (ghOrderMapper == null) {
    log.warn("GhOrderMapper未注入，跳过订单表更新");
    return 0;
}
```

---

## 📊 更新策略

### 直接关联表（通过customer_id）
- gh_order
- gh_address_center
- gh_address_renew
- gh_bank_diary
- gh_reimbursement

**更新方式：**
```java
UpdateWrapper<GhOrder> updateWrapper = new UpdateWrapper<>();
updateWrapper.eq("company_id", customerId);
updateWrapper.eq("del_flag", 0);
updateWrapper.set("company_name", newCorporateName);
updateWrapper.set("update_time", new Date());
updateWrapper.set("update_by", "customer_name_sync");

return ghOrderMapper.update(null, updateWrapper);
```

### 间接关联表（通过order_id）
- gh_accounting_contract
- gh_accounting_handover
- gh_business_task

**更新方式：**
```java
// 1. 先查询客户的所有订单ID
List<GhOrder> orders = ghOrderMapper.selectList(orderWrapper);
List<String> orderIds = orders.stream()
    .map(GhOrder::getId)
    .collect(Collectors.toList());

// 2. 通过订单ID更新关联表
UpdateWrapper<GhAccountingContract> updateWrapper = new UpdateWrapper<>();
updateWrapper.in("order_id", orderIds);
updateWrapper.eq("del_flag", 0);
updateWrapper.set("company_name", newCorporateName);

return ghAccountingContractMapper.update(null, updateWrapper);
```

### 复杂关联表（通过关联表）
- gh_opportunity（通过gh_customer_relation）

**更新方式：**
```java
// 使用Mapper XML中的自定义SQL
return customerNameSyncMapper.updateOpportunityNameByCustomerId(
    customerId, 
    newCorporateName
);
```

---

## ⚠️ 注意事项

### 1. 与触发器的关系
- Java代码和数据库触发器可以共存
- 触发器作为最后一道防线
- Java代码提供更好的日志和控制

**建议：**
- 开发环境：使用Java代码（便于调试）
- 生产环境：同时使用Java代码和触发器（双重保障）

### 2. 性能考虑
- 单个客户更新通常很快（< 100ms）
- 如果客户关联数据量大，可能需要优化
- 考虑异步更新（使用消息队列）

### 3. 异常处理
当前实现：捕获异常但不抛出，避免影响客户更新

**可选策略：**
```java
// 策略1：忽略同步失败（当前实现）
catch (Exception e) {
    log.error("同步失败", e);
    // 不抛出异常
}

// 策略2：强制同步成功
catch (Exception e) {
    log.error("同步失败", e);
    throw e; // 抛出异常，回滚整个事务
}

// 策略3：异步重试
catch (Exception e) {
    log.error("同步失败", e);
    // 发送到消息队列，稍后重试
}
```

### 4. 批量更新
当前实现不支持批量更新客户名称的自动同步

**如需批量更新：**
```java
// 方法1：循环调用
for (GhCustomer customer : customers) {
    ghCustomerService.updateById(customer);
}

// 方法2：手动调用同步服务
for (GhCustomer customer : customers) {
    super.updateById(customer);
    customerNameSyncService.syncCustomerNameToRelatedTables(
        customer.getId(), 
        customer.getCorporateName()
    );
}
```

---

## 🧪 测试建议

### 1. 单元测试
```java
@Test
public void testSyncCustomerName() {
    // 1. 创建测试客户
    GhCustomer customer = new GhCustomer();
    customer.setCorporateName("测试公司A");
    ghCustomerService.save(customer);
    
    // 2. 创建测试订单
    GhOrder order = new GhOrder();
    order.setCompanyId(customer.getId());
    order.setCompanyName("测试公司A");
    ghOrderService.save(order);
    
    // 3. 修改客户名称
    customer.setCorporateName("测试公司B");
    ghCustomerService.updateById(customer);
    
    // 4. 验证订单名称已更新
    GhOrder updatedOrder = ghOrderService.getById(order.getId());
    assertEquals("测试公司B", updatedOrder.getCompanyName());
}
```

### 2. 集成测试
```java
@Test
public void testSyncAllTables() {
    // 创建完整的测试数据
    // 修改客户名称
    // 验证所有9个表都已更新
}
```

### 3. 性能测试
```java
@Test
public void testSyncPerformance() {
    // 创建大量关联数据
    // 测量更新时间
    // 验证性能指标
}
```

---

## 📝 使用示例

### 示例1：在Controller中使用
```java
@PutMapping(value = "/edit")
public Result<?> edit(@RequestBody GhCustomer ghCustomer) {
    // 直接调用updateById，自动触发同步
    ghCustomerService.updateById(ghCustomer);
    return Result.OK("编辑成功!");
}
```

### 示例2：手动调用同步服务
```java
@Autowired
private ICustomerNameSyncService customerNameSyncService;

public void manualSync(String customerId, String newName) {
    SyncResult result = customerNameSyncService
        .syncCustomerNameToRelatedTables(customerId, newName);
    
    if (result.isSuccess()) {
        log.info("同步成功：{}", result);
    } else {
        log.error("同步失败：{}", result.getMessage());
    }
}
```

### 示例3：检查同步结果
```java
SyncResult result = customerNameSyncService
    .syncCustomerNameToRelatedTables(customerId, newName);

System.out.println("订单表更新：" + result.getOrderCount() + " 条");
System.out.println("合同表更新：" + result.getContractCount() + " 条");
System.out.println("总计更新：" + result.getTotalCount() + " 条");
```

---

## 🔍 数据一致性检查

### 使用Mapper方法检查
```java
@Autowired
private CustomerNameSyncMapper customerNameSyncMapper;

// 检查订单表一致性
List<Map<String, Object>> inconsistentOrders = 
    customerNameSyncMapper.checkOrderConsistency();

// 统计所有表的不一致记录
List<Map<String, Object>> stats = 
    customerNameSyncMapper.countInconsistentRecords();
```

---

## 🚀 部署步骤

### 1. 编译项目
```bash
mvn clean compile
```

### 2. 运行测试
```bash
mvn test
```

### 3. 打包部署
```bash
mvn clean package
```

### 4. 验证功能
- 修改一个客户的名称
- 检查日志输出
- 验证关联表是否更新

---

## 📊 监控和日志

### 日志级别
```properties
# application.properties
logging.level.org.jeecg.modules.customer.service.impl.CustomerNameSyncServiceImpl=INFO
```

### 关键日志
```
INFO  - 开始同步客户名称：customerId=xxx, newName=xxx
INFO  - 更新订单表：5 条记录
INFO  - 更新代账合同表：3 条记录
INFO  - 客户名称同步完成：SyncResult{success=true, totalCount=15}
```

### 错误日志
```
ERROR - 客户名称同步失败：customerId=xxx, newName=xxx
ERROR - 更新订单表失败
```

---

## 🔧 故障排查

### 问题1：同步服务未注入
**症状：** 日志显示 "CustomerNameSyncService未注入"

**解决：**
- 检查 `@Service` 注解
- 检查Spring扫描路径
- 检查 `@Autowired(required = false)`

### 问题2：某个表未更新
**症状：** 部分表更新成功，部分表未更新

**解决：**
- 检查对应的Mapper是否注入
- 检查表结构和字段名
- 查看详细日志

### 问题3：性能问题
**症状：** 更新客户名称很慢

**解决：**
- 检查关联数据量
- 优化索引
- 考虑异步更新

---

## 📚 相关文档

- [客户名称同步更新方案.md](../../../../../../../db/客户名称同步更新方案.md)
- [客户名称同步_执行指南.md](../../../../../../../db/客户名称同步_执行指南.md)
- [sync_customer_name_update.sql](../../../../../../../db/sync_customer_name_update.sql)

---

**版本：** v1.0  
**更新日期：** 2026-03-01  
**维护人员：** 开发团队
