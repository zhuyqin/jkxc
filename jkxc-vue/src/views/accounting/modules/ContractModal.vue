<template>
  <j-modal
    :title="title"
    :width="1200"
    :visible="visible"
    :switchFullscreen="switchFullscreen"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <contract-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"></contract-form>
    <template slot="footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" @click="handleOk" :loading="confirmLoading">确定</a-button>
    </template>
  </j-modal>
</template>

<script>
import ContractForm from './ContractForm'

export default {
  name: 'ContractModal',
  components: {
    ContractForm,
  },
  data() {
    return {
      title: '操作',
      visible: false,
      disableSubmit: false,
      switchFullscreen: true,
      confirmLoading: false,
    }
  },
  methods: {
    add(customerInfo = null) {
      this.visible = true
      this.title = '新增合同'
      this.$nextTick(() => {
        this.$refs.realForm.add(customerInfo)
      })
    },
    edit(record) {
      this.visible = true
      this.title = '编辑合同'
      this.$nextTick(() => {
        this.$refs.realForm.edit(record)
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      this.$refs.realForm.submitForm()
    },
    submitCallback() {
      this.$emit('ok')
      this.visible = false
    },
    handleCancel() {
      this.close()
    },
  },
}
</script>

