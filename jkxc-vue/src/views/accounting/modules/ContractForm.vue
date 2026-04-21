<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row :gutter="24">
          <a-card title="基本信息" :bordered="false" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-model-item label="合同编号" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contractNo">
                  <a-input v-model="model.contractNo" placeholder="请输入合同编号" :disabled="formDisabled"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="订单编号" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="orderNo">
                  <a-input v-model="model.orderNo" placeholder="请输入订单编号" :disabled="true"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="公司名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="companyName">
                  <a-input v-model="model.companyName" placeholder="请输入公司名称" :disabled="true"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="合同金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contractAmount">
                  <a-input-number v-model="model.contractAmount" placeholder="请输入合同金额" :min="0" :precision="2" style="width: 100%" :disabled="formDisabled"></a-input-number>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="签订日期" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="signDate">
                  <a-date-picker v-model="model.signDate" placeholder="请选择签订日期" style="width: 100%" valueFormat="YYYY-MM-DD" :disabled="formDisabled"></a-date-picker>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="合同状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contractStatus">
                  <j-dict-select-tag type="list" v-model="model.contractStatus" dictCode="contract_status" placeholder="请选择合同状态" :disabled="formDisabled"></j-dict-select-tag>
                </a-form-model-item>
              </a-col>
              <a-col :span="24">
                <a-form-model-item label="合同备注" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="remarks">
                  <a-textarea v-model="model.remarks" rows="3" placeholder="请输入合同备注" :disabled="formDisabled"></a-textarea>
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-card>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
import { httpAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'ContractForm',
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      model: {},
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      labelColc: {
        xs: { span: 24 },
        sm: { span: 2 },
      },
      wrapperColc: {
        xs: { span: 24 },
        sm: { span: 22 },
      },
      validatorRules: {
        contractNo: [{ required: true, message: '请输入合同编号!', trigger: 'blur' }],
        orderNo: [{ required: false, message: '请输入订单编号!', trigger: 'blur' }], // 订单编号改为可选
        companyName: [{ required: true, message: '请输入公司名称!', trigger: 'blur' }],
        contractAmount: [{ required: true, message: '请输入合同金额!', trigger: 'blur' }],
        signDate: [{ required: true, message: '请选择签订日期!', trigger: 'change' }],
      },
      url: {
        add: '/order/accountingContract/add',
        edit: '/order/accountingContract/edit',
      },
      confirmLoading: false,
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  methods: {
    add(customerInfo = null) {
      const defaultModel = {}
      // 如果传入了客户信息，自动填充
      if (customerInfo) {
        defaultModel.companyName = customerInfo.corporateName || customerInfo.companyName || ''
      }
      this.edit(defaultModel)
    },
    edit(record) {
      this.model = Object.assign({}, record)
      // 设置默认值
      if (!this.model.signDate) {
        this.model.signDate = moment().format('YYYY-MM-DD')
      }
      // 确保日期格式正确
      if (this.model.signDate && typeof this.model.signDate === 'string') {
        this.model.signDate = moment(this.model.signDate).format('YYYY-MM-DD')
      }
    },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.confirmLoading = true
          let httpurl = ''
          let method = ''
          if (!this.model.id) {
            httpurl += this.url.add
            method = 'POST'
          } else {
            httpurl += this.url.edit
            method = 'PUT'
          }
          httpAction(httpurl, this.model, method).then((res) => {
            if (res.success) {
              this.$message.success(res.message)
              this.$emit('ok')
            } else {
              this.$message.warning(res.message)
            }
          }).finally(() => {
            this.confirmLoading = false
          })
        }
      })
    },
  },
}
</script>

<style scoped>
</style>

