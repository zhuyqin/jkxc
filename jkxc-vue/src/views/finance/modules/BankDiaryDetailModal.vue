<template>
  <a-modal
    :title="title"
    :width="900"
    :visible="visible"
    :confirmLoading="false"
    @cancel="handleCancel"
    :footer="null"
    :destroyOnClose="true">
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered size="small" v-if="model.id">
        <a-descriptions-item label="下单日期">
          {{ model.orderDate ? moment(model.orderDate).format('YYYY-MM-DD') : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="业务类型">
          {{ getBusinessTypeText(model.businessType) }}
        </a-descriptions-item>
        <a-descriptions-item label="费用详情" :span="2">
          {{ model.feeDetail || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="收入金额">
          <span v-if="model.incomeAmount && parseFloat(model.incomeAmount) > 0" style="color: #52c41a; font-weight: 600">
            ¥{{ formatAmount(model.incomeAmount) }}
          </span>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="支出金额">
          <span v-if="model.expenseAmount && parseFloat(model.expenseAmount) > 0" style="color: #f5222d; font-weight: 600">
            ¥{{ formatAmount(model.expenseAmount) }}
          </span>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="结余金额">
          <span v-if="model.balanceAmount" style="color: #1890ff; font-weight: 600">
            ¥{{ formatAmount(model.balanceAmount) }}
          </span>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="银行账户">
          {{ model.bankAccountName || model.bankAccountId_dictText || model.bankAccountId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="操作人员">
          {{ model.operatorName || model.operatorId_dictText || model.operatorId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="公司名称">
          {{ model.companyName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="备注信息" :span="2">
          {{ model.remarks || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="凭证附件" :span="2" v-if="model.vouchers">
          <j-image-upload v-model="voucherList" :disabled="true"></j-image-upload>
        </a-descriptions-item>
        <!-- 如果是报销业务，显示报销详情 -->
        <a-descriptions-item label="关联报销" :span="2" v-if="model.businessType === 'reimbursement' && model.reimbursementId">
          <a-button type="link" @click="viewReimbursement">查看报销详情</a-button>
        </a-descriptions-item>
        <!-- 如果是订单业务，显示订单详情 -->
        <a-descriptions-item label="关联订单" :span="2" v-if="model.businessType === 'order' && model.orderId">
          <a-button type="link" @click="viewOrder">查看订单详情</a-button>
        </a-descriptions-item>
      </a-descriptions>
    </a-spin>
  </a-modal>
</template>

<script>
import { getAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'BankDiaryDetailModal',
  data() {
    return {
      title: '银行日记详情',
      visible: false,
      loading: false,
      model: {},
      voucherList: [],
    }
  },
  methods: {
    show(record) {
      this.visible = true
      this.model = Object.assign({}, record)
      // 如果只有ID，需要查询详情
      if (record.id && !record.orderDate) {
        this.loading = true
        getAction('/bankDiary/ghBankDiary/queryById', { id: record.id }).then((res) => {
          if (res.success && res.result) {
            this.model = Object.assign({}, res.result)
            // 处理凭证列表
            if (this.model.vouchers) {
              try {
                const vouchers = JSON.parse(this.model.vouchers)
                this.voucherList = Array.isArray(vouchers) ? vouchers : []
              } catch (e) {
                this.voucherList = []
              }
            }
          }
        }).finally(() => {
          this.loading = false
        })
      } else {
        // 处理凭证列表
        if (this.model.vouchers) {
          try {
            const vouchers = JSON.parse(this.model.vouchers)
            this.voucherList = Array.isArray(vouchers) ? vouchers : []
          } catch (e) {
            this.voucherList = []
          }
        }
      }
    },
    handleCancel() {
      this.visible = false
      this.model = {}
      this.voucherList = []
    },
    getBusinessTypeText(type) {
      const typeMap = {
        'reimbursement': '报销',
        'order': '订单',
        'manual': '手动',
      }
      return typeMap[type] || type || '-'
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    viewReimbursement() {
      if (this.model.reimbursementId) {
        this.$router.push({
          path: '/reimbursement-management/list',
          query: { id: this.model.reimbursementId },
        })
      }
    },
    viewOrder() {
      if (this.model.orderId) {
        this.$router.push({
          path: '/order/ghOrder/list',
          query: { id: this.model.orderId },
        })
      }
    },
    moment,
  },
}
</script>

<style scoped>
</style>

