<template>
  <div>
    <a-card :bordered="false">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="16" style="width: 100%;">
          <a-col :md="8" :sm="12">
            <a-form-item label="流程名称">
              <a-input 
                placeholder="请输入流程名称" 
                v-model="queryParam.processName"
                allow-clear
                prefix-icon="search"
              >
                <a-icon slot="prefix" type="search" />
              </a-input>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="12">
            <a-form-item label="流程编码">
              <a-input 
                placeholder="请输入流程编码" 
                v-model="queryParam.processCode"
                allow-clear
              >
                <a-icon slot="prefix" type="code" />
              </a-input>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="12">
            <a-form-item>
              <a-button type="primary" @click="searchQuery" icon="search" :loading="loading">
                查询
              </a-button>
              <a-button @click="searchReset" icon="reload" style="margin-left: 8px">
                重置
              </a-button>
            </a-form-item>
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
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <span slot="processName" slot-scope="text, record">
          {{ text }}
          <a-tag v-if="record.bindings && record.bindings.length > 0" color="cyan" size="small" style="margin-left: 8px;">
            {{ record.bindings.length }}个业务类型
          </a-tag>
        </span>
        <span slot="status" slot-scope="text">
          <a-badge 
            :status="text === 1 ? 'success' : 'default'" 
            :text="text === 1 ? '启用' : '禁用'"
            style="font-weight: 500;"
          />
        </span>
        <span slot="steps" slot-scope="text, record">
          <template v-if="record.steps && record.steps.length > 0">
            <div v-for="(stepGroup, stepIndex) in getGroupedSteps(record.steps)" :key="stepIndex" style="margin-bottom: 4px;">
              <a-tag color="blue" style="margin-right: 4px;">
                {{ stepGroup.stepOrder }}. 
                <span v-for="(roleName, roleIndex) in stepGroup.roleNames" :key="roleIndex">
                  {{ roleName }}<span v-if="roleIndex < stepGroup.roleNames.length - 1">、</span>
                </span>
              </a-tag>
            </div>
          </template>
          <span v-else style="color: #999;">暂无步骤</span>
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
    </a-card>
    <AuditProcessModal ref="modalForm" @ok="modalFormOk"></AuditProcessModal>
  </div>
</template>

<script>
  import { filterObj } from '@/utils/util';
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import AuditProcessModal from './modules/AuditProcessModal'
  import { queryAuditProcessList, deleteAuditProcess, deleteBatchAuditProcess, queryAuditProcessById } from '@/api/api'

  export default {
    name: "AuditProcessList",
    mixins:[JeecgListMixin],
    components: {AuditProcessModal},
    data() {
      return {
        description: '审核流程管理页面',
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
            width: 200,
            scopedSlots: {customRender: 'processName'},
            fixed: 'left',
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
            width: 400,
          },
          {
            title: '状态',
            align: "center",
            dataIndex: 'status',
            scopedSlots: {customRender: 'status'},
            width: 100,
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            width: 150,
            scopedSlots: {customRender: 'action'},
            fixed: 'right',
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
        var param = Object.assign({}, this.queryParam, this.isorter);
        param.field = this.getQueryField();
        param.pageNo = this.ipagination.current;
        param.pageSize = this.ipagination.pageSize;
        if (this.superQueryParams) {
          param['superQueryParams'] = encodeURI(this.superQueryParams)
          param['superQueryMatchType'] = this.superQueryMatchType
        }
        return filterObj(param);
      },
      // 重置搜索框的内容
      searchReset() {
        var that = this;
        that.queryParam.processName = "";
        that.queryParam.processCode = "";
        that.loadData(this.ipagination.current);
      },
      onSelectChange(selectedRowKeys) {
        this.selectedRowKeys = selectedRowKeys;
      },
      batchDel() {
        if (this.selectedRowKeys.length === 0) {
          this.$message.warning('请选择要删除的数据！');
          return;
        }
        this.$confirm({
          title: "确认删除",
          content: "是否删除选中数据?",
          onOk: () => {
            deleteBatchAuditProcess({ids: this.selectedRowKeys.join(',')}).then((res) => {
              if (res.success) {
                this.$message.success(res.message || "删除成功!");
                this.selectedRowKeys = [];
                this.loadData();
              } else {
                this.$message.warning(res.message);
              }
            });
          }
        });
      },
      getGroupedSteps(steps) {
        // 按stepOrder分组，相同stepOrder的步骤合并
        const grouped = {}
        steps.forEach(step => {
          if (!grouped[step.stepOrder]) {
            grouped[step.stepOrder] = {
              stepOrder: step.stepOrder,
              roleNames: []
            }
          }
          if (step.roleName) {
            grouped[step.stepOrder].roleNames.push(step.roleName)
          }
        })
        return Object.values(grouped).sort((a, b) => a.stepOrder - b.stepOrder)
      },
      handleEdit(record) {
        // 加载完整数据（包含步骤）
        queryAuditProcessById({id: record.id}).then((res) => {
          if (res.success && res.result) {
            // 将步骤按stepOrder分组，合并相同stepOrder的角色
            const groupedSteps = this.groupStepsByOrder(res.result.steps || [])
            res.result.steps = groupedSteps
            this.$refs.modalForm.edit(res.result);
          } else {
            this.$message.warning('加载数据失败');
          }
        });
      },
      groupStepsByOrder(steps) {
        // 按stepOrder分组
        const grouped = {}
        steps.forEach(step => {
          if (!grouped[step.stepOrder]) {
            grouped[step.stepOrder] = {
              id: step.id || step.stepId || null, // 保留步骤ID（取第一个步骤的ID，相同stepOrder的步骤应该有相同的表单配置）
              stepOrder: step.stepOrder,
              roleIds: [],
              roleNames: [],
              description: step.description || ''
            }
          }
          if (step.roleId) {
            grouped[step.stepOrder].roleIds.push(step.roleId)
          }
          if (step.roleName) {
            grouped[step.stepOrder].roleNames.push(step.roleName)
          }
          // 如果描述不同，取第一个
          if (!grouped[step.stepOrder].description && step.description) {
            grouped[step.stepOrder].description = step.description
          }
        })
        return Object.values(grouped).sort((a, b) => a.stepOrder - b.stepOrder)
      },
      modalFormOk() {
        this.loadData(1)
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
  
  .table-operator {
    margin-top: 16px;
    padding-top: 16px;
  }
  
  .table-operator .ant-btn {
    margin-right: 8px;
  }
  
  .table-operator .ant-btn:last-child {
    margin-right: 0;
  }
</style>

