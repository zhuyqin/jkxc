package org.jeecg.modules.order.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单收费记录表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhOrderPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 收费时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="收费时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date paymentTime;
    
    /**
     * 收费金额
     */
    @Excel(name="收费金额",width=15)
    private BigDecimal amount;
    
    /**
     * 收款方式
     */
    @Excel(name="收款方式",width=15, dicCode="payment_method")
    private String paymentMethod;
    
    /**
     * 收款账号
     */
    @Excel(name="收款账号",width=20)
    private String accountNumber;
    
    /**
     * 状态：0-待确认，1-已确认
     */
    @Excel(name="状态",width=10, dicCode="yn")
    private String status;
    
    /**
     * 备注
     */
    @Excel(name="备注",width=40)
    private String remarks;
    
    /**
     * 凭证附件（JSON数组）
     */
    private String imageVoucher;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
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
     * 删除标志：0-正常，1-已删除
     */
    private Integer delFlag;
    
    /**
     * 合同金额（从订单获取，不持久化）
     */
    private transient BigDecimal contractAmount;
    
    /**
     * 尾款金额（从订单获取，不持久化）
     */
    private transient BigDecimal finalPaymentAmount;
    
    /**
     * 服务开始时间（从订单创建时间计算，不持久化）
     */
    private transient Date serviceStartTime;

    /**
     * 服务开始时间原始字符串（用于保持原始格式，如月份格式YYYY-MM，不持久化）
     */
    private transient String serviceStartTimeStr;

    /**
     * 服务到期时间（不持久化）
     */
    private transient Date serviceEndTime;

    /**
     * 服务到期时间原始字符串（用于保持原始格式，如月份格式YYYY-MM，不持久化）
     */
    private transient String serviceEndTimeStr;
    
    /**
     * 合计月份（不持久化）
     */
    private transient Integer totalMonths;
    
    /**
     * 收款人（从银行账户获取，用于前端展示，不持久化）
     */
    private transient String payeePerson;
    
    /**
     * 网点名称（从银行账户获取，用于前端展示，不持久化）
     */
    private transient String accountNotes;
    
    /**
     * 收款账户（卡号，从银行账户获取，用于前端展示，不持久化）
     */
    private transient String collectionAccount;
}

