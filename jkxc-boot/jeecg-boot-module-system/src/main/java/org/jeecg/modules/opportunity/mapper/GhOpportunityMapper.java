package org.jeecg.modules.opportunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.opportunity.entity.GhOpportunity;

import java.util.Date;
import java.util.Map;

/**
 * @Description: 商机管理Mapper
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOpportunityMapper extends BaseMapper<GhOpportunity> {

    @Select("SELECT COUNT(1) AS cnt, COALESCE(SUM(clue_bonus), 0) AS amt FROM gh_opportunity "
            + "WHERE del_flag = 0 AND clue_bonus_awarded = 1 "
            + "AND clue_bonus_awarded_time >= #{monthStart} AND clue_bonus_awarded_time <= #{monthEnd}")
    Map<String, Object> selectClueBonusMonthStats(@Param("monthStart") Date monthStart, @Param("monthEnd") Date monthEnd);
}

