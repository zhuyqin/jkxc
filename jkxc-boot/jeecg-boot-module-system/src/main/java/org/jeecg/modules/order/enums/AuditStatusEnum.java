package org.jeecg.modules.order.enums;

/**
 * 审核状态枚举
 * 
 * @author jeecg-boot
 * @since 2025-01-02
 */
public enum AuditStatusEnum {
    
    /**
     * 待审核
     */
    PENDING("pending", "待审核"),
    
    /**
     * 已通过
     */
    APPROVED("approved", "已通过"),
    
    /**
     * 已驳回
     */
    REJECTED("rejected", "已驳回");
    
    private final String code;
    private final String desc;
    
    AuditStatusEnum(String code, String desc) {
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
    public static AuditStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AuditStatusEnum status : values()) {
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
     * 判断是否为已通过状态
     */
    public static boolean isApproved(String code) {
        return APPROVED.code.equals(code);
    }
    
    /**
     * 判断是否为已驳回状态
     */
    public static boolean isRejected(String code) {
        return REJECTED.code.equals(code);
    }
}

