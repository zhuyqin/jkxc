package org.jeecg.modules.system.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAuditTask;
import org.jeecg.modules.system.service.ISysAuditTaskService;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.apache.shiro.SecurityUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 审批任务表 前端控制器
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@RestController
@RequestMapping("/sys/auditTask")
@Slf4j
public class SysAuditTaskController {

    @Autowired
    private ISysAuditTaskService sysAuditTaskService;
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysRoleService sysRoleService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysUserRoleService sysUserRoleService;

    /**
     * 查询待审核任务列表（一次性任务）
     * @param taskType 任务类型：once-一次性任务，recurring-周期任务
     * @param pageNo
     * @param pageSize
     * @param isPersonal 是否个人模式：true-个人，false-团队
     * @return
     */
    @RequestMapping(value = "/getPendingTasks", method = RequestMethod.GET)
    public Result<IPage<SysAuditTask>> getPendingTasks(
            @RequestParam(name="taskType",required=true) String taskType,
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name="isPersonal", defaultValue="true") Boolean isPersonal,
            @RequestParam(name="contractNo", required=false) String contractNo,
            @RequestParam(name="orderNo", required=false) String orderNo,
            @RequestParam(name="companyName", required=false) String companyName) {
        Result<IPage<SysAuditTask>> result = new Result<>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            // 获取当前用户的角色ID列表
            List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(sysUser.getUsername());
            
            // 计算数据角色ID列表（用于数据权限过滤）
            List<String> dataRoleIds = new ArrayList<>();
            if (sysRoleService != null && roleIds != null && !roleIds.isEmpty()) {
                for (String roleId : roleIds) {
                    org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
                    if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
                        // 递归查询该角色及其所有下级角色的ID
                        List<String> allSubRoleIds = getAllSubRoleIds(roleId);
                        dataRoleIds.addAll(allSubRoleIds);
                    }
                }
            }
            
            Page<SysAuditTask> page = new Page<>(pageNo, pageSize);
            IPage<SysAuditTask> pageList = sysAuditTaskService.getPendingTasks(
                page, taskType, sysUser.getId(), roleIds, isPersonal, null, contractNo, orderNo, companyName, dataRoleIds);
            
            result.setSuccess(true);
            result.setResult(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 查询已审核任务列表
     */
    @RequestMapping(value = "/getApprovedTasks", method = RequestMethod.GET)
    public Result<IPage<SysAuditTask>> getApprovedTasks(
            @RequestParam(name="taskType",required=true) String taskType,
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name="isPersonal", defaultValue="true") Boolean isPersonal,
            @RequestParam(name="orderNo", required=false) String orderNo,
            @RequestParam(name="companyName", required=false) String companyName) {
        Result<IPage<SysAuditTask>> result = new Result<>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(sysUser.getUsername());
            
            // 计算数据角色ID列表（用于数据权限过滤）
            List<String> dataRoleIds = new ArrayList<>();
            if (sysRoleService != null && roleIds != null && !roleIds.isEmpty()) {
                for (String roleId : roleIds) {
                    org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
                    if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
                        List<String> allSubRoleIds = getAllSubRoleIds(roleId);
                        dataRoleIds.addAll(allSubRoleIds);
                    }
                }
            }
            
            Page<SysAuditTask> page = new Page<>(pageNo, pageSize);
            IPage<SysAuditTask> pageList = sysAuditTaskService.getApprovedTasks(
                page, taskType, sysUser.getId(), roleIds, isPersonal, orderNo, companyName, dataRoleIds);
            
            result.setSuccess(true);
            result.setResult(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 查询已驳回任务列表
     */
    @RequestMapping(value = "/getRejectedTasks", method = RequestMethod.GET)
    public Result<IPage<SysAuditTask>> getRejectedTasks(
            @RequestParam(name="taskType",required=true) String taskType,
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name="isPersonal", defaultValue="true") Boolean isPersonal,
            @RequestParam(name="orderNo", required=false) String orderNo,
            @RequestParam(name="companyName", required=false) String companyName) {
        Result<IPage<SysAuditTask>> result = new Result<>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(sysUser.getUsername());
            
            // 计算数据角色ID列表（用于数据权限过滤）
            List<String> dataRoleIds = new ArrayList<>();
            if (sysRoleService != null && roleIds != null && !roleIds.isEmpty()) {
                for (String roleId : roleIds) {
                    org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
                    if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
                        List<String> allSubRoleIds = getAllSubRoleIds(roleId);
                        dataRoleIds.addAll(allSubRoleIds);
                    }
                }
            }
            
            Page<SysAuditTask> page = new Page<>(pageNo, pageSize);
            IPage<SysAuditTask> pageList = sysAuditTaskService.getRejectedTasks(
                page, taskType, sysUser.getId(), roleIds, isPersonal, orderNo, companyName, dataRoleIds);
            
            result.setSuccess(true);
            result.setResult(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 通过id查询任务详情（包含审核表单配置）
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysAuditTask> queryById(@RequestParam(name="id",required=true) String id) {
        Result<SysAuditTask> result = new Result<>();
        try {
            SysAuditTask task = sysAuditTaskService.getById(id);
            if (task == null) {
                result.error500("未找到对应实体");
            } else {
                result.setResult(task);
                result.setSuccess(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败");
        }
        return result;
    }

    /**
     * 执行审核操作
     * @param jsonObject 包含taskId、auditResult、auditData、auditRemark、rejectReason
     * @return
     */
    @RequestMapping(value = "/executeAudit", method = RequestMethod.POST)
    public Result<?> executeAudit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String taskId = jsonObject.getString("taskId");
            String auditResult = jsonObject.getString("auditResult");
            String auditData = jsonObject.getString("auditData");
            String auditRemark = jsonObject.getString("auditRemark");
            String rejectReason = jsonObject.getString("rejectReason");
            
            if (oConvertUtils.isEmpty(taskId) || oConvertUtils.isEmpty(auditResult)) {
                result.error500("参数不完整");
                return result;
            }
            
            // 驳回时必须填写驳回原因
            if ("returned".equals(auditResult) && oConvertUtils.isEmpty(rejectReason)) {
                result.error500("驳回时必须填写驳回原因");
                return result;
            }
            
            boolean success = sysAuditTaskService.executeAudit(taskId, auditResult, auditData, auditRemark, rejectReason);
            if (success) {
                result.success("审核成功！");
            } else {
                result.error500("审核失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 驳回后重新提交
     * @param jsonObject 包含orderId、originalTaskId
     * @return
     */
    @RequestMapping(value = "/resubmit", method = RequestMethod.POST)
    public Result<?> resubmit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String orderId = jsonObject.getString("orderId");
            String originalTaskId = jsonObject.getString("originalTaskId");
            
            if (oConvertUtils.isEmpty(orderId) || oConvertUtils.isEmpty(originalTaskId)) {
                result.error500("参数不完整");
                return result;
            }
            
            SysAuditTask newTask = sysAuditTaskService.resubmitAfterReject(orderId, originalTaskId);
            if (newTask != null) {
                result.success("重新提交成功！");
            } else {
                result.error500("重新提交失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询订单的完整审核流程进度（包含所有任务、驳回重新提交等）
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/getOrderAuditProgress", method = RequestMethod.GET)
    public Result<JSONObject> getOrderAuditProgress(@RequestParam(name="orderId",required=true) String orderId) {
        Result<JSONObject> result = new Result<>();
        try {
            JSONObject progress = new JSONObject();
            
            // 获取订单的所有审核任务
            List<SysAuditTask> allTasks = sysAuditTaskService.getTasksByOrderId(orderId);
            
            // 按步骤顺序分组
            Map<Integer, List<SysAuditTask>> tasksByStep = allTasks.stream()
                .collect(Collectors.groupingBy(SysAuditTask::getStepOrder));
            
            // 构建流程进度数据
            List<JSONObject> steps = new ArrayList<>();
            for (Map.Entry<Integer, List<SysAuditTask>> entry : tasksByStep.entrySet()) {
                Integer stepOrder = entry.getKey();
                List<SysAuditTask> stepTasks = entry.getValue();
                
                JSONObject stepInfo = new JSONObject();
                stepInfo.put("stepOrder", stepOrder);
                
                // 判断步骤状态
                boolean allApproved = stepTasks.stream()
                    .allMatch(t -> "approved".equals(t.getTaskStatus()));
                boolean hasRejected = stepTasks.stream()
                    .anyMatch(t -> "rejected".equals(t.getTaskStatus()));
                boolean hasPending = stepTasks.stream()
                    .anyMatch(t -> "pending".equals(t.getTaskStatus()));
                
                if (allApproved) {
                    stepInfo.put("status", "approved");
                } else if (hasRejected) {
                    stepInfo.put("status", "rejected");
                } else if (hasPending) {
                    stepInfo.put("status", "pending");
                } else {
                    stepInfo.put("status", "unknown");
                }
                
                // 任务列表
                List<JSONObject> taskList = new ArrayList<>();
                for (SysAuditTask task : stepTasks) {
                    JSONObject taskInfo = new JSONObject();
                    taskInfo.put("id", task.getId());
                    taskInfo.put("roleName", task.getCurrentRoleName());
                    taskInfo.put("taskStatus", task.getTaskStatus());
                    taskInfo.put("taskType", task.getTaskType()); // 添加任务类型
                    taskInfo.put("auditResult", task.getAuditResult());
                    taskInfo.put("auditRemark", task.getAuditRemark());
                    taskInfo.put("rejectReason", task.getRejectReason());
                    taskInfo.put("auditUserName", task.getAuditUserName());
                    taskInfo.put("auditTime", task.getAuditTime());
                    taskInfo.put("assignedUserName", task.getAssignedUserName());
                    taskList.add(taskInfo);
                }
                stepInfo.put("tasks", taskList);
                
                steps.add(stepInfo);
            }
            
            progress.put("steps", steps);
            progress.put("totalSteps", tasksByStep.size());
            
            result.setResult(progress);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 递归查询指定角色及其所有下级角色的ID
     * @param roleId 角色ID
     * @return 角色ID列表（包含当前角色及其所有下级角色）
     */
    private List<String> getAllSubRoleIds(String roleId) {
        List<String> result = new ArrayList<>();
        if (oConvertUtils.isEmpty(roleId) || sysRoleService == null) {
            return result;
        }
        
        // 添加当前角色ID
        result.add(roleId);
        
        // 查询直接下级角色（parent_id = roleId 且 role_type = 2）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        query.eq(org.jeecg.modules.system.entity.SysRole::getParentId, roleId);
        query.eq(org.jeecg.modules.system.entity.SysRole::getRoleType, 2); // 只查询数据角色
        query.eq(org.jeecg.modules.system.entity.SysRole::getStatus, 1); // 只查询启用的角色
        List<org.jeecg.modules.system.entity.SysRole> children = sysRoleService.list(query);
        
        // 递归查询每个子角色的下级角色
        if (children != null && !children.isEmpty()) {
            for (org.jeecg.modules.system.entity.SysRole child : children) {
                List<String> subRoleIds = getAllSubRoleIds(child.getId());
                result.addAll(subRoleIds);
            }
        }
        
        return result;
    }
}

