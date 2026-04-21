<template>
  <j-modal
    :title="title"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    switchFullscreen
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭"
    :maskClosable="false"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :labelCol="labelCol" :wrapperCol="wrapperCol">
        <a-form-model-item label="表单名称" prop="formName">
          <a-input v-model="model.formName" placeholder="请输入表单名称" :maxLength="100"></a-input>
        </a-form-model-item>
        <a-form-model-item label="表单编码" prop="formCode">
          <a-input v-model="model.formCode" placeholder="请输入表单编码（唯一标识）" :maxLength="50" :disabled="!!model.id"></a-input>
        </a-form-model-item>
        <a-form-model-item label="表单描述" prop="description">
          <a-textarea v-model="model.description" placeholder="请输入表单描述" :rows="3" :maxLength="500"></a-textarea>
        </a-form-model-item>
        <a-form-model-item label="状态" prop="status">
          <a-radio-group v-model="model.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </j-modal>
</template>

<script>
  import { httpAction } from '@/api/manage'
  import { addDynamicForm, editDynamicForm, queryDynamicFormById } from '@/api/api'

  export default {
    name: 'DynamicFormModal',
    data () {
      return {
        title:"操作",
        visible: false,
        model: {},
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
          formName: [
            { required: true, message: '请输入表单名称!' },
          ],
          formCode: [
            { required: true, message: '请输入表单编码!' },
            { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '表单编码必须以字母开头，只能包含字母、数字和下划线!' },
          ],
        },
        url: {
          add: "/dynamicform/form/add",
          edit: "/dynamicform/form/edit",
          queryById: "/dynamicform/form/queryById",
        },
      }
    },
    created () {
    },
    methods: {
      add () {
        this.edit({});
      },
      edit (record) {
        this.model = Object.assign({}, record);
        if (!this.model.status) {
          this.model.status = 1;
        }
        if (!this.model.currentVersion) {
          this.model.currentVersion = 1;
        }
        this.visible = true;
      },
      close () {
        this.$emit('close');
        this.visible = false;
      },
      handleOk () {
        const that = this;
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if(!this.model.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
              method = 'put';
            }
            httpAction(httpurl,this.model,method).then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
                that.close();
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
            })
          }
        })
      },
      handleCancel () {
        this.close()
      },
    }
  }
</script>

