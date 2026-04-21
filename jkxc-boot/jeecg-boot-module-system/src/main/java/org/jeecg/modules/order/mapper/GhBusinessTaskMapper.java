package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhBusinessTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 工商任务表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhBusinessTaskMapper extends BaseMapper<GhBusinessTask> {

    /**
     * 根据订单ID查询工商任务
     * @param orderId 订单ID
     * @return 工商任务
     */
    GhBusinessTask getTaskByOrderId(@Param("orderId") String orderId);

    /**
     * 根据任务状态和用户ID查询任务列表
     * @param taskStatus 任务状态
     * @param userId 用户ID（可选，用于查询分配给当前用户的任务）
     * @return 任务列表
     */
    List<GhBusinessTask> getTasksByStatus(@Param("taskStatus") String taskStatus, @Param("userId") String userId);
}

