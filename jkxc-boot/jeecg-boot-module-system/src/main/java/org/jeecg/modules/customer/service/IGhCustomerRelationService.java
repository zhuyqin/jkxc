package org.jeecg.modules.customer.service;

import org.jeecg.modules.customer.entity.GhCustomerRelation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 客户关联表 服务类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
public interface IGhCustomerRelationService extends IService<GhCustomerRelation> {
    
    /**
     * 获取客户的所有关联企业ID列表
     * @param customerId 客户ID
     * @return 关联企业ID列表
     */
    List<String> getRelatedCustomerIds(String customerId);
    
    /**
     * 添加关联关系（双向）
     * @param customerId 客户ID
     * @param relatedCustomerId 关联客户ID
     * @return 是否成功
     */
    boolean addRelation(String customerId, String relatedCustomerId);
    
    /**
     * 删除关联关系（双向）
     * @param customerId 客户ID
     * @param relatedCustomerId 关联客户ID
     * @return 是否成功
     */
    boolean removeRelation(String customerId, String relatedCustomerId);
    
    /**
     * 批量添加关联关系（双向）
     * @param customerId 客户ID
     * @param relatedCustomerIds 关联客户ID列表
     * @return 是否成功
     */
    boolean batchAddRelations(String customerId, List<String> relatedCustomerIds);
    
    /**
     * 删除客户的所有关联关系
     * @param customerId 客户ID
     * @return 是否成功
     */
    boolean removeAllRelations(String customerId);
}

