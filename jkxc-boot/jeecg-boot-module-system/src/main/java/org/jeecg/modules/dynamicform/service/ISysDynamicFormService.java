package org.jeecg.modules.dynamicform.service;

import org.jeecg.modules.dynamicform.entity.SysDynamicForm;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;

import java.util.List;

/**
 * @Description: 动态表单主表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface ISysDynamicFormService extends IService<SysDynamicForm> {
    
    /**
     * 创建新版本
     * @param formId 表单ID
     * @param formConfig 表单配置JSON
     * @param versionDesc 版本描述
     * @return 版本ID
     */
    String createVersion(String formId, String formConfig, String versionDesc);
    
    /**
     * 根据业务类型获取绑定的表单版本
     * @param businessType 业务类型
     * @return 表单版本信息（包含表单配置）
     */
    SysDynamicFormVersion getFormVersionByBusinessType(String businessType);
    
    /**
     * 复制表单（包括所有版本，版本号从1开始）
     * @param formId 源表单ID
     * @param newFormName 新表单名称
     * @param newFormCode 新表单编码
     * @return 新表单ID
     */
    String copyForm(String formId, String newFormName, String newFormCode);
}

