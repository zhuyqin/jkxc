package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.order.entity.GhOrderFormData;
import org.jeecg.modules.order.service.IGhOrderFormDataService;
import org.jeecg.modules.system.entity.SysAuditTask;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.entity.SysAuditStepForm;
import org.jeecg.modules.system.entity.SysAuditFormIndicator;
import org.jeecg.modules.system.entity.SysAuditTaskCost;
import org.jeecg.modules.system.mapper.SysAuditTaskMapper;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.order.util.AccountingContractServiceStaffJsonUtil;
import org.jeecg.modules.order.service.IGhOrderOperationLogService;
import org.jeecg.modules.order.service.IGhOrderExpenseService;
import org.jeecg.modules.order.entity.GhOrderExpense;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.apache.shiro.SecurityUtils;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批任务表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Service
@Slf4j
public class SysAuditTaskServiceImpl extends ServiceImpl<SysAuditTaskMapper, SysAuditTask> implements ISysAuditTaskService {

    @Autowired
    private IGhOrderService ghOrderService;
    
    @Autowired
    private ISysAuditTaskCostService sysAuditTaskCostService;
    
    @Autowired
    private ISysAuditProcessService sysAuditProcessService;
    
    @Autowired
    private ISysAuditStepService sysAuditStepService;
    
    @Autowired
    private ISysAuditStepFormService sysAuditStepFormService;
    
    @Autowired
    private ISysAuditFormIndicatorService sysAuditFormIndicatorService;
    
    @Autowired
    private IGhOrderOperationLogService ghOrderOperationLogService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderExpenseService ghOrderExpenseService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhAccountingContractService ghAccountingContractService;
    
    @Autowired
    private org.jeecg.modules.address.service.IGhAddressCenterService ghAddressCenterService;
    
    @Autowired(required = false)
    private IGhBankDiaryService ghBankDiaryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.order.service.IGhOrderPaymentService ghOrderPaymentService;
    
    @Autowired(required = false)
    private org.jeecg.modules.reimbursement.service.IGhReimbursementService ghReimbursementService;
    
    @Autowired(required = false)
    private org.jeecg.modules.reimbursement.util.ReimbursementCategoryUtil reimbursementCategoryUtil;
    
    @Autowired(required = false)
    private ISysDictService sysDictService;
    
    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    @Autowired(required = false)
    private IGhOrderFormDataService ghOrderFormDataService;
    
    @Autowired(required = false)
    private org.jeecg.modules.order.service.IGhOrderStepService ghOrderStepService;
    
    @Autowired(required = false)
    private org.jeecg.modules.notification.service.IWechatNotificationService wechatNotificationService;
    
    @Autowired(required = false)
    private ISysUserService sysUserService;

    @Override
    public List<SysAuditTask> getTasksByOrderId(String orderId) {
        LambdaQueryWrapper<SysAuditTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditTask::getOrderId, orderId);
        wrapper.eq(SysAuditTask::getDelFlag, 0);
        wrapper.orderByAsc(SysAuditTask::getStepOrder);
        wrapper.orderByAsc(SysAuditTask::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public java.util.Map<String, List<SysAuditTask>> getTasksByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        // 批量查询所有订单的审核任务
        LambdaQueryWrapper<SysAuditTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysAuditTask::getOrderId, orderIds);
        wrapper.eq(SysAuditTask::getDelFlag, 0);
        wrapper.orderByAsc(SysAuditTask::getStepOrder);
        wrapper.orderByAsc(SysAuditTask::getCreateTime);
        List<SysAuditTask> allTasks = this.list(wrapper);
        
