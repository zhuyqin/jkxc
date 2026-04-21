<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="合同编号">
              <a-input placeholder="请输入合同编号" v-model="queryParam.contractNo" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="订单编号">
              <JInput placeholder="请输入订单编号" v-model="queryParam.orderNo" type="like" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="服务人员">
              <j-search-select-tag
                placeholder="请选择"
                v-model="queryParam.servicePerson"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="到期时间">
              <a-range-picker
                style="width: 210px"
                v-model="queryParam.expireDateRange"
                format="YYYY-MM-DD"
                @change="onExpireDateChange"
                :placeholder="['开始时间', '结束时间']"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- Tab页区域 -->
    <a-tabs default-active-key="serving" @change="handleTabChange" style="margin-top: 16px; margin-bottom: 16px;">
      <a-tab-pane key="serving">
        <span slot="tab">服务中 ({{ tabCounts.serving }})</span>
      </a-tab-pane>
      <a-tab-pane key="currentRenewal">
        <span slot="tab">当期续费 ({{ tabCounts.currentRenewal }})</span>
      </a-tab-pane>
      <a-tab-pane key="t2OverdueRenewal">
        <span slot="tab">t-2逾期续费 ({{ tabCounts.t2OverdueRenewal }})</span>
      </a-tab-pane>
      <a-tab-pane key="t6ExpectedRenewal">
        <span slot="tab">t+6预期续费 ({{ tabCounts.t6ExpectedRenewal }})</span>
      </a-tab-pane>
      <a-tab-pane key="t3OverdueCustomer">
        <span slot="tab">t-3逾期客户 ({{ tabCounts.t3OverdueCustomer }})</span>
      </a-tab-pane>
      <a-tab-pane key="lossAudit">
        <span slot="tab">流失审核 ({{ tabCounts.lossAudit }})</span>
        <loss-audit-list 
          ref="lossAuditList" 
          v-if="activeTabKey === 'lossAudit'"
          :parentQueryParam="queryParam"
        ></loss-audit-list>
      </a-tab-pane>
      <a-tab-pane key="lossCustomer">
        <span slot="tab">流失客户 ({{ tabCounts.lossCustomer }})</span>
      </a-tab-pane>
    </a-tabs>

    <!-- 表格区域 -->
    <a-table
      v-if="activeTabKey !== 'lossAudit'"
      ref="table"
      size="middle"
      :scroll="{ x: true }"
      bordered
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="countdownDays" slot-scope="text, record">
        <a-tag class="countdown-tag" :class="getCountdownClass(record.countdownDays)">
          {{ record.countdownDays !== null && record.countdownDays !== undefined ? record.countdownDays + '天' : '-' }}
        </a-tag>
      </span>
      <span slot="serviceAndAudit" slot-scope="text, record">
        <a-button type="link" size="small" class="svc-view-btn" @click="openServicePersonnelModal(record)">查看</a-button>
      </span>
      <span slot="totalSpending" slot-scope="text">
        <span v-if="text" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="isRecurring" slot-scope="text, record">
        <a-tag 
          :color="getIsRecurringColor(record.customerOrderCount)"
          style="cursor: pointer;"
          @click="handleRecurringClick(record)"
        >
          {{ getIsRecurringText(record) }}
        </a-tag>
      </span>
      <span slot="enterpriseLevel" slot-scope="text, record">
        <a-tag 
          v-if="record.enterpriseLevel_dictText || text" 
          color="gold"
          style="cursor: pointer;"
          @click="handleEnterpriseLevelClick(record)"
        >
          {{ record.enterpriseLevel_dictText || (text === '1' ? '一星' : text === '2' ? '二星' : text === '3' ? '三星' : text === '4' ? '四星' : text === '5' ? '五星' : text) }}
        </a-tag>
        <span v-else>-</span>
      </span>
      <span slot="businessTag" slot-scope="text">
        <template v-if="text">
          <a-tag v-for="(tag, index) in (text.split(','))" :key="index" color="blue" style="margin-bottom: 4px;">{{ tag }}</a-tag>
        </template>
        <span v-else>-</span>
      </span>
      <span slot="action" slot-scope="text, record">
        <a-dropdown>
          <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
            <a-menu-item key="contractDetail">
              <a-icon type="file-text" />合约详情
            </a-menu-item>
            <a-menu-item key="repurchaseDetail">
              <a-icon type="reload" />复购详情
            </a-menu-item>
            <a-menu-item key="renewal">
              <a-icon type="clock-circle" />续费操作
            </a-menu-item>
            <a-menu-item key="lossApply">
              <a-icon type="disconnect" />流失申请
            </a-menu-item>
            <a-menu-item key="addOrder">
              <a-icon type="file-add" />新增订单
            </a-menu-item>
            <a-menu-item key="operationRecord">
              <a-icon type="history" />操作记录
            </a-menu-item>
          </a-menu>
          <a-button>
            操作 <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </span>
    </a-table>

    <!-- 合同表单弹窗 -->
    <contract-modal ref="modalForm" @ok="modalFormOk"></contract-modal>

    <!-- 合约详情抽屉（右侧） -->
    <contract-detail-drawer ref="contractDetailDrawer" @changed="loadData"></contract-detail-drawer>
    
    <!-- 新增订单弹窗 -->
    <order-modal ref="orderModal" @ok="handleOrderOk"></order-modal>
    
    <!-- 企业等级详情弹窗 -->
    <enterprise-level-detail-modal ref="enterpriseLevelDetailModal"></enterprise-level-detail-modal>
    
    <!-- 客户订单记录弹窗 -->
    <customer-order-modal ref="customerOrderModal"></customer-order-modal>

    <j-modal
      :visible.sync="svcModal.visible"
      :width="960"
      title="人员详情"
      :footer="null"
      :switchFullscreen="true"
      @cancel="closeServicePersonnelModal"
    >
      <div class="svc-modal-wrap">
        <div class="svc-modal-header">
          <div class="svc-modal-company">{{ svcModal.record.companyName || '—' }}</div>
        </div>

        <a-row :gutter="16" type="flex" class="svc-modal-split-row">
          <a-col :xs="24" :sm="24" :md="12" class="svc-modal-split-col">
            <div class="svc-modal-panel svc-modal-panel--staff-form">
              <div class="svc-modal-panel-head svc-modal-panel-head--with-action">
                <span class="svc-modal-panel-title">服务人员</span>
                <a-button type="primary" size="small" :loading="svcStaffSaving" @click="saveAccountingServiceStaff">保存</a-button>
              </div>
              <div class="svc-modal-panel-body">
                <a-form-model layout="vertical" class="svc-staff-form">
                  <a-form-model-item v-for="role in serviceStaffRoleKeys" :key="role" :label="role" class="svc-staff-form-item">
                    <j-search-select-tag
                      v-model="svcStaffForm[role]"
                      dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                      :async="false"
                      :placeholder="'请选择' + role"
                      :getPopupContainer="staffSelectPopupContainer"
                      style="width: 100%"
                    />
                  </a-form-model-item>
                </a-form-model>
              </div>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" class="svc-modal-split-col svc-modal-split-col--right">
            <div class="svc-modal-right-stack">
              <div class="svc-modal-panel svc-modal-panel--biz">
                <div class="svc-modal-panel-head">
                  <span class="svc-modal-panel-title">业务人员</span>
                </div>
                <div class="svc-modal-panel-body">
                  <div class="svc-modal-line">
                    <a-tag color="blue" class="svc-role-tag">{{ buildBizRoleName(svcModal.record) }}</a-tag>
                    <span class="svc-person-name svc-person-strong">{{ buildBizPersonName(svcModal.record) }}</span>
                  </div>
                </div>
              </div>

              <div class="svc-modal-panel svc-modal-panel--audit">
                <div class="svc-modal-panel-head">
                  <span class="svc-modal-panel-title">审批人员</span>
                </div>
                <div class="svc-modal-panel-body svc-modal-panel-body--scroll svc-modal-panel-body--audit">
                  <div v-if="svcModalAuditPairs.length" class="svc-role-people-list">
                    <div v-for="(it, idx) in svcModalAuditPairs" :key="'a' + idx" class="svc-role-people-row">
                      <a-tag color="purple" class="svc-role-tag">{{ it.role }}</a-tag>
                      <span class="svc-person-name">{{ it.people }}</span>
                    </div>
                  </div>
                  <a-empty v-else description="暂无审批人员" :imageStyle="{ height: '40px' }" class="svc-modal-empty" />
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>
    </j-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { getAction, deleteAction, postAction } from '@/api/manage'
