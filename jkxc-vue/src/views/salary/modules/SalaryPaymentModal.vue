<template>
  <j-modal
    :title="title"
    :width="900"
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
            <a-form-model-item label="发放月份" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="paymentMonth">
              <a-month-picker
                placeholder="请选择发放月份"
                v-model="paymentMonthMoment"
                format="YYYY-MM"
                style="width: 100%"
                @change="handleMonthChange"
                :disabled="isPayMode"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="基本工资" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.baseSalary" placeholder="请输入基本工资" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="绩效工资" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.performanceSalary" placeholder="请输入绩效工资" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="公积金补贴" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.housingFundSubsidy" placeholder="请输入公积金补贴" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="其他补贴" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.otherSubsidy" placeholder="请输入其他补贴" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="高温补贴" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.highTemperatureSubsidy" placeholder="请输入高温补贴" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="全勤奖金" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.fullAttendanceBonus" placeholder="请输入全勤奖金" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="公司社保" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.companySocialSecurity" placeholder="请输入公司社保" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="个人社保" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.personalSocialSecurity" placeholder="请输入个人社保" :min="0" :precision="2" style="width: 100%" :disabled="isPayMode" @change="calculateAmounts"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="应发总额" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.totalAmount" placeholder="自动计算" :min="0" :precision="2" style="width: 100%" :disabled="true"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="扣除总额" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.deductionAmount" placeholder="自动计算" :min="0" :precision="2" style="width: 100%" :disabled="true"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="实发金额" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number v-model="model.actualAmount" placeholder="自动计算" :min="0" :precision="2" style="width: 100%" :disabled="true"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12" v-if="isPayMode">
            <a-form-model-item label="发放时间" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-date
                placeholder="请选择发放时间"
                v-model="model.paymentTime"
                style="width: 100%"
                :show-time="true"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" v-if="isPayMode">
            <a-form-model-item label="发放方式" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-dict-select-tag
                placeholder="请选择发放方式"
                v-model="model.paymentMethod"
                dictCode="payment_method"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" v-if="isPayMode">
            <a-form-model-item label="发放账号" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-search-select-tag
                placeholder="请选择发放账号"
                v-model="model.paymentAccountId"
                dict="gh_bank_management,account_name,id,del_flag=0"
                :async="false"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelColc" :wrapperCol="wrapperColc">
              <a-textarea v-model="model.remarks" placeholder="请输入备注" :rows="3" :disabled="isPayMode"></a-textarea>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </a-spin>
  </j-modal>
</template>

<script>
import { httpAction, getAction, postAction } from '@/api/manage'
import { JeecgFormMixin } from '@/mixins/JeecgFormMixin'
import moment from 'moment'

