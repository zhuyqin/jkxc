package org.jeecg.modules.order.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.order.entity.GhOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 订单主表
 * @Author: jeecg-boot
 * @Date: 2025-01-01
 * @Version: V1.0
 */
public interface GhOrderMapper extends BaseMapper<GhOrder> {

    /**
     * 根据订单ID查询订单及其步骤信息
     * @param orderId 订单ID
     * @return 订单信息
     */
    GhOrder getOrderWithSteps(@Param("orderId") String orderId);
    
    /**
     * 统计指定时间范围内的订单数量和金额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Map包含count和amount
     */
    Map<String, Object> getOrderStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 首页 KPI：本年范围内一次扫描，输出今日/本月/本年的订单数与金额
     */
    Map<String, Object> getDashboardOrderKpis(@Param("todayStart") Date todayStart,
                                               @Param("todayEnd") Date todayEnd,
                                               @Param("monthStart") Date monthStart,
                                               @Param("monthEnd") Date monthEnd,
                                               @Param("yearStart") Date yearStart,
                                               @Param("yearEnd") Date yearEnd);
    
    /**
     * 获取月度订单趋势（全年12个月）
     * @param year 年份
     * @return 月度统计列表
     */
    List<Map<String, Object>> getMonthlyTrend(@Param("year") int year);
    
    /**
     * 获取业务类型分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 业务类型统计列表
     */
    List<Map<String, Object>> getBusinessTypeDistribution(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 获取业务员排名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 业务员排名列表
     */
    List<Map<String, Object>> getSalesmanRank(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("limit") int limit);
    
    /**
     * 获取不同业务员数量
     * @return 业务员数量
     */
    int getDistinctSalesmanCount();
    
    /**
     * 获取高价值客户Top N
     * @param limit 限制数量
     * @return 客户价值列表
     */
    List<Map<String, Object>> getTopValueCustomers(@Param("limit") int limit);

    /**
     * 最近订单摘要（仅列表展示字段）
     */
    List<Map<String, Object>> getRecentOrdersSummary(@Param("limit") int limit);

    /**
     * 某业务员去重客户数
     */
    int countDistinctCustomersBySalesman(@Param("salesman") String salesman);
}

