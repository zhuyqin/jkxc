<template>
  <a-modal
    title="线索分配"
    :width="500"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    :confirmLoading="confirmLoading"
  >
    <a-form-model ref="assignForm" :model="assignForm" :rules="assignFormRules">
      <a-form-model-item label="分配业务人员" :labelCol="{span: 6}" :wrapperCol="{span: 18}" prop="chargePerson">
        <j-search-select-tag
          v-model="assignForm.chargePerson"
          placeholder="请选择业务人员"
          dict="sys_user,realname,realname,1=1 and del_flag = '0'"
          :async="false"
        />
      </a-form-model-item>
    </a-form-model>
  </a-modal>
</template>

<script>
import { postAction } from '@/api/manage'

export default {
  name: 'AssignModal',
  data() {
    return {
      visible: false,
      confirmLoading: false,
      opportunityRecord: {},
      assignForm: {
        id: '',
        chargePerson: '',
      },
      assignFormRules: {
        chargePerson: [{ required: true, message: '请选择业务人员!', trigger: 'change' }],
      },
    }
  },
  methods: {
    show(record) {
      this.visible = true
      this.opportunityRecord = record || {}
      this.assignForm.id = record ? record.id : ''
      this.assignForm.chargePerson = record ? (record.chargePerson || record.founder) : ''
      this.$nextTick(() => {
        if (this.$refs.assignForm) {
          this.$refs.assignForm.resetFields()
        }
      })
    },
    handleOk() {
      this.$refs.assignForm.validate((valid) => {
        if (valid) {
          this.confirmLoading = true
          postAction('/opportunity/ghOpportunity/assign', {
            id: this.assignForm.id,
            chargePerson: this.assignForm.chargePerson,
          }).then((res) => {
            if (res.success) {
              this.$message.success('分配成功！')
              this.handleCancel()
              this.$emit('ok')
            } else {
              this.$message.error(res.message || '分配失败！')
            }
          }).finally(() => {
            this.confirmLoading = false
          })
        }
      })
    },
    handleCancel() {
      this.visible = false
      this.opportunityRecord = {}
      this.assignForm = {
        id: '',
        chargePerson: '',
      }
      this.$nextTick(() => {
        if (this.$refs.assignForm) {
          this.$refs.assignForm.resetFields()
        }
      })
    },
  },
}
</script>

<style scoped>
</style>

