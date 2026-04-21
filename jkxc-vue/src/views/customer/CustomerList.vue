<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.corporateName" type="like"></JInput>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="联系人">
              <JInput placeholder="请输入联系人" v-model="queryParam.contacts" type="like"></JInput>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="所属区域">
              <a-input placeholder="请输入所属区域" v-model="queryParam.region" allowClear></a-input>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务员">
              <a-input placeholder="请输入业务员" v-model="queryParam.salesman" allowClear></a-input>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="状态">
              <j-dict-select-tag
                placeholder="请选择状态"
                v-model="queryParam.status"
                dictCode="status"
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
      <a-button @click="handleAdd" type="primary" icon="plus">添加客户</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('客户信息')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <a-button type="primary" icon="delete" @click="batchDel" :disabled="selectedRowKeys.length === 0">批量删除</a-button>
    </div>
    <!-- table区域-begin -->
    <div>
      <a-table
        ref="table"
        size="small"
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
        <span slot="corporateName" slot-scope="text, record">
          <a @click="handleCustomerDetail(record)" style="color: #1890ff; cursor: pointer;">{{ text }}</a>
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
        <span slot="totalSpending" slot-scope="text">
          <span v-if="text" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
          <span v-else>-</span>
        </span>
        <span slot="orderRecord" slot-scope="text, record">
          <a-tag 
            :color="getIsRecurringColor(record.orderCount)"
            style="cursor: pointer;"
            @click="handleViewOrders(record)"
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
        <template slot="status" slot-scope="text, record">
          <a-badge :status="record.status === '1' ? 'success' : 'default'" :text="record.status === '1' ? '启用' : '禁用'" />
        </template>
        <span slot="action" slot-scope="text, record">
          <a-dropdown>
            <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
              <a-menu-item key="addContract">
                <a-icon type="file-add" />新增合同
              </a-menu-item>
              <a-menu-item key="edit">
                <a-icon type="edit" />客户编辑
              </a-menu-item>
              <a-menu-item key="linkCompany">
                <a-icon type="link" />客户关联
              </a-menu-item>
            </a-menu>
            <a-button>
              操作 <a-icon type="down" />
            </a-button>
          </a-dropdown>
        </span>
      </a-table>
    </div>
    <customer-modal ref="modalForm" @ok="modalFormOk"></customer-modal>
    
    <!-- 客户详情弹窗 -->
    <customer-detail-modal ref="customerDetailModal"></customer-detail-modal>
    
    <!-- 客户关联弹窗 -->
    <customer-link-modal ref="customerLinkModal" @ok="handleLinkOk"></customer-link-modal>
    
    <!-- 新增订单弹窗（新增合同使用订单表单） -->
    <order-modal ref="orderModal" @ok="handleOrderOk"></order-modal>
    
    <!-- 客户订单记录弹窗 -->
    <customer-order-modal ref="customerOrderModal"></customer-order-modal>
    
    <!-- 企业等级详情弹窗 -->
    <enterprise-level-detail-modal ref="enterpriseLevelDetailModal"></enterprise-level-detail-modal>
  </a-card>
</template>

<script>
import { filterObj } from '@/utils/util'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { processQueryParams } from '@/utils/util'
import CustomerModal from './modules/CustomerModal'
import CustomerDetailModal from './modules/CustomerDetailModal'
import CustomerLinkModal from './modules/CustomerLinkModal'
import CustomerOrderModal from './modules/CustomerOrderModal'
import EnterpriseLevelDetailModal from './modules/EnterpriseLevelDetailModal'
import OrderModal from '@/views/order/modules/OrderModal'
import { queryCustomerList, deleteCustomer, deleteBatchCustomer, queryCustomerById } from '@/api/api'
import { postAction } from '@/api/manage'

