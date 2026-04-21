package org.jeecg.modules.address.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 地址中心DTO（用于报销等功能）
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Data
@ApiModel(value="gh_address_center_dto对象", description="地址中心DTO")
public class GhAddressCenterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private String id;
    
    @ApiModelProperty(value = "公司名称")
    private String companyName;
    
    @ApiModelProperty(value = "公司资料id")
    private String dataId;
    
    @ApiModelProperty(value = "合同金额")
    private Integer contractAmount;
    
    @ApiModelProperty(value = "地址成本")
    private String addressCost;
    
    @ApiModelProperty(value = "报销类目")
    private String category;
    
    @ApiModelProperty(value = "报销账号")
    private String bxAccount;
    
    @ApiModelProperty(value = "报销银行")
    private String bxBank;
    
    @ApiModelProperty(value = "报销公司名称")
    private String bxCompanyName;
    
    @ApiModelProperty(value = "报销备注")
    private String bxRemarks;
    
    @ApiModelProperty(value = "总价")
    private Double totalPrice;
}

