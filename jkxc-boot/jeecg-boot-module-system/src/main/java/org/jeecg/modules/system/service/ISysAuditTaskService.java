package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditTask;

import java.util.List;

/**
 * <p>
 * 审批任务表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface ISysAuditTaskService extends IService<SysAuditTask> {
    
    /**
     * 根据订单ID查询所有任务
     */
    List<SysAuditTask> getTasksByOrderId(String orderId);
    
    /**
     * 批量查询订单的审核任务（优化N+1查询）
     * @param orderIds 订单ID列表
     * @return Map<订单ID, 审核任务列表>
     */
    java.util.Map<String, List<SysAuditTask>> getTasksByOrderIds(List<String> orderIds);
    
    /**
     * 根据订单ID和步骤ID查询任务
     */
    SysAuditTask getTaskByOrderIdAndStepId(String orderId, String stepId);
    
    /**
     * 根据订单ID和步骤顺序查询任务
     */
    SysAuditTask getTaskByOrderIdAndStepOrder(String orderId, Integer stepOrder);
    
    /**
     * 创建审批任务（订单创建时调用）
     */
    SysAuditTask createTask(String orderId, String processId, String stepId, Integer stepOrder, String taskType);
    
    /**
     * 查询待审核任务列表（支持个人/团队切换、角色过滤、指定人员过滤）
     */
    IPage<SysAuditTask> getPendingTasks(Page<SysAuditTask> page, 
                                        String taskType,
                                        String userId,
                                        List<String> roleIds,
                                        Boolean isPersonal,
                                        String assignedUserId,
                                        String contractNo,
                                        String orderNo,
                                        String companyName,
                                        List<String> dataRoleIds);
    
    /**
     * 查询已审核任务列表
     */
    IPage<SysAuditTask> getApprovedTasks(Page<SysAuditTask> page,
                                         String taskType,
                                         String userId,
                                         List<String> roleIds,
                                         Boolean isPersonal,
                                         String orderNo,
                                         String companyName,
                                         List<String> dataRoleIds);
    
    /**
     * 查询已驳回任务列表
     */
    IPage<SysAuditTask> getRejectedTasks(Page<SysAuditTask> page,
                                         String taskType,
                                         String userId,
                                         List<String> roleIds,
                                         Boolean isPersonal,
                                         String orderNo,
                                         String companyName,
                                         List<String> dataRoleIds);
    
    /**
     * 执行审核操作
     * @param taskId 任务ID
     * @param auditResult 审核结果：approved-通过，rejected-不通过，returned-驳回
     * @param auditData 审核数据JSON
     * @param auditRemark 审核备注
     * @param rejectReason 驳回原因（驳回时必填）
     * @return
     */
    boolean executeAudit(String taskId, String auditResult, String auditData, String auditRemark, String rejectReason);
    
    /**
     * 驳回后重新提交（创建新的审核任务）
     */
    SysAuditTask resubmitAfterReject(String orderId, String originalTaskId);
    
    /**
     * 订单完成时，为所有成本支出创建报销记录
     * @param orderId 订单ID
     */
    void createReimbursementsForCompletedOrder(String orderId);
}

