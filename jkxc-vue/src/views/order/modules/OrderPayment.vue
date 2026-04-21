<template>
  <div>
    <a-spin :spinning="loading">
      <!-- 订单基本信息 -->
      <a-card title="订单信息" :bordered="false" style="margin-bottom: 16px">
        <a-row :gutter="16">
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">下单时间：</span>
            <span>{{ formatDateTime(orderInfo.createTime) || '--' }}</span>
          </a-col>
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">订单编号：</span>
            {{ orderInfo.orderNo || '--' }}
          </a-col>
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">业务人员：</span>
            {{ orderInfo.salesman || '--' }}
          </a-col>
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">所属团队：</span>
            {{ orderInfo.teamName || '--' }}
          </a-col>
        </a-row>
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">公司名称：</span>
            {{ orderInfo.companyName || companyName || '--' }}
          </a-col>
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">业务类型：</span>
            {{ orderInfo.businessType_dictText || orderInfo.businessTypeName || '--' }}
          </a-col>
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">客户来源：</span>
            {{ orderInfo.opportunitySource_dictText || orderInfo.opportunitySource || '--' }}
          </a-col>
          <a-col :span="6">
            <span style="color: rgba(0, 0, 0, 0.85); font-weight: 500">备注信息：</span>
            <span>{{ orderInfo.remarks || '--' }}</span>
          </a-col>
        </a-row>
      </a-card>

      <!-- 收费记录 -->
      <a-card title="收费记录" :bordered="false" style="margin-bottom: 16px">
        <a-table
          :columns="paymentColumns"
          :dataSource="paymentList"
          :pagination="false"
          :loading="loading"
          :scroll="{ x: 1800 }"
          rowKey="id"
        >
          <template slot="contractAmount" slot-scope="text">
            <span style="color: #1890ff; font-weight: 600">¥{{ formatAmount(text) }}</span>
          </template>
          <template slot="amount" slot-scope="text">
            <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(text) }}</span>
          </template>
          <template slot="finalPaymentAmount" slot-scope="text">
            <span style="color: #faad14; font-weight: 600">¥{{ formatAmount(text) }}</span>
          </template>
          <template slot="paymentTime" slot-scope="text">
            {{ formatDateTime(text) }}
          </template>
          <template slot="accountNumber" slot-scope="text, record">
            <span v-if="record.paymentMethod || record.payeePerson || record.accountNotes || record.collectionAccount">
              {{ [record.paymentMethod, record.payeePerson, record.accountNotes, record.collectionAccount].filter(Boolean).join(' / ') }}
            </span>
            <span v-else-if="text">{{ text }}</span>
            <span v-else>--</span>
          </template>
          <template slot="serviceStartTime" slot-scope="text, record">
            {{ formatServiceTime(record.serviceStartTimeStr, record.serviceStartTime) }}
          </template>
          <template slot="serviceEndTime" slot-scope="text, record">
            {{ formatServiceTime(record.serviceEndTimeStr, record.serviceEndTime) || '--' }}
          </template>
          <template slot="totalMonths" slot-scope="text">
            {{ text || 0 }}个月
          </template>
          <template slot="imageVoucher" slot-scope="text, record">
            <div v-if="text" style="display: flex; flex-wrap: wrap; gap: 8px;">
              <img
                v-for="(img, index) in getImageList(text)"
                :key="index"
                :src="getImageUrl(img)"
                alt="凭证图片"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </template>
        </a-table>
        <a-empty v-if="!loading && (!paymentList || paymentList.length === 0)" description="暂无收费记录" style="padding: 40px 0" />
      </a-card>
      
      <!-- 图片预览弹窗 -->
      <a-modal :visible="previewVisible" :footer="null" @cancel="previewVisible = false" :width="800">
        <img alt="预览" style="width: 100%" :src="previewImageUrl" />
      </a-modal>

      <!-- 支出记录 -->
      <a-card title="成本支出" :bordered="false">
        <a-table
          :columns="expenseColumns"
          :dataSource="expenseList"
          :pagination="false"
          :loading="loading"
          :scroll="{ x: 800 }"
          rowKey="id"
        >
          <template slot="amount" slot-scope="text">
            <span style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
          </template>
          <template slot="category" slot-scope="text">
            {{ text || '--' }}
          </template>
          <template slot="paymentAccount" slot-scope="text">
            {{ text || '--' }}
          </template>
          <template slot="expenseTime" slot-scope="text">
            {{ formatDateTime(text) }}
          </template>
          <template slot="auditStatus" slot-scope="text">
            <a-tag :color="text === '1' ? 'green' : 'orange'">
              {{ text === '1' ? '已审核' : '待审核' }}
            </a-tag>
          </template>
        </a-table>
        <a-empty v-if="!loading && (!expenseList || expenseList.length === 0)" description="暂无支出记录" style="padding: 40px 0" />
      </a-card>

      <!-- 统计汇总 -->
      <a-card title="统计汇总" :bordered="false" style="margin-top: 16px">
        <a-row :gutter="24">
          <a-col :span="6">
            <a-statistic
              title="收款金额（总）"
              :value="totalReceivedAmount"
              :precision="2"
              prefix="¥"
              :value-style="{ color: '#52c41a' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="支出金额（总）"
              :value="totalExpense"
              :precision="2"
              prefix="¥"
              :value-style="{ color: '#f5222d' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="毛利润额（核算业绩）"
              :value="grossProfit"
              :precision="2"
              prefix="¥"
              :value-style="{ color: grossProfit >= 0 ? '#52c41a' : '#f5222d' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="毛利润率"
              :value="grossProfitRate"
              :precision="2"
              suffix="%"
              :value-style="{ color: grossProfitRate >= 0 ? '#52c41a' : '#f5222d' }"
            />
          </a-col>
        </a-row>
      </a-card>
    </a-spin>
  </div>
