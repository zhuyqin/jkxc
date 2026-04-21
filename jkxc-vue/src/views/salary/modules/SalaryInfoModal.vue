<template>
  <j-modal
    :title="title"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    switchFullscreen
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭">
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row>
          <a-col :span="12">
            <a-form-model-item label="员工姓名" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
              <j-search-select-tag
                placeholder="请选择员工"
                v-model="model.userId"
                dict="sys_user,realname,id,del_flag=0 and status=1"
                :async="false"
                @change="handleUserChange"
                :disabled="!!model.id"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="当前职位" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input v-model="model.currentPosition" placeholder="请输入当前职位"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="当前星级" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-dict-select-tag
                placeholder="请选择当前星级"
                v-model="model.currentStarLevel"
                dictCode="star_level"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="基本工资" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="baseSalary">
              <a-input-number v-model="model.baseSalary" placeholder="请输入基本工资" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="绩效工资" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.performanceSalary" placeholder="请输入绩效工资" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="公积金补贴" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.housingFundSubsidy" placeholder="请输入公积金补贴" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="其他补贴" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.otherSubsidy" placeholder="请输入其他补贴" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="高温补贴" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.highTemperatureSubsidy" placeholder="请输入高温补贴" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="全勤奖金" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.fullAttendanceBonus" placeholder="请输入全勤奖金" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="应发总额" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="totalAmount" placeholder="自动计算" :min="0" :precision="2" style="width: 100%" :disabled="true"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="公司社保" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.companySocialSecurity" placeholder="请输入公司社保" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="个人社保" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.personalSocialSecurity" placeholder="请输入个人社保" :min="0" :precision="2" style="width: 100%" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="扣除总额" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="deductionAmount" placeholder="自动计算" :min="0" :precision="2" style="width: 100%" :disabled="true"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelColc" :wrapperCol="wrapperColc">
              <a-textarea v-model="model.remarks" placeholder="请输入备注" :rows="3"></a-textarea>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </a-spin>
  </j-modal>
</template>

<script>
import { httpAction, getAction } from '@/api/manage'
import { JeecgFormMixin } from '@/mixins/JeecgFormMixin'

export default {
  name: 'SalaryInfoModal',
  mixins: [JeecgFormMixin],
  data() {
    return {
      title: '工资信息',
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
      labelColc: {
        xs: { span: 24 },
        sm: { span: 3 },
      },
      wrapperColc: {
        xs: { span: 24 },
        sm: { span: 20 },
      },
      model: {},
      totalAmount: null, // 应发总额
      deductionAmount: null, // 扣除总额
      validatorRules: {
        userId: [{ required: true, message: '请选择员工!' }],
        baseSalary: [{ required: true, message: '请输入基本工资!' }],
      },
      url: {
        add: '/salary/ghSalaryInfo/add',
        edit: '/salary/ghSalaryInfo/edit',
        queryById: '/salary/ghSalaryInfo/queryById',
      },
    }
  },
  methods: {
    add() {
      this.edit({})
    },
    edit(record) {
      this.visible = true
      this.model = Object.assign({}, record)
      // 如果只有ID，需要查询详情
      if (record.id && !record.userId) {
        getAction(this.url.queryById, { id: record.id }).then((res) => {
          if (res.success && res.result) {
            this.model = Object.assign({}, res.result)
            this.calculateAmounts()
          }
        })
      } else {
        this.calculateAmounts()
      }
    },
    handleUserChange(value) {
      // 当选择员工时，自动填充职位（如果用户表中有职位信息）
      if (value && this.model.userId !== value) {
        // 这里可以通过API查询用户信息，但为了简化，暂时不自动填充
        // 如果需要自动填充，可以调用用户查询接口
      }
      this.calculateAmounts()
    },
    calculateAmounts() {
      // 应发总额 = 基本工资 + 绩效工资 + 公积金补贴 + 高温补贴 + 其他补贴 + 全勤奖金
      let total = 0
      if (this.model.baseSalary) total += parseFloat(this.model.baseSalary) || 0
      if (this.model.performanceSalary) total += parseFloat(this.model.performanceSalary) || 0
      if (this.model.housingFundSubsidy) total += parseFloat(this.model.housingFundSubsidy) || 0
      if (this.model.highTemperatureSubsidy) total += parseFloat(this.model.highTemperatureSubsidy) || 0
      if (this.model.otherSubsidy) total += parseFloat(this.model.otherSubsidy) || 0
      if (this.model.fullAttendanceBonus) total += parseFloat(this.model.fullAttendanceBonus) || 0
      
      // 扣除总额 = 公司社保 + 个人社保
      let deduction = 0
      if (this.model.companySocialSecurity) deduction += parseFloat(this.model.companySocialSecurity) || 0
      if (this.model.personalSocialSecurity) deduction += parseFloat(this.model.personalSocialSecurity) || 0
      
      this.totalAmount = total > 0 ? total : null
      this.deductionAmount = deduction > 0 ? deduction : null
    },
    handleOk() {
      const that = this
      // 防止重复提交
      if (that.confirmLoading) {
        return
      }
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true
          let httpurl = ''
          let method = ''
          if (!that.model.id) {
            httpurl = that.url.add
            method = 'post'
          } else {
            httpurl = that.url.edit
            method = 'put'
          }
          httpAction(httpurl, that.model, method).then((res) => {
            if (res.success) {
              that.$message.success(res.message || '操作成功！')
              that.$emit('ok')
              that.handleCancel()
            } else {
              that.$message.warning(res.message)
            }
          }).catch((err) => {
            console.error('操作失败', err)
            that.$message.error('操作失败，请重试')
          }).finally(() => {
            that.confirmLoading = false
          })
        }
      })
    },
    handleCancel() {
      this.visible = false
      this.model = {}
      this.$refs.form.resetFields()
    },
  },
}
</script>

<style scoped>
</style>

