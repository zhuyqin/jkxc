package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditProcessBinding;

import java.util.List;

/**
 * <p>
 * 流程审批业务类型绑定表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface ISysAuditProcessBindingService extends IService<SysAuditProcessBinding> {
    
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
     * 保存流程的业务类型绑定（批量）
     */
    boolean saveBindings(String processId, List<SysAuditProcessBinding> bindings);
    
    /**
     * 删除流程的所有绑定
     */
    void deleteByProcessId(String processId);
}

