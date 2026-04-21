package org.jeecg.modules.customer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.customer.entity.GhCustomer;
import org.jeecg.modules.customer.mapper.GhCustomerMapper;
import org.jeecg.modules.customer.service.ICustomerNameSyncService;
import org.jeecg.modules.customer.service.IGhCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 客户表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Slf4j
@Service
public class GhCustomerServiceImpl extends ServiceImpl<GhCustomerMapper, GhCustomer> implements IGhCustomerService {

    @Autowired(required = false)
    private ICustomerNameSyncService customerNameSyncService;
    
    /**
     * 重写updateById方法，在更新客户时同步更新关联表的公司名称
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(GhCustomer entity) {
        if (entity == null || oConvertUtils.isEmpty(entity.getId())) {
            return false;
        }
        
        // 获取更新前的客户信息
        GhCustomer oldCustomer = this.getById(entity.getId());
        
        // 执行客户信息更新
        boolean result = super.updateById(entity);
        
        // 如果更新成功，且客户名称发生变化，同步更新关联表
        if (result && oldCustomer != null && 
            oConvertUtils.isNotEmpty(entity.getCorporateName()) &&
            !entity.getCorporateName().equals(oldCustomer.getCorporateName())) {
            
            log.info("检测到客户名称变化：customerId={}, oldName={}, newName={}", 
                    entity.getId(), oldCustomer.getCorporateName(), entity.getCorporateName());
            
            try {
                if (customerNameSyncService != null) {
                    ICustomerNameSyncService.SyncResult syncResult = 
                        customerNameSyncService.syncCustomerNameToRelatedTables(
                            entity.getId(), 
                            entity.getCorporateName()
                        );
                    
                    if (syncResult.isSuccess()) {
                        log.info("客户名称同步成功：{}", syncResult);
                    } else {
                        log.error("客户名称同步失败：{}", syncResult.getMessage());
                        // 注意：这里不抛出异常，避免影响客户信息的更新
                        // 如果需要强制同步成功，可以抛出异常回滚事务
                    }
                } else {
                    log.warn("CustomerNameSyncService未注入，跳过客户名称同步");
                }
            } catch (Exception e) {
                log.error("客户名称同步过程中发生异常", e);
                // 注意：这里捕获异常，避免影响客户信息的更新
                // 如果需要强制同步成功，可以重新抛出异常
                // throw e;
            }
        }
        
        return result;
    }
}

