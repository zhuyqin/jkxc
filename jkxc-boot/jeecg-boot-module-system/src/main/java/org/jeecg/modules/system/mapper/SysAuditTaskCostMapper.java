package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysAuditTaskCost;

import java.util.List;

/**
 * <p>
 * 审批任务成本记录表 Mapper 接口
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface SysAuditTaskCostMapper extends BaseMapper<SysAuditTaskCost> {
    
    /**
     * 根据任务ID查询所有成本记录
     */
    List<SysAuditTaskCost> getCostsByTaskId(String taskId);
    
    /**
     * 删除任务的所有成本记录
     */
    void deleteByTaskId(String taskId);
}

