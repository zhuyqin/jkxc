package org.jeecg.modules.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.mapper.SysUserDepartMapper;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <P>
 * 用户部门表实现类
 * <p/>
 * @Author ZhiLin
 *@since 2019-02-22
 */
@Service
public class SysUserDepartServiceImpl extends ServiceImpl<SysUserDepartMapper, SysUserDepart> implements ISysUserDepartService {
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	

	/**
	 * 根据用户id查询部门信息
	 */
	@Override
	public List<DepartIdModel> queryDepartIdsOfUser(String userId) {
		LambdaQueryWrapper<SysUserDepart> queryUDep = new LambdaQueryWrapper<SysUserDepart>();
		LambdaQueryWrapper<SysDepart> queryDep = new LambdaQueryWrapper<SysDepart>();
		try {
			queryUDep.eq(SysUserDepart::getUserId, userId);
			List<String> depIdList = new ArrayList<>();
			List<DepartIdModel> depIdModelList = new ArrayList<>();
			List<SysUserDepart> userDepList = this.list(queryUDep);
			if(userDepList != null && userDepList.size() > 0) {
			for(SysUserDepart userDepart : userDepList) {
					depIdList.add(userDepart.getDepId());
				}
			queryDep.in(SysDepart::getId, depIdList);
			List<SysDepart> depList = sysDepartService.list(queryDep);
			if(depList != null || depList.size() > 0) {
				for(SysDepart depart : depList) {
					depIdModelList.add(new DepartIdModel().convertByUserDepart(depart));
				}
			}
			return depIdModelList;
			}
		}catch(Exception e) {
			e.fillInStackTrace();
		}
		return null;
		
		
	}


	/**
	 * 根据部门id查询用户信息
	 */
	@Override
	public List<SysUser> queryUserByDepId(String depId) {
		LambdaQueryWrapper<SysUserDepart> queryUDep = new LambdaQueryWrapper<SysUserDepart>();
		queryUDep.eq(SysUserDepart::getDepId, depId);
		List<String> userIdList = new ArrayList<>();
		List<SysUserDepart> uDepList = this.list(queryUDep);
		if(uDepList != null && uDepList.size() > 0) {
			for(SysUserDepart uDep : uDepList) {
				userIdList.add(uDep.getUserId());
			}
			List<SysUser> userList = (List<SysUser>) sysUserService.listByIds(userIdList);
			//update-begin-author:taoyan date:201905047 for:接口调用查询返回结果不能返回密码相关信息
			for (SysUser sysUser : userList) {
				sysUser.setSalt("");
				sysUser.setPassword("");
			}
			//update-end-author:taoyan date:201905047 for:接口调用查询返回结果不能返回密码相关信息
			return userList;
		}
		return new ArrayList<SysUser>();
	}

	/**
	 * 根据部门code，查询当前部门和下级部门的 用户信息
	 */
	@Override
	public List<SysUser> queryUserByDepCode(String depCode,String realname) {
		//update-begin-author:taoyan date:20210422 for: 根据部门选择用户接口代码优化
		if(oConvertUtils.isNotEmpty(realname)){
			realname = realname.trim();
		}
		List<SysUser> userList = this.baseMapper.queryDepartUserList(depCode, realname);
		Map<String, SysUser> map = new HashMap<String, SysUser>();
		for (SysUser sysUser : userList) {
			// 返回的用户数据去掉密码信息
			sysUser.setSalt("");
			sysUser.setPassword("");
			map.put(sysUser.getId(), sysUser);
		}
		return new ArrayList<SysUser>(map.values());
		//update-end-author:taoyan date:20210422 for: 根据部门选择用户接口代码优化

	}

	@Override
	public IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, String roleId, int pageSize, int pageNo) {
		IPage<SysUser> pageList = null;
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		
		// 如果指定了roleId且没有选择部门，直接根据角色查询用户（更高效）
		if(oConvertUtils.isNotEmpty(roleId) && oConvertUtils.isEmpty(departId)){
			// 直接根据角色ID查询用户
			pageList = sysUserService.getUserByRoleId(page, roleId, username);
		} else if(oConvertUtils.isEmpty(departId)){
			// 部门ID不存在 直接查询用户表即可
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
			query.eq(SysUser::getDelFlag, 0); // 只查询未删除的用户
			if(oConvertUtils.isNotEmpty(username)){
				query.like(SysUser::getUsername, username);
			}
			pageList = sysUserService.page(page, query);
		}else{
			// 有部门ID 需要走自定义sql
			SysDepart sysDepart = sysDepartService.getById(departId);
			pageList = this.baseMapper.queryDepartUserPageList(page, sysDepart.getOrgCode(), username, realname);
		}
		List<SysUser> userList = pageList.getRecords();
		
		// 如果指定了roleId且有部门ID，需要过滤出属于该角色的用户
		if(oConvertUtils.isNotEmpty(roleId) && oConvertUtils.isNotEmpty(departId) && userList != null && userList.size() > 0){
			// 查询该角色的所有用户ID（只查询未删除且状态正常的用户）
			LambdaQueryWrapper<SysUserRole> roleQuery = new LambdaQueryWrapper<>();
			roleQuery.eq(SysUserRole::getRoleId, roleId);
			List<SysUserRole> userRoleList = sysUserRoleService.list(roleQuery);
			if(userRoleList != null && userRoleList.size() > 0){
				List<String> roleUserIds = userRoleList.stream()
					.map(SysUserRole::getUserId)
					.collect(Collectors.toList());
				// 查询这些用户ID对应的用户，确保用户状态正常
				if(roleUserIds != null && roleUserIds.size() > 0){
					LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
					userQuery.in(SysUser::getId, roleUserIds);
					userQuery.eq(SysUser::getDelFlag, 0);
					userQuery.and(wrapper -> wrapper.eq(SysUser::getStatus, 1).or().isNull(SysUser::getStatus));
					List<SysUser> validUsers = sysUserService.list(userQuery);
					List<String> validUserIds = validUsers.stream()
						.map(SysUser::getId)
						.collect(Collectors.toList());
					// 过滤用户列表，只保留属于该角色且状态正常的用户
					userList = userList.stream()
						.filter(user -> validUserIds.contains(user.getId()))
						.collect(Collectors.toList());
				} else {
					userList = new ArrayList<>();
				}
				// 更新分页信息
				pageList.setRecords(userList);
				pageList.setTotal(userList.size());
			} else {
				// 如果该角色下没有用户，返回空列表
				pageList.setRecords(new ArrayList<>());
				pageList.setTotal(0);
				userList = new ArrayList<>();
			}
		}
		
		if(userList!=null && userList.size()>0){
			List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, SysUser> map = new HashMap<String, SysUser>();
			if(userIds!=null && userIds.size()>0){
				// 查部门名称
				Map<String,String>  useDepNames = sysUserService.getDepNamesByUserIds(userIds);
				userList.forEach(item->{
					//TODO 临时借用这个字段用于页面展示
					item.setOrgCodeTxt(useDepNames.get(item.getId()));
					item.setSalt("");
					item.setPassword("");
					// 去重
					map.put(item.getId(), item);
				});
			}
			pageList.setRecords(new ArrayList<SysUser>(map.values()));
		}
		return pageList;
	}

}
