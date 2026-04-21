package org.jeecg.modules.customer.entity;

import java.io.Serializable;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecg.common.aspect.annotation.Dict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 客户表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("gh_customer")
public class GhCustomer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 公司名称
     */
    @Excel(name="公司名称",width=30)
    private String corporateName;
    
    /**
     * 联系人
     */
    @Excel(name="联系人",width=15)
    private String contacts;
    
    /**
     * 联系电话
     */
    @Excel(name="联系电话",width=20)
    private String contactInformation;
    
    /**
     * 所属区域
     */
    @Excel(name="所属区域",width=20)
    private String region;
    
    /**
     * 详细地址
     */
    @Excel(name="详细地址",width=40)
    private String address;
    
    /**
     * 法人姓名
     */
    @Excel(name="法人姓名",width=15)
    private String legalPersonName;
    
    /**
     * 法人电话
     */
    @Excel(name="法人电话",width=20)
    private String legalPersonPhone;
    
    /**
     * 税号
     */
    @Excel(name="税号",width=20)
    private String dutyParagraph;
    
    /**
     * 注册地址
     */
    @Excel(name="注册地址",width=40)
    private String registeredAddress;
    
    /**
     * 实际地址
     */
    @Excel(name="实际地址",width=40)
    private String actualAddress;
    
    /**
     * 注册资金
     */
    @Excel(name="注册资金",width=15)
    private String registeredCapital;
    
    /**
     * 经营范围
     */
    @Excel(name="经营范围",width=50)
    private String businessScope;
    
    /**
     * 企业等级
     */
    @Excel(name="企业等级",width=15, dicCode="enterprise_level")
    @Dict(dicCode="enterprise_level")
    private String enterpriseLevel;
    
    /**
     * 消费金额（公司全部订单金额总和）
     */
    @Excel(name="消费金额",width=15)
    private java.math.BigDecimal totalSpending;
    
    /**
     * 业务标签（公司所有业务类型，逗号分隔）
     */
    @Excel(name="业务标签",width=50)
    private String businessTag;
    
    /**
     * 关联企业ID
     */
    @Excel(name="关联企业ID",width=32)
    private String relatedCompanyId;
    
    /**
     * 关联企业名称
     */
    @Excel(name="关联企业名称",width=200)
    private String relatedCompanyName;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    @Excel(name="状态",width=10, dicCode="status")
    private String status;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="创建时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
    /**
     * 删除标志（0-未删除，1-已删除）
     */
    private Integer delFlag;
    
    /**
     * 订单数量（计算字段，不存储在数据库，用于显示复购信息）
     */
    @TableField(exist = false)
    private Integer orderCount;
}

