package org.jeecg.modules.system.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAuditProcess;
import org.jeecg.modules.system.entity.SysAuditStep;
import org.jeecg.modules.system.entity.SysAuditProcessBinding;
import org.jeecg.modules.system.entity.SysAuditStepForm;
import org.jeecg.modules.system.entity.SysAuditFormIndicator;
import org.jeecg.modules.system.service.ISysAuditProcessService;
import org.jeecg.modules.system.service.ISysAuditProcessBindingService;
import org.jeecg.modules.system.service.ISysAuditStepFormService;
import org.jeecg.modules.system.service.ISysAuditFormIndicatorService;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 审核流程表 前端控制器
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/sys/auditProcess")
@Slf4j
public class SysAuditProcessController {

    @Autowired
    private ISysAuditProcessService sysAuditProcessService;
    
    @Autowired
    private ISysAuditProcessBindingService sysAuditProcessBindingService;
    
    @Autowired
    private ISysAuditStepFormService sysAuditStepFormService;
    
    @Autowired
    private ISysAuditFormIndicatorService sysAuditFormIndicatorService;

    /**
     * 分页列表查询
     * @param process
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<SysAuditProcess>> queryPageList(SysAuditProcess process,
                                                          @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                          @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                          HttpServletRequest req) {
        Result<IPage<SysAuditProcess>> result = new Result<>();
        QueryWrapper<SysAuditProcess> queryWrapper = QueryGenerator.initQueryWrapper(process, req.getParameterMap());
        Page<SysAuditProcess> page = new Page<>(pageNo, pageSize);
        IPage<SysAuditProcess> pageList = sysAuditProcessService.page(page, queryWrapper);
        
        // 为每个流程加载步骤信息
        if (pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
            for (SysAuditProcess item : pageList.getRecords()) {
                SysAuditProcess processWithSteps = sysAuditProcessService.getProcessWithSteps(item.getId());
                if (processWithSteps != null && processWithSteps.getSteps() != null) {
                    item.setSteps(processWithSteps.getSteps());
                }
            }
        }
        
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 添加
     * @param jsonObject 包含process信息和stepFormConfigs
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            // 解析流程信息
            SysAuditProcess process = jsonObject.toJavaObject(SysAuditProcess.class);
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                process.setCreateBy(sysUser.getUsername());
            }
            process.setCreateTime(new Date());
            
            // 提取表单配置映射
            JSONObject stepFormConfigs = jsonObject.getJSONObject("stepFormConfigs");
            
            // 保存流程和步骤
            boolean success = sysAuditProcessService.saveMain(process, process.getSteps());
            if (success) {
                // 保存业务类型绑定
                if (process.getBindings() != null && !process.getBindings().isEmpty()) {
                    sysAuditProcessBindingService.saveBindings(process.getId(), process.getBindings());
                }
                
                // 保存步骤表单配置
                if (stepFormConfigs != null && !stepFormConfigs.isEmpty()) {
                    sysAuditProcessService.saveStepFormConfigs(process.getId(), stepFormConfigs);
                }
                
                result.success("添加成功！");
            } else {
                result.error500("添加失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 编辑
     * @param jsonObject 包含process信息和stepFormConfigs
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<?> edit(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            // 解析流程信息
            SysAuditProcess process = jsonObject.toJavaObject(SysAuditProcess.class);
            
            SysAuditProcess existingProcess = sysAuditProcessService.getById(process.getId());
            if (existingProcess == null) {
                result.error500("未找到对应实体");
                return result;
            }
            
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                process.setUpdateBy(sysUser.getUsername());
            }
            process.setUpdateTime(new Date());
            
            // 提取表单配置映射
            JSONObject stepFormConfigs = jsonObject.getJSONObject("stepFormConfigs");
            
            // 更新流程和步骤
            boolean success = sysAuditProcessService.updateMain(process, process.getSteps());
            if (success) {
                // 更新业务类型绑定
                if (process.getBindings() != null) {
                    sysAuditProcessBindingService.saveBindings(process.getId(), process.getBindings());
                }
                
                // 保存步骤表单配置
                if (stepFormConfigs != null && !stepFormConfigs.isEmpty()) {
                    sysAuditProcessService.saveStepFormConfigs(process.getId(), stepFormConfigs);
                }
                
                result.success("修改成功!");
            } else {
                result.error500("修改失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        Result<?> result = new Result<>();
        try {
            // 删除流程及其所有关联数据（绑定记录、步骤、表单配置等）
            boolean success = sysAuditProcessService.removeProcessWithRelations(id);
            if (success) {
                result.success("删除成功!");
            } else {
                result.error500("删除失败！未找到对应的流程");
            }
        } catch (Exception e) {
            log.error("删除流程失败，流程ID: " + id, e);
            result.error500("删除失败！" + e.getMessage());
        }
        return result;
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        Result<?> result = new Result<>();
        if (oConvertUtils.isEmpty(ids)) {
            result.error500("未选中数据！");
        } else {
            try {
                String[] idArray = ids.split(",");
                for (String id : idArray) {
                    // 删除流程及其所有关联数据（绑定记录、步骤、表单配置等）
                    sysAuditProcessService.removeProcessWithRelations(id);
                }
                result.success("删除成功!");
            } catch (Exception e) {
                log.error("批量删除流程失败", e);
                result.error500("删除失败！" + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 通过id查询（包含步骤）
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysAuditProcess> queryById(@RequestParam(name="id",required=true) String id) {
        Result<SysAuditProcess> result = new Result<>();
        try {
            SysAuditProcess process = sysAuditProcessService.getProcessWithSteps(id);
            if (process == null) {
                result.error500("未找到对应实体");
            } else {
                // 加载业务类型绑定
                List<SysAuditProcessBinding> bindings = sysAuditProcessBindingService.getBindingsByProcessId(id);
                process.setBindings(bindings);
                
                result.setResult(process);
                result.setSuccess(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败");
        }
        return result;
    }

    /**
     * 查询所有流程（不分页）
     * @return
     */
    @RequestMapping(value = "/queryAll", method = RequestMethod.GET)
    public Result<List<SysAuditProcess>> queryAll() {
        Result<List<SysAuditProcess>> result = new Result<>();
        try {
            List<SysAuditProcess> list = sysAuditProcessService.list();
            result.setResult(list);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败");
        }
        return result;
    }
    
