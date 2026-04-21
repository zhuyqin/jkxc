package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhAccountingHandover;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 代账交接表
 * @Author: jeecg-boot
 * @Date: 2025-01-02
 * @Version: V1.0
 */
public interface GhAccountingHandoverMapper extends BaseMapper<GhAccountingHandover> {

    /**
     * 根据订单ID查询代账交接
     * @param orderId 订单ID
     * @return 代账交接
     */
    GhAccountingHandover getHandoverByOrderId(@Param("orderId") String orderId);
}

