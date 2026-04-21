package org.jeecg.modules.order.enums;

/**
 * 交单方式枚举
 * 
 * @author jeecg-boot
 * @since 2025-01-02
 */
public enum DeliveryMethodEnum {
    
    /**
     * 工商部
     */
    BUSINESS_DEPT("gsb", "工商部"),
    
    /**
     * 会计部
     */
    ACCOUNTING_DEPT("kjb", "会计部");
    
    private final String code;
    private final String desc;
    
    DeliveryMethodEnum(String code, String desc) {
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
    public static DeliveryMethodEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DeliveryMethodEnum method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为工商部
     */
    public static boolean isBusinessDept(String code) {
        return BUSINESS_DEPT.code.equals(code);
    }
    
    /**
     * 判断是否为会计部
     */
    public static boolean isAccountingDept(String code) {
        return ACCOUNTING_DEPT.code.equals(code);
    }
}

