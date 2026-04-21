<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务员">
              <j-search-select-tag
                placeholder="请输入业务员"
                v-model="queryParam.salesman"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="服务人员">
              <j-search-select-tag
                placeholder="请输入服务人员"
                v-model="queryParam.advisor"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="地址状态">
              <j-dict-select-tag
                placeholder="请选择地址状态"
                v-model="queryParam.addressStatus"
                dictCode="enterprise_status"
              />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="到期时间">
              <a-range-picker
                style="width: 210px"
                v-model="queryParam.terminationDateRange"
                format="YYYY-MM-DD"
                @change="onTerminationDateChange"
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

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button v-has="'address:export'" type="primary" icon="download" @click="handleExportXls('地址管理')">导出</a-button>
      <a-button
        style="margin-left: 8px"
        icon="shop"
        v-has="'business:supplier:list'"
        @click="handleManageSupplier"
      >地址商管理</a-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel">
            <a-icon type="delete" />删除
          </a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px">
          批量操作 <a-icon type="down" />
        </a-button>
      </a-dropdown>
    </div>

    <!-- Tab页区域 -->
    <a-tabs default-active-key="serving" @change="handleTabChange" style="margin-top: 16px; margin-bottom: 16px;">
      <a-tab-pane key="serving">
        <span slot="tab">服务中<span v-if="tabCounts.serving !== undefined"> ({{ tabCounts.serving }})</span></span>
      </a-tab-pane>
      <a-tab-pane key="currentRenewal">
        <span slot="tab">当期续费<span v-if="tabCounts.currentRenewal !== undefined"> ({{ tabCounts.currentRenewal }})</span></span>
      </a-tab-pane>
      <a-tab-pane key="t2OverdueRenewal">
        <span slot="tab">t-2逾期续费<span v-if="tabCounts.t2OverdueRenewal !== undefined"> ({{ tabCounts.t2OverdueRenewal }})</span></span>
      </a-tab-pane>
      <a-tab-pane key="t6ExpectedRenewal">
        <span slot="tab">t+6预期续费<span v-if="tabCounts.t6ExpectedRenewal !== undefined"> ({{ tabCounts.t6ExpectedRenewal }})</span></span>
      </a-tab-pane>
      <a-tab-pane key="t3OverdueCustomer">
        <span slot="tab">t-3逾期客户<span v-if="tabCounts.t3OverdueCustomer !== undefined"> ({{ tabCounts.t3OverdueCustomer }})</span></span>
      </a-tab-pane>
      <a-tab-pane key="terminatedCustomer">
        <span slot="tab">已终止客户<span v-if="tabCounts.terminatedCustomer !== undefined"> ({{ tabCounts.terminatedCustomer }})</span></span>
      </a-tab-pane>
    </a-tabs>

    <!-- 表格区域 -->
    <a-table
      ref="table"
      size="middle"
      :scroll="{ x: true }"
      bordered
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="addressStatus" slot-scope="text, record">
        <a-badge :status="getStatusBadgeStatus(record.addressStatus)" :text="record.addressStatus_dictText || text" />
      </span>
      <span slot="sfjz" slot-scope="text, record">
        <a-tag v-if="record.sfjz === '1' || record.sfjz === '是' || record.sfjz_dictText === '是'" color="green">
          {{ record.sfjz_dictText || text || '否' }}
        </a-tag>
        <a-tag v-else color="blue">
          {{ record.sfjz_dictText || text || '否' }}
        </a-tag>
      </span>
      <span slot="sffs" slot-scope="text, record">
        <a-tag v-if="record.sffs === '1' || record.sffs === '是' || record.sffs_dictText === '是'" color="green">
          {{ record.sffs_dictText || text || '否' }}
        </a-tag>
        <a-tag v-else color="blue">
          {{ record.sffs_dictText || text || '否' }}
        </a-tag>
      </span>
      <span slot="countdownDays" slot-scope="text, record">
        <a-tag class="countdown-tag" :class="getCountdownClass(record.countdownDays)">
          {{ record.countdownDays !== null && record.countdownDays !== undefined ? record.countdownDays + '天' : '-' }}
        </a-tag>
      </span>
      <span slot="enterpriseLevel" slot-scope="text, record">
        <a-tag 
          v-if="record.enterpriseLevel" 
          color="gold"
          style="cursor: pointer;"
          @click="handleEnterpriseLevelClick(record)"
        >
          {{ record.enterpriseLevel === '1' ? '一星' : record.enterpriseLevel === '2' ? '二星' : record.enterpriseLevel === '3' ? '三星' : record.enterpriseLevel === '4' ? '四星' : record.enterpriseLevel === '5' ? '五星' : record.enterpriseLevel }}
        </a-tag>
        <span v-else>-</span>
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
      <span slot="businessTag" slot-scope="text">
        <template v-if="text">
          <a-tag v-for="(tag, index) in (text.split(','))" :key="index" color="blue" style="margin-bottom: 4px;">{{ tag }}</a-tag>
        </template>
        <span v-else>-</span>
      </span>
      <span slot="serviceAndAudit" slot-scope="text, record">
        <a-button type="link" size="small" class="svc-view-btn" @click="openServicePersonnelModal(record)">查看</a-button>
      </span>
      <span slot="action" slot-scope="text, record">
        <a-dropdown>
          <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
            <a-menu-item v-if="!record.reimbursementFlag || record.reimbursementFlag !== '1'" key="reimbursement">
              <a-icon type="dollar" />报销申请
            </a-menu-item>
            <a-menu-item key="contractDetail">
              <a-icon type="file-text" />合约详情
            </a-menu-item>
            <a-menu-item key="repurchaseDetail">
              <a-icon type="reload" />复购详情
            </a-menu-item>
            <a-menu-item key="renewal">
              <a-icon type="clock-circle" />续费操作
            </a-menu-item>
            <a-menu-item key="customerEdit">
              <a-icon type="edit" />信息编辑
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

    <!-- 地址表单弹窗 -->
    <address-modal ref="modalForm" @ok="modalFormOk"></address-modal>

    <!-- 收支详情弹窗 -->
    <income-expense-modal ref="incomeExpenseModal" :addressId="selectedAddressId"></income-expense-modal>
    
    <!-- 企业等级详情弹窗 -->
    <enterprise-level-detail-modal ref="enterpriseLevelDetailModal"></enterprise-level-detail-modal>
    
    <!-- 客户订单记录弹窗 -->
    <customer-order-modal ref="customerOrderModal"></customer-order-modal>
    
    <!-- 订单详情弹窗（用于显示合约详情） -->
    <order-modal ref="orderModal" @ok="handleOrderModalOk"></order-modal>
    
    <!-- 地址详情抽屉页 -->
    <address-detail-drawer ref="addressDetailDrawer" @ok="handleDrawerOk"></address-detail-drawer>

    <!-- 服务人员详情弹窗 -->
    <j-modal
      :visible.sync="svcModal.visible"
      :width="820"
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
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" class="svc-modal-split-col">
            <div class="svc-modal-panel svc-modal-panel--svc">
              <div class="svc-modal-panel-head">
                <span class="svc-modal-panel-title">审批人员</span>
              </div>
              <div class="svc-modal-panel-body svc-modal-panel-body--scroll">
                <div v-if="svcModalServicePairs.length" class="svc-role-people-list">
                  <div
                    v-for="(it, idx) in svcModalServicePairs"
                    :key="idx"
                    class="svc-role-people-row"
                  >
                    <a-tag color="purple" class="svc-role-tag">{{ it.role }}</a-tag>
                    <span class="svc-person-name">{{ it.people }}</span>
                  </div>
                </div>
                <a-empty
                  v-else
                  description="暂无审批人员"
                  :imageStyle="{ height: '44px' }"
                  class="svc-modal-empty"
                />
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
import { getAction, deleteAction } from '@/api/manage'
import { filterObj } from '@/utils/util'
import AddressModal from './modules/AddressModal'
import IncomeExpenseModal from './modules/IncomeExpenseModal'
import EnterpriseLevelDetailModal from '@/views/customer/modules/EnterpriseLevelDetailModal'
import CustomerOrderModal from '@/views/customer/modules/CustomerOrderModal'
import OrderModal from '@/views/order/modules/OrderModal'
import AddressDetailDrawer from './modules/AddressDetailDrawer'
import moment from 'moment'

