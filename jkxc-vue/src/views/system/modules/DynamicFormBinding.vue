<template>
  <j-modal
    :title="'业务类型绑定 - ' + formName"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="确定"
    cancelText="关闭"
  >
    <a-spin :spinning="confirmLoading">
      <a-alert
        message="提示"
        description="一个业务类型只能绑定一个表单，但一个表单可以绑定多个业务类型。绑定后该业务类型在创建订单时会自动加载该表单的最新版本。订单保存时会记录使用的表单和版本信息，以便查看历史订单时显示当时的表单版本。"
        type="info"
        show-icon
        style="margin-bottom: 16px;"
      />
      
      <a-form-model :model="binding" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="业务类型" required>
          <j-category-select
            v-model="binding.businessType"
            back="label"
            pcode="A01"
            placeholder="请选择业务类型（可多选）"
            loadTriggleChange
            :multiple="true"
            :disabled="!!binding.id"
            @change="handleBusinessTypeChange"
          />
        </a-form-model-item>
        
        <a-form-model-item label="绑定版本" v-if="currentVersion">
          <a-input :value="'v' + currentVersion.version + ' - ' + (currentVersion.versionDesc || '无描述')" disabled>
            <a-tag slot="suffix" color="green">最新版本</a-tag>
          </a-input>
        </a-form-model-item>
        
        <a-form-model-item label="状态">
          <a-radio-group v-model="binding.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-model-item>
      </a-form-model>
      
      <a-divider />
      
      <a-table
        :columns="bindingColumns"
        :dataSource="bindingList"
        :loading="bindingListLoading"
        :pagination="false"
        rowKey="id"
        size="small"
      >
        <span slot="businessTypeName" slot-scope="text">
          <a-tag color="blue">{{ text }}</a-tag>
        </span>
        <span slot="status" slot-scope="text">
          <a-badge :status="text === 1 ? 'success' : 'default'" :text="text === 1 ? '启用' : '禁用'" />
        </span>
        <span slot="action" slot-scope="text, record">
          <a-popconfirm title="确定删除该绑定吗?" @confirm="() => deleteBinding(record.id)">
            <a>
              <a-icon type="delete" /> 删除
            </a>
          </a-popconfirm>
        </span>
      </a-table>
    </a-spin>
  </j-modal>
</template>

