package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysAuditFormIndicator;
import org.jeecg.modules.system.mapper.SysAuditFormIndicatorMapper;
import org.jeecg.modules.system.service.ISysAuditFormIndicatorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 审批表单特殊指标配置表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Service
@Slf4j
public class SysAuditFormIndicatorServiceImpl extends ServiceImpl<SysAuditFormIndicatorMapper, SysAuditFormIndicator> implements ISysAuditFormIndicatorService {

    @Override
    public List<SysAuditFormIndicator> getIndicatorsByStepFormId(String stepFormId) {
        LambdaQueryWrapper<SysAuditFormIndicator> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditFormIndicator::getStepFormId, stepFormId);
        wrapper.orderByAsc(SysAuditFormIndicator::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIndicators(String stepFormId, List<SysAuditFormIndicator> indicators) {
        // 删除原有指标
        deleteByStepFormId(stepFormId);
        
        // 保存新指标
        if (indicators != null && !indicators.isEmpty()) {
            Date now = new Date();
            for (SysAuditFormIndicator indicator : indicators) {
                indicator.setStepFormId(stepFormId);
                indicator.setCreateTime(now);
                if (indicator.getSortOrder() == null) {
                    indicator.setSortOrder(0);
                }
                this.save(indicator);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByStepFormId(String stepFormId) {
        LambdaQueryWrapper<SysAuditFormIndicator> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditFormIndicator::getStepFormId, stepFormId);
        this.remove(wrapper);
    }
}