export default {
  name: 'GhAddressCenterList',
  mixins: [JeecgListMixin],
  components: {
    AddressModal,
    IncomeExpenseModal,
    EnterpriseLevelDetailModal,
    CustomerOrderModal,
    OrderModal,
    AddressDetailDrawer,
  },
  data() {
    return {
      description: '地址管理',
      selectedAddressId: '',
      currentTab: 'serving', // 当前选中的tab页
      tabCounts: {}, // Tab统计数量
      svcModal: {
        visible: false,
        record: {},
      },
      // 查询参数
      queryParam: {
        companyName: '',
        salesman: '',
        advisor: '',
        addressStatus: '',
        terminationDateRange: [],
        terminationDate_begin: '',
        terminationDate_end: '',
        tabType: 'serving', // 默认tab：serving-服务中, currentRenewal-当期续费, t2OverdueRenewal-t-2逾期续费, t6ExpectedRenewal-t+6预期续费, t3OverdueCustomer-t-3逾期客户, terminatedCustomer-已终止客户
      },
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
          dataIndex: 'advisor',
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
          dataIndex: 'terminationDate',
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
          title: '累计时间',
          align: 'center',
          dataIndex: 'accumulatedTime',
          width: 120,
        },
        {
          title: '企业等级',
          align: 'center',
          dataIndex: 'enterpriseLevel',
          width: 120,
          scopedSlots: { customRender: 'enterpriseLevel' },
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
        list: '/address/ghAddressCenter/list',
        delete: '/address/ghAddressCenter/delete',
        deleteBatch: '/address/ghAddressCenter/deleteBatch',
        exportXlsUrl: '/address/ghAddressCenter/exportXls',
      },
    }
  },
  computed: {
    svcModalServicePairs() {
      return this.buildServiceRolePeoplePairs(this.svcModal.record)
    },
  },
  methods: {
    openServicePersonnelModal(record) {
      this.svcModal.record = record || {}
      this.svcModal.visible = true
    },
    closeServicePersonnelModal() {
      this.svcModal.visible = false
      this.svcModal.record = {}
    },
    onTerminationDateChange(value, dateString) {
      this.queryParam.terminationDate_begin = dateString[0]
      this.queryParam.terminationDate_end = dateString[1]
    },
    handleManageSupplier() {
      this.$router.push({ path: '/business-management/supplier' })
    },
    getQueryParams() {
      // 重写getQueryParams方法，添加tabType参数
      const param = Object.assign({}, this.queryParam, this.isorter)
      param.field = this.getQueryField()
      param.pageNo = this.ipagination.current
      param.pageSize = this.ipagination.pageSize
      return filterObj(param)
    },
    getStatusBadgeStatus(status) {
      const statusMap = {
        '1': 'success',
        '2': 'warning',
        '3': 'error',
        '4': 'default',
      }
      return statusMap[status] || 'default'
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
    // 点击企业等级
    handleEnterpriseLevelClick(record) {
      this.$refs.enterpriseLevelDetailModal.show(record)
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    handleAdd() {
      this.$refs.modalForm.add()
      this.$refs.modalForm.title = '新增地址'
    },
    handleEdit(record) {
      this.$refs.modalForm.disableSubmit = false // 编辑时可编辑
      this.$refs.modalForm.title = '编辑地址'
      this.$refs.modalForm.edit(record)
    },
    handleDetail(record) {
      this.$refs.modalForm.edit(record)
      this.$refs.modalForm.title = '地址详情'
      this.$refs.modalForm.disableSubmit = true
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
    handleIncomeExpense(record) {
      this.selectedAddressId = record.id
      this.$refs.incomeExpenseModal.show(record)
    },
    modalFormOk() {
      this.loadData()
      this.loadTabCounts() // 刷新Tab统计数量
    },
    handleOrderModalOk() {
      // 订单弹窗关闭后的回调（合约详情是只读的，不需要刷新）
    },
    handleTabChange(activeKey) {
      this.currentTab = activeKey
      this.queryParam.tabType = activeKey
      this.ipagination.current = 1
      this.loadData()
    },
    // 加载Tab统计数量
    loadTabCounts() {
      getAction('/address/ghAddressCenter/getTabCounts').then((res) => {
        if (res.success && res.result) {
          this.tabCounts = res.result
        }
      }).catch((err) => {
        console.error('加载Tab统计数量失败:', err)
      })
    },
    handleActionMenuClick({ key }, record) {
      // 所有操作都打开抽屉页，并跳转到对应的tab
      if (!record || !record.id) {
        this.$message.warning('地址信息不完整')
        return
      }
      
      // 映射操作key到抽屉页的tab key
      const tabMap = {
        'reimbursement': 'reimbursement', // 报销申请 -> 报销申请
        'contractDetail': 'contractInfo', // 合约详情 -> 合同信息
        'repurchaseDetail': 'repurchase', // 复购详情 -> 复购情况
        'renewal': 'renewal', // 续费操作 -> 续费操作
        'customerEdit': 'infoEdit', // 信息编辑 -> 信息编辑
        'operationRecord': 'operationRecord', // 操作记录 -> 操作记录
      }
      
      const targetTab = tabMap[key] || 'contractInfo'
      if (this.$refs.addressDetailDrawer) {
        this.$refs.addressDetailDrawer.open(record, targetTab)
      } else {
        // 如果抽屉页组件不存在，使用原来的方式
      switch (key) {
        case 'reimbursement':
          this.handleReimbursement(record)
          break
        case 'contractDetail':
          this.handleContractDetail(record)
          break
        case 'repurchaseDetail':
          this.handleRepurchaseDetail(record)
          break
        case 'renewal':
          this.handleRenewal(record)
          break
        case 'customerEdit':
          this.handleCustomerEdit(record)
          break
        case 'operationRecord':
          this.handleOperationRecord(record)
          break
        default:
          break
      }
      }
    },
    handleDrawerOk() {
      // 抽屉页操作完成后的回调
      this.loadData()
      this.loadTabCounts() // 刷新Tab统计数量
    },
    // 报销成本
    handleReimbursement(record) {
      const that = this
      this.$confirm({
        title: '确认报销',
        content: `确定要报销"${record.companyName || record.id}"的成本吗？`,
        onOk() {
          postAction('/address/ghAddressCenter/baoxiao', { id: record.id }).then((res) => {
            if (res.success) {
              that.$message.success('报销成功！')
              that.loadData()
            } else {
              that.$message.error(res.message || '报销失败！')
            }
          }).catch((err) => {
            that.$message.error('报销失败：' + (err.message || '未知错误'))
          })
        },
      })
    },
    handleContractDetail(record) {
      // 合约详情：根据contractId查询订单信息并显示
      if (!record || !record.contractId) {
        this.$message.warning('该地址记录未关联订单，无法查看合约详情')
        return
      }
      // 查询订单详情
      getAction('/order/queryById', { id: record.contractId }).then((res) => {
        if (res.success && res.result) {
          // 使用订单详情弹窗显示合同信息
          if (this.$refs.orderModal) {
            this.$refs.orderModal.edit(res.result)
            this.$refs.orderModal.title = '合约详情'
            this.$refs.orderModal.disableSubmit = true
          } else {
            // 如果没有订单弹窗，显示提示信息
            this.$message.info('合约详情功能开发中，请稍后使用')
          }
        } else {
          this.$message.error(res.message || '获取订单详情失败')
        }
      }).catch((err) => {
        console.error('获取订单详情失败:', err)
        this.$message.error('获取订单详情失败')
      })
    },
    handleRepurchaseDetail(record) {
      // 复购详情：显示客户的所有订单
      if (!record) {
        return
      }
      // 构造客户记录对象，传递给CustomerOrderModal
      const customerRecord = {
        id: record.dataId || record.customerId || record.companyId, // 客户ID
        corporateName: record.companyName, // 公司名称
        totalSpending: record.totalSpending, // 总消费金额
      }
      this.$refs.customerOrderModal.show(customerRecord)
    },
    handleRenewal(record) {
      // 续费操作：打开续费弹窗
      if (!record || !record.id) {
        this.$message.warning('地址信息不完整，无法进行续费操作')
        return
      }
      // 打开续费弹窗（如果存在）
      if (this.$refs.renewalModal) {
        this.$refs.renewalModal.show(record)
      } else {
        // 如果没有续费弹窗组件，显示提示信息
        this.$message.info('续费功能开发中，请稍后使用')
      }
    },
    handleCustomerEdit(record) {
      // 编辑地址信息（包括地址商信息）
      this.handleEdit(record)
    },
    handleOperationRecord(record) {
      // 操作记录：显示地址的操作记录
      if (!record || !record.id) {
        this.$message.warning('地址信息不完整，无法查看操作记录')
        return
      }
      // 打开操作记录弹窗（如果存在）
      if (this.$refs.operationLogModal) {
        this.$refs.operationLogModal.show(record.id)
      } else {
        // 如果没有操作记录组件，显示提示信息
        this.$message.info('操作记录功能开发中，请稍后使用')
      }
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
      return (record && (record.salesman || '')).trim() || '—'
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
    // 加载Tab统计数量
    this.loadTabCounts()
    
    // 计算倒计天数
    this.$nextTick(() => {
      if (this.dataSource && this.dataSource.length > 0) {
        this.dataSource.forEach(item => {
          if (item.terminationDate) {
            const today = moment()
            const endDate = moment(item.terminationDate)
            const days = endDate.diff(today, 'days')
            this.$set(item, 'countdownDays', days)
          }
        })
      }
    })
  },
  watch: {
    dataSource: {
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          newVal.forEach(item => {
            if (item.terminationDate) {
              const today = moment()
              const endDate = moment(item.terminationDate)
              const days = endDate.diff(today, 'days')
              this.$set(item, 'countdownDays', days)
            }
          })
        }
      },
      deep: true,
    },
  },
}
</script>

<style scoped>
@import '~@assets/less/common.less'

/* 确保标签文字可见 */
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

/* 倒计天数：固定高对比配色（不用 color 预设，避免与全局 .ant-tag 白字冲突） */
::v-deep .ant-table .ant-tag.countdown-tag {
  min-width: 52px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
  line-height: 1.4;
  border: 1px solid transparent !important;
}
/* >30 天：深绿底 + 白字 */
::v-deep .ant-table .ant-tag.countdown-tag.countdown-success {
  background-color: #237804 !important;
  color: #ffffff !important;
  border-color: #135200 !important;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.12);
}
/* 0～30 天：深橙底 + 白字 */
::v-deep .ant-table .ant-tag.countdown-tag.countdown-warning {
  background-color: #d46b08 !important;
  color: #ffffff !important;
  border-color: #ad4e00 !important;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.12);
}
/* 已逾期：深红底 + 白字 */
::v-deep .ant-table .ant-tag.countdown-tag.countdown-error {
  background-color: #a8071a !important;
  color: #ffffff !important;
  border-color: #820014 !important;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.15);
}
/* 无数据：浅灰底 + 深字 */
::v-deep .ant-table .ant-tag.countdown-tag.countdown-default {
  background-color: #f0f0f0 !important;
  color: #262626 !important;
  border-color: #d9d9d9 !important;
  font-weight: 500;
  text-shadow: none;
}

/* 确保表格文字可见 */
::v-deep .ant-table-tbody > tr > td {
  color: rgba(0, 0, 0, 0.85) !important;
}

/* 服务人员列：一行紧凑展示，标签灰 / 值加粗，避免被表格样式渲染成链接 */
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

.svc-view-link {
  font-weight: 600;
}
.svc-view-btn {
  font-weight: 600;
  padding: 0;
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
.svc-modal-wrap {
  padding: 4px 2px 8px;
}
.svc-modal-split-row {
  align-items: stretch;
}
.svc-modal-split-col {
  display: flex;
}
.svc-modal-panel {
  flex: 1;
  width: 100%;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
  overflow: hidden;
}
.svc-modal-panel--biz .svc-modal-panel-body {
  background: #fff;
}
.svc-modal-panel--svc .svc-modal-panel-body {
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
  max-height: 320px;
  overflow-y: auto;
}
.svc-modal-empty {
  margin: auto 0;
  padding: 8px 0 16px;
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
/* tooltip 内可读全文，不受表格列宽限制 */
.svc-audit-tooltip-wrap .ant-tooltip-inner {
  max-width: 360px;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.5;
}
</style>