import { filterObj } from '@/utils/util'
import ContractModal from './modules/ContractModal'
import ContractDetailDrawer from './modules/ContractDetailDrawer'
import OrderModal from '@/views/order/modules/OrderModal'
import EnterpriseLevelDetailModal from '@/views/customer/modules/EnterpriseLevelDetailModal'
import CustomerOrderModal from '@/views/customer/modules/CustomerOrderModal'
import LossAuditList from './modules/LossAuditList'
import moment from 'moment'

export default {
  name: 'AccountingContractList',
  mixins: [JeecgListMixin],
  components: {
    ContractModal,
    ContractDetailDrawer,
    OrderModal,
    EnterpriseLevelDetailModal,
    CustomerOrderModal,
    LossAuditList,
  },
  data() {
    return {
      description: '合同管理',
      // 查询参数
      queryParam: {
        contractNo: '',
        orderNo: '',
        companyName: '',
        servicePerson: '',
        expireDateRange: [],
        expireDate_begin: '',
        expireDate_end: '',
        tabType: 'serving', // 默认tab：serving-服务中, currentRenewal-当期续费(0-30天), t2OverdueRenewal-t-2逾期续费(-1到-60天), t6ExpectedRenewal-t+6预期续费(0-180天), t3OverdueCustomer-t-3逾期客户(-61到-365天), lossAudit-流失审核, lossCustomer-流失客户
      },
      currentTab: 'serving', // 当前选中的tab页
      activeTabKey: 'serving', // 当前激活的tab key
      // Tab统计数量
      tabCounts: {
        serving: 0,
        currentRenewal: 0,
        t2OverdueRenewal: 0,
        t6ExpectedRenewal: 0,
        t3OverdueCustomer: 0,
        lossAudit: 0,
        lossCustomer: 0,
      },
      svcModal: {
        visible: false,
        record: {},
      },
      serviceStaffRoleKeys: ['财税主管', '财税顾问', '主办会计'],
      svcStaffForm: {
        财税主管: '',
        财税顾问: '',
        主办会计: '',
      },
      svcStaffSaving: false,
      // 表头
      columns: [
        {
          title: '创建时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 180,
          customRender: (text) => {
            return text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : '-'
          },
        },
        {
          title: '公司名称',
          align: 'left',
          dataIndex: 'companyName',
          width: 200,
        },
        {
          title: '人员信息',
          align: 'center',
          dataIndex: 'servicePerson',
          width: 90,
          scopedSlots: { customRender: 'serviceAndAudit' },
          customCell: () => ({
            style: {
              whiteSpace: 'normal',
              verticalAlign: 'middle',
            },
          }),
        },
        {
          title: '到期时间',
          align: 'center',
          dataIndex: 'expireDate',
          width: 120,
          customRender: (text) => {
            return text ? moment(text).format('YYYY-MM') : '-'
          },
        },
        {
          title: '倒计天数',
          align: 'center',
          dataIndex: 'countdownDays',
          width: 100,
          scopedSlots: { customRender: 'countdownDays' },
        },
        {
          title: '消费金额',
          align: 'center',
          dataIndex: 'totalSpending',
          width: 120,
          scopedSlots: { customRender: 'totalSpending' },
        },
        {
          title: '复购信息',
          align: 'center',
          dataIndex: 'customerOrderCount',
          width: 100,
          scopedSlots: { customRender: 'isRecurring' },
        },
        {
          title: '累计时间',
          align: 'center',
          dataIndex: 'serviceMonths',
          width: 100,
          customRender: (text) => {
            if (text !== null && text !== undefined) {
              return text + '个月'
            }
            return '-'
          },
        },
        {
          title: '企业等级',
          align: 'center',
          dataIndex: 'enterpriseLevel',
          width: 100,
          scopedSlots: { customRender: 'enterpriseLevel' },
        },
        {
          title: '业务标签',
          align: 'center',
          dataIndex: 'businessTag',
          width: 200,
          scopedSlots: { customRender: 'businessTag' },
        },
        {
          title: '关联企业',
          align: 'center',
          dataIndex: 'relatedCompanyName',
          width: 200,
        },
        {
          title: '操作',
          align: 'center',
          dataIndex: 'action',
          fixed: 'right',
          width: 150,
          scopedSlots: { customRender: 'action' },
        },
      ],
      url: {
        list: '/order/accountingContract/list',
        delete: '/order/accountingContract/delete',
        applyLoss: '/order/accountingContract/applyLoss',
        tabStatistics: '/order/accountingContract/tabStatistics',
        saveServiceStaff: '/order/accountingContract/saveServiceStaff',
      },
    }
  },
  computed: {
    svcModalAuditPairs() {
      return this.buildServiceRolePeoplePairs(this.svcModal.record)
    },
  },
  methods: {
    openServicePersonnelModal(record) {
      this.svcModal.record = Object.assign({}, record || {})
      this.parseSvcStaffFormFromRecord(this.svcModal.record)
      this.svcModal.visible = true
    },
    closeServicePersonnelModal() {
      this.svcModal.visible = false
      this.svcModal.record = {}
      this.resetSvcStaffForm()
    },
    staffSelectPopupContainer() {
      return document.body
    },
    resetSvcStaffForm() {
      this.svcStaffForm = {
        财税主管: '',
        财税顾问: '',
        主办会计: '',
      }
    },
    parseSvcStaffFormFromRecord(rec) {
      const empty = { 财税主管: '', 财税顾问: '', 主办会计: '' }
      if (!rec || !rec.serviceStaffJson) {
        this.svcStaffForm = { ...empty }
        return
      }
      try {
        const raw = rec.serviceStaffJson
        const o = typeof raw === 'string' ? JSON.parse(raw) : raw
        const pick = (k, alt) => {
          const v = o[k] != null && o[k] !== '' ? o[k] : alt != null && o[alt] != null ? o[alt] : ''
          return v !== '' && v != null ? String(v).trim() : ''
        }
        this.svcStaffForm = {
          ...empty,
          财税主管: pick('财税主管', 'taxDirector'),
          财税顾问: pick('财税顾问', 'taxAdvisor'),
          主办会计: pick('主办会计', 'leadAccountant'),
        }
      } catch (e) {
        this.svcStaffForm = { ...empty }
      }
    },
    saveAccountingServiceStaff() {
      if (!this.svcModal.record || !this.svcModal.record.id) {
        return
      }
      if (this.svcStaffSaving) {
        return
      }
      this.svcStaffSaving = true
      const payload = {
        id: this.svcModal.record.id,
        财税主管: this.svcStaffForm.财税主管 || '',
        财税顾问: this.svcStaffForm.财税顾问 || '',
        主办会计: this.svcStaffForm.主办会计 || '',
      }
      postAction(this.url.saveServiceStaff, payload)
        .then((res) => {
          if (res.success) {
            this.$message.success(res.message || '保存成功')
            const m = {}
            if (payload.财税主管 && String(payload.财税主管).trim()) m['财税主管'] = String(payload.财税主管).trim()
            if (payload.财税顾问 && String(payload.财税顾问).trim()) m['财税顾问'] = String(payload.财税顾问).trim()
            if (payload.主办会计 && String(payload.主办会计).trim()) m['主办会计'] = String(payload.主办会计).trim()
            this.$set(this.svcModal.record, 'serviceStaffJson', Object.keys(m).length ? JSON.stringify(m) : null)
            this.loadData()
          } else {
            this.$message.error(res.message || '保存失败')
          }
        })
        .catch(() => {
          this.$message.error('保存失败')
        })
        .finally(() => {
          this.svcStaffSaving = false
        })
    },
    onExpireDateChange(value, dateString) {
      this.queryParam.expireDate_begin = dateString[0]
      this.queryParam.expireDate_end = dateString[1]
    },
    // 加载Tab统计数据
    loadTabStatistics() {
      getAction(this.url.tabStatistics).then((res) => {
        if (res.success && res.result) {
          this.tabCounts = {
            serving: res.result.serving || 0,
            currentRenewal: res.result.currentRenewal || 0,
            t2OverdueRenewal: res.result.t2OverdueRenewal || 0,
            t6ExpectedRenewal: res.result.t6ExpectedRenewal || 0,
            t3OverdueCustomer: res.result.t3OverdueCustomer || 0,
            lossAudit: res.result.lossAudit || 0,
            lossCustomer: res.result.lossCustomer || 0,
          }
        }
      }).catch((err) => {
        console.error('加载Tab统计数据失败', err)
      })
    },
    // Tab页切换事件
    handleTabChange(key) {
      this.currentTab = key
      this.activeTabKey = key
      this.queryParam.tabType = key
      // 切换tab时重置到第一页
      this.ipagination.current = 1
      if (key !== 'lossAudit') {
        this.loadData(1)
      } else {
        // 切换到流失审核tab时，触发子组件加载数据
        this.$nextTick(() => {
          if (this.$refs.lossAuditList) {
            this.$refs.lossAuditList.loadData(1)
          }
        })
      }
    },
    getQueryParams() {
      // 重写查询参数方法，确保包含tabType
      var param = Object.assign({}, this.queryParam)
      param.field = this.getQueryField()
      param.pageNo = this.ipagination.current
      param.pageSize = this.ipagination.pageSize
      // tabType已经包含在queryParam中，会随param一起传递
      return filterObj(param)
    },
    getStatusBadgeStatus(status) {
      const statusMap = {
        'draft': 'default',
        'signed': 'success',
        'executing': 'processing',
        'completed': 'success',
        'terminated': 'error',
      }
      return statusMap[status] || 'default'
    },
    getStatusText(status) {
      const statusMap = {
        'draft': '草稿',
        'signed': '已签订',
        'executing': '执行中',
        'completed': '已完成',
        'terminated': '已终止',
      }
      return statusMap[status] || status
    },
    getCountdownClass(days) {
      // 返回自定义类名，用于CSS样式控制
      if (days === null || days === undefined) {
        return 'countdown-default'
      }
      if (days < 0) {
        return 'countdown-error'
      }
      if (days <= 30) {
        return 'countdown-warning'
      }
      return 'countdown-success'
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    buildSubsequentAuditPersonsText(record) {
      const rows = record && record.auditStepSummary
      if (!rows || !rows.length) {
        return '—'
      }
      const orders = rows.map((r) => r.stepOrder).filter((s) => s != null && s !== '')
      if (!orders.length) {
        return '—'
      }
      const minStep = Math.min(...orders.map((s) => Number(s)))
      const sub = rows.filter((r) => r.stepOrder != null && Number(r.stepOrder) > minStep)
      if (!sub.length) {
        return '—'
      }
      const names = []
      const seen = new Set()
      for (const r of sub) {
        let p = (r.personText || '').trim()
        if (!p || p === '—') {
          continue
        }
        if (!seen.has(p)) {
          seen.add(p)
          names.push(p)
        }
      }
      return names.length ? names.join('、') : '—'
    },
    buildBizRoleName(record) {
      const rows = record && record.auditStepSummary
      if (!rows || !rows.length) return '业务人员'
      const orders = rows.map((r) => r.stepOrder).filter((s) => s != null && s !== '')
      if (!orders.length) return '业务人员'
      const minStep = Math.min(...orders.map((s) => Number(s)))
      const first = rows.find((r) => r.stepOrder != null && Number(r.stepOrder) === minStep)
      return (first && (first.roleName || first.stepName || first.role || first.step)) || '业务人员'
    },
    buildBizPersonName(record) {
      const rows = record && record.auditStepSummary
      if (rows && rows.length) {
        const orders = rows.map((r) => r.stepOrder).filter((s) => s != null && s !== '')
        if (orders.length) {
          const minStep = Math.min(...orders.map((s) => Number(s)))
          const first = rows.find((r) => r.stepOrder != null && Number(r.stepOrder) === minStep)
          const p = first && (first.personText || first.personName || first.person || '')
          if (p && String(p).trim()) return String(p).trim()
        }
      }
      return (record && (record.signer || '')).trim() || '—'
    },
    buildServiceRolePeoplePairs(record) {
      const rows = record && record.auditStepSummary
      if (!rows || !rows.length) return []
      const orders = rows.map((r) => r.stepOrder).filter((s) => s != null && s !== '')
      if (!orders.length) return []
      const minStep = Math.min(...orders.map((s) => Number(s)))
      const sub = rows.filter((r) => r.stepOrder != null && Number(r.stepOrder) > minStep)
      if (!sub.length) return []

      const roleToPeople = new Map()
      for (const r of sub) {
        const role = (r.roleName || r.stepName || r.role || r.step || '审批人员').toString().trim() || '审批人员'
        const pRaw = (r.personText || r.personName || r.person || '').toString().trim()
        if (!pRaw) continue
        const prev = roleToPeople.get(role) || []
        if (!prev.includes(pRaw)) {
          prev.push(pRaw)
          roleToPeople.set(role, prev)
        }
      }
      return Array.from(roleToPeople.entries()).map(([role, peopleArr]) => ({
        role,
        people: peopleArr.join('、'),
      }))
    },
    // 操作菜单点击事件
    handleActionMenuClick({ key }, record) {
      switch (key) {
        case 'contractDetail':
          this.handleContractDetail(record)
          break
        case 'repurchaseDetail':
          this.handleRepurchaseDetail(record)
          break
        case 'renewal':
          this.handleRenewal(record)
          break
        case 'lossApply':
          this.handleLossApply(record)
          break
        case 'addOrder':
          this.handleAddOrder(record)
          break
        case 'operationRecord':
          this.handleOperationRecord(record)
          break
      }
    },
    // 合约详情
    handleContractDetail(record) {
      this.$refs.contractDetailDrawer.open(record)
    },
    // 复购详情
    handleRepurchaseDetail(record) {
      // 统一使用右侧抽屉（切到复购Tab）
      this.$refs.contractDetailDrawer.open(record)
      this.$nextTick(() => {
        if (this.$refs.contractDetailDrawer) {
          this.$refs.contractDetailDrawer.activeTab = 'repurchase'
        }
      })
    },
    // 续费操作
    handleRenewal(record) {
      // 统一使用右侧抽屉（切到续费Tab）
      this.$refs.contractDetailDrawer.open(record, 'renewal')
    },
    // 流失申请：改为打开右侧抽屉，在抽屉中完成原因选择和发起审核
    handleLossApply(record) {
      this.$refs.contractDetailDrawer.open(record, 'lossApply')
    },
    // 新增订单
    handleAddOrder(record) {
      // 打开订单表单弹窗，自动填充客户信息
      const orderData = {
        companyName: record.companyName,
        contacts: '',
        contactInformation: '',
        address: '',
        region: '',
      }
      this.$refs.orderModal.add(orderData)
    },
    // 操作记录
    handleOperationRecord(record) {
      // 统一使用右侧抽屉（切到操作记录Tab）
      this.$refs.contractDetailDrawer.open(record)
      this.$nextTick(() => {
        if (this.$refs.contractDetailDrawer) {
          this.$refs.contractDetailDrawer.activeTab = 'operationRecord'
        }
      })
    },
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
      this.$refs.modalForm.title = '编辑合同'
    },
    handleDetail(record) {
      this.handleContractDetail(record)
    },
    handleDelete(id) {
      deleteAction(this.url.delete, { id: id }).then((res) => {
        if (res.success) {
          this.$message.success('删除成功')
          this.loadData()
        } else {
          this.$message.error(res.message || '删除失败')
        }
      })
    },
    modalFormOk() {
      this.loadData()
      // 刷新统计数据
      this.loadTabStatistics()
    },
    // 订单保存成功回调
    handleOrderOk() {
      this.$message.success('订单创建成功！')
      // 可以刷新合同列表（如果需要）
      // this.loadData()
    },
    // 点击企业等级
    handleEnterpriseLevelClick(record) {
      this.$refs.enterpriseLevelDetailModal.show(record)
    },
    // 获取复购信息的颜色
    getIsRecurringColor(orderCount) {
      if (orderCount != null && orderCount > 1) {
        return 'green' // 有复购 - 绿色
      } else if (orderCount != null && orderCount === 1) {
        return 'orange' // 只有一单 - 橙色
      }
      return 'default' // 未设置 - 默认灰色
    },
    // 获取复购信息的文本（显示订单数量）
    getIsRecurringText(record) {
      const orderCount = record.customerOrderCount
      if (orderCount != null && orderCount > 0) {
        return `${orderCount}单`
      }
      return '-'
    },
    // 点击复购信息，查看该客户的所有订单
    handleRecurringClick(record) {
      if (!record) {
        return
      }
      // 构造客户记录对象，传递给CustomerOrderModal
      const customerRecord = {
        id: record.customerId || record.companyId, // 客户ID
        corporateName: record.companyName, // 公司名称
        totalSpending: record.totalSpending, // 总消费金额
      }
      this.$refs.customerOrderModal.show(customerRecord)
    },
  },
  mounted() {
    // 初始化时加载默认tab（服务中）的数据
    // tabType已经在queryParam中设置，会随查询一起传递
    // 加载Tab统计数据
    this.loadTabStatistics()
  },
}
</script>

