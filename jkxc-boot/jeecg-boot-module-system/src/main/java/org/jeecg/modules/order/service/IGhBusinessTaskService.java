package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhBusinessTask;

import java.util.List;

/**
 * <p>
 * 工商任务表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhBusinessTaskService extends IService<GhBusinessTask> {

    /**
     * 根据订单ID查询工商任务
     * @param orderId 订单ID
     * @return 工商任务
     */
    GhBusinessTask getTaskByOrderId(String orderId);

    /**
     * 批量查询订单的工商任务（优化N+1查询）
     * @param orderIds 订单ID列表
     * @return Map<订单ID, 工商任务>
     */
    java.util.Map<String, GhBusinessTask> getTasksByOrderIds(List<String> orderIds);

    /**
     * 根据任务状态查询任务列表
     * @param taskStatus 任务状态
     * @param userId 用户ID（可选）
     * @return 任务列表
     */
    List<GhBusinessTask> getTasksByStatus(String taskStatus, String userId);

    /**
     * 工商经理审核
     * @param taskId 任务ID
     * @param auditStatus 审核状态：approved-通过,rejected-驳回
     * @param assignType 分配类型：public_sea-放入公海,assign_user-指定工商人员
     * @param assignedUserId 分配的工商人员ID（当assignType为assign_user时必填）
     * @param assignedUserName 分配的工商人员姓名
     * @param remark 审核备注
     * @return
     */
    boolean managerAudit(String taskId, String auditStatus, String assignType, String assignedUserId, String assignedUserName, String remark);

    /**
     * 工商人员接收任务
     * @param taskId 任务ID
     * @param userId 接收人ID
     * @param userName 接收人姓名
     * @return
     */
    boolean receiveTask(String taskId, String userId, String userName);

    /**
     * 更新任务成本信息
     * @param taskId 任务ID
     * @param costCategory 成本类目
     * @param costAmount 成本金额
     * @return
     */
    boolean updateCost(String taskId, String costCategory, java.math.BigDecimal costAmount);

    /**
     * 交接任务
     * @param taskId 任务ID
     * @param userId 交接人ID
     * @param userName 交接人姓名
     * @return
     */
    boolean handoverTask(String taskId, String userId, String userName);

    /**
     * 业务人员确认交接完成
     * @param taskId 任务ID
     * @param userId 业务人员ID
     * @param userName 业务人员姓名
     * @return
     */
    boolean confirmHandover(String taskId, String userId, String userName);

    /**
     * 转为异常任务
     * @param taskId 任务ID
     * @param exceptionType 异常类型：problem_task-问题任务,recycle_bin-回收站
     * @param reason 原因
     * @return
     */
    boolean convertToException(String taskId, String exceptionType, String reason);

    /**
     * 转分配工商人员
     * @param taskId 任务ID
     * @param assignedUserId 分配的工商人员ID
     * @param assignedUserName 分配的工商人员姓名
     * @return
     */
    boolean reassignTask(String taskId, String assignedUserId, String assignedUserName);
}

