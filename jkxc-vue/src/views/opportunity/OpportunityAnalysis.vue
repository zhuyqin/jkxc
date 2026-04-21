<template>
  <a-card :bordered="false" title="线索数据分析">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="8" :md="12" :sm="24">
            <a-form-item label="统计年度">
              <a-select v-model="queryYear" style="width: 100%" :disabled="useCustomRange" @change="onYearChange">
                <a-select-option v-for="y in yearOptions" :key="y" :value="y">{{ y }} 年</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="4" :lg="6" :md="12" :sm="24">
            <a-form-item label="统计月份">
              <a-select v-model="queryMonth" style="width: 100%" :disabled="useCustomRange">
                <a-select-option :value="0">全年</a-select-option>
                <a-select-option v-for="m in 12" :key="m" :value="m">{{ m }} 月</a-select-option>
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
          <a-col :xl="4" :lg="6" :md="12" :sm="24">
            <a-form-item>
              <a-button type="primary" icon="search" :loading="loading" @click="loadData">查询</a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <a-spin :spinning="loading">
      <a-tabs v-model="activeTab">
        <a-tab-pane key="overview" tab="线索分析">
          <a-row :gutter="16" style="margin-bottom: 16px">
            <a-col :xs="24" :sm="12" :md="8" :lg="4">
              <a-card size="small" :bordered="false" class="stat-mini">
                <a-statistic title="线索总数" :value="num(summary.totalCount)" suffix="条" />
              </a-card>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="5">
              <a-card size="small" :bordered="false" class="stat-mini">
                <a-statistic title="线索奖金总额" :value="num(summary.totalBonus)" :precision="2" prefix="¥" />
              </a-card>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="5">
              <a-card size="small" :bordered="false" class="stat-mini">
                <a-statistic title="客户来源数" :value="num(summary.sourceCount)" suffix="个" />
              </a-card>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="5">
              <a-card size="small" :bordered="false" class="stat-mini">
                <a-statistic title="线索名称数" :value="num(summary.opportunityNameCount)" suffix="个" />
              </a-card>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="5">
              <a-card size="small" :bordered="false" class="stat-mini">
                <a-statistic title="领取人员数" :value="num(summary.receiverCount)" suffix="人" />
              </a-card>
            </a-col>
          </a-row>

          <div v-if="rangeHint" class="range-hint">{{ rangeHint }}</div>

          <a-row :gutter="16" style="margin-top: 16px">
            <a-col :span="24">
              <a-card title="客户来源月度明细" :bordered="false" size="small" style="margin-bottom: 16px">
                <a-table
                  :columns="sourceMonthlyColumns"
                  :data-source="bySourceMonth"
                  :pagination="false"
                  :summary="(pageData) => renderMonthlyMatrixSummary(pageData, 'sourceName', '客户来源')"
                  row-key="sourceName"
                  bordered
                  size="small"
                  :scroll="{ x: 'max-content' }"
                />
              </a-card>

              <a-card title="线索名称月度明细" :bordered="false" size="small" style="margin-bottom: 16px">
                <a-table
                  :columns="nameMonthlyColumns"
                  :data-source="byOpportunityNameMonth"
                  :pagination="false"
                  :summary="(pageData) => renderMonthlyMatrixSummary(pageData, 'opportunityName', '线索名称')"
                  row-key="opportunityName"
                  bordered
                  size="small"
                  :scroll="{ x: 'max-content' }"
                />
              </a-card>

              <a-card title="业务人员月度明细" :bordered="false" size="small" style="margin-bottom: 16px">
                <a-table
                  :columns="receiverMonthlyColumns"
                  :data-source="byReceiverMonth"
                  :pagination="false"
                  :summary="(pageData) => renderMonthlyMatrixSummary(pageData, 'receiverName', '业务人员')"
                  row-key="receiverName"
                  bordered
                  size="small"
                  :scroll="{ x: 'max-content' }"
                />
              </a-card>

              <a-card title="按月份汇总" :bordered="false" size="small">
                <a-table :columns="monthColumns" :data-source="byMonth" :pagination="false" :summary="renderByMonthSummary" row-key="monthKey" size="small" bordered />
              </a-card>
            </a-col>
          </a-row>
        </a-tab-pane>
        <a-tab-pane key="ad" tab="投放分析">
          <div v-if="rangeHint" class="range-hint">{{ rangeHint }}</div>
          <a-table
            style="margin-top: 12px"
            bordered
            size="small"
            row-key="sourceKey"
            :loading="adLoading"
            :columns="adColumns"
            :data-source="adRows"
            :summary="renderAdSummary"
            :pagination="false"
          />
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <a-modal
      :visible="detail.visible"
      :title="detail.title"
      :width="1020"
      :confirmLoading="detail.loading"
      :footer="null"
      @cancel="closeDetail"
      destroyOnClose
    >
      <a-table
        bordered
        size="small"
        :columns="detail.columns"
        :data-source="detail.data"
        :loading="detail.loading"
        row-key="id"
        :pagination="detail.pagination"
        @change="handleDetailTableChange"
        :scroll="{ x: 'max-content' }"
      />
    </a-modal>
  </a-card>
