package org.jeecg.modules.system.entity;

import java.io.Serializable;
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
 * 审批表单特殊指标配置表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_audit_form_indicator")
public class SysAuditFormIndicator implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 步骤表单ID（关联sys_audit_step_form表）
     */
    private String stepFormId;
    
    /**
     * 指标类型：next_auditor-下个流程审批人员选择，cost_input-成本填写
     */
    private String indicatorType;
    
    /**
     * 指标名称（显示名称）
     */
    private String indicatorName;
    
    /**
     * 指标配置JSON（根据不同类型存储不同配置）
     */
    private String indicatorConfig;
    
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

