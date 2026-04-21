package org.jeecg.modules.order.service;

import java.util.List;

import org.jeecg.modules.order.entity.GhOrderOperationLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 订单操作记录
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface IGhOrderOperationLogService extends IService<GhOrderOperationLog> {
    
    /**
     * 根据订单ID查询操作记录
     * @param orderId 订单ID
     * @return 操作记录列表
     */
    List<GhOrderOperationLog> getLogsByOrderId(String orderId);
    
    /**
     * 记录订单操作
     * @param orderId 订单ID
     * @param operationType 操作类型
     * @param operationDesc 操作描述
     * @param operatorName 操作人姓名
     * @param beforeData 操作前数据
     * @param afterData 操作后数据
     * @param changedFields 变更字段
     * @param remarks 备注
     */
    void saveOperationLog(String orderId, String operationType, String operationDesc, 
                          String operatorName, String beforeData, String afterData, 
                          String changedFields, String remarks);
}

