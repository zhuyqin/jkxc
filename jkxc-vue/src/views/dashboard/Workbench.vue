<template>
  <div class="workbench">
    <a-card :bordered="false">
      <!-- 欢迎信息 -->
      <a-row style="margin-bottom: 24px">
        <a-col :span="24">
          <a-card :bordered="false" style="background: #1890ff; color: white">
            <h2 style="color: white; margin: 0; font-weight: 500">
              {{ timeFix }}，{{ username }}！
            </h2>
            <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0 0; font-size: 14px">
              今天是 {{ currentDate }}，祝您工作愉快！
            </p>
          </a-card>
        </a-col>
      </a-row>

      <!-- 我的统计卡片 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-icon" style="background: #fff1f0">
              <a-icon type="bell" style="color: #f5222d; font-size: 24px" />
            </div>
            <a-statistic
              title="待办一次性业务"
              :value="myStatistics.pendingOneTimeTasks"
              suffix="个"
              :value-style="{ color: '#262626', fontSize: '28px', fontWeight: '500' }"
            />
            <a-button type="link" @click="handleViewOneTimeTasks" style="padding: 0; margin-top: 8px; color: #1890ff">
              查看详情 <a-icon type="right" />
            </a-button>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-icon" style="background: #fff7e6">
              <a-icon type="clock-circle" style="color: #fa8c16; font-size: 24px" />
            </div>
            <a-statistic
              title="待办周期性业务"
              :value="myStatistics.pendingRecurringTasks"
              suffix="个"
              :value-style="{ color: '#262626', fontSize: '28px', fontWeight: '500' }"
            />
            <a-button type="link" @click="handleViewRecurringTasks" style="padding: 0; margin-top: 8px; color: #1890ff">
              查看详情 <a-icon type="right" />
            </a-button>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-icon" style="background: #e6f7ff">
              <a-icon type="team" style="color: #1890ff; font-size: 24px" />
            </div>
            <a-statistic
              title="我的客户"
              :value="myStatistics.myCustomers"
              suffix="家"
              :value-style="{ color: '#262626', fontSize: '28px', fontWeight: '500' }"
            />
            <a-button type="link" @click="handleViewCustomers" style="padding: 0; margin-top: 8px; color: #1890ff">
              查看详情 <a-icon type="right" />
            </a-button>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-icon" style="background: #f6ffed">
              <a-icon type="file-text" style="color: #52c41a; font-size: 24px" />
            </div>
            <a-statistic
              title="我的订单（本月）"
              :value="myStatistics.myOrders"
              suffix="单"
              :value-style="{ color: '#262626', fontSize: '28px', fontWeight: '500' }"
            />
            <a-button type="link" @click="handleViewOrders" style="padding: 0; margin-top: 8px; color: #1890ff">
              查看详情 <a-icon type="right" />
            </a-button>
          </a-card>
        </a-col>
      </a-row>

      <!-- 待办任务和快捷操作 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :span="12">
          <a-card title="待办任务" :bordered="false">
            <a-list
              :dataSource="pendingTasks"
              :loading="loading"
              :pagination="false"
            >
              <a-list-item slot="renderItem" slot-scope="item">
                <a-list-item-meta>
                  <a slot="title">{{ item.title }}</a>
                  <span slot="description">{{ formatDate(item.time) }}</span>
                </a-list-item-meta>
                <a-button type="link" size="small" @click="handleTask(item)">
                  处理
                </a-button>
              </a-list-item>
              <a-empty v-if="pendingTasks.length === 0" description="暂无待办任务" />
            </a-list>
            <div style="text-align: center; margin-top: 16px">
              <a-button type="link" @click="handleViewAllTasks">
                查看全部任务 <a-icon type="right" />
              </a-button>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="快捷操作" :bordered="false">
            <a-row :gutter="16">
              <a-col :span="12" v-for="action in quickActions" :key="action.name" style="margin-bottom: 16px">
                <a-button
                  type="primary"
                  block
                  size="large"
                  @click="handleQuickAction(action)"
                  class="quick-action-btn"
                >
                  <a-icon :type="action.icon" />
                  {{ action.name }}
                </a-button>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>

      <!-- 待处理订单和系统通知 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="待处理订单" :bordered="false">
            <a-table
              :columns="orderColumns"
              :dataSource="pendingOrders"
              :pagination="false"
              size="small"
              :loading="loading"
              :scroll="{ y: 300 }"
            >
              <template slot="amount" slot-scope="text">
                ¥{{ formatAmount(text) }}
              </template>
              <template slot="createTime" slot-scope="text">
                {{ formatDate(text) }}
              </template>
              <template slot="action" slot-scope="text, record">
                <a-button type="link" size="small" @click="handleViewOrder(record)">
                  查看
                </a-button>
              </template>
            </a-table>
            <div style="text-align: center; margin-top: 16px">
              <a-button type="link" @click="handleViewAllOrders">
                查看全部订单 <a-icon type="right" />
              </a-button>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="系统通知" :bordered="false">
            <a-list
              :dataSource="notifications"
              :loading="loading"
              :pagination="false"
            >
              <a-list-item slot="renderItem" slot-scope="item">
                <a-list-item-meta>
                  <a slot="title">{{ item.content }}</a>
                  <span slot="description">{{ formatDate(item.time) }}</span>
                </a-list-item-meta>
              </a-list-item>
              <a-empty v-if="notifications.length === 0" description="暂无通知" />
            </a-list>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'Workbench',
  data() {
    return {
      loading: false,
      username: '',
      currentDate: '',
      timeFix: '',
      myStatistics: {
        pendingTasks: 0,
        myCustomers: 0,
        myOrders: 0,
        myPerformance: 0,
      },
      pendingTasks: [],
      pendingOrders: [],
      quickActions: [],
      notifications: [],
      orderColumns: [
        {
          title: '订单号',
          dataIndex: 'orderNo',
          key: 'orderNo',
          width: 150,
        },
        {
          title: '客户名称',
          dataIndex: 'companyName',
          key: 'companyName',
        },
        {
          title: '金额',
          dataIndex: 'amount',
          key: 'amount',
          width: 120,
          scopedSlots: { customRender: 'amount' },
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          key: 'createTime',
          width: 150,
          scopedSlots: { customRender: 'createTime' },
        },
        {
          title: '操作',
          key: 'action',
          width: 80,
          scopedSlots: { customRender: 'action' },
        },
      ],
    }
  },
  mounted() {
    this.initTime()
    this.loadData()
  },
  methods: {
    initTime() {
      const hour = new Date().getHours()
      if (hour < 9) {
        this.timeFix = '早上好'
      } else if (hour < 12) {
        this.timeFix = '上午好'
      } else if (hour < 14) {
        this.timeFix = '中午好'
      } else if (hour < 17) {
        this.timeFix = '下午好'
      } else if (hour < 19) {
        this.timeFix = '傍晚好'
      } else {
        this.timeFix = '晚上好'
      }
      this.currentDate = moment().format('YYYY年MM月DD日 dddd')
    },
    async loadData() {
      this.loading = true
      try {
        // 获取用户信息
        const userInfo = this.$store.getters.userInfo
        if (userInfo && userInfo.realname) {
          this.username = userInfo.realname
        }

        const res = await getAction('/dashboard/getWorkbench')
        if (res.success && res.result) {
          this.myStatistics = res.result.myStatistics || this.myStatistics
          this.pendingTasks = res.result.pendingTasks || []
          this.pendingOrders = res.result.pendingOrders || []
          this.quickActions = res.result.quickActions || []
          this.notifications = res.result.notifications || []
        } else {
          this.$message.warning(res.message || '获取工作台数据失败')
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    handleQuickAction(action) {
      if (action.path) {
        this.$router.push(action.path)
      }
    },
    handleTask(task) {
      // 根据任务类型跳转到相应页面
      if (task.type === 'task') {
        this.$router.push({
          path: '/order/businessTask',
          query: { taskId: task.id },
        })
      }
    },
    handleViewOrder(record) {
      this.$router.push({
        path: '/order/ghOrder',
        query: { id: record.id },
      })
    },
    handleViewTasks() {
      this.$router.push('/order/businessTask')
    },
    handleViewOneTimeTasks() {
      // 跳转到一次性任务列表页面，默认显示待审核tab
      this.$router.push({
        path: '/system/oneTimeTaskList',
        query: { tab: 'pending' }
      })
    },
    handleViewRecurringTasks() {
      // 跳转到周期性任务列表页面，默认显示待审核tab
      this.$router.push({
        path: '/system/recurringTaskList',
        query: { tab: 'pending' }
      })
    },
    handleViewCustomers() {
      const userInfo = this.$store.getters.userInfo
      const userInfoFromLS = JSON.parse(localStorage.getItem('Login_Userinfo') || '{}')
      
      let salesman = ''
      if (userInfo && userInfo.realname) {
        salesman = userInfo.realname
      } else if (userInfoFromLS && userInfoFromLS.realname) {
        salesman = userInfoFromLS.realname
      } else if (this.username) {
        salesman = this.username
      }
      
      if (!salesman) {
        this.$message.warning('无法获取当前用户信息')
        this.$router.push('/customer/ghCustomer')
        return
      }
      
      this.$router.push({
        path: '/customer/ghCustomer',
        query: {
          salesman: salesman,
          _fromDashboard: '1'
        }
      })
    },
    handleViewOrders() {
      this.$router.push('/order/ghOrder')
    },
    handleViewPerformance() {
      this.$router.push('/operation-analysis/new-order-analysis')
    },
    handleViewAllTasks() {
      this.$router.push('/order/businessTask')
    },
    handleViewAllOrders() {
      this.$router.push('/order/ghOrder')
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      const num = typeof amount === 'string' ? parseFloat(amount) : amount
      if (num >= 10000) {
        return (num / 10000).toFixed(2) + '万'
      }
      return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    formatDate(date) {
      if (!date) return ''
      return moment(date).format('YYYY-MM-DD HH:mm')
    },
  },
}
</script>

<style lang="less" scoped>
.workbench {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .stat-card {
    position: relative;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
      transform: translateY(-2px);
    }

    .stat-icon {
      position: absolute;
      top: 16px;
      right: 16px;
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .quick-action-btn {
    height: 60px;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s;

    .anticon {
      font-size: 18px;
      margin-right: 8px;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
    }
  }
}
</style>

