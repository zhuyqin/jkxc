package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 审核流程表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysAuditProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 流程名称（如：订单审批、会计合同审批）
     */
    @Excel(name="流程名称",width=20)
    private String processName;
    
    /**
     * 流程编码（唯一标识）
     */
    @Excel(name="流程编码",width=20)
    private String processCode;
    
    /**
     * 流程描述
     */
    @Excel(name="流程描述",width=40)
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @Excel(name="状态",width=10, dicCode="yn")
    private Integer status;
    
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
    
    /**
     * 审核步骤列表（不映射到数据库）
     */
    @TableField(exist = false)
    private List<SysAuditStep> steps;
    
    /**
     * 业务类型绑定列表（不映射到数据库）
     */
    @TableField(exist = false)
    private List<SysAuditProcessBinding> bindings;

}

