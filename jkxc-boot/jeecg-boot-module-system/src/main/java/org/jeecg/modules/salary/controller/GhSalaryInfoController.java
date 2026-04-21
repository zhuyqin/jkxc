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
import org.jeecg.modules.salary.service.IGhSalaryInfoService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 工资信息
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Api(tags="工资信息")
@RestController
@RequestMapping("/salary/ghSalaryInfo")
@Slf4j
public class GhSalaryInfoController extends JeecgController<GhSalaryInfo, IGhSalaryInfoService> {
    
    @Autowired
    private IGhSalaryInfoService ghSalaryInfoService;
    
    @Autowired(required = false)
    private ISysUserService sysUserService;

    /**
     * 分页列表查询（只显示在职员工，自动同步在职用户）
     *
     * @param ghSalaryInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "工资信息-分页列表查询")
    @ApiOperation(value="工资信息-分页列表查询", notes="工资信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(GhSalaryInfo ghSalaryInfo,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        try {
            // 自动同步在职用户到工资信息表
            syncActiveUsersToSalaryInfo();
            
            QueryWrapper<GhSalaryInfo> queryWrapper = QueryGenerator.initQueryWrapper(ghSalaryInfo, req.getParameterMap());
            queryWrapper.eq("del_flag", 0);
            
            // 只查询在职员工（关联sys_user表，del_flag=0 且 status=1）
            if (sysUserService != null) {
                QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
                userWrapper.eq("del_flag", 0);
                userWrapper.eq("status", 1);
                List<SysUser> activeUsers = sysUserService.list(userWrapper);
                if (activeUsers != null && !activeUsers.isEmpty()) {
                    List<String> userIds = new java.util.ArrayList<>();
                    for (SysUser user : activeUsers) {
                        userIds.add(user.getId());
                    }
                    if (!userIds.isEmpty()) {
                        queryWrapper.in("user_id", userIds);
                    } else {
                        // 如果没有在职员工，返回空列表
                        Page<GhSalaryInfo> page = new Page<>(pageNo, pageSize);
                        return Result.OK(page);
                    }
                } else {
                    // 如果没有在职员工，返回空列表
                    Page<GhSalaryInfo> page = new Page<>(pageNo, pageSize);
                    return Result.OK(page);
                }
            }
            
            queryWrapper.orderByDesc("create_time");
            
            Page<GhSalaryInfo> page = new Page<>(pageNo, pageSize);
            IPage<GhSalaryInfo> pageList = ghSalaryInfoService.page(page, queryWrapper);
            
            // 填充员工姓名和职位信息
            if (pageList.getRecords() != null && !pageList.getRecords().isEmpty() && sysUserService != null) {
                for (GhSalaryInfo salaryInfo : pageList.getRecords()) {
                    if (StringUtils.isNotBlank(salaryInfo.getUserId())) {
                        try {
                            SysUser user = sysUserService.getById(salaryInfo.getUserId());
                            if (user != null) {
                                salaryInfo.setUserName(user.getRealname());
                                // 如果工资信息中没有职位，从用户表获取并更新
                                if (StringUtils.isBlank(salaryInfo.getCurrentPosition()) && StringUtils.isNotBlank(user.getPost())) {
                                    salaryInfo.setCurrentPosition(user.getPost());
                                    // 更新到数据库
                                    QueryWrapper<GhSalaryInfo> updateWrapper = new QueryWrapper<>();
                                    updateWrapper.eq("id", salaryInfo.getId());
                                    GhSalaryInfo updateInfo = new GhSalaryInfo();
                                    updateInfo.setCurrentPosition(user.getPost());
                                    ghSalaryInfoService.update(updateInfo, updateWrapper);
                                }
                            }
                        } catch (Exception e) {
                            log.debug("查询员工信息失败，userId: {}", salaryInfo.getUserId(), e);
                        }
                    }
                }
            }
            
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("查询工资信息列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 同步在职用户到工资信息表（自动为没有工资信息的在职用户创建默认记录）
     */
    private void syncActiveUsersToSalaryInfo() {
        if (sysUserService == null) {
            return;
        }
        
        try {
            // 查询所有在职用户
            QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
            userWrapper.eq("del_flag", 0);
            userWrapper.eq("status", 1);
            List<SysUser> activeUsers = sysUserService.list(userWrapper);
            
            if (activeUsers == null || activeUsers.isEmpty()) {
                return;
            }
            
            // 查询已有工资信息的用户ID
            QueryWrapper<GhSalaryInfo> salaryWrapper = new QueryWrapper<>();
            salaryWrapper.eq("del_flag", 0);
            List<GhSalaryInfo> existingSalaryInfos = ghSalaryInfoService.list(salaryWrapper);
            List<String> existingUserIds = new java.util.ArrayList<>();
            if (existingSalaryInfos != null) {
                for (GhSalaryInfo salaryInfo : existingSalaryInfos) {
                    if (StringUtils.isNotBlank(salaryInfo.getUserId())) {
                        existingUserIds.add(salaryInfo.getUserId());
                    }
                }
            }
            
            // 为没有工资信息的在职用户创建默认记录
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String createBy = sysUser != null ? sysUser.getUsername() : "system";
            Date now = new Date();
            
            int createCount = 0;
            for (SysUser user : activeUsers) {
                if (!existingUserIds.contains(user.getId())) {
                    try {
                        GhSalaryInfo newSalaryInfo = new GhSalaryInfo();
                        newSalaryInfo.setUserId(user.getId());
                        newSalaryInfo.setUserName(user.getRealname());
                        // 从用户表获取职位
                        if (StringUtils.isNotBlank(user.getPost())) {
                            newSalaryInfo.setCurrentPosition(user.getPost());
                        }
                        newSalaryInfo.setCreateBy(createBy);
                        newSalaryInfo.setCreateTime(now);
                        newSalaryInfo.setDelFlag(0);
                        ghSalaryInfoService.save(newSalaryInfo);
                        createCount++;
                    } catch (Exception e) {
                        log.warn("为用户创建工资信息失败，userId: {}, userName: {}", user.getId(), user.getRealname(), e);
                    }
                }
            }
            
            if (createCount > 0) {
                log.info("自动同步在职用户到工资信息表，共创建 {} 条记录", createCount);
            }
        } catch (Exception e) {
            log.error("同步在职用户到工资信息表失败", e);
        }
    }

