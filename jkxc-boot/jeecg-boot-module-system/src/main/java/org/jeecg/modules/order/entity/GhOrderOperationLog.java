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
 * 订单操作记录表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhOrderOperationLog implements Serializable {

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
     * 操作类型：create-创建,update-修改,approve-审批,reject-驳回,delete-删除等
     */
    @Excel(name="操作类型",width=20)
    private String operationType;
    
    /**
     * 操作描述
     */
    @Excel(name="操作描述",width=40)
    private String operationDesc;
    
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
     * 操作前数据（JSON格式）
     */
    private String beforeData;
    
    /**
     * 操作后数据（JSON格式）
     */
    private String afterData;
    
    /**
     * 变更字段（多个字段用逗号分隔）
     */
    private String changedFields;
    
    /**
     * 备注
     */
    @Excel(name="备注",width=40)
    private String remarks;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name="操作时间",width=20,format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /**
     * 创建人
     */
    private String createBy;
}

