package org.jeecg.modules.customer.controller;

import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.customer.entity.GhCustomer;
import org.jeecg.modules.customer.service.IGhCustomerService;
import org.jeecg.modules.order.entity.GhOrder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;
import org.jeecg.common.aspect.annotation.AutoLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 客户管理
 * @Author: jeecg-boot
 * @Date: 2025-01-31
 * @Version: V1.0
 */
@Api(tags="客户管理")
@RestController
@RequestMapping("/customer/ghCustomer")
@Slf4j
public class GhCustomerController extends JeecgController<GhCustomer, IGhCustomerService> {
	
	@Autowired
	private IGhCustomerService ghCustomerService;
	
	@Autowired(required = false)
	private org.jeecg.modules.order.service.IGhOrderService ghOrderService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysCategoryService sysCategoryService;
	
	@Autowired(required = false)
	private org.jeecg.modules.system.service.ISysDictService sysDictService;
	
	@Autowired(required = false)
	private org.jeecg.modules.order.service.IGhAccountingContractService ghAccountingContractService;
	
	/**
	 * 分页列表查询
	 */
	@AutoLog(value = "客户管理-分页列表查询")
	@ApiOperation(value="客户管理-分页列表查询", notes="客户管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(GhCustomer ghCustomer,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(name="salesman", required=false) String salesman,
								   HttpServletRequest req) {
		QueryWrapper<GhCustomer> queryWrapper = QueryGenerator.initQueryWrapper(ghCustomer, req.getParameterMap());
		
		if(oConvertUtils.isNotEmpty(salesman)) {
			QueryWrapper<GhOrder> orderWrapper = new QueryWrapper<>();
			orderWrapper.eq("del_flag", 0);
			orderWrapper.eq("salesman", salesman);
			orderWrapper.select("DISTINCT company_id");
			List<GhOrder> orders = ghOrderService.list(orderWrapper);
			
			if(orders != null && !orders.isEmpty()) {
				Set<String> customerIds = orders.stream()
					.map(GhOrder::getCompanyId)
					.filter(id -> oConvertUtils.isNotEmpty(id))
					.collect(Collectors.toSet());
				
				if(!customerIds.isEmpty()) {
					queryWrapper.in("id", customerIds);
				} else {
					queryWrapper.eq("1", "0");
				}
			} else {
				queryWrapper.eq("1", "0");
			}
		}
		
		Page<GhCustomer> page = new Page<>(pageNo, pageSize);
		IPage<GhCustomer> pageList = ghCustomerService.page(page, queryWrapper);
		// 业务标签、消费金额、复购（订单数）、企业等级：与地址中心/合同列表同一套订单聚合口径
		if (pageList != null && pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
			List<String> pageCustomerIds = pageList.getRecords().stream()
				.map(GhCustomer::getId)
				.filter(id -> oConvertUtils.isNotEmpty(id))
				.collect(Collectors.toList());
			Map<String, List<GhOrder>> ordersByCustomerId = new HashMap<>();
			if (ghOrderService != null && !pageCustomerIds.isEmpty()) {
				QueryWrapper<GhOrder> orderBatch = new QueryWrapper<>();
				orderBatch.eq("del_flag", 0);
				orderBatch.in("company_id", pageCustomerIds);
				List<GhOrder> allOrders = ghOrderService.list(orderBatch);
				if (allOrders != null) {
					ordersByCustomerId = allOrders.stream().collect(Collectors.groupingBy(GhOrder::getCompanyId));
				}
			}
			for (GhCustomer customer : pageList.getRecords()) {
				String computedBusinessTag = calculateBusinessTag(customer);
				customer.setBusinessTag(computedBusinessTag);
				List<GhOrder> orders = ordersByCustomerId.getOrDefault(customer.getId(), Collections.emptyList());
				fillCustomerListComputedFields(customer, orders, computedBusinessTag);
			}
		}
		return Result.OK(pageList);
	}

	/**
	 * 计算业务标签：公司所有业务类型（去重，逗号分隔），并将业务类型ID转为名称
	 */
	private String calculateBusinessTag(GhCustomer customer) {
		try {
			if (customer == null) {
				return null;
			}
			if (ghOrderService == null || sysCategoryService == null) {
				return customer.getBusinessTag();
			}

			QueryWrapper<GhOrder> wrapper = new QueryWrapper<>();
			wrapper.eq("del_flag", 0);
			// 业务类型不为空且不为空字符串
			wrapper.isNotNull("business_type");
			wrapper.ne("business_type", "");

			// 优先按 company_id（customer.id）查询
			if (oConvertUtils.isNotEmpty(customer.getId())) {
				wrapper.eq("company_id", customer.getId());
			} else if (oConvertUtils.isNotEmpty(customer.getCorporateName())) {
				wrapper.eq("company_name", customer.getCorporateName());
			} else {
				return null;
			}

			List<GhOrder> orders = ghOrderService.list(wrapper);
			if (orders == null || orders.isEmpty()) {
				return null;
			}

			Set<String> businessTypeIds = new HashSet<>();
			for (GhOrder order : orders) {
				if (order != null && oConvertUtils.isNotEmpty(order.getBusinessType())) {
					businessTypeIds.add(order.getBusinessType());
				}
			}

			if (businessTypeIds.isEmpty()) {
				return null;
			}

			// 把业务类型ID转成字典名称
			List<String> businessTypeNames = sysCategoryService.loadDictItem(String.join(",", businessTypeIds), false);
			if (businessTypeNames == null || businessTypeNames.isEmpty()) {
				return null;
			}
			return String.join(",", businessTypeNames);
		} catch (Exception e) {
			log.error("计算业务标签失败", e);
			// 兜底：返回当前数据库字段，避免列表直接报错
			return customer != null ? customer.getBusinessTag() : null;
		}
	}