<style scoped>
@import '~@assets/less/common.less';

/* 修复表格文字和背景色一致的问题 */
::v-deep .ant-table {
  color: rgba(0, 0, 0, 0.85);
}

::v-deep .ant-table-tbody > tr > td {
  color: rgba(0, 0, 0, 0.85);
}

::v-deep .ant-table-thead > tr > th {
  color: rgba(0, 0, 0, 0.85);
  background: #fafafa;
}

::v-deep .ant-table-tbody > tr:hover > td {
  background: #f5f5f5;
}

/* 修复 Tag 组件文字颜色和背景色对比度问题 */
::v-deep .ant-tag {
  color: #fff !important;
  border-color: transparent;
}

/* 确保不同颜色的 Tag 都有清晰的文字颜色 */
::v-deep .ant-tag-green {
  background-color: #52c41a !important;
  color: #fff !important;
}

::v-deep .ant-tag-blue {
  background-color: #1890ff !important;
  color: #fff !important;
}

::v-deep .ant-tag-orange {
  background-color: #fa8c16 !important;
  color: #fff !important;
}

::v-deep .ant-tag-red {
  background-color: #f5222d !important;
  color: #fff !important;
}

::v-deep .ant-tag-volcano {
  background-color: #fa541c !important;
  color: #fff !important;
}

