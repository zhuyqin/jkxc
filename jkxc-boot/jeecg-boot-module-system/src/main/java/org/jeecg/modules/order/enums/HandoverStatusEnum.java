package org.jeecg.modules.order.enums;

/**
 * 代账交接状态枚举
 * 
 * @author jeecg-boot
 * @since 2025-01-02
 */
public enum HandoverStatusEnum {
    
    /**
     * 待审核
     */
    PENDING("pending", "待审核"),
    
    /**
     * 审核通过
     */
    APPROVED("approved", "审核通过"),
    
    /**
     * 审核驳回
     */
    REJECTED("rejected", "审核驳回"),
    
    /**
     * 已完成
     */
    COMPLETED("completed", "已完成");
    
    private final String code;
    private final String desc;
    
    HandoverStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static HandoverStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (HandoverStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为待审核状态
     */
    public static boolean isPending(String code) {
        return PENDING.code.equals(code);
    }
    
    /**
     * 判断是否为审核通过状态
     */
    public static boolean isApproved(String code) {
        return APPROVED.code.equals(code);
    }
    
    /**
     * 判断是否为审核驳回状态
     */
    public static boolean isRejected(String code) {
        return REJECTED.code.equals(code);
    }
    
    /**
     * 判断是否为已完成状态
     */
    public static boolean isCompleted(String code) {
        return COMPLETED.code.equals(code);
    }
}

