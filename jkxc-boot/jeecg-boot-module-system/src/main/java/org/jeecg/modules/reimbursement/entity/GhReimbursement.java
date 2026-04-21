package org.jeecg.modules.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 报销审批
 * @Author: jeecg-boot
 * @Date:   2022-03-19
 * @Version: V1.0
 */
@Data
@TableName("gh_reimbursement")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_reimbursement对象", description="报销审批")
public class GhReimbursement implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**报销名称*/
	@Excel(name = "报销名称", width = 15)
    @ApiModelProperty(value = "报销名称")
    private String reimbursementName;

    @ApiModelProperty(value = "供应商联系人")
    @TableField(exist = false)
    private String supplierContact;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
    private Double unitPrice;
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
    private Double num;
	/**总价*/
	@Excel(name = "总价", width = 15)
    @ApiModelProperty(value = "总价")
    private Double totalPrice;

    @TableField(exist = false)
    @ApiModelProperty(value = "所属部门")
    private String depart;
	/**报销类目*/
	@Excel(name = "报销类目", width = 15)
    @ApiModelProperty(value = "报销类目")
    @Dict(dictTable = "sys_category", dicText = "name", dicCode = "id")
    private String category;
    @Dict(dicCode = "audit_status")
    private String auditFlag;
    private String companyId;
    private String companyName;
    private String contractAmount;
    private String cost;
    private String contractNo;
    private String remarks;
    @Dict(dicCode = "audit_status")
    private String zgauditFlag;
    @Dict(dicCode = "audit_status")
    private String jlauditFlag;

    @Dict(dicCode = "payment_method")
    private String paymentMethod;
    private String collectionAccountNumber;
    private String vouchers;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date collectionTime;
    private String bxAccount;
    private String bxBank;
    private String bxCompanyName;
    private String bxRemarks;
    private String zfVouchers;
    private String zfRemark;
    private String supplierCompany;

    private String contractId;

    /**报销时间*/
    @Excel(name = "报销时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "报销时间")
    private Date reimbursementTime;
    
    /**报销人员*/
    @Excel(name = "报销人员", width = 15)
    @ApiModelProperty(value = "报销人员")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "realname")
    private String reimbursementPerson;
    
    /**所属团队*/
    @Excel(name = "所属团队", width = 15, dictTable = "sys_team", dicText = "team_name", dicCode = "id")
    @ApiModelProperty(value = "所属团队")
    @Dict(dictTable = "sys_team", dicText = "team_name", dicCode = "id")
    private String teamId;
    
    /**报销依据*/
    @Excel(name = "报销依据", width = 15)
    @ApiModelProperty(value = "报销依据")
    private String reimbursementBasis;
    
    /**报销账号ID（用于三联级选择）*/
    @ApiModelProperty(value = "报销账号ID")
    private String reimbursementAccountId;
    
    /**支付依据*/
    @Excel(name = "支付依据", width = 15)
    @ApiModelProperty(value = "支付依据")
    private String paymentBasis;
    
    /**支付银行开户行*/
    @Excel(name = "支付银行开户行", width = 15)
    @ApiModelProperty(value = "支付银行开户行")
    private String paymentBankName;
    
    /**支付银行卡号*/
    @Excel(name = "支付银行卡号", width = 15)
    @ApiModelProperty(value = "支付银行卡号")
    private String paymentBankAccount;
    
    /**支付单位ID（银行账户ID）*/
    @ApiModelProperty(value = "支付单位ID")
    private String paymentBankAccountId;
    
    /**订单支出记录ID*/
    @ApiModelProperty(value = "订单支出记录ID")
    private String orderExpenseId;
    
    /**删除标志：0-正常，1-已删除*/
    @ApiModelProperty(value = "删除标志")
    private Integer delFlag;
    
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 1查询没有人工提成的数据
     */
    @TableField(exist = false)
    private String searchStatus;
    @TableField(exist = false)
    private String searchIds;
    
    /**所属团队名称（用于显示）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "所属团队名称")
    private String teamName;
    
    /**报销账号显示（用于列表显示三联级）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "报销账号显示")
    private String reimbursementAccountDisplay;
    
    /**支付账号显示（用于列表显示）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "支付账号显示")
    private String paymentAccountDisplay;
}