    /**
     * 保存步骤表单配置
     * @param jsonObject 包含stepId、formConfig、indicators等
     * @return
     */
    @RequestMapping(value = "/saveStepForm", method = RequestMethod.POST)
    public Result<?> saveStepForm(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();
        try {
            String stepId = jsonObject.getString("stepId");
            if (oConvertUtils.isEmpty(stepId)) {
                result.error500("步骤ID不能为空");
                return result;
            }
            
            // 保存表单配置
            SysAuditStepForm form = new SysAuditStepForm();
            form.setFormName(jsonObject.getString("formName"));
            form.setFormConfig(jsonObject.getString("formConfig"));
            form.setRemarkRequired(jsonObject.getInteger("remarkRequired"));
            
            boolean success = sysAuditStepFormService.saveOrUpdateForm(stepId, form);
            if (success) {
                // 获取保存后的表单ID
                SysAuditStepForm savedForm = sysAuditStepFormService.getFormByStepId(stepId);
                
                // 保存指标配置
                if (jsonObject.containsKey("indicators")) {
                    List<SysAuditFormIndicator> indicators = JSONObject.parseArray(
                        jsonObject.getString("indicators"), SysAuditFormIndicator.class);
                    if (savedForm != null) {
                        sysAuditFormIndicatorService.saveIndicators(savedForm.getId(), indicators);
                    }
                }
                
                result.success("保存成功！");
            } else {
                result.error500("保存失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询步骤表单配置（包含指标）
     * @param stepId
     * @return
     */
    @RequestMapping(value = "/getStepForm", method = RequestMethod.GET)
    public Result<JSONObject> getStepForm(@RequestParam(name="stepId",required=true) String stepId) {
        Result<JSONObject> result = new Result<>();
        try {
            log.info("[getStepForm] 收到请求, stepId: {}", stepId);
            SysAuditStepForm form = sysAuditStepFormService.getFormByStepId(stepId);
            log.info("[getStepForm] 查询到的form: {}", form != null ? form.getId() : "null");
            JSONObject data = new JSONObject();
            
            if (form != null) {
                data.put("form", form);
                // 加载指标
                List<SysAuditFormIndicator> indicators = sysAuditFormIndicatorService.getIndicatorsByStepFormId(form.getId());
                log.info("[getStepForm] 查询到的indicators数量: {}", indicators != null ? indicators.size() : 0);
                log.info("[getStepForm] indicators详情: {}", indicators);
                data.put("indicators", indicators);
            } else {
                log.warn("[getStepForm] 未找到表单配置, stepId: {}", stepId);
                data.put("form", null);
                data.put("indicators", new java.util.ArrayList<>());
            }
            
            result.setResult(data);
            result.setSuccess(true);
            log.info("[getStepForm] 返回数据: {}", data.toJSONString());
        } catch (Exception e) {
            log.error("[getStepForm] 查询失败, stepId: " + stepId, e);
            result.error500("查询失败");
        }
        return result;
    }
    
    /**
     * 根据业务类型查询绑定的流程
     * @param businessTypeId
     * @param taskType
     * @return
     */
    @RequestMapping(value = "/getProcessByBusinessType", method = RequestMethod.GET)
    public Result<SysAuditProcessBinding> getProcessByBusinessType(
            @RequestParam(name="businessTypeId",required=true) String businessTypeId,
            @RequestParam(name="taskType",required=false) String taskType) {
        Result<SysAuditProcessBinding> result = new Result<>();
        try {
            SysAuditProcessBinding binding;
            if (oConvertUtils.isNotEmpty(taskType)) {
                binding = sysAuditProcessBindingService.getBindingByBusinessTypeAndTaskType(businessTypeId, taskType);
            } else {
                binding = sysAuditProcessBindingService.getBindingByBusinessTypeId(businessTypeId);
            }
            
            if (binding != null) {
                result.setResult(binding);
                result.setSuccess(true);
            } else {
                result.setResult(null);
                result.setSuccess(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("查询失败");
        }
        return result;
    }

}