export default {
  name: 'SalaryPaymentModal',
  mixins: [JeecgFormMixin],
  data() {
    return {
      title: '工资发放记录',
      visible: false,
      confirmLoading: false,
      isPayMode: false,
      paymentMonthMoment: null,
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
      validatorRules: {
        userId: [{ required: true, message: '请选择员工!' }],
        paymentMonth: [{ required: true, message: '请选择发放月份!' }],
      },
      url: {
        add: '/salary/ghSalaryPayment/add',
        edit: '/salary/ghSalaryPayment/edit',
        pay: '/salary/ghSalaryPayment/pay',
        queryById: '/salary/ghSalaryPayment/queryById',
      },
    }
  },
  watch: {
    model: {
      deep: true,
      handler() {
        this.calculateAmounts()
      },
    },
  },
  methods: {
    moment,
    add() {
      this.isPayMode = false
      this.edit({})
    },
    edit(record) {
      this.isPayMode = false
      this.visible = true
      this.model = Object.assign({}, record)
      // 处理月份
      if (record.paymentMonth) {
        this.paymentMonthMoment = moment(record.paymentMonth, 'YYYY-MM')
      } else {
        this.paymentMonthMoment = null
      }
      // 如果只有ID，需要查询详情
      if (record.id && !record.userId) {
        getAction(this.url.queryById, { id: record.id }).then((res) => {
          if (res.success && res.result) {
            this.model = Object.assign({}, res.result)
            if (res.result.paymentMonth) {
              this.paymentMonthMoment = moment(res.result.paymentMonth, 'YYYY-MM')
            }
          }
        })
      }
      this.calculateAmounts()
    },
    pay(record) {
      this.isPayMode = true
      this.visible = true
      this.model = Object.assign({}, record)
      // 如果只有ID，需要查询详情
      if (record.id && !record.userId) {
        getAction(this.url.queryById, { id: record.id }).then((res) => {
          if (res.success && res.result) {
            this.model = Object.assign({}, res.result)
          }
        })
      }
    },
    handleUserChange(value) {
      // 当选择员工时，自动从工资信息中获取数据
      if (value && this.model.userId !== value && !this.isPayMode) {
        // 从工资信息中同步数据
        getAction('/salary/ghSalaryInfo/queryByUserId', { userId: value }).then((res) => {
          if (res.success && res.result) {
            const salaryInfo = res.result
            // 如果字段为空，则从工资信息中同步
            if (!this.model.baseSalary) this.model.baseSalary = salaryInfo.baseSalary
            if (!this.model.performanceSalary) this.model.performanceSalary = salaryInfo.performanceSalary
            if (!this.model.housingFundSubsidy) this.model.housingFundSubsidy = salaryInfo.housingFundSubsidy
            if (!this.model.highTemperatureSubsidy) this.model.highTemperatureSubsidy = salaryInfo.highTemperatureSubsidy
            if (!this.model.otherSubsidy) this.model.otherSubsidy = salaryInfo.otherSubsidy
            if (!this.model.fullAttendanceBonus) this.model.fullAttendanceBonus = salaryInfo.fullAttendanceBonus
            if (!this.model.companySocialSecurity) this.model.companySocialSecurity = salaryInfo.companySocialSecurity
            if (!this.model.personalSocialSecurity) this.model.personalSocialSecurity = salaryInfo.personalSocialSecurity
            this.calculateAmounts()
          }
        }).catch(() => {
          // 如果查询失败，不影响继续操作
        })
      }
      this.calculateAmounts()
    },
    handleMonthChange(date, dateString) {
      this.model.paymentMonth = dateString || null
    },
    calculateAmounts() {
      // 应发总额 = 基本工资 + 绩效工资 + 公积金补贴 + 高温补贴 + 其他补贴 + 全勤奖金
      let totalAmount = 0
      if (this.model.baseSalary) totalAmount += parseFloat(this.model.baseSalary) || 0
      if (this.model.performanceSalary) totalAmount += parseFloat(this.model.performanceSalary) || 0
      if (this.model.housingFundSubsidy) totalAmount += parseFloat(this.model.housingFundSubsidy) || 0
      if (this.model.highTemperatureSubsidy) totalAmount += parseFloat(this.model.highTemperatureSubsidy) || 0
      if (this.model.otherSubsidy) totalAmount += parseFloat(this.model.otherSubsidy) || 0
      if (this.model.fullAttendanceBonus) totalAmount += parseFloat(this.model.fullAttendanceBonus) || 0
      
      // 扣除总额 = 公司社保 + 个人社保
      let deductionAmount = 0
      if (this.model.companySocialSecurity) deductionAmount += parseFloat(this.model.companySocialSecurity) || 0
      if (this.model.personalSocialSecurity) deductionAmount += parseFloat(this.model.personalSocialSecurity) || 0
      
      // 实发金额 = 应发总额 - 扣除总额
      const actualAmount = totalAmount - deductionAmount
      
      this.model.totalAmount = totalAmount > 0 ? totalAmount : null
      this.model.deductionAmount = deductionAmount > 0 ? deductionAmount : null
      this.model.actualAmount = actualAmount
    },
    handleOk() {
      const that = this
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true
          
          if (that.isPayMode) {
            // 发放操作
            const params = {
              id: that.model.id,
              paymentTime: that.model.paymentTime ? moment(that.model.paymentTime).format('YYYY-MM-DD HH:mm:ss') : moment().format('YYYY-MM-DD HH:mm:ss'),
              paymentMethod: that.model.paymentMethod,
              paymentAccountId: that.model.paymentAccountId,
            }
            postAction(that.url.pay, params).then((res) => {
              if (res.success) {
                that.$message.success(res.message || '发放成功！')
                that.$emit('ok')
                that.handleCancel()
              } else {
                that.$message.warning(res.message)
              }
            }).finally(() => {
              that.confirmLoading = false
            })
          } else {
            // 新增/编辑操作
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
            }).finally(() => {
              that.confirmLoading = false
            })
          }
        }
      })
    },
    handleCancel() {
      this.visible = false
      this.isPayMode = false
      this.model = {}
      this.paymentMonthMoment = null
      this.$refs.form.resetFields()
    },
  },
}
</script>

<style scoped>
</style>

