package org.jeecg.modules.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 报销审批DTO
 * @Author: jeecg-boot
 * @Date:   2022-03-19
 * @Version: V1.0
 */
@Data
public class GhReimbursementDTO implements Serializable {
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
	/**报销类目*/
	@Excel(name = "报销类目", width = 15)
    @ApiModelProperty(value = "报销类目")
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
    private String batchIds;
    private String batchCompanyName;
}

