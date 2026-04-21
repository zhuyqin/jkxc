package org.jeecg.modules.reimbursement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.address.entity.GhAddressCenter;
import org.jeecg.modules.address.service.IGhAddressCenterService;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.jeecg.modules.reimbursement.entity.GhReimbursement;
import org.jeecg.modules.reimbursement.entity.GhReimbursementDTO;
import org.jeecg.modules.reimbursement.mapper.GhReimbursementMapper;
import org.jeecg.modules.reimbursement.service.IGhReimbursementService;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysTeam;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysTeamService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrderExpense;
import org.jeecg.modules.order.service.IGhOrderExpenseService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 报销审批
 * @Author: jeecg-boot
 * @Date: 2022-03-19
 * @Version: V1.0
 */
@Api(tags = "报销审批")
@RestController
@RequestMapping("/GhReimbursement/ghReimbursement")
@Slf4j
public class GhReimbursementController extends JeecgController<GhReimbursement, IGhReimbursementService> {
    @Autowired
    private IGhReimbursementService ghReimbursementService;

    @Autowired
    private GhReimbursementMapper ghReimbursementMapper;
    
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    
    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    @Value("${category.zc.xzzc:}")
    private String xzzc;
    
    @Value("${category.zc.ywzc:}")
    private String ywzc;
    
    @Autowired(required = false)
    private ISysDepartService sysDepartService;
    
    @Autowired(required = false)
    private ISysUserService sysUserService;
    
    @Autowired(required = false)
    private ISysTeamService sysTeamService;
    
    @Autowired(required = false)
    private IGhBankManagementService ghBankManagementService;
    
    @Autowired(required = false)
    private IGhAddressCenterService ghAddressCenterService;
    
    @Autowired(required = false)
    private IGhBankDiaryService ghBankDiaryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.order.service.IGhOrderExpenseService ghOrderExpenseService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.mapper.SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysRoleService sysRoleService;
    
    @Autowired(required = false)
    private ISysUserRoleService sysUserRoleService;

