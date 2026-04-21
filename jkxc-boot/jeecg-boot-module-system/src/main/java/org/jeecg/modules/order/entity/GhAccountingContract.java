package org.jeecg.modules.order.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;

/**
 * @Description: 合同管理表
 * @Author: jeecg-boot
 * @Date: 2025-01-03
 * @Version: V1.0
 */
@Data
@TableName("gh_accounting_contract")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_accounting_contract对象", description="合同管理表")
public class GhAccountingContract implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
	/**合同编号*/
	@Excel(name = "合同编号", width = 15)
    @ApiModelProperty(value = "合同编号")
    private String contractNo;
	/**订单ID*/
	@Excel(name = "订单ID", width = 15)
    @ApiModelProperty(value = "订单ID")
    private String orderId;
	/**订单编号（冗余字段）*/
	@Excel(name = "订单编号", width = 15)
    @ApiModelProperty(value = "订单编号（冗余字段）")
    private String orderNo;
	/**公司名称（冗余字段）*/
	@Excel(name = "公司名称", width = 15)
    @ApiModelProperty(value = "公司名称（冗余字段）")
    private String companyName;
	/**服务人员*/
	@Excel(name = "服务人员", width = 15)
    @ApiModelProperty(value = "服务人员")
    private String servicePerson;

	/**服务人员JSON*/
    @ApiModelProperty(value = "服务人员JSON")
    @TableField("service_staff_json")
    private String serviceStaffJson;
	/**合同金额*/
	@Excel(name = "合同金额", width = 15)
    @ApiModelProperty(value = "合同金额")
    private BigDecimal contractAmount;
	/**签订日期*/
	@Excel(name = "签订日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "签订日期")
    private Date signDate;
	/**到期时间*/
	@Excel(name = "到期时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "到期时间")
    private Date expireDate;
	/**合同状态：draft-草稿,signed-已签订,executing-执行中,completed-已完成,terminated-已终止*/
	@Excel(name = "合同状态", width = 15, dicCode = "contract_status")
    @ApiModelProperty(value = "合同状态：draft-草稿,signed-已签订,executing-执行中,completed-已完成,terminated-已终止")
    private String contractStatus;
	/**流失标志：0-正常，1-已流失*/
	@Excel(name = "流失标志", width = 15, dicCode = "loss_flag")
    @ApiModelProperty(value = "流失标志：0-正常，1-已流失")
    private Integer lossFlag;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remarks;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
	/**删除标志：0-正常，1-已删除*/
	@Excel(name = "删除标志", width = 15, dicCode = "del_flag")
    @ApiModelProperty(value = "删除标志：0-正常，1-已删除")
    private Integer delFlag;
	
	/**消费金额（计算字段，不存储在数据库）*/
	@TableField(exist = false)
    @ApiModelProperty(value = "消费金额（计算字段）")
    private java.math.BigDecimal totalSpending;
	
	/**业务标签（计算字段，不存储在数据库）*/
	@TableField(exist = false)
    @ApiModelProperty(value = "业务标签（计算字段）")
    private String businessTag;
	
	/**关联企业（计算字段，不存储在数据库）*/
	@TableField(exist = false)
    @ApiModelProperty(value = "关联企业（计算字段）")
    private String relatedCompanyName;
	
	/**倒计天数（计算字段，不存储在数据库）*/
	@TableField(exist = false)
    @ApiModelProperty(value = "倒计天数（计算字段）")
    private Integer countdownDays;
	
	/**累计时间（服务月数，计算字段，不存储在数据库）*/
	@TableField(exist = false)
    @ApiModelProperty(value = "累计时间（服务月数，计算字段）")
    private Integer serviceMonths;
	
	/**企业等级（计算字段，不存储在数据库）*/
	@TableField(exist = false)
	@Dict(dicCode = "enterprise_level")
    @ApiModelProperty(value = "企业等级（计算字段）")
    private String enterpriseLevel;
    
    /**客户订单总数（计算字段，不存储在数据库，用于显示复购信息）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "客户订单总数（计算字段）")
    private Integer customerOrderCount;
    
    /**签单人员（计算字段，从订单获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "签单人员（计算字段）")
    private String signer;
    
    /**来源（计算字段，从订单获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "来源（计算字段）")
    private String source;
    
    /**来源字典文本（计算字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "来源字典文本（计算字段）")
    private String source_dictText;
    
    /**产品名称（计算字段，从订单的业务类型获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "产品名称（计算字段）")
    private String productName;
    
    /**业务类型（计算字段，从订单获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "业务类型（计算字段）")
    private String businessType;
    
    /**业务类型字典文本（计算字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "业务类型字典文本（计算字段）")
    private String businessType_dictText;
    
    /**业务类型名称（计算字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "业务类型名称（计算字段）")
    private String businessTypeName;
    
    /**续签状态（计算字段，不存储在数据库）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "续签状态（计算字段）")
    private String renewalStatus;
    
    /**到款金额（计算字段，从订单获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "到款金额（计算字段）")
    private java.math.BigDecimal paidAmount;

    /**
     * 关联订单审核步骤摘要（列表「服务人员」列中审批后续步骤展示，不存库）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "审核步骤摘要")
    private List<Map<String, Object>> auditStepSummary;
}

