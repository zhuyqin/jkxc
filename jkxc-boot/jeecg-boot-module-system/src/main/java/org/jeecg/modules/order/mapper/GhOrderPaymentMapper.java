package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhOrderPayment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 订单收费记录表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOrderPaymentMapper extends BaseMapper<GhOrderPayment> {

    /**
     * 根据订单ID查询收费记录列表
     * @param orderId 订单ID
     * @return 收费记录列表
     */
    List<GhOrderPayment> getPaymentsByOrderId(@Param("orderId") String orderId);
}