        // 按订单ID分组
        return allTasks.stream()
            .collect(java.util.stream.Collectors.groupingBy(SysAuditTask::getOrderId));
    }

    @Override
    public SysAuditTask getTaskByOrderIdAndStepId(String orderId, String stepId) {
        LambdaQueryWrapper<SysAuditTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditTask::getOrderId, orderId);
        wrapper.eq(SysAuditTask::getStepId, stepId);
        wrapper.eq(SysAuditTask::getDelFlag, 0);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public SysAuditTask getTaskByOrderIdAndStepOrder(String orderId, Integer stepOrder) {
        LambdaQueryWrapper<SysAuditTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditTask::getOrderId, orderId);
        wrapper.eq(SysAuditTask::getStepOrder, stepOrder);
        wrapper.eq(SysAuditTask::getDelFlag, 0);
        wrapper.orderByDesc(SysAuditTask::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAuditTask createTask(String orderId, String processId, String stepId, Integer stepOrder, String taskType) {
        GhOrder order = ghOrderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 查询步骤信息，获取角色信息
        org.jeecg.modules.system.entity.SysAuditStep step = sysAuditStepService.getById(stepId);
        if (step == null) {
            throw new RuntimeException("审核步骤不存在");
        }
        
        SysAuditTask task = new SysAuditTask();
        task.setOrderId(orderId);
        task.setOrderNo(order.getOrderNo());
        task.setProcessId(processId);
        task.setStepId(stepId);
        task.setStepOrder(stepOrder);
        // 确保taskType不为空，默认为'once'（一次性任务）
        if (oConvertUtils.isEmpty(taskType)) {
            taskType = "once";
            log.warn("创建审核任务时taskType为空，默认设置为'once'，订单ID: {}", orderId);
        }
        task.setTaskType(taskType);
        task.setTaskStatus("pending");
        task.setCurrentRoleId(step.getRoleId());
        task.setCurrentRoleName(step.getRoleName());
        task.setDelFlag(0);
        task.setCreateTime(new Date());
        
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null) {
            task.setCreateBy(sysUser.getUsername());
        }
        
        this.save(task);
        return task;
    }

    /**
     * 从订单表单数据中提取服务时间、到期时间（月份格式）和服务人员
     */
    private void extractServiceTimeFromFormData(SysAuditTask task) {
        // 如果已经有起始月份、截止月份和服务人员，不需要再提取
        if (oConvertUtils.isNotEmpty(task.getStartMonth()) && 
            oConvertUtils.isNotEmpty(task.getEndMonth()) && 
            oConvertUtils.isNotEmpty(task.getServiceUserName())) {
            return;
        }
        
        try {
            // 查询订单表单数据
            if (ghOrderFormDataService == null || oConvertUtils.isEmpty(task.getOrderId())) {
                return;
            }
            
            GhOrderFormData formData = ghOrderFormDataService.getFormDataByOrderId(task.getOrderId());
            if (formData == null || oConvertUtils.isEmpty(formData.getFormData())) {
                return;
            }
            
            // 解析表单数据JSON
            JSONObject dynamicFormData = JSONObject.parseObject(formData.getFormData());
            if (dynamicFormData == null || dynamicFormData.isEmpty()) {
                return;
            }
            
            String serviceStartTimeStr = null;
            String serviceEndTimeStr = null;
            String servicePerson = null;
            
            // 遍历表单数据，查找服务时间、到期时间和服务人员
            for (String key : dynamicFormData.keySet()) {
                Object value = dynamicFormData.get(key);
                if (value == null) {
                    continue;
                }
                
                String valueStr = value.toString();
                String keyLower = key.toLowerCase().trim();
                
                // 匹配服务人员字段（优先提取服务人员）
                if (oConvertUtils.isEmpty(task.getServiceUserName()) && oConvertUtils.isNotEmpty(valueStr)) {
                    if (keyLower.contains("服务人员") || keyLower.contains("服务人") || 
                        keyLower.contains("顾问") || keyLower.contains("advisor") ||
                        keyLower.contains("service") || keyLower.contains("person")) {
                        servicePerson = valueStr.trim();
                        task.setServiceUserName(servicePerson);
                        log.debug("从订单表单数据提取到服务人员: {} = {}", key, valueStr);
                    }
                }
                
                // 匹配开始月份字段
                if (oConvertUtils.isEmpty(task.getStartMonth()) && 
                    (keyLower.contains("开始") || keyLower.contains("起始") || 
                     keyLower.contains("start") || keyLower.contains("begin")) &&
                    (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期")) && 
                    oConvertUtils.isNotEmpty(valueStr)) {
                    // 检查是否是月份格式 YYYY-MM
                    if (valueStr.matches("^\\d{4}-\\d{2}$")) {
                        serviceStartTimeStr = valueStr;
                        task.setStartMonth(serviceStartTimeStr);
                        log.debug("从订单表单数据提取到起始月份: {} = {}", key, valueStr);
                    }
                }
                
                // 匹配结束月份字段
                if (oConvertUtils.isEmpty(task.getEndMonth()) &&
                    (keyLower.contains("结束") || keyLower.contains("终止") || 
                     keyLower.contains("到期") || keyLower.contains("end") ||
                     keyLower.contains("expire") || keyLower.contains("terminate")) &&
                    (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期")) &&
                    oConvertUtils.isNotEmpty(valueStr)) {
                    // 检查是否是月份格式 YYYY-MM
                    if (valueStr.matches("^\\d{4}-\\d{2}$")) {
                        serviceEndTimeStr = valueStr;
                        task.setEndMonth(serviceEndTimeStr);
                        log.debug("从订单表单数据提取到截止月份: {} = {}", key, valueStr);
                    }
                }
            }
            
            // 如果从表单数据中没有提取到服务人员，尝试从订单步骤中获取
            if (oConvertUtils.isEmpty(task.getServiceUserName()) && oConvertUtils.isNotEmpty(task.getOrderId())) {
                try {
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.order.entity.GhOrderStep> stepWrapper = 
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                    stepWrapper.eq(org.jeecg.modules.order.entity.GhOrderStep::getOrderId, task.getOrderId());
                    stepWrapper.orderByAsc(org.jeecg.modules.order.entity.GhOrderStep::getStepOrder);
                    stepWrapper.last("LIMIT 1");
                    org.jeecg.modules.order.entity.GhOrderStep firstStep = ghOrderStepService.getOne(stepWrapper);
                    if (firstStep != null && oConvertUtils.isNotEmpty(firstStep.getOperatorName())) {
                        task.setServiceUserName(firstStep.getOperatorName());
                        log.debug("从订单步骤提取到服务人员: {}", firstStep.getOperatorName());
            }
        } catch (Exception e) {
                    log.debug("从订单步骤获取服务人员失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("从订单表单数据提取服务时间和服务人员失败，订单ID: {}", task.getOrderId(), e);
        }
    }

    @Override
    public IPage<SysAuditTask> getPendingTasks(Page<SysAuditTask> page, String taskType, String userId, List<String> roleIds, Boolean isPersonal, String assignedUserId, String contractNo, String orderNo, String companyName, List<String> dataRoleIds) {
        IPage<SysAuditTask> result = baseMapper.getPendingTasks(page, taskType, userId, roleIds, isPersonal, assignedUserId, contractNo, orderNo, companyName, dataRoleIds);
        // 如果是周期任务，从订单表单数据中提取服务时间和到期时间
        if ("recurring".equals(taskType) && result != null && result.getRecords() != null) {
            for (SysAuditTask task : result.getRecords()) {
                extractServiceTimeFromFormData(task);
            }
        }
        return result;
    }

    @Override
    public IPage<SysAuditTask> getApprovedTasks(Page<SysAuditTask> page, String taskType, String userId, List<String> roleIds, Boolean isPersonal, String orderNo, String companyName, List<String> dataRoleIds) {
        IPage<SysAuditTask> result = baseMapper.getApprovedTasks(page, taskType, userId, roleIds, isPersonal, orderNo, companyName, dataRoleIds);
        // 如果是周期任务，从订单表单数据中提取服务时间和到期时间
        if ("recurring".equals(taskType) && result != null && result.getRecords() != null) {
            for (SysAuditTask task : result.getRecords()) {
                extractServiceTimeFromFormData(task);
            }
        }
        return result;
    }

    @Override
    public IPage<SysAuditTask> getRejectedTasks(Page<SysAuditTask> page, String taskType, String userId, List<String> roleIds, Boolean isPersonal, String orderNo, String companyName, List<String> dataRoleIds) {
        IPage<SysAuditTask> result = baseMapper.getRejectedTasks(page, taskType, userId, roleIds, isPersonal, orderNo, companyName, dataRoleIds);
        // 如果是周期任务，从订单表单数据中提取服务时间和到期时间
        if ("recurring".equals(taskType) && result != null && result.getRecords() != null) {
            for (SysAuditTask task : result.getRecords()) {
                extractServiceTimeFromFormData(task);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeAudit(String taskId, String auditResult, String auditData, String auditRemark, String rejectReason) {
        SysAuditTask task = this.getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!"pending".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务状态不正确，无法审核");
        }
        
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            throw new RuntimeException("请先登录");
        }
        
        Date now = new Date();
        task.setAuditResult(auditResult);
        task.setAuditData(auditData);
        task.setAuditRemark(auditRemark);
        task.setAuditTime(now);
        task.setAuditUserId(sysUser.getId());
        task.setAuditUserName(sysUser.getRealname());
        task.setUpdateTime(now);
        task.setUpdateBy(sysUser.getUsername());
        
        // 根据审核结果设置任务状态
        if ("approved".equals(auditResult)) {
            task.setTaskStatus("approved");
        } else if ("rejected".equals(auditResult)) {
            task.setTaskStatus("rejected");
        } else if ("returned".equals(auditResult)) {
            task.setTaskStatus("rejected");
            if (oConvertUtils.isNotEmpty(rejectReason)) {
                task.setRejectReason(rejectReason);
            }
        }
        
        boolean success = this.updateById(task);
        
        if (success) {
            // 处理成本填写数据
            if (oConvertUtils.isNotEmpty(auditData)) {
                try {
                    JSONObject auditDataJson = JSONObject.parseObject(auditData);
                    // 获取当前步骤的表单配置
                    SysAuditStepForm stepForm = sysAuditStepFormService.getFormByStepId(task.getStepId());
                    if (stepForm != null) {
                        List<SysAuditFormIndicator> indicators = sysAuditFormIndicatorService.getIndicatorsByStepFormId(stepForm.getId());
                        for (SysAuditFormIndicator indicator : indicators) {
                            if ("cost_input".equals(indicator.getIndicatorType())) {
                                // 处理成本填写数据
                                String indicatorKey = "indicator_" + indicator.getId();
                                if (auditDataJson.containsKey(indicatorKey)) {
                                    Object costData = auditDataJson.get(indicatorKey);
                                    if (costData instanceof List) {
                                        List<SysAuditTaskCost> costs = new ArrayList<>();
                                        List<JSONObject> costList = (List<JSONObject>) costData;
                                        for (JSONObject costItem : costList) {
                                            SysAuditTaskCost cost = new SysAuditTaskCost();
                                            cost.setTaskId(taskId);
                                            cost.setIndicatorId(indicator.getId());
                                            cost.setExpenseName(costItem.getString("expenseName"));
                                            cost.setCategoryId(costItem.getString("categoryId"));
                                            cost.setCategoryName(costItem.getString("categoryName"));
                                            cost.setAmount(costItem.getBigDecimal("amount"));
                                            cost.setRemark(costItem.getString("remark"));
                                            cost.setSortOrder(costItem.getInteger("sortOrder"));
                                            costs.add(cost);
                                        }
                                        sysAuditTaskCostService.saveCosts(taskId, costs);
                                        
                                        // 注意：不再在审核时创建支出记录，支出记录应在订单完成时创建
                                        // 审核时只同步成本数据，不创建支出记录
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("处理成本填写数据失败", e);
                }
            }
            
            // 更新订单状态和处理下一步
            GhOrder order = ghOrderService.getById(task.getOrderId());
            if (order != null) {
                if ("returned".equals(auditResult)) {
                    // 驳回：判断是否是第一步驳回
                    boolean isFirstStep = (task.getStepOrder() != null && task.getStepOrder() == 1);
                    
                    if (isFirstStep) {
                        // 第一步驳回：废除收费记录、扣除资金日记、平账
                        try {
                            rollbackFirstStepRejection(order.getId());
                        } catch (Exception e) {
                            log.error("第一步驳回回退处理失败，订单ID: {}", order.getId(), e);
                            // 不抛出异常，避免影响订单状态更新
                        }
                        
                        // 第一步驳回：重置到第一步
                        order.setAuditStatus("pending");
                        order.setOrderStatus("0"); // 待审核
                        order.setCurrentAuditStep(1); // 重置到第一步
                        
                        // 删除当前步骤及之后的所有任务
                        deleteTasksFromStep(order.getId(), 1);
                        
                        // 重新创建第一步的任务
                        recreateFirstStepTasks(order);
                    } else {
                        // 非第一步驳回：回退到上一步
                        Integer previousStepOrder = task.getStepOrder() - 1;
                        if (previousStepOrder < 1) {
                            previousStepOrder = 1; // 确保不会小于1
                        }
                        
                        order.setAuditStatus("pending");
                        order.setOrderStatus("0"); // 待审核
                        order.setCurrentAuditStep(previousStepOrder); // 回退到上一步
                        
                        // 删除当前步骤及之后的所有任务
                        deleteTasksFromStep(order.getId(), task.getStepOrder());
                        
                        // 重新创建上一步的任务（如果上一步的任务不存在或已被删除）
                        recreateStepTasks(order, previousStepOrder);
                    }
                    
                    ghOrderService.updateById(order);
                    
                    // 记录操作日志
                    recordAuditLog(order.getId(), task, auditResult, auditRemark, rejectReason);
                    
                } else if ("rejected".equals(auditResult)) {
                    // 不通过：订单审核不通过
                    order.setAuditStatus("rejected");
                    order.setOrderStatus("4"); // 已驳回
                    ghOrderService.updateById(order);
                    
                    // 记录操作日志
                    recordAuditLog(order.getId(), task, auditResult, auditRemark, null);
                    
                } else if ("approved".equals(auditResult)) {
                    // 通过：检查并行审核是否都完成，然后进入下一步
                    handleApprovedTask(task, order, auditData);
                }
            }
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAuditTask resubmitAfterReject(String orderId, String originalTaskId) {
        // 获取原任务信息
        SysAuditTask originalTask = this.getById(originalTaskId);
        if (originalTask == null) {
            throw new RuntimeException("原任务不存在");
        }
        
        // 创建新的审核任务（从第一步开始）
        SysAuditTask newTask = createTask(orderId, originalTask.getProcessId(), 
                                         originalTask.getStepId(), 1, originalTask.getTaskType());
        
        // 更新订单状态
        GhOrder order = ghOrderService.getById(orderId);
        if (order != null) {
            order.setAuditStatus("pending");
            order.setOrderStatus("0");
            order.setCurrentAuditStep(1);
            ghOrderService.updateById(order);
        }
        
        return newTask;
    }
    
    /**
     * 处理审核通过的任务：检查并行审核是否都完成，然后进入下一步
     */
    private void handleApprovedTask(SysAuditTask task, GhOrder order, String auditData) {
        try {
            // 获取当前步骤的所有并行任务（相同stepOrder的所有任务）
            List<SysAuditTask> allTasks = getTasksByOrderId(task.getOrderId());
            List<SysAuditTask> parallelTasks = allTasks.stream()
                .filter(t -> t.getStepOrder() != null && t.getStepOrder().equals(task.getStepOrder()))
                .filter(t -> t.getDelFlag() == null || t.getDelFlag() == 0)
                .collect(Collectors.toList());
            
            // 检查所有并行任务是否都已完成
            boolean allApproved = true;
            for (SysAuditTask parallelTask : parallelTasks) {
                if (!"approved".equals(parallelTask.getTaskStatus()) && 
                    !"rejected".equals(parallelTask.getTaskStatus())) {
                    allApproved = false;
                    break;
                }
            }
            
            if (!allApproved) {
                // 还有未完成的并行任务，等待
                log.info("订单 {} 的步骤 {} 还有未完成的并行审核任务", order.getId(), task.getStepOrder());
                return;
            }
            
            // 检查是否有任务被拒绝
            boolean hasRejected = parallelTasks.stream()
                .anyMatch(t -> "rejected".equals(t.getTaskStatus()));
            
            if (hasRejected) {
                // 有任务被拒绝，订单审核不通过
                order.setAuditStatus("rejected");
                order.setOrderStatus("4");
                ghOrderService.updateById(order);
                
                // 记录操作日志
                recordAuditLog(order.getId(), task, "rejected", null, null);
                return;
            }
            
            // 检查是否是第一个审核步骤通过，如果是，将订单状态设置为"进行中"
            boolean isFirstStep = (task.getStepOrder() != null && task.getStepOrder() == 1);
            if (isFirstStep && !"1".equals(order.getOrderStatus()) && !"2".equals(order.getOrderStatus())) {
                // 第一个审核步骤通过，订单进入进行中状态
                order.setOrderStatus("1"); // 进行中
                log.info("订单 {} 第一个审核步骤通过，订单状态已更新为进行中", order.getId());
            }
            
            // 所有并行任务都通过，进入下一步
            SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(task.getProcessId());
            if (process == null || process.getSteps() == null || process.getSteps().isEmpty()) {
                log.error("流程不存在或没有步骤，流程ID: {}", task.getProcessId());
                return;
            }
            
            // 对于流失审核（反向流程），需要特殊处理
            boolean isLossAudit = "loss".equals(task.getTaskType());
            boolean isLastStep = false;
            List<SysAuditStep> nextSteps = new ArrayList<>();
            
            if (isLossAudit) {
                // 流失审核是反向流程：最后一步先审，第一步最后审
                // 当 stepOrder=1 的审核通过时，才是最后一步
                if (task.getStepOrder() != null && task.getStepOrder() == 1) {
                    isLastStep = true;
                } else {
                    // 获取上一步（stepOrder - 1）
                    Integer previousStepOrder = task.getStepOrder() - 1;
                    if (previousStepOrder >= 1) {
                        nextSteps = process.getSteps().stream()
                            .filter(step -> step.getStepOrder() != null && step.getStepOrder().equals(previousStepOrder))
                            .collect(Collectors.toList());
                    }
                }
            } else {
                // 正常流程：获取下一步步骤
                Integer nextStepOrder = task.getStepOrder() + 1;
                nextSteps = process.getSteps().stream()
                    .filter(step -> step.getStepOrder() != null && step.getStepOrder().equals(nextStepOrder))
                    .collect(Collectors.toList());
            }
            
            // 注意：不再在审核时创建支出记录，支出记录应在订单完成时创建
            // syncTaskCostsToOrderExpense(order.getId()); // 已移除，避免重复创建
            
            if (nextSteps.isEmpty() || isLastStep) {
                // 没有下一步，所有审核步骤完成
                
                // 检查是否是流失审核（taskType="loss"）
                if ("loss".equals(task.getTaskType())) {
                    // 流失审核最后一步通过，标记合同为流失
                    try {
                        // 根据订单ID查找合同
                        org.jeecg.modules.order.entity.GhAccountingContract contract = null;
                        if (ghAccountingContractService != null) {
                            contract = ghAccountingContractService.getOne(
                                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhAccountingContract>()
                                    .eq("order_id", order.getId())
                                    .last("limit 1")
                            );
                            
                            // 如果找不到，尝试根据订单编号查找
                            if (contract == null && oConvertUtils.isNotEmpty(order.getOrderNo())) {
                                contract = ghAccountingContractService.getOne(
                                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhAccountingContract>()
                                        .eq("contract_no", order.getOrderNo())
                                        .last("limit 1")
                                );
                            }
                        }
                        
                        if (contract != null) {
                            contract.setLossFlag(1);
                            contract.setContractStatus("terminated");
                            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                            if (sysUser != null) {
                                contract.setUpdateBy(sysUser.getUsername());
                            }
                            contract.setUpdateTime(new Date());
                            ghAccountingContractService.updateById(contract);
                            
                            log.info("流失审核通过，合同 {} 已标记为流失", contract.getId());
                            
                            // 记录操作日志
                            if (ghOrderOperationLogService != null) {
                                String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
                                String operationDesc = String.format("流失审核通过，合同编号：%s，公司名称：%s", 
                                    contract.getContractNo(), contract.getCompanyName());
                                ghOrderOperationLogService.saveOperationLog(
                                    order.getId(),
                                    "流失审核通过",
                                    operationDesc,
                                    operatorName,
                                    null,
                                    null,
                                    "lossFlag,contractStatus",
                                    "流失审核流程已完成，合同已标记为流失"
                                );
                            }
                        } else {
                            log.warn("流失审核通过，但未找到对应的合同，订单ID: {}", order.getId());
                        }
                    } catch (Exception e) {
                        log.error("标记合同为流失失败，订单ID: {}", order.getId(), e);
                        // 不抛出异常，避免影响审核流程
                    }
                    
                    // 流失审核完成，不执行订单完成的其他逻辑
                    order.setAuditStatus("approved");
                    order.setOrderStatus("2"); // 已完成
                    order.setCurrentAuditStep(null);
                    ghOrderService.updateById(order);
                    
                    // 记录审核日志
                    recordAuditLog(order.getId(), task, "approved", null, null);
                    log.info("订单 {} 流失审核流程已完成", order.getId());
                    
                } else {
                    // 正常审核流程完成
                    order.setAuditStatus("approved");
                    order.setOrderStatus("2"); // 已完成（所有审批流程走完就自动已完成）
                    order.setCurrentAuditStep(null);
                    ghOrderService.updateById(order);
                    
                    // 检查是否需要转为合同
                    checkAndConvertToContract(order.getId(), task, auditData);
                    
                    // 检查是否需要转为地址
                    checkAndConvertToAddress(order.getId(), task, auditData);
                    
                    // 创建银行日记记录（订单审核完成时创建收入记录）
                    try {
                        createBankDiaryFromOrder(order);
                    } catch (Exception e) {
                        log.error("创建银行日记记录失败，订单ID: {}", order.getId(), e);
                        // 不抛出异常，避免影响审核流程
                    }
                    
                    // 订单完成时，如果有成本支出，创建支出记录和报销记录
                    try {
                        createReimbursementsForCompletedOrder(order.getId());
                        log.info("订单完成，已为订单 {} 创建支出和报销记录", order.getId());
                    } catch (Exception e) {
                        log.error("订单完成时创建支出和报销记录失败，订单ID: {}", order.getId(), e);
                        // 不抛出异常，避免影响审核流程
                    }
                    
                    // 记录操作日志
                    recordAuditLog(order.getId(), task, "approved", null, null);
                    log.info("订单 {} 审核流程已完成", order.getId());
                }
                
            } else {
                // 创建下一步的审核任务
                // 对于流失审核（反向流程），下一步是 stepOrder - 1
                Integer nextStepOrderValue;
                if (isLossAudit) {
                    nextStepOrderValue = task.getStepOrder() - 1;
                } else {
                    nextStepOrderValue = task.getStepOrder() + 1;
                }
                
                // 解析auditData，检查是否有"下个流程审批人员选择"指标
                String assignedUserId = null;
                String assignedUserName = null;
                String nextStepId = null;
                
                if (oConvertUtils.isNotEmpty(auditData)) {
                    try {
                        JSONObject auditDataJson = JSONObject.parseObject(auditData);
                        SysAuditStepForm stepForm = sysAuditStepFormService.getFormByStepId(task.getStepId());
                        if (stepForm != null) {
                            List<SysAuditFormIndicator> indicators = sysAuditFormIndicatorService.getIndicatorsByStepFormId(stepForm.getId());
                            for (SysAuditFormIndicator indicator : indicators) {
                                if ("next_auditor".equals(indicator.getIndicatorType())) {
                                    String indicatorKey = "indicator_" + indicator.getId();
                                    if (auditDataJson.containsKey(indicatorKey)) {
                                        JSONObject nextAuditorConfig = auditDataJson.getJSONObject(indicatorKey);
                                        assignedUserId = nextAuditorConfig.getString("userId");
                                        assignedUserName = nextAuditorConfig.getString("userName");
                                        nextStepId = nextAuditorConfig.getString("nextStepId");
                                        
                                        // 如果指定了下一步的步骤ID，只创建该步骤的任务
                                        if (oConvertUtils.isNotEmpty(nextStepId)) {
                                            // 使用final变量来避免lambda表达式中的编译错误
                                            final String finalNextStepId = nextStepId;
                                            nextSteps = nextSteps.stream()
                                                .filter(step -> step.getId().equals(finalNextStepId))
                                                .collect(Collectors.toList());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析下个审批人员配置失败", e);
                    }
                }
                
                // 为下一步的每个角色创建审核任务（支持并行审核）
                for (SysAuditStep nextStep : nextSteps) {
                    SysAuditTask nextTask = createTask(
                        order.getId(),
                        task.getProcessId(),
                        nextStep.getId(),
                        nextStep.getStepOrder(),
                        task.getTaskType()
                    );
                    
                    // 设置当前审核角色信息
                    nextTask.setCurrentRoleId(nextStep.getRoleId());
                    nextTask.setCurrentRoleName(nextStep.getRoleName());
                    
                    // 如果指定了审核人员，设置指定人员
                    if (oConvertUtils.isNotEmpty(assignedUserId)) {
                        nextTask.setAssignedUserId(assignedUserId);
                        nextTask.setAssignedUserName(assignedUserName);
                    }
                    
                    this.updateById(nextTask);
                    
                    // 发送企业微信通知给下一步审核人
                    try {
                        if (wechatNotificationService != null) {
                            List<String> nextAuditors = getTaskAuditors(nextTask);
                            if (nextAuditors != null && !nextAuditors.isEmpty()) {
                                for (String auditorUsername : nextAuditors) {
                                    try {
                                        // 构建通知内容
                                        String content = buildAuditNotificationContent(order, nextTask);
                                        
                                        wechatNotificationService.sendTextMessage(
                                            auditorUsername,
                                            content
                                        );
                                        log.info("审核通知已发送给下一步审核人：{}", auditorUsername);
                                    } catch (Exception e) {
                                        log.error("发送审核通知失败，审核人：{}", auditorUsername, e);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("发送审核流程通知失败", e);
                        // 通知失败不影响业务流程
                    }
                }
                
                // 更新订单的当前审核步骤
                order.setCurrentAuditStep(nextStepOrderValue);
                order.setAuditStatus("pending");
                ghOrderService.updateById(order);
                
                // 记录操作日志
                recordAuditLog(order.getId(), task, "approved", null, null);
                log.info("订单 {} 进入步骤 {} 审核", order.getId(), nextStepOrderValue);
            }
            
        } catch (Exception e) {
            log.error("处理审核通过任务失败，任务ID: " + task.getId(), e);
            throw new RuntimeException("处理审核通过任务失败", e);
        }
    }
    
    /**
     * 记录审核操作日志
     */
    private void recordAuditLog(String orderId, SysAuditTask task, String auditResult, String auditRemark, String rejectReason) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String operatorName = sysUser != null ? sysUser.getRealname() : task.getAuditUserName();
            
            String logContent = String.format("审核操作 - 步骤：%s，角色：%s，结果：%s",
                task.getStepOrder(), task.getCurrentRoleName(), 
                "approved".equals(auditResult) ? "通过" : 
                "rejected".equals(auditResult) ? "不通过" : "驳回");
            
            if (oConvertUtils.isNotEmpty(auditRemark)) {
                logContent += "，备注：" + auditRemark;
            }
            if (oConvertUtils.isNotEmpty(rejectReason)) {
                logContent += "，驳回原因：" + rejectReason;
            }
            
            ghOrderOperationLogService.saveOperationLog(
                orderId,
                "audit",
                "审核操作",
                operatorName,
                null,
                JSONObject.toJSONString(task),
                null,
                logContent
            );
        } catch (Exception e) {
            log.error("记录审核操作日志失败", e);
        }
    }
    
    /**
     * 审核时填写成本后，立即创建支出记录（待审核状态）
     */
    private void createExpenseRecordsFromCosts(String orderId, List<SysAuditTaskCost> costs, SysAuditTask task, SysAuditFormIndicator indicator) {
        if (costs == null || costs.isEmpty() || ghOrderExpenseService == null) {
            return;
        }
        
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        
        for (SysAuditTaskCost cost : costs) {
            if (cost.getAmount() == null || cost.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            
            // 检查是否已存在相同的支出记录（根据订单ID、类目和任务ID、指标ID）
            String expenseRemark = (cost.getExpenseName() != null ? cost.getExpenseName() : "") + 
                (cost.getRemark() != null && !cost.getRemark().isEmpty() ? " - " + cost.getRemark() : "") +
                " [任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId() + "]";
            
            // 获取报销类目名称（优先使用成本记录中的报销类目名称）
            String reimbursementCategoryName = cost.getCategoryName();
            // 如果报销类目名称为空，根据报销类目ID查询名称
            if (oConvertUtils.isEmpty(reimbursementCategoryName) && oConvertUtils.isNotEmpty(cost.getCategoryId()) && sysCategoryService != null) {
                try {
                    org.jeecg.modules.system.entity.SysCategory category = sysCategoryService.getById(cost.getCategoryId());
                    if (category != null && oConvertUtils.isNotEmpty(category.getName())) {
                        reimbursementCategoryName = category.getName();
                    }
                } catch (Exception e) {
                    log.debug("根据报销类目ID查询名称失败，类目ID: {}", cost.getCategoryId(), e);
                }
            }
            // 如果还是没有，使用默认值
            if (oConvertUtils.isEmpty(reimbursementCategoryName)) {
                reimbursementCategoryName = "审批成本";
            }
            
            LambdaQueryWrapper<GhOrderExpense> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GhOrderExpense::getOrderId, orderId);
            wrapper.eq(GhOrderExpense::getCategory, reimbursementCategoryName);
            wrapper.like(GhOrderExpense::getRemarks, "任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId());
            
            GhOrderExpense existingExpense = ghOrderExpenseService.getOne(wrapper);
            
            if (existingExpense == null) {
                // 创建新的支出记录（待审核状态）
                GhOrderExpense expense = new GhOrderExpense();
                expense.setOrderId(orderId);
                expense.setExpenseTime(cost.getCreateTime() != null ? cost.getCreateTime() : new Date());
                expense.setAmount(cost.getAmount());
                expense.setCategory(reimbursementCategoryName); // 使用报销类目名称
                expense.setRemarks(expenseRemark);
                expense.setAuditStatus("0"); // 待审核
                expense.setDelFlag(0); // 正常状态
                expense.setCreateTime(new Date());
                
                if (sysUser != null) {
                    expense.setCreateBy(sysUser.getUsername());
                }
                
                ghOrderExpenseService.save(expense);
                log.info("审核时创建支出记录（待审核），订单ID: {}, 金额: {}", orderId, cost.getAmount());
            } else {
                // 更新现有支出记录（如果金额不同，且状态为待审核）
                if (existingExpense.getAmount() == null || 
                    existingExpense.getAmount().compareTo(cost.getAmount()) != 0) {
                    existingExpense.setAmount(cost.getAmount());
                    existingExpense.setAuditStatus("0"); // 确保状态为待审核
                    existingExpense.setUpdateTime(new Date());
                    if (sysUser != null) {
                        existingExpense.setUpdateBy(sysUser.getUsername());
                    }
                    ghOrderExpenseService.updateById(existingExpense);
                    log.info("更新支出记录（待审核），订单ID: {}, 金额: {}", orderId, cost.getAmount());
                }
            }
        }
    }
    
    /**
     * 同步审批任务成本到订单支出记录（已废弃，保留用于兼容）
     * 注意：此方法不再创建报销记录，报销记录应在订单完成时创建
     */
    private void syncTaskCostsToOrderExpense(String orderId) {
        try {
            // 获取该订单的所有审批任务
            List<SysAuditTask> tasks = getTasksByOrderId(orderId);
            if (tasks == null || tasks.isEmpty()) {
                return;
            }
            
            // 遍历所有任务，收集成本数据
            for (SysAuditTask task : tasks) {
                List<SysAuditTaskCost> costs = sysAuditTaskCostService.getCostsByTaskId(task.getId());
                if (costs == null || costs.isEmpty()) {
                    continue;
                }
                
                // 将每个成本记录同步到订单支出记录表
                for (SysAuditTaskCost cost : costs) {
                    if (cost.getAmount() == null || cost.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    
                    // 检查是否已存在相同的支出记录（根据订单ID、类目和报销名称）
                    // 使用备注字段存储任务ID和指标ID以便后续匹配
                    String expenseRemark = (cost.getExpenseName() != null ? cost.getExpenseName() : "") + 
                        (cost.getRemark() != null && !cost.getRemark().isEmpty() ? " - " + cost.getRemark() : "") +
                        " [任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId() + "]";
                    
                    LambdaQueryWrapper<GhOrderExpense> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(GhOrderExpense::getOrderId, orderId);
                    wrapper.eq(GhOrderExpense::getCategory, 
                        cost.getCategoryName() != null ? cost.getCategoryName() : "审批成本");
                    wrapper.like(GhOrderExpense::getRemarks, "任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId());
                    
                    GhOrderExpense existingExpense = ghOrderExpenseService.getOne(wrapper);
                    
                    if (existingExpense == null) {
                        // 创建新的支出记录
                        GhOrderExpense expense = new GhOrderExpense();
                        expense.setOrderId(orderId);
                        expense.setExpenseTime(cost.getCreateTime() != null ? cost.getCreateTime() : new Date());
                        expense.setAmount(cost.getAmount());
                        expense.setCategory(cost.getCategoryName() != null ? cost.getCategoryName() : "审批成本");
                        expense.setRemarks(expenseRemark);
                        expense.setCreateTime(new Date());
                        
                        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                        if (sysUser != null) {
                            expense.setCreateBy(sysUser.getUsername());
                        }
                        
                        // 设置审核状态为已审核（因为这是审核通过时同步的）
                        expense.setAuditStatus("1");
                        ghOrderExpenseService.save(expense);
                        log.info("同步审批任务成本到订单支出记录，订单ID: {}, 金额: {}", orderId, cost.getAmount());
                    } else {
                        // 更新现有支出记录（如果金额不同）
                        if (existingExpense.getAmount() == null || 
                            existingExpense.getAmount().compareTo(cost.getAmount()) != 0) {
                            existingExpense.setAmount(cost.getAmount());
                            existingExpense.setUpdateTime(new Date());
                            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                            if (sysUser != null) {
                                existingExpense.setUpdateBy(sysUser.getUsername());
                            }
                            ghOrderExpenseService.updateById(existingExpense);
                            log.info("更新订单支出记录，订单ID: {}, 金额: {}", orderId, cost.getAmount());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("同步审批任务成本到订单支出记录失败，订单ID: " + orderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 检查并转为合同
     */
    private void checkAndConvertToContract(String orderId, SysAuditTask task, String auditData) {
        try {
            if (oConvertUtils.isEmpty(auditData)) {
                return;
            }
            
            // 获取当前步骤的表单配置
            SysAuditStepForm stepForm = sysAuditStepFormService.getFormByStepId(task.getStepId());
            if (stepForm == null) {
                return;
            }
            
            // 获取该步骤的所有指标
            List<SysAuditFormIndicator> indicators = sysAuditFormIndicatorService.getIndicatorsByStepFormId(stepForm.getId());
            if (indicators == null || indicators.isEmpty()) {
                return;
            }
            
            // 解析auditData
            JSONObject auditDataJson = JSONObject.parseObject(auditData);
            
            // 查找转为合同指标
            for (SysAuditFormIndicator indicator : indicators) {
                if ("convert_contract".equals(indicator.getIndicatorType())) {
                    String indicatorKey = "indicator_" + indicator.getId();
                    if (auditDataJson.containsKey(indicatorKey)) {
                        String convertValue = auditDataJson.getString(indicatorKey);
                        if ("yes".equals(convertValue) || "true".equals(convertValue) || "1".equals(convertValue)) {
                            // 需要转为合同，传递表单配置和审核数据以便提取开始时间和结束时间
                            createContractFromOrder(orderId, task, stepForm, auditDataJson);
                            log.info("订单 {} 已自动转为合同", orderId);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查并转为合同失败，订单ID: " + orderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 从订单创建合同（重载方法，兼容旧调用）
     */
    private void createContractFromOrder(String orderId) {
        createContractFromOrder(orderId, null, null, null);
    }
    
    /**
     * 从订单创建合同（支持从动态表单提取开始时间和结束时间）
     */
    private void createContractFromOrder(String orderId, SysAuditTask task, SysAuditStepForm stepForm, JSONObject auditDataJson) {
        try {
            // 检查是否已经存在合同
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.order.entity.GhAccountingContract> contractWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            contractWrapper.eq(org.jeecg.modules.order.entity.GhAccountingContract::getOrderId, orderId);
            contractWrapper.eq(org.jeecg.modules.order.entity.GhAccountingContract::getDelFlag, 0);
            
            org.jeecg.modules.order.entity.GhAccountingContract existingContract = ghAccountingContractService.getOne(contractWrapper);
            if (existingContract != null) {
                log.info("订单 {} 已存在合同，合同编号: {}", orderId, existingContract.getContractNo());
                return;
            }
            
            // 获取订单信息
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                log.error("订单不存在，订单ID: {}", orderId);
                return;
            }
            
            // 创建合同
            org.jeecg.modules.order.entity.GhAccountingContract contract = 
                new org.jeecg.modules.order.entity.GhAccountingContract();
            contract.setOrderId(orderId);
            contract.setOrderNo(order.getOrderNo());
            contract.setCompanyName(order.getCompanyName());
            contract.setServicePerson(order.getSalesman()); // 服务人员使用订单的业务员
            
            // 使用订单的合同金额，如果没有则使用订单金额
            if (order.getContractAmount() != null) {
                contract.setContractAmount(order.getContractAmount());
            } else if (order.getOrderAmount() != null) {
                contract.setContractAmount(order.getOrderAmount());
            }
            
            contract.setSignDate(new Date()); // 签订日期为当前日期
            
            // 优先从任务中获取截至月份（从订单表单数据中提取的）
            Date startDate = null;
            Date endDate = null;
            if (task != null) {
                // 优先使用任务中提取的截至月份
                if (oConvertUtils.isNotEmpty(task.getEndMonth())) {
                    endDate = parseDate(task.getEndMonth());
                    log.info("订单 {} 合同到期时间从任务截至月份提取: {} -> {}", orderId, task.getEndMonth(), endDate);
                }
                // 如果有起始月份，也可以使用（虽然合同通常不需要起始日期）
                if (oConvertUtils.isNotEmpty(task.getStartMonth())) {
                    startDate = parseDate(task.getStartMonth());
                    log.debug("订单 {} 合同起始时间从任务起始月份提取: {} -> {}", orderId, task.getStartMonth(), startDate);
                }
            }
            
            // 如果从任务中没有获取到，再从动态表单中提取开始时间和结束时间
            if (endDate == null && stepForm != null && auditDataJson != null && oConvertUtils.isNotEmpty(stepForm.getFormConfig())) {
                try {
                    JSONObject formConfig = JSONObject.parseObject(stepForm.getFormConfig());
                    if (formConfig != null && formConfig.containsKey("fields")) {
                        com.alibaba.fastjson.JSONArray fields = formConfig.getJSONArray("fields");
                        if (fields != null && !fields.isEmpty()) {
                            for (int i = 0; i < fields.size(); i++) {
                                JSONObject field = fields.getJSONObject(i);
                                String fieldName = field.getString("name");
                                String fieldLabel = field.getString("label");
                                String fieldType = field.getString("type");
                                
                                // 处理日期类型和月份类型的字段（月份类型也需要处理）
                                if (("date".equals(fieldType) || "month".equals(fieldType)) && oConvertUtils.isNotEmpty(fieldName)) {
                                    if (auditDataJson.containsKey(fieldName)) {
                                        String dateValue = auditDataJson.getString(fieldName);
                                        if (oConvertUtils.isNotEmpty(dateValue)) {
                                            // 判断是开始时间还是结束时间（根据label关键词匹配，优先匹配label）
                                            if (fieldLabel != null) {
                                                String labelLower = fieldLabel.toLowerCase().trim();
                                                String nameLower = fieldName != null ? fieldName.toLowerCase().trim() : "";
                                                
                                                // 匹配开始时间的关键词（合同不需要开始时间，但为了统一逻辑保留）
                                                if (labelLower.contains("开始") || labelLower.contains("起始") || 
                                                    labelLower.equals("开始时间") || labelLower.equals("起始日期") ||
                                                    labelLower.equals("起始时间") || labelLower.contains("开始日期") ||
                                                    nameLower.contains("start") || nameLower.contains("begin")) {
                                                    startDate = parseDate(dateValue);
                                                    log.debug("合同：匹配到开始时间字段: {} = {}", fieldLabel, dateValue);
                                                } 
                                                // 匹配结束时间的关键词（合同的到期时间）
                                                else if (labelLower.contains("结束") || labelLower.contains("终止") || 
                                                         labelLower.contains("到期") || labelLower.equals("结束时间") ||
                                                         labelLower.equals("终止日期") || labelLower.equals("终止时间") ||
                                                         labelLower.equals("到期时间") || labelLower.contains("结束日期") ||
                                                         labelLower.contains("到期日期") || nameLower.contains("end") || 
                                                         nameLower.contains("expire") || nameLower.contains("terminate")) {
                                                    endDate = parseDate(dateValue);
                                                    log.debug("合同：匹配到结束时间字段: {} = {}", fieldLabel, dateValue);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("从表单配置中提取日期字段失败: " + e.getMessage());
                }
                
                // 如果从表单配置中还是没有提取到，直接从auditDataJson中查找包含"结束月份"关键词的字段
                if (endDate == null && auditDataJson != null) {
                    try {
                        for (String key : auditDataJson.keySet()) {
                            Object value = auditDataJson.get(key);
                            if (value == null) {
                                continue;
                            }
                            String valueStr = value.toString();
                            String keyLower = key.toLowerCase().trim();
                            
                            // 匹配结束月份字段（不限制字段类型，直接从审核数据中查找）
                            if ((keyLower.contains("结束") || keyLower.contains("终止") || 
                                 keyLower.contains("到期") || keyLower.contains("end") ||
                                 keyLower.contains("expire") || keyLower.contains("terminate")) &&
                                (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期") ||
                                 keyLower.contains("month") || keyLower.contains("date") || keyLower.contains("time")) &&
                                oConvertUtils.isNotEmpty(valueStr)) {
                                // 检查是否是月份格式 YYYY-MM 或日期格式
                                if (valueStr.matches("^\\d{4}-\\d{2}$") || valueStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                                    endDate = parseDate(valueStr);
                                    log.info("合同：从审核数据中直接提取到结束月份字段: {} = {} -> {}", key, valueStr, endDate);
                                    break; // 找到第一个匹配的就退出
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("从审核数据中直接提取结束月份字段失败: " + e.getMessage());
                    }
                }
            }
            
            // 如果从任务和审核数据中都没有提取到，直接从订单表单数据中提取"结束月份"
            if (endDate == null && ghOrderFormDataService != null) {
                try {
                    org.jeecg.modules.order.entity.GhOrderFormData formData = ghOrderFormDataService.getFormDataByOrderId(orderId);
                    if (formData != null && oConvertUtils.isNotEmpty(formData.getFormData())) {
                        JSONObject dynamicFormData = JSONObject.parseObject(formData.getFormData());
                        if (dynamicFormData != null && !dynamicFormData.isEmpty()) {
                            // 遍历表单数据，查找"结束月份"字段
                            for (String key : dynamicFormData.keySet()) {
                                Object value = dynamicFormData.get(key);
                                if (value == null) {
                                    continue;
                                }
                                String valueStr = value.toString();
                                String keyLower = key.toLowerCase().trim();
                                
                                // 匹配结束月份字段
                                if ((keyLower.contains("结束") || keyLower.contains("终止") || 
                                     keyLower.contains("到期") || keyLower.contains("end") ||
                                     keyLower.contains("expire") || keyLower.contains("terminate")) &&
                                    (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期") ||
                                     keyLower.contains("month") || keyLower.contains("date") || keyLower.contains("time")) &&
                                    oConvertUtils.isNotEmpty(valueStr)) {
                                    // 检查是否是月份格式 YYYY-MM 或日期格式
                                    if (valueStr.matches("^\\d{4}-\\d{2}$") || valueStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                                        endDate = parseDate(valueStr);
                                        log.info("合同：从订单表单数据中直接提取到结束月份字段: {} = {} -> {}", key, valueStr, endDate);
                                        break; // 找到第一个匹配的就退出
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("从订单表单数据中提取结束月份字段失败: " + e.getMessage());
                }
            }
            
            // 如果从表单中提取到了结束时间，使用它作为到期时间；否则使用默认值（当月1号）
            if (endDate != null) {
                contract.setExpireDate(endDate);
                log.info("订单 {} 合同到期时间从表单中提取: {}", orderId, endDate);
            } else {
                // 到期时间：默认设置为当月1号（转合同保存到代账管理的日期默认都是1号）
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calendar.set(java.util.Calendar.MINUTE, 0);
                calendar.set(java.util.Calendar.SECOND, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                // 设置为下个月的1号（如果当前是1号，则设置为下个月1号；否则设置为下个月1号）
                calendar.add(java.util.Calendar.MONTH, 1);
                contract.setExpireDate(calendar.getTime());
                log.info("订单 {} 合同到期时间设置为下月1号: {}", orderId, calendar.getTime());
            }
            
            contract.setContractStatus("signed"); // 已签订
            contract.setRemarks("审批转为合同");
            contract.setCreateTime(new Date());
            contract.setDelFlag(0);
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                contract.setCreateBy(sysUser.getUsername());
            }
            
            // 生成合同编号：HT + 日期 + 订单编号后6位
            String contractNo = "HT" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()) + 
                (order.getOrderNo() != null && order.getOrderNo().length() > 6 ? 
                    order.getOrderNo().substring(order.getOrderNo().length() - 6) : 
                    String.format("%06d", (int)(Math.random() * 1000000)));
            contract.setContractNo(contractNo);

            try {
                List<SysAuditTask> auditTasksForStaff = this.getTasksByOrderId(orderId);
                AccountingContractServiceStaffJsonUtil.applyDerivedToContract(contract, auditTasksForStaff, true);
            } catch (Exception ex) {
                log.warn("订单 {} 转合同时写入服务人员JSON失败: {}", orderId, ex.getMessage());
            }

            ghAccountingContractService.save(contract);
            log.info("订单 {} 已转为合同，合同编号: {}, 到期时间: {}", orderId, contractNo, contract.getExpireDate());
        } catch (Exception e) {
            log.error("从订单创建合同失败，订单ID: " + orderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 检查并转为地址
     */
    private void checkAndConvertToAddress(String orderId, SysAuditTask task, String auditData) {
        try {
            if (oConvertUtils.isEmpty(auditData)) {
                return;
            }
            
            // 获取当前步骤的表单配置
            SysAuditStepForm stepForm = sysAuditStepFormService.getFormByStepId(task.getStepId());
            if (stepForm == null) {
                return;
            }
            
            // 获取该步骤的所有指标
            List<SysAuditFormIndicator> indicators = sysAuditFormIndicatorService.getIndicatorsByStepFormId(stepForm.getId());
            if (indicators == null || indicators.isEmpty()) {
                return;
            }
            
            // 解析auditData
            JSONObject auditDataJson = JSONObject.parseObject(auditData);
            
            // 查找转为地址指标
            for (SysAuditFormIndicator indicator : indicators) {
                if ("convert_address".equals(indicator.getIndicatorType())) {
                    String indicatorKey = "indicator_" + indicator.getId();
                    if (auditDataJson.containsKey(indicatorKey)) {
                        String convertValue = auditDataJson.getString(indicatorKey);
                        if ("yes".equals(convertValue) || "true".equals(convertValue) || "1".equals(convertValue)) {
                            // 需要转为地址，传递表单配置和审核数据以便提取开始时间和结束时间
                            createAddressFromOrder(orderId, task, stepForm, auditDataJson);
                            log.info("订单 {} 已自动转为地址", orderId);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查并转为地址失败，订单ID: " + orderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 从订单创建地址中心记录（重载方法，兼容旧调用）
     */
    private void createAddressFromOrder(String orderId) {
        createAddressFromOrder(orderId, null, null, null);
    }
    
    /**
     * 从订单创建地址中心记录（支持从动态表单提取开始时间和结束时间）
     */
    private void createAddressFromOrder(String orderId, SysAuditTask task, SysAuditStepForm stepForm, JSONObject auditDataJson) {
        try {
            // 检查是否已经存在地址记录（通过订单ID关联）
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.address.entity.GhAddressCenter> addressWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            addressWrapper.eq(org.jeecg.modules.address.entity.GhAddressCenter::getContractId, orderId);
            
            org.jeecg.modules.address.entity.GhAddressCenter existingAddress = ghAddressCenterService.getOne(addressWrapper);
            if (existingAddress != null) {
                log.info("订单 {} 已存在地址记录，地址ID: {}", orderId, existingAddress.getId());
                return;
            }
            
            // 获取订单信息
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                log.error("订单不存在，订单ID: {}", orderId);
                return;
            }
            
            // 获取客户ID（如果订单已有companyId则使用，否则不设置，让地址记录独立存在）
            String companyId = order.getCompanyId();
            
            // 创建地址中心记录
            org.jeecg.modules.address.entity.GhAddressCenter addressCenter = 
                new org.jeecg.modules.address.entity.GhAddressCenter();
            addressCenter.setCompanyName(order.getCompanyName());
            addressCenter.setSalesman(order.getSalesman());
            if (oConvertUtils.isNotEmpty(companyId)) {
                addressCenter.setDataId(companyId); // 客户ID
            }
            addressCenter.setContractId(orderId); // 使用contractId字段存储订单ID
            addressCenter.setAddressStatus("1"); // 正常状态
            addressCenter.setRemarks("审批转为地址");
            addressCenter.setCreateTime(new Date());
            addressCenter.setDelFlag(0); // 正常状态
            
            // 设置服务人员（周期任务：使用最后一步审核的人员；其他任务：优先从任务中获取）
            String servicePerson = null;
            
            // 如果是周期任务（taskType='recurring'），获取最后一步审核的人员
            if (task != null && "recurring".equals(task.getTaskType())) {
                try {
                    // 获取该订单的所有审核任务，按步骤顺序倒序排列，取最后一步已审核的任务
                    List<SysAuditTask> allTasks = getTasksByOrderId(orderId);
                    if (allTasks != null && !allTasks.isEmpty()) {
                        // 过滤出已审核的任务，按步骤顺序倒序排列
                        SysAuditTask lastApprovedTask = allTasks.stream()
                            .filter(t -> "approved".equals(t.getTaskStatus()) || "approved".equals(t.getAuditResult()))
                            .filter(t -> !"loss".equals(t.getTaskType())) // 排除流失审核任务
                            .sorted((t1, t2) -> {
                                // 按步骤顺序倒序排列（最后一步在前）
                                int stepOrder1 = t1.getStepOrder() != null ? t1.getStepOrder() : 0;
                                int stepOrder2 = t2.getStepOrder() != null ? t2.getStepOrder() : 0;
                                if (stepOrder2 != stepOrder1) {
                                    return Integer.compare(stepOrder2, stepOrder1); // 倒序
                                }
                                // 如果步骤顺序相同，按审核时间倒序排列
                                if (t1.getAuditTime() != null && t2.getAuditTime() != null) {
                                    return t2.getAuditTime().compareTo(t1.getAuditTime());
                                }
                                return 0;
                            })
                            .findFirst()
                            .orElse(null);
                        
                        if (lastApprovedTask != null) {
                            // 优先使用审核人，如果没有则使用分配人
                            servicePerson = lastApprovedTask.getAuditUserName();
                            if (oConvertUtils.isEmpty(servicePerson)) {
                                servicePerson = lastApprovedTask.getAssignedUserName();
                            }
                            if (oConvertUtils.isNotEmpty(servicePerson)) {
                                log.info("订单 {} 地址服务人员从最后一步审核任务中提取: {} (步骤: {}, 任务ID: {})", 
                                    orderId, servicePerson, lastApprovedTask.getStepOrder(), lastApprovedTask.getId());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取最后一步审核人员失败: " + e.getMessage(), e);
                }
            }
            
            // 如果不是周期任务，或者周期任务中没有找到，使用原来的逻辑
            if (oConvertUtils.isEmpty(servicePerson)) {
            if (task != null && oConvertUtils.isNotEmpty(task.getServiceUserName())) {
                servicePerson = task.getServiceUserName();
                log.info("订单 {} 地址服务人员从任务中提取: {}", orderId, servicePerson);
            } else if (task != null) {
                // 尝试从订单步骤中获取服务人员
                try {
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.order.entity.GhOrderStep> stepWrapper = 
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                    stepWrapper.eq(org.jeecg.modules.order.entity.GhOrderStep::getOrderId, orderId);
                        stepWrapper.orderByDesc(org.jeecg.modules.order.entity.GhOrderStep::getStepOrder); // 改为倒序，获取最后一步
                    stepWrapper.last("LIMIT 1");
                        org.jeecg.modules.order.entity.GhOrderStep lastStep = ghOrderStepService.getOne(stepWrapper);
                        if (lastStep != null && oConvertUtils.isNotEmpty(lastStep.getOperatorName())) {
                            servicePerson = lastStep.getOperatorName();
                            log.info("订单 {} 地址服务人员从订单最后一步中提取: {}", orderId, servicePerson);
                    }
                } catch (Exception e) {
                    log.debug("从订单步骤获取服务人员失败: " + e.getMessage());
                }
            }
            }
            
            // 如果从步骤中没有获取到，使用订单的业务员
            if (oConvertUtils.isEmpty(servicePerson) && oConvertUtils.isNotEmpty(order.getSalesman())) {
                servicePerson = order.getSalesman();
                log.info("订单 {} 地址服务人员使用订单业务员: {}", orderId, servicePerson);
            }
            if (oConvertUtils.isNotEmpty(servicePerson)) {
                addressCenter.setAdvisor(servicePerson); // 设置服务人员（财税顾问字段）
            }
            
            // 优先从任务中获取起始月份和截至月份（从订单表单数据中提取的）
            Date startDate = null;
            Date endDate = null;
            if (task != null) {
                // 优先使用任务中提取的起始月份
                if (oConvertUtils.isNotEmpty(task.getStartMonth())) {
                    startDate = parseDate(task.getStartMonth());
                    log.info("订单 {} 地址起始日期从任务起始月份提取: {} -> {}", orderId, task.getStartMonth(), startDate);
                }
                // 优先使用任务中提取的截至月份
                if (oConvertUtils.isNotEmpty(task.getEndMonth())) {
                    endDate = parseDate(task.getEndMonth());
                    log.info("订单 {} 地址终止日期从任务截至月份提取: {} -> {}", orderId, task.getEndMonth(), endDate);
                }
            }
            
            // 如果从任务中没有获取到，再从动态表单中提取开始时间和结束时间
            if ((startDate == null || endDate == null) && stepForm != null && auditDataJson != null && oConvertUtils.isNotEmpty(stepForm.getFormConfig())) {
                try {
                    JSONObject formConfig = JSONObject.parseObject(stepForm.getFormConfig());
                    if (formConfig != null && formConfig.containsKey("fields")) {
                        com.alibaba.fastjson.JSONArray fields = formConfig.getJSONArray("fields");
                        if (fields != null && !fields.isEmpty()) {
                            for (int i = 0; i < fields.size(); i++) {
                                JSONObject field = fields.getJSONObject(i);
                                String fieldName = field.getString("name");
                                String fieldLabel = field.getString("label");
                                String fieldType = field.getString("type");
                                
                                // 处理日期类型和月份类型的字段（月份类型也需要处理）
                                if (("date".equals(fieldType) || "month".equals(fieldType)) && oConvertUtils.isNotEmpty(fieldName)) {
                                    if (auditDataJson.containsKey(fieldName)) {
                                        String dateValue = auditDataJson.getString(fieldName);
                                        if (oConvertUtils.isNotEmpty(dateValue)) {
                                            // 判断是开始时间还是结束时间（根据label关键词匹配，优先匹配label）
                                            if (fieldLabel != null) {
                                                String labelLower = fieldLabel.toLowerCase().trim();
                                                String nameLower = fieldName != null ? fieldName.toLowerCase().trim() : "";
                                                
                                                // 匹配开始时间的关键词（地址的起始日期）
                                                if (labelLower.contains("开始") || labelLower.contains("起始") || 
                                                    labelLower.equals("开始时间") || labelLower.equals("起始日期") ||
                                                    labelLower.equals("起始时间") || labelLower.contains("开始日期") ||
                                                    nameLower.contains("start") || nameLower.contains("begin")) {
                                                    startDate = parseDate(dateValue);
                                                    log.debug("地址：匹配到开始时间字段: {} = {}", fieldLabel, dateValue);
                                                } 
                                                // 匹配结束时间的关键词（地址的终止日期）
                                                else if (labelLower.contains("结束") || labelLower.contains("终止") || 
                                                         labelLower.contains("到期") || labelLower.equals("结束时间") ||
                                                         labelLower.equals("终止日期") || labelLower.equals("终止时间") ||
                                                         labelLower.equals("到期时间") || labelLower.contains("结束日期") ||
                                                         labelLower.contains("到期日期") || nameLower.contains("end") || 
                                                         nameLower.contains("expire") || nameLower.contains("terminate")) {
                                                    endDate = parseDate(dateValue);
                                                    log.debug("地址：匹配到结束时间字段: {} = {}", fieldLabel, dateValue);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("从表单配置中提取日期字段失败: " + e.getMessage());
                }
                
                // 如果从表单配置中还是没有提取到，直接从auditDataJson中查找包含"结束月份"关键词的字段
                if (endDate == null && auditDataJson != null) {
                    try {
                        for (String key : auditDataJson.keySet()) {
                            Object value = auditDataJson.get(key);
                            if (value == null) {
                                continue;
                            }
                            String valueStr = value.toString();
                            String keyLower = key.toLowerCase().trim();
                            
                            // 匹配结束月份字段（不限制字段类型，直接从审核数据中查找）
                            if ((keyLower.contains("结束") || keyLower.contains("终止") || 
                                 keyLower.contains("到期") || keyLower.contains("end") ||
                                 keyLower.contains("expire") || keyLower.contains("terminate")) &&
                                (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期") ||
                                 keyLower.contains("month") || keyLower.contains("date") || keyLower.contains("time")) &&
                                oConvertUtils.isNotEmpty(valueStr)) {
                                // 检查是否是月份格式 YYYY-MM 或日期格式
                                if (valueStr.matches("^\\d{4}-\\d{2}$") || valueStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                                    endDate = parseDate(valueStr);
                                    log.info("地址：从审核数据中直接提取到结束月份字段: {} = {} -> {}", key, valueStr, endDate);
                                    break; // 找到第一个匹配的就退出
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("从审核数据中直接提取结束月份字段失败: " + e.getMessage());
                    }
                }
                
                // 如果从表单配置中还是没有提取到开始日期，直接从auditDataJson中查找
                if (startDate == null && auditDataJson != null) {
                    try {
                        for (String key : auditDataJson.keySet()) {
                            Object value = auditDataJson.get(key);
                            if (value == null) {
                                continue;
                            }
                            String valueStr = value.toString();
                            String keyLower = key.toLowerCase().trim();
                            
                            // 匹配开始月份字段（不限制字段类型，直接从审核数据中查找）
                            if ((keyLower.contains("开始") || keyLower.contains("起始") || 
                                 keyLower.contains("start") || keyLower.contains("begin")) &&
                                (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期") ||
                                 keyLower.contains("month") || keyLower.contains("date") || keyLower.contains("time")) &&
                                oConvertUtils.isNotEmpty(valueStr)) {
                                // 检查是否是月份格式 YYYY-MM 或日期格式
                                if (valueStr.matches("^\\d{4}-\\d{2}$") || valueStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                                    startDate = parseDate(valueStr);
                                    log.info("地址：从审核数据中直接提取到开始月份字段: {} = {} -> {}", key, valueStr, startDate);
                                    break; // 找到第一个匹配的就退出
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("从审核数据中直接提取开始月份字段失败: " + e.getMessage());
                    }
                }
            }
            
            // 如果从任务和审核数据中都没有提取到，直接从订单表单数据中提取"开始月份"和"结束月份"
            if ((startDate == null || endDate == null) && ghOrderFormDataService != null) {
                try {
                    org.jeecg.modules.order.entity.GhOrderFormData formData = ghOrderFormDataService.getFormDataByOrderId(orderId);
                    if (formData != null && oConvertUtils.isNotEmpty(formData.getFormData())) {
                        JSONObject dynamicFormData = JSONObject.parseObject(formData.getFormData());
                        if (dynamicFormData != null && !dynamicFormData.isEmpty()) {
                            // 遍历表单数据，查找"开始月份"和"结束月份"字段
                            for (String key : dynamicFormData.keySet()) {
                                Object value = dynamicFormData.get(key);
                                if (value == null) {
                                    continue;
                                }
                                String valueStr = value.toString();
                                String keyLower = key.toLowerCase().trim();
                                
                                // 匹配开始月份字段
                                if (startDate == null &&
                                    (keyLower.contains("开始") || keyLower.contains("起始") || 
                                     keyLower.contains("start") || keyLower.contains("begin")) &&
                                    (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期") ||
                                     keyLower.contains("month") || keyLower.contains("date") || keyLower.contains("time")) &&
                                    oConvertUtils.isNotEmpty(valueStr)) {
                                    // 检查是否是月份格式 YYYY-MM 或日期格式
                                    if (valueStr.matches("^\\d{4}-\\d{2}$") || valueStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                                        startDate = parseDate(valueStr);
                                        log.info("地址：从订单表单数据中直接提取到开始月份字段: {} = {} -> {}", key, valueStr, startDate);
                                    }
                                }
                                
                                // 匹配结束月份字段
                                if (endDate == null &&
                                    (keyLower.contains("结束") || keyLower.contains("终止") || 
                                     keyLower.contains("到期") || keyLower.contains("end") ||
                                     keyLower.contains("expire") || keyLower.contains("terminate")) &&
                                    (keyLower.contains("月份") || keyLower.contains("时间") || keyLower.contains("日期") ||
                                     keyLower.contains("month") || keyLower.contains("date") || keyLower.contains("time")) &&
                                    oConvertUtils.isNotEmpty(valueStr)) {
                                    // 检查是否是月份格式 YYYY-MM 或日期格式
                                    if (valueStr.matches("^\\d{4}-\\d{2}$") || valueStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                                        endDate = parseDate(valueStr);
                                        log.info("地址：从订单表单数据中直接提取到结束月份字段: {} = {} -> {}", key, valueStr, endDate);
                                    }
                                }
                                
                                // 如果两个都找到了，可以提前退出
                                if (startDate != null && endDate != null) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("从订单表单数据中提取开始/结束月份字段失败: " + e.getMessage());
                }
            }
            
            // 设置起始日期和终止日期
            if (startDate != null) {
                addressCenter.setStartDate(startDate);
                log.info("订单 {} 地址起始日期从表单中提取: {}", orderId, startDate);
            }
            if (endDate != null) {
                addressCenter.setTerminationDate(endDate);
                log.info("订单 {} 地址终止日期从表单中提取: {}", orderId, endDate);
            }
            
            // 如果订单有合同金额，设置合同金额
            if (order.getContractAmount() != null) {
                addressCenter.setContractAmount(order.getContractAmount().intValue());
            } else if (order.getOrderAmount() != null) {
                addressCenter.setContractAmount(order.getOrderAmount().intValue());
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                addressCenter.setCreateBy(sysUser.getUsername());
            }
            
            ghAddressCenterService.save(addressCenter);
            log.info("订单 {} 已转为地址，地址ID: {}, 起始日期: {}, 终止日期: {}", 
                orderId, addressCenter.getId(), addressCenter.getStartDate(), addressCenter.getTerminationDate());
        } catch (Exception e) {
            log.error("从订单创建地址失败，订单ID: " + orderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 解析日期字符串为Date对象
     * 支持多种日期格式：yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, yyyy-MM（月份格式）等
     */
    private Date parseDate(String dateValue) {
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
            log.warn("解析日期失败，日期值: " + dateValue + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从订单记录创建银行日记记录（订单审核完成时）
     */
    private void createBankDiaryFromOrder(GhOrder order) {
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
        String feeDetail = "订单审核通过";
        if (oConvertUtils.isNotEmpty(order.getOrderNo())) {
            feeDetail += " - " + order.getOrderNo();
        }
        if (oConvertUtils.isNotEmpty(order.getCompanyName())) {
            feeDetail += " - " + order.getCompanyName();
        }
        // 获取业务类型名称
        if (oConvertUtils.isNotEmpty(order.getBusinessType()) && sysDictService != null) {
            try {
                String businessTypeName = sysDictService.queryDictTextByKey("sys_category", order.getBusinessType());
                if (oConvertUtils.isNotEmpty(businessTypeName)) {
                    feeDetail += " - " + businessTypeName;
                }
            } catch (Exception e) {
                log.debug("获取业务类型名称失败", e);
            }
        }
        bankDiary.setFeeDetail(feeDetail);
        
        // 收入金额（订单审核通过是收入）
        bankDiary.setIncomeAmount(orderAmount);
        bankDiary.setExpenseAmount(BigDecimal.ZERO);
        
        // 银行账户（从订单收款账号获取，如果没有则留空）
        // 注意：订单审核通过时可能还没有收款，所以银行账户可能为空
        
        // 操作人员
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null) {
            bankDiary.setOperatorId(sysUser.getId());
            bankDiary.setOperatorName(sysUser.getRealname());
            bankDiary.setCreateBy(sysUser.getUsername());
        }
        
        // 公司信息
        bankDiary.setCompanyId(order.getCompanyId());
        bankDiary.setCompanyName(order.getCompanyName());
        
        // 备注
        bankDiary.setRemarks("订单审核通过自动创建");
        
        bankDiary.setCreateTime(new Date());
        bankDiary.setDelFlag(0);
        
        // 计算结余金额（如果银行账户为空，结余金额也为空）
        // 注意：订单审核通过时可能还没有确定收款账号，所以这里不计算结余金额
        // 如果后续有收款记录，可以在收款时更新银行日记
        
        ghBankDiaryService.save(bankDiary);
        log.info("为订单ID {} 创建了银行日记记录", order.getId());
    }
    
    /**
     * 第一步驳回时的回退处理：废除收费记录、扣除资金日记、平账
     * @param orderId 订单ID
     */
    private void rollbackFirstStepRejection(String orderId) {
        if (oConvertUtils.isEmpty(orderId)) {
            return;
        }
        
        log.info("开始处理第一步驳回回退，订单ID: {}", orderId);
        
        // 1. 废除收费记录（逻辑删除或状态改为废除）
        if (ghOrderPaymentService != null) {
            try {
                QueryWrapper<org.jeecg.modules.order.entity.GhOrderPayment> paymentWrapper = new QueryWrapper<>();
                paymentWrapper.eq("order_id", orderId);
                paymentWrapper.eq("del_flag", 0);
                List<org.jeecg.modules.order.entity.GhOrderPayment> payments = ghOrderPaymentService.list(paymentWrapper);
                
                if (payments != null && !payments.isEmpty()) {
                    for (org.jeecg.modules.order.entity.GhOrderPayment payment : payments) {
                        // 逻辑删除收费记录
                        payment.setDelFlag(1);
                        payment.setUpdateTime(new Date());
                        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                        if (sysUser != null) {
                            payment.setUpdateBy(sysUser.getUsername());
                        }
                        ghOrderPaymentService.updateById(payment);
                        log.info("废除收费记录，订单ID: {}, 收费记录ID: {}", orderId, payment.getId());
                    }
                }
            } catch (Exception e) {
                log.error("废除收费记录失败，订单ID: {}", orderId, e);
            }
        }
        
        // 2. 查找并删除资金日记收入记录
        if (ghBankDiaryService != null) {
            try {
                QueryWrapper<GhBankDiary> diaryWrapper = new QueryWrapper<>();
                diaryWrapper.eq("order_id", orderId);
                diaryWrapper.eq("business_type", "订单收入");
                diaryWrapper.eq("del_flag", 0);
                List<GhBankDiary> bankDiaries = ghBankDiaryService.list(diaryWrapper);
                
                if (bankDiaries != null && !bankDiaries.isEmpty()) {
                    for (GhBankDiary bankDiary : bankDiaries) {
                        BigDecimal incomeAmount = bankDiary.getIncomeAmount();
                        String bankAccountId = bankDiary.getBankAccountId();
                        Date orderDate = bankDiary.getOrderDate();
                        
                        // 逻辑删除资金日记记录
                        bankDiary.setDelFlag(1);
                        bankDiary.setUpdateTime(new Date());
                        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                        if (sysUser != null) {
                            bankDiary.setUpdateBy(sysUser.getUsername());
                        }
                        ghBankDiaryService.updateById(bankDiary);
                        log.info("删除资金日记收入记录，订单ID: {}, 资金日记ID: {}, 收入金额: {}", 
                                orderId, bankDiary.getId(), incomeAmount);
                        
                        // 3. 如果已有收入，创建一条支出记录来平账
                        if (incomeAmount != null && incomeAmount.compareTo(BigDecimal.ZERO) > 0 && 
                            oConvertUtils.isNotEmpty(bankAccountId)) {
                            try {
                                // 创建平账支出记录
                                GhBankDiary offsetDiary = new GhBankDiary();
                                offsetDiary.setOrderDate(new Date());
                                offsetDiary.setBusinessType("订单驳回平账");
                                offsetDiary.setBusinessId(orderId);
                                offsetDiary.setOrderId(orderId);
                                offsetDiary.setFeeDetail("订单第一步驳回平账 - 订单ID: " + orderId);
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
                                offsetDiary.setRemarks("订单第一步驳回平账，原收入金额: " + incomeAmount);
                                
                                offsetDiary.setCreateTime(new Date());
                                offsetDiary.setDelFlag(0);
                                
                                // 计算结余金额
                                BigDecimal balanceAmount = calculateBankDiaryBalance(bankAccountId, offsetDiary.getOrderDate());
                                if (offsetDiary.getExpenseAmount() != null) {
                                    balanceAmount = balanceAmount.subtract(offsetDiary.getExpenseAmount());
                                }
                                offsetDiary.setBalanceAmount(balanceAmount);
                                
                                ghBankDiaryService.save(offsetDiary);
                                log.info("创建平账支出记录，订单ID: {}, 支出金额: {}", orderId, incomeAmount);
                                
                                // 更新后续记录的结余金额
                                updateSubsequentBankDiaryBalances(bankAccountId, offsetDiary.getOrderDate());
                            } catch (Exception e) {
                                log.error("创建平账支出记录失败，订单ID: {}", orderId, e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("处理资金日记回退失败，订单ID: {}", orderId, e);
            }
        }
        
        log.info("第一步驳回回退处理完成，订单ID: {}", orderId);
    }
    
    /**
     * 计算指定账户在指定日期之前的结余金额
     */
    private BigDecimal calculateBankDiaryBalance(String bankAccountId, Date orderDate) {
        if (ghBankDiaryService == null || oConvertUtils.isEmpty(bankAccountId)) {
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
                BigDecimal previousBalance = calculateBankDiaryBalance(bankAccountId, orderDate);
                
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
     * 删除从指定步骤开始的所有任务
     * @param orderId 订单ID
     * @param fromStepOrder 从哪个步骤开始删除
     */
    private void deleteTasksFromStep(String orderId, Integer fromStepOrder) {
        if (oConvertUtils.isEmpty(orderId) || fromStepOrder == null) {
            return;
        }
        
        try {
            LambdaQueryWrapper<SysAuditTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysAuditTask::getOrderId, orderId);
            wrapper.ge(SysAuditTask::getStepOrder, fromStepOrder);
            wrapper.eq(SysAuditTask::getDelFlag, 0);
            
            List<SysAuditTask> tasksToDelete = this.list(wrapper);
            if (tasksToDelete != null && !tasksToDelete.isEmpty()) {
                for (SysAuditTask task : tasksToDelete) {
                    task.setDelFlag(1);
                    task.setUpdateTime(new Date());
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        task.setUpdateBy(sysUser.getUsername());
                    }
                    this.updateById(task);
                }
                log.info("删除了订单 {} 从步骤 {} 开始的所有任务，共 {} 条", orderId, fromStepOrder, tasksToDelete.size());
            }
        } catch (Exception e) {
            log.error("删除任务失败，订单ID: {}, 步骤: {}", orderId, fromStepOrder, e);
        }
    }
    
    /**
     * 重新创建第一步的任务
     * @param order 订单对象
     */
    private void recreateFirstStepTasks(GhOrder order) {
        if (order == null || oConvertUtils.isEmpty(order.getMatchedProcessId())) {
            return;
        }
        
        try {
            SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(order.getMatchedProcessId());
            if (process == null || process.getSteps() == null || process.getSteps().isEmpty()) {
                log.error("流程不存在或没有步骤，流程ID: {}", order.getMatchedProcessId());
                return;
            }
            
            // 获取第一步的所有步骤（支持并行审核）
            List<SysAuditStep> firstSteps = process.getSteps().stream()
                .filter(step -> step.getStepOrder() != null && step.getStepOrder() == 1)
                .collect(Collectors.toList());
            
            if (firstSteps.isEmpty()) {
                log.error("流程没有第一步，流程ID: {}", order.getMatchedProcessId());
                return;
            }
            
            String taskType = order.getTaskType();
            if (oConvertUtils.isEmpty(taskType)) {
                taskType = "once";
            }
            
            // 为第一步的每个角色创建审核任务
            for (SysAuditStep step : firstSteps) {
                try {
                    SysAuditTask task = createTask(
                        order.getId(),
                        order.getMatchedProcessId(),
                        step.getId(),
                        step.getStepOrder(),
                        taskType
                    );
                    log.info("重新创建第一步审核任务，任务ID: {}, 订单ID: {}, 步骤ID: {}", 
                            task.getId(), order.getId(), step.getId());
                } catch (Exception e) {
                    log.error("重新创建第一步审核任务失败，订单ID: {}, 步骤ID: {}", order.getId(), step.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("重新创建第一步任务失败，订单ID: {}", order.getId(), e);
        }
    }
    
    /**
     * 重新创建指定步骤的任务（用于驳回回退）
     * @param order 订单对象
     * @param stepOrder 步骤顺序
     */
    private void recreateStepTasks(GhOrder order, Integer stepOrder) {
        if (order == null || oConvertUtils.isEmpty(order.getMatchedProcessId()) || stepOrder == null || stepOrder < 1) {
            return;
        }
        
        try {
            // 检查该步骤的任务是否已存在
            List<SysAuditTask> existingTasks = getTasksByOrderId(order.getId());
            boolean hasExistingTask = existingTasks.stream()
                .anyMatch(t -> t.getStepOrder() != null && t.getStepOrder().equals(stepOrder) && 
                              (t.getDelFlag() == null || t.getDelFlag() == 0));
            
            if (hasExistingTask) {
                log.info("订单 {} 的步骤 {} 任务已存在，跳过重新创建", order.getId(), stepOrder);
                return;
            }
            
            SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(order.getMatchedProcessId());
            if (process == null || process.getSteps() == null || process.getSteps().isEmpty()) {
                log.error("流程不存在或没有步骤，流程ID: {}", order.getMatchedProcessId());
                return;
            }
            
            // 获取指定步骤的所有步骤（支持并行审核）
            List<SysAuditStep> targetSteps = process.getSteps().stream()
                .filter(step -> step.getStepOrder() != null && step.getStepOrder().equals(stepOrder))
                .collect(Collectors.toList());
            
            if (targetSteps.isEmpty()) {
                log.warn("流程没有步骤 {}，流程ID: {}", stepOrder, order.getMatchedProcessId());
                return;
            }
            
            String taskType = order.getTaskType();
            if (oConvertUtils.isEmpty(taskType)) {
                taskType = "once";
            }
            
            // 为指定步骤的每个角色创建审核任务
            for (SysAuditStep step : targetSteps) {
                try {
                    SysAuditTask task = createTask(
                        order.getId(),
                        order.getMatchedProcessId(),
                        step.getId(),
                        step.getStepOrder(),
                        taskType
                    );
                    log.info("重新创建步骤 {} 审核任务，任务ID: {}, 订单ID: {}, 步骤ID: {}", 
                            stepOrder, task.getId(), order.getId(), step.getId());
                } catch (Exception e) {
                    log.error("重新创建步骤 {} 审核任务失败，订单ID: {}, 步骤ID: {}", 
                            stepOrder, order.getId(), step.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("重新创建步骤 {} 任务失败，订单ID: {}", stepOrder, order.getId(), e);
        }
    }
    
    /**
     * 订单完成时，为所有成本支出创建报销记录
     * @param orderId 订单ID
     */
    public void createReimbursementsForCompletedOrder(String orderId) {
        if (oConvertUtils.isEmpty(orderId) || ghReimbursementService == null) {
            return;
        }
        
        try {
            // 获取该订单的所有审批任务
            List<SysAuditTask> tasks = getTasksByOrderId(orderId);
            if (tasks == null || tasks.isEmpty()) {
                return;
            }
            
            // 遍历所有任务，收集成本数据并创建报销记录
            for (SysAuditTask task : tasks) {
                List<SysAuditTaskCost> costs = sysAuditTaskCostService.getCostsByTaskId(task.getId());
                if (costs == null || costs.isEmpty()) {
                    continue;
                }
                
                // 为每个成本记录创建报销记录（报销记录创建时会先创建支出记录）
                for (SysAuditTaskCost cost : costs) {
                    if (cost.getAmount() == null || cost.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    
                    try {
                        // createReimbursementFromCost 方法内部会先调用 createExpenseFromCost 创建支出记录
                        // 然后将支出ID保存到报销记录中
                        createReimbursementFromCost(orderId, cost, task);
                    } catch (Exception e) {
                        log.error("订单完成时创建报销记录失败，订单ID: {}, 成本ID: {}", orderId, cost.getId(), e);
                        // 不抛出异常，避免影响主流程
                    }
                }
            }
        } catch (Exception e) {
            log.error("订单完成时创建报销记录失败，订单ID: " + orderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 从成本自动生成报销记录（待审核状态）
     * 先创建支出记录，然后将支出ID保存到报销记录中
     * @param orderId 订单ID
     * @param cost 成本记录
     * @param task 审批任务
     */
    private void createReimbursementFromCost(String orderId, SysAuditTaskCost cost, SysAuditTask task) {
        if (ghReimbursementService == null || ghOrderExpenseService == null || oConvertUtils.isEmpty(orderId) || cost == null) {
            return;
        }
        
        // 检查是否已存在该成本对应的报销记录
        QueryWrapper<org.jeecg.modules.reimbursement.entity.GhReimbursement> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("remarks", "任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId());
        queryWrapper.eq("del_flag", 0);
        long count = ghReimbursementService.count(queryWrapper);
        if (count > 0) {
            log.info("成本已存在报销记录，跳过创建，任务ID: {}, 指标ID: {}", task.getId(), cost.getIndicatorId());
            return;
        }
        
        try {
            // 获取订单信息
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                log.warn("订单不存在，无法创建报销记录，订单ID: {}", orderId);
                return;
            }
            
            // 先创建支出记录
            String expenseId = createExpenseFromCost(orderId, cost, task);
            if (oConvertUtils.isEmpty(expenseId)) {
                log.warn("创建支出记录失败，无法创建报销记录，订单ID: {}, 成本ID: {}", orderId, cost.getId());
                return;
            }
            
            // 创建报销记录
            org.jeecg.modules.reimbursement.entity.GhReimbursement reimbursement = 
                new org.jeecg.modules.reimbursement.entity.GhReimbursement();
            
            // 基本信息
            reimbursement.setReimbursementName(cost.getExpenseName() != null ? cost.getExpenseName() : "审批成本");
            reimbursement.setReimbursementTime(cost.getCreateTime() != null ? cost.getCreateTime() : new Date());
            // 使用任务（task）对应步骤的操作人作为报销人员
            // 优先使用任务审核人，如果没有则查询订单步骤表中对应步骤的操作人
            String reimbursementPerson = null;
            if (task != null && oConvertUtils.isNotEmpty(task.getAuditUserName())) {
                // 优先使用任务的审核人
                reimbursementPerson = task.getAuditUserName();
            } else if (task != null && oConvertUtils.isNotEmpty(task.getAssignedUserName())) {
                // 其次使用任务的指定审核人
                reimbursementPerson = task.getAssignedUserName();
            } else if (task != null && task.getStepOrder() != null && ghOrderStepService != null) {
                // 如果任务有步骤顺序，查询订单步骤表中对应步骤的操作人
                try {
                    LambdaQueryWrapper<org.jeecg.modules.order.entity.GhOrderStep> stepWrapper = 
                        new LambdaQueryWrapper<>();
                    stepWrapper.eq(org.jeecg.modules.order.entity.GhOrderStep::getOrderId, orderId);
                    stepWrapper.eq(org.jeecg.modules.order.entity.GhOrderStep::getStepOrder, task.getStepOrder());
                    stepWrapper.eq(org.jeecg.modules.order.entity.GhOrderStep::getStatus, "1"); // 已完成
                    stepWrapper.orderByDesc(org.jeecg.modules.order.entity.GhOrderStep::getCreateTime);
                    stepWrapper.last("LIMIT 1");
                    org.jeecg.modules.order.entity.GhOrderStep orderStep = ghOrderStepService.getOne(stepWrapper);
                    if (orderStep != null && oConvertUtils.isNotEmpty(orderStep.getOperatorName())) {
                        reimbursementPerson = orderStep.getOperatorName();
                    }
                } catch (Exception e) {
                    log.debug("查询订单步骤操作人失败，订单ID: {}, 步骤顺序: {}", orderId, task.getStepOrder(), e);
                }
            }
            // 如果以上都没有找到，使用订单业务员作为兜底
            if (oConvertUtils.isEmpty(reimbursementPerson)) {
                reimbursementPerson = order.getSalesman();
            }
            reimbursement.setReimbursementPerson(reimbursementPerson);
            reimbursement.setUnitPrice(cost.getAmount() != null ? cost.getAmount().doubleValue() : 0.0);
            reimbursement.setNum(1.0);
            reimbursement.setTotalPrice(cost.getAmount() != null ? cost.getAmount().doubleValue() : 0.0);
            
            // 报销类目：直接使用成本记录中的报销类目ID
            String categoryId = cost.getCategoryId();
            // 如果报销类目ID为空，尝试根据报销类目名称匹配
            if (oConvertUtils.isEmpty(categoryId) && reimbursementCategoryUtil != null) {
                try {
                    String categoryName = cost.getCategoryName();
                    if (oConvertUtils.isNotEmpty(categoryName)) {
                        if (categoryName.contains("地址") || categoryName.contains("地址费用")) {
                            categoryId = reimbursementCategoryUtil.getAddressCategoryId();
                        } else if (categoryName.contains("刻章") || categoryName.contains("印章")) {
                            categoryId = reimbursementCategoryUtil.getSealCategoryId();
                        } else if (categoryName.contains("商标")) {
                            categoryId = reimbursementCategoryUtil.getTrademarkCategoryId();
                        } else if (categoryName.contains("审计") || categoryName.contains("审批成本")) {
                            categoryId = reimbursementCategoryUtil.getAuditCategoryId();
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取报销类目ID失败", e);
                }
            }
            reimbursement.setCategory(categoryId);
            
            // 公司信息
            reimbursement.setCompanyId(order.getCompanyId());
            reimbursement.setCompanyName(order.getCompanyName());
            
            // 订单关联信息
            reimbursement.setContractNo(order.getOrderNo());
            if (order.getContractAmount() != null) {
                reimbursement.setContractAmount(order.getContractAmount().toString());
            }
            
            // 备注：记录关联的任务和成本信息
            String remarks = "订单成本自动生成 - " + (cost.getRemark() != null ? cost.getRemark() : "") +
                " [任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId() + "]";
            reimbursement.setRemarks(remarks);
            
            // 保存支出记录ID
            reimbursement.setOrderExpenseId(expenseId);
            
            // 状态：待审核（auditFlag = "0"）
            reimbursement.setAuditFlag("0");
            
            // 创建信息
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                reimbursement.setCreateBy(sysUser.getUsername());
            }
            reimbursement.setCreateTime(new Date());
            reimbursement.setDelFlag(0);
            
            // 保存报销记录
            ghReimbursementService.save(reimbursement);
            log.info("自动生成报销记录，订单ID: {}, 报销ID: {}, 支出ID: {}, 金额: {}, 类目: {}", 
                    orderId, reimbursement.getId(), expenseId, reimbursement.getTotalPrice(), categoryId);
        } catch (Exception e) {
            log.error("创建报销记录失败，订单ID: {}, 成本ID: {}", orderId, cost.getId(), e);
        }
    }
    
    /**
     * 从成本创建支出记录（订单完成时）
     * @param orderId 订单ID
     * @param cost 成本记录
     * @param task 审批任务
     * @return 支出记录ID，如果创建失败返回null
     */
    private String createExpenseFromCost(String orderId, SysAuditTaskCost cost, SysAuditTask task) {
        if (ghOrderExpenseService == null || oConvertUtils.isEmpty(orderId) || cost == null) {
            return null;
        }
        
        try {
            // 检查是否已存在该成本对应的支出记录
            String expenseRemark = (cost.getExpenseName() != null ? cost.getExpenseName() : "") + 
                (cost.getRemark() != null && !cost.getRemark().isEmpty() ? " - " + cost.getRemark() : "") +
                " [任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId() + "]";
            
            // 获取报销类目名称（优先使用成本记录中的报销类目名称）
            String reimbursementCategoryName = cost.getCategoryName();
            // 如果报销类目名称为空，根据报销类目ID查询名称
            if (oConvertUtils.isEmpty(reimbursementCategoryName) && oConvertUtils.isNotEmpty(cost.getCategoryId()) && sysCategoryService != null) {
                try {
                    org.jeecg.modules.system.entity.SysCategory category = sysCategoryService.getById(cost.getCategoryId());
                    if (category != null && oConvertUtils.isNotEmpty(category.getName())) {
                        reimbursementCategoryName = category.getName();
                    }
                } catch (Exception e) {
                    log.debug("根据报销类目ID查询名称失败，类目ID: {}", cost.getCategoryId(), e);
                }
            }
            // 如果还是没有，使用默认值
            if (oConvertUtils.isEmpty(reimbursementCategoryName)) {
                reimbursementCategoryName = "审批成本";
            }
            
            LambdaQueryWrapper<GhOrderExpense> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GhOrderExpense::getOrderId, orderId);
            wrapper.eq(GhOrderExpense::getCategory, reimbursementCategoryName);
            wrapper.like(GhOrderExpense::getRemarks, "任务ID:" + task.getId() + ",指标ID:" + cost.getIndicatorId());
            wrapper.eq(GhOrderExpense::getDelFlag, 0);
            
            GhOrderExpense existingExpense = ghOrderExpenseService.getOne(wrapper);
            
            if (existingExpense == null) {
                // 创建新的支出记录（待审核状态）
                GhOrderExpense expense = new GhOrderExpense();
                expense.setOrderId(orderId);
                expense.setExpenseTime(cost.getCreateTime() != null ? cost.getCreateTime() : new Date());
                expense.setAmount(cost.getAmount());
                expense.setCategory(reimbursementCategoryName); // 使用报销类目名称
                expense.setRemarks(expenseRemark);
                expense.setAuditStatus("0"); // 待审核
                expense.setPaymentAccount(null); // 支付账户待出纳审核时填写
                expense.setDelFlag(0);
                expense.setCreateTime(new Date());
                
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    expense.setCreateBy(sysUser.getUsername());
                }
                
                ghOrderExpenseService.save(expense);
                log.info("订单完成时创建支出记录（待审核），订单ID: {}, 支出ID: {}, 金额: {}, 类目: {}", 
                    orderId, expense.getId(), cost.getAmount(), expense.getCategory());
                return expense.getId();
            } else {
                // 更新现有支出记录（确保状态为待审核）
                if (!"0".equals(existingExpense.getAuditStatus())) {
                    existingExpense.setAuditStatus("0"); // 确保状态为待审核
                    existingExpense.setUpdateTime(new Date());
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        existingExpense.setUpdateBy(sysUser.getUsername());
                    }
                    ghOrderExpenseService.updateById(existingExpense);
                    log.info("订单完成时更新支出记录状态为待审核，订单ID: {}, 支出ID: {}", orderId, existingExpense.getId());
                }
                return existingExpense.getId();
            }
        } catch (Exception e) {
            log.error("创建支出记录失败，订单ID: {}, 成本ID: {}", orderId, cost.getId(), e);
            return null;
        }
    }
    
    /**
     * 获取审核任务的审核人用户名列表
     * 只有指定了审核人才返回，否则返回空列表（不通知角色的所有人）
     */
    private List<String> getTaskAuditors(SysAuditTask task) {
        try {
            List<String> auditors = new ArrayList<>();
            
            // 只有指定了审核人，才发送通知
            if (oConvertUtils.isNotEmpty(task.getAssignedUserId())) {
                if (sysUserService != null) {
                    org.jeecg.modules.system.entity.SysUser auditor = sysUserService.getById(task.getAssignedUserId());
                    if (auditor != null) {
                        auditors.add(auditor.getUsername());
                    }
                }
            }
            // 如果没有指定审核人，返回空列表，不通知角色的所有人
            
            return auditors;
        } catch (Exception e) {
            log.error("获取审核任务的审核人失败", e);
            return null;
        }
    }
    
    /**
     * 构建审核通知内容
     */
    private String buildAuditNotificationContent(GhOrder order, SysAuditTask task) {
        if (order == null) {
            return String.format("您有一条待审核的订单（步骤%d），请及时处理。", task.getStepOrder());
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return String.format(
            "您有一条待审核的订单（步骤%d），下单时间为：%s，公司名称为：%s，业务类型为：%s，请您审核，谢谢！",
            task.getStepOrder(),
            order.getCreateTime() != null ? sdf.format(order.getCreateTime()) : "未知",
            oConvertUtils.getString(order.getCompanyName(), "未知"),
            oConvertUtils.getString(order.getBusinessType(), "未知")
        );
    }
}

