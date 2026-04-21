<template>
  <a-modal
    :title="title"
    :width="640"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules">
        <a-form-model-item label="团队名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="teamName">
          <a-input placeholder="请输入团队名称" v-model="model.teamName" />
        </a-form-model-item>

        <a-form-model-item label="团队编码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="teamCode">
          <a-input placeholder="请输入团队编码" v-model="model.teamCode" />
        </a-form-model-item>

        <a-form-model-item label="团队负责人" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <j-select-user-by-dep v-model="model.teamLeader" :multi="false" store="id" text="realname" placeholder="请选择团队负责人" />
        </a-form-model-item>

        <a-form-model-item label="团队描述" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-textarea placeholder="请输入团队描述" v-model="model.description" :rows="4" />
        </a-form-model-item>

        <a-form-model-item label="状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="status">
          <a-radio-group v-model="model.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-model-item>

        <a-form-model-item label="排序号" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-input-number placeholder="请输入排序号" v-model="model.sortOrder" :min="0" style="width: 100%" />
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
  import { getAction, postAction, putAction } from '@/api/manage'

  export default {
    name: 'TeamModal',
    data() {
      return {
        title: '操作',
        visible: false,
        model: {
          teamName: '',
          teamCode: '',
          teamLeader: '',
          teamLeaderName: '',
          description: '',
          status: 1,
          sortOrder: 0
        },
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        confirmLoading: false,
        validatorRules: {
          teamName: [{ required: true, message: '请输入团队名称!', trigger: 'blur' }],
        }
      }
    },
    methods: {
      add() {
        this.edit({});
      },
      edit(record) {
        this.model = Object.assign({}, record);
        this.model.status = record.status !== undefined ? record.status : 1;
        this.model.sortOrder = record.sortOrder !== undefined ? record.sortOrder : 0;
        this.visible = true;
      },
      close() {
        this.$emit('close');
        this.visible = false;
        this.$refs.form.resetFields();
      },
      handleCancel() {
        this.close();
      },
      handleOk() {
        const that = this;
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if (!this.model.id) {
              httpurl += '/sys/team/add';
              method = 'post';
            } else {
              httpurl += '/sys/team/edit';
              method = 'put';
            }
            let formData = Object.assign(this.model, {});
            if (method === 'post') {
              postAction(httpurl, formData).then((res) => {
                if (res.success) {
                  that.$message.success(res.message);
                  that.$emit('ok');
                } else {
                  that.$message.warning(res.message);
                }
              }).finally(() => {
                that.confirmLoading = false;
                that.close();
              });
            } else if (method === 'put') {
              putAction(httpurl, formData).then((res) => {
                if (res.success) {
                  that.$message.success(res.message);
                  that.$emit('ok');
                } else {
                  that.$message.warning(res.message);
                }
              }).finally(() => {
                that.confirmLoading = false;
                that.close();
              });
            }
          } else {
            return false;
          }
        });
      },
    }
  }
</script>

<style scoped>
</style>

