package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhAccountingHandover;
import org.jeecg.modules.order.entity.GhAccountingHandoverAudit;
import org.jeecg.modules.order.mapper.GhAccountingHandoverAuditMapper;
import org.jeecg.modules.order.mapper.GhAccountingHandoverMapper;
import org.jeecg.modules.order.service.IGhAccountingHandoverAuditService;
import org.jeecg.modules.order.service.IGhOrderAuditService;
import org.jeecg.modules.order.enums.AuditStatusEnum;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.service.ISysAuditProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 代账交接审核记录表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-02
 */
@Service
@Slf4j
public class GhAccountingHandoverAuditServiceImpl extends ServiceImpl<GhAccountingHandoverAuditMapper, GhAccountingHandoverAudit> implements IGhAccountingHandoverAuditService {

    @Autowired
    private ISysAuditProcessService sysAuditProcessService;
    
    @Autowired
    private GhAccountingHandoverMapper ghAccountingHandoverMapper;
    
    @Autowired
    private IGhOrderAuditService ghOrderAuditService;

    @Override
    public List<GhAccountingHandoverAudit> getAuditsByHandoverId(String handoverId) {
        return baseMapper.getAuditsByHandoverId(handoverId);
    }

    @Override
    public GhAccountingHandoverAudit getPendingAuditByHandoverIdAndRole(String handoverId, String roleId) {
        return baseMapper.getPendingAuditByHandoverIdAndRole(handoverId, roleId, AuditStatusEnum.PENDING.getCode());
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

        // 获取handover信息以获取orderId
        GhAccountingHandover handover = ghAccountingHandoverMapper.selectById(handoverId);
        if (handover == null) {
            log.warn("代账交接记录不存在，handoverId: {}", handoverId);
            return false;
        }
        
        // 检查订单的第一步审核是否已完成
        // 如果订单的第一步已完成，代账交接的第一步也应该自动标记为已完成
        boolean firstStepCompleted = false;
        if (oConvertUtils.isNotEmpty(handover.getOrderId())) {
            // 查询订单的审核记录
            List<org.jeecg.modules.order.entity.GhOrderAudit> orderAudits = 
                ghOrderAuditService.getAuditsByOrderId(handover.getOrderId());
            
            // 检查第一步（stepOrder=1）的所有审核记录是否都已通过
            List<org.jeecg.modules.order.entity.GhOrderAudit> firstStepAudits = orderAudits.stream()
                .filter(a -> a.getStepOrder() != null && a.getStepOrder() == 1)
                .collect(Collectors.toList());
            
            if (!firstStepAudits.isEmpty()) {
                // 检查第一步的所有审核记录是否都已通过
                firstStepCompleted = firstStepAudits.stream()
                    .allMatch(a -> AuditStatusEnum.isApproved(a.getAuditStatus()));
            }
        }
        
        // 按步骤顺序创建审核记录
        for (SysAuditStep step : process.getSteps()) {
            GhAccountingHandoverAudit audit = new GhAccountingHandoverAudit();
            audit.setHandoverId(handoverId);
            audit.setOrderId(handover.getOrderId());
            audit.setAuditProcessId(auditProcessId);
            audit.setStepOrder(step.getStepOrder());
            audit.setRoleId(step.getRoleId());
            audit.setRoleName(step.getRoleName());
            
            // 如果是第一步且订单的第一步已完成，自动标记为已完成
            if (step.getStepOrder() == 1 && firstStepCompleted) {
                audit.setAuditStatus(AuditStatusEnum.APPROVED.getCode()); // 已通过
                audit.setAuditTime(new Date());
                audit.setAuditRemark("订单第一步审核已完成，自动通过");
            } else {
                audit.setAuditStatus(AuditStatusEnum.PENDING.getCode()); // 待审核
            }
            
            audit.setCreateTime(new Date());
            
            this.save(audit);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public boolean executeAudit(String handoverId, String roleId, String auditStatus, String auditRemark) {
        // 查找当前角色对应的待审核记录
        GhAccountingHandoverAudit audit = baseMapper.getPendingAuditByHandoverIdAndRole(handoverId, roleId, AuditStatusEnum.PENDING.getCode());
        if (audit == null) {
            log.warn("未找到待审核记录，代账交接ID: {}, 角色ID: {}", handoverId, roleId);
            return false;
        }

        // 更新审核记录
        audit.setAuditStatus(auditStatus);
        audit.setAuditRemark(auditRemark);
        audit.setAuditTime(new Date());
        audit.setUpdateTime(new Date());
        
        // 获取当前登录用户信息
        try {
            org.apache.shiro.subject.Subject subject = org.apache.shiro.SecurityUtils.getSubject();
            if (subject != null && subject.getPrincipal() != null) {
                org.jeecg.common.system.vo.LoginUser sysUser = 
                    (org.jeecg.common.system.vo.LoginUser) subject.getPrincipal();
                if (sysUser != null) {
                    audit.setAuditUserId(sysUser.getId());
                    audit.setAuditUserName(sysUser.getRealname());
                    audit.setUpdateBy(sysUser.getUsername());
                }
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户信息失败", e);
        }

        boolean success = this.updateById(audit);

        // 如果审核通过，检查是否所有步骤都已完成
        if (success && AuditStatusEnum.isApproved(auditStatus)) {
            // 检查当前步骤的所有角色是否都已审核通过
            List<GhAccountingHandoverAudit> currentStepAudits = this.list(
                new LambdaQueryWrapper<GhAccountingHandoverAudit>()
                    .eq(GhAccountingHandoverAudit::getHandoverId, handoverId)
                    .eq(GhAccountingHandoverAudit::getStepOrder, audit.getStepOrder())
            );
            
            boolean allApproved = currentStepAudits.stream()
                .allMatch(a -> AuditStatusEnum.isApproved(a.getAuditStatus()));
            
            if (allApproved) {
                // 当前步骤所有角色都已审核通过，进入下一步
                log.info("代账交接 {} 的第 {} 步审核已完成，可以进入下一步", handoverId, audit.getStepOrder());
            }
        }

        return success;
    }
}

