package org.jeecg.modules.order.entity;

import java.io.Serializable;
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
 * 订单审核记录表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhOrderAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 审核流程ID（关联sys_audit_process表）
     */
    private String auditProcessId;
    
    /**
     * 审核步骤顺序（从1开始）
     */
    @Excel(name="步骤顺序",width=10)
    private Integer stepOrder;
    
    /**
     * 审核角色ID（关联sys_role表）
     */
    private String roleId;
    
    /**
     * 审核角色名称
     */
    @Excel(name="审核角色",width=20)
    private String roleName;
    
    /**
     * 审核状态：pending-待审核,approved-已通过,rejected-已驳回
     */
    @Excel(name="审核状态",width=15)
    private String auditStatus;
    
    /**
     * 审核人ID
     */
    private String auditUserId;
    
    /**
     * 审核人姓名
     */
    @Excel(name="审核人",width=15)
    private String auditUserName;
    
    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="审核时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    
    /**
     * 审核备注
     */
    @Excel(name="审核备注",width=40)
    private String auditRemark;
    
    /**
     * 驳回原因
     */
    @Excel(name="驳回原因",width=40)
    private String rejectReason;
    
    /**
     * 是否重新提交：0-否，1-是
     */
    @Excel(name="是否重新提交",width=15, dicCode="yn")
    private Integer isResubmit;
    
    /**
     * 重新提交时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="重新提交时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date resubmitTime;
    
    /**
     * 父审核记录ID（驳回后重新提交时关联原审核记录）
     */
    private String parentAuditId;
    
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

