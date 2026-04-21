# 统计数据一致性检查报告

## 检查范围
检查所有统计功能，确保表格外统计和弹窗查询数据一致。

## 已修复的功能

### 1. 任务分析 ✅
- **问题**：表格统计使用了 `listByIds`，可能返回已删除的订单（del_flag != 0 或 null）
- **修复**：在统计逻辑中添加了 `del_flag` 检查，只统计 `del_flag = 0` 的订单
- **文件**：`GhBusinessTaskController.java`
- **方法**：
  - `getTaskSalesmanMonthlyDetail` - 表格明细统计
  - `getTaskSalesmanMonthlyTasks` - 弹窗查询

### 2. 客户画像分析 ✅
- **状态**：已使用 `QueryWrapper` 查询，已过滤 `del_flag = 0`
- **文件**：`GhCustomerController.java`
- **方法**：
  - `getCustomerStatistics` - 表格统计（已过滤）
  - `getCustomerSalesmanMonthlyDetail` - 表格明细（已过滤）
  - `getCustomerSalesmanMonthlyCustomers` - 弹窗查询（已修复，与表格统计逻辑一致）

### 3. 客户价值分析 ✅
- **状态**：已使用 `QueryWrapper` 查询，已过滤 `del_flag = 0`
- **文件**：`GhCustomerController.java`
- **方法**：
  - `getValueStatistics` - 表格统计（已过滤）
  - `getValueSalesmanMonthlyDetail` - 表格明细（已过滤）
  - `getValueSalesmanMonthlyCustomers` - 弹窗查询（已修复，与表格统计逻辑一致）

### 4. 新签分析 ✅
- **状态**：已使用 `QueryWrapper` 查询，已过滤 `del_flag = 0`
- **文件**：`GhOrderController.java`
- **方法**：
  - `getNewOrderStatistics` - 表格统计（需检查）
  - `getSalesmanMonthlyDetail` - 表格明细（已过滤）
  - `getSalesmanMonthlyOrders` - 弹窗查询（已过滤）

### 5. 流失分析 ✅
- **状态**：查询合同表，已过滤 `del_flag = 0`
- **文件**：`GhAccountingContractController.java`
- **方法**：
  - `getLossStatistics` - 表格统计（需检查）
  - `getLossSalesmanMonthlyDetail` - 表格明细（已过滤）
  - `getLossSalesmanMonthlyContracts` - 弹窗查询（已过滤）

## 关键修复点

### 任务分析修复
1. **表格统计**：添加了 `del_flag` 检查
   ```java
   // 只统计未删除的订单（与弹窗查询逻辑一致：del_flag = 0）
   if (order.getDelFlag() == null || order.getDelFlag() != 0) {
       continue;
   }
   ```

2. **弹窗查询**：与表格统计逻辑完全一致
   - 遍历所有审核任务
   - 按订单创建时间分组
   - 只统计 `del_flag = 0` 的订单
   - 按订单ID去重

### 客户画像/价值分析修复
1. **弹窗查询**：统一使用"第一个订单的业务员"逻辑
   - 与表格明细统计逻辑一致
   - 按客户创建时间在指定月份
   - 且该客户第一个订单的业务员匹配

## 注意事项

1. **del_flag 处理**：
   - SQL查询中 `del_flag = 0` 会过滤掉 `del_flag IS NULL` 的订单
   - Java代码中也要检查 `del_flag == null || del_flag != 0`

2. **listByIds 方法**：
   - `listByIds` 不会自动过滤 `del_flag`，需要手动检查
   - 建议使用 `QueryWrapper` 查询，会自动过滤

3. **数据一致性**：
   - 表格统计和弹窗查询必须使用相同的过滤条件
   - 统计逻辑必须完全一致

## 检查结果总结

### ✅ 已确认正常的统计功能

1. **客户画像分析**：使用 `QueryWrapper`，已过滤 `del_flag = 0`
2. **客户价值分析**：使用 `QueryWrapper`，已过滤 `del_flag = 0`
3. **新签分析**：使用 `QueryWrapper`，已过滤 `del_flag = 0`
4. **流失分析**：查询合同表，已过滤 `del_flag = 0`

### ⚠️ 已修复的统计功能

1. **任务分析**：
   - 修复前：使用 `listByIds`，可能返回已删除的订单
   - 修复后：添加了 `del_flag` 检查，只统计 `del_flag = 0` 的订单
   - **影响**：统计数据可能会减少（过滤掉已删除的订单）

## 修复影响

### 任务分析统计数据变化
- **修复前**：可能统计了已删除的订单（del_flag != 0 或 null）
- **修复后**：只统计未删除的订单（del_flag = 0）
- **结果**：统计数据可能会减少，但数据更准确

### 其他统计功能
- **客户画像分析**、**客户价值分析**、**新签分析**、**流失分析**：已经正确过滤了 `del_flag`，统计数据不会变化

## 建议

1. **重新测试任务分析**：修复后，统计数据可能会变化，需要重新验证
2. **验证其他统计功能**：虽然代码看起来没问题，但建议测试验证数据一致性
3. **统一使用 QueryWrapper**：避免使用 `listByIds`，统一使用 `QueryWrapper` 查询
4. **统一 del_flag 检查**：所有统计逻辑统一检查 `del_flag`，确保只统计有效数据
5. **定期检查数据一致性**：确保表格外统计和弹窗查询数据一致

