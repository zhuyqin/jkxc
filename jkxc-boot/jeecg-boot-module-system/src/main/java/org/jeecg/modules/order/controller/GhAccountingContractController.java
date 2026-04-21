package org.jeecg.modules.order.controller;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhAccountingContract;
import org.jeecg.modules.order.service.IGhAccountingContractService;
import org.jeecg.modules.bankdiary.entity.GhBankDiary;
import org.jeecg.modules.bankdiary.service.IGhBankDiaryService;
import org.jeecg.modules.bank.entity.GhBankManagement;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang.StringUtils;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.SecurityUtils;
import org.jeecg.boot.starter.lock.annotation.JRepeat;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.order.util.AccountingContractServiceStaffJsonUtil;

/**
 * @Description: 合同管理表
 * @Author: jeecg-boot
 * @Date: 2025-01-03
 * @Version: V1.0
 */
@Api(tags="合同管理")
@RestController
@RequestMapping("/order/accountingContract")
@Slf4j
public class GhAccountingContractController extends JeecgController<GhAccountingContract, IGhAccountingContractService> {
	@Autowired
	private IGhAccountingContractService ghAccountingContractService;
	
	@Autowired(required = false)
	private org.jeecg.modules.customer.service.IGhCustomerService ghCustomerService;
	
	@Autowired(required = false)
	private org.jeecg.modules.order.service.IGhOrderService ghOrderService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysCategoryService sysCategoryService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysDictService sysDictService;
	
	@Autowired(required = false)
	private org.jeecg.modules.renew.service.IGhAddressRenewService ghAddressRenewService;
	
	@Autowired(required = false)
	private org.jeecg.modules.bank.service.IGhBankManagementService ghBankManagementService;
	
	@Autowired(required = false)
	private org.jeecg.modules.order.service.IGhOrderOperationLogService ghOrderOperationLogService;
	
	@Autowired(required = false)
	private org.jeecg.modules.bankdiary.service.IGhBankDiaryService ghBankDiaryService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysAuditProcessBindingService sysAuditProcessBindingService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysAuditProcessService sysAuditProcessService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysAuditTaskService sysAuditTaskService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysRoleService sysRoleService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysUserRoleService sysUserRoleService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.mapper.SysUserRoleMapper sysUserRoleMapper;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysUserService sysUserService;

	/** 免登录回填接口口令，与 jeecg.accounting.service-staff-backfill-token 一致；未配置则拒绝公开回填 */
	@org.springframework.beans.factory.annotation.Value("${jeecg.accounting.service-staff-backfill-token:}")
	private String serviceStaffBackfillToken;

	private static final String[] ACCOUNTING_SERVICE_STAFF_KEYS =
			new String[] { "财税主管", "财税顾问", "主办会计" };

	/**
 * 获取各Tab的统计数量
 *
 * @return
 */
@AutoLog(value = "合同管理-获取Tab统计数量")
@ApiOperation(value="合同管理-获取Tab统计数量", notes="合同管理-获取Tab统计数量")
@GetMapping(value = "/tabStatistics")
public Result<?> getTabStatistics() {
    try {
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        java.util.Map<String, Object> scopeCtx = resolveAccountingScopeContext();
        @SuppressWarnings("unchecked")
        java.util.List<String> allowedRealnames = (java.util.List<String>) scopeCtx.get("allowedRealnames");
        String currentRealname = (String) scopeCtx.get("currentRealname");
        
        // 服务中：loss_flag != 1 且没有流失审核任务（与列表相同的数据权限）
        QueryWrapper<GhAccountingContract> servingWrapper = new QueryWrapper<>();
        servingWrapper.eq("del_flag", 0);
        applyAccountingContractServiceStaffDataScope(servingWrapper, allowedRealnames, currentRealname);
        servingWrapper.and(wrapper -> {
            wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
        });
        servingWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
            "INNER JOIN gh_order o ON t.order_id = o.id " +
            "WHERE o.id = gh_accounting_contract.order_id " +
            "AND t.task_type = 'loss' " +
            "AND t.task_status = 'pending' " +
            "AND t.del_flag = 0)");
        counts.put("serving", Long.valueOf(ghAccountingContractService.count(servingWrapper)));
        
        // 当期续费：到期时间在0-30天内（一个月内到期），且服务中
        QueryWrapper<GhAccountingContract> currentRenewalWrapper = new QueryWrapper<>();
        currentRenewalWrapper.eq("del_flag", 0);
        applyAccountingContractServiceStaffDataScope(currentRenewalWrapper, allowedRealnames, currentRealname);
        currentRenewalWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN 0 AND 30");
        currentRenewalWrapper.and(wrapper -> {
            wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
        });
        currentRenewalWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
            "INNER JOIN gh_order o ON t.order_id = o.id " +
            "WHERE o.id = gh_accounting_contract.order_id " +
            "AND t.task_type = 'loss' " +
            "AND t.task_status = 'pending' " +
            "AND t.del_flag = 0)");
        counts.put("currentRenewal", Long.valueOf(ghAccountingContractService.count(currentRenewalWrapper)));
        
