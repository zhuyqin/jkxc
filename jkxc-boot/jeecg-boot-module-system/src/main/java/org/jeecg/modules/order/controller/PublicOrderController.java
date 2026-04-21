package org.jeecg.modules.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开订单接口（无需登录）
 * 用于企业微信通知跳转查看订单详情
 * 
 * @author jeecg-boot
 * @since 2026-03-01
 */
@Api(tags = "公开订单接口")
@RestController
@RequestMapping("/public/order")
@Slf4j
public class PublicOrderController {
    
    @Autowired
    private IGhOrderService ghOrderService;
    
    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    /**
     * 获取订单详情（无需登录）
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    @ApiOperation(value = "获取订单详情（无需登录）", notes = "用于企业微信通知跳转")
    @GetMapping("/detail")
    public Result<?> getOrderDetail(@RequestParam(name = "id", required = true) String id) {
        try {
            // 查询订单
            GhOrder order = ghOrderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("id", order.getId());
            data.put("orderNo", order.getOrderNo());
            data.put("companyName", order.getCompanyName());
            data.put("businessType", order.getBusinessType());
            data.put("businessTypeName", getBusinessTypeName(order.getBusinessType()));
            data.put("orderStatus", order.getOrderStatus());
            data.put("orderStatusName", getDictText("order_status", order.getOrderStatus()));
            data.put("createTime", order.getCreateTime());
            data.put("createBy", order.getCreateBy());
            data.put("remarks", order.getRemarks());
            
            // 金额相关字段
            data.put("orderAmount", order.getOrderAmount());
            data.put("contractAmount", order.getContractAmount());
            data.put("finalPaymentAmount", order.getFinalPaymentAmount());
            data.put("receivedAmount", order.getReceivedAmount());
            
            // 联系信息
            data.put("contacts", order.getContacts());
            data.put("contactInformation", order.getContactInformation());
            data.put("address", order.getAddress());
            data.put("salesman", order.getSalesman());
            data.put("region", order.getRegion());
            
            // 其他信息
            data.put("deliveryMethod", order.getDeliveryMethod());
            data.put("collectionTime", order.getCollectionTime());
            data.put("collectionAccountNumber", order.getCollectionAccountNumber());
            
            return Result.OK(data);
        } catch (Exception e) {
            log.error("获取订单详情失败：id={}", id, e);
            return Result.error("获取订单详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取业务类型中文名称
     */
    private String getBusinessTypeName(String businessTypeId) {
        if (oConvertUtils.isEmpty(businessTypeId)) {
            return "未知";
        }
        
        try {
            if (sysCategoryService != null) {
                SysCategory category = sysCategoryService.getById(businessTypeId);
                if (category != null) {
                    return category.getName();
                }
            }
        } catch (Exception e) {
            log.error("获取业务类型名称失败：businessTypeId={}", businessTypeId, e);
        }
        
        return "未知";
    }
    
    /**
     * 获取字典文本
     * 简化版本，只处理订单状态
     */
    private String getDictText(String dictCode, String dictValue) {
        if (oConvertUtils.isEmpty(dictValue)) {
            return "未知";
        }
        
        // 订单状态字典
        if ("order_status".equals(dictCode)) {
            switch (dictValue) {
                case "0": return "待审核";
                case "1": return "审核中";
                case "2": return "已通过";
                case "3": return "已拒绝";
                case "4": return "进行中";
                case "5": return "已完成";
                case "6": return "已取消";
                default: return dictValue;
            }
        }
        
        return dictValue;
    }
}