    /**
     * 添加
     *
     * @param ghSalaryInfo
     * @return
     */
    @AutoLog(value = "工资信息-添加")
    @ApiOperation(value="工资信息-添加", notes="工资信息-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody GhSalaryInfo ghSalaryInfo) {
        try {
            // 参数校验
            if (StringUtils.isBlank(ghSalaryInfo.getUserId())) {
                return Result.error("请选择员工！");
            }
            
            // 检查该员工是否已有工资信息（使用同步块防止并发）
            synchronized (("salary_info_" + ghSalaryInfo.getUserId()).intern()) {
                QueryWrapper<GhSalaryInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", ghSalaryInfo.getUserId());
                queryWrapper.eq("del_flag", 0);
                GhSalaryInfo existing = ghSalaryInfoService.getOne(queryWrapper);
                if (existing != null) {
                    return Result.error("该员工已存在工资信息，请使用编辑功能！");
                }
                
                // 填充员工姓名
                if (StringUtils.isNotBlank(ghSalaryInfo.getUserId()) && sysUserService != null) {
                    try {
                        SysUser user = sysUserService.getById(ghSalaryInfo.getUserId());
                        if (user != null) {
                            ghSalaryInfo.setUserName(user.getRealname());
                            // 如果职位为空，从用户表获取
                            if (StringUtils.isBlank(ghSalaryInfo.getCurrentPosition()) && StringUtils.isNotBlank(user.getPost())) {
                                ghSalaryInfo.setCurrentPosition(user.getPost());
                            }
                        }
                    } catch (Exception e) {
                        log.debug("查询员工信息失败", e);
                    }
                }
                
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    ghSalaryInfo.setCreateBy(sysUser.getUsername());
                }
                ghSalaryInfo.setCreateTime(new Date());
                ghSalaryInfo.setDelFlag(0);
                
                // 保存，如果数据库唯一约束冲突，捕获异常
                try {
                    ghSalaryInfoService.save(ghSalaryInfo);
                    return Result.OK("添加成功！");
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    log.warn("员工工资信息重复创建，员工ID: {}", ghSalaryInfo.getUserId(), e);
                    return Result.error("该员工已存在工资信息，请使用编辑功能！");
                } catch (Exception e) {
                    // 检查是否是数据库唯一约束异常
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && (errorMsg.contains("Duplicate entry") || 
                                             errorMsg.contains("uk_user_id") ||
                                             errorMsg.contains("UNIQUE constraint") ||
                                             errorMsg.contains("duplicate key"))) {
                        log.warn("员工工资信息重复创建（数据库唯一约束），员工ID: {}", ghSalaryInfo.getUserId(), e);
                        return Result.error("该员工已存在工资信息，请使用编辑功能！");
                    }
                    // 其他异常继续抛出
                    throw e;
                }
            }
        } catch (Exception e) {
            log.error("添加工资信息失败", e);
            // 检查是否是唯一约束异常
            if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("uk_user_id"))) {
                return Result.error("该员工已存在工资信息，请使用编辑功能！");
            }
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 根据员工ID查询工资信息
     *
     * @param userId 员工ID
     * @return
     */
    @AutoLog(value = "工资信息-根据员工ID查询")
    @ApiOperation(value="工资信息-根据员工ID查询", notes="工资信息-根据员工ID查询")
    @GetMapping(value = "/queryByUserId")
    public Result<?> queryByUserId(@RequestParam(name="userId", required=true) String userId) {
        try {
            QueryWrapper<GhSalaryInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("del_flag", 0);
            GhSalaryInfo salaryInfo = ghSalaryInfoService.getOne(queryWrapper);
            if (salaryInfo == null) {
                return Result.error("该员工没有工资信息！");
            }
            return Result.OK(salaryInfo);
        } catch (Exception e) {
            log.error("查询工资信息失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 编辑
     *
     * @param ghSalaryInfo
     * @return
     */
    @AutoLog(value = "工资信息-编辑")
    @ApiOperation(value="工资信息-编辑", notes="工资信息-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody GhSalaryInfo ghSalaryInfo) {
        try {
            // 填充员工姓名
            if (StringUtils.isNotBlank(ghSalaryInfo.getUserId()) && sysUserService != null) {
                try {
                    SysUser user = sysUserService.getById(ghSalaryInfo.getUserId());
                    if (user != null) {
                        ghSalaryInfo.setUserName(user.getRealname());
                        // 如果职位为空，从用户表获取
                        if (StringUtils.isBlank(ghSalaryInfo.getCurrentPosition()) && StringUtils.isNotBlank(user.getPost())) {
                            ghSalaryInfo.setCurrentPosition(user.getPost());
                        }
                    }
                } catch (Exception e) {
                    log.debug("查询员工信息失败", e);
                }
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                ghSalaryInfo.setUpdateBy(sysUser.getUsername());
            }
            ghSalaryInfo.setUpdateTime(new Date());
            ghSalaryInfoService.updateById(ghSalaryInfo);
            return Result.OK("编辑成功!");
        } catch (Exception e) {
            log.error("编辑工资信息失败", e);
            return Result.error("编辑失败!" + e.getMessage());
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "工资信息-通过id删除")
    @ApiOperation(value="工资信息-通过id删除", notes="工资信息-通过id删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        try {
            GhSalaryInfo ghSalaryInfo = ghSalaryInfoService.getById(id);
            if (ghSalaryInfo != null) {
                ghSalaryInfo.setDelFlag(1);
                LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if (sysUser != null) {
                    ghSalaryInfo.setUpdateBy(sysUser.getUsername());
                }
                ghSalaryInfo.setUpdateTime(new Date());
                ghSalaryInfoService.updateById(ghSalaryInfo);
            }
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error("删除工资信息失败", e);
            return Result.error("删除失败!");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "工资信息-批量删除")
    @ApiOperation(value="工资信息-批量删除", notes="工资信息-批量删除")
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        try {
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                GhSalaryInfo ghSalaryInfo = ghSalaryInfoService.getById(id);
                if (ghSalaryInfo != null) {
                    ghSalaryInfo.setDelFlag(1);
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    if (sysUser != null) {
                        ghSalaryInfo.setUpdateBy(sysUser.getUsername());
                    }
                    ghSalaryInfo.setUpdateTime(new Date());
                    ghSalaryInfoService.updateById(ghSalaryInfo);
                }
            }
            return Result.OK("批量删除成功!");
        } catch (Exception e) {
            log.error("批量删除工资信息失败", e);
            return Result.error("批量删除失败!");
        }
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "工资信息-通过id查询")
    @ApiOperation(value="工资信息-通过id查询", notes="工资信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        try {
            GhSalaryInfo ghSalaryInfo = ghSalaryInfoService.getById(id);
            if (ghSalaryInfo == null) {
                return Result.error("未找到对应数据");
            }
            
            // 填充员工姓名和职位
            if (StringUtils.isNotBlank(ghSalaryInfo.getUserId()) && sysUserService != null) {
                try {
                    SysUser user = sysUserService.getById(ghSalaryInfo.getUserId());
                    if (user != null) {
                        ghSalaryInfo.setUserName(user.getRealname());
                        // 如果职位为空，从用户表获取
                        if (StringUtils.isBlank(ghSalaryInfo.getCurrentPosition()) && StringUtils.isNotBlank(user.getPost())) {
                            ghSalaryInfo.setCurrentPosition(user.getPost());
                        }
                    }
                } catch (Exception e) {
                    log.debug("查询员工信息失败", e);
                }
            }
            
            return Result.OK(ghSalaryInfo);
        } catch (Exception e) {
            log.error("查询工资信息详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ghSalaryInfo
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, GhSalaryInfo ghSalaryInfo) {
        return super.exportXls(request, ghSalaryInfo, GhSalaryInfo.class, "工资信息");
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
        return super.importExcel(request, response, GhSalaryInfo.class);
    }
}

