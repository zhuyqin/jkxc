package org.jeecg.modules.bankdiary.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Description: 银行日记
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Api(tags="银行日记")
@RestController
@RequestMapping("/bankDiary/ghBankDiary")
@Slf4j
public class GhBankDiaryController extends JeecgController<GhBankDiary, IGhBankDiaryService> {
    
    @Autowired
    private IGhBankDiaryService ghBankDiaryService;
    
    @Autowired(required = false)
    private IGhBankManagementService ghBankManagementService;
    
    @Autowired(required = false)
    private ISysUserService sysUserService;

    /**
     * 分页列表查询
     *
     * @param ghBankDiary
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "银行日记-分页列表查询")
    @ApiOperation(value="银行日记-分页列表查询", notes="银行日记-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhBankDiary ghBankDiary,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        long startTime = System.currentTimeMillis();
        try {
            // 优化：手动构建QueryWrapper，避免QueryGenerator重复添加条件
            long queryWrapperStart = System.currentTimeMillis();
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            
            // 优化查询顺序：先使用最有选择性的条件，减少查询范围
            // 1. 如果指定了bankAccountId，优先使用（索引：idx_bank_account_id）
            if (StringUtils.isNotBlank(ghBankDiary.getBankAccountId())) {
                queryWrapper.eq("bank_account_id", ghBankDiary.getBankAccountId());
            }
            
            // 2. 必须过滤已删除记录（索引：idx_del_flag）
            queryWrapper.eq("del_flag", 0);
            
            // 3. 处理其他查询条件（从请求参数中获取）
            // 创建日期筛选（前端传 createDate=YYYY-MM-DD）
            String createDate = req.getParameter("createDate");
            if (StringUtils.isNotBlank(createDate)) {
                // create_time 是 datetime，按当天范围过滤，避免直接 eq 导致匹配不上
                queryWrapper.ge("create_time", createDate.trim() + " 00:00:00");
                queryWrapper.le("create_time", createDate.trim() + " 23:59:59");
            }
            
            String businessType = req.getParameter("businessType");
            if (StringUtils.isNotBlank(businessType)) {
                queryWrapper.eq("business_type", businessType);
            }
            
            // 4. 按创建时间倒序排列（索引：idx_create_time）
            // 注意：如果bankAccountId存在，可以使用复合索引 idx_bank_account_del_flag_create_time
            queryWrapper.orderByDesc("create_time");
            
            long queryWrapperTime = System.currentTimeMillis() - queryWrapperStart;
            
            Page<GhBankDiary> page = new Page<>(pageNo, pageSize);
            long dbQueryStart = System.currentTimeMillis();
            IPage<GhBankDiary> pageList = ghBankDiaryService.page(page, queryWrapper);
            long dbQueryTime = System.currentTimeMillis() - dbQueryStart;
            
            // 批量填充银行账户名称和操作人员姓名，避免N+1查询问题
            long fillDataStart = System.currentTimeMillis();
            long bankQueryTime = 0;
            long userQueryTime = 0;
            if (pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
                // 收集需要查询的银行账户ID和操作人员ID
                Set<String> bankAccountIds = new HashSet<>();
                Set<String> operatorIds = new HashSet<>();
                
                for (GhBankDiary diary : pageList.getRecords()) {
                    if (StringUtils.isNotBlank(diary.getBankAccountId())) {
                        bankAccountIds.add(diary.getBankAccountId());
                    }
                    if (StringUtils.isNotBlank(diary.getOperatorId())) {
                        operatorIds.add(diary.getOperatorId());
                    }
                }
                
                // 批量查询银行账户信息
                long bankQueryStart = System.currentTimeMillis();
                Map<String, GhBankManagement> bankMap = new HashMap<>();
                if (!bankAccountIds.isEmpty() && ghBankManagementService != null) {
                    try {
                        List<GhBankManagement> bankList = ghBankManagementService.listByIds(bankAccountIds);
                        if (bankList != null) {
                            for (GhBankManagement bank : bankList) {
                                bankMap.put(bank.getId(), bank);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("批量查询银行账户信息失败", e);
                    }
                }
                bankQueryTime = System.currentTimeMillis() - bankQueryStart;
                
                // 批量查询用户信息
                long userQueryStart = System.currentTimeMillis();
                Map<String, String> userRealnameMap = new HashMap<>();
                if (!operatorIds.isEmpty() && sysUserService != null) {
                    try {
                        List<SysUser> userList = sysUserService.listByIds(operatorIds);
                        if (userList != null) {
                            for (SysUser user : userList) {
                                if (user != null && StringUtils.isNotBlank(user.getRealname())) {
                                    userRealnameMap.put(user.getId(), user.getRealname());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("批量查询操作人员信息失败", e);
                    }
                }
                userQueryTime = System.currentTimeMillis() - userQueryStart;
                
                // 填充银行账户名称和操作人员姓名
                for (GhBankDiary diary : pageList.getRecords()) {
                    // 填充银行账户名称
                    if (StringUtils.isNotBlank(diary.getBankAccountId())) {
                        GhBankManagement bank = bankMap.get(diary.getBankAccountId());
                        if (bank != null) {
                            // 构建账户显示名称：收款单位/人 - 收款账户（网点名称）
                            StringBuilder accountName = new StringBuilder();
                            if (StringUtils.isNotBlank(bank.getPayeePerson())) {
                                accountName.append(bank.getPayeePerson());
                            }
                            if (StringUtils.isNotBlank(bank.getCollectionAccount())) {
                                if (accountName.length() > 0) {
                                    accountName.append(" - ");
                                }
                                accountName.append(bank.getCollectionAccount());
                            }
                            if (StringUtils.isNotBlank(bank.getAccountNotes())) {
                                if (accountName.length() > 0) {
                                    accountName.append("（").append(bank.getAccountNotes()).append("）");
                                } else {
                                    accountName.append(bank.getAccountNotes());
                                }
                            }
                            diary.setBankAccountName(accountName.length() > 0 ? accountName.toString() : "");
                        }
                    }
                    
                    // 填充操作人员姓名
                    if (StringUtils.isNotBlank(diary.getOperatorId())) {
                        String realname = userRealnameMap.get(diary.getOperatorId());
                        if (StringUtils.isNotBlank(realname)) {
                            diary.setOperatorName(realname);
                        }
                    }
                }
            }
            long fillDataTime = System.currentTimeMillis() - fillDataStart;
            long totalTime = System.currentTimeMillis() - startTime;
            
            // 记录性能日志（使用INFO级别，确保总是输出）
            int recordCount = pageList.getRecords() != null ? pageList.getRecords().size() : 0;
            if (totalTime > 1000) {
                log.warn("【性能警告】银行日记查询耗时较长: 总耗时={}ms, 构建查询条件={}ms, 数据库查询={}ms, 批量查询银行账户={}ms, 批量查询用户={}ms, 填充数据={}ms, bankAccountId={}, 记录数={}, 页码={}, 页大小={}", 
                    totalTime, queryWrapperTime, dbQueryTime, bankQueryTime, userQueryTime, fillDataTime, 
                    ghBankDiary.getBankAccountId(), recordCount, pageNo, pageSize);
            } else {
                log.info("【性能日志】银行日记查询耗时: 总耗时={}ms, 构建查询条件={}ms, 数据库查询={}ms, 批量查询银行账户={}ms, 批量查询用户={}ms, 填充数据={}ms, bankAccountId={}, 记录数={}, 页码={}, 页大小={}", 
                    totalTime, queryWrapperTime, dbQueryTime, bankQueryTime, userQueryTime, fillDataTime,
                    ghBankDiary.getBankAccountId(), recordCount, pageNo, pageSize);
            }
            
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("查询银行日记列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param ghBankDiary
     * @return
     */
    @AutoLog(value = "银行日记-添加")
    @ApiOperation(value="银行日记-添加", notes="银行日记-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhBankDiary ghBankDiary, HttpServletRequest request) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghBankDiary.setCreateBy(sysUser.getUsername());
                ghBankDiary.setOperatorId(sysUser.getId());
                ghBankDiary.setOperatorName(sysUser.getRealname());
            }
            
