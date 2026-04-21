<template>
  <div class="task-tab-content">
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            :title="taskType === 'once' ? '一次性任务数' : '周期任务数'"
            :value="statistics.totalCount"
            suffix="单"
            :value-style="{ color: '#1890ff', fontSize: '24px' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            :title="taskType === 'once' ? '一次性任务金额' : '周期任务金额'"
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
            title="平均任务金额"
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
            title="完成率"
            :value="statistics.completionRate"
            suffix="%"
            :precision="2"
            :value-style="{ color: '#722ed1', fontSize: '24px' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16">
      <!-- 月度趋势图 -->
      <a-col :span="24" style="margin-bottom: 16px">
        <a-card :title="taskType === 'once' ? '月度一次性任务趋势' : '月度周期任务趋势'" :bordered="false" class="chart-card">
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
        <a-card :title="taskType === 'once' ? '业务员一次性任务排名（Top 10）' : '业务员周期任务排名（Top 10）'" :bordered="false" class="chart-card">
          <div :ref="`salesmanRankChart`" style="width: 100%; height: 350px"></div>
        </a-card>
      </a-col>

      <!-- 每日任务趋势 -->
      <a-col :span="24" style="margin-bottom: 16px">
        <a-card :title="taskType === 'once' ? '每日一次性任务趋势' : '每日周期任务趋势'" :bordered="false" class="chart-card">
          <div :ref="`dailyTrendChart`" style="width: 100%; height: 400px; min-height: 400px;"></div>
        </a-card>
      </a-col>

      <!-- 业务人员月度明细 -->
      <a-col :span="24">
        <a-card :title="taskType === 'once' ? '业务人员一次性任务月度明细' : '业务人员周期任务月度明细'" :bordered="false" class="table-card">
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
                @click="handleShowTaskList(record.salesman, m, record[`month${m}_count`])"
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

    <!-- 任务列表弹窗 -->
    <j-modal
      :visible.sync="taskListModal.visible"
      :width="1200"
      :title="taskListModal.title"
      :fullscreen.sync="taskListModal.fullscreen"
      :switchFullscreen="taskListModal.switchFullscreen"
    >
      <a-table
        :columns="taskListColumns"
        :data-source="taskListData"
        :loading="taskListLoading"
        bordered
        size="small"
        rowKey="id"
        :pagination="{
          current: taskListPagination.current,
          pageSize: taskListPagination.pageSize,
          total: taskListPagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
          pageSizeOptions: ['10', '20', '50', '100'],
        }"
        @change="handleTaskListTableChange"
      >
        <template slot="orderNo" slot-scope="text, record">
          <a @click="handleViewTask(record.id)" style="color: #1890ff; cursor: pointer">
            {{ text || record.orderNo || '-' }}
          </a>
        </template>
        <template slot="companyName" slot-scope="text">
          <span>{{ text || '-' }}</span>
        </template>
        <template slot="businessType" slot-scope="text, record">
          <span>{{ record.businessType_dictText || record.businessTypeName || record.businessType || '-' }}</span>
        </template>
        <template slot="costAmount" slot-scope="text">
          <span style="color: #f5222d; font-weight: 600">
            ¥{{ formatAmount(text) }}
          </span>
        </template>
        <template slot="createTime" slot-scope="text">
          {{ formatDateTime(text) }}
        </template>
        <template slot="taskStatus" slot-scope="text, record">
          <a-tag :color="getTaskStatusColor(record.taskStatus)" style="margin: 0">
            {{ record.taskStatus_dictText || getTaskStatusText(record.taskStatus) }}
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
  name: 'TaskTabContent',
  props: {
    queryParam: {
      type: Object,
      required: true,
    },
    taskType: {
      type: String,
      required: true,
      validator: (value) => ['once', 'recurring'].includes(value),
    },
  },
  data() {
    return {
      statistics: {
        totalCount: 0,
        totalAmount: 0,
        avgAmount: 0,
        completionRate: 0,
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
      taskListModal: {
        visible: false,
        title: '',
        fullscreen: false,
        switchFullscreen: true,
        currentSalesman: '',
        currentMonth: null,
      },
      taskListData: [],
      taskListLoading: false,
      taskListPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      taskListColumns: [
        {
          title: '订单编号',
          dataIndex: 'orderNo',
          key: 'orderNo',
          width: 150,
          scopedSlots: { customRender: 'orderNo' },
        },
        {
          title: '公司名称',
          dataIndex: 'companyName',
          key: 'companyName',
          width: 200,
          scopedSlots: { customRender: 'companyName' },
        },
        {
          title: '业务类型',
          dataIndex: 'businessType',
          key: 'businessType',
          width: 120,
          scopedSlots: { customRender: 'businessType' },
        },
        {
          title: '任务金额',
          dataIndex: 'costAmount',
          key: 'costAmount',
          width: 120,
          align: 'right',
          scopedSlots: { customRender: 'costAmount' },
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          key: 'createTime',
          width: 180,
          scopedSlots: { customRender: 'createTime' },
        },
        {
          title: '任务状态',
          dataIndex: 'taskStatus',
          key: 'taskStatus',
          width: 100,
          scopedSlots: { customRender: 'taskStatus' },
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
      
      // 添加12个月的列
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
      
      // 添加合计列
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
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return moment(dateTime).format('YYYY-MM-DD HH:mm:ss')
    },
    getTaskStatusColor(status) {
      if (!status) return 'default'
      const statusStr = String(status)
      const colorMap = {
        // 工商任务状态
        'pending_manager_audit': 'orange',
        'public_sea': 'blue',
        'assigned_to_me': 'cyan',
        'task': 'green',
        'handover': 'purple',
        'completed': 'green',
        'problem_task': 'red',
        'recycle_bin': 'default',
        // 审核状态
        'pending': 'orange',
        'approved': 'green',
        'rejected': 'red',
      }
      return colorMap[statusStr] || 'default'
    },
    getTaskStatusText(status) {
      if (!status) return '-'
      const statusStr = String(status)
      const textMap = {
        // 工商任务状态
        'pending_manager_audit': '待经理审核',
        'public_sea': '公海待接收',
        'assigned_to_me': '待本人接收',
        'task': '任务',
        'handover': '交接',
        'completed': '已完成',
        'problem_task': '问题任务',
        'recycle_bin': '回收站',
        // 审核状态
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已驳回',
      }
      return textMap[statusStr] || statusStr
    },
    handleShowTaskList(salesman, month, count) {
      if (!count || count === 0) {
        return
      }
      this.taskListModal.title = `${salesman} - ${month}月任务列表（共${count}单）`
      this.taskListModal.currentSalesman = salesman
      this.taskListModal.currentMonth = month
      this.taskListModal.visible = true
      this.taskListPagination.current = 1
      this.loadTaskList(salesman, month)
    },
    async loadTaskList(salesman, month) {
      this.taskListLoading = true
      try {
        const params = {
          year: this.queryParam.year,
          month: month,
          salesman: salesman,
          businessType: this.queryParam.businessType,
          taskType: this.taskType,
          pageNo: this.taskListPagination.current,
          pageSize: this.taskListPagination.pageSize,
        }
        const res = await getAction('/order/businessTask/getTaskSalesmanMonthlyTasks', params)
        if (res.success && res.result) {
          this.taskListData = res.result.records || []
          this.taskListPagination.total = res.result.total || 0
        } else {
          this.$message.warning(res.message || '获取任务列表失败')
        }
      } catch (error) {
        console.error('加载任务列表失败:', error)
        this.$message.error('加载任务列表失败')
      } finally {
        this.taskListLoading = false
      }
    },
    handleTaskListTableChange(pagination) {
      this.taskListPagination.current = pagination.current
      this.taskListPagination.pageSize = pagination.pageSize
      this.loadTaskList(this.taskListModal.currentSalesman, this.taskListModal.currentMonth)
    },
    handleViewTask(taskId) {
      // 跳转到任务详情或订单详情
      this.$router.push({
        path: '/order/businessTask',
        query: { id: taskId }
      })
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          year: this.queryParam.year,
          month: this.queryParam.month,
          businessType: this.queryParam.businessType,
          taskType: this.taskType,
        }
        // 并行加载统计数据 and 业务人员明细数据
        const [statsRes, detailRes] = await Promise.all([
          getAction('/order/businessTask/getTaskStatistics', params),
          getAction('/order/businessTask/getTaskSalesmanMonthlyDetail', params),
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
          data: ['任务数', '任务金额'],
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
          data: data.map((item) => item.month || item.monthName || '-'),
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
            name: '任务数',
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
            name: '任务金额',
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
            name: '任务数',
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
            itemStyle: {
              color: '#1890ff',
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
              ]),
            },
          },
          {
            name: '任务金额',
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
            name: '任务数',
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
          data: ['任务数', '任务金额'],
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
            name: '任务数',
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
            name: '任务金额',
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
            name: '任务数',
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
            name: '任务金额',
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
  },
}
</script>

<style scoped lang="less">
.task-tab-content {
  .chart-card {
    background: #fff;
    
    /deep/ .ant-card-body {
      padding: 16px;
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
      }

      .ant-table-tbody > tr:hover > td {
        background: #f5f5f5;
      }

      .ant-table-tbody > tr > td {
        padding: 12px 8px;
      }
    }
  }
}
</style>

