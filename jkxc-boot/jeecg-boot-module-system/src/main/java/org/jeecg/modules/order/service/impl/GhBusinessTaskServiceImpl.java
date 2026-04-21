package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhBusinessTask;
import org.jeecg.modules.order.entity.GhOrderExpense;
import org.jeecg.modules.order.mapper.GhBusinessTaskMapper;
import org.jeecg.modules.order.service.IGhBusinessTaskService;
import org.jeecg.modules.order.service.IGhOrderExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 工商任务表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
@Slf4j
public class GhBusinessTaskServiceImpl extends ServiceImpl<GhBusinessTaskMapper, GhBusinessTask> implements IGhBusinessTaskService {

    @Autowired
    private IGhOrderExpenseService ghOrderExpenseService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderService ghOrderService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderAuditService ghOrderAuditService;

    @Override
    public GhBusinessTask getTaskByOrderId(String orderId) {
        return baseMapper.getTaskByOrderId(orderId);
    }

    @Override
    public java.util.Map<String, GhBusinessTask> getTasksByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        // 批量查询所有订单的工商任务
        LambdaQueryWrapper<GhBusinessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GhBusinessTask::getOrderId, orderIds);
        List<GhBusinessTask> allTasks = this.list(wrapper);
        
        // 按订单ID分组（每个订单只有一个工商任务）
        return allTasks.stream()
            .collect(java.util.stream.Collectors.toMap(
                GhBusinessTask::getOrderId,
                task -> task,
                (existing, replacement) -> existing // 如果有重复，保留第一个
            ));
    }

    @Override
    public List<GhBusinessTask> getTasksByStatus(String taskStatus, String userId) {
        return baseMapper.getTasksByStatus(taskStatus, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean managerAudit(String taskId, String auditStatus, String assignType, String assignedUserId, String assignedUserName, String remark) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        Date now = new Date();
        task.setManagerAuditStatus(auditStatus);
        task.setManagerAuditTime(now);
        task.setManagerAuditRemark(remark);
        task.setUpdateTime(now);

        if ("approved".equals(auditStatus)) {
            // 审核通过，根据分配类型设置任务状态
            if ("public_sea".equals(assignType)) {
                task.setTaskStatus("public_sea");
                task.setAssignType("public_sea");
            } else if ("assign_user".equals(assignType)) {
                task.setTaskStatus("assigned_to_me");
                task.setAssignType("assign_user");
                task.setAssignedUserId(assignedUserId);
                task.setAssignedUserName(assignedUserName);
                task.setAssignedTime(now);
            }
        } else if ("rejected".equals(auditStatus)) {
            // 审核驳回，保持待审核状态
            task.setTaskStatus("pending_manager_audit");
        }

        return this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveTask(String taskId, String userId, String userName) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        Date now = new Date();
        task.setReceivedUserId(userId);
        task.setReceivedUserName(userName);
        task.setReceivedTime(now);
        task.setTaskStatus("task");
        task.setUpdateTime(now);

        return this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCost(String taskId, String costCategory, BigDecimal costAmount) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        task.setCostCategory(costCategory);
        task.setCostAmount(costAmount);
        task.setUpdateTime(new Date());

        boolean success = this.updateById(task);

        // 如果更新成功，同步到订单支出记录
        if (success && costAmount != null && costAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 检查是否已存在支出记录
            LambdaQueryWrapper<GhOrderExpense> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GhOrderExpense::getOrderId, task.getOrderId());
            wrapper.eq(GhOrderExpense::getCategory, "工商成本");
            GhOrderExpense expense = ghOrderExpenseService.getOne(wrapper);

            if (expense == null) {
                // 创建新的支出记录
                expense = new GhOrderExpense();
                expense.setOrderId(task.getOrderId());
                expense.setExpenseTime(new Date());
                expense.setAmount(costAmount);
                expense.setCategory(costCategory != null ? costCategory : "工商成本");
                expense.setRemarks("工商任务成本");
                expense.setCreateTime(new Date());
                ghOrderExpenseService.save(expense);
            } else {
                // 更新现有支出记录
                expense.setAmount(costAmount);
                expense.setCategory(costCategory != null ? costCategory : "工商成本");
                expense.setUpdateTime(new Date());
                ghOrderExpenseService.updateById(expense);
            }
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handoverTask(String taskId, String userId, String userName) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        Date now = new Date();
        task.setHandoverUserId(userId);
        task.setHandoverUserName(userName);
        task.setHandoverTime(now);
        task.setHandoverStatus("pending");
        task.setTaskStatus("handover");
        task.setUpdateTime(now);

        return this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmHandover(String taskId, String userId, String userName) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        Date now = new Date();
        task.setBusinessUserId(userId);
        task.setBusinessUserName(userName);
        task.setHandoverStatus("completed");
        task.setTaskStatus("completed");
        task.setCompleteTime(now);
        task.setUpdateTime(now);

        boolean success = this.updateById(task);
        
        if (success) {
            // 更新订单审核流程：找到最后一个待审核的步骤，标记为已完成
            String orderId = task.getOrderId();
            if (oConvertUtils.isNotEmpty(orderId)) {
                try {
                    // 获取订单的所有审核记录
                    List<org.jeecg.modules.order.entity.GhOrderAudit> audits = 
                        ghOrderAuditService.getAuditsByOrderId(orderId);
                    
                    if (audits != null && !audits.isEmpty()) {
                        // 查找最后一个待审核的记录（按步骤顺序和角色名称匹配）
                        // 优先查找"业务人员"角色的待审核记录
                        org.jeecg.modules.order.entity.GhOrderAudit businessAudit = audits.stream()
                            .filter(a -> "pending".equals(a.getAuditStatus()) &&
                                    a.getRoleName() != null && 
                                    (a.getRoleName().contains("业务人员") || a.getRoleName().contains("业务")))
                            .max((a1, a2) -> Integer.compare(a1.getStepOrder(), a2.getStepOrder()))
                            .orElse(null);
                        
                        // 如果没找到业务人员，找最后一个待审核的记录
                        if (businessAudit == null) {
                            businessAudit = audits.stream()
                                .filter(a -> "pending".equals(a.getAuditStatus()))
                                .max((a1, a2) -> Integer.compare(a1.getStepOrder(), a2.getStepOrder()))
                                .orElse(null);
                        }
                        
                        if (businessAudit != null) {
                            // 更新审核记录为已完成
                            businessAudit.setAuditStatus("approved");
                            businessAudit.setAuditTime(now);
                            businessAudit.setAuditUserId(userId);
                            businessAudit.setAuditUserName(userName);
                            businessAudit.setAuditRemark("工商任务交接完成");
                            businessAudit.setUpdateTime(now);
                            ghOrderAuditService.updateById(businessAudit);
                            log.info("订单 {} 的审核步骤 {} (角色: {}) 已标记为完成", 
                                orderId, businessAudit.getStepOrder(), businessAudit.getRoleName());
                        }
                        
                        // 检查是否所有审核步骤都已完成
                        List<org.jeecg.modules.order.entity.GhOrderAudit> allAudits = 
                            ghOrderAuditService.getAuditsByOrderId(orderId);
                        boolean allApproved = allAudits.stream()
                            .allMatch(a -> "approved".equals(a.getAuditStatus()));
                        
                        if (allApproved) {
                            // 所有审核步骤都已完成，更新订单状态为已完成
                            org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(orderId);
                            if (order != null) {
                                order.setOrderStatus("2"); // 2-已完成（所有审批流程走完就自动已完成）
                                order.setAuditStatus("approved");
                                order.setUpdateTime(now);
                                ghOrderService.updateById(order);
                                log.info("订单 {} 所有审核步骤已完成，订单状态已更新为已完成", orderId);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("更新订单审核流程失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响任务完成操作
                }
            }
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean convertToException(String taskId, String exceptionType, String reason) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        task.setTaskStatus(exceptionType);
        if ("problem_task".equals(exceptionType)) {
            task.setProblemReason(reason);
        } else if ("recycle_bin".equals(exceptionType)) {
            task.setRecycleReason(reason);
        }
        task.setUpdateTime(new Date());

        return this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reassignTask(String taskId, String assignedUserId, String assignedUserName) {
        GhBusinessTask task = this.getById(taskId);
        if (task == null) {
            return false;
        }

        Date now = new Date();
        task.setAssignedUserId(assignedUserId);
        task.setAssignedUserName(assignedUserName);
        task.setAssignedTime(now);
        task.setTaskStatus("assigned_to_me");
        task.setAssignType("assign_user");
        task.setUpdateTime(now);

        return this.updateById(task);
    }
}