    /**
     * 分页列表查询
     *
     * @param ghReimbursement
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "报销审批-分页列表查询")
    @ApiOperation(value = "报销审批-分页列表查询", notes = "报销审批-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhReimbursement ghReimbursement,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        try {
            QueryWrapper<GhReimbursement> queryWrapper = QueryGenerator.initQueryWrapper(ghReimbursement, req.getParameterMap());
            
            applyReimbursementPersonScope(queryWrapper);
            
            if (StringUtils.isNotBlank(ghReimbursement.getSearchStatus())) {
                if (ghReimbursement.getSearchStatus().equals("1") && sysCategoryService != null) {
                    // 排除地址费用和人工提成
                    SysCategory one = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "地址费用"));
                    SysCategory two = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "人工提成"));
                    if (one != null) {
                        queryWrapper.ne("category", one.getId());
                    }
                    if (two != null) {
                        queryWrapper.ne("category", two.getId());
                    }
                } else if (ghReimbursement.getSearchStatus().equals("2") && sysCategoryService != null) {
                    // 只查询人工提成
                    SysCategory two = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "人工提成"));
                    if (two != null) {
                        queryWrapper.eq("category", two.getId());
                    } else {
                        // 如果找不到人工提成分类，返回空结果
                        queryWrapper.eq("id", "no_match_category");
                    }
                } else if (ghReimbursement.getSearchStatus().equals("3") && sysCategoryService != null) {
                    // 只查询地址费用
                    SysCategory one = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "地址费用"));
                    if (one != null) {
                        queryWrapper.eq("category", one.getId());
                    } else {
                        // 如果找不到地址费用分类，返回空结果
                        queryWrapper.eq("id", "no_match_category");
                    }
                }
            }
            
            if (StringUtils.isNotBlank(ghReimbursement.getSearchIds())) {
                List<String> idList = Arrays.asList(ghReimbursement.getSearchIds().split(","));
                queryWrapper.in("id", idList);
            }
            
            Page<GhReimbursement> page = new Page<GhReimbursement>(pageNo, pageSize);
            IPage<GhReimbursement> pageList = ghReimbursementService.page(page, queryWrapper);

            Map<String, GhBankManagement> bankAccountMap = new HashMap<>();
            Map<String, SysTeam> teamMap = new HashMap<>();
            Map<String, String> personTeamIdMap = new HashMap<>();
            Set<String> bankIds = new HashSet<>();
            Set<String> teamIds = new HashSet<>();
            Set<String> reimbursementPersons = new HashSet<>();
            for (GhReimbursement renew : pageList.getRecords()) {
                if (renew == null) {
                    continue;
                }
                String reimbursementAccountId = StringUtils.isNotBlank(renew.getReimbursementAccountId())
                        ? renew.getReimbursementAccountId() : renew.getCollectionAccountNumber();
                if (StringUtils.isNotBlank(reimbursementAccountId)) {
                    bankIds.add(reimbursementAccountId);
                }
                if (StringUtils.isNotBlank(renew.getPaymentBankAccountId())) {
                    bankIds.add(renew.getPaymentBankAccountId());
                }
                if (StringUtils.isNotBlank(renew.getTeamId())) {
                    teamIds.add(renew.getTeamId());
                } else if (StringUtils.isNotBlank(renew.getReimbursementPerson())) {
                    reimbursementPersons.add(renew.getReimbursementPerson().trim());
                }
            }
            if (!bankIds.isEmpty() && ghBankManagementService != null) {
                List<GhBankManagement> bankList = ghBankManagementService.listByIds(bankIds);
                if (bankList != null) {
                    for (GhBankManagement bank : bankList) {
                        if (bank != null && StringUtils.isNotBlank(bank.getId())) {
                            bankAccountMap.put(bank.getId(), bank);
                        }
                    }
                }
            }
            if (!reimbursementPersons.isEmpty() && sysUserService != null) {
                QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
                userQuery.and(w -> w.in("username", reimbursementPersons).or().in("realname", reimbursementPersons));
                List<SysUser> users = sysUserService.list(userQuery);
                if (users != null) {
                    for (SysUser user : users) {
                        if (user == null || StringUtils.isBlank(user.getTeamId())) {
                            continue;
                        }
                        if (StringUtils.isNotBlank(user.getUsername())) {
                            personTeamIdMap.putIfAbsent(user.getUsername().trim(), user.getTeamId());
                        }
                        if (StringUtils.isNotBlank(user.getRealname())) {
                            personTeamIdMap.putIfAbsent(user.getRealname().trim(), user.getTeamId());
                        }
                        teamIds.add(user.getTeamId());
                    }
                }
            }
            if (!teamIds.isEmpty() && sysTeamService != null) {
                List<SysTeam> teams = sysTeamService.listByIds(teamIds);
                if (teams != null) {
                    for (SysTeam team : teams) {
                        if (team != null && StringUtils.isNotBlank(team.getId())) {
                            teamMap.put(team.getId(), team);
                        }
                    }
                }
            }

            for (GhReimbursement renew : pageList.getRecords()) {
                // 处理供应商公司信息
                if (StringUtils.isNotBlank(renew.getCompanyId()) && ghAddressCenterService != null) {
                    QueryWrapper<GhAddressCenter> ghAddressCenterQueryWrapper = new QueryWrapper<>();
                    ghAddressCenterQueryWrapper.like("company_name", renew.getCompanyName() != null ? renew.getCompanyName().trim() : "");
                    List<GhAddressCenter> list = ghAddressCenterService.list(ghAddressCenterQueryWrapper);
                    if (!list.isEmpty()) {
                        renew.setSupplierCompany(list.get(0).getSupplierCompany());
                    }
                }
                
                // 处理报销账号显示（三联级）
                String reimbursementAccountId = StringUtils.isNotBlank(renew.getReimbursementAccountId()) 
                    ? renew.getReimbursementAccountId() : renew.getCollectionAccountNumber();
                if (StringUtils.isNotBlank(reimbursementAccountId)) {
                    GhBankManagement ghBankManagement = bankAccountMap.get(reimbursementAccountId);
                    if (ghBankManagement != null) {
                        renew.setReimbursementAccountDisplay(
                            ghBankManagement.getPaymentMethod() + " / " + 
                            ghBankManagement.getPayeePerson() + " / " + 
                            (StringUtils.isNotBlank(ghBankManagement.getAccountNotes()) ? ghBankManagement.getAccountNotes() : "（无网点名称）")
                        );
                        renew.setCollectionAccountNumber(ghBankManagement.getPayeePerson() + "--" + ghBankManagement.getAccountNotes() + "--" + ghBankManagement.getCollectionAccount());
                    }
                }
                
                // 处理支付账号显示（优先使用paymentBankAccountId查询）
                if (StringUtils.isNotBlank(renew.getPaymentBankAccountId())) {
                    GhBankManagement bankAccount = bankAccountMap.get(renew.getPaymentBankAccountId());
                    if (bankAccount != null) {
                        // 构建支付账号显示：收款方式 / 收款单位/人 / 网点名称 / 卡号
                        StringBuilder display = new StringBuilder();
                        display.append(bankAccount.getPaymentMethod()).append(" / ");
                        display.append(bankAccount.getPayeePerson()).append(" / ");
                        display.append(StringUtils.isNotBlank(bankAccount.getAccountNotes()) ? bankAccount.getAccountNotes() : "（无网点名称）");
                        if (StringUtils.isNotBlank(bankAccount.getCollectionAccount())) {
                            display.append(" / ").append(bankAccount.getCollectionAccount());
                        }
                        renew.setPaymentAccountDisplay(display.toString());
                    }
                } else if (StringUtils.isNotBlank(renew.getPaymentBankName()) || StringUtils.isNotBlank(renew.getPaymentBankAccount())) {
                    // 兼容旧数据：使用paymentBankName和paymentBankAccount
                    renew.setPaymentAccountDisplay(
                        (StringUtils.isNotBlank(renew.getPaymentBankName()) ? renew.getPaymentBankName() : "") + 
                        (StringUtils.isNotBlank(renew.getPaymentBankAccount()) ? " " + renew.getPaymentBankAccount() : "")
                    );
                }
                
                // 处理所属团队显示（每次都根据teamId动态查询最新的团队名称，确保团队名称变更后能正确显示）
                String finalTeamId = null;
                if (StringUtils.isNotBlank(renew.getTeamId())) {
                    // 优先使用报销记录中的teamId
                    finalTeamId = renew.getTeamId();
                } else if (StringUtils.isNotBlank(renew.getReimbursementPerson())) {
                    // 如果团队ID为空，尝试从报销人员获取团队信息
                    String maybeTeamId = personTeamIdMap.get(renew.getReimbursementPerson().trim());
                    if (StringUtils.isNotBlank(maybeTeamId)) {
                        finalTeamId = maybeTeamId;
                        // 更新报销记录的teamId，但不更新teamName（每次都动态查询）
                        renew.setTeamId(finalTeamId);
                    }
                }
                
                // 根据teamId查询最新的团队名称（确保团队名称变更后能正确显示）
                if (StringUtils.isNotBlank(finalTeamId)) {
                    try {
                        SysTeam team = teamMap.get(finalTeamId);
                        if (team != null) {
                            renew.setTeamName(team.getTeamName());
                        }
                    } catch (Exception e) {
                        log.debug("查询团队信息失败，teamId: {}", finalTeamId, e);
                    }
                }
                
                // 注意：现在使用所属团队，不再使用所属部门
            }

            // 如果有SupplierContact参数，再进行过滤
            if (StringUtils.isNotBlank(ghReimbursement.getSupplierContact())) {
                List<GhReimbursement> filteredRecords = pageList.getRecords().stream()
                        .filter(renew -> renew.getSupplierContact() != null &&
                                renew.getSupplierContact().toLowerCase()
                                        .contains(ghReimbursement.getSupplierContact().toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
                pageList.setRecords(filteredRecords);
            }
            
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("查询报销列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 报销数据分析：汇总金额、按类目/月份/报销人分布（与列表相同的数据权限与类目筛选逻辑）
     */
    @AutoLog(value = "报销审批-数据分析")
    @ApiOperation(value = "报销审批-数据分析", notes = "按报销时间、数据权限与类目筛选汇总")
    @GetMapping(value = "/analytics")
    public Result<?> analytics(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) String dateBegin,
                               @RequestParam(required = false) String dateEnd,
                               @RequestParam(required = false, defaultValue = "1") String searchStatus) {
        try {
            ReimbursementPersonScope scope = resolveReimbursementPersonScope();
            boolean applyPersonFilter = !scope.unrestricted;
            List<String> persons = scope.allowedRealnames;
            if (!scope.unrestricted && (persons == null || persons.isEmpty())) {
                Map<String, Object> empty = buildEmptyAnalyticsPayload(searchStatus);
                return Result.OK(empty);
            }

            Date begin;
            Date end;
            if (StringUtils.isNotBlank(dateBegin) && StringUtils.isNotBlank(dateEnd)) {
                SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
                begin = day.parse(dateBegin.trim());
                Calendar c = Calendar.getInstance();
                c.setTime(day.parse(dateEnd.trim()));
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
                c.set(Calendar.MILLISECOND, 999);
                end = c.getTime();
            } else {
                int y = year != null ? year : Calendar.getInstance().get(Calendar.YEAR);
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, y);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                begin = cal.getTime();
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 31);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                end = cal.getTime();
            }

            AnalyticsCategoryParams cat = buildAnalyticsCategoryParams(searchStatus);
            if ("2".equals(searchStatus) && oConvertUtils.isEmpty(cat.onlyCategoryId)) {
                Map<String, Object> empty = buildEmptyAnalyticsPayload(searchStatus);
                empty.put("dateBegin", begin);
                empty.put("dateEnd", end);
                return Result.OK(empty);
            }
            if ("3".equals(searchStatus) && oConvertUtils.isEmpty(cat.onlyCategoryId)) {
                Map<String, Object> empty = buildEmptyAnalyticsPayload(searchStatus);
                empty.put("dateBegin", begin);
                empty.put("dateEnd", end);
                return Result.OK(empty);
            }

            Map<String, Object> summary = ghReimbursementMapper.selectAnalyticsSummary(
                    applyPersonFilter, persons, begin, end,
                    cat.onlyCategoryId, cat.excludeAddressId, cat.excludeSalaryId);
            if (summary == null) {
                summary = new HashMap<>(4);
            }
            List<Map<String, Object>> byCategory = ghReimbursementMapper.selectAnalyticsByCategory(
                    applyPersonFilter, persons, begin, end,
                    cat.onlyCategoryId, cat.excludeAddressId, cat.excludeSalaryId);
            List<Map<String, Object>> byMonth = ghReimbursementMapper.selectAnalyticsByMonth(
                    applyPersonFilter, persons, begin, end,
                    cat.onlyCategoryId, cat.excludeAddressId, cat.excludeSalaryId);
            List<Map<String, Object>> byPerson = ghReimbursementMapper.selectAnalyticsByPerson(
                    applyPersonFilter, persons, begin, end,
                    cat.onlyCategoryId, cat.excludeAddressId, cat.excludeSalaryId);

            Calendar chartCal = Calendar.getInstance();
            chartCal.setTime(begin);
            int chartYear = chartCal.get(Calendar.YEAR);
            List<Map<String, Object>> byCategoryMonth = ghReimbursementMapper.selectAnalyticsByCategoryMonth(
                    applyPersonFilter, persons, begin, end,
                    cat.onlyCategoryId, cat.excludeAddressId, cat.excludeSalaryId, chartYear);

            Map<String, Object> result = new HashMap<>(12);
            result.put("summary", summary);
            result.put("byCategory", byCategory != null ? byCategory : Collections.emptyList());
            result.put("byMonth", byMonth != null ? byMonth : Collections.emptyList());
            result.put("byPerson", byPerson != null ? byPerson : Collections.emptyList());
            result.put("byCategoryMonth", byCategoryMonth != null ? byCategoryMonth : Collections.emptyList());
            result.put("chartYear", chartYear);
            result.put("dateBegin", begin);
            result.put("dateEnd", end);
            result.put("searchStatus", searchStatus);
            return Result.OK(result);
        } catch (Exception e) {
            log.error("报销数据分析失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping(value = "/expendlist")
    public Result<?> queryExpendlist(GhReimbursement ghReimbursement, String type, String date,
                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                     HttpServletRequest req) {
        try {
            QueryWrapper queryWrapper = new QueryWrapper();
            if (sysCategoryService != null) {
                QueryWrapper<SysCategory> categoryQueryWrapper = new QueryWrapper<>();
                if ("xzzc".equals(type) && StringUtils.isNotBlank(xzzc)) {
                    categoryQueryWrapper.likeRight("code", xzzc);
                    List<SysCategory> list = sysCategoryService.list(categoryQueryWrapper);
                    List<String> orders = list.stream().map(SysCategory::getId).collect(java.util.stream.Collectors.toList());
                    queryWrapper.in("category", orders);
                } else if ("cbzc".equals(type) && StringUtils.isNotBlank(ywzc)) {
                    categoryQueryWrapper.likeRight("code", ywzc);
                    List<SysCategory> list = sysCategoryService.list(categoryQueryWrapper);
                    List<String> orders = list.stream().map(SysCategory::getId).collect(java.util.stream.Collectors.toList());
                    queryWrapper.in("category", orders);
                } else if (!"qb".equals(type)) {
                    List<String> orders = new ArrayList<>();
                    orders.add("1");
                    queryWrapper.in("category", orders);
                }
            }
            queryWrapper.orderByDesc("create_time");
            
            if (StringUtils.isNotBlank(date)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                if ("1".equals(date)) {
                    Date zero = calendar.getTime();
                    queryWrapper.gt("create_time", zero);
                } else if ("2".equals(date)) {
                    calendar.add(Calendar.DATE, -6);
                    Date week = calendar.getTime();
                    queryWrapper.gt("create_time", week);
                } else if ("3".equals(date)) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    Date month = calendar.getTime();
                    queryWrapper.gt("create_time", month);
                } else if ("4".equals(date)) {
                    calendar.set(Calendar.DAY_OF_YEAR, 1);
                    Date year = calendar.getTime();
                    queryWrapper.gt("create_time", year);
                }
            }
            
            Page<GhReimbursement> page = new Page<GhReimbursement>(pageNo, pageSize);
            IPage<GhReimbursement> pageList = ghReimbursementService.page(page, queryWrapper);
            
            if (ghBankManagementService != null) {
                Set<String> accountIds = new HashSet<>();
                for (GhReimbursement renew : pageList.getRecords()) {
                    if (renew != null && StringUtils.isNotBlank(renew.getCollectionAccountNumber())) {
                        accountIds.add(renew.getCollectionAccountNumber());
                    }
                }
                Map<String, GhBankManagement> bankMap = new HashMap<>();
                if (!accountIds.isEmpty()) {
                    List<GhBankManagement> banks = ghBankManagementService.listByIds(accountIds);
                    if (banks != null) {
                        for (GhBankManagement bank : banks) {
                            if (bank != null && StringUtils.isNotBlank(bank.getId())) {
                                bankMap.put(bank.getId(), bank);
                            }
                        }
                    }
                }
                for (GhReimbursement renew : pageList.getRecords()) {
                    GhBankManagement ghBankManagement = bankMap.get(renew.getCollectionAccountNumber());
                    if (ghBankManagement != null) {
                        renew.setCollectionAccountNumber(ghBankManagement.getPayeePerson() + "--" + ghBankManagement.getAccountNotes() + "--" + ghBankManagement.getCollectionAccount());
                    }
                }
            }
            
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("查询支出列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping(value = "/getbxList")
    public Result<?> getbxList(@RequestParam(value = "year", required = false) Integer year,
                               @RequestParam(value = "recordType", required = false) String recordType) {
        try {
            if (year == null) {
                year = Calendar.getInstance().get(Calendar.YEAR);
            }
            List<Map<String, Object>> list = ghReimbursementMapper.getbxList(year);
            
            // 定义销售成本和管理成本的类别
            Set<String> salesCostCategories = new HashSet<>(Arrays.asList(
                    "公关费用", "刻章费用", "商标成本", "地址费用",
                    "审计成本", "渠道返佣", "知产成本", "科小成本", "转让成本"
            ));

            Set<String> managementCostCategories = new HashSet<>(Arrays.asList(
                    "办公耗材", "团建费用", "快递费用", "房租费用",
                    "水电费用", "系统费用", "维修费用", "电话话费", "其他费用"
            ));
            
            // 遍历数据并添加 cost_type 字段
            for (Map<String, Object> record : list) {
                String category = (String) record.get("报销类别");
                String costType = null;

                if (salesCostCategories.contains(category)) {
                    costType = "销售成本";
                } else if (managementCostCategories.contains(category)) {
                    costType = "管理成本";
                } else {
                    costType = "其他成本";
                }

                record.put("报销归类", costType);
            }
            
            // 定义排序优先级（销售成本 → 管理成本 → 其他成本）
            Map<String, Integer> priority = new HashMap<>();
            priority.put("销售成本", 1);
            priority.put("管理成本", 2);
            priority.put("其他成本", 3);

            // 使用 Stream 排序
            List<Map<String, Object>> sortedList = list.stream()
                    .sorted(Comparator.comparingInt(
                            map -> priority.getOrDefault((String) map.get("报销归类"), 99)
                    ))
                    .collect(java.util.stream.Collectors.toList());
            
            if (StringUtils.isNotBlank(recordType)) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> map : sortedList) {
                    if (map.get("报销归类").equals(recordType)) {
                        result.add(map);
                    }
                }
                return Result.OK(result);
            }
            
            return Result.OK(sortedList);
        } catch (Exception e) {
            log.error("查询报销统计列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param ghReimbursement
     * @return
     */
    @AutoLog(value = "报销审批-添加")
    @ApiOperation(value = "报销审批-添加", notes = "报销审批-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhReimbursement ghReimbursement, HttpServletRequest request) {
        try {
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            String username = JwtUtil.getUsername(token);
            LoginUser sysUser = sysBaseAPI.getUserByName(username);
            
            ghReimbursement.setCreateTime(new Date());
            if (sysUser != null) {
                ghReimbursement.setCreateBy(sysUser.getRealname());
                
                // 如果报销人员为空，使用当前登录用户
                if (StringUtils.isBlank(ghReimbursement.getReimbursementPerson())) {
                    ghReimbursement.setReimbursementPerson(sysUser.getRealname());
                }
            }
            
            // 如果团队ID为空，从报销人员获取
            if (StringUtils.isBlank(ghReimbursement.getTeamId()) && StringUtils.isNotBlank(ghReimbursement.getReimbursementPerson()) && sysUserService != null) {
                SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>().eq("realname", ghReimbursement.getReimbursementPerson()).or().eq("username", ghReimbursement.getReimbursementPerson()));
                if (user != null && StringUtils.isNotBlank(user.getTeamId())) {
                    ghReimbursement.setTeamId(user.getTeamId());
                }
            }
            
            // 如果报销账号ID存在，同时设置collectionAccountNumber
            if (StringUtils.isNotBlank(ghReimbursement.getReimbursementAccountId())) {
                ghReimbursement.setCollectionAccountNumber(ghReimbursement.getReimbursementAccountId());
            }
            
            ghReimbursementService.save(ghReimbursement);
            return Result.OK("添加成功！");
        } catch (Exception e) {
            log.error("添加报销失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 编辑
     *
     * @param ghReimbursement
     * @return
     */
    @AutoLog(value = "报销审批-编辑")
    @ApiOperation(value = "报销审批-编辑", notes = "报销审批-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody GhReimbursement ghReimbursement, HttpServletRequest request) {
        try {
            // 查询原始记录，检查审核状态是否从非通过变为通过
            GhReimbursement oldReimbursement = ghReimbursementService.getById(ghReimbursement.getId());
            boolean wasApproved = oldReimbursement != null && "1".equals(oldReimbursement.getAuditFlag());
            boolean isNowApproved = "1".equals(ghReimbursement.getAuditFlag());
            boolean isNowRejected = "2".equals(ghReimbursement.getAuditFlag());
            
            // 如果团队ID为空，从报销人员获取
            if (StringUtils.isBlank(ghReimbursement.getTeamId()) && StringUtils.isNotBlank(ghReimbursement.getReimbursementPerson()) && sysUserService != null) {
                SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>().eq("realname", ghReimbursement.getReimbursementPerson()).or().eq("username", ghReimbursement.getReimbursementPerson()));
                if (user != null && StringUtils.isNotBlank(user.getTeamId())) {
                    ghReimbursement.setTeamId(user.getTeamId());
                }
            }
            
            // 如果报销账号ID存在，同时设置collectionAccountNumber
            if (StringUtils.isNotBlank(ghReimbursement.getReimbursementAccountId())) {
                ghReimbursement.setCollectionAccountNumber(ghReimbursement.getReimbursementAccountId());
            }
            
            // 如果支付单位ID存在，根据ID查询银行账户信息并设置支付银行信息
            if (StringUtils.isNotBlank(ghReimbursement.getPaymentBankAccountId()) && ghBankManagementService != null) {
                GhBankManagement bankAccount = ghBankManagementService.getById(ghReimbursement.getPaymentBankAccountId());
                if (bankAccount != null) {
                    // 设置支付银行开户行和卡号（用于兼容旧数据）
                    if (StringUtils.isBlank(ghReimbursement.getPaymentBankName())) {
                        ghReimbursement.setPaymentBankName(bankAccount.getPayeePerson());
                    }
                    if (StringUtils.isBlank(ghReimbursement.getPaymentBankAccount())) {
                        ghReimbursement.setPaymentBankAccount(bankAccount.getCollectionAccount());
                    }
                }
            }
            
            ghReimbursementService.updateById(ghReimbursement);
            
            // 如果审核状态变成已审核或已拒绝，同步更新对应的支出记录
            if ((isNowApproved || isNowRejected) && ghOrderExpenseService != null) {
                try {
                    syncExpenseFromReimbursement(ghReimbursement);
                } catch (Exception e) {
                    log.error("同步支出记录失败，报销ID: {}", ghReimbursement.getId(), e);
                    // 不抛出异常，避免影响报销审核
                }
            }
            
            // 如果审核状态从未通过变为通过，创建银行日记记录
            if (!wasApproved && isNowApproved && ghBankDiaryService != null) {
                try {
                    createBankDiaryFromReimbursement(ghReimbursement, request);
                } catch (Exception e) {
                    log.error("创建银行日记记录失败，报销ID: {}", ghReimbursement.getId(), e);
                    // 不抛出异常，避免影响报销编辑
                }
            }
            
            return Result.OK("编辑成功!");
        } catch (Exception e) {
            log.error("编辑报销失败", e);
            return Result.error("编辑失败：" + e.getMessage());
        }
    }

    /**
     * 更新备注
     *
     * @param requestBody 包含id和remarks的请求体
     * @return
     */
    @AutoLog(value = "报销审批-更新备注")
    @ApiOperation(value = "报销审批-更新备注", notes = "报销审批-更新备注")
    @PostMapping(value = "/updateRemarks")
    public Result<?> updateRemarks(@RequestBody Map<String, Object> requestBody) {
        try {
            String id = (String) requestBody.get("id");
            String remarks = (String) requestBody.get("remarks");
            
            if (StringUtils.isBlank(id)) {
                return Result.error("报销记录ID不能为空！");
            }
            
            GhReimbursement reimbursement = ghReimbursementService.getById(id);
            if (reimbursement == null) {
                return Result.error("报销记录不存在！");
            }
            
            reimbursement.setRemarks(remarks);
            ghReimbursementService.updateById(reimbursement);
            
            return Result.OK("备注更新成功！");
        } catch (Exception e) {
            log.error("更新备注失败", e);
            return Result.error("更新备注失败：" + e.getMessage());
        }
    }

    /**
     * 报销审核
     *
     * @param ghReimbursement
     * @return
     */
    @AutoLog(value = "报销审批-审核")
    @ApiOperation(value = "报销审批-审核", notes = "报销审批-审核")
    @RequiresPermissions("reimbursement:audit")
    @PostMapping(value = "/audit")
    public Result<?> audit(@RequestBody GhReimbursement ghReimbursement, HttpServletRequest request) {
        try {
            // 查询原始记录，检查审核状态是否从非通过变为通过
            GhReimbursement oldReimbursement = ghReimbursementService.getById(ghReimbursement.getId());
            if (oldReimbursement == null) {
                return Result.error("报销记录不存在！");
            }
            boolean wasApproved = "1".equals(oldReimbursement.getAuditFlag());
            boolean isNowApproved = "1".equals(ghReimbursement.getAuditFlag());
            boolean isNowRejected = "2".equals(ghReimbursement.getAuditFlag());
            
            // 如果团队ID为空，从报销人员获取
            if (StringUtils.isBlank(ghReimbursement.getTeamId()) && StringUtils.isNotBlank(ghReimbursement.getReimbursementPerson()) && sysUserService != null) {
                SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>().eq("realname", ghReimbursement.getReimbursementPerson()).or().eq("username", ghReimbursement.getReimbursementPerson()));
                if (user != null && StringUtils.isNotBlank(user.getTeamId())) {
                    ghReimbursement.setTeamId(user.getTeamId());
                }
            }
            
            // 如果支付单位ID存在，根据ID查询银行账户信息并设置支付银行信息
            if (StringUtils.isNotBlank(ghReimbursement.getPaymentBankAccountId()) && ghBankManagementService != null) {
                GhBankManagement bankAccount = ghBankManagementService.getById(ghReimbursement.getPaymentBankAccountId());
                if (bankAccount != null) {
                    // 设置支付银行开户行和卡号（用于兼容旧数据）
                    if (StringUtils.isBlank(ghReimbursement.getPaymentBankName())) {
                        ghReimbursement.setPaymentBankName(bankAccount.getPayeePerson());
                    }
                    if (StringUtils.isBlank(ghReimbursement.getPaymentBankAccount())) {
                        ghReimbursement.setPaymentBankAccount(bankAccount.getCollectionAccount());
                    }
                }
            }
            
            ghReimbursementService.updateById(ghReimbursement);
            
            // 如果审核状态变成已审核或已拒绝，同步更新对应的支出记录
            if ((isNowApproved || isNowRejected) && ghOrderExpenseService != null) {
                try {
                    // 重新查询完整的报销记录，确保包含 orderExpenseId 等字段
                    GhReimbursement fullReimbursement = ghReimbursementService.getById(ghReimbursement.getId());
                    if (fullReimbursement != null) {
                        // 更新审核状态和支付账户信息（从传入的参数中获取）
                        if (ghReimbursement.getAuditFlag() != null) {
                            fullReimbursement.setAuditFlag(ghReimbursement.getAuditFlag());
                        }
                        if (ghReimbursement.getPaymentBankAccountId() != null) {
                            fullReimbursement.setPaymentBankAccountId(ghReimbursement.getPaymentBankAccountId());
                        }
                        if (ghReimbursement.getReimbursementTime() != null) {
                            fullReimbursement.setReimbursementTime(ghReimbursement.getReimbursementTime());
                        }
                        syncExpenseFromReimbursement(fullReimbursement);
                    } else {
                        // 如果查询不到，使用传入的对象（可能没有 orderExpenseId）
                    syncExpenseFromReimbursement(ghReimbursement);
                    }
                } catch (Exception e) {
                    log.error("同步支出记录失败，报销ID: {}", ghReimbursement.getId(), e);
                    // 不抛出异常，避免影响报销审核
                }
            }
            
            // 如果审核状态从未通过变为通过，创建银行日记记录
            if (!wasApproved && isNowApproved && ghBankDiaryService != null) {
                try {
                    createBankDiaryFromReimbursement(ghReimbursement, request);
                } catch (Exception e) {
                    log.error("创建银行日记记录失败，报销ID: {}", ghReimbursement.getId(), e);
                    // 不抛出异常，避免影响报销审核
                }
            }
            
            return Result.OK("审核成功!");
        } catch (Exception e) {
            log.error("审核报销失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 从报销记录创建银行日记记录
     */
    private void createBankDiaryFromReimbursement(GhReimbursement reimbursement, HttpServletRequest request) {
        // 检查是否已经存在银行日记记录
        if (ghBankDiaryService != null) {
            QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("reimbursement_id", reimbursement.getId());
            queryWrapper.eq("del_flag", 0);
            long count = ghBankDiaryService.count(queryWrapper);
            if (count > 0) {
                log.info("报销ID {} 已存在银行日记记录，跳过创建", reimbursement.getId());
                return;
            }
        }
        
        GhBankDiary bankDiary = new GhBankDiary();
        bankDiary.setOrderDate(reimbursement.getReimbursementTime() != null ? reimbursement.getReimbursementTime() : new Date());
        bankDiary.setBusinessType("reimbursement");
        bankDiary.setBusinessId(reimbursement.getId());
        bankDiary.setReimbursementId(reimbursement.getId());
        
        // 费用详情
        String feeDetail = reimbursement.getReimbursementName();
        if (StringUtils.isNotBlank(reimbursement.getCompanyName())) {
            feeDetail += " - " + reimbursement.getCompanyName();
        }
        bankDiary.setFeeDetail(feeDetail);
        
        // 支出金额（报销是支出）
        if (reimbursement.getTotalPrice() != null) {
            bankDiary.setExpenseAmount(new BigDecimal(reimbursement.getTotalPrice().toString()));
        }
        bankDiary.setIncomeAmount(BigDecimal.ZERO);
        
        // 银行账户（使用支付账号ID，报销是支出，应该使用支付账号）
        if (StringUtils.isNotBlank(reimbursement.getPaymentBankAccountId())) {
            bankDiary.setBankAccountId(reimbursement.getPaymentBankAccountId());
            // 构建三联级显示：收款方式 / 收款单位/人 / 网点名称 / 卡号
            if (ghBankManagementService != null) {
                try {
                    GhBankManagement bankAccount = ghBankManagementService.getById(reimbursement.getPaymentBankAccountId());
                    if (bankAccount != null) {
                        StringBuilder accountName = new StringBuilder();
                        if (StringUtils.isNotBlank(bankAccount.getPaymentMethod())) {
                            accountName.append(bankAccount.getPaymentMethod());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getPayeePerson())) {
                            if (accountName.length() > 0) accountName.append(" / ");
                            accountName.append(bankAccount.getPayeePerson());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getAccountNotes())) {
                            if (accountName.length() > 0) accountName.append(" / ");
                            accountName.append(bankAccount.getAccountNotes());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getCollectionAccount())) {
                            if (accountName.length() > 0) accountName.append(" / ");
                            accountName.append(bankAccount.getCollectionAccount());
                        }
                        bankDiary.setBankAccountName(accountName.length() > 0 ? accountName.toString() : "");
                    }
                } catch (Exception e) {
                    log.debug("查询支付银行账户信息失败，报销ID: {}", reimbursement.getId(), e);
                }
            }
        } else if (StringUtils.isNotBlank(reimbursement.getCollectionAccountNumber())) {
            // 兼容旧数据：如果没有支付账号ID，使用收款账号
            bankDiary.setBankAccountId(reimbursement.getCollectionAccountNumber());
        } else if (StringUtils.isNotBlank(reimbursement.getReimbursementAccountId())) {
            // 兼容旧数据：使用报销账号ID
            bankDiary.setBankAccountId(reimbursement.getReimbursementAccountId());
        }
        
        // 操作人员
        String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        if (sysUser != null) {
            bankDiary.setOperatorId(sysUser.getId());
            bankDiary.setOperatorName(sysUser.getRealname());
            bankDiary.setCreateBy(sysUser.getUsername());
        }
        
        // 公司信息
        bankDiary.setCompanyId(reimbursement.getCompanyId());
        bankDiary.setCompanyName(reimbursement.getCompanyName());
        
        // 备注
        bankDiary.setRemarks(reimbursement.getRemarks());
        
        // 凭证
        if (StringUtils.isNotBlank(reimbursement.getVouchers())) {
            bankDiary.setVouchers(reimbursement.getVouchers());
        }
        
        bankDiary.setCreateTime(new Date());
        bankDiary.setDelFlag(0);
        
        // 计算结余金额
        if (bankDiary.getBankAccountId() != null) {
            BigDecimal balanceAmount = calculateBalanceAmount(bankDiary.getBankAccountId(), bankDiary.getOrderDate());
            if (bankDiary.getExpenseAmount() != null) {
                balanceAmount = balanceAmount.subtract(bankDiary.getExpenseAmount());
            }
            bankDiary.setBalanceAmount(balanceAmount);
        }
        
        if (ghBankDiaryService != null) {
            ghBankDiaryService.save(bankDiary);
            log.info("为报销ID {} 创建了银行日记记录", reimbursement.getId());
        }
    }
    
    /**
     * 计算指定账户在指定日期之前的结余金额
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
     * 从报销记录同步更新支出记录（当出纳审核时填了支付账号）
     * 直接通过报销表中的支出ID更新支出记录
     * @param reimbursement 报销记录
     */
    private void syncExpenseFromReimbursement(GhReimbursement reimbursement) {
        if (ghOrderExpenseService == null || reimbursement == null) {
            return;
        }
        
        try {
            // 直接通过报销表中的支出ID查找支出记录
            String orderExpenseId = reimbursement.getOrderExpenseId();
            if (StringUtils.isBlank(orderExpenseId)) {
                log.debug("报销记录中未找到支出记录ID，无法同步支出记录，报销ID: {}", reimbursement.getId());
                return;
            }
            
            // 根据支出ID查找支出记录
            GhOrderExpense expense = ghOrderExpenseService.getById(orderExpenseId);
            
            if (expense == null) {
                log.debug("未找到对应的支出记录，报销ID: {}, 支出ID: {}", reimbursement.getId(), orderExpenseId);
                return;
            }
            
            // 检查支出记录是否已删除
            if (expense.getDelFlag() != null && expense.getDelFlag() != 0) {
                log.debug("支出记录已删除，无法同步，报销ID: {}, 支出ID: {}", reimbursement.getId(), orderExpenseId);
                return;
            }
            
            try {
                // 获取报销的审核状态
                String auditFlag = reimbursement.getAuditFlag();
                boolean isApproved = "1".equals(auditFlag);
                boolean isRejected = "2".equals(auditFlag);
                
                // 构建支付账户显示（从银行账户信息）- 只有已审核时才更新支付账户
                String paymentAccountDisplay = null;
                if (isApproved && StringUtils.isNotBlank(reimbursement.getPaymentBankAccountId()) && ghBankManagementService != null) {
                    GhBankManagement bankAccount = ghBankManagementService.getById(reimbursement.getPaymentBankAccountId());
                    if (bankAccount != null) {
                        // 构建支付账户显示：收款方式 / 收款单位/人 / 网点名称 / 卡号
                        StringBuilder display = new StringBuilder();
                        if (StringUtils.isNotBlank(bankAccount.getPaymentMethod())) {
                            display.append(bankAccount.getPaymentMethod());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getPayeePerson())) {
                            if (display.length() > 0) display.append(" / ");
                            display.append(bankAccount.getPayeePerson());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getAccountNotes())) {
                            if (display.length() > 0) display.append(" / ");
                            display.append(bankAccount.getAccountNotes());
                        }
                        if (StringUtils.isNotBlank(bankAccount.getCollectionAccount())) {
                            if (display.length() > 0) display.append(" / ");
                            display.append(bankAccount.getCollectionAccount());
                        }
                        paymentAccountDisplay = display.toString();
                    }
                }
                
                // 更新支出记录
                // 如果已审核且填写了支付账号，更新支付账户
                if (isApproved && StringUtils.isNotBlank(paymentAccountDisplay)) {
                    expense.setPaymentAccount(paymentAccountDisplay);
                }
                // 如果已审核且报销时间不为空，更新支出时间
                if (isApproved && reimbursement.getReimbursementTime() != null) {
                    expense.setExpenseTime(reimbursement.getReimbursementTime());
                }
                // 更新审核状态
                if (isApproved) {
                    expense.setAuditStatus("1"); // 已审核
                } else if (isRejected) {
                    expense.setAuditStatus("2"); // 已拒绝
                }
                expense.setUpdateTime(new Date());
                
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    expense.setUpdateBy(sysUser.getUsername());
                }
                
                ghOrderExpenseService.updateById(expense);
                String statusText = isApproved ? "已审核" : (isRejected ? "已拒绝" : "未知");
                log.info("同步更新支出记录，报销ID: {}, 支出ID: {}, 支付账户: {}, 审核状态: {}", 
                    reimbursement.getId(), expense.getId(), 
                    isApproved && StringUtils.isNotBlank(paymentAccountDisplay) ? paymentAccountDisplay : "未填写",
                    statusText);
            } catch (Exception e) {
                log.error("更新支出记录失败，报销ID: {}, 支出ID: {}", reimbursement.getId(), orderExpenseId, e);
            }
        } catch (Exception e) {
            log.error("同步支出记录失败，报销ID: {}", reimbursement.getId(), e);
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "报销审批-通过id删除")
    @ApiOperation(value = "报销审批-通过id删除", notes = "报销审批-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        try {
            GhReimbursement row = ghReimbursementService.getById(id);
            if (row == null) {
                return Result.error("记录不存在或已删除");
            }
            if (!isReimbursementDeletableByAudit(row.getAuditFlag())) {
                return Result.error("已审核或已拒绝的报销不可删除");
            }
            ghReimbursementService.removeById(id);
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error("删除报销失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "报销审批-批量删除")
    @ApiOperation(value = "报销审批-批量删除", notes = "报销审批-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        try {
            List<String> idList = Arrays.asList(ids.split(","));
            List<GhReimbursement> list = ghReimbursementService.listByIds(idList);
            for (GhReimbursement row : list) {
                if (row != null && !isReimbursementDeletableByAudit(row.getAuditFlag())) {
                    return Result.error("选中记录中包含已审核或已拒绝的数据，无法删除");
                }
            }
            this.ghReimbursementService.removeByIds(idList);
            return Result.OK("批量删除成功!");
        } catch (Exception e) {
            log.error("批量删除报销失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "报销审批-通过id查询")
    @ApiOperation(value = "报销审批-通过id查询", notes = "报销审批-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        try {
            GhReimbursement ghReimbursement = ghReimbursementService.getById(id);
            if (ghReimbursement == null) {
                return Result.error("未找到对应数据");
            }
            
            // 处理所属团队显示（每次都根据teamId动态查询最新的团队名称）
            String finalTeamId = null;
            if (StringUtils.isNotBlank(ghReimbursement.getTeamId())) {
                finalTeamId = ghReimbursement.getTeamId();
            } else if (StringUtils.isNotBlank(ghReimbursement.getReimbursementPerson()) && sysUserService != null) {
                // 如果团队ID为空，尝试从报销人员获取团队信息
                SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>()
                    .eq("username", ghReimbursement.getReimbursementPerson())
                    .or()
                    .eq("realname", ghReimbursement.getReimbursementPerson())
                    .last("LIMIT 1"));
                if (user != null && StringUtils.isNotBlank(user.getTeamId())) {
                    finalTeamId = user.getTeamId();
                    ghReimbursement.setTeamId(finalTeamId);
                }
            }
            
            // 根据teamId查询最新的团队名称
            if (StringUtils.isNotBlank(finalTeamId) && sysTeamService != null) {
                try {
                    SysTeam team = sysTeamService.getById(finalTeamId);
                    if (team != null) {
                        ghReimbursement.setTeamName(team.getTeamName());
                    }
                } catch (Exception e) {
                    log.debug("查询团队信息失败，teamId: {}", finalTeamId, e);
                }
            }
            
            return Result.OK(ghReimbursement);
        } catch (Exception e) {
            log.error("查询报销详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ghReimbursement
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhReimbursement ghReimbursement) {
        return super.exportXls(request, ghReimbursement, GhReimbursement.class, "报销审批");
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
        return super.importExcel(request, response, GhReimbursement.class);
    }

    /**
     * 仅待审核（或未设置审核状态）的报销允许删除；已审核(1)、已拒绝(2)不可删
     */
    private static boolean isReimbursementDeletableByAudit(String auditFlag) {
        return auditFlag == null || auditFlag.isEmpty() || "0".equals(auditFlag);
    }

    /**
     * 与列表查询一致：报销人员数据范围
     */
    private void applyReimbursementPersonScope(QueryWrapper<GhReimbursement> queryWrapper) {
        ReimbursementPersonScope scope = resolveReimbursementPersonScope();
        if (scope.unrestricted) {
            return;
        }
        if (scope.allowedRealnames == null || scope.allowedRealnames.isEmpty()) {
            queryWrapper.eq("id", "no_match_user");
        } else {
            queryWrapper.in("reimbursement_person", scope.allowedRealnames);
        }
    }

    private ReimbursementPersonScope resolveReimbursementPersonScope() {
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser == null || sysUserRoleMapper == null || sysRoleService == null || sysUserService == null) {
                return new ReimbursementPersonScope(true, null);
            }
            List<String> userRoleIds = sysUserRoleMapper.getRoleIdByUserName(loginUser.getUsername());
            if (userRoleIds != null && !userRoleIds.isEmpty()) {
                List<String> subRoleIds = new ArrayList<>();
                for (String roleId : userRoleIds) {
                    org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
                    if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
                        subRoleIds.addAll(getSubRoleIdsOnly(roleId));
                    }
                }
                List<String> allowedRealnames = new ArrayList<>();
                if (oConvertUtils.isNotEmpty(loginUser.getRealname())) {
                    allowedRealnames.add(loginUser.getRealname());
                }
                if (!subRoleIds.isEmpty()) {
                    List<String> subUserIds = getUserIdsByRoleIds(subRoleIds);
                    if (subUserIds != null && !subUserIds.isEmpty()) {
                        List<SysUser> subUsers = sysUserService.list(
                                new LambdaQueryWrapper<SysUser>()
                                        .in(SysUser::getId, subUserIds)
                                        .eq(SysUser::getDelFlag, 0)
                        );
                        if (subUsers != null && !subUsers.isEmpty()) {
                            for (SysUser user : subUsers) {
                                if (oConvertUtils.isNotEmpty(user.getRealname())) {
                                    allowedRealnames.add(user.getRealname());
                                }
                            }
                        }
                    }
                }
                if (!allowedRealnames.isEmpty()) {
                    return new ReimbursementPersonScope(false, allowedRealnames);
                }
                return new ReimbursementPersonScope(false, Collections.emptyList());
            }
            List<String> selfOnly = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(loginUser.getRealname())) {
                selfOnly.add(loginUser.getRealname());
            }
            if (!selfOnly.isEmpty()) {
                return new ReimbursementPersonScope(false, selfOnly);
            }
            return new ReimbursementPersonScope(false, Collections.emptyList());
        } catch (Exception e) {
            log.error("报销权限过滤失败", e);
            try {
                LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (loginUser != null && oConvertUtils.isNotEmpty(loginUser.getRealname())) {
                    return new ReimbursementPersonScope(false, Collections.singletonList(loginUser.getRealname()));
                }
            } catch (Exception ex) {
                log.error("设置默认权限过滤失败", ex);
            }
            return new ReimbursementPersonScope(false, Collections.emptyList());
        }
    }

    private AnalyticsCategoryParams buildAnalyticsCategoryParams(String searchStatus) {
        AnalyticsCategoryParams p = new AnalyticsCategoryParams();
        if (sysCategoryService == null) {
            return p;
        }
        if (StringUtils.isBlank(searchStatus) || "1".equals(searchStatus)) {
            SysCategory one = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "地址费用"));
            SysCategory two = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "人工提成"));
            if (one != null) {
                p.excludeAddressId = one.getId();
            }
            if (two != null) {
                p.excludeSalaryId = two.getId();
            }
        } else if ("2".equals(searchStatus)) {
            SysCategory two = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "人工提成"));
            if (two != null) {
                p.onlyCategoryId = two.getId();
            }
        } else if ("3".equals(searchStatus)) {
            SysCategory one = sysCategoryService.getOne(new QueryWrapper<SysCategory>().eq("name", "地址费用"));
            if (one != null) {
                p.onlyCategoryId = one.getId();
            }
        }
        return p;
    }

    private Map<String, Object> buildEmptyAnalyticsPayload(String searchStatus) {
        Map<String, Object> summary = new HashMap<>(8);
        summary.put("totalAmount", 0);
        summary.put("totalCount", 0);
        summary.put("approvedAmount", 0);
        summary.put("pendingAmount", 0);
        summary.put("rejectedAmount", 0);
        Map<String, Object> result = new HashMap<>(8);
        result.put("summary", summary);
        result.put("byCategory", Collections.emptyList());
        result.put("byMonth", Collections.emptyList());
        result.put("byPerson", Collections.emptyList());
        result.put("byCategoryMonth", Collections.emptyList());
        result.put("chartYear", Calendar.getInstance().get(Calendar.YEAR));
        result.put("dateBegin", null);
        result.put("dateEnd", null);
        result.put("searchStatus", searchStatus);
        return result;
    }

    private static class ReimbursementPersonScope {
        final boolean unrestricted;
        final List<String> allowedRealnames;

        ReimbursementPersonScope(boolean unrestricted, List<String> allowedRealnames) {
            this.unrestricted = unrestricted;
            this.allowedRealnames = allowedRealnames;
        }
    }

    private static class AnalyticsCategoryParams {
        String onlyCategoryId;
        String excludeAddressId;
        String excludeSalaryId;
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
        LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = new LambdaQueryWrapper<>();
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
    
    /**
     * 只获取下级角色ID（不包括当前角色）
     * @param roleId 角色ID
     * @return 下级角色ID列表（不包含当前角色）
     */
    private List<String> getSubRoleIdsOnly(String roleId) {
        List<String> result = new ArrayList<>();
        if (oConvertUtils.isEmpty(roleId) || sysRoleService == null) {
            return result;
        }
        
        // 查询直接下级角色（parent_id = roleId 且 role_type = 2）
        LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = new LambdaQueryWrapper<>();
        query.eq(org.jeecg.modules.system.entity.SysRole::getParentId, roleId);
        query.eq(org.jeecg.modules.system.entity.SysRole::getRoleType, 2); // 只查询数据角色
        query.eq(org.jeecg.modules.system.entity.SysRole::getStatus, 1); // 只查询启用的角色
        List<org.jeecg.modules.system.entity.SysRole> children = sysRoleService.list(query);
        
        // 递归查询每个子角色及其下级角色
        if (children != null && !children.isEmpty()) {
            for (org.jeecg.modules.system.entity.SysRole child : children) {
                // 添加子角色ID
                result.add(child.getId());
                // 递归查询子角色的下级角色
                List<String> subRoleIds = getSubRoleIdsOnly(child.getId());
                result.addAll(subRoleIds);
            }
        }
        
        return result;
    }
    
    /**
     * 根据角色ID列表查询用户ID列表
     * @param roleIds 角色ID列表
     * @return 用户ID列表
     */
    private List<String> getUserIdsByRoleIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty() || sysUserRoleService == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<SysUserRole> query = new LambdaQueryWrapper<>();
        query.in(SysUserRole::getRoleId, roleIds);
        List<SysUserRole> userRoles = sysUserRoleService.list(query);
        
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取用户ID并去重
        return userRoles.stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
}

