package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhOrderOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 订单操作记录
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOrderOperationLogMapper extends BaseMapper<GhOrderOperationLog> {
    
    /**
     * 根据订单ID查询操作记录
     * @param orderId 订单ID
     * @return 操作记录列表
     */
    List<GhOrderOperationLog> getLogsByOrderId(@Param("orderId") String orderId);
}

