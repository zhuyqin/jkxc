package org.jeecg.modules.dashboard.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.customer.entity.GhCustomer;
import org.jeecg.modules.customer.service.IGhCustomerService;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.system.entity.SysAuditTask;
import org.jeecg.modules.system.service.ISysAuditTaskService;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.apache.shiro.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 首页数据统计控制器
 * @author jeecg-boot
 */
@Api(tags = "首页数据统计")
@RestController
@RequestMapping("/dashboard")
@Slf4j
public class DashboardController {

    @Autowired(required = false)
    private IGhOrderService ghOrderService;

    @Autowired(required = false)
    private IGhCustomerService ghCustomerService;

    @Autowired(required = false)
    private ISysAuditTaskService sysAuditTaskService;

    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    @Autowired(required = false)
    private ISysUserService sysUserService;
    
    @Autowired(required = false)
    private ISysRoleService sysRoleService;
    
    @Autowired(required = false)
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired(required = false)
    private org.jeecg.modules.order.mapper.GhOrderMapper ghOrderMapper;

    @Autowired(required = false)
    private org.jeecg.modules.system.mapper.SysAuditTaskMapper sysAuditTaskMapper;

    @Autowired(required = false)
    private org.jeecg.modules.opportunity.mapper.GhOpportunityMapper ghOpportunityMapper;

    /**
     * 获取数据统计页数据（给领导看）
     */
    @AutoLog(value = "首页-获取数据统计")
    @ApiOperation(value = "获取数据统计页数据", notes = "获取数据统计页数据")
    @GetMapping(value = "/getStatistics")
    public Result<?> getStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 获取当前年份和月份
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int currentYear = calendar.get(java.util.Calendar.YEAR);
            int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1;

            // 1. 关键指标统计
            Map<String, Object> statistics = new HashMap<>();