	/**
	 * 列表/详情：根据订单聚合填充消费金额、订单数（复购信息）、企业等级（与地址中心、合同列表口径一致）
	 */
	private void fillCustomerListComputedFields(GhCustomer customer, List<GhOrder> orders, String businessTagText) {
		try {
			if (orders == null) {
				orders = Collections.emptyList();
			}
			BigDecimal totalSpending = BigDecimal.ZERO;
			for (GhOrder order : orders) {
				if (order == null) {
					continue;
				}
				BigDecimal amount = order.getContractAmount() != null ? order.getContractAmount()
					: (order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
				totalSpending = totalSpending.add(amount);
			}
			customer.setTotalSpending(totalSpending);
			customer.setOrderCount(orders.size());

			int serviceMonths = 0;
			if (!orders.isEmpty()) {
				Date earliest = null;
				for (GhOrder order : orders) {
					if (order != null && order.getCreateTime() != null) {
						if (earliest == null || order.getCreateTime().before(earliest)) {
							earliest = order.getCreateTime();
						}
					}
				}
				if (earliest != null) {
					serviceMonths = (int) calculateMonthsBetween(earliest, new Date());
				}
			}
			boolean hasHighEnd = oConvertUtils.isNotEmpty(businessTagText)
				&& (businessTagText.contains("高端代账") || businessTagText.contains("高新"));
			boolean hasKeXiao = oConvertUtils.isNotEmpty(businessTagText) && businessTagText.contains("科小");
			String level = calculateEnterpriseLevelForCustomer(totalSpending, orders.size(), serviceMonths, hasHighEnd, hasKeXiao);
			customer.setEnterpriseLevel(level);
		} catch (Exception e) {
			log.error("填充客户列表计算字段失败, customerId={}", customer != null ? customer.getId() : null, e);
		}
	}

	private long calculateMonthsBetween(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			return 0;
		}
		java.util.Calendar start = java.util.Calendar.getInstance();
		start.setTime(startDate);
		java.util.Calendar end = java.util.Calendar.getInstance();
		end.setTime(endDate);
		int yearDiff = end.get(java.util.Calendar.YEAR) - start.get(java.util.Calendar.YEAR);
		int monthDiff = end.get(java.util.Calendar.MONTH) - start.get(java.util.Calendar.MONTH);
		return yearDiff * 12L + monthDiff;
	}

