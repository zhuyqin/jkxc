<template>
  <a-modal
    title="企业等级详情"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :footer="null"
    :destroyOnClose="true"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="公司名称" :span="2">
          {{ detailInfo.companyName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="星级等级">
          <a-tag v-if="detailInfo.enterpriseLevel_dictText || detailInfo.enterpriseLevel" color="gold">
            {{ detailInfo.enterpriseLevel_dictText || 
               (detailInfo.enterpriseLevel === '1' ? '一星' : 
                detailInfo.enterpriseLevel === '2' ? '二星' : 
                detailInfo.enterpriseLevel === '3' ? '三星' : 
                detailInfo.enterpriseLevel === '4' ? '四星' : 
                detailInfo.enterpriseLevel === '5' ? '五星' : 
                detailInfo.enterpriseLevel) || '-' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="消费金额">
          <span v-if="detailInfo.totalSpending" style="color: #f5222d; font-weight: 600">
            ¥{{ formatAmount(detailInfo.totalSpending) }}
          </span>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="购买次数">
          {{ detailInfo.purchaseCount !== null && detailInfo.purchaseCount !== undefined ? detailInfo.purchaseCount + '次' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="服务时间">
          {{ detailInfo.serviceMonths !== null && detailInfo.serviceMonths !== undefined ? detailInfo.serviceMonths + '个月' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="高端业务">
          <a-tag :color="detailInfo.hasHighEndBusiness ? 'green' : 'orange'">
            {{ detailInfo.hasHighEndBusiness ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="转介绍数">
          {{ detailInfo.referralCount !== null && detailInfo.referralCount !== undefined ? detailInfo.referralCount + '个' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="关联数量">
          {{ detailInfo.relatedCount !== null && detailInfo.relatedCount !== undefined ? detailInfo.relatedCount + '个' : '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-spin>
  </a-modal>
</template>

<script>
import { getAction } from '@/api/manage'

export default {
  name: 'EnterpriseLevelDetailModal',
  data() {
    return {
      visible: false,
      confirmLoading: false,
      loading: false,
      detailInfo: {},
    }
  },
  methods: {
    show(record) {
      this.visible = true
      this.detailInfo = {}
      this.loadDetailInfo(record)
    },
    handleOk() {
      this.handleCancel()
    },
    handleCancel() {
      this.visible = false
      this.detailInfo = {}
    },
    loadDetailInfo(record) {
      this.loading = true
      // 优先使用customerId，如果没有则使用companyName
      // 注意：不要使用record.id，因为对于地址中心记录，record.id是地址ID，不是客户ID
      const customerId = record.customerId || record.companyId
      const companyName = record.companyName || record.corporateName
      
      const params = {}
      if (customerId) {
        // 优先使用customerId
        params.customerId = customerId
      } else if (companyName) {
        // 如果没有customerId，使用companyName查找客户
        params.companyName = companyName
      } else {
        this.$message.error('无法获取客户信息：缺少customerId或companyName')
        this.loading = false
        return
      }
      
      getAction('/customer/ghCustomer/getEnterpriseLevelDetail', params)
        .then((res) => {
          if (res.success && res.result) {
            this.detailInfo = res.result
          } else {
            this.$message.warning(res.message || '获取企业等级详情失败')
          }
        })
        .catch((err) => {
          console.error('获取企业等级详情失败', err)
          this.$message.error('获取企业等级详情失败')
        })
        .finally(() => {
          this.loading = false
        })
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

