<template>
  <a-modal
    :title="title"
    :width="1200"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :footer="null"
    :destroyOnClose="true">
    <a-spin :spinning="loading">
      <div style="margin-bottom: 16px;">
        <a-statistic
          title="订单总数"
          :value="orderList.length"
          style="display: inline-block; margin-right: 32px;"
        />
        <a-statistic
          title="总消费金额"
          :value="displayTotalAmount"
          :precision="2"
          prefix="¥"
          style="display: inline-block;"
        />
      </div>
      <a-empty v-if="!loading && (!orderList || orderList.length === 0)" description="暂无复购信息" style="padding: 40px 0" />
      <a-table
        v-else
        :columns="columns"
        :dataSource="orderList"
        :pagination="false"
        :scroll="{ x: true }"
        size="middle"
        bordered
        rowKey="id"
      >
        <span slot="orderNo" slot-scope="text, record">
          <a @click="handleOrderDetail(record)" style="color: #1890ff; cursor: pointer;">{{ text }}</a>
        </span>
        <span slot="createTime" slot-scope="text">
          {{ text ? (text.length > 19 ? text.substr(0, 19) : text) : '-' }}
        </span>
        <span slot="contractAmount" slot-scope="text">
          <span v-if="text" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
          <span v-else>-</span>
        </span>
        <span slot="orderStatus" slot-scope="text, record">
          <a-tag :color="getOrderStatusColor(record.orderStatus)">{{ getOrderStatusText(record) }}</a-tag>
        </span>
      </a-table>
    </a-spin>
  </a-modal>
</template>

