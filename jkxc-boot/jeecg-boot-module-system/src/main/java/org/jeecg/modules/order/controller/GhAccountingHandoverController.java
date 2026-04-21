package org.jeecg.modules.order.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhAccountingHandover;
import org.jeecg.modules.order.service.IGhAccountingHandoverService;
import org.jeecg.modules.order.service.IGhAccountingHandoverAuditService;
import org.jeecg.modules.order.enums.AuditStatusEnum;
import org.jeecg.modules.order.enums.HandoverStatusEnum;
import org.apache.shiro.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.jeecg.boot.starter.lock.annotation.JRepeat;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.order.util.AccountingContractServiceStaffJsonUtil;
import org.jeecg.modules.system.service.ISysAuditTaskService;

/**
 * <p>
 * 代账交接表 前端控制器
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-02
 */
@RestController
@RequestMapping("/order/accountingHandover")
@Slf4j
public class GhAccountingHandoverController {

    @Autowired
    private IGhAccountingHandoverService ghAccountingHandoverService;
    
    @Autowired
    private IGhAccountingHandoverAuditService ghAccountingHandoverAuditService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderAuditService ghOrderAuditService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhOrderService ghOrderService;
    
    @Autowired
    private org.jeecg.modules.order.service.IGhAccountingContractService ghAccountingContractService;

    @Autowired(required = false)
    private ISysAuditTaskService sysAuditTaskService;

