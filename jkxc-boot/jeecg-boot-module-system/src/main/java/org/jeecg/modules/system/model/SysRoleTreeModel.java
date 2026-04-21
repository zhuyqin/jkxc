package org.jeecg.modules.system.model;

import org.jeecg.modules.system.entity.SysRole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 角色表 存储树结构数据的实体类
 * <p>
 * 
 * @Author Auto
 * @Since 2025-12-23 
 */
public class SysRoleTreeModel implements Serializable{
	
    private static final long serialVersionUID = 1L;
    
    /** 对应SysRole中的id字段,前端数据树中的key*/
    private String key;

    /** 对应SysRole中的id字段,前端数据树中的value*/
    private String value;

    /** 对应role_name字段,前端数据树中的title*/
    private String title;

    private boolean isLeaf;
    // 以下所有字段均与SysRole相同
    
    private String id;

    private String parentId;

    private String roleName;
    
    private String roleCode;
    
    private String description;
    
    private Integer roleType; // 1-操作角色，2-数据角色
    
    private Integer roleOrder;
    
    private Integer status; // 0-禁用，1-启用

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private List<SysRoleTreeModel> children = new ArrayList<>();

    /**
     * 将SysRole对象转换成SysRoleTreeModel对象
     * @param sysRole
     */
	public SysRoleTreeModel(SysRole sysRole) {
		this.key = sysRole.getId();
        this.value = sysRole.getId();
        this.title = sysRole.getRoleName();
        this.id = sysRole.getId();
        this.parentId = sysRole.getParentId();
        this.roleName = sysRole.getRoleName();
        this.roleCode = sysRole.getRoleCode();
        this.description = sysRole.getDescription();
        this.roleType = sysRole.getRoleType();
        this.roleOrder = sysRole.getRoleOrder();
        this.status = sysRole.getStatus();
        this.createBy = sysRole.getCreateBy();
        this.createTime = sysRole.getCreateTime();
        this.updateBy = sysRole.getUpdateBy();
        this.updateTime = sysRole.getUpdateTime();
    }

    public boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean isleaf) {
         this.isLeaf = isleaf;
    }

    public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SysRoleTreeModel> getChildren() {
        return children;
    }

    public void setChildren(List<SysRoleTreeModel> children) {
        if (children==null){
            this.isLeaf=true;
        }
        this.children = children;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public Integer getRoleOrder() {
        return roleOrder;
    }

    public void setRoleOrder(Integer roleOrder) {
        this.roleOrder = roleOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public SysRoleTreeModel() { }

    /**
     * 重写equals方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (o == null || getClass() != o.getClass()) {
			return false;
		}
        SysRoleTreeModel model = (SysRoleTreeModel) o;
        return Objects.equals(id, model.id) &&
                Objects.equals(parentId, model.parentId) &&
                Objects.equals(roleName, model.roleName) &&
                Objects.equals(roleCode, model.roleCode) &&
                Objects.equals(description, model.description) &&
                Objects.equals(roleType, model.roleType) &&
                Objects.equals(roleOrder, model.roleOrder) &&
                Objects.equals(status, model.status) &&
                Objects.equals(createBy, model.createBy) &&
                Objects.equals(createTime, model.createTime) &&
                Objects.equals(updateBy, model.updateBy) &&
                Objects.equals(updateTime, model.updateTime) &&
                Objects.equals(children, model.children);
    }
    
    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, roleName, roleCode, description, 
        		roleType, roleOrder, status, createBy, createTime, updateBy, updateTime, children);
    }
}

