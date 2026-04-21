<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row :gutter="24">
          <a-card title="编辑信息" :bordered="false" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-model-item label="地址成本" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="addressCost">
                  <a-input-number v-model="model.addressCost" placeholder="请输入地址成本" :min="0" :precision="2" style="width: 100%" :disabled="formDisabled"></a-input-number>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="地址商" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="supplier">
                  <j-search-select-tag
                    v-model="model.supplier"
                    placeholder="请选择地址商"
                    dict="gh_supplier,id,supplier_company,del_flag=0"
                    :async="false"
                    :disabled="formDisabled"
                    @change="handleSupplierChange"
                  />
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
import { httpAction, getAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'AddressForm',
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
        supplier: [{ required: true, message: '请选择地址商!', trigger: 'change' }],
      },
      url: {
        add: '/address/ghAddressCenter/add',
        edit: '/address/ghAddressCenter/edit',
        querySupplierById: '/supplier/ghSupplier/queryById',
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
    add() {
      this.edit({ serviceStatus: '1' })
    },
    edit(record) {
      this.model = Object.assign({}, record)
      // 确保服务状态有默认值
      if (!this.model.serviceStatus) {
        this.model.serviceStatus = '1'
      }
      // 处理日期字段
      if (this.model.startDate) {
        this.model.startDate = moment(this.model.startDate)
      }
      if (this.model.terminationDate) {
        this.model.terminationDate = moment(this.model.terminationDate)
      }
      // 如果supplier字段有值（地址商ID），尝试加载地址商信息（如果供应商信息为空）
      if (this.model.supplier && (!this.model.supplierCompany || !this.model.supplierContact)) {
        getAction(this.url.querySupplierById, { id: this.model.supplier })
          .then((res) => {
            if (res.success && res.result) {
              const supplier = res.result
              // 如果供应商信息为空，则填充
              if (!this.model.supplierCompany) {
                this.model.supplierCompany = supplier.supplierCompany || ''
              }
              if (!this.model.supplierContact) {
                this.model.supplierContact = supplier.supplierContact || ''
              }
              if (!this.model.supplierContactNumber) {
                this.model.supplierContactNumber = supplier.supplierContactNumber || ''
              }
            }
          })
          .catch((err) => {
            console.error('加载地址商信息失败', err)
          })
      }
      this.visible = true
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
          
          // 处理日期字段
          const submitData = Object.assign({}, this.model)
          if (submitData.startDate && moment.isMoment(submitData.startDate)) {
            submitData.startDate = submitData.startDate.format('YYYY-MM-DD')
          }
          if (submitData.terminationDate && moment.isMoment(submitData.terminationDate)) {
            submitData.terminationDate = submitData.terminationDate.format('YYYY-MM-DD')
          }
          
          httpAction(httpurl, submitData, method).then((res) => {
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
    // 处理地址商选择变化
    handleSupplierChange(value) {
      if (value) {
        // 根据选择的地址商ID获取详细信息并填充表单
        getAction(this.url.querySupplierById, { id: value })
          .then((res) => {
            if (res.success && res.result) {
              const supplier = res.result
              // 自动填充供应商相关信息
              this.model.supplierCompany = supplier.supplierCompany || ''
              this.model.supplierContact = supplier.supplierContact || ''
              this.model.supplierContactNumber = supplier.supplierContactNumber || ''
              // supplier字段存储地址商的ID
              this.model.supplier = value
            }
          })
          .catch((err) => {
            console.error('获取地址商信息失败', err)
            this.$message.error('获取地址商信息失败')
          })
      } else {
        // 清空供应商相关信息
        this.model.supplierCompany = ''
        this.model.supplierContact = ''
        this.model.supplierContactNumber = ''
        this.model.supplier = ''
      }
    },
  },
}
</script>

<style scoped>
</style>