    /**
     * 分页列表查询
     * @param handoverStatus 交接状态
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<GhAccountingHandover>> queryPageList(
            @RequestParam(name="handoverStatus", required=false) String handoverStatus,
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            HttpServletRequest req) {
        Result<IPage<GhAccountingHandover>> result = new Result<>();
        
        QueryWrapper<GhAccountingHandover> queryWrapper = QueryGenerator.initQueryWrapper(new GhAccountingHandover(), req.getParameterMap());
        queryWrapper.eq("del_flag", 0);
        
        // 如果指定了交接状态，按状态过滤
        if (oConvertUtils.isNotEmpty(handoverStatus)) {
            queryWrapper.eq("handover_status", handoverStatus);
        }
        
        Page<GhAccountingHandover> page = new Page<>(pageNo, pageSize);
        IPage<GhAccountingHandover> pageList = ghAccountingHandoverService.page(page, queryWrapper);
        
        // 为每个代账交接查询审核记录，用于前端显示
        if (pageList != null && pageList.getRecords() != null) {
            for (GhAccountingHandover handover : pageList.getRecords()) {
                List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit> audits = 
                    ghAccountingHandoverAuditService.getAuditsByHandoverId(handover.getId());
                handover.setHandoverAudits(audits);
            }
        }
        
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 通过id查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<GhAccountingHandover> queryById(@RequestParam(name="id",required=true) String id) {
        Result<GhAccountingHandover> result = new Result<>();
        GhAccountingHandover handover = ghAccountingHandoverService.getById(id);
        if (handover != null) {
            // 查询审核记录
            List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit> audits = 
                ghAccountingHandoverAuditService.getAuditsByHandoverId(id);
            handover.setHandoverAudits(audits);
        }
        result.setResult(handover);
        result.setSuccess(true);
        return result;
    }

    /**
     * 代账交接审核
     * @param jsonObject 包含代账交接ID、角色ID、审核状态、审核备注
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['handoverId'] + '_' + #jsonObject['roleId']")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> audit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String handoverId = jsonObject.getString("handoverId");
            String roleId = jsonObject.getString("roleId");
            String auditStatus = jsonObject.getString("auditStatus"); // approved-通过, rejected-驳回
            String auditRemark = jsonObject.getString("auditRemark");
            
            if (oConvertUtils.isEmpty(handoverId) || oConvertUtils.isEmpty(roleId) || oConvertUtils.isEmpty(auditStatus)) {
                result.error500("参数不完整");
                return result;
            }
            
            GhAccountingHandover handover = ghAccountingHandoverService.getById(handoverId);
            if (handover == null) {
                result.error500("代账交接记录不存在");
                return result;
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            // 执行审核
            boolean success = ghAccountingHandoverAuditService.executeAudit(handoverId, roleId, auditStatus, auditRemark);
            if (!success) {
                result.error500("审核失败");
                return result;
            }
            
            // 获取当前审核记录，用于同步更新订单审核流程
            // 由于审核已经完成，需要从所有审核记录中查找当前角色的审核记录
            List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit> allHandoverAudits = 
                ghAccountingHandoverAuditService.getAuditsByHandoverId(handoverId);
            org.jeecg.modules.order.entity.GhAccountingHandoverAudit currentAudit = allHandoverAudits.stream()
                .filter(a -> roleId.equals(a.getRoleId()))
                .max((a1, a2) -> {
                    // 优先选择有审核时间的（刚审核完成的）
                    if (a2.getAuditTime() != null && a1.getAuditTime() == null) return 1;
                    if (a1.getAuditTime() != null && a2.getAuditTime() == null) return -1;
                    if (a2.getAuditTime() != null && a1.getAuditTime() != null) {
                        return a2.getAuditTime().compareTo(a1.getAuditTime());
                    }
                    return 0;
                })
                .orElse(null);
            
            // 同步更新订单审核流程
            String orderId = handover.getOrderId();
            if (oConvertUtils.isNotEmpty(orderId) && currentAudit != null && AuditStatusEnum.isApproved(auditStatus)) {
                try {
                    // 获取订单的所有审核记录
                    List<org.jeecg.modules.order.entity.GhOrderAudit> orderAudits = 
                        ghOrderAuditService.getAuditsByOrderId(orderId);
                    
                    if (orderAudits != null && !orderAudits.isEmpty()) {
                        // 根据代账交接审核记录的步骤顺序和角色名称，找到对应的订单审核记录
                        // 代账交接的步骤从2开始（因为步骤1已经在订单审核时完成）
                        // 需要根据角色名称匹配来找到对应的订单审核记录
                        Integer handoverStepOrder = currentAudit.getStepOrder();
                        String handoverRoleName = currentAudit.getRoleName();
                        
                        // 查找订单中对应步骤和角色的审核记录
                        org.jeecg.modules.order.entity.GhOrderAudit matchingOrderAudit = orderAudits.stream()
                            .filter(a -> {
                                // 代账交接的步骤2对应订单的步骤2，步骤3对应订单的步骤3，以此类推
                                // 或者根据角色名称匹配
                                boolean stepMatch = a.getStepOrder() != null && 
                                    a.getStepOrder().equals(handoverStepOrder);
                                boolean roleMatch = handoverRoleName != null && 
                                    a.getRoleName() != null && 
                                    a.getRoleName().equals(handoverRoleName);
                                return stepMatch && roleMatch && "pending".equals(a.getAuditStatus());
                            })
                            .findFirst()
                            .orElse(null);
                        
                        if (matchingOrderAudit != null) {
                            // 更新订单审核记录为已完成
                            Date now = new Date();
                            matchingOrderAudit.setAuditStatus("approved");
                            matchingOrderAudit.setAuditTime(now);
                            matchingOrderAudit.setAuditUserId(sysUser.getId());
                            matchingOrderAudit.setAuditUserName(sysUser.getRealname());
                            matchingOrderAudit.setAuditRemark("代账交接审核通过：" + (auditRemark != null ? auditRemark : ""));
                            matchingOrderAudit.setUpdateTime(now);
                            matchingOrderAudit.setUpdateBy(sysUser.getUsername());
                            ghOrderAuditService.updateById(matchingOrderAudit);
                            log.info("订单 {} 的审核步骤 {} (角色: {}) 已同步更新为完成", 
                                orderId, matchingOrderAudit.getStepOrder(), matchingOrderAudit.getRoleName());
                            
                            // 检查当前步骤的所有角色是否都已审核通过
                            List<org.jeecg.modules.order.entity.GhOrderAudit> currentStepOrderAudits = orderAudits.stream()
                                .filter(a -> a.getStepOrder() != null && 
                                    a.getStepOrder().equals(matchingOrderAudit.getStepOrder()))
                                .collect(java.util.stream.Collectors.toList());
                            
                            // 重新查询订单审核记录，确保获取最新状态
                            List<org.jeecg.modules.order.entity.GhOrderAudit> updatedOrderAudits = 
                                ghOrderAuditService.getAuditsByOrderId(orderId);
                            currentStepOrderAudits = updatedOrderAudits.stream()
                                .filter(a -> a.getStepOrder() != null && 
                                    a.getStepOrder().equals(matchingOrderAudit.getStepOrder()))
                                .collect(java.util.stream.Collectors.toList());
                            
                            boolean stepAllApproved = currentStepOrderAudits.stream()
                                .allMatch(a -> "approved".equals(a.getAuditStatus()));
                            
                            if (stepAllApproved) {
                                log.info("订单 {} 的步骤 {} 所有角色都已审核通过", orderId, matchingOrderAudit.getStepOrder());
                            }
                            
                            // 检查是否所有审核步骤都已完成
                            boolean allOrderAuditsApproved = updatedOrderAudits.stream()
                                .allMatch(a -> "approved".equals(a.getAuditStatus()));
                            
                            if (allOrderAuditsApproved) {
                                // 所有审核步骤都已完成，更新订单状态为已完成
                                org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(orderId);
                                if (order != null) {
                                    order.setOrderStatus("2"); // 2-已完成（所有审批流程走完就自动已完成）
                                    order.setAuditStatus("approved");
                                    order.setUpdateTime(now);
                                    order.setUpdateBy(sysUser.getUsername());
                                    ghOrderService.updateById(order);
                                    log.info("订单 {} 所有审核步骤已完成，订单状态已更新为已完成", orderId);
                                }
                            } else {
                                // 更新订单的当前审核步骤
                                org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(orderId);
                                if (order != null) {
                                    // 找到下一个待审核的步骤
                                    Integer nextPendingStep = updatedOrderAudits.stream()
                                        .filter(a -> "pending".equals(a.getAuditStatus()))
                                        .map(org.jeecg.modules.order.entity.GhOrderAudit::getStepOrder)
                                        .min(Integer::compareTo)
                                        .orElse(null);
                                    
                                    if (nextPendingStep != null) {
                                        order.setCurrentAuditStep(nextPendingStep);
                                    }
                                    order.setUpdateTime(now);
                                    order.setUpdateBy(sysUser.getUsername());
                                    ghOrderService.updateById(order);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("同步更新订单审核流程失败，订单ID: {}", orderId, e);
                    // 不抛出异常，避免影响代账交接审核操作
                }
            }
            
            // 获取审核记录，检查是否所有步骤都已完成
            List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit> audits = 
                ghAccountingHandoverAuditService.getAuditsByHandoverId(handoverId);
            
            // 按步骤顺序分组，检查每个步骤的所有角色是否都已审核通过
            Map<Integer, List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit>> stepAuditsMap = audits.stream()
                .collect(Collectors.groupingBy(org.jeecg.modules.order.entity.GhAccountingHandoverAudit::getStepOrder));
            
            boolean allApproved = true;
            boolean hasRejected = false;
            
            // 检查每个步骤的所有角色是否都已审核通过
            for (Map.Entry<Integer, List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit>> entry : stepAuditsMap.entrySet()) {
                List<org.jeecg.modules.order.entity.GhAccountingHandoverAudit> stepAudits = entry.getValue();
                
                // 检查当前步骤是否有驳回
                boolean stepHasRejected = stepAudits.stream()
                    .anyMatch(a -> AuditStatusEnum.isRejected(a.getAuditStatus()));
                if (stepHasRejected) {
                    hasRejected = true;
                    allApproved = false;
                    break;
                }
                // 检查当前步骤的所有角色是否都已审核通过
                boolean stepAllApproved = stepAudits.stream()
                    .allMatch(a -> AuditStatusEnum.isApproved(a.getAuditStatus()));
                if (!stepAllApproved) {
                    allApproved = false;
                }
            }
            
            // 更新代账交接状态
            if (hasRejected) {
                // 有驳回，代账交接审核失败
                handover.setHandoverStatus(HandoverStatusEnum.REJECTED.getCode());
            } else if (allApproved) {
                // 所有步骤都通过，代账交接审核通过
                handover.setHandoverStatus(HandoverStatusEnum.APPROVED.getCode());
            } else {
                // 部分步骤通过，继续审核流程
                handover.setHandoverStatus(HandoverStatusEnum.PENDING.getCode());
            }
            
            handover.setUpdateBy(sysUser.getUsername());
            handover.setUpdateTime(new Date());
            ghAccountingHandoverService.updateById(handover);
            
            result.success("审核成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 转为合同
     * @param jsonObject 包含代账交接ID
     * @return
     */
    @JRepeat(lockTime = 5, lockKey = "#jsonObject['handoverId']")
    @RequestMapping(value = "/convertToContract", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> convertToContract(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String handoverId = jsonObject.getString("handoverId");
            
            if (oConvertUtils.isEmpty(handoverId)) {
                result.error500("参数不完整");
                return result;
            }
            
            GhAccountingHandover handover = ghAccountingHandoverService.getById(handoverId);
            if (handover == null) {
                result.error500("代账交接记录不存在");
                return result;
            }
            
            // 检查是否已审核通过
            if (!HandoverStatusEnum.isApproved(handover.getHandoverStatus())) {
                result.error500("只有审核通过的代账交接才能转为合同");
                return result;
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser == null) {
                result.error500("请先登录");
                return result;
            }
            
            // 检查是否已经创建过合同
            org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(handover.getOrderId());
            if (order == null) {
                result.error500("订单不存在");
                return result;
            }
            
            // 检查是否已经存在合同
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhAccountingContract> contractWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            contractWrapper.eq("order_id", handover.getOrderId());
            contractWrapper.eq("del_flag", 0);
            org.jeecg.modules.order.entity.GhAccountingContract existingContract = 
                ghAccountingContractService.getOne(contractWrapper);
            
            if (existingContract != null) {
                result.error500("该订单已经存在合同，合同编号：" + existingContract.getContractNo());
                return result;
            }
            
            // 创建合同
            org.jeecg.modules.order.entity.GhAccountingContract contract = 
                new org.jeecg.modules.order.entity.GhAccountingContract();
            contract.setOrderId(handover.getOrderId());
            contract.setOrderNo(handover.getOrderNo());
            contract.setCompanyName(handover.getCompanyName());
            contract.setContractAmount(order.getContractAmount()); // 使用订单的合同金额
            contract.setSignDate(new Date()); // 签订日期为当前日期
            contract.setContractStatus("signed"); // 已签订
            contract.setRemarks("由代账交接转为合同");
            contract.setCreateBy(sysUser.getUsername());
            contract.setCreateTime(new Date());
            contract.setDelFlag(0);
            
            // 生成合同编号：HT + 日期 + 订单编号后6位
            String contractNo = "HT" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()) + 
                (handover.getOrderNo() != null && handover.getOrderNo().length() > 6 ? 
                    handover.getOrderNo().substring(handover.getOrderNo().length() - 6) : 
                    String.format("%06d", (int)(Math.random() * 1000000)));
            contract.setContractNo(contractNo);

            try {
                if (sysAuditTaskService != null && oConvertUtils.isNotEmpty(handover.getOrderId())) {
                    AccountingContractServiceStaffJsonUtil.applyDerivedToContract(contract,
                            sysAuditTaskService.getTasksByOrderId(handover.getOrderId()), true);
                }
            } catch (Exception ex) {
                log.warn("代账交接转合同时写入服务人员JSON失败, orderId={}: {}", handover.getOrderId(), ex.getMessage());
            }

            ghAccountingContractService.save(contract);
            
            // 更新代账交接状态为已完成
            handover.setHandoverStatus(HandoverStatusEnum.COMPLETED.getCode());
            handover.setUpdateBy(sysUser.getUsername());
            handover.setUpdateTime(new Date());
            ghAccountingHandoverService.updateById(handover);
            
            log.info("代账交接 {} 已转为合同，合同编号: {}，订单ID: {}", handoverId, contractNo, handover.getOrderId());
            
            result.success("转为合同成功，合同编号：" + contractNo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
}

