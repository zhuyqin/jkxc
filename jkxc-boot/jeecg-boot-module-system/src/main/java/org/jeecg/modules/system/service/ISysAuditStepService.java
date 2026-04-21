package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAuditStep;

import java.util.List;

/**
 * <p>
 * 审核步骤表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface ISysAuditStepService extends IService<SysAuditStep> {

    /**
     * 根据流程ID查询步骤列表
     * @param processId 流程ID
     * @return
     */
    List<SysAuditStep> getStepsByProcessId(String processId);

    /**
     * 根据流程ID删除步骤
     * @param processId 流程ID
     * @return
     */
    boolean deleteByProcessId(String processId);

}

