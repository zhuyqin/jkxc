<template>
  <div class="dashboard">
    <a-spin :spinning="loading" size="large">
      <!-- 关键指标卡片 -->
      <a-row :gutter="16" class="stat-cards">
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <div class="stat-card stat-card-blue">
            <div class="stat-icon">
              <a-icon type="shopping-cart" />
            </div>
            <div class="stat-content">
              <div class="stat-title">今日订单</div>
              <div class="stat-value">{{ statistics.todayOrders }}<span class="stat-unit">单</span></div>
              <div class="stat-desc">金额：¥{{ formatAmount(statistics.todayAmount) }}</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <div class="stat-card stat-card-green">
            <div class="stat-icon">
              <a-icon type="calendar" />
            </div>
            <div class="stat-content">
              <div class="stat-title">本月订单</div>
              <div class="stat-value">{{ statistics.monthOrders }}<span class="stat-unit">单</span></div>
              <div class="stat-desc">金额：¥{{ formatAmount(statistics.monthAmount) }}</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <div class="stat-card stat-card-orange">
            <div class="stat-icon">
              <a-icon type="bar-chart" />
            </div>
            <div class="stat-content">
              <div class="stat-title">本年订单</div>
              <div class="stat-value">{{ statistics.yearOrders }}<span class="stat-unit">单</span></div>
              <div class="stat-desc">金额：¥{{ formatAmount(statistics.yearAmount) }}</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <div class="stat-card stat-card-purple" @click="handleCardClick('customer')">
            <div class="stat-icon">
              <a-icon type="team" />
            </div>
            <div class="stat-content">
              <div class="stat-title">客户总数</div>
              <div class="stat-value">{{ statistics.totalCustomers }}<span class="stat-unit">家</span></div>
              <div class="stat-desc">&nbsp;</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <div class="stat-card stat-card-cyan">
            <div class="stat-icon">
              <a-icon type="check-circle" />
            </div>
            <div class="stat-content">
              <div class="stat-title">任务完成率</div>
              <div class="stat-value">{{ statistics.taskCompletionRate.toFixed(1) }}<span class="stat-unit">%</span></div>
              <div class="stat-desc">&nbsp;</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <div class="stat-card stat-card-pink">
            <div class="stat-icon">
              <a-icon type="user" />
            </div>
            <div class="stat-content">
              <div class="stat-title">业务员数量</div>
              <div class="stat-value">{{ statistics.salesmanCount }}<span class="stat-unit">人</span></div>
              <div class="stat-desc">&nbsp;</div>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- 月度订单趋势 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :span="24">
          <a-card title="月度订单趋势" :bordered="false" class="chart-card">
            <div ref="monthlyTrendChart" style="width: 100%; height: 400px"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 业务类型分布和业务员排名 -->
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :span="12">
          <a-card title="业务类型分布" :bordered="false" class="chart-card">
            <div ref="businessTypeChart" style="width: 100%; height: 350px"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="业务员排名（Top 10）" :bordered="false" class="chart-card">
            <div ref="salesmanRankChart" style="width: 100%; height: 350px"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 最近订单和高价值客户 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="最近订单（Top 10）" :bordered="false" class="chart-card">
            <a-table
              :columns="orderColumns"
              :dataSource="recentOrders"
              :pagination="false"
              size="small"
              :scroll="{ y: 300 }"
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
        <a-col :span="12">
          <a-card title="高价值客户（Top 10）" :bordered="false" class="chart-card">
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
    </a-spin>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'
