package org.jeecg.modules.order.service;

import org.jeecg.modules.order.entity.GhOrderFormData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 订单表单数据表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface IGhOrderFormDataService extends IService<GhOrderFormData> {
    
    /**
     * 根据订单ID获取表单数据
     * @param orderId 订单ID
     * @return 表单数据
     */
    GhOrderFormData getFormDataByOrderId(String orderId);
    
    /**
     * 保存或更新订单表单数据
     * @param orderId 订单ID
     * @param formId 表单ID
     * @param versionId 版本ID
     * @param formData 表单数据JSON
     * @param formConfig 表单配置JSON（可选，用于保存创建时的表单配置）
     */
    void saveOrUpdateFormData(String orderId, String formId, String versionId, String formData, String formConfig);
}

