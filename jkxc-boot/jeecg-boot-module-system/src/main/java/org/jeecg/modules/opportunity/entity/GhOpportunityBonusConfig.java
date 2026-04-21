package org.jeecg.modules.opportunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 线索奖金配置（线索名称/分类ID -> 奖金金额）
 */
@Data
@TableName("gh_opportunity_bonus_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "gh_opportunity_bonus_config对象", description = "线索奖金配置")
public class GhOpportunityBonusConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;

    /**线索名称对应分类（存 sys_category.id）*/
    @Excel(name = "线索名称ID", width = 20, orderNum = "1")
    @ApiModelProperty(value = "线索名称分类ID（sys_category.id导出列名称）")
    @TableField("opportunity_name")
    private String opportunityName;

    /**奖金金额*/
    @Excel(name = "奖金金额", width = 20, orderNum = "2")
    @ApiModelProperty(value = "奖金金额")
    @TableField("bonus_money")
    private BigDecimal bonusMoney;

    /**创建人*/
    @ApiModelProperty(value = "创建人")
    @TableField("create_by")
    private String createBy;

    /**更新时间*/
    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**更新人*/
    @ApiModelProperty(value = "更新人")
    @TableField("update_by")
    private String updateBy;

    /**更新时间*/
    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**删除标志*/
    @ApiModelProperty(value = "删除标志")
    @TableField("del_flag")
    private Integer delFlag = 0;
}

