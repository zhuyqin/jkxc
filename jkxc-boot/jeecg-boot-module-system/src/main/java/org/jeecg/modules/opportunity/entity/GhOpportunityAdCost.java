package org.jeecg.modules.opportunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("gh_opportunity_ad_cost")
@ApiModel(value = "gh_opportunity_ad_cost对象", description = "线索类别广告费用")
public class GhOpportunityAdCost implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty(value = "统计年")
    private Integer statYear;

    @ApiModelProperty(value = "统计月：1-12，0表示全年")
    private Integer statMonth;

    @ApiModelProperty(value = "线索名称键值（优先存sys_category.id）")
    private String opportunityNameKey;

    @ApiModelProperty(value = "线索名称展示")
    private String opportunityName;

    @ApiModelProperty(value = "广告费用")
    private BigDecimal adCost;

    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private Integer delFlag;
}

