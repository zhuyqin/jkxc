<template>
  <a-drawer
    :title="title"
    :width="width"
    placement="right"
    :closable="false"
    @close="close"
    destroyOnClose
    :visible="visible">
    <opportunity-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"></opportunity-form>
    <div class="drawer-footer">
      <a-button @click="handleCancel" style="margin-bottom: 0;">关闭</a-button>
      <a-button 
        v-if="!disableSubmit" 
        @click="handleOk" 
        type="primary" 
        style="margin-bottom: 0;"
        :loading="this.$refs.realForm && this.$refs.realForm.confirmLoading"
        :disabled="this.$refs.realForm && this.$refs.realForm.confirmLoading"
      >提交</a-button>
    </div>
  </a-drawer>
</template>

<script>
import OpportunityForm from './OpportunityForm'

export default {
  name: 'OpportunityModal',
  components: {
    OpportunityForm,
  },
  data() {
    return {
      title: '操作',
      width: 800,
      visible: false,
      disableSubmit: false,
    }
  },
  methods: {
    add() {
      this.visible = true
      this.title = '新增线索'
      this.disableSubmit = false
      this.$nextTick(() => {
        this.$refs.realForm.add()
      })
    },
    edit(record) {
      this.visible = true
      this.title = '编辑线索'
      this.disableSubmit = false
      this.$nextTick(() => {
        this.$refs.realForm.edit(record)
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      // 防止重复点击
      if (this.$refs.realForm && this.$refs.realForm.confirmLoading) {
        return
      }
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

<style lang="less" scoped>
/** Button按钮间距 */
.drawer-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  border-top: 1px solid #e8e8e8;
  padding: 10px 16px;
  text-align: right;
  left: 0;
  background: #fff;
  border-radius: 0 0 4px 4px;
}

.ant-btn {
  margin-left: 8px;
  margin-bottom: 0;
}
</style>

