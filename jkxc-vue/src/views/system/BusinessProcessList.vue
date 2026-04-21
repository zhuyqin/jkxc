<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="12">
          <a-col :md="7" :sm="8">
            <a-form-item label="流程名称" :labelCol="{span: 6}" :wrapperCol="{span: 14, offset: 1}">
              <a-input placeholder="请输入流程名称" v-model="queryParam.processName"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="7" :sm="8">
            <a-form-item label="流程编码" :labelCol="{span: 6}" :wrapperCol="{span: 14, offset: 1}">
              <a-input placeholder="请输入流程编码" v-model="queryParam.processCode"></a-input>
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
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        @change="handleTableChange"
      >
        <span slot="status" slot-scope="text">
          <a-badge :status="text == 1 ? 'success' : 'default'" :text="text == 1 ? '启用' : '禁用'" />
        </span>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)" style="margin-right: 8px">
            <a-icon type="edit"/>
            编辑
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
    <audit-process-modal ref="modalForm" @ok="modalFormOk"></audit-process-modal>
  </a-card>
</template>

<script>
  import { filterObj } from '@/utils/util';
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import AuditProcessModal from './modules/AuditProcessModal'
  import { queryAuditProcessList, deleteAuditProcess, deleteBatchAuditProcess, queryAuditProcessById } from '@/api/api'

  export default {
    name: "BusinessProcessList",
    mixins:[JeecgListMixin],
    components: {AuditProcessModal},
    data() {
      return {
        description: '工商流程管理页面',
        selectedRowKeys: [],
        // 查询条件
        queryParam: {
          processName: "",
          processCode: "",
        },
        // 表头
        columns: [
          {
            title: '流程名称',
            align: "left",
            dataIndex: 'processName',
            width: 150,
          },
          {
            title: '流程编码',
            align: "left",
            dataIndex: 'processCode',
            width: 150,
          },
          {
            title: '审核步骤',
            align: "left",
            dataIndex: 'steps',
            scopedSlots: {customRender: 'steps'},
            width: 300,
          },
          {
            title: '状态',
            align: "center",
            dataIndex: 'status',
            scopedSlots: {customRender: 'status'},
            width: 80,
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            width: 150,
            scopedSlots: {customRender: 'action'},
          }
        ],
        url: {
          list: "/sys/auditProcess/list",
          delete: "/sys/auditProcess/delete",
          deleteBatch: "/sys/auditProcess/deleteBatch",
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
        this.$refs.modalForm.title = "新增工商流程";
      },
      handleEdit(record) {
        this.$refs.modalForm.edit(record);
        this.$refs.modalForm.title = "编辑工商流程";
      },
      handleDelete(id) {
        deleteAuditProcess({id: id}).then((res) => {
          if (res.success) {
            this.$message.success("删除成功");
            this.loadData();
          } else {
            this.$message.error(res.message);
          }
        });
      },
      batchDel() {
        if (this.selectedRowKeys.length <= 0) {
          this.$message.warning('请选择一条记录！');
          return;
        } else {
          var ids = "";
          for (var a = 0; a < this.selectedRowKeys.length; a++) {
            ids += this.selectedRowKeys[a] + ",";
          }
          var that = this;
          this.$confirm({
            title: "确认删除",
            content: "是否删除选中数据?",
            onOk: function () {
              deleteBatchAuditProcess({ids: ids}).then((res) => {
                if (res.success) {
                  that.$message.success("删除成功");
                  that.loadData();
                  that.onClearSelected();
                } else {
                  that.$message.error(res.message);
                }
              });
            }
          });
        }
      },
    }
  }
</script>

<style scoped>
  @import '~@assets/less/common.less'
</style>

