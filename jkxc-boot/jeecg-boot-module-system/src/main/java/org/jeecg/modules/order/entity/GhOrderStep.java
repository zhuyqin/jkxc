package org.jeecg.modules.order.entity;

import java.io.Serializable;
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
 * 订单流程步骤表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhOrderStep implements Serializable {

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
     * 步骤类型：0-订单创建，1-经理审核，2-出纳审核，3-任务分配，4-任务处理，5-任务完成，6-订单完成，7-订单驳回
     */
    @Excel(name="步骤类型",width=15)
    private String stepType;
    
    /**
     * 步骤顺序（从1开始）
     */
    @Excel(name="步骤顺序",width=10)
    private Integer stepOrder;
    
    /**
     * 操作人ID
     */
    private String operatorId;
    
    /**
     * 操作人姓名
     */
    @Excel(name="操作人",width=15)
    private String operatorName;
    
    /**
     * 状态：0-待处理，1-已完成，2-处理中，3-已驳回，4-已取消
     */
    @Excel(name="状态",width=10, dicCode="step_status")
    private String status;
    
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
}

