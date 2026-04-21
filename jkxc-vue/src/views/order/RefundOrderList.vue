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
          <a-col :md="8" :sm="24">
            <a-form-item label="创建时间" :labelCol="labelCol" :wrapperCol="wrapperCol">
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

    <!-- Tab页 -->
    <a-tabs v-model="activeTab" @change="handleTabChange" style="margin-top: 16px; margin-bottom: 16px;">
      <a-tab-pane key="pending" tab="待审核">
        <!-- table区域-begin -->
        <div class="order-table-wrapper">
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
          <a-tag color="blue" @click="showCompanyInfo(record)">{{ record.companyName }}</a-tag>
        </template>
        <template slot="orderStatus" slot-scope="text, record">
          <a-tag :color="getOrderStatusColor(record.orderStatus)">已退单</a-tag>
        </template>
        <template slot="refundReason" slot-scope="text, record">
          <span>{{ getRefundReason(record) }}</span>
        </template>
        <template slot="operationLog" slot-scope="text, record">
          <a-button type="link" size="small" @click="showOperationLogModal(record)">
            <a-icon type="history" /> 查看记录
          </a-button>
        </template>
        <span slot="action" slot-scope="text, record">
          <a-button type="primary" size="small" @click="handleAuditRefund(record)" v-has="'order:auditRefund'">
            退单审核
          </a-button>
        </span>
          </a-table>
        </div>
      </a-tab-pane>
      <a-tab-pane key="completed" tab="已完成">
        <!-- table区域-begin -->
        <div class="order-table-wrapper">
          <a-table
            ref="completedTable"
            size="middle"
            :scroll="{ x: 2000 }"
            bordered
            rowKey="id"
            :columns="columns"
            :dataSource="completedDataSource"
            :pagination="completedPagination"
            :loading="completedLoading"
            class="j-table-force-nowrap"
            @change="handleCompletedTableChange"
          >
            <template slot="companyName" slot-scope="text, record">
              <a-tag color="blue" @click="showCompanyInfo(record)">{{ record.companyName }}</a-tag>
            </template>
            <template slot="orderStatus" slot-scope="text, record">
              <a-tag :color="getOrderStatusColor(record.orderStatus)">已退单</a-tag>
            </template>
            <template slot="refundReason" slot-scope="text, record">
              <span>{{ getRefundReason(record) }}</span>
            </template>
            <template slot="operationLog" slot-scope="text, record">
              <a-button type="link" size="small" @click="showOperationLogModal(record)">
                <a-icon type="history" /> 查看记录
              </a-button>
            </template>
            <span slot="action" slot-scope="text, record">
              <a-tag color="green">已审核</a-tag>
            </span>
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>

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

    <!-- 退单审核弹窗 -->
    <a-modal
      :visible="auditRefundModal.visible"
      title="退单审核"
      :width="700"
      :confirmLoading="auditRefundModal.loading"
      @ok="confirmAuditRefund"
      @cancel="cancelAuditRefund"
      okText="确认审核"
      okType="danger"
      cancelText="取消"
    >
      <div>
        <p style="margin-bottom: 16px; font-size: 14px;">
          确定要审核通过退单订单 <span style="font-weight: 600; color: #1890ff;">{{ auditRefundModal.orderNo }}</span> 吗？
        </p>
        
        <a-alert
          v-if="auditRefundModal.refundReason && auditRefundModal.refundReason !== '-'"
          message="退单原因"
          type="warning"
          show-icon
          style="margin-bottom: 16px;"
        >
          <div slot="description" style="font-size: 14px; line-height: 1.8; color: #fa8c16; font-weight: 500; padding: 8px 0;">
            {{ auditRefundModal.refundReason }}
          </div>
        </a-alert>
        
        <a-alert
          message="审核说明"
          type="info"
          show-icon
          style="margin-bottom: 0;"
        >
          <div slot="description" style="font-size: 13px; line-height: 1.6;">
            审核通过后，订单将被删除，相关的支出、收入和银行日记账记录也将被删除并平账。
          </div>
        </a-alert>
      </div>
    </a-modal>

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
import OrderOperationLog from './modules/OrderOperationLog'
import CustomerDetailModal from '@/views/customer/modules/CustomerDetailModal'
import { filterDictTextByCache } from '@/components/dict/JDictSelectUtil'

