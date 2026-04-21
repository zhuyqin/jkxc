package org.jeecg.modules.salary.entity;

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
 * @Description: 工资发放记录
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Data
@TableName("gh_salary_payment")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_salary_payment对象", description="工资发放记录")
public class GhSalaryPayment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    
    /**员工ID*/
    @Excel(name = "员工姓名", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "员工ID")
    private String userId;
    
    /**员工姓名（冗余字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "员工姓名")
    private String userName;
    
    /**当前职位（冗余字段，从工资信息表关联获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "当前职位")
    private String currentPosition;
    
    /**当前星级（冗余字段，从工资信息表关联获取）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "当前星级")
    private String currentStarLevel;
    
    /**发放月份*/
    @Excel(name = "发放月份", width = 15)
    @ApiModelProperty(value = "发放月份（格式：YYYY-MM）")
    private String paymentMonth;
    
    /**基本工资*/
    @Excel(name = "基本工资", width = 15)
    @ApiModelProperty(value = "基本工资")
    private BigDecimal baseSalary;
    
    /**绩效工资*/
    @Excel(name = "绩效工资", width = 15)
    @ApiModelProperty(value = "绩效工资")
    private BigDecimal performanceSalary;
    
    /**公积金补贴*/
    @Excel(name = "公积金补贴", width = 15)
    @ApiModelProperty(value = "公积金补贴")
    private BigDecimal housingFundSubsidy;
    
    /**高温补贴*/
    @Excel(name = "高温补贴", width = 15)
    @ApiModelProperty(value = "高温补贴")
    private BigDecimal highTemperatureSubsidy;
    
    /**其他补贴*/
    @Excel(name = "其他补贴", width = 15)
    @ApiModelProperty(value = "其他补贴")
    private BigDecimal otherSubsidy;
    
    /**全勤奖金*/
    @Excel(name = "全勤奖金", width = 15)
    @ApiModelProperty(value = "全勤奖金")
    private BigDecimal fullAttendanceBonus;
    
    /**公司社保*/
    @Excel(name = "公司社保", width = 15)
    @ApiModelProperty(value = "公司社保")
    private BigDecimal companySocialSecurity;
    
    /**个人社保*/
    @Excel(name = "个人社保", width = 15)
    @ApiModelProperty(value = "个人社保")
    private BigDecimal personalSocialSecurity;
    
    /**应发总额*/
    @Excel(name = "应发总额", width = 15)
    @ApiModelProperty(value = "应发总额")
    private BigDecimal totalAmount;
    
    /**扣除总额*/
    @Excel(name = "扣除总额", width = 15)
    @ApiModelProperty(value = "扣除总额")
    private BigDecimal deductionAmount;
    
    /**实发金额*/
    @Excel(name = "实发金额", width = 15)
    @ApiModelProperty(value = "实发金额")
    private BigDecimal actualAmount;
    
    /**发放状态*/
    @Excel(name = "发放状态", width = 15, dicCode = "payment_status")
    @Dict(dicCode = "payment_status")
    @ApiModelProperty(value = "发放状态")
    private String paymentStatus;
    
    /**发放时间*/
    @Excel(name = "发放时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "发放时间")
    private Date paymentTime;
    
    /**发放方式*/
    @Excel(name = "发放方式", width = 15, dicCode = "payment_method")
    @Dict(dicCode = "payment_method")
    @ApiModelProperty(value = "发放方式")
    private String paymentMethod;
    
    /**发放账号ID*/
    @Excel(name = "发放账号", width = 20)
    @ApiModelProperty(value = "发放账号ID")
    private String paymentAccountId;
    
    /**发放账号（冗余字段）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "发放账号")
    private String paymentAccount;
    
    /**凭证附件*/
    @ApiModelProperty(value = "凭证附件")
    private String vouchers;
    
    /**备注*/
    @Excel(name = "备注", width = 30)
    @ApiModelProperty(value = "备注")
    private String remarks;
    
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

