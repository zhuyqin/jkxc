package org.jeecg.modules.notification.service;

/**
 * 企业微信通知服务接口
 * 
 * @author jeecg-boot
 * @since 2026-03-01
 */
public interface IWechatNotificationService {
    
    /**
     * 发送订单审核通知（给出纳）
     * 
     * @param orderId 订单ID
     * @param cashierUsername 出纳用户名
     * @return 是否发送成功
     */
    boolean sendOrderAuditNotification(String orderId, String cashierUsername);
    
    /**
     * 发送订单提交成功通知（给业务员）
     * 
     * @param orderId 订单ID
     * @param salesmanUsername 业务员用户名
     * @return 是否发送成功
     */
    boolean sendOrderSubmitNotification(String orderId, String salesmanUsername);
    
    /**
     * 发送订单提交通知（给上级）
     * 
     * @param orderId 订单ID
     * @param salesmanRealname 业务员真实姓名（提交人）
     * @param supervisorUsername 上级用户名
     * @return 是否发送成功
     */
    boolean sendOrderSubmitToSupervisorNotification(String orderId, String salesmanRealname, String supervisorUsername);
    
    /**
     * 发送商机提交成功通知（给业务员）
     * 
     * @param opportunityId 商机ID
     * @param salesmanUsername 业务员用户名
     * @return 是否发送成功
     */
    boolean sendOpportunitySubmitNotification(String opportunityId, String salesmanUsername);
    
    /**
     * 发送商机提交通知（给上级）
     * 
     * @param opportunityId 商机ID
     * @param salesmanRealname 业务员真实姓名（提交人）
     * @param supervisorUsername 上级用户名
     * @return 是否发送成功
     */
    boolean sendOpportunitySubmitToSupervisorNotification(String opportunityId, String salesmanRealname, String supervisorUsername);
    
    /**
     * 发送文本消息
     * 
     * @param username 接收人用户名（多个用逗号分隔）
     * @param content 消息内容
     * @return 是否发送成功
     */
    boolean sendTextMessage(String username, String content);
    
    /**
     * 发送文本卡片消息
     * 
     * @param username 接收人用户名（多个用逗号分隔）
     * @param title 标题
     * @param description 描述
     * @param url 跳转链接
     * @return 是否发送成功
     */
    boolean sendTextCardMessage(String username, String title, String description, String url);
}
