package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhOrderStep;

import java.util.List;

/**
 * <p>
 * 订单流程步骤表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhOrderStepService extends IService<GhOrderStep> {

    /**
     * 根据订单ID删除步骤
     * @param orderId 订单ID
     */
    void deleteByOrderId(String orderId);

    /**
     * 根据订单ID查询步骤列表
     * @param orderId 订单ID
     * @return 步骤列表
     */
    List<GhOrderStep> getStepsByOrderId(String orderId);
}

