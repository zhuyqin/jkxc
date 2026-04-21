<template>
  <a-spin :spinning="confirmLoading">
    <a-form-model ref="form" :model="model" :rules="validatorRules" :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-model-item label="地址商户" prop="supplierCompany">
            <a-input v-model="model.supplierCompany" placeholder="请输入地址商户" :disabled="formDisabled"></a-input>
          </a-form-model-item>
        </a-col>
        <a-col :span="12">
          <a-form-model-item label="联系人员" prop="supplierContact">
            <a-input v-model="model.supplierContact" placeholder="请输入联系人员" :disabled="formDisabled"></a-input>
          </a-form-model-item>
        </a-col>
        <a-col :span="12">
          <a-form-model-item label="联系电话" prop="supplierContactNumber">
            <a-input v-model="model.supplierContactNumber" placeholder="请输入联系电话" :disabled="formDisabled"></a-input>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="备注信息" prop="remarks" :labelCol="{ span: 3 }" :wrapperCol="{ span: 21 }">
            <a-textarea v-model="model.remarks" rows="4" placeholder="请输入备注信息" :disabled="formDisabled"></a-textarea>
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>
  </a-spin>
</template>

<script>
import { httpAction, getAction } from '@/api/manage'

export default {
  name: 'SupplierForm',
  props: {
    disabled: {
      type: Boolean,
      default: false,
      required: false,
    },
  },
  data() {
    return {
      model: {},
      confirmLoading: false,
      validatorRules: {
        supplierCompany: [{ required: true, message: '请输入地址商户!' }],
        supplierContact: [{ required: true, message: '请输入联系人员!' }],
        supplierContactNumber: [{ required: true, message: '请输入联系电话!' }],
      },
      url: {
        add: '/supplier/ghSupplier/add',
        edit: '/supplier/ghSupplier/edit',
        queryById: '/supplier/ghSupplier/queryById',
      },
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  created() {
    // 备份model原始值
    this.modelDefault = JSON.parse(JSON.stringify(this.model))
  },
  methods: {
    add() {
      this.edit(this.modelDefault)
    },
    edit(record) {
      this.model = Object.assign({}, record)
      this.visible = true
    },
    submitForm() {
      const that = this
      // 触发表单验证
      this.$refs.form.validate((valid) => {
        if (valid) {
          that.confirmLoading = true
          let httpurl = ''
          let method = ''
          if (!this.model.id) {
            httpurl += this.url.add
            method = 'post'
          } else {
            httpurl += this.url.edit
            method = 'put'
          }
          httpAction(httpurl, this.model, method)
            .then((res) => {
              if (res.success) {
                that.$message.success(res.message)
                that.$emit('ok')
              } else {
                that.$message.warning(res.message)
              }
            })
            .finally(() => {
              that.confirmLoading = false
            })
        }
      })
    },
  },
}
</script>

