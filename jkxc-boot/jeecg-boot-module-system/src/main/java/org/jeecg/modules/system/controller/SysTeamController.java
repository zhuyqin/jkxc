package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysTeam;
import org.jeecg.modules.system.service.ISysTeamService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * <p>
 * 团队管理表 前端控制器
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-03
 */
@Slf4j
@Api(tags = "团队管理")
@RestController
@RequestMapping("/sys/team")
public class SysTeamController {

    @Autowired
    private ISysTeamService sysTeamService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 分页列表查询
     *
     * @param sysTeam
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "团队管理-分页列表查询", notes = "团队管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysTeam sysTeam,
                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                    HttpServletRequest req) {
        QueryWrapper<SysTeam> queryWrapper = QueryGenerator.initQueryWrapper(sysTeam, req.getParameterMap());
        Page<SysTeam> page = new Page<SysTeam>(pageNo, pageSize);
        IPage<SysTeam> pageList = sysTeamService.page(page, queryWrapper);
        
        // 填充团队负责人姓名（如果为空）
        if (pageList != null && pageList.getRecords() != null) {
            for (SysTeam team : pageList.getRecords()) {
                if (oConvertUtils.isNotEmpty(team.getTeamLeader()) && oConvertUtils.isEmpty(team.getTeamLeaderName())) {
                    org.jeecg.modules.system.entity.SysUser leader = sysUserService.getById(team.getTeamLeader());
                    if (leader != null) {
                        team.setTeamLeaderName(leader.getRealname());
                    }
                }
            }
        }
        
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param sysTeam
     * @return
     */
    @AutoLog(value = "团队管理-添加")
    @ApiOperation(value = "团队管理-添加", notes = "团队管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysTeam sysTeam) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            return Result.error("请先登录");
        }

        // 如果指定了团队负责人，获取负责人姓名
        if (oConvertUtils.isNotEmpty(sysTeam.getTeamLeader())) {
            org.jeecg.modules.system.entity.SysUser leader = null;
            // 先尝试作为ID查询
            leader = sysUserService.getById(sysTeam.getTeamLeader());
            // 如果按ID查询不到，可能是用户名，尝试按用户名查询
            if (leader == null) {
                leader = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser>()
                    .eq(org.jeecg.modules.system.entity.SysUser::getUsername, sysTeam.getTeamLeader())
                    .eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0));
                // 如果找到用户，更新teamLeader为正确的ID
                if (leader != null) {
                    sysTeam.setTeamLeader(leader.getId());
                }
            }
            if (leader != null) {
                sysTeam.setTeamLeaderName(leader.getRealname());
            }
        }

        sysTeam.setCreateBy(sysUser.getUsername());
        sysTeam.setCreateTime(new Date());
        sysTeamService.save(sysTeam);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param sysTeam
     * @return
     */
    @AutoLog(value = "团队管理-编辑")
    @ApiOperation(value = "团队管理-编辑", notes = "团队管理-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysTeam sysTeam) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            return Result.error("请先登录");
        }

        // 如果指定了团队负责人，获取负责人姓名
        if (oConvertUtils.isNotEmpty(sysTeam.getTeamLeader())) {
            org.jeecg.modules.system.entity.SysUser leader = null;
            // 先尝试作为ID查询
            leader = sysUserService.getById(sysTeam.getTeamLeader());
            // 如果按ID查询不到，可能是用户名，尝试按用户名查询
            if (leader == null) {
                leader = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.jeecg.modules.system.entity.SysUser>()
                    .eq(org.jeecg.modules.system.entity.SysUser::getUsername, sysTeam.getTeamLeader())
                    .eq(org.jeecg.modules.system.entity.SysUser::getDelFlag, 0));
                // 如果找到用户，更新teamLeader为正确的ID
                if (leader != null) {
                    sysTeam.setTeamLeader(leader.getId());
                }
            }
            if (leader != null) {
                sysTeam.setTeamLeaderName(leader.getRealname());
            }
        }

        sysTeam.setUpdateBy(sysUser.getUsername());
        sysTeam.setUpdateTime(new Date());
        sysTeamService.updateById(sysTeam);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "团队管理-通过id删除")
    @ApiOperation(value = "团队管理-通过id删除", notes = "团队管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysTeamService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "团队管理-批量删除")
    @ApiOperation(value = "团队管理-批量删除", notes = "团队管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysTeamService.removeByIds(java.util.Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "团队管理-通过id查询", notes = "团队管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysTeam sysTeam = sysTeamService.getById(id);
        if (sysTeam == null) {
            return Result.error("未找到对应数据");
        }
        return Result.ok(sysTeam);
    }

    /**
     * 获取所有启用的团队列表（用于下拉选择）
     *
     * @return
     */
    @ApiOperation(value = "获取所有启用的团队列表", notes = "获取所有启用的团队列表")
    @GetMapping(value = "/listAll")
    public Result<?> listAll() {
        QueryWrapper<SysTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByAsc("sort_order", "create_time");
        return Result.ok(sysTeamService.list(queryWrapper));
    }
}