export default {
  name: 'CustomerList',
  mixins: [JeecgListMixin],
  components: {
    CustomerModal,
    CustomerDetailModal,
    CustomerLinkModal,
    CustomerOrderModal,
    EnterpriseLevelDetailModal,
    OrderModal,
  },
  data() {
    return {
      description: '客户管理页面',
      selectedRowKeys: [],
      selectedRows: [],
      // 查询条件
      queryParam: {
        corporateName: '',
        contacts: '',
        region: '',
        status: '',
        salesman: '',  // 添加业务员筛选字段
      },
      // 标记是否需要延迟加载数据（用于处理路由参数）
      delayLoad: false,
      // 表头
      columns: [
        {
          title: '公司名称',
          align: 'left',
          dataIndex: 'corporateName',
          width: 200,
          scopedSlots: { customRender: 'corporateName' },
        },
        {
          title: '联系人',
          align: 'left',
          dataIndex: 'contacts',
          width: 120,
        },
        {
          title: '联系电话',
          align: 'left',
          dataIndex: 'contactInformation',
          width: 150,
        },
        {
          title: '所属区域',
          align: 'left',
          dataIndex: 'region',
          width: 150,
        },
        {
          title: '法人姓名',
          align: 'left',
          dataIndex: 'legalPersonName',
          width: 120,
        },
        {
          title: '法人电话',
          align: 'left',
          dataIndex: 'legalPersonPhone',
          width: 150,
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
          dataIndex: 'orderCount',
          width: 100,
          scopedSlots: { customRender: 'orderRecord' },
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
          title: '状态',
          align: 'center',
          dataIndex: 'status',
          scopedSlots: { customRender: 'status' },
          width: 80,
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: 'center',
          width: 200,
          fixed: 'right',
          scopedSlots: { customRender: 'action' },
        },
      ],
      url: {
        list: '/customer/ghCustomer/list',
        delete: '/customer/ghCustomer/delete',
        deleteBatch: '/customer/ghCustomer/deleteBatch',
        exportXlsUrl: '/customer/ghCustomer/exportXls',
        importExcelUrl: 'customer/ghCustomer/importExcel',
      },
      dictOptions: {},
      superFieldList: [],
    }
  },
  computed: {
    importExcelUrl: function() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    },
  },
  beforeCreate() {
    const query = this.$route.query
    if (query && (query.salesman || query._fromDashboard)) {
      this._hasSalesmanParam = true
    }
  },
  created() {
    if (this.$route.query.salesman) {
      this.queryParam.salesman = this.$route.query.salesman
      if (this._hasSalesmanParam) {
        this.$nextTick(() => {
          this.loadData(1)
        })
      }
    } 
    else if (this.$route.query._fromDashboard) {
      const userInfo = this.$store.getters.userInfo
      if (userInfo && userInfo.realname) {
        this.queryParam.salesman = userInfo.realname
        if (this._hasSalesmanParam) {
          this.$nextTick(() => {
            this.loadData(1)
          })
        }
      }
    }
  },
  watch: {
    '$route.query': {
      handler(newQuery, oldQuery) {
        if (newQuery.salesman !== oldQuery.salesman) {
          if (newQuery.salesman) {
            this.queryParam.salesman = newQuery.salesman
          } else if (newQuery._fromDashboard) {
            const userInfo = this.$store.getters.userInfo
            if (userInfo && userInfo.realname) {
              this.queryParam.salesman = userInfo.realname
            }
          } else {
            this.queryParam.salesman = ''
          }
          this.loadData(1)
        }
      },
      deep: true
    }
  },
  methods: {
    getQueryParams() {
      var param = Object.assign({}, this.queryParam)
      param.field = this.getQueryField()
      param.pageNo = this.ipagination.current
      param.pageSize = this.ipagination.pageSize
      const processed = processQueryParams(param)
      const filtered = filterObj(processed)
      return filtered
    },
    handleAdd: function() {
      this.$refs.modalForm.add()
      this.$refs.modalForm.title = '添加客户'
    },
    handleEdit: function(record) {
      this.$refs.modalForm.edit(record)
      this.$refs.modalForm.title = '编辑客户'
    },
    handleDetail: function(record) {
      this.$refs.modalForm.edit(record)
      this.$refs.modalForm.title = '客户详情'
      this.$refs.modalForm.disableSubmit = true
    },
    handleDelete: function(id) {
      if (!id) {
        this.$message.warning('请选择一条记录!')
        return
      }
      var that = this
      deleteCustomer({ id: id }).then((res) => {
        if (res.success) {
          that.$message.success(res.message)
          that.loadData()
        } else {
          that.$message.warning(res.message)
        }
      })
    },
    batchDel: function() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.warning('请选择一条记录!')
        return
      } else {
        var ids = ''
        for (var a = 0; a < this.selectedRowKeys.length; a++) {
          ids += this.selectedRowKeys[a] + ','
        }
        var that = this
        deleteBatchCustomer({ ids: ids }).then((res) => {
          if (res.success) {
            that.$message.success(res.message)
            that.loadData()
            that.selectedRowKeys = []
            that.selectedRows = []
          } else {
            that.$message.warning(res.message)
          }
        })
      }
    },
    handleCustomerDetail(record) {
      this.$refs.customerDetailModal.show(record)
    },
    // 点击企业等级
    handleEnterpriseLevelClick(record) {
      this.$refs.enterpriseLevelDetailModal.show(record)
    },
    // 操作菜单点击事件
    handleActionMenuClick({ key }, record) {
      switch (key) {
        case 'addContract':
          this.handleAddContract(record)
          break
        case 'edit':
          this.handleEdit(record)
          break
        case 'linkCompany':
          this.handleLinkCompany(record)
          break
      }
    },
    // 新增合同（使用订单表单）
    handleAddContract(record) {
      // 打开订单表单弹窗，自动填充客户信息
      // 创建一个包含客户信息的订单对象
      const orderData = {
        companyName: record.corporateName, // 订单表单使用companyName字段
        companyId: record.id, // 客户ID
        contacts: record.contacts || '',
        contactInformation: record.contactInformation || '',
        address: record.address || '',
        region: record.region || '',
      }
      // 打开订单表单并传递客户信息
      this.$refs.orderModal.add(orderData)
    },
    // 订单保存成功回调
    handleOrderOk() {
      this.$message.success('订单创建成功！')
      // 刷新客户列表，更新消费金额等信息
      this.loadData()
    },
    // 客户关联
    handleLinkCompany(record) {
      this.$refs.customerLinkModal.show(record)
    },
    // 关联成功回调
    handleLinkOk() {
      this.loadData()
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    // 查看订单记录
    handleViewOrders(record) {
      this.$refs.customerOrderModal.show(record)
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
      const orderCount = record.orderCount
      if (orderCount != null && orderCount > 0) {
        return `${orderCount}单`
      }
      return '-'
    },
  },
}
</script>

<style scoped>
@import '~@assets/less/common.less';

/* 设置表格行高更小 */
::v-deep .ant-table-small .ant-table-tbody > tr > td {
  padding: 8px 16px;
}

::v-deep .ant-table-small .ant-table-thead > tr > th {
  padding: 8px 16px;
}

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
</style>

