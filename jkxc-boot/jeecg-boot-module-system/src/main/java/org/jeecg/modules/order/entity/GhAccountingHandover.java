package org.jeecg.modules.order.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 代账交接表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-02
 */
@Data
@TableName("gh_accounting_handover")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GhAccountingHandover implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 订单ID
     */
    @Excel(name="订单ID",width=20)
    private String orderId;
    
    /**
     * 订单编号（冗余字段）
     */
    @Excel(name="订单编号",width=20)
    private String orderNo;
    
    /**
     * 公司名称（冗余字段）
     */
    @Excel(name="公司名称",width=30)
    private String companyName;
    
    /**
     * 交接状态：pending-待审核,approved-审核通过,rejected-审核驳回,completed-已完成
     */
    @Excel(name="交接状态",width=20)
    private String handoverStatus;
    
    /**
     * 审核流程ID（关联sys_audit_process表）
     */
    private String auditProcessId;
    
    /**
     * 当前审核步骤顺序
     */
    private Integer currentAuditStep;
    
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
    
    /**
     * 删除标志：0-正常，1-已删除
     */
    private Integer delFlag;
    
    /**
     * 交接状态字典文本（不映射到数据库）
     */
    @TableField(exist = false)
    private String handoverStatus_dictText;
    
    /**
     * 审核记录列表（不映射到数据库）
     */
    @TableField(exist = false)
    private List<GhAccountingHandoverAudit> handoverAudits;
}