import moment from 'moment'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      loading: false,
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
          width: 150,
        },
        {
          title: '客户名称',
          dataIndex: 'companyName',
          key: 'companyName',
        },
        {
          title: '业务员',
          dataIndex: 'salesman',
          key: 'salesman',
          width: 100,
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
    }
  },
  mounted() {
    this.initCharts()
    this.loadData()
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    // 销毁图表
    Object.values(this.charts).forEach((chart) => {
      if (chart) {
        chart.dispose()
      }
    })
  },
  methods: {
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
    async loadData() {
      this.loading = true
      try {
        const res = await getAction('/dashboard/getStatistics')
        if (res.success && res.result) {
          this.statistics = res.result.statistics || this.statistics
          this.monthlyTrend = res.result.monthlyTrend || []
          this.businessTypeDistribution = res.result.businessTypeDistribution || []
          this.salesmanRank = res.result.salesmanRank || []
          this.recentOrders = res.result.recentOrders || []
          this.topCustomers = res.result.topCustomers || []

          this.updateCharts()
        } else {
          this.$message.warning(res.message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
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
            lineStyle: {
              color: '#ddd'
            }
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          },
          padding: 12
        },
        legend: {
          data: ['订单数', '订单金额'],
          top: 10,
          textStyle: {
            fontSize: 13,
            color: '#666'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.map((item) => item.month),
          axisLine: {
            lineStyle: {
              color: '#ddd'
            }
          },
          axisLabel: {
            color: '#666',
            fontSize: 12
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '订单数',
            position: 'left',
            axisLine: {
              lineStyle: {
                color: '#ddd'
              }
            },
            axisLabel: {
              color: '#666',
              fontSize: 12
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0'
              }
            }
          },
          {
            type: 'value',
            name: '订单金额',
            position: 'right',
            axisLine: {
              lineStyle: {
                color: '#ddd'
              }
            },
            axisLabel: {
              color: '#666',
              fontSize: 12,
              formatter: (value) => {
                if (value >= 10000) {
                  return (value / 10000).toFixed(1) + '万'
                }
                return value
              },
            },
            splitLine: {
              show: false
            }
          },
        ],
        series: [
          {
            name: '订单数',
            type: 'line',
            data: data.map((item) => item.orders),
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3
            },
            itemStyle: {
              color: '#667eea',
              borderWidth: 2,
              borderColor: '#fff'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
                  { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
                ]
              }
            }
          },
          {
            name: '订单金额',
            type: 'line',
            yAxisIndex: 1,
            data: data.map((item) => item.amount),
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3
            },
            itemStyle: {
              color: '#52c41a',
              borderWidth: 2,
              borderColor: '#fff'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
                  { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
                ]
              }
            }
          },
        ],
      }
      this.charts.monthlyTrend.setOption(option)
    },
    updateBusinessTypeChart() {
      if (!this.charts.businessType) return
      const data = this.businessTypeDistribution || []
      
      const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#43e97b', '#38f9d7', '#fa709a', '#fee140']
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          },
          padding: 12
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          data: data.map((item) => item.name),
          textStyle: {
            fontSize: 13,
            color: '#666'
          },
          itemGap: 12
        },
        color: colors,
        series: [
          {
            name: '业务类型',
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center',
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold',
                color: '#333'
              },
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.3)'
              }
            },
            labelLine: {
              show: false,
            },
            data: data.map((item, index) => ({
              value: item.count,
              name: item.name,
              itemStyle: {
                borderRadius: 8,
                borderColor: '#fff',
                borderWidth: 2
              }
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
            shadowStyle: {
              color: 'rgba(0, 0, 0, 0.05)'
            }
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          },
          padding: 12
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
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#666',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        yAxis: {
          type: 'category',
          data: data.map((item) => item.salesman),
          inverse: true,
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#666',
            fontSize: 12
          }
        },
        series: [
          {
            name: '订单数',
            type: 'bar',
            data: data.map((item) => item.orders),
            barWidth: '60%',
            itemStyle: {
              borderRadius: [0, 8, 8, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#667eea' },
                { offset: 0.5, color: '#764ba2' },
                { offset: 1, color: '#f093fb' },
              ]),
            },
            label: {
              show: true,
              position: 'right',
              color: '#667eea',
              fontWeight: 'bold',
              fontSize: 13
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(102, 126, 234, 0.5)'
              }
            }
          },
        ],
      }
      this.charts.salesmanRank.setOption(option)
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
    handleCardClick(type) {
      // 处理卡片点击事件
      switch (type) {
        case 'customer':
          // 跳转到客户管理页面
          this.$router.push({ path: '/customer/CustomerList' })
          break
        default:
          break
      }
    },
  },
}
</script>

<style lang="less" scoped>
.dashboard {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: calc(100vh - 64px);
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><defs><pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse"><path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255,255,255,0.05)" stroke-width="1"/></pattern></defs><rect width="100%" height="100%" fill="url(%23grid)"/></svg>');
    pointer-events: none;
  }

  .stat-cards {
    margin-bottom: 24px;
    position: relative;
    z-index: 1;
  }

  .stat-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 16px;
    position: relative;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.3);
    overflow: hidden;
    cursor: pointer;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, transparent, currentColor, transparent);
      opacity: 0;
      transition: opacity 0.3s;
    }
    
    &:hover {
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
      transform: translateY(-4px) scale(1.02);
      
      &::before {
        opacity: 1;
      }
      
      .stat-icon {
        transform: scale(1.1) rotate(5deg);
      }
    }

    .stat-icon {
      position: absolute;
      top: 24px;
      right: 24px;
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .stat-content {
      padding-right: 70px;
    }

    .stat-title {
      color: #666;
      font-size: 14px;
      margin-bottom: 12px;
      font-weight: 500;
      letter-spacing: 0.5px;
    }

    .stat-value {
      color: #1a1a1a;
      font-size: 32px;
      font-weight: 700;
      line-height: 1.2;
      font-family: 'Helvetica Neue', Arial, sans-serif;

      .stat-unit {
        font-size: 18px;
        margin-left: 4px;
        color: #999;
        font-weight: 500;
      }
    }

    .stat-desc {
      color: #999;
      font-size: 13px;
      margin-top: 10px;
      font-weight: 400;
    }

    &.stat-card-blue {
      &::before {
        color: #1890ff;
      }
      .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }

    &.stat-card-green {
      &::before {
        color: #52c41a;
      }
      .stat-icon {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
        color: #fff;
      }
    }

    &.stat-card-orange {
      &::before {
        color: #fa8c16;
      }
      .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: #fff;
      }
    }

    &.stat-card-purple {
      &::before {
        color: #722ed1;
      }
      .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: #fff;
      }
    }

    &.stat-card-cyan {
      &::before {
        color: #13c2c2;
      }
      .stat-icon {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        color: #fff;
      }
    }

    &.stat-card-pink {
      &::before {
        color: #eb2f96;
      }
      .stat-icon {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: #fff;
      }
    }
  }
  
  .chart-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }
    
    /deep/ .ant-card-head {
      border-bottom: 1px solid rgba(0, 0, 0, 0.06);
      padding: 16px 24px;
      
      .ant-card-head-title {
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
      }
    }
    
    /deep/ .ant-card-body {
      padding: 24px;
    }
    
    /deep/ .ant-table {
      font-size: 13px;
      
      .ant-table-thead > tr > th {
        background: #fafafa;
        font-weight: 600;
        color: #333;
      }
      
      .ant-table-tbody > tr:hover > td {
        background: #f5f7fa;
      }
    }
  }
}
</style>

