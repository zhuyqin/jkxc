# JKXC项目业务逻辑分析

## 项目概述

JKXC是基于Jeecg-Boot低代码平台开发的**企业管理系统**，主要服务于代账公司的业务管理需求，包含客户管理、订单管理、审批流程、财务管理、人力资源等核心业务模块。

## 技术架构

### 后端 (jkxc-boot)
- **基础框架**: Spring Boot 2.3.5.RELEASE
- **持久层**: MyBatis-Plus 3.4.3.1
- **安全框架**: Apache Shiro 1.7.0 + JWT 3.11.0
- **数据库**: MySQL 5.7+
- **缓存**: Redis
- **连接池**: Druid 1.1.22
- **其他**: Fastjson, POI, Swagger, Quartz, Lombok

### 前端 (jkxc-vue)
- **框架**: Vue.js
- **UI组件**: Ant Design Vue
- **构建工具**: Webpack, @vue/cli 3.2.1
- **HTTP客户端**: Axios
- **图表**: @antv/g2, Viser-vue

## 核心业务模块

### 1. 客户管理模块 (customer)

**功能**:
- 客户基本信息管理（公司名称、联系人、法人信息、税号等）
- 客户关系维护
- 客户状态管理

**数据表**: `gh_customer`

**关键字段**:
- 公司信息：corporate_name, legal_person_name, duty_paragraph(税号)
- 联系信息：contacts, contact_information, legal_person_phone
- 地址信息：region, address, registered_address, actual_address
- 财务信息：registered_capital, business_scope

### 2. 商机管理模块 (opportunity)

**功能**:
- 商机录入和跟踪
- 商机状态管理（进行中、已转化、已流失）
- 商机转化为订单

**数据表**: `gh_opportunity`

**业务流程**: 商机 → 跟进 → 转化为订单

### 3. 跟进记录模块 (followup)

**功能**:
- 记录客户跟进详情
- 跟进历史查询
- 跟进效果分析

**数据表**: `gh_followup_detail`

### 4. 订单管理模块 (order)

这是系统的**核心模块**，包含多个子功能：

#### 4.1 订单主表 (gh_order)

**关键字段**:
- 订单信息：order_no, company_id, company_name, salesman
- 业务信息：business_type, order_amount, operation_type
- 状态管理：order_status (0-待审核, 1-审核通过, 2-处理中, 3-已完成, 4-已驳回)
- 流程关联：matched_process_id, task_type

**业务类型**:
- 新签订单
- 续费订单
- 其他业务类型（通过sys_category配置）

#### 4.2 订单流程步骤 (gh_order_step)

**功能**: 记录订单审批流程的每个步骤

**步骤类型**:
- 0: 订单创建
- 1: 经理审核
- 2: 出纳审核
- 3: 任务分配
- 4: 任务处理
- 5: 任务完成
- 6: 订单完成
- 7: 订单驳回

#### 4.3 订单收费记录 (gh_order_payment)

**功能**: 管理订单的收款信息

**关键字段**:
- payment_time: 收费时间
- amount: 收费金额
- payment_method: 收款方式
- account_number: 收款账号
- status: 确认状态

#### 4.4 订单支出记录 (gh_order_expense)

**功能**: 记录订单相关的成本支出

**关键字段**:
- expense_time: 支出时间
- amount: 支出金额
- category: 支出类目
- audit_status: 审核状态
- payment_account: 付款账户

#### 4.5 业务任务 (gh_business_task)

**功能**: 订单审批通过后的具体业务任务管理

**任务类型**:
- 一次性任务
- 周期任务

#### 4.6 会计合同管理 (gh_accounting_contract)

**功能**: 管理代账合同信息

#### 4.7 会计交接管理 (gh_accounting_handover)

**功能**: 管理会计人员交接流程

### 5. 审批流程系统 (system/audit)

这是系统的**创新功能**，实现了可配置化的审批流程：

#### 5.1 流程定义 (sys_audit_process)

**功能**: 定义审批流程模板

**关键字段**:
- process_name: 流程名称
- process_code: 流程编码（唯一）
- task_type: 任务类型（once-一次性, recurring-周期）
- description: 流程描述
- status: 启用状态

