package org.jeecg.modules.reimbursement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.reimbursement.entity.GhReimbursement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 报销审批
 * @Author: jeecg-boot
 * @Date:   2022-03-19
 * @Version: V1.0
 */
@Mapper
public interface GhReimbursementMapper extends BaseMapper<GhReimbursement> {

    List<Map<String, Object>> getbxList(Integer year);

    /**
     * 报销数据分析：汇总（按报销时间、数据权限、类目筛选）
     */
    Map<String, Object> selectAnalyticsSummary(@Param("applyPersonFilter") boolean applyPersonFilter,
                                               @Param("persons") List<String> persons,
                                               @Param("dateBegin") Date dateBegin,
                                               @Param("dateEnd") Date dateEnd,
                                               @Param("onlyCategoryId") String onlyCategoryId,
                                               @Param("excludeAddressId") String excludeAddressId,
                                               @Param("excludeSalaryId") String excludeSalaryId);

    List<Map<String, Object>> selectAnalyticsByCategory(@Param("applyPersonFilter") boolean applyPersonFilter,
                                                        @Param("persons") List<String> persons,
                                                        @Param("dateBegin") Date dateBegin,
                                                        @Param("dateEnd") Date dateEnd,
                                                        @Param("onlyCategoryId") String onlyCategoryId,
                                                        @Param("excludeAddressId") String excludeAddressId,
                                                        @Param("excludeSalaryId") String excludeSalaryId);

    List<Map<String, Object>> selectAnalyticsByMonth(@Param("applyPersonFilter") boolean applyPersonFilter,
                                                     @Param("persons") List<String> persons,
                                                     @Param("dateBegin") Date dateBegin,
                                                     @Param("dateEnd") Date dateEnd,
                                                     @Param("onlyCategoryId") String onlyCategoryId,
                                                     @Param("excludeAddressId") String excludeAddressId,
                                                     @Param("excludeSalaryId") String excludeSalaryId);

    List<Map<String, Object>> selectAnalyticsByPerson(@Param("applyPersonFilter") boolean applyPersonFilter,
                                                      @Param("persons") List<String> persons,
                                                      @Param("dateBegin") Date dateBegin,
                                                      @Param("dateEnd") Date dateEnd,
                                                      @Param("onlyCategoryId") String onlyCategoryId,
                                                      @Param("excludeAddressId") String excludeAddressId,
                                                      @Param("excludeSalaryId") String excludeSalaryId);

    /**
     * 按报销类目 × 月份（1–12）汇总金额；仅统计自然年 chartYear（与统计区间求交）
     */
    List<Map<String, Object>> selectAnalyticsByCategoryMonth(@Param("applyPersonFilter") boolean applyPersonFilter,
                                                             @Param("persons") List<String> persons,
                                                             @Param("dateBegin") Date dateBegin,
                                                             @Param("dateEnd") Date dateEnd,
                                                             @Param("onlyCategoryId") String onlyCategoryId,
                                                             @Param("excludeAddressId") String excludeAddressId,
                                                             @Param("excludeSalaryId") String excludeSalaryId,
                                                             @Param("chartYear") int chartYear);
}

