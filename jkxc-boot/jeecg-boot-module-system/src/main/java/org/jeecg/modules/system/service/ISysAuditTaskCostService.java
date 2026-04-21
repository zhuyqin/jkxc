package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditTaskCost;

import java.util.List;

/**
 * <p>
 * 审批任务成本记录表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface ISysAuditTaskCostService extends IService<SysAuditTaskCost> {
    
    /**
     * 根据任务ID查询所有成本记录
     */
    List<SysAuditTaskCost> getCostsByTaskId(String taskId);
    
    /**
     * 保存任务的成本记录（批量）
     */
    boolean saveCosts(String taskId, List<SysAuditTaskCost> costs);
    
    /**
     * 删除任务的所有成本记录
     */
    void deleteByTaskId(String taskId);
}

