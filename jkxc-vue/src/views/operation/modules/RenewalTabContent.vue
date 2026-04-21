<template>
  <div class="renewal-tab-content">
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            :title="renewalType === 'accounting' ? '代账续费单数' : '地址续费单数'"
            :value="statistics.totalCount"
            suffix="单"
            :value-style="{ color: '#1890ff', fontSize: '24px' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            :title="renewalType === 'accounting' ? '代账续费金额' : '地址续费金额'"
            :value="statistics.totalAmount"
            prefix="¥"
            :precision="2"
            :value-style="{ color: '#52c41a', fontSize: '24px' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="平均续费金额"
            :value="statistics.avgAmount"
            prefix="¥"
            :precision="2"
            :value-style="{ color: '#fa8c16', fontSize: '24px' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="业务员数量"
            :value="statistics.salesmanCount"
            suffix="人"
            :value-style="{ color: '#722ed1', fontSize: '24px' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16">
        <!-- 月度趋势图 -->
      <a-col :span="24" style="margin-bottom: 16px">
        <a-card :title="renewalType === 'accounting' ? '月度代账续费趋势' : '月度地址续费趋势'" :bordered="false" class="chart-card">
          <div :ref="`monthlyTrendChart`" style="width: 100%; height: 400px; min-height: 400px;"></div>
        </a-card>
      </a-col>

      <!-- 业务类型分布 -->
      <a-col :span="12" style="margin-bottom: 16px">
        <a-card title="业务类型分布" :bordered="false" class="chart-card">
          <div :ref="`businessTypeChart`" style="width: 100%; height: 350px"></div>
        </a-card>
      </a-col>

      <!-- 业务员排名 -->
      <a-col :span="12" style="margin-bottom: 16px">
        <a-card :title="renewalType === 'accounting' ? '业务员代账续费排名（Top 10）' : '业务员地址续费排名（Top 10）'" :bordered="false" class="chart-card">
          <div :ref="`salesmanRankChart`" style="width: 100%; height: 350px"></div>
        </a-card>
      </a-col>

      <!-- 每日续费趋势 -->
      <a-col :span="24" style="margin-bottom: 16px">
        <a-card :title="renewalType === 'accounting' ? '每日代账续费趋势' : '每日地址续费趋势'" :bordered="false" class="chart-card">
          <div :ref="`dailyTrendChart`" style="width: 100%; height: 400px; min-height: 400px;"></div>
        </a-card>
      </a-col>

      <!-- 业务人员月度明细 -->
      <a-col :span="24">
        <a-card :title="renewalType === 'accounting' ? '业务人员代账续费月度明细' : '业务人员地址续费月度明细'" :bordered="false" class="table-card">
          <a-table
            :columns="salesmanMonthlyColumns"
            :data-source="salesmanMonthlyDataWithTotal"
            :pagination="false"
            :scroll="{ x: 'max-content' }"
            :loading="loading"
            :row-key="(record, index) => record.__isTotalRow ? '__total__' : (record.salesman || index)"
            bordered
            size="middle"
          >
            <template slot="salesman" slot-scope="text">
              <span v-if="text === '合计'" style="font-weight: 700">{{ text }}</span>
              <a-tag v-else color="blue">{{ text }}</a-tag>
            </template>
            <template
              v-for="m in 12"
              :slot="`month${m}_count`"
              slot-scope="text, record"
            >
              <span
                v-if="record.__isTotalRow"
                :key="'count-total-' + m"
                style="font-weight: 700"
              >
                {{ record[`month${m}_count`] || 0 }}
              </span>
              <a
                v-else
                :key="'count-' + m"
                @click="handleShowRenewalList(record.salesman, m, record[`month${m}_count`])"
                style="color: #1890ff; font-weight: 500; cursor: pointer; text-decoration: underline;"
              >
                {{ record[`month${m}_count`] || 0 }}
              </a>
            </template>
            <template
              v-for="m in 12"
              :slot="`month${m}_amount`"
              slot-scope="text, record"
            >
              <span :key="'amount-' + m" style="color: #52c41a; font-weight: 500">
                ¥{{ formatAmount(record[`month${m}_amount`]) }}
              </span>
            </template>
            <template slot="totalCount" slot-scope="text, record">
              <span :style="{ color: '#1890ff', fontWeight: record.__isTotalRow ? 700 : 600, fontSize: '14px' }">
                {{ record.totalCount || 0 }}
              </span>
            </template>
            <template slot="totalAmount" slot-scope="text, record">
              <span :style="{ color: '#52c41a', fontWeight: record.__isTotalRow ? 700 : 600, fontSize: '14px' }">
                ¥{{ formatAmount(record.totalAmount) }}
              </span>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 续费记录列表弹窗 -->
    <j-modal
      :visible.sync="renewalListModal.visible"
      :width="1200"
      :title="renewalListModal.title"
      :fullscreen.sync="renewalListModal.fullscreen"
      :switchFullscreen="renewalListModal.switchFullscreen"
    >
      <a-table
        :columns="renewalListColumns"
        :data-source="renewalListData"
        :loading="renewalListLoading"
        bordered
        size="small"
        rowKey="id"
        :pagination="{
          current: renewalListPagination.current,
          pageSize: renewalListPagination.pageSize,
          total: renewalListPagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
          pageSizeOptions: ['10', '20', '50', '100'],
        }"
        @change="handleRenewalListTableChange"
      >
        <template slot="companyName" slot-scope="text">
          <span>{{ text || '-' }}</span>
        </template>
        <template slot="renewalTime" slot-scope="text">
          {{ formatDate(text) }}
        </template>
        <template slot="amounts" slot-scope="text">
          <span style="color: #f5222d; font-weight: 600">
            ¥{{ formatAmount(text) }}
          </span>
        </template>
        <template slot="amountReceived" slot-scope="text">
          <span style="color: #52c41a; font-weight: 600">
            ¥{{ formatAmount(text) }}
          </span>
        </template>
        <template slot="paymentTime" slot-scope="text">
          {{ formatDateTime(text) }}
        </template>
        <template slot="postExpirationDate" slot-scope="text">
          {{ formatMonth(text) }}
        </template>
        <template slot="auditStatus" slot-scope="text, record">
          <a-tag :color="getAuditStatusColor(record.auditStatus)" style="margin: 0">
            {{ getAuditStatusText(record.auditStatus) }}
          </a-tag>
        </template>
      </a-table>
    </j-modal>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'
