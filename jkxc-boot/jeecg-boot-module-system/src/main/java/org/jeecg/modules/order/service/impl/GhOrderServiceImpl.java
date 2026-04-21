package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.entity.GhOrderStep;
import org.jeecg.modules.order.mapper.GhOrderMapper;
import org.jeecg.modules.order.mapper.GhOrderStepMapper;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.order.service.IGhOrderStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
@Slf4j
public class GhOrderServiceImpl extends ServiceImpl<GhOrderMapper, GhOrder> implements IGhOrderService {

    @Autowired
    private GhOrderStepMapper ghOrderStepMapper;
    
    @Autowired
    private IGhOrderStepService ghOrderStepService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderAuditService ghOrderAuditService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhBusinessTaskService ghBusinessTaskService;
    
    @Autowired
    private org.jeecg.modules.system.service.ISysAuditProcessService sysAuditProcessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMain(GhOrder order, List<GhOrderStep> steps) {
        // 生成订单编号
        if (oConvertUtils.isEmpty(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }
        
        // 保存订单
        order.setCreateTime(new Date());
        order.setDelFlag(0);
        this.save(order);
        
        // 自动创建"订单创建"步骤
        GhOrderStep createStep = new GhOrderStep();
        createStep.setOrderId(order.getId());
        createStep.setStepType("0"); // 订单创建
        createStep.setStepOrder(0);
        createStep.setStatus("1"); // 已完成
        createStep.setOperatorName(order.getCreateBy());
        createStep.setRemarks("订单创建");
        createStep.setCreateTime(new Date());
        createStep.setCreateBy(order.getCreateBy());
        ghOrderStepMapper.insert(createStep);
        
        // 保存其他步骤
        if (steps != null && !steps.isEmpty()) {
            for (GhOrderStep step : steps) {
                step.setOrderId(order.getId());
                if (step.getStepOrder() == null) {
                    step.setStepOrder(1);
                }
                step.setCreateTime(new Date());
                ghOrderStepMapper.insert(step);
            }
        }
        
        // 如果订单配置了审核流程，初始化审核流程
        if (oConvertUtils.isNotEmpty(order.getAuditProcessId())) {
            try {
                ghOrderAuditService.initOrderAudit(order.getId(), order.getAuditProcessId());
                log.info("订单 {} 的审核流程已初始化", order.getId());
            } catch (Exception e) {
                log.error("初始化订单审核流程失败", e);
                // 不抛出异常，避免影响订单创建
            }
        }
        
        // 注意：工商任务不在订单创建时创建，而是在订单审核通过后创建
        // 这样可以确保只有审核通过的订单才会创建工商任务
        // 工商任务的创建逻辑在 GhOrderController.audit 方法中处理
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMain(GhOrder order, List<GhOrderStep> steps) {
        // 更新订单
        order.setUpdateTime(new Date());
        this.updateById(order);
        
        // 删除原有步骤
        ghOrderStepService.deleteByOrderId(order.getId());
        
        // 保存新步骤
        if (steps != null && !steps.isEmpty()) {
            for (GhOrderStep step : steps) {
                step.setId(null); // 清空ID，让系统重新生成
                step.setOrderId(order.getId());
                if (step.getStepOrder() == null) {
                    step.setStepOrder(1);
                }
                step.setCreateTime(new Date());
                ghOrderStepMapper.insert(step);
            }
        }
        
        return true;
    }

    @Override
    public GhOrder getOrderWithSteps(String id) {
        GhOrder order = this.getById(id);
        if (order != null) {
            List<GhOrderStep> steps = ghOrderStepMapper.getStepsByOrderId(id);
            order.setSteps(steps);
        }
        return order;
    }

    @Override
    public String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        
        // 查询当天最大订单号（格式：年月日 + 3位序号，如：20250115001）
        LambdaQueryWrapper<GhOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(GhOrder::getOrderNo, dateStr);
        queryWrapper.orderByDesc(GhOrder::getOrderNo);
        queryWrapper.last("LIMIT 1");
        
        GhOrder lastOrder = this.getOne(queryWrapper);
        int sequence = 1;
        if (lastOrder != null && oConvertUtils.isNotEmpty(lastOrder.getOrderNo())) {
            String lastNo = lastOrder.getOrderNo();
            // 订单号格式：yyyyMMdd + 3位序号（共11位）
            if (lastNo.length() == 11 && lastNo.startsWith(dateStr)) {
                try {
                    String sequenceStr = lastNo.substring(8); // 取后3位
                    sequence = Integer.parseInt(sequenceStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("解析订单号失败: {}", lastNo);
                }
            }
        }
        
        // 格式：年月日 + 3位序号（从001开始）
        return dateStr + String.format("%03d", sequence);
    }
    
    /**
     * 创建工商任务
     * @param order 订单
     */
    private void createBusinessTask(GhOrder order) {
        // 检查是否已存在工商任务
        org.jeecg.modules.order.entity.GhBusinessTask existingTask = 
            ghBusinessTaskService.getTaskByOrderId(order.getId());
        if (existingTask != null) {
            log.info("订单 {} 已存在工商任务，跳过创建", order.getId());
            return;
        }
        
        // 创建新的工商任务
        org.jeecg.modules.order.entity.GhBusinessTask task = 
            new org.jeecg.modules.order.entity.GhBusinessTask();
        task.setOrderId(order.getId());
        task.setOrderNo(order.getOrderNo());
        task.setCompanyName(order.getCompanyName());
        task.setTaskStatus("pending_manager_audit"); // 待工商经理审核
        task.setManagerAuditStatus("pending");
        task.setDelFlag(0);
        task.setCreateTime(new Date());
        task.setCreateBy(order.getCreateBy());
        
        ghBusinessTaskService.save(task);
        log.info("为订单 {} 创建了工商任务", order.getId());
    }
}

