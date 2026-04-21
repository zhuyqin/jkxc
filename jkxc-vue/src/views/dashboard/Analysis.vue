<template>
  <div class="analysis-container">
    <!-- admin角色显示数据统计页 -->
    <div v-if="isAdmin" class="dashboard-content">
      <a-card :bordered="false">
        <!-- 关键指标卡片 -->
        <a-row :gutter="16" style="margin-bottom: 24px">
          <a-col :span="4">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="今日订单"
                :value="statistics.todayOrders"
                suffix="单"
                :value-style="{ color: '#1890ff', fontSize: '24px' }"
              />
              <div style="margin-top: 8px; color: #999; font-size: 12px">
                金额：¥{{ formatAmount(statistics.todayAmount) }}
              </div>
            </a-card>
          </a-col>
          <a-col :span="4">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="本月订单"
                :value="statistics.monthOrders"
                suffix="单"
                :value-style="{ color: '#52c41a', fontSize: '24px' }"
              />
              <div style="margin-top: 8px; color: #999; font-size: 12px">
                金额：¥{{ formatAmount(statistics.monthAmount) }}
              </div>
            </a-card>
          </a-col>
          <a-col :span="4">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="本年订单"
                :value="statistics.yearOrders"
                suffix="单"
                :value-style="{ color: '#fa8c16', fontSize: '24px' }"
              />
              <div style="margin-top: 8px; color: #999; font-size: 12px">
                金额：¥{{ formatAmount(statistics.yearAmount) }}
              </div>
            </a-card>
          </a-col>
          <a-col :span="4">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="客户总数"
                :value="statistics.totalCustomers"
                suffix="家"
                :value-style="{ color: '#722ed1', fontSize: '24px' }"
              />
            </a-card>
          </a-col>
          <a-col :span="4">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="任务完成率"
                :value="statistics.taskCompletionRate"
                suffix="%"
                :precision="2"
                :value-style="{ color: '#13c2c2', fontSize: '24px' }"
              />
            </a-card>
          </a-col>
          <a-col :span="4">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="业务员数量"
                :value="statistics.salesmanCount"
                suffix="人"
                :value-style="{ color: '#eb2f96', fontSize: '24px' }"
              />
            </a-card>
          </a-col>
        </a-row>

        <!-- 线索奖金统计 -->
        <a-row :gutter="16" style="margin-bottom: 24px">
          <a-col :span="8">
            <a-card :bordered="false" :loading="loadingStates.statistics">
              <a-statistic
                title="线索奖金（本月）"
                :value="statistics.clueBonusThisMonthAmount"
                prefix="¥"
                :precision="2"
                :value-style="{ color: '#f5222d', fontSize: '24px' }"
              />
              <div style="margin-top: 8px; color: #999; font-size: 12px">
                发放线索数：{{ statistics.clueBonusThisMonthCount }}
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 月度订单趋势 -->
        <a-row :gutter="16" style="margin-bottom: 24px">
          <a-col :span="24">
            <a-card title="月度订单趋势" :bordered="false" :loading="loadingStates.monthlyTrend">
              <div ref="monthlyTrendChart" style="width: 100%; height: 400px"></div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 业务类型分布和业务员排名 -->
        <a-row :gutter="16" style="margin-bottom: 24px">
          <a-col :span="12">
            <a-card title="业务类型分布" :bordered="false" :loading="loadingStates.businessType">
              <div ref="businessTypeChart" style="width: 100%; height: 350px"></div>
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card title="业务员排名（Top 10）" :bordered="false" :loading="loadingStates.salesmanRank">
              <div ref="salesmanRankChart" style="width: 100%; height: 350px"></div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 最近订单和高价值客户 -->
        <a-row :gutter="16">
          <a-col :span="14">
            <a-card title="最近订单（Top 10）" :bordered="false" :loading="loadingStates.recentOrders">
              <a-table
                :columns="orderColumns"
                :dataSource="recentOrders"
                :pagination="false"
                size="small"
                :scroll="{ y: 300, x: 'max-content' }"
              >
                <template slot="amount" slot-scope="text">
                  ¥{{ formatAmount(text) }}
                </template>
                <template slot="createTime" slot-scope="text">
                  {{ formatDate(text) }}
                </template>
              </a-table>
            </a-card>
          </a-col>
          <a-col :span="10">
            <a-card title="高价值客户（Top 10）" :bordered="false" :loading="loadingStates.topCustomers">
              <a-table
                :columns="customerColumns"
                :dataSource="topCustomers"
                :pagination="false"
                size="small"
                :scroll="{ y: 300 }"
              >
                <template slot="value" slot-scope="text">
                  ¥{{ formatAmount(text) }}
                </template>
              </a-table>
            </a-card>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 其他角色显示工作台 -->
    <div v-else-if="isEmployee" class="workbench-content">
      <a-card :bordered="false">
        <!-- 欢迎信息 -->
        <a-row style="margin-bottom: 24px">
          <a-col :span="24">
            <a-card :bordered="false" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white">
              <h2 style="color: white; margin: 0">
                {{ timeFix }}，{{ username }}！
              </h2>
              <p style="color: rgba(255,255,255,0.8); margin: 8px 0 0 0">
                今天是 {{ currentDate }}，祝您工作愉快！
              </p>
            </a-card>
          </a-col>
        </a-row>

        <!-- 我的统计卡片 -->
        <a-row :gutter="16" style="margin-bottom: 24px">
          <a-col :span="6">
            <a-card :bordered="false" :loading="workbenchLoadingStates.myStatistics">
              <a-statistic
                title="待办一次性业务"
                :value="myStatistics.pendingOneTimeTasks"
                suffix="个"
                :value-style="{ color: '#f5222d', fontSize: '28px' }"
              />
              <a-button type="link" @click="handleViewOneTimeTasks" style="padding: 0; margin-top: 8px">
                查看详情 <a-icon type="right" />
              </a-button>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" :loading="workbenchLoadingStates.myStatistics">
              <a-statistic
                title="待办周期性业务"
                :value="myStatistics.pendingRecurringTasks"
                suffix="个"
                :value-style="{ color: '#fa8c16', fontSize: '28px' }"
              />
              <a-button type="link" @click="handleViewRecurringTasks" style="padding: 0; margin-top: 8px">
                查看详情 <a-icon type="right" />
              </a-button>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" :loading="workbenchLoadingStates.myStatistics">
              <a-statistic
                title="我的客户"
                :value="myStatistics.myCustomers"
                suffix="家"
                :value-style="{ color: '#1890ff', fontSize: '28px' }"
              />
              <a-button type="link" @click="handleViewCustomers" style="padding: 0; margin-top: 8px">
                查看详情 <a-icon type="right" />
              </a-button>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" :loading="workbenchLoadingStates.myStatistics">
              <a-statistic
                title="我的订单（本月）"
                :value="myStatistics.myOrders"
                suffix="单"
                :value-style="{ color: '#52c41a', fontSize: '28px' }"
              />
              <a-button type="link" @click="handleViewOrders" style="padding: 0; margin-top: 8px">
                查看详情 <a-icon type="right" />
              </a-button>
            </a-card>
          </a-col>
        </a-row>

        <!-- 待办任务和快捷操作 -->
        <a-row :gutter="16" style="margin-bottom: 24px">
          <a-col :span="16">
            <a-card title="待办任务" :bordered="false" :loading="workbenchLoadingStates.pendingTasks">
              <a-tabs default-active-key="once" size="small">
                <a-tab-pane key="once" tab="一次性任务">
                  <a-list
                    :dataSource="pendingTasks.oneTime"
                    :pagination="false"
                  >
                    <a-list-item slot="renderItem" slot-scope="item" :key="item.id">
                      <a-list-item-meta>
                        <a slot="title">
                          <span v-if="item.companyName" style="font-weight: 500; color: #1890ff">
                            {{ item.companyName }}
                          </span>
                          <span v-else-if="item.orderNo" style="font-weight: 500; color: #1890ff">
                            订单：{{ item.orderNo }}
                          </span>
                          <span v-else>{{ item.title }}</span>
                        </a>
                        <span slot="description">
                          <div v-if="item.orderNo" style="margin-top: 4px; color: #666">
                            <a-icon type="file-text" style="margin-right: 4px" />
                            <span>订单号：{{ item.orderNo }}</span>
                          </div>
                          <div v-if="item.orderCreateTime" style="margin-top: 4px">
                            <a-icon type="calendar" style="margin-right: 4px" />
                            <span>订单时间：{{ formatDate(item.orderCreateTime) }}</span>
                          </div>
                          <div style="margin-top: 4px; color: #999">
                            <a-icon type="clock-circle" style="margin-right: 4px" />
                            <span>任务时间：{{ formatDate(item.time) }}</span>
                          </div>
                        </span>
                      </a-list-item-meta>
                      <a-button type="link" size="small" @click="handleTask(item, 'once')">
                        处理
                      </a-button>
                    </a-list-item>
                    <template v-if="!pendingTasks.oneTime || pendingTasks.oneTime.length === 0">
                      <a-empty description="暂无一次性待办任务" />
                    </template>
                  </a-list>
                  <div style="text-align: center; margin-top: 16px">
                    <a-button type="link" @click="handleViewOneTimeTasks">
                      查看全部一次性任务 <a-icon type="right" />
                    </a-button>
                  </div>
                </a-tab-pane>
                <a-tab-pane key="recurring" tab="周期任务">
                  <a-list
                    :dataSource="pendingTasks.recurring"
                    :pagination="false"
                  >
                    <a-list-item slot="renderItem" slot-scope="item" :key="item.id">
                      <a-list-item-meta>
                        <a slot="title">
                          <span v-if="item.companyName" style="font-weight: 500; color: #1890ff">
                            {{ item.companyName }}
                          </span>
                          <span v-else-if="item.orderNo" style="font-weight: 500; color: #1890ff">
                            订单：{{ item.orderNo }}
                          </span>
                          <span v-else>{{ item.title }}</span>
                        </a>
                        <span slot="description">
                          <div v-if="item.orderNo" style="margin-top: 4px; color: #666">
                            <a-icon type="file-text" style="margin-right: 4px" />
                            <span>订单号：{{ item.orderNo }}</span>
                          </div>
                          <div v-if="item.orderCreateTime" style="margin-top: 4px">
                            <a-icon type="calendar" style="margin-right: 4px" />
                            <span>订单时间：{{ formatDate(item.orderCreateTime) }}</span>
                          </div>
                          <div style="margin-top: 4px; color: #999">
                            <a-icon type="clock-circle" style="margin-right: 4px" />
                            <span>任务时间：{{ formatDate(item.time) }}</span>
                          </div>
                        </span>
                      </a-list-item-meta>
                      <a-button type="link" size="small" @click="handleTask(item, 'recurring')">
                        处理
                      </a-button>
                    </a-list-item>
                    <template v-if="!pendingTasks.recurring || pendingTasks.recurring.length === 0">
                      <a-empty description="暂无周期待办任务" />
                    </template>
                  </a-list>
                  <div style="text-align: center; margin-top: 16px">
                    <a-button type="link" @click="handleViewRecurringTasks">
                      查看全部周期任务 <a-icon type="right" />
                    </a-button>
                  </div>
                </a-tab-pane>
              </a-tabs>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="快捷操作" :bordered="false" :loading="workbenchLoadingStates.quickActions">
              <a-row :gutter="16">
                <a-col :span="12" v-for="action in quickActions" :key="action.name" style="margin-bottom: 16px">
                  <a-button
                    type="primary"
                    block
                    size="large"
                    @click="handleQuickAction(action)"
                    style="height: 60px; font-size: 16px"
                  >
                    <a-icon :type="action.icon" style="font-size: 20px; margin-right: 8px" />
                    {{ action.name }}
                  </a-button>
                </a-col>
              </a-row>
            </a-card>
          </a-col>
        </a-row>

        <!-- 我的订单和系统通知 -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-card title="我的订单（最近）" :bordered="false" :loading="workbenchLoadingStates.pendingOrders">
              <a-table
                :columns="workbenchOrderColumns"
                :dataSource="pendingOrders"
                :pagination="false"
                size="small"
                :scroll="{ y: 300, x: 770 }"
                bordered
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

    <!-- 如果角色判断中，显示加载状态 -->
    <div v-else-if="roleChecking" class="loading-container">
      <a-spin size="large" tip="加载中...">
        <div style="min-height: 400px"></div>
      </a-spin>
    </div>
    <!-- 如果无法判断角色，默认显示工作台 -->
    <div v-else class="workbench-content">
      <a-card :bordered="false">
        <a-empty description="无法获取用户角色信息，请刷新页面重试" />
      </a-card>
    </div>
  </div>
