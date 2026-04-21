package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhAccountingHandoverAudit;

import java.util.List;

/**
 * <p>
 * 代账交接审核记录表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-02
 */
public interface IGhAccountingHandoverAuditService extends IService<GhAccountingHandoverAudit> {

    /**
     * 根据代账交接ID查询审核记录列表
     * @param handoverId 代账交接ID
     * @return 审核记录列表
     */
    List<GhAccountingHandoverAudit> getAuditsByHandoverId(String handoverId);
    
    /**
     * 根据代账交接ID和角色ID查询待审核的记录
     * @param handoverId 代账交接ID
     * @param roleId 角色ID
     * @return 审核记录
     */
    GhAccountingHandoverAudit getPendingAuditByHandoverIdAndRole(String handoverId, String roleId);
    
    /**
     * 初始化代账交接审核流程（根据审核流程配置创建审核记录）
     * @param handoverId 代账交接ID
     * @param auditProcessId 审核流程ID
     * @return
     */
    boolean initHandoverAudit(String handoverId, String auditProcessId);
    
    /**
     * 执行代账交接审核
     * @param handoverId 代账交接ID
     * @param roleId 审核角色ID
     * @param auditStatus 审核状态：approved-通过,rejected-驳回
     * @param auditRemark 审核备注
     * @return
     */
    boolean executeAudit(String handoverId, String roleId, String auditStatus, String auditRemark);
}

