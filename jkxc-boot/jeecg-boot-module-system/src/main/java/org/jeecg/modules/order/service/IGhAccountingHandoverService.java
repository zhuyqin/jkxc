package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhAccountingHandover;

/**
 * <p>
 * 代账交接表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-02
 */
public interface IGhAccountingHandoverService extends IService<GhAccountingHandover> {

    /**
     * 根据订单ID查询代账交接
     * @param orderId 订单ID
     * @return 代账交接
     */
    GhAccountingHandover getHandoverByOrderId(String orderId);
    
    /**
     * 初始化代账交接审核流程（根据审核流程配置创建审核记录）
     * @param handoverId 代账交接ID
     * @param auditProcessId 审核流程ID
     * @return
     */
    boolean initHandoverAudit(String handoverId, String auditProcessId);
}

