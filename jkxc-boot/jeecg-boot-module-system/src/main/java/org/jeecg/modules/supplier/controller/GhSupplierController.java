package org.jeecg.modules.supplier.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.supplier.entity.GhSupplier;
import org.jeecg.modules.supplier.service.IGhSupplierService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 供货商地址信息
 * @Author: jeecg-boot
 * @Date:   2022-02-23
 * @Version: V1.0
 */
@Api(tags="供货商地址信息")
@RestController
@RequestMapping("/supplier/ghSupplier")
@Slf4j
public class GhSupplierController extends JeecgController<GhSupplier, IGhSupplierService> {
	@Autowired
	private IGhSupplierService ghSupplierService;
	
	/**
	 * 分页列表查询
	 *
	 * @param ghSupplier
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "供货商地址信息-分页列表查询")
	@ApiOperation(value="供货商地址信息-分页列表查询", notes="供货商地址信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(GhSupplier ghSupplier,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<GhSupplier> queryWrapper = QueryGenerator.initQueryWrapper(ghSupplier, req.getParameterMap());
		Page<GhSupplier> page = new Page<GhSupplier>(pageNo, pageSize);
		IPage<GhSupplier> pageList = ghSupplierService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param ghSupplier
	 * @return
	 */
	@AutoLog(value = "供货商地址信息-添加")
	@ApiOperation(value="供货商地址信息-添加", notes="供货商地址信息-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody GhSupplier ghSupplier) {
		ghSupplierService.save(ghSupplier);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param ghSupplier
	 * @return
	 */
	@AutoLog(value = "供货商地址信息-编辑")
	@ApiOperation(value="供货商地址信息-编辑", notes="供货商地址信息-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody GhSupplier ghSupplier) {
		ghSupplierService.updateById(ghSupplier);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "供货商地址信息-通过id删除")
	@ApiOperation(value="供货商地址信息-通过id删除", notes="供货商地址信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ghSupplierService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "供货商地址信息-批量删除")
	@ApiOperation(value="供货商地址信息-批量删除", notes="供货商地址信息-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ghSupplierService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "供货商地址信息-通过id查询")
	@ApiOperation(value="供货商地址信息-通过id查询", notes="供货商地址信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		GhSupplier ghSupplier = ghSupplierService.getById(id);
		if(ghSupplier==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(ghSupplier);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param ghSupplier
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhSupplier ghSupplier) {
        return super.exportXls(request, ghSupplier, GhSupplier.class, "供货商地址信息");
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
        return super.importExcel(request, response, GhSupplier.class);
    }

}

