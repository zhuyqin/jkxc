package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysAuditTaskCost;
import org.jeecg.modules.system.mapper.SysAuditTaskCostMapper;
import org.jeecg.modules.system.service.ISysAuditTaskCostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 审批任务成本记录表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Service
@Slf4j
public class SysAuditTaskCostServiceImpl extends ServiceImpl<SysAuditTaskCostMapper, SysAuditTaskCost> implements ISysAuditTaskCostService {

    @Override
    public List<SysAuditTaskCost> getCostsByTaskId(String taskId) {
        LambdaQueryWrapper<SysAuditTaskCost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditTaskCost::getTaskId, taskId);
        wrapper.orderByAsc(SysAuditTaskCost::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCosts(String taskId, List<SysAuditTaskCost> costs) {
        // 删除原有成本记录
        deleteByTaskId(taskId);
        
        // 保存新成本记录
        if (costs != null && !costs.isEmpty()) {
            Date now = new Date();
            for (SysAuditTaskCost cost : costs) {
                cost.setTaskId(taskId);
                cost.setCreateTime(now);
                if (cost.getSortOrder() == null) {
                    cost.setSortOrder(0);
                }
                this.save(cost);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTaskId(String taskId) {
        LambdaQueryWrapper<SysAuditTaskCost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditTaskCost::getTaskId, taskId);
        this.remove(wrapper);
    }
}

