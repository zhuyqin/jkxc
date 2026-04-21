package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 团队管理表
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-03
 */
@Data
@TableName("sys_team")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysTeam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 团队名称
     */
    @Excel(name = "团队名称", width = 20)
    private String teamName;

    /**
     * 团队编码
     */
    @Excel(name = "团队编码", width = 15)
    private String teamCode;

    /**
     * 团队负责人ID（关联sys_user.id）
     */
    @Excel(name = "团队负责人", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String teamLeader;

    /**
     * 团队负责人姓名
     */
    @Excel(name = "团队负责人姓名", width = 15)
    private String teamLeaderName;

    /**
     * 团队描述
     */
    @Excel(name = "团队描述", width = 30)
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Excel(name = "状态", width = 10, dicCode = "sys_status")
    @Dict(dicCode = "sys_status")
    private Integer status;

    /**
     * 排序号
     */
    @Excel(name = "排序号", width = 10)
    private Integer sortOrder;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 删除标志：0-正常，1-已删除
     */
    @TableLogic
    private Integer delFlag;
}

