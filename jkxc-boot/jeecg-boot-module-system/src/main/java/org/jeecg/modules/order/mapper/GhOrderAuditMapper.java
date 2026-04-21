package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhOrderAudit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 订单审核记录表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOrderAuditMapper extends BaseMapper<GhOrderAudit> {

    /**
     * 根据订单ID查询审核记录列表（按步骤顺序排序）
     * @param orderId 订单ID
     * @return 审核记录列表
     */
    List<GhOrderAudit> getAuditsByOrderId(@Param("orderId") String orderId);

    /**
     * 根据订单ID和步骤顺序查询审核记录
     * @param orderId 订单ID
     * @param stepOrder 步骤顺序
     * @return 审核记录
     */
    GhOrderAudit getAuditByOrderIdAndStep(@Param("orderId") String orderId, @Param("stepOrder") Integer stepOrder);

    /**
     * 根据订单ID和角色ID查询待审核的记录
     * @param orderId 订单ID
     * @param roleId 角色ID
     * @return 审核记录
     */
    GhOrderAudit getPendingAuditByOrderIdAndRole(@Param("orderId") String orderId, @Param("roleId") String roleId);
}

