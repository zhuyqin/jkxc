<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
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
            <a-form-item label="业务人员">
              <j-search-select-tag
                placeholder="请输入业务人员"
                v-model="queryParam.salesman"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
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

    <div class="table-page-search-wrapper">
      <!-- Tab页 -->
      <a-tabs v-model="activeTab" @change="handleTabChange" class="enhanced-tabs">
        <div slot="tabBarExtraContent" class="view-mode-toggle-extra">
          <a-radio-group 
            v-model="isPersonal" 
            @change="handleViewModeChange" 
            button-style="solid"
            class="view-mode-toggle"
          >
            <a-radio-button :value="true">
              <a-icon type="user" />
              <span>个人</span>
            </a-radio-button>
            <a-radio-button :value="false">
              <a-icon type="team" />
              <span>团队</span>
            </a-radio-button>
          </a-radio-group>
        </div>
        <a-tab-pane key="pending">
          <span slot="tab">
            <a-icon type="clock-circle" />
            <span>待审核</span>
          </span>
          <a-table
            ref="table"
            rowKey="id"
            size="middle"
            :columns="columns"
            :dataSource="pendingDataSource"
            :pagination="pendingPagination"
            :loading="pendingLoading"
            :scroll="{ x: 1800 }"
            bordered
            class="enhanced-table"
            @change="handleTableChange"
          >
            <span slot="orderNo" slot-scope="text, record">
              <a @click="handleViewOrder(record.orderId)">{{ text }}</a>
            </span>
            <span slot="orderStatus" slot-scope="text">
              <a-tag v-if="text === '0'" color="orange">待审核</a-tag>
              <a-tag v-else-if="text === '1'" color="blue">进行中</a-tag>
              <a-tag v-else-if="text === '2'" color="green">已完成</a-tag>
              <a-tag v-else-if="text === '3'" color="red">已退单</a-tag>
              <a-tag v-else-if="text === '4'" color="red">已驳回</a-tag>
              <span v-else>-</span>
            </span>
            <span slot="companyName" slot-scope="text">
              <a-tooltip v-if="text" :title="text">
                <span>{{ text.length > 20 ? text.substring(0, 20) + '...' : text }}</span>
              </a-tooltip>
              <span v-else>-</span>
            </span>
            <span slot="expenseDetail" slot-scope="text, record">
              <div class="expense-detail-cell">
                <a-tag v-if="record.orderId" color="blue" @click="showPaymentModal(record)" class="expense-detail-tag">费用详情</a-tag>
                <span v-else style="color: #999;">-</span>
              </div>
            </span>
            <span slot="serviceUserName" slot-scope="text, record">
              <template v-if="formatServiceUser(record) !== '-'">
                <div style="display: flex; flex-wrap: wrap; gap: 4px;">
                  <a-tag 
                    v-for="(item, index) in formatServiceUser(record).split('; ')" 
                    :key="index"
                    color="green"
                    style="margin: 0;"
                  >
                    {{ item }}
                  </a-tag>
                </div>
              </template>
              <span v-else style="color: #999;">-</span>
            </span>
            <span slot="processProgress" slot-scope="text, record">
              <a-button 
                v-if="record.currentAuditStep && record.totalSteps" 
                type="link" 
                size="small" 
                @click="showProcessModal(record)"
              >
                第{{ record.currentAuditStep }}/{{ record.totalSteps }}步
              </a-button>
              <span v-else>-</span>
            </span>
            <span slot="remarks" slot-scope="text">
              <a-tooltip v-if="text" :title="text">
                <span>{{ text.length > 30 ? text.substring(0, 30) + '...' : text }}</span>
              </a-tooltip>
              <span v-else>-</span>
            </span>
            <span slot="action" slot-scope="text, record">
              <a @click="handleAudit(record)" style="margin-right: 8px">
                <a-icon type="check-circle"/>
                审核
              </a>
            </span>
          </a-table>
        </a-tab-pane>
        
        <a-tab-pane key="approved">
          <span slot="tab">
            <a-icon type="check-circle" />
            <span>已审核</span>
          </span>
          <a-table
            ref="table"
            rowKey="id"
            size="middle"
            :columns="approvedColumns"
            :dataSource="approvedDataSource"
            :pagination="approvedPagination"
            :loading="approvedLoading"
            :scroll="{ x: 1800 }"
            bordered
            class="enhanced-table"
            @change="handleApprovedTableChange"
          >
            <span slot="orderNo" slot-scope="text, record">
              <a @click="handleViewOrder(record.orderId)">{{ text }}</a>
            </span>
            <span slot="orderStatus" slot-scope="text">
              <a-tag v-if="text === '0'" color="orange">待审核</a-tag>
              <a-tag v-else-if="text === '1'" color="blue">进行中</a-tag>
              <a-tag v-else-if="text === '2'" color="green">已完成</a-tag>
              <a-tag v-else-if="text === '3'" color="red">已退单</a-tag>
              <a-tag v-else-if="text === '4'" color="red">已驳回</a-tag>
              <span v-else>-</span>
            </span>
            <span slot="companyName" slot-scope="text">
              <a-tooltip v-if="text" :title="text">
                <span>{{ text.length > 20 ? text.substring(0, 20) + '...' : text }}</span>
              </a-tooltip>
              <span v-else>-</span>
            </span>
            <span slot="expenseDetail" slot-scope="text, record">
              <div class="expense-detail-cell">
                <a-tag v-if="record.orderId" color="blue" @click="showPaymentModal(record)" class="expense-detail-tag">费用详情</a-tag>
                <span v-else style="color: #999;">-</span>
              </div>
            </span>
            <span slot="serviceUserName" slot-scope="text, record">
              <template v-if="formatServiceUser(record) !== '-'">
                <div style="display: flex; flex-wrap: wrap; gap: 4px;">
                  <a-tag 
                    v-for="(item, index) in formatServiceUser(record).split('; ')" 
                    :key="index"
                    color="green"
                    style="margin: 0;"
                  >
                    {{ item }}
                  </a-tag>
                </div>
              </template>
              <span v-else style="color: #999;">-</span>
            </span>
            <span slot="processProgress" slot-scope="text, record">
              <a-button 
                v-if="record.currentAuditStep && record.totalSteps" 
                type="link" 
                size="small" 
                @click="showProcessModal(record)"
              >
                第{{ record.currentAuditStep }}/{{ record.totalSteps }}步
              </a-button>
              <span v-else>-</span>
            </span>
            <span slot="remarks" slot-scope="text">
              <a-tooltip v-if="text" :title="text">
                <span>{{ text.length > 30 ? text.substring(0, 30) + '...' : text }}</span>
              </a-tooltip>
              <span v-else>-</span>
            </span>
            <span slot="action" slot-scope="text, record">
              <a @click="handleViewOperationLog(record.orderId)">
                <a-icon type="file-text"/>
                操作记录
              </a>
            </span>
          </a-table>
        </a-tab-pane>
        
        <a-tab-pane key="rejected">
          <span slot="tab">
            <a-icon type="close-circle" />
            <span>已驳回</span>
          </span>
          <a-table
            ref="table"
            rowKey="id"
            size="middle"
            :columns="rejectedColumns"
            :dataSource="rejectedDataSource"
            :pagination="rejectedPagination"
            :loading="rejectedLoading"
            :scroll="{ x: 1800 }"
            bordered
            class="enhanced-table"
            @change="handleRejectedTableChange"
          >
            <span slot="orderNo" slot-scope="text, record">
              <a @click="handleViewOrder(record.orderId)">{{ text }}</a>
            </span>
            <span slot="orderStatus" slot-scope="text">
              <a-tag v-if="text === '0'" color="orange">待审核</a-tag>
              <a-tag v-else-if="text === '1'" color="blue">进行中</a-tag>
              <a-tag v-else-if="text === '2'" color="green">已完成</a-tag>
              <a-tag v-else-if="text === '3'" color="red">已退单</a-tag>
              <a-tag v-else-if="text === '4'" color="red">已驳回</a-tag>
              <span v-else>-</span>
            </span>
            <span slot="companyName" slot-scope="text">
              <a-tooltip v-if="text" :title="text">
                <span>{{ text.length > 20 ? text.substring(0, 20) + '...' : text }}</span>
              </a-tooltip>
              <span v-else>-</span>
            </span>
            <span slot="expenseDetail" slot-scope="text, record">
              <div class="expense-detail-cell">
                <a-tag v-if="record.orderId" color="blue" @click="showPaymentModal(record)" class="expense-detail-tag">费用详情</a-tag>
                <span v-else style="color: #999;">-</span>
              </div>
            </span>
            <span slot="serviceUserName" slot-scope="text, record">
              <template v-if="formatServiceUser(record) !== '-'">
                <div style="display: flex; flex-wrap: wrap; gap: 4px;">
                  <a-tag 
                    v-for="(item, index) in formatServiceUser(record).split('; ')" 
                    :key="index"
                    color="green"
                    style="margin: 0;"
                  >
                    {{ item }}
                  </a-tag>
                </div>
              </template>
              <span v-else style="color: #999;">-</span>
            </span>
            <span slot="processProgress" slot-scope="text, record">
              <a-button 
                v-if="record.currentAuditStep && record.totalSteps" 
                type="link" 
                size="small" 
                @click="showProcessModal(record)"
              >
                第{{ record.currentAuditStep }}/{{ record.totalSteps }}步
              </a-button>
              <span v-else>-</span>
            </span>
            <span slot="remarks" slot-scope="text">
              <a-tooltip v-if="text" :title="text">
                <span>{{ text.length > 30 ? text.substring(0, 30) + '...' : text }}</span>
              </a-tooltip>
              <span v-else>-</span>
            </span>
            <span slot="action" slot-scope="text, record">
              <a @click="handleViewOperationLog(record.orderId)">
                <a-icon type="file-text"/>
                操作记录
              </a>
            </span>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </div>
    
    <!-- 审核弹窗 -->
    <audit-task-modal ref="auditModal" @ok="handleAuditOk"></audit-task-modal>
    
    <!-- 操作记录弹窗 -->
    <j-modal
      :visible.sync="operationLogModal.visible"
      :width="1400"
      :title="operationLogModal.title + (operationLogModal.orderNo ? '（订单编号：' + operationLogModal.orderNo + '）' : '')"
      :fullscreen.sync="operationLogModal.fullscreen"
      :switchFullscreen="operationLogModal.switchFullscreen"
    >
      <order-operation-log :orderId="operationLogModal.orderId"></order-operation-log>
    </j-modal>

    <!-- 流程进度弹窗 -->
    <j-modal
      :visible.sync="processModal.visible"
      :width="1200"
      :title="processModal.title"
      :fullscreen.sync="processModal.fullscreen"
      :switchFullscreen="processModal.switchFullscreen"
    >
      <order-step v-if="processModal.visible" :id="processModal.orderId"></order-step>
    </j-modal>

    <!-- 费用详情弹窗 -->
    <j-modal
      :visible.sync="paymentModal.visible"
      :width="1200"
      :title="paymentModal.title"
      :fullscreen.sync="paymentModal.fullscreen"
      :switchFullscreen="paymentModal.switchFullscreen"
    >
      <order-payment v-if="paymentModal.visible" :orderId="paymentModal.orderId" :companyName="paymentModal.companyName"></order-payment>
    </j-modal>
  </a-card>
