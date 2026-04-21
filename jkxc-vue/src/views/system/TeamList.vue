<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :md="6" :sm="8">
            <a-form-item label="团队名称">
              <a-input placeholder="请输入团队名称" v-model="queryParam.teamName"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="团队编码">
              <a-input placeholder="请输入团队编码" v-model="queryParam.teamCode"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="状态">
              <a-select v-model="queryParam.status" placeholder="请选择状态" allowClear>
                <a-select-option value="1">启用</a-select-option>
                <a-select-option value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">添加团队</a-button>
      <a-button type="primary" icon="delete" @click="batchDel" :disabled="selectedRowKeys.length === 0">批量删除</a-button>
    </div>

    <!-- table区域-begin -->
    <div>
      <a-table
        ref="table"
        size="middle"
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
        <span slot="teamLeaderName" slot-scope="text, record">
          {{ record.teamLeaderName || record.teamLeader_dictText || '-' }}
        </span>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
            <a>删除</a>
          </a-popconfirm>
        </span>
      </a-table>
    </div>
    <!-- table区域-end -->

    <team-modal ref="modalForm" @ok="modalFormOk"></team-modal>
  </a-card>
</template>

<script>
  import TeamModal from './modules/TeamModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'

  export default {
    name: "TeamList",
    mixins: [JeecgListMixin],
    components: {
      TeamModal
    },
    data() {
      return {
        description: '所属团队管理页面',
        // 表头
        columns: [
          {
            title: '团队名称',
            align: "left",
            dataIndex: 'teamName',
            width: 150,
          },
          {
            title: '团队编码',
            align: "left",
            dataIndex: 'teamCode',
            width: 120,
          },
          {
            title: '团队负责人',
            align: "left",
            dataIndex: 'teamLeaderName',
            width: 120,
            scopedSlots: {customRender: 'teamLeaderName'},
          },
          {
            title: '团队描述',
            align: "left",
            dataIndex: 'description',
            ellipsis: true,
          },
          {
            title: '状态',
            align: "center",
            dataIndex: 'status',
            scopedSlots: {customRender: 'status'},
            width: 80,
          },
          {
            title: '排序号',
            align: "center",
            dataIndex: 'sortOrder',
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
          list: "/sys/team/list",
          delete: "/sys/team/delete",
          deleteBatch: "/sys/team/deleteBatch",
        },
      }
    },
    methods: {
      handleAdd() {
        this.$refs.modalForm.add();
        this.$refs.modalForm.title = "新增团队";
      },
      handleEdit(record) {
        this.$refs.modalForm.edit(record);
        this.$refs.modalForm.title = "编辑团队";
      },
      handleDelete(id) {
        this.handleDeleteById(id);
      },
      batchDel() {
        if (this.selectedRowKeys.length <= 0) {
          this.$message.warning('请选择一条记录！');
          return;
        } else {
          this.handleDeleteBatch();
        }
      },
    }
  }
</script>

<style scoped>
  @import '~@assets/less/common.less'
</style>

