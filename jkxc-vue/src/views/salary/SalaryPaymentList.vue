<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="员工姓名">
              <j-search-select-tag
                placeholder="请选择员工"
                v-model="queryParam.userId"
                dict="sys_user,realname,id,del_flag=0 and status=1"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="发放月份">
              <a-month-picker
                placeholder="请选择发放月份"
                v-model="queryParam.paymentMonth"
                format="YYYY-MM"
                style="width: 100%"
                @change="handleMonthChange"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="发放状态">
              <j-dict-select-tag
                placeholder="请选择发放状态"
                v-model="queryParam.paymentStatus"
                dictCode="payment_status"
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
    <div class="table-operator" style="margin-top: 16px;">
      <a-button @click="handleBatchCreate" type="primary" icon="plus">批量创建</a-button>
      <a-button @click="handleAdd" type="primary" icon="plus" style="margin-left: 8px">新增发放记录</a-button>
      <a-button @click="handleBatchPay" type="primary" icon="pay-circle" style="margin-left: 8px" :disabled="selectedRowKeys.length === 0">批量发放</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls" style="margin-left: 8px">导出</a-button>
    </div>

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
      :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="paymentMonth" slot-scope="text">
        {{ text || '-' }}
      </span>
      <span slot="salaryAmount" slot-scope="text">
        <span v-if="text" style="color: #1890ff; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="actualAmount" slot-scope="text">
        <span v-if="text" style="color: #52c41a; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="paymentStatus" slot-scope="text, record">
        <a-badge v-if="text === 'paid'" status="success" text="已发放" />
        <a-badge v-else-if="text === 'pending'" status="processing" text="待发放" />
        <a-badge v-else-if="text === 'cancelled'" status="default" text="已取消" />
        <span v-else>{{ text || '-' }}</span>
      </span>
      <span slot="paymentTime" slot-scope="text">
        {{ text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : '-' }}
      </span>
      <span slot="action" slot-scope="text, record">
        <a v-if="record.paymentStatus !== 'paid'" @click="handlePay(record)">发放</a>
        <template v-if="record.paymentStatus !== 'paid'">
          <a-divider type="vertical" />
        </template>
        <a @click="handleEdit(record)">编辑</a>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)" :disabled="record.paymentStatus === 'paid'">
          <a :disabled="record.paymentStatus === 'paid'">删除</a>
        </a-popconfirm>
      </span>
    </a-table>

    <!-- 工资发放表单弹窗 -->
    <salary-payment-modal ref="modalForm" @ok="modalFormOk"></salary-payment-modal>
    
    <!-- 批量创建弹窗 -->
    <salary-batch-create-modal ref="batchCreateModal" @ok="modalFormOk"></salary-batch-create-modal>
    
    <!-- 批量发放弹窗 -->
    <salary-batch-pay-modal ref="batchPayModal" :selectedIds="selectedRowKeys" @ok="handleBatchPayOk"></salary-batch-pay-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import SalaryPaymentModal from './modules/SalaryPaymentModal'
import SalaryBatchCreateModal from './modules/SalaryBatchCreateModal'
import SalaryBatchPayModal from './modules/SalaryBatchPayModal'
import moment from 'moment'

export default {
  name: 'SalaryPaymentList',
  mixins: [JeecgListMixin],
  components: {
    SalaryPaymentModal,
    SalaryBatchCreateModal,
    SalaryBatchPayModal,
  },
  data() {
    return {
      description: '工资发放管理页面',
      selectedRowKeys: [],
      queryParam: {
        userId: '',
        paymentMonth: null,
        paymentStatus: '',
      },
      url: {
        list: '/salary/ghSalaryPayment/list',
        delete: '/salary/ghSalaryPayment/delete',
        deleteBatch: '/salary/ghSalaryPayment/deleteBatch',
        exportXlsUrl: '/salary/ghSalaryPayment/exportXls',
        importExcelUrl: '/salary/ghSalaryPayment/importExcel',
      },
      columns: [
        {
          title: '员工姓名',
          align: 'center',
          dataIndex: 'userId',
          width: 120,
          fixed: 'left',
          customRender: (text, record) => {
            return record.userName || record.userId_dictText || text || '-'
          },
        },
        {
          title: '发放月份',
          align: 'center',
          dataIndex: 'paymentMonth',
          width: 100,
          fixed: 'left',
          scopedSlots: { customRender: 'paymentMonth' },
        },
        {
          title: '当前职位',
          align: 'center',
          dataIndex: 'currentPosition',
          width: 120,
          customRender: (text, record) => {
            return record.currentPosition || '-'
          },
        },
        {
          title: '当前星级',
          align: 'center',
          dataIndex: 'currentStarLevel',
          width: 100,
          customRender: (text, record) => {
            return record.currentStarLevel_dictText || text || '-'
          },
        },
        {
          title: '基本工资',
          align: 'center',
          dataIndex: 'baseSalary',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '绩效工资',
          align: 'center',
          dataIndex: 'performanceSalary',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '公积金补贴',
          align: 'center',
          dataIndex: 'housingFundSubsidy',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '其他补贴',
          align: 'center',
          dataIndex: 'otherSubsidy',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '高温补贴',
          align: 'center',
          dataIndex: 'highTemperatureSubsidy',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '全勤奖金',
          align: 'center',
          dataIndex: 'fullAttendanceBonus',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '合计金额',
          align: 'center',
          dataIndex: 'totalAmount',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '公司社保',
          align: 'center',
          dataIndex: 'companySocialSecurity',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '个人社保',
          align: 'center',
          dataIndex: 'personalSocialSecurity',
          width: 100,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '扣除总额',
          align: 'center',
          dataIndex: 'deductionAmount',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '实发金额',
          align: 'center',
          dataIndex: 'actualAmount',
          width: 120,
          scopedSlots: { customRender: 'actualAmount' },
        },
        {
          title: '发放状态',
          align: 'center',
          dataIndex: 'paymentStatus',
          width: 100,
          scopedSlots: { customRender: 'paymentStatus' },
        },
        {
          title: '发放时间',
          align: 'center',
          dataIndex: 'paymentTime',
          width: 160,
          scopedSlots: { customRender: 'paymentTime' },
        },
        {
          title: '操作',
          align: 'center',
          dataIndex: 'action',
          fixed: 'right',
          width: 180,
          scopedSlots: { customRender: 'action' },
        },
      ],
    }
  },
  methods: {
    moment,
    // 新增发放记录
    handleAdd() {
      this.$refs.modalForm.add()
    },
    // 编辑发放记录
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
    },
    // 批量创建
    handleBatchCreate() {
      this.$refs.batchCreateModal.show()
    },
    // 单个发放
    handlePay(record) {
      this.$refs.modalForm.pay(record)
    },
    // 批量发放
    handleBatchPay() {
      if (this.selectedRowKeys.length === 0) {
        this.$message.warning('请选择要发放的记录！')
        return
      }
      this.$refs.batchPayModal.show()
    },
    // 批量发放成功回调
    handleBatchPayOk() {
      this.selectedRowKeys = []
      this.loadData()
    },
    // 表单提交成功回调
    modalFormOk() {
      this.selectedRowKeys = []
      this.loadData()
    },
    // 月份选择变化
    handleMonthChange(date, dateString) {
      this.queryParam.paymentMonth = dateString || null
    },
    // 表格选择变化
    onSelectChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
  },
}
</script>

<style scoped>
@import '~@/assets/less/common.less';

/* 确保表格文字可见 */
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
</style>

