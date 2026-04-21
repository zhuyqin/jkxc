package org.jeecg.modules.dynamicform.service.impl;

import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;
import org.jeecg.modules.dynamicform.mapper.SysDynamicFormVersionMapper;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormVersionService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;

/**
 * @Description: 动态表单版本表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Service
public class SysDynamicFormVersionServiceImpl extends ServiceImpl<SysDynamicFormVersionMapper, SysDynamicFormVersion> implements ISysDynamicFormVersionService {
    
    @Override
    public List<SysDynamicFormVersion> getVersionsByFormId(String formId) {
        return this.list(new LambdaQueryWrapper<SysDynamicFormVersion>()
            .eq(SysDynamicFormVersion::getFormId, formId)
            .orderByDesc(SysDynamicFormVersion::getVersion));
    }
    
    @Override
    public SysDynamicFormVersion getVersionById(String versionId) {
        return this.getById(versionId);
    }
    
    @Override
    public SysDynamicFormVersion getCurrentVersionByFormId(String formId) {
        return this.getOne(new LambdaQueryWrapper<SysDynamicFormVersion>()
            .eq(SysDynamicFormVersion::getFormId, formId)
            .eq(SysDynamicFormVersion::getIsCurrent, 1)
            .orderByDesc(SysDynamicFormVersion::getVersion)
            .last("LIMIT 1"));
    }
}

