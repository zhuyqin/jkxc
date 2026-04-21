<template>
  <a-modal
    :title="title"
    :width="600"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭"
    :destroyOnClose="true"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :labelCol="labelCol" :wrapperCol="wrapperCol">
        <a-form-model-item label="线索名称" prop="opportunityName">
          <j-category-select
            v-model="model.opportunityName"
            pcode="A01"
            placeholder="请选择线索名称"
            loadTriggleChange
            :disabled="formDisabled"
          />
        </a-form-model-item>

        <a-form-model-item label="奖金金额" prop="bonusMoney">
          <a-input-number
            v-model="model.bonusMoney"
            placeholder="请输入奖金金额"
            :min="0"
            :precision="2"
            style="width: 100%"
            :disabled="formDisabled"
          />
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
import { httpAction } from '@/api/manage'

export default {
  name: 'OpportunityBonusConfigModal',
  data() {
    return {
      title: '操作',
      visible: false,
      confirmLoading: false,
      model: {},
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      formDisabled: false,
      validatorRules: {
        opportunityName: [{ required: true, message: '请选择线索名称!' }],
        bonusMoney: [{ required: true, message: '请输入奖金金额!', type: 'number' }],
      },
      url: {
        add: '/opportunity/ghOpportunityBonusConfig/add',
        edit: '/opportunity/ghOpportunityBonusConfig/edit',
      },
    }
  },
  methods: {
    add() {
      this.edit({})
    },
    edit(record) {
      this.model = Object.assign({}, record)
      this.formDisabled = false
      this.visible = true
    },
    handleOk() {
      const that = this
      this.$refs.form.validate((valid) => {
        if (!valid) return
        that.confirmLoading = true
        const httpurl = that.model && that.model.id ? that.url.edit : that.url.add
        const method = that.model && that.model.id ? 'put' : 'post'
        httpAction(httpurl, that.model, method)
          .then((res) => {
            if (res.success) {
              that.$message.success(res.message || '操作成功')
              that.$emit('ok')
              that.handleCancel()
            } else {
              that.$message.warning(res.message || '操作失败')
            }
          })
          .finally(() => {
            that.confirmLoading = false
          })
      })
    },
    handleCancel() {
      this.visible = false
      this.confirmLoading = false
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
  },
}
</script>

