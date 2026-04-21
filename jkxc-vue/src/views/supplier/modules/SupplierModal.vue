<template>
  <a-modal
    :title="title"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :destroyOnClose="true"
  >
    <supplier-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"></supplier-form>
  </a-modal>
</template>

<script>
import SupplierForm from './SupplierForm'

export default {
  name: 'SupplierModal',
  components: {
    SupplierForm,
  },
  data() {
    return {
      title: '操作',
      visible: false,
      disableSubmit: false,
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
    submitCallback() {
      this.$emit('ok')
      this.visible = false
    },
    handleOk() {
      this.$refs.realForm.submitForm()
    },
    handleCancel() {
      this.close()
    },
  },
}
</script>

