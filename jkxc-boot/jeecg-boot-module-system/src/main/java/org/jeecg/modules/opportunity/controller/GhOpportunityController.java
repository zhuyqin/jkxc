package org.jeecg.modules.opportunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.opportunity.entity.GhOpportunity;
import org.jeecg.modules.opportunity.entity.GhOpportunityAdCost;
import org.jeecg.modules.opportunity.entity.GhOpportunityBonusConfig;
import org.jeecg.modules.opportunity.service.IGhOpportunityAdCostService;
import org.jeecg.modules.opportunity.service.IGhOpportunityService;
import org.jeecg.modules.opportunity.service.IGhOpportunityBonusConfigService;
import org.jeecg.modules.customer.entity.GhCustomer;
import org.jeecg.modules.customer.service.IGhCustomerService;
import org.jeecg.modules.followup.entity.GhFollowupDetail;
import org.jeecg.modules.followup.service.IGhFollowupDetailService;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.system.entity.SysCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.jeecg.common.util.oConvertUtils;

/**
 * @Description: 商机管理
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Api(tags = "商机管理")
@RestController
@RequestMapping("/opportunity/ghOpportunity")
@Slf4j
public class GhOpportunityController extends JeecgController<GhOpportunity, IGhOpportunityService> {
    
    @Autowired
    private IGhOpportunityService opportunityService;

    @Autowired(required = false)
    private IGhOpportunityBonusConfigService opportunityBonusConfigService;
    
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    
    @Autowired
    private IGhCustomerService customerService;
    
    @Autowired(required = false)
    private IGhFollowupDetailService followupDetailService;

    @Autowired(required = false)
    private IGhOrderService orderService;

    @Autowired(required = false)
    private IGhOpportunityAdCostService opportunityAdCostService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysUserService sysUserService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysCategoryService sysCategoryService;
    
    @Autowired(required = false)
    private org.jeecg.modules.notification.service.IWechatNotificationService wechatNotificationService;
    
    @Autowired(required = false)
    private org.jeecg.modules.system.service.ISysTeamService sysTeamService;
    
    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    /**
     * 左侧「跟进时间」分档，与 {@link #countFollowupTime}、列表「距离天数」一致：<br>
     * 取 <b>GREATEST(最近跟进时间, modi_time, create_time)</b> 作为「最后活跃」——跟进会写跟进表并往往同步改 modi_time；<br>
     * 仅编辑也会更新 modi_time，可把分档拉回「3天内」等，无需再录一条跟进。<br>
     * 距今天数分档与 Java 列表/统计一致：按「now − lastTouch」的<strong>秒差</strong>区间划分（与 TimeUnit.DAYS 按 86400s 一致），<br>
     * 不用 DATEDIFF(日历)、不用 GREATEST(0,…DIV…)（易被 Druid Wall 判为恒真拦截）。<br>
     * key1：秒差 ∈ [0, 4×86400)；key2：[4×86400, 8×86400)；key3：[8×86400, 16×86400)；key4：≥ 16×86400。
     */
    private void appendFollowupTimeKeyFilter(QueryWrapper<GhOpportunity> queryWrapper, String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        final String lastTouch = "GREATEST("
                + "IFNULL((SELECT MAX(f.create_date) FROM gh_followup_detail f WHERE f.opport_id = gh_opportunity.id AND f.del_flag = 0), '1970-01-01 00:00:00'), "
                + "IFNULL(gh_opportunity.modi_time, '1970-01-01 00:00:00'), "
                + "IFNULL(gh_opportunity.create_time, '1970-01-01 00:00:00'))";
        final String diffSec = "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(" + lastTouch + "))";
        if ("1".equals(key)) {
            queryWrapper.apply(diffSec + " >= 0 AND " + diffSec + " < " + (4 * 86400));
        } else if ("2".equals(key)) {
            queryWrapper.apply(diffSec + " >= " + (4 * 86400) + " AND " + diffSec + " < " + (8 * 86400));
        } else if ("3".equals(key)) {
            queryWrapper.apply(diffSec + " >= " + (8 * 86400) + " AND " + diffSec + " < " + (16 * 86400));
        } else if ("4".equals(key)) {
            queryWrapper.apply(diffSec + " >= " + (16 * 86400));
        }
    }

    /**
     * 个人线索：列表与 {@link #countFollowupTime} 共用。<br>
     * 不用 (IS NULL OR …) / 单段内 OR，避免 Druid Wall 将片段误判为「恒真」而拦截。
     */
    private void appendPersonalTabFilters(QueryWrapper<GhOpportunity> queryWrapper, HttpServletRequest req) {
        queryWrapper.apply("COALESCE(state,'') NOT IN ({0},{1},{2})", "invalid", "1", "intention_a_plus");
        queryWrapper.apply("COALESCE(owner_type,'') IN ({0},{1})", "", "personal");
        String token = req.getHeader(CommonConstant.X_ACCESS_TOKEN);
        if (StringUtils.isNotEmpty(token)) {
            String username = JwtUtil.getUsername(token);
            List<String> roles = sysBaseAPI.getRolesByUsername(username);
            if (roles == null || !roles.contains("admin")) {
                LoginUser sysUser = sysBaseAPI.getUserByName(username);
                if (sysUser != null && StringUtils.isNotEmpty(StringUtils.trimToEmpty(sysUser.getRealname()))) {
                    String rn = StringUtils.trim(sysUser.getRealname());
                    queryWrapper.and(w -> w
                            .apply("TRIM(IFNULL(founder,'')) = {0}", rn)
                            .or()
                            .apply("TRIM(IFNULL(charge_person,'')) = {0}", rn));
                }
            }
        }
    }

    /**
     * 分页列表查询（改造为线索管理：公海线索、个人线索、无效线索）
     * 公海客户逻辑：根据状态筛选（不在intention_a_plus和invalid中），且跟进时间距离创建时间>30天
     * 个人客户逻辑：包含之前的个人客户和签约客户（状态不为invalid），如果不是admin则根据founder筛选
     */
    @AutoLog(value = "商机-分页列表查询")
    @ApiOperation(value = "商机-分页列表查询", notes = "商机-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhOpportunity opportunity, String ownerType, String key,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        // 如果是签约线索tab，需要先清除opportunity对象中的state和ownerType属性，避免QueryGenerator自动添加冲突的条件
        String originalState = null;
        String originalOwnerType = null;
        if ("signed".equals(ownerType)) {
            if (opportunity != null) {
                if (StringUtils.isNotEmpty(opportunity.getState())) {
                    originalState = opportunity.getState();
                    opportunity.setState(null); // 临时清除，由后续逻辑统一处理
                }
                if (StringUtils.isNotEmpty(opportunity.getOwnerType())) {
                    originalOwnerType = opportunity.getOwnerType();
                    opportunity.setOwnerType(null); // 临时清除ownerType，因为签约线索不应该限制owner_type
                }
            }
        }
        
        // 列表查询不按奖金发放标记过滤，避免影响线索可见性
        if (opportunity != null) {
            opportunity.setClueBonusAwarded(null);
        }
        // 签约/个人 Tab：从参数 Map 移除 ownerType，避免 QueryGenerator 生成 owner_type=? 与后续手写 OR 条件叠加，
        // 变成 (owner_type=?) AND (owner_type IS NULL OR ... OR ?) 被 Druid 判为恒真（sql injection violation）。
        Map<String, String[]> paramMap = req.getParameterMap();
        if (paramMap != null && (paramMap.containsKey("clueBonusAwarded") || paramMap.containsKey("clue_bonus_awarded"))) {
            paramMap = new HashMap<>(paramMap);
            paramMap.remove("clueBonusAwarded");
            paramMap.remove("clue_bonus_awarded");
        }
        if ("signed".equals(ownerType) || "personal".equals(ownerType)) {
            paramMap = new HashMap<>(req.getParameterMap());
            paramMap.remove("ownerType");
            paramMap.remove("clueBonusAwarded");
            paramMap.remove("clue_bonus_awarded");
            if (opportunity != null) {
                opportunity.setOwnerType(null);
            }
        }
        
        // 在QueryGenerator之前检查是否有customerId参数（优先处理customerId查询）
        boolean hasCustomerId = false;
        String customerIdValue = null;
        
        // 优先从opportunity对象中获取
        if (opportunity != null && StringUtils.isNotEmpty(opportunity.getCustomerId())) {
            customerIdValue = opportunity.getCustomerId();
            hasCustomerId = true;
            log.info("从opportunity对象中获取到customerId: {}", customerIdValue);
        } 
        // 如果opportunity对象中没有，从paramMap中获取（在QueryGenerator处理之前）
        else if (paramMap != null) {
            // 检查customerId（驼峰命名）
            if (paramMap.containsKey("customerId")) {
                String[] customerIdValues = paramMap.get("customerId");
                if (customerIdValues != null && customerIdValues.length > 0 && StringUtils.isNotEmpty(customerIdValues[0])) {
                    customerIdValue = customerIdValues[0];
                    hasCustomerId = true;
                    log.info("从paramMap中获取到customerId: {}", customerIdValue);
                }
            }
            // 检查customer_id（下划线命名，兼容前端可能传递的格式）
            if (!hasCustomerId && paramMap.containsKey("customer_id")) {
                String[] customerIdValues = paramMap.get("customer_id");
                if (customerIdValues != null && customerIdValues.length > 0 && StringUtils.isNotEmpty(customerIdValues[0])) {
                    customerIdValue = customerIdValues[0];
                    hasCustomerId = true;
                    log.info("从paramMap中获取到customer_id: {}", customerIdValue);
                }
            }
        }
        
        QueryWrapper<GhOpportunity> queryWrapper = QueryGenerator.initQueryWrapper(opportunity, paramMap);
        queryWrapper.eq("del_flag", 0);
        
        // 如果找到了customerId，显式添加查询条件（确保能正确查询，即使QueryGenerator已经处理过）
        if (hasCustomerId && StringUtils.isNotEmpty(customerIdValue)) {
            queryWrapper.eq("customer_id", customerIdValue);
            log.info("添加customerId查询条件: customer_id = {}", customerIdValue);
        } else {
            log.info("未找到customerId参数，hasCustomerId={}, customerIdValue={}", hasCustomerId, customerIdValue);
        }
        
        // 打印查询条件用于调试
        log.debug("查询条件: {}", queryWrapper.getTargetSql());
        
        // 按归属类型筛选（四个tab页：公海线索、个人线索、签约线索、无效线索）
        // 如果有customerId，跳过ownerType的默认逻辑，直接返回
        if (hasCustomerId && StringUtils.isEmpty(ownerType)) {
            // 有customerId且没有指定ownerType，直接查询，不应用默认公海逻辑
        } else if (StringUtils.isNotEmpty(ownerType)) {
            if ("public".equals(ownerType)) {
                // 公海线索：优先使用ownerType字段，如果ownerType为"public"则直接显示
                // 如果没有设置ownerType，则使用时间条件（跟进时间距离创建时间>30天）作为默认逻辑
                queryWrapper.and(wrapper -> {
                    // 情况1：ownerType为"public"的直接显示（无论时间条件）
                    wrapper.eq("owner_type", "public")
                            .or(innerWrapper -> {
                                // 情况2：ownerType为空或null，且满足时间条件
                                innerWrapper.and(subWrapper -> {
                                    subWrapper.isNull("owner_type")
                                            .or()
                                            .eq("owner_type", "");
                                })
                                .notIn("state", Arrays.asList("1", "intention_a_plus", "invalid"))
                                .apply("DATEDIFF(NOW(), COALESCE(modi_time, create_time)) > 30");
                            });
                });
            } else if ("personal".equals(ownerType)) {
                appendPersonalTabFilters(queryWrapper, req);
            } else if ("signed".equals(ownerType)) {
                // 签约线索：状态为已签约（根据code：1 或 intention_a_plus）
                // 使用in方法查询state，覆盖QueryGenerator可能添加的state条件
                queryWrapper.in("state", Arrays.asList("1", "intention_a_plus"));
            } else if ("invalid".equals(ownerType)) {
                // 无效线索：状态为无效客户
                queryWrapper.eq("state", "invalid");
            }
        } else if (!hasCustomerId) {
            // 如果既没有指定状态，也没有指定ownerType，且没有customerId，则使用默认的公海客户逻辑
            queryWrapper.notIn("state", Arrays.asList("1", "intention_a_plus", "invalid"))
                    .apply("DATEDIFF(NOW(), COALESCE(modi_time, create_time)) > 30");
        }
        // 如果有customerId，不应用默认的公海逻辑，直接使用customerId查询
        
        // 跟进时间分档：与 countFollowupTime 统计口径一致（见 appendFollowupTimeKeyFilter）
        appendFollowupTimeKeyFilter(queryWrapper, key);
        
        queryWrapper.orderByDesc("create_time");
        
        Page<GhOpportunity> page = new Page<>(pageNo, pageSize);
        IPage<GhOpportunity> pageList = opportunityService.page(page, queryWrapper);
        
        // syDay、followupDays：与左侧分档一致 — max(最近跟进, modi_time, create_time)
        Date now = new Date();
        Map<String, Date> latestFollowupMap = buildLatestFollowupDateMap(pageList.getRecords());
        for (GhOpportunity record : pageList.getRecords()) {
            long anchorMs = Long.MIN_VALUE;
            Date latestFollowupDate = latestFollowupMap.get(record.getId());
            if (latestFollowupDate != null) {
                anchorMs = Math.max(anchorMs, latestFollowupDate.getTime());
            }
            if (record.getModiTime() != null) {
                anchorMs = Math.max(anchorMs, record.getModiTime().getTime());
            }
            if (record.getCreateTime() != null) {
                anchorMs = Math.max(anchorMs, record.getCreateTime().getTime());
            }
            if (anchorMs > Long.MIN_VALUE) {
                long diffInMillies = Math.abs(now.getTime() - anchorMs);
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                record.setSyDay(diff);
                record.setFollowupDays(diff);
            } else {
                record.setSyDay(0L);
                record.setFollowupDays(null);
            }
        }

        // 线索名称：库中多为 sys_category.id，补充中文 name，避免列表仅显示一长串数字
        fillOpportunityNameCategoryText(pageList.getRecords());

        // 线索奖金：如果未发放（clueBonus 为空），则从配置表按 opportunityName 回填金额用于展示
        fillClueBonusFromConfig(pageList.getRecords());
        
        return Result.OK(pageList);
    }

    private Map<String, Date> buildLatestFollowupDateMap(List<GhOpportunity> records) {
        Map<String, Date> latestMap = new HashMap<>();
        if (records == null || records.isEmpty() || followupDetailService == null) {
            return latestMap;
        }
        Set<String> opportIds = new HashSet<>();
        for (GhOpportunity record : records) {
            if (record != null && StringUtils.isNotBlank(record.getId())) {
                opportIds.add(record.getId());
            }
        }
        if (opportIds.isEmpty()) {
            return latestMap;
        }
        try {
            QueryWrapper<GhFollowupDetail> wrapper = new QueryWrapper<>();
            wrapper.select("opport_id", "MAX(create_date) AS latest_create_date");
            wrapper.eq("del_flag", 0);
            wrapper.in("opport_id", opportIds);
            wrapper.groupBy("opport_id");
            List<Map<String, Object>> rows = followupDetailService.listMaps(wrapper);
            if (rows == null || rows.isEmpty()) {
                return latestMap;
            }
            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }
                Object opportIdObj = row.get("opport_id");
                if (opportIdObj == null) {
                    continue;
                }
                String opportId = String.valueOf(opportIdObj);
                Date latest = toDate(row.get("latest_create_date"));
                if (latest != null) {
                    latestMap.put(opportId, latest);
                }
            }
        } catch (Exception e) {
            log.debug("批量查询最近跟进时间失败", e);
        }
        return latestMap;
    }

    private Date toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 线索数据分析：按年/月、客户来源、线索名称、线索领取人员统计
     */
    @AutoLog(value = "商机-数据分析")
    @ApiOperation(value = "商机-数据分析", notes = "按创建时间统计线索数量与奖金")
    @GetMapping(value = "/analytics")
    public Result<?> analytics(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) String dateBegin,
                               @RequestParam(required = false) String dateEnd) {
        try {
            Date begin;
            Date end;
            if (StringUtils.isNotBlank(dateBegin) && StringUtils.isNotBlank(dateEnd)) {
                java.text.SimpleDateFormat day = new java.text.SimpleDateFormat("yyyy-MM-dd");
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

            QueryWrapper<GhOpportunity> qw = new QueryWrapper<>();
            qw.eq("del_flag", 0);
            qw.ge("create_time", begin);
            qw.le("create_time", end);
            qw.orderByAsc("create_time");
            List<GhOpportunity> records = opportunityService.list(qw);
            if (records == null) {
                records = Collections.emptyList();
            }
            fillClueBonusFromConfig(records);

            Map<String, String> sourceDictMap = new HashMap<>();
            try {
                List<org.jeecg.common.system.vo.DictModel> sourceItems = sysBaseAPI != null ? sysBaseAPI.getDictItems("opportunity_source") : null;
                if (sourceItems != null) {
                    for (org.jeecg.common.system.vo.DictModel item : sourceItems) {
                        if (item.getValue() != null) {
                            sourceDictMap.put(item.getValue(), item.getText());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("加载客户来源字典失败", e);
            }

            Set<String> categoryIds = new HashSet<>();
            for (GhOpportunity r : records) {
                if (r == null || StringUtils.isBlank(r.getOpportunityName())) {
                    continue;
                }
                String n = r.getOpportunityName().trim();
                if (n.matches("^\\d{5,}$") || n.matches("^[a-zA-Z0-9_-]{16,}$")) {
                    categoryIds.add(n);
                }
            }
            Map<String, String> categoryNameMap = new HashMap<>();
            if (!categoryIds.isEmpty() && sysCategoryService != null) {
                Collection<SysCategory> cats = sysCategoryService.listByIds(categoryIds);
                if (cats != null) {
                    for (SysCategory c : cats) {
                        if (c != null && StringUtils.isNotBlank(c.getId())) {
                            categoryNameMap.put(c.getId(), c.getName());
                        }
                    }
                }
            }

            BigDecimal totalBonus = BigDecimal.ZERO;
            Map<String, Map<String, Object>> byMonthMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> bySourceMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> byOppNameMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> byReceiverMap = new LinkedHashMap<>();
            Map<String, Map<Integer, int[]>> sourceMonthCountMap = new LinkedHashMap<>();
            Map<String, Map<Integer, BigDecimal>> sourceMonthAmountMap = new LinkedHashMap<>();
            Map<String, Map<Integer, int[]>> oppNameMonthCountMap = new LinkedHashMap<>();
            Map<String, Map<Integer, BigDecimal>> oppNameMonthAmountMap = new LinkedHashMap<>();
            Map<String, Map<Integer, int[]>> receiverMonthCountMap = new LinkedHashMap<>();
            Map<String, Map<Integer, BigDecimal>> receiverMonthAmountMap = new LinkedHashMap<>();
            Set<String> receiverDistinct = new HashSet<>();
            Set<String> sourceDistinct = new HashSet<>();
            Set<String> oppNameDistinct = new HashSet<>();

            for (GhOpportunity r : records) {
                if (r == null) {
                    continue;
                }
                BigDecimal bonus = r.getClueBonus() != null ? r.getClueBonus() : BigDecimal.ZERO;
                totalBonus = totalBonus.add(bonus);

                String monthKey = r.getCreateTime() == null
                        ? "未知"
                        : new java.text.SimpleDateFormat("yyyy-MM").format(r.getCreateTime());
                Map<String, Object> monthRow = byMonthMap.computeIfAbsent(monthKey, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("monthKey", k);
                    m.put("cnt", 0);
                    m.put("bonusAmount", BigDecimal.ZERO);
                    return m;
                });
                monthRow.put("cnt", ((Integer) monthRow.get("cnt")) + 1);
                monthRow.put("bonusAmount", ((BigDecimal) monthRow.get("bonusAmount")).add(bonus));

                String sourceCode = StringUtils.defaultIfBlank(r.getOpportunitySource(), "");
                String sourceName = sourceDictMap.getOrDefault(sourceCode, StringUtils.defaultIfBlank(sourceCode, "未填写"));
                Map<String, Object> sourceRow = bySourceMap.computeIfAbsent(sourceName, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("sourceName", k);
                    m.put("cnt", 0);
                    m.put("bonusAmount", BigDecimal.ZERO);
                    return m;
                });
                sourceRow.put("cnt", ((Integer) sourceRow.get("cnt")) + 1);
                sourceRow.put("bonusAmount", ((BigDecimal) sourceRow.get("bonusAmount")).add(bonus));
                sourceDistinct.add(sourceName);

                String oppRaw = StringUtils.defaultIfBlank(r.getOpportunityName(), "");
                String oppDisplay = categoryNameMap.getOrDefault(oppRaw.trim(), StringUtils.defaultIfBlank(oppRaw, "未填写"));
                Map<String, Object> oppRow = byOppNameMap.computeIfAbsent(oppDisplay, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("opportunityName", k);
                    m.put("cnt", 0);
                    m.put("bonusAmount", BigDecimal.ZERO);
                    return m;
                });
                oppRow.put("cnt", ((Integer) oppRow.get("cnt")) + 1);
                oppRow.put("bonusAmount", ((BigDecimal) oppRow.get("bonusAmount")).add(bonus));
                oppNameDistinct.add(oppDisplay);

                String receiver = StringUtils.isNotBlank(r.getChargePerson())
                        ? r.getChargePerson().trim()
                        : StringUtils.defaultIfBlank(r.getFounder(), "未分配");
                Map<String, Object> receiverRow = byReceiverMap.computeIfAbsent(receiver, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("receiverName", k);
                    m.put("cnt", 0);
                    m.put("bonusAmount", BigDecimal.ZERO);
                    return m;
                });
                receiverRow.put("cnt", ((Integer) receiverRow.get("cnt")) + 1);
                receiverRow.put("bonusAmount", ((BigDecimal) receiverRow.get("bonusAmount")).add(bonus));
                receiverDistinct.add(receiver);

                if (r.getCreateTime() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(r.getCreateTime());
                    int monthNum = cal.get(Calendar.MONTH) + 1;

                    Map<Integer, int[]> sourceCntMap = sourceMonthCountMap.computeIfAbsent(sourceName, k -> new HashMap<>());
                    int[] sourceCntArr = sourceCntMap.computeIfAbsent(monthNum, k -> new int[]{0});
                    sourceCntArr[0] = sourceCntArr[0] + 1;
                    Map<Integer, BigDecimal> sourceAmtMap = sourceMonthAmountMap.computeIfAbsent(sourceName, k -> new HashMap<>());
                    BigDecimal sourceCurrent = sourceAmtMap.getOrDefault(monthNum, BigDecimal.ZERO);
                    sourceAmtMap.put(monthNum, sourceCurrent.add(bonus));

                    Map<Integer, int[]> oppCntMap = oppNameMonthCountMap.computeIfAbsent(oppDisplay, k -> new HashMap<>());
                    int[] oppCntArr = oppCntMap.computeIfAbsent(monthNum, k -> new int[]{0});
                    oppCntArr[0] = oppCntArr[0] + 1;
                    Map<Integer, BigDecimal> oppAmtMap = oppNameMonthAmountMap.computeIfAbsent(oppDisplay, k -> new HashMap<>());
                    BigDecimal oppCurrent = oppAmtMap.getOrDefault(monthNum, BigDecimal.ZERO);
                    oppAmtMap.put(monthNum, oppCurrent.add(bonus));

                    Map<Integer, int[]> monthCntMap = receiverMonthCountMap.computeIfAbsent(receiver, k -> new HashMap<>());
                    int[] cntArr = monthCntMap.computeIfAbsent(monthNum, k -> new int[]{0});
                    cntArr[0] = cntArr[0] + 1;

                    Map<Integer, BigDecimal> monthAmtMap = receiverMonthAmountMap.computeIfAbsent(receiver, k -> new HashMap<>());
                    BigDecimal current = monthAmtMap.getOrDefault(monthNum, BigDecimal.ZERO);
                    monthAmtMap.put(monthNum, current.add(bonus));
                }
            }

            Comparator<Map<String, Object>> byCntDesc = Comparator.comparingInt((Map<String, Object> m) -> (Integer) m.get("cnt")).reversed();
            Comparator<Map<String, Object>> byBonusDesc = Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("bonusAmount")).reversed();

            List<Map<String, Object>> byMonth = new ArrayList<>(byMonthMap.values());
            byMonth.sort(Comparator.comparing(m -> String.valueOf(m.get("monthKey"))));
            List<Map<String, Object>> bySource = new ArrayList<>(bySourceMap.values());
            bySource.sort(byCntDesc.thenComparing(byBonusDesc));
            List<Map<String, Object>> byOpportunityName = new ArrayList<>(byOppNameMap.values());
            byOpportunityName.sort(byCntDesc.thenComparing(byBonusDesc));
            List<Map<String, Object>> byReceiver = new ArrayList<>(byReceiverMap.values());
            byReceiver.sort(byCntDesc.thenComparing(byBonusDesc));
            List<Map<String, Object>> bySourceMonth = new ArrayList<>();
            for (Map.Entry<String, Map<Integer, int[]>> e : sourceMonthCountMap.entrySet()) {
                String sourceName = e.getKey();
                Map<String, Object> row = new HashMap<>();
                row.put("sourceName", sourceName);
                int totalCnt = 0;
                BigDecimal totalAmt = BigDecimal.ZERO;
                for (int m = 1; m <= 12; m++) {
                    int cnt = 0;
                    BigDecimal amt = BigDecimal.ZERO;
                    int[] arr = e.getValue().get(m);
                    if (arr != null) {
                        cnt = arr[0];
                    }
                    Map<Integer, BigDecimal> amtMap = sourceMonthAmountMap.get(sourceName);
                    if (amtMap != null && amtMap.get(m) != null) {
                        amt = amtMap.get(m);
                    }
                    row.put("month" + m + "_count", cnt);
                    row.put("month" + m + "_amount", amt);
                    totalCnt += cnt;
                    totalAmt = totalAmt.add(amt);
                }
                row.put("totalCount", totalCnt);
                row.put("totalAmount", totalAmt);
                bySourceMonth.add(row);
            }
            bySourceMonth.sort((a, b) -> Integer.compare((Integer) b.get("totalCount"), (Integer) a.get("totalCount")));

            List<Map<String, Object>> byOpportunityNameMonth = new ArrayList<>();
            for (Map.Entry<String, Map<Integer, int[]>> e : oppNameMonthCountMap.entrySet()) {
                String oppName = e.getKey();
                Map<String, Object> row = new HashMap<>();
                row.put("opportunityName", oppName);
                int totalCnt = 0;
                BigDecimal totalAmt = BigDecimal.ZERO;
                for (int m = 1; m <= 12; m++) {
                    int cnt = 0;
                    BigDecimal amt = BigDecimal.ZERO;
                    int[] arr = e.getValue().get(m);
                    if (arr != null) {
                        cnt = arr[0];
                    }
                    Map<Integer, BigDecimal> amtMap = oppNameMonthAmountMap.get(oppName);
                    if (amtMap != null && amtMap.get(m) != null) {
                        amt = amtMap.get(m);
                    }
                    row.put("month" + m + "_count", cnt);
                    row.put("month" + m + "_amount", amt);
                    totalCnt += cnt;
                    totalAmt = totalAmt.add(amt);
                }
                row.put("totalCount", totalCnt);
                row.put("totalAmount", totalAmt);
                byOpportunityNameMonth.add(row);
            }
            byOpportunityNameMonth.sort((a, b) -> Integer.compare((Integer) b.get("totalCount"), (Integer) a.get("totalCount")));

            List<Map<String, Object>> byReceiverMonth = new ArrayList<>();
            for (Map.Entry<String, Map<Integer, int[]>> e : receiverMonthCountMap.entrySet()) {
                String receiverName = e.getKey();
                Map<String, Object> row = new HashMap<>();
                row.put("receiverName", receiverName);
                int totalCnt = 0;
                BigDecimal totalAmt = BigDecimal.ZERO;
                for (int m = 1; m <= 12; m++) {
                    int cnt = 0;
                    BigDecimal amt = BigDecimal.ZERO;
                    int[] arr = e.getValue().get(m);
                    if (arr != null) {
                        cnt = arr[0];
                    }
                    Map<Integer, BigDecimal> amtMap = receiverMonthAmountMap.get(receiverName);
                    if (amtMap != null && amtMap.get(m) != null) {
                        amt = amtMap.get(m);
                    }
                    row.put("month" + m + "_count", cnt);
                    row.put("month" + m + "_amount", amt);
                    totalCnt += cnt;
                    totalAmt = totalAmt.add(amt);
                }
                row.put("totalCount", totalCnt);
                row.put("totalAmount", totalAmt);
                byReceiverMonth.add(row);
            }
            byReceiverMonth.sort((a, b) -> Integer.compare((Integer) b.get("totalCount"), (Integer) a.get("totalCount")));

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalCount", records.size());
            summary.put("totalBonus", totalBonus);
            summary.put("receiverCount", receiverDistinct.size());
            summary.put("sourceCount", sourceDistinct.size());
            summary.put("opportunityNameCount", oppNameDistinct.size());

            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            result.put("byMonth", byMonth);
            result.put("bySource", bySource);
            result.put("byOpportunityName", byOpportunityName);
            result.put("byReceiver", byReceiver);
            result.put("bySourceMonth", bySourceMonth);
            result.put("byOpportunityNameMonth", byOpportunityNameMonth);
            result.put("byReceiverMonth", byReceiverMonth);
            result.put("dateBegin", begin);
            result.put("dateEnd", end);
            return Result.OK(result);
        } catch (Exception e) {
            log.error("线索数据分析失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 数据分析-明细：按维度（客户来源/线索名称/业务人员）+ 月份查询线索列表（按 create_time 口径）
     */
    @AutoLog(value = "商机-数据分析明细")
    @ApiOperation(value = "商机-数据分析明细", notes = "按维度+月份查询线索明细（分页）")
    @GetMapping(value = "/analyticsDetail")
    public Result<?> analyticsDetail(
            @RequestParam String dim,
            @RequestParam String dimValue,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String dateBegin,
            @RequestParam(required = false) String dateEnd,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize
    ) {
        try {
            if (StringUtils.isBlank(dim) || StringUtils.isBlank(dimValue)) {
                return Result.error("参数错误：dim/dimValue 不能为空");
            }

            // 先计算查询区间：优先 dateBegin/dateEnd；否则用 year + month
            Date begin;
            Date end;
            if (StringUtils.isNotBlank(dateBegin) && StringUtils.isNotBlank(dateEnd)) {
                java.text.SimpleDateFormat day = new java.text.SimpleDateFormat("yyyy-MM-dd");
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
                if (month != null && month >= 1 && month <= 12) {
                    cal.set(Calendar.MONTH, month - 1);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    begin = cal.getTime();
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.MILLISECOND, -1);
                    end = cal.getTime();
                } else {
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
            }

            QueryWrapper<GhOpportunity> qw = new QueryWrapper<>();
            qw.eq("del_flag", 0);
            qw.ge("create_time", begin);
            qw.le("create_time", end);
            // 自定义区间下点击某个月时，仍需要按月份过滤，确保与外层月度单元格口径一致
            if (month != null && month >= 1 && month <= 12) {
                qw.apply("MONTH(create_time) = {0}", month);
            }

            String dv = dimValue.trim();
            if ("source".equalsIgnoreCase(dim)) {
                // dimValue 是展示名：反查 code；若是“未填写”则匹配空
                if ("未填写".equals(dv)) {
                    qw.and(w -> w.isNull("opportunity_source").or().eq("opportunity_source", ""));
                } else {
                    List<org.jeecg.common.system.vo.DictModel> items = sysBaseAPI != null ? sysBaseAPI.getDictItems("opportunity_source") : null;
                    java.util.List<String> codes = new java.util.ArrayList<>();
                    if (items != null) {
                        for (org.jeecg.common.system.vo.DictModel it : items) {
                            if (it != null && StringUtils.equals(dv, it.getText())) {
                                codes.add(it.getValue());
                            }
                        }
                    }
                    if (codes.isEmpty()) {
                        // 兜底：按原值匹配（可能已直接存中文）
                        qw.eq("opportunity_source", dv);
                    } else {
                        qw.in("opportunity_source", codes);
                    }
                }
            } else if ("name".equalsIgnoreCase(dim)) {
                // dimValue 是分类 name 或原值：反查 sys_category.id；若“未填写”则匹配空
                if ("未填写".equals(dv)) {
                    qw.and(w -> w.isNull("opportunity_name").or().eq("opportunity_name", ""));
                } else if (sysCategoryService != null) {
                    QueryWrapper<SysCategory> cqw = new QueryWrapper<>();
                    cqw.eq("name", dv);
                    cqw.eq("del_flag", 0);
                    java.util.List<SysCategory> cats = sysCategoryService.list(cqw);
                    if (cats != null && !cats.isEmpty()) {
                        java.util.List<String> ids = new java.util.ArrayList<>();
                        for (SysCategory c : cats) {
                            if (c != null && StringUtils.isNotBlank(c.getId())) {
                                ids.add(c.getId());
                            }
                        }
                        if (!ids.isEmpty()) {
                            qw.in("opportunity_name", ids);
                        } else {
                            qw.eq("opportunity_name", dv);
                        }
                    } else {
                        qw.eq("opportunity_name", dv);
                    }
                } else {
                    qw.eq("opportunity_name", dv);
                }
            } else if ("receiver".equalsIgnoreCase(dim)) {
                // receiver 口径：charge_person 优先，否则 founder；“未分配”表示两者都空
                if ("未分配".equals(dv)) {
                    qw.and(w -> w
                            .and(w2 -> w2.isNull("charge_person").or().eq("charge_person", ""))
                            .and(w3 -> w3.isNull("founder").or().eq("founder", "")));
                } else {
                    qw.and(w -> w.eq("charge_person", dv).or()
                            .and(w2 -> w2.and(w3 -> w3.isNull("charge_person").or().eq("charge_person", "")).eq("founder", dv)));
                }
            } else {
                return Result.error("不支持的维度：" + dim);
            }

            qw.orderByDesc("create_time");
            Page<GhOpportunity> page = new Page<>(pageNo, pageSize);
            IPage<GhOpportunity> pageList = opportunityService.page(page, qw);

            // 补充展示字段
            fillOpportunityNameCategoryText(pageList.getRecords());
            fillClueBonusFromConfig(pageList.getRecords());
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("线索数据分析明细查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 投放分析：按线索类别统计广告费用、线索数量、线索成本、签约金额、投产比
     */
    @AutoLog(value = "商机-投放分析")
    @ApiOperation(value = "商机-投放分析", notes = "按线索类别统计广告费用与投产比")
    @GetMapping(value = "/analyticsAd")
    public Result<?> analyticsAd(@RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) String dateBegin,
                                 @RequestParam(required = false) String dateEnd) {
        try {
            Date begin;
            Date end;
            if (StringUtils.isNotBlank(dateBegin) && StringUtils.isNotBlank(dateEnd)) {
                java.text.SimpleDateFormat day = new java.text.SimpleDateFormat("yyyy-MM-dd");
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
                if (month != null && month >= 1 && month <= 12) {
                    cal.set(Calendar.MONTH, month - 1);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    begin = cal.getTime();
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.MILLISECOND, -1);
                    end = cal.getTime();
                } else {
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
            }

            QueryWrapper<GhOpportunity> qw = new QueryWrapper<>();
            qw.eq("del_flag", 0);
            qw.ge("create_time", begin);
            qw.le("create_time", end);
            List<GhOpportunity> records = opportunityService.list(qw);
            if (records == null) {
                records = new ArrayList<>();
            }

            // 客户来源字典映射：code -> text
            Map<String, String> sourceDictMap = new HashMap<>();
            try {
                List<org.jeecg.common.system.vo.DictModel> sourceItems = sysBaseAPI != null ? sysBaseAPI.getDictItems("opportunity_source") : null;
                if (sourceItems != null) {
                    for (org.jeecg.common.system.vo.DictModel item : sourceItems) {
                        if (item != null && StringUtils.isNotBlank(item.getValue())) {
                            sourceDictMap.put(item.getValue(), item.getText());
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            // 按线索来源聚合
            Map<String, Map<String, Object>> rowMap = new LinkedHashMap<>();
            Map<String, Set<String>> sourceCustomerIds = new HashMap<>();
            for (GhOpportunity r : records) {
                if (r == null) continue;
                String raw = StringUtils.defaultIfBlank(r.getOpportunitySource(), "").trim();
                String key = StringUtils.isBlank(raw) ? "__EMPTY__" : raw;
                String display = "未填写";
                if (!StringUtils.isBlank(raw)) {
                    display = sourceDictMap.getOrDefault(raw, raw);
                }
                final String displayFinal = display;
                Map<String, Object> row = rowMap.computeIfAbsent(key, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("sourceKey", key);
                    m.put("sourceName", displayFinal);
                    m.put("clueCount", 0);
                    m.put("signAmount", BigDecimal.ZERO);
                    m.put("clueCost", BigDecimal.ZERO);
                    return m;
                });
                row.put("clueCount", ((Integer) row.get("clueCount")) + 1);
                // 线索成本口径：已确认有效线索的线索奖金（按来源汇总）
                Integer confirmValid = r.getConfirmValid();
                if (confirmValid != null && confirmValid == 1 && r.getClueBonus() != null) {
                    row.put("clueCost", ((BigDecimal) row.get("clueCost")).add(r.getClueBonus()));
                }

                if (StringUtils.isNotBlank(r.getCustomerId())) {
                    sourceCustomerIds.computeIfAbsent(key, k -> new HashSet<>()).add(r.getCustomerId().trim());
                }
            }

            // 签约金额：按同区间内订单金额汇总（通过 customerId -> order.company_id 关联）
            if (!sourceCustomerIds.isEmpty() && orderService != null) {
                Set<String> allCustomerIds = new HashSet<>();
                for (Set<String> s : sourceCustomerIds.values()) allCustomerIds.addAll(s);
                if (!allCustomerIds.isEmpty()) {
                    QueryWrapper<GhOrder> orderQw = new QueryWrapper<>();
                    orderQw.eq("del_flag", 0);
                    orderQw.in("company_id", allCustomerIds);
                    orderQw.ge("create_time", begin);
                    orderQw.le("create_time", end);
                    List<GhOrder> orders = orderService.list(orderQw);
                    Map<String, BigDecimal> customerOrderAmount = new HashMap<>();
                    if (orders != null) {
                        for (GhOrder o : orders) {
                            if (o == null || StringUtils.isBlank(o.getCompanyId())) continue;
                            BigDecimal amt = o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO;
                            customerOrderAmount.merge(o.getCompanyId().trim(), amt, BigDecimal::add);
                        }
                    }
                    for (Map.Entry<String, Set<String>> e : sourceCustomerIds.entrySet()) {
                        BigDecimal total = BigDecimal.ZERO;
                        for (String cid : e.getValue()) {
                            total = total.add(customerOrderAmount.getOrDefault(cid, BigDecimal.ZERO));
                        }
                        Map<String, Object> row = rowMap.get(e.getKey());
                        if (row != null) row.put("signAmount", total);
                    }
                }
            }

            // 广告费用：按年/月+线索来源读取手工录入值（复用 gh_opportunity_ad_cost 表）
            int statYear = year != null ? year : Calendar.getInstance().get(Calendar.YEAR);
            int statMonth = (month != null && month >= 1 && month <= 12) ? month : 0;
            Map<String, BigDecimal> adCostMap = new HashMap<>();
            if (opportunityAdCostService != null) {
                QueryWrapper<GhOpportunityAdCost> cw = new QueryWrapper<>();
                cw.eq("del_flag", 0);
                cw.eq("stat_year", statYear);
                cw.eq("stat_month", statMonth);
                List<GhOpportunityAdCost> costs = opportunityAdCostService.list(cw);
                if (costs != null) {
                    for (GhOpportunityAdCost c : costs) {
                        if (c == null) continue;
                        String key = StringUtils.defaultIfBlank(c.getOpportunityNameKey(), "__EMPTY__");
                        adCostMap.put(key, c.getAdCost() == null ? BigDecimal.ZERO : c.getAdCost());
                    }
                }
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> e : rowMap.entrySet()) {
                String key = e.getKey();
                Map<String, Object> row = e.getValue();
                // 兼容前端旧字段名，避免其他页面复用时空值
                row.put("opportunityNameKey", row.get("sourceKey"));
                row.put("opportunityName", row.get("sourceName"));
                BigDecimal adCost = adCostMap.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal signAmount = (BigDecimal) row.get("signAmount");
                BigDecimal clueCost = (BigDecimal) row.get("clueCost");
                BigDecimal roi = adCost.compareTo(BigDecimal.ZERO) > 0
                        ? signAmount.divide(adCost, 4, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                row.put("adCost", adCost);
                row.put("clueCost", clueCost);
                row.put("roi", roi);
                rows.add(row);
            }
            rows.sort((a, b) -> Integer.compare((Integer) b.get("clueCount"), (Integer) a.get("clueCount")));

            Map<String, Object> result = new HashMap<>();
            result.put("rows", rows);
            result.put("year", statYear);
            result.put("month", statMonth);
            result.put("dateBegin", begin);
            result.put("dateEnd", end);
            // 仅年/月模式允许保存广告费；自定义区间仅展示不保存
            result.put("canSaveAdCost", StringUtils.isBlank(dateBegin) || StringUtils.isBlank(dateEnd));
            return Result.OK(result);
        } catch (Exception e) {
            log.error("投放分析失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存线索来源广告费用（按年/月）
     */
    @AutoLog(value = "商机-保存广告费用")
    @ApiOperation(value = "商机-保存广告费用", notes = "保存线索来源广告费用（按年/月）")
    @PostMapping(value = "/saveAdCost")
    public Result<?> saveAdCost(HttpServletRequest request, @RequestBody java.util.Map<String, Object> params) {
        try {
            if (opportunityAdCostService == null) {
                return Result.error("广告费用服务未启用");
            }
            Integer year = params.get("year") == null ? null : Integer.valueOf(String.valueOf(params.get("year")));
            Integer month = params.get("month") == null ? 0 : Integer.valueOf(String.valueOf(params.get("month")));
            String key = params.get("sourceKey") != null ? String.valueOf(params.get("sourceKey"))
                    : (params.get("opportunityNameKey") == null ? "__EMPTY__" : String.valueOf(params.get("opportunityNameKey")));
            String name = params.get("sourceName") != null ? String.valueOf(params.get("sourceName"))
                    : (params.get("opportunityName") == null ? "未填写" : String.valueOf(params.get("opportunityName")));
            BigDecimal adCost = params.get("adCost") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(params.get("adCost")));
            if (year == null) {
                return Result.error("参数错误：year不能为空");
            }
            if (month == null || month < 0 || month > 12) {
                return Result.error("参数错误：month取值应为0-12");
            }

            QueryWrapper<GhOpportunityAdCost> qw = new QueryWrapper<>();
            qw.eq("del_flag", 0);
            qw.eq("stat_year", year);
            qw.eq("stat_month", month);
            qw.eq("opportunity_name_key", key);
            GhOpportunityAdCost row = opportunityAdCostService.getOne(qw);
            Date now = new Date();
            String username = null;
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            if (StringUtils.isNotBlank(token)) {
                username = JwtUtil.getUsername(token);
            }
            if (row == null) {
                row = new GhOpportunityAdCost();
                row.setStatYear(year);
                row.setStatMonth(month);
                row.setOpportunityNameKey(key);
                row.setOpportunityName(name);
                row.setAdCost(adCost);
                row.setDelFlag(0);
                row.setCreateBy(username);
                row.setCreateTime(now);
                row.setUpdateBy(username);
                row.setUpdateTime(now);
                opportunityAdCostService.save(row);
            } else {
                row.setOpportunityName(name);
                row.setAdCost(adCost);
                row.setUpdateBy(username);
                row.setUpdateTime(now);
                opportunityAdCostService.updateById(row);
            }
            return Result.OK("保存成功");
        } catch (Exception e) {
            log.error("保存广告费用失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @AutoLog(value = "商机-修正2026年1-2月订单创建时间")
    @ApiOperation(value = "商机-修正2026年1-2月订单创建时间", notes = "将2026年1-2月中同一天超过20条的订单创建时间统一减1年")
    @PostMapping(value = "/fixCreateTime2026JanFebShiftPrevYear")
    public Result<?> fixCreateTime2026JanFebShiftPrevYear() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date begin = sdf.parse("2026-01-01");
            Date end = sdf.parse("2026-02-28");
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(end);
            c.set(java.util.Calendar.HOUR_OF_DAY, 23);
            c.set(java.util.Calendar.MINUTE, 59);
            c.set(java.util.Calendar.SECOND, 59);
            c.set(java.util.Calendar.MILLISECOND, 999);
            end = c.getTime();

            if (orderService == null) {
                return Result.error("订单服务未启用");
            }

            QueryWrapper<GhOrder> groupQw = new QueryWrapper<>();
            groupQw.select("DATE(create_time) AS day_key", "COUNT(1) AS cnt");
            groupQw.apply("IFNULL(del_flag,0)=0");
            groupQw.ge("create_time", begin);
            groupQw.le("create_time", end);
            groupQw.groupBy("DATE(create_time)");
            groupQw.having("COUNT(1) > {0}", 20);
            List<Map<String, Object>> grouped = orderService.listMaps(groupQw);
            if (grouped == null || grouped.isEmpty()) {
                return Result.OK("无需修正：gh_order 在2026年1-2月没有超过20条的日期");
            }

            List<String> days = new ArrayList<>();
            Map<String, Integer> dayCountMap = new LinkedHashMap<>();
            int updated = 0;
            for (Map<String, Object> row : grouped) {
                if (row == null) {
                    continue;
                }
                Object dayObj = row.get("day_key");
                if (dayObj == null) {
                    dayObj = row.get("DAY_KEY");
                }
                if (dayObj == null) {
                    dayObj = row.get("DATE(create_time)");
                }
                if (dayObj == null) {
                    continue;
                }
                String day = String.valueOf(dayObj);
                if (day.length() >= 10) {
                    day = day.substring(0, 10);
                }
                days.add(day);
                Object cntObj = row.get("cnt") != null ? row.get("cnt") : row.get("CNT");
                int cnt = cntObj == null ? 0 : Integer.parseInt(String.valueOf(cntObj));
                dayCountMap.put(day, cnt);
                Date dayStart = sdf.parse(day);
                java.util.Calendar dc = java.util.Calendar.getInstance();
                dc.setTime(dayStart);
                dc.set(java.util.Calendar.HOUR_OF_DAY, 23);
                dc.set(java.util.Calendar.MINUTE, 59);
                dc.set(java.util.Calendar.SECOND, 59);
                dc.set(java.util.Calendar.MILLISECOND, 999);
                Date dayEnd = dc.getTime();

                com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<GhOrder> uw = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
                uw.apply("IFNULL(del_flag,0)=0");
                uw.ge("create_time", dayStart);
                uw.le("create_time", dayEnd);
                uw.setSql("create_time = DATE_SUB(create_time, INTERVAL 1 YEAR)");
                orderService.update(null, uw);
                QueryWrapper<GhOrder> checkQw = new QueryWrapper<>();
                checkQw.apply("IFNULL(del_flag,0)=0");
                checkQw.ge("create_time", dayStart);
                checkQw.le("create_time", dayEnd);
                int remain = orderService.count(checkQw);
                updated += Math.max(cnt - remain, 0);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("days", days);
            result.put("dayCounts", dayCountMap);
            result.put("updated", updated);
            result.put("table", "gh_order");
            result.put("rule", "gh_order：2026年1-2月，同日>20条，create_time减1年");
            return Result.OK("修正完成（gh_order），共处理 " + updated + " 条", result);
        } catch (Exception e) {
            log.error("修正创建时间失败", e);
            return Result.error("修正失败：" + e.getMessage());
        }
    }

    /**
     * 将 opportunityName（分类 id）批量转为 opportunityName_dictText（分类中文 name）
     */
    private void fillOpportunityNameCategoryText(java.util.List<GhOpportunity> records) {
        if (records == null || records.isEmpty() || sysCategoryService == null) {
            return;
        }
        java.util.Set<String> catIds = new java.util.HashSet<>();
        for (GhOpportunity record : records) {
            if (record == null) {
                continue;
            }
            String v = record.getOpportunityName();
            if (StringUtils.isEmpty(v)) {
                continue;
            }
            String t = v.trim();
            if (isOpportunityNameStoredAsCategoryId(t)) {
                catIds.add(t);
            }
        }
        if (catIds.isEmpty()) {
            return;
        }
        java.util.Collection<SysCategory> cats = sysCategoryService.listByIds(catIds);
        if (cats == null || cats.isEmpty()) {
            return;
        }
        java.util.Map<String, String> idToName = new java.util.HashMap<>(cats.size());
        for (SysCategory c : cats) {
            if (c != null && StringUtils.isNotEmpty(c.getId())) {
                idToName.put(c.getId(), c.getName());
            }
        }
        for (GhOpportunity record : records) {
            if (record == null) {
                continue;
            }
            String v = record.getOpportunityName();
            if (StringUtils.isEmpty(v)) {
                continue;
            }
            String name = idToName.get(v.trim());
            if (StringUtils.isNotEmpty(name)) {
                record.setOpportunityName_dictText(name);
            }
        }
    }

    /**
     * 与前端线索列表一致：opportunityName 存 sys_category.id（Snowflake/字母数字），不含中文；否则视为已存中文或自由文本不参与 id 翻译
     */
    private static boolean isOpportunityNameStoredAsCategoryId(String t) {
        if (StringUtils.isEmpty(t)) {
            return false;
        }
        String s = t.trim();
        if (java.util.regex.Pattern.compile("[\\u4e00-\\u9fa5]").matcher(s).find()) {
            return false;
        }
        return java.util.regex.Pattern.matches("^[a-zA-Z0-9_-]+$", s);
    }

    /**
     * 将 gh_opportunity_bonus_config.bonus_money 回填到 gh_opportunity.clueBonus（仅用于列表展示，不落库）
     */
    private void fillClueBonusFromConfig(java.util.List<GhOpportunity> records) {
        if (records == null || records.isEmpty() || opportunityBonusConfigService == null) {
            return;
        }
        java.util.Set<String> oppNameKeys = new java.util.HashSet<>();
        for (GhOpportunity record : records) {
            if (record == null) {
                continue;
            }
            // 已有奖金（可能是发放后写入）则不覆盖
            if (record.getClueBonus() != null) {
                continue;
            }
            String k = record.getOpportunityName();
            if (StringUtils.isNotEmpty(k)) {
                oppNameKeys.add(k.trim());
            }
        }
        if (oppNameKeys.isEmpty()) {
            return;
        }

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GhOpportunityBonusConfig> cfgWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        cfgWrapper.eq("del_flag", 0);
        cfgWrapper.in("opportunity_name", oppNameKeys);
        java.util.List<GhOpportunityBonusConfig> cfgs = opportunityBonusConfigService.list(cfgWrapper);
        if (cfgs == null || cfgs.isEmpty()) {
            return;
        }
        java.util.Map<String, java.math.BigDecimal> keyToMoney = new java.util.HashMap<>(cfgs.size());
        for (GhOpportunityBonusConfig cfg : cfgs) {
            if (cfg == null || StringUtils.isEmpty(cfg.getOpportunityName())) {
                continue;
            }
            keyToMoney.put(cfg.getOpportunityName().trim(), cfg.getBonusMoney());
        }
        if (keyToMoney.isEmpty()) {
            return;
        }
        for (GhOpportunity record : records) {
            if (record == null || record.getClueBonus() != null) {
                continue;
            }
            String k = record.getOpportunityName();
            if (StringUtils.isEmpty(k)) {
                continue;
            }
            java.math.BigDecimal money = keyToMoney.get(k.trim());
            if (money != null) {
                record.setClueBonus(money);
            }
        }
    }

    /**
     * 统计跟进时间距离天数的数量（用于左侧菜单显示）
     * 统计全部数据，不分页
     */
    @AutoLog(value = "商机-统计跟进时间距离天数数量")
    @ApiOperation(value = "商机-统计跟进时间距离天数数量", notes = "商机-统计跟进时间距离天数数量")
    @GetMapping(value = "/countFollowupTime")
    public Result<?> countFollowupTime(GhOpportunity opportunity, String ownerType,
                                       HttpServletRequest req) {
        try {
            // 复用queryPageList的查询逻辑，但不分页
            String originalState = null;
            String originalOwnerType = null;
            if ("signed".equals(ownerType)) {
                if (opportunity != null) {
                    if (StringUtils.isNotEmpty(opportunity.getState())) {
                        originalState = opportunity.getState();
                        opportunity.setState(null);
                    }
                    if (StringUtils.isNotEmpty(opportunity.getOwnerType())) {
                        originalOwnerType = opportunity.getOwnerType();
                        opportunity.setOwnerType(null);
                    }
                }
            }
            
            if (opportunity != null) {
                opportunity.setClueBonusAwarded(null);
            }
            Map<String, String[]> paramMap = req.getParameterMap();
            if (paramMap != null && (paramMap.containsKey("clueBonusAwarded") || paramMap.containsKey("clue_bonus_awarded"))) {
                paramMap = new HashMap<>(paramMap);
                paramMap.remove("clueBonusAwarded");
                paramMap.remove("clue_bonus_awarded");
            }
            if ("signed".equals(ownerType) || "personal".equals(ownerType)) {
                paramMap = new HashMap<>(req.getParameterMap());
                paramMap.remove("ownerType");
                paramMap.remove("clueBonusAwarded");
                paramMap.remove("clue_bonus_awarded");
                if (opportunity != null) {
                    opportunity.setOwnerType(null);
                }
            }
            
            boolean hasCustomerId = false;
            String customerIdValue = null;
            
            if (opportunity != null && StringUtils.isNotEmpty(opportunity.getCustomerId())) {
                customerIdValue = opportunity.getCustomerId();
                hasCustomerId = true;
            } else if (paramMap != null) {
                if (paramMap.containsKey("customerId")) {
                    String[] customerIdValues = paramMap.get("customerId");
                    if (customerIdValues != null && customerIdValues.length > 0 && StringUtils.isNotEmpty(customerIdValues[0])) {
                        customerIdValue = customerIdValues[0];
                        hasCustomerId = true;
                    }
                }
                if (!hasCustomerId && paramMap.containsKey("customer_id")) {
                    String[] customerIdValues = paramMap.get("customer_id");
                    if (customerIdValues != null && customerIdValues.length > 0 && StringUtils.isNotEmpty(customerIdValues[0])) {
                        customerIdValue = customerIdValues[0];
                        hasCustomerId = true;
                    }
                }
            }
            
            QueryWrapper<GhOpportunity> queryWrapper = QueryGenerator.initQueryWrapper(opportunity, paramMap);
            queryWrapper.eq("del_flag", 0);
            
            if (hasCustomerId && StringUtils.isNotEmpty(customerIdValue)) {
                queryWrapper.eq("customer_id", customerIdValue);
            }
            
            // 按归属类型筛选
            if (hasCustomerId && StringUtils.isEmpty(ownerType)) {
                // 有customerId且没有指定ownerType，直接查询
            } else if (StringUtils.isNotEmpty(ownerType)) {
                if ("public".equals(ownerType)) {
                    queryWrapper.and(wrapper -> {
                        wrapper.eq("owner_type", "public")
                                .or(innerWrapper -> {
                                    innerWrapper.and(subWrapper -> {
                                        subWrapper.isNull("owner_type")
                                                .or()
                                                .eq("owner_type", "");
                                    })
                                    .notIn("state", Arrays.asList("1", "intention_a_plus", "invalid"))
                                    .apply("DATEDIFF(NOW(), COALESCE(modi_time, create_time)) > 30");
                                });
                    });
                } else if ("personal".equals(ownerType)) {
                    appendPersonalTabFilters(queryWrapper, req);
                } else if ("signed".equals(ownerType)) {
                    queryWrapper.in("state", Arrays.asList("1", "intention_a_plus"));
                } else if ("invalid".equals(ownerType)) {
                    queryWrapper.eq("state", "invalid");
                }
            } else if (!hasCustomerId) {
                queryWrapper.notIn("state", Arrays.asList("1", "intention_a_plus", "invalid"))
                        .apply("DATEDIFF(NOW(), COALESCE(modi_time, create_time)) > 30");
            }
            
            // 单次聚合统计，避免拉全量+N次跟进查询导致超时
            final String lastTouch = "GREATEST("
                    + "IFNULL((SELECT MAX(f.create_date) FROM gh_followup_detail f WHERE f.opport_id = gh_opportunity.id AND f.del_flag = 0), '1970-01-01 00:00:00'), "
                    + "IFNULL(gh_opportunity.modi_time, '1970-01-01 00:00:00'), "
                    + "IFNULL(gh_opportunity.create_time, '1970-01-01 00:00:00'))";
            final String diffSec = "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(" + lastTouch + "))";
            queryWrapper.select(
                    "COUNT(1) AS total",
                    "SUM(CASE WHEN " + diffSec + " >= 0 AND " + diffSec + " < 345600 THEN 1 ELSE 0 END) AS key1",
                    "SUM(CASE WHEN " + diffSec + " >= 345600 AND " + diffSec + " < 691200 THEN 1 ELSE 0 END) AS key2",
                    "SUM(CASE WHEN " + diffSec + " >= 691200 AND " + diffSec + " < 1382400 THEN 1 ELSE 0 END) AS key3",
                    "SUM(CASE WHEN " + diffSec + " >= 1382400 THEN 1 ELSE 0 END) AS key4"
            );
            List<Map<String, Object>> rows = opportunityService.listMaps(queryWrapper);
            Map<String, Object> agg = (rows != null && !rows.isEmpty() && rows.get(0) != null) ? rows.get(0) : new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            result.put("total", toIntSafe(agg.get("total")));
            result.put("key1", toIntSafe(agg.get("key1")));
            result.put("key2", toIntSafe(agg.get("key2")));
            result.put("key3", toIntSafe(agg.get("key3")));
            result.put("key4", toIntSafe(agg.get("key4")));
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("统计跟进时间距离天数数量失败", e);
            return Result.error("统计失败：" + e.getMessage());
        }
    }

    private int toIntSafe(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 添加（线索）
     */
    @AutoLog(value = "商机-添加")
    @ApiOperation(value = "商机-添加", notes = "商机-添加")
    @PostMapping(value = "/add")
    public Result<?> add(HttpServletRequest request, @RequestBody GhOpportunity opportunity) {
        String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        
        Date createTime = new Date();
        opportunity.setCreateTime(createTime);
        // 跟进时间创建的时候取创建时间
        opportunity.setModiTime(createTime);
        opportunity.setDelFlag(0);
        // 避免 Spring 绑定默认值导致列表查询被动携带 clue_bonus_awarded=0
        if (opportunity.getClueBonusAwarded() == null) {
            opportunity.setClueBonusAwarded(0);
        }
        // 新录入线索默认无效，需在跟进弹窗中“确认有效”后才发放线索奖金
        if (opportunity.getConfirmValid() == null) {
            opportunity.setConfirmValid(0);
        }
        
        // 生成线索编号：XS + 日期 + 序号
        if (StringUtils.isEmpty(opportunity.getOpportunityNo())) {
            String opportunityNo = generateOpportunityNo();
            opportunity.setOpportunityNo(opportunityNo);
        }

        // 新增时固化线索奖金：若未传 clueBonus，则按 opportunityName 从配置表取 bonus_money 写入 clue_bonus
        if (opportunity.getClueBonus() == null
                && opportunityBonusConfigService != null
                && StringUtils.isNotEmpty(opportunity.getOpportunityName())) {
            try {
                QueryWrapper<GhOpportunityBonusConfig> cfgWrapper = new QueryWrapper<>();
                cfgWrapper.eq("del_flag", 0);
                cfgWrapper.eq("opportunity_name", opportunity.getOpportunityName().trim());
                cfgWrapper.last("LIMIT 1");
                GhOpportunityBonusConfig cfg = opportunityBonusConfigService.getOne(cfgWrapper);
                if (cfg != null && cfg.getBonusMoney() != null) {
                    opportunity.setClueBonus(cfg.getBonusMoney());
                }
            } catch (Exception e) {
                // 配置查询失败不影响新增主流程
                log.warn("新增线索固化奖金失败，opportunityName={}", opportunity.getOpportunityName(), e);
            }
        }
        
        // 默认归属类型：如果有业务人员则设为个人，否则设为公海
        if (StringUtils.isEmpty(opportunity.getOwnerType())) {
            if (StringUtils.isNotEmpty(opportunity.getFounder()) || StringUtils.isNotEmpty(opportunity.getChargePerson())) {
                opportunity.setOwnerType("personal");
            } else {
                opportunity.setOwnerType("public");
            }
        }
        
        if (sysUser != null) {
            opportunity.setCreateBy(sysUser.getUsername());
            if (StringUtils.isEmpty(opportunity.getFounder())) {
                opportunity.setFounder(sysUser.getRealname());
            }
            // 如果有业务人员，设置为个人线索
            if (StringUtils.isNotEmpty(opportunity.getFounder()) || StringUtils.isNotEmpty(opportunity.getChargePerson())) {
                opportunity.setOwnerType("personal");
            }
        }
        
        // 检查客户名称是否重复（可选，根据业务需求）
        // List<GhOpportunity> existing = opportunityService.list(
        //     new QueryWrapper<GhOpportunity>()
        //         .eq("corporate_name", opportunity.getCorporateName())
        //         .eq("del_flag", 0)
        // );
        // if (existing.size() > 0) {
        //     return Result.error("添加失败！客户名称重复");
        // }
        
        // 如果状态为"无效客户"，自动设置为无效线索
        if ("invalid".equals(opportunity.getState())) {
            opportunity.setOwnerType("invalid");
        }
        
        opportunityService.save(opportunity);
        
        // 更新商机的跟进时间（与创建时间相同）
        opportunity.setModiTime(createTime);
        opportunityService.updateById(opportunity);
        
        // 发送企业微信通知
        try {
            if (sysUser != null && wechatNotificationService != null) {
                String salesmanUsername = sysUser.getUsername();
                String salesmanRealname = sysUser.getRealname();
                
                // 1. 发送通知给业务员
                try {
                    wechatNotificationService.sendOpportunitySubmitNotification(
                        opportunity.getId(), 
                        salesmanUsername
                    );
                    log.info("商机提交通知已发送给业务员：{}", salesmanUsername);
                } catch (Exception e) {
                    log.error("发送业务员通知失败", e);
                }
                
                // 2. 发送通知给上级
                try {
                    String supervisorUsername = getSupervisorUsername(sysUser.getId());
                    if (StringUtils.isNotEmpty(supervisorUsername)) {
                        wechatNotificationService.sendOpportunitySubmitToSupervisorNotification(
                            opportunity.getId(), 
                            salesmanRealname,
                            supervisorUsername
                        );
                        log.info("商机提交通知已发送给上级：{}", supervisorUsername);
                    }
                } catch (Exception e) {
                    log.error("发送上级通知失败", e);
                }
            }
        } catch (Exception e) {
            log.error("发送企业微信通知失败", e);
            // 通知失败不影响业务流程
        }
        
        return Result.OK("添加成功！");
    }
    
    /**
     * 生成线索编号：XS + 日期(YYYYMMDD) + 4位序号
     */
    private String generateOpportunityNo() {
        String prefix = "XS" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
        
        // 查询当天最大的序号
        QueryWrapper<GhOpportunity> wrapper = new QueryWrapper<>();
        wrapper.likeRight("opportunity_no", prefix);
        wrapper.orderByDesc("opportunity_no");
        wrapper.last("LIMIT 1");
        
        GhOpportunity lastOpportunity = opportunityService.getOne(wrapper);
        int sequence = 1;
        
        if (lastOpportunity != null && StringUtils.isNotEmpty(lastOpportunity.getOpportunityNo())) {
            String lastNo = lastOpportunity.getOpportunityNo();
            if (lastNo.startsWith(prefix) && lastNo.length() > prefix.length()) {
                try {
                    String seqStr = lastNo.substring(prefix.length());
                    sequence = Integer.parseInt(seqStr) + 1;
                } catch (NumberFormatException e) {
                    sequence = 1;
                }
            }
        }
        
        return prefix + String.format("%04d", sequence);
    }

    /**
     * 编辑（线索）
     */
    @AutoLog(value = "商机-编辑")
    @ApiOperation(value = "商机-编辑", notes = "商机-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(HttpServletRequest request, @RequestBody GhOpportunity opportunity) {
        String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        
        Date updateTime = new Date();
        opportunity.setUpdateTime(updateTime);
        // 如果状态改变或业务人员改变，更新跟进时间
        GhOpportunity oldOpportunity = opportunityService.getById(opportunity.getId());
        if (oldOpportunity != null) {
            if (!StringUtils.equals(oldOpportunity.getState(), opportunity.getState()) ||
                !StringUtils.equals(oldOpportunity.getFounder(), opportunity.getFounder()) ||
                !StringUtils.equals(oldOpportunity.getChargePerson(), opportunity.getChargePerson())) {
                opportunity.setModiTime(updateTime);
            } else {
                opportunity.setModiTime(oldOpportunity.getModiTime()); // 保持原跟进时间
            }
        } else {
            opportunity.setModiTime(updateTime);
        }
        
        if (sysUser != null) {
            opportunity.setUpdateBy(sysUser.getUsername());
            // 如果有业务人员，设置为个人线索
            if (StringUtils.isNotEmpty(opportunity.getFounder()) || StringUtils.isNotEmpty(opportunity.getChargePerson())) {
                opportunity.setOwnerType("personal");
            }
        }
        
        // 如果状态为"无效客户"，自动设置为无效线索
        if ("invalid".equals(opportunity.getState())) {
            opportunity.setOwnerType("invalid");
        }
        
        opportunityService.updateById(opportunity);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除（需要删除权限）
     */
    @AutoLog(value = "商机-通过id删除")
    @ApiOperation(value = "商机-通过id删除", notes = "商机-通过id删除")
    @RequiresPermissions("opportunity:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        GhOpportunity opportunity = opportunityService.getById(id);
        if (opportunity != null) {
            opportunity.setDelFlag(1);
            opportunityService.updateById(opportunity);
        }
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除（需要删除权限）
     */
    @AutoLog(value = "商机-批量删除")
    @ApiOperation(value = "商机-批量删除", notes = "商机-批量删除")
    @RequiresPermissions("opportunity:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        for (String id : idList) {
            GhOpportunity opportunity = opportunityService.getById(id);
            if (opportunity != null) {
                opportunity.setDelFlag(1);
                opportunityService.updateById(opportunity);
            }
        }
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     */
    @AutoLog(value = "商机-通过id查询")
    @ApiOperation(value = "商机-通过id查询", notes = "商机-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        GhOpportunity opportunity = opportunityService.getById(id);
        if (opportunity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(opportunity);
    }

    /**
     * 确认线索有效（确认后才发放线索奖金，只发放一次）
     */
    @AutoLog(value = "商机-确认有效")
    @ApiOperation(value = "商机-确认有效", notes = "确认有效后发放线索奖金（只发放一次）")
    @RequiresPermissions("opportunity:confirmValid")
    @PostMapping(value = "/confirmValid")
    public Result<?> confirmValid(HttpServletRequest request, @RequestBody java.util.Map<String, String> params) {
        try {
            String id = params != null ? params.get("id") : null;
            if (StringUtils.isBlank(id)) {
                return Result.error("参数错误：缺少id");
            }
            GhOpportunity opportunity = opportunityService.getById(id);
            if (opportunity == null || opportunity.getDelFlag() != null && opportunity.getDelFlag() == 1) {
                return Result.error("未找到对应线索");
            }
            Date now = new Date();
            // 先确认有效
            if (opportunity.getConfirmValid() == null || opportunity.getConfirmValid() != 1) {
                opportunity.setConfirmValid(1);
                opportunity.setConfirmValidTime(now);
            }

            // 发放线索奖金：只发一次（以 clue_bonus_awarded 为准）
            Integer awarded = opportunity.getClueBonusAwarded();
            if (awarded == null || awarded != 1) {
                // 若 clueBonus 为空，则按 opportunityName（分类 id）从配置表取
                if (opportunity.getClueBonus() == null
                        && opportunityBonusConfigService != null
                        && StringUtils.isNotEmpty(opportunity.getOpportunityName())) {
                    try {
                        QueryWrapper<GhOpportunityBonusConfig> cfgWrapper = new QueryWrapper<>();
                        cfgWrapper.eq("del_flag", 0);
                        cfgWrapper.eq("opportunity_name", opportunity.getOpportunityName().trim());
                        cfgWrapper.last("LIMIT 1");
                        GhOpportunityBonusConfig cfg = opportunityBonusConfigService.getOne(cfgWrapper);
                        if (cfg != null && cfg.getBonusMoney() != null) {
                            opportunity.setClueBonus(cfg.getBonusMoney());
                        }
                    } catch (Exception e) {
                        log.warn("确认有效时取奖金配置失败，opportunityName={}", opportunity.getOpportunityName(), e);
                    }
                }
                if (opportunity.getClueBonus() != null) {
                    opportunity.setClueBonusAwarded(1);
                    opportunity.setClueBonusAwardedTime(now);
                }
            }

            // 更新时间
            opportunity.setModiTime(now);
            opportunity.setUpdateTime(now);
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            if (StringUtils.isNotEmpty(token) && sysBaseAPI != null) {
                String username = JwtUtil.getUsername(token);
                if (StringUtils.isNotEmpty(username)) {
                    opportunity.setUpdateBy(username);
                }
            }

            opportunityService.updateById(opportunity);
            return Result.OK("确认有效成功！");
        } catch (Exception e) {
            log.error("确认有效失败", e);
            return Result.error("确认有效失败：" + e.getMessage());
        }
    }

    /**
     * 导出excel（带下拉选择框）
     */
    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("opportunity:export")
    public void exportXls(HttpServletRequest request, HttpServletResponse response, GhOpportunity opportunity) {
        try {
            // Step.1 组装查询条件
            Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
            // 移除ownerType参数，导出时包含所有tab页的数据
            paramMap.remove("ownerType");
            // 如果opportunity对象中有ownerType，也清除
            if (opportunity != null) {
                opportunity.setOwnerType(null);
            }
            
            QueryWrapper<GhOpportunity> queryWrapper = QueryGenerator.initQueryWrapper(opportunity, paramMap);
            queryWrapper.eq("del_flag", 0);
            
            // 导出时包含所有tab页的数据，不限制ownerType
            // 只排除已删除的数据（del_flag = 0）
            
            // 记录查询条件用于调试
            log.info("导出查询条件: {}", queryWrapper.getTargetSql());
            log.info("导出查询参数: {}", queryWrapper.getParamNameValuePairs());
            
            // Step.2 获取导出数据
            List<GhOpportunity> pageList = opportunityService.list(queryWrapper);
            log.info("查询到的数据条数: {}", pageList != null ? pageList.size() : 0);
            List<GhOpportunity> exportList = null;
            
            // 过滤选中数据
            String selections = request.getParameter("selections");
            if (oConvertUtils.isNotEmpty(selections)) {
                List<String> selectionList = Arrays.asList(selections.split(","));
                exportList = pageList.stream().filter(item -> selectionList.contains(item.getId())).collect(java.util.stream.Collectors.toList());
            } else {
                exportList = pageList;
            }
            
            // 如果没有数据，记录日志并返回空Excel
            if (exportList == null || exportList.isEmpty()) {
                log.warn("导出数据为空，查询条件: {}", queryWrapper.getTargetSql());
                // 创建一个空的Excel文件
                org.jeecgframework.poi.excel.entity.ExportParams exportParams = new org.jeecgframework.poi.excel.entity.ExportParams("线索管理报表", "导出人:" + ((LoginUser) SecurityUtils.getSubject().getPrincipal()).getRealname(), "线索管理");
                exportParams.setImageBasePath(upLoadPath);
                org.apache.poi.ss.usermodel.Workbook workbook = org.jeecgframework.poi.excel.ExcelExportUtil.exportExcel(exportParams, GhOpportunity.class, new java.util.ArrayList<>());
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                String fileName = "线索管理_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
                response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
                workbook.write(response.getOutputStream());
                workbook.close();
                return;
            }
            
            // 获取字典数据用于转换
            List<org.jeecg.common.system.vo.DictModel> stateDictItems = sysBaseAPI != null ? sysBaseAPI.getDictItems("opportunity_status") : null;
            List<org.jeecg.common.system.vo.DictModel> sourceDictItems = sysBaseAPI != null ? sysBaseAPI.getDictItems("opportunity_source") : null;
            
            // 创建字典值到文本的映射
            java.util.Map<String, String> stateDictMap = new java.util.HashMap<>();
            if (stateDictItems != null) {
                for (org.jeecg.common.system.vo.DictModel item : stateDictItems) {
                    if (item.getValue() != null && item.getText() != null) {
                        stateDictMap.put(item.getValue(), item.getText());
                    }
                }
            }
            
            java.util.Map<String, String> sourceDictMap = new java.util.HashMap<>();
            if (sourceDictItems != null) {
                for (org.jeecg.common.system.vo.DictModel item : sourceDictItems) {
                    if (item.getValue() != null && item.getText() != null) {
                        sourceDictMap.put(item.getValue(), item.getText());
                    }
                }
            }
            
            // 处理导出数据：将字典值和ID转换为名称
            for (GhOpportunity item : exportList) {
                // 将当前状态字典值转换为文本
                if (StringUtils.isNotEmpty(item.getState()) && stateDictMap.containsKey(item.getState())) {
                    item.setState(stateDictMap.get(item.getState()));
                }
                
                // 将客户来源字典值转换为文本
                if (StringUtils.isNotEmpty(item.getOpportunitySource()) && sourceDictMap.containsKey(item.getOpportunitySource())) {
                    item.setOpportunitySource(sourceDictMap.get(item.getOpportunitySource()));
                }
                
                // 将商机名称ID转换为名称
                if (StringUtils.isNotEmpty(item.getOpportunityName()) && sysCategoryService != null) {
                    try {
                        org.jeecg.modules.system.entity.SysCategory category = sysCategoryService.getById(item.getOpportunityName());
                        if (category != null && StringUtils.isNotEmpty(category.getName())) {
                            item.setOpportunityName(category.getName());
                        }
                    } catch (Exception e) {
                        log.debug("转换商机名称失败，ID: {}", item.getOpportunityName(), e);
                    }
                }
            }
            
            // Step.3 使用EasyPoi生成Excel Workbook
            org.jeecgframework.poi.excel.entity.ExportParams exportParams = new org.jeecgframework.poi.excel.entity.ExportParams("线索管理报表", "导出人:" + ((LoginUser) SecurityUtils.getSubject().getPrincipal()).getRealname(), "线索管理");
            exportParams.setImageBasePath(upLoadPath);
            
            // 使用EasyPoi创建Workbook
            org.apache.poi.ss.usermodel.Workbook workbook = org.jeecgframework.poi.excel.ExcelExportUtil.exportExcel(exportParams, GhOpportunity.class, exportList);
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            // EasyPoi默认会在第0行添加标题，第1行是表头，第2行开始是数据
            // 查找表头行（通常在第1行，如果第1行为空则尝试第0行）
            org.apache.poi.ss.usermodel.Row headerRow = null;
            for (int i = 0; i <= 2; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row != null) {
                    // 检查这一行是否包含表头关键词
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            if ("当前状态".equals(cellValue) || "状态".equals(cellValue) || 
                                "客户来源".equals(cellValue) || "商机来源".equals(cellValue) ||
                                "业务人员".equals(cellValue) || "客户名称".equals(cellValue)) {
                                headerRow = row;
                                break;
                            }
                        }
                    }
                    if (headerRow != null) {
                        break;
                    }
                }
            }
            
            if (headerRow == null) {
                log.warn("未找到表头行，无法添加下拉选择框");
            } else {
                log.info("找到表头行，行号: {}", headerRow.getRowNum());
                
                // 动态查找列索引（使用新的列名）
                int stateColumnIndex = findColumnIndex(headerRow, "当前状态");
                if (stateColumnIndex < 0) {
                    stateColumnIndex = findColumnIndex(headerRow, "状态"); // 兼容旧列名
                }
                int sourceColumnIndex = findColumnIndex(headerRow, "客户来源");
                if (sourceColumnIndex < 0) {
                    sourceColumnIndex = findColumnIndex(headerRow, "商机来源"); // 兼容旧列名
                }
                
                log.info("列索引 - 当前状态: {}, 客户来源: {}", stateColumnIndex, sourceColumnIndex);
                
                // 使用前面已经获取的字典数据（stateDictItems 和 sourceDictItems 已在前面定义）
                
                // 创建数据验证辅助对象
                org.apache.poi.ss.usermodel.DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                
                // 为状态列添加下拉选择
                if (stateColumnIndex >= 0 && stateDictItems != null && !stateDictItems.isEmpty()) {
                    String[] stateOptions = stateDictItems.stream()
                        .map(item -> item.getText())
                        .toArray(String[]::new);
                    addDataValidation(sheet, validationHelper, stateColumnIndex, stateOptions);
                    log.info("已为当前状态列添加下拉选择，选项数: {}", stateOptions.length);
                } else {
                    log.warn("当前状态列索引无效或字典数据为空 - 索引: {}, 字典项数: {}", stateColumnIndex, stateDictItems != null ? stateDictItems.size() : 0);
                }
                
                // 为客户来源列添加下拉选择
                if (sourceColumnIndex >= 0 && sourceDictItems != null && !sourceDictItems.isEmpty()) {
                    String[] sourceOptions = sourceDictItems.stream()
                        .map(item -> item.getText())
                        .toArray(String[]::new);
                    addDataValidation(sheet, validationHelper, sourceColumnIndex, sourceOptions);
                    log.info("已为客户来源列添加下拉选择，选项数: {}", sourceOptions.length);
                } else {
                    log.warn("客户来源列索引无效或字典数据为空 - 索引: {}, 字典项数: {}", sourceColumnIndex, sourceDictItems != null ? sourceDictItems.size() : 0);
                }
            }
            
            // Step.5 输出Excel到响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "线索管理_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            try {
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (java.io.IOException ioException) {
                log.error("写入错误信息失败", ioException);
            }
        }
    }
    
    /**
     * 获取单元格值（字符串形式）
     * @param cell 单元格
     * @return 字符串值
     */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 查找列索引
     * @param headerRow 表头行
     * @param columnName 列名
     * @return 列索引，如果找不到返回-1
     */
    private int findColumnIndex(org.apache.poi.ss.usermodel.Row headerRow, String columnName) {
        if (headerRow == null) {
            return -1;
        }
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String cellValue = getCellValueAsString(cell);
                if (columnName.equals(cellValue)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * 为指定列添加下拉选择框
     * @param sheet Excel工作表
     * @param validationHelper 数据验证辅助对象
     * @param columnIndex 列索引（从0开始）
     * @param options 下拉选项数组
     */
    private void addDataValidation(org.apache.poi.ss.usermodel.Sheet sheet, 
                                   org.apache.poi.ss.usermodel.DataValidationHelper validationHelper,
                                   int columnIndex, 
                                   String[] options) {
        try {
            if (options == null || options.length == 0) {
                log.warn("下拉选项为空，列索引: {}", columnIndex);
                return;
            }
            
            // 创建下拉选项的约束
            org.apache.poi.ss.usermodel.DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
            
            // 获取数据起始行（EasyPoi通常在第2行开始数据，第0行是标题，第1行是表头）
            int firstDataRow = 2;
            int lastRow = sheet.getLastRowNum();
            if (lastRow < firstDataRow) {
                lastRow = firstDataRow + 1000; // 如果没有数据，至少设置1000行
            } else {
                lastRow = Math.max(lastRow, firstDataRow + 1000); // 至少1000行，或者更多
            }
            
            // 设置验证区域（从数据起始行开始，到指定行）
            org.apache.poi.ss.util.CellRangeAddressList addressList = new org.apache.poi.ss.util.CellRangeAddressList(firstDataRow, lastRow, columnIndex, columnIndex);
            
            // 创建数据验证
            org.apache.poi.ss.usermodel.DataValidation validation = validationHelper.createValidation(constraint, addressList);
            
            // 设置验证属性
            validation.setSuppressDropDownArrow(false); // false表示显示下拉箭头
            validation.setShowErrorBox(true); // 显示错误提示框
            validation.setShowPromptBox(true); // 显示提示框
            validation.setErrorStyle(org.apache.poi.ss.usermodel.DataValidation.ErrorStyle.STOP); // 错误时停止输入
            validation.createErrorBox("输入错误", "请从下拉列表中选择有效的选项！");
            validation.createPromptBox("提示", "请从下拉列表中选择");
            
            // 应用到工作表
            sheet.addValidationData(validation);
            
            log.info("成功为列 {} 添加下拉选择框，选项数: {}, 验证区域: 行{}-{}", columnIndex, options.length, firstDataRow, lastRow);
        } catch (Exception e) {
            log.error("为列 {} 添加下拉选择框失败", columnIndex, e);
        }
    }

    /**
     * 通过excel导入数据（自定义处理字典值转换）
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @RequiresPermissions("opportunity:import")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件对象
            
            // 检查文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String lowerFilename = originalFilename.toLowerCase();
                if (lowerFilename.endsWith(".csv")) {
                    return Result.error("不支持CSV格式，请使用Excel格式（.xlsx或.xls）导入。请先导出Excel模板，填写数据后再导入。");
                }
                if (!lowerFilename.endsWith(".xlsx") && !lowerFilename.endsWith(".xls")) {
                    return Result.error("不支持的文件格式，请使用Excel格式（.xlsx或.xls）导入。");
                }
            }
            
            org.jeecgframework.poi.excel.entity.ImportParams params = new org.jeecgframework.poi.excel.entity.ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<GhOpportunity> list = org.jeecgframework.poi.excel.ExcelImportUtil.importExcel(file.getInputStream(), GhOpportunity.class, params);
                
                log.info("导入Excel解析成功，共 {} 条数据", list != null ? list.size() : 0);
                
                // 先查询当天最大的序号，用于批量生成线索编号
                String todayPrefix = "XS" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
                QueryWrapper<GhOpportunity> maxNoWrapper = new QueryWrapper<>();
                maxNoWrapper.likeRight("opportunity_no", todayPrefix);
                maxNoWrapper.orderByDesc("opportunity_no");
                maxNoWrapper.last("LIMIT 1");
                GhOpportunity lastOpportunity = opportunityService.getOne(maxNoWrapper);
                int startSequence = 1;
                if (lastOpportunity != null && StringUtils.isNotEmpty(lastOpportunity.getOpportunityNo())) {
                    String lastNo = lastOpportunity.getOpportunityNo();
                    if (lastNo.startsWith(todayPrefix) && lastNo.length() > todayPrefix.length()) {
                        try {
                            String seqStr = lastNo.substring(todayPrefix.length());
                            startSequence = Integer.parseInt(seqStr) + 1;
                        } catch (NumberFormatException e) {
                            startSequence = 1;
                        }
                    }
                }
                
                // 用于批量导入时生成唯一编号的计数器
                final int[] sequenceCounter = {startSequence};
                
                // 处理字典值转换
                int rowIndex = 0;
                for (GhOpportunity opportunity : list) {
                    rowIndex++;
                    log.debug("处理第 {} 条导入数据，客户名称: {}, 商机名称: {}", rowIndex, opportunity.getCorporateName(), opportunity.getOpportunityName());
                    
                    // 转换状态字典值（从文本转换为字典值）
                    if (StringUtils.isNotEmpty(opportunity.getState())) {
                        String stateValue = convertDictTextToValue("opportunity_status", opportunity.getState());
                        if (stateValue != null) {
                            opportunity.setState(stateValue);
                        }
                    }
                    
                    // 转换客户来源字典值
                    if (StringUtils.isNotEmpty(opportunity.getOpportunitySource())) {
                        String sourceValue = convertDictTextToValue("opportunity_source", opportunity.getOpportunitySource());
                        if (sourceValue != null) {
                            opportunity.setOpportunitySource(sourceValue);
                        }
                    }
                    
                    // 转换归属类型字典值
                    if (StringUtils.isNotEmpty(opportunity.getOwnerType())) {
                        String ownerTypeValue = convertDictTextToValue("owner_type", opportunity.getOwnerType());
                        if (ownerTypeValue != null) {
                            opportunity.setOwnerType(ownerTypeValue);
                        }
                    }
                    
                    // 转换商机名称：从名称转换为分类ID（在pcode="A01"下查找）
                    if (StringUtils.isNotEmpty(opportunity.getOpportunityName())) {
                        String originalName = opportunity.getOpportunityName();
                        log.debug("开始转换商机名称: {}", originalName);
                        String categoryId = findCategoryIdByName(originalName, "A01");
                        if (categoryId != null) {
                            opportunity.setOpportunityName(categoryId);
                            log.info("导入时商机名称转换成功: {} -> {}", originalName, categoryId);
                        } else {
                            log.warn("导入时未找到匹配的商机名称分类: {}，将保留原值", originalName);
                            // 如果找不到匹配的分类，保留原值（可能是分类名称，也可能是ID）
                            // 如果原值看起来像ID（32位字符串），则保留；否则尝试直接查询
                            if (originalName.length() == 32 && originalName.matches("^[a-zA-Z0-9]{32}$")) {
                                // 看起来像ID，直接使用
                                log.info("商机名称看起来像ID，直接使用: {}", originalName);
                            } else {
                                // 不是ID，尝试查询是否存在该名称的分类
                                log.warn("商机名称 '{}' 未找到匹配的分类，将保留原值", originalName);
                            }
                        }
                    } else {
                        log.debug("第 {} 条数据的商机名称为空", rowIndex);
                    }
                    
                    // 验证业务人员是否存在（根据姓名查找用户）
                    if (StringUtils.isNotEmpty(opportunity.getFounder())) {
                        boolean userExists = validateUserByRealname(opportunity.getFounder());
                        if (!userExists) {
                            log.warn("导入时未找到匹配的业务人员: {}", opportunity.getFounder());
                            // 可以选择清空或保留（根据业务需求，这里保留但记录警告）
                        }
                    }
                    
                    // 处理所属区域：如果不填，默认为"浙江省/杭州市/拱墅区"
                    if (StringUtils.isEmpty(opportunity.getRegion())) {
                        opportunity.setRegion("浙江省/杭州市/拱墅区");
                    } else {
                        // 规范化区域格式：去除空格，统一使用"/"分隔符
                        String region = opportunity.getRegion().trim();
                        // 如果包含"-"或其他分隔符，统一转换为"/"
                        region = region.replaceAll("[-\\s]+", "/");
                        // 去除多余的"/"
                        region = region.replaceAll("/+", "/");
                        // 去除首尾的"/"
                        if (region.startsWith("/")) {
                            region = region.substring(1);
                        }
                        if (region.endsWith("/")) {
                            region = region.substring(0, region.length() - 1);
                        }
                        opportunity.setRegion(region);
                    }
                    
                    // 设置默认值
                    if (opportunity.getDelFlag() == null) {
                        opportunity.setDelFlag(0);
                    }
                    if (opportunity.getCreateTime() == null) {
                        opportunity.setCreateTime(new Date());
                    }

                    // 导入时固化线索奖金：若未填 clueBonus，则按 opportunityName 从配置表取 bonus_money 写入 clue_bonus
                    if (opportunity.getClueBonus() == null
                            && opportunityBonusConfigService != null
                            && StringUtils.isNotEmpty(opportunity.getOpportunityName())) {
                        try {
                            QueryWrapper<GhOpportunityBonusConfig> cfgWrapper = new QueryWrapper<>();
                            cfgWrapper.eq("del_flag", 0);
                            cfgWrapper.eq("opportunity_name", opportunity.getOpportunityName().trim());
                            cfgWrapper.last("LIMIT 1");
                            GhOpportunityBonusConfig cfg = opportunityBonusConfigService.getOne(cfgWrapper);
                            if (cfg != null && cfg.getBonusMoney() != null) {
                                opportunity.setClueBonus(cfg.getBonusMoney());
                            }
                        } catch (Exception e) {
                            log.warn("导入线索固化奖金失败，opportunityName={}", opportunity.getOpportunityName(), e);
                        }
                    }
                    
                    // 导入时生成新的线索编号（批量导入时使用递增序号）
                    String opportunityNo = todayPrefix + String.format("%04d", sequenceCounter[0]++);
                    opportunity.setOpportunityNo(opportunityNo);
                    
                    // 设置默认归属类型
                    if (StringUtils.isEmpty(opportunity.getOwnerType())) {
                        if (StringUtils.isNotEmpty(opportunity.getFounder()) || StringUtils.isNotEmpty(opportunity.getChargePerson())) {
                            opportunity.setOwnerType("personal");
                        } else {
                            opportunity.setOwnerType("public");
                        }
                    }
                    
                    // 如果状态为"无效客户"，自动设置为无效线索
                    if ("invalid".equals(opportunity.getState())) {
                        opportunity.setOwnerType("invalid");
                    }
                }
                
                //update-begin-author:taoyan date:20190528 for:批量插入数据
                long start = System.currentTimeMillis();
                opportunityService.saveBatch(list);
                //400条 saveBatch消耗时间1592毫秒  循环插入消耗时间1947毫秒
                //1200条  saveBatch消耗时间3687毫秒 循环插入消耗时间5212毫秒
                log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
                //update-end-author:taoyan date:20190528 for:批量插入数据
                return Result.ok("文件导入成功！数据行数：" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }
    
    /**
     * 根据分类名称查找分类ID（在指定pcode下递归查找，只返回最底层的叶子节点）
     * @param categoryName 分类名称
     * @param pcode 父级分类代码（如"A01"）
     * @return 分类ID，如果找不到则返回null
     */
    private String findCategoryIdByName(String categoryName, String pcode) {
        if (StringUtils.isEmpty(categoryName) || sysCategoryService == null) {
            return null;
        }
        
        try {
            // 先查找pcode对应的父级分类（sys_category表没有del_flag字段）
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.system.entity.SysCategory> parentWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            parentWrapper.eq("code", pcode);
            org.jeecg.modules.system.entity.SysCategory parentCategory = sysCategoryService.getOne(parentWrapper);
            
            if (parentCategory == null) {
                log.warn("未找到父级分类，pcode: {}", pcode);
                return null;
            }
            
            // 递归查找分类树中是否存在该名称（只返回最底层的叶子节点）
            return findCategoryIdByNameRecursive(categoryName, parentCategory.getId());
        } catch (Exception e) {
            log.error("查找分类ID失败，分类名称: {}, pcode: {}", categoryName, pcode, e);
            return null;
        }
    }
    
    /**
     * 递归查找分类树中是否存在指定名称（只返回最底层的叶子节点）
     * @param categoryName 要查找的分类名称
     * @param parentId 父级分类ID
     * @return 如果找到返回分类ID（最底层叶子节点），否则返回null
     */
    private String findCategoryIdByNameRecursive(String categoryName, String parentId) {
        if (sysCategoryService == null || StringUtils.isEmpty(categoryName) || StringUtils.isEmpty(parentId)) {
            return null;
        }
        
        try {
            // 根据父级ID查询所有子分类（使用pid字段查询子分类，sys_category表没有del_flag字段）
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.system.entity.SysCategory> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("pid", parentId);  // 使用pid（父级ID）来查询子分类
            
            java.util.List<org.jeecg.modules.system.entity.SysCategory> categories = sysCategoryService.list(wrapper);
            
            if (categories != null && !categories.isEmpty()) {
                // 先递归查找所有子分类，找到所有匹配的名称
                java.util.List<String> matchedIds = new java.util.ArrayList<>();
                for (org.jeecg.modules.system.entity.SysCategory category : categories) {
                    // 检查当前分类名称是否匹配（精确匹配）
                    if (categoryName.equals(category.getName())) {
                        matchedIds.add(category.getId());
                    }
                    // 递归查找子分类
                    String foundId = findCategoryIdByNameRecursive(categoryName, category.getId());
                    if (foundId != null) {
                        matchedIds.add(foundId);
                    }
                }
                
                // 如果有匹配的分类，找出最底层的（没有子节点的）
                if (!matchedIds.isEmpty()) {
                    for (String matchedId : matchedIds) {
                        // 检查该分类是否有子节点（sys_category表没有del_flag字段）
                        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.system.entity.SysCategory> childWrapper = 
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                        childWrapper.eq("pid", matchedId);  // 使用pid查询子节点
                        long childCount = sysCategoryService.count(childWrapper);
                        
                        // 如果没有子节点（是叶子节点），返回该分类ID
                        if (childCount == 0) {
                            return matchedId;
                        }
                    }
                    // 如果所有匹配的分类都有子节点，返回最后一个（最底层的）
                    return matchedIds.get(matchedIds.size() - 1);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("递归查找分类失败，分类名称: {}, 父级ID: {}", categoryName, parentId, e);
            return null;
        }
    }
    
    /**
     * 根据真实姓名验证用户是否存在
     * @param realname 用户真实姓名
     * @return 如果用户存在返回true，否则返回false
     */
    private boolean validateUserByRealname(String realname) {
        if (StringUtils.isEmpty(realname) || sysUserService == null) {
            return false;
        }
        
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(org.jeecg.modules.system.entity.SysUser::getRealname, realname);
            wrapper.eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0);
            wrapper.last("LIMIT 1");
            
            org.jeecg.modules.system.entity.SysUser user = sysUserService.getOne(wrapper);
            return user != null;
        } catch (Exception e) {
            log.error("验证用户是否存在失败，真实姓名: {}", realname, e);
            return false;
        }
    }
    
    /**
     * 将字典文本转换为字典值
     * @param dictCode 字典代码
     * @param dictText 字典文本
     * @return 字典值，如果找不到则返回null
     */
    private String convertDictTextToValue(String dictCode, String dictText) {
        if (StringUtils.isEmpty(dictText) || sysBaseAPI == null) {
            return null;
        }
        
        try {
            // 先尝试直接匹配（如果已经是字典值）
            List<org.jeecg.common.system.vo.DictModel> dictItems = sysBaseAPI.getDictItems(dictCode);
            if (dictItems != null) {
                for (org.jeecg.common.system.vo.DictModel item : dictItems) {
                    // 如果文本匹配字典值，直接返回
                    if (dictText.equals(item.getValue())) {
                        return item.getValue();
                    }
                    // 如果文本匹配字典文本，返回字典值
                    if (dictText.equals(item.getText())) {
                        return item.getValue();
                    }
                }
            }
            
            // 如果找不到匹配项，尝试模糊匹配（去除空格、括号等）
            String cleanText = dictText.trim();
            if (dictItems != null) {
                for (org.jeecg.common.system.vo.DictModel item : dictItems) {
                    String itemText = item.getText();
                    if (itemText != null) {
                        // 去除括号内容进行匹配（如"意向A+(已签约)"匹配"意向A+"）
                        String itemTextClean = itemText.replaceAll("\\(.*?\\)", "").trim();
                        String cleanTextNoBracket = cleanText.replaceAll("\\(.*?\\)", "").trim();
                        if (itemTextClean.equals(cleanTextNoBracket) || itemText.equals(cleanText)) {
                            return item.getValue();
                        }
                    }
                }
            }
            
            log.warn("未找到匹配的字典项 - dictCode: {}, dictText: {}", dictCode, dictText);
            return null;
        } catch (Exception e) {
            log.error("转换字典文本失败 - dictCode: {}, dictText: {}", dictCode, dictText, e);
            return null;
        }
    }
    
    /**
     * 线索分配（将公海线索分配给指定业务人员）
     */
    @AutoLog(value = "商机-线索分配")
    @ApiOperation(value = "商机-线索分配", notes = "商机-线索分配")
    @PostMapping(value = "/assign")
    public Result<?> assign(@RequestBody java.util.Map<String, String> params) {
        try {
            String id = params.get("id");
            String chargePerson = params.get("chargePerson");
            
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(chargePerson)) {
                return Result.error("参数不完整");
            }
            
            GhOpportunity opportunity = opportunityService.getById(id);
            if (opportunity == null) {
                return Result.error("线索不存在");
            }
            
            opportunity.setChargePerson(chargePerson);
            opportunity.setFounder(chargePerson); // 业务人员
            opportunity.setOwnerType("personal"); // 设置为个人线索
            opportunity.setModiTime(new Date());
            opportunity.setUpdateTime(new Date());
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                opportunity.setUpdateBy(sysUser.getUsername());
            }
            
            opportunityService.updateById(opportunity);
            
            return Result.OK("分配成功！");
        } catch (Exception e) {
            log.error("线索分配失败", e);
            return Result.error("分配失败：" + e.getMessage());
        }
    }
    
    /**
     * 移入公海（将个人线索移入公海）
     */
    @AutoLog(value = "商机-移入公海")
    @ApiOperation(value = "商机-移入公海", notes = "商机-移入公海")
    @PostMapping(value = "/moveToPublic")
    public Result<?> moveToPublic(@RequestBody java.util.Map<String, String> params) {
        try {
            String id = params.get("id");
            
            if (StringUtils.isEmpty(id)) {
                return Result.error("参数不完整");
            }
            
            GhOpportunity opportunity = opportunityService.getById(id);
            if (opportunity == null) {
                return Result.error("线索不存在");
            }
            
            opportunity.setOwnerType("public"); // 设置为公海线索
            opportunity.setChargePerson(null); // 清空业务人员
            opportunity.setFounder(null); // 清空负责人
            opportunity.setModiTime(new Date());
            opportunity.setUpdateTime(new Date());
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                opportunity.setUpdateBy(sysUser.getUsername());
            }
            
            opportunityService.updateById(opportunity);
            
            return Result.OK("移入公海成功！");
        } catch (Exception e) {
            log.error("移入公海失败", e);
            return Result.error("移入公海失败：" + e.getMessage());
        }
    }
    
    /**
     * 转客户（将商机转为客户）
     */
    @AutoLog(value = "商机-转客户")
    @ApiOperation(value = "商机-转客户", notes = "商机-转客户")
    @PostMapping(value = "/convertToCustomer")
    public Result<?> convertToCustomer(HttpServletRequest request, @RequestBody java.util.Map<String, String> params) {
        try {
            String id = params.get("id");
            
            if (StringUtils.isEmpty(id)) {
                return Result.error("参数不完整");
            }
            
            GhOpportunity opportunity = opportunityService.getById(id);
            if (opportunity == null) {
                return Result.error("线索不存在");
            }
            
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            String username = JwtUtil.getUsername(token);
            LoginUser sysUser = sysBaseAPI.getUserByName(username);
            
            // 复用内部转客户方法
            return convertToCustomerInternal(opportunity, sysUser);
            
        } catch (Exception e) {
            log.error("转客户失败", e);
            return Result.error("转客户失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量转客户（将多个商机转为客户）
     * 只转换状态为"意向A+(已签约)"的线索（state为"intention_a_plus"或"1"）
     */
    @AutoLog(value = "商机-批量转客户")
    @ApiOperation(value = "商机-批量转客户", notes = "商机-批量转客户，只转换状态为意向A+(已签约)的线索")
    @PostMapping(value = "/batchConvertToCustomer")
    public Result<?> batchConvertToCustomer(HttpServletRequest request, @RequestBody java.util.Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) params.get("ids");
            
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要转换的线索");
            }
            
            // 查询选中的线索
            List<GhOpportunity> opportunities = opportunityService.listByIds(ids);
            if (opportunities == null || opportunities.isEmpty()) {
                return Result.error("未找到要转换的线索");
            }
            
            // 筛选出状态为"意向A+(已签约)"的线索
            List<GhOpportunity> eligibleOpportunities = new ArrayList<>();
            for (GhOpportunity opp : opportunities) {
                String state = opp.getState();
                if (("intention_a_plus".equals(state) || "1".equals(state)) && 
                    StringUtils.isNotEmpty(opp.getCorporateName())) {
                    eligibleOpportunities.add(opp);
                }
            }
            
            if (eligibleOpportunities.isEmpty()) {
                return Result.error("所选线索中没有状态为\"意向A+(已签约)\"的线索，无法批量转客户");
            }
            
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            String username = JwtUtil.getUsername(token);
            LoginUser sysUser = sysBaseAPI.getUserByName(username);
            
            int successCount = 0;
            int failCount = 0;
            List<String> failMessages = new ArrayList<>();
            
            // 批量转换
            for (GhOpportunity opportunity : eligibleOpportunities) {
                try {
                    // 复用单个转客户的逻辑
                    Result<?> result = convertToCustomerInternal(opportunity, sysUser);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        failMessages.add(opportunity.getCorporateName() + "：" + result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    failMessages.add(opportunity.getCorporateName() + "：转客户失败 - " + e.getMessage());
                    log.error("批量转客户失败，线索ID：{}，公司名称：{}", opportunity.getId(), opportunity.getCorporateName(), e);
                }
            }
            
            // 构建返回结果
            java.util.Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("successCount", successCount);
            resultMap.put("failCount", failCount);
            resultMap.put("failMessages", failMessages);
            
            String message = String.format("批量转客户完成！成功：%d条，失败：%d条", successCount, failCount);
            return Result.OK(message, resultMap);
            
        } catch (Exception e) {
            log.error("批量转客户失败", e);
            return Result.error("批量转客户失败：" + e.getMessage());
        }
    }
    
    /**
     * 全部已签约线索转客户（自动过滤已转换的线索）
     * 查询所有状态为"意向A+(已签约)"且未转客户的线索，然后批量转换
     */
    @AutoLog(value = "商机-全部已签约线索转客户")
    @ApiOperation(value = "商机-全部已签约线索转客户", notes = "将所有已签约且未转客户的线索转为客户，自动过滤已转换的线索")
    @RequiresPermissions("opportunity:convertAllSignedToCustomer")
    @PostMapping(value = "/convertAllSignedToCustomer")
    public Result<?> convertAllSignedToCustomer(HttpServletRequest request) {
        try {
            String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
            String username = JwtUtil.getUsername(token);
            LoginUser sysUser = sysBaseAPI.getUserByName(username);
            
            // 查询所有状态为"意向A+(已签约)"且未转客户的线索
            QueryWrapper<GhOpportunity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("state", Arrays.asList("intention_a_plus", "1")); // 状态为意向A+或1
            queryWrapper.and(wrapper -> wrapper.isNull("customer_id").or().eq("customer_id", "")); // 未转客户（customerId为空）
            queryWrapper.isNotNull("corporate_name"); // 公司名称不为空
            queryWrapper.ne("corporate_name", ""); // 公司名称不为空字符串
            queryWrapper.eq("del_flag", 0); // 未删除
            
            List<GhOpportunity> opportunities = opportunityService.list(queryWrapper);
            
            if (opportunities == null || opportunities.isEmpty()) {
                return Result.OK("没有需要转换的已签约线索", new java.util.HashMap<String, Object>() {{
                    put("totalCount", 0);
                    put("successCount", 0);
                    put("failCount", 0);
                    put("skippedCount", 0);
                }});
            }
            
            int totalCount = opportunities.size();
            int successCount = 0;
            int failCount = 0;
            int skippedCount = 0;
            List<String> failMessages = new ArrayList<>();
            
            // 批量转换
            for (GhOpportunity opportunity : opportunities) {
                try {
                    // 再次检查是否已转客户（防止并发问题）
                    if (StringUtils.isNotEmpty(opportunity.getCustomerId())) {
                        skippedCount++;
                        continue;
                    }
                    
                    // 复用内部转客户方法
                    Result<?> result = convertToCustomerInternal(opportunity, sysUser);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        failMessages.add(opportunity.getCorporateName() + "：" + result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    failMessages.add(opportunity.getCorporateName() + "：转客户失败 - " + e.getMessage());
                    log.error("全部转客户失败，线索ID：{}，公司名称：{}", opportunity.getId(), opportunity.getCorporateName(), e);
                }
            }
            
            // 构建返回结果
            java.util.Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalCount", totalCount);
            resultMap.put("successCount", successCount);
            resultMap.put("failCount", failCount);
            resultMap.put("skippedCount", skippedCount);
            resultMap.put("failMessages", failMessages);
            
            String message = String.format("全部转客户完成！共找到%d条已签约线索，成功：%d条，失败：%d条", 
                    totalCount, successCount, failCount);
            if (skippedCount > 0) {
                message += String.format("，已跳过（已转换）：%d条", skippedCount);
            }
            
            return Result.OK(message, resultMap);
            
        } catch (Exception e) {
            log.error("全部转客户失败", e);
            return Result.error("全部转客户失败：" + e.getMessage());
        }
    }
    
    /**
     * 转客户的内部方法（供单个和批量转客户复用）
     * @param opportunity 商机信息
     * @param sysUser 当前登录用户
     * @return Result
     */
    private Result<?> convertToCustomerInternal(GhOpportunity opportunity, LoginUser sysUser) {
        try {
            if (opportunity == null) {
                return Result.error("线索不存在");
            }
            
            if (StringUtils.isEmpty(opportunity.getCorporateName())) {
                return Result.error("公司名称不能为空，无法转客户");
            }
            
            // 检查客户是否已存在（根据公司名称）
            QueryWrapper<GhCustomer> customerQueryWrapper = new QueryWrapper<>();
            customerQueryWrapper.eq("corporate_name", opportunity.getCorporateName());
            customerQueryWrapper.eq("del_flag", 0);
            List<GhCustomer> existingCustomers = customerService.list(customerQueryWrapper);
            
            GhCustomer customer;
            boolean isNew = false;
            boolean isConvertedByOther = false; // 标记是否由其他线索转换
            
            if (existingCustomers != null && !existingCustomers.isEmpty()) {
                // 客户已存在，检查是否有其他线索已经转换为该客户
                customer = existingCustomers.get(0);
                isNew = false;
                
                // 检查是否有其他线索（排除当前线索）已经转换为该客户
                QueryWrapper<GhOpportunity> opportunityQueryWrapper = new QueryWrapper<>();
                opportunityQueryWrapper.eq("corporate_name", opportunity.getCorporateName());
                opportunityQueryWrapper.eq("customer_id", customer.getId());
                opportunityQueryWrapper.ne("id", opportunity.getId()); // 排除当前线索
                opportunityQueryWrapper.eq("del_flag", 0);
                List<GhOpportunity> convertedOpportunities = opportunityService.list(opportunityQueryWrapper);
                
                if (convertedOpportunities != null && !convertedOpportunities.isEmpty()) {
                    // 已有其他线索转换为该客户，不创建新客户，只关联
                    isConvertedByOther = true;
                    log.info("该客户已由其他线索转换，公司名称：{}，客户ID：{}，当前线索ID：{}", 
                             opportunity.getCorporateName(), customer.getId(), opportunity.getId());
                } else {
                    // 客户存在但没有其他线索转换（可能是手动创建的），更新客户信息（保留已有信息，补充缺失信息）
                    // 更新客户信息（如果商机中的信息不为空且客户中的信息为空，则更新）
                    if (StringUtils.isNotEmpty(opportunity.getContacts()) && StringUtils.isEmpty(customer.getContacts())) {
                        customer.setContacts(opportunity.getContacts());
                    }
                    if (StringUtils.isNotEmpty(opportunity.getContactInformation()) && StringUtils.isEmpty(customer.getContactInformation())) {
                        customer.setContactInformation(opportunity.getContactInformation());
                    }
                    if (StringUtils.isNotEmpty(opportunity.getRegion()) && StringUtils.isEmpty(customer.getRegion())) {
                        customer.setRegion(opportunity.getRegion());
                    }
                    if (StringUtils.isNotEmpty(opportunity.getAddress()) && StringUtils.isEmpty(customer.getAddress())) {
                        customer.setAddress(opportunity.getAddress());
                        // 如果实际地址为空，也设置一下
                        if (StringUtils.isEmpty(customer.getActualAddress())) {
                            customer.setActualAddress(opportunity.getAddress());
                        }
                    }
                    // 备注信息合并（如果商机有备注且客户备注为空）
                    if (StringUtils.isNotEmpty(opportunity.getRemarks()) && StringUtils.isEmpty(customer.getBusinessScope())) {
                        customer.setBusinessScope(opportunity.getRemarks());
                    }
                    
                    customer.setUpdateTime(new Date());
                    if (sysUser != null) {
                        customer.setUpdateBy(sysUser.getUsername());
                    }
                    
                    customerService.updateById(customer);
                    log.info("更新已存在客户，公司名称：{}，客户ID：{}", opportunity.getCorporateName(), customer.getId());
                }
            } else {
                // 创建新客户
                customer = new GhCustomer();
                customer.setCorporateName(opportunity.getCorporateName());
                customer.setContacts(opportunity.getContacts());
                customer.setContactInformation(opportunity.getContactInformation());
                customer.setRegion(opportunity.getRegion());
                customer.setAddress(opportunity.getAddress());
                customer.setActualAddress(opportunity.getAddress()); // 实际地址也设置为详细地址
                
                // 如果商机的负责人（chargePerson）有值，可以映射到法人姓名或联系人
                if (StringUtils.isNotEmpty(opportunity.getChargePerson())) {
                    // 如果联系人为空，则使用负责人作为联系人
                    if (StringUtils.isEmpty(customer.getContacts())) {
                        customer.setContacts(opportunity.getChargePerson());
                    }
                }
                
                // 备注信息可以作为经营范围或留空
                if (StringUtils.isNotEmpty(opportunity.getRemarks())) {
                    customer.setBusinessScope(opportunity.getRemarks());
                }
                
                customer.setStatus("1"); // 默认启用状态
                customer.setDelFlag(0);
                customer.setCreateTime(new Date());
                if (sysUser != null) {
                    customer.setCreateBy(sysUser.getUsername());
                }
                
                customerService.save(customer);
                isNew = true;
                log.info("从商机创建客户成功，公司名称：{}，客户ID：{}", opportunity.getCorporateName(), customer.getId());
            }
            
            // 更新商机的customerId（关联到客户）
            opportunity.setCustomerId(customer.getId());
            opportunity.setUpdateTime(new Date());
            if (sysUser != null) {
                opportunity.setUpdateBy(sysUser.getUsername());
            }
            opportunityService.updateById(opportunity);
            
            // 根据情况返回不同的提示信息
            String message;
            if (isNew) {
                message = "转客户成功！已创建新客户";
            } else if (isConvertedByOther) {
                message = "该客户已由其他线索转换，无需重复创建，已关联到现有客户";
            } else {
                message = "转客户成功！已更新现有客户";
            }
            return Result.OK(message, customer.getId());
            
        } catch (Exception e) {
            log.error("转客户失败，线索ID：{}", opportunity != null ? opportunity.getId() : "null", e);
            return Result.error("转客户失败：" + e.getMessage());
        }
    }
    
    /**
     * 从商机信息创建客户记录（内部方法，已废弃，使用convertToCustomer代替）
     * @param opportunity 商机信息
     * @param sysUser 当前登录用户
     */
    @Deprecated
    private void createCustomerFromOpportunity(GhOpportunity opportunity, LoginUser sysUser) {
        try {
            // 检查客户是否已存在（根据公司名称）
            QueryWrapper<GhCustomer> customerQueryWrapper = new QueryWrapper<>();
            customerQueryWrapper.eq("corporate_name", opportunity.getCorporateName());
            customerQueryWrapper.eq("del_flag", 0);
            List<GhCustomer> existingCustomers = customerService.list(customerQueryWrapper);
            
            GhCustomer customer;
            if (existingCustomers != null && !existingCustomers.isEmpty()) {
                // 客户已存在，更新customerId即可
                customer = existingCustomers.get(0);
                opportunity.setCustomerId(customer.getId());
                log.info("客户已存在，公司名称：{}，客户ID：{}", opportunity.getCorporateName(), customer.getId());
            } else {
                // 创建新客户
                customer = new GhCustomer();
                customer.setCorporateName(opportunity.getCorporateName());
                customer.setContacts(opportunity.getContacts());
                customer.setContactInformation(opportunity.getContactInformation());
                customer.setRegion(opportunity.getRegion());
                customer.setAddress(opportunity.getAddress());
                customer.setStatus("1"); // 默认启用状态
                customer.setDelFlag(0);
                customer.setCreateTime(new Date());
                if (sysUser != null) {
                    customer.setCreateBy(sysUser.getUsername());
                }
                
                customerService.save(customer);
                opportunity.setCustomerId(customer.getId());
                log.info("从商机创建客户成功，公司名称：{}，客户ID：{}", opportunity.getCorporateName(), customer.getId());
            }
        } catch (Exception e) {
            log.error("从商机创建客户失败，公司名称：" + opportunity.getCorporateName(), e);
            // 创建客户失败不影响商机保存，只记录日志
        }
    }
    
    /**
     * 获取上级用户名
     */
    private String getSupervisorUsername(String userId) {
        try {
            if (sysUserService != null) {
                org.jeecg.modules.system.entity.SysUser user = sysUserService.getById(userId);
                if (user != null) {
                    // 从用户的直属上级字段获取上级用户名
                    if (StringUtils.isNotEmpty(user.getDirectSupervisor())) {
                        return user.getDirectSupervisor();
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取上级用户名失败", e);
        }
        return null;
    }
}