::v-deep .ant-tag-warning {
  background-color: #faad14 !important;
  color: #fff !important;
}

::v-deep .ant-tag-error {
  background-color: #f5222d !important;
  color: #fff !important;
}

::v-deep .ant-tag-gold {
  background-color: #faad14 !important;
  color: #fff !important;
}

::v-deep .ant-tag-default {
  background-color: #d9d9d9 !important;
  color: rgba(0, 0, 0, 0.85) !important;
}

/* 倒计天数：固定高对比（与地址管理列表一致） */
::v-deep .ant-table .ant-tag.countdown-tag {
  min-width: 52px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
  line-height: 1.4;
  border: 1px solid transparent !important;
}
::v-deep .ant-table .ant-tag.countdown-tag.countdown-success {
  background-color: #237804 !important;
  color: #ffffff !important;
  border-color: #135200 !important;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.12);
}
::v-deep .ant-table .ant-tag.countdown-tag.countdown-warning {
  background-color: #d46b08 !important;
  color: #ffffff !important;
  border-color: #ad4e00 !important;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.12);
}
::v-deep .ant-table .ant-tag.countdown-tag.countdown-error {
  background-color: #a8071a !important;
  color: #ffffff !important;
  border-color: #820014 !important;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.15);
}
::v-deep .ant-table .ant-tag.countdown-tag.countdown-default {
  background-color: #f0f0f0 !important;
  color: #262626 !important;
  border-color: #d9d9d9 !important;
  font-weight: 500;
  text-shadow: none;
}

