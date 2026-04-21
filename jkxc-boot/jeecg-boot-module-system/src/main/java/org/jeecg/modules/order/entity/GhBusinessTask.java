package org.jeecg.modules.order.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
 * 工商任务表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhBusinessTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 订单ID
     */
    @Excel(name="订单ID",width=20)
    private String orderId;
    
    /**
     * 订单编号（冗余字段）
     */
    @Excel(name="订单编号",width=20)
    private String orderNo;
    
    /**
     * 公司名称（冗余字段）
     */
    @Excel(name="公司名称",width=30)
    private String companyName;
    
    /**
     * 任务状态：pending_manager_audit-待工商经理审核,public_sea-公海待接收,assigned_to_me-待本人接收,task-任务,handover-交接,completed-已完单,problem_task-问题任务,recycle_bin-回收站
     */
    @Excel(name="任务状态",width=20)
    private String taskStatus;
    
    /**
     * 工商经理审核状态：pending-待审核,approved-已通过,rejected-已驳回
     */
    @Excel(name="经理审核状态",width=15)
    private String managerAuditStatus;
    
    /**
     * 工商经理审核时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="经理审核时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date managerAuditTime;
    
    /**
     * 工商经理审核人ID
     */
    private String managerAuditUserId;
    
    /**
     * 工商经理审核人姓名
     */
    @Excel(name="经理审核人",width=15)
    private String managerAuditUserName;
    
    /**
     * 工商经理审核备注
     */
    @Excel(name="经理审核备注",width=40)
    private String managerAuditRemark;
    
    /**
     * 分配类型：public_sea-放入公海,assign_user-指定工商人员
     */
    @Excel(name="分配类型",width=15)
    private String assignType;
    
    /**
     * 分配的工商人员ID
     */
    private String assignedUserId;
    
    /**
     * 分配的工商人员姓名
     */
    @Excel(name="分配人员",width=15)
    private String assignedUserName;
    
    /**
     * 分配时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="分配时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date assignedTime;
    
    /**
     * 接收人ID
     */
    private String receivedUserId;
    
    /**
     * 接收人姓名
     */
    @Excel(name="接收人",width=15)
    private String receivedUserName;
    
    /**
     * 接收时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="接收时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date receivedTime;
    
    /**
     * 成本类目
     */
    @Excel(name="成本类目",width=20)
    private String costCategory;
    
    /**
     * 成本金额
     */
    @Excel(name="成本金额",width=15)
    private BigDecimal costAmount;
    
    /**
     * 交接人ID（工商人员）
     */
    private String handoverUserId;
    
    /**
     * 交接人姓名
     */
    @Excel(name="交接人",width=15)
    private String handoverUserName;
    
    /**
     * 交接时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="交接时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date handoverTime;
    
    /**
     * 交接状态：pending-待交接,handovered-已交接,completed-交接完成
     */
    @Excel(name="交接状态",width=15)
    private String handoverStatus;
    
    /**
     * 业务人员ID
     */
    private String businessUserId;
    
    /**
     * 业务人员姓名
     */
    @Excel(name="业务人员",width=15)
    private String businessUserName;
    
    /**
     * 完成时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="完成时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
    
    /**
     * 问题原因
     */
    @Excel(name="问题原因",width=40)
    private String problemReason;
    
    /**
     * 回收原因
     */
    @Excel(name="回收原因",width=40)
    private String recycleReason;
    
    /**
     * 备注
     */
    @Excel(name="备注",width=40)
    private String remarks;
    
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
    
    /**
     * 任务状态字典文本（不映射到数据库）
     */
    @TableField(exist = false)
    private String taskStatus_dictText;
}

