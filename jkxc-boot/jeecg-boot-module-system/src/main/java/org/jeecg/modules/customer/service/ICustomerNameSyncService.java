package org.jeecg.modules.customer.service;

/**
 * 客户名称同步服务接口
 * 用于在修改客户名称时，同步更新所有关联表中的公司名称
 * 
 * @author jeecg-boot
 * @since 2026-03-01
 */
public interface ICustomerNameSyncService {
    
    /**
     * 同步客户名称到所有关联表
     * 
     * @param customerId 客户ID
     * @param newCorporateName 新的公司名称
     * @return 同步结果，包含更新的记录数
     */
    SyncResult syncCustomerNameToRelatedTables(String customerId, String newCorporateName);
    
    /**
     * 同步结果类
     */
    class SyncResult {
        private boolean success;
        private String message;
        private int orderCount;
        private int contractCount;
        private int handoverCount;
        private int taskCount;
        private int addressCount;
        private int renewCount;
        private int diaryCount;
        private int reimbursementCount;
        private int opportunityCount;
        private int totalCount;
        
        public SyncResult() {
        }
        
        public SyncResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getOrderCount() {
            return orderCount;
        }
        
        public void setOrderCount(int orderCount) {
            this.orderCount = orderCount;
        }
        
        public int getContractCount() {
            return contractCount;
        }
        
        public void setContractCount(int contractCount) {
            this.contractCount = contractCount;
        }
        
        public int getHandoverCount() {
            return handoverCount;
        }
        
        public void setHandoverCount(int handoverCount) {
            this.handoverCount = handoverCount;
        }
        
        public int getTaskCount() {
            return taskCount;
        }
        
        public void setTaskCount(int taskCount) {
            this.taskCount = taskCount;
        }
        
        public int getAddressCount() {
            return addressCount;
        }
        
        public void setAddressCount(int addressCount) {
            this.addressCount = addressCount;
        }
        
        public int getRenewCount() {
            return renewCount;
        }
        
        public void setRenewCount(int renewCount) {
            this.renewCount = renewCount;
        }
        
        public int getDiaryCount() {
            return diaryCount;
        }
        
        public void setDiaryCount(int diaryCount) {
            this.diaryCount = diaryCount;
        }
        
        public int getReimbursementCount() {
            return reimbursementCount;
        }
        
        public void setReimbursementCount(int reimbursementCount) {
            this.reimbursementCount = reimbursementCount;
        }
        
        public int getOpportunityCount() {
            return opportunityCount;
        }
        
        public void setOpportunityCount(int opportunityCount) {
            this.opportunityCount = opportunityCount;
        }
        
        public int getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
        
        public void calculateTotal() {
            this.totalCount = orderCount + contractCount + handoverCount + taskCount + 
                            addressCount + renewCount + diaryCount + reimbursementCount + opportunityCount;
        }
        
        @Override
        public String toString() {
            return "SyncResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", orderCount=" + orderCount +
                    ", contractCount=" + contractCount +
                    ", handoverCount=" + handoverCount +
                    ", taskCount=" + taskCount +
                    ", addressCount=" + addressCount +
                    ", renewCount=" + renewCount +
                    ", diaryCount=" + diaryCount +
                    ", reimbursementCount=" + reimbursementCount +
                    ", opportunityCount=" + opportunityCount +
                    ", totalCount=" + totalCount +
                    '}';
        }
    }
}
