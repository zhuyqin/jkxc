<template>
  <j-modal
    :title="'版本管理 - ' + formName"
    :width="1000"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @cancel="handleCancel"
    :footer="null"
    cancelText="关闭"
  >
    <a-table
      :columns="columns"
      :dataSource="versions"
      :loading="loading"
      :pagination="false"
      rowKey="id"
    >
      <span slot="isCurrent" slot-scope="text">
        <a-badge :status="text === 1 ? 'success' : 'default'" :text="text === 1 ? '当前版本' : '历史版本'" />
      </span>
      <span slot="action" slot-scope="text, record">
        <a @click="viewVersion(record)" style="margin-right: 8px">
          <a-icon type="eye" /> 查看
        </a>
      </span>
    </a-table>
    
    <j-modal
      title="版本详情"
      :width="800"
      :visible="versionDetailVisible"
      @cancel="versionDetailVisible = false"
      :footer="null"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="版本号">{{ currentVersion.version }}</a-descriptions-item>
        <a-descriptions-item label="是否当前版本">
          <a-badge :status="currentVersion.isCurrent === 1 ? 'success' : 'default'" :text="currentVersion.isCurrent === 1 ? '是' : '否'" />
        </a-descriptions-item>
        <a-descriptions-item label="版本描述" :span="2">{{ currentVersion.versionDesc || '无' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentVersion.createTime }}</a-descriptions-item>
        <a-descriptions-item label="表单配置" :span="2">
          <pre style="max-height: 400px; overflow: auto; background: #f5f5f5; padding: 16px; border-radius: 4px;">{{ formatFormConfig(currentVersion.formConfig) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </j-modal>
  </j-modal>
</template>

<script>
  import { getFormVersions } from '@/api/api'

  export default {
    name: 'DynamicFormVersion',
    data () {
      return {
        visible: false,
        confirmLoading: false,
        loading: false,
        formId: '',
        formName: '',
        versions: [],
        versionDetailVisible: false,
        currentVersion: {},
        columns: [
          {
            title: '版本号',
            dataIndex: 'version',
            width: 100,
            align: 'center',
          },
          {
            title: '版本描述',
            dataIndex: 'versionDesc',
            ellipsis: true,
          },
          {
            title: '状态',
            dataIndex: 'isCurrent',
            width: 120,
            align: 'center',
            scopedSlots: { customRender: 'isCurrent' },
          },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            width: 180,
          },
          {
            title: '操作',
            dataIndex: 'action',
            width: 100,
            align: 'center',
            scopedSlots: { customRender: 'action' },
          },
        ],
      }
    },
    methods: {
      show (record) {
        this.formId = record.id;
        this.formName = record.formName;
        this.visible = true;
        this.loadVersions();
      },
      loadVersions () {
        this.loading = true;
        getFormVersions({ formId: this.formId }).then((res) => {
          if (res.success) {
            this.versions = res.result || [];
          } else {
            this.$message.warning(res.message);
          }
        }).finally(() => {
          this.loading = false;
        });
      },
      viewVersion (record) {
        this.currentVersion = record;
        this.versionDetailVisible = true;
      },
      formatFormConfig (configStr) {
        try {
          const config = JSON.parse(configStr);
          return JSON.stringify(config, null, 2);
        } catch (e) {
          return configStr;
        }
      },
      handleCancel () {
        this.visible = false;
        this.versions = [];
      },
    }
  }
</script>

