package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhAccountingHandover;
import org.jeecg.modules.order.mapper.GhAccountingHandoverMapper;
import org.jeecg.modules.order.service.IGhAccountingHandoverService;
import org.jeecg.modules.order.service.IGhAccountingHandoverAuditService;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.service.ISysAuditProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 代账交接表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-02
 */
@Service
@Slf4j
public class GhAccountingHandoverServiceImpl extends ServiceImpl<GhAccountingHandoverMapper, GhAccountingHandover> implements IGhAccountingHandoverService {

    @Autowired
    private IGhAccountingHandoverAuditService ghAccountingHandoverAuditService;
    
    @Autowired
    private ISysAuditProcessService sysAuditProcessService;

    @Override
    public GhAccountingHandover getHandoverByOrderId(String orderId) {
        return baseMapper.getHandoverByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initHandoverAudit(String handoverId, String auditProcessId) {
        if (oConvertUtils.isEmpty(auditProcessId)) {
            log.warn("审核流程ID为空，跳过初始化代账交接审核流程");
            return false;
        }

        // 获取审核流程配置
        SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(auditProcessId);
        if (process == null || process.getSteps() == null || process.getSteps().isEmpty()) {
            log.warn("审核流程配置不存在或没有步骤，跳过初始化");
            return false;
        }

        // 使用代账交接审核Service来初始化审核记录
        return ghAccountingHandoverAuditService.initHandoverAudit(handoverId, auditProcessId);
    }
}

