package org.jeecg.modules.order.controller;

import java.math.BigDecimal;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhBusinessTask;
import org.jeecg.modules.order.service.IGhBusinessTaskService;
import org.apache.shiro.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.jeecg.boot.starter.lock.annotation.JRepeat;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * 工商任务表 前端控制器
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/order/businessTask")
@Slf4j
public class GhBusinessTaskController {

    @Autowired
    private IGhBusinessTaskService ghBusinessTaskService;

    /**
     * 分页列表查询（根据任务状态）
     * @param taskStatus 任务状态
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<GhBusinessTask>> queryPageList(
            @RequestParam(name="taskStatus", required=false) String taskStatus,
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            HttpServletRequest req) {
        Result<IPage<GhBusinessTask>> result = new Result<>();
        
        QueryWrapper<GhBusinessTask> queryWrapper = QueryGenerator.initQueryWrapper(new GhBusinessTask(), req.getParameterMap());
        queryWrapper.eq("del_flag", 0);
        
        // 如果指定了任务状态，按状态过滤
        if (oConvertUtils.isNotEmpty(taskStatus)) {
            queryWrapper.eq("task_status", taskStatus);
        }
        
        // 如果是待本人接收或任务状态，只查询分配给当前用户的任务
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null && ("assigned_to_me".equals(taskStatus) || "task".equals(taskStatus))) {
            queryWrapper.and(wrapper -> wrapper
                .eq("assigned_user_id", sysUser.getId())
                .or()
                .eq("received_user_id", sysUser.getId())
            );
        }
        
        // 如果是公海待接收，查询公海任务（未分配或已放入公海）
        if ("public_sea".equals(taskStatus)) {
            queryWrapper.and(wrapper -> wrapper
                .eq("assign_type", "public_sea")
                .or()
                .isNull("assigned_user_id")
            );
        }
        
        Page<GhBusinessTask> page = new Page<>(pageNo, pageSize);
        IPage<GhBusinessTask> pageList = ghBusinessTaskService.page(page, queryWrapper);
        
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 通过id查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<GhBusinessTask> queryById(@RequestParam(name="id",required=true) String id) {
        Result<GhBusinessTask> result = new Result<>();
        GhBusinessTask task = ghBusinessTaskService.getById(id);
        if (task == null) {
            result.error500("未找到对应实体");
        } else {
            result.setResult(task);
            result.setSuccess(true);
        }
        return result;
    }

    /**
     * 根据订单ID查询工商任务
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/queryByOrderId", method = RequestMethod.GET)
    public Result<GhBusinessTask> queryByOrderId(@RequestParam(name="orderId",required=true) String orderId) {
        Result<GhBusinessTask> result = new Result<>();
        GhBusinessTask task = ghBusinessTaskService.getTaskByOrderId(orderId);
        if (task == null) {
            result.error500("未找到对应实体");
        } else {
            result.setResult(task);
            result.setSuccess(true);
        }
        return result;
    }

    /**
     * 工商经理审核
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/managerAudit", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> managerAudit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");
            String auditStatus = jsonObject.getString("auditStatus"); // approved-通过, rejected-驳回
            String assignType = jsonObject.getString("assignType"); // public_sea-放入公海, assign_user-指定工商人员
            String assignedUserId = jsonObject.getString("assignedUserId");
            String assignedUserName = jsonObject.getString("assignedUserName");
            String remark = jsonObject.getString("remark");

            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                return Result.error("请先登录");
            }

            boolean success = ghBusinessTaskService.managerAudit(taskId, auditStatus, assignType, assignedUserId, assignedUserName, remark);
            if (success) {
                result.success("审核成功");
            } else {
                result.error500("审核失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 工商人员接收任务
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> receiveTask(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");

            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                return Result.error("请先登录");
            }

            boolean success = ghBusinessTaskService.receiveTask(taskId, sysUser.getId(), sysUser.getRealname());
            if (success) {
                result.success("接收成功");
            } else {
                result.error500("接收失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新任务成本信息
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/updateCost", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateCost(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");
            String costCategory = jsonObject.getString("costCategory");
            java.math.BigDecimal costAmount = jsonObject.getBigDecimal("costAmount");

            boolean success = ghBusinessTaskService.updateCost(taskId, costCategory, costAmount);
            if (success) {
                result.success("更新成功");
            } else {
                result.error500("更新失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 交接任务
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/handover", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> handoverTask(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");

            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                return Result.error("请先登录");
            }

            boolean success = ghBusinessTaskService.handoverTask(taskId, sysUser.getId(), sysUser.getRealname());
            if (success) {
                result.success("交接成功");
            } else {
                result.error500("交接失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 业务人员确认交接完成
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/confirmHandover", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> confirmHandover(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");

            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                return Result.error("请先登录");
            }

            boolean success = ghBusinessTaskService.confirmHandover(taskId, sysUser.getId(), sysUser.getRealname());
            if (success) {
                result.success("确认完成");
            } else {
                result.error500("确认失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 转为异常任务
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/convertToException", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> convertToException(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");
            String exceptionType = jsonObject.getString("exceptionType"); // problem_task-问题任务, recycle_bin-回收站
            String reason = jsonObject.getString("reason");

            boolean success = ghBusinessTaskService.convertToException(taskId, exceptionType, reason);
            if (success) {
                result.success("操作成功");
            } else {
                result.error500("操作失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 转分配工商人员
     * @param jsonObject
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['taskId']")
    @RequestMapping(value = "/reassign", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> reassignTask(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");
            String assignedUserId = jsonObject.getString("assignedUserId");
            String assignedUserName = jsonObject.getString("assignedUserName");

            boolean success = ghBusinessTaskService.reassignTask(taskId, assignedUserId, assignedUserName);
            if (success) {
                result.success("分配成功");
            } else {
                result.error500("分配失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysCategoryService sysCategoryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.order.service.IGhOrderService ghOrderService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysAuditTaskService sysAuditTaskService;
    
    /**
     * 任务中心分析统计（基于审核任务）
     * @param year 年份
     * @param month 月份（可选）
     * @param businessType 业务类型（可选）
     * @param taskType 任务类型（once-一次性任务，recurring-周期任务）
     * @return 统计数据
     */
    @GetMapping(value = "/getTaskStatistics")
    public Result<?> getTaskStatistics(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "taskType", required = false) String taskType) {
        try {
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 查询审核任务（sys_audit_task）而不是工商任务
            QueryWrapper<org.jeecg.modules.system.entity.SysAuditTask> queryWrapper = new QueryWrapper<>();
            // 过滤已删除的审核任务
            queryWrapper.eq("del_flag", 0);
            
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            queryWrapper.ge("create_time", yearStart);
            queryWrapper.le("create_time", yearEnd);
            
            if (month != null && month >= 1 && month <= 12) {
                calendar.set(year, month - 1, 1, 0, 0, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                Date monthStart = calendar.getTime();
                
                calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
                calendar.set(java.util.Calendar.MILLISECOND, 999);
                Date monthEnd = calendar.getTime();
                
                queryWrapper.ge("create_time", monthStart);
                queryWrapper.le("create_time", monthEnd);
            }
            
            // 如果指定了任务类型，过滤任务类型
            if (oConvertUtils.isNotEmpty(taskType)) {
                queryWrapper.eq("task_type", taskType);
            }
            
            // 查询所有符合条件的审核任务
            List<org.jeecg.modules.system.entity.SysAuditTask> tasks = new ArrayList<>();
            if (sysAuditTaskService != null) {
                tasks = sysAuditTaskService.list(queryWrapper);
            }
            
            log.info("任务分析查询结果：年份={}, 月份={}, 业务类型={}, 任务类型={}, 审核任务数量={}", 
                    year, month, businessType, taskType, tasks.size());
            
            // 如果没有数据，记录警告日志
            if (tasks.isEmpty()) {
                log.warn("任务分析查询结果为空，请检查：1. 数据库中是否有审核任务数据；2. 任务创建时间是否在查询范围内");
            }
            
            // 批量查询所有订单，避免循环查询（性能优化）
            // 使用QueryWrapper查询，确保只查询未删除的订单（del_flag = 0）
            Map<String, org.jeecg.modules.order.entity.GhOrder> orderMap = new HashMap<>();
            if (ghOrderService != null && !tasks.isEmpty()) {
                java.util.Set<String> orderIds = tasks.stream()
                    .map(org.jeecg.modules.system.entity.SysAuditTask::getOrderId)
                    .filter(id -> oConvertUtils.isNotEmpty(id))
                    .collect(java.util.stream.Collectors.toSet());
                
                if (!orderIds.isEmpty()) {
                    // 使用QueryWrapper查询，过滤del_flag = 0
                    QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
                    orderWrapper.eq("del_flag", 0);
                    orderWrapper.in("id", orderIds);
                    List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
                    for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                        orderMap.put(order.getId(), order);
                    }
                }
            }
            
            // 如果指定了业务类型，过滤订单
            if (oConvertUtils.isNotEmpty(businessType)) {
                tasks = tasks.stream()
                    .filter(task -> {
                        org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                        return order != null && businessType.equals(order.getBusinessType());
                    })
                    .collect(java.util.stream.Collectors.toList());
                log.info("按业务类型过滤后，任务数量={}", tasks.size());
            }
            
            // 按订单去重：一个订单有多个审核任务，但只算一个订单
            // 使用Map按订单ID分组，每个订单只保留一个任务（取第一个）
            // 同时过滤已删除的订单（del_flag != 0）
            Map<String, org.jeecg.modules.system.entity.SysAuditTask> uniqueOrderTasks = new java.util.LinkedHashMap<>();
            Map<String, Boolean> orderCompletedMap = new HashMap<>(); // 记录订单是否所有任务都完成
            
            for (org.jeecg.modules.system.entity.SysAuditTask task : tasks) {
                if (oConvertUtils.isNotEmpty(task.getOrderId())) {
                    // 检查订单是否存在且未删除
                    org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                    if (order != null && (order.getDelFlag() == null || order.getDelFlag() == 0)) {
                        // 如果这个订单还没有记录，添加第一个任务
                        if (!uniqueOrderTasks.containsKey(task.getOrderId())) {
                            uniqueOrderTasks.put(task.getOrderId(), task);
                            orderCompletedMap.put(task.getOrderId(), true); // 默认完成
                        }
                        
                        // 如果任务状态不是approved，标记订单为未完成
                        if (task.getTaskStatus() == null || !"approved".equals(task.getTaskStatus())) {
                            orderCompletedMap.put(task.getOrderId(), false);
                        }
                    }
                }
            }
            
            List<org.jeecg.modules.system.entity.SysAuditTask> uniqueTasks = new ArrayList<>(uniqueOrderTasks.values());
            log.info("按订单去重后，订单数量={}", uniqueTasks.size());
            
            Map<String, Object> result = new HashMap<>();
            
            Map<String, Object> statistics = new HashMap<>();
            // uniqueTasks已经过滤了已删除的订单，所以直接使用size
            int totalCount = uniqueTasks.size();
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal completionRate = BigDecimal.ZERO;
            int completedCount = 0;
            java.util.Set<String> salesmanSet = new java.util.HashSet<>();
            
            for (org.jeecg.modules.system.entity.SysAuditTask task : uniqueTasks) {
                // 从订单中获取金额
                org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                // uniqueTasks已经过滤了已删除的订单，这里再次检查确保数据正确
                if (order != null && (order.getDelFlag() == null || order.getDelFlag() == 0)) {
                    // 使用订单金额（order_amount）或合同金额（contract_amount）
                    BigDecimal amount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                                       (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                    totalAmount = totalAmount.add(amount);
                    
                    // 统计业务人员
                    if (oConvertUtils.isNotEmpty(order.getSalesman())) {
                        salesmanSet.add(order.getSalesman());
                    }
                    
                    // 判断订单是否所有任务都完成
                    Boolean isCompleted = orderCompletedMap.get(task.getOrderId());
                    if (isCompleted != null && isCompleted) {
                        completedCount++;
                    }
                }
            }
            
            BigDecimal avgAmount = totalCount > 0 ? 
                totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
            
            if (totalCount > 0) {
                completionRate = new BigDecimal(completedCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            statistics.put("totalCount", totalCount);
            statistics.put("totalAmount", totalAmount);
            statistics.put("avgAmount", avgAmount);
            statistics.put("completionRate", completionRate);
            
            List<Map<String, Object>> monthlyTrend = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                calendar.set(year, m - 1, 1, 0, 0, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                Date mStart = calendar.getTime();
                
                calendar.set(year, m - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
                calendar.set(java.util.Calendar.MILLISECOND, 999);
                Date mEnd = calendar.getTime();
                
                // 按订单去重统计，使用订单创建时间
                java.util.Set<String> monthOrderIds = new java.util.HashSet<>();
                BigDecimal amount = BigDecimal.ZERO;
                
                for (org.jeecg.modules.system.entity.SysAuditTask task : tasks) {
                    if (oConvertUtils.isNotEmpty(task.getOrderId())) {
                        org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                        // 只统计未删除的订单（del_flag = 0 或 null）
                        if (order != null && (order.getDelFlag() == null || order.getDelFlag() == 0) 
                            && order.getCreateTime() != null &&
                            !order.getCreateTime().before(mStart) && 
                            !order.getCreateTime().after(mEnd)) {
                            
                            // 如果这个订单还没统计过，才计入
                            if (!monthOrderIds.contains(task.getOrderId())) {
                                monthOrderIds.add(task.getOrderId());
                                
                                BigDecimal taskAmount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                                                       (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                                amount = amount.add(taskAmount);
                            }
                        }
                    }
                }
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", m + "月");
                monthData.put("count", monthOrderIds.size());
                monthData.put("amount", amount);
                monthlyTrend.add(monthData);
            }
            
            // 业务类型统计（按订单去重）
            Map<String, Map<String, Object>> businessTypeMap = new HashMap<>();
            // 【性能优化】使用缓存避免重复查询字典
            Map<String, String> businessTypeNameCache = new HashMap<>();
            java.util.Set<String> processedOrderIds = new java.util.HashSet<>();
            
            for (org.jeecg.modules.system.entity.SysAuditTask task : uniqueTasks) {
                if (oConvertUtils.isNotEmpty(task.getOrderId())) {
                    org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                    // 只统计未删除的订单（del_flag = 0 或 null）
                    if (order != null && (order.getDelFlag() == null || order.getDelFlag() == 0)) {
                        String bt = order.getBusinessType();
                        String btName = "其他";
                        
                        if (oConvertUtils.isNotEmpty(bt)) {
                            // 先从缓存中获取
                            if (businessTypeNameCache.containsKey(bt)) {
                                btName = businessTypeNameCache.get(bt);
                            } else {
                                // 缓存中没有，查询数据库并缓存
                                try {
                                    if (sysCategoryService != null) {
                                        java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
                                        if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
                                            btName = names.get(0);
                                        } else {
                                            btName = bt;
                                        }
                                    } else {
                                        btName = bt;
                                    }
                                    businessTypeNameCache.put(bt, btName);
                                } catch (Exception e) {
                                    log.warn("获取业务类型名称失败，业务类型ID: " + bt, e);
                                    btName = bt;
                                    businessTypeNameCache.put(bt, btName);
                                }
                            }
                        }
                        
                        Map<String, Object> btData = businessTypeMap.getOrDefault(btName, new HashMap<>());
                        int count = (Integer) btData.getOrDefault("count", 0);
                        BigDecimal amount = (BigDecimal) btData.getOrDefault("amount", BigDecimal.ZERO);
                        
                        BigDecimal taskAmount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                                               (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                        
                        btData.put("name", btName);
                        btData.put("count", count + 1);
                        btData.put("amount", amount.add(taskAmount));
                        businessTypeMap.put(btName, btData);
                    }
                }
            }
            
            List<Map<String, Object>> businessTypeList = new ArrayList<>(businessTypeMap.values());
            
            // 业务人员统计（按订单去重）
            Map<String, Map<String, Object>> salesmanMap = new HashMap<>();
            for (org.jeecg.modules.system.entity.SysAuditTask task : uniqueTasks) {
                org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                // 只统计未删除的订单（del_flag = 0 或 null）
                if (order != null && (order.getDelFlag() == null || order.getDelFlag() == 0)) {
                    String sm = order.getSalesman();
                    if (oConvertUtils.isEmpty(sm)) {
                        sm = "未分配";
                    }
                    
                    Map<String, Object> smData = salesmanMap.getOrDefault(sm, new HashMap<>());
                    int count = (Integer) smData.getOrDefault("count", 0);
                    BigDecimal amount = (BigDecimal) smData.getOrDefault("amount", BigDecimal.ZERO);
                    
                    BigDecimal taskAmount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                                        (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                    
                    smData.put("salesman", sm);
                    smData.put("count", count + 1);
                    smData.put("amount", amount.add(taskAmount));
                    salesmanMap.put(sm, smData);
                }
            }
            
            List<Map<String, Object>> salesmanRank = new ArrayList<>(salesmanMap.values());
            salesmanRank.sort((a, b) -> {
                Integer countA = (Integer) a.get("count");
                Integer countB = (Integer) b.get("count");
                return countB.compareTo(countA);
            });
            
            List<Map<String, Object>> dailyTrend = new ArrayList<>();
            if (month != null && month >= 1 && month <= 12) {
                calendar.set(year, month - 1, 1, 0, 0, 0);
                int daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                
                for (int d = 1; d <= daysInMonth; d++) {
                    calendar.set(year, month - 1, d, 0, 0, 0);
                    calendar.set(java.util.Calendar.MILLISECOND, 0);
                    Date dStart = calendar.getTime();
                    
                    calendar.set(year, month - 1, d, 23, 59, 59);
                    calendar.set(java.util.Calendar.MILLISECOND, 999);
                    Date dEnd = calendar.getTime();
                    
                    // 按订单去重统计，使用订单创建时间
                    java.util.Set<String> dayOrderIds = new java.util.HashSet<>();
                    BigDecimal amount = BigDecimal.ZERO;
                    
                    for (org.jeecg.modules.system.entity.SysAuditTask task : tasks) {
                        if (oConvertUtils.isNotEmpty(task.getOrderId())) {
                            org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                            // 只统计未删除的订单（del_flag = 0 或 null）
                            if (order != null && (order.getDelFlag() == null || order.getDelFlag() == 0)
                                && order.getCreateTime() != null &&
                                !order.getCreateTime().before(dStart) && 
                                !order.getCreateTime().after(dEnd)) {
                                
                                // 如果这个订单还没统计过，才计入
                                if (!dayOrderIds.contains(task.getOrderId())) {
                                    dayOrderIds.add(task.getOrderId());
                                    
                                    BigDecimal taskAmount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                                                           (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                                    amount = amount.add(taskAmount);
                                }
                            }
                        }
                    }
                    
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", String.format("%d-%02d-%02d", year, month, d));
                    dayData.put("count", dayOrderIds.size());
                    dayData.put("amount", amount);
                    dailyTrend.add(dayData);
                }
            } else {
                for (Map<String, Object> monthData : monthlyTrend) {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", monthData.get("month"));
                    dayData.put("count", monthData.get("count"));
                    dayData.put("amount", monthData.get("amount"));
                    dailyTrend.add(dayData);
                }
            }
            
            result.put("statistics", statistics);
            result.put("monthlyTrend", monthlyTrend);
            result.put("businessType", businessTypeList);
            result.put("salesmanRank", salesmanRank);
            result.put("dailyTrend", dailyTrend);
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取任务中心分析统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务人员任务月度明细数据（基于审核任务）
     */
    @GetMapping(value = "/getTaskSalesmanMonthlyDetail")
    public Result<?> getTaskSalesmanMonthlyDetail(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "taskType", required = false) String taskType) {
        try {
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 查询审核任务
            QueryWrapper<org.jeecg.modules.system.entity.SysAuditTask> queryWrapper = new QueryWrapper<>();
            
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            queryWrapper.ge("create_time", yearStart);
            queryWrapper.le("create_time", yearEnd);
            
            // 如果指定了任务类型，过滤任务类型
            if (oConvertUtils.isNotEmpty(taskType)) {
                queryWrapper.eq("task_type", taskType);
            }
            
            List<org.jeecg.modules.system.entity.SysAuditTask> tasks = new ArrayList<>();
            if (sysAuditTaskService != null) {
                tasks = sysAuditTaskService.list(queryWrapper);
            }
            
            // 批量查询所有订单
            Map<String, org.jeecg.modules.order.entity.GhOrder> orderMap = new HashMap<>();
            if (ghOrderService != null && !tasks.isEmpty()) {
                java.util.Set<String> orderIds = tasks.stream()
                    .map(org.jeecg.modules.system.entity.SysAuditTask::getOrderId)
                    .filter(id -> oConvertUtils.isNotEmpty(id))
                    .collect(java.util.stream.Collectors.toSet());
                
                if (!orderIds.isEmpty()) {
                    List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.listByIds(orderIds);
                    for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                        orderMap.put(order.getId(), order);
                    }
                }
            }
            
            // 如果指定了业务类型，过滤订单
            if (oConvertUtils.isNotEmpty(businessType)) {
                tasks = tasks.stream()
                    .filter(task -> {
                        org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                        return order != null && businessType.equals(order.getBusinessType());
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 按业务人员和月份统计，每个订单只统计一次
            Map<String, Map<Integer, Map<String, Object>>> salesmanMonthlyMap = new HashMap<>();
            Map<String, Map<Integer, java.util.Set<String>>> salesmanMonthOrderIds = new HashMap<>(); // 记录已统计的订单ID
            
            for (org.jeecg.modules.system.entity.SysAuditTask task : tasks) {
                if (oConvertUtils.isEmpty(task.getOrderId())) {
                    continue;
                }
                
                // 从订单中获取业务人员和创建时间
                org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                if (order == null || order.getCreateTime() == null) {
                    continue;
                }
                
                // 只统计未删除的订单（与弹窗查询逻辑一致：del_flag = 0）
                // SQL查询中 o.del_flag = 0 会过滤掉 del_flag != 0 和 del_flag IS NULL 的订单
                // 所以这里也要过滤掉 del_flag != 0 或 del_flag IS NULL 的订单
                if (order.getDelFlag() == null || order.getDelFlag() != 0) {
                    continue;
                }
                
                // 只统计订单创建时间在当年的订单
                calendar.setTime(order.getCreateTime());
                int orderYear = calendar.get(java.util.Calendar.YEAR);
                if (orderYear != year) {
                    continue; // 跳过不在当年创建的订单
                }
                
                String salesman = oConvertUtils.isEmpty(order.getSalesman()) ? "未分配" : order.getSalesman();
                BigDecimal taskAmount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                            (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                
                // 使用订单创建时间而不是审核任务创建时间
                int orderMonth = calendar.get(java.util.Calendar.MONTH) + 1;
                
                // 检查这个订单在这个业务人员的这个月份是否已经统计过
                Map<Integer, java.util.Set<String>> monthOrderIdsMap = salesmanMonthOrderIds.getOrDefault(salesman, new HashMap<>());
                java.util.Set<String> monthOrderIds = monthOrderIdsMap.getOrDefault(orderMonth, new java.util.HashSet<>());
                
                // 如果这个订单还没统计过，才计入
                if (!monthOrderIds.contains(task.getOrderId())) {
                    monthOrderIds.add(task.getOrderId());
                    monthOrderIdsMap.put(orderMonth, monthOrderIds);
                    salesmanMonthOrderIds.put(salesman, monthOrderIdsMap);
                    
                    Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.getOrDefault(salesman, new HashMap<>());
                    Map<String, Object> monthData = monthlyData.getOrDefault(orderMonth, new HashMap<>());
                    
                    Object countObj = monthData.getOrDefault("count", 0);
                    int count = 0;
                    if (countObj instanceof Integer) {
                        count = (Integer) countObj;
                    } else if (countObj instanceof Number) {
                        count = ((Number) countObj).intValue();
                    }
                    
                    Object amountObj = monthData.getOrDefault("amount", BigDecimal.ZERO);
                    BigDecimal amount = BigDecimal.ZERO;
                    if (amountObj instanceof BigDecimal) {
                        amount = (BigDecimal) amountObj;
                    } else if (amountObj instanceof Number) {
                        amount = new BigDecimal(amountObj.toString());
                    }
                    
                    monthData.put("count", count + 1);
                    monthData.put("amount", amount.add(taskAmount));
                    
                    monthlyData.put(orderMonth, monthData);
                    salesmanMonthlyMap.put(salesman, monthlyData);
                }
            }
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<String> salesmen = new ArrayList<>(salesmanMonthlyMap.keySet());
            java.util.Collections.sort(salesmen);
            
            for (String salesman : salesmen) {
                Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.get(salesman);
                
                Map<String, Object> row = new HashMap<>();
                row.put("salesman", salesman);
                
                int totalCount = 0;
                BigDecimal totalAmount = BigDecimal.ZERO;
                
                for (int m = 1; m <= 12; m++) {
                    Map<String, Object> monthData = monthlyData.getOrDefault(m, new HashMap<>());
                    
                    Object countObj = monthData.getOrDefault("count", 0);
                    int count = 0;
                    if (countObj instanceof Integer) {
                        count = (Integer) countObj;
                    } else if (countObj instanceof Number) {
                        count = ((Number) countObj).intValue();
                    }
                    
                    Object amountObj = monthData.getOrDefault("amount", BigDecimal.ZERO);
                    BigDecimal amount = BigDecimal.ZERO;
                    if (amountObj instanceof BigDecimal) {
                        amount = (BigDecimal) amountObj;
                    } else if (amountObj instanceof Number) {
                        amount = new BigDecimal(amountObj.toString());
                    } else if (amountObj != null) {
                        try {
                            amount = new BigDecimal(amountObj.toString());
                        } catch (Exception e) {
                            amount = BigDecimal.ZERO;
                        }
                    }
                    
                    row.put("month" + m + "_count", count);
                    row.put("month" + m + "_amount", amount);
                    
                    totalCount += count;
                    totalAmount = totalAmount.add(amount);
                }
                
                row.put("totalCount", totalCount);
                row.put("totalAmount", totalAmount);
                resultList.add(row);
            }
            
            return Result.OK(resultList);
        } catch (Exception e) {
            log.error("获取业务人员任务月度明细数据失败", e);
            return Result.error("获取数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务人员任务月度订单列表（按订单去重，只查询有审核任务的订单）
     */
    @GetMapping(value = "/getTaskSalesmanMonthlyTasks")
    public Result<?> getTaskSalesmanMonthlyTasks(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = true) Integer month,
            @RequestParam(name = "salesman", required = true) String salesman,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "taskType", required = false) String taskType,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            if (ghOrderService == null || sysAuditTaskService == null) {
                Page<org.jeecg.modules.order.entity.GhOrder> emptyPage = new Page<>(pageNo, pageSize);
                return Result.OK(emptyPage);
            }
            
            // 与表格明细统计逻辑完全一致：遍历所有审核任务，按订单创建时间分组
            // 第一步：查询审核任务（与表格明细统计逻辑一致）
            QueryWrapper<org.jeecg.modules.system.entity.SysAuditTask> taskWrapper = new QueryWrapper<>();
            
            // 时间范围过滤（使用审核任务创建时间，限制在当年）
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            taskWrapper.ge("create_time", yearStart);
            taskWrapper.le("create_time", yearEnd);
            
            // 任务类型过滤
            if (oConvertUtils.isNotEmpty(taskType)) {
                taskWrapper.eq("task_type", taskType);
            }
            
            // 查询当年的审核任务
            List<org.jeecg.modules.system.entity.SysAuditTask> tasks = sysAuditTaskService.list(taskWrapper);
            
            // 批量查询所有订单（与表格明细统计逻辑一致）
            Map<String, org.jeecg.modules.order.entity.GhOrder> orderMap = new HashMap<>();
            if (ghOrderService != null && !tasks.isEmpty()) {
                java.util.Set<String> orderIds = tasks.stream()
                    .map(org.jeecg.modules.system.entity.SysAuditTask::getOrderId)
                    .filter(id -> oConvertUtils.isNotEmpty(id))
                    .collect(java.util.stream.Collectors.toSet());
                
                if (!orderIds.isEmpty()) {
                    List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.listByIds(orderIds);
                    for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                        orderMap.put(order.getId(), order);
                    }
                }
            }
            
            // 如果指定了业务类型，过滤任务（与表格明细统计逻辑一致）
            if (oConvertUtils.isNotEmpty(businessType)) {
                tasks = tasks.stream()
                    .filter(task -> {
                        org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                        return order != null && businessType.equals(order.getBusinessType());
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 第二步：遍历审核任务，按订单创建时间分组（与表格明细统计逻辑完全一致）
            Map<String, org.jeecg.modules.order.entity.GhOrder> uniqueOrders = new LinkedHashMap<>();
            java.util.Set<String> processedOrderIds = new java.util.HashSet<>(); // 记录已处理的订单ID（去重）
            
            // 时间范围过滤（使用订单创建时间）
            calendar.set(year, month - 1, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date monthStart = calendar.getTime();
            
            calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date monthEnd = calendar.getTime();
            
            for (org.jeecg.modules.system.entity.SysAuditTask task : tasks) {
                if (oConvertUtils.isEmpty(task.getOrderId())) {
                    continue;
                }
                
                // 从订单中获取信息（与表格明细统计逻辑一致）
                org.jeecg.modules.order.entity.GhOrder order = orderMap.get(task.getOrderId());
                if (order == null || order.getCreateTime() == null) {
                    continue;
                }
                
                // 只统计订单创建时间在当年的订单（与表格明细统计逻辑一致）
                calendar.setTime(order.getCreateTime());
                int orderYear = calendar.get(java.util.Calendar.YEAR);
                if (orderYear != year) {
                    continue; // 跳过不在当年创建的订单
                }
                
                // 使用订单创建时间而不是审核任务创建时间（与表格明细统计逻辑一致）
                int orderMonth = calendar.get(java.util.Calendar.MONTH) + 1;
                
                // 只统计指定月份的订单
                if (orderMonth != month) {
                    continue;
                }
                
                // 业务员匹配
                String orderSalesman = oConvertUtils.isEmpty(order.getSalesman()) ? "未分配" : order.getSalesman();
                if (!salesman.equals(orderSalesman)) {
                    continue;
                }
                
                // 只统计未删除的订单
                if (order.getDelFlag() != null && order.getDelFlag() != 0) {
                    continue;
                }
                
                // 按订单ID去重（与表格明细统计逻辑一致：每个订单只统计一次）
                if (!processedOrderIds.contains(task.getOrderId())) {
                    processedOrderIds.add(task.getOrderId());
                    uniqueOrders.put(order.getId(), order);
                }
            }
            
            // 转换为列表用于分页
            List<org.jeecg.modules.order.entity.GhOrder> uniqueOrderList = new ArrayList<>(uniqueOrders.values());
            
            // 内存分页
            int total = uniqueOrderList.size();
            int start = (pageNo - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<org.jeecg.modules.order.entity.GhOrder> pageData = start < total ? 
                uniqueOrderList.subList(start, end) : new ArrayList<>();
            
            // 构造分页对象
            Page<org.jeecg.modules.order.entity.GhOrder> page = new Page<>(pageNo, pageSize, total);
            page.setRecords(pageData);
            IPage<org.jeecg.modules.order.entity.GhOrder> pageList = page;
            
            // 将订单数据转换为前端期望的格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (pageList != null && pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
                for (org.jeecg.modules.order.entity.GhOrder order : pageList.getRecords()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", order.getId());
                    item.put("orderNo", order.getOrderNo());
                    item.put("companyName", order.getCompanyName());
                    item.put("businessType", order.getBusinessType());
                    item.put("salesman", order.getSalesman());
                    item.put("createTime", order.getCreateTime());
                    
                    // 任务金额：使用订单金额或合同金额
                    BigDecimal amount = order.getOrderAmount() != null ? order.getOrderAmount() : 
                                       (order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO);
                    item.put("costAmount", amount);
                    
                    // 任务状态：使用审核状态，并添加状态文本
                    String auditStatus = order.getAuditStatus();
                    if (oConvertUtils.isEmpty(auditStatus)) {
                        auditStatus = "pending"; // 默认待审核
                    }
                    item.put("taskStatus", auditStatus);
                    
                    // 添加状态文本（中文）
                    String statusText = "";
                    switch (auditStatus) {
                        case "pending":
                            statusText = "待审核";
                            break;
                        case "approved":
                            statusText = "已通过";
                            break;
                        case "rejected":
                            statusText = "已驳回";
                            break;
                        default:
                            statusText = auditStatus;
                            break;
                    }
                    item.put("taskStatus_dictText", statusText);
                    
                    // 获取业务类型名称
                    if (oConvertUtils.isNotEmpty(order.getBusinessType()) && sysCategoryService != null) {
                        try {
                            java.util.List<String> names = sysCategoryService.loadDictItem(order.getBusinessType(), false);
                            if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
                                item.put("businessType_dictText", names.get(0));
                                item.put("businessTypeName", names.get(0));
                            }
                        } catch (Exception e) {
                            log.warn("获取业务类型名称失败", e);
                        }
                    }
                    
                    resultList.add(item);
                }
            }
            
            // 构造分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", resultList);
            result.put("total", pageList != null ? pageList.getTotal() : 0);
            result.put("size", pageList != null ? pageList.getSize() : pageSize);
            result.put("current", pageList != null ? pageList.getCurrent() : pageNo);
            result.put("pages", pageList != null ? pageList.getPages() : 0);
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取业务人员任务月度订单列表失败", e);
            return Result.error("获取数据失败：" + e.getMessage());
        }
    }
}

