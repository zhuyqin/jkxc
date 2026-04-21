package org.jeecg.modules.supplier.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 供货商地址信息
 * @Author: jeecg-boot
 * @Date:   2022-02-23
 * @Version: V1.0
 */
@Data
@TableName("gh_supplier")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_supplier对象", description="供货商地址信息")
public class GhSupplier implements Serializable {
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
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**供应商公司*/
	@Excel(name = "供应商公司", width = 15)
    @ApiModelProperty(value = "供应商公司")
    private String supplierCompany;
	/**供应商联系人*/
	@Excel(name = "供应商联系人", width = 15)
    @ApiModelProperty(value = "供应商联系人")
    private String supplierContact;
	/**供应商联系电话*/
	@Excel(name = "供应商联系电话", width = 15)
    @ApiModelProperty(value = "供应商联系电话")
    private String supplierContactNumber;
	/**是否被用*/
	@Excel(name = "是否被用", width = 15)
    @Dict(dicCode = "used")
    @ApiModelProperty(value = "是否被用")
    private String used;
	/**被用公司*/
	@Excel(name = "被用公司", width = 15)
    @ApiModelProperty(value = "被用公司")
    private String usedCompany;
    private String legalPerson;
    private String legalTel;
    private String addressRequirements;
    private String picId;
    private String remarks;
    private String companyName;
    private String bxAccount;
    private String bxBank;
}

