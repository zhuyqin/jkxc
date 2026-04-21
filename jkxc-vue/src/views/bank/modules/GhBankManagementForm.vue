<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="收款单位/人" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="payeePerson">
              <a-input v-model="model.payeePerson" placeholder="请输入收款单位/人"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="收款方式" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="paymentMethod">
              <j-dict-select-tag type="list" v-model="model.paymentMethod" dictCode="payment_method"
                                 placeholder="请选择收款方式"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="收款账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="collectionAccount">
              <a-input v-model="model.collectionAccount" placeholder="请输入收款账户"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="网点名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="accountNotes">
              <a-input v-model="model.accountNotes" placeholder="请输入网点名称"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="收款备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="collectionRemarks">
              <a-input v-model="model.collectionRemarks" placeholder="请输入收款备注"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="收款码" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-image-upload class="avatar-uploader" text="上传" v-model="model.pic"></j-image-upload>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="是否隐藏" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="hidden">
              <j-dict-select-tag type="radioButton" v-model="model.hidden" dictCode="yesno"
                                 placeholder="请选择是否隐藏"/>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
  import { httpAction, getAction } from '@/api/manage'

  export default {
    name: 'GhBankManagementForm',
    components: {
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    data () {
      return {
        model:{},
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
          payeePerson: [
            {required: true, message: '请输入收款单位/人!', trigger: 'blur'},
          ],
          paymentMethod: [
            {required: true, message: '请选择收款方式!', trigger: 'change'},
          ],
          collectionAccount: [
            {required: true, message: '请输入收款账户!', trigger: 'blur'},
          ],
          accountNotes: [
            {required: true, message: '请输入网点名称!', trigger: 'blur'},
          ],
          hidden: [
            {required: true, message: '请选择是否隐藏!', trigger: 'change'},
          ],
        },
        url: {
          add: "/bank/ghBankManagement/add",
          edit: "/bank/ghBankManagement/edit",
          queryById: "/bank/ghBankManagement/queryById"
        }
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
    },
    created () {
       //备份model原始值
      this.modelDefault = JSON.parse(JSON.stringify(this.model));
    },
    methods: {
      add () {
        this.edit(this.modelDefault);
      },
      edit (record) {
        this.model = Object.assign({}, record);
        this.visible = true;
      },
      submitForm () {
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
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
            })
          }
         
        })
      },
    }
  }
</script>

