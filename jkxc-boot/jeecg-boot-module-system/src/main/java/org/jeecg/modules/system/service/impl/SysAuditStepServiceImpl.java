package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.mapper.SysAuditStepMapper;
import org.jeecg.modules.system.service.ISysAuditStepService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审核步骤表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
@Slf4j
public class SysAuditStepServiceImpl extends ServiceImpl<SysAuditStepMapper, SysAuditStep> implements ISysAuditStepService {

    @Override
    public List<SysAuditStep> getStepsByProcessId(String processId) {
        LambdaQueryWrapper<SysAuditStep> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysAuditStep::getProcessId, processId);
        queryWrapper.orderByAsc(SysAuditStep::getStepOrder);
        return this.list(queryWrapper);
    }

    @Override
    public boolean deleteByProcessId(String processId) {
        LambdaQueryWrapper<SysAuditStep> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysAuditStep::getProcessId, processId);
        return this.remove(queryWrapper);
    }

}