        // t-2逾期续费：已过期两个月内（-1到-60天），且服务中
        QueryWrapper<GhAccountingContract> t2OverdueWrapper = new QueryWrapper<>();
        t2OverdueWrapper.eq("del_flag", 0);
        applyAccountingContractServiceStaffDataScope(t2OverdueWrapper, allowedRealnames, currentRealname);
        t2OverdueWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN -60 AND -1");
        t2OverdueWrapper.and(wrapper -> {
            wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
        });
        t2OverdueWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
            "INNER JOIN gh_order o ON t.order_id = o.id " +
            "WHERE o.id = gh_accounting_contract.order_id " +
            "AND t.task_type = 'loss' " +
            "AND t.task_status = 'pending' " +
            "AND t.del_flag = 0)");
        counts.put("t2OverdueRenewal", Long.valueOf(ghAccountingContractService.count(t2OverdueWrapper)));
        
        // t+6预期续费：六个月内到期（0-180天），且服务中
        QueryWrapper<GhAccountingContract> t6ExpectedWrapper = new QueryWrapper<>();
        t6ExpectedWrapper.eq("del_flag", 0);
        applyAccountingContractServiceStaffDataScope(t6ExpectedWrapper, allowedRealnames, currentRealname);
        t6ExpectedWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN 0 AND 180");
        t6ExpectedWrapper.and(wrapper -> {
            wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
        });
        t6ExpectedWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
            "INNER JOIN gh_order o ON t.order_id = o.id " +
            "WHERE o.id = gh_accounting_contract.order_id " +
            "AND t.task_type = 'loss' " +
            "AND t.task_status = 'pending' " +
            "AND t.del_flag = 0)");
        counts.put("t6ExpectedRenewal", Long.valueOf(ghAccountingContractService.count(t6ExpectedWrapper)));
        
        // t-3逾期客户：已过期超过两个月小于一年（-61到-365天），且服务中
        QueryWrapper<GhAccountingContract> t3OverdueCustomerWrapper = new QueryWrapper<>();
        t3OverdueCustomerWrapper.eq("del_flag", 0);
        applyAccountingContractServiceStaffDataScope(t3OverdueCustomerWrapper, allowedRealnames, currentRealname);
        t3OverdueCustomerWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN -365 AND -61");
        t3OverdueCustomerWrapper.and(wrapper -> {
            wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
        });
        t3OverdueCustomerWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
            "INNER JOIN gh_order o ON t.order_id = o.id " +
            "WHERE o.id = gh_accounting_contract.order_id " +
            "AND t.task_type = 'loss' " +
            "AND t.task_status = 'pending' " +
            "AND t.del_flag = 0)");
        counts.put("t3OverdueCustomer", Long.valueOf(ghAccountingContractService.count(t3OverdueCustomerWrapper)));
        
        // 流失审核：与流失审核列表一致，按当前登录人的待审核 loss 任务数统计
        long lossAuditCount = 0L;
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null && sysAuditTaskService != null && sysUserRoleMapper != null) {
                List<String> roleIds = sysUserRoleMapper.getRoleIdByUserName(sysUser.getUsername());
                // 与 /sys/auditTask/getPendingTasks 保持一致：计算数据角色（role_type=2）可见范围（自己+下级）
                List<String> dataRoleIds = new ArrayList<>();
                if (sysRoleService != null && roleIds != null && !roleIds.isEmpty()) {
                    for (String roleId : roleIds) {
                        org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
                        if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
                            List<String> allSubRoleIds = getSubRoleIdsOnly(roleId);
                            dataRoleIds.addAll(allSubRoleIds);
                        }
                    }
                }
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<org.jeecg.modules.system.entity.SysAuditTask> taskPage =
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 1);
                com.baomidou.mybatisplus.core.metadata.IPage<org.jeecg.modules.system.entity.SysAuditTask> pendingLossPage =
                        sysAuditTaskService.getPendingTasks(taskPage, "loss", sysUser.getId(), roleIds, true,
                                null, null, null, null, dataRoleIds);
                if (pendingLossPage != null) {
                    lossAuditCount = pendingLossPage.getTotal();
                }
            }
        } catch (Exception e) {
            log.warn("计算流失审核统计失败，回退为0", e);
        }
        counts.put("lossAudit", lossAuditCount);
        
        // 流失客户：contract_status = 'terminated' 或 loss_flag = 1
        QueryWrapper<GhAccountingContract> lossCustomerWrapper = new QueryWrapper<>();
        lossCustomerWrapper.eq("del_flag", 0);
        applyAccountingContractServiceStaffDataScope(lossCustomerWrapper, allowedRealnames, currentRealname);
        lossCustomerWrapper.and(wrapper -> {
            wrapper.eq("loss_flag", 1).or().eq("contract_status", "terminated");
        });
        counts.put("lossCustomer", Long.valueOf(ghAccountingContractService.count(lossCustomerWrapper)));
        
        return Result.OK(counts);
    } catch (Exception e) {
        log.error("获取Tab统计数量失败", e);
        return Result.error("获取统计数量失败");
    }
}

	/**
	 * 分页列表查询
	 *
	 * @param ghAccountingContract
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "合同管理-分页列表查询")
	@ApiOperation(value="合同管理-分页列表查询", notes="合同管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(GhAccountingContract ghAccountingContract,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(name="tabType", required = false) String tabType,
								   HttpServletRequest req) {
		String servicePersonFilter = ghAccountingContract != null ? ghAccountingContract.getServicePerson() : null;
		if (ghAccountingContract != null) {
			ghAccountingContract.setServicePerson(null);
		}
		java.util.Map<String, String[]> parameterMap = new java.util.HashMap<>(req.getParameterMap());
		parameterMap.remove("servicePerson");
		QueryWrapper<GhAccountingContract> queryWrapper = QueryGenerator.initQueryWrapper(ghAccountingContract, parameterMap);
		queryWrapper.eq("del_flag", 0);
		if (oConvertUtils.isNotEmpty(servicePersonFilter)) {
			final String sp = servicePersonFilter.trim();
			queryWrapper.and(w -> {
				java.util.concurrent.atomic.AtomicBoolean first = new java.util.concurrent.atomic.AtomicBoolean(true);
				appendOrGroupServiceStaffJsonEquals(w, sp, first);
			});
		}
		
		// 服务人员权限：service_staff_json 三类 + 数据角色上下级（与 Tab 统计一致）
		java.util.Map<String, Object> scopeCtx = resolveAccountingScopeContext();
		@SuppressWarnings("unchecked")
		java.util.List<String> allowedRealnames = (java.util.List<String>) scopeCtx.get("allowedRealnames");
		String currentRealname = (String) scopeCtx.get("currentRealname");
		applyAccountingContractServiceStaffDataScope(queryWrapper, allowedRealnames, currentRealname);
		
		// 根据tab类型筛选
		java.util.Date now = new java.util.Date();
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
		String todayStr = dateFormat.format(now);
		
		// 如果没有指定tabType，默认使用"serving"
		// 但如果指定了orderId参数，则不进行tabType过滤（用于流失审核等场景）
		if (oConvertUtils.isEmpty(tabType)) {
			// 检查是否有orderId参数（用于精确查询，不需要tabType过滤）
			String orderId = req.getParameter("orderId");
			if (oConvertUtils.isEmpty(orderId)) {
				tabType = "serving";
			}
			// 如果有orderId，tabType保持为空，跳过所有tabType相关的过滤
		}
		
		if ("serving".equals(tabType)) {
			// 服务中：正常状态（未流失）的单子，排除流失客户和已提交流失申请的合同
			queryWrapper.and(wrapper -> {
				wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
			});
			// 排除有流失审核任务的合同（通过子查询）
			queryWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
				"INNER JOIN gh_order o ON t.order_id = o.id " +
				"WHERE o.id = gh_accounting_contract.order_id " +
				"AND t.task_type = 'loss' " +
				"AND t.task_status = 'pending' " +
				"AND t.del_flag = 0)");
		} else if ("currentRenewal".equals(tabType)) {
			// 当期续费：到期时间在0到30天（一个月内到期）
			queryWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN 0 AND 30");
			queryWrapper.ne("contract_status", "terminated");
			queryWrapper.and(wrapper -> {
				wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
			});
			// 排除有流失审核任务的合同
			queryWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
				"INNER JOIN gh_order o ON t.order_id = o.id " +
				"WHERE o.id = gh_accounting_contract.order_id " +
				"AND t.task_type = 'loss' " +
				"AND t.task_status = 'pending' " +
				"AND t.del_flag = 0)");
		} else if ("t2OverdueRenewal".equals(tabType)) {
			// t-2逾期续费：已过期两个月内（-1到-60天）
			queryWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN -60 AND -1");
			queryWrapper.ne("contract_status", "terminated");
			queryWrapper.and(wrapper -> {
				wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
			});
			// 排除有流失审核任务的合同
			queryWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
				"INNER JOIN gh_order o ON t.order_id = o.id " +
				"WHERE o.id = gh_accounting_contract.order_id " +
				"AND t.task_type = 'loss' " +
				"AND t.task_status = 'pending' " +
				"AND t.del_flag = 0)");
		} else if ("t6ExpectedRenewal".equals(tabType)) {
			// t+6预期续费：六个月内到期（0-180天）
			queryWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN 0 AND 180");
			queryWrapper.ne("contract_status", "terminated");
			queryWrapper.and(wrapper -> {
				wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
			});
			// 排除有流失审核任务的合同
			queryWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
				"INNER JOIN gh_order o ON t.order_id = o.id " +
				"WHERE o.id = gh_accounting_contract.order_id " +
				"AND t.task_type = 'loss' " +
				"AND t.task_status = 'pending' " +
				"AND t.del_flag = 0)");
		} else if ("t3OverdueCustomer".equals(tabType)) {
			// t-3逾期客户：已过期超过两个月小于一年（-61到-365天）
			queryWrapper.apply("DATEDIFF(expire_date, CURDATE()) BETWEEN -365 AND -61");
			queryWrapper.ne("contract_status", "terminated");
			queryWrapper.and(wrapper -> {
				wrapper.ne("loss_flag", 1).or().isNull("loss_flag");
			});
			// 排除有流失审核任务的合同
			queryWrapper.apply("NOT EXISTS (SELECT 1 FROM sys_audit_task t " +
				"INNER JOIN gh_order o ON t.order_id = o.id " +
				"WHERE o.id = gh_accounting_contract.order_id " +
				"AND t.task_type = 'loss' " +
				"AND t.task_status = 'pending' " +
				"AND t.del_flag = 0)");
		} else if ("lossAudit".equals(tabType)) {
			// 流失审核：有待审核的流失任务
			queryWrapper.apply("EXISTS (SELECT 1 FROM sys_audit_task t " +
				"INNER JOIN gh_order o ON t.order_id = o.id " +
				"WHERE o.id = gh_accounting_contract.order_id " +
				"AND t.task_type = 'loss' " +
				"AND t.task_status = 'pending' " +
				"AND t.del_flag = 0)");
		} else if ("lossCustomer".equals(tabType)) {
			// 流失客户：流失标志为1或合同状态为已终止
			queryWrapper.and(wrapper -> {
				wrapper.eq("loss_flag", 1).or().eq("contract_status", "terminated");
			});
		}
		
		queryWrapper.orderByDesc("create_time");
		Page<GhAccountingContract> page = new Page<GhAccountingContract>(pageNo, pageSize);
		IPage<GhAccountingContract> pageList = ghAccountingContractService.page(page, queryWrapper);
		
		// 为每个合同计算消费金额、业务标签、关联企业（从客户表获取）和倒计天数
		// 优化：使用批量查询避免N+1问题
		if (pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
			// 第一步：收集所有公司名称和计算倒计天数
			java.util.Set<String> companyNames = new java.util.HashSet<>();
			for (GhAccountingContract contract : pageList.getRecords()) {
				// 计算倒计天数（无论是否找到客户都要计算）
				if (contract.getExpireDate() != null) {
					long diffInMillies = contract.getExpireDate().getTime() - System.currentTimeMillis();
					long diff = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
					contract.setCountdownDays((int) diff);
				}
				
				if (oConvertUtils.isNotEmpty(contract.getCompanyName())) {
					companyNames.add(contract.getCompanyName());
				}
			}
			
			// 第二步：批量查询所有客户（1次查询）
			java.util.Map<String, org.jeecg.modules.customer.entity.GhCustomer> customerMap = new java.util.HashMap<>();
			if (!companyNames.isEmpty() && ghCustomerService != null) {
				com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.customer.entity.GhCustomer> customerWrapper = 
					new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
				customerWrapper.in("corporate_name", companyNames);
				customerWrapper.eq("del_flag", 0);
				
				java.util.List<org.jeecg.modules.customer.entity.GhCustomer> customers = ghCustomerService.list(customerWrapper);
				for (org.jeecg.modules.customer.entity.GhCustomer customer : customers) {
					customerMap.put(customer.getCorporateName(), customer);
				}
			}
			
			// 第三步：收集所有客户ID
			java.util.Set<String> customerIds = new java.util.HashSet<>();
			for (org.jeecg.modules.customer.entity.GhCustomer customer : customerMap.values()) {
				if (oConvertUtils.isNotEmpty(customer.getId())) {
					customerIds.add(customer.getId());
				}
			}
			
			// 第四步：批量查询所有订单（1次查询）
			java.util.Map<String, java.util.List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomerId = new java.util.HashMap<>();
			if (!customerIds.isEmpty() && ghOrderService != null) {
				com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = 
					new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
				orderWrapper.in("company_id", customerIds);
				orderWrapper.eq("del_flag", 0);
				
				java.util.List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(orderWrapper);
				for (org.jeecg.modules.order.entity.GhOrder order : allOrders) {
					String customerId = order.getCompanyId();
					if (oConvertUtils.isNotEmpty(customerId)) {
						ordersByCustomerId.computeIfAbsent(customerId, k -> new java.util.ArrayList<>()).add(order);
					}
				}
			}
			
			// 第五步：收集所有业务类型ID并批量查询字典（1次查询）
			java.util.Set<String> allBusinessTypeIds = new java.util.HashSet<>();
			for (java.util.List<org.jeecg.modules.order.entity.GhOrder> orders : ordersByCustomerId.values()) {
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
						allBusinessTypeIds.add(order.getBusinessType());
					}
				}
			}
			
			java.util.Map<String, String> businessTypeNameMap = new java.util.HashMap<>();
			if (!allBusinessTypeIds.isEmpty() && sysCategoryService != null) {
				String ids = String.join(",", allBusinessTypeIds);
				java.util.List<String> businessTypeNames = sysCategoryService.loadDictItem(ids, false);
				// 假设返回的名称顺序与ID顺序一致
				java.util.List<String> idList = new java.util.ArrayList<>(allBusinessTypeIds);
				for (int i = 0; i < idList.size() && i < businessTypeNames.size(); i++) {
					businessTypeNameMap.put(idList.get(i), businessTypeNames.get(i));
				}
			}
			
			// 第六步：为每个合同计算字段（使用缓存的数据，无需额外查询）
			for (GhAccountingContract contract : pageList.getRecords()) {
				if (oConvertUtils.isNotEmpty(contract.getCompanyName())) {
					org.jeecg.modules.customer.entity.GhCustomer customer = customerMap.get(contract.getCompanyName());
					if (customer != null) {
						// 使用批量查询的数据计算客户字段
						calculateCustomerFieldsFromCache(contract, customer, ordersByCustomerId, businessTypeNameMap);
					}
				}
			}

			// 第七步：批量加载关联订单与审核任务；签单人员/业务员供列表「业务人员」展示（与地址管理口径一致）
			java.util.List<String> orderIdsForAudit = new java.util.ArrayList<>();
			for (GhAccountingContract contract : pageList.getRecords()) {
				if (oConvertUtils.isNotEmpty(contract.getOrderId())) {
					orderIdsForAudit.add(contract.getOrderId());
				}
			}
			java.util.Map<String, org.jeecg.modules.order.entity.GhOrder> orderByIdForList =
				new java.util.HashMap<>();
			if (!orderIdsForAudit.isEmpty() && ghOrderService != null) {
				java.util.List<String> distinctOrderIds = new java.util.ArrayList<>(
					new java.util.LinkedHashSet<>(orderIdsForAudit));
				java.util.List<org.jeecg.modules.order.entity.GhOrder> ordersForList =
					ghOrderService.listByIds(distinctOrderIds);
				if (ordersForList != null) {
					for (org.jeecg.modules.order.entity.GhOrder o : ordersForList) {
						if (o != null && oConvertUtils.isNotEmpty(o.getId())) {
							orderByIdForList.put(o.getId(), o);
						}
					}
				}
			}
			java.util.Map<String, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> auditTasksByOrder =
				new java.util.HashMap<>();
			if (!orderIdsForAudit.isEmpty() && sysAuditTaskService != null) {
				auditTasksByOrder = sysAuditTaskService.getTasksByOrderIds(orderIdsForAudit);
			}
			for (GhAccountingContract contract : pageList.getRecords()) {
				if (oConvertUtils.isNotEmpty(contract.getOrderId())) {
					org.jeecg.modules.order.entity.GhOrder o = orderByIdForList.get(contract.getOrderId());
					if (o != null) {
						contract.setSigner(o.getSalesman());
					}
				}
				if (oConvertUtils.isEmpty(contract.getOrderId())) {
					contract.setAuditStepSummary(new java.util.ArrayList<>());
					continue;
				}
				java.util.List<org.jeecg.modules.system.entity.SysAuditTask> auditTasks =
					auditTasksByOrder.getOrDefault(contract.getOrderId(), java.util.Collections.emptyList());
				contract.setAuditStepSummary(buildContractAuditStepSummaryForList(auditTasks));
			}
		}
		
		return Result.OK(pageList);
	}

	/**
	 * 代账列表数据权限仅依据 service_staff_json 三类岗位姓名；与 allowedRealnames 中每名做 OR。
	 */
	private void appendOrGroupServiceStaffJsonEquals(
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GhAccountingContract> w,
			String rn,
			java.util.concurrent.atomic.AtomicBoolean firstSegment) {
		if (oConvertUtils.isEmpty(rn)) {
			return;
		}
		for (String key : ACCOUNTING_SERVICE_STAFF_KEYS) {
			if (firstSegment.getAndSet(false)) {
				w.apply(
						"JSON_UNQUOTE(JSON_EXTRACT(IFNULL(service_staff_json,'{}'), '$.\"" + key + "\"')) = {0}",
						rn);
			} else {
				w.or().apply(
						"JSON_UNQUOTE(JSON_EXTRACT(IFNULL(service_staff_json,'{}'), '$.\"" + key + "\"')) = {0}",
						rn);
			}
		}
	}

	private void applyAccountingListDataScopeByRealnames(
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GhAccountingContract> queryWrapper,
			java.util.List<String> allowedRealnames) {
		if (allowedRealnames == null || allowedRealnames.isEmpty()) {
			return;
		}
		queryWrapper.and(w -> {
			java.util.concurrent.atomic.AtomicBoolean first = new java.util.concurrent.atomic.AtomicBoolean(true);
			for (String rn : allowedRealnames) {
				if (oConvertUtils.isEmpty(rn)) {
					continue;
				}
				appendOrGroupServiceStaffJsonEquals(w, rn, first);
			}
		});
	}

	private void applyAccountingListDataScopeSingleRealname(
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GhAccountingContract> queryWrapper,
			String rn) {
		if (oConvertUtils.isEmpty(rn)) {
			return;
		}
		queryWrapper.and(w -> {
			java.util.concurrent.atomic.AtomicBoolean first = new java.util.concurrent.atomic.AtomicBoolean(true);
			appendOrGroupServiceStaffJsonEquals(w, rn, first);
		});
	}

	/**
	 * 与列表、Tab 统计共用：仅按 service_staff_json 三类岗位姓名 + 数据角色下级姓名过滤。
	 */
	private void applyAccountingContractServiceStaffDataScope(
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GhAccountingContract> queryWrapper) {
		java.util.Map<String, Object> scopeCtx = resolveAccountingScopeContext();
		@SuppressWarnings("unchecked")
		java.util.List<String> allowedRealnames = (java.util.List<String>) scopeCtx.get("allowedRealnames");
		String currentRealname = (String) scopeCtx.get("currentRealname");
		applyAccountingContractServiceStaffDataScope(queryWrapper, allowedRealnames, currentRealname);
	}

	private void applyAccountingContractServiceStaffDataScope(
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GhAccountingContract> queryWrapper,
			java.util.List<String> allowedRealnames,
			String currentRealname) {
		if (allowedRealnames != null && !allowedRealnames.isEmpty()) {
			applyAccountingListDataScopeByRealnames(queryWrapper, allowedRealnames);
			return;
		}
		applyAccountingListDataScopeSingleRealname(queryWrapper, currentRealname);
	}

	/**
	 * 仅解析一次数据权限范围（自己 + 数据角色下级），供列表和统计复用，减少重复查询。
	 */
	private java.util.Map<String, Object> resolveAccountingScopeContext() {
		java.util.Map<String, Object> ctx = new java.util.HashMap<>();
		try {
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			String currentRealname = sysUser != null ? sysUser.getRealname() : null;
			ctx.put("currentRealname", currentRealname);
			if (sysUser != null && sysUserRoleMapper != null && sysRoleService != null && sysUserRoleService != null) {
				List<String> userRoleIds = sysUserRoleMapper.getRoleIdByUserName(sysUser.getUsername());
				if (userRoleIds != null && !userRoleIds.isEmpty()) {
					List<String> subRoleIds = new ArrayList<>();
					for (String roleId : userRoleIds) {
						org.jeecg.modules.system.entity.SysRole role = sysRoleService.getById(roleId);
						if (role != null && role.getRoleType() != null && role.getRoleType() == 2) {
							List<String> allSubRoleIds = getSubRoleIdsOnly(roleId);
							subRoleIds.addAll(allSubRoleIds);
						}
					}
					java.util.Set<String> allowedSet = new java.util.LinkedHashSet<>();
					if (oConvertUtils.isNotEmpty(sysUser.getRealname())) {
						allowedSet.add(sysUser.getRealname());
					}
					if (!subRoleIds.isEmpty()) {
						List<String> subUserIds = getUserIdsByRoleIds(subRoleIds);
						if (subUserIds != null && !subUserIds.isEmpty() && sysUserService != null) {
							com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser> userQueryWrapper =
									new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
							userQueryWrapper.in(org.jeecg.modules.system.entity.SysUser::getId, subUserIds);
							userQueryWrapper.eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0);
							userQueryWrapper.and(wrapper -> wrapper.eq(org.jeecg.modules.system.entity.SysUser::getStatus, 1)
									.or().isNull(org.jeecg.modules.system.entity.SysUser::getStatus));
							List<org.jeecg.modules.system.entity.SysUser> users = sysUserService.list(userQueryWrapper);
							if (users != null && !users.isEmpty()) {
								for (org.jeecg.modules.system.entity.SysUser user : users) {
									if (oConvertUtils.isNotEmpty(user.getRealname())) {
										allowedSet.add(user.getRealname());
									}
								}
							}
						}
					}
					java.util.List<String> allowedRealnames = new java.util.ArrayList<>(allowedSet);
					ctx.put("allowedRealnames", allowedRealnames);
					return ctx;
				}
			}
		} catch (Exception e) {
			log.warn("解析服务人员权限上下文失败，使用默认过滤（只显示自己的）", e);
		}
		ctx.put("allowedRealnames", new java.util.ArrayList<>());
		return ctx;
	}

	/** 列表用审核步骤行（与地址中心 GhAddressCenter 列表口径一致） */
	private java.util.List<java.util.Map<String, Object>> buildContractAuditStepSummaryForList(
			java.util.List<org.jeecg.modules.system.entity.SysAuditTask> tasks) {
		java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
		if (tasks == null || tasks.isEmpty()) {
			return rows;
		}
		java.util.Map<Integer, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> byStep =
			tasks.stream().collect(java.util.stream.Collectors.groupingBy(
				org.jeecg.modules.system.entity.SysAuditTask::getStepOrder));
		java.util.List<Integer> stepOrders = new java.util.ArrayList<>(byStep.keySet());
		java.util.Collections.sort(stepOrders);
		for (Integer so : stepOrders) {
			java.util.List<org.jeecg.modules.system.entity.SysAuditTask> stepTasks = byStep.get(so);
			if (stepTasks == null) {
				continue;
			}
			String stepStatus = aggregateContractAuditStepStatus(stepTasks);
			for (org.jeecg.modules.system.entity.SysAuditTask t : stepTasks) {
				java.util.Map<String, Object> row = new java.util.HashMap<>();
				row.put("stepOrder", so);
				row.put("stepStatus", stepStatus);
				row.put("roleName", t.getCurrentRoleName());
				row.put("personText", formatContractAuditTaskPersonForList(t));
				rows.add(row);
			}
		}
		return rows;
	}

	private String aggregateContractAuditStepStatus(java.util.List<org.jeecg.modules.system.entity.SysAuditTask> stepTasks) {
		if (stepTasks == null || stepTasks.isEmpty()) {
			return "unknown";
		}
		boolean allApproved = stepTasks.stream()
			.allMatch(t -> "approved".equals(t.getTaskStatus()));
		boolean hasRejected = stepTasks.stream()
			.anyMatch(t -> "rejected".equals(t.getTaskStatus()));
		boolean hasPending = stepTasks.stream()
			.anyMatch(t -> "pending".equals(t.getTaskStatus()));
		if (allApproved) {
			return "approved";
		}
		if (hasRejected) {
			return "rejected";
		}
		if (hasPending) {
			return "pending";
		}
		return "unknown";
	}

	private String formatContractAuditTaskPersonForList(org.jeecg.modules.system.entity.SysAuditTask task) {
		if (task == null) {
			return "-";
		}
		String st = task.getTaskStatus();
		if ("pending".equals(st)) {
			return oConvertUtils.isNotEmpty(task.getAssignedUserName())
				? "待审 " + task.getAssignedUserName()
				: "待审";
		}
		if ("approved".equals(st)) {
			return oConvertUtils.isNotEmpty(task.getAuditUserName())
				? task.getAuditUserName()
				: "已通过";
		}
		if ("rejected".equals(st)) {
			return oConvertUtils.isNotEmpty(task.getAuditUserName())
				? task.getAuditUserName()
				: "已驳回";
		}
		return oConvertUtils.isNotEmpty(task.getAuditUserName()) ? task.getAuditUserName() : "-";
	}
	
	/**
	 * 为合同计算客户相关字段（使用缓存数据，避免N+1查询）
	 * @param contract 合同对象
	 * @param customer 客户对象
	 * @param ordersByCustomerId 按客户ID分组的订单Map
	 * @param businessTypeNameMap 业务类型ID到名称的映射
	 */
	private void calculateCustomerFieldsFromCache(
			GhAccountingContract contract, 
			org.jeecg.modules.customer.entity.GhCustomer customer,
			java.util.Map<String, java.util.List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomerId,
			java.util.Map<String, String> businessTypeNameMap) {
		try {
			String customerId = customer.getId();
			java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ordersByCustomerId.get(customerId);
			if (orders == null) {
				orders = new java.util.ArrayList<>();
			}
			
			// 1. 消费金额：公司全部订单的金额总和
			java.math.BigDecimal totalSpending = java.math.BigDecimal.ZERO;
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				java.math.BigDecimal amount = order.getContractAmount() != null ? 
					order.getContractAmount() : 
					(order.getOrderAmount() != null ? order.getOrderAmount() : java.math.BigDecimal.ZERO);
				totalSpending = totalSpending.add(amount);
			}
			contract.setTotalSpending(totalSpending);
			
			// 2. 业务标签：公司所有业务类型（去重，逗号分隔）
			java.util.Set<String> businessTypeNames = new java.util.HashSet<>();
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
					String typeName = businessTypeNameMap.get(order.getBusinessType());
					if (oConvertUtils.isNotEmpty(typeName)) {
						businessTypeNames.add(typeName);
					} else {
						businessTypeNames.add(order.getBusinessType());
					}
				}
			}
			if (!businessTypeNames.isEmpty()) {
				contract.setBusinessTag(String.join(",", businessTypeNames));
			}
			
			// 3. 关联企业：直接从客户表获取
			contract.setRelatedCompanyName(customer.getRelatedCompanyName());
			
			// 4. 累计时间（服务月数）：从最早订单创建时间到现在
			int serviceMonths = 0;
			java.util.Date earliestDate = null;
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				if (order.getCreateTime() != null) {
					if (earliestDate == null || order.getCreateTime().before(earliestDate)) {
						earliestDate = order.getCreateTime();
					}
				}
			}
			if (earliestDate != null) {
				java.util.Date now = new java.util.Date();
				java.util.Calendar startCal = java.util.Calendar.getInstance();
				startCal.setTime(earliestDate);
				java.util.Calendar endCal = java.util.Calendar.getInstance();
				endCal.setTime(now);
				
				int yearDiff = endCal.get(java.util.Calendar.YEAR) - startCal.get(java.util.Calendar.YEAR);
				int monthDiff = endCal.get(java.util.Calendar.MONTH) - startCal.get(java.util.Calendar.MONTH);
				serviceMonths = Math.max(0, yearDiff * 12 + monthDiff);
			}
			contract.setServiceMonths(serviceMonths);
			
			// 5. 订单数量：用于显示复购信息
			int orderCount = orders.size();
			contract.setCustomerOrderCount(orderCount);
			
			// 6. 判断是否有高端业务和科小业务
			boolean hasHighEndBusiness = false;
			boolean hasKeXiaoBusiness = false;
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
					String typeName = businessTypeNameMap.get(order.getBusinessType());
					if (oConvertUtils.isNotEmpty(typeName)) {
						if (typeName.contains("高端代账") || typeName.contains("高新")) {
							hasHighEndBusiness = true;
						}
						if (typeName.contains("科小")) {
							hasKeXiaoBusiness = true;
						}
					}
				}
			}
			
			// 7. 企业等级：根据消费金额、购买次数、服务时间、高端业务、科小业务计算
			String enterpriseLevel = calculateEnterpriseLevel(totalSpending, orderCount, serviceMonths, hasHighEndBusiness, hasKeXiaoBusiness);
			contract.setEnterpriseLevel(enterpriseLevel);
		} catch (Exception e) {
			log.error("为合同计算客户字段失败（使用缓存），合同ID: " + contract.getId(), e);
		}
	}
	
	/**
	 * 为合同计算客户相关字段：消费金额、业务标签、关联企业、累计时间、企业等级、订单数量
	 * 注意：倒计天数已在queryPageList中统一计算，这里不再计算
	 */
	private void calculateCustomerFieldsForContract(GhAccountingContract contract, org.jeecg.modules.customer.entity.GhCustomer customer) {
		try {
			// 1. 消费金额：公司全部订单的金额总和
			java.math.BigDecimal totalSpending = calculateTotalSpending(customer.getCorporateName(), customer.getId());
			contract.setTotalSpending(totalSpending);
			
			// 2. 业务标签：公司所有业务类型（去重，逗号分隔）
			String businessTag = calculateBusinessTag(customer.getCorporateName(), customer.getId());
			contract.setBusinessTag(businessTag);
			
			// 3. 关联企业：直接从客户表获取
			contract.setRelatedCompanyName(customer.getRelatedCompanyName());
			
			// 4. 累计时间（服务月数）：从最早订单创建时间到现在
			int serviceMonths = getServiceMonths(customer.getId());
			contract.setServiceMonths(serviceMonths);
			
			// 5. 订单数量：用于显示复购信息
			int orderCount = getPurchaseCount(customer.getCorporateName(), customer.getId());
			contract.setCustomerOrderCount(orderCount);
			
			// 6. 企业等级：根据消费金额、购买次数、服务时间、高端业务、科小业务计算
			int purchaseCount = orderCount; // 购买次数就是订单数量
			boolean hasHighEndBusiness = hasHighEndBusiness(customer.getCorporateName(), customer.getId());
			boolean hasKeXiaoBusiness = hasKeXiaoBusiness(customer.getCorporateName(), customer.getId());
			String enterpriseLevel = calculateEnterpriseLevel(totalSpending, purchaseCount, serviceMonths, hasHighEndBusiness, hasKeXiaoBusiness);
			contract.setEnterpriseLevel(enterpriseLevel);
		} catch (Exception e) {
			log.error("为合同计算客户字段失败，合同ID: " + contract.getId(), e);
		}
	}
	
	/**
	 * 计算消费金额：公司全部订单的金额总和
	 */
	private java.math.BigDecimal calculateTotalSpending(String companyName, String customerId) {
		try {
			if (ghOrderService == null) {
				return java.math.BigDecimal.ZERO;
			}
			
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			if (oConvertUtils.isNotEmpty(customerId)) {
				wrapper.eq("company_id", customerId);
			} else if (oConvertUtils.isNotEmpty(companyName)) {
				wrapper.eq("company_name", companyName);
			} else {
				return java.math.BigDecimal.ZERO;
			}
			
			java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(wrapper);
			java.math.BigDecimal total = java.math.BigDecimal.ZERO;
			
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				java.math.BigDecimal amount = order.getContractAmount() != null ? 
					order.getContractAmount() : 
					(order.getOrderAmount() != null ? order.getOrderAmount() : java.math.BigDecimal.ZERO);
				total = total.add(amount);
			}
			
			return total;
		} catch (Exception e) {
			log.error("计算消费金额失败", e);
			return java.math.BigDecimal.ZERO;
		}
	}
	
	/**
	 * 计算业务标签：公司所有业务类型（去重，逗号分隔）
	 */
	private String calculateBusinessTag(String companyName, String customerId) {
		try {
			if (ghOrderService == null) {
				return null;
			}
			
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			wrapper.isNotNull("business_type");
			wrapper.ne("business_type", "");
			if (oConvertUtils.isNotEmpty(customerId)) {
				wrapper.eq("company_id", customerId);
			} else if (oConvertUtils.isNotEmpty(companyName)) {
				wrapper.eq("company_name", companyName);
			} else {
				return null;
			}
			
			java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(wrapper);
			java.util.Set<String> businessTypeIds = new java.util.HashSet<>();
			
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
					businessTypeIds.add(order.getBusinessType());
				}
			}
			
			if (businessTypeIds.isEmpty()) {
				return null;
			}
			
			// 如果有字典服务，将业务类型ID转换为名称
			if (sysCategoryService != null) {
				String ids = String.join(",", businessTypeIds);
				java.util.List<String> businessTypeNames = sysCategoryService.loadDictItem(ids, false);
				return String.join(",", businessTypeNames);
			} else {
				return String.join(",", businessTypeIds);
			}
		} catch (Exception e) {
			log.error("计算业务标签失败", e);
			return null;
		}
	}
	
	/**
	 * 获取购买次数：该公司的订单数量
	 */
	private int getPurchaseCount(String companyName, String customerId) {
		try {
			if (ghOrderService == null) {
				return 0;
			}
			
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			if (oConvertUtils.isNotEmpty(customerId)) {
				wrapper.eq("company_id", customerId);
			} else if (oConvertUtils.isNotEmpty(companyName)) {
				wrapper.eq("company_name", companyName);
			} else {
				return 0;
			}
			
			return (int) ghOrderService.count(wrapper);
		} catch (Exception e) {
			log.error("获取购买次数失败，公司名称: " + companyName + ", 客户ID: " + customerId, e);
			return 0;
		}
	}
	
	/**
	 * 获取服务时间（月数）：从最早订单创建时间到现在
	 */
	private int getServiceMonths(String customerId) {
		try {
			if (ghOrderService == null || oConvertUtils.isEmpty(customerId)) {
				return 0;
			}
			
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			wrapper.eq("company_id", customerId);
			wrapper.orderByAsc("create_time");
			wrapper.last("LIMIT 1");
			
			org.jeecg.modules.order.entity.GhOrder earliestOrder = ghOrderService.getOne(wrapper);
			if (earliestOrder == null || earliestOrder.getCreateTime() == null) {
				return 0;
			}
			
			java.util.Date earliestDate = earliestOrder.getCreateTime();
			java.util.Date now = new java.util.Date();
			
			// 计算月份差（使用Calendar更准确）
			java.util.Calendar startCal = java.util.Calendar.getInstance();
			startCal.setTime(earliestDate);
			java.util.Calendar endCal = java.util.Calendar.getInstance();
			endCal.setTime(now);
			
			int yearDiff = endCal.get(java.util.Calendar.YEAR) - startCal.get(java.util.Calendar.YEAR);
			int monthDiff = endCal.get(java.util.Calendar.MONTH) - startCal.get(java.util.Calendar.MONTH);
			int totalMonths = yearDiff * 12 + monthDiff;
			
			return Math.max(0, totalMonths);
		} catch (Exception e) {
			log.error("获取服务时间失败，客户ID: " + customerId, e);
			return 0;
		}
	}
	
	/**
	 * 判断是否有高端业务：包含"高端代账"或"高新"
	 */
	private boolean hasHighEndBusiness(String companyName, String customerId) {
		try {
			if (ghOrderService == null || sysCategoryService == null) {
				return false;
			}
			
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			wrapper.isNotNull("business_type");
			wrapper.ne("business_type", "");
			if (oConvertUtils.isNotEmpty(customerId)) {
				wrapper.eq("company_id", customerId);
			} else if (oConvertUtils.isNotEmpty(companyName)) {
				wrapper.eq("company_name", companyName);
			} else {
				return false;
			}
			
			java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(wrapper);
			if (orders == null || orders.isEmpty()) {
				return false;
			}
			
			// 检查是否有业务类型名称包含"高端代账"或"高新"
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
					// 通过字典服务查询业务类型名称
					java.util.List<String> names = sysCategoryService.loadDictItem(order.getBusinessType(), false);
					if (names != null && !names.isEmpty()) {
						String businessTypeName = names.get(0);
						if (businessTypeName != null && (businessTypeName.contains("高端代账") || businessTypeName.contains("高新"))) {
							return true;
						}
					}
				}
			}
			
			return false;
		} catch (Exception e) {
			log.error("判断高端业务失败，公司名称: " + companyName + ", 客户ID: " + customerId, e);
			return false;
		}
	}
	
	/**
	 * 判断是否有科小业务：包含"科小"
	 */
	private boolean hasKeXiaoBusiness(String companyName, String customerId) {
		try {
			if (ghOrderService == null || sysCategoryService == null) {
				return false;
			}
			
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.order.entity.GhOrder> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			wrapper.isNotNull("business_type");
			wrapper.ne("business_type", "");
			if (oConvertUtils.isNotEmpty(customerId)) {
				wrapper.eq("company_id", customerId);
			} else if (oConvertUtils.isNotEmpty(companyName)) {
				wrapper.eq("company_name", companyName);
			} else {
				return false;
			}
			
			java.util.List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(wrapper);
			if (orders == null || orders.isEmpty()) {
				return false;
			}
			
			// 检查是否有业务类型名称包含"科小"
			for (org.jeecg.modules.order.entity.GhOrder order : orders) {
				if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
					// 通过字典服务查询业务类型名称
					java.util.List<String> names = sysCategoryService.loadDictItem(order.getBusinessType(), false);
					if (names != null && !names.isEmpty()) {
						String businessTypeName = names.get(0);
						if (businessTypeName != null && businessTypeName.contains("科小")) {
							return true;
						}
					}
				}
			}
			
			return false;
		} catch (Exception e) {
			log.error("判断科小业务失败，公司名称: " + companyName + ", 客户ID: " + customerId, e);
			return false;
		}
	}
	
	/**
	 * 计算企业等级
	 * 一星：消费金额>=2000 或 购买次数>=5 或 服务时间>=12个月
	 * 二星：消费金额>=8000 或 购买次数>=10 或 服务时间>=24个月 或 有科小
	 * 三星：消费金额>=16000 或 购买次数>=15 或 服务时间>=36个月 或 有科小
	 * 四星：消费金额>=32000 或 购买次数>=20 或 服务时间>=48个月 或 有高端代账/高新
	 * 五星：消费金额>=70000 或 购买次数>=21 或 服务时间>=60个月 或 有高端代账/高新
	 */
	private String calculateEnterpriseLevel(java.math.BigDecimal totalSpending, int purchaseCount, int serviceMonths, boolean hasHighEndBusiness, boolean hasKeXiaoBusiness) {
		try {
			double spending = totalSpending != null ? totalSpending.doubleValue() : 0.0;
			int maxLevel = 0;
			
			// 判断消费金额等级（消费金额低于2000的全部按一星处理）
			if (spending >= 70000) {
				maxLevel = Math.max(maxLevel, 5);
			} else if (spending >= 32000) {
				maxLevel = Math.max(maxLevel, 4);
			} else if (spending >= 16000) {
				maxLevel = Math.max(maxLevel, 3);
			} else if (spending >= 8000) {
				maxLevel = Math.max(maxLevel, 2);
			} else if (spending >= 2000) {
				maxLevel = Math.max(maxLevel, 1);
			} else if (spending > 0) {
				// 消费金额低于2000的全部按一星处理
				maxLevel = Math.max(maxLevel, 1);
			}
			
			// 判断购买次数等级
			if (purchaseCount >= 21) {
				maxLevel = Math.max(maxLevel, 5);
			} else if (purchaseCount >= 20) {
				maxLevel = Math.max(maxLevel, 4);
			} else if (purchaseCount >= 15) {
				maxLevel = Math.max(maxLevel, 3);
			} else if (purchaseCount >= 10) {
				maxLevel = Math.max(maxLevel, 2);
			} else if (purchaseCount >= 5) {
				maxLevel = Math.max(maxLevel, 1);
			}
			
			// 判断服务时间等级
			if (serviceMonths >= 60) {
				maxLevel = Math.max(maxLevel, 5);
			} else if (serviceMonths >= 48) {
				maxLevel = Math.max(maxLevel, 4);
			} else if (serviceMonths >= 36) {
				maxLevel = Math.max(maxLevel, 3);
			} else if (serviceMonths >= 24) {
				maxLevel = Math.max(maxLevel, 2);
			} else if (serviceMonths >= 12) {
				maxLevel = Math.max(maxLevel, 1);
			}
			
			// 判断科小业务等级（有科小业务至少三星）
			if (hasKeXiaoBusiness) {
				maxLevel = Math.max(maxLevel, 3);
			}
			
			// 判断高端业务等级（有高端业务至少四星）
			if (hasHighEndBusiness) {
				maxLevel = Math.max(maxLevel, 4);
			}
			
			// 返回星级（转换为字典值：1-一星，2-二星，3-三星，4-四星，5-五星）
			switch (maxLevel) {
				case 5: return "5";
				case 4: return "4";
				case 3: return "3";
				case 2: return "2";
				case 1: return "1";
				default: return null;
			}
		} catch (Exception e) {
			log.error("计算企业等级失败", e);
			return null;
		}
	}
	
	/**
	 *   添加
	 *
	 * @param ghAccountingContract
	 * @return
	 */
	@AutoLog(value = "合同管理-添加")
	@ApiOperation(value="合同管理-添加", notes="合同管理-添加")
	@PostMapping(value = "/add")
	@JRepeat(lockTime = 5, lockKey = "#ghAccountingContract.contractNo")
	public Result<?> add(@RequestBody GhAccountingContract ghAccountingContract) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (sysUser != null) {
			ghAccountingContract.setCreateBy(sysUser.getUsername());
		}
		ghAccountingContractService.save(ghAccountingContract);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param ghAccountingContract
	 * @return
	 */
	@AutoLog(value = "合同管理-编辑")
	@ApiOperation(value="合同管理-编辑", notes="合同管理-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
	@JRepeat(lockTime = 5, lockKey = "#ghAccountingContract.id")
	public Result<?> edit(@RequestBody GhAccountingContract ghAccountingContract) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (sysUser != null) {
			ghAccountingContract.setUpdateBy(sysUser.getUsername());
		}
		ghAccountingContractService.updateById(ghAccountingContract);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "合同管理-通过id删除")
	@ApiOperation(value="合同管理-通过id删除", notes="合同管理-通过id删除")
	@DeleteMapping(value = "/delete")
	@JRepeat(lockTime = 5, lockKey = "#id")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		GhAccountingContract contract = ghAccountingContractService.getById(id);
		if (contract != null) {
			contract.setDelFlag(1);
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (sysUser != null) {
				contract.setUpdateBy(sysUser.getUsername());
			}
			ghAccountingContractService.updateById(contract);
		}
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "合同管理-批量删除")
	@ApiOperation(value="合同管理-批量删除", notes="合同管理-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	@JRepeat(lockTime = 5, lockKey = "#ids")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ghAccountingContractService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "合同管理-通过id查询")
	@ApiOperation(value="合同管理-通过id查询", notes="合同管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		GhAccountingContract ghAccountingContract = ghAccountingContractService.getById(id);
		if(ghAccountingContract==null) {
			return Result.error("未找到对应数据");
		}
		
		// 计算倒计天数
		if (ghAccountingContract.getExpireDate() != null) {
			long diffInMillies = ghAccountingContract.getExpireDate().getTime() - System.currentTimeMillis();
			long diff = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
			ghAccountingContract.setCountdownDays((int) diff);
		}
		
		// 从订单中获取签单人员、来源、产品名称等信息
		if (oConvertUtils.isNotEmpty(ghAccountingContract.getOrderId()) && ghOrderService != null) {
			try {
				org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(ghAccountingContract.getOrderId());
				if (order != null) {
					// 签单人员（业务员）
					if (oConvertUtils.isEmpty(ghAccountingContract.getServicePerson()) && oConvertUtils.isNotEmpty(order.getSalesman())) {
						// 如果合同没有服务人员，使用订单的业务员
						ghAccountingContract.setServicePerson(order.getSalesman());
					}
					// 设置签单人员（从订单获取）
					ghAccountingContract.setSigner(order.getSalesman());
					// 设置来源（从订单获取），并查询字典文本
					ghAccountingContract.setSource(order.getOpportunitySource());
					if (oConvertUtils.isNotEmpty(order.getOpportunitySource())) {
						// 如果订单没有字典文本，手动查询字典
						if (oConvertUtils.isEmpty(order.getOpportunitySource_dictText()) && sysDictService != null) {
							try {
								String sourceText = sysDictService.queryDictTextByKey("opportunity_source", order.getOpportunitySource());
								ghAccountingContract.setSource_dictText(sourceText);
							} catch (Exception e) {
								log.debug("查询来源字典失败", e);
								ghAccountingContract.setSource_dictText(order.getOpportunitySource());
							}
						} else {
							ghAccountingContract.setSource_dictText(order.getOpportunitySource_dictText());
						}
					}
					// 设置业务类型相关字段（从订单获取）
					if (oConvertUtils.isNotEmpty(order.getBusinessType())) {
						ghAccountingContract.setBusinessType(order.getBusinessType());
						// 获取业务类型字典文本
						String businessTypeText = order.getBusinessType_dictText();
						// 如果订单没有字典文本，手动查询字典
						if (oConvertUtils.isEmpty(businessTypeText) && sysCategoryService != null) {
							try {
								java.util.List<String> names = sysCategoryService.loadDictItem(order.getBusinessType(), false);
								if (names != null && !names.isEmpty()) {
									businessTypeText = names.get(0);
								}
							} catch (Exception e) {
								log.debug("查询业务类型字典失败", e);
								businessTypeText = order.getBusinessType();
							}
						}
						// 如果还是没有，使用业务类型ID
						if (oConvertUtils.isEmpty(businessTypeText)) {
							businessTypeText = order.getBusinessType();
						}
						ghAccountingContract.setBusinessType_dictText(businessTypeText);
						ghAccountingContract.setBusinessTypeName(businessTypeText);
						// 设置产品名称（从订单的业务类型获取）
						ghAccountingContract.setProductName(businessTypeText);
					}
					// 设置到款金额（从订单获取）
					if (order.getReceivedAmount() != null) {
						ghAccountingContract.setPaidAmount(order.getReceivedAmount());
					}
				}
			} catch (Exception e) {
				log.warn("从订单获取信息失败，订单ID: " + ghAccountingContract.getOrderId(), e);
			}
		}
		
		// 计算客户相关字段：累计时间、企业等级、业务标签、关联企业
		if (oConvertUtils.isNotEmpty(ghAccountingContract.getCompanyName())) {
			// 根据公司名称查找客户
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.customer.entity.GhCustomer> customerWrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			customerWrapper.eq("corporate_name", ghAccountingContract.getCompanyName());
			customerWrapper.eq("del_flag", 0);
			customerWrapper.last("LIMIT 1");
			
			if (ghCustomerService != null) {
				try {
					org.jeecg.modules.customer.entity.GhCustomer customer = ghCustomerService.getOne(customerWrapper);
					if (customer != null) {
						// 计算客户字段：消费金额、业务标签、关联企业、累计时间、企业等级
						calculateCustomerFieldsForContract(ghAccountingContract, customer);
					}
				} catch (Exception e) {
					log.warn("查找客户失败，公司名称: " + ghAccountingContract.getCompanyName(), e);
				}
			}
		}
		
		return Result.OK(ghAccountingContract);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param ghAccountingContract
    */
    @RequestMapping(value = "/exportXls")
    @org.apache.shiro.authz.annotation.RequiresPermissions("accountingContract:export")
    public ModelAndView exportXls(HttpServletRequest request, GhAccountingContract ghAccountingContract) {
        return super.exportXls(request, ghAccountingContract, GhAccountingContract.class, "合同管理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @org.apache.shiro.authz.annotation.RequiresPermissions("accountingContract:import")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, GhAccountingContract.class);
    }

	/**
	 * 修改服务人员
	 *
	 * @param id
	 * @param servicePerson
	 * @return
	 */
	@AutoLog(value = "合同管理-修改服务人员")
	@ApiOperation(value="合同管理-修改服务人员", notes="合同管理-修改服务人员")
	@PostMapping(value = "/editServicePerson")
	public Result<?> editServicePerson(@RequestBody Map<String, Object> payload) {
		if (payload == null) {
			return Result.error("请求参数不能为空");
		}
		String id = (String) payload.get("id");
		String servicePerson = (String) payload.get("servicePerson");
		if (oConvertUtils.isEmpty(id)) {
			return Result.error("合同ID不能为空");
		}
		if (oConvertUtils.isEmpty(servicePerson)) {
			return Result.error("服务人员不能为空");
		}
		GhAccountingContract contract = ghAccountingContractService.getById(id);
		if (contract == null) {
			return Result.error("合同不存在");
		}
		String oldServicePerson = contract.getServicePerson();
		contract.setServicePerson(servicePerson);
		ghAccountingContractService.updateById(contract);
		
		// 记录操作日志
		if (ghOrderOperationLogService != null) {
			try {
				// 优先使用合同上已有的订单ID，如果没有则根据合同编号反查订单
				String orderId = contract.getOrderId();
				if (oConvertUtils.isEmpty(orderId) && oConvertUtils.isNotEmpty(contract.getContractNo()) && ghOrderService != null) {
					try {
						org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getOne(
							new QueryWrapper<org.jeecg.modules.order.entity.GhOrder>()
								.eq("contract_no", contract.getContractNo())
								.last("limit 1")
						);
						if (order != null) {
							orderId = order.getId();
						}
					} catch (Exception e) {
						log.warn("根据合同编号查询订单失败，contractNo: {}", contract.getContractNo(), e);
					}
				}

				if (oConvertUtils.isNotEmpty(orderId)) {
					LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
					String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
					// 为避免方向歧义，采用“原/新”文案而不是箭头
					String operationDesc = String.format("修改服务人员：原服务人员【%s】，新服务人员【%s】",
						oConvertUtils.isEmpty(oldServicePerson) ? "空" : oldServicePerson,
						servicePerson);
					ghOrderOperationLogService.saveOperationLog(
					orderId,
					"修改服务人员",
					operationDesc,
					operatorName,
					null,
					null,
					"servicePerson",
					operationDesc
					);
				} else {
					log.warn("修改服务人员时未能获取订单ID，无法记录操作日志，合同ID: {}", contract.getId());
				}
			} catch (Exception e) {
				log.warn("记录操作日志失败", e);
			}
		}
		
		return Result.OK("修改服务人员成功");
	}

	@AutoLog(value = "合同管理-保存服务人员")
	@ApiOperation(value = "合同管理-保存服务人员", notes = "财税主管、财税顾问、主办会计")
	@PostMapping(value = "/saveServiceStaff")
	public Result<?> saveServiceStaff(@RequestBody Map<String, Object> body) {
		if (body == null) {
			return Result.error("请求参数不能为空");
		}
		String id = (String) body.get("id");
		if (oConvertUtils.isEmpty(id)) {
			return Result.error("合同ID不能为空");
		}
		GhAccountingContract contract = ghAccountingContractService.getById(id);
		if (contract == null) {
			return Result.error("合同不存在");
		}
		JSONObject jo = new JSONObject();
		for (String key : ACCOUNTING_SERVICE_STAFF_KEYS) {
			Object v = body.get(key);
			if (v == null) {
				continue;
			}
			String s = String.valueOf(v).trim();
			if (StringUtils.isNotBlank(s)) {
				jo.put(key, s);
			}
		}
		contract.setServiceStaffJson(jo.isEmpty() ? null : jo.toJSONString());
		ghAccountingContractService.updateById(contract);

		if (ghOrderOperationLogService != null) {
			try {
				String orderId = contract.getOrderId();
				if (oConvertUtils.isNotEmpty(orderId)) {
					LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
					String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
					String desc = "保存服务人员：" + (contract.getServiceStaffJson() == null ? "清空" : contract.getServiceStaffJson());
					ghOrderOperationLogService.saveOperationLog(
							orderId,
							"保存服务人员",
							desc,
							operatorName,
							null,
							null,
							"serviceStaffJson",
							desc);
				}
			} catch (Exception e) {
				log.warn("记录保存服务人员日志失败", e);
			}
		}
		return Result.OK("保存成功");
	}

	@AutoLog(value = "合同管理-从审批回填服务人员")
	@ApiOperation(value = "从审批任务回填 service_staff_json（需登录+import 权限；与主办会计等合并）")
	@org.apache.shiro.authz.annotation.RequiresPermissions("accountingContract:import")
	@PostMapping(value = "/backfillServiceStaffFromAudit")
	public Result<?> backfillServiceStaffFromAudit(
			@RequestParam(name = "pageSize", defaultValue = "500") int pageSize,
			@RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun) {
		return runBackfillServiceStaffFromAudit(pageSize, dryRun);
	}

	@AutoLog(value = "合同管理-从审批回填服务人员(免登录)")
	@ApiOperation(value = "免登录回填（需 accessToken 与 jeecg.accounting.service-staff-backfill-token 一致；Shiro 已 anon）")
	@PostMapping(value = "/backfillServiceStaffFromAuditPublic")
	public Result<?> backfillServiceStaffFromAuditPublic(
			@RequestParam(name = "accessToken") String accessToken,
			@RequestParam(name = "pageSize", defaultValue = "500") int pageSize,
			@RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun) {
		if (oConvertUtils.isEmpty(serviceStaffBackfillToken)) {
			return Result.error("服务端未配置 jeecg.accounting.service-staff-backfill-token，拒绝调用");
		}
		if (!serviceStaffBackfillToken.equals(accessToken)) {
			return Result.error("accessToken 无效");
		}
		return runBackfillServiceStaffFromAudit(pageSize, dryRun);
	}

	/** 历史回填：从审批推导全部匹配角色（含主办会计）；与转单时仅写财税主管/财税顾问不同 */
	private Result<?> runBackfillServiceStaffFromAudit(int pageSize, boolean dryRun) {
		if (sysAuditTaskService == null) {
			return Result.error("审核任务服务不可用，无法回填");
		}
		if (pageSize < 1 || pageSize > 2000) {
			return Result.error("pageSize 需在 1～2000 之间");
		}
		int scanned = 0;
		int updated = 0;
		int pageNo = 1;
		while (true) {
			Page<GhAccountingContract> page = new Page<>(pageNo, pageSize);
			QueryWrapper<GhAccountingContract> qw = new QueryWrapper<>();
			qw.eq("del_flag", 0);
			qw.isNotNull("order_id");
			qw.ne("order_id", "");
			IPage<GhAccountingContract> p = ghAccountingContractService.page(page, qw);
			if (p.getRecords() == null || p.getRecords().isEmpty()) {
				break;
			}
			java.util.List<String> orderIds = new java.util.ArrayList<>();
			for (GhAccountingContract c : p.getRecords()) {
				if (oConvertUtils.isNotEmpty(c.getOrderId())) {
					orderIds.add(c.getOrderId());
				}
			}
			java.util.Map<String, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> byOrder =
					orderIds.isEmpty()
							? java.util.Collections.emptyMap()
							: sysAuditTaskService.getTasksByOrderIds(orderIds);
			for (GhAccountingContract c : p.getRecords()) {
				scanned++;
				java.util.List<org.jeecg.modules.system.entity.SysAuditTask> tasks =
						byOrder.getOrDefault(c.getOrderId(), java.util.Collections.emptyList());
				JSONObject derived = AccountingContractServiceStaffJsonUtil.deriveServiceStaffJsonFromAuditTasks(tasks);
				if (derived.isEmpty()) {
					continue;
				}
				JSONObject merged = AccountingContractServiceStaffJsonUtil.mergeServiceStaffJsonPreserveKeys(
						c.getServiceStaffJson(), derived);
				String newJson = merged.isEmpty() ? null : merged.toJSONString();
				if (java.util.Objects.equals(c.getServiceStaffJson(), newJson)) {
					continue;
				}
				updated++;
				if (!dryRun) {
					c.setServiceStaffJson(newJson);
					ghAccountingContractService.updateById(c);
				}
			}
			if (p.getRecords().size() < pageSize) {
				break;
			}
			pageNo++;
		}
		String msg = dryRun ? String.format("演练：扫描 %d 条，将变更 %d 条", scanned, updated)
				: String.format("完成：扫描 %d 条，已更新 %d 条", scanned, updated);
		return Result.OK(msg);
	}

	/**
	 * 流失申请（设置合同的流失标志为1）
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "合同管理-流失申请")
	@ApiOperation(value="合同管理-流失申请", notes="合同管理-流失申请")
	@PostMapping(value = "/applyLoss")
	public Result<?> applyLoss(@RequestBody java.util.Map<String, Object> payload) {
		if (payload == null) {
			return Result.error("请求参数不能为空");
		}
		String id = (String) payload.get("id");
		if (oConvertUtils.isEmpty(id)) {
			return Result.error("合同ID不能为空");
		}
		GhAccountingContract contract = ghAccountingContractService.getById(id);
		if (contract == null) {
			return Result.error("合同不存在");
		}
		
		// 流失类型：normal-正常流失，abnormal-非正常流失
		String lossType = (String) payload.get("lossType");
		java.util.List<String> normalReasons = (java.util.List<String>) payload.get("normalReasons");
		java.util.List<String> abnormalReasons = (java.util.List<String>) payload.get("abnormalReasons");
		String extraRemark = (String) payload.get("remark");
		
		if ((normalReasons == null || normalReasons.isEmpty()) && (abnormalReasons == null || abnormalReasons.isEmpty())) {
			return Result.error("请选择至少一个流失原因");
		}
		
		// 这里先不直接标记流失，后续会接入审批流程；当前先在备注中记录流失申请信息
		StringBuilder lossInfo = new StringBuilder();
		lossInfo.append("【流失申请】");
		if ("abnormal".equals(lossType)) {
			lossInfo.append("类型：非正常流失；");
		} else {
			lossInfo.append("类型：正常流失；");
		}
		if (normalReasons != null && !normalReasons.isEmpty()) {
			lossInfo.append("正常流失原因ID：").append(String.join(",", normalReasons)).append("；");
		}
		if (abnormalReasons != null && !abnormalReasons.isEmpty()) {
			lossInfo.append("非正常流失原因ID：").append(String.join(",", abnormalReasons)).append("；");
		}
		if (oConvertUtils.isNotEmpty(extraRemark)) {
			lossInfo.append("补充说明：").append(extraRemark);
		}
		
		String oldRemarks = contract.getRemarks();
		if (oConvertUtils.isNotEmpty(oldRemarks)) {
			contract.setRemarks(oldRemarks + "\n" + lossInfo.toString());
		} else {
			contract.setRemarks(lossInfo.toString());
		}
		
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (sysUser != null) {
			contract.setUpdateBy(sysUser.getUsername());
		}
		contract.setUpdateTime(new java.util.Date());
		ghAccountingContractService.updateById(contract);
		
		// 优先使用合同上已有的订单ID，如果没有则根据合同编号反查订单
		String orderId = contract.getOrderId();
		org.jeecg.modules.order.entity.GhOrder order = null;
		if (oConvertUtils.isEmpty(orderId) && oConvertUtils.isNotEmpty(contract.getContractNo()) && ghOrderService != null) {
			try {
				order = ghOrderService.getOne(
					new QueryWrapper<org.jeecg.modules.order.entity.GhOrder>()
						.eq("contract_no", contract.getContractNo())
						.last("limit 1")
				);
				if (order != null) {
					orderId = order.getId();
				}
			} catch (Exception e) {
				log.warn("根据合同编号查询订单失败，contractNo: {}", contract.getContractNo(), e);
			}
		} else if (oConvertUtils.isNotEmpty(orderId) && ghOrderService != null) {
			order = ghOrderService.getById(orderId);
		}
		
		// 创建反向审批任务（流失审核流程）
		if (oConvertUtils.isNotEmpty(orderId) && order != null && oConvertUtils.isNotEmpty(order.getBusinessType())) {
			try {
				// 根据业务类型获取审批流程（taskType="loss"）
				if (sysAuditProcessBindingService != null && sysAuditProcessService != null && sysAuditTaskService != null) {
					org.jeecg.modules.system.entity.SysAuditProcessBinding binding = 
						sysAuditProcessBindingService.getBindingByBusinessTypeAndTaskType(order.getBusinessType(), "loss");
					
					if (binding == null) {
						// 如果没有流失审核流程，尝试获取默认流程
						binding = sysAuditProcessBindingService.getBindingByBusinessTypeId(order.getBusinessType());
					}
					
					if (binding != null && oConvertUtils.isNotEmpty(binding.getProcessId())) {
						// 获取流程的所有步骤
						org.jeecg.modules.system.entity.SysAuditProcess process = 
							sysAuditProcessService.getProcessWithSteps(binding.getProcessId());
						
						if (process != null && process.getSteps() != null && !process.getSteps().isEmpty()) {
							java.util.List<org.jeecg.modules.system.entity.SysAuditStep> steps = process.getSteps();
							
							// 按stepOrder排序，然后反向（最后一步先审）
							steps.sort((a, b) -> {
								Integer orderA = a.getStepOrder() != null ? a.getStepOrder() : 0;
								Integer orderB = b.getStepOrder() != null ? b.getStepOrder() : 0;
								return orderB.compareTo(orderA); // 降序，最后一步在前
							});
							
							// 只创建第一步（原流程的最后一步）的审核任务
							org.jeecg.modules.system.entity.SysAuditStep firstStep = steps.get(0);
							
							// 为这一步的所有角色创建审核任务（支持并行审核）
							java.util.List<org.jeecg.modules.system.entity.SysAuditStep> parallelSteps = new java.util.ArrayList<>();
							Integer firstStepOrder = firstStep.getStepOrder();
							for (org.jeecg.modules.system.entity.SysAuditStep step : steps) {
								if (step.getStepOrder() != null && step.getStepOrder().equals(firstStepOrder)) {
									parallelSteps.add(step);
								}
							}
							
							for (org.jeecg.modules.system.entity.SysAuditStep step : parallelSteps) {
								sysAuditTaskService.createTask(
									orderId,
									binding.getProcessId(),
									step.getId(),
									step.getStepOrder(),
									"loss" // taskType标记为流失审核
								);
							}
							
							log.info("为订单 {} 创建流失审核任务，流程ID: {}, 第一步步骤顺序: {}", 
								orderId, binding.getProcessId(), firstStepOrder);
						}
					} else {
						log.warn("订单 {} 的业务类型 {} 未绑定审批流程，无法创建流失审核任务", 
							orderId, order.getBusinessType());
					}
				}
			} catch (Exception e) {
				log.error("创建流失审核任务失败，订单ID: {}", orderId, e);
				// 不抛出异常，避免影响流失申请的提交
			}
		}
		
		// 记录操作日志
		if (ghOrderOperationLogService != null && oConvertUtils.isNotEmpty(orderId)) {
			try {
				String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
				String operationDesc = String.format("提交流失申请，合同编号：%s，公司名称：%s", 
					contract.getContractNo(), contract.getCompanyName());
				ghOrderOperationLogService.saveOperationLog(
					orderId,
					"流失申请",
					operationDesc,
					operatorName,
					null,
					null,
					"lossFlag,contractStatus",
					lossInfo.toString()
				);
			} catch (Exception e) {
				log.warn("记录操作日志失败", e);
			}
		}
		
		return Result.OK("流失申请已提交，后续将按审批流程进行审核！");
	}

	/**
	 * 查询合同的续费记录
	 *
	 * @param contractId 合同ID
	 * @return
	 */
	@AutoLog(value = "合同管理-查询续费记录")
	@ApiOperation(value="合同管理-查询续费记录", notes="合同管理-查询续费记录")
	@GetMapping(value = "/getRenewalRecords")
	public Result<?> getRenewalRecords(@RequestParam(name="contractId",required=true) String contractId) {
		try {
			GhAccountingContract contract = ghAccountingContractService.getById(contractId);
			if (contract == null) {
				return Result.error("合同不存在");
			}
			
			if (ghAddressRenewService == null) {
				return Result.OK(new java.util.ArrayList<>());
			}
			
			// 查询续费记录：根据合同ID（htId）或合同编号查询
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.jeecg.modules.renew.entity.GhAddressRenew> wrapper = 
				new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			// 优先使用ht_id查询，如果没有则使用contract_no
			if (oConvertUtils.isNotEmpty(contractId)) {
				wrapper.eq("ht_id", contractId);
			}
			// 如果合同编号不为空，也作为查询条件（OR关系）
			if (oConvertUtils.isNotEmpty(contract.getContractNo())) {
				if (oConvertUtils.isNotEmpty(contractId)) {
					wrapper.or().eq("contract_no", contract.getContractNo());
				} else {
					wrapper.eq("contract_no", contract.getContractNo());
				}
			}
			// 查询所有续费类型
			wrapper.orderByDesc("renewal_time");
			
			java.util.List<org.jeecg.modules.renew.entity.GhAddressRenew> renewalRecords = ghAddressRenewService.list(wrapper);
			log.info("查询续费记录，合同ID: {}, 合同编号: {}, 查询到 {} 条记录", contractId, contract.getContractNo(), renewalRecords != null ? renewalRecords.size() : 0);
			
			// 手动处理字典文本转换
			if (sysDictService != null && renewalRecords != null) {
				for (org.jeecg.modules.renew.entity.GhAddressRenew record : renewalRecords) {
					// 处理续费类型字典文本
					if (oConvertUtils.isNotEmpty(record.getDetailType())) {
						java.util.List<org.jeecg.common.system.vo.DictModel> dictList = sysDictService.queryDictItemsByCode("detail_type");
						if (dictList != null && !dictList.isEmpty()) {
							for (org.jeecg.common.system.vo.DictModel dict : dictList) {
								if (record.getDetailType().equals(dict.getValue())) {
									// 使用反射设置字典文本字段
									try {
										java.lang.reflect.Field field = record.getClass().getDeclaredField("detailType_dictText");
										field.setAccessible(true);
										field.set(record, dict.getText());
									} catch (Exception e) {
										log.warn("设置续费类型字典文本失败: " + e.getMessage());
									}
									break;
								}
							}
						}
					}
					// 根据账号ID查询银行账户信息，设置三级级联显示文本
					if (oConvertUtils.isNotEmpty(record.getCollectionAccountNumber()) && ghBankManagementService != null) {
						try {
							org.jeecg.modules.bank.entity.GhBankManagement bankAccount = ghBankManagementService.getById(record.getCollectionAccountNumber());
							if (bankAccount != null) {
								// 设置三级级联信息
								try {
									java.lang.reflect.Field field1 = record.getClass().getDeclaredField("payeePerson");
									field1.setAccessible(true);
									field1.set(record, bankAccount.getPayeePerson());
								} catch (Exception e) {
									log.warn("设置收款单位/人失败: " + e.getMessage());
								}
								try {
									java.lang.reflect.Field field2 = record.getClass().getDeclaredField("accountNotes");
									field2.setAccessible(true);
									field2.set(record, bankAccount.getAccountNotes());
								} catch (Exception e) {
									log.warn("设置网点名称失败: " + e.getMessage());
								}
								// 设置收款账户（卡号）
								try {
									java.lang.reflect.Field field4 = record.getClass().getDeclaredField("collectionAccount");
									field4.setAccessible(true);
									field4.set(record, bankAccount.getCollectionAccount());
								} catch (Exception e) {
									log.warn("设置收款账户（卡号）失败: " + e.getMessage());
								}
								// 如果收款方式为空，从银行账户中获取
								if (oConvertUtils.isEmpty(record.getPaymentMethod()) && oConvertUtils.isNotEmpty(bankAccount.getPaymentMethod())) {
									try {
										java.lang.reflect.Field field3 = record.getClass().getDeclaredField("paymentMethod");
										field3.setAccessible(true);
										field3.set(record, bankAccount.getPaymentMethod());
									} catch (Exception e) {
										log.warn("设置收款方式失败: " + e.getMessage());
									}
								}
							}
						} catch (Exception e) {
							log.warn("查询银行账户信息失败，账号ID: " + record.getCollectionAccountNumber(), e);
						}
					}
					// 处理收款方式字典文本
					if (oConvertUtils.isNotEmpty(record.getPaymentMethod())) {
						java.util.List<org.jeecg.common.system.vo.DictModel> dictList = sysDictService.queryDictItemsByCode("payment_method");
						if (dictList != null && !dictList.isEmpty()) {
							for (org.jeecg.common.system.vo.DictModel dict : dictList) {
								if (record.getPaymentMethod().equals(dict.getValue())) {
									try {
										java.lang.reflect.Field field = record.getClass().getDeclaredField("paymentMethod_dictText");
										field.setAccessible(true);
										field.set(record, dict.getText());
									} catch (Exception e) {
										log.warn("设置收款方式字典文本失败: " + e.getMessage());
									}
									break;
								}
							}
						}
					}
				}
			}
			
			return Result.OK(renewalRecords);
		} catch (Exception e) {
			log.error("查询续费记录失败，合同ID: " + contractId, e);
			return Result.error("查询续费记录失败");
		}
	}

	/**
	 * 新增合同续费记录
	 *
	 * @param renewalData 续费数据
	 * @return
	 */
	@AutoLog(value = "合同管理-新增续费记录")
	@ApiOperation(value="合同管理-新增续费记录", notes="合同管理-新增续费记录")
	@PostMapping(value = "/addRenewalRecord")
	public Result<?> addRenewalRecord(@RequestBody java.util.Map<String, Object> renewalData) {
		try {
			if (ghAddressRenewService == null) {
				return Result.error("续费服务未初始化");
			}
			
			String contractId = (String) renewalData.get("contractId");
			if (oConvertUtils.isEmpty(contractId)) {
				return Result.error("合同ID不能为空");
			}
			
			GhAccountingContract contract = ghAccountingContractService.getById(contractId);
			if (contract == null) {
				return Result.error("合同不存在");
			}
			
			// 创建续费记录
			org.jeecg.modules.renew.entity.GhAddressRenew renewal = new org.jeecg.modules.renew.entity.GhAddressRenew();
			renewal.setHtId(contractId);
			renewal.setContractNo(contract.getContractNo());
			renewal.setCompanyName(contract.getCompanyName());
			renewal.setCompanyId(contract.getOrderId()); // 使用订单ID作为公司ID
			
			// 续费类型：使用前端传递的renewalType，如果没有则默认为"代账续费"
			String renewalType = (String) renewalData.get("renewalType");
			if (oConvertUtils.isNotEmpty(renewalType)) {
				renewal.setDetailType(renewalType);
			} else {
				renewal.setDetailType("代账续费"); // 默认值
			}
			
			renewal.setRenewalTime(new java.util.Date());
			
			// 原到期日期
			if (contract.getExpireDate() != null) {
				renewal.setOriginalDueDate(contract.getExpireDate());
			}
			
			// 后到期日期（到期月份）
			String expireMonth = (String) renewalData.get("expireMonth");
			if (oConvertUtils.isNotEmpty(expireMonth)) {
				try {
					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
					java.util.Date monthDate = sdf.parse(expireMonth);
					// 设置为该月的最后一天
					java.util.Calendar cal = java.util.Calendar.getInstance();
					cal.setTime(monthDate);
					cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
					java.util.Date newExpireDate = cal.getTime();

					// 校验：续费后的到期日期必须晚于当前合同到期日期
					if (contract.getExpireDate() != null && newExpireDate != null) {
						// 如果新到期日小于等于当前到期日，则不允许续费
						if (!newExpireDate.after(contract.getExpireDate())) {
							return Result.error("到期月份必须晚于当前合同的到期日期，请重新选择！");
						}
					}

					renewal.setPostExpirationDate(newExpireDate);
				} catch (Exception e) {
					log.warn("解析到期月份失败: " + expireMonth, e);
				}
			}
			
			// 应续费金额
			String amounts = (String) renewalData.get("amounts");
			if (oConvertUtils.isNotEmpty(amounts)) {
				renewal.setAmounts(amounts);
			} else if (contract.getContractAmount() != null) {
				renewal.setAmounts(contract.getContractAmount().toString());
			}
			
			// 到款金额
			String amountReceived = (String) renewalData.get("amountReceived");
			if (oConvertUtils.isNotEmpty(amountReceived)) {
				renewal.setAmountReceived(amountReceived);
			}
			
			// 收款时间
			String paymentTimeStr = (String) renewalData.get("paymentTime");
			if (oConvertUtils.isNotEmpty(paymentTimeStr)) {
				try {
					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					java.util.Date paymentTime = sdf.parse(paymentTimeStr);
					renewal.setPaymentTime(paymentTime);
				} catch (Exception e) {
					log.warn("解析收款时间失败: " + paymentTimeStr, e);
				}
			}
			
			// 收款方式
			String paymentMethod = (String) renewalData.get("paymentMethod");
			if (oConvertUtils.isNotEmpty(paymentMethod)) {
				renewal.setPaymentMethod(paymentMethod);
			}
			
			// 收款账号
			String collectionAccountNumber = (String) renewalData.get("collectionAccountNumber");
			if (oConvertUtils.isNotEmpty(collectionAccountNumber)) {
				renewal.setCollectionAccountNumber(collectionAccountNumber);
			}
			
			// 凭证
			String vouchers = (String) renewalData.get("vouchers");
			if (oConvertUtils.isNotEmpty(vouchers)) {
				renewal.setVouchers(vouchers);
			}
			
			// 备注
			String remarks = (String) renewalData.get("remarks");
			if (oConvertUtils.isNotEmpty(remarks)) {
				renewal.setRemarks(remarks);
			}
			
			// 签单员（从合同的服务人员获取）
			if (oConvertUtils.isNotEmpty(contract.getServicePerson())) {
				renewal.setSaleMan(contract.getServicePerson());
			}
			
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (sysUser != null) {
				renewal.setCreator(sysUser.getRealname());
			}
			
			// 设置审核状态为待审核
			renewal.setAuditStatus("0");
			
			renewal.setCreateTime(new java.util.Date());
			
			// 保存续费记录
			ghAddressRenewService.save(renewal);
			log.info("========== 代账续费记录保存成功 ==========");
			log.info("续费记录ID: {}, 合同ID: {}, 合同编号: {}, 续费类型: {}", renewal.getId(), contractId, contract.getContractNo(), renewal.getDetailType());
			
			// 创建银行日记收入记录
			log.info("========== 开始创建银行日记记录 ==========");
			log.info("ghBankDiaryService 是否为空: {}", ghBankDiaryService == null);
			log.info("collectionAccountNumber: [{}], amountReceived: [{}]", 
					renewal.getCollectionAccountNumber(), renewal.getAmountReceived());
			
			if (ghBankDiaryService != null && 
				StringUtils.isNotBlank(renewal.getCollectionAccountNumber()) && 
				StringUtils.isNotBlank(renewal.getAmountReceived())) {
				try {
					String orderId = contract.getOrderId();
					log.info("关联订单ID: {}", orderId);
					createBankDiaryFromRenewal(renewal, sysUser, orderId);
					log.info("========== 代账续费银行日记记录创建成功 ==========");
					log.info("续费记录ID: {}", renewal.getId());
				} catch (Exception e) {
					log.error("========== 创建代账续费银行日记记录失败 ==========");
					log.error("续费记录ID: {}", renewal.getId(), e);
					e.printStackTrace();
					// 不抛出异常，避免影响续费记录保存
				}
			} else {
				log.warn("========== 代账续费银行日记记录创建条件不满足 ==========");
				log.warn("续费记录ID: {}", renewal.getId());
				log.warn("ghBankDiaryService 为空: {}", ghBankDiaryService == null);
				log.warn("collectionAccountNumber 为空: {}", StringUtils.isBlank(renewal.getCollectionAccountNumber()));
				log.warn("amountReceived 为空: {}", StringUtils.isBlank(renewal.getAmountReceived()));
			}
			
			// 记录操作日志
			if (ghOrderOperationLogService != null && oConvertUtils.isNotEmpty(contract.getOrderId())) {
				try {
					String operatorName = sysUser != null ? sysUser.getRealname() : "系统";
					String operationDesc = String.format("新增续费记录，续费类型：%s，应续费金额：%s，到款金额：%s，到期月份：%s", 
						renewal.getDetailType() != null ? renewal.getDetailType() : "代账续费",
						renewal.getAmounts() != null ? renewal.getAmounts() : "-",
						renewal.getAmountReceived() != null ? renewal.getAmountReceived() : "-",
						expireMonth != null ? expireMonth : "-");
					ghOrderOperationLogService.saveOperationLog(
						contract.getOrderId(),
						"新增续费",
						operationDesc,
						operatorName,
						null,
						null,
						null,
						operationDesc
					);
				} catch (Exception e) {
					log.warn("记录操作日志失败", e);
				}
			}
			
			return Result.OK("续费记录保存成功");
		} catch (Exception e) {
			log.error("新增续费记录失败", e);
			return Result.error("新增续费记录失败: " + e.getMessage());
		}
	}
	
	/**
	 * 从续费记录创建银行日记收入记录
	 * @param renew 续费记录
	 * @param sysUser 当前登录用户
	 * @param orderId 关联订单ID（可选）
	 */
	private void createBankDiaryFromRenewal(org.jeecg.modules.renew.entity.GhAddressRenew renew, LoginUser sysUser, String orderId) {
		if (ghBankDiaryService == null) {
			return;
		}
		
		// 检查是否已经存在该续费记录对应的银行日记记录
		QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("business_id", renew.getId());
		queryWrapper.eq("business_type", "续费收入");
		queryWrapper.eq("del_flag", 0);
		long count = ghBankDiaryService.count(queryWrapper);
		if (count > 0) {
			log.info("续费记录ID {} 已存在银行日记记录，跳过创建", renew.getId());
			return;
		}
		
		// 获取到款金额
		BigDecimal receivedAmount = null;
		try {
			if (StringUtils.isNotBlank(renew.getAmountReceived())) {
				receivedAmount = new BigDecimal(renew.getAmountReceived());
			}
		} catch (Exception e) {
			log.error("解析续费到款金额失败: {}", renew.getAmountReceived(), e);
			return;
		}
		
		if (receivedAmount == null || receivedAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("续费记录ID {} 到款金额为0或空，跳过创建银行日记记录", renew.getId());
			return;
		}
		
		// 获取收款账户ID
		String bankAccountId = renew.getCollectionAccountNumber();
		if (StringUtils.isBlank(bankAccountId)) {
			log.info("续费记录ID {} 收款账户为空，跳过创建银行日记记录", renew.getId());
			return;
		}
		
		GhBankDiary bankDiary = new GhBankDiary();
		bankDiary.setOrderDate(renew.getRenewalTime() != null ? renew.getRenewalTime() : new java.util.Date());
		bankDiary.setBusinessType("续费收入");
		bankDiary.setBusinessId(renew.getId());
		
		// 费用详情（根据续费类型区分）
		String feeDetail;
		if ("4".equals(renew.getDetailType()) || "地址续费".equals(renew.getDetailType())) {
			feeDetail = "地址续费收款";
		} else {
			feeDetail = "代账续费收款";
		}
		if (StringUtils.isNotBlank(renew.getCompanyName())) {
			feeDetail += " - 公司：" + renew.getCompanyName();
		}
		bankDiary.setFeeDetail(feeDetail);
		
		// 收入金额（到款金额）
		bankDiary.setIncomeAmount(receivedAmount);
		bankDiary.setExpenseAmount(BigDecimal.ZERO);
		
		// 银行账户
		bankDiary.setBankAccountId(bankAccountId);
		
		// 尝试获取银行账户名称
		if (ghBankManagementService != null) {
			try {
				GhBankManagement bankAccount = ghBankManagementService.getById(bankAccountId);
				if (bankAccount != null) {
					StringBuilder accountName = new StringBuilder();
					if (StringUtils.isNotBlank(bankAccount.getPayeePerson())) {
						accountName.append(bankAccount.getPayeePerson());
					}
					if (StringUtils.isNotBlank(bankAccount.getCollectionAccount())) {
						if (accountName.length() > 0) {
							accountName.append(" - ");
						}
						accountName.append(bankAccount.getCollectionAccount());
					}
					if (StringUtils.isNotBlank(bankAccount.getAccountNotes())) {
						if (accountName.length() > 0) {
							accountName.append("（").append(bankAccount.getAccountNotes()).append("）");
						} else {
							accountName.append(bankAccount.getAccountNotes());
						}
					}
					bankDiary.setBankAccountName(accountName.length() > 0 ? accountName.toString() : "");
				}
			} catch (Exception e) {
				log.debug("查询银行账户信息失败", e);
			}
		}
		
		// 公司信息
		if (StringUtils.isNotBlank(renew.getCompanyId())) {
			bankDiary.setCompanyId(renew.getCompanyId());
		}
		if (StringUtils.isNotBlank(renew.getCompanyName())) {
			bankDiary.setCompanyName(renew.getCompanyName());
		}
		
		// 关联订单ID（如果有）
		if (StringUtils.isNotBlank(orderId)) {
			bankDiary.setOrderId(orderId);
		}
		
		// 操作人员
		if (sysUser != null) {
			bankDiary.setOperatorId(sysUser.getId());
			bankDiary.setOperatorName(sysUser.getRealname());
			bankDiary.setCreateBy(sysUser.getUsername());
		}
		
		// 备注
		String remarks = "代账续费收款自动生成";
		if (StringUtils.isNotBlank(renew.getRemarks())) {
			remarks += " - " + renew.getRemarks();
		}
		bankDiary.setRemarks(remarks);
		
		// 凭证
		if (StringUtils.isNotBlank(renew.getVouchers())) {
			bankDiary.setVouchers(renew.getVouchers());
		}
		
		bankDiary.setCreateTime(new java.util.Date());
		bankDiary.setDelFlag(0);
		
		// 计算结余金额
		if (bankDiary.getBankAccountId() != null) {
			BigDecimal balanceAmount = calculateBalanceAmount(bankDiary.getBankAccountId(), bankDiary.getOrderDate());
			if (bankDiary.getIncomeAmount() != null) {
				balanceAmount = balanceAmount.add(bankDiary.getIncomeAmount());
			}
			if (bankDiary.getExpenseAmount() != null) {
				balanceAmount = balanceAmount.subtract(bankDiary.getExpenseAmount());
			}
			bankDiary.setBalanceAmount(balanceAmount);
		}
		
		ghBankDiaryService.save(bankDiary);
		
		// 更新后续记录的结余金额
		if (bankDiary.getBankAccountId() != null) {
			updateSubsequentBankDiaryBalances(bankDiary.getBankAccountId(), bankDiary.getOrderDate());
		}
		
		log.info("为代账续费记录ID {} 创建了银行日记记录，到款金额: {}", renew.getId(), receivedAmount);
	}
	
	/**
	 * 计算指定银行账户在指定日期之前的结余金额
	 */
	private BigDecimal calculateBalanceAmount(String bankAccountId, java.util.Date orderDate) {
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
	private void updateSubsequentBankDiaryBalances(String bankAccountId, java.util.Date orderDate) {
		if (ghBankDiaryService == null || StringUtils.isBlank(bankAccountId) || orderDate == null) {
			return;
		}
		
		try {
			QueryWrapper<GhBankDiary> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("bank_account_id", bankAccountId);
			queryWrapper.eq("del_flag", 0);
			queryWrapper.ge("order_date", orderDate);
			queryWrapper.orderByAsc("order_date", "create_time");
			java.util.List<GhBankDiary> subsequentDiaries = ghBankDiaryService.list(queryWrapper);
			
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
		com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysRole> query = 
			new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
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
		
		com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUserRole> query = 
			new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
		query.in(org.jeecg.modules.system.entity.SysUserRole::getRoleId, roleIds);
		List<org.jeecg.modules.system.entity.SysUserRole> userRoles = sysUserRoleService.list(query);
		
		if (userRoles == null || userRoles.isEmpty()) {
			return new ArrayList<>();
		}
		
		// 提取用户ID并去重
		return userRoles.stream()
				.map(org.jeecg.modules.system.entity.SysUserRole::getUserId)
				.distinct()
				.collect(java.util.stream.Collectors.toList());
	}
	
	/**
	 * 流失分析统计
	 * @param year 年份
	 * @param month 月份（可选）
	 * @param businessType 业务类型（可选）
	 * @return 统计数据
	 */
	@GetMapping(value = "/getLossStatistics")
	public Result<?> getLossStatistics(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "businessType", required = false) String businessType) {
		try {
			// 如果没有指定年份，使用当前年份
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			// 构建查询条件
			QueryWrapper<GhAccountingContract> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			queryWrapper.eq("loss_flag", 1); // 只查询流失的合同
			
			// 年份过滤
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, 0, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date yearStart = calendar.getTime();
			
			calendar.set(year, 11, 31, 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date yearEnd = calendar.getTime();
			
			Date monthStart = null;
			Date monthEnd = null;
			if (month != null && month >= 1 && month <= 12) {
				calendar.set(year, month - 1, 1, 0, 0, 0);
				calendar.set(java.util.Calendar.MILLISECOND, 0);
				monthStart = calendar.getTime();
				calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
				calendar.set(java.util.Calendar.MILLISECOND, 999);
				monthEnd = calendar.getTime();
			}
			
			// 业务类型过滤（如果指定，需要通过订单关联）
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				// 先查询符合条件的订单ID
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> orderIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getId)
					.collect(java.util.stream.Collectors.toSet());
				if (!orderIds.isEmpty()) {
					queryWrapper.in("order_id", orderIds);
				} else {
					queryWrapper.eq("1", "0"); // 没有符合条件的订单，返回空结果
				}
			}
			
			// 查询所有符合条件的合同，并按“流失发生时间（优先流失审批通过时间）”过滤
			List<GhAccountingContract> contracts = ghAccountingContractService.list(queryWrapper);
			java.util.Map<String, java.util.Date> lossTimeByOrderId = buildLossApprovedTimeByOrderIds(contracts);
			List<GhAccountingContract> filteredContracts = new ArrayList<>();
			for (GhAccountingContract contract : contracts) {
				Date lossTime = resolveContractLossTime(contract, lossTimeByOrderId);
				if (lossTime == null) {
					continue;
				}
				if (lossTime.before(yearStart) || lossTime.after(yearEnd)) {
					continue;
				}
				if (monthStart != null && monthEnd != null && (lossTime.before(monthStart) || lossTime.after(monthEnd))) {
					continue;
				}
				filteredContracts.add(contract);
			}
			
			// 统计数据
			Map<String, Object> result = new HashMap<>();
			
			// 1. 总体统计
			Map<String, Object> statistics = new HashMap<>();
			int totalCount = filteredContracts.size();
			BigDecimal totalAmount = BigDecimal.ZERO;
			BigDecimal lossRate = BigDecimal.ZERO;
			java.util.Set<String> salesmanSet = new java.util.HashSet<>();
			
			// 计算总客户数（用于计算流失率）
			QueryWrapper<GhAccountingContract> totalWrapper = new QueryWrapper<>();
			totalWrapper.eq("del_flag", 0);
			totalWrapper.ge("create_time", yearStart);
			totalWrapper.le("create_time", yearEnd);
			int totalCustomers = ghAccountingContractService.count(totalWrapper);
			
			for (GhAccountingContract contract : filteredContracts) {
				BigDecimal amount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
				totalAmount = totalAmount.add(amount);
				
				if (oConvertUtils.isNotEmpty(contract.getServicePerson())) {
					salesmanSet.add(contract.getServicePerson());
				}
			}
			
			BigDecimal avgAmount = totalCount > 0 ? 
				totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
				BigDecimal.ZERO;
			
			if (totalCustomers > 0) {
				lossRate = new BigDecimal(totalCount)
					.multiply(new BigDecimal(100))
					.divide(new BigDecimal(totalCustomers), 2, BigDecimal.ROUND_HALF_UP);
			}
			
			statistics.put("totalCount", totalCount);
			statistics.put("totalAmount", totalAmount);
			statistics.put("avgAmount", avgAmount);
			statistics.put("lossRate", lossRate);
			
			// 2. 月度趋势数据（全年12个月）
			List<Map<String, Object>> monthlyTrend = new ArrayList<>();
			for (int m = 1; m <= 12; m++) {
				calendar.set(year, m - 1, 1, 0, 0, 0);
				calendar.set(java.util.Calendar.MILLISECOND, 0);
				Date mStart = calendar.getTime();
				
				calendar.set(year, m - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
				calendar.set(java.util.Calendar.MILLISECOND, 999);
				Date mEnd = calendar.getTime();
				
				int count = 0;
				BigDecimal amount = BigDecimal.ZERO;
				
				for (GhAccountingContract contract : filteredContracts) {
					Date lossTime = resolveContractLossTime(contract, lossTimeByOrderId);
					if (lossTime != null && !lossTime.before(mStart) && !lossTime.after(mEnd)) {
						count++;
						BigDecimal contractAmount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
						amount = amount.add(contractAmount);
					}
				}
				
				Map<String, Object> monthData = new HashMap<>();
				monthData.put("month", m + "月");
				monthData.put("count", count);
				monthData.put("amount", amount);
				monthlyTrend.add(monthData);
			}
			
			// 3. 业务类型分布（需要通过订单关联）
			Map<String, Map<String, Object>> businessTypeMap = new HashMap<>();
			if (ghOrderService != null) {
				for (GhAccountingContract contract : filteredContracts) {
					if (oConvertUtils.isNotEmpty(contract.getOrderId())) {
						org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(contract.getOrderId());
						if (order != null) {
							String bt = order.getBusinessType();
							if (oConvertUtils.isEmpty(bt)) {
								bt = "其他";
							} else {
								try {
									if (sysCategoryService != null) {
										java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
										if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
											bt = names.get(0);
										}
									}
								} catch (Exception e) {
									log.warn("获取业务类型名称失败，业务类型ID: " + bt, e);
								}
							}
							
							Map<String, Object> btData = businessTypeMap.getOrDefault(bt, new HashMap<>());
							int count = (Integer) btData.getOrDefault("count", 0);
							BigDecimal amount = (BigDecimal) btData.getOrDefault("amount", BigDecimal.ZERO);
							
							BigDecimal contractAmount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
							
							btData.put("name", bt);
							btData.put("count", count + 1);
							btData.put("amount", amount.add(contractAmount));
							businessTypeMap.put(bt, btData);
						}
					}
				}
			}
			
			List<Map<String, Object>> businessTypeList = new ArrayList<>(businessTypeMap.values());
			
			// 4. 业务员排名（按流失数）
			Map<String, Map<String, Object>> salesmanMap = new HashMap<>();
			for (GhAccountingContract contract : filteredContracts) {
				String sm = contract.getServicePerson();
				if (oConvertUtils.isEmpty(sm)) {
					sm = "未分配";
				}
				
				Map<String, Object> smData = salesmanMap.getOrDefault(sm, new HashMap<>());
				int count = (Integer) smData.getOrDefault("count", 0);
				BigDecimal amount = (BigDecimal) smData.getOrDefault("amount", BigDecimal.ZERO);
				
				BigDecimal contractAmount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
				
				smData.put("salesman", sm);
				smData.put("count", count + 1);
				smData.put("amount", amount.add(contractAmount));
				salesmanMap.put(sm, smData);
			}
			
			List<Map<String, Object>> salesmanRank = new ArrayList<>(salesmanMap.values());
			// 按流失数排序
			salesmanRank.sort((a, b) -> {
				Integer countA = (Integer) a.get("count");
				Integer countB = (Integer) b.get("count");
				return countB.compareTo(countA);
			});
			
			// 5. 每日趋势数据
			List<Map<String, Object>> dailyTrend = new ArrayList<>();
			if (month != null && month >= 1 && month <= 12) {
				calendar.set(year, month - 1, 1, 0, 0, 0);
				int daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
				
				for (int d = 1; d <= daysInMonth; d++) {
					calendar.set(year, month - 1, d, 0, 0, 0);
					calendar.set(java.util.Calendar.MILLISECOND, 0);
					Date dStart = calendar.getTime();
					
					calendar.set(year, month - 1, d, 23, 59, 59);
					calendar.set(java.util.Calendar.MILLISECOND, 999);
					Date dEnd = calendar.getTime();
					
					int count = 0;
					BigDecimal amount = BigDecimal.ZERO;
					
					for (GhAccountingContract contract : filteredContracts) {
						Date lossTime = resolveContractLossTime(contract, lossTimeByOrderId);
						if (lossTime != null && !lossTime.before(dStart) && !lossTime.after(dEnd)) {
							count++;
							BigDecimal contractAmount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
							amount = amount.add(contractAmount);
						}
					}
					
					Map<String, Object> dayData = new HashMap<>();
					dayData.put("date", String.format("%d-%02d-%02d", year, month, d));
					dayData.put("count", count);
					dayData.put("amount", amount);
					dailyTrend.add(dayData);
				}
			} else {
				for (Map<String, Object> monthData : monthlyTrend) {
					Map<String, Object> dayData = new HashMap<>();
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
			log.error("获取流失分析统计数据失败", e);
			return Result.error("获取统计数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取业务人员流失月度明细数据
	 * @param year 年份
	 * @param month 月份（可选）
	 * @param businessType 业务类型（可选）
	 * @return 业务人员月度明细数据
	 */
	@GetMapping(value = "/getLossSalesmanMonthlyDetail")
	public Result<?> getLossSalesmanMonthlyDetail(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "businessType", required = false) String businessType) {
		try {
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			QueryWrapper<GhAccountingContract> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			queryWrapper.eq("loss_flag", 1);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, 0, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date yearStart = calendar.getTime();
			
			calendar.set(year, 11, 31, 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date yearEnd = calendar.getTime();
			
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> orderIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getId)
					.collect(java.util.stream.Collectors.toSet());
				if (!orderIds.isEmpty()) {
					queryWrapper.in("order_id", orderIds);
				} else {
					queryWrapper.eq("1", "0");
				}
			}
			
			List<GhAccountingContract> contracts = ghAccountingContractService.list(queryWrapper);
			java.util.Map<String, java.util.Date> lossTimeByOrderId = buildLossApprovedTimeByOrderIds(contracts);
			
			Map<String, Map<Integer, Map<String, Object>>> salesmanMonthlyMap = new HashMap<>();
			
			for (GhAccountingContract contract : contracts) {
				Date lossTime = resolveContractLossTime(contract, lossTimeByOrderId);
				if (lossTime == null || lossTime.before(yearStart) || lossTime.after(yearEnd)) {
					continue;
				}
				
				String salesman = oConvertUtils.isEmpty(contract.getServicePerson()) ? "未分配" : contract.getServicePerson();
				calendar.setTime(lossTime);
				int orderMonth = calendar.get(java.util.Calendar.MONTH) + 1;
				
				Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.getOrDefault(salesman, new HashMap<>());
				Map<String, Object> monthData = monthlyData.getOrDefault(orderMonth, new HashMap<>());
				
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
				
				BigDecimal contractAmount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
				
				monthData.put("count", count + 1);
				monthData.put("amount", amount.add(contractAmount));
				
				monthlyData.put(orderMonth, monthData);
				salesmanMonthlyMap.put(salesman, monthlyData);
			}
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			List<String> salesmen = new ArrayList<>(salesmanMonthlyMap.keySet());
			java.util.Collections.sort(salesmen);
			
			for (String salesman : salesmen) {
				Map<Integer, Map<String, Object>> monthlyData = salesmanMonthlyMap.get(salesman);
				
				Map<String, Object> row = new HashMap<>();
				row.put("salesman", salesman);
				
				int totalCount = 0;
				BigDecimal totalAmount = BigDecimal.ZERO;
				
				for (int m = 1; m <= 12; m++) {
					Map<String, Object> monthData = monthlyData.getOrDefault(m, new HashMap<>());
					
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
			
			return Result.OK(resultList);
		} catch (Exception e) {
			log.error("获取业务人员流失月度明细数据失败", e);
			return Result.error("获取数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取业务人员流失月度合同列表
	 * @param year 年份
	 * @param month 月份
	 * @param salesman 业务人员
	 * @param businessType 业务类型（可选）
	 * @param pageNo 页码
	 * @param pageSize 每页数量
	 * @return 合同列表
	 */
	@GetMapping(value = "/getLossSalesmanMonthlyContracts")
	public Result<?> getLossSalesmanMonthlyContracts(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = true) Integer month,
			@RequestParam(name = "salesman", required = true) String salesman,
			@RequestParam(name = "businessType", required = false) String businessType,
			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		try {
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			QueryWrapper<GhAccountingContract> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			queryWrapper.eq("loss_flag", 1);
			queryWrapper.eq("service_person", salesman);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, month - 1, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date monthStart = calendar.getTime();
			
			calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date monthEnd = calendar.getTime();
			
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> orderIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getId)
					.collect(java.util.stream.Collectors.toSet());
				if (!orderIds.isEmpty()) {
					queryWrapper.in("order_id", orderIds);
				} else {
					queryWrapper.eq("1", "0");
				}
			}
			
			List<GhAccountingContract> contracts = ghAccountingContractService.list(queryWrapper);
			java.util.Map<String, java.util.Date> lossTimeByOrderId = buildLossApprovedTimeByOrderIds(contracts);
			List<GhAccountingContract> filteredContracts = new ArrayList<>();
			for (GhAccountingContract contract : contracts) {
				Date lossTime = resolveContractLossTime(contract, lossTimeByOrderId);
				if (lossTime == null || lossTime.before(monthStart) || lossTime.after(monthEnd)) {
					continue;
				}
				filteredContracts.add(contract);
			}
			filteredContracts.sort((a, b) -> {
				Date ta = resolveContractLossTime(a, lossTimeByOrderId);
				Date tb = resolveContractLossTime(b, lossTimeByOrderId);
				if (ta == null && tb == null) {
					return 0;
				}
				if (ta == null) {
					return 1;
				}
				if (tb == null) {
					return -1;
				}
				return tb.compareTo(ta);
			});
			Page<GhAccountingContract> page = new Page<>(pageNo, pageSize);
			int from = Math.max(0, (pageNo - 1) * pageSize);
			int to = Math.min(filteredContracts.size(), from + pageSize);
			List<GhAccountingContract> pageRecords = from >= to ? new ArrayList<>() : filteredContracts.subList(from, to);
			page.setTotal(filteredContracts.size());
			page.setRecords(pageRecords);
			IPage<GhAccountingContract> pageList = page;
			
			// 为每个合同添加业务类型信息
			if (pageList.getRecords() != null && ghOrderService != null) {
				for (GhAccountingContract contract : pageList.getRecords()) {
					if (oConvertUtils.isNotEmpty(contract.getOrderId())) {
						org.jeecg.modules.order.entity.GhOrder order = ghOrderService.getById(contract.getOrderId());
						if (order != null) {
							String bt = order.getBusinessType();
							if (oConvertUtils.isNotEmpty(bt) && sysCategoryService != null) {
								try {
									java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
									if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
										contract.setBusinessType(bt);
										// 使用反射设置字典文本（如果实体类有该字段）
									}
								} catch (Exception e) {
									log.warn("获取业务类型名称失败", e);
								}
							}
						}
					}
				}
			}
			
			return Result.OK(pageList);
		} catch (Exception e) {
			log.error("获取业务人员流失月度合同列表失败", e);
			return Result.error("获取数据失败：" + e.getMessage());
		}
	}

	/**
	 * 按订单聚合流失审批通过时间（task_type=loss 且 task_status=approved，优先 audit_time）。
	 */
	private java.util.Map<String, java.util.Date> buildLossApprovedTimeByOrderIds(List<GhAccountingContract> contracts) {
		java.util.Map<String, java.util.Date> byOrderId = new java.util.HashMap<>();
		if (contracts == null || contracts.isEmpty() || sysAuditTaskService == null) {
			return byOrderId;
		}
		java.util.List<String> orderIds = new java.util.ArrayList<>();
		for (GhAccountingContract c : contracts) {
			if (oConvertUtils.isNotEmpty(c.getOrderId())) {
				orderIds.add(c.getOrderId());
			}
		}
		if (orderIds.isEmpty()) {
			return byOrderId;
		}
		java.util.Map<String, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> tasksByOrder =
				sysAuditTaskService.getTasksByOrderIds(orderIds);
		for (java.util.Map.Entry<String, java.util.List<org.jeecg.modules.system.entity.SysAuditTask>> e : tasksByOrder.entrySet()) {
			java.util.Date max = null;
			for (org.jeecg.modules.system.entity.SysAuditTask t : e.getValue()) {
				if (!"loss".equals(t.getTaskType()) || !"approved".equals(t.getTaskStatus())) {
					continue;
				}
				java.util.Date at = t.getAuditTime() != null ? t.getAuditTime()
						: (t.getUpdateTime() != null ? t.getUpdateTime() : t.getCreateTime());
				if (at == null) {
					continue;
				}
				if (max == null || at.after(max)) {
					max = at;
				}
			}
			if (max != null) {
				byOrderId.put(e.getKey(), max);
			}
		}
		return byOrderId;
	}

	private java.util.Date resolveContractLossTime(
			GhAccountingContract contract,
			java.util.Map<String, java.util.Date> lossTimeByOrderId) {
		if (contract == null) {
			return null;
		}
		if (oConvertUtils.isNotEmpty(contract.getOrderId()) && lossTimeByOrderId != null) {
			java.util.Date d = lossTimeByOrderId.get(contract.getOrderId());
			if (d != null) {
				return d;
			}
		}
		// 兜底：没有审批通过记录时，使用合同更新时间（流失标记变更时会刷新 update_time）
		if (contract.getUpdateTime() != null) {
			return contract.getUpdateTime();
		}
		return contract.getCreateTime();
	}
}

