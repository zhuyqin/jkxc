package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 父级角色ID（支持树结构）
     */
    private String parentId;
    
    /**
     * 角色名称
     */
    @Excel(name="角色名",width=15)
    private String roleName;
    
    /**
     * 角色编码
     */
    @Excel(name="角色编码",width=15)
    private String roleCode;
    
    /**
     * 角色类型：1-操作角色，2-数据角色
     */
    @Excel(name="角色类型",width=15, dicCode="role_type")
    private Integer roleType;
    
    /**
     * 角色排序
     */
    private Integer roleOrder;
    
    /**
     * 角色状态：0-禁用，1-启用
     */
    @Excel(name="角色状态",width=15, dicCode="role_status")
    private Integer status;
    
    /**
          * 描述
     */
    @Excel(name="描述",width=60)
    private String description;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


}