/* 服务人员列：与地址管理列表同逻辑与样式 */
.svc-personnel-compact {
  max-width: 270px;
  padding: 2px 0;
  font-size: 13px;
  line-height: 1.55;
  word-break: break-word;
  cursor: default;
  text-decoration: none !important;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.svc-personnel-compact .svc-personnel-lbl {
  color: #8c8c8c;
  font-size: 12px;
  font-weight: 400;
}
.svc-personnel-compact .svc-personnel-colon {
  color: #8c8c8c;
  font-size: 12px;
}
.svc-personnel-compact .svc-personnel-val {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  text-decoration: none !important;
}
.svc-personnel-compact .svc-personnel-dot {
  color: #d9d9d9;
  margin: 0 6px;
  font-weight: 300;
  user-select: none;
}
::v-deep .ant-table-tbody > tr > td .svc-personnel-compact .svc-personnel-val {
  color: rgba(0, 0, 0, 0.88) !important;
  text-decoration: none !important;
}
::v-deep .ant-table-tbody > tr > td .svc-personnel-compact .svc-personnel-lbl,
::v-deep .ant-table-tbody > tr > td .svc-personnel-compact .svc-personnel-colon {
  color: #8c8c8c !important;
}
::v-deep .ant-table-tbody > tr > td .svc-personnel-compact .svc-personnel-dot {
  color: #d9d9d9 !important;
}

.svc-view-btn {
  font-weight: 600;
  padding: 0;
}
.svc-modal-wrap {
  padding: 4px 2px 8px;
}
.svc-modal-split-row {
  align-items: stretch;
}
.svc-modal-split-col {
  display: flex;
}
.svc-modal-split-col--right {
  flex-direction: column;
}
.svc-modal-right-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  flex: 1;
}
.svc-modal-panel {
  flex: 1;
  width: 100%;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
  overflow: hidden;
}
.svc-modal-panel--biz .svc-modal-panel-body,
.svc-modal-panel--audit .svc-modal-panel-body,
.svc-modal-panel--staff-form .svc-modal-panel-body {
  background: #fff;
}
.svc-modal-panel-head {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(180deg, #fafafa 0%, #f5f5f5 100%);
}
.svc-modal-panel-head--with-action {
  flex-wrap: wrap;
}
.svc-modal-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.4;
}
.svc-modal-panel-body {
  flex: 1;
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.svc-modal-panel-body--scroll {
  max-height: 200px;
  overflow-y: auto;
}
.svc-modal-panel-body--audit {
  max-height: 220px;
}
.svc-modal-empty {
  margin: auto 0;
  padding: 8px 0 16px;
}
.svc-staff-form .svc-staff-form-item {
  margin-bottom: 12px;
}
.svc-staff-form .svc-staff-form-item:last-child {
  margin-bottom: 0;
}
.svc-modal-header {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: linear-gradient(90deg, rgba(24, 144, 255, 0.12), rgba(24, 144, 255, 0.03));
  border: 1px solid rgba(24, 144, 255, 0.15);
  border-radius: 8px;
}
.svc-modal-company {
  font-size: 16px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.88);
  line-height: 1.35;
}
.svc-modal-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 12px;
}
.svc-modal-line {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.svc-role-people-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.svc-role-people-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.svc-role-tag {
  flex: 0 0 auto;
  margin-right: 0;
  border: none;
  color: #fff !important;
  font-weight: 600;
  border-radius: 4px;
}
::v-deep .svc-role-tag.ant-tag-blue {
  background: #0958d9 !important;
}
::v-deep .svc-role-tag.ant-tag-purple {
  background: #531dab !important;
}
.svc-person-name {
  color: rgba(0, 0, 0, 0.85);
  word-break: break-word;
}
.svc-person-strong {
  font-weight: 700;
}
.svc-count {
  font-weight: 700;
  color: #0958d9;
}
::v-deep .svc-desc .ant-descriptions-item-label {
  color: rgba(0, 0, 0, 0.55);
  width: 96px;
}
::v-deep .svc-modal-card .ant-card-head {
  border-bottom: 1px solid #f0f0f0;
}
</style>

<style>
.svc-audit-tooltip-wrap .ant-tooltip-inner {
  max-width: 360px;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.5;
}
</style>

