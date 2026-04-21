package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.entity.SysAuditStepForm;
import org.jeecg.modules.system.entity.SysAuditFormIndicator;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.mapper.SysAuditProcessMapper;
import org.jeecg.modules.system.mapper.SysAuditStepMapper;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.service.ISysAuditProcessService;
import org.jeecg.modules.system.service.ISysAuditStepService;
import org.jeecg.modules.system.service.ISysAuditStepFormService;
import org.jeecg.modules.system.service.ISysAuditFormIndicatorService;
import org.jeecg.modules.system.service.ISysAuditProcessBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 审核流程表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
@Slf4j
public class SysAuditProcessServiceImpl extends ServiceImpl<SysAuditProcessMapper, SysAuditProcess> implements ISysAuditProcessService {

    @Autowired
    private SysAuditStepMapper sysAuditStepMapper;
    
    @Autowired
    private ISysAuditStepService sysAuditStepService;
    
    @Autowired
    private SysRoleMapper sysRoleMapper;
    
    @Autowired
    private ISysAuditStepFormService sysAuditStepFormService;
    
    @Autowired
    private ISysAuditFormIndicatorService sysAuditFormIndicatorService;
    
    @Autowired
    private ISysAuditProcessBindingService sysAuditProcessBindingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMain(SysAuditProcess process, List<SysAuditStep> steps) {
        // 保存流程
        process.setCreateTime(new Date());
        this.save(process);
        
        // 保存步骤
        // 注意：相同stepOrder的多个步骤表示这些角色需要并行审核
        if (steps != null && !steps.isEmpty()) {
            for (SysAuditStep step : steps) {
                step.setProcessId(process.getId());
                // stepOrder已经在前端设置好，直接使用
                if (step.getStepOrder() == null) {
                    step.setStepOrder(1);
                }
                step.setCreateTime(new Date());
                
                // 根据角色ID获取角色名称（如果前端没有传）
                if (oConvertUtils.isNotEmpty(step.getRoleId())) {
                    if (oConvertUtils.isEmpty(step.getRoleName())) {
                        SysRole role = sysRoleMapper.selectById(step.getRoleId());
                        if (role != null) {
                            step.setRoleName(role.getRoleName());
                        }
                    }
                }
                
                sysAuditStepMapper.insert(step);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMain(SysAuditProcess process, List<SysAuditStep> steps) {
        // 更新流程
        process.setUpdateTime(new Date());
        this.updateById(process);
        
        // 删除原有步骤
        sysAuditStepService.deleteByProcessId(process.getId());
        
        // 保存新步骤
        // 注意：相同stepOrder的多个步骤表示这些角色需要并行审核
        if (steps != null && !steps.isEmpty()) {
            for (SysAuditStep step : steps) {
                step.setId(null); // 清空ID，让系统重新生成
                step.setProcessId(process.getId());
                // stepOrder已经在前端设置好，直接使用
                if (step.getStepOrder() == null) {
                    step.setStepOrder(1);
                }
                step.setCreateTime(new Date());
                
                // 根据角色ID获取角色名称（如果前端没有传）
                if (oConvertUtils.isNotEmpty(step.getRoleId())) {
                    if (oConvertUtils.isEmpty(step.getRoleName())) {
                        SysRole role = sysRoleMapper.selectById(step.getRoleId());
                        if (role != null) {
                            step.setRoleName(role.getRoleName());
                        }
                    }
                }
                
                sysAuditStepMapper.insert(step);
            }
        }
        
        return true;
    }

    @Override
    public SysAuditProcess getProcessWithSteps(String id) {
        SysAuditProcess process = this.getById(id);
        if (process != null) {
            List<SysAuditStep> steps = sysAuditStepService.getStepsByProcessId(id);
            // 按stepOrder分组，相同stepOrder的步骤合并（支持一个步骤多个角色）
            process.setSteps(steps);
        }
        return process;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStepFormConfigs(String processId, JSONObject stepFormConfigs) {
        if (stepFormConfigs == null || stepFormConfigs.isEmpty()) {
            return;
        }
        
        // 获取流程的所有步骤
        List<SysAuditStep> allSteps = sysAuditStepService.getStepsByProcessId(processId);
        
        // 遍历表单配置映射
        for (String configKey : stepFormConfigs.keySet()) {
            try {
                // 解析配置key：可能是stepOrder或stepOrder_roleId格式
                Integer stepOrder = null;
                String targetRoleId = null;
                
                if (configKey.contains("_")) {
                    // 格式：stepOrder_roleId
                    String[] parts = configKey.split("_", 2);
                    stepOrder = Integer.parseInt(parts[0]);
                    targetRoleId = parts[1];
                } else {
                    // 格式：stepOrder（全部角色共享）
                    stepOrder = Integer.parseInt(configKey);
                }
                
                // 找到该stepOrder对应的步骤（如果指定了角色，则找到对应角色的步骤）
                SysAuditStep targetStep = null;
                for (SysAuditStep step : allSteps) {
                    if (step.getStepOrder() != null && step.getStepOrder().equals(stepOrder)) {
                        if (targetRoleId == null) {
                            // 全部角色共享，使用第一个步骤
                            targetStep = step;
                            break;
                        } else {
                            // 指定了角色，找到对应角色的步骤
                            if (targetRoleId.equals(step.getRoleId())) {
                                targetStep = step;
                                break;
                            }
                        }
                    }
                }
                
                if (targetStep == null) {
                    log.warn("未找到stepOrder={}, roleId={}的步骤，跳过表单配置保存", stepOrder, targetRoleId);
                    continue;
                }
                
                // 获取该步骤的表单配置数据
                JSONObject configData = stepFormConfigs.getJSONObject(configKey);
                if (configData == null) {
                    continue;
                }
                
                // 如果指定了角色，且该步骤已有表单配置（其他角色配置的），需要创建新的表单配置
                // 否则更新现有表单配置
                if (targetRoleId != null) {
                    // 检查该步骤是否已有表单配置
                    SysAuditStepForm existingForm = sysAuditStepFormService.getFormByStepId(targetStep.getId());
                    if (existingForm != null) {
                        // 已存在表单配置，为该角色创建新的表单配置
                        // 注意：这里需要为每个角色创建独立的表单配置
                        // 但由于SysAuditStepForm表是按stepId关联的，一个stepId只能有一个表单配置
                        // 所以我们需要为每个角色创建独立的步骤记录，或者使用其他方式存储
                        // 这里先使用现有逻辑，后续可以优化
                        log.info("步骤 {} 已有表单配置，将更新为角色 {} 的配置", targetStep.getId(), targetRoleId);
                    }
                }
                
                // 保存表单配置
                JSONObject formConfigObj = configData.getJSONObject("formConfig");
                if (formConfigObj != null) {
                    SysAuditStepForm form = new SysAuditStepForm();
                    form.setFormName(formConfigObj.getString("formName"));
                    form.setFormConfig(formConfigObj.getString("formConfig"));
                    Integer remarkRequired = formConfigObj.getInteger("remarkRequired");
                    form.setRemarkRequired(remarkRequired != null ? remarkRequired : 1);
                    
                    boolean formSaved = sysAuditStepFormService.saveOrUpdateForm(targetStep.getId(), form);
                    if (formSaved) {
                        // 保存指标配置
                        JSONArray indicatorsArray = configData.getJSONArray("indicators");
                        if (indicatorsArray != null && !indicatorsArray.isEmpty()) {
                            SysAuditStepForm savedForm = sysAuditStepFormService.getFormByStepId(targetStep.getId());
                            if (savedForm != null) {
                                // 删除旧指标
                                sysAuditFormIndicatorService.deleteByStepFormId(savedForm.getId());
                                
                                // 保存新指标
                                for (int i = 0; i < indicatorsArray.size(); i++) {
                                    JSONObject indicatorJson = indicatorsArray.getJSONObject(i);
                                    if (indicatorJson != null) {
                                        SysAuditFormIndicator indicator = new SysAuditFormIndicator();
                                        indicator.setStepFormId(savedForm.getId());
                                        indicator.setIndicatorType(indicatorJson.getString("indicatorType"));
                                        indicator.setIndicatorName(indicatorJson.getString("indicatorName"));
                                        
                                        // 将config对象转为JSON字符串
                                        JSONObject configObj = indicatorJson.getJSONObject("config");
                                        if (configObj != null) {
                                            indicator.setIndicatorConfig(configObj.toJSONString());
                                        } else {
                                            // 如果config是空对象，保存空字符串
                                            indicator.setIndicatorConfig("{}");
                                        }
                                        
                                        Integer sortOrder = indicatorJson.getInteger("sortOrder");
                                        indicator.setSortOrder(sortOrder != null ? sortOrder : i);
                                        indicator.setCreateTime(new Date());
                                        
                                        sysAuditFormIndicatorService.save(indicator);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("保存configKey={}的表单配置失败", configKey, e);
                // 继续处理下一个配置，不中断整个流程
            }
        }
    }

    /**
     * 删除流程及其所有关联数据
     * @param processId 流程ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeProcessWithRelations(String processId) {
        // 1. 删除流程的所有绑定记录
        sysAuditProcessBindingService.deleteByProcessId(processId);
        log.info("删除流程 {} 的绑定记录", processId);
        
        // 2. 删除流程的所有步骤（会级联删除步骤的表单配置和指标）
        sysAuditStepService.deleteByProcessId(processId);
        log.info("删除流程 {} 的步骤记录", processId);
        
        // 3. 删除流程本身
        boolean removed = this.removeById(processId);
        if (removed) {
            log.info("删除流程 {} 成功", processId);
        } else {
            log.warn("删除流程 {} 失败", processId);
        }
        
        return removed;
    }

}

