<template>
  <j-modal
    :title="title"
    :width="1200"
    :visible="visible"
    :switchFullscreen="switchFullscreen"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <customer-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"></customer-form>
    <template slot="footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" @click="handleOk" :loading="confirmLoading">确定</a-button>
    </template>
  </j-modal>
</template>

<script>
import CustomerForm from './CustomerForm'
export default {
  name: 'CustomerModal',
  components: {
    CustomerForm,
  },
  data() {
    return {
      title: '',
      visible: false,
      disableSubmit: false,
      switchFullscreen: true,
      confirmLoading: false,
    }
  },
  methods: {
    add() {
      this.visible = true
      this.$nextTick(() => {
        this.$refs.realForm.add()
      })
    },
    edit(record) {
      this.visible = true
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

