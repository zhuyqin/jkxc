package org.jeecg.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.address.mapper.GhAddressCenterMapper;
import org.jeecg.modules.bankdiary.mapper.GhBankDiaryMapper;
import org.jeecg.modules.customer.mapper.CustomerNameSyncMapper;
import org.jeecg.modules.customer.service.ICustomerNameSyncService;
import org.jeecg.modules.opportunity.mapper.GhOpportunityMapper;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.mapper.*;
import org.jeecg.modules.reimbursement.mapper.GhReimbursementMapper;
import org.jeecg.modules.renew.mapper.GhAddressRenewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 客户名称同步服务实现类
 * 
 * @author jeecg-boot
 * @since 2026-03-01
 */
@Slf4j
@Service
public class CustomerNameSyncServiceImpl implements ICustomerNameSyncService {
    
    @Autowired(required = false)
    private GhOrderMapper ghOrderMapper;
    
    @Autowired(required = false)
    private GhAccountingContractMapper ghAccountingContractMapper;
    
    @Autowired(required = false)
    private GhAccountingHandoverMapper ghAccountingHandoverMapper;
    
    @Autowired(required = false)
    private GhBusinessTaskMapper ghBusinessTaskMapper;
    
    @Autowired(required = false)
    private GhAddressCenterMapper ghAddressCenterMapper;
    
    @Autowired(required = false)
    private GhAddressRenewMapper ghAddressRenewMapper;
    
    @Autowired(required = false)
    private GhBankDiaryMapper ghBankDiaryMapper;
    
    @Autowired(required = false)
    private GhReimbursementMapper ghReimbursementMapper;
    
    @Autowired(required = false)
    private GhOpportunityMapper ghOpportunityMapper;
    
    @Autowired(required = false)
    private CustomerNameSyncMapper customerNameSyncMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResult syncCustomerNameToRelatedTables(String customerId, String newCorporateName) {
        SyncResult result = new SyncResult();
        
        try {
            if (oConvertUtils.isEmpty(customerId)) {
                result.setSuccess(false);
                result.setMessage("客户ID不能为空");
                return result;
            }
            
            if (oConvertUtils.isEmpty(newCorporateName)) {
                result.setSuccess(false);
                result.setMessage("新公司名称不能为空");
                return result;
            }
            
            log.info("开始同步客户名称：customerId={}, newName={}", customerId, newCorporateName);
            
            // 1. 更新订单表
            int orderCount = updateOrderTable(customerId, newCorporateName);
            result.setOrderCount(orderCount);
            log.info("更新订单表：{} 条记录", orderCount);
            
            // 2. 更新代账合同表（通过订单关联）
            int contractCount = updateContractTable(customerId, newCorporateName);
            result.setContractCount(contractCount);
            log.info("更新代账合同表：{} 条记录", contractCount);
            
            // 3. 更新代账交接表
            int handoverCount = updateHandoverTable(customerId, newCorporateName);
            result.setHandoverCount(handoverCount);
            log.info("更新代账交接表：{} 条记录", handoverCount);
            
            // 4. 更新业务任务表
            int taskCount = updateBusinessTaskTable(customerId, newCorporateName);
            result.setTaskCount(taskCount);
            log.info("更新业务任务表：{} 条记录", taskCount);
            
            // 5. 更新地址中心表
            int addressCount = updateAddressCenterTable(customerId, newCorporateName);
            result.setAddressCount(addressCount);
            log.info("更新地址中心表：{} 条记录", addressCount);
            
            // 6. 更新地址续费表
            int renewCount = updateAddressRenewTable(customerId, newCorporateName);
            result.setRenewCount(renewCount);
            log.info("更新地址续费表：{} 条记录", renewCount);
            
            // 7. 更新银行日记账表
            int diaryCount = updateBankDiaryTable(customerId, newCorporateName);
            result.setDiaryCount(diaryCount);
            log.info("更新银行日记账表：{} 条记录", diaryCount);
            
            // 8. 更新报销表
            int reimbursementCount = updateReimbursementTable(customerId, newCorporateName);
            result.setReimbursementCount(reimbursementCount);
            log.info("更新报销表：{} 条记录", reimbursementCount);
            
            // 9. 更新商机表
            int opportunityCount = updateOpportunityTable(customerId, newCorporateName);
            result.setOpportunityCount(opportunityCount);
            log.info("更新商机表：{} 条记录", opportunityCount);
            
            // 计算总数
            result.calculateTotal();
            
            result.setSuccess(true);
            result.setMessage(String.format("客户名称同步成功，共更新 %d 条记录", result.getTotalCount()));
            
            log.info("客户名称同步完成：{}", result);
            
        } catch (Exception e) {
            log.error("客户名称同步失败：customerId={}, newName={}", customerId, newCorporateName, e);
            result.setSuccess(false);
            result.setMessage("客户名称同步失败：" + e.getMessage());
            throw new RuntimeException("客户名称同步失败", e);
        }
        
        return result;
    }
    
