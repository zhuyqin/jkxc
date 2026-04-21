package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.PmsUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.model.SysRoleTreeModel;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.util.FindsRolesChildrenUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Autowired
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public Result importExcelCheckRoleCode(MultipartFile file, ImportParams params) throws Exception {
        List<Object> listSysRoles = ExcelImportUtil.importExcel(file.getInputStream(), SysRole.class, params);
        int totalCount = listSysRoles.size();
        List<String> errorStrs = new ArrayList<>();

        // 去除 listSysRoles 中重复的数据
        for (int i = 0; i < listSysRoles.size(); i++) {
            String roleCodeI =((SysRole)listSysRoles.get(i)).getRoleCode();
            for (int j = i + 1; j < listSysRoles.size(); j++) {
                String roleCodeJ =((SysRole)listSysRoles.get(j)).getRoleCode();
                // 发现重复数据
                if (roleCodeI.equals(roleCodeJ)) {
                    errorStrs.add("第 " + (j + 1) + " 行的 roleCode 值：" + roleCodeI + " 已存在，忽略导入");
                    listSysRoles.remove(j);
                    break;
                }
            }
        }
        // 去掉 sql 中的重复数据
        Integer errorLines=0;
        Integer successLines=0;
        List<String> list = ImportExcelUtil.importDateSave(listSysRoles, ISysRoleService.class, errorStrs, CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE);
         errorLines+=list.size();
         successLines+=(listSysRoles.size()-errorLines);
        return ImportExcelUtil.imporReturnRes(errorLines,successLines,list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(String roleid) {
        //1.删除角色和用户关系
        sysRoleMapper.deleteRoleUserRelation(roleid);
        //2.删除角色和权限关系
        sysRoleMapper.deleteRolePermissionRelation(roleid);
        //3.删除角色
        this.removeById(roleid);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchRole(String[] roleIds) {
        //1.删除角色和用户关系
        sysUserMapper.deleteBathRoleUserRelation(roleIds);
        //2.删除角色和权限关系
        sysUserMapper.deleteBathRolePermissionRelation(roleIds);
        //3.删除角色
        this.removeByIds(Arrays.asList(roleIds));
        return true;
    }

    @Override
    public List<SysRoleTreeModel> queryRoleTreeList(Integer roleType) {
        LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<>();
        query.eq(SysRole::getStatus, 1); // 只查询启用的角色
        if (roleType != null) {
            query.eq(SysRole::getRoleType, roleType);
        }
        query.orderByAsc(SysRole::getRoleOrder, SysRole::getCreateTime);
        List<SysRole> list = this.list(query);
        return FindsRolesChildrenUtil.wrapTreeDataToTreeList(list);
    }

    @Override
    public List<SysRoleTreeModel> queryRoleTreeListByPid(String parentId) {
        LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<>();
        if (oConvertUtils.isEmpty(parentId)) {
            query.isNull(SysRole::getParentId);
        } else {
            query.eq(SysRole::getParentId, parentId);
        }
        query.eq(SysRole::getStatus, 1); // 只查询启用的角色
        query.orderByAsc(SysRole::getRoleOrder, SysRole::getCreateTime);
        List<SysRole> list = this.list(query);
        List<SysRoleTreeModel> records = new ArrayList<>();
        for (SysRole role : list) {
            SysRoleTreeModel treeModel = new SysRoleTreeModel(role);
            // 检查是否有子节点
            LambdaQueryWrapper<SysRole> childQuery = new LambdaQueryWrapper<>();
            childQuery.eq(SysRole::getParentId, role.getId());
            childQuery.eq(SysRole::getStatus, 1);
            Integer count = this.count(childQuery);
            if (count > 0) {
                treeModel.setIsLeaf(false);
            } else {
                treeModel.setIsLeaf(true);
            }
            records.add(treeModel);
        }
        return records;
    }
}
