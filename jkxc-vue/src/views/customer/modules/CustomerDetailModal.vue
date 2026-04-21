<template>
  <a-modal
    title="客户详情"
    :width="900"
    :visible="visible"
    @cancel="handleCancel"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="公司名称" :span="2">
          {{ customerInfo.corporateName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系人">
          {{ customerInfo.contacts || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系电话">
          {{ customerInfo.contactInformation || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="所属区域">
          {{ customerInfo.region || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="详细地址">
          {{ customerInfo.address || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="法人姓名">
          {{ customerInfo.legalPersonName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="法人电话">
          {{ customerInfo.legalPersonPhone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="税号">
          {{ customerInfo.dutyParagraph || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="注册地址">
          {{ customerInfo.registeredAddress || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="实际地址">
          {{ customerInfo.actualAddress || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="注册资金">
          {{ customerInfo.registeredCapital || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="经营范围" :span="2">
          {{ customerInfo.businessScope || '-' }}
        </a-descriptions-item>
        
        <a-divider>计算字段</a-divider>
        
        <a-descriptions-item label="企业等级">
          <a-tag v-if="customerInfo.enterpriseLevel_dictText || customerInfo.enterpriseLevel" color="gold">
            {{ customerInfo.enterpriseLevel_dictText || customerInfo.enterpriseLevel || '-' }}
          </a-tag>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="消费金额">
          <span v-if="customerInfo.totalSpending" style="color: #f5222d; font-weight: 600; font-size: 16px;">
            ¥{{ formatAmount(customerInfo.totalSpending) }}
          </span>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="业务标签" :span="2">
          <template v-if="customerInfo.businessTag">
            <a-tag v-for="(tag, index) in customerInfo.businessTag.split(',')" :key="index" color="blue" style="margin-bottom: 4px;">
              {{ tag }}
            </a-tag>
          </template>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="关联企业">
          {{ customerInfo.relatedCompanyName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge :status="customerInfo.status === '1' ? 'success' : 'default'" :text="customerInfo.status === '1' ? '启用' : '禁用'" />
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ customerInfo.createTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ customerInfo.updateTime || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-spin>
  </a-modal>
</template>

<script>
import { getAction } from '@/api/manage'

export default {
  name: 'CustomerDetailModal',
  data() {
    return {
      visible: false,
      loading: false,
      customerInfo: {},
    }
  },
  methods: {
    show(record) {
      this.visible = true
      this.loading = true
      this.customerInfo = {}
      
      // 调用后端接口获取客户详情（包含计算字段）
      getAction('/customer/ghCustomer/queryById', { id: record.id }).then((res) => {
        if (res.success && res.result) {
          this.customerInfo = res.result
        } else {
          // 如果接口不存在，直接使用传入的record
          this.customerInfo = record
        }
      }).catch(() => {
        // 如果接口不存在，直接使用传入的record
        this.customerInfo = record
      }).finally(() => {
        this.loading = false
      })
    },
    handleCancel() {
      this.visible = false
      this.customerInfo = {}
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
  },
}
</script>

<style scoped>
</style>

