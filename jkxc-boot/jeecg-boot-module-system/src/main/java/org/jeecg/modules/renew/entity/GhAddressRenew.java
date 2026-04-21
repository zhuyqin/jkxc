package org.jeecg.modules.renew.entity;

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

import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @Description: 地址中心续费信息
 * @Author: jeecg-boot
 * @Date:   2022-03-04
 * @Version: V1.0
 */
@Data
@TableName("gh_address_renew")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_address_renew对象", description="地址中心续费信息")
public class GhAddressRenew implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**地址id*/
	@Excel(name = "地址id", width = 15)
    @ApiModelProperty(value = "地址id")
    private String addressId;
	/**续费时间*/
	@Excel(name = "续费时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "续费时间")
    private java.util.Date renewalTime;
	/**原到期日期*/
	@Excel(name = "原到期日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "原到期日期")
    private java.util.Date originalDueDate;
	/**后到期日期*/
	@Excel(name = "后到期日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "后到期日期")
    private java.util.Date postExpirationDate;


    @TableField(exist = false)
    @Excel(name = "所属部门", width = 15)
    @Transient
    private String depart;
	/**应续费金额*/
	@Excel(name = "应续费金额", width = 15)
    @ApiModelProperty(value = "应续费金额")
    private String enewaAmount;

    @ApiModelProperty(value = "原合同应续费金额")
    @TableField(exist = false)
    private String yhtAmount;
    @ApiModelProperty(value = "原合同到款金额")
    @TableField(exist = false)
    private String yhtReceiveAmount;
	/**到款金额*/
	@Excel(name = "到款金额", width = 15)
    @ApiModelProperty(value = "到款金额")
    private String amountReceived;
	/**收款时间*/
	@Excel(name = "收款时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "收款时间")
    private java.util.Date paymentTime;
	/**财务审核*/
	@Excel(name = "财务审核", width = 15)
    @ApiModelProperty(value = "财务审核")
    @Dict(dicCode = "audit_status")
    private String financialAudit;
	/**审核状态：0-待审核，1-已通过，2-已驳回*/
	@Excel(name = "审核状态", width = 15)
    @ApiModelProperty(value = "审核状态：0-待审核，1-已通过，2-已驳回")
    @Dict(dicCode = "audit_status")
    private String auditStatus;
    /**财务审核*/
    @Excel(name = "公司名称", width = 15)
    private String companyName;
    private String companyId;

    private String amounts;
    @Dict(dicCode = "detail_type")
    @Excel(name="收款类型",width=15,dicCode="detail_type")
    private String detailType;
    @TableField(exist = false)
    private String detailType_dictText;
    private String businessType;
    @TableField(exist = false)
    private String businessTypeValue;
    private Double chargeAmount;
    @Dict(dicCode = "payment_method")
    private String paymentMethod;
    @TableField(exist = false)
    private String paymentMethod_dictText;
    @TableField(exist = false)
    private String payeePerson;
    @TableField(exist = false)
    private String accountNotes;
    @TableField(exist = false)
    private String collectionAccount;

    private java.util.Date createTime;
    private String vouchers;

    @Excel(name = "收款账号", width = 15,dicCode ="id",dictTable = "gh_bank_management",dicText = "collection_account")
    private String collectionAccountNumber;
    private String contractNo;
    private String remarks;
    private String htId;
    private String bjFlag;
    @Excel(name = "签单员", width = 15,dicCode ="sale_man")
    private String saleMan;
    private String creator;


    @Dict(dicCode = "address_status")
    private String addressAudit;


    @TableField(exist = false)
    private String searchDate;


    @TableField(exist = false)
    private String searchIds;


    @TableField(exist = false)
    private String searchCompanyIds;

}

