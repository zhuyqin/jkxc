package org.jeecg.modules.reimbursement.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报销类目工具类
 * 用于获取报销类目的ID，支持配置和代码查询两种方式
 * 
 * @author jeecg-boot
 * @date 2025-01-20
 */
@Component
@Slf4j
public class ReimbursementCategoryUtil {
    
    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    /**
     * 配置的报销类目ID映射（业务类型 -> 报销类目ID）
     * 格式：reimbursement.category.address=1507265466155868162
     */
    @Value("${reimbursement.category.address:}")
    private String addressCategoryId;
    
    @Value("${reimbursement.category.seal:}")
    private String sealCategoryId;
    
    @Value("${reimbursement.category.trademark:}")
    private String trademarkCategoryId;
    
    @Value("${reimbursement.category.audit:}")
    private String auditCategoryId;
    
    // 缓存分类路径到ID的映射
    private static final Map<String, String> CATEGORY_PATH_CACHE = new HashMap<>();
    
    /**
     * 初始化缓存
     */
    @PostConstruct
    public void initCache() {
        if (sysCategoryService != null) {
            try {
                // 预加载常用分类
                loadCategoryByPath("报销类目/成本支出/地址费用");
                loadCategoryByPath("报销类目/成本支出/刻章费用");
                loadCategoryByPath("报销类目/成本支出/商标成本");
                loadCategoryByPath("报销类目/成本支出/审计成本");
            } catch (Exception e) {
                log.warn("初始化报销类目缓存失败", e);
            }
        }
    }
    
    /**
     * 获取地址费用的报销类目ID
     * 优先使用配置，如果没有配置则通过代码查询
     * 
     * @return 地址费用的分类ID
     */
    public String getAddressCategoryId() {
        // 1. 优先使用配置的ID
        if (addressCategoryId != null && !addressCategoryId.trim().isEmpty()) {
            return addressCategoryId.trim();
        }
        
        // 2. 从缓存获取
        String cachedId = CATEGORY_PATH_CACHE.get("报销类目/成本支出/地址费用");
        if (cachedId != null) {
            return cachedId;
        }
        
        // 3. 通过分类路径查询
        return loadCategoryByPath("报销类目/成本支出/地址费用");
    }
    
    /**
     * 获取刻章费用的报销类目ID
     */
    public String getSealCategoryId() {
        if (sealCategoryId != null && !sealCategoryId.trim().isEmpty()) {
            return sealCategoryId.trim();
        }
        return getCategoryIdByPath("报销类目/成本支出/刻章费用");
    }
    
    /**
     * 获取商标成本的报销类目ID
     */
    public String getTrademarkCategoryId() {
        if (trademarkCategoryId != null && !trademarkCategoryId.trim().isEmpty()) {
            return trademarkCategoryId.trim();
        }
        return getCategoryIdByPath("报销类目/成本支出/商标成本");
    }
    
    /**
     * 获取审计成本的报销类目ID
     */
    public String getAuditCategoryId() {
        if (auditCategoryId != null && !auditCategoryId.trim().isEmpty()) {
            return auditCategoryId.trim();
        }
        return getCategoryIdByPath("报销类目/成本支出/审计成本");
    }
    
    /**
     * 根据分类路径查询分类ID
     * 路径格式：父分类/子分类/孙分类
     * 例如：报销类目/成本支出/地址费用
     * 
     * @param categoryPath 分类路径，用"/"分隔
     * @return 分类ID，如果未找到返回null
     */
    public String getCategoryIdByPath(String categoryPath) {
        if (categoryPath == null || categoryPath.trim().isEmpty()) {
            return null;
        }
        
        // 从缓存获取
        String cachedId = CATEGORY_PATH_CACHE.get(categoryPath);
        if (cachedId != null) {
            return cachedId;
        }
        
        // 查询并缓存
        return loadCategoryByPath(categoryPath);
    }
    
    /**
     * 根据分类名称查询分类ID（递归查询，支持多级）
     * 
     * @param categoryName 分类名称
     * @param parentId 父分类ID，如果为null则从根节点查询
     * @return 分类ID，如果未找到返回null
     */
    public String getCategoryIdByName(String categoryName, String parentId) {
        if (sysCategoryService == null || categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }
        
        try {
            QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", categoryName.trim());
            if (parentId != null) {
                queryWrapper.eq("pid", parentId);
            }
            queryWrapper.last("LIMIT 1");
            
            SysCategory category = sysCategoryService.getOne(queryWrapper);
            if (category != null) {
                return category.getId();
            }
        } catch (Exception e) {
            log.error("查询分类ID失败: categoryName={}, parentId={}", categoryName, parentId, e);
        }
        
        return null;
    }
    
    /**
     * 加载分类路径并缓存
     * 
     * @param categoryPath 分类路径
     * @return 分类ID
     */
    private String loadCategoryByPath(String categoryPath) {
        if (sysCategoryService == null || categoryPath == null || categoryPath.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] pathParts = categoryPath.split("/");
            if (pathParts.length == 0) {
                return null;
            }
            
            String currentParentId = null;
            SysCategory targetCategory = null;
            
            // 逐级查询
            for (String part : pathParts) {
                String name = part.trim();
                if (name.isEmpty()) {
                    continue;
                }
                
                QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", name);
                if (currentParentId != null) {
                    queryWrapper.eq("pid", currentParentId);
                } else {
                    // 第一级，查找根节点（pid为0或null）
                    queryWrapper.and(wrapper -> wrapper.eq("pid", "0").or().isNull("pid"));
                }
                queryWrapper.last("LIMIT 1");
                
                SysCategory category = sysCategoryService.getOne(queryWrapper);
                if (category == null) {
                    log.warn("未找到分类: path={}, name={}, parentId={}", categoryPath, name, currentParentId);
                    return null;
                }
                
                targetCategory = category;
                currentParentId = category.getId();
            }
            
            if (targetCategory != null) {
                // 缓存结果
                CATEGORY_PATH_CACHE.put(categoryPath, targetCategory.getId());
                return targetCategory.getId();
            }
        } catch (Exception e) {
            log.error("加载分类路径失败: categoryPath={}", categoryPath, e);
        }
        
        return null;
    }
    
    /**
     * 根据业务类型获取对应的报销类目ID
     * 
     * @param businessType 业务类型（address-地址费用, seal-刻章费用, trademark-商标成本, audit-审计成本）
     * @return 报销类目ID
     */
    public String getCategoryIdByBusinessType(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return null;
        }
        
        switch (businessType.toLowerCase().trim()) {
            case "address":
            case "地址费用":
                return getAddressCategoryId();
            case "seal":
            case "刻章费用":
                return getCategoryIdByPath("报销类目/成本支出/刻章费用");
            case "trademark":
            case "商标成本":
                return getCategoryIdByPath("报销类目/成本支出/商标成本");
            case "audit":
            case "审计成本":
                return getCategoryIdByPath("报销类目/成本支出/审计成本");
            default:
                log.warn("未知的业务类型: {}", businessType);
                return null;
        }
    }
}

