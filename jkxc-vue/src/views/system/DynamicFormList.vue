<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="12">
          <a-col :md="7" :sm="8">
            <a-form-item label="表单名称" :labelCol="{span: 6}" :wrapperCol="{span: 14, offset: 1}">
              <a-input placeholder="请输入表单名称" v-model="queryParam.formName"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="7" :sm="8">
            <a-form-item label="表单编码" :labelCol="{span: 6}" :wrapperCol="{span: 14, offset: 1}">
              <a-input placeholder="请输入表单编码" v-model="queryParam.formCode"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="7" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>

      <div class="table-operator" style="border-top: 5px; margin-top: 16px;">
        <a-button @click="handleAdd" type="primary" icon="plus" style="margin-right: 8px">添加</a-button>
        <a-button type="primary" icon="delete" @click="batchDel" :disabled="selectedRowKeys.length === 0">批量删除</a-button>
      </div>

      <a-table
        ref="table"
        rowKey="id"
        size="middle"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        @change="handleTableChange">
        <span slot="status" slot-scope="text">
          <a-badge :status="text === 1 ? 'success' : 'default'" :text="text === 1 ? '启用' : '禁用'" />
        </span>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)" style="margin-right: 8px">
            <a-icon type="edit"/>
            编辑
          </a>
          <a @click="handleCopy(record)" style="margin-right: 8px">
            <a-icon type="copy"/>
            复制
          </a>
          <a @click="handleDesign(record)" style="margin-right: 8px">
            <a-icon type="form"/>
            设计
          </a>
          <a @click="handleVersion(record)" style="margin-right: 8px">
            <a-icon type="history"/>
            版本
          </a>
          <a @click="handleBind(record)" style="margin-right: 8px">
            <a-icon type="link"/>
            绑定
          </a>
          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
            <a>
              <a-icon type="delete"/>
              删除
            </a>
          </a-popconfirm>
        </span>
      </a-table>
    </div>
    <dynamic-form-modal ref="modalForm" @ok="modalFormOk"></dynamic-form-modal>
    <dynamic-form-designer ref="designerModal" @ok="designerOk"></dynamic-form-designer>
    <dynamic-form-version ref="versionModal" @ok="versionOk"></dynamic-form-version>
    <dynamic-form-binding ref="bindingModal" @ok="bindingOk"></dynamic-form-binding>
    
    <!-- 复制表单弹窗 -->
    <a-modal
      title="复制表单"
      :visible="copyModalVisible"
      @ok="handleCopyConfirm"
      @cancel="handleCopyCancel"
      okText="确定"
      cancelText="取消"
    >
      <a-form-model :model="copyFormData" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="新表单名称" required>
          <a-input v-model="copyFormData.newFormName" placeholder="请输入新表单名称"></a-input>
        </a-form-model-item>
        <a-form-model-item label="新表单编码" required>
          <a-input v-model="copyFormData.newFormCode" placeholder="请输入新表单编码（唯一标识）"></a-input>
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </a-card>
</template>

<script>
  import { filterObj } from '@/utils/util';
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import DynamicFormModal from './modules/DynamicFormModal'
  import DynamicFormDesigner from './modules/DynamicFormDesigner'
  import DynamicFormVersion from './modules/DynamicFormVersion'
  import DynamicFormBinding from './modules/DynamicFormBinding'
  import { queryDynamicFormList, deleteDynamicForm, deleteBatchDynamicForm, copyDynamicForm } from '@/api/api'

  export default {
    name: "DynamicFormList",
    mixins:[JeecgListMixin],
    components: {
      DynamicFormModal,
      DynamicFormDesigner,
      DynamicFormVersion,
      DynamicFormBinding
    },
    data() {
      return {
        description: '动态表单管理页面',
        selectedRowKeys: [],
        copyModalVisible: false,
        copyFormData: {
          formId: '',
          newFormName: '',
          newFormCode: '',
        },
        // 查询条件
        queryParam: {
          formName: "",
          formCode: "",
        },
        // 表头
        columns: [
          {
            title: '表单名称',
            align: "left",
            dataIndex: 'formName',
            width: 150,
          },
          {
            title: '表单编码',
            align: "left",
            dataIndex: 'formCode',
            width: 150,
          },
          {
            title: '描述',
            align: "left",
            dataIndex: 'description',
            width: 200,
          },
          {
            title: '当前版本',
            align: "center",
            dataIndex: 'currentVersion',
            width: 100,
          },
          {
            title: '状态',
            align: "center",
            dataIndex: 'status',
            width: 100,
            scopedSlots: { customRender: 'status' },
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            width: 350,
            scopedSlots: { customRender: 'action' },
          },
        ],
        url: {
          list: "/dynamicform/form/list",
          delete: "/dynamicform/form/delete",
          deleteBatch: "/dynamicform/form/deleteBatch",
        },
      }
    },
    methods: {
      getQueryParams() {
        var param = Object.assign({}, this.queryParam);
        return filterObj(param);
      },
      handleAdd() {
        this.$refs.modalForm.add();
      },
      handleEdit(record) {
        this.$refs.modalForm.edit(record);
      },
      handleDesign(record) {
        this.$refs.designerModal.design(record);
      },
      handleVersion(record) {
        this.$refs.versionModal.show(record);
      },
      handleBind(record) {
        this.$refs.bindingModal.show(record);
      },
      handleCopy(record) {
        this.copyFormData = {
          formId: record.id,
          newFormName: record.formName + '_副本',
          newFormCode: record.formCode + '_copy',
        };
        this.copyModalVisible = true;
      },
      handleCopyConfirm() {
        if (!this.copyFormData.newFormName || !this.copyFormData.newFormCode) {
          this.$message.warning('请输入表单名称和编码');
          return;
        }
        copyDynamicForm({
          formId: this.copyFormData.formId,
          newFormName: this.copyFormData.newFormName,
          newFormCode: this.copyFormData.newFormCode
        }).then((res) => {
          if (res.success) {
            this.$message.success('复制成功！');
            this.handleCopyCancel();
            this.loadData();
          } else {
            this.$message.warning(res.message);
          }
        }).catch((err) => {
          this.$message.error('复制失败：' + (err.message || '未知错误'));
        });
      },
      handleCopyCancel() {
        this.copyModalVisible = false;
        this.copyFormData = {
          formId: '',
          newFormName: '',
          newFormCode: '',
        };
      },
      handleDelete(id) {
        deleteDynamicForm({ id: id }).then((res) => {
          if (res.success) {
            this.$message.success(res.message);
            this.loadData();
          } else {
            this.$message.warning(res.message);
          }
        });
      },
      batchDel() {
        if (this.selectedRowKeys.length === 0) {
          this.$message.warning('请选择要删除的记录');
          return;
        }
        deleteBatchDynamicForm({ ids: this.selectedRowKeys.join(',') }).then((res) => {
          if (res.success) {
            this.$message.success(res.message);
            this.selectedRowKeys = [];
            this.loadData();
          } else {
            this.$message.warning(res.message);
          }
        });
      },
      modalFormOk() {
        this.loadData();
      },
      designerOk() {
        this.loadData();
      },
      versionOk() {
        this.loadData();
      },
      bindingOk() {
        this.loadData();
      },
    },
  }
</script>

