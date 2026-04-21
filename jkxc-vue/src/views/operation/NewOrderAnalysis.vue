<template>
  <div class="new-order-analysis">
    <a-card :bordered="false">
      <!-- 查询条件 -->
      <a-form layout="inline" @keyup.enter.native="handleSearch" style="margin-bottom: 16px">
        <a-form-item label="年份">
          <a-select
            v-model="queryParam.year"
            placeholder="请选择年份"
            style="width: 150px"
            @change="handleSearch"
          >
            <a-select-option v-for="i in 11" :key="i" :value="currentYear - 5 + i - 1">
              {{ currentYear - 5 + i - 1 }}年
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="月份">
          <a-select
            v-model="queryParam.month"
            placeholder="请选择月份"
            style="width: 150px"
            @change="handleSearch"
            allowClear
          >
            <a-select-option v-for="i in 12" :key="i" :value="i">
              {{ i }}月
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="业务类型">
          <j-category-select
            v-model="queryParam.businessType"
            placeholder="请选择业务类型"
            pcode="A01"
            back="value"
            style="width: 200px"
            @change="handleSearch"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch" icon="search">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset" icon="reload">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- 统计卡片 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="6">
          <a-card :bordered="false">
            <a-statistic
              title="新签订单数"
              :value="statistics.totalCount"
              suffix="单"
              :value-style="{ color: '#1890ff', fontSize: '24px' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false">
            <a-statistic
              title="新签金额"
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
              title="平均订单金额"
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
          <a-card title="月度新签订单趋势" :bordered="false" class="chart-card">
            <div ref="monthlyTrendChart" style="width: 100%; height: 400px; min-height: 400px;"></div>
          </a-card>
        </a-col>

        <!-- 业务类型分布 -->
        <a-col :span="12" style="margin-bottom: 16px">
          <a-card title="业务类型分布" :bordered="false" class="chart-card">
            <div ref="businessTypeChart" style="width: 100%; height: 350px"></div>
          </a-card>
        </a-col>

        <!-- 业务员排名 -->
        <a-col :span="12" style="margin-bottom: 16px">
          <a-card title="业务员新签排名（Top 10）" :bordered="false" class="chart-card">
            <div ref="salesmanRankChart" style="width: 100%; height: 350px"></div>
          </a-card>
        </a-col>

        <!-- 每日新签趋势 -->
        <a-col :span="24" style="margin-bottom: 16px">
          <a-card title="每日新签订单趋势" :bordered="false" class="chart-card">
            <div ref="dailyTrendChart" style="width: 100%; height: 400px; min-height: 400px;"></div>
          </a-card>
        </a-col>

        <!-- 业务人员月度明细 -->
        <a-col :span="24">
          <a-card title="业务人员月度明细" :bordered="false" class="table-card">
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
                  @click="handleShowOrderList(record.salesman, m, record[`month${m}_count`])"
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
    </a-card>

    <!-- 订单列表弹窗 -->
    <j-modal
      :visible.sync="orderListModal.visible"
      :width="1200"
      :title="orderListModal.title"
      :fullscreen.sync="orderListModal.fullscreen"
      :switchFullscreen="orderListModal.switchFullscreen"
    >
      <a-table
        :columns="orderListColumns"
        :data-source="orderListData"
        :loading="orderListLoading"
        bordered
        size="small"
        rowKey="id"
        :pagination="{
          current: orderListPagination.current,
          pageSize: orderListPagination.pageSize,
          total: orderListPagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
          pageSizeOptions: ['10', '20', '50', '100'],
        }"
        @change="handleOrderListTableChange"
      >
        <template slot="orderNo" slot-scope="text, record">
          <a @click="handleViewOrder(record.id)" style="color: #1890ff; cursor: pointer">
            {{ text }}
          </a>
        </template>
        <template slot="companyName" slot-scope="text">
          <span>{{ text || '-' }}</span>
        </template>
        <template slot="businessType" slot-scope="text, record">
          <span>{{ record.businessType_dictText || record.businessTypeName || record.businessType || '-' }}</span>
        </template>
        <template slot="contractAmount" slot-scope="text">
          <span style="color: #f5222d; font-weight: 600">
            ¥{{ formatAmount(text) }}
          </span>
        </template>
        <template slot="createTime" slot-scope="text">
          {{ formatDateTime(text) }}
        </template>
        <template slot="orderStatus" slot-scope="text, record">
          <a-tag :color="getOrderStatusColor(record.orderStatus)" style="margin: 0">
            {{ getOrderStatusText(record) }}
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
  name: 'NewOrderAnalysis',
  data() {
    const currentYear = new Date().getFullYear()
    return {
      description: '新签订单分析',
      currentYear,
      queryParam: {
        year: currentYear,
        month: null,
        businessType: null,
      },
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
      charts: {
        monthlyTrend: null,
        businessType: null,
        salesmanRank: null,
        dailyTrend: null,
      },
      salesmanMonthlyData: [],
      salesmanMonthlyColumns: [],
      loading: false,
      orderListModal: {
        visible: false,
        title: '',
        fullscreen: false,
        switchFullscreen: true,
        currentSalesman: '',
        currentMonth: null,
      },
      orderListData: [],
      orderListLoading: false,
      orderListPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      orderListColumns: [
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
          title: '合同金额',
          dataIndex: 'contractAmount',
          key: 'contractAmount',
          width: 120,
          align: 'right',
          scopedSlots: { customRender: 'contractAmount' },
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          key: 'createTime',
          width: 180,
          scopedSlots: { customRender: 'createTime' },
        },
        {
          title: '订单状态',
          dataIndex: 'orderStatus',
          key: 'orderStatus',
          width: 100,
          scopedSlots: { customRender: 'orderStatus' },
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
    this.initCharts()
    this.initSalesmanMonthlyColumns()
    this.loadData()
  },
  beforeDestroy() {
    // 移除窗口大小变化监听
    window.removeEventListener('resize', this.handleResize)
    // 销毁图表实例
    Object.values(this.charts).forEach((chart) => {
      if (chart) {
        chart.dispose()
      }
    })
  },
  methods: {
    initCharts() {
      if (!echarts) {
        console.error('ECharts未安装，请先执行: npm install echarts --save')
        return
      }
      // 初始化月度趋势图
      this.charts.monthlyTrend = echarts.init(this.$refs.monthlyTrendChart)
      // 初始化业务类型分布图
      this.charts.businessType = echarts.init(this.$refs.businessTypeChart)
      // 初始化业务员排名图
      this.charts.salesmanRank = echarts.init(this.$refs.salesmanRankChart)
      // 初始化每日趋势图
      this.charts.dailyTrend = echarts.init(this.$refs.dailyTrendChart)

      // 监听窗口大小变化，自动调整图表大小
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
      this.queryParam = {
        year: this.currentYear,
        month: null,
        businessType: null,
      }
      this.loadData()
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
      return this.$createElement('a-table-summary', [
        this.$createElement('a-table-summary-row', cells),
      ])
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return moment(dateTime).format('YYYY-MM-DD HH:mm:ss')
    },
    getOrderStatusColor(status) {
      if (!status && status !== 0 && status !== '0') return 'default'
      const statusStr = String(status)
      // 根据订单状态数字映射颜色
      const colorMap = {
        '0': 'orange',      // 待审核
        '1': 'blue',        // 进行中
        '2': 'green',       // 已完成
        '3': 'default',     // 已取消
        '4': 'red',         // 已驳回
        'pending': 'orange',
        'approved': 'green',
        'rejected': 'red',
        'completed': 'blue',
        '待审核': 'orange',
        '进行中': 'blue',
        '已完成': 'green',
        '已取消': 'default',
        '已驳回': 'red',
      }
      return colorMap[statusStr] || colorMap[status] || 'default'
    },
    getOrderStatusText(record) {
      // 优先使用字典文本
      if (record.orderStatus_dictText) {
        return record.orderStatus_dictText
      }
      // 根据订单状态数字映射中文
      const statusMap = {
        '0': '待审核',
        '1': '进行中',
        '2': '已完成',
        '3': '已取消',
        '4': '已驳回',
        0: '待审核',
        1: '进行中',
        2: '已完成',
        3: '已取消',
        4: '已驳回',
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已拒绝',
        'completed': '已完成',
      }
      const status = record.orderStatus
      return statusMap[status] || statusMap[String(status)] || status || '-'
    },
    handleShowOrderList(salesman, month, count) {
      if (!count || count === 0) {
        return
      }
      this.orderListModal.title = `${salesman} - ${month}月订单列表（共${count}单）`
      this.orderListModal.currentSalesman = salesman
      this.orderListModal.currentMonth = month
      this.orderListModal.visible = true
      this.orderListPagination.current = 1
      this.loadOrderList(salesman, month)
    },
    async loadOrderList(salesman, month) {
      this.orderListLoading = true
      try {
        const params = {
          year: this.queryParam.year,
          month: month,
          salesman: salesman,
          businessType: this.queryParam.businessType,
          pageNo: this.orderListPagination.current,
          pageSize: this.orderListPagination.pageSize,
        }
        const res = await getAction('/order/ghOrder/getSalesmanMonthlyOrders', params)
        if (res.success && res.result) {
          this.orderListData = res.result.records || []
          this.orderListPagination.total = res.result.total || 0
        } else {
          this.$message.warning(res.message || '获取订单列表失败')
        }
      } catch (error) {
        console.error('加载订单列表失败:', error)
        this.$message.error('加载订单列表失败')
      } finally {
        this.orderListLoading = false
      }
    },
    handleOrderListTableChange(pagination) {
      this.orderListPagination.current = pagination.current
      this.orderListPagination.pageSize = pagination.pageSize
      this.loadOrderList(this.orderListModal.currentSalesman, this.orderListModal.currentMonth)
    },
    handleViewOrder(orderId) {
      this.$router.push({
        path: '/order/list',
        query: { id: orderId }
      })
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          year: this.queryParam.year,
          month: this.queryParam.month,
          businessType: this.queryParam.businessType,
        }
        // 并行加载统计数据 and 业务人员明细数据
        const [statsRes, detailRes] = await Promise.all([
          getAction('/order/ghOrder/getNewOrderStatistics', params),
          getAction('/order/ghOrder/getSalesmanMonthlyDetail', params),
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
          console.log('业务人员月度明细数据:', this.salesmanMonthlyData)
          if (this.salesmanMonthlyData.length > 0) {
            console.log('第一条数据:', this.salesmanMonthlyData[0])
          }
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
          data: ['订单数', '订单金额'],
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
            interval: 0, // 强制显示所有标签
            rotate: 0, // 不旋转标签
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '订单数',
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
            name: '订单金额',
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
            name: '订单数',
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
            name: '订单金额',
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
      const data = (this.chartData.salesmanRank || []).slice(0, 10) // Top 10
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
            name: '订单数',
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
          data: ['订单数', '订单金额'],
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
            // 优先使用date字段，如果没有则使用month字段
            const dateValue = item.date || item.month
            if (!dateValue) return '-'
            
            // 如果是日期格式（包含多个-），格式化显示
            if (dateValue.includes('-') && dateValue.split('-').length === 3) {
              const dateParts = dateValue.split('-')
              // 如果选择了月份，显示月-日格式
              if (this.queryParam.month) {
                return `${dateParts[1]}-${dateParts[2]}`
              }
              // 否则显示月-日格式（简化显示）
              return `${dateParts[1]}-${dateParts[2]}`
            }
            // 如果是月份格式（如"1月"），直接使用
            return dateValue
          }),
          axisLine: {
            lineStyle: {
              color: '#d9d9d9',
            },
          },
          axisLabel: {
            color: '#666',
            interval: 0, // 强制显示所有标签
            rotate: 0, // 不旋转标签
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '订单数',
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
            name: '订单金额',
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
            name: '订单数',
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
            name: '订单金额',
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
.new-order-analysis {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
  overflow-x: hidden;
  box-sizing: border-box;

  .ant-card {
    margin-bottom: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    overflow: hidden;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    /deep/ .ant-card-head {
      border-bottom: 2px solid #f0f0f0;
      padding: 12px 16px;

      .ant-card-head-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    /deep/ .ant-card-body {
      padding: 16px;
    }
  }

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
      }

      .ant-table-tbody > tr:hover > td {
        background: #f5f5f5;
      }

      .ant-table-tbody > tr > td {
        padding: 12px 8px;
      }
    }
  }


  // 统计卡片样式优化
  /deep/ .ant-statistic {
    .ant-statistic-title {
      font-size: 14px;
      color: #8c8c8c;
      margin-bottom: 8px;
    }

    .ant-statistic-content {
      font-weight: 600;
    }
  }

  // 查询表单样式
  /deep/ .ant-form {
    background: #fff;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    box-sizing: border-box;

    .ant-form-item {
      margin-bottom: 12px;
    }
  }

  // 按钮样式
  /deep/ .ant-btn {
    border-radius: 4px;
    height: 32px;
    padding: 0 16px;
    font-weight: 500;
  }

  // 订单列表弹窗表格样式
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

