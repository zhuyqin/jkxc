<template>
  <j-modal
    title="批量发放工资"
    :width="600"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭">
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-alert
          :message="`已选择 ${selectedIds.length} 条记录`"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        />
        <a-form-model-item label="发放时间" prop="paymentTime">
          <j-date
            placeholder="请选择发放时间"
            v-model="model.paymentTime"
            style="width: 100%"
            :show-time="true"
          />
        </a-form-model-item>
        <a-form-model-item label="发放方式">
          <j-dict-select-tag
            placeholder="请选择发放方式"
            v-model="model.paymentMethod"
            dictCode="payment_method"
          />
        </a-form-model-item>
        <a-form-model-item label="发放账号">
          <j-search-select-tag
            placeholder="请选择发放账号"
            v-model="model.paymentAccountId"
            dict="gh_bank_management,account_name,id,del_flag=0"
            :async="false"
          />
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </j-modal>
</template>

<script>
import { postAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'SalaryBatchPayModal',
  props: {
    selectedIds: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      visible: false,
      confirmLoading: false,
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      model: {
        paymentTime: null,
        paymentMethod: '',
        paymentAccountId: '',
      },
      validatorRules: {},
      url: {
        batchPay: '/salary/ghSalaryPayment/batchPay',
      },
    }
  },
  methods: {
    moment,
    show() {
      this.visible = true
      this.model = {
        paymentTime: moment(),
        paymentMethod: '',
        paymentAccountId: '',
      }
      this.$refs.form.resetFields()
    },
    handleOk() {
      const that = this
      if (that.selectedIds.length === 0) {
        that.$message.warning('请选择要发放的记录！')
        return
      }
      that.confirmLoading = true
      const params = {
        ids: that.selectedIds,
        paymentTime: that.model.paymentTime ? moment(that.model.paymentTime).format('YYYY-MM-DD HH:mm:ss') : moment().format('YYYY-MM-DD HH:mm:ss'),
        paymentMethod: that.model.paymentMethod,
        paymentAccountId: that.model.paymentAccountId,
      }
      postAction(that.url.batchPay, params).then((res) => {
        if (res.success) {
          that.$message.success(res.message || '批量发放成功！')
          that.$emit('ok')
          that.handleCancel()
        } else {
          that.$message.warning(res.message)
        }
      }).finally(() => {
        that.confirmLoading = false
      })
    },
    handleCancel() {
      this.visible = false
      this.model = {
        paymentTime: moment(),
        paymentMethod: '',
        paymentAccountId: '',
      }
      this.$refs.form.resetFields()
    },
  },
}
</script>

<style scoped>
</style>

