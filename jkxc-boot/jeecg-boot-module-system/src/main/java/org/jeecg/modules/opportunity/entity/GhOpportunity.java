package org.jeecg.modules.opportunity.entity;

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
 * @Description: 商机管理
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Data
@TableName("gh_opportunity")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_opportunity对象", description="商机管理")
public class GhOpportunity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    
    /**线索编号*/
    @ApiModelProperty(value = "线索编号")
    private String opportunityNo;
    
    /**公司名称（客户名称）*/
    @Excel(name = "客户名称", width = 20, orderNum = "1")
    @ApiModelProperty(value = "公司名称")
    private String corporateName;
    
    /**线索名称（选项来自分类 pcode=A01；存分类 id 时列表可自动翻译为中文名）*/
    @Excel(name = "线索名称", width = 20, orderNum = "5")
    @Dict(dictTable = "sys_category", dicText = "name", dicCode = "id")
    @ApiModelProperty(value = "线索名称")
    private String opportunityName;

    /**线索名称中文展示（分类 name，列表/导出用；opportunityName 存 id 时由接口填充）*/
    @TableField(exist = false)
    @ApiModelProperty(value = "线索名称_翻译")
    private String opportunityName_dictText;
    
    /**负责人*/
    @ApiModelProperty(value = "负责人")
    private String chargePerson;
    
    /**联系人（联系人员）*/
    @Excel(name = "联系人员", width = 15, orderNum = "3")
    @ApiModelProperty(value = "联系人")
    private String contacts;
    
    /**联系方式*/
    @Excel(name = "联系方式", width = 15, orderNum = "4")
    @ApiModelProperty(value = "联系方式")
    private String contactInformation;
    
    /**状态（当前状态）*/
    @Excel(name = "当前状态", width = 15, orderNum = "8")
    @Dict(dicCode = "opportunity_status")
    @ApiModelProperty(value = "状态：意向a+、意向a、意向b、意向c、无效客户")
    private String state;
    
    /**归属类型：public-公海，personal-个人，invalid-无效*/
    @ApiModelProperty(value = "归属类型：public-公海，personal-个人，invalid-无效")
    private String ownerType;
    
    /**商机来源（客户来源）*/
    @Excel(name = "客户来源", width = 15, orderNum = "6")
    @Dict(dicCode = "opportunity_source")
    @ApiModelProperty(value = "商机来源")
    private String opportunitySource;
    
    /**创建人（业务人员）*/
    @Excel(name = "业务人员", width = 15, orderNum = "0")
    @ApiModelProperty(value = "创建人")
    private String founder;
    
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    
    /**修改时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private Date modiTime;
    
    /**备注*/
    @Excel(name = "备注", width = 30, orderNum = "10")
    @ApiModelProperty(value = "备注")
    private String remarks;

    /**备注图片（多图，逗号分隔或JSON数组）*/
    @ApiModelProperty(value = "备注图片")
    @TableField("remark_images")
    private String remarkImages;
    
    /**客户编号*/
    @ApiModelProperty(value = "客户编号")
    private String customerId;
    
    /**所属区域*/
    @Excel(name = "所属区域", width = 25, orderNum = "7")
    @ApiModelProperty(value = "所属区域")
    private String region;
    
    /**详细地址*/
    @Excel(name = "详细地址", width = 40, orderNum = "9")
    @ApiModelProperty(value = "详细地址")
    private String address;
    
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    
    /**删除标志*/
    @ApiModelProperty(value = "删除标志")
    private Integer delFlag;

    /**线索奖金（发放后写入）*/
    @Excel(name = "线索奖金", width = 20, orderNum = "11")
    @ApiModelProperty(value = "线索奖金")
    @TableField("clue_bonus")
    private BigDecimal clueBonus;

    /**线索奖金已发放标识*/
    @ApiModelProperty(value = "线索奖金已发放标识")
    @TableField("clue_bonus_awarded")
    private Integer clueBonusAwarded;

    /**线索奖金发放时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "线索奖金发放时间")
    @TableField("clue_bonus_awarded_time")
    private Date clueBonusAwardedTime;

    /**是否确认有效：0-否，1-是（默认0）*/
    @ApiModelProperty(value = "是否确认有效：0-否，1-是")
    @TableField("confirm_valid")
    private Integer confirmValid;

    /**确认有效时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "确认有效时间")
    @TableField("confirm_valid_time")
    private Date confirmValidTime;
    
    /**
     * 剩余天数（计算字段，不存数据库）
     */
    @TableField(exist = false)
    private Long syDay;
    
    /**
     * 跟进天数（计算字段，不存数据库）：当前时间距离最近跟进时间的天数
     */
    @TableField(exist = false)
    private Long followupDays;
}

