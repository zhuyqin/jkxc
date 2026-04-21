package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;

import java.util.List;

/**
 * <p>
 * 审核流程表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface ISysAuditProcessService extends IService<SysAuditProcess> {

    /**
     * 保存审核流程（包含步骤）
     * @param process 审核流程
     * @param steps 审核步骤列表
     * @return
     */
    boolean saveMain(SysAuditProcess process, List<SysAuditStep> steps);

    /**
     * 更新审核流程（包含步骤）
     * @param process 审核流程
     * @param steps 审核步骤列表
     * @return
     */
    boolean updateMain(SysAuditProcess process, List<SysAuditStep> steps);

    /**
     * 根据流程ID查询流程及步骤
     * @param id 流程ID
     * @return
     */
    SysAuditProcess getProcessWithSteps(String id);
    
    /**
     * 保存步骤表单配置
     * @param processId 流程ID
     * @param stepFormConfigs 表单配置映射，key为stepOrder，value为表单配置对象
     */
    void saveStepFormConfigs(String processId, JSONObject stepFormConfigs);
    
    /**
     * 删除流程及其所有关联数据（绑定记录、步骤、表单配置等）
     * @param processId 流程ID
     * @return 是否删除成功
     */
    boolean removeProcessWithRelations(String processId);

}

