package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysAuditProcessBinding;

import java.util.List;

/**
 * <p>
 * 流程审批业务类型绑定表 Mapper 接口
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface SysAuditProcessBindingMapper extends BaseMapper<SysAuditProcessBinding> {
    
    /**
     * 根据流程ID查询所有绑定
     */
    List<SysAuditProcessBinding> getBindingsByProcessId(String processId);
    
    /**
     * 根据业务类型ID查询绑定的流程
     */
    SysAuditProcessBinding getBindingByBusinessTypeId(String businessTypeId);
    
    /**
     * 根据业务类型ID和任务类型查询绑定的流程
     */
    SysAuditProcessBinding getBindingByBusinessTypeAndTaskType(String businessTypeId, String taskType);
    
    /**
     * 删除流程的所有绑定
     */
    void deleteByProcessId(String processId);
}