</template>

<script>
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { getPendingTasks, getApprovedTasks, getRejectedTasks } from '@/api/api'
  import { getAction } from '@/api/manage'
  import AuditTaskModal from './modules/AuditTaskModal'
  import OrderStep from '@/views/order/modules/OrderStep'
  import OrderPayment from '@/views/order/modules/OrderPayment'
  import OrderOperationLog from '@/views/order/modules/OrderOperationLog'
  import JInput from '@/components/jeecg/JInput'

  export default {
    name: "RecurringTaskList",
    mixins: [JeecgListMixin],
    components: { AuditTaskModal, OrderStep, OrderPayment, OrderOperationLog, JInput },
    data() {
      return {
        description: '周期任务管理页面',
        isPersonal: true,
        activeTab: 'pending',
        // 查询参数
        queryParam: {
          companyName: '',
          salesman: '',
          orderNo: '',
        },
        pendingDataSource: [],
        pendingPagination: { current: 1, pageSize: 10, total: 0 },
        pendingLoading: false,
        approvedDataSource: [],
        approvedPagination: { current: 1, pageSize: 10, total: 0 },
        approvedLoading: false,
        rejectedDataSource: [],
        rejectedPagination: { current: 1, pageSize: 10, total: 0 },
        rejectedLoading: false,
        // 操作记录弹窗
        operationLogModal: {
          visible: false,
          orderId: '',
          orderNo: '',
          title: '操作记录',
          fullscreen: false,
          switchFullscreen: true
        },
        // 流程进度弹窗
        processModal: {
          visible: false,
          orderId: '',
          title: '流程信息',
          fullscreen: false,
          switchFullscreen: true
        },
        // 费用详情弹窗
        paymentModal: {
          visible: false,
          orderId: '',
          companyName: '',
          title: '费用详情',
          fullscreen: false,
          switchFullscreen: true
        },
        // 服务人员步骤信息缓存
        serviceStepsCache: {},
        columns: [
          {
            title: '订单编号',
            align: "left",
            dataIndex: 'orderNo',
            scopedSlots: { customRender: 'orderNo' },
            width: 150,
            fixed: 'left',
          },
          {
            title: '订单状态',
            align: "center",
            dataIndex: 'orderStatus',
            width: 120,
            scopedSlots: { customRender: 'orderStatus' },
          },
          {
            title: '下单时间',
            align: "center",
            dataIndex: 'orderCreateTime',
            width: 160,
            customRender: (text) => {
              return text ? (text.length > 19 ? text.substr(0, 19) : text) : '-'
            }
          },
          {
            title: '业务人员',
            align: "center",
            dataIndex: 'salesman',
            width: 120,
          },
          {
            title: '所属团队',
            align: "center",
            dataIndex: 'teamName',
            width: 120,
          },
          {
            title: '公司名称',
            align: "left",
            dataIndex: 'companyName',
            width: 200,
            scopedSlots: { customRender: 'companyName' },
          },
          {
            title: '业务类型',
            align: "center",
            dataIndex: 'businessType',
            width: 150,
            customRender: (text, record) => {
              if (record.businessTypeName) {
                return record.businessTypeName
              }
              if (record.businessType_dictText) {
                return record.businessType_dictText
              }
              if (text) {
                const dictItems = this.$store.getters.dictItems['id'] || []
                const item = dictItems.find(d => d.value === text)
                return item ? item.text : text
              }
              return '-'
            }
          },
          {
            title: '费用详情',
            align: "center",
            dataIndex: 'expenseDetail',
            width: 110,
            scopedSlots: { customRender: 'expenseDetail' },
          },
          {
            title: '服务人员',
            align: "center",
            dataIndex: 'serviceUserName',
            width: 200,
            scopedSlots: { customRender: 'serviceUserName' },
          },
          {
            title: '起始月份',
            align: "center",
            dataIndex: 'startMonth',
            width: 120,
            customRender: (text) => {
              return text || '-'
            }
          },
          {
            title: '截至月份',
            align: "center",
            dataIndex: 'endMonth',
            width: 120,
            customRender: (text) => {
              return text || '-'
            }
          },
          {
            title: '流程进度',
            align: "center",
            dataIndex: 'processProgress',
            width: 150,
            scopedSlots: { customRender: 'processProgress' },
          },
          {
            title: '备注信息',
            align: "left",
            dataIndex: 'remarks',
            width: 200,
            scopedSlots: { customRender: 'remarks' },
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            width: 100,
            fixed: 'right',
            scopedSlots: { customRender: 'action' },
          }
        ],
        approvedColumns: [
          {
            title: '订单编号',
            align: "left",
            dataIndex: 'orderNo',
            scopedSlots: { customRender: 'orderNo' },
            width: 150,
            fixed: 'left',
          },
          {
            title: '订单状态',
            align: "center",
            dataIndex: 'orderStatus',
            width: 120,
            scopedSlots: { customRender: 'orderStatus' },
          },
          {
            title: '下单时间',
            align: "center",
            dataIndex: 'orderCreateTime',
            width: 160,
            customRender: (text) => {
              return text ? (text.length > 19 ? text.substr(0, 19) : text) : '-'
            }
          },
          {
            title: '业务人员',
            align: "center",
            dataIndex: 'salesman',
            width: 120,
          },
          {
            title: '所属团队',
            align: "center",
            dataIndex: 'teamName',
            width: 120,
          },
          {
            title: '公司名称',
            align: "left",
            dataIndex: 'companyName',
            width: 200,
            scopedSlots: { customRender: 'companyName' },
          },
          {
            title: '业务类型',
            align: "center",
            dataIndex: 'businessType',
            width: 150,
            customRender: (text, record) => {
              if (record.businessTypeName) {
                return record.businessTypeName
              }
              if (record.businessType_dictText) {
                return record.businessType_dictText
              }
              if (text) {
                const dictItems = this.$store.getters.dictItems['id'] || []
                const item = dictItems.find(d => d.value === text)
                return item ? item.text : text
              }
              return '-'
            }
          },
          {
            title: '费用详情',
            align: "center",
            dataIndex: 'expenseDetail',
            width: 110,
            scopedSlots: { customRender: 'expenseDetail' },
          },
          {
            title: '服务人员',
            align: "center",
            dataIndex: 'serviceUserName',
            width: 200,
            scopedSlots: { customRender: 'serviceUserName' },
          },
          {
            title: '起始月份',
            align: "center",
            dataIndex: 'startMonth',
            width: 120,
            customRender: (text) => {
              return text || '-'
            }
          },
          {
            title: '截至月份',
            align: "center",
            dataIndex: 'endMonth',
            width: 120,
            customRender: (text) => {
              return text || '-'
            }
          },
          {
            title: '流程进度',
            align: "center",
            dataIndex: 'processProgress',
            width: 150,
            scopedSlots: { customRender: 'processProgress' },
          },
          {
            title: '备注信息',
            align: "left",
            dataIndex: 'remarks',
            width: 200,
            scopedSlots: { customRender: 'remarks' },
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            width: 100,
            fixed: 'right',
            scopedSlots: { customRender: 'action' },
          }
        ],
        rejectedColumns: [
          {
            title: '订单编号',
            align: "left",
            dataIndex: 'orderNo',
            scopedSlots: { customRender: 'orderNo' },
            width: 150,
            fixed: 'left',
          },
          {
            title: '订单状态',
            align: "center",
            dataIndex: 'orderStatus',
            width: 120,
            scopedSlots: { customRender: 'orderStatus' },
          },
          {
            title: '下单时间',
            align: "center",
            dataIndex: 'orderCreateTime',
            width: 160,
            customRender: (text) => {
              return text ? (text.length > 19 ? text.substr(0, 19) : text) : '-'
            }
          },
          {
            title: '业务人员',
            align: "center",
            dataIndex: 'salesman',
            width: 120,
          },
          {
            title: '所属团队',
            align: "center",
            dataIndex: 'teamName',
            width: 120,
          },
          {
            title: '公司名称',
            align: "left",
            dataIndex: 'companyName',
            width: 200,
            scopedSlots: { customRender: 'companyName' },
          },
          {
            title: '业务类型',
            align: "center",
            dataIndex: 'businessType',
            width: 150,
            customRender: (text, record) => {
              if (record.businessTypeName) {
                return record.businessTypeName
              }
              if (record.businessType_dictText) {
                return record.businessType_dictText
              }
              if (text) {
                const dictItems = this.$store.getters.dictItems['id'] || []
                const item = dictItems.find(d => d.value === text)
                return item ? item.text : text
              }
              return '-'
            }
          },
          {
            title: '费用详情',
            align: "center",
            dataIndex: 'expenseDetail',
            width: 110,
            scopedSlots: { customRender: 'expenseDetail' },
          },
          {
            title: '服务人员',
            align: "center",
            dataIndex: 'serviceUserName',
            width: 200,
            scopedSlots: { customRender: 'serviceUserName' },
          },
          {
            title: '起始月份',
            align: "center",
            dataIndex: 'startMonth',
            width: 120,
            customRender: (text) => {
              return text || '-'
            }
          },
          {
            title: '截至月份',
            align: "center",
            dataIndex: 'endMonth',
            width: 120,
            customRender: (text) => {
              return text || '-'
            }
          },
          {
            title: '流程进度',
            align: "center",
            dataIndex: 'processProgress',
            width: 150,
            scopedSlots: { customRender: 'processProgress' },
          },
          {
            title: '备注信息',
            align: "left",
            dataIndex: 'remarks',
            width: 200,
            scopedSlots: { customRender: 'remarks' },
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            width: 100,
            fixed: 'right',
            scopedSlots: { customRender: 'action' },
          }
        ]
      }
    },
    created() {
      // 从URL参数中获取筛选条件
      const query = this.$route.query
      if (query.tab) {
        this.activeTab = query.tab
      }
      if (query.orderNo) {
        this.queryParam.orderNo = query.orderNo
      }
    },
    mounted() {
      // 如果URL中有订单号筛选条件，自动查询
      if (this.queryParam.orderNo) {
        this.$nextTick(() => {
          this.searchQuery()
        })
      } else {
        this.loadPendingTasks()
      }
    },
    watch: {
      '$route.query': {
        immediate: false,
        handler(newQuery, oldQuery) {
          // 监听URL参数变化，更新查询条件
          let needReload = false
          
          if (newQuery.tab && newQuery.tab !== this.activeTab) {
            this.activeTab = newQuery.tab
            needReload = true
          }
          
          const newOrderNo = newQuery.orderNo || ''
          const oldOrderNo = (oldQuery && oldQuery.orderNo) || ''
          
          if (newOrderNo !== oldOrderNo) {
            this.queryParam.orderNo = newOrderNo
            needReload = true
          }
          
          // 只有在参数真正变化时才重新查询
          if (needReload) {
            this.$nextTick(() => {
              this.searchQuery()
            })
          }
        }
      }
    },
    methods: {
      // 处理JInput组件的值：过滤掉**，去掉前后的*
      processJInputValue(value) {
        if (!value || value.trim() === '' || value.trim() === '**') {
          return ''
        }
        // 如果是like类型，去掉前后的*
        if (value.startsWith('*') && value.endsWith('*') && value.length > 2) {
          return value.substring(1, value.length - 1).trim()
        }
        return value.trim()
      },
      searchQuery() {
        // 查询时重置分页
        this.pendingPagination.current = 1
        this.approvedPagination.current = 1
        this.rejectedPagination.current = 1
        // 重新加载当前tab的数据
        if (this.activeTab === 'pending') {
          this.loadPendingTasks()
        } else if (this.activeTab === 'approved') {
          this.loadApprovedTasks()
        } else if (this.activeTab === 'rejected') {
          this.loadRejectedTasks()
        }
      },
      searchReset() {
        // 重置查询条件（清空所有查询条件，包括订单编号）
        this.queryParam = {
          companyName: '',
          salesman: '',
          orderNo: '',
        }
        // 清空URL中的orderNo参数
        if (this.$route.query.orderNo) {
          this.$router.replace({ query: { ...this.$route.query, orderNo: undefined } })
        }
        this.searchQuery()
      },
      handleViewModeChange() {
        if (this.activeTab === 'pending') {
          this.loadPendingTasks()
        } else if (this.activeTab === 'approved') {
          this.loadApprovedTasks()
        } else if (this.activeTab === 'rejected') {
          this.loadRejectedTasks()
        }
      },
      handleTabChange(activeKey) {
        this.activeTab = activeKey
        if (activeKey === 'pending') {
          this.loadPendingTasks()
        } else if (activeKey === 'approved') {
          this.loadApprovedTasks()
        } else if (activeKey === 'rejected') {
          this.loadRejectedTasks()
        }
      },
      loadPendingTasks() {
        this.pendingLoading = true
        const params = {
          taskType: 'recurring',
          pageNo: this.pendingPagination.current,
          pageSize: this.pendingPagination.pageSize,
          isPersonal: this.isPersonal
        }
        
        // 处理JInput组件的值（过滤**，去掉前后的*）
        const companyName = this.processJInputValue(this.queryParam.companyName)
        if (companyName) {
          params.companyName = companyName
        }
        
        const salesman = this.queryParam.salesman || ''
        if (salesman) {
          params.salesman = salesman
        }
        
        const orderNo = this.processJInputValue(this.queryParam.orderNo)
        if (orderNo) {
          params.orderNo = orderNo
        }
        
        getPendingTasks(params).then((res) => {
          if (res.success) {
            this.pendingDataSource = res.result.records || []
            this.pendingPagination.total = res.result.total || 0
            // 批量加载服务人员步骤信息
            this.batchLoadServiceSteps(this.pendingDataSource)
          }
        }).finally(() => {
          this.pendingLoading = false
        })
      },
      loadApprovedTasks() {
        this.approvedLoading = true
        const params = {
          taskType: 'recurring',
          pageNo: this.approvedPagination.current,
          pageSize: this.approvedPagination.pageSize,
          isPersonal: this.isPersonal
        }
        
        // 处理JInput组件的值（过滤**，去掉前后的*）
        const companyName = this.processJInputValue(this.queryParam.companyName)
        if (companyName) {
          params.companyName = companyName
        }
        
        const salesman = this.queryParam.salesman || ''
        if (salesman) {
          params.salesman = salesman
        }
        
        const orderNo = this.processJInputValue(this.queryParam.orderNo)
        if (orderNo) {
          params.orderNo = orderNo
        }
        
        getApprovedTasks(params).then((res) => {
          if (res.success) {
            this.approvedDataSource = res.result.records || []
            this.approvedPagination.total = res.result.total || 0
            // 批量加载服务人员步骤信息
            this.batchLoadServiceSteps(this.approvedDataSource)
          }
        }).finally(() => {
          this.approvedLoading = false
        })
      },
      loadRejectedTasks() {
        this.rejectedLoading = true
        const params = {
          taskType: 'recurring',
          pageNo: this.rejectedPagination.current,
          pageSize: this.rejectedPagination.pageSize,
          isPersonal: this.isPersonal
        }
        
        // 处理JInput组件的值（过滤**，去掉前后的*）
        const companyName = this.processJInputValue(this.queryParam.companyName)
        if (companyName) {
          params.companyName = companyName
        }
        
        const salesman = this.queryParam.salesman || ''
        if (salesman) {
          params.salesman = salesman
        }
        
        const orderNo = this.processJInputValue(this.queryParam.orderNo)
        if (orderNo) {
          params.orderNo = orderNo
        }
        
        getRejectedTasks(params).then((res) => {
          if (res.success) {
            this.rejectedDataSource = res.result.records || []
            this.rejectedPagination.total = res.result.total || 0
            // 批量加载服务人员步骤信息
            this.batchLoadServiceSteps(this.rejectedDataSource)
          }
        }).finally(() => {
          this.rejectedLoading = false
        })
      },
      handleTableChange(pagination) {
        this.pendingPagination.current = pagination.current
        this.pendingPagination.pageSize = pagination.pageSize
        this.loadPendingTasks()
      },
      handleApprovedTableChange(pagination) {
        this.approvedPagination.current = pagination.current
        this.approvedPagination.pageSize = pagination.pageSize
        this.loadApprovedTasks()
      },
      handleRejectedTableChange(pagination) {
        this.rejectedPagination.current = pagination.current
        this.rejectedPagination.pageSize = pagination.pageSize
        this.loadRejectedTasks()
      },
      handleAudit(record) {
        this.$refs.auditModal.open(record)
      },
      handleAuditOk() {
        if (this.activeTab === 'pending') {
          this.loadPendingTasks()
        }
      },
      handleViewOrder(orderId) {
        this.$router.push({
          path: '/order/list',
          query: { id: orderId }
        })
      },
      handleViewOperationLog(orderId) {
        // 显示操作记录弹窗
        this.operationLogModal.orderId = orderId
        // 从当前数据源中查找订单编号
        const allData = [...this.pendingDataSource, ...this.approvedDataSource, ...this.rejectedDataSource]
        const record = allData.find(item => item.orderId === orderId)
        this.operationLogModal.orderNo = record ? record.orderNo : ''
        this.operationLogModal.title = '操作记录'
        this.operationLogModal.visible = true
      },
      showProcessModal(record) {
        // 显示流程进度弹窗
        this.processModal.orderId = record.orderId
        this.processModal.visible = true
      },
      showPaymentModal(record) {
        // 显示费用详情弹窗
        this.paymentModal.orderId = record.orderId
        this.paymentModal.companyName = record.companyName || ''
        this.paymentModal.visible = true
      },
      formatServiceUser(record) {
        // 如果缓存中有步骤信息，直接使用
        if (this.serviceStepsCache[record.orderId]) {
          const steps = this.serviceStepsCache[record.orderId]
          if (steps && steps.length > 0) {
            // 过滤掉第一个大步骤（stepOrder为1或最小的stepOrder）
            const minStepOrder = Math.min(...steps.map(s => s.stepOrder || 999))
            const filteredSteps = steps.filter(step => {
              return (step.stepOrder || 999) > minStepOrder
            })
            
            if (filteredSteps.length > 0) {
              return filteredSteps.map(step => {
                const stepName = step.roleName || step.stepName || `步骤${step.stepOrder}`
                const userName = step.userName || step.assignedUserName || '-'
                return `${stepName}:${userName}`
              }).join('; ')
            }
          }
        } else if (record.orderId) {
          // 如果没有缓存，异步加载步骤信息
          this.loadServiceSteps(record.orderId)
        }
        // 如果没有步骤信息，使用原来的serviceUserName
        return record.serviceUserName || '-'
      },
      batchLoadServiceSteps(dataSource) {
        // 批量加载服务人员步骤信息
        if (!dataSource || dataSource.length === 0) {
          return
        }
        
        // 获取需要加载的订单ID列表（排除已缓存的）
        const orderIds = dataSource
          .filter(record => record.orderId && this.serviceStepsCache[record.orderId] === undefined)
          .map(record => record.orderId)
        
        if (orderIds.length === 0) {
          return
        }
        
        // 批量加载步骤信息
        orderIds.forEach(orderId => {
          // 标记为加载中
          this.serviceStepsCache[orderId] = null
          
          // 调用API获取步骤信息
          getAction('/sys/auditTask/getOrderAuditProgress', { orderId: orderId }).then((res) => {
            if (res.success && res.result && res.result.steps) {
              // 提取步骤信息
              const steps = []
              res.result.steps.forEach(stepInfo => {
                const stepOrder = stepInfo.stepOrder || 0
                const tasks = stepInfo.tasks || []
                
                tasks.forEach(task => {
                  // 过滤掉流失审核任务（taskType='loss'），避免重复显示
                  if (task.taskType !== 'loss') {
                    steps.push({
                      stepOrder: stepOrder,
                      roleName: task.roleName || '',
                      userName: task.assignedUserName || task.auditUserName || '',
                      assignedUserName: task.assignedUserName || '',
                      auditUserName: task.auditUserName || ''
                    })
                  }
                })
              })
              
              // 缓存步骤信息
              this.serviceStepsCache[orderId] = steps
              
              // 强制更新视图
              this.$forceUpdate()
            } else {
              // 如果加载失败，标记为空数组
              this.serviceStepsCache[orderId] = []
            }
          }).catch(() => {
            // 如果加载失败，标记为空数组
            this.serviceStepsCache[orderId] = []
          })
        })
      },
      loadServiceSteps(orderId) {
        // 避免重复加载
        if (this.serviceStepsCache[orderId] !== undefined) {
          return
        }
        // 标记为加载中
        this.serviceStepsCache[orderId] = null
        
        // 调用API获取步骤信息
        getAction('/sys/auditTask/getOrderAuditProgress', { orderId: orderId }).then((res) => {
          if (res.success && res.result && res.result.steps) {
            // 提取步骤信息
            const steps = []
            res.result.steps.forEach(stepInfo => {
              const stepOrder = stepInfo.stepOrder || 0
              const tasks = stepInfo.tasks || []
              
              tasks.forEach(task => {
                // 过滤掉流失审核任务（taskType='loss'），避免重复显示
                if (task.taskType !== 'loss') {
                  steps.push({
                    stepOrder: stepOrder,
                    roleName: task.roleName || '',
                    userName: task.assignedUserName || task.auditUserName || '',
                    assignedUserName: task.assignedUserName || '',
                    auditUserName: task.auditUserName || ''
                  })
                }
              })
            })
            
            // 缓存步骤信息
            this.serviceStepsCache[orderId] = steps
            
            // 强制更新视图
            this.$forceUpdate()
          } else {
            // 如果加载失败，标记为空数组
            this.serviceStepsCache[orderId] = []
          }
        }).catch(() => {
          // 如果加载失败，标记为空数组
          this.serviceStepsCache[orderId] = []
        })
      }
    }
  }
