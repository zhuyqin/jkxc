package org.jeecg.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.order.entity.GhOrderExpense;

import java.util.List;

/**
 * <p>
 * 订单支出记录表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhOrderExpenseService extends IService<GhOrderExpense> {

    /**
     * 根据订单ID查询支出记录列表
     * @param orderId 订单ID
     * @return 支出记录列表
     */
    List<GhOrderExpense> getExpensesByOrderId(String orderId);
}

