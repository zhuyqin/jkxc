<template>
  <a-drawer
    :title="title"
    :width="width"
    placement="right"
    :closable="false"
    @close="close"
    destroyOnClose
    :visible="visible">
    <order-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit" normal></order-form>
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
import OrderForm from './OrderForm'

export default {
  name: 'OrderModal',
  components: {
    OrderForm
  },
  data () {
    return {
      title: "操作",
      width: 800,
      visible: false,
      disableSubmit: false
    }
  },
  methods: {
    add (customerInfo = null) {
      this.visible = true
      this.$nextTick(() => {
        if (customerInfo) {
          // 如果传入了客户信息，使用edit方法填充
          this.$refs.realForm.edit(customerInfo);
        } else {
          this.$refs.realForm.add();
        }
      })
    },
    edit (record) {
      this.visible = true
      this.$nextTick(() => {
        this.$refs.realForm.edit(record);
      });
    },
    close () {
      this.$emit('close');
      this.visible = false;
    },
    submitCallback(){
      this.$emit('ok');
      this.visible = false;
    },
    handleOk () {
      // 防止重复点击
      if (this.$refs.realForm && this.$refs.realForm.confirmLoading) {
        return
      }
      this.$refs.realForm.submitForm();
    },
    handleCancel () {
      this.close()
    }
  }
}
</script>

<style lang="less" scoped>
/** Button按钮间距 */
.ant-btn {
  margin-left: 30px;
  margin-bottom: 30px;
  float: right;
}
.drawer-footer{
  position: absolute;
  bottom: -8px;
  width: 100%;
  border-top: 1px solid #e8e8e8;
  padding: 10px 16px;
  text-align: right;
  left: 0;
  background: #fff;
  border-radius: 0 0 2px 2px;
}
</style>

