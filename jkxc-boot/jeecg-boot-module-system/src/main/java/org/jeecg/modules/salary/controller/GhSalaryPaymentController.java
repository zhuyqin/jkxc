package org.jeecg.modules.salary.controller;

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
import org.jeecg.modules.salary.entity.GhSalaryInfo;
import org.jeecg.modules.salary.entity.GhSalaryPayment;
import org.jeecg.modules.salary.service.IGhSalaryInfoService;
import org.jeecg.modules.salary.service.IGhSalaryPaymentService;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 工资发放记录
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Api(tags="工资发放记录")
@RestController
@RequestMapping("/salary/ghSalaryPayment")
@Slf4j
public class GhSalaryPaymentController extends JeecgController<GhSalaryPayment, IGhSalaryPaymentService> {
    
    @Autowired
    private IGhSalaryPaymentService ghSalaryPaymentService;
    
    @Autowired(required = false)
    private IGhSalaryInfoService ghSalaryInfoService;
    
    @Autowired(required = false)
    private ISysUserService sysUserService;
    
    @Autowired(required = false)
    private IGhBankManagementService ghBankManagementService;

    /**
     * 分页列表查询
     *
     * @param ghSalaryPayment
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "工资发放记录-分页列表查询")
    @ApiOperation(value="工资发放记录-分页列表查询", notes="工资发放记录-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhSalaryPayment ghSalaryPayment,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        try {
            QueryWrapper<GhSalaryPayment> queryWrapper = QueryGenerator.initQueryWrapper(ghSalaryPayment, req.getParameterMap());
            queryWrapper.eq("del_flag", 0);
            queryWrapper.orderByDesc("payment_month", "create_time");
            
            Page<GhSalaryPayment> page = new Page<>(pageNo, pageSize);
            IPage<GhSalaryPayment> pageList = ghSalaryPaymentService.page(page, queryWrapper);
            
            // 填充员工姓名、当前职位、当前星级和发放账号
            if (pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
                for (GhSalaryPayment payment : pageList.getRecords()) {
                    // 填充员工姓名
                    if (StringUtils.isNotBlank(payment.getUserId()) && sysUserService != null) {
                        try {
                            SysUser user = sysUserService.getById(payment.getUserId());
                            if (user != null) {
                                payment.setUserName(user.getRealname());
                            }
                        } catch (Exception e) {
                            log.debug("查询员工信息失败，userId: {}", payment.getUserId(), e);
                        }
                    }
                    
                    // 填充当前职位和当前星级（从工资信息表关联获取）
                    if (StringUtils.isNotBlank(payment.getUserId()) && ghSalaryInfoService != null) {
                        try {
                            QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
                            salaryWrapper.eq("user_id", payment.getUserId());
                            salaryWrapper.eq("del_flag", 0);
                            salaryWrapper.last("LIMIT 1");
                            GhSalaryInfo salaryInfo = ghSalaryInfoService.getOne(salaryWrapper);
                            if (salaryInfo != null) {
                                payment.setCurrentPosition(salaryInfo.getCurrentPosition());
                                payment.setCurrentStarLevel(salaryInfo.getCurrentStarLevel());
                            }
                        } catch (Exception e) {
                            log.debug("查询工资信息失败，userId: {}", payment.getUserId(), e);
                        }
                    }
                    
                    // 填充发放账号
                    if (StringUtils.isNotBlank(payment.getPaymentAccountId()) && ghBankManagementService != null) {
                        try {
                            GhBankManagement bank = ghBankManagementService.getById(payment.getPaymentAccountId());
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
                                payment.setPaymentAccount(accountName.length() > 0 ? accountName.toString() : "");
                            }
                        } catch (Exception e) {
                            log.debug("查询银行账户信息失败", e);
                        }
                    }
                }
            }
            
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("查询工资发放记录列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param ghSalaryPayment
     * @return
     */
    @AutoLog(value = "工资发放记录-添加")
    @ApiOperation(value="工资发放记录-添加", notes="工资发放记录-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhSalaryPayment ghSalaryPayment) {
        try {
            // 检查该员工该月份是否已有发放记录
            QueryWrapper<GhSalaryPayment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", ghSalaryPayment.getUserId());
            queryWrapper.eq("payment_month", ghSalaryPayment.getPaymentMonth());
            queryWrapper.eq("del_flag", 0);
            GhSalaryPayment existing = ghSalaryPaymentService.getOne(queryWrapper);
            if (existing != null) {
                return Result.error("该员工该月份已存在发放记录，请使用编辑功能！");
            }
            
            // 从工资信息中同步数据（如果字段为空）
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && ghSalaryInfoService != null) {
                try {
                    QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
                    salaryWrapper.eq("user_id", ghSalaryPayment.getUserId());
                    salaryWrapper.eq("del_flag", 0);
                    GhSalaryInfo salaryInfo = ghSalaryInfoService.getOne(salaryWrapper);
                    if (salaryInfo != null) {
                        // 如果字段为空，则从工资信息中同步
                        if (ghSalaryPayment.getBaseSalary() == null) {
                            ghSalaryPayment.setBaseSalary(salaryInfo.getBaseSalary());
                        }
                        if (ghSalaryPayment.getPerformanceSalary() == null) {
                            ghSalaryPayment.setPerformanceSalary(salaryInfo.getPerformanceSalary());
                        }
                        if (ghSalaryPayment.getHousingFundSubsidy() == null) {
                            ghSalaryPayment.setHousingFundSubsidy(salaryInfo.getHousingFundSubsidy());
                        }
                        if (ghSalaryPayment.getHighTemperatureSubsidy() == null) {
                            ghSalaryPayment.setHighTemperatureSubsidy(salaryInfo.getHighTemperatureSubsidy());
                        }
                        if (ghSalaryPayment.getOtherSubsidy() == null) {
                            ghSalaryPayment.setOtherSubsidy(salaryInfo.getOtherSubsidy());
                        }
                        if (ghSalaryPayment.getFullAttendanceBonus() == null) {
                            ghSalaryPayment.setFullAttendanceBonus(salaryInfo.getFullAttendanceBonus());
                        }
                        if (ghSalaryPayment.getCompanySocialSecurity() == null) {
                            ghSalaryPayment.setCompanySocialSecurity(salaryInfo.getCompanySocialSecurity());
                        }
                        if (ghSalaryPayment.getPersonalSocialSecurity() == null) {
                            ghSalaryPayment.setPersonalSocialSecurity(salaryInfo.getPersonalSocialSecurity());
                        }
                    }
                } catch (Exception e) {
                    log.debug("同步工资信息失败", e);
                }
            }
            
            // 计算金额
            calculateAmounts(ghSalaryPayment);
            
            // 填充员工姓名
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && sysUserService != null) {
                try {
                    SysUser user = sysUserService.getById(ghSalaryPayment.getUserId());
                    if (user != null) {
                        ghSalaryPayment.setUserName(user.getRealname());
                    }
                } catch (Exception e) {
                    log.debug("查询员工信息失败", e);
                }
            }
            