#### 5.2 审批步骤 (sys_audit_step)

**功能**: 定义流程的具体审批步骤

**关键字段**:
- process_id: 所属流程
- step_order: 步骤顺序（支持并行审批）
- role_id: 审核角色
- description: 步骤描述

#### 5.3 业务类型绑定 (sys_audit_process_binding)

**功能**: 将业务类型绑定到审批流程

**特点**:
- 一个流程可以绑定多个业务类型
- 一个业务类型只能绑定一个流程（同一任务类型）
- 必须选择最底层的业务类型节点

#### 5.4 步骤表单配置 (sys_audit_step_form)

**功能**: 配置每个审批步骤的表单内容

**支持的组件**:
- 普通表单字段
- 特殊指标：
  - **下个流程审批人员选择**: 指定下一步的审批人
  - **成本填写**: 填写报销信息和金额

#### 5.5 审批任务 (sys_audit_task)

**功能**: 具体的审批任务实例

**关键字段**:
- business_id: 业务数据ID（如订单ID）
- business_type: 业务类型
- process_id: 流程ID
- step_id: 当前步骤ID
- assigned_user_id: 指定审批人
- task_status: 任务状态（pending, approved, rejected）
- audit_result: 审核结果（approved, rejected, returned）
- audit_data: 审核表单数据（JSON）

**数据权限**:
- **个人模式**: 只看自己的任务
- **团队模式**: 看整个角色的任务

#### 5.6 任务成本记录 (sys_audit_task_cost)

**功能**: 记录审批过程中填写的成本信息

**关键字段**:
- task_id: 任务ID
- reimbursement_name: 报销名称
- category_id: 报销类目
- amount: 金额

### 6. 财务管理模块

#### 6.1 报销管理 (reimbursement)

**数据表**: `gh_reimbursement`

**功能**:
- 报销申请
- 报销审批
- 报销记录查询

**关键字段**:
- 报销信息：reimbursement_name, category, amount
- 审批信息：audit_status, audit_data
- 付款信息：payment_account, payment_time

#### 6.2 银行流水管理 (bankdiary)

**数据表**: `gh_bank_diary`

**功能**:
- 银行日记账录入
- 流水分类管理
- 财务对账

**性能优化**: 已添加索引优化查询性能

#### 6.3 续费审核 (renew)

**数据表**: `gh_address_renew`

**功能**:
- 代账续费申请
- 续费审核流程
- 续费记录管理

#### 6.4 银行管理 (bank)

**数据表**: `gh_bank_management`

**功能**: 银行账户信息管理

### 7. 人力资源模块

#### 7.1 工资信息 (salary/ghSalaryInfo)

**功能**:
- 员工工资数据管理
- 工资结构配置
- 工资计算

**关键字段**:
- 基本工资、绩效、补贴、扣款等

#### 7.2 工资发放 (salary/ghSalaryPayment)

**功能**:
- 工资发放记录
- 发放状态跟踪

#### 7.3 团队管理 (system/team)

**数据表**: `sys_team`

**功能**:
- 团队组织架构
- 团队成员管理
- 团队权限控制

### 8. 供应商管理 (supplier)

**数据表**: `gh_supplier`

**功能**:
- 供货商信息管理
- 供货商地址管理

### 9. 地址中心 (address)

**数据表**: `gh_address_center`

**功能**: 集中管理各类地址信息

### 10. 运营分析模块 (operation)

提供多维度的数据分析功能：

#### 10.1 客户分析
- **客户画像分析** (CustomerPortraitAnalysis): 客户特征分析
- **客户价值分析** (CustomerValueAnalysis): 客户贡献度分析
- **流失分析** (LossAnalysis): 客户流失原因分析

#### 10.2 订单分析
- **新签订单分析** (NewOrderAnalysis): 新签订单趋势和统计
- **续费订单分析** (RenewalOrderAnalysis): 续费情况分析

#### 10.3 任务分析
- **任务中心分析** (TaskCenterAnalysis): 任务处理效率分析

### 11. 系统管理模块 (system)

