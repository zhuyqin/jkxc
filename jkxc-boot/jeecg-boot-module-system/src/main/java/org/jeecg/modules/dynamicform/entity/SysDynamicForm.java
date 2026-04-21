package org.jeecg.modules.dynamicform.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 动态表单主表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Data
@TableName("sys_dynamic_form")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="sys_dynamic_form对象", description="动态表单主表")
public class SysDynamicForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
    
    /**表单名称*/
    @Excel(name = "表单名称", width = 30)
    @ApiModelProperty(value = "表单名称")
    private String formName;
    
    /**表单编码*/
    @Excel(name = "表单编码", width = 30)
    @ApiModelProperty(value = "表单编码")
    private String formCode;
    
    /**表单描述*/
    @Excel(name = "表单描述", width = 50)
    @ApiModelProperty(value = "表单描述")
    private String description;
    
    /**状态：0-禁用，1-启用*/
    @Excel(name = "状态", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "状态")
    private Integer status;
    
    /**当前版本号*/
    @ApiModelProperty(value = "当前版本号")
    private Integer currentVersion;
    
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
    
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private java.util.Date updateTime;
    
    /**删除标志*/
    @TableLogic
    @ApiModelProperty(value = "删除标志")
    private Integer delFlag;
}

