package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 审批任务表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_audit_task")
public class SysAuditTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 订单ID（关联gh_order表）
     */
    private String orderId;
    
    /**
     * 订单编号（冗余字段）
     */
    @Excel(name="订单编号",width=20)
    private String orderNo;
    
    /**
     * 流程ID（关联sys_audit_process表）
     */
    private String processId;
    
    /**
     * 当前步骤ID（关联sys_audit_step表）
     */
    private String stepId;
    
    /**
     * 当前步骤顺序
     */
    @Excel(name="步骤顺序",width=10)
    private Integer stepOrder;
    
    /**
     * 任务类型：once-一次性任务，recurring-周期任务
     */
    @Excel(name="任务类型",width=15, dicCode="task_type")
    private String taskType;
    
    /**
     * 任务状态：pending-待审核，approved-已审核，rejected-已驳回
     */
    @Excel(name="任务状态",width=15, dicCode="task_status")
    private String taskStatus;
    
    /**
     * 指定审核人ID（如果配置了下个流程审批人员选择）
     */
    private String assignedUserId;
    
    /**
     * 指定审核人姓名（冗余字段）
     */
    @Excel(name="指定审核人",width=15)
    private String assignedUserName;
    
    /**
     * 当前审核角色ID
     */
    private String currentRoleId;
    
    /**
     * 当前审核角色名称（冗余字段）
     */
    @Excel(name="审核角色",width=20)
    private String currentRoleName;
    
    /**
     * 审核数据JSON（包含审核表单填写的数据）
     */
    private String auditData;
    
    /**
     * 审核结果：approved-通过，rejected-不通过，returned-驳回
     */
    @Excel(name="审核结果",width=15, dicCode="audit_result")
    private String auditResult;
    
    /**
     * 审核备注
     */
    @Excel(name="审核备注",width=40)
    private String auditRemark;
    
    /**
     * 驳回原因（驳回时填写）
     */
    @Excel(name="驳回原因",width=40)
    private String rejectReason;
    
    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="审核时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    
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
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="创建时间",width=20,format="yyyy-MM-dd HH:mm:ss")
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
     * 删除标志：0-正常，1-已删除
     */
    private Integer delFlag;
    
    // ========== 以下字段来自关联查询，不在sys_audit_task表中 ==========
    
    /**
     * 公司名称（来自gh_order表）
     */
    @TableField(exist = false)
    private String companyName;
    
    /**
     * 业务类型（来自gh_order表）
     */
    @TableField(exist = false)
    private String businessType;
    
    /**
     * 业务类型名称（来自sys_category表）
     */
    @TableField(exist = false)
    private String businessTypeName;
    
    /**
     * 业务人员（来自gh_order表）
     */
    @TableField(exist = false)
    private String salesman;
    
    /**
     * 订单创建时间（来自gh_order表）
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date orderCreateTime;
    
    /**
     * 订单备注（来自gh_order表）
     */
    @TableField(exist = false)
    private String remarks;
    
    /**
     * 当前审核步骤（来自gh_order表）
     */
    @TableField(exist = false)
    private Integer currentAuditStep;
    
    /**
     * 订单状态（来自gh_order表）
     */
    @TableField(exist = false)
    private String orderStatus;
    
    /**
     * 审核状态（来自gh_order表）
     */
    @TableField(exist = false)
    private String auditStatus;
    
    /**
     * 总步骤数（计算字段）
     */
    @TableField(exist = false)
    private Integer totalSteps;
    
    /**
     * 支出金额（计算字段，来自gh_order_expense表）
     */
    @TableField(exist = false)
    private BigDecimal expenseAmount;
    
    /**
     * 费用详情（计算字段，来自gh_order_expense表，格式：类目1:¥金额1; 类目2:¥金额2）
     */
    @TableField(exist = false)
    private String expenseDetail;
    
    /**
     * 所属团队名称（计算字段，来自sys_depart表）
     */
    @TableField(exist = false)
    private String teamName;
    
    /**
     * 接单时间（如果任务被指定给特定人员）
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date assignedTime;
    
    /**
     * 服务人员（周期任务专用，来自审核数据或订单）
     */
    @TableField(exist = false)
    private String serviceUserName;
    
    /**
     * 起始月份（周期任务专用）
     */
    @TableField(exist = false)
    private String startMonth;
    
    /**
     * 截至月份（周期任务专用）
     */
    @TableField(exist = false)
    private String endMonth;

}

