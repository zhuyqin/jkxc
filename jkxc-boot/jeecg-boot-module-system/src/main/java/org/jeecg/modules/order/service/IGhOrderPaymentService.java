package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhOrderPayment;

import java.util.List;

/**
 * <p>
 * 订单收费记录表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhOrderPaymentService extends IService<GhOrderPayment> {

    /**
     * 根据订单ID查询收费记录列表
     * @param orderId 订单ID
     * @return 收费记录列表
     */
    List<GhOrderPayment> getPaymentsByOrderId(String orderId);
}

