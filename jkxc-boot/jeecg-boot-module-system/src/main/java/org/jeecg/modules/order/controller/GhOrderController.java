package org.jeecg.modules.order.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.entity.GhOrderStep;
import org.jeecg.modules.order.entity.GhOrderPayment;
import org.jeecg.modules.order.entity.GhOrderExpense;
import org.jeecg.modules.order.entity.GhOrderFormData;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.order.service.IGhOrderPaymentService;
import org.jeecg.modules.order.service.IGhOrderExpenseService;
import org.jeecg.modules.order.service.IGhOrderFormDataService;
import org.jeecg.modules.order.service.IGhOrderOperationLogService;
import org.jeecg.modules.order.service.IGhOrderStepService;
import org.jeecg.modules.order.mapper.GhOrderMapper;
import org.jeecg.modules.order.entity.GhOrderOperationLog;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.entity.SysAuditProcessBinding;
import org.jeecg.modules.system.entity.SysAuditTask;
import org.jeecg.modules.system.service.ISysAuditProcessService;
import org.jeecg.modules.system.service.ISysAuditStepService;
import org.jeecg.modules.system.service.ISysAuditProcessBindingService;
import org.jeecg.modules.system.service.ISysAuditTaskService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.entity.SysUserRole;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.jeecg.boot.starter.lock.annotation.JRepeat;