</template>

<script>
  import { getAction } from '@/api/manage'
  import moment from 'moment'
  import * as echarts from 'echarts'

  export default {
    name: "Analysis",
    data() {
      return {
        isAdmin: false,
        isEmployee: false,
        roleChecking: true, // 角色判断中，避免显示图片轮播
        loading: false,
        // 管理员首页 - 各模块独立loading
        loadingStates: {
          statistics: false,
          monthlyTrend: false,
          businessType: false,
          salesmanRank: false,
          recentOrders: false,
          topCustomers: false,
        },
        // 员工首页 - 各模块独立loading
        workbenchLoadingStates: {
          myStatistics: false,
          pendingTasks: false,
          pendingOrders: false,
          quickActions: false,
        },
        username: '',
        currentDate: '',
        timeFix: '',
        // Dashboard 数据
        statistics: {
          todayOrders: 0,
          todayAmount: 0,
          monthOrders: 0,
          monthAmount: 0,
          yearOrders: 0,
          yearAmount: 0,
          totalCustomers: 0,
          taskCompletionRate: 0,
          salesmanCount: 0,
          clueBonusThisMonthCount: 0,
          clueBonusThisMonthAmount: 0,
        },
        monthlyTrend: [],
        businessTypeDistribution: [],
        salesmanRank: [],
        recentOrders: [],
        topCustomers: [],
        charts: {
          monthlyTrend: null,
          businessType: null,
          salesmanRank: null,
        },
        orderColumns: [
          {
            title: '订单号',
            dataIndex: 'orderNo',
            key: 'orderNo',
            width: 130,
          },
          {
            title: '客户名称',
            dataIndex: 'companyName',
            key: 'companyName',
            width: 300,
            ellipsis: {
              showTitle: true,
            },
          },
          {
            title: '金额',
            dataIndex: 'amount',
            key: 'amount',
            width: 120,
            align: 'right',
            scopedSlots: { customRender: 'amount' },
          },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 160,
            scopedSlots: { customRender: 'createTime' },
          },
        ],
        customerColumns: [
          {
            title: '客户名称',
            dataIndex: 'companyName',
            key: 'companyName',
          },
          {
            title: '客户价值',
            dataIndex: 'value',
            key: 'value',
            width: 150,
            scopedSlots: { customRender: 'value' },
          },
        ],
        // Workbench 数据
        myStatistics: {
          pendingTasks: 0,
          myCustomers: 0,
          myOrders: 0,
          myPerformance: 0,
        },
        pendingTasks: {
          oneTime: [],
          recurring: []
        },
        menuRoutes: {}, // 存储从菜单中获取的路由映射
        pendingOrders: [],
        quickActions: [],
        notifications: [],
        workbenchOrderColumns: [
          {
            title: '订单号',
            dataIndex: 'orderNo',
            key: 'orderNo',
            width: 130,
            fixed: 'left',
          },
          {
            title: '客户名称',
            dataIndex: 'companyName',
            key: 'companyName',
            width: 280,
            ellipsis: {
              showTitle: true,
            },
          },
          {
            title: '金额',
            dataIndex: 'amount',
            key: 'amount',
            width: 120,
            align: 'right',
            scopedSlots: { customRender: 'amount' },
          },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 160,
            scopedSlots: { customRender: 'createTime' },
          },
          {
            title: '操作',
            key: 'action',
            width: 80,
            fixed: 'right',
            scopedSlots: { customRender: 'action' },
          },
        ],
      }
    },
    created() {
      // 延迟执行，确保用户信息已加载
      this.$nextTick(() => {
        this.checkUserRole()
        // 加载菜单路由
        this.loadMenuRoutes()
      })
    },
    mounted() {
      // 如果角色判断还未完成，等待一下
      if (!this.isAdmin && !this.isEmployee) {
        // 延迟执行，确保角色判断完成
        setTimeout(() => {
          if (this.isAdmin) {
            this.initCharts()
            this.loadDashboardData()
          } else if (this.isEmployee) {
            this.initTime()
            this.loadWorkbenchData()
          }
        }, 100)
      } else {
        if (this.isAdmin) {
          this.initCharts()
          this.loadDashboardData()
        } else if (this.isEmployee) {
          this.initTime()
          this.loadWorkbenchData()
        }
      }
      window.addEventListener('resize', this.handleResize)
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.handleResize)
      Object.values(this.charts).forEach((chart) => {
        if (chart) {
          chart.dispose()
        }
      })
    },
    methods: {
      async checkUserRole() {
        this.roleChecking = true
        try {
          await this.checkRoleByAPI()
          
          this.$nextTick(() => {
            if (this.isAdmin) {
              this.initCharts()
              this.loadDashboardData()
            } else if (this.isEmployee) {
              this.initTime()
              this.loadWorkbenchData()
            }
            this.roleChecking = false
          })
        } catch (error) {
          console.error('获取用户角色失败:', error)
          this.isAdmin = false
          this.isEmployee = true
          this.roleChecking = false
          this.$nextTick(() => {
            this.initTime()
            this.loadWorkbenchData()
          })
        }
      },
      async checkRoleByAPI() {
        try {
          const userInfo = this.$store.getters.userInfo || JSON.parse(localStorage.getItem('Login_Userinfo') || '{}')
          const userId = userInfo.id || userInfo.userId
          
          if (userId) {
            const res = await getAction('/sys/user/queryUserRole', { userid: userId })
            
            if (res.success && res.result && res.result.length > 0) {
              const roleIds = Array.isArray(res.result) ? res.result : [res.result]
              const roleCodes = []
              
              for (const roleId of roleIds) {
                try {
                  const roleRes = await getAction('/sys/role/queryById', { id: roleId })
                  
                  if (roleRes.success && roleRes.result) {
                    const roleCode = roleRes.result.roleCode || roleRes.result.role_code
                    if (roleCode) {
                      roleCodes.push(roleCode)
                    }
                  }
                } catch (e) {
                  console.warn(`获取角色ID ${roleId} 信息失败:`, e)
                }
              }
              
              this.isAdmin = roleCodes.some(code => {
                const codeStr = code ? code.toString().toLowerCase() : ''
                return codeStr === 'admin'
              })
              this.isEmployee = !this.isAdmin && roleCodes.length > 0
            } else {
              this.isAdmin = false
              this.isEmployee = true
            }
          } else {
            this.isAdmin = false
            this.isEmployee = true
          }
        } catch (error) {
          console.error('通过API获取角色失败:', error)
          this.isAdmin = false
          this.isEmployee = true
        } finally {
          this.roleChecking = false
        }
      },
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
      initCharts() {
        this.$nextTick(() => {
          if (this.$refs.monthlyTrendChart) {
            this.charts.monthlyTrend = echarts.init(this.$refs.monthlyTrendChart)
          }
          if (this.$refs.businessTypeChart) {
            this.charts.businessType = echarts.init(this.$refs.businessTypeChart)
          }
          if (this.$refs.salesmanRankChart) {
            this.charts.salesmanRank = echarts.init(this.$refs.salesmanRankChart)
          }
        })
      },
      handleResize() {
        Object.values(this.charts).forEach((chart) => {
          if (chart) {
            chart.resize()
          }
        })
      },
      async loadDashboardData() {
        // 设置所有模块为loading状态
        Object.keys(this.loadingStates).forEach(key => {
          this.loadingStates[key] = true
        })
        
        try {
          const res = await getAction('/dashboard/getStatistics')
          if (res.success && res.result) {
            this.statistics = res.result.statistics || this.statistics
            this.loadingStates.statistics = false
            
            this.monthlyTrend = res.result.monthlyTrend || []
            this.loadingStates.monthlyTrend = false
            
            this.businessTypeDistribution = res.result.businessTypeDistribution || []
            this.loadingStates.businessType = false
            
            this.salesmanRank = res.result.salesmanRank || []
            this.loadingStates.salesmanRank = false
            
            this.recentOrders = res.result.recentOrders || []
            this.loadingStates.recentOrders = false
            
            this.topCustomers = res.result.topCustomers || []
            this.loadingStates.topCustomers = false
            
            this.updateCharts()
          } else {
            this.$message.warning(res.message || '获取统计数据失败')
            // 失败时也要取消loading
            Object.keys(this.loadingStates).forEach(key => {
              this.loadingStates[key] = false
            })
          }
        } catch (error) {
          console.error('加载数据失败:', error)
          this.$message.error('加载数据失败')
          // 失败时也要取消loading
          Object.keys(this.loadingStates).forEach(key => {
            this.loadingStates[key] = false
          })
        }
      },
      async loadWorkbenchData() {
        // 设置所有模块为loading状态
        Object.keys(this.workbenchLoadingStates).forEach(key => {
          this.workbenchLoadingStates[key] = true
        })
        
        try {
          const userInfo = this.$store.getters.userInfo
          if (userInfo && userInfo.realname) {
            this.username = userInfo.realname
          }

          // 确保菜单路由已加载
          if (Object.keys(this.menuRoutes).length === 0) {
            await this.loadMenuRoutes()
          }

          const res = await getAction('/dashboard/getWorkbench')
          if (res.success && res.result) {
            this.myStatistics = res.result.myStatistics || this.myStatistics
            this.workbenchLoadingStates.myStatistics = false
            
            // 处理分类的待办任务
            if (res.result.pendingTasks) {
              const pendingTasksData = res.result.pendingTasks
              
              if (Array.isArray(pendingTasksData)) {
                this.$set(this, 'pendingTasks', { oneTime: [], recurring: [] })
              } else {
                const oneTimeList = Array.isArray(pendingTasksData.oneTime) ? pendingTasksData.oneTime : 
                                  (Array.isArray(pendingTasksData.one_time) ? pendingTasksData.one_time : [])
                const recurringList = Array.isArray(pendingTasksData.recurring) ? pendingTasksData.recurring : 
                                    (Array.isArray(pendingTasksData.recurring_task) ? pendingTasksData.recurring_task : [])
                
                this.$set(this, 'pendingTasks', {
                  oneTime: oneTimeList,
                  recurring: recurringList
                })
              }
            } else {
              this.pendingTasks = { oneTime: [], recurring: [] }
            }
            this.workbenchLoadingStates.pendingTasks = false
            
            this.pendingOrders = res.result.pendingOrders || []
            this.workbenchLoadingStates.pendingOrders = false
            
            // 从菜单中获取快捷操作的正确路由
            this.quickActions = this.buildQuickActions(res.result.quickActions || [])
            this.workbenchLoadingStates.quickActions = false
            
            this.notifications = res.result.notifications || []
          } else {
            this.$message.warning(res.message || '获取工作台数据失败')
            // 失败时也要取消loading
            Object.keys(this.workbenchLoadingStates).forEach(key => {
              this.workbenchLoadingStates[key] = false
            })
          }
        } catch (error) {
          console.error('加载数据失败:', error)
          this.$message.error('加载数据失败')
          // 失败时也要取消loading
          Object.keys(this.workbenchLoadingStates).forEach(key => {
            this.workbenchLoadingStates[key] = false
          })
        }
      },
      updateCharts() {
        this.updateMonthlyTrendChart()
        this.updateBusinessTypeChart()
        this.updateSalesmanRankChart()
      },
      updateMonthlyTrendChart() {
        if (!this.charts.monthlyTrend) return
        const data = this.monthlyTrend || []
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
            },
          },
          legend: {
            data: ['订单数', '订单金额'],
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true,
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: data.map((item) => item.month),
          },
          yAxis: [
            {
              type: 'value',
              name: '订单数',
              position: 'left',
            },
            {
              type: 'value',
              name: '订单金额',
              position: 'right',
              axisLabel: {
                formatter: (value) => {
                  if (value >= 10000) {
                    return (value / 10000).toFixed(1) + '万'
                  }
                  return value
                },
              },
            },
          ],
          series: [
            {
              name: '订单数',
              type: 'line',
              data: data.map((item) => item.orders),
              smooth: true,
              itemStyle: {
                color: '#1890ff',
              },
            },
            {
              name: '订单金额',
              type: 'line',
              yAxisIndex: 1,
              data: data.map((item) => item.amount),
              smooth: true,
              itemStyle: {
                color: '#52c41a',
              },
            },
          ],
        }
        this.charts.monthlyTrend.setOption(option)
      },
      updateBusinessTypeChart() {
        if (!this.charts.businessType) return
        const data = this.businessTypeDistribution || []
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: data.map((item) => item.name),
          },
          series: [
            {
              name: '业务类型',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: data.map((item) => ({
                value: item.count,
                name: item.name,
              })),
            },
          ],
        }
        this.charts.businessType.setOption(option)
      },
      updateSalesmanRankChart() {
        if (!this.charts.salesmanRank) return
        const data = this.salesmanRank || []
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow',
            },
          },
          grid: {
            left: '15%',
            right: '10%',
            bottom: '3%',
            top: '5%',
            containLabel: false,
          },
          xAxis: {
            type: 'value',
          },
          yAxis: {
            type: 'category',
            data: data.map((item) => item.salesman),
            inverse: true,
          },
          series: [
            {
              name: '订单数',
              type: 'bar',
              data: data.map((item) => item.orders),
              barWidth: '60%',
              itemStyle: {
                borderRadius: [0, 4, 4, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#83bff6' },
                  { offset: 0.5, color: '#188df0' },
                  { offset: 1, color: '#0050b3' },
                ]),
              },
              label: {
                show: true,
                position: 'right',
                color: '#1890ff',
                fontWeight: 'bold',
              },
            },
          ],
        }
        this.charts.salesmanRank.setOption(option)
      },
      handleQuickAction(action) {
        if (action.path) {
          this.$router.push(action.path)
        }
      },
      // 从菜单数据中加载路由映射
      async loadMenuRoutes() {
        try {
          // 如果权限列表为空，尝试从store获取
          let permissionList = this.$store.getters.permissionList || []
          
          // 如果还是为空，等待一下再试
          if (permissionList.length === 0) {
            await new Promise(resolve => setTimeout(resolve, 100))
            permissionList = this.$store.getters.permissionList || []
          }
          
          const routeMap = {}
          
          // 递归查找菜单中的路由
          const findRoutes = (menuList) => {
            if (!menuList || !Array.isArray(menuList)) return
            for (const menu of menuList) {
              if (menu.path && menu.meta && menu.meta.title) {
                // 存储路由映射，支持通过标题查找
                const title = menu.meta.title
                routeMap[title] = menu.path
                
                // 也通过组件名存储（去掉views/前缀，转换为路径格式）
                if (menu.component) {
                  let componentPath = menu.component
                  // 如果component是类似 "system/OneTimeTaskList"，转换为路径
                  if (componentPath.includes('/')) {
                    const parts = componentPath.split('/')
                    const componentName = parts[parts.length - 1]
                    // 存储组件名到路径的映射
                    routeMap[componentName] = menu.path
                  }
                }
              }
              if (menu.children && menu.children.length > 0) {
                findRoutes(menu.children)
              }
            }
          }
          
          findRoutes(permissionList)
          this.menuRoutes = routeMap
        } catch (error) {
          console.error('加载菜单路由失败:', error)
          this.menuRoutes = {}
        }
      },
      // 构建快捷操作，使用菜单中的路由
      buildQuickActions(backendActions) {
        // 定义操作名称到可能的菜单标题的映射
        const actionRouteMap = {
          '创建订单': ['订单管理', '订单', 'GhOrder', 'ghOrder'],
          '添加客户': ['客户管理', '客户', 'GhCustomer', 'ghCustomer'],
          '创建任务': ['任务管理', '任务', 'BusinessTask', 'businessTask'],
          '查看报表': ['订单分析', '新订单分析', 'NewOrderAnalysis', 'new-order-analysis']
        }
        
        return backendActions.map(action => {
          // 尝试从菜单路由中查找
          let routePath = action.path
          
          // 根据操作名称查找对应的菜单标题
          const menuTitles = actionRouteMap[action.name] || []
          for (const title of menuTitles) {
            if (this.menuRoutes[title]) {
              routePath = this.menuRoutes[title]
              // 创建订单和添加客户跳转到列表页面，不添加/add后缀
              // 其他操作如果是添加/创建，添加/add后缀
              if ((action.name === '创建订单' || action.name === '添加客户')) {
                // 跳转到列表页面，不添加/add
                routePath = routePath.replace(/\/add$/, '')
              } else if (action.name.includes('创建') || action.name.includes('添加')) {
                routePath = routePath.endsWith('/add') ? routePath : routePath + '/add'
              }
              break
            }
          }
          
          // 如果没找到，使用后端返回的默认路径
          if (!routePath || routePath === action.path) {
            routePath = action.path
          }
          
          return {
            ...action,
            path: routePath
          }
        })
      },
      async handleTask(task, taskType) {
        if (Object.keys(this.menuRoutes).length === 0) {
          await this.loadMenuRoutes()
        }
        
        const query = {}
        
        if (taskType === 'recurring') {
          let routePath = this.menuRoutes['周期任务'] || 
                         this.menuRoutes['RecurringTaskList'] || 
                         '/system/recurringTaskList'
          
          if (task.orderNo) {
            query.orderNo = task.orderNo
          }
          query.tab = 'pending'
          
          this.$router.push({
            path: routePath,
            query: query,
          }).catch(err => {
            console.error('路由跳转失败:', err)
          })
        } else if (taskType === 'once') {
          let routePath = this.menuRoutes['一次性任务'] || 
                         this.menuRoutes['OneTimeTaskList'] || 
                         '/system/oneTimeTaskList'
          
          if (task.orderNo) {
            query.orderNo = task.orderNo
          }
          query.tab = 'pending'
          
          this.$router.push({
            path: routePath,
            query: query,
          }).catch(err => {
            console.error('路由跳转失败:', err)
          })
        } else {
          let routePath = this.menuRoutes['任务中心'] || 
                         this.menuRoutes['工商任务'] || 
                         this.menuRoutes['任务管理'] ||
                         this.menuRoutes['BusinessTaskList'] || 
                         this.menuRoutes['businessTask'] ||
                         '/order/businessTask'
          
          if (task.orderNo) {
            query.orderNo = task.orderNo
          }
          query.tab = 'task'
          
          this.$router.push({
            path: routePath,
            query: query,
          }).catch(err => {
            console.error('路由跳转失败:', err)
          })
        }
      },
      handleViewOneTimeTasks() {
        const routePath = this.menuRoutes['一次性任务'] || 
                         this.menuRoutes['OneTimeTaskList'] || 
                         '/system/oneTimeTaskList'
        this.$router.push({
          path: routePath,
          query: { tab: 'pending' }
        })
      },
      handleViewRecurringTasks() {
        const routePath = this.menuRoutes['周期任务'] || 
                         this.menuRoutes['RecurringTaskList'] || 
                         '/system/recurringTaskList'
        this.$router.push({
          path: routePath,
          query: { tab: 'pending' }
        })
      },
      async handleViewOrder(record) {
        // 确保菜单路由已加载
        if (Object.keys(this.menuRoutes).length === 0) {
          await this.loadMenuRoutes()
        }
        
        // 从菜单路由中获取订单管理的路径
        let routePath = this.menuRoutes['订单管理'] || 
                        this.menuRoutes['订单'] || 
                        this.menuRoutes['GhOrder'] || 
                        this.menuRoutes['ghOrder'] ||
                        null
        
        // 如果菜单路由中没有找到，尝试从权限列表中查找
        if (!routePath) {
          const permissionList = this.$store.getters.permissionList || []
          const findRoute = (menuList) => {
            for (const menu of menuList) {
              if (menu.path && menu.meta && menu.meta.title) {
                const title = menu.meta.title
                // 检查是否是订单相关的菜单
                if (title.includes('订单') || title.includes('Order') || 
                    (menu.component && menu.component.includes('GhOrder'))) {
                  return menu.path
                }
              }
              if (menu.children && menu.children.length > 0) {
                const childRoute = findRoute(menu.children)
                if (childRoute) return childRoute
              }
            }
            return null
          }
          routePath = findRoute(permissionList)
        }
        
        // 如果还是没找到，使用默认路径
        if (!routePath) {
          routePath = '/order/ghOrder'
        }
        
        this.$router.push({
          path: routePath,
          query: { id: record.id },
        }).catch(err => {
          console.error('路由跳转失败:', err, { path: routePath })
          this.$message.error('无法跳转到订单页面，路径：' + routePath)
        })
      },
      async handleViewTasks() {
        // 确保菜单路由已加载
        if (Object.keys(this.menuRoutes).length === 0) {
          await this.loadMenuRoutes()
        }
        
        const routePath = this.menuRoutes['任务中心'] || 
                         this.menuRoutes['工商任务'] || 
                         this.menuRoutes['任务管理'] ||
                         '/order/businessTask'
        
        this.$router.push(routePath).catch(err => {
          console.error('路由跳转失败:', err)
        })
      },
      async handleViewCustomers() {
        if (Object.keys(this.menuRoutes).length === 0) {
          await this.loadMenuRoutes()
        }
        
        const routePath = this.menuRoutes['客户管理'] || 
                         this.menuRoutes['客户'] || 
                         this.menuRoutes['GhCustomer'] ||
                         '/customer/ghCustomer'
        
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
          return
        }
        
        if (this.$route.path === routePath) {
          this.$router.replace({
            path: routePath,
            query: {
              salesman: salesman,
              _fromDashboard: '1',
              _t: Date.now()
            }
          }).catch(err => {
            console.error('路由刷新失败:', err)
          })
        } else {
          this.$router.push({
            path: routePath,
            query: {
              salesman: salesman,
              _fromDashboard: '1'
            }
          }).catch(err => {
            console.error('路由跳转失败:', err)
          })
        }
      },
      async handleViewOrders() {
        // 确保菜单路由已加载
        if (Object.keys(this.menuRoutes).length === 0) {
          await this.loadMenuRoutes()
        }
        
        const routePath = this.menuRoutes['订单管理'] || 
                         this.menuRoutes['订单'] || 
                         this.menuRoutes['GhOrder'] ||
                         '/order/ghOrder'
        
        this.$router.push(routePath).catch(err => {
          console.error('路由跳转失败:', err)
        })
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
      }
    }
  }
