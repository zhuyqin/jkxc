<template>
  <j-modal
    :title="title"
    :width="1200"
    :visible="visible"
    :switchFullscreen="switchFullscreen"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <address-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"></address-form>
    <template slot="footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" @click="handleOk" :loading="confirmLoading">确定</a-button>
    </template>
  </j-modal>
</template>

<script>
import AddressForm from './AddressForm'

export default {
  name: 'AddressModal',
  components: {
    AddressForm,
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
    add() {
      this.visible = true
      this.disableSubmit = false
      this.$nextTick(() => {
        this.$refs.realForm.add()
      })
    },
    edit(record) {
      this.visible = true
      this.disableSubmit = false // 默认可编辑
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

