package org.jeecg.modules.notification.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jeecg.qywx.api.message.JwMessageAPI;
import com.jeecg.qywx.api.message.vo.Text;
import com.jeecg.qywx.api.message.vo.TextCard;
import com.jeecg.qywx.api.message.vo.TextCardEntity;
import com.jeecg.qywx.api.message.vo.TextEntity;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.thirdapp.ThirdAppConfig;
import org.jeecg.modules.notification.service.IWechatNotificationService;
import org.jeecg.modules.order.entity.GhOrder;
import org.jeecg.modules.order.service.IGhOrderService;
import org.jeecg.modules.opportunity.entity.GhOpportunity;
import org.jeecg.modules.opportunity.service.IGhOpportunityService;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * 企业微信通知服务实现类
 * 
 * @author jeecg-boot
 * @since 2026-03-01
 */
@Slf4j
@Service
public class WechatNotificationServiceImpl implements IWechatNotificationService {
    
    @Autowired
    private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
    
    @Autowired
    private ThirdAppConfig thirdAppConfig;
    
    @Autowired(required = false)
    private IGhOrderService ghOrderService;
    
    @Autowired(required = false)
    private IGhOpportunityService ghOpportunityService;
    
    @Autowired
    private ISysUserService sysUserService;
    
    @Autowired(required = false)
    private ISysCategoryService sysCategoryService;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 获取业务类型的中文名称
     * 
     * @param businessTypeId 业务类型ID
     * @return 业务类型中文名称
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
     * 根据用户名获取企业微信的userid
     * 通过用户的手机号调用企业微信API获取userid
     * 
     * @param username 用户名
     * @return 企业微信userid，如果获取失败则返回null
     */
    private String getWechatUserId(String username) {
        if (oConvertUtils.isEmpty(username)) {
            log.warn("用户名为空，无法获取企业微信userid");
            return null;
        }
        
        try {
            // 1. 获取用户信息
            SysUser user = sysUserService.getUserByName(username);
            if (user == null) {
                log.warn("用户不存在：{}", username);
                return null;
            }
            
            // 2. 获取用户手机号
            String phone = user.getPhone();
            if (oConvertUtils.isEmpty(phone)) {
                log.warn("用户{}未配置手机号，无法获取企业微信userid", username);
                return null;
            }
            
            // 3. 调用企业微信API获取userid
            String accessToken = wechatEnterpriseService.getAppAccessToken();
            if (accessToken == null) {
                log.error("获取企业微信AccessToken失败");
                return null;
            }
            
            // 4. 构建请求
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserid?access_token=" + accessToken;
            JSONObject requestBody = new JSONObject();
            requestBody.put("mobile", phone);
            
            // 5. 发送请求
            JSONObject result = RestUtil.post(url, requestBody);

            // 6. 解析结果
            if (result != null && result.getIntValue("errcode") == 0) {
                String userid = result.getString("userid");
                log.info("通过手机号获取企业微信userid成功：username={}, phone={}, userid={}", username, phone, userid);
                return userid;
            } else {
                log.error("通过手机号获取企业微信userid失败：username={}, phone={}, response={}", username, phone, result);
                return null;
            }
        } catch (Exception e) {
            log.error("获取企业微信userid异常：username={}", username, e);
            return null;
        }
    }
    
    @Override
    public boolean sendOrderAuditNotification(String orderId, String cashierUsername) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        if (ghOrderService == null) {
            log.warn("订单服务未注入，无法发送通知");
            return false;
        }
        
        try {
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                log.error("订单不存在：{}", orderId);
                return false;
            }
            
            // 获取业务类型中文名称
            String businessTypeName = getBusinessTypeName(order.getBusinessType());
            
            // 构建通知内容
            String description = String.format(
                "出纳：您有一条待审核的订单，下单时间为：%s，公司名称为：%s，业务类型为：%s，请您审核，谢谢！",
                order.getCreateTime() != null ? DATE_FORMAT.format(order.getCreateTime()) : "未知",
                oConvertUtils.getString(order.getCompanyName(), "未知"),
                businessTypeName
            );
            