<script>
  import { bindBusinessType, getFormVersions, getBindingList } from '@/api/api'
  import { httpAction, deleteAction } from '@/api/manage'

  export default {
    name: 'DynamicFormBinding',
    data () {
      return {
        visible: false,
        confirmLoading: false,
        formId: '',
        formName: '',
        binding: {
          businessType: '', // j-category-select组件期望字符串（逗号分隔），不是数组
          versionId: '',
          status: 1,
        },
        versions: [],
        versionsLoading: false,
        currentVersion: null, // 当前最新版本
        bindingList: [],
        bindingListLoading: false,
        bindingColumns: [
          {
            title: '业务类型',
            dataIndex: 'businessTypeName',
            scopedSlots: { customRender: 'businessTypeName' },
          },
          {
            title: '版本号',
            dataIndex: 'version',
            width: 100,
            align: 'center',
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 100,
            align: 'center',
            scopedSlots: { customRender: 'status' },
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
        this.binding = {
          businessType: '', // j-category-select组件期望字符串（逗号分隔）
          versionId: '',
          status: 1,
        };
        this.visible = true;
        this.loadVersions();
        this.loadBindingList();
      },
      loadVersions () {
        this.versionsLoading = true;
        getFormVersions({ formId: this.formId }).then((res) => {
          if (res.success) {
            this.versions = res.result || [];
            // 自动选择最新版本（isCurrent=1的版本）
            this.currentVersion = this.versions.find(v => v.isCurrent === 1) || 
                                  (this.versions.length > 0 ? this.versions[this.versions.length - 1] : null);
            if (this.currentVersion) {
              this.binding.versionId = this.currentVersion.id;
            }
          } else {
            this.$message.warning(res.message);
          }
        }).finally(() => {
          this.versionsLoading = false;
        });
      },
      handleBusinessTypeChange() {
        // 业务类型变化时，重新加载最新版本
        if (this.currentVersion) {
          this.binding.versionId = this.currentVersion.id;
        }
      },
      loadBindingList () {
        if (!this.formId) {
          return;
        }
        this.bindingListLoading = true;
        getBindingList({ formId: this.formId }).then((res) => {
          if (res.success) {
            this.bindingList = res.result || [];
          } else {
            this.$message.warning(res.message || '加载绑定列表失败');
            this.bindingList = [];
          }
        }).catch((err) => {
          console.error('加载绑定列表失败', err);
          this.$message.error('加载绑定列表失败');
          this.bindingList = [];
        }).finally(() => {
          this.bindingListLoading = false;
        });
      },
      handleOk () {
        // 处理多选业务类型
        // j-category-select组件返回的是逗号分隔的字符串
        let businessTypes = [];
        if (this.binding.businessType && typeof this.binding.businessType === 'string') {
          // 如果是字符串，按逗号分割并过滤空值
          businessTypes = this.binding.businessType.split(',').filter(v => {
            const trimmed = String(v).trim();
            return trimmed && trimmed !== '' && trimmed !== 'null' && trimmed !== 'undefined';
          });
        } else if (Array.isArray(this.binding.businessType)) {
          // 如果是数组（兼容处理），提取value并过滤空值
          businessTypes = this.binding.businessType.map(item => {
            if (typeof item === 'string') {
              return item.trim();
            } else if (item && item.value) {
              return String(item.value).trim();
            } else if (item && item.key) {
              return String(item.key).trim();
            }
            return item;
          }).filter(v => {
            const trimmed = String(v).trim();
            return trimmed && trimmed !== '' && trimmed !== 'null' && trimmed !== 'undefined';
          });
        }
        
        if (businessTypes.length === 0) {
          this.$message.warning('请选择至少一个业务类型');
          return;
        }
        if (!this.binding.versionId || !this.currentVersion) {
          this.$message.warning('表单没有可用版本，请先创建表单版本');
          return;
        }
        // 确保绑定的是最新版本
        this.binding.versionId = this.currentVersion.id;
        
        this.confirmLoading = true;
        
        // 一次性绑定所有业务类型（后端会先删除该表单的所有历史绑定，然后创建新绑定）
        bindBusinessType({
          businessType: businessTypes.join(','), // 将多个业务类型用逗号连接
          formId: this.formId,
          versionId: this.binding.versionId,
          status: this.binding.status, // 传递状态
          deleteHistory: true, // 删除历史绑定
        }).then((res) => {
          if (res.success) {
            this.$message.success(`成功绑定 ${businessTypes.length} 个业务类型`);
            this.loadBindingList(); // 重新加载绑定列表
            this.$emit('ok');
            this.handleCancel(); // 绑定成功后关闭弹窗
          } else {
            this.$message.warning(res.message || '绑定失败');
            this.loadBindingList(); // 重新加载绑定列表
          }
        }).catch((err) => {
          this.$message.error('绑定失败：' + (err.message || '未知错误'));
          this.loadBindingList(); // 重新加载绑定列表
        }).finally(() => {
          this.confirmLoading = false;
        });
      },
      deleteBinding (id) {
        deleteAction('/dynamicform/form/deleteBinding', { id: id }).then((res) => {
          if (res.success) {
            this.$message.success('删除成功');
            this.loadBindingList();
          } else {
            this.$message.warning(res.message);
          }
        });
      },
      handleCancel () {
        this.visible = false;
        this.binding = {
          businessType: '', // j-category-select组件期望字符串
          versionId: '',
          status: 1,
        };
        this.versions = [];
        this.currentVersion = null;
        this.bindingList = [];
      },
    }
  }
</script>