            // 如果没有指定下单日期，使用当前日期
            if (ghBankDiary.getOrderDate() == null) {
                ghBankDiary.setOrderDate(new Date());
            }
            
            // 设置创建时间
            Date createTime = new Date();
            ghBankDiary.setCreateTime(createTime);
            
            // 按创建时间计算结余金额（查询该账户创建时间之前最近一条记录的结余金额）
            BigDecimal balanceAmount = calculateBalanceAmountByCreateTime(ghBankDiary.getBankAccountId(), createTime);
            if (ghBankDiary.getIncomeAmount() != null) {
                balanceAmount = balanceAmount.add(ghBankDiary.getIncomeAmount());
            }
            if (ghBankDiary.getExpenseAmount() != null) {
                balanceAmount = balanceAmount.subtract(ghBankDiary.getExpenseAmount());
            }
            ghBankDiary.setBalanceAmount(balanceAmount);
            ghBankDiary.setDelFlag(0);
            ghBankDiaryService.save(ghBankDiary);
            return Result.OK("添加成功！");
        } catch (Exception e) {
            log.error("添加银行日记失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 编辑
     *
     * @param ghBankDiary
     * @return
     */
    @AutoLog(value = "银行日记-编辑")
    @ApiOperation(value="银行日记-编辑", notes="银行日记-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody GhBankDiary ghBankDiary) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghBankDiary.setUpdateBy(sysUser.getUsername());
            }
            Date updateTime = new Date();
            ghBankDiary.setUpdateTime(updateTime);
            
            // 获取原记录信息（用于判断创建时间是否改变）
            GhBankDiary oldDiary = ghBankDiaryService.getById(ghBankDiary.getId());
            Date createTime = oldDiary != null && oldDiary.getCreateTime() != null ? oldDiary.getCreateTime() : new Date();
            
            // 按创建时间重新计算结余金额
            BigDecimal balanceAmount = calculateBalanceAmountByCreateTime(ghBankDiary.getBankAccountId(), createTime);
            if (ghBankDiary.getIncomeAmount() != null) {
                balanceAmount = balanceAmount.add(ghBankDiary.getIncomeAmount());
            }
            if (ghBankDiary.getExpenseAmount() != null) {
                balanceAmount = balanceAmount.subtract(ghBankDiary.getExpenseAmount());
            }
            ghBankDiary.setBalanceAmount(balanceAmount);
            
            ghBankDiaryService.updateById(ghBankDiary);
            
            // 更新后续记录的结余金额（按创建时间，传入记录ID以便排除当前记录）
            updateSubsequentBalancesByCreateTime(ghBankDiary.getBankAccountId(), createTime, ghBankDiary.getId());
            
            return Result.OK("编辑成功!");
        } catch (Exception e) {
            log.error("编辑银行日记失败", e);
            return Result.error("编辑失败!" + e.getMessage());
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "银行日记-通过id删除")
    @ApiOperation(value="银行日记-通过id删除", notes="银行日记-通过id删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        try {
            GhBankDiary ghBankDiary = ghBankDiaryService.getById(id);
            if (ghBankDiary != null) {
                String bankAccountId = ghBankDiary.getBankAccountId();
                Date orderDate = ghBankDiary.getOrderDate();
                
                ghBankDiary.setDelFlag(1);
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    ghBankDiary.setUpdateBy(sysUser.getUsername());
                }
                ghBankDiary.setUpdateTime(new Date());
                ghBankDiaryService.updateById(ghBankDiary);
                
                // 更新后续记录的结余金额
                if (bankAccountId != null && orderDate != null) {
                    updateSubsequentBalances(bankAccountId, orderDate);
                }
            }
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error("删除银行日记失败", e);
            return Result.error("删除失败!");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "银行日记-批量删除")
    @ApiOperation(value="银行日记-批量删除", notes="银行日记-批量删除")
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        try {
            String[] idArray = ids.split(",");
            // 收集需要更新结余金额的账户和日期
            java.util.Map<String, Date> accountDates = new java.util.HashMap<>();
            
            for (String id : idArray) {
                GhBankDiary ghBankDiary = ghBankDiaryService.getById(id);
                if (ghBankDiary != null) {
                    String bankAccountId = ghBankDiary.getBankAccountId();
                    Date orderDate = ghBankDiary.getOrderDate();
                    
                    ghBankDiary.setDelFlag(1);
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        ghBankDiary.setUpdateBy(sysUser.getUsername());
                    }
                    ghBankDiary.setUpdateTime(new Date());
                    ghBankDiaryService.updateById(ghBankDiary);
                    
                    // 记录需要更新的账户和日期（取最早的日期）
                    if (bankAccountId != null && orderDate != null) {
                        Date existingDate = accountDates.get(bankAccountId);
                        if (existingDate == null || orderDate.before(existingDate)) {
                            accountDates.put(bankAccountId, orderDate);
                        }
                    }
                }
            }
            
            // 更新所有受影响账户的后续记录结余金额
            for (java.util.Map.Entry<String, Date> entry : accountDates.entrySet()) {
                updateSubsequentBalances(entry.getKey(), entry.getValue());
            }
            
            return Result.OK("批量删除成功!");
        } catch (Exception e) {
            log.error("批量删除银行日记失败", e);
            return Result.error("批量删除失败!");
        }
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "银行日记-通过id查询")
    @ApiOperation(value="银行日记-通过id查询", notes="银行日记-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        try {
            GhBankDiary ghBankDiary = ghBankDiaryService.getById(id);
            if (ghBankDiary == null) {
                return Result.error("未找到对应数据");
            }
            
            // 填充银行账户名称
            if (StringUtils.isNotBlank(ghBankDiary.getBankAccountId()) && ghBankManagementService != null) {
                try {
                    GhBankManagement bank = ghBankManagementService.getById(ghBankDiary.getBankAccountId());
                    if (bank != null) {
                        // 构建账户显示名称：收款单位/人 - 收款账户（网点名称）
                        StringBuilder accountName = new StringBuilder();
                        if (StringUtils.isNotBlank(bank.getPayeePerson())) {
                            accountName.append(bank.getPayeePerson());
                        }
                        if (StringUtils.isNotBlank(bank.getCollectionAccount())) {
                            if (accountName.length() > 0) {
                                accountName.append(" - ");
                            }
                            accountName.append(bank.getCollectionAccount());
                        }
                        if (StringUtils.isNotBlank(bank.getAccountNotes())) {
                            if (accountName.length() > 0) {
                                accountName.append("（").append(bank.getAccountNotes()).append("）");
                            } else {
                                accountName.append(bank.getAccountNotes());
                            }
                        }
                        ghBankDiary.setBankAccountName(accountName.length() > 0 ? accountName.toString() : "");
                    }
                } catch (Exception e) {
                    log.debug("查询银行账户信息失败", e);
                }
            }
            
            // 填充操作人员姓名
            if (StringUtils.isNotBlank(ghBankDiary.getOperatorId()) && sysUserService != null) {
                try {
                    SysUser user = sysUserService.getById(ghBankDiary.getOperatorId());
                    if (user != null) {
                        ghBankDiary.setOperatorName(user.getRealname());
                    }
                } catch (Exception e) {
                    log.debug("查询操作人员信息失败", e);
                }
            }
            
            return Result.OK(ghBankDiary);
        } catch (Exception e) {
            log.error("查询银行日记详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ghBankDiary
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhBankDiary ghBankDiary) {
        return super.exportXls(request, ghBankDiary, GhBankDiary.class, "银行日记");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, GhBankDiary.class);
    }
    
    /**
     * 计算指定账户在指定日期之前的结余金额
     */
    private BigDecimal calculateBalanceAmount(String bankAccountId, Date orderDate) {
        if (StringUtils.isBlank(bankAccountId)) {
            return BigDecimal.ZERO;
        }
        
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bank_account_id", bankAccountId);
        queryWrapper.eq("del_flag", 0);
        if (orderDate != null) {
            queryWrapper.lt("order_date", orderDate);
        }
        // 先按order_date倒序，相同日期按create_time倒序
        queryWrapper.orderByDesc("order_date", "create_time");
        queryWrapper.last("LIMIT 1");
        
        GhBankDiary lastDiary = ghBankDiaryService.getOne(queryWrapper);
        if (lastDiary != null && lastDiary.getBalanceAmount() != null) {
            return lastDiary.getBalanceAmount();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算指定账户在指定记录（按创建时间）之前的结余金额
     * 注意：结余金额按每个银行账户（bankAccountId）分开计算，不同账户之间互不影响
     * 
     * @param bankAccountId 银行账户ID（确保按账户分开计算）
     * @param createTime 创建时间
     * @return 该账户在指定时间之前的结余金额
     */
    private BigDecimal calculateBalanceAmountByCreateTime(String bankAccountId, Date createTime) {
        if (StringUtils.isBlank(bankAccountId)) {
            return BigDecimal.ZERO;
        }
        
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        // 关键：按银行账户ID过滤，确保不同账户的结余金额分开计算
        queryWrapper.eq("bank_account_id", bankAccountId);
        queryWrapper.eq("del_flag", 0);
        if (createTime != null) {
            queryWrapper.lt("create_time", createTime);
        }
        // 按创建时间倒序，获取最近的记录
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        
        GhBankDiary lastDiary = ghBankDiaryService.getOne(queryWrapper);
        if (lastDiary != null && lastDiary.getBalanceAmount() != null) {
            return lastDiary.getBalanceAmount();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 更新后续记录的结余金额（按创建时间）
     * 注意：只更新同一银行账户的后续记录，确保不同账户的结余金额分开计算
     * 
     * @param bankAccountId 银行账户ID（确保按账户分开计算）
     * @param createTime 当前记录的创建时间
     * @param currentDiaryId 当前记录ID（可选，用于排除当前记录）
     */
    private void updateSubsequentBalancesByCreateTime(String bankAccountId, Date createTime, String currentDiaryId) {
        if (StringUtils.isBlank(bankAccountId) || createTime == null) {
            return;
        }
        
        QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
        // 关键：按银行账户ID过滤，确保只更新同一账户的后续记录
        queryWrapper.eq("bank_account_id", bankAccountId);
        queryWrapper.eq("del_flag", 0);
        queryWrapper.gt("create_time", createTime);
        // 排除当前记录
        if (StringUtils.isNotBlank(currentDiaryId)) {
            queryWrapper.ne("id", currentDiaryId);
        }
        // 按创建时间升序排列，确保按顺序计算
        queryWrapper.orderByAsc("create_time");
        
        List<GhBankDiary> subsequentDiaries = ghBankDiaryService.list(queryWrapper);
        if (subsequentDiaries == null || subsequentDiaries.isEmpty()) {
            return;
        }
        
        // 获取当前记录之前的结余金额
        BigDecimal currentBalance = calculateBalanceAmountByCreateTime(bankAccountId, createTime);
        
        // 加上当前记录的金额（如果提供了当前记录ID）
        if (StringUtils.isNotBlank(currentDiaryId)) {
            GhBankDiary currentDiary = ghBankDiaryService.getById(currentDiaryId);
            if (currentDiary != null) {
                if (currentDiary.getIncomeAmount() != null) {
                    currentBalance = currentBalance.add(currentDiary.getIncomeAmount());
                }
                if (currentDiary.getExpenseAmount() != null) {
                    currentBalance = currentBalance.subtract(currentDiary.getExpenseAmount());
                }
            }
        }
        
        // 重新计算后续所有记录的结余金额
        for (GhBankDiary diary : subsequentDiaries) {
            if (diary.getIncomeAmount() != null) {
                currentBalance = currentBalance.add(diary.getIncomeAmount());
            }
            if (diary.getExpenseAmount() != null) {
                currentBalance = currentBalance.subtract(diary.getExpenseAmount());
            }
            diary.setBalanceAmount(currentBalance);
            diary.setUpdateTime(new Date());
            ghBankDiaryService.updateById(diary);
        }
    }
    
    /**
     * 更新后续记录的结余金额（按订单日期，保留兼容性）
     */
    private void updateSubsequentBalances(String bankAccountId, Date orderDate) {
        // 这个方法保留用于兼容，但实际应该使用按创建时间的方法
        updateSubsequentBalancesByCreateTime(bankAccountId, orderDate, null);
    }
    
    /**
     * 资金转移：将一条收入记录的金额转移到另一个账户
     * 
     * @param requestBody 包含sourceId（源记录ID）和targetBankAccountId（目标账户ID）的请求体
     * @return
     */
    @AutoLog(value = "银行日记-资金转移")
    @ApiOperation(value = "银行日记-资金转移", notes = "将一条收入记录的金额转移到另一个账户")
    @PostMapping(value = "/transfer")
    public Result<?> transfer(@RequestBody Map<String, Object> requestBody) {
        try {
            String sourceId = (String) requestBody.get("sourceId");
            String targetBankAccountId = (String) requestBody.get("targetBankAccountId");
            String remarks = (String) requestBody.get("remarks");
            
            if (StringUtils.isBlank(sourceId)) {
                return Result.error("源记录ID不能为空！");
            }
            if (StringUtils.isBlank(targetBankAccountId)) {
                return Result.error("目标账户ID不能为空！");
            }
            
            // 查询源记录
            GhBankDiary sourceDiary = ghBankDiaryService.getById(sourceId);
            if (sourceDiary == null) {
                return Result.error("源记录不存在！");
            }
            
            // 检查源记录是否有收入金额
            if (sourceDiary.getIncomeAmount() == null || sourceDiary.getIncomeAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("源记录没有收入金额，无法转移！");
            }
            
            // 检查目标账户是否存在
            if (ghBankManagementService != null) {
                GhBankManagement targetAccount = ghBankManagementService.getById(targetBankAccountId);
                if (targetAccount == null) {
                    return Result.error("目标账户不存在！");
                }
            }
            
            // 检查源账户和目标账户是否相同
            if (sourceDiary.getBankAccountId().equals(targetBankAccountId)) {
                return Result.error("源账户和目标账户不能相同！");
            }
            
            BigDecimal transferAmount = sourceDiary.getIncomeAmount();
            Date currentDate = new Date();
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String operatorId = loginUser != null ? loginUser.getId() : null;
            
            // 1. 在源账户创建一条支出记录（扣除金额）
            GhBankDiary sourceExpense = new GhBankDiary();
            sourceExpense.setBankAccountId(sourceDiary.getBankAccountId());
            sourceExpense.setOrderDate(sourceDiary.getOrderDate() != null ? sourceDiary.getOrderDate() : currentDate);
            sourceExpense.setExpenseAmount(transferAmount);
            sourceExpense.setIncomeAmount(BigDecimal.ZERO);
            sourceExpense.setCategory(sourceDiary.getCategory());
            sourceExpense.setFeeDetail("资金转移至其他账户");
            sourceExpense.setRemarks(StringUtils.isNotBlank(remarks) ? remarks : ("资金转移至目标账户，原记录ID: " + sourceId));
            sourceExpense.setOperatorId(operatorId);
            sourceExpense.setDelFlag(0);
            sourceExpense.setCreateTime(currentDate);
            sourceExpense.setUpdateTime(currentDate);
            
            // 计算源账户的结余金额
            BigDecimal sourceBalance = calculateBalanceAmountByCreateTime(sourceDiary.getBankAccountId(), currentDate);
            sourceBalance = sourceBalance.subtract(transferAmount);
            sourceExpense.setBalanceAmount(sourceBalance);
            
            ghBankDiaryService.save(sourceExpense);
            
            // 更新源账户的后续记录结余金额
            updateSubsequentBalancesByCreateTime(sourceDiary.getBankAccountId(), currentDate, sourceExpense.getId());
            
            // 2. 在目标账户创建一条收入记录（增加金额）
            GhBankDiary targetIncome = new GhBankDiary();
            targetIncome.setBankAccountId(targetBankAccountId);
            targetIncome.setOrderDate(sourceDiary.getOrderDate() != null ? sourceDiary.getOrderDate() : currentDate);
            targetIncome.setIncomeAmount(transferAmount);
            targetIncome.setExpenseAmount(BigDecimal.ZERO);
            targetIncome.setCategory(sourceDiary.getCategory());
            targetIncome.setFeeDetail("资金转移自其他账户");
            targetIncome.setRemarks(StringUtils.isNotBlank(remarks) ? remarks : ("资金转移自源账户，原记录ID: " + sourceId));
            targetIncome.setOperatorId(operatorId);
            targetIncome.setDelFlag(0);
            targetIncome.setCreateTime(currentDate);
            targetIncome.setUpdateTime(currentDate);
            
            // 计算目标账户的结余金额
            BigDecimal targetBalance = calculateBalanceAmountByCreateTime(targetBankAccountId, currentDate);
            targetBalance = targetBalance.add(transferAmount);
            targetIncome.setBalanceAmount(targetBalance);
            
            ghBankDiaryService.save(targetIncome);
            
            // 更新目标账户的后续记录结余金额
            updateSubsequentBalancesByCreateTime(targetBankAccountId, currentDate, targetIncome.getId());
            
            log.info("资金转移成功：从账户 {} 转移 {} 元到账户 {}", sourceDiary.getBankAccountId(), transferAmount, targetBankAccountId);
            
            return Result.OK("资金转移成功！");
        } catch (Exception e) {
            log.error("资金转移失败", e);
            return Result.error("资金转移失败：" + e.getMessage());
        }
    }
}

