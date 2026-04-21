<template>
  <div class="public-order-detail">
    <a-spin :spinning="loading">
      <div class="detail-container">
        <!-- 标题 -->
        <div class="detail-header">
          <h2>订单详情</h2>
        </div>

        <!-- 订单信息 -->
        <div class="detail-content" v-if="orderData">
          <a-descriptions bordered :column="1">
            <a-descriptions-item label="订单编号">
              {{ orderData.orderNo || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="公司名称">
              {{ orderData.companyName || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="业务类型">
              {{ orderData.businessTypeName || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="订单状态">
              <a-tag :color="getStatusColor(orderData.orderStatus)">
                {{ orderData.orderStatusName || '-' }}
              </a-tag>
            </a-descriptions-item>
            
            <!-- 金额信息 -->
            <a-descriptions-item label="订单金额">
              <span class="amount-text">{{ formatAmount(orderData.orderAmount) }}</span>
            </a-descriptions-item>
            
            <a-descriptions-item label="合同金额">
              <span class="amount-text">{{ formatAmount(orderData.contractAmount) }}</span>
            </a-descriptions-item>
            
            <a-descriptions-item label="尾款金额">
              <span class="amount-text">{{ formatAmount(orderData.finalPaymentAmount) }}</span>
            </a-descriptions-item>
            
            <a-descriptions-item label="收款金额">
              <span class="amount-text">{{ formatAmount(orderData.receivedAmount) }}</span>
            </a-descriptions-item>
            
            <!-- 联系信息 -->
            <a-descriptions-item label="业务员">
              {{ orderData.salesman || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="联系人">
              {{ orderData.contacts || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="联系方式">
              {{ orderData.contactInformation || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="所属区域">
              {{ orderData.region || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="详细地址">
              {{ orderData.address || '-' }}
            </a-descriptions-item>
            
            <!-- 其他信息 -->
            <a-descriptions-item label="交单方式">
              {{ orderData.deliveryMethod || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="收款时间">
              {{ orderData.collectionTime || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="收款账号">
              {{ orderData.collectionAccountNumber || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="创建时间">
              {{ orderData.createTime || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="创建人">
              {{ orderData.createBy || '-' }}
            </a-descriptions-item>
            
            <a-descriptions-item label="备注">
              {{ orderData.remarks || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 错误提示 -->
        <div class="detail-error" v-if="error">
          <a-result
            status="error"
            title="加载失败"
            :sub-title="error"
          />
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'

export default {
  name: 'PublicOrderDetail',
  data() {
    return {
      loading: false,
      orderData: null,
      error: null
    }
  },
  created() {
    this.loadOrderDetail()
  },
  methods: {
    /**
     * 加载订单详情
     */
    loadOrderDetail() {
      const orderId = this.$route.query.id
      if (!orderId) {
        this.error = '订单ID不能为空'
        return
      }

      this.loading = true
      this.error = null

      getAction('/public/order/detail', { id: orderId })
        .then(res => {
          if (res.success) {
            this.orderData = res.result
          } else {
            this.error = res.message || '加载订单详情失败'
          }
        })
        .catch(err => {
          console.error('加载订单详情失败', err)
          this.error = '加载订单详情失败，请稍后重试'
        })
        .finally(() => {
          this.loading = false
        })
    },

    /**
     * 获取状态颜色
     */
    getStatusColor(status) {
      const colorMap = {
        '0': 'orange',    // 待审核
        '1': 'blue',      // 审核中
        '2': 'green',     // 已通过
        '3': 'red',       // 已拒绝
        '4': 'cyan',      // 进行中
        '5': 'green',     // 已完成
        '6': 'default'    // 已取消
      }
      return colorMap[status] || 'default'
    },

    /**
     * 格式化金额
     */
    formatAmount(amount) {
      if (amount === null || amount === undefined || amount === '') {
        return '-'
      }
      // 转换为数字并格式化为千分位
      const num = parseFloat(amount)
      if (isNaN(num)) {
        return '-'
      }
      return '¥ ' + num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }
  }
}
</script>

<style lang="less" scoped>
.public-order-detail {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 24px;

  .detail-container {
    max-width: 800px;
    margin: 0 auto;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 24px;

    .detail-header {
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #e8e8e8;

      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
        color: #262626;
      }
    }

    .detail-content {
      /deep/ .ant-descriptions-item-label {
        width: 120px;
        font-weight: 500;
        background-color: #fafafa;
      }

      /deep/ .ant-descriptions-item-content {
        word-break: break-all;
      }

      .amount-text {
        font-size: 16px;
        font-weight: 500;
        color: #f5222d;
      }
    }

    .detail-error {
      padding: 40px 0;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .public-order-detail {
    padding: 12px;

    .detail-container {
      padding: 16px;

      .detail-header h2 {
        font-size: 20px;
      }

      .detail-content {
        /deep/ .ant-descriptions-item-label {
          width: 100px;
        }
      }
    }
  }
}
</style>
