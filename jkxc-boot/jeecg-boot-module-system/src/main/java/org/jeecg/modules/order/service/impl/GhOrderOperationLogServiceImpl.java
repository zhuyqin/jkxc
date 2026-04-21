package org.jeecg.modules.order.service.impl;

import java.util.Date;
import java.util.List;

import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrderOperationLog;
import org.jeecg.modules.order.mapper.GhOrderOperationLogMapper;
import org.jeecg.modules.order.service.IGhOrderOperationLogService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 订单操作记录
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Slf4j
@Service
public class GhOrderOperationLogServiceImpl extends ServiceImpl<GhOrderOperationLogMapper, GhOrderOperationLog> implements IGhOrderOperationLogService {
    
    @Override
    public List<GhOrderOperationLog> getLogsByOrderId(String orderId) {
        // 使用 baseMapper 而不是直接注入的 mapper，确保使用正确的映射
        return baseMapper.getLogsByOrderId(orderId);
    }
    
    @Override
    public void saveOperationLog(String orderId, String operationType, String operationDesc, 
                                  String operatorName, String beforeData, String afterData, 
                                  String changedFields, String remarks) {
        GhOrderOperationLog operationLog = new GhOrderOperationLog();
        operationLog.setOrderId(orderId);
        operationLog.setOperationType(operationType);
        operationLog.setOperationDesc(operationDesc);
        operationLog.setOperatorName(operatorName);
        operationLog.setBeforeData(beforeData);
        operationLog.setAfterData(afterData);
        operationLog.setChangedFields(changedFields);
        operationLog.setRemarks(remarks);
        operationLog.setCreateTime(new Date());
        
        // 获取当前登录用户
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                operationLog.setOperatorId(sysUser.getId());
                if (oConvertUtils.isEmpty(operatorName)) {
                    operationLog.setOperatorName(sysUser.getRealname());
                }
                operationLog.setCreateBy(sysUser.getUsername());
            }
        } catch (Exception e) {
            // 忽略获取用户信息的异常
            log.debug("获取当前登录用户信息失败", e);
        }
        
        try {
            this.save(operationLog);
        } catch (Exception e) {
            // 记录操作日志失败不应该影响主流程，但需要记录错误日志
            log.error("保存订单操作记录失败，订单ID: {}, 操作类型: {}, 操作描述: {}", 
                     orderId, operationType, operationDesc, e);
        }
    }
}

