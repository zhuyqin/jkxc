package org.jeecg.modules.renew.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.jeecg.modules.renew.entity.GhAddressRenew;
import org.jeecg.modules.renew.service.IGhAddressRenewService;
import org.jeecg.modules.order.entity.GhAccountingContract;
import org.jeecg.modules.order.service.IGhAccountingContractService;
import org.jeecg.modules.order.service.IGhOrderOperationLogService;
import org.jeecg.modules.address.entity.GhAddressCenter;
import org.jeecg.modules.address.service.IGhAddressCenterService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 地址/代账续费信息（用于续费审核）
 * 仅包含续费列表与审核接口，供财务管理中的“续费审核”功能使用
 * @Author: jkxc
 * @Date: 2025-01-24
 */
@Api(tags = "续费信息（代账续费审核）")
@RestController
@RequestMapping("/renew/ghAddressRenew")
@Slf4j
public class GhAddressRenewController extends JeecgController<GhAddressRenew, IGhAddressRenewService> {

    @Autowired
    private IGhAddressRenewService ghAddressRenewService;
    @Autowired(required = false)
    private IGhBankManagementService ghBankManagementService;
    @Autowired(required = false)
    private IGhAccountingContractService ghAccountingContractService;
    @Autowired(required = false)
    private IGhOrderOperationLogService ghOrderOperationLogService;
    @Autowired(required = false)
    private IGhAddressCenterService ghAddressCenterService;
    @Autowired(required = false)
    private IGhBankDiaryService ghBankDiaryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysDictService sysDictService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysCategoryService sysCategoryService;

