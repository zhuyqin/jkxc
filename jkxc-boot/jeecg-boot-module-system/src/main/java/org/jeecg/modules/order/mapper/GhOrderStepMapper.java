package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhOrderStep;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 订单流程步骤表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOrderStepMapper extends BaseMapper<GhOrderStep> {

    /**
     * 根据订单ID查询步骤列表
     * @param orderId 订单ID
     * @return 步骤列表
     */
    List<GhOrderStep> getStepsByOrderId(@Param("orderId") String orderId);
}