	/**
	 * 与 GhAddressCenterController / GhAccountingContractController 中企业等级规则一致
	 */
	private String calculateEnterpriseLevelForCustomer(BigDecimal totalSpending, int purchaseCount, int serviceMonths,
			boolean hasHighEndBusiness, boolean hasKeXiaoBusiness) {
		try {
			double spending = totalSpending != null ? totalSpending.doubleValue() : 0.0;
			int maxLevel = 0;
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
				maxLevel = Math.max(maxLevel, 1);
			}
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
			if (hasKeXiaoBusiness) {
				maxLevel = Math.max(maxLevel, 3);
			}
			if (hasHighEndBusiness) {
				maxLevel = Math.max(maxLevel, 4);
			}
			switch (maxLevel) {
				case 5:
					return "5";
				case 4:
					return "4";
				case 3:
					return "3";
				case 2:
					return "2";
				case 1:
					return "1";
				default:
					return null;
			}
		} catch (Exception e) {
			log.error("计算企业等级失败", e);
			return null;
		}
	}
	
	/**
	 * 添加
	 */
	@AutoLog(value = "客户管理-添加")
	@ApiOperation(value="客户管理-添加", notes="客户管理-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody GhCustomer ghCustomer) {
		ghCustomerService.save(ghCustomer);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "客户管理-编辑")
	@ApiOperation(value="客户管理-编辑", notes="客户管理-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody GhCustomer ghCustomer) {
		ghCustomerService.updateById(ghCustomer);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "客户管理-通过id删除")
	@ApiOperation(value="客户管理-通过id删除", notes="客户管理-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ghCustomerService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 * 批量删除
	 */
	@AutoLog(value = "客户管理-批量删除")
	@ApiOperation(value="客户管理-批量删除", notes="客户管理-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ghCustomerService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 */
	@AutoLog(value = "客户管理-通过id查询")
	@ApiOperation(value="客户管理-通过id查询", notes="客户管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		GhCustomer ghCustomer = ghCustomerService.getById(id);
		if(ghCustomer==null) {
			return Result.error("未找到对应数据");
		}
		String computedBusinessTag = calculateBusinessTag(ghCustomer);
		ghCustomer.setBusinessTag(computedBusinessTag);
		if (ghOrderService != null && oConvertUtils.isNotEmpty(ghCustomer.getId())) {
			QueryWrapper<GhOrder> ow = new QueryWrapper<>();
			ow.eq("del_flag", 0);
			ow.eq("company_id", ghCustomer.getId());
			List<GhOrder> orders = ghOrderService.list(ow);
			fillCustomerListComputedFields(ghCustomer, orders, computedBusinessTag);
		}
		return Result.OK(ghCustomer);
	}
	
	/**
	 * 获取关联企业列表
	 */
	@AutoLog(value = "客户管理-获取关联企业列表")
	@ApiOperation(value="客户管理-获取关联企业列表", notes="客户管理-获取关联企业列表")
	@GetMapping(value = "/getRelatedCompanies")
	public Result<?> getRelatedCompanies(@RequestParam(name="customerId",required=true) String customerId) {
		try {
			GhCustomer customer = ghCustomerService.getById(customerId);
			if(customer == null) {
				return Result.error("客户不存在");
			}
			
			// 获取关联企业ID列表
			String relatedCompanyIds = customer.getRelatedCompanyId();
			if(oConvertUtils.isEmpty(relatedCompanyIds)) {
				return Result.OK(new ArrayList<>());
			}
			
			// 查询关联企业详情
			List<String> idList = Arrays.asList(relatedCompanyIds.split(","));
			List<GhCustomer> relatedCompanies = ghCustomerService.listByIds(idList);
			
			return Result.OK(relatedCompanies);
		} catch (Exception e) {
			log.error("获取关联企业列表失败", e);
			return Result.error("获取关联企业列表失败：" + e.getMessage());
		}
	}
	
	/**
	 * 移除关联企业（双向移除）
	 */
	@AutoLog(value = "客户管理-移除关联企业")
	@ApiOperation(value="客户管理-移除关联企业", notes="客户管理-移除关联企业")
	@DeleteMapping(value = "/removeRelation")
	public Result<?> removeRelation(
			@RequestParam(name="customerId",required=true) String customerId,
			@RequestParam(name="relatedCustomerId",required=true) String relatedCustomerId) {
		try {
			// 验证两个客户都存在
			GhCustomer customer = ghCustomerService.getById(customerId);
			if(customer == null) {
				return Result.error("客户不存在");
			}
			
			GhCustomer relatedCustomer = ghCustomerService.getById(relatedCustomerId);
			if(relatedCustomer == null) {
				return Result.error("关联客户不存在");
			}
			
			// 移除A->B的关联
			String relatedCompanyIds = customer.getRelatedCompanyId();
			if(oConvertUtils.isNotEmpty(relatedCompanyIds)) {
				List<String> idList = new ArrayList<>(Arrays.asList(relatedCompanyIds.split(",")));
				idList.remove(relatedCustomerId);
				customer.setRelatedCompanyId(idList.isEmpty() ? "" : String.join(",", idList));
			}
			
			// 移除B->A的关联（双向移除）
			String reverseRelatedIds = relatedCustomer.getRelatedCompanyId();
			if(oConvertUtils.isNotEmpty(reverseRelatedIds)) {
				List<String> reverseIdList = new ArrayList<>(Arrays.asList(reverseRelatedIds.split(",")));
				reverseIdList.remove(customerId);
				relatedCustomer.setRelatedCompanyId(reverseIdList.isEmpty() ? "" : String.join(",", reverseIdList));
			}
			
			// 批量更新（使用事务保证原子性）
			List<GhCustomer> updateList = new ArrayList<>();
			updateList.add(customer);
			updateList.add(relatedCustomer);
			ghCustomerService.updateBatchById(updateList);
			
			log.info("双向移除关联成功: {} <-> {}", customerId, relatedCustomerId);
			return Result.OK("移除成功");
		} catch (Exception e) {
			log.error("移除关联企业失败", e);
			return Result.error("移除关联企业失败：" + e.getMessage());
		}
	}
	
	/**
	 * 批量设置关联企业（双向关联）
	 */
	@AutoLog(value = "客户管理-批量设置关联企业")
	@ApiOperation(value="客户管理-批量设置关联企业", notes="客户管理-批量设置关联企业")
	@PostMapping(value = "/setRelations")
	public Result<?> setRelations(
			@RequestParam(name="customerId",required=true) String customerId,
			@RequestBody List<String> relatedCompanyIds) {
		try {
			GhCustomer customer = ghCustomerService.getById(customerId);
			if(customer == null) {
				return Result.error("客户不存在");
			}
			
			// 验证不能关联自己
			if(relatedCompanyIds != null && relatedCompanyIds.contains(customerId)) {
				return Result.error("不能关联当前客户自己");
			}
			
			// 获取当前的关联企业ID列表
			Set<String> oldRelatedIds = new HashSet<>();
			if(oConvertUtils.isNotEmpty(customer.getRelatedCompanyId())) {
				oldRelatedIds.addAll(Arrays.asList(customer.getRelatedCompanyId().split(",")));
			}
			
			// 新的关联企业ID列表
			Set<String> newRelatedIds = new HashSet<>();
			if(relatedCompanyIds != null && !relatedCompanyIds.isEmpty()) {
				newRelatedIds.addAll(relatedCompanyIds);
			}
			
			// 找出需要添加的关联（新增的）
			Set<String> toAdd = new HashSet<>(newRelatedIds);
			toAdd.removeAll(oldRelatedIds);
			
			// 找出需要移除的关联（删除的）
			Set<String> toRemove = new HashSet<>(oldRelatedIds);
			toRemove.removeAll(newRelatedIds);
			
			// 需要更新的客户列表
			List<GhCustomer> updateList = new ArrayList<>();
			
			// 更新当前客户的关联企业ID
			customer.setRelatedCompanyId(newRelatedIds.isEmpty() ? "" : String.join(",", newRelatedIds));
			updateList.add(customer);
			
			// 处理新增的双向关联
			for(String relatedId : toAdd) {
				GhCustomer relatedCustomer = ghCustomerService.getById(relatedId);
				if(relatedCustomer != null) {
					Set<String> relatedIds = new HashSet<>();
					if(oConvertUtils.isNotEmpty(relatedCustomer.getRelatedCompanyId())) {
						relatedIds.addAll(Arrays.asList(relatedCustomer.getRelatedCompanyId().split(",")));
					}
					// 添加反向关联
					if(!relatedIds.contains(customerId)) {
						relatedIds.add(customerId);
						relatedCustomer.setRelatedCompanyId(String.join(",", relatedIds));
						updateList.add(relatedCustomer);
					}
				}
			}
			
			// 处理移除的双向关联
			for(String relatedId : toRemove) {
				GhCustomer relatedCustomer = ghCustomerService.getById(relatedId);
				if(relatedCustomer != null) {
					Set<String> relatedIds = new HashSet<>();
					if(oConvertUtils.isNotEmpty(relatedCustomer.getRelatedCompanyId())) {
						relatedIds.addAll(Arrays.asList(relatedCustomer.getRelatedCompanyId().split(",")));
					}
					// 移除反向关联
					if(relatedIds.contains(customerId)) {
						relatedIds.remove(customerId);
						relatedCustomer.setRelatedCompanyId(relatedIds.isEmpty() ? "" : String.join(",", relatedIds));
						updateList.add(relatedCustomer);
					}
				}
			}
			
			// 批量更新（使用事务保证原子性）
			if(!updateList.isEmpty()) {
				ghCustomerService.updateBatchById(updateList);
			}
			
			log.info("双向设置关联成功: 客户ID={}, 新增关联={}, 移除关联={}", customerId, toAdd, toRemove);
			return Result.OK("设置成功");
		} catch (Exception e) {
			log.error("设置关联企业失败", e);
			return Result.error("设置关联企业失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取企业等级详情
	 */
	@AutoLog(value = "客户管理-获取企业等级详情")
	@ApiOperation(value="客户管理-获取企业等级详情", notes="客户管理-获取企业等级详情")
	@GetMapping(value = "/getEnterpriseLevelDetail")
	public Result<?> getEnterpriseLevelDetail(
			@RequestParam(name="customerId",required=true) String customerId) {
		try {
			GhCustomer customer = ghCustomerService.getById(customerId);
			if(customer == null) {
				return Result.error("客户不存在");
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put("customer", customer);
			
			// 获取该客户的所有订单
			if(ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("company_id", customerId);
				orderWrapper.orderByDesc("create_time");
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				
				result.put("orders", orders);
				result.put("orderCount", orders.size());
				
				// 计算总消费金额
				BigDecimal totalAmount = orders.stream()
					.map(order -> order.getContractAmount() != null ? order.getContractAmount() :
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
				result.put("totalAmount", totalAmount);
			}
			
			return Result.OK(result);
		} catch (Exception e) {
			log.error("获取企业等级详情失败", e);
			return Result.error("获取企业等级详情失败：" + e.getMessage());
		}
	}
	
	/**
	 * 客户画像分析统计
	 * @param year 年份
	 * @param month 月份（可选）
	 * @param businessType 业务类型（可选）
	 * @return 统计数据
	 */
	@AutoLog(value = "客户管理-客户画像分析统计")
	@ApiOperation(value="客户管理-客户画像分析统计", notes="客户管理-客户画像分析统计")
	@GetMapping(value = "/getCustomerStatistics")
	public Result<?> getCustomerStatistics(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "businessType", required = false) String businessType) {
		try {
			// 如果没有指定年份，使用当前年份
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			// 构建查询条件 - 查询所有客户（通过订单关联业务类型）
			QueryWrapper<GhCustomer> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			
			// 年份过滤（基于客户创建时间）
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, 0, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date yearStart = calendar.getTime();
			
			calendar.set(year, 11, 31, 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date yearEnd = calendar.getTime();
			
			queryWrapper.ge("create_time", yearStart);
			queryWrapper.le("create_time", yearEnd);
			
			// 月份过滤（如果指定）
			if (month != null && month >= 1 && month <= 12) {
				calendar.set(year, month - 1, 1, 0, 0, 0);
				calendar.set(java.util.Calendar.MILLISECOND, 0);
				Date monthStart = calendar.getTime();
				
				calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
				calendar.set(java.util.Calendar.MILLISECOND, 999);
				Date monthEnd = calendar.getTime();
				
				queryWrapper.ge("create_time", monthStart);
				queryWrapper.le("create_time", monthEnd);
			}
			
			// 查询所有符合条件的客户
			List<GhCustomer> customers = ghCustomerService.list(queryWrapper);
			
			// 如果指定了业务类型，需要通过订单过滤客户
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> customerIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getCompanyId)
					.filter(id -> oConvertUtils.isNotEmpty(id))
					.collect(java.util.stream.Collectors.toSet());
				
				if (!customerIds.isEmpty()) {
					customers = customers.stream()
						.filter(c -> customerIds.contains(c.getId()))
						.collect(java.util.stream.Collectors.toList());
				} else {
					customers = new ArrayList<>();
				}
			}
			
			// 【性能优化】一次性批量查询所有订单，避免N+1查询问题
			List<String> customerIds = customers.stream()
				.map(GhCustomer::getId)
				.collect(java.util.stream.Collectors.toList());
			
			Map<String, List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomer = new HashMap<>();
			if (!customerIds.isEmpty() && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrdersWrapper = new QueryWrapper<>();
				allOrdersWrapper.eq("del_flag", 0);
				allOrdersWrapper.in("company_id", customerIds);
				List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrdersWrapper);
				
				// 按客户ID分组
				ordersByCustomer = allOrders.stream()
					.collect(java.util.stream.Collectors.groupingBy(
						org.jeecg.modules.order.entity.GhOrder::getCompanyId
					));
			}
			
			// 统计数据
			Map<String, Object> result = new HashMap<>();
			
			// 1. 总体统计
			Map<String, Object> statistics = new HashMap<>();
			int totalCount = customers.size();
			BigDecimal totalAmount = BigDecimal.ZERO;
			java.util.Set<String> salesmanSet = new java.util.HashSet<>();
			
			// 计算总客户数（用于计算客户占比）
			QueryWrapper<GhCustomer> totalWrapper = new QueryWrapper<>();
			totalWrapper.eq("del_flag", 0);
			int totalAllCustomers = ghCustomerService.count(totalWrapper);
			
			// 【优化】从内存中的订单Map获取，而不是查询数据库
			for (GhCustomer customer : customers) {
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					BigDecimal amount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					totalAmount = totalAmount.add(amount);
					
					if (oConvertUtils.isNotEmpty(order.getSalesman())) {
						salesmanSet.add(order.getSalesman());
					}
				}
			}
			
			BigDecimal avgAmount = totalCount > 0 ? 
				totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
				BigDecimal.ZERO;
			
			BigDecimal customerRatio = totalAllCustomers > 0 ? 
				new BigDecimal(totalCount)
					.multiply(new BigDecimal(100))
					.divide(new BigDecimal(totalAllCustomers), 2, BigDecimal.ROUND_HALF_UP) : 
				BigDecimal.ZERO;
			
			statistics.put("totalCount", totalCount);
			statistics.put("totalAmount", totalAmount);
			statistics.put("avgAmount", avgAmount);
			statistics.put("customerRatio", customerRatio);
			
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
				
				for (GhCustomer customer : customers) {
					if (customer.getCreateTime() != null && 
						!customer.getCreateTime().before(mStart) && 
						!customer.getCreateTime().after(mEnd)) {
						count++;
						
						// 【优化】从内存中的订单Map获取
						List<org.jeecg.modules.order.entity.GhOrder> orders = 
							ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
						
						for (org.jeecg.modules.order.entity.GhOrder order : orders) {
							// 只统计该月创建的订单
							if (order.getCreateTime() != null &&
								!order.getCreateTime().before(mStart) &&
								!order.getCreateTime().after(mEnd)) {
								BigDecimal orderAmount = order.getContractAmount() != null ? 
									order.getContractAmount() : 
									(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
								amount = amount.add(orderAmount);
							}
						}
					}
				}
				
				Map<String, Object> monthData = new HashMap<>();
				monthData.put("month", m + "月");
				monthData.put("count", count);
				monthData.put("amount", amount);
				monthlyTrend.add(monthData);
			}
			
			// 3. 业务类型分布（通过订单统计）
			Map<String, Map<String, Object>> businessTypeMap = new HashMap<>();
			// 【性能优化】先收集所有业务类型ID，然后批量查询字典名称
			Map<String, String> businessTypeNameCache = new HashMap<>();
			
			// 【优化】从内存中的订单Map获取
			for (GhCustomer customer : customers) {
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					String bt = order.getBusinessType();
					String btName = "其他";
					
					if (oConvertUtils.isNotEmpty(bt)) {
						// 先从缓存中获取
						if (businessTypeNameCache.containsKey(bt)) {
							btName = businessTypeNameCache.get(bt);
						} else {
							// 缓存中没有，查询数据库并缓存
							try {
								if (sysCategoryService != null) {
									java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
									if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
										btName = names.get(0);
									} else {
										btName = bt; // 如果查不到名称，使用ID
									}
								} else {
									btName = bt;
								}
								businessTypeNameCache.put(bt, btName);
							} catch (Exception e) {
								log.warn("获取业务类型名称失败，业务类型ID: " + bt, e);
								btName = bt;
								businessTypeNameCache.put(bt, btName);
							}
						}
					}
					
					Map<String, Object> btData = businessTypeMap.getOrDefault(btName, new HashMap<>());
					int count = (Integer) btData.getOrDefault("count", 0);
					BigDecimal amount = (BigDecimal) btData.getOrDefault("amount", BigDecimal.ZERO);
					
					BigDecimal orderAmount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					
					btData.put("name", btName);
					btData.put("count", count + 1);
					btData.put("amount", amount.add(orderAmount));
					businessTypeMap.put(btName, btData);
				}
			}
			
			List<Map<String, Object>> businessTypeList = new ArrayList<>(businessTypeMap.values());
			
			// 4. 业务员排名（按客户数）
			Map<String, Map<String, Object>> salesmanMap = new HashMap<>();
			// 【优化】从内存中的订单Map获取
			for (GhCustomer customer : customers) {
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				// 获取该客户的主要业务员（取第一个订单的业务员）
				String sm = "未分配";
				if (!orders.isEmpty() && oConvertUtils.isNotEmpty(orders.get(0).getSalesman())) {
					sm = orders.get(0).getSalesman();
				}
				
				Map<String, Object> smData = salesmanMap.getOrDefault(sm, new HashMap<>());
				int count = (Integer) smData.getOrDefault("count", 0);
				BigDecimal amount = (BigDecimal) smData.getOrDefault("amount", BigDecimal.ZERO);
				
				// 计算该客户的总订单金额
				BigDecimal customerAmount = BigDecimal.ZERO;
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					BigDecimal orderAmount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					customerAmount = customerAmount.add(orderAmount);
				}
				
				smData.put("salesman", sm);
				smData.put("count", count + 1);
				smData.put("amount", amount.add(customerAmount));
				salesmanMap.put(sm, smData);
			}
			
			List<Map<String, Object>> salesmanRank = new ArrayList<>(salesmanMap.values());
			// 按客户数排序
			salesmanRank.sort((a, b) -> {
				Integer countA = (Integer) a.get("count");
				Integer countB = (Integer) b.get("count");
				return countB.compareTo(countA);
			});
			
			// 5. 每日趋势数据（如果指定了月份，显示该月每日；否则显示全年每月）
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
					
					for (GhCustomer customer : customers) {
						if (customer.getCreateTime() != null && 
							!customer.getCreateTime().before(dStart) && 
							!customer.getCreateTime().after(dEnd)) {
							count++;
							
							// 【优化】从内存中的订单Map获取
							List<org.jeecg.modules.order.entity.GhOrder> orders = 
								ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
							
							for (org.jeecg.modules.order.entity.GhOrder order : orders) {
								// 只统计该日创建的订单
								if (order.getCreateTime() != null &&
									!order.getCreateTime().before(dStart) &&
									!order.getCreateTime().after(dEnd)) {
									BigDecimal orderAmount = order.getContractAmount() != null ? 
										order.getContractAmount() : 
										(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
									amount = amount.add(orderAmount);
								}
							}
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
			log.error("获取客户画像统计数据失败", e);
			return Result.error("获取统计数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取业务人员客户月度明细数据
	 * @param year 年份
	 * @param month 月份（可选）
	 * @param businessType 业务类型（可选）
	 * @return 业务人员月度明细数据
	 */
	@AutoLog(value = "客户管理-获取业务人员客户月度明细")
	@ApiOperation(value="客户管理-获取业务人员客户月度明细", notes="客户管理-获取业务人员客户月度明细")
	@GetMapping(value = "/getCustomerSalesmanMonthlyDetail")
	public Result<?> getCustomerSalesmanMonthlyDetail(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "businessType", required = false) String businessType) {
		try {
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			QueryWrapper<GhCustomer> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, 0, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date yearStart = calendar.getTime();
			
			calendar.set(year, 11, 31, 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date yearEnd = calendar.getTime();
			
			queryWrapper.ge("create_time", yearStart);
			queryWrapper.le("create_time", yearEnd);
			
			// 如果指定了业务类型，需要通过订单过滤客户
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> customerIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getCompanyId)
					.filter(id -> oConvertUtils.isNotEmpty(id))
					.collect(java.util.stream.Collectors.toSet());
				
				if (!customerIds.isEmpty()) {
					queryWrapper.in("id", customerIds);
				} else {
					queryWrapper.eq("1", "0");
				}
			}
			
			List<GhCustomer> customers = ghCustomerService.list(queryWrapper);
			
			// 【性能优化】一次性批量查询所有订单，避免N+1查询问题
			List<String> customerIds = customers.stream()
				.map(GhCustomer::getId)
				.collect(java.util.stream.Collectors.toList());
			
			Map<String, List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomer = new HashMap<>();
			if (!customerIds.isEmpty() && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrdersWrapper = new QueryWrapper<>();
				allOrdersWrapper.eq("del_flag", 0);
				allOrdersWrapper.in("company_id", customerIds);
				allOrdersWrapper.orderByAsc("create_time");
				List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrdersWrapper);
				
				// 按客户ID分组
				ordersByCustomer = allOrders.stream()
					.collect(java.util.stream.Collectors.groupingBy(
						org.jeecg.modules.order.entity.GhOrder::getCompanyId
					));
			}
			
			Map<String, Map<Integer, Map<String, Object>>> salesmanMonthlyMap = new HashMap<>();
			
			for (GhCustomer customer : customers) {
				if (customer.getCreateTime() == null) {
					continue;
				}
				
				// 【优化】从内存中的订单Map获取
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				// 获取该客户的主要业务员
				String salesman = "未分配";
				if (!orders.isEmpty() && oConvertUtils.isNotEmpty(orders.get(0).getSalesman())) {
					salesman = orders.get(0).getSalesman();
				}
				
				calendar.setTime(customer.getCreateTime());
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
				
				// 【优化】计算该客户的总订单金额（从已获取的订单列表）
				BigDecimal customerAmount = BigDecimal.ZERO;
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					BigDecimal orderAmount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					customerAmount = customerAmount.add(orderAmount);
				}
				
				monthData.put("count", count + 1);
				monthData.put("amount", amount.add(customerAmount));
				
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
			log.error("获取业务人员客户月度明细数据失败", e);
			return Result.error("获取数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取业务人员客户月度客户列表
	 * @param year 年份
	 * @param month 月份
	 * @param salesman 业务人员
	 * @param businessType 业务类型（可选）
	 * @param pageNo 页码
	 * @param pageSize 每页数量
	 * @return 客户列表
	 */
	@AutoLog(value = "客户管理-获取业务人员客户月度客户列表")
	@ApiOperation(value="客户管理-获取业务人员客户月度客户列表", notes="客户管理-获取业务人员客户月度客户列表")
	@GetMapping(value = "/getCustomerSalesmanMonthlyCustomers")
	public Result<?> getCustomerSalesmanMonthlyCustomers(
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
			
			QueryWrapper<GhCustomer> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, month - 1, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date monthStart = calendar.getTime();
			
			calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date monthEnd = calendar.getTime();
			
			// 查询客户创建时间在指定月份内的客户
			queryWrapper.ge("create_time", monthStart);
			queryWrapper.le("create_time", monthEnd);
			
			// 先查询符合条件的客户
			List<GhCustomer> allCustomers = ghCustomerService.list(queryWrapper);
			
			// 过滤出第一个订单的业务员匹配的客户（与表格明细统计逻辑一致）
			java.util.List<GhCustomer> filteredCustomers = new ArrayList<>();
			if (ghOrderService != null) {
				// 批量查询所有客户的订单
				java.util.List<String> customerIds = allCustomers.stream()
					.map(GhCustomer::getId)
					.collect(java.util.stream.Collectors.toList());
				
				Map<String, List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomer = new HashMap<>();
				if (!customerIds.isEmpty()) {
					QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrdersWrapper = new QueryWrapper<>();
					allOrdersWrapper.eq("del_flag", 0);
					allOrdersWrapper.in("company_id", customerIds);
					allOrdersWrapper.orderByAsc("create_time");
					List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrdersWrapper);
					
					// 如果指定了业务类型，过滤订单
					if (oConvertUtils.isNotEmpty(businessType)) {
						allOrders = allOrders.stream()
							.filter(order -> businessType.equals(order.getBusinessType()))
							.collect(java.util.stream.Collectors.toList());
					}
					
					// 按客户ID分组
					ordersByCustomer = allOrders.stream()
						.collect(java.util.stream.Collectors.groupingBy(
							org.jeecg.modules.order.entity.GhOrder::getCompanyId
						));
				}
				
				// 过滤出第一个订单的业务员匹配的客户
				for (GhCustomer customer : allCustomers) {
					List<org.jeecg.modules.order.entity.GhOrder> orders = 
						ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
					
					// 获取该客户的第一个订单的业务员（与表格明细统计逻辑一致）
					String firstOrderSalesman = "未分配";
					if (!orders.isEmpty() && oConvertUtils.isNotEmpty(orders.get(0).getSalesman())) {
						firstOrderSalesman = orders.get(0).getSalesman();
					}
					
					// 如果第一个订单的业务员匹配，则包含该客户
					if (salesman.equals(firstOrderSalesman)) {
						filteredCustomers.add(customer);
					}
				}
			} else {
				// 如果没有订单服务，返回空列表
				filteredCustomers = new ArrayList<>();
			}
			
			// 使用内存分页
			int total = filteredCustomers.size();
			int start = (pageNo - 1) * pageSize;
			int end = Math.min(start + pageSize, total);
			List<GhCustomer> pageData = start < total ? filteredCustomers.subList(start, end) : new ArrayList<>();
			
			Page<GhCustomer> page = new Page<>(pageNo, pageSize, total);
			page.setRecords(pageData);
			
			return Result.OK(page);
		} catch (Exception e) {
			log.error("获取业务人员客户月度客户列表失败", e);
			return Result.error("获取数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 客户价值分析统计
	 * @param year 年份
	 * @param month 月份（可选）
	 * @param businessType 业务类型（可选）
	 * @return 统计数据
	 */
	@AutoLog(value = "客户管理-客户价值分析统计")
	@ApiOperation(value="客户管理-客户价值分析统计", notes="客户管理-客户价值分析统计")
	@GetMapping(value = "/getValueStatistics")
	public Result<?> getValueStatistics(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "businessType", required = false) String businessType) {
		try {
			// 如果没有指定年份，使用当前年份
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			// 构建查询条件 - 统计所有客户（不筛选金额）
			QueryWrapper<GhCustomer> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			
			// 年份过滤（基于客户创建时间）
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, 0, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date yearStart = calendar.getTime();
			
			calendar.set(year, 11, 31, 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date yearEnd = calendar.getTime();
			
			queryWrapper.ge("create_time", yearStart);
			queryWrapper.le("create_time", yearEnd);
			
			// 月份过滤（如果指定）
			if (month != null && month >= 1 && month <= 12) {
				calendar.set(year, month - 1, 1, 0, 0, 0);
				calendar.set(java.util.Calendar.MILLISECOND, 0);
				Date monthStart = calendar.getTime();
				
				calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
				calendar.set(java.util.Calendar.MILLISECOND, 999);
				Date monthEnd = calendar.getTime();
				
				queryWrapper.ge("create_time", monthStart);
				queryWrapper.le("create_time", monthEnd);
			}
			
			// 查询所有符合条件的客户
			List<GhCustomer> customers = ghCustomerService.list(queryWrapper);
			
			// 如果指定了业务类型，需要通过订单过滤客户
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> customerIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getCompanyId)
					.filter(id -> oConvertUtils.isNotEmpty(id))
					.collect(java.util.stream.Collectors.toSet());
				
				if (!customerIds.isEmpty()) {
					customers = customers.stream()
						.filter(c -> customerIds.contains(c.getId()))
						.collect(java.util.stream.Collectors.toList());
				} else {
					customers = new ArrayList<>();
				}
			}
			
			// 【性能优化】批量查询所有订单，避免N+1查询
			List<String> customerIds = customers.stream()
				.map(GhCustomer::getId)
				.collect(java.util.stream.Collectors.toList());
			
			Map<String, List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomer = new HashMap<>();
			if (!customerIds.isEmpty() && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrdersWrapper = new QueryWrapper<>();
				allOrdersWrapper.eq("del_flag", 0);
				allOrdersWrapper.in("company_id", customerIds);
				List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrdersWrapper);
				
				// 按客户ID分组
				ordersByCustomer = allOrders.stream()
					.collect(java.util.stream.Collectors.groupingBy(
						org.jeecg.modules.order.entity.GhOrder::getCompanyId
					));
			}
			
			// 计算所有客户的价值（不筛选金额）
			Map<String, BigDecimal> customerValueMap = new HashMap<>();
			
			for (GhCustomer customer : customers) {
				BigDecimal totalValue = BigDecimal.ZERO;
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					BigDecimal amount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					totalValue = totalValue.add(amount);
				}
				
				// 统计所有客户，不筛选金额
				customerValueMap.put(customer.getId(), totalValue);
			}
			
			// 统计数据
			Map<String, Object> result = new HashMap<>();
			
			// 1. 总体统计
			Map<String, Object> statistics = new HashMap<>();
			int totalCount = customers.size();
			BigDecimal totalAmount = BigDecimal.ZERO;
			java.util.Set<String> salesmanSet = new java.util.HashSet<>();
			
			// 计算总客户数（用于计算价值占比）
			QueryWrapper<GhCustomer> totalWrapper = new QueryWrapper<>();
			totalWrapper.eq("del_flag", 0);
			int totalAllCustomers = ghCustomerService.count(totalWrapper);
			
			// 计算每个客户的总价值
			for (GhCustomer customer : customers) {
				BigDecimal value = customerValueMap.get(customer.getId());
				if (value != null) {
					totalAmount = totalAmount.add(value);
				}
				
				// 【优化】从已经批量查询的订单Map中获取业务员
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				if (!orders.isEmpty() && oConvertUtils.isNotEmpty(orders.get(0).getSalesman())) {
					salesmanSet.add(orders.get(0).getSalesman());
				}
			}
			
			BigDecimal avgAmount = totalCount > 0 ? 
				totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
				BigDecimal.ZERO;
			
			// 计算所有客户的总价值（用于计算价值占比）
			BigDecimal totalAllValue = BigDecimal.ZERO;
			if (ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrderWrapper = new QueryWrapper<>();
				allOrderWrapper.eq("del_flag", 0);
				List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrderWrapper);
				for (org.jeecg.modules.order.entity.GhOrder order : allOrders) {
					BigDecimal amount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					totalAllValue = totalAllValue.add(amount);
				}
			}
			
			BigDecimal valueRatio = totalAllValue.compareTo(BigDecimal.ZERO) > 0 ? 
				totalAmount
					.multiply(new BigDecimal(100))
					.divide(totalAllValue, 2, BigDecimal.ROUND_HALF_UP) : 
				BigDecimal.ZERO;
			
			statistics.put("totalCount", totalCount);
			statistics.put("totalAmount", totalAmount);
			statistics.put("avgAmount", avgAmount);
			statistics.put("valueRatio", valueRatio);
			
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
				
				for (GhCustomer customer : customers) {
					if (customer.getCreateTime() != null && 
						!customer.getCreateTime().before(mStart) && 
						!customer.getCreateTime().after(mEnd)) {
						count++;
						
						// 计算该客户在该月的订单金额
						BigDecimal customerValue = customerValueMap.get(customer.getId());
						if (customerValue != null) {
							amount = amount.add(customerValue);
						}
					}
				}
				
				Map<String, Object> monthData = new HashMap<>();
				monthData.put("month", m + "月");
				monthData.put("count", count);
				monthData.put("amount", amount);
				monthlyTrend.add(monthData);
			}
			
			// 3. 业务类型分布（通过订单统计）
			Map<String, Map<String, Object>> businessTypeMap = new HashMap<>();
			// 【性能优化】使用缓存避免重复查询字典
			Map<String, String> businessTypeNameCache = new HashMap<>();
			
			// 【优化】从内存中的订单Map获取
			for (GhCustomer customer : customers) {
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					String bt = order.getBusinessType();
					String btName = "其他";
					
					if (oConvertUtils.isNotEmpty(bt)) {
						// 先从缓存中获取
						if (businessTypeNameCache.containsKey(bt)) {
							btName = businessTypeNameCache.get(bt);
						} else {
							// 缓存中没有，查询数据库并缓存
							try {
								if (sysCategoryService != null) {
									java.util.List<String> names = sysCategoryService.loadDictItem(bt, false);
									if (names != null && !names.isEmpty() && oConvertUtils.isNotEmpty(names.get(0))) {
										btName = names.get(0);
									} else {
										btName = bt;
									}
								} else {
									btName = bt;
								}
								businessTypeNameCache.put(bt, btName);
							} catch (Exception e) {
								log.warn("获取业务类型名称失败，业务类型ID: " + bt, e);
								btName = bt;
								businessTypeNameCache.put(bt, btName);
							}
						}
					}
					
					Map<String, Object> btData = businessTypeMap.getOrDefault(btName, new HashMap<>());
					int count = (Integer) btData.getOrDefault("count", 0);
					BigDecimal amount = (BigDecimal) btData.getOrDefault("amount", BigDecimal.ZERO);
					
					BigDecimal orderAmount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					
					btData.put("name", btName);
					btData.put("count", count + 1);
					btData.put("amount", amount.add(orderAmount));
					businessTypeMap.put(btName, btData);
				}
			}
			
			List<Map<String, Object>> businessTypeList = new ArrayList<>(businessTypeMap.values());
			
			// 4. 客户价值排名（Top 10）
			List<Map<String, Object>> customerValueRank = new ArrayList<>();
			for (GhCustomer customer : customers) {
				BigDecimal customerValue = customerValueMap.get(customer.getId());
				if (customerValue == null) {
					customerValue = BigDecimal.ZERO;
				}
				
				Map<String, Object> customerData = new HashMap<>();
				customerData.put("customerName", customer.getCorporateName() != null ? customer.getCorporateName() : "未命名客户");
				customerData.put("value", customerValue);
				customerValueRank.add(customerData);
			}
			
			// 按客户价值（金额）降序排序
			customerValueRank.sort((a, b) -> {
				BigDecimal valueA = (BigDecimal) a.get("value");
				BigDecimal valueB = (BigDecimal) b.get("value");
				return valueB.compareTo(valueA);
			});
			
			// 只取Top 10
			if (customerValueRank.size() > 10) {
				customerValueRank = customerValueRank.subList(0, 10);
			}
			
			// 5. 每日趋势数据（如果指定了月份，显示该月每日；否则显示全年每月）
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
					
					for (GhCustomer customer : customers) {
						if (customer.getCreateTime() != null && 
							!customer.getCreateTime().before(dStart) && 
							!customer.getCreateTime().after(dEnd)) {
							count++;
							
							BigDecimal customerValue = customerValueMap.get(customer.getId());
							if (customerValue != null) {
								amount = amount.add(customerValue);
							}
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
			result.put("customerValueRank", customerValueRank);
			result.put("dailyTrend", dailyTrend);
			
			return Result.OK(result);
		} catch (Exception e) {
			log.error("获取客户价值统计数据失败", e);
			return Result.error("获取统计数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取业务人员价值月度明细数据
	 * @param year 年份
	 * @param month 月份（可选）
	 * @param businessType 业务类型（可选）
	 * @return 业务人员月度明细数据
	 */
	@AutoLog(value = "客户管理-获取业务人员价值月度明细")
	@ApiOperation(value="客户管理-获取业务人员价值月度明细", notes="客户管理-获取业务人员价值月度明细")
	@GetMapping(value = "/getValueSalesmanMonthlyDetail")
	public Result<?> getValueSalesmanMonthlyDetail(
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "businessType", required = false) String businessType) {
		try {
			if (year == null) {
				year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			
			QueryWrapper<GhCustomer> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, 0, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date yearStart = calendar.getTime();
			
			calendar.set(year, 11, 31, 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date yearEnd = calendar.getTime();
			
			queryWrapper.ge("create_time", yearStart);
			queryWrapper.le("create_time", yearEnd);
			
			// 如果指定了业务类型，需要通过订单过滤客户
			if (oConvertUtils.isNotEmpty(businessType) && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> orderWrapper = new QueryWrapper<>();
				orderWrapper.eq("del_flag", 0);
				orderWrapper.eq("business_type", businessType);
				List<org.jeecg.modules.order.entity.GhOrder> orders = ghOrderService.list(orderWrapper);
				java.util.Set<String> customerIds = orders.stream()
					.map(org.jeecg.modules.order.entity.GhOrder::getCompanyId)
					.filter(id -> oConvertUtils.isNotEmpty(id))
					.collect(java.util.stream.Collectors.toSet());
				
				if (!customerIds.isEmpty()) {
					queryWrapper.in("id", customerIds);
				} else {
					queryWrapper.eq("1", "0");
				}
			}
			
			List<GhCustomer> customers = ghCustomerService.list(queryWrapper);
			
			// 【性能优化】批量查询所有订单，避免N+1查询
			List<String> customerIds = customers.stream()
				.map(GhCustomer::getId)
				.collect(java.util.stream.Collectors.toList());
			
			Map<String, List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomer = new HashMap<>();
			if (!customerIds.isEmpty() && ghOrderService != null) {
				QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrdersWrapper = new QueryWrapper<>();
				allOrdersWrapper.eq("del_flag", 0);
				allOrdersWrapper.in("company_id", customerIds);
				allOrdersWrapper.orderByAsc("create_time");
				List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrdersWrapper);
				
				// 按客户ID分组
				ordersByCustomer = allOrders.stream()
					.collect(java.util.stream.Collectors.groupingBy(
						org.jeecg.modules.order.entity.GhOrder::getCompanyId
					));
			}
			
			// 计算所有客户的价值（不筛选金额）
			Map<String, BigDecimal> customerValueMap = new HashMap<>();
			
			for (GhCustomer customer : customers) {
				BigDecimal totalValue = BigDecimal.ZERO;
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				
				for (org.jeecg.modules.order.entity.GhOrder order : orders) {
					BigDecimal amount = order.getContractAmount() != null ? 
						order.getContractAmount() : 
						(order.getOrderAmount() != null ? order.getOrderAmount() : BigDecimal.ZERO);
					totalValue = totalValue.add(amount);
				}
				
				// 统计所有客户，不筛选金额
				customerValueMap.put(customer.getId(), totalValue);
			}
			
			Map<String, Map<Integer, Map<String, Object>>> salesmanMonthlyMap = new HashMap<>();
			
			for (GhCustomer customer : customers) {
				if (customer.getCreateTime() == null) {
					continue;
				}
				
				// 【优化】从已经批量查询的订单Map中获取业务员
				String salesman = "未分配";
				List<org.jeecg.modules.order.entity.GhOrder> orders = 
					ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
				if (!orders.isEmpty() && oConvertUtils.isNotEmpty(orders.get(0).getSalesman())) {
					salesman = orders.get(0).getSalesman();
				}
				
				calendar.setTime(customer.getCreateTime());
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
				
				BigDecimal customerValue = customerValueMap.get(customer.getId());
				if (customerValue == null) {
					customerValue = BigDecimal.ZERO;
				}
				
				monthData.put("count", count + 1);
				monthData.put("amount", amount.add(customerValue));
				
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
			log.error("获取业务人员价值月度明细数据失败", e);
			return Result.error("获取数据失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取业务人员价值月度客户列表
	 * @param year 年份
	 * @param month 月份
	 * @param salesman 业务人员
	 * @param businessType 业务类型（可选）
	 * @param pageNo 页码
	 * @param pageSize 每页数量
	 * @return 客户列表
	 */
	@AutoLog(value = "客户管理-获取业务人员价值月度客户列表")
	@ApiOperation(value="客户管理-获取业务人员价值月度客户列表", notes="客户管理-获取业务人员价值月度客户列表")
	@GetMapping(value = "/getValueSalesmanMonthlyCustomers")
	public Result<?> getValueSalesmanMonthlyCustomers(
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
			
			QueryWrapper<GhCustomer> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 0);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, month - 1, 1, 0, 0, 0);
			calendar.set(java.util.Calendar.MILLISECOND, 0);
			Date monthStart = calendar.getTime();
			
			calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
			calendar.set(java.util.Calendar.MILLISECOND, 999);
			Date monthEnd = calendar.getTime();
			
			// 查询客户创建时间在指定月份内的客户
			queryWrapper.ge("create_time", monthStart);
			queryWrapper.le("create_time", monthEnd);
			
			// 先查询符合条件的客户
			List<GhCustomer> allCustomers = ghCustomerService.list(queryWrapper);
			
			// 过滤出第一个订单的业务员匹配的客户（与表格明细统计逻辑一致）
			java.util.List<GhCustomer> filteredCustomers = new ArrayList<>();
			if (ghOrderService != null) {
				// 批量查询所有客户的订单
				java.util.List<String> customerIds = allCustomers.stream()
					.map(GhCustomer::getId)
					.collect(java.util.stream.Collectors.toList());
				
				Map<String, List<org.jeecg.modules.order.entity.GhOrder>> ordersByCustomer = new HashMap<>();
				if (!customerIds.isEmpty()) {
					QueryWrapper<org.jeecg.modules.order.entity.GhOrder> allOrdersWrapper = new QueryWrapper<>();
					allOrdersWrapper.eq("del_flag", 0);
					allOrdersWrapper.in("company_id", customerIds);
					allOrdersWrapper.orderByAsc("create_time");
					List<org.jeecg.modules.order.entity.GhOrder> allOrders = ghOrderService.list(allOrdersWrapper);
					
					// 如果指定了业务类型，过滤订单
					if (oConvertUtils.isNotEmpty(businessType)) {
						allOrders = allOrders.stream()
							.filter(order -> businessType.equals(order.getBusinessType()))
							.collect(java.util.stream.Collectors.toList());
					}
					
					// 按客户ID分组
					ordersByCustomer = allOrders.stream()
						.collect(java.util.stream.Collectors.groupingBy(
							org.jeecg.modules.order.entity.GhOrder::getCompanyId
						));
				}
				
				// 过滤出第一个订单的业务员匹配的客户
				for (GhCustomer customer : allCustomers) {
					List<org.jeecg.modules.order.entity.GhOrder> orders = 
						ordersByCustomer.getOrDefault(customer.getId(), new ArrayList<>());
					
					// 获取该客户的第一个订单的业务员（与表格明细统计逻辑一致）
					String firstOrderSalesman = "未分配";
					if (!orders.isEmpty() && oConvertUtils.isNotEmpty(orders.get(0).getSalesman())) {
						firstOrderSalesman = orders.get(0).getSalesman();
					}
					
					// 如果第一个订单的业务员匹配，则包含该客户
					if (salesman.equals(firstOrderSalesman)) {
						filteredCustomers.add(customer);
					}
				}
			} else {
				// 如果没有订单服务，返回空列表
				filteredCustomers = new ArrayList<>();
			}
			
			// 使用内存分页
			int total = filteredCustomers.size();
			int start = (pageNo - 1) * pageSize;
			int end = Math.min(start + pageSize, total);
			List<GhCustomer> pageData = start < total ? filteredCustomers.subList(start, end) : new ArrayList<>();
			
			Page<GhCustomer> page = new Page<>(pageNo, pageSize, total);
			page.setRecords(pageData);
			
			return Result.OK(page);
		} catch (Exception e) {
			log.error("获取业务人员价值月度客户列表失败", e);
			return Result.error("获取数据失败：" + e.getMessage());
		}
	}
}