export default {
  name: 'RefundOrderList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    OrderOperationLog,
    CustomerDetailModal,
  },
  data() {
    return {
      description: '退单订单管理页面',
      // 禁用 mixin 的自动查询，手动控制查询时机
      disableMixinCreated: true,
      activeTab: 'pending', // 当前标签页：pending-待审核，completed-已完成
      // 已完成数据
      completedDataSource: [],
      completedPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      completedLoading: false,
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
          title: '订单状态',
          align: 'center',
          dataIndex: 'orderStatus',
          width: 120,
          scopedSlots: { customRender: 'orderStatus' },
        },
        {
          title: '退单原因',
          align: 'left',
          dataIndex: 'remarks',
          width: 250,
          scopedSlots: { customRender: 'refundReason' },
        },
        {
          title: '操作记录',
          align: 'center',
          dataIndex: 'operationLog',
          width: 120,
          scopedSlots: { customRender: 'operationLog' },
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: 'center',
          fixed: 'right',
          width: 200,
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
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      operationLogModal: {
        title: '操作记录',
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
        orderNo: '',
      },
      companyModal: {
        title: '公司信息',
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        loading: false,
      },
      auditRefundModal: {
        visible: false,
        loading: false,
        orderId: '',
        orderNo: '',
        refundReason: '',
      },
    }
  },
  created() {
    // 设置查询参数：只查询退单状态的订单（orderStatus = '3'）
    this.queryParam.orderStatus = '3'
    // 待审核标签页：只查询未删除的订单（delFlag = '0'）
    this.queryParam.delFlag = '0'
    this.initColumns()
    this.initDictConfig()
    // 由于禁用了 mixin 的自动查询，需要手动调用 loadData()
    this.$nextTick(() => {
      this.loadData()
    })
  },
  methods: {
    onCreateDateChange(value, dateString) {
      this.queryParam.createTime_begin = dateString[0]
      this.queryParam.createTime_end = dateString[1]
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
          console.error('查询公司信息失败:', err)
          this.$message.error('查询公司信息失败')
        })
      } else if (companyId) {
        this.$refs.customerDetailModal.show({ id: companyId })
      } else {
        this.$message.warning('未找到公司ID')
      }
    },
    showOperationLogModal(record) {
      this.operationLogModal.orderId = record.id
      this.operationLogModal.orderNo = record.orderNo || '--'
      this.operationLogModal.title = '操作记录'
      this.operationLogModal.visible = true
    },
    getOrderStatusColor(orderStatus) {
      // 退单状态显示红色
      return '#ff4d4f'
    },
    getRefundReason(record) {
      // 从订单备注中提取退单原因
      if (record.remarks) {
        // 查找"退单原因："后面的内容
        const reasonMatch = record.remarks.match(/退单原因[：:]\s*(.+?)(?:\n|$)/)
        if (reasonMatch && reasonMatch[1]) {
          return reasonMatch[1].trim()
        }
        // 如果没有找到，返回整个备注（可能备注就是退单原因）
        return record.remarks
      }
      return '-'
    },
    // 退单审核
    handleAuditRefund(record) {
      // 从订单备注中提取退单原因
      let refundReason = this.getRefundReason(record)
      
      // 打开审核弹窗
      this.auditRefundModal.orderId = record.id
      this.auditRefundModal.orderNo = record.orderNo || '--'
      this.auditRefundModal.refundReason = refundReason
      this.auditRefundModal.loading = false
      this.auditRefundModal.visible = true
    },
    // 确认退单审核
    confirmAuditRefund() {
      const that = this
      this.auditRefundModal.loading = true
      httpAction('/order/auditRefund', {
        id: this.auditRefundModal.orderId
      }, 'POST').then((res) => {
        if (res.success) {
          that.$message.success('退单审核成功！订单及相关财务记录已删除')
          that.auditRefundModal.visible = false
          // 根据当前标签页刷新数据
          if (that.activeTab === 'pending') {
            that.loadData(1)
          } else if (that.activeTab === 'completed') {
            that.loadCompletedData()
          }
        } else {
          that.$message.error(res.message || '退单审核失败')
        }
      }).catch((err) => {
        console.error('退单审核失败:', err)
        that.$message.error('退单审核失败：' + (err.message || '未知错误'))
      }).finally(() => {
        that.auditRefundModal.loading = false
      })
    },
    // 取消退单审核
    cancelAuditRefund() {
      this.auditRefundModal.visible = false
      this.auditRefundModal.orderId = ''
      this.auditRefundModal.orderNo = ''
      this.auditRefundModal.refundReason = ''
      this.auditRefundModal.loading = false
    },
    initColumns() {
      this.columns = this.defColumns
    },
    initDictConfig() {
      // 初始化字典配置
    },
    // Tab切换
    handleTabChange(activeKey) {
      this.activeTab = activeKey
      if (activeKey === 'pending') {
        // 待审核：查询未删除的退单订单
        this.queryParam.delFlag = '0'
        this.loadData(1)
      } else if (activeKey === 'completed') {
        // 已完成：查询已删除的退单订单
        this.loadCompletedData()
      }
    },
    // 加载已完成数据
    loadCompletedData() {
      this.completedLoading = true
      const params = {
        pageNo: this.completedPagination.current,
        pageSize: this.completedPagination.pageSize,
        orderStatus: '3', // 退单状态
        delFlag: '1', // 已删除（已审核通过）
      }
      
      // 添加查询条件
      if (this.queryParam.orderNo) {
        params.orderNo = this.queryParam.orderNo
      }
      if (this.queryParam.companyName) {
        params.companyName = this.queryParam.companyName
      }
      if (this.queryParam.salesman) {
        params.salesman = this.queryParam.salesman
      }
      if (this.queryParam.businessType) {
        params.businessType = this.queryParam.businessType
      }
      if (this.queryParam.createTime_begin) {
        params.createTime_begin = this.queryParam.createTime_begin
      }
      if (this.queryParam.createTime_end) {
        params.createTime_end = this.queryParam.createTime_end
      }
      
      getAction(this.url.list, params).then((res) => {
        if (res.success && res.result) {
          this.completedDataSource = res.result.records || []
          this.completedPagination.total = res.result.total || 0
        } else {
          this.completedDataSource = []
          this.completedPagination.total = 0
        }
      }).catch(() => {
        this.completedDataSource = []
        this.completedPagination.total = 0
      }).finally(() => {
        this.completedLoading = false
      })
    },
    // 已完成表格分页变化
    handleCompletedTableChange(pagination) {
      this.completedPagination.current = pagination.current
      this.completedPagination.pageSize = pagination.pageSize
      this.loadCompletedData()
    },
    // 查询按钮
    searchQuery() {
      if (this.activeTab === 'pending') {
        this.loadData(1)
      } else if (this.activeTab === 'completed') {
        this.completedPagination.current = 1
        this.loadCompletedData()
      }
    },
    // 重置查询
    searchReset() {
      this.queryParam = {
        orderStatus: '3',
        delFlag: this.activeTab === 'pending' ? '0' : undefined,
        orderNo: '',
        companyName: '',
        salesman: '',
        businessType: '',
        createTime_begin: '',
        createTime_end: '',
        createDateRange: null,
      }
      if (this.activeTab === 'pending') {
        this.loadData(1)
      } else if (this.activeTab === 'completed') {
        this.completedPagination.current = 1
        this.loadCompletedData()
      }
    },
  }
}
</script>

<style scoped>
.order-table-wrapper {
  margin-top: 16px;
}
</style>