    /**
     * 分页列表查询（供续费审核列表使用）
     *
     * @param ghAddressRenew 查询条件
     * @param pageNo         页码
     * @param pageSize       每页条数
     * @param req            请求
     * @return 分页数据
     */
    @AutoLog(value = "续费信息-分页列表查询")
    @ApiOperation(value = "续费信息-分页列表查询", notes = "续费信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhAddressRenew ghAddressRenew,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<GhAddressRenew> queryWrapper = QueryGenerator.initQueryWrapper(ghAddressRenew, req.getParameterMap());

        // 如果前端传了审核状态（auditStatus），按照审核状态过滤
        if (ghAddressRenew.getAuditStatus() != null && !ghAddressRenew.getAuditStatus().trim().isEmpty()) {
            queryWrapper.eq("audit_status", ghAddressRenew.getAuditStatus().trim());
        }

        // 只按创建时间倒序排列，方便审核查看最新记录
        queryWrapper.orderByDesc("create_time");

        Page<GhAddressRenew> page = new Page<>(pageNo, pageSize);
        IPage<GhAddressRenew> pageList = ghAddressRenewService.page(page, queryWrapper);

        // 收款账户三联级+账号信息填充：收款方式 / 收款单位/人 / 网点名称 / 卡号
        if (pageList != null && pageList.getRecords() != null && ghBankManagementService != null) {
            for (GhAddressRenew renew : pageList.getRecords()) {
                try {
                    if (renew.getCollectionAccountNumber() != null && !renew.getCollectionAccountNumber().trim().isEmpty()) {
                        GhBankManagement bankAccount = ghBankManagementService.getById(renew.getCollectionAccountNumber());
                        if (bankAccount != null) {
                            // 收款方式
                            if (bankAccount.getPaymentMethod() != null) {
                                renew.setPaymentMethod(bankAccount.getPaymentMethod());
                            }
                            // 收款单位/人
                            if (bankAccount.getPayeePerson() != null) {
                                renew.setPayeePerson(bankAccount.getPayeePerson());
                            }
                            // 网点名称
                            if (bankAccount.getAccountNotes() != null) {
                                renew.setAccountNotes(bankAccount.getAccountNotes());
                            }
                            // 卡号
                            if (bankAccount.getCollectionAccount() != null) {
                                renew.setCollectionAccount(bankAccount.getCollectionAccount());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("加载续费收款账户信息失败, id={}", renew.getCollectionAccountNumber(), e);
                }
            }
        }

        return Result.OK(pageList);
    }

    /**
     * 续费审核
     *
     * @param id          续费记录ID
     * @param auditStatus 审核状态：0-待审核，1-已通过，2-已驳回
     * @param remark      审核备注
     * @return 结果
     */
    @AutoLog(value = "续费信息-续费审核")
    @ApiOperation(value = "续费信息-续费审核", notes = "续费信息-续费审核")
    @PostMapping(value = "/audit")
    public Result<?> auditRenewal(@RequestBody Map<String, Object> payload) {
        if (payload == null) {
            return Result.error("请求参数不能为空");
        }
        String id = (String) payload.get("id");
        String auditStatus = (String) payload.get("auditStatus");
        String remark = (String) payload.get("remark");
        if (id == null || id.trim().isEmpty()) {
            return Result.error("续费记录ID不能为空");
        }
        if (auditStatus == null || auditStatus.trim().isEmpty()) {
            return Result.error("审核状态不能为空");
        }

        GhAddressRenew renew = ghAddressRenewService.getById(id);
        if (renew == null) {
            return Result.error("续费记录不存在");
        }

        // 设置审核状态
        renew.setAuditStatus(auditStatus);
        // 同步财务审核字段（可选，根据业务需要）
        renew.setFinancialAudit(auditStatus);
        
        // 如果审核不通过（驳回），进行平账操作
        if ("2".equals(auditStatus)) {
            try {
                offsetBankDiaryForRejectedRenewal(renew, remark);
            } catch (Exception e) {
                log.error("续费审核驳回时平账失败, renewId={}", id, e);
                // 不抛出异常，避免影响审核流程
            }
        }

        // 追加审核备注
        if (remark != null && !remark.trim().isEmpty()) {
            String oldRemarks = renew.getRemarks();
            String newRemark = "【审核备注】" + remark.trim();
            if (oldRemarks != null && !oldRemarks.trim().isEmpty()) {
                renew.setRemarks(oldRemarks + "\n" + newRemark);
            } else {
                renew.setRemarks(newRemark);
            }
        }

        // 记录审核人（如果实体有相应字段可在此扩展，这里仅示例获取当前用户）
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                log.info("续费审核 - 审核人: {}, 续费ID: {}, 状态: {}", sysUser.getRealname(), id, auditStatus);
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户失败（续费审核时）", e);
        }

        // 如果审核通过，则更新对应合同或地址的到期日期为本次续费后的到期日
        String contractIdForLog = null;
        if ("1".equals(auditStatus) && renew.getPostExpirationDate() != null) {
            // 判断续费类型：地址续费（detailType='4'）还是代账续费
            String detailType = renew.getDetailType();
            if ("4".equals(detailType)) {
                // 地址续费：更新地址的到期日期
                try {
                    String addressId = renew.getAddressId();
                    if (addressId != null && !addressId.trim().isEmpty() && ghAddressCenterService != null) {
                        GhAddressCenter address = ghAddressCenterService.getById(addressId);
                        if (address != null) {
                            address.setTerminationDate(renew.getPostExpirationDate());
                            ghAddressCenterService.updateById(address);
                            log.info("地址续费审核通过，已更新地址到期日期，地址ID: {}, 新到期日: {}", addressId, renew.getPostExpirationDate());
                        }
                    }
                } catch (Exception e) {
                    log.error("地址续费审核通过后更新地址到期日期失败, renewId={}", id, e);
                }
            } else {
                // 代账续费：更新合同的到期日期
                try {
                    String contractId = renew.getHtId();
                    contractIdForLog = contractId;
                    if (contractId != null && !contractId.trim().isEmpty() && ghAccountingContractService != null) {
                        GhAccountingContract contract = ghAccountingContractService.getById(contractId);
                        if (contract != null) {
                            contract.setExpireDate(renew.getPostExpirationDate());
                            ghAccountingContractService.updateById(contract);
                            log.info("代账续费审核通过，已更新合同到期日期，合同ID: {}, 新到期日: {}", contractId, renew.getPostExpirationDate());
                        }
                    }
                } catch (Exception e) {
                    log.error("代账续费审核通过后更新合同到期日期失败, renewId={}", id, e);
                }
            }
        }

        // 在代账或地址的操作记录中增加一条“续费审核”记录
        if (ghOrderOperationLogService != null) {
            try {
                String orderId = null;
                String detailType = renew.getDetailType();
                
                if ("4".equals(detailType)) {
                    // 地址续费：从地址获取订单ID
                    String addressId = renew.getAddressId();
                    if (addressId != null && !addressId.trim().isEmpty() && ghAddressCenterService != null) {
                        GhAddressCenter address = ghAddressCenterService.getById(addressId);
                        if (address != null && address.getContractId() != null) {
                            orderId = address.getContractId();
                        }
                    }
                } else {
                    // 代账续费：优先从合同中拿订单ID
                    if (contractIdForLog != null && ghAccountingContractService != null) {
                        GhAccountingContract contract = ghAccountingContractService.getById(contractIdForLog);
                        if (contract != null && contract.getOrderId() != null) {
                            orderId = contract.getOrderId();
                        }
                    }
                }
                // 兼容：addRenewalRecord 中使用订单ID作为 companyId 传入
                if (orderId == null && renew.getCompanyId() != null && !renew.getCompanyId().trim().isEmpty()) {
                    orderId = renew.getCompanyId();
                }

                if (orderId != null && !orderId.trim().isEmpty()) {
                    String statusText;
                    if ("1".equals(auditStatus)) {
                        statusText = "通过";
                    } else if ("2".equals(auditStatus)) {
                        statusText = "驳回";
                    } else {
                        statusText = "待审核";
                    }

                    StringBuilder desc = new StringBuilder();
                    desc.append("续费审核").append(statusText);
                    if (renew.getContractNo() != null) {
                        desc.append("，合同编号：").append(renew.getContractNo());
                    }
                    if (renew.getCompanyName() != null) {
                        desc.append("，公司名称：").append(renew.getCompanyName());
                    }
                    if (renew.getPostExpirationDate() != null) {
                        desc.append("，到期月份：")
                            .append(new java.text.SimpleDateFormat("yyyy-MM").format(renew.getPostExpirationDate()));
                    }
                    if (remark != null && !remark.trim().isEmpty()) {
                        desc.append("，审核备注：").append(remark.trim());
                    }

                    String operatorName = null;
                    try {
                        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                        if (sysUser != null) {
                            operatorName = sysUser.getRealname();
                        }
                    } catch (Exception e) {
                        log.debug("获取当前登录用户失败（记录续费审核操作日志时）", e);
                    }

                    ghOrderOperationLogService.saveOperationLog(
                        orderId,
                        "续费审核",
                        "续费审核" + statusText,
                        operatorName,
                        null,
                        null,
                        null,
                        desc.toString()
                    );
                }
            } catch (Exception e) {
                log.error("记录续费审核操作日志失败, renewId={}", id, e);
            }
        }

        ghAddressRenewService.updateById(renew);
        return Result.OK("续费审核成功");
    }
    
    /**
     * 续费审核驳回时进行平账操作
     * @param renew 续费记录
     * @param remark 审核备注
     */
    private void offsetBankDiaryForRejectedRenewal(GhAddressRenew renew, String remark) {
        if (ghBankDiaryService == null) {
            return;
        }
        
        log.info("开始处理续费审核驳回平账，续费记录ID: {}", renew.getId());
        
        try {
            // 查找该续费记录对应的银行日记收入记录
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("business_id", renew.getId());
            queryWrapper.eq("business_type", "续费收入");
            queryWrapper.eq("del_flag", 0);
            List<GhBankDiary> bankDiaries = ghBankDiaryService.list(queryWrapper);
            
            if (bankDiaries != null && !bankDiaries.isEmpty()) {
                for (GhBankDiary bankDiary : bankDiaries) {
                    BigDecimal incomeAmount = bankDiary.getIncomeAmount();
                    String bankAccountId = bankDiary.getBankAccountId();
                    Date orderDate = bankDiary.getOrderDate();
                    
                    // 逻辑删除银行日记收入记录
                    bankDiary.setDelFlag(1);
                    bankDiary.setUpdateTime(new Date());
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        bankDiary.setUpdateBy(sysUser.getUsername());
                    }
                    ghBankDiaryService.updateById(bankDiary);
                    log.info("删除续费银行日记收入记录，续费记录ID: {}, 银行日记ID: {}, 收入金额: {}", 
                            renew.getId(), bankDiary.getId(), incomeAmount);
                    
                    // 如果已有收入，创建一条支出记录来平账
                    if (incomeAmount != null && incomeAmount.compareTo(BigDecimal.ZERO) > 0 && 
                        StringUtils.isNotBlank(bankAccountId)) {
                        try {
                            // 创建平账支出记录
                            GhBankDiary offsetDiary = new GhBankDiary();
                            offsetDiary.setOrderDate(new Date());
                            offsetDiary.setBusinessType("续费驳回平账");
                            offsetDiary.setBusinessId(renew.getId());
                            
                            // 费用详情
                            String feeDetail = "续费审核驳回平账";
                            if (StringUtils.isNotBlank(renew.getCompanyName())) {
                                feeDetail += " - 公司：" + renew.getCompanyName();
                            }
                            offsetDiary.setFeeDetail(feeDetail);
                            
                            offsetDiary.setIncomeAmount(BigDecimal.ZERO);
                            offsetDiary.setExpenseAmount(incomeAmount); // 支出金额等于原收入金额
                            offsetDiary.setBankAccountId(bankAccountId);
                            
                            // 公司信息
                            if (StringUtils.isNotBlank(renew.getCompanyId())) {
                                offsetDiary.setCompanyId(renew.getCompanyId());
                            }
                            if (StringUtils.isNotBlank(renew.getCompanyName())) {
                                offsetDiary.setCompanyName(renew.getCompanyName());
                            }
                            
                            // 操作人员
                            LoginUser sysUser2 = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                            if (sysUser2 != null) {
                                offsetDiary.setOperatorId(sysUser2.getId());
                                offsetDiary.setOperatorName(sysUser2.getRealname());
                                offsetDiary.setCreateBy(sysUser2.getUsername());
                            }
                            
                            // 备注
                            String offsetRemarks = "续费审核驳回平账，原收入金额: " + incomeAmount;
                            if (StringUtils.isNotBlank(remark)) {
                                offsetRemarks += "，驳回原因: " + remark;
                            }
                            offsetDiary.setRemarks(offsetRemarks);
                            
                            offsetDiary.setCreateTime(new Date());
                            offsetDiary.setDelFlag(0);
                            
                            // 计算结余金额
                            BigDecimal balanceAmount = calculateBalanceAmount(bankAccountId, offsetDiary.getOrderDate());
                            if (offsetDiary.getExpenseAmount() != null) {
                                balanceAmount = balanceAmount.subtract(offsetDiary.getExpenseAmount());
                            }
                            offsetDiary.setBalanceAmount(balanceAmount);
                            
                            ghBankDiaryService.save(offsetDiary);
                            log.info("创建续费驳回平账支出记录，续费记录ID: {}, 支出金额: {}", renew.getId(), incomeAmount);
                            
                            // 更新后续记录的结余金额
                            updateSubsequentBankDiaryBalances(bankAccountId, offsetDiary.getOrderDate());
                        } catch (Exception e) {
                            log.error("创建续费驳回平账支出记录失败，续费记录ID: {}", renew.getId(), e);
                        }
                    }
                }
            } else {
                log.info("未找到续费记录ID {} 对应的银行日记收入记录，无需平账", renew.getId());
            }
        } catch (Exception e) {
            log.error("处理续费审核驳回平账失败，续费记录ID: {}", renew.getId(), e);
        }
        
        log.info("续费审核驳回平账处理完成，续费记录ID: {}", renew.getId());
    }
    
    /**
     * 计算指定银行账户在指定日期之前的结余金额
     */
    private BigDecimal calculateBalanceAmount(String bankAccountId, Date orderDate) {
        if (ghBankDiaryService == null || StringUtils.isBlank(bankAccountId)) {
            return BigDecimal.ZERO;
        }
        
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bank_account_id", bankAccountId);
        queryWrapper.eq("del_flag", 0);
        if (orderDate != null) {
            queryWrapper.lt("order_date", orderDate);
        }
        queryWrapper.orderByDesc("order_date", "create_time");
        queryWrapper.last("LIMIT 1");
        
        GhBankDiary lastDiary = ghBankDiaryService.getOne(queryWrapper);
        if (lastDiary != null && lastDiary.getBalanceAmount() != null) {
            return lastDiary.getBalanceAmount();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 更新后续记录的结余金额
     */
    private void updateSubsequentBankDiaryBalances(String bankAccountId, Date orderDate) {
        if (ghBankDiaryService == null || StringUtils.isBlank(bankAccountId) || orderDate == null) {
            return;
        }
        
        try {
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("bank_account_id", bankAccountId);
            queryWrapper.eq("del_flag", 0);
            queryWrapper.ge("order_date", orderDate);
            queryWrapper.orderByAsc("order_date", "create_time");
            List<GhBankDiary> subsequentDiaries = ghBankDiaryService.list(queryWrapper);
            
            if (subsequentDiaries != null && !subsequentDiaries.isEmpty()) {
                BigDecimal previousBalance = calculateBalanceAmount(bankAccountId, orderDate);
                // 加上当前记录的金额
                QueryWrapper<GhBankDiary> currentQuery = new QueryWrapper<>();
                currentQuery.eq("bank_account_id", bankAccountId);
                currentQuery.eq("order_date", orderDate);
                currentQuery.eq("del_flag", 0);
                currentQuery.orderByDesc("create_time");
                currentQuery.last("LIMIT 1");
                GhBankDiary currentDiary = ghBankDiaryService.getOne(currentQuery);
                if (currentDiary != null) {
                    if (currentDiary.getIncomeAmount() != null) {
                        previousBalance = previousBalance.add(currentDiary.getIncomeAmount());
                    }
                    if (currentDiary.getExpenseAmount() != null) {
                        previousBalance = previousBalance.subtract(currentDiary.getExpenseAmount());
                    }
                }
                
                for (GhBankDiary diary : subsequentDiaries) {
                    if (diary.getIncomeAmount() != null) {
                        previousBalance = previousBalance.add(diary.getIncomeAmount());
                    }
                    if (diary.getExpenseAmount() != null) {
                        previousBalance = previousBalance.subtract(diary.getExpenseAmount());
                    }
                    diary.setBalanceAmount(previousBalance);
                    ghBankDiaryService.updateById(diary);
                }
                
                log.info("更新了 {} 条后续银行日记记录的结余金额", subsequentDiaries.size());
            }
        } catch (Exception e) {
            log.error("更新后续银行日记记录结余金额失败", e);
        }
    }
    
    /**
     * 续费订单统计分析
     * @param year 年份
     * @param month 月份（可选）
     * @param businessType 业务类型（可选）
     * @param renewalType 续费类型：accounting-代账续费，address-地址续费
     * @return 统计数据
     */
    @GetMapping(value = "/getRenewalStatistics")
    public Result<?> getRenewalStatistics(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "renewalType", required = true) String renewalType) {
        try {
            // 如果没有指定年份，使用当前年份
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 构建查询条件
            QueryWrapper<GhAddressRenew> queryWrapper = new QueryWrapper<>();
            
            // 根据续费类型过滤：代账续费或地址续费
            // 代账续费：通过 ht_id 关联到 gh_accounting_contract（有 ht_id 且不为空）
            // 地址续费：通过 address_id 关联（有 address_id 且 ht_id 为空）
            if ("accounting".equals(renewalType)) {
                // 代账续费：有 ht_id
                queryWrapper.isNotNull("ht_id");
                queryWrapper.ne("ht_id", "");
            } else if ("address".equals(renewalType)) {
                // 地址续费：有 address_id 且 ht_id 为空
                queryWrapper.isNotNull("address_id");
                queryWrapper.ne("address_id", "");
                queryWrapper.and(wrapper -> {
                    wrapper.isNull("ht_id").or().eq("ht_id", "");
                });
            }
            
            // 年份过滤（根据续费时间 renewal_time）
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            queryWrapper.ge("renewal_time", yearStart);
            queryWrapper.le("renewal_time", yearEnd);
            
            // 月份过滤（如果指定）
            if (month != null && month >= 1 && month <= 12) {
                calendar.set(year, month - 1, 1, 0, 0, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                Date monthStart = calendar.getTime();
                
                calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
                calendar.set(java.util.Calendar.MILLISECOND, 999);
                Date monthEnd = calendar.getTime();
                
                queryWrapper.ge("renewal_time", monthStart);
                queryWrapper.le("renewal_time", monthEnd);
            }
            
            // 业务类型过滤（如果指定）
            if (org.jeecg.common.util.oConvertUtils.isNotEmpty(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 查询所有符合条件的续费记录
            List<GhAddressRenew> renewals = ghAddressRenewService.list(queryWrapper);
            
            // 统计数据
            Map<String, Object> result = new java.util.HashMap<>();
            
            // 1. 总体统计
            Map<String, Object> statistics = new java.util.HashMap<>();
            int totalCount = renewals.size();
            BigDecimal totalAmount = BigDecimal.ZERO;
            int salesmanCount = 0;
            java.util.Set<String> salesmanSet = new java.util.HashSet<>();
            
            for (GhAddressRenew renewal : renewals) {
                try {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmounts())) {
                        amount = new BigDecimal(renewal.getAmounts());
                    } else if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmountReceived())) {
                        amount = new BigDecimal(renewal.getAmountReceived());
                    }
                    totalAmount = totalAmount.add(amount);
                    
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getSaleMan())) {
                        salesmanSet.add(renewal.getSaleMan());
                    }
                } catch (Exception e) {
                    log.warn("处理续费记录金额失败: " + renewal.getId(), e);
                }
            }
            
            salesmanCount = salesmanSet.size();
            BigDecimal avgAmount = totalCount > 0 ? 
                totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
            
            statistics.put("totalCount", totalCount);
            statistics.put("totalAmount", totalAmount);
            statistics.put("avgAmount", avgAmount);
            statistics.put("salesmanCount", salesmanCount);
            
            // 2. 月度趋势数据（全年12个月）
            List<Map<String, Object>> monthlyTrend = new java.util.ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                calendar.set(year, m - 1, 1, 0, 0, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                Date mStart = calendar.getTime();
                
                calendar.set(year, m - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
                calendar.set(java.util.Calendar.MILLISECOND, 999);
                Date mEnd = calendar.getTime();
                
                int count = 0;
                BigDecimal amount = BigDecimal.ZERO;
                
                for (GhAddressRenew renewal : renewals) {
                    if (renewal.getRenewalTime() != null && 
                        !renewal.getRenewalTime().before(mStart) && 
                        !renewal.getRenewalTime().after(mEnd)) {
                        count++;
                        try {
                            BigDecimal renewalAmount = BigDecimal.ZERO;
                            if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmounts())) {
                                renewalAmount = new BigDecimal(renewal.getAmounts());
                            } else if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmountReceived())) {
                                renewalAmount = new BigDecimal(renewal.getAmountReceived());
                            }
                            amount = amount.add(renewalAmount);
                        } catch (Exception e) {
                            log.warn("处理续费金额失败", e);
                        }
                    }
                }
                
                Map<String, Object> monthData = new java.util.HashMap<>();
                monthData.put("month", m + "月");
                monthData.put("count", count);
                monthData.put("amount", amount);
                monthlyTrend.add(monthData);
            }
            
            // 3. 业务类型分布
            Map<String, Map<String, Object>> businessTypeMap = new java.util.HashMap<>();
            for (GhAddressRenew renewal : renewals) {
                String bt = renewal.getBusinessType();
                if (org.jeecg.common.util.oConvertUtils.isEmpty(bt)) {
                    bt = "其他";
                } else {
                    // 获取业务类型名称
                    try {
                        if (sysCategoryService != null) {
                            java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
                            if (names != null && !names.isEmpty() && org.jeecg.common.util.oConvertUtils.isNotEmpty(names.get(0))) {
                                bt = names.get(0);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("获取业务类型名称失败，业务类型ID: " + bt, e);
                    }
                }
                
                Map<String, Object> btData = businessTypeMap.getOrDefault(bt, new java.util.HashMap<>());
                int count = (Integer) btData.getOrDefault("count", 0);
                BigDecimal amount = (BigDecimal) btData.getOrDefault("amount", BigDecimal.ZERO);
                
                try {
                    BigDecimal renewalAmount = BigDecimal.ZERO;
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmounts())) {
                        renewalAmount = new BigDecimal(renewal.getAmounts());
                    } else if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmountReceived())) {
                        renewalAmount = new BigDecimal(renewal.getAmountReceived());
                    }
                    btData.put("name", bt);
                    btData.put("count", count + 1);
                    btData.put("amount", amount.add(renewalAmount));
                    businessTypeMap.put(bt, btData);
                } catch (Exception e) {
                    log.warn("处理业务类型金额失败", e);
                }
            }
            
            List<Map<String, Object>> businessTypeList = new java.util.ArrayList<>(businessTypeMap.values());
            
            // 4. 业务员排名（按续费单数）
            Map<String, Map<String, Object>> salesmanMap = new java.util.HashMap<>();
            for (GhAddressRenew renewal : renewals) {
                String salesman = org.jeecg.common.util.oConvertUtils.isEmpty(renewal.getSaleMan()) ? "未分配" : renewal.getSaleMan();
                
                Map<String, Object> smData = salesmanMap.getOrDefault(salesman, new java.util.HashMap<>());
                int count = (Integer) smData.getOrDefault("count", 0);
                BigDecimal amount = (BigDecimal) smData.getOrDefault("amount", BigDecimal.ZERO);
                
                try {
                    BigDecimal renewalAmount = BigDecimal.ZERO;
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmounts())) {
                        renewalAmount = new BigDecimal(renewal.getAmounts());
                    } else if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmountReceived())) {
                        renewalAmount = new BigDecimal(renewal.getAmountReceived());
                    }
                    smData.put("salesman", salesman);
                    smData.put("count", count + 1);
                    smData.put("amount", amount.add(renewalAmount));
                    salesmanMap.put(salesman, smData);
                } catch (Exception e) {
                    log.warn("处理业务员金额失败", e);
                }
            }
            
            List<Map<String, Object>> salesmanRank = new java.util.ArrayList<>(salesmanMap.values());
            salesmanRank.sort((a, b) -> {
                int countA = (Integer) a.getOrDefault("count", 0);
                int countB = (Integer) b.getOrDefault("count", 0);
                return Integer.compare(countB, countA);
            });
            
            // 5. 每日趋势数据
            List<Map<String, Object>> dailyTrend = new java.util.ArrayList<>();
            if (month != null && month >= 1 && month <= 12) {
                // 显示指定月份的每日数据
                calendar.set(year, month - 1, 1, 0, 0, 0);
                int daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                
                for (int d = 1; d <= daysInMonth; d++) {
                    calendar.set(year, month - 1, d, 0, 0, 0);
                    calendar.set(java.util.Calendar.MILLISECOND, 0);
                    Date dayStart = calendar.getTime();
                    
                    calendar.set(year, month - 1, d, 23, 59, 59);
                    calendar.set(java.util.Calendar.MILLISECOND, 999);
                    Date dayEnd = calendar.getTime();
                    
                    int count = 0;
                    BigDecimal amount = BigDecimal.ZERO;
                    
                    for (GhAddressRenew renewal : renewals) {
                        if (renewal.getRenewalTime() != null && 
                            !renewal.getRenewalTime().before(dayStart) && 
                            !renewal.getRenewalTime().after(dayEnd)) {
                            count++;
                            try {
                                BigDecimal renewalAmount = BigDecimal.ZERO;
                                if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmounts())) {
                                    renewalAmount = new BigDecimal(renewal.getAmounts());
                                } else if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmountReceived())) {
                                    renewalAmount = new BigDecimal(renewal.getAmountReceived());
                                }
                                amount = amount.add(renewalAmount);
                            } catch (Exception e) {
                                log.warn("处理每日金额失败", e);
                            }
                        }
                    }
                    
                    Map<String, Object> dayData = new java.util.HashMap<>();
                    dayData.put("date", String.format("%d-%02d-%02d", year, month, d));
                    dayData.put("count", count);
                    dayData.put("amount", amount);
                    dailyTrend.add(dayData);
                }
            } else {
                // 显示全年每月数据（与月度趋势相同，但需要添加date字段）
                for (Map<String, Object> monthData : monthlyTrend) {
                    Map<String, Object> dayData = new java.util.HashMap<>();
                    dayData.put("date", monthData.get("month"));
                    dayData.put("count", monthData.get("count"));
                    dayData.put("amount", monthData.get("amount"));
                    dailyTrend.add(dayData);
                }
            }
            
            result.put("statistics", statistics);
            result.put("monthlyTrend", monthlyTrend);
            result.put("businessType", businessTypeList);
            result.put("salesmanRank", salesmanRank);
            result.put("dailyTrend", dailyTrend);
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取续费订单统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务人员月度明细数据
     * @param year 年份
     * @param month 月份（可选）
     * @param businessType 业务类型（可选）
     * @param renewalType 续费类型：accounting-代账续费，address-地址续费
     * @return 业务人员月度明细数据
     */
    @GetMapping(value = "/getSalesmanMonthlyDetail")
    public Result<?> getSalesmanMonthlyDetail(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "renewalType", required = true) String renewalType) {
        try {
            // 如果没有指定年份，使用当前年份
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 构建查询条件
            QueryWrapper<GhAddressRenew> queryWrapper = new QueryWrapper<>();
            
            // 根据续费类型过滤
            if ("accounting".equals(renewalType)) {
                queryWrapper.isNotNull("ht_id");
                queryWrapper.ne("ht_id", "");
            } else if ("address".equals(renewalType)) {
                queryWrapper.isNotNull("address_id");
                queryWrapper.ne("address_id", "");
                queryWrapper.and(wrapper -> {
                    wrapper.isNull("ht_id").or().eq("ht_id", "");
                });
            }
            
            // 年份过滤
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, 0, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date yearStart = calendar.getTime();
            
            calendar.set(year, 11, 31, 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date yearEnd = calendar.getTime();
            
            queryWrapper.ge("renewal_time", yearStart);
            queryWrapper.le("renewal_time", yearEnd);
            
            // 业务类型过滤（如果指定）
            if (org.jeecg.common.util.oConvertUtils.isNotEmpty(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 查询所有符合条件的续费记录
            List<GhAddressRenew> renewals = ghAddressRenewService.list(queryWrapper);
            
            // 按业务人员和月份分组统计
            Map<String, Map<Integer, Map<String, Object>>> salesmanMonthlyMap = new java.util.HashMap<>();
            
            int processedCount = 0;
            
            for (GhAddressRenew renewal : renewals) {
                if (renewal.getRenewalTime() == null) {
                    continue;
                }
                
                String salesman = org.jeecg.common.util.oConvertUtils.isEmpty(renewal.getSaleMan()) ? "未分配" : renewal.getSaleMan();
                
                // 获取续费时间的月份
                calendar.setTime(renewal.getRenewalTime());
                int renewalMonth = calendar.get(java.util.Calendar.MONTH) + 1;
                
                processedCount++;
                
                // 获取或创建业务人员的月份数据
                Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.getOrDefault(salesman, new java.util.HashMap<>());
                Map<String, Object> monthData = monthlyData.getOrDefault(renewalMonth, new java.util.HashMap<>());
                
                Object countObj = monthData.getOrDefault("count", 0);
                int count = 0;
                if (countObj instanceof Integer) {
                    count = (Integer) countObj;
                } else if (countObj instanceof Number) {
                    count = ((Number) countObj).intValue();
                }
                
                Object amountObj = monthData.getOrDefault("amount", BigDecimal.ZERO);
                BigDecimal amount = BigDecimal.ZERO;
                if (amountObj instanceof BigDecimal) {
                    amount = (BigDecimal) amountObj;
                } else if (amountObj instanceof Number) {
                    amount = new BigDecimal(amountObj.toString());
                }
                
                try {
                    BigDecimal renewalAmount = BigDecimal.ZERO;
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmounts())) {
                        renewalAmount = new BigDecimal(renewal.getAmounts());
                    } else if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAmountReceived())) {
                        renewalAmount = new BigDecimal(renewal.getAmountReceived());
                    }
                    
                    monthData.put("count", count + 1);
                    monthData.put("amount", amount.add(renewalAmount));
                    
                    monthlyData.put(renewalMonth, monthData);
                    salesmanMonthlyMap.put(salesman, monthlyData);
                } catch (Exception e) {
                    log.warn("处理续费金额失败", e);
                }
            }
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new java.util.ArrayList<>();
            
            // 获取所有业务人员
            List<String> salesmen = new java.util.ArrayList<>(salesmanMonthlyMap.keySet());
            java.util.Collections.sort(salesmen);
            
            for (String salesman : salesmen) {
                Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.get(salesman);
                
                Map<String, Object> row = new java.util.HashMap<>();
                row.put("salesman", salesman);
                
                // 初始化12个月的数据
                int totalCount = 0;
                BigDecimal totalAmount = BigDecimal.ZERO;
                
                for (int m = 1; m <= 12; m++) {
                    Map<String, Object> monthData = monthlyData.getOrDefault(m, new java.util.HashMap<>());
                    
                    Object countObj = monthData.getOrDefault("count", 0);
                    int count = 0;
                    if (countObj instanceof Integer) {
                        count = (Integer) countObj;
                    } else if (countObj instanceof Number) {
                        count = ((Number) countObj).intValue();
                    }
                    
                    Object amountObj = monthData.getOrDefault("amount", BigDecimal.ZERO);
                    BigDecimal amount = BigDecimal.ZERO;
                    if (amountObj instanceof BigDecimal) {
                        amount = (BigDecimal) amountObj;
                    } else if (amountObj instanceof Number) {
                        amount = new BigDecimal(amountObj.toString());
                    } else if (amountObj != null) {
                        try {
                            amount = new BigDecimal(amountObj.toString());
                        } catch (Exception e) {
                            amount = BigDecimal.ZERO;
                        }
                    }
                    
                    row.put("month" + m + "_count", count);
                    row.put("month" + m + "_amount", amount);
                    
                    totalCount += count;
                    totalAmount = totalAmount.add(amount);
                }
                
                row.put("totalCount", totalCount);
                row.put("totalAmount", totalAmount);
                
                resultList.add(row);
            }
            
            log.info("续费订单月度明细统计完成 - 处理记录数: {}, 业务人员数: {}", 
                processedCount, resultList.size());
            
            return Result.OK(resultList);
        } catch (Exception e) {
            log.error("获取业务人员月度明细数据失败", e);
            return Result.error("获取数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务人员月度续费记录列表
     * @param year 年份
     * @param month 月份
     * @param salesman 业务人员
     * @param businessType 业务类型（可选）
     * @param renewalType 续费类型：accounting-代账续费，address-地址续费
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 续费记录列表
     */
    @GetMapping(value = "/getSalesmanMonthlyRenewals")
    public Result<?> getSalesmanMonthlyRenewals(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = true) Integer month,
            @RequestParam(name = "salesman", required = true) String salesman,
            @RequestParam(name = "businessType", required = false) String businessType,
            @RequestParam(name = "renewalType", required = true) String renewalType,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            // 如果没有指定年份，使用当前年份
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            
            // 构建查询条件
            QueryWrapper<GhAddressRenew> queryWrapper = new QueryWrapper<>();
            
            // 根据续费类型过滤（与getSalesmanMonthlyDetail保持一致）
            if ("accounting".equals(renewalType)) {
                queryWrapper.isNotNull("ht_id");
                queryWrapper.ne("ht_id", "");
            } else if ("address".equals(renewalType)) {
                queryWrapper.isNotNull("address_id");
                queryWrapper.ne("address_id", "");
                queryWrapper.and(wrapper -> {
                    wrapper.isNull("ht_id").or().eq("ht_id", "");
                });
            }
            
            // 年份和月份过滤
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month - 1, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date monthStart = calendar.getTime();
            
            calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            Date monthEnd = calendar.getTime();
            
            queryWrapper.ge("renewal_time", monthStart);
            queryWrapper.le("renewal_time", monthEnd);
            
            // 业务人员过滤
            if (org.jeecg.common.util.oConvertUtils.isNotEmpty(salesman)) {
                queryWrapper.eq("sale_man", salesman);
            }
            
            // 业务类型过滤（如果指定）
            if (org.jeecg.common.util.oConvertUtils.isNotEmpty(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 排序
            queryWrapper.orderByDesc("renewal_time");
            
            // 分页查询
            Page<GhAddressRenew> page = new Page<>(pageNo, pageSize);
            IPage<GhAddressRenew> pageResult = ghAddressRenewService.page(page, queryWrapper);
            
            // 处理字典文本
            List<GhAddressRenew> records = pageResult.getRecords();
            if (records != null && !records.isEmpty()) {
                for (GhAddressRenew renewal : records) {
                    // 业务类型字典文本
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getBusinessType())) {
                        try {
                            if (sysCategoryService != null) {
                                java.util.List<String> names = sysCategoryService.loadDictItem(renewal.getBusinessType(), false);
                                if (names != null && !names.isEmpty() && org.jeecg.common.util.oConvertUtils.isNotEmpty(names.get(0))) {
                                    try {
                                        java.lang.reflect.Field field = renewal.getClass().getDeclaredField("businessType_dictText");
                                        field.setAccessible(true);
                                        field.set(renewal, names.get(0));
                                    } catch (Exception e) {
                                        log.warn("设置业务类型字典文本失败", e);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("获取业务类型字典文本失败，业务类型ID: " + renewal.getBusinessType(), e);
                        }
                    }
                    
                    // 审核状态字典文本
                    if (org.jeecg.common.util.oConvertUtils.isNotEmpty(renewal.getAuditStatus())) {
                        try {
                            if (sysDictService != null) {
                                String statusText = sysDictService.queryDictTextByKey("audit_status", renewal.getAuditStatus());
                                if (org.jeecg.common.util.oConvertUtils.isNotEmpty(statusText)) {
                                    try {
                                        java.lang.reflect.Field field = renewal.getClass().getDeclaredField("auditStatus_dictText");
                                        field.setAccessible(true);
                                        field.set(renewal, statusText);
                                    } catch (Exception e) {
                                        log.warn("设置审核状态字典文本失败", e);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("获取审核状态字典文本失败，审核状态: " + renewal.getAuditStatus(), e);
                        }
                    }
                }
            }
            
            // 转换为前端需要的格式
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("records", records);
            result.put("total", pageResult.getTotal());
            result.put("current", pageResult.getCurrent());
            result.put("size", pageResult.getSize());
            result.put("pages", pageResult.getPages());
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取业务人员月度续费记录列表失败", e);
            return Result.error("获取续费记录列表失败：" + e.getMessage());
        }
    }
}


