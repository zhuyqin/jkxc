package org.jeecg.modules.order.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecg.common.aspect.annotation.Dict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单主表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 订单编号
     */
    @Excel(name="订单编号",width=20)
    private String orderNo;
    
    /**
     * 公司ID
     */
    private String companyId;
    
    /**
     * 公司名称
     */
    @Excel(name="公司名称",width=30)
    private String companyName;
    
    /**
     * 业务员
     */
    @Excel(name="业务员",width=15)
    private String salesman;
    
    /**
     * 业务类型
     */
    @Excel(name="业务类型",width=20)
    @Dict(dicCode = "id", dicText = "name", dictTable = "sys_category")
    private String businessType;
    
    /**
     * 订单金额（合同金额）
     */
    @Excel(name="订单金额",width=15)
    private BigDecimal orderAmount;
    
    /**
     * 合同金额
     */
    private BigDecimal contractAmount;
    
    /**
     * 尾款金额
     */
    private BigDecimal finalPaymentAmount;
    
    /**
     * 收款金额
     */
    private BigDecimal receivedAmount;
    
    /**
     * 所属区域
     */
    private String region;
    
    /**
     * 联系人员
     */
    private String contacts;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 联系方式
     */
    private String contactInformation;
    
    /**
     * 商机来源
     */
    @Excel(name="商机来源",width=15)
    @Dict(dicCode = "opportunity_source")
    private String opportunitySource;
    
    /**
     * 交单方式
     */
    private String deliveryMethod;
    
    /**
     * 流程审批ID（关联sys_audit_process表，已废弃，由业务类型自动匹配）
     */
    private String auditProcessId;
    
    /**
     * 自动匹配的流程ID（根据业务类型自动匹配）
     */
    private String matchedProcessId;
    
    /**
     * 任务类型（从流程配置中获取：once-一次性任务，recurring-周期任务）
     */
    private String taskType;
    
    /**
     * 当前审核步骤顺序
     */
    private Integer currentAuditStep;
    
    /**
     * 审核状态：pending-待审核,approved-已通过,rejected-已驳回
     */
    private String auditStatus;
    
    /**
     * 收款时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date collectionTime;
    
    /**
     * 收款账号
     */
    private String collectionAccountNumber;
    
    /**
     * 周期业务：0-否，1-是
     */
    private String isRecurring;
    
    /**
     * 图片凭证
     */
    private String imageVoucher;
    
    /**
     * 订单状态：1-进行中，2-已完成
     */
    @Excel(name="订单状态",width=15, dicCode="order_status")
    @Dict(dicCode = "order_status")
    private String orderStatus;
    
    /**
     * 操作类型
     */
    private String operationType;
    
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
     * 订单流程步骤列表（不映射到数据库）
     */
    @TableField(exist = false)
    private List<GhOrderStep> steps;
    
    /**
     * 订单状态字典文本（不映射到数据库）
     */
    @TableField(exist = false)
    private String orderStatus_dictText;
    
    /**
     * 业务类型字典文本（不映射到数据库）
     */
    @TableField(exist = false)
    private String businessType_dictText;
    
    /**
     * 第一个审核步骤通过的时间（计算字段，用于计算进行中天数）
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date firstAuditTime;
    
    /**
     * 动态表单版本ID（不映射到数据库）
     */
    @TableField(exist = false)
    private String dynamicFormVersionId;
    
    /**
     * 表单版本ID（不映射到数据库，用于兼容）
     */
    @TableField(exist = false)
    private String formVersionId;
    
    /**
     * 表单ID（不映射到数据库）
     */
    @TableField(exist = false)
    private String formId;
    
    /**
     * 动态表单数据（不映射到数据库）
     */
    @TableField(exist = false)
    private String dynamicFormData;
    
    /**
     * 动态表单配置（不映射到数据库，保存创建时的表单配置，用于查看详情时还原）
     */
    @TableField(exist = false)
    private String dynamicFormConfig;
    
    /**
     * 审核记录列表（不映射到数据库，用于前端判断）
     */
    @TableField(exist = false)
    private List<org.jeecg.modules.order.entity.GhOrderAudit> orderAudits;
    
    /**
     * 任务状态（不映射到数据库，用于前端判断是否可以确认）
     */
    @TableField(exist = false)
    private String taskStatus;
    
    /**
     * 交接状态（不映射到数据库，用于前端判断是否可以确认）
     */
    @TableField(exist = false)
    private String handoverStatus;
    
    /**
     * 所属团队名称（不映射到数据库，通过业务员关联查询）
     */
    @TableField(exist = false)
    private String teamName;
    
    /**
     * 商机来源字典文本（不映射到数据库）
     */
    @TableField(exist = false)
    private String opportunitySource_dictText;

    
    /**
     * 未收金额（不映射到数据库，用于查询未收全订单时计算）
     */
    @TableField(exist = false)
    private BigDecimal unreceivedAmount;
    
    /**
     * 客户订单总数（不映射到数据库，用于显示复购信息）
     */
    @TableField(exist = false)
    private Integer customerOrderCount;
}

