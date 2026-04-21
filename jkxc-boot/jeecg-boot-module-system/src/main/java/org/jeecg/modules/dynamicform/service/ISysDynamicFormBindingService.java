package org.jeecg.modules.dynamicform.service;

import org.jeecg.modules.dynamicform.entity.SysDynamicFormBinding;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 业务类型表单绑定表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface ISysDynamicFormBindingService extends IService<SysDynamicFormBinding> {
    
    /**
     * 根据业务类型获取绑定信息
     * @param businessType 业务类型
     * @return 绑定信息
     */
    SysDynamicFormBinding getBindingByBusinessType(String businessType);
    
    /**
     * 绑定业务类型到表单版本
     * @param businessType 业务类型
     * @param formId 表单ID
     * @param versionId 版本ID
     * @param status 状态（1-启用，0-禁用）
     * @return 绑定ID
     */
    String bindBusinessType(String businessType, String formId, String versionId, Integer status);
    
    /**
     * 根据表单ID删除所有绑定关系
     * @param formId 表单ID
     */
    void deleteBindingsByFormId(String formId);
}