</script>

<style scoped>
  .analysis-container {
    width: 100%;
    margin: 0;
    padding: 0;
    min-height: calc(100vh - 64px);
  }

  .dashboard-content,
  .workbench-content {
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
    min-height: calc(100vh - 64px);
  }

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 64px);
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  }

  /* 优化卡片样式 - 添加阴影和圆角 */
  :deep(.ant-card) {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  :deep(.ant-card:hover) {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }

  /* 优化统计卡片样式 */
  :deep(.ant-statistic) {
    .ant-statistic-title {
      font-size: 14px;
      color: #666;
      font-weight: 500;
      margin-bottom: 8px;
    }
    .ant-statistic-content {
      font-weight: 600;
    }
  }

  /* 管理员首页 - 关键指标卡片优化 */
  .dashboard-content {
    :deep(.ant-row:first-child .ant-card) {
      background: #ffffff;
      border: 1px solid #e8e8e8;
      position: relative;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      height: 100%;
      min-height: 120px;
    }

    :deep(.ant-row:first-child .ant-card-body) {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 20px;
    }

    :deep(.ant-row:first-child .ant-card:hover) {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    :deep(.ant-row:first-child .ant-card::before) {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
    }

    :deep(.ant-row:first-child .ant-col:nth-child(1) .ant-card::before) {
      background: #1890ff;
    }
    :deep(.ant-row:first-child .ant-col:nth-child(2) .ant-card::before) {
      background: #52c41a;
    }
    :deep(.ant-row:first-child .ant-col:nth-child(3) .ant-card::before) {
      background: #fa8c16;
    }
    :deep(.ant-row:first-child .ant-col:nth-child(4) .ant-card::before) {
      background: #722ed1;
    }
    :deep(.ant-row:first-child .ant-col:nth-child(5) .ant-card::before) {
      background: #13c2c2;
    }
    :deep(.ant-row:first-child .ant-col:nth-child(6) .ant-card::before) {
      background: #eb2f96;
    }

    /* 优化统计数值的显示 */
    :deep(.ant-row:first-child .ant-statistic-title) {
      color: #666;
      font-size: 14px;
      margin-bottom: 12px;
    }

    :deep(.ant-row:first-child .ant-statistic-content) {
      font-size: 28px;
      font-weight: 600;
    }

    /* 金额小字样式 */
    :deep(.ant-row:first-child .ant-card-body > div:last-child) {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
      font-size: 12px;
      color: #999;
    }
  }

  /* 普通员工首页 - 欢迎卡片优化 */
  .workbench-content {
    :deep(.ant-row:first-child .ant-card) {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
    }

    /* 我的统计卡片优化 */
    :deep(.ant-row:nth-child(2) .ant-card) {
      background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
      border: none;
      position: relative;
      overflow: hidden;
    }

    :deep(.ant-row:nth-child(2) .ant-card::before) {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
    }

    :deep(.ant-row:nth-child(2) .ant-col:nth-child(1) .ant-card::before) {
      background: linear-gradient(90deg, #f5222d, #ff4d4f);
    }
    :deep(.ant-row:nth-child(2) .ant-col:nth-child(2) .ant-card::before) {
      background: linear-gradient(90deg, #1890ff, #40a9ff);
    }
    :deep(.ant-row:nth-child(2) .ant-col:nth-child(3) .ant-card::before) {
      background: linear-gradient(90deg, #52c41a, #73d13d);
    }
    :deep(.ant-row:nth-child(2) .ant-col:nth-child(4) .ant-card::before) {
      background: linear-gradient(90deg, #fa8c16, #ffa940);
    }

    /* 快捷操作按钮优化 */
    :deep(.ant-btn-primary) {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
      transition: all 0.3s ease;
    }

    :deep(.ant-btn-primary:hover) {
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.5);
      transform: translateY(-2px);
    }
  }

  /* 确保表格表头固定 */
  :deep(.ant-card-body) {
    .ant-table-wrapper {
      .ant-table {
        .ant-table-header {
          overflow: visible !important;
        }
        .ant-table-body {
          overflow-y: auto !important;
        }
        .ant-table-thead {
          position: sticky;
          top: 0;
          z-index: 10;
          background: #fafafa;
        }
      }
    }
  }

  /* 优化工作台订单表格样式 */
  .workbench-content {
    :deep(.ant-table) {
      .ant-table-tbody > tr > td {
        padding: 12px 16px;
      }
      .ant-table-thead > tr > th {
        padding: 12px 16px;
        font-weight: 600;
        background: #fafafa;
      }
      /* 客户名称列样式优化 - 允许换行显示完整名称 */
      .ant-table-tbody > tr > td:nth-child(2) {
        max-width: 300px;
        word-break: break-all;
        white-space: normal;
        line-height: 1.5;
      }
      /* 表格边框优化 */
      &.ant-table-bordered {
        .ant-table-tbody > tr > td {
          border-right: 1px solid #f0f0f0;
        }
      }
    }
  }
</style>