#### 11.1 用户权限管理
- 用户管理 (SysUserController)
- 角色管理 (SysRoleController)
- 权限管理 (SysPermissionController)
- 部门管理
- 部门角色 (SysDepartRoleController)

#### 11.2 基础配置
- 字典管理 (SysDictController, SysDictItemController)
- 菜单管理
- 数据源管理 (SysDataSourceController)
- 分类管理 (SysCategoryController)

#### 11.3 动态表单 (dynamicform)

**数据表**: `sys_dynamic_form`, `sys_dynamic_form_binding`

**功能**:
- 动态表单设计
- 表单与业务绑定
- 表单数据管理

#### 11.4 系统监控
- 日志管理 (SysLogController, SysDataLogController)
- 在线用户 (SysUserOnlineController)
- 定时任务 (QuartzJobController)
- 系统监控

#### 11.5 消息通知 (message)
- 系统消息
- 消息模板
- WebSocket推送

## 核心业务流程

### 1. 订单审批流程

```
订单创建 
  ↓
根据业务类型自动匹配审批流程
  ↓
创建审批任务（支持并行审批）
  ↓
第一步审批（如：经理审核）
  ├─ 通过 → 进入下一步
  ├─ 不通过 → 订单状态变为"已驳回"
  └─ 驳回 → 订单回到"未审批"状态，可修改重新提交
  ↓
第二步审批（如：出纳审核）
  ├─ 通过 → 进入下一步
  └─ 不通过/驳回 → 同上
  ↓
所有审批通过
  ↓
创建业务任务
  ↓
任务处理
  ↓
任务完成
  ↓
订单完成
```

### 2. 可配置化审批流程

```
管理员配置流程
  ├─ 创建流程定义
  ├─ 配置审批步骤（角色、顺序）
  ├─ 绑定业务类型
  └─ 配置步骤表单
  
订单创建时
  ↓
根据业务类型查找绑定的流程
  ↓
自动创建审批任务
  ↓
任务路由到对应菜单（一次性/周期）
  ↓
审批人员处理任务
  ├─ 填写表单（包括特殊指标）
  ├─ 选择下个审批人（如果配置）
  └─ 填写成本信息（如果配置）
  ↓
审批完成后自动流转
```

### 3. 任务中心工作流

```
任务中心
  ├─ 一次性任务菜单
  │   ├─ 未审核 Tab
  │   ├─ 已审核 Tab
  │   └─ 已驳回 Tab
  └─ 周期任务菜单
      ├─ 未审核 Tab
      ├─ 已审核 Tab
      └─ 已驳回 Tab

每个Tab支持：
  ├─ 个人模式：只看自己的任务
  └─ 团队模式：看整个角色的任务
```

## 数据库设计特点

### 1. 主要数据表

**客户相关**:
- gh_customer: 客户表
- gh_opportunity: 商机表
- gh_followup_detail: 跟进记录表

**订单相关**:
- gh_order: 订单主表
- gh_order_step: 订单流程步骤表
- gh_order_payment: 订单收费记录表
- gh_order_expense: 订单支出记录表
- gh_business_task: 业务任务表
- gh_accounting_contract: 会计合同表
- gh_accounting_handover: 会计交接表

**审批流程**:
- sys_audit_process: 审核流程表
- sys_audit_step: 审核步骤表
- sys_audit_process_binding: 流程业务类型绑定表
- sys_audit_step_form: 审批步骤表单配置表
- sys_audit_form_indicator: 审批表单特殊指标配置表
- sys_audit_task: 审批任务表
- sys_audit_task_cost: 审批任务成本记录表

**财务相关**:
- gh_reimbursement: 报销表
- gh_bank_diary: 银行流水表
- gh_address_renew: 续费信息表
- gh_bank_management: 银行管理表

**人力资源**:
- gh_salary_info: 工资信息表
- gh_salary_payment: 工资发放表
- sys_team: 团队表

**其他**:
- gh_supplier: 供应商表
- gh_address_center: 地址中心表
- sys_dynamic_form: 动态表单表

### 2. 索引优化

系统已对高频查询字段添加索引：
- 订单状态、创建时间
- 审批任务状态、角色
- 银行流水日期、类别
- 客户名称、删除标志

