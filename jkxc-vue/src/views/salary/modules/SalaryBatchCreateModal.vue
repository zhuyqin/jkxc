<template>
  <j-modal
    title="批量创建工资发放记录"
    :width="600"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭">
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-form-model-item label="发放月份" prop="paymentMonth">
          <a-month-picker
            placeholder="请选择发放月份"
            v-model="paymentMonthMoment"
            format="YYYY-MM"
            style="width: 100%"
            @change="handleMonthChange"
          />
        </a-form-model-item>
        <a-form-model-item label="选择员工（可选）">
          <j-search-select-tag
            placeholder="不选择则创建所有在职员工的发放记录"
            v-model="model.userIds"
            dict="sys_user,realname,id,del_flag=0 and status=1"
            :async="false"
            mode="multiple"
          />
          <div style="margin-top: 8px; color: #999; font-size: 12px;">
            提示：不选择员工则自动为所有在职员工创建发放记录
          </div>
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </j-modal>
</template>

<script>
import { postAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'SalaryBatchCreateModal',
  data() {
    return {
      visible: false,
      confirmLoading: false,
      paymentMonthMoment: null,
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      model: {
        paymentMonth: null,
        userIds: [],
      },
      validatorRules: {
        paymentMonth: [{ required: true, message: '请选择发放月份!' }],
      },
      url: {
        batchCreate: '/salary/ghSalaryPayment/batchCreate',
      },
    }
  },
  methods: {
    moment,
    show() {
      this.visible = true
      this.model = {
        paymentMonth: null,
        userIds: [],
      }
      this.paymentMonthMoment = null
      this.$refs.form.resetFields()
    },
    handleMonthChange(date, dateString) {
      this.model.paymentMonth = dateString || null
    },
    handleOk() {
      const that = this
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true
          const params = {
            paymentMonth: that.model.paymentMonth,
            userIds: that.model.userIds && that.model.userIds.length > 0 ? that.model.userIds : null,
          }
          postAction(that.url.batchCreate, params).then((res) => {
            if (res.success) {
              that.$message.success(res.message || '批量创建成功！')
              that.$emit('ok')
              that.handleCancel()
            } else {
              that.$message.warning(res.message)
            }
          }).finally(() => {
            that.confirmLoading = false
          })
        }
      })
    },
    handleCancel() {
      this.visible = false
      this.model = {
        paymentMonth: null,
        userIds: [],
      }
      this.paymentMonthMoment = null
      this.$refs.form.resetFields()
    },
  },
}
</script>

<style scoped>
</style>

