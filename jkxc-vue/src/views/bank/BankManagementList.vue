<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="收款单位/人">
              <JInput placeholder="请输入收款单位/人" v-model="queryParam.payeePerson" type="like"></JInput>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="收款账户">
              <JInput placeholder="请输入收款账户" v-model="queryParam.collectionAccount" type="like"></JInput>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="收款方式">
              <j-dict-select-tag
                placeholder="请选择收款方式"
                v-model="queryParam.paymentMethod"
                dictCode="payment_method"
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
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('收款账号管理')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <a-button type="primary" icon="delete" @click="batchDel" :disabled="selectedRowKeys.length === 0">批量删除</a-button>
    </div>
    <!-- table区域-begin -->
    <div>
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
        <template slot="hidden" slot-scope="text, record">
          <a-badge :status="record.hidden === '0' ? 'success' : 'default'" :text="record.hidden === '0' ? '显示' : '隐藏'" />
        </template>
        <template slot="imgSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无图片</span>
          <img v-else :src="getImgView(text)" @click="handleImg(text)" height="25px" alt="" style="max-width:80px;font-size: 12px;font-style: italic;cursor: pointer;"/>
        </template>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
            <a>删除</a>
          </a-popconfirm>
        </span>
      </a-table>
    </div>
    <j-modal
      :visible.sync="modalImg.visible"
      :width="500"
      title="图片预览"
      :fullscreen.sync="modalImg.fullscreen"
      :switchFullscreen="modalImg.switchFullscreen"
    >
      <img alt="example" style="width: 100%" :src="modalImg.text" />
    </j-modal>
    <gh-bank-management-modal ref="modalForm" @ok="modalFormOk"></gh-bank-management-modal>
  </a-card>
</template>

<script>
import { filterObj } from '@/utils/util'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import GhBankManagementModal from './modules/GhBankManagementModal'
import { getAction } from '@/api/manage'
import { initDictOptions, filterDictText } from '@/components/dict/JDictSelectUtil'

export default {
  name: 'BankManagementList',
  mixins: [JeecgListMixin],
  components: {
    GhBankManagementModal,
  },
  data() {
    return {
      description: '收款账号管理页面',
      selectedRowKeys: [],
      selectedRows: [],
      paymentMethodDictOptions: [], // 收款方式字典选项
      // 查询条件
      queryParam: {
        payeePerson: '',
        collectionAccount: '',
        paymentMethod: '',
      },
      // 表头
      columns: [
        {
          title: '收款单位/人',
          align: 'center',
          dataIndex: 'payeePerson',
        },
        {
          title: '收款方式',
          align: 'center',
          dataIndex: 'paymentMethod',
          customRender: (text) => {
            return filterDictText(this.paymentMethodDictOptions, text)
          },
        },
        {
          title: '收款账户',
          align: 'center',
          dataIndex: 'collectionAccount',
        },
        {
          title: '网点名称',
          align: 'center',
          dataIndex: 'accountNotes',
        },
        {
          title: '收款备注',
          align: 'center',
          dataIndex: 'collectionRemarks',
        },
        {
          title: '收款码',
          align: 'center',
          dataIndex: 'pic',
          scopedSlots: { customRender: 'imgSlot' },
        },
        {
          title: '是否隐藏',
          align: 'center',
          dataIndex: 'hidden',
          scopedSlots: { customRender: 'hidden' },
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: 'center',
          fixed: 'right',
          width: 147,
          scopedSlots: { customRender: 'action' },
        },
      ],
      url: {
        list: '/bank/ghBankManagement/list',
        delete: '/bank/ghBankManagement/delete',
        deleteBatch: '/bank/ghBankManagement/deleteBatch',
        exportXlsUrl: '/bank/ghBankManagement/exportXls',
        importExcelUrl: 'bank/ghBankManagement/importExcel',
      },
      modalImg: {
        visible: false,
        text: '',
        fullscreen: false,
        switchFullscreen: false,
      },
    }
  },
  computed: {
    importExcelUrl: function(){
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
    },
  },
  created() {
    // 初始化字典配置
    this.initDictConfig()
  },
  methods: {
    initDictConfig() {
      // 初始化收款方式字典
      initDictOptions('payment_method').then((res) => {
        if (res.success) {
          this.paymentMethodDictOptions = res.result
        }
      })
    },
    handleImg(text){
      this.modalImg.visible = true
      this.modalImg.text = text
    },
  },
}
</script>

<style scoped>
@import '~@assets/less/common.less';
</style>

