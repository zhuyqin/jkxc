package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhOrderAudit;

import java.util.List;

/**
 * <p>
 * 订单审核记录表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhOrderAuditService extends IService<GhOrderAudit> {

    /**
     * 根据订单ID查询审核记录列表
     * @param orderId 订单ID
     * @return 审核记录列表
     */
    List<GhOrderAudit> getAuditsByOrderId(String orderId);

    /**
     * 批量查询订单的审核记录（优化N+1查询）
     * @param orderIds 订单ID列表
     * @return Map<订单ID, 审核记录列表>
     */
    java.util.Map<String, List<GhOrderAudit>> getAuditsByOrderIds(List<String> orderIds);

    /**
     * 根据订单ID和步骤顺序查询审核记录
     * @param orderId 订单ID
     * @param stepOrder 步骤顺序
     * @return 审核记录
     */
    GhOrderAudit getAuditByOrderIdAndStep(String orderId, Integer stepOrder);

    /**
     * 根据订单ID和角色ID查询待审核的记录
     * @param orderId 订单ID
     * @param roleId 角色ID
     * @return 审核记录
     */
    GhOrderAudit getPendingAuditByOrderIdAndRole(String orderId, String roleId);

    /**
     * 初始化订单审核流程（根据审核流程配置创建审核记录）
     * @param orderId 订单ID
     * @param auditProcessId 审核流程ID
     * @return
     */
    boolean initOrderAudit(String orderId, String auditProcessId);

    /**
     * 执行订单审核
     * @param orderId 订单ID
     * @param roleId 审核角色ID
     * @param auditStatus 审核状态：approved-通过,rejected-驳回
     * @param auditRemark 审核备注
     * @return
     */
    boolean executeAudit(String orderId, String roleId, String auditStatus, String auditRemark);
}

