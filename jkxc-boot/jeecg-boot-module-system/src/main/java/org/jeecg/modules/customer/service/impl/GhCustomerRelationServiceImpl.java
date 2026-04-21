package org.jeecg.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.customer.entity.GhCustomerRelation;
import org.jeecg.modules.customer.mapper.GhCustomerRelationMapper;
import org.jeecg.modules.customer.service.IGhCustomerRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户关联表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Slf4j
@Service
public class GhCustomerRelationServiceImpl extends ServiceImpl<GhCustomerRelationMapper, GhCustomerRelation> implements IGhCustomerRelationService {

    @Override
    public List<String> getRelatedCustomerIds(String customerId) {
        if (oConvertUtils.isEmpty(customerId)) {
            return new ArrayList<>();
        }
        
        QueryWrapper<GhCustomerRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("customer_id", customerId);
        queryWrapper.eq("del_flag", 0);
        List<GhCustomerRelation> relations = this.list(queryWrapper);
        
        return relations.stream()
                .map(GhCustomerRelation::getRelatedCustomerId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRelation(String customerId, String relatedCustomerId) {
        if (oConvertUtils.isEmpty(customerId) || oConvertUtils.isEmpty(relatedCustomerId)) {
            return false;
        }
        
        // 检查是否已存在关联关系
        if (relationExists(customerId, relatedCustomerId)) {
            log.info("关联关系已存在：{} -> {}", customerId, relatedCustomerId);
            return true;
        }
        
        // 创建正向关联（A -> B）
        GhCustomerRelation relation1 = new GhCustomerRelation();
        relation1.setCustomerId(customerId);
        relation1.setRelatedCustomerId(relatedCustomerId);
        relation1.setCreateTime(new Date());
        relation1.setDelFlag(0);
        this.save(relation1);
        
        // 创建反向关联（B -> A），避免重复
        if (!customerId.equals(relatedCustomerId)) {
            if (!relationExists(relatedCustomerId, customerId)) {
                GhCustomerRelation relation2 = new GhCustomerRelation();
                relation2.setCustomerId(relatedCustomerId);
                relation2.setRelatedCustomerId(customerId);
                relation2.setCreateTime(new Date());
                relation2.setDelFlag(0);
                this.save(relation2);
            }
        }
        
        log.info("建立双向关联：{} <-> {}", customerId, relatedCustomerId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRelation(String customerId, String relatedCustomerId) {
        if (oConvertUtils.isEmpty(customerId) || oConvertUtils.isEmpty(relatedCustomerId)) {
            return false;
        }
        
        // 删除正向关联（A -> B）
        QueryWrapper<GhCustomerRelation> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("customer_id", customerId);
        wrapper1.eq("related_customer_id", relatedCustomerId);
        wrapper1.eq("del_flag", 0);
        List<GhCustomerRelation> relations1 = this.list(wrapper1);
        for (GhCustomerRelation relation : relations1) {
            relation.setDelFlag(1);
            relation.setUpdateTime(new Date());
            this.updateById(relation);
        }
        
        // 删除反向关联（B -> A）
        QueryWrapper<GhCustomerRelation> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("customer_id", relatedCustomerId);
        wrapper2.eq("related_customer_id", customerId);
        wrapper2.eq("del_flag", 0);
        List<GhCustomerRelation> relations2 = this.list(wrapper2);
        for (GhCustomerRelation relation : relations2) {
            relation.setDelFlag(1);
            relation.setUpdateTime(new Date());
            this.updateById(relation);
        }
        
        log.info("删除双向关联：{} <-> {}", customerId, relatedCustomerId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddRelations(String customerId, List<String> relatedCustomerIds) {
        if (oConvertUtils.isEmpty(customerId) || relatedCustomerIds == null || relatedCustomerIds.isEmpty()) {
            return false;
        }
        
        for (String relatedCustomerId : relatedCustomerIds) {
            if (oConvertUtils.isNotEmpty(relatedCustomerId)) {
                addRelation(customerId, relatedCustomerId);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllRelations(String customerId) {
        if (oConvertUtils.isEmpty(customerId)) {
            return false;
        }
        
        // 删除所有正向关联
        QueryWrapper<GhCustomerRelation> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("customer_id", customerId);
        wrapper1.eq("del_flag", 0);
        List<GhCustomerRelation> relations1 = this.list(wrapper1);
        for (GhCustomerRelation relation : relations1) {
            relation.setDelFlag(1);
            relation.setUpdateTime(new Date());
            this.updateById(relation);
            
            // 同时删除反向关联
            QueryWrapper<GhCustomerRelation> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("customer_id", relation.getRelatedCustomerId());
            wrapper2.eq("related_customer_id", customerId);
            wrapper2.eq("del_flag", 0);
            List<GhCustomerRelation> relations2 = this.list(wrapper2);
            for (GhCustomerRelation reverseRelation : relations2) {
                reverseRelation.setDelFlag(1);
                reverseRelation.setUpdateTime(new Date());
                this.updateById(reverseRelation);
            }
        }
        
        log.info("删除客户 {} 的所有关联关系", customerId);
        return true;
    }
    
    /**
     * 检查关联关系是否已存在
     */
    private boolean relationExists(String customerId, String relatedCustomerId) {
        QueryWrapper<GhCustomerRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("customer_id", customerId);
        queryWrapper.eq("related_customer_id", relatedCustomerId);
        queryWrapper.eq("del_flag", 0);
        return this.count(queryWrapper) > 0;
    }
}

