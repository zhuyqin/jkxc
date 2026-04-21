package org.jeecg.modules.opportunity.controller;

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
import org.jeecg.modules.opportunity.entity.GhOpportunityBonusConfig;
import org.jeecg.modules.opportunity.service.IGhOpportunityBonusConfigService;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * @Description: 线索奖金配置（商机名称ID -> 奖金金额）
 */
@Api(tags = "线索奖金配置")
@RestController
@RequestMapping("/opportunity/ghOpportunityBonusConfig")
@Slf4j
public class GhOpportunityBonusConfigController extends JeecgController<GhOpportunityBonusConfig, IGhOpportunityBonusConfigService> {

    @Autowired
    private IGhOpportunityBonusConfigService bonusConfigService;

    @AutoLog(value = "线索奖金配置-分页列表查询")
    @ApiOperation(value = "线索奖金配置-分页列表查询", notes = "线索奖金配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhOpportunityBonusConfig bonusConfig,
                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                     HttpServletRequest req) {
        QueryWrapper<GhOpportunityBonusConfig> queryWrapper = QueryGenerator.initQueryWrapper(bonusConfig, req.getParameterMap());
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByDesc("create_time");

        Page<GhOpportunityBonusConfig> page = new Page<>(pageNo, pageSize);
        IPage<GhOpportunityBonusConfig> pageList = bonusConfigService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "线索奖金配置-添加")
    @ApiOperation(value = "线索奖金配置-添加", notes = "线索奖金配置-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhOpportunityBonusConfig entity) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user != null) {
            entity.setCreateBy(user.getUsername());
        }
        entity.setCreateTime(new Date());
        entity.setDelFlag(0);
        bonusConfigService.save(entity);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "线索奖金配置-编辑")
    @ApiOperation(value = "线索奖金配置-编辑", notes = "线索奖金配置-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody GhOpportunityBonusConfig entity) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user != null) {
            entity.setUpdateBy(user.getUsername());
        }
        entity.setUpdateTime(new Date());
        bonusConfigService.updateById(entity);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "线索奖金配置-删除")
    @ApiOperation(value = "线索奖金配置-删除", notes = "线索奖金配置-逻辑删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        GhOpportunityBonusConfig row = bonusConfigService.getById(id);
        if (row == null) {
            return Result.error("未找到对应数据");
        }
        row.setDelFlag(1);
        row.setUpdateTime(new Date());
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user != null) {
            row.setUpdateBy(user.getUsername());
        }
        bonusConfigService.updateById(row);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "线索奖金配置-查询详情")
    @ApiOperation(value = "线索奖金配置-查询详情", notes = "通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        GhOpportunityBonusConfig row = bonusConfigService.getById(id);
        if (row == null || (row.getDelFlag() != null && row.getDelFlag() != 0)) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(row);
    }
}

