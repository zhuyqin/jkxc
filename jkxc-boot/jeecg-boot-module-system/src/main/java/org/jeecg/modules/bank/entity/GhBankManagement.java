package org.jeecg.modules.bank.entity;

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

import java.io.Serializable;

/**
 * @Description: 收款账号管理
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Data
@TableName("gh_bank_management")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_bank_management对象", description="收款账号管理")
public class GhBankManagement implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**收款单位/人*/
	@Excel(name = "收款单位/人", width = 15)
    @ApiModelProperty(value = "收款单位/人")
    private String payeePerson;
	/**收款方式*/
	@Excel(name = "收款方式", width = 15, dicCode = "payment_method")
	@Dict(dicCode = "payment_method")
    @ApiModelProperty(value = "收款方式")
    private String paymentMethod;
	/**收款账户*/
	@Excel(name = "收款账户", width = 15)
    @ApiModelProperty(value = "收款账户")
    private String collectionAccount;
	/**网点名称*/
	@Excel(name = "网点名称", width = 15)
    @ApiModelProperty(value = "网点名称")
    private String accountNotes;
	/**收款备注*/
	@Excel(name = "收款备注", width = 15)
    @ApiModelProperty(value = "收款备注")
    private String collectionRemarks;
	/**收款码*/
	@ApiModelProperty(value = "收款码")
    private String pic;
	/**是否隐藏*/
	@Excel(name = "是否隐藏", width = 15, dicCode = "yesno")
	@Dict(dicCode = "yesno")
    @ApiModelProperty(value = "是否隐藏")
    private String hidden;
	/**创建人*/
	@ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建时间*/
	@ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
	/**更新人*/
	@ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新时间*/
	@ApiModelProperty(value = "更新时间")
    private java.util.Date updateTime;
	/**删除标志*/
	@ApiModelProperty(value = "删除标志")
    private Integer delFlag;
}

