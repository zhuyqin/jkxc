package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysAuditFormIndicator;

import java.util.List;

/**
 * <p>
 * 审批表单特殊指标配置表 Mapper 接口
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface SysAuditFormIndicatorMapper extends BaseMapper<SysAuditFormIndicator> {
    
    /**
     * 根据步骤表单ID查询所有指标
     */
    List<SysAuditFormIndicator> getIndicatorsByStepFormId(String stepFormId);
    
    /**
     * 删除步骤表单的所有指标
     */
    void deleteByStepFormId(String stepFormId);
}