/**
 * <p>
 * 订单主表 前端控制器
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class GhOrderController {

    @Autowired
    private IGhOrderService ghOrderService;
    
    @Autowired
    private IGhOrderPaymentService ghOrderPaymentService;
    
    @Autowired
    private IGhOrderExpenseService ghOrderExpenseService;
    
    @Autowired
    private IGhOrderFormDataService ghOrderFormDataService;
    
    @Autowired
    private IGhBankManagementService ghBankManagementService;
    
    @Autowired
    private ISysAuditProcessService sysAuditProcessService;
    
    @Autowired
    private ISysAuditStepService sysAuditStepService;
    
    @Autowired
    private IGhOrderOperationLogService ghOrderOperationLogService;
    
    @Autowired
    private IGhOrderStepService ghOrderStepService;
    
    @Autowired
    private ISysDictService sysDictService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderAuditService ghOrderAuditService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhBusinessTaskService ghBusinessTaskService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhAccountingHandoverService ghAccountingHandoverService;
    
    @Autowired(required = false)
    private IGhBankDiaryService ghBankDiaryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.reimbursement.service.IGhReimbursementService ghReimbursementService;
    
    @Autowired
    private org.jeecg.modules.system.mapper.SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired
    private ISysAuditProcessBindingService sysAuditProcessBindingService;
    
    @Autowired
    private ISysAuditTaskService sysAuditTaskService;
    
    @Autowired
    private org.jeecg.modules.system.service.ISysUserService sysUserService;
    
    @Autowired
    private org.jeecg.modules.system.service.ISysTeamService sysTeamService;
    
    @Autowired
    private org.jeecg.modules.system.service.ISysRoleService sysRoleService;
    
    @Autowired
    private org.jeecg.modules.system.service.ISysUserRoleService sysUserRoleService;
    
    @Autowired
    private GhOrderMapper ghOrderMapper;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysCategoryService sysCategoryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.notification.service.IWechatNotificationService wechatNotificationService;

    /**
     * 分页列表查询
     * @param order
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    /**
     * 分页列表查询（已优化N+1查询问题）
     * @param order
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    /**
     * 分页列表查询（已优化N+1查询问题 - 第二版）
     * @param order
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<GhOrder>> queryPageList(GhOrder order,
                                                @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                @RequestParam(name="ignoreDataPermission", required = false, defaultValue = "false") Boolean ignoreDataPermission,
                                                HttpServletRequest req) {
        Result<IPage<GhOrder>> result = new Result<>();
        QueryWrapper<GhOrder> queryWrapper = QueryGenerator.initQueryWrapper(order, req.getParameterMap());
        queryWrapper.eq("del_flag", 0);

        // 权限过滤：只能看到自己和下级的数据
        if (!Boolean.TRUE.equals(ignoreDataPermission)) {
            try {
                LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (loginUser != null) {
                    List<String> userRoleIds = sysUserRoleMapper.getRoleIdByUserName(loginUser.getUsername());
                    if (userRoleIds != null && !userRoleIds.isEmpty()) {
                        List<String> subRoleIds = new ArrayList<>();
                        for (String roleId : userRoleIds) {
                            org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
                            if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
                                // 只查询下级角色的ID（不包括当前角色）
                                List<String> allSubRoleIds = getSubRoleIdsOnly(roleId);
                                subRoleIds.addAll(allSubRoleIds);
                            }
                        }

                        // 构建允许查看的用户姓名列表
                        List<String> allowedRealnames = new ArrayList<>();
                        
                        // 1. 添加当前用户自己
                        if (oConvertUtils.isNotEmpty(loginUser.getRealname())) {
                            allowedRealnames.add(loginUser.getRealname());
                        }
                        
                        // 2. 如果有下级角色，添加下级角色的用户
                        if (!subRoleIds.isEmpty()) {
                            List<String> subUserIds = getUserIdsByRoleIds(subRoleIds);
                            if (subUserIds != null && !subUserIds.isEmpty()) {
                                List<org.jeecg.modules.system.entity.SysUser> subUsers = sysUserService.list(
                                        new LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser>()
                                                .in(org.jeecg.modules.system.entity.SysUser::getId, subUserIds)
                                                .eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0)
                                );

                                if (subUsers != null && !subUsers.isEmpty()) {
                                    for (org.jeecg.modules.system.entity.SysUser user : subUsers) {
                                        if (oConvertUtils.isNotEmpty(user.getRealname())) {
                                            allowedRealnames.add(user.getRealname());
                                        }
                                    }
                                }
                            }
                        }

                        // 根据业务员姓名过滤
                        if (!allowedRealnames.isEmpty()) {
                            queryWrapper.in("salesman", allowedRealnames);
                        } else {
                            queryWrapper.eq("id", "no_match_user");
                        }
                    } else {
                        if (oConvertUtils.isNotEmpty(loginUser.getRealname())) {
                            queryWrapper.eq("salesman", loginUser.getRealname());
                        } else {
                            queryWrapper.eq("id", "no_match_user");
                        }
                    }
                } else {
                    queryWrapper.eq("id", "no_match_user");
                }
            } catch (Exception e) {
                log.error("订单权限过滤失败", e);
                try {
                    LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (loginUser != null && oConvertUtils.isNotEmpty(loginUser.getRealname())) {
                        queryWrapper.eq("salesman", loginUser.getRealname());
                    } else {
                        queryWrapper.eq("id", "no_match_user");
                    }
                } catch (Exception ex) {
                    log.error("设置默认权限过滤失败", ex);
                    queryWrapper.eq("id", "no_match_user");
                }
            }
        }

        Page<GhOrder> page = new Page<>(pageNo, pageSize);
        IPage<GhOrder> pageList = ghOrderService.page(page, queryWrapper);

        // ========== 优化：批量查询，彻底解决N+1问题 ==========
        if (pageList != null && pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
            List<GhOrder> orders = pageList.getRecords();

            // 1. 收集所有订单ID
            List<String> orderIds = orders.stream()
                    .map(GhOrder::getId)
                    .collect(Collectors.toList());

            // 2. 批量查询审核记录（1次查询）
            Map<String, List<org.jeecg.modules.order.entity.GhOrderAudit>> auditsMap =
                    ghOrderAuditService.getAuditsByOrderIds(orderIds);

            // 3. 批量查询工商任务（1次查询）
            Map<String, org.jeecg.modules.order.entity.GhBusinessTask> tasksMap =
                    ghBusinessTaskService.getTasksByOrderIds(orderIds);

            // 4. 批量查询审核任务（1次查询）
            Map<String, List<org.jeecg.modules.system.entity.SysAuditTask>> auditTasksMap = null;
            if (sysAuditTaskService != null) {
                auditTasksMap = sysAuditTaskService.getTasksByOrderIds(orderIds);
            }

            // 5. 批量查询用户和团队信息（2次查询）
            List<String> salesmanNames = orders.stream()
                    .map(GhOrder::getSalesman)
                    .filter(oConvertUtils::isNotEmpty)
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, org.jeecg.modules.system.entity.SysUser> userMap = new HashMap<>();
            Map<String, org.jeecg.modules.system.entity.SysTeam> teamMap = new HashMap<>();

            if (!salesmanNames.isEmpty()) {
                List<org.jeecg.modules.system.entity.SysUser> users = sysUserService.list(
                        new LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser>()
                                .in(org.jeecg.modules.system.entity.SysUser::getRealname, salesmanNames)
                                .eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0)
                );

                userMap = users.stream()
                        .collect(Collectors.toMap(
                                org.jeecg.modules.system.entity.SysUser::getRealname,
                                user -> user,
                                (existing, replacement) -> existing
                        ));

                List<String> teamIds = users.stream()
                        .map(org.jeecg.modules.system.entity.SysUser::getTeamId)
                        .filter(oConvertUtils::isNotEmpty)
                        .distinct()
                        .collect(Collectors.toList());

                if (!teamIds.isEmpty()) {
                    List<org.jeecg.modules.system.entity.SysTeam> teams = sysTeamService.listByIds(teamIds);
                    teamMap = teams.stream()
                            .collect(Collectors.toMap(
                                    org.jeecg.modules.system.entity.SysTeam::getId,
                                    team -> team,
                                    (existing, replacement) -> existing
                            ));
                }
            }

            // 6. 批量查询客户订单数量（优化：2次GROUP BY查询替代N次）
            Map<String, Integer> customerOrderCountMap = new HashMap<>();
            Map<String, String> customerRecurringMap = new HashMap<>();

            // 收集所有唯一的客户ID和名称
            Set<String> companyIds = orders.stream()
                    .map(GhOrder::getCompanyId)
                    .filter(oConvertUtils::isNotEmpty)
                    .collect(Collectors.toSet());

            Set<String> companyNames = orders.stream()
                    .filter(o -> oConvertUtils.isEmpty(o.getCompanyId()))
                    .map(GhOrder::getCompanyName)
                    .filter(oConvertUtils::isNotEmpty)
                    .collect(Collectors.toSet());

            // 一次性查询所有客户的订单数量（按客户ID分组统计）
            if (!companyIds.isEmpty()) {
                try {
                    List<Map<String, Object>> countResults = ghOrderMapper.selectMaps(
                            new QueryWrapper<GhOrder>()
                                    .select("company_id", "COUNT(*) as order_count")
                                    .eq("del_flag", 0)
                                    .in("company_id", companyIds)
                                    .groupBy("company_id")
                    );

                    for (Map<String, Object> row : countResults) {
                        String companyId = (String) row.get("company_id");
                        Long count = (Long) row.get("order_count");
                        if (count != null) {
                            customerOrderCountMap.put("id_" + companyId, count.intValue());
                            customerRecurringMap.put("id_" + companyId, count > 1 ? "1" : "0");
                        }
                    }
                } catch (Exception e) {
                    log.error("批量查询客户订单数失败（按ID）", e);
                }
            }

            // 一次性查询所有客户的订单数量（按客户名称分组统计）
            if (!companyNames.isEmpty()) {
                try {
                    List<Map<String, Object>> countResults = ghOrderMapper.selectMaps(
                            new QueryWrapper<GhOrder>()
                                    .select("company_name", "COUNT(*) as order_count")
                                    .eq("del_flag", 0)
                                    .in("company_name", companyNames)
                                    .groupBy("company_name")
                    );

                    for (Map<String, Object> row : countResults) {
                        String companyName = (String) row.get("company_name");
                        Long count = (Long) row.get("order_count");
                        if (count != null) {
                            customerOrderCountMap.put("name_" + companyName, count.intValue());
                            customerRecurringMap.put("name_" + companyName, count > 1 ? "1" : "0");
                        }
                    }
                } catch (Exception e) {
                    log.error("批量查询客户订单数失败（按名称）", e);
                }
            }

            // 7. 遍历订单，从Map中填充数据（内存操作，极快）
            final Map<String, List<org.jeecg.modules.system.entity.SysAuditTask>> finalAuditTasksMap = auditTasksMap;
            for (GhOrder orderItem : orders) {
                // 设置审核记录
                if (orderItem.getAuditProcessId() != null) {
                    List<org.jeecg.modules.order.entity.GhOrderAudit> audits = auditsMap.get(orderItem.getId());
                    orderItem.setOrderAudits(audits != null ? audits : new ArrayList<>());
                } else {
                    orderItem.setOrderAudits(new ArrayList<>());
                }

                // 设置工商任务状态
                org.jeecg.modules.order.entity.GhBusinessTask task = tasksMap.get(orderItem.getId());
                if (task != null) {
                    orderItem.setTaskStatus(task.getTaskStatus());
                    orderItem.setHandoverStatus(task.getHandoverStatus());
                }

                // 设置业务员团队名称
                if (oConvertUtils.isNotEmpty(orderItem.getSalesman())) {
                    org.jeecg.modules.system.entity.SysUser user = userMap.get(orderItem.getSalesman());
                    if (user != null && oConvertUtils.isNotEmpty(user.getTeamId())) {
                        org.jeecg.modules.system.entity.SysTeam team = teamMap.get(user.getTeamId());
                        if (team != null) {
                            orderItem.setTeamName(team.getTeamName());
                        }
                    }
                }

                // 设置第一个审核步骤通过的时间
                if ("1".equals(orderItem.getOrderStatus()) && finalAuditTasksMap != null) {
                    List<org.jeecg.modules.system.entity.SysAuditTask> tasks = finalAuditTasksMap.get(orderItem.getId());
                    if (tasks != null && !tasks.isEmpty()) {
                        org.jeecg.modules.system.entity.SysAuditTask firstApprovedTask = tasks.stream()
                                .filter(t -> t.getStepOrder() != null && t.getStepOrder() == 1)
                                .filter(t -> "approved".equals(t.getTaskStatus()))
                                .filter(t -> t.getAuditTime() != null)
                                .min((t1, t2) -> t1.getAuditTime().compareTo(t2.getAuditTime()))
                                .orElse(null);

                        if (firstApprovedTask != null && firstApprovedTask.getAuditTime() != null) {
                            orderItem.setFirstAuditTime(firstApprovedTask.getAuditTime());
                        }
                    }
                }

                // 设置复购信息
                String companyId = orderItem.getCompanyId();
                String companyName = orderItem.getCompanyName();

                if (oConvertUtils.isNotEmpty(companyId)) {
                    String key = "id_" + companyId;
                    orderItem.setCustomerOrderCount(customerOrderCountMap.getOrDefault(key, 0));
                    orderItem.setIsRecurring(customerRecurringMap.getOrDefault(key, "0"));
                } else if (oConvertUtils.isNotEmpty(companyName)) {
                    String key = "name_" + companyName;
                    orderItem.setCustomerOrderCount(customerOrderCountMap.getOrDefault(key, 0));
                    orderItem.setIsRecurring(customerRecurringMap.getOrDefault(key, "0"));
                } else {
                    orderItem.setCustomerOrderCount(0);
                    orderItem.setIsRecurring("0");
                }
            }
        }

        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
         * 添加
         * @param order
         * @return
         */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['companyName'] + '_' + #jsonObject['salesman']")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            GhOrder order = jsonObject.toJavaObject(GhOrder.class);
            
            // 如果前端提交了contractNo（合同编号），映射到orderNo（订单编号）
            if (jsonObject.containsKey("contractNo") && oConvertUtils.isNotEmpty(jsonObject.getString("contractNo"))) {
                order.setOrderNo(jsonObject.getString("contractNo"));
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                order.setCreateBy(sysUser.getUsername());
            }
            
            // 根据业务类型自动匹配流程（必须是最底层业务类型）
            if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
                // 优先查找一次性任务的绑定（订单创建默认是一次性任务）
                SysAuditProcessBinding binding = sysAuditProcessBindingService.getBindingByBusinessTypeAndTaskType(
                    order.getBusinessType(), "once");
                
                // 如果没有找到一次性任务的绑定，尝试查找任意绑定
                if (binding == null) {
                    binding = sysAuditProcessBindingService.getBindingByBusinessTypeId(order.getBusinessType());
                }
                
                if (binding != null) {
                    order.setMatchedProcessId(binding.getProcessId());
                    // 如果绑定中没有taskType，默认设置为'once'（一次性任务）
                    String taskType = binding.getTaskType();
                    if (oConvertUtils.isEmpty(taskType)) {
                        taskType = "once";
                    }
                    order.setTaskType(taskType);
                    // 设置审核流程ID（兼容旧逻辑）
                    order.setAuditProcessId(binding.getProcessId());
                } else {
                    // 业务类型没有绑定审核流程，返回错误提示
                    result.error500("选择的业务类型未配置审核流程，请联系管理员为该业务类型绑定审核流程！");
                    return result;
                }
            }
            
            // 保存订单和步骤
            boolean success = ghOrderService.saveMain(order, order.getSteps());
            if (success) {
                // 如果匹配到流程，创建初始审核任务
                if (oConvertUtils.isNotEmpty(order.getMatchedProcessId())) {
                    try {
                        SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(order.getMatchedProcessId());
                        if (process == null) {
                            // 流程不存在，可能是流程被删除了但绑定记录还在
                            log.error("订单创建时，绑定的审核流程不存在，流程ID: {}, 订单ID: {}", order.getMatchedProcessId(), order.getId());
                            result.error500("绑定的审核流程不存在，可能流程已被删除。请联系管理员重新配置业务类型的审核流程！");
                            return result;
                        }
                        
                        if (process.getSteps() == null || process.getSteps().isEmpty()) {
                            log.error("订单创建时，审核流程没有配置步骤，流程ID: {}, 订单ID: {}", order.getMatchedProcessId(), order.getId());
                            result.error500("审核流程未配置审核步骤，请联系管理员配置审核步骤！");
                            return result;
                        }
                        
                        // 获取第一步（stepOrder=1的所有步骤，支持并行审核）
                        List<SysAuditStep> firstSteps = new java.util.ArrayList<>();
                        for (SysAuditStep step : process.getSteps()) {
                            if (step.getStepOrder() != null && step.getStepOrder() == 1) {
                                firstSteps.add(step);
                            }
                        }
                        
                        if (firstSteps.isEmpty()) {
                            log.error("订单创建时，审核流程没有第一步，流程ID: {}, 订单ID: {}", order.getMatchedProcessId(), order.getId());
                            result.error500("审核流程未配置第一步审核步骤，请联系管理员配置审核步骤！");
                            return result;
                        }
                        
                        // 为第一步的每个角色创建审核任务（支持并行审核）
                        String taskType = order.getTaskType();
                        if (oConvertUtils.isEmpty(taskType)) {
                            taskType = "once"; // 默认一次性任务
                            log.warn("订单创建时taskType为空，默认设置为'once'，订单ID: {}", order.getId());
                        }
                        log.info("订单创建，开始创建审核任务，订单ID: {}, 流程ID: {}, taskType: {}, 第一步步骤数: {}", 
                                order.getId(), order.getMatchedProcessId(), taskType, firstSteps.size());
                        
                        for (SysAuditStep step : firstSteps) {
                            try {
                                SysAuditTask task = sysAuditTaskService.createTask(
                                    order.getId(),
                                    order.getMatchedProcessId(),
                                    step.getId(),
                                    step.getStepOrder(),
                                    taskType
                                );
                                log.info("成功创建审核任务，任务ID: {}, 订单ID: {}, 步骤ID: {}, 角色ID: {}, taskType: {}", 
                                        task.getId(), order.getId(), step.getId(), step.getRoleId(), taskType);
                            } catch (Exception e) {
                                log.error("创建审核任务失败，订单ID: {}, 步骤ID: {}, 角色ID: {}", 
                                        order.getId(), step.getId(), step.getRoleId(), e);
                                throw e; // 重新抛出异常，让外层catch处理
                            }
                        }
                        
                        // 更新订单的当前审核步骤
                        order.setCurrentAuditStep(1);
                        order.setAuditStatus("pending");
                        order.setOrderStatus("0"); // 待审核
                        ghOrderService.updateById(order);
                        log.info("订单创建完成，订单ID: {}, 订单状态: 0(待审核), 审核状态: pending", order.getId());
                    } catch (Exception e) {
                        log.error("创建审核任务失败，订单ID: " + order.getId(), e);
                        result.error500("创建审核任务失败：" + e.getMessage());
                        return result;
                    }
                }
                // 保存动态表单数据
                if (jsonObject.containsKey("dynamicFormVersionId") && jsonObject.containsKey("dynamicFormData")) {
                    String versionId = jsonObject.getString("dynamicFormVersionId");
                    String formData = jsonObject.getString("dynamicFormData");
                    String formId = jsonObject.getString("formId"); // 从请求中获取formId
                    String formConfig = jsonObject.getString("dynamicFormConfig"); // 获取表单配置
                    if (oConvertUtils.isNotEmpty(versionId) && oConvertUtils.isNotEmpty(formData)) {
                        ghOrderFormDataService.saveOrUpdateFormData(
                            order.getId(),
                            formId, // 使用前端传递的formId
                            versionId,
                            formData,
                            formConfig // 传递表单配置
                        );
                    }
                }
                
                // 如果有收款金额，自动创建收费记录（下合同也算收费）
                if (order.getReceivedAmount() != null && order.getReceivedAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    GhOrderPayment payment = new GhOrderPayment();
                    payment.setOrderId(order.getId());
                    payment.setAmount(order.getReceivedAmount());
                    payment.setPaymentTime(order.getCollectionTime() != null ? order.getCollectionTime() : new Date());
                    payment.setStatus("1"); // 已确认（提交时直接确认）
                    payment.setRemarks("下合同收款");
                    
                    // 如果有收款账号，查询收款方式
                    if (oConvertUtils.isNotEmpty(order.getCollectionAccountNumber())) {
                        GhBankManagement bankAccount = ghBankManagementService.getById(order.getCollectionAccountNumber());
                        if (bankAccount != null) {
                            payment.setPaymentMethod(bankAccount.getPaymentMethod());
                            // accountNumber保存为银行账户ID
                            payment.setAccountNumber(order.getCollectionAccountNumber());
                        } else {
                            // 如果查询不到，可能是旧的字符串格式，保留原值
                            payment.setAccountNumber(order.getCollectionAccountNumber());
                        }
                    }
                    
                    // 设置创建信息
                    if (sysUser != null) {
                        payment.setCreateBy(sysUser.getUsername());
                    }
                    payment.setCreateTime(new Date());
                    
                    // 保存收费记录
                    ghOrderPaymentService.save(payment);
                    
                    // 订单提交时立即创建资金日记（收入记录）
                    // 根据收款金额和收款账户创建资金日记
                    if (oConvertUtils.isNotEmpty(order.getCollectionAccountNumber()) && ghBankDiaryService != null) {
                        try {
                            createBankDiaryFromOrderPayment(order, payment, sysUser);
                        } catch (Exception e) {
                            log.error("订单提交时创建资金日记失败，订单ID: {}", order.getId(), e);
                            // 不抛出异常，避免影响订单创建流程
                        }
                    }
                }
                
                // 记录操作日志：订单创建
                String operatorName = sysUser != null ? sysUser.getRealname() : order.getCreateBy();
                ghOrderOperationLogService.saveOperationLog(
                    order.getId(),
                    "create",
                    "创建订单",
                    operatorName,
                    null,
                    JSONObject.toJSONString(order),
                    null,
                    "订单创建，订单编号：" + order.getOrderNo()
                );
                
                // 发送企业微信通知
                try {
                    if (sysUser != null && wechatNotificationService != null) {
                        String salesmanUsername = sysUser.getUsername();
                        String salesmanRealname = sysUser.getRealname();
                        
                        // 1. 发送通知给业务员
                        try {
                            wechatNotificationService.sendOrderSubmitNotification(
                                order.getId(), 
                                salesmanUsername
                            );
                            log.info("订单提交通知已发送给业务员：{}", salesmanUsername);
                        } catch (Exception e) {
                            log.error("发送业务员通知失败", e);
                        }
                        
                        // 2. 发送通知给第一步审核人（出纳或其他角色）
                        try {
                            // 获取第一步审核任务的审核人
                            List<String> firstStepAuditors = getFirstStepAuditors(order.getId());
                            if (firstStepAuditors != null && !firstStepAuditors.isEmpty()) {
                                for (String auditorUsername : firstStepAuditors) {
                                    wechatNotificationService.sendOrderAuditNotification(
                                        order.getId(), 
                                        auditorUsername
                                    );
                                    log.info("订单审核通知已发送给审核人：{}", auditorUsername);
                                }
                            }
                        } catch (Exception e) {
                            log.error("发送审核人通知失败", e);
                        }
                        
                        // 3. 发送通知给上级
                        try {
                            String supervisorUsername = getSupervisorUsername(sysUser.getId());
                            if (oConvertUtils.isNotEmpty(supervisorUsername)) {
                                wechatNotificationService.sendOrderSubmitToSupervisorNotification(
                                    order.getId(), 
                                    salesmanRealname,
                                    supervisorUsername
                                );
                                log.info("订单提交通知已发送给上级：{}", supervisorUsername);
                            }
                        } catch (Exception e) {
                            log.error("发送上级通知失败", e);
                        }
                    }
                } catch (Exception e) {
                    log.error("发送企业微信通知失败", e);
                    // 通知失败不影响业务流程
                }
                
                result.success("添加成功！");
            } else {
                result.error500("添加失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 编辑
     * @param order
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['id']")
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<?> edit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            GhOrder order = jsonObject.toJavaObject(GhOrder.class);
            
            GhOrder existingOrder = ghOrderService.getById(order.getId());
            if (existingOrder == null) {
                result.error500("未找到对应实体");
                return result;
            }
            
            // 检查订单是否已经通过第一个审核步骤，如果已通过则不允许修改
            if ("1".equals(existingOrder.getOrderStatus()) || "2".equals(existingOrder.getOrderStatus())) {
                result.error500("订单已通过第一个审核步骤，不能修改信息");
                return result;
            }
            
            // 检查第一个审核步骤是否已通过（通过查询审核任务）
            if (sysAuditTaskService != null) {
                try {
                    List<org.jeecg.modules.system.entity.SysAuditTask> tasks = sysAuditTaskService.getTasksByOrderId(order.getId());
                    if (tasks != null && !tasks.isEmpty()) {
                        // 检查是否存在第一个步骤（stepOrder=1）且已通过的任务
                        boolean firstStepApproved = tasks.stream()
                            .anyMatch(t -> t.getStepOrder() != null && t.getStepOrder() == 1 
                                && "approved".equals(t.getTaskStatus()));
                        if (firstStepApproved) {
                            result.error500("订单已通过第一个审核步骤，不能修改信息");
                            return result;
                        }
                    }
                } catch (Exception e) {
                    log.debug("检查订单审核步骤失败，订单ID: {}", order.getId(), e);
                    // 如果检查失败，继续执行（避免因为检查失败而阻止正常编辑）
                }
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                order.setUpdateBy(sysUser.getUsername());
            }
            
            // 获取修改前的动态表单数据
            GhOrderFormData oldFormData = ghOrderFormDataService.getFormDataByOrderId(order.getId());
            String oldFormDataStr = oldFormData != null ? oldFormData.getFormData() : null;
            
            // 更新订单和步骤
            boolean success = ghOrderService.updateMain(order, order.getSteps());
            if (success) {
                // 保存或更新动态表单数据
                String newFormDataStr = null;
                boolean formDataChanged = false;
                if (jsonObject.containsKey("dynamicFormData")) {
                    String formData = jsonObject.getString("dynamicFormData");
                    // 即使 formData 是空字符串或 "{}"，也要处理
                    if (formData != null) {
                        // 如果 formData 是空字符串或 "{}"，视为 null
                        String trimmedFormData = formData.trim();
                        if (trimmedFormData.isEmpty() || trimmedFormData.equals("{}") || trimmedFormData.equals("null")) {
                            newFormDataStr = null;
                        } else {
                            newFormDataStr = trimmedFormData;
                        }
                        
                        // 比较动态表单数据是否有变化（使用JSON比较）
                        formDataChanged = isFormDataChanged(oldFormDataStr, newFormDataStr);
                        
                        // 如果有版本ID和表单ID，保存或更新动态表单数据
                        if (jsonObject.containsKey("dynamicFormVersionId") || jsonObject.containsKey("formVersionId")) {
                            String versionId = jsonObject.getString("dynamicFormVersionId");
                            if (oConvertUtils.isEmpty(versionId)) {
                                versionId = jsonObject.getString("formVersionId");
                            }
                            String formId = jsonObject.getString("formId"); // 从请求中获取formId
                            String formConfig = jsonObject.getString("dynamicFormConfig"); // 获取表单配置
                            if (oConvertUtils.isNotEmpty(versionId) && oConvertUtils.isNotEmpty(formId)) {
                                ghOrderFormDataService.saveOrUpdateFormData(
                                    order.getId(),
                                    formId, // 使用前端传递的formId
                                    versionId,
                                    newFormDataStr != null ? newFormDataStr : "{}", // 如果为null，保存空对象
                                    formConfig // 传递表单配置（编辑时保持原有配置，新增时保存新配置）
                                );
                            }
                        }
                    }
                }
                
                // 如果修改了收款金额，更新对应的收费记录
                if (order.getReceivedAmount() != null) {
                    java.math.BigDecimal oldAmount = existingOrder.getReceivedAmount() != null ? existingOrder.getReceivedAmount() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal newAmount = order.getReceivedAmount();
                    
                    if (newAmount.compareTo(oldAmount) != 0) {
                        // 查找"下合同收款"的记录并更新金额
                        LambdaQueryWrapper<GhOrderPayment> paymentWrapper = new LambdaQueryWrapper<>();
                        paymentWrapper.eq(GhOrderPayment::getOrderId, order.getId());
                        paymentWrapper.eq(GhOrderPayment::getRemarks, "下合同收款");
                        List<GhOrderPayment> payments = ghOrderPaymentService.list(paymentWrapper);
                        
                        if (payments != null && !payments.isEmpty()) {
                            // 更新第一条"下合同收款"记录的金额
                            GhOrderPayment payment = payments.get(0);
                            payment.setAmount(newAmount);
                            if (order.getCollectionTime() != null) {
                                payment.setPaymentTime(order.getCollectionTime());
                            }
                            if (sysUser != null) {
                                payment.setUpdateBy(sysUser.getUsername());
                            }
                            payment.setUpdateTime(new Date());
                            ghOrderPaymentService.updateById(payment);
                        } else if (newAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                            // 如果没有"下合同收款"记录，但新金额大于0，创建新记录
                            GhOrderPayment payment = new GhOrderPayment();
                            payment.setOrderId(order.getId());
                            payment.setAmount(newAmount);
                            payment.setPaymentTime(order.getCollectionTime() != null ? order.getCollectionTime() : new Date());
                            payment.setStatus("1"); // 已确认（提交时直接确认）
                            payment.setRemarks("下合同收款");
                            
                            // 如果有收款账号，查询收款方式
                            if (oConvertUtils.isNotEmpty(order.getCollectionAccountNumber())) {
                                GhBankManagement bankAccount = ghBankManagementService.getById(order.getCollectionAccountNumber());
                                if (bankAccount != null) {
                                    payment.setPaymentMethod(bankAccount.getPaymentMethod());
                                    payment.setAccountNumber(bankAccount.getCollectionAccount());
                                } else {
                                    payment.setAccountNumber(order.getCollectionAccountNumber());
                                }
                            }
                            
                            if (sysUser != null) {
                                payment.setCreateBy(sysUser.getUsername());
                            }
                            payment.setCreateTime(new Date());
                            ghOrderPaymentService.save(payment);
                        } else if (newAmount.compareTo(java.math.BigDecimal.ZERO) == 0 && oldAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                            // 如果新金额为0，但旧金额大于0，删除"下合同收款"记录
                            for (GhOrderPayment payment : payments) {
                                ghOrderPaymentService.removeById(payment.getId());
                            }
                        }
                    }
                }
                
                // 记录操作日志：订单修改（包含详细的变更信息）
                String operatorName = sysUser != null ? sysUser.getRealname() : order.getUpdateBy();
                String changedFields = getChangedFields(existingOrder, order);
                
                // 如果动态表单有变化，添加到变更字段列表
                if (formDataChanged) {
                    if (oConvertUtils.isNotEmpty(changedFields)) {
                        changedFields += "、动态表单";
                    } else {
                        changedFields = "动态表单";
                    }
                }
                
                // 生成详细的变更描述（包含动态表单的具体字段变化）
                // 注意：如果 formDataChanged 为 true，确保传入正确的 oldFormDataStr 和 newFormDataStr
                String changeDesc = buildChangeDescription(existingOrder, order, oldFormDataStr, newFormDataStr);
                
                // 调试日志：输出变更信息
                if (formDataChanged) {
                    log.info("动态表单变化检测 - oldFormDataStr: " + (oldFormDataStr != null ? oldFormDataStr.substring(0, Math.min(100, oldFormDataStr.length())) : "null") + 
                            ", newFormDataStr: " + (newFormDataStr != null ? newFormDataStr.substring(0, Math.min(100, newFormDataStr.length())) : "null") +
                            ", changeDesc: " + changeDesc);
                }
                
                // 如果没有任何变更，不记录操作日志
                if (oConvertUtils.isEmpty(changedFields) && oConvertUtils.isEmpty(changeDesc)) {
                    result.success("修改成功!");
                    return result;
                }
                
                // 记录操作日志（使用 try-catch 确保即使日志保存失败也不影响订单修改）
                try {
                    ghOrderOperationLogService.saveOperationLog(
                        order.getId(),
                        "update",
                        "修改订单" + (oConvertUtils.isNotEmpty(changedFields) ? "（" + changedFields + "）" : ""),
                        operatorName,
                        JSONObject.toJSONString(existingOrder) + (oldFormDataStr != null ? "|动态表单:" + oldFormDataStr : ""),
                        JSONObject.toJSONString(order) + (newFormDataStr != null ? "|动态表单:" + newFormDataStr : ""),
                        changedFields,
                        changeDesc
                    );
                } catch (Exception e) {
                    log.error("保存订单修改操作记录失败，订单ID: {}", order.getId(), e);
                    // 不抛出异常，避免影响订单修改流程
                }
                
                result.success("修改成功!");
            } else {
                result.error500("修改失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 通过id删除
     * @param id
     * @return
     */
    @JRepeat(lockTime = 3, lockKey = "#id")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        Result<?> result = new Result<>();
        try {
            GhOrder order = ghOrderService.getById(id);
            if (order != null) {
                // 删除所有关联数据
                // 1. 删除订单流程步骤
                ghOrderStepService.deleteByOrderId(id);
                
                // 2. 删除订单收费记录
                LambdaQueryWrapper<GhOrderPayment> paymentWrapper = new LambdaQueryWrapper<>();
                paymentWrapper.eq(GhOrderPayment::getOrderId, id);
                ghOrderPaymentService.remove(paymentWrapper);
                
                // 3. 删除订单支出记录
                LambdaQueryWrapper<GhOrderExpense> expenseWrapper = new LambdaQueryWrapper<>();
                expenseWrapper.eq(GhOrderExpense::getOrderId, id);
                ghOrderExpenseService.remove(expenseWrapper);
                
                // 4. 删除订单动态表单数据
                LambdaQueryWrapper<GhOrderFormData> formDataWrapper = new LambdaQueryWrapper<>();
                formDataWrapper.eq(GhOrderFormData::getOrderId, id);
                ghOrderFormDataService.remove(formDataWrapper);
                
                // 5. 记录删除操作日志（保留操作记录用于审计）
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                ghOrderOperationLogService.saveOperationLog(
                    id,
                    "delete",
                    "删除订单",
                    operatorName,
                    JSONObject.toJSONString(order),
                    null,
                    null,
                    "订单已删除，订单编号：" + order.getOrderNo()
                );
                
                // 6. 订单非正常完成状态：扣除资金日记
                try {
                    deductBankDiaryForAbnormalOrder(order.getId(), "删除", "订单已删除");
                } catch (Exception e) {
                    log.error("删除订单时扣除资金日记失败，订单ID: {}", id, e);
                    // 不抛出异常，避免影响订单删除流程
                }
                
                // 7. 回退支出记录和报销（只回退未提交状态的报销）
                try {
                    rollbackExpenseAndReimbursement(order.getId(), "删除", "订单已删除");
                } catch (Exception e) {
                    log.error("删除订单时回退支出记录和报销失败，订单ID: {}", id, e);
                    // 不抛出异常，避免影响订单删除流程
                }
                
                // 8. 逻辑删除订单主表（不删除操作记录，保留用于审计）
                order.setDelFlag(1);
                ghOrderService.updateById(order);
                
                result.success("删除成功!");
            } else {
                result.error500("未找到对应实体");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("删除失败！");
        }
        return result;
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        Result<?> result = new Result<>();
        try {
            if (oConvertUtils.isEmpty(ids)) {
                result.error500("参数不识别！");
                return result;
            }
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                GhOrder order = ghOrderService.getById(id);
                if (order != null) {
                    // 删除所有关联数据
                    // 1. 删除订单流程步骤
                    ghOrderStepService.deleteByOrderId(id);
                    
                    // 2. 删除订单收费记录
                    LambdaQueryWrapper<GhOrderPayment> paymentWrapper = new LambdaQueryWrapper<>();
                    paymentWrapper.eq(GhOrderPayment::getOrderId, id);
                    ghOrderPaymentService.remove(paymentWrapper);
                    
                    // 3. 删除订单支出记录
                    LambdaQueryWrapper<GhOrderExpense> expenseWrapper = new LambdaQueryWrapper<>();
                    expenseWrapper.eq(GhOrderExpense::getOrderId, id);
                    ghOrderExpenseService.remove(expenseWrapper);
                    
                    // 4. 删除订单动态表单数据
                    LambdaQueryWrapper<GhOrderFormData> formDataWrapper = new LambdaQueryWrapper<>();
                    formDataWrapper.eq(GhOrderFormData::getOrderId, id);
                    ghOrderFormDataService.remove(formDataWrapper);
                    
                    // 5. 记录删除操作日志（保留操作记录用于审计）
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                    ghOrderOperationLogService.saveOperationLog(
                        id,
                        "delete",
                        "删除订单",
                        operatorName,
                        JSONObject.toJSONString(order),
                        null,
                        null,
                        "订单已删除，订单编号：" + order.getOrderNo()
                    );
                    
                    // 6. 订单非正常完成状态：扣除资金日记
                    try {
                        deductBankDiaryForAbnormalOrder(order.getId(), "删除", "订单已删除");
                    } catch (Exception e) {
                        log.error("删除订单时扣除资金日记失败，订单ID: {}", id, e);
                        // 不抛出异常，避免影响订单删除流程
                    }
                    
                    // 7. 回退支出记录和报销（只回退未提交状态的报销）
                    try {
                        rollbackExpenseAndReimbursement(order.getId(), "删除", "订单已删除");
                    } catch (Exception e) {
                        log.error("删除订单时回退支出记录和报销失败，订单ID: {}", id, e);
                        // 不抛出异常，避免影响订单删除流程
                    }
                    
                    // 8. 逻辑删除订单主表（不删除操作记录，保留用于审计）
                    order.setDelFlag(1);
                    ghOrderService.updateById(order);
                }
            }
            result.success("删除成功!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("删除失败！");
        }
        return result;
    }

    /**
     * 退单
     * @param jsonObject 包含订单ID和退单原因
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['id']")
    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public Result<?> refund(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("id");
            String reason = jsonObject.getString("reason");
            
            if (oConvertUtils.isEmpty(orderId)) {
                result.error500("订单ID不能为空");
                return result;
            }
            
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                result.error500("未找到对应订单");
                return result;
            }
            
            // 只有未审核和进行中的订单可以退单
            String orderStatus = order.getOrderStatus();
            if (!"0".equals(orderStatus) && !"1".equals(orderStatus)) {
                result.error500("只有未审核和进行中的订单可以退单");
                return result;
            }
            
            // 保存修改前的订单状态（用于操作日志）
            String oldOrderStatus = order.getOrderStatus();
            String oldAuditStatus = order.getAuditStatus();
            String oldRemarks = order.getRemarks();
            
            // 更新订单状态为退单（假设退单状态为"3"）
            order.setOrderStatus("3");
            order.setAuditStatus("rejected"); // 退单时审核状态设为已驳回
            order.setRemarks(order.getRemarks() != null ? 
                order.getRemarks() + "\n退单原因：" + reason : 
                "退单原因：" + reason);
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                order.setUpdateBy(sysUser.getUsername());
            }
            order.setUpdateTime(new Date());
            
            boolean success = ghOrderService.updateById(order);
            if (success) {
                // 1. 订单非正常完成状态：扣除资金日记（平账）
                try {
                    deductBankDiaryForAbnormalOrder(order.getId(), "退单", reason);
                } catch (Exception e) {
                    log.error("退单时扣除资金日记失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响订单状态更新
                }
                
                // 2. 回退支出记录和报销（只回退未提交状态的报销）
                try {
                    rollbackExpenseAndReimbursement(order.getId(), "退单", reason);
                } catch (Exception e) {
                    log.error("退单时回退支出记录和报销失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响订单状态更新
                }
                
                // 3. 记录操作日志（记录修改前后的状态）
                String operatorName = sysUser != null ? sysUser.getRealname() : order.getUpdateBy();
                // 创建修改前的订单对象用于日志记录
                GhOrder oldOrder = new GhOrder();
                oldOrder.setOrderStatus(oldOrderStatus);
                oldOrder.setAuditStatus(oldAuditStatus);
                oldOrder.setRemarks(oldRemarks);
                // 复制其他字段
                oldOrder.setId(order.getId());
                oldOrder.setOrderNo(order.getOrderNo());
                oldOrder.setCompanyName(order.getCompanyName());
                oldOrder.setContractAmount(order.getContractAmount());
                oldOrder.setBusinessType(order.getBusinessType());
                
                String oldStatusText = getDictText("order_status", oldOrderStatus);
                String newStatusText = getDictText("order_status", "3");
                ghOrderOperationLogService.saveOperationLog(
                    order.getId(),
                    "refund",
                    "退单",
                    operatorName,
                    JSONObject.toJSONString(oldOrder),
                    JSONObject.toJSONString(order),
                    "订单状态",
                    "订单状态：" + (oldStatusText != null ? oldStatusText : oldOrderStatus) + " → " + (newStatusText != null ? newStatusText : "退单") + "；退单原因：" + reason
                );
                
                result.success("退单成功！");
            } else {
                result.error500("退单失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 退单审核（审核通过后删除订单及相关财务记录）
     * @param jsonObject 包含订单ID
     * @return
     */
    @RequiresPermissions("order:auditRefund")
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['id']")
    @RequestMapping(value = "/auditRefund", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> auditRefund(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("id");
            
            if (oConvertUtils.isEmpty(orderId)) {
                result.error500("订单ID不能为空");
                return result;
            }
            
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                result.error500("未找到对应订单");
                return result;
            }
            
            // 只有退单状态的订单可以审核
            if (!"3".equals(order.getOrderStatus())) {
                result.error500("只有退单状态的订单可以审核");
                return result;
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
            
            // 1. 删除订单流程步骤
            try {
                ghOrderStepService.deleteByOrderId(orderId);
            } catch (Exception e) {
                log.error("删除订单流程步骤失败，订单ID: {}", orderId, e);
            }
            
            // 2. 删除订单收费记录（逻辑删除）
            try {
                LambdaQueryWrapper<GhOrderPayment> paymentWrapper = new LambdaQueryWrapper<>();
                paymentWrapper.eq(GhOrderPayment::getOrderId, orderId);
                paymentWrapper.eq(GhOrderPayment::getDelFlag, 0);
                List<GhOrderPayment> payments = ghOrderPaymentService.list(paymentWrapper);
                for (GhOrderPayment payment : payments) {
                    payment.setDelFlag(1);
                    payment.setUpdateTime(new Date());
                    if (sysUser != null) {
                        payment.setUpdateBy(sysUser.getUsername());
                    }
                    ghOrderPaymentService.updateById(payment);
                }
            } catch (Exception e) {
                log.error("删除订单收费记录失败，订单ID: {}", orderId, e);
            }
            
            // 3. 删除订单支出记录（逻辑删除）
            try {
                LambdaQueryWrapper<GhOrderExpense> expenseWrapper = new LambdaQueryWrapper<>();
                expenseWrapper.eq(GhOrderExpense::getOrderId, orderId);
                expenseWrapper.eq(GhOrderExpense::getDelFlag, 0);
                List<GhOrderExpense> expenses = ghOrderExpenseService.list(expenseWrapper);
                for (GhOrderExpense expense : expenses) {
                    expense.setDelFlag(1);
                    expense.setUpdateTime(new Date());
                    if (sysUser != null) {
                        expense.setUpdateBy(sysUser.getUsername());
                    }
                    ghOrderExpenseService.updateById(expense);
                }
            } catch (Exception e) {
                log.error("删除订单支出记录失败，订单ID: {}", orderId, e);
            }
            
            // 4. 删除订单动态表单数据
            try {
                LambdaQueryWrapper<GhOrderFormData> formDataWrapper = new LambdaQueryWrapper<>();
                formDataWrapper.eq(GhOrderFormData::getOrderId, orderId);
                ghOrderFormDataService.remove(formDataWrapper);
            } catch (Exception e) {
                log.error("删除订单动态表单数据失败，订单ID: {}", orderId, e);
            }
            
            // 5. 删除银行日记账记录（平账）
            try {
                deleteBankDiaryForOrder(orderId, sysUser);
            } catch (Exception e) {
                log.error("删除银行日记账记录失败，订单ID: {}", orderId, e);
            }
            
            // 6. 记录操作日志
            try {
                ghOrderOperationLogService.saveOperationLog(
                    orderId,
                    "auditRefund",
                    "退单审核",
                    operatorName,
                    JSONObject.toJSONString(order),
                    null,
                    null,
                    "退单审核通过，订单已删除，相关财务记录已删除并平账"
                );
            } catch (Exception e) {
                log.error("记录操作日志失败，订单ID: {}", orderId, e);
            }
            
            // 7. 逻辑删除订单主表
            order.setDelFlag(1);
            order.setUpdateTime(new Date());
            if (sysUser != null) {
                order.setUpdateBy(sysUser.getUsername());
            }
            ghOrderService.updateById(order);
            
            result.success("退单审核成功！订单及相关财务记录已删除");
        } catch (Exception e) {
            log.error("退单审核失败", e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 删除订单相关的银行日记账记录并平账
     * @param orderId 订单ID
     * @param sysUser 当前用户
     */
    private void deleteBankDiaryForOrder(String orderId, LoginUser sysUser) {
        if (ghBankDiaryService == null || oConvertUtils.isEmpty(orderId)) {
            return;
        }
        
        log.info("开始删除订单相关的银行日记账记录，订单ID: {}", orderId);
        
        try {
            // 查找该订单的所有银行日记账记录（包括收入和支出）
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId);
            queryWrapper.eq("del_flag", 0);
            List<GhBankDiary> bankDiaries = ghBankDiaryService.list(queryWrapper);
            
            if (bankDiaries != null && !bankDiaries.isEmpty()) {
                // 按账户分组，记录需要更新结余的账户和日期
                Map<String, Date> accountDates = new HashMap<>();
                
                for (GhBankDiary bankDiary : bankDiaries) {
                    String bankAccountId = bankDiary.getBankAccountId();
                    Date orderDate = bankDiary.getOrderDate();
                    
                    // 逻辑删除银行日记账记录
                    bankDiary.setDelFlag(1);
                    bankDiary.setUpdateTime(new Date());
                    if (sysUser != null) {
                        bankDiary.setUpdateBy(sysUser.getUsername());
                    }
                    ghBankDiaryService.updateById(bankDiary);
                    
                    log.info("删除银行日记账记录，订单ID: {}, 银行日记账ID: {}, 账户ID: {}, 收入: {}, 支出: {}", 
                            orderId, bankDiary.getId(), bankAccountId, 
                            bankDiary.getIncomeAmount(), bankDiary.getExpenseAmount());
                    
                    // 记录需要更新结余的账户和日期（取最早的日期）
                    if (bankAccountId != null && orderDate != null) {
                        Date existingDate = accountDates.get(bankAccountId);
                        if (existingDate == null || orderDate.before(existingDate)) {
                            accountDates.put(bankAccountId, orderDate);
                        }
                    }
                }
                
                // 更新所有受影响账户的后续记录结余金额
                for (Map.Entry<String, Date> entry : accountDates.entrySet()) {
                    try {
                        updateSubsequentBankDiaryBalances(entry.getKey(), entry.getValue());
                        log.info("更新账户后续记录结余金额，账户ID: {}, 日期: {}", entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        log.error("更新账户后续记录结余金额失败，账户ID: {}, 日期: {}", entry.getKey(), entry.getValue(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("删除订单相关的银行日记账记录失败，订单ID: {}", orderId, e);
            throw e;
        }
        
        log.info("删除订单相关的银行日记账记录完成，订单ID: {}", orderId);
    }
    
    /**
     * 驳回
     * @param jsonObject 包含订单ID和驳回原因
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['id']")
    @RequestMapping(value = "/reject", method = RequestMethod.POST)
    public Result<?> reject(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("id");
            String reason = jsonObject.getString("reason");
            
            if (oConvertUtils.isEmpty(orderId)) {
                result.error500("订单ID不能为空");
                return result;
            }
            
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                result.error500("未找到对应订单");
                return result;
            }
            
            // 只有进行中的订单可以驳回
            if (!"1".equals(order.getOrderStatus())) {
                result.error500("只有进行中的订单可以驳回");
                return result;
            }
            
            // 保存修改前的订单状态（用于操作日志）
            String oldOrderStatus = order.getOrderStatus();
            String oldAuditStatus = order.getAuditStatus();
            String oldRemarks = order.getRemarks();
            
            // 更新订单状态为驳回（假设驳回状态为"4"）
            order.setOrderStatus("4");
            order.setAuditStatus("rejected"); // 驳回时审核状态设为已驳回
            order.setRemarks(order.getRemarks() != null ? 
                order.getRemarks() + "\n驳回原因：" + reason : 
                "驳回原因：" + reason);
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                order.setUpdateBy(sysUser.getUsername());
            }
            order.setUpdateTime(new Date());
            
            boolean success = ghOrderService.updateById(order);
            if (success) {
                // 1. 订单非正常完成状态：扣除资金日记（平账）
                try {
                    deductBankDiaryForAbnormalOrder(order.getId(), "驳回", reason);
                } catch (Exception e) {
                    log.error("驳回时扣除资金日记失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响订单状态更新
                }
                
                // 2. 回退支出记录和报销（只回退未提交状态的报销）
                try {
                    rollbackExpenseAndReimbursement(order.getId(), "驳回", reason);
                } catch (Exception e) {
                    log.error("驳回时回退支出记录和报销失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响订单状态更新
                }
                
                // 3. 记录操作日志（记录修改前后的状态）
                String operatorName = sysUser != null ? sysUser.getRealname() : order.getUpdateBy();
                // 创建修改前的订单对象用于日志记录
                GhOrder oldOrder = new GhOrder();
                oldOrder.setOrderStatus(oldOrderStatus);
                oldOrder.setAuditStatus(oldAuditStatus);
                oldOrder.setRemarks(oldRemarks);
                // 复制其他字段
                oldOrder.setId(order.getId());
                oldOrder.setOrderNo(order.getOrderNo());
                oldOrder.setCompanyName(order.getCompanyName());
                oldOrder.setContractAmount(order.getContractAmount());
                oldOrder.setBusinessType(order.getBusinessType());
                
                String oldStatusText = getDictText("order_status", oldOrderStatus);
                String newStatusText = getDictText("order_status", "4");
                ghOrderOperationLogService.saveOperationLog(
                    order.getId(),
                    "reject",
                    "驳回",
                    operatorName,
                    JSONObject.toJSONString(oldOrder),
                    JSONObject.toJSONString(order),
                    "订单状态",
                    "订单状态：" + (oldStatusText != null ? oldStatusText : oldOrderStatus) + " → " + (newStatusText != null ? newStatusText : "驳回") + "；驳回原因：" + reason
                );
                
                result.success("驳回成功！");
            } else {
                result.error500("驳回失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 确认订单（业务人员确认，必须在工商人员完成交接后才能确认）
     * @param jsonObject 包含订单ID
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['id']")
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> confirm(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("id");
            
            if (oConvertUtils.isEmpty(orderId)) {
                result.error500("订单ID不能为空");
                return result;
            }
            
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                result.error500("未找到对应订单");
                return result;
            }
            
            // 只有进行中的订单可以确认
            if (!"1".equals(order.getOrderStatus())) {
                result.error500("只有进行中的订单可以确认");
                return result;
            }
            
            // 检查是否有工商任务，且任务状态为已完成（completed），交接状态为已完成（completed）
            org.jeecg.modules.order.entity.GhBusinessTask task = 
                ghBusinessTaskService.getTaskByOrderId(orderId);
            if (task == null) {
                result.error500("未找到对应的工商任务，无法确认");
                return result;
            }
            
            // 必须任务状态为completed且交接状态为completed才能确认
            if (!"completed".equals(task.getTaskStatus()) || 
                !"completed".equals(task.getHandoverStatus())) {
                result.error500("工商人员尚未完成交接，无法确认订单");
                return result;
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            Date now = new Date();
            
            // 保存修改前的订单状态（用于操作日志）
            String oldOrderStatus = order.getOrderStatus();
            String oldAuditStatus = order.getAuditStatus();
            
            // 更新订单状态为已完单
            order.setOrderStatus("2"); // 2-已完单
            order.setAuditStatus("approved");
            order.setUpdateBy(sysUser.getUsername());
            order.setUpdateTime(now);
            
            boolean success = ghOrderService.updateById(order);
            if (success) {
                // 记录操作日志（记录修改前后的状态）
                String operatorName = sysUser.getRealname();
                // 创建修改前的订单对象用于日志记录
                GhOrder oldOrder = new GhOrder();
                oldOrder.setOrderStatus(oldOrderStatus);
                oldOrder.setAuditStatus(oldAuditStatus);
                // 复制其他字段
                oldOrder.setId(order.getId());
                oldOrder.setOrderNo(order.getOrderNo());
                oldOrder.setCompanyName(order.getCompanyName());
                oldOrder.setContractAmount(order.getContractAmount());
                oldOrder.setBusinessType(order.getBusinessType());
                
                String oldStatusText = getDictText("order_status", oldOrderStatus);
                String newStatusText = getDictText("order_status", "2");
                ghOrderOperationLogService.saveOperationLog(
                    order.getId(),
                    "confirm",
                    "确认",
                    operatorName,
                    JSONObject.toJSONString(oldOrder),
                    JSONObject.toJSONString(order),
                    "订单状态",
                    "订单状态：" + (oldStatusText != null ? oldStatusText : oldOrderStatus) + " → " + (newStatusText != null ? newStatusText : "已完单") + "；业务人员确认完成"
                );
                
                // 订单完成时，如果有成本支出，创建报销记录
                try {
                    if (sysAuditTaskService != null) {
                        sysAuditTaskService.createReimbursementsForCompletedOrder(order.getId());
                        log.info("订单完成，已为订单 {} 创建报销记录", order.getId());
                    }
                } catch (Exception e) {
                    log.error("订单完成时创建报销记录失败，订单ID: {}", order.getId(), e);
                    // 不抛出异常，避免影响订单确认
                }
                
                result.success("确认成功！");
            } else {
                result.error500("确认失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 通过id查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<GhOrder> queryById(@RequestParam(name="id",required=true) String id) {
        Result<GhOrder> result = new Result<>();
        try {
            GhOrder order = ghOrderService.getById(id);
            if (order == null) {
                result.error500("订单不存在");
                return result;
            }
            
            // 查询第一个审核步骤通过的时间（用于计算进行中天数）
            if ("1".equals(order.getOrderStatus()) && sysAuditTaskService != null) {
                try {
                    // 查询该订单的第一个审核步骤（stepOrder=1）且已通过的任务
                    List<org.jeecg.modules.system.entity.SysAuditTask> firstStepTasks = 
                        sysAuditTaskService.getTasksByOrderId(order.getId());
                    if (firstStepTasks != null && !firstStepTasks.isEmpty()) {
                        // 找到第一个步骤（stepOrder=1）且已通过的任务
                        org.jeecg.modules.system.entity.SysAuditTask firstApprovedTask = firstStepTasks.stream()
                            .filter(t -> t.getStepOrder() != null && t.getStepOrder() == 1)
                            .filter(t -> "approved".equals(t.getTaskStatus()))
                            .filter(t -> t.getAuditTime() != null)
                            .min((t1, t2) -> t1.getAuditTime().compareTo(t2.getAuditTime()))
                            .orElse(null);
                        
                        if (firstApprovedTask != null && firstApprovedTask.getAuditTime() != null) {
                            order.setFirstAuditTime(firstApprovedTask.getAuditTime());
                        }
                    }
                } catch (Exception e) {
                    log.debug("查询订单 {} 的第一个审核步骤时间失败: {}", order.getId(), e.getMessage());
                }
            }
            
            {
                // 查询订单的表单数据，获取formId和formVersionId
                GhOrderFormData formData = ghOrderFormDataService.getFormDataByOrderId(id);
                if (formData != null) {
                    // 将formId和versionId设置到order对象中
                    order.setFormId(formData.getFormId());
                    order.setFormVersionId(formData.getVersionId());
                    order.setDynamicFormVersionId(formData.getVersionId()); // 兼容字段
                    order.setDynamicFormData(formData.getFormData());
                    // 设置表单配置（优先使用保存的配置）
                    order.setDynamicFormConfig(formData.getFormConfig());
                }
                
                // 查询业务员的所属团队名称
                if (oConvertUtils.isNotEmpty(order.getSalesman())) {
                    try {
                        // 根据业务员姓名查询用户
                        org.jeecg.modules.system.entity.SysUser user = sysUserService.getOne(
                            new LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser>()
                                .eq(org.jeecg.modules.system.entity.SysUser::getRealname, order.getSalesman())
                                .eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0)
                        );
                        if (user != null && oConvertUtils.isNotEmpty(user.getTeamId())) {
                            // 根据团队ID查询团队名称
                            org.jeecg.modules.system.entity.SysTeam team = sysTeamService.getById(user.getTeamId());
                            if (team != null) {
                                order.setTeamName(team.getTeamName());
                            }
                        }
                    } catch (Exception e) {
                        log.debug("查询订单 {} 的业务员团队失败: {}", order.getId(), e.getMessage());
                    }
                }
                
                // 查询业务类型的字典文本
                if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
                    try {
                        String businessTypeText = sysDictService.queryTableDictTextByKey(
                            "sys_category", "name", "id", order.getBusinessType()
                        );
                        if (oConvertUtils.isNotEmpty(businessTypeText)) {
                            order.setBusinessType_dictText(businessTypeText);
                        }
                    } catch (Exception e) {
                        log.debug("查询订单 {} 的业务类型字典文本失败: {}", order.getId(), e.getMessage());
                    }
                }
                
                // 查询客户来源的字典文本
                if (oConvertUtils.isNotEmpty(order.getOpportunitySource())) {
                    try {
                        String opportunitySourceText = sysDictService.queryDictTextByKey(
                            "opportunity_source", order.getOpportunitySource()
                        );
                        if (oConvertUtils.isNotEmpty(opportunitySourceText)) {
                            order.setOpportunitySource_dictText(opportunitySourceText);
                        }
                    } catch (Exception e) {
                        log.debug("查询订单 {} 的客户来源字典文本失败: {}", order.getId(), e.getMessage());
                    }
                }
                
                result.setResult(order);
                result.setSuccess(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败！");
        }
        return result;
    }

    /**
     * 获取订单流程步骤
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/getOrderSteps", method = RequestMethod.GET)
    public Result<List<GhOrderStep>> getOrderSteps(@RequestParam(name="orderId",required=true) String orderId) {
        Result<List<GhOrderStep>> result = new Result<>();
        try {
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                result.error500("订单不存在");
                return result;
            }
            
            // 获取已执行的步骤
            List<GhOrderStep> executedSteps = ghOrderService.getOrderWithSteps(orderId).getSteps();
            
            // 如果订单绑定了流程，根据流程配置生成完整的步骤列表
            if (oConvertUtils.isNotEmpty(order.getAuditProcessId())) {
                SysAuditProcess process = sysAuditProcessService.getById(order.getAuditProcessId());
                if (process != null) {
                    // 查询流程配置的所有步骤
                    LambdaQueryWrapper<SysAuditStep> stepWrapper = new LambdaQueryWrapper<>();
                    stepWrapper.eq(SysAuditStep::getProcessId, order.getAuditProcessId());
                    stepWrapper.orderByAsc(SysAuditStep::getStepOrder);
                    List<SysAuditStep> processSteps = sysAuditStepService.list(stepWrapper);
                    
                    // 生成完整的步骤列表（包括已执行和未执行的）
                    List<GhOrderStep> fullSteps = new java.util.ArrayList<>();
                    
                    // 首先添加"订单创建"步骤（固定步骤）
                    GhOrderStep createStep = findExecutedStep(executedSteps, "0");
                    if (createStep == null) {
                        createStep = new GhOrderStep();
                        createStep.setStepType("0");
                        createStep.setStepOrder(0);
                        createStep.setStatus("1"); // 已完成（订单已创建）
                        createStep.setOperatorName(order.getCreateBy());
                        createStep.setCreateTime(order.getCreateTime());
                        createStep.setRemarks("订单创建");
                    }
                    fullSteps.add(createStep);
                    
                    // 根据流程配置生成步骤（去重，确保每个流程步骤只显示一次）
                    java.util.Set<Integer> addedStepOrders = new java.util.HashSet<>();
                    java.util.Map<String, Boolean> usedStepTypes = new java.util.HashMap<>();
                    
                    for (SysAuditStep processStep : processSteps) {
                        // 如果该步骤顺序已经添加过，跳过（避免重复）
                        if (addedStepOrders.contains(processStep.getStepOrder())) {
                            continue;
                        }
                        addedStepOrders.add(processStep.getStepOrder());
                        
                        // 将流程步骤类型映射到订单步骤类型
                        // 1-经理审核，2-出纳审核，3-任务分配，4-任务处理，5-任务完成，6-订单完成
                        String stepType = mapProcessStepToOrderStep(processStep.getStepOrder());
                        
                        // 查找已执行的步骤（优先按stepOrder查找）
                        GhOrderStep orderStep = findExecutedStepByOrder(executedSteps, processStep.getStepOrder());
                        
                        if (orderStep == null) {
                            // 如果按stepOrder找不到，再按stepType查找，但要确保该stepType还没被使用
                            if (!usedStepTypes.containsKey(stepType) || !usedStepTypes.get(stepType)) {
                                orderStep = findExecutedStep(executedSteps, stepType);
                                if (orderStep != null) {
                                    usedStepTypes.put(stepType, true);
                                }
                            }
                        }
                        
                        if (orderStep == null) {
                            // 创建未执行的步骤
                            orderStep = new GhOrderStep();
                            orderStep.setStepType(stepType);
                            orderStep.setStepOrder(processStep.getStepOrder());
                            orderStep.setStatus("0"); // 待处理
                            orderStep.setOperatorName(null);
                            orderStep.setRemarks(processStep.getDescription());
                        } else {
                            // 使用已执行的步骤信息
                            orderStep.setStepOrder(processStep.getStepOrder());
                            if (oConvertUtils.isEmpty(orderStep.getRemarks())) {
                                orderStep.setRemarks(processStep.getDescription());
                            }
                        }
                        fullSteps.add(orderStep);
                    }
                    
                    result.setResult(fullSteps);
                    result.setSuccess(true);
                    return result;
                }
            }
            
            // 如果没有绑定流程，返回已执行的步骤
            result.setResult(executedSteps);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败！");
        }
        return result;
    }
    
    /**
     * 查找已执行的步骤（按stepType）
     */
    private GhOrderStep findExecutedStep(List<GhOrderStep> executedSteps, String stepType) {
        if (executedSteps == null || executedSteps.isEmpty()) {
            return null;
        }
        for (GhOrderStep step : executedSteps) {
            if (stepType.equals(step.getStepType())) {
                return step;
            }
        }
        return null;
    }
    
    /**
     * 查找已执行的步骤（按stepOrder）
     */
    private GhOrderStep findExecutedStepByOrder(List<GhOrderStep> executedSteps, Integer stepOrder) {
        if (executedSteps == null || executedSteps.isEmpty() || stepOrder == null) {
            return null;
        }
        for (GhOrderStep step : executedSteps) {
            if (stepOrder.equals(step.getStepOrder())) {
                return step;
            }
        }
        return null;
    }
    
    /**
     * 将流程步骤顺序映射到订单步骤类型
     * 根据流程步骤的顺序，映射到对应的订单步骤类型
     */
    private String mapProcessStepToOrderStep(Integer stepOrder) {
        // 根据步骤顺序映射：1-经理审核，2-出纳审核，3-任务分配，4-任务处理，5-任务完成，6-订单完成
        switch (stepOrder) {
            case 1: return "1"; // 经理审核
            case 2: return "2"; // 出纳审核
            case 3: return "3"; // 任务分配
            case 4: return "4"; // 任务处理
            case 5: return "5"; // 任务完成
            case 6: return "6"; // 订单完成
            default: return String.valueOf(stepOrder);
        }
    }

    /**
     * 获取订单收费记录
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/getPaymentList", method = RequestMethod.GET)
    public Result<List<GhOrderPayment>> getPaymentList(@RequestParam(name="orderId",required=true) String orderId) {
        Result<List<GhOrderPayment>> result = new Result<>();
        try {
            List<GhOrderPayment> payments = ghOrderPaymentService.getPaymentsByOrderId(orderId);
            
            // 查询订单信息，用于填充合同金额、尾款金额、凭证图片等
            GhOrder order = ghOrderService.getById(orderId);
            if (order != null) {
                // 从动态表单数据中提取开始日期和结束日期
                Date serviceStartTime = null;
                Date serviceEndTime = null;
                String serviceStartTimeStr = null; // 保存原始格式字符串
                String serviceEndTimeStr = null; // 保存原始格式字符串
                
                // 从 gh_order_form_data 表查询动态表单数据（而不是从 order.getDynamicFormData()）
                String dynamicFormDataStr = null;
                GhOrderFormData formData = ghOrderFormDataService.getFormDataByOrderId(orderId);
                if (formData != null && oConvertUtils.isNotEmpty(formData.getFormData())) {
                    dynamicFormDataStr = formData.getFormData();
                }
                
                // 尝试从动态表单数据中提取开始日期和结束日期
                if (oConvertUtils.isNotEmpty(dynamicFormDataStr)) {
                    try {
                        JSONObject dynamicFormData = JSONObject.parseObject(dynamicFormDataStr);
                        if (dynamicFormData != null) {
                            // 遍历动态表单数据，查找开始日期和结束日期字段
                            // 分两遍遍历：第一遍优先匹配包含"月份"的字段，第二遍匹配其他日期/时间字段
                            // 第一遍：优先匹配"开始月份"和"结束月份"
                            for (String key : dynamicFormData.keySet()) {
                                Object value = dynamicFormData.get(key);
                                if (value != null) {
                                    String valueStr = value.toString();
                                    String keyLower = key.toLowerCase().trim();
                                    
                                    // 匹配开始月份字段
                                    if ((keyLower.contains("开始") || keyLower.contains("起始") || 
                                         keyLower.contains("start") || keyLower.contains("begin")) &&
                                        keyLower.contains("月份") && oConvertUtils.isNotEmpty(valueStr)) {
                                        // 保存原始字符串（月份格式）
                                        if (valueStr.matches("^\\d{4}-\\d{2}$")) {
                                            serviceStartTimeStr = valueStr; // 月份格式，保存原始字符串
                                        }
                                        Date parsedDate = parseDateFromString(valueStr);
                                        if (parsedDate != null) {
                                            serviceStartTime = parsedDate;
                                            log.debug("从动态表单提取到服务开始时间（月份）: {} = {}", key, valueStr);
                                        }
                                    }
                                    // 匹配结束月份字段
                                    else if ((keyLower.contains("结束") || keyLower.contains("终止") || 
                                              keyLower.contains("到期") || keyLower.contains("end") ||
                                              keyLower.contains("expire") || keyLower.contains("terminate")) &&
                                             keyLower.contains("月份") && oConvertUtils.isNotEmpty(valueStr)) {
                                        // 保存原始字符串（月份格式）
                                        if (valueStr.matches("^\\d{4}-\\d{2}$")) {
                                            serviceEndTimeStr = valueStr; // 月份格式，保存原始字符串
                                        }
                                        Date parsedDate = parseDateFromString(valueStr);
                                        if (parsedDate != null) {
                                            serviceEndTime = parsedDate;
                                            log.debug("从动态表单提取到服务结束时间（月份）: {} = {}", key, valueStr);
                                        }
                                    }
                                }
                            }
                            
                            // 第二遍：如果没有匹配到月份字段，再匹配日期/时间字段
                            if (serviceStartTime == null || serviceEndTime == null) {
                                for (String key : dynamicFormData.keySet()) {
                                    Object value = dynamicFormData.get(key);
                                    if (value != null) {
                                        String valueStr = value.toString();
                                        String keyLower = key.toLowerCase().trim();
                                        
                                        // 跳过已经匹配过的月份字段
                                        if (keyLower.contains("月份")) {
                                            continue;
                                        }
                                        
                                        // 匹配开始日期/时间字段
                                        if (serviceStartTime == null &&
                                            (keyLower.contains("开始") || keyLower.contains("起始") || 
                                             keyLower.contains("start") || keyLower.contains("begin")) &&
                                            oConvertUtils.isNotEmpty(valueStr)) {
                                            Date parsedDate = parseDateFromString(valueStr);
                                            if (parsedDate != null) {
                                                serviceStartTime = parsedDate;
                                                serviceStartTimeStr = null; // 非月份格式，不保存原始字符串
                                                log.debug("从动态表单提取到服务开始时间（日期/时间）: {} = {}", key, valueStr);
                                            }
                                        }
                                        // 匹配结束日期/时间字段
                                        else if (serviceEndTime == null &&
                                                 (keyLower.contains("结束") || keyLower.contains("终止") || 
                                                  keyLower.contains("到期") || keyLower.contains("end") ||
                                                  keyLower.contains("expire") || keyLower.contains("terminate")) &&
                                                 oConvertUtils.isNotEmpty(valueStr)) {
                                            Date parsedDate = parseDateFromString(valueStr);
                                            if (parsedDate != null) {
                                                serviceEndTime = parsedDate;
                                                serviceEndTimeStr = null; // 非月份格式，不保存原始字符串
                                                log.debug("从动态表单提取到服务结束时间（日期/时间）: {} = {}", key, valueStr);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("从动态表单数据提取日期失败", e);
                    }
                }
                
                // 如果从动态表单中没有提取到开始时间，使用订单创建时间
                if (serviceStartTime == null) {
                    serviceStartTime = order.getCreateTime();
                }
                
                // 计算合计月份（从服务开始时间到现在或到期时间）
                Integer totalMonths = 0;
                Date endDateForMonths = serviceEndTime != null ? serviceEndTime : new Date();
                if (serviceStartTime != null) {
                    try {
                        java.time.LocalDate startDate = serviceStartTime.toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.time.LocalDate endDate = endDateForMonths.toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        totalMonths = (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
                    } catch (Exception e) {
                        log.debug("计算合计月份失败", e);
                    }
                }
                
                // 计算已确认的收款总金额
                BigDecimal totalReceivedAmount = BigDecimal.ZERO;
                for (GhOrderPayment p : payments) {
                    if (p.getStatus() != null && "1".equals(p.getStatus()) && p.getAmount() != null) {
                        totalReceivedAmount = totalReceivedAmount.add(p.getAmount());
                    }
                }
                
                // 获取合同金额
                BigDecimal contractAmount = order.getContractAmount() != null ? 
                    order.getContractAmount() : order.getOrderAmount();
                
                // 为每条收费记录添加订单相关信息
                for (GhOrderPayment payment : payments) {
                    // 设置合同金额
                    payment.setContractAmount(contractAmount);
                    
                    // 设置尾款金额（合同金额 - 已确认的收款总金额）
                    if (contractAmount != null) {
                        payment.setFinalPaymentAmount(contractAmount.subtract(totalReceivedAmount));
                    }
                    
                    // 设置服务开始时间（从动态表单提取或订单创建时间）
                    payment.setServiceStartTime(serviceStartTime);
                    // 设置服务开始时间原始字符串（如果是月份格式）
                    payment.setServiceStartTimeStr(serviceStartTimeStr);
                    log.debug("设置收费记录服务开始时间: serviceStartTime={}, serviceStartTimeStr={}", 
                        serviceStartTime, serviceStartTimeStr);
                    
                    // 设置服务到期时间（从动态表单提取）
                    payment.setServiceEndTime(serviceEndTime);
                    // 设置服务到期时间原始字符串（如果是月份格式）
                    payment.setServiceEndTimeStr(serviceEndTimeStr);
                    log.debug("设置收费记录服务到期时间: serviceEndTime={}, serviceEndTimeStr={}", 
                        serviceEndTime, serviceEndTimeStr);
                    
                    // 设置合计月份
                    payment.setTotalMonths(totalMonths);
                    
                    // 设置凭证图片
                    payment.setImageVoucher(order.getImageVoucher());
                    
                    // 填充收款账户三联级信息（收款方式/收款人/网点名称）和卡号
                    // accountNumber保存的是银行账户ID，需要查询并展示三级级联内容
                    if (payment.getAccountNumber() != null && !payment.getAccountNumber().trim().isEmpty()) {
                        try {
                            // accountNumber应该是银行账户ID
                            GhBankManagement bankAccount = ghBankManagementService.getById(payment.getAccountNumber());
                            if (bankAccount != null) {
                                // 设置三级级联信息（用于前端展示）
                                payment.setPaymentMethod(bankAccount.getPaymentMethod());
                                payment.setPayeePerson(bankAccount.getPayeePerson());
                                payment.setAccountNotes(bankAccount.getAccountNotes());
                                // 设置收款账户（卡号）
                                payment.setCollectionAccount(bankAccount.getCollectionAccount());
                                // accountNumber保持为ID（不修改，用于数据一致性）
                            }
                        } catch (Exception e) {
                            // 如果查询失败，可能是旧的字符串格式（兼容旧数据）
                            log.debug("收款账号查询失败，accountNumber: {}", payment.getAccountNumber(), e);
                        }
                    }
                }
            }
            
            result.setResult(payments);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败！");
        }
        return result;
    }

    /**
     * 获取订单支出记录
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/getExpenseList", method = RequestMethod.GET)
    public Result<List<GhOrderExpense>> getExpenseList(@RequestParam(name="orderId",required=true) String orderId) {
        Result<List<GhOrderExpense>> result = new Result<>();
        try {
            List<GhOrderExpense> expenses = ghOrderExpenseService.getExpensesByOrderId(orderId);
            result.setResult(expenses);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败！");
        }
        return result;
    }
    
    /**
     * 获取订单操作记录
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/getOperationLogs", method = RequestMethod.GET)
    public Result<List<GhOrderOperationLog>> getOperationLogs(@RequestParam(name="orderId",required=true) String orderId) {
        Result<List<GhOrderOperationLog>> result = new Result<>();
        try {
            List<GhOrderOperationLog> logs = ghOrderOperationLogService.getLogsByOrderId(orderId);
            result.setResult(logs);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败！");
        }
        return result;
    }
    
    /**
     * 比较两个订单对象，返回变更的字段
     */
    private String getChangedFields(GhOrder oldOrder, GhOrder newOrder) {
        java.util.List<String> changedFields = new java.util.ArrayList<>();
        
        // 公司相关
        if (!java.util.Objects.equals(oldOrder.getCompanyName(), newOrder.getCompanyName())) {
            changedFields.add("公司名称");
        }
        if (!java.util.Objects.equals(oldOrder.getCompanyId(), newOrder.getCompanyId())) {
            changedFields.add("公司ID");
        }
        
        // 业务相关
        if (!java.util.Objects.equals(oldOrder.getSalesman(), newOrder.getSalesman())) {
            changedFields.add("业务员");
        }
        if (!java.util.Objects.equals(oldOrder.getBusinessType(), newOrder.getBusinessType())) {
            changedFields.add("业务类型");
        }
        if (!java.util.Objects.equals(oldOrder.getOpportunitySource(), newOrder.getOpportunitySource())) {
            changedFields.add("商机来源");
        }
        if (!java.util.Objects.equals(oldOrder.getAuditProcessId(), newOrder.getAuditProcessId())) {
            changedFields.add("流程审批");
        }
        
        // 金额相关（使用数值比较，忽略格式差异）
        if (!isAmountEqual(oldOrder.getContractAmount(), newOrder.getContractAmount())) {
            changedFields.add("合同金额");
        }
        if (!isAmountEqual(oldOrder.getOrderAmount(), newOrder.getOrderAmount())) {
            changedFields.add("订单金额");
        }
        if (!isAmountEqual(oldOrder.getReceivedAmount(), newOrder.getReceivedAmount())) {
            changedFields.add("收款金额");
        }
        if (!isAmountEqual(oldOrder.getFinalPaymentAmount(), newOrder.getFinalPaymentAmount())) {
            changedFields.add("尾款金额");
        }
        
        // 收款相关
        if (!java.util.Objects.equals(oldOrder.getCollectionTime(), newOrder.getCollectionTime())) {
            changedFields.add("收款时间");
        }
        if (!java.util.Objects.equals(oldOrder.getCollectionAccountNumber(), newOrder.getCollectionAccountNumber())) {
            changedFields.add("收款账户");
        }
        
        // 联系信息
        if (!java.util.Objects.equals(oldOrder.getRegion(), newOrder.getRegion())) {
            changedFields.add("所属区域");
        }
        if (!java.util.Objects.equals(oldOrder.getContacts(), newOrder.getContacts())) {
            changedFields.add("联系人员");
        }
        if (!java.util.Objects.equals(oldOrder.getAddress(), newOrder.getAddress())) {
            changedFields.add("详细地址");
        }
        if (!java.util.Objects.equals(oldOrder.getContactInformation(), newOrder.getContactInformation())) {
            changedFields.add("联系方式");
        }
        
        // 其他字段
        if (!java.util.Objects.equals(oldOrder.getIsRecurring(), newOrder.getIsRecurring())) {
            changedFields.add("周期业务");
        }
        if (!java.util.Objects.equals(oldOrder.getImageVoucher(), newOrder.getImageVoucher())) {
            changedFields.add("图片凭证");
        }
        if (!java.util.Objects.equals(oldOrder.getOrderStatus(), newOrder.getOrderStatus())) {
            changedFields.add("订单状态");
        }
        if (!java.util.Objects.equals(oldOrder.getRemarks(), newOrder.getRemarks())) {
            changedFields.add("备注");
        }
        
        return String.join("、", changedFields);
    }
    
    /**
     * 比较两个金额是否相等（忽略格式差异，如200.00和200视为相等）
     */
    private boolean isAmountEqual(java.math.BigDecimal oldAmount, java.math.BigDecimal newAmount) {
        if (oldAmount == null && newAmount == null) {
            return true;
        }
        if (oldAmount == null || newAmount == null) {
            return false;
        }
        return oldAmount.compareTo(newAmount) == 0;
    }
    
    /**
     * 比较动态表单数据是否真正变化
     */
    private boolean isFormDataChanged(String oldFormData, String newFormData) {
        if (oldFormData == null && newFormData == null) {
            return false;
        }
        if (oldFormData == null && newFormData != null && !newFormData.trim().isEmpty()) {
            return true; // 从无到有
        }
        if (oldFormData != null && newFormData == null) {
            return true; // 从有到无
        }
        if (oldFormData == null || newFormData == null) {
            return false;
        }
        
        // 比较JSON内容是否真正变化
        try {
            com.alibaba.fastjson.JSONObject oldJson = com.alibaba.fastjson.JSON.parseObject(oldFormData);
            com.alibaba.fastjson.JSONObject newJson = com.alibaba.fastjson.JSON.parseObject(newFormData);
            return !oldJson.equals(newJson);
        } catch (Exception e) {
            // 如果不是JSON格式，直接比较字符串
            return !oldFormData.equals(newFormData);
        }
    }
    
    /**
     * 构建详细的变更描述（从什么改成什么）
     */
    private String buildChangeDescription(GhOrder oldOrder, GhOrder newOrder, String oldFormData, String newFormData) {
        java.util.List<String> changes = new java.util.ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        
        // 公司相关
        if (!java.util.Objects.equals(oldOrder.getCompanyName(), newOrder.getCompanyName())) {
            changes.add("公司名称：" + (oldOrder.getCompanyName() != null ? oldOrder.getCompanyName() : "空") + 
                       " → " + (newOrder.getCompanyName() != null ? newOrder.getCompanyName() : "空"));
        }
        
        // 业务相关
        if (!java.util.Objects.equals(oldOrder.getSalesman(), newOrder.getSalesman())) {
            changes.add("业务员：" + (oldOrder.getSalesman() != null ? oldOrder.getSalesman() : "空") + 
                       " → " + (newOrder.getSalesman() != null ? newOrder.getSalesman() : "空"));
        }
        if (!java.util.Objects.equals(oldOrder.getBusinessType(), newOrder.getBusinessType())) {
            String oldValue = getDictText("sys_category", oldOrder.getBusinessType());
            String newValue = getDictText("sys_category", newOrder.getBusinessType());
            changes.add("业务类型：" + (oldValue != null ? oldValue : "空") + 
                       " → " + (newValue != null ? newValue : "空"));
        }
        if (!java.util.Objects.equals(oldOrder.getOpportunitySource(), newOrder.getOpportunitySource())) {
            String oldValue = getDictText("opportunity_source", oldOrder.getOpportunitySource());
            String newValue = getDictText("opportunity_source", newOrder.getOpportunitySource());
            changes.add("商机来源：" + (oldValue != null ? oldValue : "空") + 
                       " → " + (newValue != null ? newValue : "空"));
        }
        if (!java.util.Objects.equals(oldOrder.getAuditProcessId(), newOrder.getAuditProcessId())) {
            changes.add("流程审批：" + (oldOrder.getAuditProcessId() != null ? oldOrder.getAuditProcessId() : "空") + 
                       " → " + (newOrder.getAuditProcessId() != null ? newOrder.getAuditProcessId() : "空"));
        }
        
        // 金额相关（使用数值比较）
        if (!isAmountEqual(oldOrder.getContractAmount(), newOrder.getContractAmount())) {
            String oldAmount = oldOrder.getContractAmount() != null ? oldOrder.getContractAmount().toString() : "0";
            String newAmount = newOrder.getContractAmount() != null ? newOrder.getContractAmount().toString() : "0";
            changes.add("合同金额：" + oldAmount + " → " + newAmount);
        }
        if (!isAmountEqual(oldOrder.getOrderAmount(), newOrder.getOrderAmount())) {
            String oldAmount = oldOrder.getOrderAmount() != null ? oldOrder.getOrderAmount().toString() : "0";
            String newAmount = newOrder.getOrderAmount() != null ? newOrder.getOrderAmount().toString() : "0";
            changes.add("订单金额：" + oldAmount + " → " + newAmount);
        }
        if (!isAmountEqual(oldOrder.getReceivedAmount(), newOrder.getReceivedAmount())) {
            String oldAmount = oldOrder.getReceivedAmount() != null ? oldOrder.getReceivedAmount().toString() : "0";
            String newAmount = newOrder.getReceivedAmount() != null ? newOrder.getReceivedAmount().toString() : "0";
            changes.add("收款金额：" + oldAmount + " → " + newAmount);
        }
        if (!isAmountEqual(oldOrder.getFinalPaymentAmount(), newOrder.getFinalPaymentAmount())) {
            String oldAmount = oldOrder.getFinalPaymentAmount() != null ? oldOrder.getFinalPaymentAmount().toString() : "0";
            String newAmount = newOrder.getFinalPaymentAmount() != null ? newOrder.getFinalPaymentAmount().toString() : "0";
            changes.add("尾款金额：" + oldAmount + " → " + newAmount);
        }
        
        // 收款相关
        if (!java.util.Objects.equals(oldOrder.getCollectionTime(), newOrder.getCollectionTime())) {
            String oldTime = oldOrder.getCollectionTime() != null ? sdf.format(oldOrder.getCollectionTime()) : "空";
            String newTime = newOrder.getCollectionTime() != null ? sdf.format(newOrder.getCollectionTime()) : "空";
            changes.add("收款时间：" + oldTime + " → " + newTime);
        }
        if (!java.util.Objects.equals(oldOrder.getCollectionAccountNumber(), newOrder.getCollectionAccountNumber())) {
            // 收款账户是ID，需要转换为账户名称显示
            String oldAccountName = oldOrder.getCollectionAccountNumber() != null ? 
                getBankAccountName(oldOrder.getCollectionAccountNumber()) : "空";
            String newAccountName = newOrder.getCollectionAccountNumber() != null ? 
                getBankAccountName(newOrder.getCollectionAccountNumber()) : "空";
            changes.add("收款账户：" + oldAccountName + " → " + newAccountName);
        }
        
        // 联系信息
        if (!java.util.Objects.equals(oldOrder.getRegion(), newOrder.getRegion())) {
            changes.add("所属区域：" + (oldOrder.getRegion() != null ? oldOrder.getRegion() : "空") + 
                       " → " + (newOrder.getRegion() != null ? newOrder.getRegion() : "空"));
        }
        if (!java.util.Objects.equals(oldOrder.getContacts(), newOrder.getContacts())) {
            changes.add("联系人员：" + (oldOrder.getContacts() != null ? oldOrder.getContacts() : "空") + 
                       " → " + (newOrder.getContacts() != null ? newOrder.getContacts() : "空"));
        }
        if (!java.util.Objects.equals(oldOrder.getAddress(), newOrder.getAddress())) {
            String oldAddr = oldOrder.getAddress() != null ? oldOrder.getAddress() : "空";
            String newAddr = newOrder.getAddress() != null ? newOrder.getAddress() : "空";
            if (oldAddr.length() > 50) oldAddr = oldAddr.substring(0, 50) + "...";
            if (newAddr.length() > 50) newAddr = newAddr.substring(0, 50) + "...";
            changes.add("详细地址：" + oldAddr + " → " + newAddr);
        }
        if (!java.util.Objects.equals(oldOrder.getContactInformation(), newOrder.getContactInformation())) {
            changes.add("联系方式：" + (oldOrder.getContactInformation() != null ? oldOrder.getContactInformation() : "空") + 
                       " → " + (newOrder.getContactInformation() != null ? newOrder.getContactInformation() : "空"));
        }
        
        // 其他字段
        if (!java.util.Objects.equals(oldOrder.getIsRecurring(), newOrder.getIsRecurring())) {
            String oldValue = "1".equals(oldOrder.getIsRecurring()) ? "是" : ("0".equals(oldOrder.getIsRecurring()) ? "否" : (oldOrder.getIsRecurring() != null ? oldOrder.getIsRecurring() : "空"));
            String newValue = "1".equals(newOrder.getIsRecurring()) ? "是" : ("0".equals(newOrder.getIsRecurring()) ? "否" : (newOrder.getIsRecurring() != null ? newOrder.getIsRecurring() : "空"));
            changes.add("周期业务：" + oldValue + " → " + newValue);
        }
        if (!java.util.Objects.equals(oldOrder.getImageVoucher(), newOrder.getImageVoucher())) {
            // 图片凭证通常是JSON数组，只显示是否变化
            String oldValue = oldOrder.getImageVoucher() != null ? "已上传" : "空";
            String newValue = newOrder.getImageVoucher() != null ? "已上传" : "空";
            changes.add("图片凭证：" + oldValue + " → " + newValue);
        }
        if (!java.util.Objects.equals(oldOrder.getOrderStatus(), newOrder.getOrderStatus())) {
            String oldValue = getDictText("order_status", oldOrder.getOrderStatus());
            String newValue = getDictText("order_status", newOrder.getOrderStatus());
            changes.add("订单状态：" + (oldValue != null ? oldValue : "空") + 
                       " → " + (newValue != null ? newValue : "空"));
        }
        if (!java.util.Objects.equals(oldOrder.getRemarks(), newOrder.getRemarks())) {
            String oldRemarks = oldOrder.getRemarks() != null ? oldOrder.getRemarks() : "空";
            String newRemarks = newOrder.getRemarks() != null ? newOrder.getRemarks() : "空";
            if (oldRemarks.length() > 50) oldRemarks = oldRemarks.substring(0, 50) + "...";
            if (newRemarks.length() > 50) newRemarks = newRemarks.substring(0, 50) + "...";
            changes.add("备注：" + oldRemarks + " → " + newRemarks);
        }
        
        // 动态表单数据变更（只有在真正有变化时才显示，并显示具体变化的字段）
        // 注意：这里需要检查 oldFormData 和 newFormData 是否都不为 null，或者是否真的有变化
        // 如果调用方已经判断了 formDataChanged，这里应该总是进入比较逻辑
        if (oldFormData != null || newFormData != null) {
            java.util.List<String> formDataChanges = new java.util.ArrayList<>();
            
            // 标准化处理：将 null 转换为空字符串，便于统一处理
            String oldDataStr = (oldFormData == null || oldFormData.trim().isEmpty()) ? null : oldFormData.trim();
            String newDataStr = (newFormData == null || newFormData.trim().isEmpty()) ? null : newFormData.trim();
            
            if (oldDataStr == null && newDataStr != null) {
                // 从无到有，显示所有字段
                try {
                    com.alibaba.fastjson.JSONObject newJson = com.alibaba.fastjson.JSON.parseObject(newDataStr);
                    for (String key : newJson.keySet()) {
                        Object value = newJson.get(key);
                        String valueStr = value != null ? value.toString() : "空";
                        if (valueStr.length() > 50) valueStr = valueStr.substring(0, 50) + "...";
                        formDataChanges.add("动态表单." + key + "：无 → " + valueStr);
                    }
                } catch (Exception e) {
                    formDataChanges.add("动态表单：无 → 已填写");
                }
            } else if (oldDataStr != null && newDataStr == null) {
                // 从有到无
                formDataChanges.add("动态表单：已填写 → 已清空");
            } else if (oldDataStr != null && newDataStr != null) {
                // 都有内容，比较JSON
                try {
                    com.alibaba.fastjson.JSONObject oldJson = com.alibaba.fastjson.JSON.parseObject(oldDataStr);
                    com.alibaba.fastjson.JSONObject newJson = com.alibaba.fastjson.JSON.parseObject(newDataStr);
                    
                    // 找出所有变化的字段
                    java.util.Set<String> allKeys = new java.util.HashSet<>();
                    allKeys.addAll(oldJson.keySet());
                    allKeys.addAll(newJson.keySet());
                    
                    for (String key : allKeys) {
                        Object oldValue = oldJson.get(key);
                        Object newValue = newJson.get(key);
                        
                        // 比较值是否变化（支持各种数据类型）
                        boolean valueChanged = false;
                        if (oldValue == null && newValue != null) {
                            valueChanged = true;
                        } else if (oldValue != null && newValue == null) {
                            valueChanged = true;
                        } else if (oldValue != null && newValue != null) {
                            // 处理各种数据类型
                            String oldStr = formatFormFieldValue(oldValue);
                            String newStr = formatFormFieldValue(newValue);
                            if (!oldStr.equals(newStr)) {
                                valueChanged = true;
                            }
                        }
                        
                        if (valueChanged) {
                            String oldStr = oldValue != null ? formatFormFieldValue(oldValue) : "空";
                            String newStr = newValue != null ? formatFormFieldValue(newValue) : "空";
                            if (oldStr.length() > 50) oldStr = oldStr.substring(0, 50) + "...";
                            if (newStr.length() > 50) newStr = newStr.substring(0, 50) + "...";
                            formDataChanges.add("动态表单." + key + "：" + oldStr + " → " + newStr);
                        }
                    }
                } catch (Exception e) {
                    // 如果不是JSON格式，直接比较字符串
                    if (!oldDataStr.equals(newDataStr)) {
                        formDataChanges.add("动态表单：内容已更新");
                    }
                }
            }
            
            // 如果有变化，添加到changes列表
            if (!formDataChanges.isEmpty()) {
                changes.addAll(formDataChanges);
            }
        }
        
        return String.join("；", changes);
    }
    
    /**
     * 格式化动态表单字段值（支持各种数据类型：日期、数组、对象、数字、文本等）
     */
    private String formatFormFieldValue(Object value) {
        if (value == null) {
            return "空";
        }
        
        // 如果是数组（如联级选择框、多选等）
        if (value instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) value;
            if (list.isEmpty()) {
                return "空";
            }
            // 将数组元素转换为字符串，用逗号分隔
            java.util.List<String> items = new java.util.ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    items.add(item.toString());
                }
            }
            return String.join("、", items);
        }
        
        // 如果是数组（JSONArray）
        if (value instanceof com.alibaba.fastjson.JSONArray) {
            com.alibaba.fastjson.JSONArray array = (com.alibaba.fastjson.JSONArray) value;
            if (array.isEmpty()) {
                return "空";
            }
            java.util.List<String> items = new java.util.ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                Object item = array.get(i);
                if (item != null) {
                    items.add(formatFormFieldValue(item)); // 递归处理数组元素，支持嵌套结构
                }
            }
            return String.join("、", items);
        }
        
        // 如果是对象（如对象选择、复杂结构等）
        if (value instanceof com.alibaba.fastjson.JSONObject) {
            com.alibaba.fastjson.JSONObject obj = (com.alibaba.fastjson.JSONObject) value;
            // 尝试提取常见的显示字段
            if (obj.containsKey("label")) {
                return obj.getString("label");
            } else if (obj.containsKey("name")) {
                return obj.getString("name");
            } else if (obj.containsKey("text")) {
                return obj.getString("text");
            } else if (obj.containsKey("value")) {
                return formatFormFieldValue(obj.get("value"));
            } else {
                // 如果是对象但没有明确的显示字段，显示为JSON字符串（截断）
                String jsonStr = obj.toJSONString();
                return jsonStr.length() > 30 ? jsonStr.substring(0, 30) + "..." : jsonStr;
            }
        }
        
        // 如果是数字
        if (value instanceof java.lang.Number) {
            return value.toString();
        }
        
        // 如果是布尔值
        if (value instanceof java.lang.Boolean) {
            return ((Boolean) value) ? "是" : "否";
        }
        
        // 如果是日期字符串或时间戳
        String strValue = value.toString();
        // 尝试解析为日期格式
        try {
            // 如果是时间戳（纯数字且长度>=10）
            if (strValue.matches("^\\d{10,13}$")) {
                long timestamp = Long.parseLong(strValue);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.format(new java.util.Date(timestamp));
            }
            // 如果是日期格式字符串，直接返回
            if (strValue.matches("^\\d{4}-\\d{2}-\\d{2}.*")) {
                return strValue;
            }
        } catch (Exception e) {
            // 解析失败，继续使用原字符串
        }
        
        // 默认返回字符串
        return strValue;
    }
    
    /**
     * 订单审核（支持与审核流程管理配合）
     * @param jsonObject 包含订单ID、角色ID、审核状态、审核备注
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['orderId'] + '_' + #jsonObject['roleId']")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> audit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("orderId");
            String roleId = jsonObject.getString("roleId");
            String auditStatus = jsonObject.getString("auditStatus"); // approved-通过, rejected-驳回
            String auditRemark = jsonObject.getString("auditRemark");
            
            if (oConvertUtils.isEmpty(orderId) || oConvertUtils.isEmpty(roleId) || oConvertUtils.isEmpty(auditStatus)) {
                result.error500("参数不完整");
                return result;
            }
            
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                result.error500("订单不存在");
                return result;
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            // 执行审核
            boolean success = ghOrderAuditService.executeAudit(orderId, roleId, auditStatus, auditRemark);
            if (!success) {
                result.error500("审核失败");
                return result;
            }
            
            // 获取审核记录，检查是否所有步骤都已完成
            List<org.jeecg.modules.order.entity.GhOrderAudit> audits = ghOrderAuditService.getAuditsByOrderId(orderId);
            
            // 按步骤顺序分组，检查每个步骤的所有角色是否都已审核通过
            // 相同stepOrder的多个角色表示并行审核，需要都通过才能进入下一步
            Map<Integer, List<org.jeecg.modules.order.entity.GhOrderAudit>> stepAuditsMap = audits.stream()
                .collect(Collectors.groupingBy(org.jeecg.modules.order.entity.GhOrderAudit::getStepOrder));
            
            boolean allApproved = true;
            boolean hasRejected = false;
            
            // 检查每个步骤的所有角色是否都已审核通过
            boolean firstStepCompleted = false; // 第一步是否完成
            for (Map.Entry<Integer, List<org.jeecg.modules.order.entity.GhOrderAudit>> entry : stepAuditsMap.entrySet()) {
                List<org.jeecg.modules.order.entity.GhOrderAudit> stepAudits = entry.getValue();
                Integer stepOrder = entry.getKey();
                
                // 检查当前步骤是否有驳回
                boolean stepHasRejected = stepAudits.stream()
                    .anyMatch(a -> "rejected".equals(a.getAuditStatus()));
                if (stepHasRejected) {
                    hasRejected = true;
                    allApproved = false;
                    break;
                }
                // 检查当前步骤的所有角色是否都已审核通过
                boolean stepAllApproved = stepAudits.stream()
                    .allMatch(a -> "approved".equals(a.getAuditStatus()));
                if (!stepAllApproved) {
                    allApproved = false;
                }
                
                // 检查第一步（stepOrder=1）是否完成
                if (stepOrder == 1 && stepAllApproved) {
                    firstStepCompleted = true;
                }
            }
            
            // 如果是出纳审核通过，立即更新收费记录状态为已确认（不需要等所有步骤完成）
            if ("approved".equals(auditStatus)) {
                // 从审核记录中查找当前审核的角色（审核通过后状态已变为approved）
                org.jeecg.modules.order.entity.GhOrderAudit currentAudit = audits.stream()
                    .filter(a -> roleId.equals(a.getRoleId()) && "approved".equals(a.getAuditStatus()))
                    .findFirst()
                    .orElse(null);
                if (currentAudit != null && currentAudit.getRoleName() != null && 
                    currentAudit.getRoleName().contains("出纳")) {
                    // 更新订单的所有收费记录状态为已确认
                    LambdaQueryWrapper<GhOrderPayment> paymentWrapper = new LambdaQueryWrapper<>();
                    paymentWrapper.eq(GhOrderPayment::getOrderId, orderId);
                    paymentWrapper.eq(GhOrderPayment::getStatus, "0"); // 只更新待确认的记录
                    List<GhOrderPayment> payments = ghOrderPaymentService.list(paymentWrapper);
                    for (GhOrderPayment payment : payments) {
                        payment.setStatus("1"); // 已确认
                        payment.setUpdateBy(sysUser.getUsername());
                        payment.setUpdateTime(new Date());
                        ghOrderPaymentService.updateById(payment);
                    }
                }
            }
            
            // 如果第一步（管理员和出纳并行审核）都完成了，根据交单方式创建相应任务
            if (firstStepCompleted && oConvertUtils.isNotEmpty(order.getDeliveryMethod())) {
                // 交单方式是工商部，创建工商任务
                if (org.jeecg.modules.order.enums.DeliveryMethodEnum.isBusinessDept(order.getDeliveryMethod())) {
                try {
                    org.jeecg.modules.order.entity.GhBusinessTask task = 
                        ghBusinessTaskService.getTaskByOrderId(orderId);
                    if (task == null) {
                        // 创建工商任务
                        task = new org.jeecg.modules.order.entity.GhBusinessTask();
                        task.setOrderId(orderId);
                        task.setOrderNo(order.getOrderNo());
                        task.setCompanyName(order.getCompanyName());
                        task.setTaskStatus("pending_manager_audit");
                        task.setManagerAuditStatus("pending");
                        task.setDelFlag(0);
                        task.setCreateTime(new Date());
                        task.setCreateBy(sysUser.getUsername());
                        ghBusinessTaskService.save(task);
                        log.info("订单 {} 第一步审核完成后，创建了工商任务", orderId);
                    } else {
                        // 如果任务已存在，确保状态为待经理审核
                        if (!"pending_manager_audit".equals(task.getTaskStatus())) {
                            task.setTaskStatus("pending_manager_audit");
                            task.setManagerAuditStatus("pending");
                            task.setUpdateTime(new Date());
                            task.setUpdateBy(sysUser.getUsername());
                            ghBusinessTaskService.updateById(task);
                            log.info("订单 {} 第一步审核完成后，更新了工商任务状态为待经理审核", orderId);
                        }
                    }
                } catch (Exception e) {
                    log.error("创建工商任务失败，订单ID: {}", orderId, e);
                    // 抛出异常，触发事务回滚
                    throw new RuntimeException("创建工商任务失败: " + e.getMessage(), e);
                    }
                }
                // 交单方式是会计部，创建代账交接任务
                if (org.jeecg.modules.order.enums.DeliveryMethodEnum.isAccountingDept(order.getDeliveryMethod())) {
                    try {
                        // 检查是否已存在代账交接任务
                        org.jeecg.modules.order.entity.GhAccountingHandover handover = 
                            ghAccountingHandoverService.getHandoverByOrderId(orderId);
                        if (handover == null) {
                            // 创建代账交接任务
                            handover = new org.jeecg.modules.order.entity.GhAccountingHandover();
                            handover.setOrderId(orderId);
                            handover.setOrderNo(order.getOrderNo());
                            handover.setCompanyName(order.getCompanyName());
                            handover.setHandoverStatus(org.jeecg.modules.order.enums.HandoverStatusEnum.PENDING.getCode()); // 待审核
                            handover.setAuditProcessId(order.getAuditProcessId()); // 使用订单的审核流程
                            handover.setCurrentAuditStep(1);
                            handover.setDelFlag(0);
                            handover.setCreateTime(new Date());
                            handover.setCreateBy(sysUser.getUsername());
                            ghAccountingHandoverService.save(handover);
                            log.info("订单 {} 第一步审核完成后，创建了代账交接任务", orderId);
                            
                            // 如果订单有审核流程，初始化代账交接的审核流程（跳过第一步，从第二步开始）
                            if (oConvertUtils.isNotEmpty(order.getAuditProcessId())) {
                                // 初始化审核流程（会创建所有步骤的审核记录）
                                ghAccountingHandoverService.initHandoverAudit(handover.getId(), order.getAuditProcessId());
                                log.info("订单 {} 的代账交接审核流程已初始化", orderId);
                            }
                        } else {
                            // 如果任务已存在，确保状态为待审核
                            if (!"pending".equals(handover.getHandoverStatus())) {
                                handover.setHandoverStatus("pending");
                                handover.setCurrentAuditStep(1);
                                handover.setUpdateTime(new Date());
                                handover.setUpdateBy(sysUser.getUsername());
                                ghAccountingHandoverService.updateById(handover);
                                log.info("订单 {} 第一步审核完成后，更新了代账交接任务状态为待审核", orderId);
                            }
                        }
                    } catch (Exception e) {
                        log.error("创建代账交接任务失败，订单ID: {}", orderId, e);
                        // 抛出异常，触发事务回滚
                        throw new RuntimeException("创建代账交接任务失败: " + e.getMessage(), e);
                    }
                }
                // 一次性业务（交单方式不是工商部也不是会计部），也创建工商任务
                if (!org.jeecg.modules.order.enums.DeliveryMethodEnum.isBusinessDept(order.getDeliveryMethod()) &&
                    !org.jeecg.modules.order.enums.DeliveryMethodEnum.isAccountingDept(order.getDeliveryMethod())) {
                    try {
                        org.jeecg.modules.order.entity.GhBusinessTask task = 
                            ghBusinessTaskService.getTaskByOrderId(orderId);
                        if (task == null) {
                            // 创建工商任务（一次性业务也走工商任务流程）
                            task = new org.jeecg.modules.order.entity.GhBusinessTask();
                            task.setOrderId(orderId);
                            task.setOrderNo(order.getOrderNo());
                            task.setCompanyName(order.getCompanyName());
                            task.setTaskStatus("pending_manager_audit");
                            task.setManagerAuditStatus("pending");
                            task.setDelFlag(0);
                            task.setCreateTime(new Date());
                            task.setCreateBy(sysUser.getUsername());
                            ghBusinessTaskService.save(task);
                            log.info("订单 {} 第一步审核完成后，创建了工商任务（一次性业务）", orderId);
                        }
                    } catch (Exception e) {
                        log.error("创建工商任务失败（一次性业务），订单ID: {}", orderId, e);
                        // 抛出异常，触发事务回滚
                        throw new RuntimeException("创建工商任务失败: " + e.getMessage(), e);
                    }
                }
            }
            
            // 更新订单状态
            if (hasRejected) {
                // 有驳回，订单审核失败
                order.setAuditStatus("rejected");
                order.setOrderStatus("4"); // 驳回状态
            } else if (allApproved) {
                // 所有步骤都通过，订单审核通过，自动变为已完成
                order.setAuditStatus("approved");
                order.setOrderStatus("2"); // 已完成（所有审批流程走完就自动已完成）
                
                // 如果交单方式是工商部，创建或更新工商任务
                if (oConvertUtils.isNotEmpty(order.getDeliveryMethod()) && "gsb".equals(order.getDeliveryMethod())) {
                    try {
                        org.jeecg.modules.order.entity.GhBusinessTask task = 
                            ghBusinessTaskService.getTaskByOrderId(orderId);
                        if (task == null) {
                            // 创建工商任务
                            task = new org.jeecg.modules.order.entity.GhBusinessTask();
                            task.setOrderId(orderId);
                            task.setOrderNo(order.getOrderNo());
                            task.setCompanyName(order.getCompanyName());
                            task.setTaskStatus("pending_manager_audit");
                            task.setManagerAuditStatus("pending");
                            task.setDelFlag(0);
                            task.setCreateTime(new Date());
                            task.setCreateBy(sysUser.getUsername());
                            ghBusinessTaskService.save(task);
                            log.info("订单 {} 审核通过后，创建了工商任务", orderId);
                        } else {
                            // 如果任务已存在，确保状态为待经理审核
                            if (!"pending_manager_audit".equals(task.getTaskStatus())) {
                                task.setTaskStatus("pending_manager_audit");
                                task.setManagerAuditStatus("pending");
                                task.setUpdateTime(new Date());
                                task.setUpdateBy(sysUser.getUsername());
                                ghBusinessTaskService.updateById(task);
                                log.info("订单 {} 审核通过后，更新了工商任务状态为待经理审核", orderId);
                            }
                        }
                    } catch (Exception e) {
                        log.error("创建工商任务失败，订单ID: {}", orderId, e);
                        // 抛出异常，触发事务回滚
                        throw new RuntimeException("创建工商任务失败: " + e.getMessage(), e);
                    }
                }
                
                // 创建银行日记记录（订单审核通过时创建收入记录）
                try {
                    createBankDiaryFromOrder(order, sysUser);
                } catch (Exception e) {
                    log.error("创建银行日记记录失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响订单审核流程
                }
            } else {
                // 部分步骤通过，继续审核流程
                order.setAuditStatus("pending");
            }
            
            order.setUpdateBy(sysUser.getUsername());
            order.setUpdateTime(new Date());
            ghOrderService.updateById(order);
            
            // 记录操作日志
            String operatorName = sysUser.getRealname();
            String action = "approved".equals(auditStatus) ? "审核通过" : "审核驳回";
            ghOrderOperationLogService.saveOperationLog(
                orderId,
                "audit",
                action,
                operatorName,
                null,
                null,
                "审核流程",
                action + "；审核备注：" + (auditRemark != null ? auditRemark : "无")
            );
            
            result.success("审核成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取订单审核记录
     * @param orderId 订单ID
     * @return
     */
    @RequestMapping(value = "/getOrderAudits", method = RequestMethod.GET)
    public Result<List<org.jeecg.modules.order.entity.GhOrderAudit>> getOrderAudits(
            @RequestParam(name="orderId",required=true) String orderId) {
        Result<List<org.jeecg.modules.order.entity.GhOrderAudit>> result = new Result<>();
        try {
            List<org.jeecg.modules.order.entity.GhOrderAudit> audits = 
                ghOrderAuditService.getAuditsByOrderId(orderId);
            result.setResult(audits);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取当前用户可审核的订单列表
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getMyAuditOrders", method = RequestMethod.GET)
    public Result<IPage<GhOrder>> getMyAuditOrders(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        Result<IPage<GhOrder>> result = new Result<>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            // 获取当前用户的角色ID列表
            List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(sysUser.getUsername());
            if (roleIds == null || roleIds.isEmpty()) {
                result.setResult(new Page<>());
                result.setSuccess(true);
                return result;
            }
            
            // 查询待当前用户角色审核的订单
            // 这里需要根据审核记录表查询，暂时返回空列表
            // 实际实现需要关联查询订单审核记录表
            Page<GhOrder> page = new Page<>(pageNo, pageSize);
            result.setResult(page);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取字典文本（如果获取失败，返回原值）
     */
    private String getDictText(String dictCode, String dictValue) {
        if (dictValue == null || dictValue.trim().isEmpty()) {
            return null;
        }
        try {
            String text = sysDictService.queryDictTextByKey(dictCode, dictValue);
            return text != null && !text.trim().isEmpty() ? text : dictValue;
        } catch (Exception e) {
            // 如果获取字典文本失败，返回原值
            log.debug("获取字典文本失败 - dictCode: {}, dictValue: {}, error: {}", dictCode, dictValue, e.getMessage());
            return dictValue;
        }
    }
    
    /**
     * 获取银行账户名称（三级级联格式：收款方式/收款单位/人/网点名称/卡号）
     */
    private String getBankAccountName(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return "空";
        }
        try {
            if (ghBankManagementService == null) {
                return accountId;
            }
            org.jeecg.modules.bank.entity.GhBankManagement bankAccount = ghBankManagementService.getById(accountId);
            if (bankAccount == null) {
                return accountId; // 如果查询不到，返回原ID
            }
            // 构建三级级联格式：收款方式 / 收款单位/人 / 网点名称 / 卡号
            StringBuilder accountName = new StringBuilder();
            if (oConvertUtils.isNotEmpty(bankAccount.getPaymentMethod())) {
                accountName.append(bankAccount.getPaymentMethod());
            }
            if (oConvertUtils.isNotEmpty(bankAccount.getPayeePerson())) {
                if (accountName.length() > 0) accountName.append(" / ");
                accountName.append(bankAccount.getPayeePerson());
            }
            if (oConvertUtils.isNotEmpty(bankAccount.getAccountNotes())) {
                if (accountName.length() > 0) accountName.append(" / ");
                accountName.append(bankAccount.getAccountNotes());
            }
            if (oConvertUtils.isNotEmpty(bankAccount.getCollectionAccount())) {
                if (accountName.length() > 0) accountName.append(" / ");
                accountName.append(bankAccount.getCollectionAccount());
            }
            return accountName.length() > 0 ? accountName.toString() : accountId;
        } catch (Exception e) {
            log.debug("获取银行账户名称失败 - accountId: {}, error: {}", accountId, e.getMessage());
            return accountId; // 如果查询失败，返回原ID
        }
    }
    
    /**
     * 查询未收全的订单（订单金额 > 已收金额）
     * @param order
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/listUnreceivedOrders", method = RequestMethod.GET)
    public Result<IPage<GhOrder>> listUnreceivedOrders(GhOrder order,
                                                        @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                        HttpServletRequest req) {
        Result<IPage<GhOrder>> result = new Result<>();
        try {
            QueryWrapper<GhOrder> queryWrapper = QueryGenerator.initQueryWrapper(order, req.getParameterMap());
            queryWrapper.eq("del_flag", 0); // 只查询未删除的记录
            
            // 查询所有订单，然后过滤出未收全的订单
            Page<GhOrder> page = new Page<>(pageNo, pageSize);
            IPage<GhOrder> pageList = ghOrderService.page(page, queryWrapper);
            
            // 过滤出未收全的订单（订单金额 > 已收金额）
            // 注意：需要先查询所有符合条件的订单，然后过滤，最后再分页
            // 因为分页后再过滤会导致总数不准确
            List<GhOrder> allUnreceivedOrders = new java.util.ArrayList<>();
            
            // 先查询所有符合条件的订单（不分页）
            QueryWrapper<GhOrder> allQueryWrapper = QueryGenerator.initQueryWrapper(order, req.getParameterMap());
            allQueryWrapper.eq("del_flag", 0);
            List<GhOrder> allOrders = ghOrderService.list(allQueryWrapper);
            
            if (allOrders != null && !allOrders.isEmpty()) {
                for (GhOrder orderItem : allOrders) {
                    // 获取订单金额（合同金额或订单金额）
                    BigDecimal orderAmount = orderItem.getContractAmount() != null ? 
                        orderItem.getContractAmount() : orderItem.getOrderAmount();
                    
                    if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue; // 跳过金额为0或null的订单
                    }
                    
                    // 计算已收金额（从收款记录表查询）
                    List<GhOrderPayment> payments = ghOrderPaymentService.getPaymentsByOrderId(orderItem.getId());
                    BigDecimal receivedAmount = BigDecimal.ZERO;
                    if (payments != null && !payments.isEmpty()) {
                        for (GhOrderPayment payment : payments) {
                            if (payment.getStatus() != null && "1".equals(payment.getStatus()) 
                                && payment.getAmount() != null) {
                                receivedAmount = receivedAmount.add(payment.getAmount());
                            }
                        }
                    }
                    
                    // 如果已收金额 < 订单金额，则添加到未收全订单列表
                    if (receivedAmount.compareTo(orderAmount) < 0) {
                        // 设置未收金额
                        BigDecimal unreceivedAmount = orderAmount.subtract(receivedAmount);
                        orderItem.setUnreceivedAmount(unreceivedAmount);
                        orderItem.setReceivedAmount(receivedAmount);
                        allUnreceivedOrders.add(orderItem);
                    }
                }
            }
            
            // 手动分页
            int total = allUnreceivedOrders.size();
            int startIndex = (pageNo - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            List<GhOrder> unreceivedOrders = new java.util.ArrayList<>();
            if (startIndex < total) {
                unreceivedOrders = allUnreceivedOrders.subList(startIndex, endIndex);
            }
            
            // 创建新的分页结果
            IPage<GhOrder> resultPage = new Page<>(pageNo, pageSize, total);
            resultPage.setRecords(unreceivedOrders);
            result.setResult(resultPage);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败！");
        }
        return result;
    }
    
    /**
     * 创建收款记录（支持附件上传）
     * @param payment
     * @return
     */
    @RequestMapping(value = "/addPayment", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addPayment(@RequestBody GhOrderPayment payment) {
        Result<?> result = new Result<>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            // 验证必填字段
            if (payment.getOrderId() == null || payment.getOrderId().trim().isEmpty()) {
                result.error500("订单ID不能为空");
                return result;
            }
            
            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                result.error500("收款金额必须大于0");
                return result;
            }
            
            // 查询订单信息
            GhOrder order = ghOrderService.getById(payment.getOrderId());
            if (order == null) {
                result.error500("订单不存在");
                return result;
            }
            
            // 获取订单金额（合同金额或订单金额）
            BigDecimal orderAmount = order.getContractAmount() != null ? 
                order.getContractAmount() : order.getOrderAmount();
            
            if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                result.error500("订单金额无效");
                return result;
            }
            
            // 计算已收金额（从收款记录表查询）
            List<GhOrderPayment> payments = ghOrderPaymentService.getPaymentsByOrderId(payment.getOrderId());
            BigDecimal receivedAmount = BigDecimal.ZERO;
            if (payments != null && !payments.isEmpty()) {
                for (GhOrderPayment p : payments) {
                    if (p.getStatus() != null && "1".equals(p.getStatus()) && p.getAmount() != null) {
                        receivedAmount = receivedAmount.add(p.getAmount());
                    }
                }
            }
            
            // 计算未收金额
            BigDecimal unreceivedAmount = orderAmount.subtract(receivedAmount);
            
            // 验证收款金额不能超过未收金额
            if (payment.getAmount().compareTo(unreceivedAmount) > 0) {
                result.error500("收款金额不能超过未收金额（" + unreceivedAmount + "）");
                return result;
            }
            
            // 处理收款账号：accountNumber应该保存为银行账户ID，而不是字符串
            // 如果前端传入的是ID，直接保存；如果是字符串，尝试查询对应的ID
            if (payment.getAccountNumber() != null && !payment.getAccountNumber().trim().isEmpty()) {
                try {
                    // 先尝试作为ID查询
                    GhBankManagement bankAccount = ghBankManagementService.getById(payment.getAccountNumber());
                    if (bankAccount != null) {
                        // 确认是ID，直接保存ID
                        // accountNumber已经是ID，不需要修改
                        // 如果收款方式为空，自动填充
                        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) {
                            payment.setPaymentMethod(bankAccount.getPaymentMethod());
                        }
                    } else {
                        // 如果不是ID，尝试通过收款账号查询ID
                        QueryWrapper<GhBankManagement> wrapper = new QueryWrapper<>();
                        wrapper.eq("collection_account", payment.getAccountNumber());
                        wrapper.eq("del_flag", 0);
                        List<GhBankManagement> bankAccounts = ghBankManagementService.list(wrapper);
                        if (bankAccounts != null && !bankAccounts.isEmpty()) {
                            // 找到对应的银行账户，保存ID
                            payment.setAccountNumber(bankAccounts.get(0).getId());
                            if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) {
                                payment.setPaymentMethod(bankAccounts.get(0).getPaymentMethod());
                            }
                        } else {
                            // 如果查询不到，说明可能是旧的字符串格式，保留原值（兼容旧数据）
                            log.warn("收款账号不是有效的ID，且无法通过账号查询到ID: {}", payment.getAccountNumber());
                        }
                    }
                } catch (Exception e) {
                    // 如果查询失败，保留原值（兼容旧数据）
                    log.warn("收款账号查询失败，保留原值: {}", payment.getAccountNumber(), e);
                }
            }
            
            // 设置默认值
            if (payment.getPaymentTime() == null) {
                payment.setPaymentTime(new Date());
            }
            if (payment.getStatus() == null || payment.getStatus().trim().isEmpty()) {
                payment.setStatus("1"); // 默认已确认
            }
            payment.setCreateBy(sysUser.getUsername());
            payment.setCreateTime(new Date());
            
            // 保存收款记录
            ghOrderPaymentService.save(payment);
            
            // 更新订单的已收金额
            BigDecimal newReceivedAmount = receivedAmount.add(payment.getAmount());
            order.setReceivedAmount(newReceivedAmount);
            ghOrderService.updateById(order);
            
            // 创建银行日记记录（未收订单收款后也要进入银行日记）
            if (payment.getStatus() != null && "1".equals(payment.getStatus())) {
                // 只有已确认的收款才创建银行日记记录
                createBankDiaryFromOrderPayment(order, payment, sysUser);
            }
            
            // 记录操作日志
            GhOrderOperationLog log = new GhOrderOperationLog();
            log.setOrderId(payment.getOrderId());
            log.setOperationType("收款");
            log.setOperatorId(sysUser.getId());
            log.setOperatorName(sysUser.getRealname());
            log.setOperationDesc("创建收款记录，金额：" + payment.getAmount() + 
                (payment.getRemarks() != null ? "，备注：" + payment.getRemarks() : ""));
            log.setCreateTime(new Date());
            log.setCreateBy(sysUser.getUsername());
            ghOrderOperationLogService.save(log);
            
            result.success("收款成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("收款失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 手动触发转地址（用于已审核完成的订单）
     * @param jsonObject 包含订单ID或订单编号
     * @return
     */
    @RequestMapping(value = "/convertToAddress", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> convertToAddress(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("orderId");
            String orderNo = jsonObject.getString("orderNo");
            
            if (oConvertUtils.isEmpty(orderId) && oConvertUtils.isEmpty(orderNo)) {
                result.error500("参数不完整，请提供订单ID或订单编号");
                return result;
            }
            
            GhOrder order = null;
            if (oConvertUtils.isNotEmpty(orderId)) {
                order = ghOrderService.getById(orderId);
            } else if (oConvertUtils.isNotEmpty(orderNo)) {
                QueryWrapper<GhOrder> wrapper = new QueryWrapper<>();
                wrapper.eq("order_no", orderNo);
                wrapper.eq("del_flag", 0);
                order = ghOrderService.getOne(wrapper);
            }
            
            if (order == null) {
                result.error500("订单不存在");
                return result;
            }
            
            // 使用SpringContextUtils获取地址服务
            org.jeecg.modules.address.service.IGhAddressCenterService addressService = 
                org.jeecg.common.util.SpringContextUtils.getBean(org.jeecg.modules.address.service.IGhAddressCenterService.class);
            
            // 检查是否已经存在地址记录
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.address.entity.GhAddressCenter> addressWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            addressWrapper.eq("contract_id", order.getId());
            addressWrapper.eq("del_flag", 0);
            org.jeecg.modules.address.entity.GhAddressCenter existingAddress = addressService.getOne(addressWrapper);
            
            if (existingAddress != null) {
                result.error500("该订单已存在地址记录，地址ID: " + existingAddress.getId());
                return result;
            }
            
            // 创建地址记录
            org.jeecg.modules.address.entity.GhAddressCenter addressCenter = 
                new org.jeecg.modules.address.entity.GhAddressCenter();
            addressCenter.setCompanyName(order.getCompanyName());
            addressCenter.setSalesman(order.getSalesman());
            if (oConvertUtils.isNotEmpty(order.getCompanyId())) {
                addressCenter.setDataId(order.getCompanyId());
            }
            addressCenter.setContractId(order.getId());
            addressCenter.setAddressStatus("1");
            addressCenter.setRemarks("手动转为地址");
            addressCenter.setCreateTime(new Date());
            addressCenter.setDelFlag(0);
            
            if (order.getContractAmount() != null) {
                addressCenter.setContractAmount(order.getContractAmount().intValue());
            } else if (order.getOrderAmount() != null) {
                addressCenter.setContractAmount(order.getOrderAmount().intValue());
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                addressCenter.setCreateBy(sysUser.getUsername());
            }
            
            addressService.save(addressCenter);
            
            result.success("转地址成功！地址ID: " + addressCenter.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("转地址失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 从订单收费记录创建银行日记记录（订单提交时）
     * @param order 订单对象
     * @param payment 收费记录
     * @param sysUser 当前登录用户
     */
    private void createBankDiaryFromOrderPayment(GhOrder order, GhOrderPayment payment, LoginUser sysUser) {
        if (ghBankDiaryService == null) {
            return;
        }
        
        // 检查是否已经存在该收费记录对应的银行日记记录（通过businessId，即收款记录ID）
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("business_id", payment.getId());
        queryWrapper.eq("business_type", "订单收入");
        queryWrapper.eq("del_flag", 0);
        long count = ghBankDiaryService.count(queryWrapper);
        if (count > 0) {
            log.info("收款记录ID {} 已存在银行日记记录，跳过创建", payment.getId());
            return;
        }
        
        // 获取收款金额
        BigDecimal receivedAmount = payment.getAmount();
        if (receivedAmount == null || receivedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("订单ID {} 收款金额为0或空，跳过创建银行日记记录", order.getId());
            return;
        }
        
        GhBankDiary bankDiary = new GhBankDiary();
        bankDiary.setOrderDate(payment.getPaymentTime() != null ? payment.getPaymentTime() : order.getCreateTime() != null ? order.getCreateTime() : new Date());
        bankDiary.setBusinessType("订单收入");
        bankDiary.setBusinessId(payment.getId());
        bankDiary.setOrderId(order.getId());
        
        // 费用详情
        String feeDetail = "订单收款 - 订单号：" + order.getOrderNo();
        if (oConvertUtils.isNotEmpty(order.getCompanyName())) {
            feeDetail += "，公司：" + order.getCompanyName();
        }
        bankDiary.setFeeDetail(feeDetail);
        
        // 收入金额（收款金额）
        bankDiary.setIncomeAmount(receivedAmount);
        bankDiary.setExpenseAmount(BigDecimal.ZERO);
        
        // 银行账户（优先从收款记录获取，如果没有则从订单获取）
        String bankAccountId = null;
        if (oConvertUtils.isNotEmpty(payment.getAccountNumber())) {
            // 收款记录中指定的账户（未收订单收款时使用）
            bankAccountId = payment.getAccountNumber();
        } else if (oConvertUtils.isNotEmpty(order.getCollectionAccountNumber())) {
            // 订单中指定的账户（订单提交时使用）
            bankAccountId = order.getCollectionAccountNumber();
        }
        
        if (oConvertUtils.isNotEmpty(bankAccountId)) {
            bankDiary.setBankAccountId(bankAccountId);
            // 尝试获取银行账户名称
            if (ghBankManagementService != null) {
                try {
                    GhBankManagement bankAccount = ghBankManagementService.getById(bankAccountId);
                    if (bankAccount != null) {
                        // 构建账户显示名称：收款单位/人 - 收款账户（网点名称）
                        StringBuilder accountName = new StringBuilder();
                        if (StringUtils.isNotBlank(bankAccount.getPayeePerson())) {
                            accountName.append(bankAccount.getPayeePerson());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getCollectionAccount())) {
                            if (accountName.length() > 0) {
                                accountName.append(" - ");
                            }
                            accountName.append(bankAccount.getCollectionAccount());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getAccountNotes())) {
                            if (accountName.length() > 0) {
                                accountName.append("（").append(bankAccount.getAccountNotes()).append("）");
                            } else {
                                accountName.append(bankAccount.getAccountNotes());
                            }
                        }
                        bankDiary.setBankAccountName(accountName.length() > 0 ? accountName.toString() : "");
                    }
                } catch (Exception e) {
                    log.debug("查询银行账户信息失败", e);
                }
            }
        }
        
        // 操作人员
        if (sysUser != null) {
            bankDiary.setOperatorId(sysUser.getId());
            bankDiary.setOperatorName(sysUser.getRealname());
            bankDiary.setCreateBy(sysUser.getUsername());
        }
        
        // 备注
        bankDiary.setRemarks("订单收款自动生成 - " + (payment.getRemarks() != null ? payment.getRemarks() : ""));
        
        bankDiary.setCreateTime(new Date());
        bankDiary.setDelFlag(0);
        
        // 计算结余金额
        if (bankDiary.getBankAccountId() != null) {
            BigDecimal balanceAmount = calculateBalanceAmount(bankDiary.getBankAccountId(), bankDiary.getOrderDate());
            if (bankDiary.getIncomeAmount() != null) {
                balanceAmount = balanceAmount.add(bankDiary.getIncomeAmount());
            }
            if (bankDiary.getExpenseAmount() != null) {
                balanceAmount = balanceAmount.subtract(bankDiary.getExpenseAmount());
            }
            bankDiary.setBalanceAmount(balanceAmount);
        }
        
        ghBankDiaryService.save(bankDiary);
        
        // 更新后续记录的结余金额
        if (bankDiary.getBankAccountId() != null) {
            updateSubsequentBankDiaryBalances(bankDiary.getBankAccountId(), bankDiary.getOrderDate());
        }
        
        log.info("为订单ID {} 收款记录ID {} 创建了银行日记记录，收款金额: {}", order.getId(), payment.getId(), receivedAmount);
    }
    
    /**
     * 从订单创建银行日记记录（订单审核通过时）
     * @param order 订单对象
     * @param sysUser 当前登录用户
     */
    private void createBankDiaryFromOrder(GhOrder order, LoginUser sysUser) {
        if (ghBankDiaryService == null) {
            return;
        }
        
        // 检查是否已经存在银行日记记录
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", order.getId());
        queryWrapper.eq("del_flag", 0);
        long count = ghBankDiaryService.count(queryWrapper);
        if (count > 0) {
            log.info("订单ID {} 已存在银行日记记录，跳过创建", order.getId());
            return;
        }
        
        // 获取订单金额（合同金额或订单金额）
        BigDecimal orderAmount = order.getContractAmount() != null ? 
            order.getContractAmount() : order.getOrderAmount();
        
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("订单ID {} 金额为0或空，跳过创建银行日记记录", order.getId());
            return;
        }
        
        GhBankDiary bankDiary = new GhBankDiary();
        bankDiary.setOrderDate(order.getCreateTime() != null ? order.getCreateTime() : new Date());
        bankDiary.setBusinessType("订单收入");
        bankDiary.setBusinessId(order.getId());
        bankDiary.setOrderId(order.getId());
        
        // 费用详情
        String feeDetail = "订单号：" + order.getOrderNo();
        if (oConvertUtils.isNotEmpty(order.getCompanyName())) {
            feeDetail += "，公司：" + order.getCompanyName();
        }
        bankDiary.setFeeDetail(feeDetail);
        
        // 收入金额（订单审核通过是收入）
        bankDiary.setIncomeAmount(orderAmount);
        bankDiary.setExpenseAmount(BigDecimal.ZERO);
        
        // 银行账户（从订单收款账号获取，如果没有则留空）
        // 注意：订单审核通过时可能还没有收款，所以银行账户可能为空
        if (oConvertUtils.isNotEmpty(order.getCollectionAccountNumber())) {
            bankDiary.setBankAccountId(order.getCollectionAccountNumber());
            // 尝试获取银行账户名称
            if (ghBankManagementService != null) {
                try {
                    GhBankManagement bankAccount = ghBankManagementService.getById(order.getCollectionAccountNumber());
                    if (bankAccount != null) {
                        // 构建账户显示名称：收款单位/人 - 收款账户（网点名称）
                        StringBuilder accountName = new StringBuilder();
                        if (StringUtils.isNotBlank(bankAccount.getPayeePerson())) {
                            accountName.append(bankAccount.getPayeePerson());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getCollectionAccount())) {
                            if (accountName.length() > 0) {
                                accountName.append(" - ");
                            }
                            accountName.append(bankAccount.getCollectionAccount());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getAccountNotes())) {
                            if (accountName.length() > 0) {
                                accountName.append("（").append(bankAccount.getAccountNotes()).append("）");
                            } else {
                                accountName.append(bankAccount.getAccountNotes());
                            }
                        }
                        bankDiary.setBankAccountName(accountName.length() > 0 ? accountName.toString() : "");
                    }
                } catch (Exception e) {
                    log.debug("查询银行账户信息失败", e);
                }
            }
        }
        
        // 操作人员
        if (sysUser != null) {
            bankDiary.setOperatorId(sysUser.getId());
            bankDiary.setOperatorName(sysUser.getRealname());
            bankDiary.setCreateBy(sysUser.getUsername());
        }
        
        // 备注
        bankDiary.setRemarks("订单审核通过自动生成");
        
        bankDiary.setCreateTime(new Date());
        bankDiary.setDelFlag(0);
        
        // 计算结余金额
        if (bankDiary.getBankAccountId() != null) {
            BigDecimal balanceAmount = calculateBalanceAmount(bankDiary.getBankAccountId(), bankDiary.getOrderDate());
            if (bankDiary.getIncomeAmount() != null) {
                balanceAmount = balanceAmount.add(bankDiary.getIncomeAmount());
            }
            if (bankDiary.getExpenseAmount() != null) {
                balanceAmount = balanceAmount.subtract(bankDiary.getExpenseAmount());
            }
            bankDiary.setBalanceAmount(balanceAmount);
        }
        
        ghBankDiaryService.save(bankDiary);
        log.info("为订单ID {} 创建了银行日记记录", order.getId());
    }
    
    /**
     * 计算指定账户在指定日期之前的结余金额
     */
    private BigDecimal calculateBalanceAmount(String bankAccountId, Date orderDate) {
        if (ghBankDiaryService == null || StringUtils.isBlank(bankAccountId)) {
            return BigDecimal.ZERO;
        }
        
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bank_account_id", bankAccountId);
        queryWrapper.eq("del_flag", 0);
        if (orderDate != null) {
            queryWrapper.lt("order_date", orderDate);
        }
        queryWrapper.orderByDesc("order_date", "create_time");
        queryWrapper.last("LIMIT 1");
        
        GhBankDiary lastDiary = ghBankDiaryService.getOne(queryWrapper);
        if (lastDiary != null && lastDiary.getBalanceAmount() != null) {
            return lastDiary.getBalanceAmount();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 订单非正常完成状态时扣除资金日记
     * @param orderId 订单ID
     * @param statusType 状态类型（退单、驳回、删除等）
     * @param reason 原因
     */
    private void deductBankDiaryForAbnormalOrder(String orderId, String statusType, String reason) {
        if (ghBankDiaryService == null || oConvertUtils.isEmpty(orderId)) {
            return;
        }
        
        log.info("开始处理订单非正常完成状态扣除资金日记，订单ID: {}, 状态类型: {}", orderId, statusType);
        
        try {
            // 查找该订单的所有资金日记收入记录
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId);
            queryWrapper.in("business_type", java.util.Arrays.asList("订单收入"));
            queryWrapper.eq("del_flag", 0);
            List<GhBankDiary> bankDiaries = ghBankDiaryService.list(queryWrapper);
            
            if (bankDiaries != null && !bankDiaries.isEmpty()) {
                for (GhBankDiary bankDiary : bankDiaries) {
                    BigDecimal incomeAmount = bankDiary.getIncomeAmount();
                    String bankAccountId = bankDiary.getBankAccountId();
                    Date orderDate = bankDiary.getOrderDate();
                    
                    // 逻辑删除资金日记收入记录
                    bankDiary.setDelFlag(1);
                    bankDiary.setUpdateTime(new Date());
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        bankDiary.setUpdateBy(sysUser.getUsername());
                    }
                    ghBankDiaryService.updateById(bankDiary);
                    log.info("删除资金日记收入记录，订单ID: {}, 资金日记ID: {}, 收入金额: {}", 
                            orderId, bankDiary.getId(), incomeAmount);
                    
                    // 如果已有收入，创建一条支出记录来平账
                    if (incomeAmount != null && incomeAmount.compareTo(BigDecimal.ZERO) > 0 && 
                        oConvertUtils.isNotEmpty(bankAccountId)) {
                        try {
                            // 创建平账支出记录
                            GhBankDiary offsetDiary = new GhBankDiary();
                            offsetDiary.setOrderDate(new Date());
                            offsetDiary.setBusinessType("订单" + statusType + "平账");
                            offsetDiary.setBusinessId(orderId);
                            offsetDiary.setOrderId(orderId);
                            offsetDiary.setFeeDetail("订单" + statusType + "平账 - 订单ID: " + orderId + (reason != null ? "，原因: " + reason : ""));
                            offsetDiary.setIncomeAmount(BigDecimal.ZERO);
                            offsetDiary.setExpenseAmount(incomeAmount); // 支出金额等于原收入金额
                            offsetDiary.setBankAccountId(bankAccountId);
                            
                            // 操作人员
                            LoginUser sysUser2 = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                            if (sysUser2 != null) {
                                offsetDiary.setOperatorId(sysUser2.getId());
                                offsetDiary.setOperatorName(sysUser2.getRealname());
                                offsetDiary.setCreateBy(sysUser2.getUsername());
                            }
                            
                            // 备注
                            offsetDiary.setRemarks("订单" + statusType + "平账，原收入金额: " + incomeAmount + (reason != null ? "，原因: " + reason : ""));
                            
                            offsetDiary.setCreateTime(new Date());
                            offsetDiary.setDelFlag(0);
                            
                            // 计算结余金额
                            BigDecimal balanceAmount = calculateBalanceAmount(bankAccountId, offsetDiary.getOrderDate());
                            if (offsetDiary.getExpenseAmount() != null) {
                                balanceAmount = balanceAmount.subtract(offsetDiary.getExpenseAmount());
                            }
                            offsetDiary.setBalanceAmount(balanceAmount);
                            
                            ghBankDiaryService.save(offsetDiary);
                            log.info("创建平账支出记录，订单ID: {}, 支出金额: {}, 状态类型: {}", orderId, incomeAmount, statusType);
                            
                            // 更新后续记录的结余金额
                            updateSubsequentBankDiaryBalances(bankAccountId, offsetDiary.getOrderDate());
                        } catch (Exception e) {
                            log.error("创建平账支出记录失败，订单ID: {}", orderId, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理订单非正常完成状态扣除资金日记失败，订单ID: {}", orderId, e);
        }
        
        log.info("订单非正常完成状态扣除资金日记处理完成，订单ID: {}, 状态类型: {}", orderId, statusType);
    }
    
    /**
     * 更新后续记录的结余金额
     */
    private void updateSubsequentBankDiaryBalances(String bankAccountId, Date orderDate) {
        if (ghBankDiaryService == null || oConvertUtils.isEmpty(bankAccountId) || orderDate == null) {
            return;
        }
        
        try {
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("bank_account_id", bankAccountId);
            queryWrapper.eq("del_flag", 0);
            queryWrapper.ge("order_date", orderDate);
            queryWrapper.orderByAsc("order_date", "create_time");
            
            List<GhBankDiary> subsequentDiaries = ghBankDiaryService.list(queryWrapper);
            if (subsequentDiaries != null && !subsequentDiaries.isEmpty()) {
                // 获取当前记录之前的结余金额
                BigDecimal previousBalance = calculateBalanceAmount(bankAccountId, orderDate);
                
                // 重新计算后续所有记录的结余金额
                BigDecimal currentBalance = previousBalance;
                for (GhBankDiary diary : subsequentDiaries) {
                    if (diary.getIncomeAmount() != null) {
                        currentBalance = currentBalance.add(diary.getIncomeAmount());
                    }
                    if (diary.getExpenseAmount() != null) {
                        currentBalance = currentBalance.subtract(diary.getExpenseAmount());
                    }
                    diary.setBalanceAmount(currentBalance);
                    diary.setUpdateTime(new Date());
                    ghBankDiaryService.updateById(diary);
                }
                log.info("更新了 {} 条后续资金日记记录的结余金额", subsequentDiaries.size());
            }
        } catch (Exception e) {
            log.error("更新后续资金日记记录结余金额失败", e);
        }
    }
    
    /**
     * 解析日期字符串为Date对象
     * 支持多种日期格式：yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, yyyy-MM（月份格式）等
     */
    private Date parseDateFromString(String dateValue) {
        if (oConvertUtils.isEmpty(dateValue)) {
            return null;
        }
        
        try {
            // 先尝试月份格式（YYYY-MM），如果是月份格式，转换为该月的第一天
            if (dateValue.matches("^\\d{4}-\\d{2}$")) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
                    java.util.Date monthDate = sdf.parse(dateValue);
                    // 月份格式转换为该月的第一天
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(monthDate);
                    cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    log.debug("解析月份格式日期: {} -> {}", dateValue, cal.getTime());
                    return cal.getTime();
                } catch (Exception e) {
                    // 月份格式解析失败，继续尝试其他格式
                }
            }
            
            // 尝试多种日期格式
            String[] formats = {
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            };
            
            for (String format : formats) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
                    return sdf.parse(dateValue);
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
            
            // 如果都失败，尝试使用默认格式
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateValue);
        } catch (Exception e) {
            log.debug("解析日期失败，日期值: {}, 错误: {}", dateValue, e.getMessage());
            return null;
        }
    }
    
    /**
     * 订单异常状态时回退支出记录和报销（只回退未提交状态的报销）
     * @param orderId 订单ID
     * @param reasonType 原因类型（如：删除、回收站、问题任务等）
     * @param reasonDetail 原因详情
     */
    private void rollbackExpenseAndReimbursement( String orderId, String reasonType, String reasonDetail) {
        if (oConvertUtils.isEmpty(orderId)) {
            return;
        }
        
        log.info("开始回退订单支出记录和报销，订单ID: {}, 原因类型: {}", orderId, reasonType);
        
        try {
            // 1. 回退支出记录（逻辑删除）
            if (ghOrderExpenseService != null) {
                LambdaQueryWrapper<GhOrderExpense> expenseWrapper = new LambdaQueryWrapper<>();
                expenseWrapper.eq(GhOrderExpense::getOrderId, orderId);
                expenseWrapper.eq(GhOrderExpense::getDelFlag, 0); // 只处理未删除的记录
                List<GhOrderExpense> expenses = ghOrderExpenseService.list(expenseWrapper);
                
                if (expenses != null && !expenses.isEmpty()) {
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                    Date now = new Date();
                    
                    for (GhOrderExpense expense : expenses) {
                        expense.setDelFlag(1);
                        expense.setUpdateTime(now);
                        if (sysUser != null) {
                            expense.setUpdateBy(sysUser.getUsername());
                        }
                        ghOrderExpenseService.updateById(expense);
                        log.info("回退支出记录，订单ID: {}, 支出记录ID: {}, 金额: {}", 
                                orderId, expense.getId(), expense.getAmount());
                    }
                }
            }
            
            // 2. 回退报销记录（只回退未提交状态的报销，即auditFlag="0"）
            if (ghReimbursementService != null) {
                // 获取订单信息
                GhOrder order = ghOrderService.getById(orderId);
                if (order != null && oConvertUtils.isNotEmpty(order.getOrderNo())) {
                    QueryWrapper<org.jeecg.modules.reimbursement.entity.GhReimbursement> reimbursementWrapper = new QueryWrapper<>();
                    reimbursementWrapper.eq("contract_no", order.getOrderNo()); // 通过订单编号关联
                    reimbursementWrapper.eq("audit_flag", "0"); // 只回退待提交状态的报销
                    reimbursementWrapper.eq("del_flag", 0); // 只处理未删除的记录
                    
                    List<org.jeecg.modules.reimbursement.entity.GhReimbursement> reimbursements = 
                        ghReimbursementService.list(reimbursementWrapper);
                
                if (reimbursements != null && !reimbursements.isEmpty()) {
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                    Date now = new Date();
                    
                    for (org.jeecg.modules.reimbursement.entity.GhReimbursement reimbursement : reimbursements) {
                        reimbursement.setDelFlag(1);
                        reimbursement.setUpdateTime(now);
                        if (sysUser != null) {
                            reimbursement.setUpdateBy(sysUser.getUsername());
                        }
                        // 更新备注，记录回退原因
                        String originalRemarks = reimbursement.getRemarks() != null ? reimbursement.getRemarks() : "";
                        reimbursement.setRemarks(originalRemarks + " [订单" + reasonType + "回退: " + reasonDetail + "]");
                        ghReimbursementService.updateById(reimbursement);
                        log.info("回退报销记录，订单ID: {}, 报销ID: {}, 金额: {}", 
                                orderId, reimbursement.getId(), reimbursement.getTotalPrice());
                    }
                }
                } // 关闭 if (order != null && oConvertUtils.isNotEmpty(order.getOrderNo()))
            }
            
            // 3. 回退关联的资金日记支出记录（如果有）
            if (ghBankDiaryService != null) {
                QueryWrapper<GhBankDiary> bankDiaryWrapper = new QueryWrapper<>();
                bankDiaryWrapper.eq("order_id", orderId);
                bankDiaryWrapper.eq("business_type", "订单支出"); // 订单支出类型的资金日记
                bankDiaryWrapper.eq("del_flag", 0);
                List<GhBankDiary> expenseDiaries = ghBankDiaryService.list(bankDiaryWrapper);
                
                if (expenseDiaries != null && !expenseDiaries.isEmpty()) {
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    Date now = new Date();
                    
                    for (GhBankDiary diary : expenseDiaries) {
                        diary.setDelFlag(1);
                        diary.setUpdateTime(now);
                        if (sysUser != null) {
                            diary.setUpdateBy(sysUser.getUsername());
                        }
                        ghBankDiaryService.updateById(diary);
                        log.info("回退资金日记支出记录，订单ID: {}, 资金日记ID: {}, 支出金额: {}", 
                                orderId, diary.getId(), diary.getExpenseAmount());
                        
                        // 更新后续记录的结余金额
                        if (diary.getBankAccountId() != null) {
                            updateSubsequentBankDiaryBalances(diary.getBankAccountId(), diary.getOrderDate());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("回退订单支出记录和报销失败，订单ID: {}", orderId, e);
            // 不抛出异常，避免影响主流程
        }
        
        log.info("订单支出记录和报销回退完成，订单ID: {}, 原因类型: {}", orderId, reasonType);
    }
    
    /**
     * 递归查询角色树的所有下级角色ID（包括自身）
     * @param roleId 角色ID
     * @return 所有下级角色ID列表（包括自身）
     */
    private List<String> getAllSubRoleIds(String roleId) {
        List<String> result = new ArrayList<>();
        if (oConvertUtils.isEmpty(roleId)) {
            return result;
        }
        
        // 添加自身
        result.add(roleId);
        
        // 查询直接子角色
        LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = new LambdaQueryWrapper<>();
        query.eq(org.jeecg.modules.system.entity.SysRole::getParentId, roleId);
        query.eq(org.jeecg.modules.system.entity.SysRole::getStatus, 1); // 只查询启用的角色
        query.eq(org.jeecg.modules.system.entity.SysRole::getRoleType, 2); // 只查询数据角色
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
        if (oConvertUtils.isEmpty(roleId)) {
            return result;
        }
        
        // 查询直接子角色
        LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = new LambdaQueryWrapper<>();
        query.eq(org.jeecg.modules.system.entity.SysRole::getParentId, roleId);
        query.eq(org.jeecg.modules.system.entity.SysRole::getStatus, 1); // 只查询启用的角色
        query.eq(org.jeecg.modules.system.entity.SysRole::getRoleType, 2); // 只查询数据角色
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
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<SysUserRole> query = new LambdaQueryWrapper<>();
        query.in(SysUserRole::getRoleId, roleIds);
        List<SysUserRole> userRoles = sysUserRoleService.list(query);
        
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取用户ID并去重
        return userRoles.stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 新签订单统计分析
     * @param year 年份
     * @param month 月份（可选）
     * @param businessType 业务类型（可选）
     * @return 统计数据
     */
    @GetMapping(value = "/ghOrder/getNewOrderStatistics")
    public Result<?> getNewOrderStatistics(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "businessType", required = false) String businessType) {
        try {
            // 如果没有指定年份，使用当前年份
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 构建查询条件
            QueryWrapper<GhOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("del_flag", 0);
            
            // 年份过滤
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            queryWrapper.ge("create_time", yearStart);
            queryWrapper.le("create_time", yearEnd);
            
            // 月份过滤（如果指定）
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
            
            // 业务类型过滤（如果指定）
            if (oConvertUtils.isNotEmpty(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 查询所有符合条件的订单
            List<GhOrder> orders = ghOrderService.list(queryWrapper);
            
            // 统计数据
            Map<String, Object> result = new HashMap<>();
            
            // 1. 总体统计
            Map<String, Object> statistics = new HashMap<>();
            int totalCount = orders.size();
            BigDecimal totalAmount = BigDecimal.ZERO;
            int salesmanCount = 0;
            java.util.Set<String> salesmanSet = new java.util.HashSet<>();
            
            for (GhOrder order : orders) {
                BigDecimal amount = order.getContractAmount() != null ? 
                    order.getContractAmount() : 
                    (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
                totalAmount = totalAmount.add(amount);
                
                if (oConvertUtils.isNotEmpty(order.getSalesman())) {
                    salesmanSet.add(order.getSalesman());
                }
            }
            
            salesmanCount = salesmanSet.size();
            BigDecimal avgAmount = totalCount > 0 ? 
                totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
            
            statistics.put("totalCount", totalCount);
            statistics.put("totalAmount", totalAmount);
            statistics.put("avgAmount", avgAmount);
            statistics.put("salesmanCount", salesmanCount);
            
            // 2. 月度趋势数据（全年12个月）
            List<Map<String, Object>> monthlyTrend = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                calendar.set(year, m - 1, 1, 0, 0, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                Date mStart = calendar.getTime();
                
                calendar.set(year, m - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
                calendar.set(java.util.Calendar.MILLISECOND, 999);
                Date mEnd = calendar.getTime();
                
                int count = 0;
                BigDecimal amount = BigDecimal.ZERO;
                
                for (GhOrder order : orders) {
                    if (order.getCreateTime() != null && 
                        !order.getCreateTime().before(mStart) && 
                        !order.getCreateTime().after(mEnd)) {
                        count++;
                        BigDecimal orderAmount = order.getContractAmount() != null ? 
                            order.getContractAmount() : 
                            (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
                        amount = amount.add(orderAmount);
                    }
                }
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", m + "月");
                monthData.put("count", count);
                monthData.put("amount", amount);
                monthlyTrend.add(monthData);
            }
            
            // 3. 业务类型分布
            Map<String, Map<String, Object>> businessTypeMap = new HashMap<>();
            // 【性能优化】使用缓存避免重复查询字典
            Map<String, String> businessTypeNameCache = new HashMap<>();
            
            for (GhOrder order : orders) {
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
                                    btName = bt; // 如果查不到名称，使用ID
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
                
                BigDecimal orderAmount = order.getContractAmount() != null ? 
                    order.getContractAmount() : 
                    (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
                
                btData.put("name", btName);
                btData.put("count", count + 1);
                btData.put("amount", amount.add(orderAmount));
                businessTypeMap.put(btName, btData);
            }
            
            List<Map<String, Object>> businessTypeList = new ArrayList<>(businessTypeMap.values());
            
            // 4. 业务员排名（按订单数）
            Map<String, Map<String, Object>> salesmanMap = new HashMap<>();
            for (GhOrder order : orders) {
                String sm = order.getSalesman();
                if (oConvertUtils.isEmpty(sm)) {
                    sm = "未分配";
                }
                
                Map<String, Object> smData = salesmanMap.getOrDefault(sm, new HashMap<>());
                int count = (Integer) smData.getOrDefault("count", 0);
                BigDecimal amount = (BigDecimal) smData.getOrDefault("amount", BigDecimal.ZERO);
                
                BigDecimal orderAmount = order.getContractAmount() != null ? 
                    order.getContractAmount() : 
                    (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
                
                smData.put("salesman", sm);
                smData.put("count", count + 1);
                smData.put("amount", amount.add(orderAmount));
                salesmanMap.put(sm, smData);
            }
            
            List<Map<String, Object>> salesmanRank = new ArrayList<>(salesmanMap.values());
            // 按订单数排序
            salesmanRank.sort((a, b) -> {
                Integer countA = (Integer) a.get("count");
                Integer countB = (Integer) b.get("count");
                return countB.compareTo(countA);
            });
            
            // 5. 每日趋势数据（如果指定了月份，显示该月每日；否则显示全年每月）
            List<Map<String, Object>> dailyTrend = new ArrayList<>();
            if (month != null && month >= 1 && month <= 12) {
                // 显示指定月份的每日数据
                calendar.set(year, month - 1, 1, 0, 0, 0);
                int daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                
                for (int d = 1; d <= daysInMonth; d++) {
                    calendar.set(year, month - 1, d, 0, 0, 0);
                    calendar.set(java.util.Calendar.MILLISECOND, 0);
                    Date dStart = calendar.getTime();
                    
                    calendar.set(year, month - 1, d, 23, 59, 59);
                    calendar.set(java.util.Calendar.MILLISECOND, 999);
                    Date dEnd = calendar.getTime();
                    
                    int count = 0;
                    BigDecimal amount = BigDecimal.ZERO;
                    
                    for (GhOrder order : orders) {
                        if (order.getCreateTime() != null && 
                            !order.getCreateTime().before(dStart) && 
                            !order.getCreateTime().after(dEnd)) {
                            count++;
                            BigDecimal orderAmount = order.getContractAmount() != null ? 
                                order.getContractAmount() : 
                                (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
                            amount = amount.add(orderAmount);
                        }
                    }
                    
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", String.format("%d-%02d-%02d", year, month, d));
                    dayData.put("count", count);
                    dayData.put("amount", amount);
                    dailyTrend.add(dayData);
                }
            } else {
                // 显示全年每月数据（与月度趋势相同，但需要添加date字段）
                for (Map<String, Object> monthData : monthlyTrend) {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", monthData.get("month")); // 使用month作为date显示
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
            log.error("获取新签订单统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务人员月度明细数据
     * @param year 年份
     * @param month 月份（可选）
     * @param businessType 业务类型（可选）
     * @return 业务人员月度明细数据
     */
    @GetMapping(value = "/ghOrder/getSalesmanMonthlyDetail")
    public Result<?> getSalesmanMonthlyDetail(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "businessType", required = false) String businessType) {
        try {
            // 如果没有指定年份，使用当前年份
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 构建查询条件
            QueryWrapper<GhOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("del_flag", 0);
            
            // 年份过滤
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            queryWrapper.ge("create_time", yearStart);
            queryWrapper.le("create_time", yearEnd);
            
            // 业务人员月度明细不受月份过滤影响，始终显示全年数据
            // 不添加月份过滤条件
            
            // 业务类型过滤（如果指定）
            if (oConvertUtils.isNotEmpty(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 查询所有符合条件的订单（全年数据）
            List<GhOrder> orders = ghOrderService.list(queryWrapper);
            
            log.info("业务人员月度明细查询 - 年份: {}, 月份: {}, 业务类型: {}, 查询到订单数: {}", 
                year, month, businessType, orders.size());
            
            // 按业务人员和月份分组统计
            // Map<业务人员, Map<月份, {count, amount}>>
            Map<String, Map<Integer, Map<String, Object>>> salesmanMonthlyMap = new HashMap<>();
            
            int processedCount = 0;
            int skippedCount = 0;
            
            for (GhOrder order : orders) {
                if (order.getCreateTime() == null) {
                    skippedCount++;
                    continue; // 跳过创建时间为空的订单
                }
                
                String salesman = oConvertUtils.isEmpty(order.getSalesman()) ? "未分配" : order.getSalesman();
                
                // 获取订单创建时间的月份（查询条件已经按年份过滤，不需要再次检查年份）
                calendar.setTime(order.getCreateTime());
                int orderMonth = calendar.get(java.util.Calendar.MONTH) + 1; // Calendar月份从0开始
                
                // 业务人员月度明细始终统计所有月份，不受月份参数影响
                processedCount++;
                
                // 调试日志：打印前10条订单信息
                if (processedCount <= 10) {
                    log.info("订单处理 - 业务人员: {}, 订单月份: {}, 创建时间: {}", 
                        salesman, orderMonth, order.getCreateTime());
                }
                
                // 获取或创建业务人员的月份数据
                Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.getOrDefault(salesman, new HashMap<>());
                Map<String, Object> monthData = monthlyData.getOrDefault(orderMonth, new HashMap<>());
                
                // 安全地获取count和amount，处理类型转换
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
                
                BigDecimal orderAmount = order.getContractAmount() != null ? 
                    order.getContractAmount() : 
                    (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
                
                monthData.put("count", count + 1);
                monthData.put("amount", amount.add(orderAmount));
                
                monthlyData.put(orderMonth, monthData);
                salesmanMonthlyMap.put(salesman, monthlyData);
            }
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            
            // 获取所有业务人员
            List<String> salesmen = new ArrayList<>(salesmanMonthlyMap.keySet());
            java.util.Collections.sort(salesmen);
            
            for (String salesman : salesmen) {
                Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.get(salesman);
                
                Map<String, Object> row = new HashMap<>();
                row.put("salesman", salesman);
                
                // 初始化12个月的数据
                int totalCount = 0;
                BigDecimal totalAmount = BigDecimal.ZERO;
                
                for (int m = 1; m <= 12; m++) {
                    Map<String, Object> monthData = monthlyData.getOrDefault(m, new HashMap<>());
                    
                    // 安全地获取count和amount，处理类型转换
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
            
            log.info("业务人员月度明细统计完成 - 处理订单数: {}, 跳过订单数: {}, 业务人员数: {}", 
                processedCount, skippedCount, resultList.size());
            
            return Result.OK(resultList);
        } catch (Exception e) {
            log.error("获取业务人员月度明细数据失败", e);
            return Result.error("获取数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务人员月度订单列表
     * @param year 年份
     * @param month 月份
     * @param salesman 业务人员
     * @param businessType 业务类型（可选）
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    @GetMapping(value = "/ghOrder/getSalesmanMonthlyOrders")
    public Result<?> getSalesmanMonthlyOrders(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = true) Integer month,
            @RequestParam(name = "salesman", required = true) String salesman,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            // 如果没有指定年份，使用当前年份
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 构建查询条件
            QueryWrapper<GhOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("del_flag", 0);
            
            // 年份和月份过滤
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month - 1, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date monthStart = calendar.getTime();
            
            calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date monthEnd = calendar.getTime();
            
            queryWrapper.ge("create_time", monthStart);
            queryWrapper.le("create_time", monthEnd);
            
            // 业务人员过滤
            if (oConvertUtils.isNotEmpty(salesman)) {
                queryWrapper.eq("salesman", salesman);
            }
            
            // 业务类型过滤（如果指定）
            if (oConvertUtils.isNotEmpty(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 排序
            queryWrapper.orderByDesc("create_time");
            
            // 分页查询
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<GhOrder> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            com.baomidou.mybatisplus.core.metadata.IPage<GhOrder> pageResult = ghOrderService.page(page, queryWrapper);
            
            // 处理字典文本
            List<GhOrder> records = pageResult.getRecords();
            if (records != null && !records.isEmpty()) {
                // 【性能优化】使用缓存避免重复查询字典
                Map<String, String> businessTypeNameCache = new HashMap<>();
                
                for (GhOrder order : records) {
                    // 业务类型字典文本
                    if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
                        String bt = order.getBusinessType();
                        // 先从缓存中获取
                        if (businessTypeNameCache.containsKey(bt)) {
                            order.setBusinessType_dictText(businessTypeNameCache.get(bt));
                        } else {
                            // 缓存中没有，查询数据库并缓存
                            try {
                                if (sysCategoryService != null) {
                                    java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
                                    if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
                                        String btName = names.get(0);
                                        order.setBusinessType_dictText(btName);
                                        businessTypeNameCache.put(bt, btName);
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("获取业务类型字典文本失败，业务类型ID: " + bt, e);
                            }
                        }
                    }
                    
                    // 订单状态字典文本
                    if (oConvertUtils.isNotEmpty(order.getOrderStatus())) {
                        try {
                            if (sysDictService != null) {
                                String statusText = sysDictService.queryDictTextByKey("order_status", order.getOrderStatus());
                                if (oConvertUtils.isNotEmpty(statusText)) {
                                    order.setOrderStatus_dictText(statusText);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("获取订单状态字典文本失败，订单状态: " + order.getOrderStatus(), e);
                        }
                    }
                }
            }
            
            // 转换为前端需要的格式
            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", pageResult.getTotal());
            result.put("current", pageResult.getCurrent());
            result.put("size", pageResult.getSize());
            result.put("pages", pageResult.getPages());
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取业务人员月度订单列表失败", e);
            return Result.error("获取订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取第一步审核人用户名列表
     */
    private List<String> getFirstStepAuditors(String orderId) {
        try {
            // 查询该订单的第一步审核任务
            LambdaQueryWrapper<SysAuditTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysAuditTask::getOrderId, orderId);
            queryWrapper.eq(SysAuditTask::getStepOrder, 1);
            queryWrapper.eq(SysAuditTask::getTaskStatus, "pending");
            queryWrapper.eq(SysAuditTask::getDelFlag, 0);
            
            List<SysAuditTask> tasks = sysAuditTaskService.list(queryWrapper);
            if (tasks == null || tasks.isEmpty()) {
                return null;
            }
            
            // 获取审核人用户名（只返回指定的审核人，不返回角色的所有人）
            List<String> auditors = new ArrayList<>();
            for (SysAuditTask task : tasks) {
                // 只有指定了审核人，才发送通知
                if (oConvertUtils.isNotEmpty(task.getAssignedUserId())) {
                    org.jeecg.modules.system.entity.SysUser auditor = sysUserService.getById(task.getAssignedUserId());
                    if (auditor != null) {
                        auditors.add(auditor.getUsername());
                    }
                }
                // 如果没有指定审核人，不通知角色的所有人
            }
            return auditors;
        } catch (Exception e) {
            log.error("获取第一步审核人失败", e);
            return null;
        }
    }
    
    /**
     * 获取上级用户名
     */
    private String getSupervisorUsername(String userId) {
        try {
            org.jeecg.modules.system.entity.SysUser user = sysUserService.getById(userId);
            if (user != null) {
                // 从用户的直属上级字段获取上级用户名
                if (oConvertUtils.isNotEmpty(user.getDirectSupervisor())) {
                    return user.getDirectSupervisor();
                }
            }
        } catch (Exception e) {
            log.error("获取上级用户名失败", e);
        }
        return null;
    }
    
}

