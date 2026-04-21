package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAuditProcessBinding;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysAuditProcessBindingMapper;
import org.jeecg.modules.system.mapper.SysCategoryMapper;
import org.jeecg.modules.system.service.ISysAuditProcessBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 流程审批业务类型绑定表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Service
@Slf4j
public class SysAuditProcessBindingServiceImpl extends ServiceImpl<SysAuditProcessBindingMapper, SysAuditProcessBinding> implements ISysAuditProcessBindingService {

    @Autowired
    private SysCategoryMapper sysCategoryMapper;

    @Override
    public List<SysAuditProcessBinding> getBindingsByProcessId(String processId) {
        LambdaQueryWrapper<SysAuditProcessBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditProcessBinding::getProcessId, processId);
        wrapper.orderByAsc(SysAuditProcessBinding::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public SysAuditProcessBinding getBindingByBusinessTypeId(String businessTypeId) {
        LambdaQueryWrapper<SysAuditProcessBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditProcessBinding::getBusinessTypeId, businessTypeId);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public SysAuditProcessBinding getBindingByBusinessTypeAndTaskType(String businessTypeId, String taskType) {
        LambdaQueryWrapper<SysAuditProcessBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditProcessBinding::getBusinessTypeId, businessTypeId);
        wrapper.eq(SysAuditProcessBinding::getTaskType, taskType);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBindings(String processId, List<SysAuditProcessBinding> bindings) {
        // 删除原有绑定
        deleteByProcessId(processId);
        
        // 保存新绑定
        if (bindings != null && !bindings.isEmpty()) {
            for (SysAuditProcessBinding binding : bindings) {
                binding.setProcessId(processId);
                binding.setCreateTime(new Date());
                
                // 根据业务类型ID获取业务类型信息（如果前端没有传）
                if (oConvertUtils.isNotEmpty(binding.getBusinessTypeId())) {
                    if (oConvertUtils.isEmpty(binding.getBusinessTypeName()) || oConvertUtils.isEmpty(binding.getBusinessTypeCode())) {
                        SysCategory category = sysCategoryMapper.selectById(binding.getBusinessTypeId());
                        if (category != null) {
                            binding.setBusinessTypeName(category.getName());
                            binding.setBusinessTypeCode(category.getCode());
                            
                            // 验证是否为最底层节点（hasChild != '1'）
                            if ("1".equals(category.getHasChild())) {
                                log.warn("业务类型 {} 不是最底层节点，但被绑定到流程 {}", category.getName(), processId);
                            }
                        }
                    }
                }
                
                this.save(binding);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProcessId(String processId) {
        LambdaQueryWrapper<SysAuditProcessBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditProcessBinding::getProcessId, processId);
        this.remove(wrapper);
    }
}

