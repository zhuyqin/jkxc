package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 流程审批业务类型绑定表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_audit_process_binding")
public class SysAuditProcessBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 流程ID（关联sys_audit_process表）
     */
    private String processId;
    
    /**
     * 业务类型ID（关联sys_category表，必须是最底层节点）
     */
    @Excel(name="业务类型ID",width=20)
    private String businessTypeId;
    
    /**
     * 业务类型编码（冗余字段，方便查询）
     */
    @Excel(name="业务类型编码",width=20)
    private String businessTypeCode;
    
    /**
     * 业务类型名称（冗余字段，方便查询）
     */
    @Excel(name="业务类型名称",width=30)
    private String businessTypeName;
    
    /**
     * 任务类型：once-一次性任务，recurring-周期任务
     */
    @Excel(name="任务类型",width=15, dicCode="task_type")
    private String taskType;
    
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

