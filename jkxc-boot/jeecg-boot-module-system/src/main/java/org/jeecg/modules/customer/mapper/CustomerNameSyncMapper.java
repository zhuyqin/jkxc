package org.jeecg.modules.customer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 客户名称同步Mapper
 * 用于处理复杂的JOIN UPDATE操作和数据一致性检查
 * 
 * @author jeecg-boot
 * @since 2026-03-01
 */
@Mapper
public interface CustomerNameSyncMapper {
    
    /**
     * 更新商机表的公司名称（通过客户关联表）
     * 
     * @param customerId 客户ID
     * @param newCorporateName 新的公司名称
     * @return 更新的记录数
     */
    int updateOpportunityNameByCustomerId(@Param("customerId") String customerId, 
                                          @Param("newCorporateName") String newCorporateName);
    
    /**
     * 检查订单表数据一致性
     * 
     * @return 不一致的记录列表
     */
    List<Map<String, Object>> checkOrderConsistency();
    
    /**
     * 检查代账合同表数据一致性
     * 
     * @return 不一致的记录列表
     */
    List<Map<String, Object>> checkContractConsistency();
    
    /**
     * 检查地址中心表数据一致性
     * 
     * @return 不一致的记录列表
     */
    List<Map<String, Object>> checkAddressConsistency();
    
    /**
     * 检查银行日记账表数据一致性
     * 
     * @return 不一致的记录列表
     */
    List<Map<String, Object>> checkBankDiaryConsistency();
    
    /**
     * 统计所有表的不一致记录数
     * 
     * @return 统计结果
     */
    List<Map<String, Object>> countInconsistentRecords();
}
