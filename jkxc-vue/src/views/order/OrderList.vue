<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="订单编号">
              <JInput placeholder="请输入订单编号" v-model="queryParam.orderNo" type="like" />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like"></JInput>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务员">
              <j-search-select-tag
                placeholder="请输入业务员"
                v-model="queryParam.salesman"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
              >
              </j-search-select-tag>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="订单状态" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-dict-select-tag
                placeholder="请选择订单状态"
                v-model="queryParam.orderStatus"
                dictCode="order_status"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务类型" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-category-select
                v-model="queryParam.businessType"
                back="label"
                pcode="A01"
                placeholder="请选择业务类型"
                loadTriggleChange
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="客户来源" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-dict-select-tag
                placeholder="请选择客户来源"
                v-model="queryParam.opportunitySource"
                dictCode="opportunity_source"
              />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="下单时间" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-range-picker
                style="width: 210px"
                v-model="queryParam.createDateRange"
                format="YYYY-MM-DD"
                @change="onCreateDateChange"
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
    <!-- 查询区域-END -->

    <!-- table区域-begin -->
    <div class="order-table-wrapper">
      <div class="order-table-header">
        <a-button @click="handleAdd" type="primary" icon="plus">添加订单</a-button>
      </div>
      <a-table
        ref="table"
        size="middle"
        :scroll="{ x: 2000 }"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        class="j-table-force-nowrap"
        @change="handleTableChange"
      >
        <template slot="companyName" slot-scope="text, record">
          <a-tooltip :title="record.companyName" placement="topLeft">
            <a-tag 
              color="blue" 
              @click="showCompanyInfo(record)"
              style="max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block;"
            >
              {{ record.companyName }}
            </a-tag>
          </a-tooltip>
        </template>
        <template slot="process" slot-scope="text, record">
          <a-button type="primary" icon="stock" size="small" @click="showProcessModal(record)">流程</a-button>
        </template>
        <template slot="payment" slot-scope="text, record">
          <a-tag color="blue" @click="showPaymentModal(record)">收费详情</a-tag>
        </template>
        <template slot="orderStatus" slot-scope="text, record">
          <a-tag :color="getOrderStatusColor(record.orderStatus)">{{ getOrderStatusText(record) }}</a-tag>
        </template>
        <template slot="isRecurring" slot-scope="text, record">
          <a-tag 
            :color="getIsRecurringColor(record.isRecurring)"
            style="cursor: pointer;"
            @click="handleRecurringClick(record)"
          >
            {{ getIsRecurringText(record) }}
          </a-tag>
        </template>
        <template slot="operationLog" slot-scope="text, record">
          <a-button type="link" size="small" @click="showOperationLogModal(record)">
            <a-icon type="history" /> 查看记录
          </a-button>
        </template>
        <span slot="action" slot-scope="text, record">
          <a-dropdown>
            <a-menu slot="overlay">
              <a-menu-item key="edit" v-if="canEdit(record)">
                <a @click="handleEdit(record)">修改信息</a>
              </a-menu-item>
              <a-menu-item key="reject" v-if="canReject(record)">
                <a @click="handleReject(record)">驳回订单</a>
              </a-menu-item>
              <a-menu-item key="refund" v-if="canRefund(record)">
                <a @click="handleRefund(record)">退单申请</a>
              </a-menu-item>
              <a-menu-item key="auditRefund" v-if="canAuditRefund(record)" v-has="'order:auditRefund'">
                <a @click="handleAuditRefund(record)">退单审核</a>
              </a-menu-item>
              <a-menu-item key="confirm" v-if="canConfirm(record)">
                <a @click="handleConfirm(record)">确认订单</a>
              </a-menu-item>
              <a-menu-item key="detail">
                <a @click="handleDetail(record)">订单详情</a>
              </a-menu-item>
              <a-menu-item key="operationLog">
                <a @click="showOperationLogModal(record)">操作记录</a>
              </a-menu-item>
            </a-menu>
            <a-button type="link" size="small">
              操作 <a-icon type="down" />
            </a-button>
          </a-dropdown>
        </span>
      </a-table>
    </div>

    <!-- 订单表单弹窗 -->
    <order-modal ref="modalForm" @ok="modalFormOk"></order-modal>

    <!-- 复购信息弹窗（查看客户所有订单） -->
    <customer-order-modal ref="customerOrderModal"></customer-order-modal>

    <!-- 客户详情弹窗 -->
    <customer-detail-modal ref="customerDetailModal"></customer-detail-modal>

    <!-- 流程信息弹窗 -->
    <j-modal
      :visible.sync="processModal.visible"
      :width="1200"
      :title="processModal.title"
      :fullscreen.sync="processModal.fullscreen"
      :switchFullscreen="processModal.switchFullscreen"
    >
      <order-step v-if="processModal.visible" :id="processModal.orderId" :operationType="processModal.operationType"></order-step>
    </j-modal>

    <!-- 收费详情弹窗 -->
    <j-modal
      :visible.sync="paymentModal.visible"
      :width="1200"
      :title="paymentModal.title"
      :fullscreen.sync="paymentModal.fullscreen"
      :switchFullscreen="paymentModal.switchFullscreen"
    >
      <order-payment :orderId="paymentModal.orderId" :companyName="paymentModal.companyName"></order-payment>
    </j-modal>

    <!-- 退单弹窗 -->
    <j-modal
      :visible.sync="refundModal.visible"
      :width="700"
      title="退单申请"
      :confirmLoading="refundModal.loading"
      @ok="confirmRefund"
      @cancel="handleRefundCancel"
    >
      <a-spin :spinning="refundModal.loading">
        <a-alert
          message="退单提示"
          description="退单后，订单状态将变更为「已退单」，系统将自动扣除相关资金日记记录，并回退未提交的报销记录。请确认是否继续。"
          type="warning"
          show-icon
          style="margin-bottom: 16px;"
        />
        <a-form-model :model="refundModal" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-form-model-item label="订单编号">
            <span style="font-weight: 600; color: #1890ff;">{{ refundModal.orderNo || '--' }}</span>
          </a-form-model-item>
          <a-form-model-item label="公司名称">
            <span>{{ refundModal.companyName || '--' }}</span>
          </a-form-model-item>
          <a-form-model-item label="订单金额">
            <span v-if="refundModal.contractAmount" style="color: #f5222d; font-weight: 600;">
              ¥{{ formatAmount(refundModal.contractAmount) }}
            </span>
            <span v-else>--</span>
          </a-form-model-item>
          <a-form-model-item label="业务类型">
            <span>{{ refundModal.businessType || '--' }}</span>
          </a-form-model-item>
          <a-form-model-item label="订单状态">
            <a-tag :color="getOrderStatusColor(refundModal.orderStatus)">
              {{ getOrderStatusTextForRefund(refundModal.orderStatus) }}
            </a-tag>
          </a-form-model-item>
          <a-form-model-item label="退单原因" required>
            <a-textarea
              v-model="refundModal.reason"
              :rows="4"
              placeholder="请输入退单原因（必填，最多500字）"
              :maxLength="500"
              :disabled="refundModal.loading"
            />
            <div style="margin-top: 4px; color: #999; font-size: 12px;">
              已输入 {{ refundModal.reason ? refundModal.reason.length : 0 }} / 500 字
            </div>
          </a-form-model-item>
        </a-form-model>
      </a-spin>
    </j-modal>

    <!-- 驳回弹窗 -->
    <j-modal
      :visible.sync="rejectModal.visible"
      :width="600"
      title="驳回"
      @ok="confirmReject"
      @cancel="rejectModal.visible = false"
    >
      <a-form-model :model="rejectModal" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-model-item label="订单编号">
          <span>{{ rejectModal.orderNo }}</span>
        </a-form-model-item>
        <a-form-model-item label="驳回原因" required>
          <a-textarea
            v-model="rejectModal.reason"
            :rows="4"
            placeholder="请输入驳回原因"
            :maxLength="500"
          />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 操作记录弹窗 -->
    <j-modal
      :visible.sync="operationLogModal.visible"
      :width="1400"
      :title="operationLogModal.title + '（订单编号：' + operationLogModal.orderNo + '）'"
      :fullscreen.sync="operationLogModal.fullscreen"
      :switchFullscreen="operationLogModal.switchFullscreen"
    >
      <order-operation-log :orderId="operationLogModal.orderId"></order-operation-log>
    </j-modal>


    <!-- 公司信息弹窗 -->
    <j-modal
      :visible.sync="companyModal.visible"
      :width="1200"
      :title="companyModal.title + (companyInformation.corporateName ? '（' + companyInformation.corporateName + '）' : '')"
      :fullscreen.sync="companyModal.fullscreen"
      :switchFullscreen="companyModal.switchFullscreen"
    >
      <a-spin :spinning="companyModal.loading">
        <a-descriptions :column="2" bordered v-if="companyInformation && Object.keys(companyInformation).length > 0">
          <a-descriptions-item label="公司名称" :span="2">
            <span style="font-size: 16px; font-weight: 600; color: #1890ff;">
              {{ companyInformation.corporateName || companyInformation.companyName || '--' }}
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="统一社会信用代码">
            {{ companyInformation.unifiedSocialCreditCode || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="法定代表人">
            {{ companyInformation.legalRepresentative || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="联系人">
            {{ companyInformation.contacts || companyInformation.contactPerson || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="联系电话">
            {{ companyInformation.contactInformation || companyInformation.contactPhone || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="所属区域">
            {{ companyInformation.region || companyInformation.area || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="详细地址" :span="2">
            {{ companyInformation.address || companyInformation.detailedAddress || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="注册地址">
            {{ companyInformation.registeredAddress || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="注册资本">
            {{ companyInformation.registeredCapital || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="成立日期">
            {{ companyInformation.establishmentDate || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="营业期限">
            {{ companyInformation.businessTerm || '--' }}
          </a-descriptions-item>
          <a-descriptions-item label="经营范围" :span="2">
            <div style="max-height: 100px; overflow-y: auto;">
              {{ companyInformation.businessScope || '--' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">
            {{ companyInformation.remarks || '--' }}
          </a-descriptions-item>
        </a-descriptions>
        <a-empty v-else description="暂无公司信息" style="padding: 40px 0" />
      </a-spin>
    </j-modal>
  </a-card>
</template>

<script>
import { getCompanyInformation } from '@/api/api'
import { getAction, httpAction } from '@/api/manage'
import { mixinDevice } from '@/utils/mixin'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import OrderModal from './modules/OrderModal'
import OrderStep from './modules/OrderStep'
import OrderPayment from './modules/OrderPayment'
import OrderOperationLog from './modules/OrderOperationLog'
import CustomerOrderModal from '@/views/customer/modules/CustomerOrderModal'
import CustomerDetailModal from '@/views/customer/modules/CustomerDetailModal'
import { filterMultiDictText, filterDictTextByCache } from '@/components/dict/JDictSelectUtil'
import Vue from 'vue'

export default {
  name: 'OrderList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    OrderModal,
    OrderStep,
    OrderPayment,
    OrderOperationLog,
    CustomerOrderModal,
    CustomerDetailModal,
  },
  data() {
    return {
      description: '订单管理页面',
      // 禁用 mixin 的自动查询，手动控制查询时机
      disableMixinCreated: true,
      // 表头
      columns: [],
      defColumns: [
        {
          title: '订单编号',
          align: 'center',
          dataIndex: 'orderNo',
          width: 150,
        },
        {
          title: '下单时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 180,
          customRender: function (text) {
            if (!text) return '-';
            return text.length > 19 ? text.substr(0, 19) : text;
          },
        },
        {
          title: '业务人员',
          align: 'center',
          dataIndex: 'salesman',
          width: 120,
        },
        {
          title: '所属团队',
          align: 'center',
          dataIndex: 'teamName',
          width: 120,
          customRender: (text) => {
            return text || '-';
          },
        },
        {
          title: '公司名称',
          align: 'center',
          dataIndex: 'companyName',
          width: 200,
          scopedSlots: { customRender: 'companyName' },
        },
        {
          title: '业务类型',
          align: 'center',
          dataIndex: 'businessType',
          width: 150,
          customRender: (text, record) => record.businessType_dictText || text || '-',
        },
        {
          title: '客户来源',
          align: 'center',
          dataIndex: 'opportunitySource',
          width: 120,
          customRender: (text, record) => {
            if (record.opportunitySource_dictText) {
              return record.opportunitySource_dictText;
            }
            return text || '-';
          },
        },
        {
          title: '订单状态',
          align: 'center',
          dataIndex: 'orderStatus',
          width: 120,
          scopedSlots: { customRender: 'orderStatus' },
        },
        {
          title: '流程进度',
          align: 'center',
          dataIndex: 'process',
          width: 100,
          scopedSlots: { customRender: 'process' },
        },
        {
          title: '费用详情',
          align: 'center',
          dataIndex: 'payment',
          width: 100,
          scopedSlots: { customRender: 'payment' },
        },
        {
          title: '复购信息',
          align: 'center',
          dataIndex: 'isRecurring',
          width: 100,
          scopedSlots: { customRender: 'isRecurring' },
        },
        {
          title: '备注信息',
          align: 'left',
          dataIndex: 'remarks',
          width: 200,
          customRender: (text) => {
            return text || '-';
          },
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: 'center',
          fixed: 'right',
          width: 150,
          scopedSlots: { customRender: 'action' },
        },
      ],
      url: {
        list: '/order/list',
        delete: '/order/delete',
        deleteBatch: '/order/deleteBatch',
      },
      dictOptions: {},
      companyInformation: {},
      //列设置
      settingColumns: [],
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      processModal: {
        title: '流程信息',
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
        operationType: '',
      },
      paymentModal: {
        title: '收费详情',
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
        companyName: '',
      },
      companyModal: {
        title: '公司信息',
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        loading: false,
      },
      operationLogModal: {
        title: '操作记录',
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
        orderNo: '',
      },
      refundModal: {
        visible: false,
        loading: false,
        orderId: '',
        orderNo: '',
        companyName: '',
        contractAmount: '',
        orderStatus: '',
        businessType: '',
        reason: '',
        orderInfo: null, // 保存完整的订单信息
      },
      rejectModal: {
        visible: false,
        orderId: '',
        orderNo: '',
        reason: '',
      },
    }
  },
  created() {
    // 先设置查询参数
    // 检查路由参数，如果有orderNo，自动填入搜索框
    if (this.$route.query && this.$route.query.orderNo) {
      this.queryParam.orderNo = this.$route.query.orderNo
    }
    this.initColumns()
    this.initDictConfig()
    // 由于禁用了 mixin 的自动查询，需要手动调用 loadData()
    // 此时 queryParam.orderNo 已经设置好了（如果有路由参数的话）
    // 使用 $nextTick 确保在表格初始化完成后再查询，避免 handleTableChange 触发额外的查询
    this.$nextTick(() => {
      this.loadData()
    })
  },
  methods: {
    onCreateDateChange(value, dateString) {
      this.queryParam.createTime_begin = dateString[0]
      this.queryParam.createTime_end = dateString[1]
    },
    showProcessModal(record) {
      this.processModal.orderId = record.id
      this.processModal.operationType = record.operationType || 'default'
      this.processModal.visible = true
    },
    showPaymentModal(record) {
      this.paymentModal.orderId = record.id
      this.paymentModal.companyName = record.companyName
      this.paymentModal.visible = true
    },
    showCompanyInfo(record) {
      // 优先使用companyId，如果没有则根据公司名称查询
      let companyId = record.companyId || record.dataId
      
      if (!companyId && record.companyName) {
        // 如果没有公司ID，根据公司名称查询
        getAction('/customer/ghCustomer/list', { 
          corporateName: record.companyName, 
          delFlag: 0, 
          pageNo: 1, 
          pageSize: 1 
        }).then((res) => {
          if (res.success && res.result && res.result.records && res.result.records.length > 0) {
            companyId = res.result.records[0].id
            // 使用客户详情弹窗展示信息
            this.$refs.customerDetailModal.show({ id: companyId })
          } else {
            this.$message.warning('未找到公司信息，请检查公司名称是否正确')
          }
        }).catch((err) => {
          console.error('查询公司ID失败:', err)
          this.$message.error('查询公司信息失败')
        })
      } else if (!companyId) {
        this.$message.warning('未找到公司ID和公司名称')
      } else {
        // 使用客户详情弹窗展示信息
        this.$refs.customerDetailModal.show({ id: companyId })
      }
    },
    showOperationLogModal(record) {
      this.operationLogModal.orderId = record.id
      this.operationLogModal.orderNo = record.orderNo || '--'
      this.operationLogModal.visible = true
    },
    getOrderStatusColor(status) {
      const colorMap = {
        '0': 'orange', // 待审核
        '1': 'blue',   // 进行中
        '2': 'green',  // 已完成
        '3': 'volcano', // 已取消（使用火山红/橙红色，确保前景色和背景色有区别）
        '4': 'red'     // 已驳回
      }
      return colorMap[status] || 'default'
    },
    // 获取复购信息的颜色
    getIsRecurringColor(isRecurring) {
      if (isRecurring === '1' || isRecurring === 1) {
        return 'green' // 是 - 绿色
      } else if (isRecurring === '0' || isRecurring === 0) {
        return 'orange' // 否 - 橙色
      }
      return 'default' // 未设置 - 默认灰色
    },
    // 获取复购信息的文本（显示订单数量）
    getIsRecurringText(record) {
      const isRecurring = record.isRecurring
      const orderCount = record.customerOrderCount
      
      if (isRecurring === '1' || isRecurring === 1) {
        // 如果有订单数量，显示数量
        if (orderCount != null && orderCount > 0) {
          return `${orderCount}单`
        }
        return '是'
      } else if (isRecurring === '0' || isRecurring === 0) {
        // 即使不是复购，也显示订单数量
        if (orderCount != null && orderCount > 0) {
          return `${orderCount}单`
        }
        return '否'
      }
      // 如果订单数量存在，显示数量
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
        id: record.companyId, // 客户ID
        corporateName: record.companyName, // 公司名称
        totalSpending: null, // 总消费金额（如果后端有返回的话）
      }
      this.$refs.customerOrderModal.show(customerRecord)
    },
    getOrderStatusText(record) {
      // 如果订单已完成，直接显示"已完成"
      if (record.orderStatus === '2') {
        return '已完成'
      }
      
      // 如果订单未完成（进行中），显示距离现在过了多少天
      // 使用第一个审核步骤通过的时间来计算天数，如果没有则使用创建时间
      if (record.orderStatus === '1') {
        try {
          // 优先使用第一个审核步骤通过的时间，如果没有则使用创建时间
          const startTime = record.firstAuditTime ? new Date(record.firstAuditTime) : (record.createTime ? new Date(record.createTime) : null)
          if (!startTime) {
            return '进行中'
          }
          
          const now = new Date()
          const diffTime = now.getTime() - startTime.getTime()
          const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
          
          if (diffDays < 0) {
            return '进行中'
          } else if (diffDays === 0) {
            return '进行中（今天）'
          } else {
            return `进行中（${diffDays}天）`
          }
        } catch (e) {
          console.error('计算订单天数失败:', e)
          return '进行中'
        }
      }
      
      // 其他状态使用原来的逻辑
      if (record.orderStatus_dictText) {
        return record.orderStatus_dictText
      }
      if (record.orderStatus) {
        const text = filterDictTextByCache('order_status', record.orderStatus)
        if (text) {
          return text
        }
        const statusMap = {
          '0': '待审核',
          '1': '进行中',
          '2': '已完成',
          '3': '已取消',
          '4': '已驳回'
        }
        return statusMap[record.orderStatus] || record.orderStatus
      }
      return '—'
    },
    // 重写handleEdit方法，先获取完整的订单数据（包含动态表单数据）
    handleEdit(record) {
      if (!record || !record.id) {
        this.$message.warning('订单ID不存在')
        return
      }
      
      // 检查是否可以编辑（第一个审核步骤通过后不能修改）
      if (!this.canEdit(record)) {
        this.$message.warning('订单已通过第一个审核步骤，不能修改信息。如需修改，请联系管理员。')
        return
      }
      
      // 先调用queryById获取完整的订单数据（包含动态表单数据）
      getAction('/order/queryById', { id: record.id }).then((res) => {
        if (res.success && res.result) {
          // 再次检查是否可以编辑（防止在查询过程中状态发生变化）
          if (res.result.orderStatus === '1' || res.result.orderStatus === '2' || res.result.firstAuditTime) {
            this.$message.warning('订单已通过第一个审核步骤，不能修改信息。如需修改，请联系管理员。')
            return
          }
          // 使用完整的数据进行编辑
          this.$refs.modalForm.edit(res.result)
          this.$refs.modalForm.title = "编辑"
          this.$refs.modalForm.disableSubmit = false
        } else {
          this.$message.error(res.message || '获取订单详情失败')
        }
      }).catch((err) => {
        console.error('获取订单详情失败:', err)
        this.$message.error('获取订单详情失败')
      })
    },
    // 重写handleDetail方法，先获取完整的订单数据（包含动态表单数据）
    handleDetail(record) {
      if (!record || !record.id) {
        this.$message.warning('订单ID不存在')
        return
      }
      // 先调用queryById获取完整的订单数据（包含动态表单数据）
      getAction('/order/queryById', { id: record.id }).then((res) => {
        if (res.success && res.result) {
          // 使用完整的数据显示详情
          this.$refs.modalForm.edit(res.result)
          this.$refs.modalForm.title = "详情"
          this.$refs.modalForm.disableSubmit = true
        } else {
          this.$message.error(res.message || '获取订单详情失败')
        }
      }).catch((err) => {
        console.error('获取订单详情失败:', err)
        this.$message.error('获取订单详情失败')
      })
    },
    // 判断是否可以编辑（第一个审核步骤通过后不能修改）
    canEdit(record) {
      // 如果订单状态是"1"（进行中）或"2"（已完成），说明已经通过了第一个审核步骤，不能修改
      // 或者如果firstAuditTime存在，说明第一个审核步骤已通过，不能修改
      // 支持字符串和数字类型的比较
      if (record.orderStatus === '1' || record.orderStatus === 1 || 
          record.orderStatus === '2' || record.orderStatus === 2) {
        return false
      }
      if (record.firstAuditTime) {
        return false
      }
      // 其他情况可以编辑（待审核、已取消、已驳回等）
      return true
    },
    // 判断是否可以退单（未审核和进行中的订单可以退单，与客户信息无关）
    canRefund(record) {
      if (!record) {
        return false
      }
      
      // 支持字符串和数字类型的比较
      // 订单状态为 '0' 或 0 表示未审核，'1' 或 1 表示进行中，都可以退单
      const orderStatus = record.orderStatus
      const orderStatusText = record.orderStatus_dictText
      
      // 调试日志（开发时可以查看）
      // console.log('canRefund check:', {
      //   orderStatus,
      //   orderStatusType: typeof orderStatus,
      //   orderStatusText,
      //   recordId: record.id,
      //   orderNo: record.orderNo
      // })
      
      // 兼容多种格式：字符串 '0'/'1'、数字 0/1、字符串 '待审核'/'进行中' 等
      const isPending = orderStatus === '0' || 
                       orderStatus === 0 || 
                       String(orderStatus) === '0' ||
                       orderStatus === '待审核' ||
                       (orderStatusText && orderStatusText.includes('待审核'))
      
      const isInProgress = orderStatus === '1' || 
                          orderStatus === 1 || 
                          String(orderStatus) === '1' ||
                          orderStatus === '进行中' ||
                          (orderStatusText && orderStatusText.includes('进行中'))
      
      return isPending || isInProgress
    },
    
    // 判断是否可以退单审核（订单状态为退单状态 '3'）
    canAuditRefund(record) {
      if (!record) {
        return false
      }
      
      // 订单状态为 '3' 表示已退单，可以审核
      const orderStatus = record.orderStatus
      const isRefunded = orderStatus === '3' || orderStatus === 3
      
      return isRefunded
    },
    // 判断是否可以驳回（只有进行中的订单可以驳回）
    canReject(record) {
      // 支持字符串和数字类型的比较
      return record.orderStatus === '1' || record.orderStatus === 1 // 进行中
    },
    // 判断是否可以确认（必须在工商人员完成交接后才能确认）
    canConfirm(record) {
      // 只有进行中的订单可以确认（支持字符串和数字类型的比较）
      if (record.orderStatus !== '1' && record.orderStatus !== 1) {
        return false
      }
      // 必须有任务，且任务状态为completed，交接状态为completed
      if (record.taskStatus === 'completed' && record.handoverStatus === 'completed') {
        return true
      }
      return false
    },
    // 确认（一次性业务）
    handleConfirm(record) {
      this.$confirm({
        title: '确认订单',
        content: `确定要确认订单 ${record.orderNo || record.id} 吗？确认后订单状态将变为已完单。`,
        onOk: () => {
          httpAction('/order/confirm', {
            id: record.id
          }, 'POST').then((res) => {
            if (res.success) {
              this.$message.success('确认成功')
              this.loadData()
            } else {
              this.$message.error(res.message || '确认失败')
            }
          }).catch((err) => {
            console.error('确认失败:', err)
            this.$message.error('确认失败')
          })
        }
      })
    },
    // 退单
    handleRefund(record) {
      this.refundModal.orderId = record.id
      this.refundModal.orderNo = record.orderNo || '--'
      this.refundModal.companyName = record.companyName || '--'
      this.refundModal.contractAmount = record.contractAmount || record.orderAmount || ''
      this.refundModal.orderStatus = record.orderStatus || ''
      this.refundModal.businessType = record.businessType_dictText || record.businessTypeName || record.businessType || '--'
      this.refundModal.reason = ''
      this.refundModal.orderInfo = record
      this.refundModal.loading = false
      this.refundModal.visible = true
    },
    // 取消退单
    handleRefundCancel() {
      this.refundModal.visible = false
      this.refundModal.reason = ''
      this.refundModal.orderInfo = null
      this.refundModal.loading = false
    },
    // 确认退单
    confirmRefund() {
      // 验证退单原因
      if (!this.refundModal.reason || this.refundModal.reason.trim() === '') {
        this.$message.warning('请输入退单原因')
        return
      }
      
      if (this.refundModal.reason.trim().length < 5) {
        this.$message.warning('退单原因至少需要5个字符')
        return
      }
      
      // 确认对话框
      const that = this
      this.$confirm({
        title: '确认退单',
        content: `确定要退单订单"${this.refundModal.orderNo}"吗？退单后订单状态将变更为"已退单"，系统将自动处理相关资金记录。`,
        okText: '确认退单',
        okType: 'danger',
        cancelText: '取消',
        onOk() {
          that.refundModal.loading = true
          httpAction('/order/refund', {
            id: that.refundModal.orderId,
            reason: that.refundModal.reason.trim()
          }, 'POST').then((res) => {
            if (res.success) {
              that.$message.success('退单成功！订单状态已更新为"已退单"')
              that.refundModal.visible = false
              that.refundModal.reason = ''
              that.refundModal.orderInfo = null
              that.loadData()
            } else {
              that.$message.error(res.message || '退单失败')
            }
          }).catch((err) => {
            console.error('退单失败:', err)
            that.$message.error('退单失败：' + (err.message || '未知错误'))
          }).finally(() => {
            that.refundModal.loading = false
          })
        }
      })
    },
    // 获取订单状态文本（用于退单弹窗）
    getOrderStatusTextForRefund(status) {
      const statusMap = {
        '0': '待审核',
        '1': '进行中',
        '2': '已完成',
        '3': '已退单',
        '4': '已驳回'
      }
      return statusMap[status] || '未知状态'
    },
    // 格式化金额
    formatAmount(amount) {
      if (!amount && amount !== 0) return '0.00'
      const num = Number(amount)
      if (isNaN(num)) return '0.00'
      return num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    // 退单审核
    handleAuditRefund(record) {
      // 从订单备注中提取退单原因
      let refundReason = ''
      if (record.remarks) {
        // 查找"退单原因："后面的内容
        const reasonMatch = record.remarks.match(/退单原因[：:]\s*(.+?)(?:\n|$)/)
        if (reasonMatch && reasonMatch[1]) {
          refundReason = reasonMatch[1].trim()
        }
      }
      
      // 构建确认对话框内容
      let content = `确定要审核通过退单订单"${record.orderNo}"吗？`
      if (refundReason) {
        content += `\n\n退单原因：${refundReason}`
      }
      content += `\n\n审核通过后，订单将被删除，相关的支出、收入和银行日记账记录也将被删除并平账。`
      
      const that = this
      this.$confirm({
        title: '退单审核',
        content: content,
        okText: '确认审核',
        okType: 'danger',
        cancelText: '取消',
        width: 600,
        onOk() {
          httpAction('/order/auditRefund', {
            id: record.id
          }, 'POST').then((res) => {
            if (res.success) {
              that.$message.success('退单审核成功！订单及相关财务记录已删除')
              that.loadData()
            } else {
              that.$message.error(res.message || '退单审核失败')
            }
          }).catch((err) => {
            console.error('退单审核失败:', err)
            that.$message.error('退单审核失败：' + (err.message || '未知错误'))
          })
        }
      })
    },
    // 驳回
    handleReject(record) {
      this.rejectModal.orderId = record.id
      this.rejectModal.orderNo = record.orderNo || '--'
      this.rejectModal.reason = ''
      this.rejectModal.visible = true
    },
    // 确认驳回
    confirmReject() {
      if (!this.rejectModal.reason || this.rejectModal.reason.trim() === '') {
        this.$message.warning('请输入驳回原因')
        return
      }
      httpAction('/order/reject', {
        id: this.rejectModal.orderId,
        reason: this.rejectModal.reason
      }, 'POST').then((res) => {
        if (res.success) {
          this.$message.success('驳回成功')
          this.rejectModal.visible = false
          this.loadData()
        } else {
          this.$message.error(res.message || '驳回失败')
        }
      }).catch((err) => {
        console.error('驳回失败:', err)
        this.$message.error('驳回失败')
      })
    },
    initDictConfig() {
      // 初始化字典配置，保持sys_category和sys_dict不变
      // 这里可以根据需要加载业务类型等字典
    },
    initColumns() {
      // 直接使用 defColumns，不依赖 localStorage 的列设置
      // 这样可以确保所有列都能正确显示
      this.columns = this.defColumns
      
      // 如果需要保留列设置功能，可以取消下面的注释
      /*
      var key = this.$route.name + ':colsettings'
      let colSettings = Vue.ls.get(key)
      if (colSettings == null || colSettings == undefined) {
        let allSettingColumns = []
        this.defColumns.forEach(function (item, i, array) {
          allSettingColumns.push(item.dataIndex)
        })
        this.settingColumns = allSettingColumns
        this.columns = this.defColumns
      } else {
        this.settingColumns = colSettings
        const cols = this.defColumns.filter((item) => {
          if (item.dataIndex == 'action') {
            return true
          }
          if (colSettings.includes(item.dataIndex)) {
            return true
          }
          return false
        })
        this.columns = cols
      }
      */
    },
  },
}
</script>
<style scoped>
@import '~@assets/less/common.less';

/* 订单表格包装器 - 让按钮和表格合为一体 */
.order-table-wrapper {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.order-table-header {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: none;
}

.order-table-wrapper >>> .ant-table {
  border: none;
  border-radius: 0;
  border-top: none;
}

.order-table-wrapper >>> .ant-table-wrapper {
  border-top: none;
}

.order-table-wrapper >>> .ant-table-thead > tr > th {
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.order-table-wrapper >>> .ant-table-thead > tr:first-child > th:first-child {
  border-left: none;
  border-top-left-radius: 0;
}

.order-table-wrapper >>> .ant-table-thead > tr:first-child > th:last-child {
  border-right: none;
  border-top-right-radius: 0;
}

.order-table-wrapper >>> .ant-table-tbody > tr > td:first-child {
  border-left: none;
}

.order-table-wrapper >>> .ant-table-tbody > tr > td:last-child {
  border-right: none;
}

.order-table-wrapper >>> .ant-table-tbody > tr:last-child > td:first-child {
  border-bottom-left-radius: 4px;
}

.order-table-wrapper >>> .ant-table-tbody > tr:last-child > td:last-child {
  border-bottom-right-radius: 4px;
}

/* 修复表格左右两侧的空白问题 */
.ant-table-wrapper {
  overflow: visible;
}

.ant-table-body {
  overflow-x: auto !important;
}

/* 修复 fixed 列导致的空白 */
.ant-table-fixed-left,
.ant-table-fixed-right {
  box-shadow: none;
}

.ant-table-scroll {
  overflow-x: auto;
}
</style>