            // 构建跳转链接（无需登录的公开页面）
            String url = RestUtil.getBaseUrl() + "/public/order/detail?id=" + orderId;
            
            // 发送文本卡片消息
            return sendTextCardMessage(
                cashierUsername,
                "订单审核通知",
                description,
                url
            );
        } catch (Exception e) {
            log.error("发送订单审核通知失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendOrderSubmitNotification(String orderId, String salesmanUsername) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        if (ghOrderService == null) {
            log.warn("订单服务未注入，无法发送通知");
            return false;
        }
        
        try {
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                log.error("订单不存在：{}", orderId);
                return false;
            }
            
            // 获取业务类型中文名称
            String businessTypeName = getBusinessTypeName(order.getBusinessType());
            
            // 构建通知内容
            String description = String.format(
                "业务：您的订单已提交成功，下单时间为：%s，公司名称为：%s，业务类型为：%s，我们将尽快为您处理，请留意后续通知。",
                order.getCreateTime() != null ? DATE_FORMAT.format(order.getCreateTime()) : "未知",
                oConvertUtils.getString(order.getCompanyName(), "未知"),
                businessTypeName
            );
            
            // 构建跳转链接（无需登录的公开页面）
            String url = RestUtil.getBaseUrl() + "/public/order/detail?id=" + orderId;
            
            // 发送文本卡片消息
            return sendTextCardMessage(
                salesmanUsername,
                "订单提交成功",
                description,
                url
            );
        } catch (Exception e) {
            log.error("发送订单提交通知失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendOrderSubmitToSupervisorNotification(String orderId, String salesmanUsername, String supervisorUsername) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        if (ghOrderService == null) {
            log.warn("订单服务未注入，无法发送通知");
            return false;
        }
        
        try {
            GhOrder order = ghOrderService.getById(orderId);
            if (order == null) {
                log.error("订单不存在：{}", orderId);
                return false;
            }
            
            // 获取业务类型中文名称
            String businessTypeName = getBusinessTypeName(order.getBusinessType());
            
            // 构建通知内容
            String description = String.format(
                "上级：已收到您团队%s提交的订单，下单时间为：%s，公司名称为：%s，业务类型为：%s，我们将尽快为您处理，请留意后续通知。",
                oConvertUtils.getString(salesmanUsername, "未知"),
                order.getCreateTime() != null ? DATE_FORMAT.format(order.getCreateTime()) : "未知",
                oConvertUtils.getString(order.getCompanyName(), "未知"),
                businessTypeName
            );
            
            // 构建跳转链接（无需登录的公开页面）
            String url = RestUtil.getBaseUrl() + "/public/order/detail?id=" + orderId;
            
            // 发送文本卡片消息
            return sendTextCardMessage(
                supervisorUsername,
                "团队订单提交通知",
                description,
                url
            );
        } catch (Exception e) {
            log.error("发送订单提交通知（上级）失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendOpportunitySubmitNotification(String opportunityId, String salesmanUsername) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        if (ghOpportunityService == null) {
            log.warn("商机服务未注入，无法发送通知");
            return false;
        }
        
        try {
            GhOpportunity opportunity = ghOpportunityService.getById(opportunityId);
            if (opportunity == null) {
                log.error("商机不存在：{}", opportunityId);
                return false;
            }
            
            // 构建通知内容
            String content = String.format(
                "您的商机线索已提交成功，提交时间为：%s，公司名称为：%s，商机名称：%s，请知悉及时安排跟进。",
                opportunity.getCreateTime() != null ? DATE_FORMAT.format(opportunity.getCreateTime()) : "未知",
                oConvertUtils.getString(opportunity.getCorporateName(), "未知"),
                oConvertUtils.getString(opportunity.getOpportunityName(), "未知")
            );
            
            // 发送文本消息
            return sendTextMessage(salesmanUsername, content);
        } catch (Exception e) {
            log.error("发送商机提交通知失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendOpportunitySubmitToSupervisorNotification(String opportunityId, String salesmanRealname, String supervisorUsername) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        if (ghOpportunityService == null) {
            log.warn("商机服务未注入，无法发送通知");
            return false;
        }
        
        try {
            GhOpportunity opportunity = ghOpportunityService.getById(opportunityId);
            if (opportunity == null) {
                log.error("商机不存在：{}", opportunityId);
                return false;
            }
            
            // 构建通知内容
            String content = String.format(
                "商机：已收到您团队%s商机线索的信息，提交时间为：%s，公司名称为：%s，商机名称：%s，请知悉及时安排跟进。",
                oConvertUtils.getString(salesmanRealname, "未知"),
                opportunity.getCreateTime() != null ? DATE_FORMAT.format(opportunity.getCreateTime()) : "未知",
                oConvertUtils.getString(opportunity.getCorporateName(), "未知"),
                oConvertUtils.getString(opportunity.getOpportunityName(), "未知")
            );
            
            // 发送文本消息
            return sendTextMessage(supervisorUsername, content);
        } catch (Exception e) {
            log.error("发送商机提交通知（上级）失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendTextMessage(String username, String content) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        // 通过手机号获取企业微信userid
        String userid = getWechatUserId(username);
        if (userid == null) {
            log.error("无法获取用户{}的企业微信userid，跳过发送通知", username);
            return false;
        }
        
        try {
            String accessToken = wechatEnterpriseService.getAppAccessToken();
            if (accessToken == null) {
                log.error("获取企业微信AccessToken失败");
                return false;
            }
            
            Text text = new Text();
            text.setMsgtype("text");
            text.setTouser(userid);  // 使用企业微信userid
            
            TextEntity entity = new TextEntity();
            entity.setContent(content);
            text.setText(entity);
            
            text.setAgentid(thirdAppConfig.getWechatEnterprise().getAgentIdInt());
            
            JSONObject response = JwMessageAPI.sendTextMessage(text, accessToken);
            
            if (response != null && response.getIntValue("errcode") == 0) {
                log.info("企业微信文本消息发送成功：username={}, userid={}", username, userid);
                return true;
            } else {
                log.error("企业微信文本消息发送失败：username={}, userid={}, response={}", username, userid, response);
                return false;
            }
        } catch (Exception e) {
            log.error("发送企业微信文本消息异常：username={}, userid={}", username, userid, e);
            return false;
        }
    }
    
    @Override
    public boolean sendTextCardMessage(String username, String title, String description, String url) {
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
            log.warn("企业微信未启用，跳过发送通知");
            return false;
        }
        
        // 通过手机号获取企业微信userid
        String userid = getWechatUserId(username);
        if (userid == null) {
            log.error("无法获取用户{}的企业微信userid，跳过发送通知", username);
            return false;
        }
        
        try {
            String accessToken = wechatEnterpriseService.getAppAccessToken();
            if (accessToken == null) {
                log.error("获取企业微信AccessToken失败");
                return false;
            }
            
            TextCard textCard = new TextCard();
            textCard.setAgentid(thirdAppConfig.getWechatEnterprise().getAgentIdInt());
            textCard.setTouser(userid);  // 使用企业微信userid
            
            TextCardEntity entity = new TextCardEntity();
            entity.setTitle(title);
            entity.setDescription(description);
            entity.setUrl(url);
            textCard.setTextcard(entity);
            
            JSONObject response = JwMessageAPI.sendTextCardMessage(textCard, accessToken);
            
            if (response != null && response.getIntValue("errcode") == 0) {
                log.info("企业微信文本卡片消息发送成功：username={}, userid={}, title={}", username, userid, title);
                return true;
            } else {
                log.error("企业微信文本卡片消息发送失败：username={}, userid={}, response={}", username, userid, response);
                return false;
            }
        } catch (Exception e) {
            log.error("发送企业微信文本卡片消息异常：username={}, userid={}", username, userid, e);
            return false;
        }
    }
}
