<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="地址商户">
              <a-input placeholder="请输入地址商户" v-model="queryParam.supplierCompany" allowClear></a-input>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="联系人员">
              <a-input placeholder="请输入联系人员" v-model="queryParam.supplierContact" allowClear></a-input>
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
      <a-button @click="handleAdd" type="primary" icon="plus">新增地址商</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('地址商管理')">导出</a-button>
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
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="action" slot-scope="text, record">
        <a @click="handleEdit(record)">编辑</a>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
          <a>删除</a>
        </a-popconfirm>
      </span>
    </a-table>

    <!-- 地址商表单弹窗 -->
    <supplier-modal ref="modalForm" @ok="modalFormOk"></supplier-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import SupplierModal from './modules/SupplierModal'

export default {
  name: 'SupplierList',
  mixins: [JeecgListMixin],
  components: {
    SupplierModal,
  },
  data() {
    return {
      description: '地址商管理页面',
      queryParam: {
        supplierCompany: '',
        supplierContact: '',
      },
      url: {
        list: '/supplier/ghSupplier/list',
        delete: '/supplier/ghSupplier/delete',
        deleteBatch: '/supplier/ghSupplier/deleteBatch',
        exportXlsUrl: '/supplier/ghSupplier/exportXls',
        importExcelUrl: '/supplier/ghSupplier/importExcel',
      },
      columns: [
        {
          title: '地址商户',
          align: 'center',
          dataIndex: 'supplierCompany',
          width: 200,
        },
        {
          title: '联系人员',
          align: 'center',
          dataIndex: 'supplierContact',
          width: 150,
        },
        {
          title: '联系电话',
          align: 'center',
          dataIndex: 'supplierContactNumber',
          width: 150,
        },
        {
          title: '备注信息',
          align: 'center',
          dataIndex: 'remarks',
          width: 200,
          ellipsis: true,
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
    }
  },
  methods: {
    // 新增地址商
    handleAdd() {
      this.$refs.modalForm.add()
    },
    // 编辑地址商
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
    },
    // 表单提交成功回调
    modalFormOk() {
      this.loadData()
    },
  },
}
</script>

<style scoped>
@import '~@/assets/less/common.less';
</style>


