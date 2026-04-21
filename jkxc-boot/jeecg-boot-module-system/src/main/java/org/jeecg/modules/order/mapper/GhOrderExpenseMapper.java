package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhOrderExpense;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 订单支出记录表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOrderExpenseMapper extends BaseMapper<GhOrderExpense> {

    /**
     * 根据订单ID查询支出记录列表
     * @param orderId 订单ID
     * @return 支出记录列表
     */
    List<GhOrderExpense> getExpensesByOrderId(@Param("orderId") String orderId);
}