    /**
     * 更新订单表
     */
    private int updateOrderTable(String customerId, String newCorporateName) {
        if (ghOrderMapper == null) {
            log.warn("GhOrderMapper未注入，跳过订单表更新");
            return 0;
        }
        
        try {
            UpdateWrapper<GhOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("company_id", customerId);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghOrderMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新订单表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新代账合同表（通过订单关联）
     */
    private int updateContractTable(String customerId, String newCorporateName) {
        if (ghAccountingContractMapper == null || ghOrderMapper == null) {
            log.warn("GhAccountingContractMapper或GhOrderMapper未注入，跳过代账合同表更新");
            return 0;
        }
        
        try {
            // 先查询该客户的所有订单ID
            QueryWrapper<GhOrder> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("company_id", customerId);
            orderWrapper.eq("del_flag", 0);
            orderWrapper.select("id");
            List<GhOrder> orders = ghOrderMapper.selectList(orderWrapper);
            
            if (orders == null || orders.isEmpty()) {
                return 0;
            }
            
            // 提取订单ID列表
            List<String> orderIds = new java.util.ArrayList<>();
            for (GhOrder order : orders) {
                orderIds.add(order.getId());
            }
            
            // 更新代账合同表
            UpdateWrapper<org.jeecg.modules.order.entity.GhAccountingContract> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("order_id", orderIds);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghAccountingContractMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新代账合同表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新代账交接表
     */
    private int updateHandoverTable(String customerId, String newCorporateName) {
        if (ghAccountingHandoverMapper == null || ghOrderMapper == null) {
            log.warn("GhAccountingHandoverMapper或GhOrderMapper未注入，跳过代账交接表更新");
            return 0;
        }
        
        try {
            // 先查询该客户的所有订单ID
            QueryWrapper<GhOrder> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("company_id", customerId);
            orderWrapper.eq("del_flag", 0);
            orderWrapper.select("id");
            List<GhOrder> orders = ghOrderMapper.selectList(orderWrapper);
            
            if (orders == null || orders.isEmpty()) {
                return 0;
            }
            
            // 提取订单ID列表
            List<String> orderIds = new java.util.ArrayList<>();
            for (GhOrder order : orders) {
                orderIds.add(order.getId());
            }
            
            // 更新代账交接表
            UpdateWrapper<org.jeecg.modules.order.entity.GhAccountingHandover> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("order_id", orderIds);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghAccountingHandoverMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新代账交接表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新业务任务表
     */
    private int updateBusinessTaskTable(String customerId, String newCorporateName) {
        if (ghBusinessTaskMapper == null || ghOrderMapper == null) {
            log.warn("GhBusinessTaskMapper或GhOrderMapper未注入，跳过业务任务表更新");
            return 0;
        }
        
        try {
            // 先查询该客户的所有订单ID
            QueryWrapper<GhOrder> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("company_id", customerId);
            orderWrapper.eq("del_flag", 0);
            orderWrapper.select("id");
            List<GhOrder> orders = ghOrderMapper.selectList(orderWrapper);
            
            if (orders == null || orders.isEmpty()) {
                return 0;
            }
            
            // 提取订单ID列表
            List<String> orderIds = new java.util.ArrayList<>();
            for (GhOrder order : orders) {
                orderIds.add(order.getId());
            }
            
            // 更新业务任务表
            UpdateWrapper<org.jeecg.modules.order.entity.GhBusinessTask> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("order_id", orderIds);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghBusinessTaskMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新业务任务表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新地址中心表
     */
    private int updateAddressCenterTable(String customerId, String newCorporateName) {
        if (ghAddressCenterMapper == null) {
            log.warn("GhAddressCenterMapper未注入，跳过地址中心表更新");
            return 0;
        }
        
        try {
            UpdateWrapper<org.jeecg.modules.address.entity.GhAddressCenter> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("data_id", customerId);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghAddressCenterMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新地址中心表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新地址续费表
     */
    private int updateAddressRenewTable(String customerId, String newCorporateName) {
        if (ghAddressRenewMapper == null) {
            log.warn("GhAddressRenewMapper未注入，跳过地址续费表更新");
            return 0;
        }
        
        try {
            UpdateWrapper<org.jeecg.modules.renew.entity.GhAddressRenew> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("company_id", customerId);
            updateWrapper.set("company_name", newCorporateName);
            // 注意：地址续费表可能没有update_time和update_by字段
            
            return ghAddressRenewMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新地址续费表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新银行日记账表
     */
    private int updateBankDiaryTable(String customerId, String newCorporateName) {
        if (ghBankDiaryMapper == null) {
            log.warn("GhBankDiaryMapper未注入，跳过银行日记账表更新");
            return 0;
        }
        
        try {
            UpdateWrapper<org.jeecg.modules.bankdiary.entity.GhBankDiary> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("company_id", customerId);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghBankDiaryMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新银行日记账表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新报销表
     */
    private int updateReimbursementTable(String customerId, String newCorporateName) {
        if (ghReimbursementMapper == null) {
            log.warn("GhReimbursementMapper未注入，跳过报销表更新");
            return 0;
        }
        
        try {
            UpdateWrapper<org.jeecg.modules.reimbursement.entity.GhReimbursement> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("company_id", customerId);
            updateWrapper.eq("del_flag", 0);
            updateWrapper.set("company_name", newCorporateName);
            updateWrapper.set("update_time", new Date());
            updateWrapper.set("update_by", "customer_name_sync");
            
            return ghReimbursementMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新报销表失败", e);
            throw e;
        }
    }
    
    /**
     * 更新商机表（通过客户关联表）
     */
    private int updateOpportunityTable(String customerId, String newCorporateName) {
        if (customerNameSyncMapper == null) {
            log.warn("CustomerNameSyncMapper未注入，跳过商机表更新");
            return 0;
        }
        
        try {
            return customerNameSyncMapper.updateOpportunityNameByCustomerId(customerId, newCorporateName);
        } catch (Exception e) {
            log.error("更新商机表失败", e);
            throw e;
        }
    }
}
