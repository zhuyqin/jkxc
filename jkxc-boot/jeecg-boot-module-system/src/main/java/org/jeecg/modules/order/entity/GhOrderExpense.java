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
 * 订单支出记录表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhOrderExpense implements Serializable {

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
     * 支出时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="支出时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date expenseTime;
    
    /**
     * 支出金额
     */
    @Excel(name="支出金额",width=15)
    private BigDecimal amount;
    
    /**
     * 支出类目
     */
    @Excel(name="支出类目",width=20)
    private String category;
    
    /**
     * 审核状态：0-待审核，1-已审核
     */
    @Excel(name="审核状态",width=15)
    private String auditStatus;
    
    /**
     * 支付账户
     */
    @Excel(name="支付账户",width=20)
    private String paymentAccount;
    
    /**
     * 备注
     */
    @Excel(name="备注",width=40)
    private String remarks;
    
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
}