</template>

<script>
import { getAction } from '@api/manage'

export default {
  name: 'OrderPayment',
  props: {
    orderId: {
      type: String,
      default: '',
      required: true
    },
    companyName: {
      type: String,
      default: '',
      required: false
    }
  },
  data() {
    return {
      loading: false,
      orderInfo: {},
      paymentList: [],
      expenseList: [],
      paymentColumns: [
        {
          title: '合同金额',
          dataIndex: 'contractAmount',
          key: 'contractAmount',
          width: 120,
          scopedSlots: { customRender: 'contractAmount' },
        },
        {
          title: '收款金额',
          dataIndex: 'amount',
          key: 'amount',
          width: 120,
          scopedSlots: { customRender: 'amount' },
        },
        {
          title: '尾款金额',
          dataIndex: 'finalPaymentAmount',
          key: 'finalPaymentAmount',
          width: 120,
          scopedSlots: { customRender: 'finalPaymentAmount' },
        },
        {
          title: '收款时间',
          dataIndex: 'paymentTime',
          key: 'paymentTime',
          width: 160,
          scopedSlots: { customRender: 'paymentTime' },
        },
        {
          title: '收款账户',
          dataIndex: 'accountNumber',
          key: 'accountNumber',
          width: 250,
          scopedSlots: { customRender: 'accountNumber' },
        },
        {
          title: '服务时间',
          dataIndex: 'serviceStartTime',
          key: 'serviceStartTime',
          width: 160,
          scopedSlots: { customRender: 'serviceStartTime' },
        },
        {
          title: '到期时间',
          dataIndex: 'serviceEndTime',
          key: 'serviceEndTime',
          width: 160,
          scopedSlots: { customRender: 'serviceEndTime' },
        },
        {
          title: '合计月份',
          dataIndex: 'totalMonths',
          key: 'totalMonths',
          width: 100,
          scopedSlots: { customRender: 'totalMonths' },
        },
        {
          title: '凭证图片',
          dataIndex: 'imageVoucher',
          key: 'imageVoucher',
          width: 200,
          scopedSlots: { customRender: 'imageVoucher' },
        },
      ],
      previewVisible: false,
      previewImageUrl: '',
      expenseColumns: [
        {
          title: '支出金额',
          dataIndex: 'amount',
          key: 'amount',
          width: 120,
          scopedSlots: { customRender: 'amount' },
        },
        {
          title: '支出类目',
          dataIndex: 'category',
          key: 'category',
          width: 150,
          scopedSlots: { customRender: 'category' },
        },
        {
          title: '支付账户',
          dataIndex: 'paymentAccount',
          key: 'paymentAccount',
          width: 150,
          scopedSlots: { customRender: 'paymentAccount' },
        },
        {
          title: '审核状态',
          dataIndex: 'auditStatus',
          key: 'auditStatus',
          width: 100,
          align: 'center',
          scopedSlots: { customRender: 'auditStatus' },
        },
        {
          title: '支付时间',
          dataIndex: 'expenseTime',
          key: 'expenseTime',
          width: 160,
          scopedSlots: { customRender: 'expenseTime' },
        },
      ],
    }
  },
  computed: {
    receivedAmount() {
      if (!this.paymentList || this.paymentList.length === 0) {
        return '0.00'
      }
      const total = this.paymentList
        .filter(item => item.status === '1')
        .reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
      return total.toFixed(2)
    },
    // 收款金额（总）- 所有已确认的收款金额
    totalReceivedAmount() {
      if (!this.paymentList || this.paymentList.length === 0) {
        return 0
      }
      return this.paymentList
        .filter(item => item.status === '1')
        .reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
    },
    // 支出金额（总）
    totalExpense() {
      if (!this.expenseList || this.expenseList.length === 0) {
        return 0
      }
      return this.expenseList.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
    },
    // 毛利润额（核算业绩）= 收款金额（总）- 支出金额（总）
    grossProfit() {
      return this.totalReceivedAmount - this.totalExpense
    },
    // 毛利润率 = (毛利润额 / 收款金额（总）) * 100
    grossProfitRate() {
      if (this.totalReceivedAmount === 0) {
        return 0
      }
      return ((this.grossProfit / this.totalReceivedAmount) * 100).toFixed(2)
    },
  },
  created() {
    this.init()
  },
  watch: {
    orderId: {
      handler() {
        this.init()
      },
      immediate: true
    }
  },
  methods: {
    formatDateTime(dateTime) {
      if (!dateTime) return '--'
      if (typeof dateTime === 'string') {
        return dateTime.length > 19 ? dateTime.substr(0, 19) : dateTime
      }
      if (dateTime instanceof Date) {
        return dateTime.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
      }
      return dateTime
    },
    formatServiceTime(originalStr, dateTime) {
      // 如果原始字符串是月份格式（YYYY-MM），直接返回原始字符串
      if (originalStr && /^\d{4}-\d{2}$/.test(originalStr)) {
        return originalStr
      }
      // 否则按日期格式显示
      return this.formatDateTime(dateTime)
    },
    formatAmount(amount) {
      if (!amount && amount !== 0) return '0.00'
      const num = Number(amount)
      return isNaN(num) ? '0.00' : num.toFixed(2)
    },
    getImageList(imageVoucher) {
      if (!imageVoucher) return []
      try {
        // 如果是JSON字符串，解析为数组
        if (typeof imageVoucher === 'string' && imageVoucher.startsWith('[')) {
          return JSON.parse(imageVoucher)
        }
        // 如果是逗号分隔的字符串，转换为数组
        if (typeof imageVoucher === 'string' && imageVoucher.includes(',')) {
          return imageVoucher.split(',').filter(item => item.trim())
        }
        // 如果是单个字符串，转换为数组
        return [imageVoucher]
      } catch (e) {
        return [imageVoucher]
      }
    },
    getImageUrl(imagePath) {
      if (!imagePath) return ''
      // 如果已经是完整的URL，直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      // 如果是相对路径，通过后端接口访问
      if (imagePath.startsWith('/')) {
        return imagePath
      }
      // 否则通过后端静态资源接口访问
      return '/sys/common/static/' + imagePath
    },
    previewImage(imageUrl) {
      this.previewImageUrl = imageUrl
      this.previewVisible = true
    },
    init() {
      if (!this.orderId) {
        return
      }
      this.loading = true
      // 加载订单信息和收费详情
      Promise.all([
        getAction('/order/queryById', { id: this.orderId }),
        getAction('/order/getPaymentList', { orderId: this.orderId }),
        getAction('/order/getExpenseList', { orderId: this.orderId })
      ]).then(([orderRes, paymentRes, expenseRes]) => {
        if (orderRes.success) {
          this.orderInfo = orderRes.result || {}
          // 如果后端没有返回teamName，需要根据业务员查询
          if (!this.orderInfo.teamName && this.orderInfo.salesman) {
            this.loadTeamName()
          }
        }
        if (paymentRes.success) {
          this.paymentList = paymentRes.result || []
        }
        if (expenseRes.success) {
          this.expenseList = expenseRes.result || []
        }
      }).catch(err => {
        console.error('加载收费详情失败:', err)
        this.$message.error('加载收费详情失败')
      }).finally(() => {
        this.loading = false
      })
    },
    // 加载团队名称（如果后端没有返回）
    loadTeamName() {
      // 这里可以调用API查询业务员的团队，但为了简化，暂时不实现
      // 因为后端queryById应该已经返回了teamName
    },
  },
}
</script>

<style scoped>
</style>

