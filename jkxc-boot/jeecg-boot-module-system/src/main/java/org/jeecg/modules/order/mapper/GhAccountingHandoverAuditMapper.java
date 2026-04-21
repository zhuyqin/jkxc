package org.jeecg.modules.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhAccountingHandoverAudit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 代账交接审核记录表
 * @Author: jeecg-boot
 * @Date: 2025-01-02
 * @Version: V1.0
 */
public interface GhAccountingHandoverAuditMapper extends BaseMapper<GhAccountingHandoverAudit> {

    /**
     * 根据代账交接ID查询审核记录列表（按步骤顺序排序）
     * @param handoverId 代账交接ID
     * @return 审核记录列表
     */
    List<GhAccountingHandoverAudit> getAuditsByHandoverId(@Param("handoverId") String handoverId);
    
    /**
     * 根据代账交接ID和角色ID查询待审核的记录
     * @param handoverId 代账交接ID
     * @param roleId 角色ID
     * @param auditStatus 审核状态（可选，默认pending）
     * @return 审核记录
     */
    GhAccountingHandoverAudit getPendingAuditByHandoverIdAndRole(@Param("handoverId") String handoverId, @Param("roleId") String roleId, @Param("auditStatus") String auditStatus);
}