            // 填充当前职位和当前星级（从工资信息表关联获取）
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && ghSalaryInfoService != null) {
                try {
                    QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
                    salaryWrapper.eq("user_id", ghSalaryPayment.getUserId());
                    salaryWrapper.eq("del_flag", 0);
                    salaryWrapper.last("LIMIT 1");
                    GhSalaryInfo salaryInfo = ghSalaryInfoService.getOne(salaryWrapper);
                    if (salaryInfo != null) {
                        ghSalaryPayment.setCurrentPosition(salaryInfo.getCurrentPosition());
                        ghSalaryPayment.setCurrentStarLevel(salaryInfo.getCurrentStarLevel());
                    }
                } catch (Exception e) {
                    log.debug("查询工资信息失败，userId: {}", ghSalaryPayment.getUserId(), e);
                }
            }
            
            // 默认状态为待发放
            if (StringUtils.isBlank(ghSalaryPayment.getPaymentStatus())) {
                ghSalaryPayment.setPaymentStatus("pending");
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghSalaryPayment.setCreateBy(sysUser.getUsername());
            }
            ghSalaryPayment.setCreateTime(new Date());
            ghSalaryPayment.setDelFlag(0);
            ghSalaryPaymentService.save(ghSalaryPayment);
            return Result.OK("添加成功！");
        } catch (Exception e) {
            log.error("添加工资发放记录失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 编辑
     *
     * @param ghSalaryPayment
     * @return
     */
    @AutoLog(value = "工资发放记录-编辑")
    @ApiOperation(value="工资发放记录-编辑", notes="工资发放记录-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody GhSalaryPayment ghSalaryPayment) {
        try {
            // 重新计算金额
            calculateAmounts(ghSalaryPayment);
            
            // 填充员工姓名
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && sysUserService != null) {
                try {
                    SysUser user = sysUserService.getById(ghSalaryPayment.getUserId());
                    if (user != null) {
                        ghSalaryPayment.setUserName(user.getRealname());
                    }
                } catch (Exception e) {
                    log.debug("查询员工信息失败", e);
                }
            }
            
            // 填充当前职位和当前星级（从工资信息表关联获取）
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && ghSalaryInfoService != null) {
                try {
                    QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
                    salaryWrapper.eq("user_id", ghSalaryPayment.getUserId());
                    salaryWrapper.eq("del_flag", 0);
                    salaryWrapper.last("LIMIT 1");
                    GhSalaryInfo salaryInfo = ghSalaryInfoService.getOne(salaryWrapper);
                    if (salaryInfo != null) {
                        ghSalaryPayment.setCurrentPosition(salaryInfo.getCurrentPosition());
                        ghSalaryPayment.setCurrentStarLevel(salaryInfo.getCurrentStarLevel());
                    }
                } catch (Exception e) {
                    log.debug("查询工资信息失败，userId: {}", ghSalaryPayment.getUserId(), e);
                }
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghSalaryPayment.setUpdateBy(sysUser.getUsername());
            }
            ghSalaryPayment.setUpdateTime(new Date());
            ghSalaryPaymentService.updateById(ghSalaryPayment);
            return Result.OK("编辑成功!");
        } catch (Exception e) {
            log.error("编辑工资发放记录失败", e);
            return Result.error("编辑失败!" + e.getMessage());
        }
    }

    /**
     * 批量发放（根据工资信息自动生成发放记录）
     *
     * @param jsonObject 包含paymentMonth（发放月份）和userIds（员工ID列表，可选）
     * @return
     */
    @AutoLog(value = "工资发放记录-批量发放")
    @ApiOperation(value="工资发放记录-批量发放", notes="工资发放记录-批量发放")
    @PostMapping(value = "/batchCreate")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> batchCreate(@RequestBody com.alibaba.fastjson.JSONObject jsonObject) {
        try {
            String paymentMonth = jsonObject.getString("paymentMonth");
            if (StringUtils.isBlank(paymentMonth)) {
                return Result.error("发放月份不能为空！");
            }
            
            // 验证月份格式
            if (!paymentMonth.matches("\\d{4}-\\d{2}")) {
                return Result.error("发放月份格式不正确，应为YYYY-MM格式！");
            }
            
            List<String> userIds = null;
            if (jsonObject.containsKey("userIds") && jsonObject.get("userIds") != null) {
                if (jsonObject.get("userIds") instanceof List) {
                    userIds = (List<String>) jsonObject.get("userIds");
                } else if (jsonObject.get("userIds") instanceof String) {
                    userIds = Arrays.asList(jsonObject.getString("userIds").split(","));
                }
            }
            
            // 获取在职员工列表
            List<SysUser> activeUsers = new ArrayList<>();
            if (sysUserService != null) {
                QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
                userWrapper.eq("del_flag", 0);
                userWrapper.eq("status", 1);
                if (userIds != null && !userIds.isEmpty()) {
                    userWrapper.in("id", userIds);
                }
                activeUsers = sysUserService.list(userWrapper);
            }
            
            if (activeUsers == null || activeUsers.isEmpty()) {
                return Result.error("没有找到在职员工！");
            }
            
            int successCount = 0;
            int skipCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String createBy = sysUser != null ? sysUser.getUsername() : "system";
            
            for (SysUser user : activeUsers) {
                try {
                    // 检查该员工该月份是否已有发放记录
                    QueryWrapper<GhSalaryPayment> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", user.getId());
                    queryWrapper.eq("payment_month", paymentMonth);
                    queryWrapper.eq("del_flag", 0);
                    GhSalaryPayment existing = ghSalaryPaymentService.getOne(queryWrapper);
                    if (existing != null) {
                        skipCount++;
                        continue;
                    }
                    
                    // 获取该员工的工资信息
                    GhSalaryInfo salaryInfo = null;
                    if (ghSalaryInfoService != null) {
                        QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
                        salaryWrapper.eq("user_id", user.getId());
                        salaryWrapper.eq("del_flag", 0);
                        salaryInfo = ghSalaryInfoService.getOne(salaryWrapper);
                    }
                    
                    if (salaryInfo == null) {
                        errorMessages.add("员工 " + user.getRealname() + " 没有工资信息，跳过");
                        skipCount++;
                        continue;
                    }
                    
                    // 创建发放记录
                    GhSalaryPayment payment = new GhSalaryPayment();
                    payment.setUserId(user.getId());
                    payment.setUserName(user.getRealname());
                    payment.setPaymentMonth(paymentMonth);
                    
                    // 从工资信息复制数据
                    payment.setBaseSalary(salaryInfo.getBaseSalary());
                    payment.setPerformanceSalary(salaryInfo.getPerformanceSalary());
                    payment.setHousingFundSubsidy(salaryInfo.getHousingFundSubsidy());
                    payment.setHighTemperatureSubsidy(salaryInfo.getHighTemperatureSubsidy());
                    payment.setOtherSubsidy(salaryInfo.getOtherSubsidy());
                    payment.setFullAttendanceBonus(salaryInfo.getFullAttendanceBonus());
                    payment.setCompanySocialSecurity(salaryInfo.getCompanySocialSecurity());
                    payment.setPersonalSocialSecurity(salaryInfo.getPersonalSocialSecurity());
                    
                    // 计算金额
                    calculateAmounts(payment);
                    
                    payment.setPaymentStatus("pending");
                    payment.setCreateBy(createBy);
                    payment.setCreateTime(new Date());
                    payment.setDelFlag(0);
                    
                    ghSalaryPaymentService.save(payment);
                    successCount++;
                } catch (Exception e) {
                    log.error("为员工 {} 创建发放记录失败", user.getRealname(), e);
                    errorMessages.add("员工 " + user.getRealname() + " 创建失败：" + e.getMessage());
                }
            }
            
            String message = String.format("批量创建完成：成功 %d 条，跳过 %d 条", successCount, skipCount);
            if (!errorMessages.isEmpty()) {
                message += "；错误：" + String.join("；", errorMessages);
            }
            
            return Result.OK(message);
        } catch (Exception e) {
            log.error("批量创建工资发放记录失败", e);
            return Result.error("批量创建失败：" + e.getMessage());
        }
    }

    /**
     * 发放操作（将状态改为已发放）
     *
     * @param jsonObject 包含id（发放记录ID）和paymentTime（发放时间，可选）
     * @return
     */
    @AutoLog(value = "工资发放记录-发放")
    @ApiOperation(value="工资发放记录-发放", notes="工资发放记录-发放")
    @PostMapping(value = "/pay")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> pay(@RequestBody com.alibaba.fastjson.JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("id");
            if (StringUtils.isBlank(id)) {
                return Result.error("发放记录ID不能为空！");
            }
            
            GhSalaryPayment payment = ghSalaryPaymentService.getById(id);
            if (payment == null) {
                return Result.error("发放记录不存在！");
            }
            
            if ("paid".equals(payment.getPaymentStatus())) {
                return Result.error("该记录已发放，不能重复发放！");
            }
            
            // 更新状态为已发放
            payment.setPaymentStatus("paid");
            if (jsonObject.containsKey("paymentTime") && jsonObject.get("paymentTime") != null) {
                try {
                    String paymentTimeStr = jsonObject.getString("paymentTime");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    payment.setPaymentTime(sdf.parse(paymentTimeStr));
                } catch (Exception e) {
                    payment.setPaymentTime(new Date());
                }
            } else {
                payment.setPaymentTime(new Date());
            }
            
            // 如果提供了发放方式和账号，更新
            if (jsonObject.containsKey("paymentMethod")) {
                payment.setPaymentMethod(jsonObject.getString("paymentMethod"));
            }
            if (jsonObject.containsKey("paymentAccountId")) {
                payment.setPaymentAccountId(jsonObject.getString("paymentAccountId"));
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                payment.setUpdateBy(sysUser.getUsername());
            }
            payment.setUpdateTime(new Date());
            
            ghSalaryPaymentService.updateById(payment);
            
            // 创建银行日记记录（工资发放是支出）
            try {
                createBankDiaryFromSalaryPayment(payment);
            } catch (Exception e) {
                log.error("创建银行日记记录失败", e);
                // 不抛出异常，避免影响发放操作
            }
            
            return Result.OK("发放成功！");
        } catch (Exception e) {
            log.error("发放工资失败", e);
            return Result.error("发放失败：" + e.getMessage());
        }
    }

    /**
     * 批量发放
     *
     * @param jsonObject 包含ids（发放记录ID列表）
     * @return
     */
    @AutoLog(value = "工资发放记录-批量发放")
    @ApiOperation(value="工资发放记录-批量发放", notes="工资发放记录-批量发放")
    @PostMapping(value = "/batchPay")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> batchPay(@RequestBody com.alibaba.fastjson.JSONObject jsonObject) {
        try {
            List<String> ids = null;
            if (jsonObject.containsKey("ids") && jsonObject.get("ids") != null) {
                if (jsonObject.get("ids") instanceof List) {
                    ids = (List<String>) jsonObject.get("ids");
                } else if (jsonObject.get("ids") instanceof String) {
                    ids = Arrays.asList(jsonObject.getString("ids").split(","));
                }
            }
            
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要发放的记录！");
            }
            
            String paymentMethod = jsonObject.getString("paymentMethod");
            String paymentAccountId = jsonObject.getString("paymentAccountId");
            Date paymentTime = new Date();
            if (jsonObject.containsKey("paymentTime") && jsonObject.get("paymentTime") != null) {
                try {
                    String paymentTimeStr = jsonObject.getString("paymentTime");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    paymentTime = sdf.parse(paymentTimeStr);
                } catch (Exception e) {
                    // 使用当前时间
                }
            }
            
            int successCount = 0;
            int failCount = 0;
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String updateBy = sysUser != null ? sysUser.getUsername() : "system";
            
            for (String id : ids) {
                try {
                    GhSalaryPayment payment = ghSalaryPaymentService.getById(id);
                    if (payment == null || "paid".equals(payment.getPaymentStatus())) {
                        failCount++;
                        continue;
                    }
                    
                    payment.setPaymentStatus("paid");
                    payment.setPaymentTime(paymentTime);
                    if (StringUtils.isNotBlank(paymentMethod)) {
                        payment.setPaymentMethod(paymentMethod);
                    }
                    if (StringUtils.isNotBlank(paymentAccountId)) {
                        payment.setPaymentAccountId(paymentAccountId);
                    }
                    payment.setUpdateBy(updateBy);
                    payment.setUpdateTime(new Date());
                    
                    ghSalaryPaymentService.updateById(payment);
                    
                    // 创建银行日记记录
                    try {
                        createBankDiaryFromSalaryPayment(payment);
                    } catch (Exception e) {
                        log.error("创建银行日记记录失败，发放记录ID: {}", id, e);
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("发放工资失败，发放记录ID: {}", id, e);
                    failCount++;
                }
            }
            
            return Result.OK(String.format("批量发放完成：成功 %d 条，失败 %d 条", successCount, failCount));
        } catch (Exception e) {
            log.error("批量发放工资失败", e);
            return Result.error("批量发放失败：" + e.getMessage());
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "工资发放记录-通过id删除")
    @ApiOperation(value="工资发放记录-通过id删除", notes="工资发放记录-通过id删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        try {
            GhSalaryPayment ghSalaryPayment = ghSalaryPaymentService.getById(id);
            if (ghSalaryPayment != null) {
                if ("paid".equals(ghSalaryPayment.getPaymentStatus())) {
                    return Result.error("已发放的记录不能删除！");
                }
                ghSalaryPayment.setDelFlag(1);
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    ghSalaryPayment.setUpdateBy(sysUser.getUsername());
                }
                ghSalaryPayment.setUpdateTime(new Date());
                ghSalaryPaymentService.updateById(ghSalaryPayment);
            }
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error("删除工资发放记录失败", e);
            return Result.error("删除失败!");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "工资发放记录-批量删除")
    @ApiOperation(value="工资发放记录-批量删除", notes="工资发放记录-批量删除")
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            int failCount = 0;
            for (String id : idArray) {
                try {
                    GhSalaryPayment ghSalaryPayment = ghSalaryPaymentService.getById(id);
                    if (ghSalaryPayment != null) {
                        if ("paid".equals(ghSalaryPayment.getPaymentStatus())) {
                            failCount++;
                            continue;
                        }
                        ghSalaryPayment.setDelFlag(1);
                        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                        if (sysUser != null) {
                            ghSalaryPayment.setUpdateBy(sysUser.getUsername());
                        }
                        ghSalaryPayment.setUpdateTime(new Date());
                        ghSalaryPaymentService.updateById(ghSalaryPayment);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("删除工资发放记录失败，ID: {}", id, e);
                    failCount++;
                }
            }
            return Result.OK(String.format("批量删除完成：成功 %d 条，失败 %d 条", successCount, failCount));
        } catch (Exception e) {
            log.error("批量删除工资发放记录失败", e);
            return Result.error("批量删除失败!");
        }
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "工资发放记录-通过id查询")
    @ApiOperation(value="工资发放记录-通过id查询", notes="工资发放记录-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        try {
            GhSalaryPayment ghSalaryPayment = ghSalaryPaymentService.getById(id);
            if (ghSalaryPayment == null) {
                return Result.error("未找到对应数据");
            }
            
            // 填充员工姓名
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && sysUserService != null) {
                try {
                    SysUser user = sysUserService.getById(ghSalaryPayment.getUserId());
                    if (user != null) {
                        ghSalaryPayment.setUserName(user.getRealname());
                    }
                } catch (Exception e) {
                    log.debug("查询员工信息失败", e);
                }
            }
            
            // 填充当前职位和当前星级（从工资信息表关联获取）
            if (StringUtils.isNotBlank(ghSalaryPayment.getUserId()) && ghSalaryInfoService != null) {
                try {
                    QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
                    salaryWrapper.eq("user_id", ghSalaryPayment.getUserId());
                    salaryWrapper.eq("del_flag", 0);
                    salaryWrapper.last("LIMIT 1");
                    GhSalaryInfo salaryInfo = ghSalaryInfoService.getOne(salaryWrapper);
                    if (salaryInfo != null) {
                        ghSalaryPayment.setCurrentPosition(salaryInfo.getCurrentPosition());
                        ghSalaryPayment.setCurrentStarLevel(salaryInfo.getCurrentStarLevel());
                    }
                } catch (Exception e) {
                    log.debug("查询工资信息失败，userId: {}", ghSalaryPayment.getUserId(), e);
                }
            }
            
            // 填充发放账号
            if (StringUtils.isNotBlank(ghSalaryPayment.getPaymentAccountId()) && ghBankManagementService != null) {
                try {
                    GhBankManagement bank = ghBankManagementService.getById(ghSalaryPayment.getPaymentAccountId());
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
                        ghSalaryPayment.setPaymentAccount(accountName.length() > 0 ? accountName.toString() : "");
                    }
                } catch (Exception e) {
                    log.debug("查询银行账户信息失败", e);
                }
            }
            
            return Result.OK(ghSalaryPayment);
        } catch (Exception e) {
            log.error("查询工资发放记录详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ghSalaryPayment
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhSalaryPayment ghSalaryPayment) {
        return super.exportXls(request, ghSalaryPayment, GhSalaryPayment.class, "工资发放记录");
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
        return super.importExcel(request, response, GhSalaryPayment.class);
    }
    
    /**
     * 计算应发总额、扣除总额、实发金额
     */
    private void calculateAmounts(GhSalaryPayment payment) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal deductionAmount = BigDecimal.ZERO;
        
        // 应发总额 = 基本工资 + 绩效工资 + 公积金补贴 + 高温补贴 + 其他补贴 + 全勤奖金
        if (payment.getBaseSalary() != null) {
            totalAmount = totalAmount.add(payment.getBaseSalary());
        }
        if (payment.getPerformanceSalary() != null) {
            totalAmount = totalAmount.add(payment.getPerformanceSalary());
        }
        if (payment.getHousingFundSubsidy() != null) {
            totalAmount = totalAmount.add(payment.getHousingFundSubsidy());
        }
        if (payment.getHighTemperatureSubsidy() != null) {
            totalAmount = totalAmount.add(payment.getHighTemperatureSubsidy());
        }
        if (payment.getOtherSubsidy() != null) {
            totalAmount = totalAmount.add(payment.getOtherSubsidy());
        }
        if (payment.getFullAttendanceBonus() != null) {
            totalAmount = totalAmount.add(payment.getFullAttendanceBonus());
        }
        
        // 扣除总额 = 公司社保 + 个人社保
        if (payment.getCompanySocialSecurity() != null) {
            deductionAmount = deductionAmount.add(payment.getCompanySocialSecurity());
        }
        if (payment.getPersonalSocialSecurity() != null) {
            deductionAmount = deductionAmount.add(payment.getPersonalSocialSecurity());
        }
        
        // 实发金额 = 应发总额 - 扣除总额
        BigDecimal actualAmount = totalAmount.subtract(deductionAmount);
        
        payment.setTotalAmount(totalAmount);
        payment.setDeductionAmount(deductionAmount);
        payment.setActualAmount(actualAmount);
    }
    
    /**
     * 从工资发放记录创建银行日记记录
     */
    private void createBankDiaryFromSalaryPayment(GhSalaryPayment payment) {
        if (payment.getActualAmount() == null || payment.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // 检查银行日记服务是否可用
        try {
            org.jeecg.modules.bankdiary.service.IGhBankDiaryService ghBankDiaryService = 
                org.jeecg.common.util.SpringContextUtils.getBean(org.jeecg.modules.bankdiary.service.IGhBankDiaryService.class);
            
            if (ghBankDiaryService == null) {
                return;
            }
            
            // 检查是否已经存在银行日记记录
            QueryWrapper<org.jeecg.modules.bankdiary.entity.GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("business_type", "salary");
            queryWrapper.eq("business_id", payment.getId());
            queryWrapper.eq("del_flag", 0);
            long count = ghBankDiaryService.count(queryWrapper);
            if (count > 0) {
                log.info("工资发放记录ID {} 已存在银行日记记录，跳过创建", payment.getId());
                return;
            }
            
            org.jeecg.modules.bankdiary.entity.GhBankDiary bankDiary = new org.jeecg.modules.bankdiary.entity.GhBankDiary();
            // 使用发放时间，如果没有则使用当前时间
            Date orderDate = payment.getPaymentTime() != null ? payment.getPaymentTime() : new Date();
            bankDiary.setOrderDate(orderDate);
            bankDiary.setBusinessType("salary");
            bankDiary.setBusinessId(payment.getId());
            
            // 费用详情
            String feeDetail = "工资发放";
            if (StringUtils.isNotBlank(payment.getUserName())) {
                feeDetail += " - " + payment.getUserName();
            }
            if (StringUtils.isNotBlank(payment.getPaymentMonth())) {
                feeDetail += " - " + payment.getPaymentMonth();
            }
            bankDiary.setFeeDetail(feeDetail);
            
            // 支出金额（工资发放是支出）
            bankDiary.setExpenseAmount(payment.getActualAmount() != null ? payment.getActualAmount() : BigDecimal.ZERO);
            bankDiary.setIncomeAmount(BigDecimal.ZERO);
            
            // 银行账户
            if (StringUtils.isNotBlank(payment.getPaymentAccountId())) {
                bankDiary.setBankAccountId(payment.getPaymentAccountId());
            }
            
            // 操作人员
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                bankDiary.setOperatorId(sysUser.getId());
                bankDiary.setOperatorName(sysUser.getRealname());
                bankDiary.setCreateBy(sysUser.getUsername());
            }
            
            // 备注
            bankDiary.setRemarks("工资发放 - " + (payment.getUserName() != null ? payment.getUserName() : "") + 
                " - " + (payment.getPaymentMonth() != null ? payment.getPaymentMonth() : ""));
            
            bankDiary.setCreateTime(new Date());
            bankDiary.setDelFlag(0);
            
            // 计算结余金额
            if (bankDiary.getBankAccountId() != null) {
                BigDecimal balanceAmount = calculateBalanceAmount(bankDiary.getBankAccountId(), orderDate);
                if (bankDiary.getExpenseAmount() != null) {
                    balanceAmount = balanceAmount.subtract(bankDiary.getExpenseAmount());
                }
                bankDiary.setBalanceAmount(balanceAmount);
            } else {
                bankDiary.setBalanceAmount(BigDecimal.ZERO);
            }
            
            ghBankDiaryService.save(bankDiary);
            log.info("为工资发放记录ID {} 创建了银行日记记录", payment.getId());
        } catch (Exception e) {
            log.error("创建银行日记记录失败", e);
        }
    }
    
    /**
     * 计算指定账户在指定日期之前的结余金额
     */
    private BigDecimal calculateBalanceAmount(String bankAccountId, Date orderDate) {
        try {
            org.jeecg.modules.bankdiary.service.IGhBankDiaryService ghBankDiaryService = 
                org.jeecg.common.util.SpringContextUtils.getBean(org.jeecg.modules.bankdiary.service.IGhBankDiaryService.class);
            
            if (ghBankDiaryService == null || StringUtils.isBlank(bankAccountId)) {
                return BigDecimal.ZERO;
            }
            
            QueryWrapper<org.jeecg.modules.bankdiary.entity.GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("bank_account_id", bankAccountId);
            queryWrapper.eq("del_flag", 0);
            if (orderDate != null) {
                queryWrapper.lt("order_date", orderDate);
            }
            queryWrapper.orderByDesc("order_date", "create_time");
            queryWrapper.last("LIMIT 1");
            
            org.jeecg.modules.bankdiary.entity.GhBankDiary lastDiary = ghBankDiaryService.getOne(queryWrapper);
            if (lastDiary != null && lastDiary.getBalanceAmount() != null) {
                return lastDiary.getBalanceAmount();
            }
        } catch (Exception e) {
            log.debug("计算结余金额失败", e);
        }
        
        return BigDecimal.ZERO;
    }
}