<script>
import { getAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'CustomerOrderModal',
  data() {
    return {
      title: '复购信息',
      visible: false,
      confirmLoading: false,
      loading: false,
      customerId: '',
      companyName: '',
      orderList: [],
      customerTotalSpending: null, // 客户列表中的消费金额（后端已计算）
      columns: [
        {
          title: '订单编号',
          align: 'center',
          dataIndex: 'orderNo',
          width: 150,
          scopedSlots: { customRender: 'orderNo' },
        },
        {
          title: '下单时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 180,
          scopedSlots: { customRender: 'createTime' },
        },
        {
          title: '业务人员',
          align: 'center',
          dataIndex: 'salesman',
          width: 120,
        },
        {
          title: '业务类型',
          align: 'center',
          dataIndex: 'businessType',
          width: 150,
          customRender: (text, record) => {
            return record.businessType_dictText || record.businessTypeName || text || '-'
          },
        },
        {
          title: '合同金额',
          align: 'center',
          dataIndex: 'contractAmount',
          width: 120,
          scopedSlots: { customRender: 'contractAmount' },
        },
        {
          title: '订单状态',
          align: 'center',
          dataIndex: 'orderStatus',
          width: 120,
          scopedSlots: { customRender: 'orderStatus' },
        },
      ],
    }
  },
  computed: {
    // 显示的总金额：优先使用客户列表中的消费金额（后端已计算），如果没有则使用前端计算的金额
    displayTotalAmount() {
      // 如果客户列表中有消费金额（后端计算的），直接使用它，因为它包含了该客户的所有订单
      // 前端查询的订单可能因为数据权限过滤掉一部分，所以前端计算的金额可能不准确
      if (this.customerTotalSpending != null && this.customerTotalSpending !== undefined && this.customerTotalSpending !== '') {
        return parseFloat(this.customerTotalSpending) || 0
      }
      // 如果没有客户消费金额，使用前端计算的当前订单列表金额
      return this.totalAmount
    },
    // 当前订单列表的总金额（前端计算，可能因为数据权限过滤而不完整）
    totalAmount() {
      if (!this.orderList || this.orderList.length === 0) {
        return 0
      }
      // 与后端计算逻辑保持一致：优先使用contractAmount，如果为null则使用orderAmount
      // 注意：后端使用 != null 判断，所以0值会被计算，这里也要保持一致
      return this.orderList.reduce((sum, order) => {
        let amount = 0
        // 优先使用contractAmount（如果contractAmount不为null，即使为0也要使用）
        if (order.contractAmount != null) {
          amount = parseFloat(order.contractAmount) || 0
        } else if (order.orderAmount != null) {
          // 如果contractAmount为null，使用orderAmount
          amount = parseFloat(order.orderAmount) || 0
        }
        // 处理NaN情况
        if (isNaN(amount)) {
          amount = 0
        }
        return sum + amount
      }, 0)
    },
  },
  methods: {
    show(record) {
      this.visible = true
      this.customerId = record.id
      this.companyName = record.corporateName
      // 保存客户列表中的消费金额（后端已计算，包含所有订单）
      this.customerTotalSpending = record.totalSpending
      this.title = `复购信息（${record.corporateName}）`
      this.loadOrderList()
    },
    handleOk() {
      this.handleCancel()
    },
    handleCancel() {
      this.visible = false
      this.orderList = []
      this.customerId = ''
      this.companyName = ''
      this.customerTotalSpending = null
    },
    loadOrderList() {
      this.loading = true
      this.orderList = []
      
      // 优先使用customerId，如果没有则使用companyName
      const baseParams = {}
      if (this.customerId) {
        baseParams.companyId = this.customerId
      } else if (this.companyName) {
        baseParams.companyName = this.companyName
      }
      
      // 循环查询所有订单（因为可能有分页限制）
      this.loadAllOrders(baseParams, 1, [])
    },
    // 递归查询所有订单
    loadAllOrders(baseParams, pageNo, allOrders) {
      const params = {
        ...baseParams,
        pageNo: pageNo,
        pageSize: 1000, // 每次查询1000条
        ignoreDataPermission: true, // 忽略数据权限过滤，查询客户的所有订单
      }
      
      getAction('/order/list', params)
        .then((res) => {
          if (res.success && res.result && res.result.records) {
            const records = res.result.records || []
            allOrders = allOrders.concat(records)
            
            // 如果返回的记录数等于pageSize，说明可能还有更多数据，继续查询下一页
            if (records.length === 1000 && res.result.total > allOrders.length) {
              this.loadAllOrders(baseParams, pageNo + 1, allOrders)
            } else {
              // 所有订单已加载完成
              this.orderList = allOrders
              this.loading = false
            }
          } else {
            this.orderList = allOrders
            this.loading = false
          }
        })
        .catch((err) => {
          console.error('查询订单列表失败', err)
          this.$message.error('查询订单列表失败')
          this.orderList = allOrders
          this.loading = false
        })
    },
    handleOrderDetail(record) {
      // 跳转到订单列表页，并自动搜索该订单编号
      this.$router.push({
        path: '/order/list',
        query: { orderNo: record.orderNo },
      })
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getOrderStatusColor(status) {
      const colorMap = {
        '0': 'orange', // 待审核
        '1': 'blue',   // 进行中
        '2': 'green',  // 已完成
        '3': 'default', // 已取消
        '4': 'red'     // 已驳回
      }
      return colorMap[status] || 'default'
    },
    getOrderStatusText(record) {
      // 如果订单已完成，直接显示"已完成"
      if (record.orderStatus === '2') {
        return '已完成'
      }
      
      // 如果订单未完成（进行中），显示距离现在过了多少天
      // 使用第一个审核步骤通过的时间来计算天数，如果没有则使用创建时间
      if (record.orderStatus === '1') {
        try {
          // 优先使用第一个审核步骤通过的时间，如果没有则使用创建时间
          const startTime = record.firstAuditTime ? new Date(record.firstAuditTime) : (record.createTime ? new Date(record.createTime) : null)
          if (!startTime) {
            return '进行中'
          }
          
          const now = new Date()
          const diffTime = now.getTime() - startTime.getTime()
          const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
          
          if (diffDays < 0) {
            return '进行中'
          } else if (diffDays === 0) {
            return '进行中（今天）'
          } else {
            return `进行中（${diffDays}天）`
          }
        } catch (e) {
          console.error('计算订单天数失败:', e)
          return '进行中'
        }
      }
      
      // 其他状态使用字典文本或默认映射
      if (record.orderStatus_dictText) {
        return record.orderStatus_dictText
      }
      const statusMap = {
        '0': '待审核',
        '1': '进行中',
        '2': '已完成',
        '3': '已取消',
        '4': '已驳回'
      }
      return statusMap[record.orderStatus] || record.orderStatus || '-'
    },
    moment,
  },
}
</script>

<style scoped>
</style>

