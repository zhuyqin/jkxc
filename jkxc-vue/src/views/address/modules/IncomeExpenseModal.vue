<template>
  <a-modal
    title="收支详情"
    :width="800"
    :visible="visible"
    @cancel="handleCancel"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="公司名称" :span="2">
          {{ record.companyName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="合同金额">
          <span style="color: #f5222d; font-weight: 600">¥{{ formatAmount(record.contractAmount) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="续费价格">
          <span style="color: #f5222d; font-weight: 600">¥{{ formatAmount(record.renewalFee) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="地址成本">
          <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(record.addressCost) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="已收金额">
          <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(record.receivedAmout) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="收支差额">
          <span :style="{color: getBalanceColor(balance), fontWeight: '600'}">¥{{ formatAmount(balance) }}</span>
        </a-descriptions-item>
      </a-descriptions>

      <a-divider>收支明细</a-divider>
      
      <a-table
        :columns="detailColumns"
        :dataSource="detailData"
        :pagination="false"
        size="small"
        bordered
      >
        <span slot="amount" slot-scope="text, record">
          <span :style="{color: record.type === 'income' ? '#52c41a' : '#f5222d', fontWeight: '600'}">
            {{ record.type === 'income' ? '+' : '-' }}¥{{ formatAmount(text) }}
          </span>
        </span>
      </a-table>
    </a-spin>
  </a-modal>
</template>

<script>
export default {
  name: 'IncomeExpenseModal',
  props: {
    addressId: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      visible: false,
      loading: false,
      record: {},
      detailData: [],
      detailColumns: [
        {
          title: '类型',
          dataIndex: 'typeText',
          align: 'center',
          width: 100,
        },
        {
          title: '金额',
          dataIndex: 'amount',
          align: 'center',
          width: 150,
          scopedSlots: { customRender: 'amount' },
        },
        {
          title: '说明',
          dataIndex: 'remark',
          align: 'center',
        },
        {
          title: '时间',
          dataIndex: 'createTime',
          align: 'center',
          width: 180,
        },
      ],
    }
  },
  computed: {
    balance() {
      const income = parseFloat(this.record.contractAmount || 0) + parseFloat(this.record.renewalFee || 0)
      const expense = parseFloat(this.record.addressCost || 0)
      return income - expense
    },
  },
  methods: {
    show(record) {
      this.record = record
      this.visible = true
      this.loadDetailData()
    },
    loadDetailData() {
      // TODO: 从后端获取收支明细数据
      // 这里先使用模拟数据
      this.detailData = [
        {
          key: '1',
          type: 'income',
          typeText: '收入',
          amount: this.record.contractAmount || 0,
          remark: '合同金额',
          createTime: this.record.createTime || '-',
        },
        {
          key: '2',
          type: 'expense',
          typeText: '支出',
          amount: this.record.addressCost || 0,
          remark: '地址成本',
          createTime: this.record.createTime || '-',
        },
      ]
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getBalanceColor(balance) {
      if (balance > 0) return '#52c41a'
      if (balance < 0) return '#f5222d'
      return '#999'
    },
    handleCancel() {
      this.visible = false
    },
  },
}
</script>

<style scoped>
</style>

