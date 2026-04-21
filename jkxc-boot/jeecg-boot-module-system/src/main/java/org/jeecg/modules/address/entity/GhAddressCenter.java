package org.jeecg.modules.address.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * @Description: 地址中心
 * @Author: jeecg-boot
 * @Date:   2025-01-10
 * @Version: V1.0
 */
@Data
@TableName("gh_address_center")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_address_center对象", description="地址中心")
public class GhAddressCenter implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    
    /**公司名称*/
    @Excel(name = "公司名称", width = 15)
    @ApiModelProperty(value = "公司名称")
    private String companyName;
    
    /**业务员*/
    @Excel(name = "业务员", width = 15)
    @ApiModelProperty(value = "业务员")
    private String salesman;

    /**财税顾问*/
    @Excel(name = "财税顾问", width = 15)
    private String advisor;
    
    /**是否记账*/
    @Excel(name = "是否记账", width = 15, dicCode = "yesno")
    @Dict(dicCode = "yesno")
    @ApiModelProperty(value = "是否记账")
    private String sfjz;

    /**是否返税*/
    @Excel(name = "是否返税", width = 15, dicCode = "yesno")
    @Dict(dicCode = "yesno")
    @ApiModelProperty(value = "是否返税")
    private String sffs;
    
    /**地址状态*/
    @Excel(name = "地址状态", width = 15, dicCode = "enterprise_status")
    @Dict(dicCode = "enterprise_status")
    @ApiModelProperty(value = "地址状态")
    private String addressStatus;
    
    /**服务状态：1-服务中，2-已终止*/
    @Excel(name = "服务状态", width = 15, dicCode = "service_status")
    @Dict(dicCode = "service_status")
    @ApiModelProperty(value = "服务状态：1-服务中，2-已终止")
    private String serviceStatus;
    
    /**流失标志：0-正常，1-已流失*/
    @Excel(name = "流失标志", width = 15)
    @ApiModelProperty(value = "流失标志：0-正常，1-已流失")
    private Integer lossFlag;
    
    /**续费价格*/
    @Excel(name = "续费价格", width = 15)
    @ApiModelProperty(value = "续费价格")
    private Integer renewalFee;
    
    /**地址成本*/
    @Excel(name = "地址成本", width = 15)
    @ApiModelProperty(value = "地址成本")
    private java.math.BigDecimal addressCost;
    
    /**起始日期*/
    @Excel(name = "起始日期", width = 15, format = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "起始日期")
    private Date startDate;
    
    /**终止日期*/
    @Excel(name = "终止日期", width = 15, format = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "终止日期")
    private Date terminationDate;

    /**供应商*/
    @Dict(dicCode = "supplier")
    @ApiModelProperty(value = "供应商")
    private String supplier;
    
    /**街道地址*/
    @ApiModelProperty(value = "街道地址")
    private String streetAddress;
    
    /**园区地址*/
    @ApiModelProperty(value = "园区地址")
    private String parkAddress;
    
    /**供应商公司*/
    @Excel(name = "供应商公司", width = 15)
    @ApiModelProperty(value = "供应商公司")
    private String supplierCompany;
    
    /**供应商联系人*/
    @ApiModelProperty(value = "供应商联系人")
    private String supplierContact;
    
    /**供应商联系电话*/
    @ApiModelProperty(value = "供应商联系电话")
    private String supplierContactNumber;

    /**合同金额*/
    @ApiModelProperty(value = "合同金额")
    private Integer contractAmount;

    /**申请开票*/
    @ApiModelProperty(value = "申请开票")
    private String applyForInvoicing;
    
    /**收费类型*/
    @ApiModelProperty(value = "收费类型")
    @Dict(dicCode = "charge_type")
    private String chargeType;

    /**备注*/
    @ApiModelProperty(value = "备注")
    private String remarks;
    
    /**合同照片*/
    @ApiModelProperty(value = "合同照片")
    private String picId;
    
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    
    /**公司资料id*/
    @ApiModelProperty(value = "公司资料id")
    private String dataId;

    private String reimbursementFlag;
    
    /**订单ID（通过contractId字段存储订单ID，用于关联订单）*/
    private String contractId;
    
    private String receivedAmout;
    private String paymentMethod;
    private String collectionAccountNumber;
    private String renewId;
    
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**更新时间*/
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    
    /**删除标志：0-正常，1-已删除*/
    @ApiModelProperty(value = "删除标志")
    private Integer delFlag;
    
    /**倒计天数（计算字段，不存储在数据库）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "倒计天数")
    private Integer countdownDays;
    
    /**累计时间（计算字段，不存储在数据库）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "累计时间")
    private String accumulatedTime;
    
    /**企业等级（计算字段，不存储在数据库）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "企业等级")
    private String enterpriseLevel;
    
    /**消费金额（计算字段，不存储在数据库）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "消费金额")
    private java.math.BigDecimal totalSpending;
    
    /**业务标签（计算字段，不存储在数据库）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "业务标签")
    private String businessTag;
    
    /**关联企业（计算字段，不存储在数据库）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "关联企业")
    private String relatedCompanyName;
    
    /**客户订单总数（计算字段，不存储在数据库，用于显示复购信息）*/
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "客户订单总数")
    private Integer customerOrderCount;

    /**
     * 关联订单审核步骤摘要（列表展示：每行含 stepOrder、stepStatus、roleName、personText）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @ApiModelProperty(value = "审核步骤摘要")
    private List<Map<String, Object>> auditStepSummary;
}

