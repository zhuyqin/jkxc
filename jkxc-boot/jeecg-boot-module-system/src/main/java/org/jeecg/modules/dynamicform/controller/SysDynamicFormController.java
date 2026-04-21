package org.jeecg.modules.dynamicform.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.dynamicform.entity.SysDynamicForm;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormVersion;
import org.jeecg.modules.dynamicform.entity.SysDynamicFormBinding;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormService;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormVersionService;
import org.jeecg.modules.dynamicform.service.ISysDynamicFormBindingService;
import org.jeecg.modules.system.service.ISysCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

/**
 * @Description: 动态表单主表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Api(tags="动态表单管理")
@RestController
@RequestMapping("/dynamicform/form")
@Slf4j
public class SysDynamicFormController extends JeecgController<SysDynamicForm, ISysDynamicFormService> {
    
    @Autowired
    private ISysDynamicFormService sysDynamicFormService;
    
    @Autowired
    private ISysDynamicFormVersionService sysDynamicFormVersionService;
    
    @Autowired
    private ISysDynamicFormBindingService sysDynamicFormBindingService;
    
    @Autowired
    private ISysCategoryService sysCategoryService;
    
    /**
     * 分页列表查询
     */
    @AutoLog(value = "动态表单-分页列表查询")
    @ApiOperation(value="动态表单-分页列表查询", notes="动态表单-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysDynamicForm sysDynamicForm,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysDynamicForm> queryWrapper = QueryGenerator.initQueryWrapper(sysDynamicForm, req.getParameterMap());
        Page<SysDynamicForm> page = new Page<SysDynamicForm>(pageNo, pageSize);
        IPage<SysDynamicForm> pageList = sysDynamicFormService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
    
    /**
     * 添加
     */
    @AutoLog(value = "动态表单-添加")
    @ApiOperation(value="动态表单-添加", notes="动态表单-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysDynamicForm sysDynamicForm) {
        sysDynamicFormService.save(sysDynamicForm);
        return Result.OK("添加成功！");
    }
    
    /**
     * 编辑
     */
    @AutoLog(value = "动态表单-编辑")
    @ApiOperation(value="动态表单-编辑", notes="动态表单-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody SysDynamicForm sysDynamicForm) {
        sysDynamicFormService.updateById(sysDynamicForm);
        return Result.OK("编辑成功!");
    }
    
    /**
     * 通过id删除
     */
    @AutoLog(value = "动态表单-通过id删除")
    @ApiOperation(value="动态表单-通过id删除", notes="动态表单-通过id删除，同时删除所有绑定关系")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        try {
            // 先删除该表单的所有绑定关系
            sysDynamicFormBindingService.deleteBindingsByFormId(id);
            // 再删除表单
            sysDynamicFormService.removeById(id);
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error("删除表单失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除
     */
    @AutoLog(value = "动态表单-批量删除")
    @ApiOperation(value="动态表单-批量删除", notes="动态表单-批量删除，同时删除所有绑定关系")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        try {
            String[] idArray = ids.split(",");
            // 先删除所有表单的绑定关系
            for (String id : idArray) {
                if (oConvertUtils.isNotEmpty(id.trim())) {
                    sysDynamicFormBindingService.deleteBindingsByFormId(id.trim());
                }
            }
            // 再批量删除表单
            this.sysDynamicFormService.removeByIds(Arrays.asList(idArray));
            return Result.OK("批量删除成功!");
        } catch (Exception e) {
            log.error("批量删除表单失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 通过id查询
     */
    @AutoLog(value = "动态表单-通过id查询")
    @ApiOperation(value="动态表单-通过id查询", notes="动态表单-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        SysDynamicForm sysDynamicForm = sysDynamicFormService.getById(id);
        if(sysDynamicForm==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(sysDynamicForm);
    }
    
    /**
     * 创建新版本
     */
    @AutoLog(value = "动态表单-创建新版本")
    @ApiOperation(value="动态表单-创建新版本", notes="动态表单-创建新版本")
    @PostMapping(value = "/createVersion")
    public Result<?> createVersion(@RequestBody JSONObject jsonObject) {
        try {
            String formId = jsonObject.getString("formId");
            String formConfig = jsonObject.getString("formConfig");
            String versionDesc = jsonObject.getString("versionDesc");
            
            if (oConvertUtils.isEmpty(formId)) {
                return Result.error("表单ID不能为空");
            }
            if (oConvertUtils.isEmpty(formConfig)) {
                return Result.error("表单配置不能为空");
            }
            
            String versionId = sysDynamicFormService.createVersion(formId, formConfig, versionDesc);
            return Result.OK(versionId);
        } catch (Exception e) {
            log.error("创建版本失败", e);
            return Result.error("创建版本失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取表单的所有版本
     */
    @AutoLog(value = "动态表单-获取版本列表")
    @ApiOperation(value="动态表单-获取版本列表", notes="动态表单-获取版本列表")
    @GetMapping(value = "/getVersions")
    public Result<?> getVersions(@RequestParam(name="formId",required=true) String formId) {
        List<SysDynamicFormVersion> versions = sysDynamicFormVersionService.getVersionsByFormId(formId);
        return Result.OK(versions);
    }
    
    /**
     * 根据业务类型获取表单版本（新增订单时使用，只返回未删除的表单版本）
     */
    @AutoLog(value = "动态表单-根据业务类型获取表单版本")
    @ApiOperation(value="动态表单-根据业务类型获取表单版本", notes="动态表单-根据业务类型获取表单版本（新增订单时使用，返回该表单的最新版本isCurrent=1，只返回未删除的表单版本）")
    @GetMapping(value = "/getFormVersionByBusinessType")
    public Result<?> getFormVersionByBusinessType(@RequestParam(name="businessType",required=true) String businessType) {
        try {
            SysDynamicFormBinding binding = sysDynamicFormBindingService.getBindingByBusinessType(businessType);
            if (binding == null) {
                // 未绑定表单时返回空，不报错
                return Result.OK(null);
            }
            
            // 检查表单主表是否被删除（新增时，如果表单被删除，就不应该显示动态表单）
            SysDynamicForm form = sysDynamicFormService.getById(binding.getFormId());
            if (form == null) {
                // 表单已被删除，返回null（不报错，前端会处理）
                return Result.OK(null);
            }
            
            // 获取该表单的最新版本（isCurrent=1），而不是绑定表中保存的版本ID
            // 这样当创建新版本并设置为当前版本时，新增订单会自动使用最新版本
            SysDynamicFormVersion version = sysDynamicFormVersionService.getCurrentVersionByFormId(binding.getFormId());
            if (version == null) {
                // 如果没有当前版本，尝试使用绑定表中保存的版本ID（兼容旧数据）
                version = sysDynamicFormVersionService.getVersionById(binding.getVersionId());
                if (version == null) {
                    // 版本不存在，返回null（不报错，前端会处理）
                    return Result.OK(null);
                }
            }
            
            return Result.OK(version);
        } catch (Exception e) {
            log.error("根据业务类型获取表单版本失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 绑定业务类型到表单版本
     * 支持批量绑定，绑定前会先删除该表单的所有历史绑定
     */
    @AutoLog(value = "动态表单-绑定业务类型")
    @ApiOperation(value="动态表单-绑定业务类型", notes="动态表单-绑定业务类型，支持批量绑定，绑定前会先删除该表单的所有历史绑定")
    @PostMapping(value = "/bindBusinessType")
    public Result<?> bindBusinessType(@RequestBody JSONObject jsonObject) {
        try {
            String businessType = jsonObject.getString("businessType");
            String formId = jsonObject.getString("formId");
            String versionId = jsonObject.getString("versionId");
            Boolean deleteHistory = jsonObject.getBoolean("deleteHistory"); // 是否删除历史绑定，默认true
            Integer status = jsonObject.getInteger("status"); // 状态（1-启用，0-禁用），默认为1
            
            if (oConvertUtils.isEmpty(businessType)) {
                return Result.error("业务类型不能为空");
            }
            if (oConvertUtils.isEmpty(formId)) {
                return Result.error("表单ID不能为空");
            }
            if (oConvertUtils.isEmpty(versionId)) {
                return Result.error("版本ID不能为空");
            }
            
            // 如果deleteHistory为true（默认），先删除该表单的所有历史绑定
            if (deleteHistory == null || deleteHistory) {
                sysDynamicFormBindingService.deleteBindingsByFormId(formId);
                log.info("已删除表单 {} 的所有历史绑定", formId);
            }
            
            // 处理多个业务类型（逗号分隔）
            String[] businessTypeArray = businessType.split(",");
            for (String bt : businessTypeArray) {
                bt = bt.trim();
                if (oConvertUtils.isNotEmpty(bt)) {
                    // 先删除该业务类型的其他绑定（一个业务类型只能绑定一个表单）
                    sysDynamicFormBindingService.remove(new QueryWrapper<SysDynamicFormBinding>()
                        .eq("business_type", bt));
                    // 创建新绑定
                    sysDynamicFormBindingService.bindBusinessType(bt, formId, versionId, status != null ? status : 1);
                }
            }
            
            return Result.OK("绑定成功");
        } catch (Exception e) {
            log.error("绑定业务类型失败", e);
            return Result.error("绑定业务类型失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据版本ID查询版本信息
     */
    @AutoLog(value = "动态表单-根据版本ID查询")
    @ApiOperation(value="动态表单-根据版本ID查询", notes="动态表单-根据版本ID查询")
    @GetMapping(value = "/queryVersionById")
    public Result<?> queryVersionById(@RequestParam(name="id",required=true) String id) {
        try {
            SysDynamicFormVersion version = sysDynamicFormVersionService.getVersionById(id);
            if (version == null) {
                return Result.error("版本不存在");
            }
            return Result.OK(version);
        } catch (Exception e) {
            log.error("查询版本失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询表单的所有业务类型绑定
     */
    @AutoLog(value = "动态表单-查询绑定列表")
    @ApiOperation(value="动态表单-查询绑定列表", notes="动态表单-查询绑定列表")
    @GetMapping(value = "/getBindingList")
    public Result<?> getBindingList(@RequestParam(name="formId",required=true) String formId) {
        try {
            List<SysDynamicFormBinding> bindings = sysDynamicFormBindingService.list(
                new QueryWrapper<SysDynamicFormBinding>()
                    .eq("form_id", formId)
                    .orderByDesc("create_time")
            );
            
            // 查询业务类型名称并填充到结果中
            if (bindings != null && bindings.size() > 0) {
                for (SysDynamicFormBinding binding : bindings) {
                    if (oConvertUtils.isNotEmpty(binding.getBusinessType())) {
                        try {
                            // 查询业务类型名称
                            List<String> categoryNames = sysCategoryService.loadDictItem(binding.getBusinessType(), false);
                            if (categoryNames != null && categoryNames.size() > 0) {
                                // 将业务类型名称拼接（多个业务类型用逗号分隔）
                                binding.setBusinessTypeName(String.join(", ", categoryNames));
                            } else {
                                binding.setBusinessTypeName(binding.getBusinessType());
                            }
                        } catch (Exception e) {
                            log.warn("查询业务类型名称失败: " + binding.getBusinessType(), e);
                            binding.setBusinessTypeName(binding.getBusinessType());
                        }
                    }
                }
            }
            
            return Result.OK(bindings);
        } catch (Exception e) {
            log.error("查询绑定列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除业务类型绑定
     */
    @AutoLog(value = "动态表单-删除绑定")
    @ApiOperation(value="动态表单-删除绑定", notes="动态表单-删除绑定")
    @DeleteMapping(value = "/deleteBinding")
    public Result<?> deleteBinding(@RequestParam(name="id",required=true) String id) {
        try {
            boolean success = sysDynamicFormBindingService.removeById(id);
            if (success) {
                return Result.OK("删除成功");
            } else {
                return Result.error("删除失败，绑定不存在");
            }
        } catch (Exception e) {
            log.error("删除绑定失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 复制表单
     */
    @AutoLog(value = "动态表单-复制表单")
    @ApiOperation(value="动态表单-复制表单", notes="动态表单-复制表单，版本号从1开始")
    @PostMapping(value = "/copyForm")
    public Result<?> copyForm(@RequestParam(name="formId",required=true) String formId,
                             @RequestParam(name="newFormName",required=true) String newFormName,
                             @RequestParam(name="newFormCode",required=true) String newFormCode) {
        try {
            String newFormId = sysDynamicFormService.copyForm(formId, newFormName, newFormCode);
            return Result.OK(newFormId, "复制成功！");
        } catch (Exception e) {
            log.error("复制表单失败", e);
            return Result.error("复制表单失败：" + e.getMessage());
        }
    }
}

