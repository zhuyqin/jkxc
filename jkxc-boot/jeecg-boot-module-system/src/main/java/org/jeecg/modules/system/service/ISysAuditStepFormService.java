package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditStepForm;

/**
 * <p>
 * 审批步骤表单配置表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface ISysAuditStepFormService extends IService<SysAuditStepForm> {
    
    /**
     * 根据步骤ID查询表单配置
     */
    SysAuditStepForm getFormByStepId(String stepId);
    
    /**
     * 保存或更新步骤表单配置
     */
    boolean saveOrUpdateForm(String stepId, SysAuditStepForm form);
}

