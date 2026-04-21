package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrderAudit;
import org.jeecg.modules.order.mapper.GhOrderAuditMapper;
import org.jeecg.modules.order.service.IGhOrderAuditService;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.service.ISysAuditProcessService;
import org.jeecg.modules.system.service.ISysAuditStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 订单审核记录表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
@Slf4j
public class GhOrderAuditServiceImpl extends ServiceImpl<GhOrderAuditMapper, GhOrderAudit> implements IGhOrderAuditService {

    @Autowired
    private ISysAuditProcessService sysAuditProcessService;
    
    @Autowired
    private ISysAuditStepService sysAuditStepService;

    @Override
    public List<GhOrderAudit> getAuditsByOrderId(String orderId) {
        return baseMapper.getAuditsByOrderId(orderId);
    }

    @Override
    public java.util.Map<String, List<GhOrderAudit>> getAuditsByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        // 批量查询所有订单的审核记录
        LambdaQueryWrapper<GhOrderAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GhOrderAudit::getOrderId, orderIds);
        wrapper.orderByAsc(GhOrderAudit::getStepOrder);
        List<GhOrderAudit> allAudits = this.list(wrapper);
        
        // 按订单ID分组
        return allAudits.stream()
            .collect(java.util.stream.Collectors.groupingBy(GhOrderAudit::getOrderId));
    }

    @Override
    public GhOrderAudit getAuditByOrderIdAndStep(String orderId, Integer stepOrder) {
        return baseMapper.getAuditByOrderIdAndStep(orderId, stepOrder);
    }

    @Override
    public GhOrderAudit getPendingAuditByOrderIdAndRole(String orderId, String roleId) {
        return baseMapper.getPendingAuditByOrderIdAndRole(orderId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initOrderAudit(String orderId, String auditProcessId) {
        if (oConvertUtils.isEmpty(auditProcessId)) {
            log.warn("审核流程ID为空，跳过初始化审核流程");
            return false;
        }

        // 获取审核流程配置
        SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(auditProcessId);
        if (process == null || process.getSteps() == null || process.getSteps().isEmpty()) {
            log.warn("审核流程配置不存在或没有步骤，跳过初始化");
            return false;
        }

        // 按步骤顺序创建审核记录
        for (SysAuditStep step : process.getSteps()) {
            GhOrderAudit audit = new GhOrderAudit();
            audit.setOrderId(orderId);
            audit.setAuditProcessId(auditProcessId);
            audit.setStepOrder(step.getStepOrder());
            audit.setRoleId(step.getRoleId());
            audit.setRoleName(step.getRoleName());
            audit.setAuditStatus("pending"); // 待审核
            audit.setCreateTime(new Date());
            
            this.save(audit);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public boolean executeAudit(String orderId, String roleId, String auditStatus, String auditRemark) {
        // 查找当前角色对应的待审核记录
        GhOrderAudit audit = baseMapper.getPendingAuditByOrderIdAndRole(orderId, roleId);
        if (audit == null) {
            log.warn("未找到待审核记录，订单ID: {}, 角色ID: {}", orderId, roleId);
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

        // 如果审核通过，检查是否所有步骤都已完成，如果是则进入下一步
        if (success && "approved".equals(auditStatus)) {
            // 检查当前步骤的所有角色是否都已审核通过
            List<GhOrderAudit> currentStepAudits = this.list(
                new LambdaQueryWrapper<GhOrderAudit>()
                    .eq(GhOrderAudit::getOrderId, orderId)
                    .eq(GhOrderAudit::getStepOrder, audit.getStepOrder())
            );
            
            boolean allApproved = currentStepAudits.stream()
                .allMatch(a -> "approved".equals(a.getAuditStatus()));
            
            if (allApproved) {
                // 当前步骤所有角色都已审核通过，进入下一步
                // 这里可以触发下一步的审核流程，或者创建工商任务等
                log.info("订单 {} 的第 {} 步审核已完成，可以进入下一步", orderId, audit.getStepOrder());
            }
        }

        return success;
    }
}