</script>

<style scoped>
  @import '~@assets/less/common.less'

  .expense-detail-cell {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .expense-detail-tag {
    cursor: pointer;
    font-size: 12px;
    line-height: 18px;
    padding: 0 6px;
    margin: 0;
  }
  
  .view-mode-toggle-extra {
    display: flex;
    align-items: center;
    margin-right: 0;
  }
  
  .view-mode-toggle {
    border-radius: 4px;
    overflow: hidden;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper {
    border: none;
    padding: 6px 16px;
    height: auto;
    line-height: 1.5;
    transition: all 0.3s ease;
    font-size: 14px;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper:first-child {
    border-radius: 4px 0 0 4px;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper:last-child {
    border-radius: 0 4px 4px 0;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper:not(:first-child)::before {
    display: none;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper-checked {
    background: #1890ff;
    color: #fff;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled):hover {
    background: #40a9ff;
    color: #fff;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked):hover {
    color: #1890ff;
    background: #f0f7ff;
  }
  
  .view-mode-toggle >>> .ant-radio-button-wrapper span {
    margin-left: 4px;
  }
  
  /* 优化 tabs 样式 */
  .enhanced-tabs {
    margin-bottom: 0;
  }
  
  .enhanced-tabs >>> .ant-tabs-bar {
    margin-bottom: 0;
    border-bottom: none;
  }
  
  .enhanced-tabs >>> .ant-tabs-tab {
    padding: 12px 20px;
    margin: 0 4px 0 0;
    font-size: 14px;
    font-weight: 500;
    color: #666;
    border: 1px solid transparent;
    border-radius: 6px 6px 0 0;
    transition: all 0.3s ease;
    background: #fafafa;
  }
  
  .enhanced-tabs >>> .ant-tabs-tab:hover {
    color: #1890ff;
    background: #f0f7ff;
  }
  
  .enhanced-tabs >>> .ant-tabs-tab-active {
    color: #1890ff;
    background: #fff;
    border-color: #f0f0f0 #f0f0f0 #fff;
    border-bottom-color: #fff;
    font-weight: 600;
  }
  
  .enhanced-tabs >>> .ant-tabs-tab-active:hover {
    color: #1890ff;
    background: #fff;
  }
  
  .enhanced-tabs >>> .ant-tabs-tab .anticon {
    margin-right: 6px;
    font-size: 14px;
  }
  
  .enhanced-tabs >>> .ant-tabs-ink-bar {
    height: 3px;
    background: linear-gradient(90deg, #1890ff 0%, #40a9ff 100%);
    border-radius: 2px;
  }
  
  .enhanced-tabs >>> .ant-tabs-content {
    padding-top: 0;
  }
  
  /* 优化表格样式 */
  .enhanced-table {
    background: #fff;
    border-radius: 0 0 6px 6px;
    overflow: hidden;
    margin-top: 0;
  }
  
  .enhanced-table >>> .ant-table {
    border-radius: 0 0 6px 6px;
    border: 1px solid #f0f0f0;
    border-top: none;
  }
  
  .enhanced-table >>> .ant-table-thead > tr > th {
    background: #fafafa;
    color: #262626;
    font-weight: 500;
    font-size: 14px;
    padding: 14px 16px;
    border: 1px solid #f0f0f0;
    border-top: none;
    border-bottom: 1px solid #e8e8e8;
    text-align: center;
    transition: background 0.3s ease;
  }
  
  .enhanced-table >>> .ant-table-thead > tr > th:not(:last-child):not(.ant-table-selection-column):not(.ant-table-row-expand-icon-cell):not([colspan])::before {
    display: none;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr > td {
    padding: 14px 16px;
    border: 1px solid #f0f0f0;
    color: #595959;
    font-size: 14px;
    transition: all 0.3s ease;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr:hover > td {
    background: #f0f7ff;
    color: #1890ff;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr:last-child > td {
    border-bottom: 1px solid #f0f0f0;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr:last-child > td:first-child {
    border-bottom-left-radius: 6px;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr:last-child > td:last-child {
    border-bottom-right-radius: 6px;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr:nth-child(even) {
    background: #fafafa;
  }
  
  .enhanced-table >>> .ant-table-tbody > tr:nth-child(even):hover > td {
    background: #f0f7ff;
  }
  
  .enhanced-table >>> .ant-table-placeholder {
    border: 1px solid #f0f0f0;
    border-top: none;
  }
  
  .enhanced-table >>> .ant-pagination {
    margin: 16px 0;
  }
</style>

