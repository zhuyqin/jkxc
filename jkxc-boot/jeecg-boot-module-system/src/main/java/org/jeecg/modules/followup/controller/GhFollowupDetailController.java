package org.jeecg.modules.followup.controller;

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
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.followup.entity.GhFollowupDetail;
import org.jeecg.modules.followup.service.IGhFollowupDetailService;
import org.jeecg.modules.opportunity.entity.GhOpportunity;
import org.jeecg.modules.opportunity.service.IGhOpportunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 跟进记录
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Api(tags="跟进记录")
@RestController
@RequestMapping("/followup/ghFollowupDetail")
@Slf4j
public class GhFollowupDetailController extends JeecgController<GhFollowupDetail, IGhFollowupDetailService> {
    
    @Autowired
    private IGhFollowupDetailService ghFollowupDetailService;
    
    @Autowired
    private IGhOpportunityService opportunityService;

    // 线索奖金发放已迁移到“确认有效”动作中，此处不再注入奖金配置服务
    
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    
    /**
     * 分页列表查询
     */
    @AutoLog(value = "跟进记录-分页列表查询")
    @ApiOperation(value="跟进记录-分页列表查询", notes="跟进记录-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhFollowupDetail ghFollowupDetail,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<GhFollowupDetail> queryWrapper = QueryGenerator.initQueryWrapper(ghFollowupDetail, req.getParameterMap());
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByDesc("create_date");
        Page<GhFollowupDetail> page = new Page<>(pageNo, pageSize);
        IPage<GhFollowupDetail> pageList = ghFollowupDetailService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
    
    /**
     * 添加
     */
    @AutoLog(value = "跟进记录-添加")
    @ApiOperation(value="跟进记录-添加", notes="跟进记录-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhFollowupDetail ghFollowupDetail, HttpServletRequest req) {
        String token = req.getHeader(CommonConstant.X_ACCESS_TOKEN);
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        
        // 防止重复提交：检查最近5秒内是否有相同内容的跟进记录
        if (StringUtils.isNotBlank(ghFollowupDetail.getOpportId()) && 
            StringUtils.isNotBlank(ghFollowupDetail.getFollowupContent())) {
            
            Date fiveSecondsAgo = new Date(System.currentTimeMillis() - 5000);
            QueryWrapper<GhFollowupDetail> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("opport_id", ghFollowupDetail.getOpportId());
            checkWrapper.eq("followup_content", ghFollowupDetail.getFollowupContent());
            checkWrapper.ge("create_date", fiveSecondsAgo);
            checkWrapper.eq("del_flag", 0);
            
            long count = ghFollowupDetailService.count(checkWrapper);
            if (count > 0) {
                log.warn("检测到重复提交的跟进记录，商机ID: {}", ghFollowupDetail.getOpportId());
                return Result.error("请勿重复提交！");
            }
        }
        
        ghFollowupDetail.setCreateDate(new Date());
        ghFollowupDetail.setDelFlag(0);
        if (sysUser != null) {
            ghFollowupDetail.setFollowupPerson(sysUser.getRealname());
            ghFollowupDetail.setCreateBy(sysUser.getUsername());
        }
        
        // 更新商机的修改时间
        if (StringUtils.isNotBlank(ghFollowupDetail.getOpportId())) {
            GhOpportunity opportunity = opportunityService.getById(ghFollowupDetail.getOpportId());
            if (opportunity != null) {
                opportunity.setModiTime(new Date());
                opportunityService.updateById(opportunity);
            }
        }
        
        ghFollowupDetailService.save(ghFollowupDetail);

        // 线索奖金发放调整为：需在“确认有效”后触发（避免仅跟进就发放）

        return Result.OK("添加成功！");
    }
    
    /**
     * 编辑
     */
    @AutoLog(value = "跟进记录-编辑")
    @ApiOperation(value="跟进记录-编辑", notes="跟进记录-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody GhFollowupDetail ghFollowupDetail, HttpServletRequest req) {
        String token = req.getHeader(CommonConstant.X_ACCESS_TOKEN);
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        if (sysUser != null) {
            ghFollowupDetail.setUpdateBy(sysUser.getUsername());
        }
        ghFollowupDetailService.updateById(ghFollowupDetail);
        return Result.OK("编辑成功!");
    }
    
    /**
     * 通过id删除
     */
    @AutoLog(value = "跟进记录-通过id删除")
    @ApiOperation(value="跟进记录-通过id删除", notes="跟进记录-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id", required=true) String id) {
        GhFollowupDetail detail = ghFollowupDetailService.getById(id);
        if (detail != null) {
            detail.setDelFlag(1);
            ghFollowupDetailService.updateById(detail);
        }
        return Result.OK("删除成功!");
    }
    
    /**
     * 批量删除
     */
    @AutoLog(value = "跟进记录-批量删除")
    @ApiOperation(value="跟进记录-批量删除", notes="跟进记录-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name="ids", required=true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        for (String id : idList) {
            GhFollowupDetail detail = ghFollowupDetailService.getById(id);
            if (detail != null) {
                detail.setDelFlag(1);
                ghFollowupDetailService.updateById(detail);
            }
        }
        return Result.OK("批量删除成功!");
    }
    
    /**
     * 通过id查询
     */
    @AutoLog(value = "跟进记录-通过id查询")
    @ApiOperation(value="跟进记录-通过id查询", notes="跟进记录-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id", required=true) String id) {
        GhFollowupDetail ghFollowupDetail = ghFollowupDetailService.getById(id);
        if (ghFollowupDetail == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(ghFollowupDetail);
    }

    /**
     * 导出excel
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhFollowupDetail ghFollowupDetail) {
        return super.exportXls(request, ghFollowupDetail, GhFollowupDetail.class, "跟进记录");
    }

    /**
     * 通过excel导入数据
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, GhFollowupDetail.class);
    }
}

