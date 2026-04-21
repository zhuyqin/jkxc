package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
 * 审批任务成本记录表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_audit_task_cost")
public class SysAuditTaskCost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 任务ID（关联sys_audit_task表）
     */
    private String taskId;
    
    /**
     * 指标ID（关联sys_audit_form_indicator表）
     */
    private String indicatorId;
    
    /**
     * 报销名称
     */
    private String expenseName;
    
    /**
     * 报销类目ID（关联sys_category表）
     */
    private String categoryId;
    
    /**
     * 报销类目名称（冗余字段）
     */
    private String categoryName;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
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

}

