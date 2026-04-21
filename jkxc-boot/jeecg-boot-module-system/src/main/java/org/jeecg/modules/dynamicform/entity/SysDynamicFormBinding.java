package org.jeecg.modules.dynamicform.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 业务类型表单绑定表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
@Data
@TableName("sys_dynamic_form_binding")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="sys_dynamic_form_binding对象", description="业务类型表单绑定表")
public class SysDynamicFormBinding implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
    
    /**业务类型*/
    @Excel(name = "业务类型", width = 30)
    @ApiModelProperty(value = "业务类型")
    private String businessType;
    
    /**表单ID*/
    @ApiModelProperty(value = "表单ID")
    private String formId;
    
    /**版本ID*/
    @ApiModelProperty(value = "版本ID")
    private String versionId;
    
    /**版本号*/
    @ApiModelProperty(value = "版本号")
    private Integer version;
    
    /**状态：0-禁用，1-启用*/
    @Excel(name = "状态", width = 15, dicCode = "yn")
    @ApiModelProperty(value = "状态")
    private Integer status;
    
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
    
    /**业务类型名称（用于前端显示，不存储到数据库）*/
    @ApiModelProperty(value = "业务类型名称")
    private transient String businessTypeName;
}

