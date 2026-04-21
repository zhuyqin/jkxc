package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysAuditStepForm;
import org.jeecg.modules.system.mapper.SysAuditStepFormMapper;
import org.jeecg.modules.system.service.ISysAuditStepFormService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 审批步骤表单配置表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Service
@Slf4j
public class SysAuditStepFormServiceImpl extends ServiceImpl<SysAuditStepFormMapper, SysAuditStepForm> implements ISysAuditStepFormService {

    @Override
    public SysAuditStepForm getFormByStepId(String stepId) {
        LambdaQueryWrapper<SysAuditStepForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditStepForm::getStepId, stepId);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateForm(String stepId, SysAuditStepForm form) {
        SysAuditStepForm existing = getFormByStepId(stepId);
        
        if (existing != null) {
            // 更新
            form.setId(existing.getId());
            form.setStepId(stepId);
            form.setUpdateTime(new Date());
            return this.updateById(form);
        } else {
            // 新增
            form.setStepId(stepId);
            form.setCreateTime(new Date());
            return this.save(form);
        }
    }
}

