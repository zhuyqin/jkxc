package org.jeecg.modules.address.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.address.entity.GhAddressCenter;
import org.jeecg.modules.address.entity.GhAddressCenterDTO;
import org.jeecg.modules.address.service.IGhAddressCenterService;
import org.jeecg.modules.customer.service.IGhCustomerService;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.reimbursement.util.ReimbursementCategoryUtil;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.reimbursement.entity.GhReimbursement;
import org.jeecg.modules.reimbursement.service.IGhReimbursementService;
import org.jeecg.modules.order.entity.GhOrderExpense;
import org.jeecg.modules.order.service.IGhOrderExpenseService;
import org.jeecg.modules.renew.entity.GhAddressRenew;
import org.jeecg.modules.renew.service.IGhAddressRenewService;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 地址中心
 * @Author: jeecg-boot
 * @Date:   2025-01-10
 * @Version: V1.0
 */
@Api(tags="地址中心")
@RestController
@RequestMapping("/address/ghAddressCenter")
@Slf4j
public class GhAddressCenterController extends JeecgController<GhAddressCenter, IGhAddressCenterService> {
    
    @Autowired
    private IGhAddressCenterService ghAddressCenterService;
    
    @Autowired(required = false)
    private IGhCustomerService ghCustomerService;
    
    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    @Autowired(required = false)
    private IGhOrderService ghOrderService;
    
    @Autowired(required = false)
    private IGhReimbursementService ghReimbursementService;
    
    @Autowired(required = false)
    private ISysBaseAPI sysBaseAPI;
    
    @Autowired(required = false)
    private ReimbursementCategoryUtil reimbursementCategoryUtil;
    
    @Autowired(required = false)
    private IGhOrderExpenseService ghOrderExpenseService;
    
    @Autowired(required = false)
    private org.jeecg.modules.order.service.IGhOrderOperationLogService ghOrderOperationLogService;
    
    @Autowired(required = false)
    private IGhAddressRenewService ghAddressRenewService;
    
    @Autowired(required = false)
    private IGhBankDiaryService ghBankDiaryService;
    
    @Autowired(required = false)
    private IGhBankManagementService ghBankManagementService;

    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysAuditTaskService sysAuditTaskService;

    /**
     * 获取各Tab的统计数量
     *
     * @return
     */
    @AutoLog(value = "地址中心-获取Tab统计数量")
    @ApiOperation(value="地址中心-获取Tab统计数量", notes="地址中心-获取Tab统计数量")
    @GetMapping(value = "/getTabCounts")
    public Result<?> getTabCounts() {
        try {
            java.util.Map<String, Long> counts = new java.util.HashMap<>();
            java.util.Date now = new java.util.Date();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String todayStr = dateFormat.format(now);
            
            // 服务中：service_status = 1 或为空（兼容旧数据）
            QueryWrapper<GhAddressCenter> servingWrapper = new QueryWrapper<>();
            servingWrapper.eq("del_flag", 0);
            servingWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
            counts.put("serving", Long.valueOf(ghAddressCenterService.count(servingWrapper)));
            
            // 当期续费：到期时间在0-30天内，且服务中
            QueryWrapper<GhAddressCenter> currentRenewalWrapper = new QueryWrapper<>();
            currentRenewalWrapper.eq("del_flag", 0);
            currentRenewalWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN 0 AND 30");
            currentRenewalWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
            counts.put("currentRenewal", Long.valueOf(ghAddressCenterService.count(currentRenewalWrapper)));
            
            // t-2逾期续费：已过期两个月内（-1到-60天），且服务中
            QueryWrapper<GhAddressCenter> t2OverdueWrapper = new QueryWrapper<>();
            t2OverdueWrapper.eq("del_flag", 0);
            t2OverdueWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN -60 AND -1");
            t2OverdueWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
            counts.put("t2OverdueRenewal", Long.valueOf(ghAddressCenterService.count(t2OverdueWrapper)));
            
            // t+6预期续费：到期时间在0-180天内，且服务中
            QueryWrapper<GhAddressCenter> t6ExpectedWrapper = new QueryWrapper<>();
            t6ExpectedWrapper.eq("del_flag", 0);
            t6ExpectedWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN 0 AND 180");
            t6ExpectedWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
            counts.put("t6ExpectedRenewal", Long.valueOf(ghAddressCenterService.count(t6ExpectedWrapper)));
            
            // t-3逾期客户：到期时间在-61到-365天内，且服务中
            QueryWrapper<GhAddressCenter> t3OverdueWrapper = new QueryWrapper<>();
            t3OverdueWrapper.eq("del_flag", 0);
            t3OverdueWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN -365 AND -61");
            t3OverdueWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
            counts.put("t3OverdueCustomer", Long.valueOf(ghAddressCenterService.count(t3OverdueWrapper)));
            
            // 已终止客户：service_status = 2
            QueryWrapper<GhAddressCenter> terminatedWrapper = new QueryWrapper<>();
            terminatedWrapper.eq("del_flag", 0);
            terminatedWrapper.eq("service_status", "2");
            counts.put("terminatedCustomer", Long.valueOf(ghAddressCenterService.count(terminatedWrapper)));
            
            return Result.OK(counts);
        } catch (Exception e) {
            log.error("获取Tab统计数量失败", e);
            return Result.error("获取统计数量失败");
        }
    }
    
