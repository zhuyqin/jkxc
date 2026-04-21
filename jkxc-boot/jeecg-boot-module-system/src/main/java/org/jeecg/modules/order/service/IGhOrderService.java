package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.entity.GhOrderStep;

import java.util.List;

/**
 * <p>
 * 订单主表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhOrderService extends IService<GhOrder> {

    /**
     * 保存订单（包含步骤）
     * @param order 订单
     * @param steps 订单步骤列表
     * @return
     */
    boolean saveMain(GhOrder order, List<GhOrderStep> steps);

    /**
     * 更新订单（包含步骤）
     * @param order 订单
     * @param steps 订单步骤列表
     * @return
     */
    boolean updateMain(GhOrder order, List<GhOrderStep> steps);

    /**
     * 根据订单ID查询订单及步骤
     * @param id 订单ID
     * @return
     */
    GhOrder getOrderWithSteps(String id);

    /**
     * 生成订单编号
     * @return 订单编号
     */
    String generateOrderNo();
}

