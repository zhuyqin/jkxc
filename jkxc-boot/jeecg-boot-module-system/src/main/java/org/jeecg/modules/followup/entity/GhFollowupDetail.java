package org.jeecg.modules.followup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 跟进记录
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Data
@TableName("gh_followup_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="gh_followup_detail对象", description="跟进记录")
public class GhFollowupDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
    
    /**商机ID*/
    @Excel(name = "商机ID", width = 15)
    @ApiModelProperty(value = "商机ID")
    private String opportId;
    
    /**跟进内容*/
    @Excel(name = "跟进内容", width = 15)
    @ApiModelProperty(value = "跟进内容")
    private String followupContent;
    
    /**跟进方式*/
    @Excel(name = "跟进方式", width = 15)
    @ApiModelProperty(value = "跟进方式")
    private String followupWay;
    
    /**跟进人员*/
    @Excel(name = "跟进人员", width = 15)
    @ApiModelProperty(value = "跟进人员")
    private String followupPerson;
    
    /**下次跟进时间*/
    @Excel(name = "下次跟进时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "下次跟进时间")
    private Date followupNextTime;
    
    /**创建日期*/
    @Excel(name = "创建日期", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createDate;
    
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
}