    /**
     * 分页列表查询
     *
     * @param ghAddressCenter
     * @param pageNo
     * @param pageSize
     * @param tabType
     * @param req
     * @return
     */
    @AutoLog(value = "地址中心-分页列表查询")
    @ApiOperation(value="地址中心-分页列表查询", notes="地址中心-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhAddressCenter ghAddressCenter,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name="tabType", required = false) String tabType,
                                   HttpServletRequest req) {
        QueryWrapper<GhAddressCenter> queryWrapper = QueryGenerator.initQueryWrapper(ghAddressCenter, req.getParameterMap());
        queryWrapper.eq("del_flag", 0);
        
        // 根据tab类型筛选
        java.util.Date now = new java.util.Date();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String todayStr = dateFormat.format(now);
        
        // 如果没有指定tabType，默认使用"serving"
        if (oConvertUtils.isEmpty(tabType)) {
            tabType = "serving";
        }
        
        if ("serving".equals(tabType)) {
            // 服务中：service_status = 1 或为空（兼容旧数据）
            queryWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
        } else if ("currentRenewal".equals(tabType)) {
            // 当期续费：到期时间在0-30天内，且服务中
            queryWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN 0 AND 30");
            queryWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
        } else if ("t2OverdueRenewal".equals(tabType)) {
            // t-2逾期续费：已过期两个月内（-1到-60天），且服务中
            queryWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN -60 AND -1");
            queryWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
        } else if ("t6ExpectedRenewal".equals(tabType)) {
            // t+6预期续费：到期时间在0-180天内，且服务中
            queryWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN 0 AND 180");
            queryWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
        } else if ("t3OverdueCustomer".equals(tabType)) {
            // t-3逾期客户：到期时间在-61到-365天内，且服务中
            queryWrapper.apply("DATEDIFF(termination_date, CURDATE()) BETWEEN -365 AND -61");
            queryWrapper.and(wrapper -> {
                wrapper.eq("service_status", "1").or().isNull("service_status");
            });
        } else if ("terminatedCustomer".equals(tabType)) {
            // 已终止客户：service_status = 2
            queryWrapper.eq("service_status", "2");
        }
        
        queryWrapper.orderByDesc("create_time");
        Page<GhAddressCenter> page = new Page<>(pageNo, pageSize);
        IPage<GhAddressCenter> pageList = ghAddressCenterService.page(page, queryWrapper);
        
        // 为每个地址计算倒计天数、累计时间、企业等级、消费金额、业务标签、关联企业
        if (pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
            // 第一步：收集所有公司名称，批量查询客户信息
            java.util.Set<String> companyNames = new java.util.HashSet<>();
            for (GhAddressCenter address : pageList.getRecords()) {
                if (oConvertUtils.isNotEmpty(address.getCompanyName())) {
                    companyNames.add(address.getCompanyName());
                }
            }
            
            // 批量查询客户信息
            java.util.Map<String, org.jeecg.modules.customer.entity.GhCustomer> customerMap = new java.util.HashMap<>();
            if (!companyNames.isEmpty() && ghCustomerService != null) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.customer.entity.GhCustomer> customerWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                customerWrapper.in("corporate_name", companyNames);
                customerWrapper.eq("del_flag", 0);
                java.util.List<org.jeecg.modules.customer.entity.GhCustomer> customers = ghCustomerService.list(customerWrapper);
                for (org.jeecg.modules.customer.entity.GhCustomer customer : customers) {
                    customerMap.put(customer.getCorporateName(), customer);
                }
            }
            
            // 第二步：收集所有客户ID，批量查询订单信息
            java.util.Set<String> customerIds = new java.util.HashSet<>();
            for (org.jeecg.modules.customer.entity.GhCustomer customer : customerMap.values()) {
                if (oConvertUtils.isNotEmpty(customer.getId())) {
                    customerIds.add(customer.getId());
                }
            }
            
            // 批量查询订单信息（用于计算消费金额、业务标签、订单数量）
            java.util.Map<String, java.util.List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomerId = new java.util.HashMap<>();
            if (!customerIds.isEmpty() && ghOrderService != null) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                orderWrapper.in("company_id", customerIds);
                orderWrapper.eq("del_flag", 0);
                java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
                for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                    String customerId = order.getCompanyId();
                    if (!ordersByCustomerId.containsKey(customerId)) {
                        ordersByCustomerId.put(customerId, new java.util.ArrayList<>());
                    }
                    ordersByCustomerId.get(customerId).add(order);
                }
            }
            
            // 批量查询业务类型字典（用于业务标签）
            java.util.Set<String> businessTypeIds = new java.util.HashSet<>();
            for (java.util.List<org.jeecg.modules.order.entity.GhOrder> orders : ordersByCustomerId.values()) {
                for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                    if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
                        businessTypeIds.add(order.getBusinessType());
                    }
                }
            }
            
            java.util.Map<String, String> businessTypeNameMap = new java.util.HashMap<>();
            if (!businessTypeIds.isEmpty() && sysCategoryService != null) {
                try {
                    java.util.List<String> businessTypeNames = sysCategoryService.loadDictItem(String.join(",", businessTypeIds), false);
                    java.util.List<String> businessTypeIdList = new java.util.ArrayList<>(businessTypeIds);
                    for (int i = 0; i < businessTypeIdList.size() && i < businessTypeNames.size(); i++) {
                        businessTypeNameMap.put(businessTypeIdList.get(i), businessTypeNames.get(i));
                    }
                } catch (Exception e) {
                    log.warn("批量查询业务类型字典失败", e);
                }
            }
            
            // 第三步：遍历地址列表，填充计算字段
            for (GhAddressCenter address : pageList.getRecords()) {
                // 计算倒计天数
                if (address.getTerminationDate() != null) {
                    long diffInMillies = address.getTerminationDate().getTime() - System.currentTimeMillis();
                    long diff = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
                    address.setCountdownDays((int) diff);
                }
                
                // 计算累计时间（从起始日期到现在或终止日期的月数）
                if (address.getStartDate() != null) {
                    Date endDate = address.getTerminationDate() != null ? address.getTerminationDate() : new Date();
                    long months = calculateMonths(address.getStartDate(), endDate);
                    address.setAccumulatedTime(months + "个月");
                }
                
                // 如果找到客户，计算企业等级、消费金额、业务标签、关联企业
                if (oConvertUtils.isNotEmpty(address.getCompanyName())) {
                    org.jeecg.modules.customer.entity.GhCustomer customer = customerMap.get(address.getCompanyName());
                    if (customer != null) {
                        // 计算客户字段：企业等级、消费金额、业务标签、关联企业
                        calculateCustomerFieldsForAddressBatch(address, customer, ordersByCustomerId, businessTypeNameMap);
                    }
                }
            }

            // 第四步：批量加载关联订单（contractId）的审核任务，供列表「审核流程」列展示
            java.util.List<String> contractOrderIds = new java.util.ArrayList<>();
            for (GhAddressCenter address : pageList.getRecords()) {
                if (oConvertUtils.isNotEmpty(address.getContractId())) {
                    contractOrderIds.add(address.getContractId());
                }
            }
            java.util.Map<String, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> auditTasksByOrder =
                new java.util.HashMap<>();
            if (!contractOrderIds.isEmpty() && sysAuditTaskService != null) {
                auditTasksByOrder = sysAuditTaskService.getTasksByOrderIds(contractOrderIds);
            }
            for (GhAddressCenter address : pageList.getRecords()) {
                if (oConvertUtils.isEmpty(address.getContractId())) {
                    address.setAuditStepSummary(new java.util.ArrayList<>());
                    continue;
                }
                java.util.List<org.jeecg.modules.system.entity.SysAuditTask> auditTasks =
                    auditTasksByOrder.getOrDefault(address.getContractId(), java.util.Collections.emptyList());
                address.setAuditStepSummary(buildAuditStepSummaryForList(auditTasks));
            }
        }
        
        return Result.OK(pageList);
    }

    /**
     * 构造列表用的审核步骤行（与详情抽屉口径一致：按步骤顺序、角色、人员）
     */
    private java.util.List<java.util.Map<String, Object>> buildAuditStepSummaryForList(
            java.util.List<org.jeecg.modules.system.entity.SysAuditTask> tasks) {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        if (tasks == null || tasks.isEmpty()) {
            return rows;
        }
        java.util.Map<Integer, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> byStep =
            tasks.stream().collect(java.util.stream.Collectors.groupingBy(
                org.jeecg.modules.system.entity.SysAuditTask::getStepOrder));
        java.util.List<Integer> stepOrders = new java.util.ArrayList<>(byStep.keySet());
        java.util.Collections.sort(stepOrders);
        for (Integer so : stepOrders) {
            java.util.List<org.jeecg.modules.system.entity.SysAuditTask> stepTasks = byStep.get(so);
            if (stepTasks == null) {
                continue;
            }
            String stepStatus = aggregateAuditStepStatus(stepTasks);
            for (org.jeecg.modules.system.entity.SysAuditTask t : stepTasks) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("stepOrder", so);
                row.put("stepStatus", stepStatus);
                row.put("roleName", t.getCurrentRoleName());
                row.put("personText", formatAuditTaskPersonForList(t));
                rows.add(row);
            }
        }
        return rows;
    }

    private String aggregateAuditStepStatus(java.util.List<org.jeecg.modules.system.entity.SysAuditTask> stepTasks) {
        if (stepTasks == null || stepTasks.isEmpty()) {
            return "unknown";
        }
        boolean allApproved = stepTasks.stream()
            .allMatch(t -> "approved".equals(t.getTaskStatus()));
        boolean hasRejected = stepTasks.stream()
            .anyMatch(t -> "rejected".equals(t.getTaskStatus()));
        boolean hasPending = stepTasks.stream()
            .anyMatch(t -> "pending".equals(t.getTaskStatus()));
        if (allApproved) {
            return "approved";
        }
        if (hasRejected) {
            return "rejected";
        }
        if (hasPending) {
            return "pending";
        }
        return "unknown";
    }

    private String formatAuditTaskPersonForList(org.jeecg.modules.system.entity.SysAuditTask task) {
        if (task == null) {
            return "-";
        }
        String st = task.getTaskStatus();
        if ("pending".equals(st)) {
            return oConvertUtils.isNotEmpty(task.getAssignedUserName())
                ? "待审 " + task.getAssignedUserName()
                : "待审";
        }
        if ("approved".equals(st)) {
            return oConvertUtils.isNotEmpty(task.getAuditUserName())
                ? task.getAuditUserName()
                : "已通过";
        }
        if ("rejected".equals(st)) {
            return oConvertUtils.isNotEmpty(task.getAuditUserName())
                ? task.getAuditUserName()
                : "已驳回";
        }
        return oConvertUtils.isNotEmpty(task.getAuditUserName()) ? task.getAuditUserName() : "-";
    }
    
    /**
     * 计算两个日期之间的月数
     */
    private long calculateMonths(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.setTime(startDate);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.setTime(endDate);
        
        int yearDiff = end.get(java.util.Calendar.YEAR) - start.get(java.util.Calendar.YEAR);
        int monthDiff = end.get(java.util.Calendar.MONTH) - start.get(java.util.Calendar.MONTH);
        
        return yearDiff * 12L + monthDiff;
    }
    
    /**
     * 为地址计算客户相关字段：企业等级、消费金额、业务标签、关联企业、订单数量（批量优化版本）
     */
    private void calculateCustomerFieldsForAddressBatch(GhAddressCenter address, 
                                                        org.jeecg.modules.customer.entity.GhCustomer customer,
                                                        java.util.Map<String, java.util.List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomerId,
                                                        java.util.Map<String, String> businessTypeNameMap) {
        try {
            String customerId = customer.getId();
            java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ordersByCustomerId.get(customerId);
            
            if (orders == null) {
                orders = new java.util.ArrayList<>();
            }
            
            // 1. 消费金额：公司全部订单的金额总和
            java.math.BigDecimal totalSpending = java.math.BigDecimal.ZERO;
            for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                java.math.BigDecimal amount = order.getContractAmount() != null ? 
                    order.getContractAmount() : 
                    (order.getOrderAmount() != null ? order.getOrderAmount() : java.math.BigDecimal.ZERO);
                totalSpending = totalSpending.add(amount);
            }
            address.setTotalSpending(totalSpending);
            
            // 2. 业务标签：公司所有业务类型（去重，逗号分隔）
            java.util.Set<String> businessTypeNames = new java.util.LinkedHashSet<>();
            for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
                    String typeName = businessTypeNameMap.get(order.getBusinessType());
                    if (oConvertUtils.isNotEmpty(typeName)) {
                        businessTypeNames.add(typeName);
                    }
                }
            }
            if (!businessTypeNames.isEmpty()) {
                address.setBusinessTag(String.join(",", businessTypeNames));
            }
            
            // 3. 关联企业：直接从客户表获取
            address.setRelatedCompanyName(customer.getRelatedCompanyName());
            
            // 4. 订单数量：用于显示复购信息
            int orderCount = orders.size();
            address.setCustomerOrderCount(orderCount);
            
            // 5. 企业等级：根据消费金额、购买次数、服务时间、高端业务计算
            int purchaseCount = orderCount;
            
            // 计算服务月数（从最早的订单创建时间到现在）
            int serviceMonths = 0;
            if (!orders.isEmpty()) {
                Date earliestDate = null;
                for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                    if (order.getCreateTime() != null) {
                        if (earliestDate == null || order.getCreateTime().before(earliestDate)) {
                            earliestDate = order.getCreateTime();
                        }
                    }
                }
                if (earliestDate != null) {
                    serviceMonths = (int) calculateMonths(earliestDate, new Date());
                }
            }
            
            // 检查是否有高端业务或科小业务
            String businessTag = address.getBusinessTag();
            boolean hasHighEndBusiness = oConvertUtils.isNotEmpty(businessTag) && 
                (businessTag.contains("高端代账") || businessTag.contains("高新"));
            boolean hasKeXiaoBusiness = oConvertUtils.isNotEmpty(businessTag) && businessTag.contains("科小");
            
            String enterpriseLevel = calculateEnterpriseLevel(totalSpending, purchaseCount, serviceMonths, hasHighEndBusiness, hasKeXiaoBusiness);
            address.setEnterpriseLevel(enterpriseLevel);
        } catch (Exception e) {
            log.error("为地址计算客户字段失败（批量），地址ID: " + address.getId(), e);
        }
    }
    
    /**
     * 为地址计算客户相关字段：企业等级、消费金额、业务标签、关联企业、订单数量
     */
    private void calculateCustomerFieldsForAddress(GhAddressCenter address, org.jeecg.modules.customer.entity.GhCustomer customer) {
        try {
            // 1. 消费金额：公司全部订单的金额总和
            java.math.BigDecimal totalSpending = calculateTotalSpending(customer.getCorporateName(), customer.getId());
            address.setTotalSpending(totalSpending);
            
            // 2. 业务标签：公司所有业务类型（去重，逗号分隔）
            String businessTag = calculateBusinessTag(customer.getCorporateName(), customer.getId());
            address.setBusinessTag(businessTag);
            
            // 3. 关联企业：直接从客户表获取
            address.setRelatedCompanyName(customer.getRelatedCompanyName());
            
            // 4. 订单数量：用于显示复购信息
            int orderCount = getPurchaseCount(customer.getCorporateName(), customer.getId());
            address.setCustomerOrderCount(orderCount);
            
            // 5. 企业等级：根据消费金额、购买次数、服务时间、高端业务计算
            int purchaseCount = orderCount; // 购买次数就是订单数量
            int serviceMonths = getServiceMonths(customer.getId());
            boolean hasHighEndBusiness = hasHighEndBusiness(customer.getCorporateName(), customer.getId());
            boolean hasKeXiaoBusiness = hasKeXiaoBusiness(customer.getCorporateName(), customer.getId());
            String enterpriseLevel = calculateEnterpriseLevel(totalSpending, purchaseCount, serviceMonths, hasHighEndBusiness, hasKeXiaoBusiness);
            address.setEnterpriseLevel(enterpriseLevel);
        } catch (Exception e) {
            log.error("为地址计算客户字段失败，地址ID: " + address.getId(), e);
        }
    }
    
    /**
     * 计算消费金额：公司全部订单的金额总和
     */
    private java.math.BigDecimal calculateTotalSpending(String companyName, String customerId) {
        try {
            if (ghOrderService == null) {
                return java.math.BigDecimal.ZERO;
            }
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (oConvertUtils.isNotEmpty(customerId)) {
                wrapper.eq("company_id", customerId);
            } else if (oConvertUtils.isNotEmpty(companyName)) {
                wrapper.eq("company_name", companyName);
            } else {
                return java.math.BigDecimal.ZERO;
            }
            
            java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(wrapper);
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                java.math.BigDecimal amount = order.getContractAmount() != null ? 
                    order.getContractAmount() : 
                    (order.getOrderAmount() != null ? order.getOrderAmount() : java.math.BigDecimal.ZERO);
                total = total.add(amount);
            }
            
            return total;
        } catch (Exception e) {
            log.error("计算消费金额失败", e);
            return java.math.BigDecimal.ZERO;
        }
    }
    
    /**
     * 计算业务标签：公司所有业务类型（去重，逗号分隔）
     */
    private String calculateBusinessTag(String companyName, String customerId) {
        try {
            if (ghOrderService == null || sysCategoryService == null) {
                return null;
            }
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (oConvertUtils.isNotEmpty(customerId)) {
                wrapper.eq("company_id", customerId);
            } else if (oConvertUtils.isNotEmpty(companyName)) {
                wrapper.eq("company_name", companyName);
            } else {
                return null;
            }
            wrapper.isNotNull("business_type");
            wrapper.ne("business_type", "");
            
            java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(wrapper);
            java.util.Set<String> businessTypeIds = new java.util.HashSet<>();
            
            for (org.jeecg.modules.order.entity.GhOrder order : orders) {
                if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
                    businessTypeIds.add(order.getBusinessType());
                }
            }
            
            if (!businessTypeIds.isEmpty()) {
                java.util.List<String> businessTypeNames = sysCategoryService.loadDictItem(String.join(",", businessTypeIds), false);
                return String.join(",", businessTypeNames);
            }
            
            return null;
        } catch (Exception e) {
            log.error("计算业务标签失败", e);
            return null;
        }
    }
    
    /**
     * 获取购买次数
     */
    private int getPurchaseCount(String companyName, String customerId) {
        try {
            if (ghOrderService == null) {
                return 0;
            }
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (oConvertUtils.isNotEmpty(customerId)) {
                wrapper.eq("company_id", customerId);
            } else if (oConvertUtils.isNotEmpty(companyName)) {
                wrapper.eq("company_name", companyName);
            } else {
                return 0;
            }
            
            return (int) ghOrderService.count(wrapper);
        } catch (Exception e) {
            log.error("获取购买次数失败", e);
            return 0;
        }
    }
    
    /**
     * 获取服务月数（从最早的订单创建时间到现在）
     */
    private int getServiceMonths(String customerId) {
        try {
            if (ghOrderService == null || oConvertUtils.isEmpty(customerId)) {
                return 0;
            }
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.eq("company_id", customerId);
            wrapper.orderByAsc("create_time");
            wrapper.last("LIMIT 1");
            
            org.jeecg.modules.order.entity.GhOrder firstOrder = ghOrderService.getOne(wrapper);
            if (firstOrder != null && firstOrder.getCreateTime() != null) {
                return (int) calculateMonths(firstOrder.getCreateTime(), new Date());
            }
            
            return 0;
        } catch (Exception e) {
            log.error("获取服务月数失败", e);
            return 0;
        }
    }
    
    /**
     * 检查是否有高端业务（高端代账、高新）
     */
    private boolean hasHighEndBusiness(String companyName, String customerId) {
        try {
            String businessTag = calculateBusinessTag(companyName, customerId);
            if (oConvertUtils.isEmpty(businessTag)) {
                return false;
            }
            return businessTag.contains("高端代账") || businessTag.contains("高新");
        } catch (Exception e) {
            log.error("检查高端业务失败", e);
            return false;
        }
    }
    
    /**
     * 检查是否有科小业务
     */
    private boolean hasKeXiaoBusiness(String companyName, String customerId) {
        try {
            String businessTag = calculateBusinessTag(companyName, customerId);
            if (oConvertUtils.isEmpty(businessTag)) {
                return false;
            }
            return businessTag.contains("科小");
        } catch (Exception e) {
            log.error("检查科小业务失败", e);
            return false;
        }
    }
    
    /**
     * 计算企业等级
     * 一星：消费金额>=2000 或 购买次数>=5 或 服务时间>=12个月（消费金额低于2000的全部按一星处理）
     * 二星：消费金额>=8000 或 购买次数>=10 或 服务时间>=24个月 或 有科小
     * 三星：消费金额>=16000 或 购买次数>=15 或 服务时间>=36个月 或 有科小
     * 四星：消费金额>=32000 或 购买次数>=20 或 服务时间>=48个月 或 有高端代账/高新
     * 五星：消费金额>=70000 或 购买次数>=21 或 服务时间>=60个月 或 有高端代账/高新
     */
    private String calculateEnterpriseLevel(java.math.BigDecimal totalSpending, int purchaseCount, int serviceMonths, boolean hasHighEndBusiness, boolean hasKeXiaoBusiness) {
        try {
            double spending = totalSpending != null ? totalSpending.doubleValue() : 0.0;
            int maxLevel = 0;
            
            // 判断消费金额等级（消费金额低于2000的全部按一星处理）
            if (spending >= 70000) {
                maxLevel = Math.max(maxLevel, 5);
            } else if (spending >= 32000) {
                maxLevel = Math.max(maxLevel, 4);
            } else if (spending >= 16000) {
                maxLevel = Math.max(maxLevel, 3);
            } else if (spending >= 8000) {
                maxLevel = Math.max(maxLevel, 2);
            } else if (spending >= 2000) {
                maxLevel = Math.max(maxLevel, 1);
            } else if (spending > 0) {
                // 消费金额低于2000的全部按一星处理
                maxLevel = Math.max(maxLevel, 1);
            }
            
            // 判断购买次数等级
            if (purchaseCount >= 21) {
                maxLevel = Math.max(maxLevel, 5);
            } else if (purchaseCount >= 20) {
                maxLevel = Math.max(maxLevel, 4);
            } else if (purchaseCount >= 15) {
                maxLevel = Math.max(maxLevel, 3);
            } else if (purchaseCount >= 10) {
                maxLevel = Math.max(maxLevel, 2);
            } else if (purchaseCount >= 5) {
                maxLevel = Math.max(maxLevel, 1);
            }
            
            // 判断服务时间等级
            if (serviceMonths >= 60) {
                maxLevel = Math.max(maxLevel, 5);
            } else if (serviceMonths >= 48) {
                maxLevel = Math.max(maxLevel, 4);
            } else if (serviceMonths >= 36) {
                maxLevel = Math.max(maxLevel, 3);
            } else if (serviceMonths >= 24) {
                maxLevel = Math.max(maxLevel, 2);
            } else if (serviceMonths >= 12) {
                maxLevel = Math.max(maxLevel, 1);
            }
            
            // 判断科小业务等级（有科小业务至少三星）
            if (hasKeXiaoBusiness) {
                maxLevel = Math.max(maxLevel, 3);
            }
            
            // 判断高端业务等级（有高端业务至少四星）
            if (hasHighEndBusiness) {
                maxLevel = Math.max(maxLevel, 4);
            }
            
            // 返回星级（转换为字典值：1-一星，2-二星，3-三星，4-四星，5-五星）
            switch (maxLevel) {
                case 5: return "5";
                case 4: return "4";
                case 3: return "3";
                case 2: return "2";
                case 1: return "1";
                default: return null;
            }
        } catch (Exception e) {
            log.error("计算企业等级失败", e);
            return null;
        }
    }

    /**
     * 添加
     *
     * @param ghAddressCenter
     * @return
     */
    @AutoLog(value = "地址中心-添加")
    @ApiOperation(value="地址中心-添加", notes="地址中心-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhAddressCenter ghAddressCenter) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghAddressCenter.setCreateBy(sysUser.getUsername());
            }
            ghAddressCenter.setCreateTime(new Date());
            ghAddressCenter.setDelFlag(0);
            ghAddressCenterService.save(ghAddressCenter);
            return Result.OK("添加成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("添加失败！");
        }
    }

    /**
     * 编辑
     *
     * @param ghAddressCenter
     * @return
     */
    @AutoLog(value = "地址中心-编辑")
    @ApiOperation(value="地址中心-编辑", notes="地址中心-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody GhAddressCenter ghAddressCenter) {
        try {
            // 查询原始记录，用于记录操作日志
            GhAddressCenter oldAddress = null;
            if (oConvertUtils.isNotEmpty(ghAddressCenter.getId())) {
                oldAddress = ghAddressCenterService.getById(ghAddressCenter.getId());
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghAddressCenter.setUpdateBy(sysUser.getUsername());
            }
            ghAddressCenter.setUpdateTime(new Date());
            ghAddressCenterService.updateById(ghAddressCenter);
            
            // 记录操作日志
            if (ghOrderOperationLogService != null && oldAddress != null && oConvertUtils.isNotEmpty(oldAddress.getContractId())) {
                try {
                    String orderId = oldAddress.getContractId();
                    String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                    
                    // 检查是否有服务人员变更
                    String oldAdvisor = oldAddress.getAdvisor();
                    String newAdvisor = ghAddressCenter.getAdvisor();
                    if (oConvertUtils.isNotEmpty(newAdvisor) && !newAdvisor.equals(oldAdvisor)) {
                        String operationDesc = String.format("修改服务人员：%s -> %s", 
                            oConvertUtils.isEmpty(oldAdvisor) ? "空" : oldAdvisor, 
                            newAdvisor);
                        ghOrderOperationLogService.saveOperationLog(
                            orderId,
                            "修改服务人员",
                            operationDesc,
                            operatorName,
                            null,
                            null,
                            "advisor",
                            operationDesc
                        );
                    } else {
                        // 其他字段的编辑
                        String operationDesc = "编辑地址信息";
                        ghOrderOperationLogService.saveOperationLog(
                            orderId,
                            "编辑地址",
                            operationDesc,
                            operatorName,
                            null,
                            null,
                            null,
                            operationDesc
                        );
                    }
                } catch (Exception e) {
                    log.warn("记录操作日志失败", e);
                }
            }
            
            return Result.OK("编辑成功!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("编辑失败!");
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "地址中心-通过id删除")
    @ApiOperation(value="地址中心-通过id删除", notes="地址中心-通过id删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        try {
            GhAddressCenter ghAddressCenter = ghAddressCenterService.getById(id);
            if (ghAddressCenter != null) {
                ghAddressCenter.setDelFlag(1);
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    ghAddressCenter.setUpdateBy(sysUser.getUsername());
                }
                ghAddressCenter.setUpdateTime(new Date());
                ghAddressCenterService.updateById(ghAddressCenter);
            }
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("删除失败!");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "地址中心-批量删除")
    @ApiOperation(value="地址中心-批量删除", notes="地址中心-批量删除")
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        try {
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                GhAddressCenter ghAddressCenter = ghAddressCenterService.getById(id);
                if (ghAddressCenter != null) {
                    ghAddressCenter.setDelFlag(1);
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        ghAddressCenter.setUpdateBy(sysUser.getUsername());
                    }
                    ghAddressCenter.setUpdateTime(new Date());
                    ghAddressCenterService.updateById(ghAddressCenter);
                }
            }
            return Result.OK("批量删除成功!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量删除失败!");
        }
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "地址中心-通过id查询")
    @ApiOperation(value="地址中心-通过id查询", notes="地址中心-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        GhAddressCenter ghAddressCenter = ghAddressCenterService.getById(id);
        if (ghAddressCenter == null) {
            return Result.error("未找到对应数据");
        }
        
        // 计算倒计天数
        if (ghAddressCenter.getTerminationDate() != null) {
            long diffInMillies = ghAddressCenter.getTerminationDate().getTime() - System.currentTimeMillis();
            long diff = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
            ghAddressCenter.setCountdownDays((int) diff);
        }
        
        // 计算累计时间
        if (ghAddressCenter.getStartDate() != null) {
            Date endDate = ghAddressCenter.getTerminationDate() != null ? ghAddressCenter.getTerminationDate() : new Date();
            long months = calculateMonths(ghAddressCenter.getStartDate(), endDate);
            ghAddressCenter.setAccumulatedTime(months + "个月");
        }
        
        // 如果找到客户，计算企业等级、消费金额、业务标签、关联企业
        if (oConvertUtils.isNotEmpty(ghAddressCenter.getCompanyName())) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.customer.entity.GhCustomer> customerWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            customerWrapper.eq("corporate_name", ghAddressCenter.getCompanyName());
            customerWrapper.eq("del_flag", 0);
            customerWrapper.last("LIMIT 1");
            
            if (ghCustomerService != null) {
                org.jeecg.modules.customer.entity.GhCustomer customer = ghCustomerService.getOne(customerWrapper);
                if (customer != null) {
                    calculateCustomerFieldsForAddress(ghAddressCenter, customer);
                }
            }
        }
        
        return Result.OK(ghAddressCenter);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ghAddressCenter
     */
    @RequestMapping(value = "/exportXls")
    @org.apache.shiro.authz.annotation.RequiresPermissions("address:export")
    public ModelAndView exportXls(HttpServletRequest request, GhAddressCenter ghAddressCenter) {
        return super.exportXls(request, ghAddressCenter, GhAddressCenter.class, "地址中心");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @org.apache.shiro.authz.annotation.RequiresPermissions("address:import")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, GhAddressCenter.class);
    }
    
    /**
     * 地址中心-报销
     *
     * @param ghAddressCenterDTO
     * @param request
     * @return
     */
    @AutoLog(value = "地址中心-报销")
    @ApiOperation(value="地址中心-报销", notes="地址中心-报销")
    @RequestMapping(value = "/baoxiao", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> baoxiao(@RequestBody GhAddressCenterDTO ghAddressCenterDTO, HttpServletRequest request) {
        try {
            if (ghReimbursementService == null || sysBaseAPI == null) {
                return Result.error("报销服务未配置");
            }
            
            // 获取地址ID（支持DTO或直接传id）
            String addressId = ghAddressCenterDTO.getId();
            if (oConvertUtils.isEmpty(addressId)) {
                return Result.error("地址ID不能为空");
            }
            
            // 获取地址信息
            GhAddressCenter addressCenter = ghAddressCenterService.getById(addressId);
            if (addressCenter == null) {
                return Result.error("地址信息不存在");
            }
            
            GhReimbursement ghReimbursement = new GhReimbursement();
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            String username = JwtUtil.getUsername(token);
            LoginUser sysUser = sysBaseAPI.getUserByName(username);
            
            ghReimbursement.setCreateTime(new Date());
            if (sysUser != null) {
                ghReimbursement.setCreateBy(sysUser.getRealname());
                // 设置报销人员（用于权限过滤和显示）
                ghReimbursement.setReimbursementPerson(sysUser.getRealname());
            }
            ghReimbursement.setReimbursementName("挂靠地址费报销");
            // 设置报销时间
            ghReimbursement.setReimbursementTime(new Date());
            
            // 设置报销信息（优先使用DTO中的值，如果没有则使用地址记录中的值）
            Double totalPrice = ghAddressCenterDTO.getTotalPrice();
            if (totalPrice == null && addressCenter.getAddressCost() != null) {
                totalPrice = addressCenter.getAddressCost().doubleValue();
            }
            if (totalPrice == null) {
                return Result.error("报销金额不能为空");
            }
            
            ghReimbursement.setUnitPrice(totalPrice);
            ghReimbursement.setNum(1.0);
            ghReimbursement.setTotalPrice(totalPrice);
            ghReimbursement.setAuditFlag("0");
            
            if (addressCenter.getContractAmount() != null) {
                ghReimbursement.setContractAmount(String.valueOf(addressCenter.getContractAmount()));
            }
            if (addressCenter.getAddressCost() != null) {
                ghReimbursement.setCost(addressCenter.getAddressCost().toString());
            }
            
            // 公司信息（优先使用DTO，否则使用地址记录）
            String companyId = oConvertUtils.isNotEmpty(ghAddressCenterDTO.getDataId()) ? 
                ghAddressCenterDTO.getDataId() : addressCenter.getDataId();
            String companyName = oConvertUtils.isNotEmpty(ghAddressCenterDTO.getCompanyName()) ? 
                ghAddressCenterDTO.getCompanyName() : addressCenter.getCompanyName();
            
            ghReimbursement.setCompanyId(companyId);
            ghReimbursement.setCompanyName(companyName);
            
            // 报销类目和其他信息（优先使用DTO，如果没有则自动设置为"地址费用"）
            if (oConvertUtils.isNotEmpty(ghAddressCenterDTO.getCategory())) {
                ghReimbursement.setCategory(ghAddressCenterDTO.getCategory());
            } else {
                // 如果没有指定报销类目，自动查询并设置为"地址费用"的ID
                if (reimbursementCategoryUtil != null) {
                    try {
                        String addressCategoryId = reimbursementCategoryUtil.getAddressCategoryId();
                        if (addressCategoryId != null && !addressCategoryId.trim().isEmpty()) {
                            ghReimbursement.setCategory(addressCategoryId);
                            log.debug("自动设置地址费用报销类目ID: {}", addressCategoryId);
                        } else {
                            log.warn("未找到地址费用报销类目ID");
                        }
                    } catch (Exception e) {
                        log.error("查询地址费用分类ID失败", e);
                    }
                } else {
                    log.warn("ReimbursementCategoryUtil未注入，无法自动设置报销类目");
                }
            }
            if (oConvertUtils.isNotEmpty(ghAddressCenterDTO.getBxAccount())) {
                ghReimbursement.setBxAccount(ghAddressCenterDTO.getBxAccount());
            }
            if (oConvertUtils.isNotEmpty(ghAddressCenterDTO.getBxBank())) {
                ghReimbursement.setBxBank(ghAddressCenterDTO.getBxBank());
            }
            if (oConvertUtils.isNotEmpty(ghAddressCenterDTO.getBxRemarks())) {
                ghReimbursement.setBxRemarks(ghAddressCenterDTO.getBxRemarks());
            }
            if (oConvertUtils.isNotEmpty(ghAddressCenterDTO.getBxCompanyName())) {
                ghReimbursement.setBxCompanyName(ghAddressCenterDTO.getBxCompanyName());
            }
            
            // 创建成本支出记录（用于在收费详情中统计成本支出）
            String expenseId = null;
            if (ghOrderExpenseService != null) {
                try {
                    // 获取订单ID（地址记录中的contractId字段存储的是订单ID）
                    String orderId = addressCenter.getContractId();
                    
                    // 如果订单ID为空，尝试通过公司名称查找订单
                    if (oConvertUtils.isEmpty(orderId) && ghOrderService != null && oConvertUtils.isNotEmpty(companyName)) {
                        try {
                            // 查找该公司最近的订单（业务类型为挂靠地址的订单）
                            List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.order.entity.GhOrder>()
                                    .eq(org.jeecg.modules.order.entity.GhOrder::getCompanyName, companyName)
                                    .eq(org.jeecg.modules.order.entity.GhOrder::getDelFlag, 0)
                                    .orderByDesc(org.jeecg.modules.order.entity.GhOrder::getCreateTime)
                                    .last("LIMIT 1")
                            );
                            if (orders != null && !orders.isEmpty()) {
                                orderId = orders.get(0).getId();
                                log.info("通过公司名称查找到订单ID: {}", orderId);
                            }
                        } catch (Exception e) {
                            log.debug("通过公司名称查找订单失败", e);
                        }
                    }
                    
                    // 如果还是没有订单ID，记录警告但不阻止报销流程
                    if (oConvertUtils.isEmpty(orderId)) {
                        log.warn("地址报销时未找到关联的订单ID，地址ID: {}, 公司名称: {}, 无法创建成本支出记录", 
                            addressId, companyName);
                    } else {
                        // 获取报销类目名称（用于成本支出记录的category字段）
                        String expenseCategory = "地址费用";
                        String categoryId = ghAddressCenterDTO.getCategory();
                        if (oConvertUtils.isEmpty(categoryId) && reimbursementCategoryUtil != null) {
                            // 如果没有指定类目，使用地址费用类目ID
                            try {
                                categoryId = reimbursementCategoryUtil.getAddressCategoryId();
                            } catch (Exception e) {
                                log.debug("查询地址费用类目ID失败", e);
                            }
                        }
                        
                        // 根据类目ID查询类目名称
                        if (oConvertUtils.isNotEmpty(categoryId) && sysCategoryService != null) {
                            try {
                                SysCategory category = sysCategoryService.getById(categoryId);
                                if (category != null && oConvertUtils.isNotEmpty(category.getName())) {
                                    expenseCategory = category.getName();
                                }
                            } catch (Exception e) {
                                log.debug("查询报销类目名称失败，使用默认值", e);
                            }
                        }
                        
                        // 创建成本支出记录
                        GhOrderExpense expense = new GhOrderExpense();
                        expense.setOrderId(orderId); // 关联订单ID，这样才能在收费详情中统计
                        expense.setExpenseTime(new Date());
                        expense.setAmount(new BigDecimal(totalPrice));
                        expense.setCategory(expenseCategory); // 使用报销类目名称
                        expense.setRemarks("挂靠地址费报销 - " + companyName + " [地址ID:" + addressId + "]");
                        expense.setAuditStatus("0"); // 待审核
                        expense.setPaymentAccount(null); // 支付账户待出纳审核时填写
                        expense.setDelFlag(0);
                        expense.setCreateTime(new Date());
                        
                        if (sysUser != null) {
                            expense.setCreateBy(sysUser.getUsername());
                        }
                        
                        ghOrderExpenseService.save(expense);
                        expenseId = expense.getId();
                        log.info("地址报销时创建成本支出记录，订单ID: {}, 支出ID: {}, 金额: {}, 类目: {}", 
                            orderId, expenseId, totalPrice, expenseCategory);
                    }
                } catch (Exception e) {
                    log.error("创建成本支出记录失败", e);
                    // 不抛出异常，避免影响报销流程，但记录错误日志
                }
            } else {
                log.warn("GhOrderExpenseService未注入，无法创建成本支出记录");
            }
            
            // 将支出记录ID关联到报销记录中
            if (oConvertUtils.isNotEmpty(expenseId)) {
                ghReimbursement.setOrderExpenseId(expenseId);
            }
            
            // 保存报销记录
            ghReimbursementService.save(ghReimbursement);
            
            // 更新地址状态
            addressCenter.setReimbursementFlag("1");
            addressCenter.setAddressStatus("1");
            ghAddressCenterService.updateById(addressCenter);
            
            // 记录操作日志
            if (ghOrderOperationLogService != null && oConvertUtils.isNotEmpty(addressCenter.getContractId())) {
                try {
                    String orderId = addressCenter.getContractId();
                    String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                    String operationDesc = String.format("地址报销，报销金额：¥%s，公司名称：%s", 
                        totalPrice, companyName);
                    ghOrderOperationLogService.saveOperationLog(
                        orderId,
                        "地址报销",
                        operationDesc,
                        operatorName,
                        null,
                        null,
                        "reimbursementFlag,addressStatus",
                        operationDesc
                    );
                } catch (Exception e) {
                    log.warn("记录操作日志失败", e);
                }
            }
            
            return Result.OK("报销成功!");
        } catch (Exception e) {
            log.error("地址中心报销失败", e);
            return Result.error("报销失败：" + e.getMessage());
        }
    }

    /**
     * 地址续费
     *
     * @param params 续费参数
     * @param request 请求
     * @return 结果
     */
    @AutoLog(value = "地址中心-续费")
    @ApiOperation(value = "地址中心-续费", notes = "地址中心-续费")
    @PostMapping(value = "/renew")
    public Result<?> renew(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            if (ghAddressRenewService == null) {
                return Result.error("续费服务未配置");
            }

            // 获取参数
            String addressId = (String) params.get("addressId");
            String companyId = (String) params.get("companyId");
            String companyName = (String) params.get("companyName");
            String endTime = (String) params.get("endTime");
            String amounts = (String) params.get("amounts");
            String receivedAmout = (String) params.get("receivedAmout");
            String paymentTimeStr = (String) params.get("paymentTime");
            String paymentMethod = (String) params.get("paymentMethod");
            String collectionAccountNumber = (String) params.get("collectionAccountNumber");
            String vouchers = (String) params.get("vouchers");
            String remarks = (String) params.get("remarks");

            if (oConvertUtils.isEmpty(addressId)) {
                return Result.error("地址ID不能为空");
            }

            // 获取地址信息
            GhAddressCenter addressCenter = ghAddressCenterService.getById(addressId);
            if (addressCenter == null) {
                return Result.error("地址信息不存在");
            }

            // 获取当前用户
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            String username = JwtUtil.getUsername(token);
            LoginUser sysUser = sysBaseAPI != null ? sysBaseAPI.getUserByName(username) : null;

            // 解析到期时间
            Date postExpirationDate = null;
            if (!oConvertUtils.isEmpty(endTime)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    postExpirationDate = sdf.parse(endTime);
                } catch (ParseException e) {
                    log.error("解析到期时间失败: {}", endTime, e);
                    return Result.error("到期时间格式错误，应为 yyyy-MM-dd");
                }
            }

            // 解析收款时间
            Date paymentTime = null;
            if (!oConvertUtils.isEmpty(paymentTimeStr)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    paymentTime = sdf.parse(paymentTimeStr);
                } catch (ParseException e) {
                    log.error("解析收款时间失败: {}", paymentTimeStr, e);
                    return Result.error("收款时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
                }
            }

            // 创建续费记录
            GhAddressRenew ghAddressRenew = new GhAddressRenew();
            ghAddressRenew.setAddressId(addressId);
            ghAddressRenew.setCompanyId(companyId);
            ghAddressRenew.setCompanyName(companyName != null ? companyName : addressCenter.getCompanyName());
            ghAddressRenew.setRenewalTime(new Date());
            ghAddressRenew.setOriginalDueDate(addressCenter.getTerminationDate());
            ghAddressRenew.setPostExpirationDate(postExpirationDate);
            ghAddressRenew.setAmounts(amounts);
            ghAddressRenew.setAmountReceived(receivedAmout);
            ghAddressRenew.setPaymentTime(paymentTime);
            ghAddressRenew.setDetailType("4"); // 地址续费
            ghAddressRenew.setPaymentMethod(paymentMethod);
            ghAddressRenew.setCollectionAccountNumber(collectionAccountNumber);
            ghAddressRenew.setVouchers(vouchers);
            ghAddressRenew.setRemarks(remarks);
            ghAddressRenew.setAuditStatus("0"); // 待审核
            ghAddressRenew.setFinancialAudit("0"); // 待审核
            ghAddressRenew.setCreateTime(new Date());
            if (sysUser != null) {
                ghAddressRenew.setCreator(sysUser.getRealname());
            }

            // 如果地址有关联的合同，设置合同ID
            if (addressCenter.getContractId() != null) {
                ghAddressRenew.setHtId(addressCenter.getContractId());
            }

            // 保存续费记录
            ghAddressRenewService.save(ghAddressRenew);

            // 创建银行日记收入记录（从保存后的续费记录对象中获取数据）
            log.info("准备创建地址续费银行日记记录，续费记录ID: {}, ghBankDiaryService: {}, collectionAccountNumber: {}, receivedAmout: {}", 
                    ghAddressRenew.getId(), ghBankDiaryService != null, 
                    ghAddressRenew.getCollectionAccountNumber(), ghAddressRenew.getAmountReceived());
            if (ghBankDiaryService != null && 
                StringUtils.isNotBlank(ghAddressRenew.getCollectionAccountNumber()) && 
                StringUtils.isNotBlank(ghAddressRenew.getAmountReceived())) {
                try {
                    createBankDiaryFromRenewal(ghAddressRenew, sysUser, addressCenter);
                    log.info("地址续费银行日记记录创建成功，续费记录ID: {}", ghAddressRenew.getId());
                } catch (Exception e) {
                    log.error("创建地址续费银行日记记录失败，续费记录ID: {}", ghAddressRenew.getId(), e);
                    // 不抛出异常，避免影响续费记录保存
                }
            } else {
                log.warn("地址续费银行日记记录创建条件不满足，续费记录ID: {}, ghBankDiaryService: {}, collectionAccountNumber: {}, receivedAmout: {}", 
                        ghAddressRenew.getId(), ghBankDiaryService != null, 
                        ghAddressRenew.getCollectionAccountNumber(), ghAddressRenew.getAmountReceived());
            }

            // 记录操作日志
            if (ghOrderOperationLogService != null && addressCenter.getContractId() != null) {
                try {
                    String operatorName = sysUser != null ? sysUser.getRealname() : null;
                    ghOrderOperationLogService.saveOperationLog(
                        addressCenter.getContractId(),
                        "地址续费",
                        "地址续费",
                        operatorName,
                        null,
                        null,
                        null,
                        "地址续费，应续费金额：" + (amounts != null ? amounts : "0") + "，到款金额：" + (receivedAmout != null ? receivedAmout : "0")
                    );
                } catch (Exception e) {
                    log.error("记录地址续费操作日志失败", e);
                }
            }

            log.info("地址续费成功，地址ID: {}, 续费记录ID: {}", addressId, ghAddressRenew.getId());
            return Result.OK("续费记录保存成功！");
        } catch (Exception e) {
            log.error("地址中心续费失败", e);
            return Result.error("续费失败：" + e.getMessage());
        }
    }
    
    /**
     * 从续费记录创建银行日记收入记录
     * @param renew 续费记录
     * @param sysUser 当前登录用户
     * @param addressCenter 地址信息
     */
    private void createBankDiaryFromRenewal(GhAddressRenew renew, LoginUser sysUser, GhAddressCenter addressCenter) {
        if (ghBankDiaryService == null) {
            return;
        }
        
        // 检查是否已经存在该续费记录对应的银行日记记录
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("business_id", renew.getId());
        queryWrapper.eq("business_type", "续费收入");
        queryWrapper.eq("del_flag", 0);
        long count = ghBankDiaryService.count(queryWrapper);
        if (count > 0) {
            log.info("续费记录ID {} 已存在银行日记记录，跳过创建", renew.getId());
            return;
        }
        
        // 获取到款金额
        BigDecimal receivedAmount = null;
        try {
            if (oConvertUtils.isNotEmpty(renew.getAmountReceived())) {
                receivedAmount = new BigDecimal(renew.getAmountReceived());
            }
        } catch (Exception e) {
            log.error("解析续费到款金额失败: {}", renew.getAmountReceived(), e);
            return;
        }
        
        if (receivedAmount == null || receivedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("续费记录ID {} 到款金额为0或空，跳过创建银行日记记录", renew.getId());
            return;
        }
        
        // 获取收款账户ID
        String bankAccountId = renew.getCollectionAccountNumber();
        if (oConvertUtils.isEmpty(bankAccountId)) {
            log.info("续费记录ID {} 收款账户为空，跳过创建银行日记记录", renew.getId());
            return;
        }
        
        GhBankDiary bankDiary = new GhBankDiary();
        bankDiary.setOrderDate(renew.getRenewalTime() != null ? renew.getRenewalTime() : new Date());
        bankDiary.setBusinessType("续费收入");
        bankDiary.setBusinessId(renew.getId());
        
        // 费用详情
        String feeDetail = "地址续费收款";
        if (oConvertUtils.isNotEmpty(renew.getCompanyName())) {
            feeDetail += " - 公司：" + renew.getCompanyName();
        }
        bankDiary.setFeeDetail(feeDetail);
        
        // 收入金额（到款金额）
        bankDiary.setIncomeAmount(receivedAmount);
        bankDiary.setExpenseAmount(BigDecimal.ZERO);
        
        // 银行账户
        bankDiary.setBankAccountId(bankAccountId);
        
        // 尝试获取银行账户名称
        if (ghBankManagementService != null) {
            try {
                GhBankManagement bankAccount = ghBankManagementService.getById(bankAccountId);
                if (bankAccount != null) {
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
        
        // 公司信息
        if (oConvertUtils.isNotEmpty(renew.getCompanyId())) {
            bankDiary.setCompanyId(renew.getCompanyId());
        }
        if (oConvertUtils.isNotEmpty(renew.getCompanyName())) {
            bankDiary.setCompanyName(renew.getCompanyName());
        }
        
        // 关联订单ID（如果有）
        if (addressCenter != null && oConvertUtils.isNotEmpty(addressCenter.getContractId())) {
            bankDiary.setOrderId(addressCenter.getContractId());
        }
        
        // 操作人员
        if (sysUser != null) {
            bankDiary.setOperatorId(sysUser.getId());
            bankDiary.setOperatorName(sysUser.getRealname());
            bankDiary.setCreateBy(sysUser.getUsername());
        }
        
        // 备注
        String remarks = "地址续费收款自动生成";
        if (oConvertUtils.isNotEmpty(renew.getRemarks())) {
            remarks += " - " + renew.getRemarks();
        }
        bankDiary.setRemarks(remarks);
        
        // 凭证
        if (oConvertUtils.isNotEmpty(renew.getVouchers())) {
            bankDiary.setVouchers(renew.getVouchers());
        }
        
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
        
        log.info("为地址续费记录ID {} 创建了银行日记记录，到款金额: {}", renew.getId(), receivedAmount);
    }
    
    /**
     * 计算指定银行账户在指定日期之前的结余金额
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
     * 更新后续记录的结余金额
     */
    private void updateSubsequentBankDiaryBalances(String bankAccountId, Date orderDate) {
        if (ghBankDiaryService == null || StringUtils.isBlank(bankAccountId) || orderDate == null) {
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
                BigDecimal previousBalance = calculateBalanceAmount(bankAccountId, orderDate);
                // 加上当前记录的金额
                GhBankDiary currentDiary = ghBankDiaryService.getOne(
                    new QueryWrapper<GhBankDiary>()
                        .eq("bank_account_id", bankAccountId)
                        .eq("order_date", orderDate)
                        .eq("del_flag", 0)
                        .orderByDesc("create_time")
                        .last("LIMIT 1")
                );
                if (currentDiary != null) {
                    if (currentDiary.getIncomeAmount() != null) {
                        previousBalance = previousBalance.add(currentDiary.getIncomeAmount());
                    }
                    if (currentDiary.getExpenseAmount() != null) {
                        previousBalance = previousBalance.subtract(currentDiary.getExpenseAmount());
                    }
                }
                
                for (GhBankDiary diary : subsequentDiaries) {
                    if (diary.getIncomeAmount() != null) {
                        previousBalance = previousBalance.add(diary.getIncomeAmount());
                    }
                    if (diary.getExpenseAmount() != null) {
                        previousBalance = previousBalance.subtract(diary.getExpenseAmount());
                    }
                    diary.setBalanceAmount(previousBalance);
                    ghBankDiaryService.updateById(diary);
                }
                
                log.info("更新了 {} 条后续银行日记记录的结余金额", subsequentDiaries.size());
            }
        } catch (Exception e) {
            log.error("更新后续银行日记记录结余金额失败", e);
        }
    }
}

