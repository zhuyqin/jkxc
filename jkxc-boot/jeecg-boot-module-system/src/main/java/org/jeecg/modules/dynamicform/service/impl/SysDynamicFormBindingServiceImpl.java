package org.jeecg.modules.dynamicform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormBinding;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;
import org.jeecg.modules.dynamicform.mapper.SysDynamicFormBindingMapper;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormBindingService;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormVersionService;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Date;

/**
 * @Description: 业务类型表单绑定表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Service
@  Slf4j
public class SysDynamicFormBindingServiceImpl extends ServiceImpl<SysDynamicFormBindingMapper, SysDynamicFormBinding> implements ISysDynamicFormBindingService {
    
    @Autowired
    private ISysDynamicFormVersionService versionService;
    
    @Override
    public SysDynamicFormBinding getBindingByBusinessType(String businessType) {
        return this.getOne(new LambdaQueryWrapper<SysDynamicFormBinding>()
            .eq(SysDynamicFormBinding::getBusinessType, businessType)
            .eq(SysDynamicFormBinding::getStatus, 1));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String bindBusinessType(String businessType, String formId, String versionId, Integer status) {
        // 获取版本信息
        SysDynamicFormVersion version = versionService.getById(versionId);
        if (version == null) {
            throw new RuntimeException("版本不存在");
        }
        
        // 重要：使用版本对象中的formId，而不是前端传过来的formId
        // 这样可以确保formId的正确性，避免前端传错formId导致绑定表数据错误
        String actualFormId = version.getFormId();
        if (actualFormId == null || actualFormId.isEmpty()) {
            throw new RuntimeException("版本信息中缺少表单ID");
        }
        
        // 如果前端传的formId与版本中的formId不一致，记录警告日志
        if (formId != null && !formId.equals(actualFormId)) {
            log.warn("前端传递的formId({})与版本中的formId({})不一致，使用版本中的formId", formId, actualFormId);
        }
        
        // 检查是否已存在该业务类型和表单的绑定（form_id + business_type的唯一约束）
        SysDynamicFormBinding existing = this.getOne(new LambdaQueryWrapper<SysDynamicFormBinding>()
            .eq(SysDynamicFormBinding::getBusinessType, businessType)
            .eq(SysDynamicFormBinding::getFormId, actualFormId));
        
        if (existing != null) {
            // 更新现有绑定（同一业务类型绑定同一表单，更新版本）
            existing.setVersionId(versionId);
            existing.setVersion(version.getVersion());
            if (status != null) {
                existing.setStatus(status);
            }
            existing.setUpdateTime(new Date());
            this.updateById(existing);
            return existing.getId();
        } else {
            // 检查该业务类型是否已绑定其他表单（一个业务类型只能绑定一个表单）
            SysDynamicFormBinding otherBinding = this.getOne(new LambdaQueryWrapper<SysDynamicFormBinding>()
                .eq(SysDynamicFormBinding::getBusinessType, businessType));
            
            if (otherBinding != null) {
                // 如果业务类型已绑定其他表单，先删除旧绑定，再创建新绑定
                this.removeById(otherBinding.getId());
            }
            
            // 创建新绑定，使用版本中的formId
            SysDynamicFormBinding binding = new SysDynamicFormBinding();
            binding.setBusinessType(businessType);
            binding.setFormId(actualFormId); // 使用版本中的formId
            binding.setVersionId(versionId);
            binding.setVersion(version.getVersion());
            binding.setStatus(status != null ? status : 1); // 使用传入的状态，默认为1
            binding.setCreateTime(new Date());
            this.save(binding);
            return binding.getId();
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBindingsByFormId(String formId) {
        if (oConvertUtils.isEmpty(formId)) {
            return;
        }
        // 删除该表单的所有绑定关系
        this.remove(new LambdaQueryWrapper<SysDynamicFormBinding>()
            .eq(SysDynamicFormBinding::getFormId, formId));
        log.info("已删除表单 {} 的所有绑定关系", formId);
    }
}

