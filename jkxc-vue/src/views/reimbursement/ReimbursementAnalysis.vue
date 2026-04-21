<template>
  <a-card :bordered="false" title="报销数据分析">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="统计年度">
              <a-select
                v-model="queryYear"
                placeholder="选择年度"
                style="width: 100%"
                :disabled="useCustomRange"
                @change="onYearChange"
              >
                <a-select-option v-for="y in yearOptions" :key="y" :value="y">{{ y }} 年</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="10" :lg="10" :md="12" :sm="24">
            <a-form-item label="自定义区间">
              <a-range-picker
                v-model="dateRange"
                style="width: 100%"
                format="YYYY-MM-DD"
                :placeholder="['开始', '结束']"
                @change="onRangeChange"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="数据范围">
              <a-select v-model="searchStatus" style="width: 100%">
                <a-select-option value="1">全部（不含地址费用、人工提成）</a-select-option>
                <a-select-option value="2">仅人工提成</a-select-option>
                <a-select-option value="3">仅地址费用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="4" :lg="8" :md="12" :sm="24">
            <a-form-item>
              <a-button type="primary" icon="search" :loading="loading" @click="loadData">查询</a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <a-spin :spinning="loading">
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small" :bordered="false" class="stat-mini">
            <a-statistic title="报销总额" :value="num(summary.totalAmount)" :precision="2" prefix="¥" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small" :bordered="false" class="stat-mini">
            <a-statistic title="笔数" :value="num(summary.totalCount)" suffix="笔" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small" :bordered="false" class="stat-mini">
            <a-statistic title="已审核金额" :value="num(summary.approvedAmount)" :precision="2" prefix="¥" value-style="color:#52c41a" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small" :bordered="false" class="stat-mini">
            <a-statistic title="待审核金额" :value="num(summary.pendingAmount)" :precision="2" prefix="¥" value-style="color:#fa8c16" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small" :bordered="false" class="stat-mini">
            <a-statistic title="已拒绝金额" :value="num(summary.rejectedAmount)" :precision="2" prefix="¥" value-style="color:#f5222d" />
          </a-card>
        </a-col>
      </a-row>

      <div v-if="rangeHint" class="range-hint">{{ rangeHint }}</div>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="24" :lg="12">
          <a-card title="按报销类目分布" :bordered="false" size="small">
            <div ref="categoryChart" class="chart-box"></div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card title="按报销人 Top20（金额）" :bordered="false" size="small">
            <div ref="personChart" class="chart-box"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="24">
          <a-card :bordered="false" size="small" class="category-month-table-card">
            <template slot="title">
              <span>报销类目月度明细</span>
              <span v-if="chartYear" class="chart-subtitle">（{{ chartYear }}年 1～12 月，与上方筛选区间求交；每行一个类目）</span>
            </template>
            <a-table
              :columns="categoryMonthlyColumns"
              :data-source="categoryMonthlyData"
              :pagination="false"
              :scroll="{ x: 'max-content' }"
              :loading="loading"
              row-key="categoryRowKey"
              bordered
              size="middle"
            >
              <template slot="categoryName" slot-scope="text">
                <a-tag color="blue">{{ text }}</a-tag>
              </template>
              <template
                v-for="m in 12"
                :slot="`month${m}_count`"
                slot-scope="text, record"
              >
                <a
                  v-if="record.categoryId"
                  :key="'c-' + m"
                  class="cell-link-count"
                  @click="handleCategoryMonthDrill(record, m)"
                >
                  {{ record[`month${m}_count`] || 0 }}
                </a>
                <span v-else :key="'c2-' + m" class="cell-plain-count">{{ record[`month${m}_count`] || 0 }}</span>
              </template>
              <template
                v-for="m in 12"
                :slot="`month${m}_amount`"
                slot-scope="text, record"
              >
                <span :key="'a-' + m" class="cell-amount">¥{{ formatAmount(record[`month${m}_amount`]) }}</span>
              </template>
              <template slot="totalCount" slot-scope="text, record">
                <span class="cell-total-count">{{ record.totalCount || 0 }}</span>
              </template>
              <template slot="totalAmount" slot-scope="text, record">
                <span class="cell-total-amount">¥{{ formatAmount(record.totalAmount) }}</span>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <j-modal
      :visible.sync="detailModal.visible"
      :title="detailModal.title"
      :width="1100"
      :footer="null"
      :switchFullscreen="true"
      @cancel="closeDetailModal"
    >
      <a-table
        :columns="reimbursementDetailColumns"
        :data-source="reimbursementDetailList"
        :loading="reimbursementDetailLoading"
        row-key="id"
        bordered
        size="small"
        :pagination="{
          current: reimbursementDetailPagination.current,
          pageSize: reimbursementDetailPagination.pageSize,
          total: reimbursementDetailPagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (t) => `共 ${t} 条`,
        }"
        @change="handleDetailTableChange"
      />
    </j-modal>
  </a-card>
