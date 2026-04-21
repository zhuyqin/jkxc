package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditFormIndicator;

import java.util.List;

/**
 * <p>
 * 审批表单特殊指标配置表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface ISysAuditFormIndicatorService extends IService<SysAuditFormIndicator> {
    
    /**
     * 根据步骤表单ID查询所有指标
     */
    List<SysAuditFormIndicator> getIndicatorsByStepFormId(String stepFormId);
    
    /**
     * 保存步骤表单的指标配置（批量）
     */
    boolean saveIndicators(String stepFormId, List<SysAuditFormIndicator> indicators);
    
    /**
     * 删除步骤表单的所有指标
     */
    void deleteByStepFormId(String stepFormId);
}