            // 今日统计
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date todayStart = calendar.getTime();

            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
            calendar.set(java.util.Calendar.MINUTE, 59);
            calendar.set(java.util.Calendar.SECOND, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date todayEnd = calendar.getTime();

            // 本月区间
            calendar.set(currentYear, currentMonth - 1, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date monthStart = calendar.getTime();

            calendar.set(currentYear, currentMonth - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date monthEnd = calendar.getTime();

            // 本年区间
            calendar.set(currentYear, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();

            calendar.set(currentYear, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();

            if (ghOrderMapper != null) {
                Map<String, Object> kpi = ghOrderMapper.getDashboardOrderKpis(
                        todayStart, todayEnd, monthStart, monthEnd, yearStart, yearEnd);
                if (kpi != null) {
                    statistics.put("todayOrders", dashboardLong(kpi.get("todayOrders")));
                    statistics.put("todayAmount", dashboardDecimal(kpi.get("todayAmount")));
                    statistics.put("monthOrders", dashboardLong(kpi.get("monthOrders")));
                    statistics.put("monthAmount", dashboardDecimal(kpi.get("monthAmount")));
                    statistics.put("yearOrders", dashboardLong(kpi.get("yearOrders")));
                    statistics.put("yearAmount", dashboardDecimal(kpi.get("yearAmount")));
                } else {
                    statistics.put("todayOrders", 0);
                    statistics.put("todayAmount", BigDecimal.ZERO);
                    statistics.put("monthOrders", 0);
                    statistics.put("monthAmount", BigDecimal.ZERO);
                    statistics.put("yearOrders", 0);
                    statistics.put("yearAmount", BigDecimal.ZERO);
                }
            } else {
                statistics.put("todayOrders", 0);
                statistics.put("todayAmount", BigDecimal.ZERO);
                statistics.put("monthOrders", 0);
                statistics.put("monthAmount", BigDecimal.ZERO);
                statistics.put("yearOrders", 0);
                statistics.put("yearAmount", BigDecimal.ZERO);
            }

            // 客户总数（包含del_flag为0或null的客户）
            if (ghCustomerService != null) {
                QueryWrapper<GhCustomer> customerWrapper = new QueryWrapper<>();
                customerWrapper.and(wrapper -> wrapper.eq("del_flag", 0).or().isNull("del_flag"));
                int totalCustomers = ghCustomerService.count(customerWrapper);
                statistics.put("totalCustomers", totalCustomers);
            } else {
                statistics.put("totalCustomers", 0);
            }

            // 任务完成率（SQL 聚合，避免加载全年任务到内存）
            if (sysAuditTaskMapper != null) {
                Map<String, Object> taskAgg = sysAuditTaskMapper.selectDashboardTaskCompletion(yearStart, yearEnd);
                long totalTasks = taskAgg != null ? dashboardLong(taskAgg.get("total")) : 0L;
                long completedTasks = taskAgg != null ? dashboardLong(taskAgg.get("completed")) : 0L;
                BigDecimal completionRate = totalTasks == 0 ? BigDecimal.ZERO :
                    new BigDecimal(completedTasks)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalTasks), 2, BigDecimal.ROUND_HALF_UP);
                statistics.put("taskCompletionRate", completionRate);
            } else {
                statistics.put("taskCompletionRate", BigDecimal.ZERO);
            }

            // 业务员数量（使用优化的SQL查询）
            if (ghOrderMapper != null) {
                int salesmanCount = ghOrderMapper.getDistinctSalesmanCount();
                statistics.put("salesmanCount", salesmanCount);
            } else {
                statistics.put("salesmanCount", 0);
            }

            // 线索奖金统计（本月，SQL 聚合）
            if (ghOpportunityMapper != null) {
                Map<String, Object> bonusAgg = ghOpportunityMapper.selectClueBonusMonthStats(monthStart, monthEnd);
                statistics.put("clueBonusThisMonthCount",
                        bonusAgg != null ? (int) dashboardLong(bonusAgg.get("cnt")) : 0);
                statistics.put("clueBonusThisMonthAmount",
                        bonusAgg != null ? dashboardDecimal(bonusAgg.get("amt")) : BigDecimal.ZERO);
            } else {
                statistics.put("clueBonusThisMonthCount", 0);
                statistics.put("clueBonusThisMonthAmount", BigDecimal.ZERO);
            }

            result.put("statistics", statistics);

            // 2. 月度趋势数据（使用优化的SQL聚合查询）
            List<Map<String, Object>> monthlyTrend = new ArrayList<>();
            if (ghOrderMapper != null) {
                List<Map<String, Object>> dbMonthlyTrend = ghOrderMapper.getMonthlyTrend(currentYear);
                // 创建一个Map方便查找
                Map<Integer, Map<String, Object>> monthDataMap = new HashMap<>();
                for (Map<String, Object> data : dbMonthlyTrend) {
                    Integer month = (Integer) data.get("month");
                    monthDataMap.put(month, data);
                }
                
                // 填充12个月的数据
                for (int m = 1; m <= 12; m++) {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", m + "月");
                    if (monthDataMap.containsKey(m)) {
                        Map<String, Object> dbData = monthDataMap.get(m);
                        monthData.put("orders", dbData.get("orders"));
                        monthData.put("amount", dbData.get("amount"));
                    } else {
                        monthData.put("orders", 0);
                        monthData.put("amount", BigDecimal.ZERO);
                    }
                    monthlyTrend.add(monthData);
                }
            }
            result.put("monthlyTrend", monthlyTrend);

            // 3. 业务类型分布（使用优化的SQL聚合查询）
            List<Map<String, Object>> businessTypeDistribution = new ArrayList<>();
            if (ghOrderMapper != null) {
                businessTypeDistribution = ghOrderMapper.getBusinessTypeDistribution(yearStart, yearEnd);
                java.util.Set<String> typeCodes = new java.util.HashSet<>();
                for (Map<String, Object> btData : businessTypeDistribution) {
                    String businessType = (String) btData.get("businessType");
                    if (oConvertUtils.isNotEmpty(businessType) && !"其他".equals(businessType)) {
                        typeCodes.add(businessType);
                    }
                }
                Map<String, String> typeNameByCode = new HashMap<>();
                if (sysCategoryService != null) {
                    for (String code : typeCodes) {
                        try {
                            List<String> names = sysCategoryService.loadDictItem(code, false);
                            if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
                                typeNameByCode.put(code, names.get(0));
                            } else {
                                typeNameByCode.put(code, code);
                            }
                        } catch (Exception e) {
                            log.warn("获取业务类型名称失败: {}", code, e);
                            typeNameByCode.put(code, code);
                        }
                    }
                }
                for (Map<String, Object> btData : businessTypeDistribution) {
                    String businessType = (String) btData.get("businessType");
                    if (oConvertUtils.isNotEmpty(businessType) && !"其他".equals(businessType)) {
                        btData.put("name", typeNameByCode.getOrDefault(businessType, businessType));
                    } else {
                        btData.put("name", "其他");
                    }
                }
            }
            result.put("businessTypeDistribution", businessTypeDistribution);

            // 4. 业务员排名（Top 10，使用优化的SQL聚合查询）
            List<Map<String, Object>> salesmanRank = new ArrayList<>();
            if (ghOrderMapper != null) {
                salesmanRank = ghOrderMapper.getSalesmanRank(yearStart, yearEnd, 10);
            }
            result.put("salesmanRank", salesmanRank);

            // 5. 最近订单（Top 10，只查展示列）
            List<Map<String, Object>> recentOrders = new ArrayList<>();
            if (ghOrderMapper != null) {
                List<Map<String, Object>> rows = ghOrderMapper.getRecentOrdersSummary(10);
                recentOrders = rows != null ? rows : new ArrayList<>();
            }
            result.put("recentOrders", recentOrders);

            // 6. 高价值客户（Top 10，使用优化的SQL聚合查询）
            List<Map<String, Object>> topCustomers = new ArrayList<>();
            if (ghOrderMapper != null) {
                topCustomers = ghOrderMapper.getTopValueCustomers(10);
            }
            result.put("topCustomers", topCustomers);

            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取数据统计失败", e);
            return Result.error("获取数据统计失败：" + e.getMessage());
        }
    }
    /**
     * 获取工作台数据（给员工看）
     */
    @AutoLog(value = "首页-获取工作台数据")
    @ApiOperation(value = "获取工作台数据", notes = "获取工作台数据")
    @GetMapping(value = "/getWorkbench")
    public Result<?> getWorkbench() {
        try {
            // 获取当前登录用户
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser == null) {
                return Result.error("请先登录");
            }

            String username = loginUser.getUsername();
            String realname = loginUser.getRealname();

            Map<String, Object> result = new HashMap<>();

            // 获取当前年份和月份
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int currentYear = calendar.get(java.util.Calendar.YEAR);
            int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1;

            calendar.set(currentYear, currentMonth - 1, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date monthStart = calendar.getTime();

            calendar.set(currentYear, currentMonth - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date monthEnd = calendar.getTime();

            // 1. 我的统计
            Map<String, Object> myStatistics = new HashMap<>();

            // 待办一次性业务数量和待办周期性业务数量（个人模式：统计我有权限审核的任务）
            int pendingOneTimeTasksCount = 0;
            int pendingRecurringTasksCount = 0;
            
            if (sysAuditTaskService != null && sysUserRoleMapper != null) {
                // 获取当前用户的角色ID列表
                List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(username);
                
                // 查询一次性待办任务数量（个人模式：不传dataRoleIds，只统计我有权限审核的）
                Page<SysAuditTask> oneTimePage = new Page<>(1, 1);
                IPage<SysAuditTask> oneTimeTaskPage = sysAuditTaskService.getPendingTasks(
                    oneTimePage, "once", loginUser.getId(), roleIds, true, null, null, null, null, null);
                pendingOneTimeTasksCount = (int) oneTimeTaskPage.getTotal();
                
                // 查询周期性待办任务数量（个人模式：不传dataRoleIds，只统计我有权限审核的）
                Page<SysAuditTask> recurringPage = new Page<>(1, 1);
                IPage<SysAuditTask> recurringTaskPage = sysAuditTaskService.getPendingTasks(
                    recurringPage, "recurring", loginUser.getId(), roleIds, true, null, null, null, null, null);
                pendingRecurringTasksCount = (int) recurringTaskPage.getTotal();
            }
            
            myStatistics.put("pendingOneTimeTasks", pendingOneTimeTasksCount);
            myStatistics.put("pendingRecurringTasks", pendingRecurringTasksCount);

            // 我的客户数（COUNT DISTINCT，避免拉全量订单）
            int myCustomersCount = 0;
            if (ghOrderMapper != null && oConvertUtils.isNotEmpty(realname)) {
                myCustomersCount = ghOrderMapper.countDistinctCustomersBySalesman(realname);
            }
            myStatistics.put("myCustomers", myCustomersCount);

            // 我的订单数（本月）
            int myOrdersCount = 0;
            if (ghOrderService != null) {
                QueryWrapper<GhOrder> myOrderWrapper = new QueryWrapper<>();
                myOrderWrapper.eq("del_flag", 0);
                myOrderWrapper.eq("salesman", realname);
                myOrderWrapper.ge("create_time", monthStart);
                myOrderWrapper.le("create_time", monthEnd);
                myOrdersCount = ghOrderService.count(myOrderWrapper);
                
            }
            myStatistics.put("myOrders", myOrdersCount);

            result.put("myStatistics", myStatistics);

            // 2. 待办任务列表（分为一次性和周期性）- 个人模式
            Map<String, Object> pendingTasksMap = new HashMap<>();
            List<Map<String, Object>> oneTimeTasks = new ArrayList<>();
            List<Map<String, Object>> recurringTasks = new ArrayList<>();
            
            if (sysAuditTaskService != null && sysUserRoleMapper != null) {
                // 获取当前用户的角色ID列表
                List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(username);
                
                // 查询一次性任务（个人模式：不传dataRoleIds）
                Page<SysAuditTask> oneTimePage = new Page<>(1, 5);
                IPage<SysAuditTask> oneTimeTaskPage = sysAuditTaskService.getPendingTasks(
                    oneTimePage, "once", loginUser.getId(), roleIds, true, null, null, null, null, null);
                List<SysAuditTask> oneTimeTaskList = oneTimeTaskPage.getRecords();

                // 批量查询关联的订单信息（一次性任务）
                Map<String, GhOrder> oneTimeOrderMap = new HashMap<>();
                if (ghOrderService != null && !oneTimeTaskList.isEmpty()) {
                    Set<String> oneTimeOrderIds = oneTimeTaskList.stream()
                        .map(SysAuditTask::getOrderId)
                        .filter(id -> oConvertUtils.isNotEmpty(id))
                        .collect(Collectors.toSet());
                    
                    if (!oneTimeOrderIds.isEmpty()) {
                        QueryWrapper<GhOrder> oneTimeOrderWrapper = new QueryWrapper<>();
                        oneTimeOrderWrapper.eq("del_flag", 0);
                        oneTimeOrderWrapper.in("id", oneTimeOrderIds);
                        List<GhOrder> oneTimeOrders = ghOrderService.list(oneTimeOrderWrapper);
                        for (GhOrder order : oneTimeOrders) {
                            oneTimeOrderMap.put(order.getId(), order);
                        }
                    }
                }

                for (SysAuditTask task : oneTimeTaskList) {
                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("id", task.getId());
                    taskData.put("type", "once");
                    taskData.put("taskType", task.getTaskType());
                    taskData.put("time", task.getCreateTime());
                    taskData.put("status", task.getTaskStatus());
                    
                    // 获取关联的订单信息
                    GhOrder order = null;
                    if (oConvertUtils.isNotEmpty(task.getOrderId())) {
                        order = oneTimeOrderMap.get(task.getOrderId());
                    }
                    
                    // 构建标题，包含公司名称
                    String title = "审核任务：一次性任务";
                    String companyName = "";
                    String orderNo = "";
                    Date orderCreateTime = null;
                    
                    if (order != null) {
                        companyName = order.getCompanyName() != null ? order.getCompanyName() : "";
                        orderNo = order.getOrderNo() != null ? order.getOrderNo() : "";
                        orderCreateTime = order.getCreateTime();
                        if (oConvertUtils.isNotEmpty(companyName)) {
                            title = "审核任务：" + companyName;
                        }
                    }
                    
                    taskData.put("title", title);
                    taskData.put("companyName", companyName);
                    taskData.put("orderNo", orderNo);
                    taskData.put("orderCreateTime", orderCreateTime);
                    taskData.put("orderId", task.getOrderId());
                    
                    oneTimeTasks.add(taskData);
                }
                
                // 查询周期性任务（个人模式：不传dataRoleIds）
                Page<SysAuditTask> recurringPage = new Page<>(1, 5);
                IPage<SysAuditTask> recurringTaskPage = sysAuditTaskService.getPendingTasks(
                    recurringPage, "recurring", loginUser.getId(), roleIds, true, null, null, null, null, null);
                List<SysAuditTask> recurringTaskList = recurringTaskPage.getRecords();

                // 批量查询关联的订单信息
                Map<String, GhOrder> orderMap = new HashMap<>();
                if (ghOrderService != null && !recurringTaskList.isEmpty()) {
                    Set<String> orderIds = recurringTaskList.stream()
                        .map(SysAuditTask::getOrderId)
                        .filter(id -> oConvertUtils.isNotEmpty(id))
                        .collect(Collectors.toSet());
                    
                    if (!orderIds.isEmpty()) {
                        QueryWrapper<GhOrder> orderWrapper = new QueryWrapper<>();
                        orderWrapper.eq("del_flag", 0);
                        orderWrapper.in("id", orderIds);
                        List<GhOrder> orders = ghOrderService.list(orderWrapper);
                        for (GhOrder order : orders) {
                            orderMap.put(order.getId(), order);
                        }
                    }
                }

                for (SysAuditTask task : recurringTaskList) {
                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("id", task.getId());
                    taskData.put("type", "recurring");
                    taskData.put("taskType", task.getTaskType());
                    taskData.put("time", task.getCreateTime());
                    taskData.put("status", task.getTaskStatus());
                    
                    // 获取关联的订单信息
                    GhOrder order = null;
                    if (oConvertUtils.isNotEmpty(task.getOrderId())) {
                        order = orderMap.get(task.getOrderId());
                    }
                    
                    // 构建标题，包含公司名称和订单时间
                    String title = "审核任务：周期任务";
                    String companyName = "";
                    String orderNo = "";
                    Date orderCreateTime = null;
                    
                    if (order != null) {
                        companyName = order.getCompanyName() != null ? order.getCompanyName() : "";
                        orderNo = order.getOrderNo() != null ? order.getOrderNo() : "";
                        orderCreateTime = order.getCreateTime();
                        if (oConvertUtils.isNotEmpty(companyName)) {
                            title = "审核任务：" + companyName;
                        }
                    }
                    
                    taskData.put("title", title);
                    taskData.put("companyName", companyName);
                    taskData.put("orderNo", orderNo);
                    taskData.put("orderCreateTime", orderCreateTime);
                    taskData.put("orderId", task.getOrderId());
                    
                    recurringTasks.add(taskData);
                }
            }
            
            pendingTasksMap.put("oneTime", oneTimeTasks);
            pendingTasksMap.put("recurring", recurringTasks);
            result.put("pendingTasks", pendingTasksMap);

            // 3. 待处理订单 - 个人模式（只显示我的订单）
            List<Map<String, Object>> pendingOrders = new ArrayList<>();
            if (ghOrderService != null && oConvertUtils.isNotEmpty(realname)) {
                QueryWrapper<GhOrder> pendingOrderWrapper = new QueryWrapper<>();
                pendingOrderWrapper.eq("del_flag", 0);
                pendingOrderWrapper.eq("salesman", realname);
                pendingOrderWrapper.orderByDesc("create_time");
                pendingOrderWrapper.last("LIMIT 10");
                List<GhOrder> pendingOrderList = ghOrderService.list(pendingOrderWrapper);

                for (GhOrder order : pendingOrderList) {
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("id", order.getId());
                    orderData.put("orderNo", order.getOrderNo());
                    orderData.put("companyName", order.getCompanyName());
                    orderData.put("amount", order.getContractAmount() != null ? order.getContractAmount() :
                        (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO));
                    orderData.put("createTime", order.getCreateTime());
                    pendingOrders.add(orderData);
                }
            }
            result.put("pendingOrders", pendingOrders);

            // 4. 快捷操作列表（只保留创建订单和添加客户）
            List<Map<String, Object>> quickActions = new ArrayList<>();
            quickActions.add(createQuickAction("创建订单", "plus", "/order/ghOrder"));
            quickActions.add(createQuickAction("添加客户", "user-add", "/customer/ghCustomer"));
            result.put("quickActions", quickActions);

            // 5. 系统通知（示例数据，可以根据实际需求从数据库获取）
            List<Map<String, Object>> notifications = new ArrayList<>();
            Map<String, Object> notification1 = new HashMap<>();
            notification1.put("id", "1");
            notification1.put("content", "您有5个待处理任务");
            notification1.put("time", new Date());
            notification1.put("type", "task");
            notifications.add(notification1);

            Map<String, Object> notification2 = new HashMap<>();
            notification2.put("id", "2");
            notification2.put("content", "本月业绩目标完成80%");
            notification2.put("time", new Date());
            notification2.put("type", "performance");
            notifications.add(notification2);

            result.put("notifications", notifications);

            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取工作台数据失败", e);
            return Result.error("获取工作台数据失败：" + e.getMessage());
        }
    }

    /**
     * 创建快捷操作项
     */
    private Map<String, Object> createQuickAction(String name, String icon, String path) {
        Map<String, Object> action = new HashMap<>();
        action.put("name", name);
        action.put("icon", icon);
        action.put("path", path);
        return action;
    }

    private static long dashboardLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return 0L;
        }
    }

    private static BigDecimal dashboardDecimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        }
        if (o instanceof Number) {
            return new BigDecimal(((Number) o).toString());
        }
        try {
            return new BigDecimal(String.valueOf(o));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 递归查询指定角色及其所有下级角色的ID
     * @param roleId 角色ID
     * @return 角色ID列表（包含当前角色及其所有下级角色）
     */
    private List<String> getAllSubRoleIds(String roleId) {
        List<String> result = new ArrayList<>();
        if (oConvertUtils.isEmpty(roleId) || sysRoleService == null) {
            return result;
        }
        
        // 添加当前角色ID
        result.add(roleId);
        
        // 查询直接下级角色（parent_id = roleId 且 role_type = 2）
        LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = 
            new LambdaQueryWrapper<>();
        query.eq(org.jeecg.modules.system.entity.SysRole::getParentId, roleId);
        query.eq(org.jeecg.modules.system.entity.SysRole::getRoleType, 2); // 只查询数据角色
        query.eq(org.jeecg.modules.system.entity.SysRole::getStatus, 1); // 只查询启用的角色
        List<org.jeecg.modules.system.entity.SysRole> children = sysRoleService.list(query);
        
        // 递归查询每个子角色的下级角色
        if (children != null && !children.isEmpty()) {
            for (org.jeecg.modules.system.entity.SysRole child : children) {
                List<String> subRoleIds = getAllSubRoleIds(child.getId());
                result.addAll(subRoleIds);
            }
        }
        
        return result;
    }
    
    /**
     * 只获取下级角色ID（不包括当前角色）
     * @param roleId 角色ID
     * @return 下级角色ID列表（不包含当前角色）
     */
    private List<String> getSubRoleIdsOnly(String roleId) {
        List<String> result = new ArrayList<>();
        if (oConvertUtils.isEmpty(roleId) || sysRoleService == null) {
            return result;
        }
        
        // 查询直接下级角色（parent_id = roleId 且 role_type = 2）
        LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = 
            new LambdaQueryWrapper<>();
        query.eq(org.jeecg.modules.system.entity.SysRole::getParentId, roleId);
        query.eq(org.jeecg.modules.system.entity.SysRole::getRoleType, 2); // 只查询数据角色
        query.eq(org.jeecg.modules.system.entity.SysRole::getStatus, 1); // 只查询启用的角色
        List<org.jeecg.modules.system.entity.SysRole> children = sysRoleService.list(query);
        
        // 递归查询每个子角色及其下级角色
        if (children != null && !children.isEmpty()) {
            for (org.jeecg.modules.system.entity.SysRole child : children) {
                // 添加子角色ID
                result.add(child.getId());
                // 递归查询子角色的下级角色
                List<String> subRoleIds = getSubRoleIdsOnly(child.getId());
                result.addAll(subRoleIds);
            }
        }
        
        return result;
    }
    
    /**
     * 根据角色ID列表查询用户ID列表
     * @param roleIds 角色ID列表
     * @return 用户ID列表
     */
    private List<String> getUserIdsByRoleIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty() || sysUserRoleMapper == null) {
            return new ArrayList<>();
        }
        
        List<String> userIds = new ArrayList<>();
        for (String roleId : roleIds) {
            List<String> roleUserIds = sysUserRoleMapper.getUserIdByRoleId(roleId);
            if (roleUserIds != null && !roleUserIds.isEmpty()) {
                userIds.addAll(roleUserIds);
            }
        }
        
        // 去重
        return new ArrayList<>(new HashSet<>(userIds));
    }
}