### 3. 字符集

- 数据库字符集：utf8mb4
- 排序规则：utf8mb4_general_ci
- 支持中文、emoji等多语言字符

## API接口设计

### 命名规范

**后端Controller路径**:
- `/sys/*`: 系统管理相关
- `/order/*`: 订单管理相关
- `/customer/*`: 客户管理相关
- `/opportunity/*`: 商机管理相关
- `/salary/*`: 工资管理相关
- `/supplier/*`: 供应商管理相关
- `/renew/*`: 续费管理相关
- `/GhReimbursement/*`: 报销管理相关

**前端API调用**:
- 统一使用 Axios
- 请求拦截器处理 Token
- 响应拦截器处理错误

### 主要API端点

**订单管理**:
- `POST /order/add`: 创建订单（自动匹配流程）
- `GET /order/list`: 订单列表
- `PUT /order/edit`: 编辑订单
- `DELETE /order/delete`: 删除订单

**审批任务**:
- `GET /sys/auditTask/pendingList`: 待审核任务列表
- `GET /sys/auditTask/approvedList`: 已审核任务列表
- `GET /sys/auditTask/rejectedList`: 已驳回任务列表
- `POST /sys/auditTask/audit`: 执行审核操作
- `POST /sys/auditTask/resubmit`: 驳回后重新提交

**审批流程配置**:
- `POST /sys/auditProcess/saveBinding`: 保存业务类型绑定
- `GET /sys/auditProcess/getByBusinessType`: 根据业务类型查询流程
- `POST /sys/auditProcess/saveStepForm`: 保存步骤表单配置

## 前端页面结构

### 主要页面路径

**客户管理**: `/customer/CustomerList.vue`

**商机管理**: `/opportunity/OpportunityList.vue`

**订单管理**:
- `/order/OrderList.vue`: 订单列表
- `/order/FdNewOrderList.vue`: 新签订单
- `/order/RefundOrderList.vue`: 退款订单
- `/order/UnreceivedOrderList.vue`: 未收款订单
- `/order/BusinessTaskList.vue`: 业务任务

**会计管理**:
- `/accounting/AccountingContractList.vue`: 合同管理
- `/accounting/AccountingHandoverList.vue`: 交接管理

**财务管理**:
- `/finance/BankDiaryList.vue`: 银行流水
- `/finance/RenewalAuditList.vue`: 续费审核
- `/reimbursement/ReimbursementList.vue`: 报销管理

**工资管理**:
- `/salary/SalaryInfoList.vue`: 工资信息
- `/salary/SalaryPaymentList.vue`: 工资发放

**运营分析**:
- `/operation/CustomerPortraitAnalysis.vue`: 客户画像
- `/operation/CustomerValueAnalysis.vue`: 客户价值
- `/operation/LossAnalysis.vue`: 流失分析
- `/operation/NewOrderAnalysis.vue`: 新签订单分析
- `/operation/RenewalOrderAnalysis.vue`: 续费订单分析
- `/operation/TaskCenterAnalysis.vue`: 任务中心分析

**系统管理**:
- `/system/UserList.vue`: 用户管理
- `/system/RoleList.vue`: 角色管理
- `/system/PermissionList.vue`: 权限管理
- `/system/AuditProcessList.vue`: 审批流程管理
- `/system/OneTimeTaskList.vue`: 一次性任务中心
- `/system/RecurringTaskList.vue`: 周期任务中心
- `/system/TeamList.vue`: 团队管理
- `/system/DynamicFormList.vue`: 动态表单

## 当前开发进度

### ✅ 已完成

**后端**:
- 所有核心业务模块的 Entity、Mapper、Service、Controller
- 审批流程系统完整后端实现
- 订单自动匹配流程逻辑
- 审批任务的创建、查询、审核逻辑
- 数据权限控制（个人/团队模式）
- 驳回重新提交功能

**数据库**:
- 所有业务表结构
- 审批流程相关表
- 索引优化
- 字符集统一

**前端**:
- 基础页面框架
- 审批流程管理页面扩展（业务类型绑定、任务类型设置）
- 步骤表单配置弹窗基础框架
