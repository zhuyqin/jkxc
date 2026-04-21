package org.jeecg.modules.dynamicform.service;

import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 动态表单版本表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface ISysDynamicFormVersionService extends IService<SysDynamicFormVersion> {
    
    /**
     * 根据表单ID查询所有版本
     * @param formId 表单ID
     * @return 版本列表
     */
    List<SysDynamicFormVersion> getVersionsByFormId(String formId);
    
    /**
     * 根据版本ID获取表单配置
     * @param versionId 版本ID
     * @return 表单版本信息
     */
    SysDynamicFormVersion getVersionById(String versionId);
    
    /**
     * 根据表单ID获取当前版本（isCurrent=1）
     * @param formId 表单ID
     * @return 当前版本信息，如果没有则返回null
     */
    SysDynamicFormVersion getCurrentVersionByFormId(String formId);
}

