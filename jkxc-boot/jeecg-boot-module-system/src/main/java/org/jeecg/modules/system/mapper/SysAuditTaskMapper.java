package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysAuditTask;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 审批任务表 Mapper 接口
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
public interface SysAuditTaskMapper extends BaseMapper<SysAuditTask> {
    
    /**
     * 根据订单ID查询所有任务
     */
    List<SysAuditTask> getTasksByOrderId(String orderId);
    
    /**
     * 根据订单ID和步骤ID查询任务
     */
    SysAuditTask getTaskByOrderIdAndStepId(@Param("orderId") String orderId, @Param("stepId") String stepId);
    
    /**
     * 根据订单ID和步骤顺序查询任务
     */
    SysAuditTask getTaskByOrderIdAndStepOrder(@Param("orderId") String orderId, @Param("stepOrder") Integer stepOrder);
    
    /**
     * 查询待审核任务列表（支持个人/团队切换、角色过滤、指定人员过滤）
     */
    IPage<SysAuditTask> getPendingTasks(Page<SysAuditTask> page, 
                                        @Param("taskType") String taskType,
                                        @Param("userId") String userId,
                                        @Param("roleIds") List<String> roleIds,
                                        @Param("isPersonal") Boolean isPersonal,
                                        @Param("assignedUserId") String assignedUserId,
                                        @Param("contractNo") String contractNo,
                                        @Param("orderNo") String orderNo,
                                        @Param("companyName") String companyName,
                                        @Param("dataRoleIds") List<String> dataRoleIds);
    
    /**
     * 查询已审核任务列表
     */
    IPage<SysAuditTask> getApprovedTasks(Page<SysAuditTask> page,
                                         @Param("taskType") String taskType,
                                         @Param("userId") String userId,
                                         @Param("roleIds") List<String> roleIds,
                                         @Param("isPersonal") Boolean isPersonal,
                                         @Param("orderNo") String orderNo,
                                         @Param("companyName") String companyName,
                                         @Param("dataRoleIds") List<String> dataRoleIds);
    
    /**
     * 查询已驳回任务列表
     */
    IPage<SysAuditTask> getRejectedTasks(Page<SysAuditTask> page,
                                         @Param("taskType") String taskType,
                                         @Param("userId") String userId,
                                         @Param("roleIds") List<String> roleIds,
                                         @Param("isPersonal") Boolean isPersonal,
                                         @Param("orderNo") String orderNo,
                                         @Param("companyName") String companyName,
                                         @Param("dataRoleIds") List<String> dataRoleIds);

    /**
     * 首页：统计自然年内任务总数与已完成数（内存聚合改为 SQL）
     */
    Map<String, Object> selectDashboardTaskCompletion(@Param("yearStart") Date yearStart, @Param("yearEnd") Date yearEnd);
}