</template>

<script>
import { getAction, postAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'OpportunityAnalysis',
  data() {
    const y = new Date().getFullYear()
    return {
      activeTab: 'overview',
      loading: false,
      adLoading: false,
      queryYear: y,
      queryMonth: 0,
      yearOptions: [],
      dateRange: [],
      useCustomRange: false,
      summary: { totalCount: 0, totalBonus: 0, receiverCount: 0, sourceCount: 0, opportunityNameCount: 0 },
      byMonth: [],
      bySource: [],
      byOpportunityName: [],
      byReceiver: [],
      bySourceMonth: [],
      byOpportunityNameMonth: [],
      byReceiverMonth: [],
      dateBegin: null,
      dateEnd: null,
      rangeHint: '',
      monthColumns: [
        { title: '月份', dataIndex: 'monthKey', width: 120 },
        { title: '线索数', dataIndex: 'cnt', width: 100 },
        { title: '奖金', dataIndex: 'bonusAmount', customRender: (t) => `¥${(Number(t) || 0).toFixed(2)}` },
      ],
      sourceMonthlyColumns: [],
      nameMonthlyColumns: [],
      receiverMonthlyColumns: [],
      adRows: [],
      adCanSave: true,
      adColumns: [
        { title: '线索来源', dataIndex: 'sourceName', width: 200 },
        {
          title: '广告费用',
          dataIndex: 'adCost',
          width: 180,
          customRender: (t, record) => (
            <a-input-number
              min={0}
              precision={2}
              value={Number(t) || 0}
              style="width:120px"
              disabled={!this.adCanSave}
              onChange={(v) => this.changeAdCost(record, v)}
            />
          ),
        },
        { title: '线索数量', dataIndex: 'clueCount', width: 110, align: 'center' },
        { title: '线索成本', dataIndex: 'clueCost', width: 120, align: 'right', customRender: (t) => `¥${(Number(t) || 0).toFixed(2)}` },
        { title: '签约金额', dataIndex: 'signAmount', width: 130, align: 'right', customRender: (t) => `¥${(Number(t) || 0).toFixed(2)}` },
        { title: '投产比', dataIndex: 'roi', width: 120, align: 'right', customRender: (t) => `${(Number(t) || 0).toFixed(2)}` },
        {
          title: '操作',
          key: 'action',
          width: 90,
          align: 'center',
          customRender: (_, record) => (
            <a-button type="link" size="small" disabled={!this.adCanSave} onClick={() => this.saveAdCost(record)}>
              保存
            </a-button>
          ),
        },
      ],
      detail: {
        visible: false,
        title: '明细',
        loading: false,
        dim: '',
        dimValue: '',
        month: null,
        year: null,
        dateBegin: null,
        dateEnd: null,
        data: [],
        columns: [
          { title: '线索编号', dataIndex: 'opportunityNo', width: 160, fixed: 'left' },
          { title: '客户名称', dataIndex: 'corporateName', width: 220 },
          { title: '线索名称', dataIndex: 'opportunityName_dictText', width: 180, customRender: (t, r) => t || r.opportunityName || '-' },
          { title: '客户来源', dataIndex: 'opportunitySource_dictText', width: 140, customRender: (t, r) => t || r.opportunitySource || '-' },
          { title: '业务人员', dataIndex: 'founder', width: 120 },
          { title: '负责人', dataIndex: 'chargePerson', width: 120 },
          { title: '创建时间', dataIndex: 'createTime', width: 170, customRender: (t) => (t ? moment(t).format('YYYY-MM-DD HH:mm:ss') : '-') },
          { title: '修改时间', dataIndex: 'modiTime', width: 170, customRender: (t) => (t ? moment(t).format('YYYY-MM-DD HH:mm:ss') : '-') },
          {
            title: '线索奖金',
            dataIndex: 'clueBonus',
            width: 120,
            align: 'right',
            customRender: (t) => `¥${(Number(t) || 0).toFixed(2)}`,
          },
        ],
        pagination: { current: 1, pageSize: 20, total: 0, showSizeChanger: true, showQuickJumper: true },
      },
    }
  },
  mounted() {
    for (let i = 0; i < 8; i++) this.yearOptions.push(new Date().getFullYear() - i)
    this.initSourceMonthlyColumns()
    this.initNameMonthlyColumns()
    this.initReceiverMonthlyColumns()
    this.$nextTick(() => this.loadData())
  },
  methods: {
    num(v) {
      const n = Number(v)
      return Number.isFinite(n) ? n : 0
    },
    onYearChange() {
      this.useCustomRange = false
      this.dateRange = []
    },
    onRangeChange(dates) {
      this.useCustomRange = !!(dates && dates.length === 2 && dates[0] && dates[1])
    },
    loadData() {
      this.loading = true
      const params = {}
      if (this.useCustomRange && this.dateRange && this.dateRange.length === 2) {
        params.dateBegin = moment(this.dateRange[0]).format('YYYY-MM-DD')
        params.dateEnd = moment(this.dateRange[1]).format('YYYY-MM-DD')
      } else {
        params.year = this.queryYear
        if (this.queryMonth && this.queryMonth > 0) {
          params.month = this.queryMonth
        }
      }
      getAction('/opportunity/ghOpportunity/analytics', params)
        .then((res) => {
          if (!res.success) {
            this.$message.warning(res.message || '加载失败')
            return
          }
          const r = res.result || {}
          this.summary = {
            totalCount: this.num(r.summary && r.summary.totalCount),
            totalBonus: this.num(r.summary && r.summary.totalBonus),
            receiverCount: this.num(r.summary && r.summary.receiverCount),
            sourceCount: this.num(r.summary && r.summary.sourceCount),
            opportunityNameCount: this.num(r.summary && r.summary.opportunityNameCount),
          }
          this.byMonth = Array.isArray(r.byMonth) ? r.byMonth : []
          this.bySource = Array.isArray(r.bySource) ? r.bySource : []
          this.byOpportunityName = Array.isArray(r.byOpportunityName) ? r.byOpportunityName : []
          this.byReceiver = Array.isArray(r.byReceiver) ? r.byReceiver : []
          this.bySourceMonth = Array.isArray(r.bySourceMonth) ? r.bySourceMonth : []
          this.byOpportunityNameMonth = Array.isArray(r.byOpportunityNameMonth) ? r.byOpportunityNameMonth : []
          this.byReceiverMonth = Array.isArray(r.byReceiverMonth) ? r.byReceiverMonth : []
          this.dateBegin = r.dateBegin
          this.dateEnd = r.dateEnd
          this.rangeHint = r.dateBegin && r.dateEnd
            ? `统计区间：${moment(r.dateBegin).format('YYYY-MM-DD')} ~ ${moment(r.dateEnd).format('YYYY-MM-DD')}`
            : ''
        })
        .catch(() => this.$message.error('加载数据分析失败'))
        .finally(() => {
          this.loading = false
          this.loadAdData()
        })
    },
    loadAdData() {
      this.adLoading = true
      const params = {}
      if (this.useCustomRange && this.dateRange && this.dateRange.length === 2) {
        params.dateBegin = moment(this.dateRange[0]).format('YYYY-MM-DD')
        params.dateEnd = moment(this.dateRange[1]).format('YYYY-MM-DD')
      } else {
        params.year = this.queryYear
        params.month = this.queryMonth
      }
      getAction('/opportunity/ghOpportunity/analyticsAd', params)
        .then((res) => {
          if (!res || !res.success) {
            this.$message.warning((res && res.message) || '加载投放分析失败')
            return
          }
          const r = res.result || {}
          this.adRows = Array.isArray(r.rows) ? r.rows : []
          this.adCanSave = !!r.canSaveAdCost
          if (!this.adCanSave) {
            this.$message.info('自定义区间仅展示，不支持保存广告费用，请使用按年/月查询后保存。')
          }
        })
        .catch(() => this.$message.error('加载投放分析失败'))
        .finally(() => {
          this.adLoading = false
        })
    },
    renderMonthlyMatrixSummary(pageData, firstField, firstLabel) {
      const sum = (key) => pageData.reduce((acc, row) => acc + (Number(row[key]) || 0), 0)
      const cells = [this.$createElement('td', { style: { fontWeight: 600 } }, [`${firstLabel}合计`])]
      for (let m = 1; m <= 12; m++) {
        cells.push(this.$createElement('td', { style: { textAlign: 'center', fontWeight: 600 } }, [String(sum(`month${m}_count`))]))
        cells.push(this.$createElement('td', { style: { textAlign: 'right', fontWeight: 600 } }, [`¥${(sum(`month${m}_amount`)).toFixed(2)}`]))
      }
      cells.push(this.$createElement('td', { style: { textAlign: 'center', fontWeight: 700 } }, [String(sum('totalCount'))]))
      cells.push(this.$createElement('td', { style: { textAlign: 'right', fontWeight: 700 } }, [`¥${(sum('totalAmount')).toFixed(2)}`]))
      return this.$createElement('a-table-summary', [this.$createElement('a-table-summary-row', cells)])
    },
    renderByMonthSummary(pageData) {
      const totalCnt = pageData.reduce((acc, row) => acc + (Number(row.cnt) || 0), 0)
      const totalAmt = pageData.reduce((acc, row) => acc + (Number(row.bonusAmount) || 0), 0)
      const cells = [
        this.$createElement('td', { style: { fontWeight: 600 } }, ['合计']),
        this.$createElement('td', { style: { textAlign: 'center', fontWeight: 700 } }, [String(totalCnt)]),
        this.$createElement('td', { style: { textAlign: 'right', fontWeight: 700 } }, [`¥${totalAmt.toFixed(2)}`]),
      ]
      return this.$createElement('a-table-summary', [this.$createElement('a-table-summary-row', cells)])
    },
    renderAdSummary(pageData) {
      const sum = (key) => pageData.reduce((acc, row) => acc + (Number(row[key]) || 0), 0)
      const totalAd = sum('adCost')
      const totalClue = sum('clueCount')
      const totalSign = sum('signAmount')
      const totalCost = sum('clueCost')
      const totalRoi = totalAd > 0 ? totalSign / totalAd : 0
      const cells = [
        this.$createElement('td', { style: { fontWeight: 600 } }, ['合计']),
        this.$createElement('td', { style: { textAlign: 'right', fontWeight: 700 } }, [`¥${totalAd.toFixed(2)}`]),
        this.$createElement('td', { style: { textAlign: 'center', fontWeight: 700 } }, [String(totalClue)]),
        this.$createElement('td', { style: { textAlign: 'right', fontWeight: 700 } }, [`¥${totalCost.toFixed(2)}`]),
        this.$createElement('td', { style: { textAlign: 'right', fontWeight: 700 } }, [`¥${totalSign.toFixed(2)}`]),
        this.$createElement('td', { style: { textAlign: 'right', fontWeight: 700 } }, [totalRoi.toFixed(2)]),
        this.$createElement('td', { style: { textAlign: 'center' } }, ['-']),
      ]
      return this.$createElement('a-table-summary', [this.$createElement('a-table-summary-row', cells)])
    },
    changeAdCost(record, v) {
      this.$set(record, 'adCost', Number(v) || 0)
      const adCost = Number(record.adCost) || 0
      const sign = Number(record.signAmount) || 0
      this.$set(record, 'roi', adCost > 0 ? Number((sign / adCost).toFixed(4)) : 0)
    },
    saveAdCost(record) {
      if (!this.adCanSave) return
      const params = {
        year: this.queryYear,
        month: this.queryMonth || 0,
        sourceKey: record.sourceKey,
        sourceName: record.sourceName,
        adCost: Number(record.adCost) || 0,
      }
      postAction('/opportunity/ghOpportunity/saveAdCost', params).then((res) => {
        if (res && res.success) {
          this.$message.success('保存成功')
          this.loadAdData()
        } else {
          this.$message.warning((res && res.message) || '保存失败')
        }
      }).catch(() => this.$message.error('保存失败'))
    },
    initSourceMonthlyColumns() {
      this.sourceMonthlyColumns = this.buildMonthlyColumns('sourceName', '客户来源')
    },
    initNameMonthlyColumns() {
      this.nameMonthlyColumns = this.buildMonthlyColumns('opportunityName', '线索名称')
    },
    initReceiverMonthlyColumns() {
      this.receiverMonthlyColumns = this.buildMonthlyColumns('receiverName', '业务人员')
    },
    buildMonthlyColumns(firstDataIndex, firstTitle) {
      const vm = this
      const cols = [
        { title: firstTitle, dataIndex: firstDataIndex, key: firstDataIndex, width: 160, fixed: 'left' },
      ]
      for (let m = 1; m <= 12; m++) {
        cols.push({
          title: `${m}月`,
          key: `month${m}`,
          align: 'center',
          children: [
            {
              title: '线索数',
              dataIndex: `month${m}_count`,
              key: `month${m}_count`,
              width: 80,
              align: 'center',
              customRender: (t, record) => {
                const n = Number(t) || 0
                if (!n) return '0'
                return (
                  <a onClick={() => vm.openDetail(firstDataIndex, record[firstDataIndex], m)}>
                    {n}
                  </a>
                )
              },
            },
            {
              title: '金额',
              dataIndex: `month${m}_amount`,
              key: `month${m}_amount`,
              width: 120,
              align: 'right',
              customRender: (t, record) => {
                const amt = Number(t) || 0
                const txt = `¥${amt.toFixed(2)}`
                const n = Number(record && record[`month${m}_count`]) || 0
                if (!n) return txt
                return (
                  <a onClick={() => vm.openDetail(firstDataIndex, record[firstDataIndex], m)}>
                    {txt}
                  </a>
                )
              },
            },
          ],
        })
      }
      cols.push({
        title: '合计',
        key: 'total',
        fixed: 'right',
        children: [
          {
            title: '线索数',
            dataIndex: 'totalCount',
            key: 'totalCount',
            width: 90,
            align: 'center',
            customRender: (t, record) => {
              const n = Number(t) || 0
              if (!n) return '0'
              return (
                <a onClick={() => vm.openDetail(firstDataIndex, record[firstDataIndex], null)}>
                  {n}
                </a>
              )
            },
          },
          {
            title: '金额',
            dataIndex: 'totalAmount',
            key: 'totalAmount',
            width: 130,
            align: 'right',
            customRender: (t, record) => {
              const amt = Number(t) || 0
              const txt = `¥${amt.toFixed(2)}`
              const n = Number(record && record.totalCount) || 0
              if (!n) return txt
              return (
                <a onClick={() => vm.openDetail(firstDataIndex, record[firstDataIndex], null)}>
                  {txt}
                </a>
              )
            },
          },
        ],
      })
      return cols
    },

    dimToApi(firstDataIndex) {
      if (firstDataIndex === 'sourceName') return 'source'
      if (firstDataIndex === 'opportunityName') return 'name'
      if (firstDataIndex === 'receiverName') return 'receiver'
      return ''
    },
    openDetail(firstDataIndex, dimValue, month) {
      const dim = this.dimToApi(firstDataIndex)
      if (!dim) return
      const y = this.useCustomRange && this.dateBegin ? moment(this.dateBegin).year() : this.queryYear
      this.detail.visible = true
      this.detail.dim = dim
      this.detail.dimValue = dimValue || '未填写'
      this.detail.month = month
      this.detail.year = y
      this.detail.dateBegin = this.dateBegin
      this.detail.dateEnd = this.dateEnd
      this.detail.pagination.current = 1
      const monthTxt = month ? `${month}月` : '合计'
      this.detail.title = `明细 - ${monthTxt} - ${dimValue || '未填写'}`
      this.loadDetail()
    },
    closeDetail() {
      this.detail.visible = false
      this.detail.data = []
    },
    handleDetailTableChange(pagination) {
      this.detail.pagination.current = pagination.current
      this.detail.pagination.pageSize = pagination.pageSize
      this.loadDetail()
    },
    loadDetail() {
      if (!this.detail.visible) return
      this.detail.loading = true
      const params = {
        dim: this.detail.dim,
        dimValue: this.detail.dimValue,
        year: this.detail.year,
        month: this.detail.month || undefined,
        pageNo: this.detail.pagination.current,
        pageSize: this.detail.pagination.pageSize,
      }
      // 与 analytics 保持一致的区间
      if (this.detail.dateBegin && this.detail.dateEnd) {
        params.dateBegin = moment(this.detail.dateBegin).format('YYYY-MM-DD')
        params.dateEnd = moment(this.detail.dateEnd).format('YYYY-MM-DD')
      }
      getAction('/opportunity/ghOpportunity/analyticsDetail', params)
        .then((res) => {
          if (!res || !res.success) {
            this.$message.warning((res && res.message) || '加载明细失败')
            return
          }
          const r = res.result || {}
          this.detail.data = r.records || r || []
          this.detail.pagination.total = r.total || 0
        })
        .catch(() => this.$message.error('加载明细失败'))
        .finally(() => (this.detail.loading = false))
    },
  },
}
</script>

<style scoped>
.stat-mini {
  background: #fafafa;
}
.range-hint {
  margin: 0 0 12px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}
</style>
