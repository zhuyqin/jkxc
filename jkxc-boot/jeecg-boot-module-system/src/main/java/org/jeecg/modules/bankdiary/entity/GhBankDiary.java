package org.jeecg.modules.bankdiary.entity;

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
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 银行日记
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Data
@TableName("gh_bank_diary")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_bank_diary对象", description="银行日记")
public class GhBankDiary implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    
    /**下单日期*/
    @Excel(name = "下单日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "下单日期")
    private Date orderDate;
    
    /**业务类型*/
    @Excel(name = "业务类型", width = 15, dicCode = "business_type")
    @Dict(dicCode = "business_type")
    @ApiModelProperty(value = "业务类型（reimbursement-报销，order-订单，manual-手动）")
    private String businessType;
    
    /**业务ID*/
    @ApiModelProperty(value = "业务ID（报销ID或订单ID）")
    private String businessId;
    
    /**费用详情*/
    @Excel(name = "费用详情", width = 30)
    @ApiModelProperty(value = "费用详情")
    private String feeDetail;
    
    /**业务类型（支出时的报销类目）*/
    @Excel(name = "业务类型", width = 15)
    @ApiModelProperty(value = "业务类型（支出时的报销类目ID）")
    @Dict(dictTable = "sys_category", dicText = "name", dicCode = "id")
    private String category;
    
    /**收入金额*/
    @Excel(name = "收入金额", width = 15)
    @ApiModelProperty(value = "收入金额")
    private BigDecimal incomeAmount;
    
    /**支出金额*/
    @Excel(name = "支出金额", width = 15)
    @ApiModelProperty(value = "支出金额")
    private BigDecimal expenseAmount;
    
    /**结余金额*/
    @Excel(name = "结余金额", width = 15)
    @ApiModelProperty(value = "结余金额")
    private BigDecimal balanceAmount;
    
    /**银行账户ID*/
    @Excel(name = "银行账户", width = 20)
    @ApiModelProperty(value = "银行账户ID")
    private String bankAccountId;
    
    /**银行账户名称（冗余字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "银行账户名称")
    private String bankAccountName;
    
    /**操作人员ID*/
    @Excel(name = "操作人员", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "操作人员ID")
    private String operatorId;
    
    /**操作人员姓名（冗余字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "操作人员姓名")
    private String operatorName;
    
    /**备注信息*/
    @Excel(name = "备注信息", width = 30)
    @ApiModelProperty(value = "备注信息")
    private String remarks;
    
    /**凭证附件（JSON数组）*/
    @ApiModelProperty(value = "凭证附件")
    private String vouchers;
    
    /**公司ID*/
    @ApiModelProperty(value = "公司ID")
    private String companyId;
    
    /**公司名称*/
    @Excel(name = "公司名称", width = 20)
    @ApiModelProperty(value = "公司名称")
    private String companyName;
    
    /**关联报销ID*/
    @ApiModelProperty(value = "关联报销ID")
    private String reimbursementId;
    
    /**关联订单ID*/
    @ApiModelProperty(value = "关联订单ID")
    private String orderId;
    
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
    
    /**删除标志*/
    @ApiModelProperty(value = "删除标志")
    private Integer delFlag;
}