import moment from 'moment'
import * as echarts from 'echarts'

export default {
  name: 'RenewalTabContent',
  props: {
    queryParam: {
      type: Object,
      required: true,
    },
    renewalType: {
      type: String,
      required: true,
      validator: (value) => ['accounting', 'address'].includes(value),
    },
  },
  data() {
    return {
      statistics: {
        totalCount: 0,
        totalAmount: 0,
        avgAmount: 0,
        salesmanCount: 0,
      },
      chartData: {
        monthlyTrend: [],
        businessType: [],
        salesmanRank: [],
        dailyTrend: [],
      },
      charts: {},
      salesmanMonthlyData: [],
      salesmanMonthlyColumns: [],
      loading: false,
      renewalListModal: {
        visible: false,
        title: '',
        fullscreen: false,
        switchFullscreen: true,
        currentSalesman: '',
        currentMonth: null,
      },
      renewalListData: [],
      renewalListLoading: false,
      renewalListPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      renewalListColumns: [
        {
          title: '公司名称',
          dataIndex: 'companyName',
          key: 'companyName',
          width: 200,
          scopedSlots: { customRender: 'companyName' },
        },
        {
          title: '续费时间',
          dataIndex: 'renewalTime',
          key: 'renewalTime',
          width: 120,
          scopedSlots: { customRender: 'renewalTime' },
        },
        {
          title: '续费金额',
          dataIndex: 'amounts',
          key: 'amounts',
          width: 120,
          align: 'right',
          scopedSlots: { customRender: 'amounts' },
        },
        {
          title: '到款金额',
          dataIndex: 'amountReceived',
          key: 'amountReceived',
          width: 120,
          align: 'right',
          scopedSlots: { customRender: 'amountReceived' },
        },
        {
          title: '收款时间',
          dataIndex: 'paymentTime',
          key: 'paymentTime',
          width: 180,
          scopedSlots: { customRender: 'paymentTime' },
        },
        {
          title: '后到期日期',
          dataIndex: 'postExpirationDate',
          key: 'postExpirationDate',
          width: 120,
          scopedSlots: { customRender: 'postExpirationDate' },
        },
        {
          title: '审核状态',
          dataIndex: 'auditStatus',
          key: 'auditStatus',
          width: 100,
          align: 'center',
          scopedSlots: { customRender: 'auditStatus' },
        },
      ],
    }
  },
  computed: {
    salesmanMonthlyDataWithTotal() {
      const rows = Array.isArray(this.salesmanMonthlyData) ? this.salesmanMonthlyData : []
      if (rows.length === 0) return []
      const total = { __isTotalRow: true, salesman: '合计' }
      for (let m = 1; m <= 12; m++) {
        total[`month${m}_count`] = rows.reduce((acc, row) => acc + (Number(row[`month${m}_count`]) || 0), 0)
        total[`month${m}_amount`] = rows.reduce((acc, row) => acc + (Number(row[`month${m}_amount`]) || 0), 0)
      }
      total.totalCount = rows.reduce((acc, row) => acc + (Number(row.totalCount) || 0), 0)
      total.totalAmount = rows.reduce((acc, row) => acc + (Number(row.totalAmount) || 0), 0)
      return [...rows, total]
    },
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
      this.initSalesmanMonthlyColumns()
      this.loadData()
    })
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach((chart) => {
      if (chart) {
        chart.dispose()
      }
    })
  },
  watch: {
    queryParam: {
      deep: true,
      handler() {
        this.loadData()
      },
    },
  },
  methods: {
    initCharts() {
      if (!echarts) {
        console.error('ECharts未安装，请先执行: npm install echarts --save')
        return
      }
      const chartNames = ['monthlyTrend', 'businessType', 'salesmanRank', 'dailyTrend']
      chartNames.forEach((name) => {
        const refName = `${name}Chart`
        const ref = this.$refs[refName]
        if (ref) {
          this.charts[name] = echarts.init(ref)
        }
      })
      window.addEventListener('resize', this.handleResize)
    },
    handleResize() {
      Object.values(this.charts).forEach((chart) => {
        if (chart) {
          chart.resize()
        }
      })
    },
    handleSearch() {
      this.loadData()
    },
    handleReset() {
      // 重置由父组件处理
      this.loadData()
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          year: this.queryParam.year,
          month: this.queryParam.month,
          businessType: this.queryParam.businessType,
          renewalType: this.renewalType,
        }
        const [statsRes, detailRes] = await Promise.all([
          getAction('/renew/ghAddressRenew/getRenewalStatistics', params),
          getAction('/renew/ghAddressRenew/getSalesmanMonthlyDetail', params),
        ])
        
        if (statsRes.success && statsRes.result) {
          this.statistics = statsRes.result.statistics || this.statistics
          this.chartData = {
            monthlyTrend: statsRes.result.monthlyTrend || [],
            businessType: statsRes.result.businessType || [],
            salesmanRank: statsRes.result.salesmanRank || [],
            dailyTrend: statsRes.result.dailyTrend || [],
          }
          this.updateCharts()
        } else {
          this.$message.warning(statsRes.message || '获取统计数据失败')
        }
        
        if (detailRes.success && detailRes.result) {
          this.salesmanMonthlyData = detailRes.result || []
        } else {
          this.$message.warning(detailRes.message || '获取业务人员明细数据失败')
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
      this.updateDailyTrendChart()
    },
    updateMonthlyTrendChart() {
      if (!this.charts.monthlyTrend) return
      const data = this.chartData.monthlyTrend || []
      
      // 确保始终显示12个月
      const monthDataMap = {}
      
      // 将后端返回的数据转换为Map，key为月份数字（1-12）
      data.forEach((item) => {
        const monthStr = item.month || item.monthName || ''
        // 提取月份数字（例如："1月" -> 1, "12月" -> 12）
        const monthMatch = monthStr.match(/(\d+)/)
        if (monthMatch) {
          const monthNum = parseInt(monthMatch[1])
          if (monthNum >= 1 && monthNum <= 12) {
            monthDataMap[monthNum] = item
          }
        }
      })
      
      // 生成12个月的完整数据（强制生成12个月，即使数据为空）
      const monthLabels = []
      const countData = []
      const amountData = []
      
      for (let m = 1; m <= 12; m++) {
        monthLabels.push(m + '月')
        const monthData = monthDataMap[m] || {}
        countData.push(monthData.count || 0)
        const amount = monthData.amount || 0
        amountData.push(typeof amount === 'number' ? amount : parseFloat(amount) || 0)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999',
            },
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e8e8e8',
          borderWidth: 1,
          textStyle: {
            color: '#333',
          },
        },
        legend: {
          data: ['续费单数', '续费金额'],
          top: 10,
          textStyle: {
            fontSize: 12,
          },
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '8%',
          top: '15%',
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: monthLabels,
          axisLine: {
            lineStyle: {
              color: '#d9d9d9',
            },
          },
          axisLabel: {
            color: '#666',
            interval: 0,
            rotate: 0,
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '续费单数',
            position: 'left',
            axisLine: {
              lineStyle: {
                color: '#1890ff',
              },
            },
            axisLabel: {
              color: '#666',
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0',
              },
            },
          },
          {
            type: 'value',
            name: '续费金额',
            position: 'right',
            axisLine: {
              lineStyle: {
                color: '#52c41a',
              },
            },
            axisLabel: {
              color: '#666',
              formatter: (value) => {
                if (value >= 10000) {
                  return '¥' + (value / 10000).toFixed(1) + '万'
                }
                return '¥' + value
              },
            },
            splitLine: {
              show: false,
            },
          },
        ],
        series: [
          {
            name: '续费单数',
            type: 'line',
            yAxisIndex: 0,
            data: countData,
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 3,
              color: '#1890ff',
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
              ]),
            },
            itemStyle: {
              color: '#1890ff',
            },
          },
          {
            name: '续费金额',
            type: 'line',
            yAxisIndex: 1,
            data: amountData,
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 3,
              color: '#52c41a',
            },
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
      const data = this.chartData.businessType || []
      const colors = [
        '#1890ff',
        '#52c41a',
        '#fa8c16',
        '#eb2f96',
        '#722ed1',
        '#13c2c2',
        '#f5222d',
        '#faad14',
        '#2f54eb',
        '#a0d911',
      ]
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}单 ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e8e8e8',
          borderWidth: 1,
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          data: data.map((item) => item.name),
          textStyle: {
            fontSize: 12,
            color: '#666',
          },
        },
        series: [
          {
            name: '业务类型',
            type: 'pie',
            radius: ['45%', '75%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 2,
            },
            label: {
              show: true,
              formatter: '{b}\n{c}单 ({d}%)',
              fontSize: 11,
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10,
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
              },
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold',
              },
            },
            data: data.map((item, index) => ({
              value: item.count,
              name: item.name,
              itemStyle: {
                color: colors[index % colors.length],
              },
            })),
          },
        ],
      }
      this.charts.businessType.setOption(option)
    },
    updateSalesmanRankChart() {
      if (!this.charts.salesmanRank) return
      const data = (this.chartData.salesmanRank || []).slice(0, 10)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e8e8e8',
          borderWidth: 1,
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
            lineStyle: {
              color: '#d9d9d9',
            },
          },
          axisLabel: {
            color: '#666',
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0',
            },
          },
        },
        yAxis: {
          type: 'category',
          data: data.map((item) => item.salesman),
          inverse: true,
          axisLine: {
            lineStyle: {
              color: '#d9d9d9',
            },
          },
          axisLabel: {
            color: '#666',
          },
        },
        series: [
          {
            name: '续费单数',
            type: 'bar',
            data: data.map((item) => item.count),
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
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(24, 144, 255, 0.5)',
              },
            },
          },
        ],
      }
      this.charts.salesmanRank.setOption(option)
    },
    updateDailyTrendChart() {
      if (!this.charts.dailyTrend) return
      const data = this.chartData.dailyTrend || []
      if (!data || data.length === 0) {
        return
      }
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999',
            },
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e8e8e8',
          borderWidth: 1,
          textStyle: {
            color: '#333',
          },
        },
        legend: {
          data: ['续费单数', '续费金额'],
          top: 10,
          textStyle: {
            fontSize: 12,
          },
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '8%',
          top: '15%',
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.map((item) => {
            const dateValue = item.date || item.month
            if (!dateValue) return '-'
            if (dateValue.includes('-') && dateValue.split('-').length === 3) {
              const dateParts = dateValue.split('-')
              if (this.queryParam.month) {
                return `${dateParts[1]}-${dateParts[2]}`
              }
              return `${dateParts[1]}-${dateParts[2]}`
            }
            return dateValue
          }),
          axisLine: {
            lineStyle: {
              color: '#d9d9d9',
            },
          },
          axisLabel: {
            color: '#666',
            interval: 0,
            rotate: 0,
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '续费单数',
            position: 'left',
            axisLine: {
              lineStyle: {
                color: '#1890ff',
              },
            },
            axisLabel: {
              color: '#666',
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0',
              },
            },
          },
          {
            type: 'value',
            name: '续费金额',
            position: 'right',
            axisLine: {
              lineStyle: {
                color: '#52c41a',
              },
            },
            axisLabel: {
              color: '#666',
              formatter: (value) => {
                if (value >= 10000) {
                  return '¥' + (value / 10000).toFixed(1) + '万'
                }
                return '¥' + value
              },
            },
            splitLine: {
              show: false,
            },
          },
        ],
        series: [
          {
            name: '续费单数',
            type: 'line',
            yAxisIndex: 0,
            data: data.map((item) => item.count || 0),
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 3,
              color: '#1890ff',
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
              ]),
            },
            itemStyle: {
              color: '#1890ff',
            },
          },
          {
            name: '续费金额',
            type: 'line',
            yAxisIndex: 1,
            data: data.map((item) => {
              const amount = item.amount || 0
              return typeof amount === 'number' ? amount : parseFloat(amount) || 0
            }),
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 3,
              color: '#52c41a',
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
                { offset: 1, color: 'rgba(82, 196, 26, 0.05)' },
              ]),
            },
            itemStyle: {
              color: '#52c41a',
            },
          },
        ],
      }
      this.charts.dailyTrend.setOption(option)
    },
    initSalesmanMonthlyColumns() {
      const columns = [
        {
          title: '业务人员',
          dataIndex: 'salesman',
          key: 'salesman',
          width: 120,
          fixed: 'left',
          scopedSlots: { customRender: 'salesman' },
        },
      ]
      
      for (let m = 1; m <= 12; m++) {
        columns.push({
          title: `${m}月`,
          key: `month${m}`,
          align: 'center',
          children: [
            {
              title: '单量',
              dataIndex: `month${m}_count`,
              key: `month${m}_count`,
              width: 80,
              align: 'center',
              scopedSlots: { customRender: `month${m}_count` },
            },
            {
              title: '金额',
              dataIndex: `month${m}_amount`,
              key: `month${m}_amount`,
              width: 120,
              align: 'right',
              scopedSlots: { customRender: `month${m}_amount` },
            },
          ],
        })
      }
      
      columns.push({
        title: '合计',
        key: 'total',
        align: 'center',
        fixed: 'right',
        children: [
          {
            title: '单量',
            dataIndex: 'totalCount',
            key: 'totalCount',
            width: 100,
            align: 'center',
            scopedSlots: { customRender: 'totalCount' },
          },
          {
            title: '金额',
            dataIndex: 'totalAmount',
            key: 'totalAmount',
            width: 150,
            align: 'right',
            scopedSlots: { customRender: 'totalAmount' },
          },
        ],
      })
      
      this.salesmanMonthlyColumns = columns
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      const num = typeof amount === 'number' ? amount : parseFloat(amount)
      return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    renderMonthlySummary(pageData) {
      const rows = Array.isArray(pageData) ? pageData : []
      const sum = (key) => rows.reduce((acc, row) => acc + (Number(row[key]) || 0), 0)
      const mkCell = (index, content, style = {}) =>
        this.$createElement('a-table-summary-cell', { attrs: { index }, style }, [content])
      const cells = [mkCell(0, '合计', { fontWeight: 600, textAlign: 'center' })]
      for (let m = 1; m <= 12; m++) {
        const baseIndex = 1 + (m - 1) * 2
        cells.push(mkCell(baseIndex, String(sum(`month${m}_count`)), { textAlign: 'center', fontWeight: 600 }))
        cells.push(mkCell(baseIndex + 1, `¥${this.formatAmount(sum(`month${m}_amount`))}`, { textAlign: 'right', fontWeight: 600, color: '#52c41a' }))
      }
      cells.push(mkCell(25, String(sum('totalCount')), { textAlign: 'center', fontWeight: 700 }))
      cells.push(mkCell(26, `¥${this.formatAmount(sum('totalAmount'))}`, { textAlign: 'right', fontWeight: 700, color: '#52c41a' }))
      return this.$createElement('a-table-summary', [this.$createElement('a-table-summary-row', cells)])
    },
    formatDate(date) {
      if (!date) return '-'
      return moment(date).format('YYYY-MM-DD')
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return moment(dateTime).format('YYYY-MM-DD HH:mm:ss')
    },
    formatMonth(date) {
      if (!date) return '-'
      return moment(date).format('YYYY-MM')
    },
    getAuditStatusColor(status) {
      if (!status && status !== 0 && status !== '0') return 'default'
      const statusStr = String(status)
      const colorMap = {
        '0': 'orange',
        '1': 'green',
        '2': 'red',
        0: 'orange',
        1: 'green',
        2: 'red',
      }
      return colorMap[statusStr] || colorMap[status] || 'default'
    },
    getAuditStatusText(status) {
      const statusMap = {
        '0': '待审核',
        '1': '已通过',
        '2': '已驳回',
        0: '待审核',
        1: '已通过',
        2: '已驳回',
      }
      const statusValue = status
      return statusMap[statusValue] || statusMap[String(statusValue)] || status || '-'
    },
    handleShowRenewalList(salesman, month, count) {
      if (!count || count === 0) {
        return
      }
      this.renewalListModal.title = `${salesman} - ${month}月${this.renewalType === 'accounting' ? '代账' : '地址'}续费列表（共${count}单）`
      this.renewalListModal.currentSalesman = salesman
      this.renewalListModal.currentMonth = month
      this.renewalListModal.visible = true
      this.renewalListPagination.current = 1
      this.loadRenewalList(salesman, month)
    },
    async loadRenewalList(salesman, month) {
      this.renewalListLoading = true
      try {
        const params = {
          year: this.queryParam.year,
          month: month,
          salesman: salesman,
          businessType: this.queryParam.businessType,
          renewalType: this.renewalType,
          pageNo: this.renewalListPagination.current,
          pageSize: this.renewalListPagination.pageSize,
        }
        const res = await getAction('/renew/ghAddressRenew/getSalesmanMonthlyRenewals', params)
        if (res.success && res.result) {
          this.renewalListData = res.result.records || []
          this.renewalListPagination.total = res.result.total || 0
        } else {
          this.$message.warning(res.message || '获取续费列表失败')
        }
      } catch (error) {
        console.error('加载续费列表失败:', error)
        this.$message.error('加载续费列表失败')
      } finally {
        this.renewalListLoading = false
      }
    },
    handleRenewalListTableChange(pagination) {
      this.renewalListPagination.current = pagination.current
      this.renewalListPagination.pageSize = pagination.pageSize
      this.loadRenewalList(this.renewalListModal.currentSalesman, this.renewalListModal.currentMonth)
    },
  },
}
</script>

<style scoped lang="less">
.renewal-tab-content {
  .chart-card {
    background: #fff;
    
    /deep/ .ant-card-body {
      padding: 16px;
      overflow: visible;
    }
    
    > div {
      width: 100% !important;
      overflow: visible;
    }
  }

  .table-card {
    background: #fff;

    /deep/ .ant-table {
      .ant-table-thead > tr > th {
        background: #fafafa;
        font-weight: 600;
        text-align: center;
        padding: 8px 8px;
      }

      .ant-table-tbody > tr:hover > td {
        background: #f5f5f5;
      }

      .ant-table-tbody > tr > td {
        padding: 8px 8px;
      }
    }
  }

  // 续费列表弹窗表格样式
  /deep/ .ant-modal-body {
    .ant-table {
      .ant-table-thead > tr > th {
        padding: 8px 8px;
        background: #fafafa;
        font-weight: 600;
      }

      .ant-table-tbody > tr > td {
        padding: 8px 8px;
      }

      .ant-tag {
        margin: 0;
        padding: 2px 8px;
        font-size: 12px;
        line-height: 20px;
      }
    }

    .ant-pagination {
      margin-top: 16px;
    }
  }
}
</style>