</template>

<script>
import { getAction } from '@/api/manage'
import moment from 'moment'
import * as echarts from 'echarts'

export default {
  name: 'ReimbursementAnalysis',
  data() {
    const y = new Date().getFullYear()
    return {
      loading: false,
      queryYear: y,
      yearOptions: [],
      dateRange: [],
      useCustomRange: false,
      searchStatus: '1',
      summary: {
        totalAmount: 0,
        totalCount: 0,
        approvedAmount: 0,
        pendingAmount: 0,
        rejectedAmount: 0,
      },
      byCategory: [],
      byMonth: [],
      byCategoryMonth: [],
      chartYear: null,
      byPerson: [],
      dateBegin: null,
      dateEnd: null,
      rangeHint: '',
      charts: {
        category: null,
        person: null,
      },
      categoryMonthlyColumns: [],
      categoryMonthlyData: [],
      detailModal: {
        visible: false,
        title: '',
        categoryId: '',
        month: null,
      },
      reimbursementDetailList: [],
      reimbursementDetailLoading: false,
      reimbursementDetailPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      reimbursementDetailColumns: [
        {
          title: '报销时间',
          dataIndex: 'reimbursementTime',
          width: 110,
          customRender: (t) => (t ? moment(t).format('YYYY-MM-DD') : '-'),
        },
        {
          title: '报销人员',
          dataIndex: 'reimbursementPerson',
          width: 100,
          customRender: (text, record) => record.reimbursementPerson_dictText || text || '-',
        },
        {
          title: '类目',
          dataIndex: 'category',
          width: 120,
          customRender: (text, record) => record.category_dictText || text || '-',
        },
        { title: '公司名称', dataIndex: 'companyName', ellipsis: true },
        {
          title: '金额',
          dataIndex: 'totalPrice',
          width: 120,
          align: 'right',
          customRender: (t) => {
            const n = typeof t === 'number' ? t : parseFloat(t)
            const x = Number.isFinite(n) ? n : 0
            const s = x.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            return `¥${s}`
          },
        },
        {
          title: '审核',
          dataIndex: 'auditFlag',
          width: 90,
          customRender: (t) => {
            if (t === '1') return '已审核'
            if (t === '2') return '已拒绝'
            return '待审核'
          },
        },
      ],
    }
  },
  mounted() {
    for (let i = 0; i < 8; i++) {
      this.yearOptions.push(new Date().getFullYear() - i)
    }
    this.initCategoryMonthlyColumns()
    this.$nextTick(() => {
      this.initCharts()
      this.loadData()
    })
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    Object.keys(this.charts).forEach((k) => {
      if (this.charts[k]) {
        this.charts[k].dispose()
        this.charts[k] = null
      }
    })
  },
  methods: {
    num(v) {
      if (v == null || v === '') return 0
      const n = Number(v)
      return Number.isFinite(n) ? n : 0
    },
    onYearChange() {
      this.useCustomRange = false
      this.dateRange = []
    },
    onRangeChange(dates) {
      if (dates && dates.length === 2 && dates[0] && dates[1]) {
        this.useCustomRange = true
      } else {
        this.useCustomRange = false
      }
    },
    initCharts() {
      if (this.$refs.categoryChart) {
        this.charts.category = echarts.init(this.$refs.categoryChart)
      }
      if (this.$refs.personChart) {
        this.charts.person = echarts.init(this.$refs.personChart)
      }
    },
    handleResize() {
      Object.keys(this.charts).forEach((k) => {
        if (this.charts[k]) this.charts[k].resize()
      })
    },
    pickSummary(raw) {
      if (!raw || typeof raw !== 'object') {
        return {
          totalAmount: 0,
          totalCount: 0,
          approvedAmount: 0,
          pendingAmount: 0,
          rejectedAmount: 0,
        }
      }
      return {
        totalAmount: this.num(raw.totalAmount != null ? raw.totalAmount : raw.TOTALAMOUNT),
        totalCount: this.num(raw.totalCount != null ? raw.totalCount : raw.TOTALCOUNT),
        approvedAmount: this.num(raw.approvedAmount != null ? raw.approvedAmount : raw.APPROVEDAMOUNT),
        pendingAmount: this.num(raw.pendingAmount != null ? raw.pendingAmount : raw.PENDINGAMOUNT),
        rejectedAmount: this.num(raw.rejectedAmount != null ? raw.rejectedAmount : raw.REJECTEDAMOUNT),
      }
    },
    loadData() {
      this.loading = true
      const params = { searchStatus: this.searchStatus }
      if (this.useCustomRange && this.dateRange && this.dateRange.length === 2) {
        params.dateBegin = moment(this.dateRange[0]).format('YYYY-MM-DD')
        params.dateEnd = moment(this.dateRange[1]).format('YYYY-MM-DD')
      } else {
        params.year = this.queryYear
      }
      getAction('/GhReimbursement/ghReimbursement/analytics', params)
        .then((res) => {
          if (!res.success) {
            this.$message.warning(res.message || '加载失败')
            return
          }
          const r = res.result || {}
          this.summary = this.pickSummary(r.summary)
          this.byCategory = Array.isArray(r.byCategory) ? r.byCategory : []
          this.byMonth = Array.isArray(r.byMonth) ? r.byMonth : []
          this.byCategoryMonth = Array.isArray(r.byCategoryMonth) ? r.byCategoryMonth : []
          this.chartYear = r.chartYear != null ? Number(r.chartYear) : null
          this.byPerson = Array.isArray(r.byPerson) ? r.byPerson : []
          this.dateBegin = r.dateBegin
          this.dateEnd = r.dateEnd
          if (r.dateBegin && r.dateEnd) {
            const a = moment(r.dateBegin).format('YYYY-MM-DD')
            const b = moment(r.dateEnd).format('YYYY-MM-DD')
            this.rangeHint = `统计区间：${a} ~ ${b}（按报销时间）`
          } else {
            this.rangeHint = ''
          }
          this.buildCategoryMonthlyTableData()
          this.$nextTick(() => {
            if ((!this.charts.category && this.$refs.categoryChart) || (!this.charts.person && this.$refs.personChart)) {
              this.initCharts()
            }
            this.updateCharts()
          })
        })
        .catch((e) => {
          console.error(e)
          this.$message.error('加载数据分析失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    rowAmount(row) {
      return this.num(row.amount != null ? row.amount : row.AMOUNT)
    },
    rowName(row, key, alt) {
      const v = row[key] != null ? row[key] : row[alt]
      return v != null ? String(v) : '-'
    },
    updateCharts() {
      if (this.charts.category) {
        const data = this.byCategory.map((row) => ({
          name: this.rowName(row, 'categoryName', 'CATEGORYNAME') || '未分类',
          value: this.rowAmount(row),
        }))
        this.charts.category.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
          legend: { type: 'scroll', bottom: 0 },
          series: [
            {
              type: 'pie',
              radius: ['32%', '62%'],
              avoidLabelOverlap: true,
              data: data.length ? data : [{ name: '暂无数据', value: 1 }],
            },
          ],
        })
      }
      if (this.charts.person) {
        const list = [...this.byPerson].sort((a, b) => this.rowAmount(b) - this.rowAmount(a))
        const names = list.map((row) => this.rowName(row, 'personName', 'PERSONNAME'))
        const vals = list.map((row) => this.rowAmount(row))
        this.charts.person.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: 80, right: 24, top: 16, bottom: 24 },
          xAxis: { type: 'value', name: '金额(元)' },
          yAxis: { type: 'category', data: names.length ? names : ['暂无'], inverse: true },
          series: [
            {
              type: 'bar',
              data: vals.length ? vals : [0],
              itemStyle: { color: '#1890ff' },
            },
          ],
        })
      }
      this.handleResize()
    },
    initCategoryMonthlyColumns() {
      const columns = [
        {
          title: '报销类目',
          dataIndex: 'categoryName',
          key: 'categoryName',
          width: 140,
          fixed: 'left',
          scopedSlots: { customRender: 'categoryName' },
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
              width: 72,
              align: 'center',
              scopedSlots: { customRender: `month${m}_count` },
            },
            {
              title: '金额',
              dataIndex: `month${m}_amount`,
              key: `month${m}_amount`,
              width: 118,
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
            width: 88,
            align: 'center',
            scopedSlots: { customRender: 'totalCount' },
          },
          {
            title: '金额',
            dataIndex: 'totalAmount',
            key: 'totalAmount',
            width: 132,
            align: 'right',
            scopedSlots: { customRender: 'totalAmount' },
          },
        ],
      })
      this.categoryMonthlyColumns = columns
    },
    formatAmount(amount) {
      if (amount == null || amount === '') return '0.00'
      const n = typeof amount === 'number' ? amount : parseFloat(amount)
      const x = Number.isFinite(n) ? n : 0
      return x.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    rowMonthNum(row) {
      const v = row.monthNum != null ? row.monthNum : row.MONTHNUM
      const n = Number(v)
      return Number.isFinite(n) ? n : null
    },
    rowCategoryName(row) {
      return this.rowName(row, 'categoryName', 'CATEGORYNAME') || '未分类'
    },
    rowCount(row) {
      return this.num(row.cnt != null ? row.cnt : row.CNT)
    },
    buildCategoryMonthlyTableData() {
      const raw = this.byCategoryMonth || []
      const map = {}
      const baseRow = (categoryId, categoryName) => {
        const row = {
          categoryId: categoryId || '',
          categoryName: categoryName || '未分类',
          categoryRowKey: (categoryId || '') + '::' + (categoryName || ''),
          totalCount: 0,
          totalAmount: 0,
        }
        for (let m = 1; m <= 12; m++) {
          row[`month${m}_count`] = 0
          row[`month${m}_amount`] = 0
        }
        return row
      }
      raw.forEach((r) => {
        const name = this.rowCategoryName(r)
        let id = r.categoryId != null ? String(r.categoryId) : r.CATEGORYID != null ? String(r.CATEGORYID) : ''
        const key = id ? `id:${id}` : `n:${name}`
        if (!map[key]) {
          map[key] = baseRow(id, name)
        }
        const m = this.rowMonthNum(r)
        if (m == null || m < 1 || m > 12) return
        const cnt = this.rowCount(r)
        const amt = this.rowAmount(r)
        map[key][`month${m}_count`] += cnt
        map[key][`month${m}_amount`] += amt
        map[key].totalCount += cnt
        map[key].totalAmount += amt
      })
      this.categoryMonthlyData = Object.values(map).sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0))
    },
    handleCategoryMonthDrill(record, month) {
      const c = record[`month${month}_count`]
      if (!c) return
      if (!record.categoryId) {
        this.$message.info('该类目无编号，无法打开明细')
        return
      }
      this.detailModal.categoryId = record.categoryId
      this.detailModal.month = month
      this.detailModal.title = `${record.categoryName} · ${month}月 · 报销明细`
      this.detailModal.visible = true
      this.reimbursementDetailPagination.current = 1
      this.loadReimbursementDetailList()
    },
    closeDetailModal() {
      this.detailModal.visible = false
    },
    loadReimbursementDetailList() {
      const y = this.chartYear || this.queryYear
      const m = this.detailModal.month
      if (m == null) return
      const monthStart = moment([y, m - 1, 1])
      const monthEnd = moment(monthStart).endOf('month')
      let rangeStart = monthStart.clone()
      let rangeEnd = monthEnd.clone()
      if (this.dateBegin) {
        const db = moment(this.dateBegin).startOf('day')
        if (db.isAfter(rangeStart)) rangeStart = db
      }
      if (this.dateEnd) {
        const de = moment(this.dateEnd).endOf('day')
        if (de.isBefore(rangeEnd)) rangeEnd = de
      }
      this.reimbursementDetailLoading = true
      const params = {
        pageNo: this.reimbursementDetailPagination.current,
        pageSize: this.reimbursementDetailPagination.pageSize,
        searchStatus: this.searchStatus,
        category: this.detailModal.categoryId,
        reimbursementTime_begin: rangeStart.format('YYYY-MM-DD HH:mm:ss'),
        reimbursementTime_end: rangeEnd.format('YYYY-MM-DD HH:mm:ss'),
      }
      getAction('/GhReimbursement/ghReimbursement/list', params)
        .then((res) => {
          if (res.success && res.result) {
            this.reimbursementDetailList = res.result.records || []
            this.reimbursementDetailPagination.total = res.result.total || 0
          } else {
            this.reimbursementDetailList = []
            this.reimbursementDetailPagination.total = 0
            this.$message.warning(res.message || '加载明细失败')
          }
        })
        .catch(() => {
          this.reimbursementDetailList = []
          this.reimbursementDetailPagination.total = 0
          this.$message.error('加载明细失败')
        })
        .finally(() => {
          this.reimbursementDetailLoading = false
        })
    },
    handleDetailTableChange(pagination) {
      this.reimbursementDetailPagination.current = pagination.current
      this.reimbursementDetailPagination.pageSize = pagination.pageSize
      this.loadReimbursementDetailList()
    },
  },
}
</script>

<style scoped>
.stat-mini {
  background: #fafafa;
}
.chart-box {
  width: 100%;
  height: 340px;
}
.category-month-table-card {
  margin-bottom: 8px;
}
.cell-link-count {
  color: #1890ff;
  font-weight: 500;
  cursor: pointer;
  text-decoration: underline;
}
.cell-plain-count {
  color: rgba(0, 0, 0, 0.65);
}
.cell-amount {
  color: #52c41a;
  font-weight: 500;
}
.cell-total-count {
  color: #1890ff;
  font-weight: 600;
  font-size: 14px;
}
.cell-total-amount {
  color: #52c41a;
  font-weight: 600;
  font-size: 14px;
}
.range-hint {
  margin: 0 0 12px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}
.chart-subtitle {
  margin-left: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-weight: normal;
  font-size: 13px;
}
</style>
