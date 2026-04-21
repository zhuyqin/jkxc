package org.jeecg.modules.order.service.impl;

import org.jeecg.modules.order.entity.GhOrderFormData;
import org.jeecg.modules.order.mapper.GhOrderFormDataMapper;
import org.jeecg.modules.order.service.IGhOrderFormDataService;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormVersionService;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Date;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description: 订单表单数据表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Service
public class GhOrderFormDataServiceImpl extends ServiceImpl<GhOrderFormDataMapper, GhOrderFormData> implements IGhOrderFormDataService {
    
    @Autowired
    private ISysDynamicFormVersionService sysDynamicFormVersionService;
    
    @Override
    public GhOrderFormData getFormDataByOrderId(String orderId) {
        return this.getOne(new LambdaQueryWrapper<GhOrderFormData>()
            .eq(GhOrderFormData::getOrderId, orderId));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateFormData(String orderId, String formId, String versionId, String formData, String formConfig) {
        // 如果formId为空，从版本表查询
        if (oConvertUtils.isEmpty(formId) && oConvertUtils.isNotEmpty(versionId)) {
            SysDynamicFormVersion version = sysDynamicFormVersionService.getById(versionId);
            if (version != null) {
                formId = version.getFormId();
            }
        }
        
        // 将formData转换为JSON字符串（如果已经是字符串则直接使用）
        String formDataStr = formData;
        if (formData != null && !formData.startsWith("{") && !formData.startsWith("[")) {
            try {
                formDataStr = JSONObject.toJSONString(formData);
            } catch (Exception e) {
                formDataStr = formData;
            }
        }
        
        // 处理formConfig：如果为空，尝试从版本表获取（兼容旧数据）
        String formConfigStr = formConfig;
        if (oConvertUtils.isEmpty(formConfigStr) && oConvertUtils.isNotEmpty(versionId)) {
            SysDynamicFormVersion version = sysDynamicFormVersionService.getById(versionId);
            if (version != null && oConvertUtils.isNotEmpty(version.getFormConfig())) {
                formConfigStr = version.getFormConfig();
            }
        }
        
        GhOrderFormData existing = this.getFormDataByOrderId(orderId);
        
        if (existing != null) {
            // 更新
            existing.setFormId(formId); // 更新formId
            existing.setVersionId(versionId); // 更新versionId
            // 更新版本号
            if (oConvertUtils.isNotEmpty(versionId)) {
                SysDynamicFormVersion version = sysDynamicFormVersionService.getById(versionId);
                if (version != null) {
                    existing.setVersion(version.getVersion());
                }
            }
            existing.setFormData(formDataStr);
            // 只有在提供了formConfig时才更新（避免覆盖已保存的配置）
            if (oConvertUtils.isNotEmpty(formConfigStr)) {
                existing.setFormConfig(formConfigStr);
            }
            existing.setUpdateTime(new Date());
            this.updateById(existing);
        } else {
            // 新增
            GhOrderFormData orderFormData = new GhOrderFormData();
            orderFormData.setOrderId(orderId);
            orderFormData.setFormId(formId);
            orderFormData.setVersionId(versionId);
            
            // 从版本表获取版本号
            if (oConvertUtils.isNotEmpty(versionId)) {
                SysDynamicFormVersion version = sysDynamicFormVersionService.getById(versionId);
                if (version != null) {
                    orderFormData.setVersion(version.getVersion());
                } else {
                    orderFormData.setVersion(1);
                }
            } else {
                orderFormData.setVersion(1);
            }
            
            orderFormData.setFormData(formDataStr);
            orderFormData.setFormConfig(formConfigStr); // 保存表单配置
            orderFormData.setCreateTime(new Date());
            this.save(orderFormData);
        }
    }
}

