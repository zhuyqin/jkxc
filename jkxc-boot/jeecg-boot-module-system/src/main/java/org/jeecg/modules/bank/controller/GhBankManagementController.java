package org.jeecg.modules.bank.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.bank.entity.GhBankManagement;
import org.jeecg.modules.bank.service.IGhBankManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 收款账号管理
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Api(tags="收款账号管理")
@RestController
@RequestMapping("/bank/ghBankManagement")
@Slf4j
public class GhBankManagementController extends JeecgController<GhBankManagement, IGhBankManagementService> {
	@Autowired
	private IGhBankManagementService ghBankManagementService;
	
	/**
	 * 分页列表查询
	 *
	 * @param ghBankManagement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "收款账号管理-分页列表查询")
	@ApiOperation(value="收款账号管理-分页列表查询", notes="收款账号管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(GhBankManagement ghBankManagement,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<GhBankManagement> queryWrapper = QueryGenerator.initQueryWrapper(ghBankManagement, req.getParameterMap());
		queryWrapper.eq("del_flag", 0);
		Page<GhBankManagement> page = new Page<GhBankManagement>(pageNo, pageSize);
		IPage<GhBankManagement> pageList = ghBankManagementService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 * 查询所有收款账号列表（不分页，用于级联选择器）
	 *
	 * @return
	 */
	@AutoLog(value = "收款账号管理-查询所有")
	@ApiOperation(value="收款账号管理-查询所有", notes="收款账号管理-查询所有（不分页）")
	@GetMapping(value = "/listAll")
	public Result<?> listAll() {
		QueryWrapper<GhBankManagement> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("del_flag", 0);
		queryWrapper.eq("hidden", "0"); // 只查询未隐藏的
		queryWrapper.orderByDesc("create_time");
		List<GhBankManagement> list = ghBankManagementService.list(queryWrapper);
		return Result.OK(list);
	}
	
	/**
	 * 根据收款方式查询收款账号列表（用于订单管理联动）
	 *
	 * @param paymentMethod 收款方式
	 * @return
	 */
	@AutoLog(value = "收款账号管理-根据收款方式查询")
	@ApiOperation(value="收款账号管理-根据收款方式查询", notes="收款账号管理-根据收款方式查询")
	@GetMapping(value = "/listByPaymentMethod")
	public Result<?> listByPaymentMethod(@RequestParam(name="paymentMethod", required=true) String paymentMethod) {
		QueryWrapper<GhBankManagement> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("payment_method", paymentMethod);
		queryWrapper.eq("del_flag", 0);
		queryWrapper.eq("hidden", "0"); // 只查询未隐藏的
		queryWrapper.orderByDesc("create_time");
		List<GhBankManagement> list = ghBankManagementService.list(queryWrapper);
		return Result.OK(list);
	}
	
	/**
	 *   添加
	 *
	 * @param ghBankManagement
	 * @return
	 */
	@AutoLog(value = "收款账号管理-添加")
	@ApiOperation(value="收款账号管理-添加", notes="收款账号管理-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody GhBankManagement ghBankManagement) {
		ghBankManagement.setCreateTime(new Date());
		ghBankManagement.setDelFlag(0);
		ghBankManagementService.save(ghBankManagement);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param ghBankManagement
	 * @return
	 */
	@AutoLog(value = "收款账号管理-编辑")
	@ApiOperation(value="收款账号管理-编辑", notes="收款账号管理-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody GhBankManagement ghBankManagement) {
		ghBankManagement.setUpdateTime(new Date());
		ghBankManagementService.updateById(ghBankManagement);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "收款账号管理-通过id删除")
	@ApiOperation(value="收款账号管理-通过id删除", notes="收款账号管理-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		GhBankManagement ghBankManagement = ghBankManagementService.getById(id);
		if (ghBankManagement != null) {
			ghBankManagement.setDelFlag(1);
			ghBankManagement.setUpdateTime(new Date());
			ghBankManagementService.updateById(ghBankManagement);
		}
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "收款账号管理-批量删除")
	@ApiOperation(value="收款账号管理-批量删除", notes="收款账号管理-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		List<String> idList = Arrays.asList(ids.split(","));
		for (String id : idList) {
			GhBankManagement ghBankManagement = ghBankManagementService.getById(id);
			if (ghBankManagement != null) {
				ghBankManagement.setDelFlag(1);
				ghBankManagement.setUpdateTime(new Date());
				ghBankManagementService.updateById(ghBankManagement);
			}
		}
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "收款账号管理-通过id查询")
	@ApiOperation(value="收款账号管理-通过id查询", notes="收款账号管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		GhBankManagement ghBankManagement = ghBankManagementService.getById(id);
		if(ghBankManagement==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(ghBankManagement);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param ghBankManagement
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhBankManagement ghBankManagement) {
        return super.exportXls(request, ghBankManagement, GhBankManagement.class, "收款账号管理");
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
        return super.importExcel(request, response, GhBankManagement.class);
    }

}

