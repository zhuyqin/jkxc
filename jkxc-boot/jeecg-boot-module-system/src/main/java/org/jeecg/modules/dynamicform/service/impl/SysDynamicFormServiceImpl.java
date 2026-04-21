package org.jeecg.modules.dynamicform.service.impl;

import org.jeecg.modules.dynamicform.entity.SysDynamicForm;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;
import org.jeecg.modules.dynamicform.mapper.SysDynamicFormMapper;
import org.jeecg.modules.dynamicform.mapper.SysDynamicFormVersionMapper;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormService;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormVersionService;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Date;
import java.util.UUID;
import java.util.List;

/**
 * @Description: 动态表单主表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Service
public class SysDynamicFormServiceImpl extends ServiceImpl<SysDynamicFormMapper, SysDynamicForm> implements ISysDynamicFormService {
    
    @Autowired
    private SysDynamicFormVersionMapper versionMapper;
    
    @Autowired
    private ISysDynamicFormVersionService versionService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createVersion(String formId, String formConfig, String versionDesc) {
        // 获取当前表单
        SysDynamicForm form = this.getById(formId);
        if (form == null) {
            throw new RuntimeException("表单不存在");
        }
        
        // 将旧版本标记为非当前版本
        versionMapper.update(null, 
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysDynamicFormVersion>()
                .eq(SysDynamicFormVersion::getFormId, formId)
                .eq(SysDynamicFormVersion::getIsCurrent, 1)
                .set(SysDynamicFormVersion::getIsCurrent, 0));
        
        // 创建新版本
        int newVersion = form.getCurrentVersion() + 1;
        SysDynamicFormVersion version = new SysDynamicFormVersion();
        version.setFormId(formId);
        version.setVersion(newVersion);
        version.setFormConfig(formConfig);
        version.setVersionDesc(versionDesc);
        version.setIsCurrent(1);
        version.setCreateTime(new Date());
        versionMapper.insert(version);
        
        // 更新表单的当前版本号
        form.setCurrentVersion(newVersion);
        form.setUpdateTime(new Date());
        this.updateById(form);
        
        return version.getId();
    }
    
    @Override
    public SysDynamicFormVersion getFormVersionByBusinessType(String businessType) {
        // 这个方法应该通过绑定表查询，但为了简化，这里返回null
        // 实际实现应该在Controller中通过BindingService查询
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copyForm(String formId, String newFormName, String newFormCode) {
        // 获取源表单
        SysDynamicForm sourceForm = this.getById(formId);
        if (sourceForm == null) {
            throw new RuntimeException("源表单不存在");
        }
        
        // 检查新表单编码是否已存在
        SysDynamicForm existing = this.getOne(new LambdaQueryWrapper<SysDynamicForm>()
            .eq(SysDynamicForm::getFormCode, newFormCode));
        if (existing != null) {
            throw new RuntimeException("表单编码已存在：" + newFormCode);
        }
        
        // 创建新表单
        SysDynamicForm newForm = new SysDynamicForm();
        newForm.setFormName(newFormName);
        newForm.setFormCode(newFormCode);
        newForm.setDescription(sourceForm.getDescription() + "（复制）");
        newForm.setStatus(sourceForm.getStatus());
        newForm.setCurrentVersion(0); // 版本号从0开始，创建第一个版本后会变成1
        newForm.setCreateTime(new Date());
        this.save(newForm);
        
        // 获取源表单的所有版本
        List<SysDynamicFormVersion> sourceVersions = versionService.list(
            new LambdaQueryWrapper<SysDynamicFormVersion>()
                .eq(SysDynamicFormVersion::getFormId, formId)
                .orderByAsc(SysDynamicFormVersion::getVersion));
        
        // 复制所有版本，版本号从1开始重新编号
        int newVersionNumber = 1;
        SysDynamicFormVersion currentVersion = null;
        
        for (SysDynamicFormVersion sourceVersion : sourceVersions) {
            SysDynamicFormVersion newVersion = new SysDynamicFormVersion();
            newVersion.setFormId(newForm.getId());
            newVersion.setVersion(newVersionNumber);
            newVersion.setFormConfig(sourceVersion.getFormConfig());
            newVersion.setVersionDesc(sourceVersion.getVersionDesc());
            newVersion.setIsCurrent(sourceVersion.getIsCurrent() == 1 ? 1 : 0);
            newVersion.setCreateTime(new Date());
            versionMapper.insert(newVersion);
            
            if (sourceVersion.getIsCurrent() == 1) {
                currentVersion = newVersion;
            }
            
            newVersionNumber++;
        }
        
        // 如果没有当前版本，将第一个版本设为当前版本
        if (currentVersion == null && !sourceVersions.isEmpty()) {
            SysDynamicFormVersion firstVersion = versionMapper.selectOne(
                new LambdaQueryWrapper<SysDynamicFormVersion>()
                    .eq(SysDynamicFormVersion::getFormId, newForm.getId())
                    .eq(SysDynamicFormVersion::getVersion, 1));
            if (firstVersion != null) {
                firstVersion.setIsCurrent(1);
                versionMapper.updateById(firstVersion);
                currentVersion = firstVersion;
            }
        }
        
        // 更新新表单的当前版本号
        if (currentVersion != null) {
            newForm.setCurrentVersion(currentVersion.getVersion());
        } else {
            newForm.setCurrentVersion(0);
        }
        this.updateById(newForm);
        
        return newForm.getId();
    }
}

