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
            <a-form-model-item label="下单日期" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="orderDate">
              <j-date placeholder="请选择下单日期" v-model="model.orderDate" style="width: 100%" :show-time="false" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="type">
              <a-radio-group v-model="model.type" @change="handleTypeChange">
                <a-radio value="expense">支出</a-radio>
                <a-radio value="income">收入</a-radio>
              </a-radio-group>
            </a-form-model-item>
          </a-col>
          <a-col :span="12" v-if="model.type === 'expense'">
            <a-form-model-item label="业务类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="category">
              <j-category-select v-model="model.category" back="value" pcode="A02" placeholder="请选择业务类型" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" v-if="model.type === 'income'">
            <a-form-model-item label="收入金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="incomeAmount">
              <a-input-number v-model="model.incomeAmount" placeholder="请输入收入金额" :min="0" :precision="2" style="width: 100%"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="12" v-if="model.type === 'expense'">
            <a-form-model-item label="支出金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="expenseAmount">
              <a-input-number v-model="model.expenseAmount" placeholder="请输入支出金额" :min="0" :precision="2" style="width: 100%"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="费用详情" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="feeDetail">
              <a-input v-model="model.feeDetail" placeholder="请输入费用详情"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="银行账户" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="bankAccountId">
              <a-cascader
                v-model="model.bankAccountCascader"
                :options="bankAccountOptions"
                placeholder="请选择收款方式/收款单位/人/网点名称"
                :loading="bankAccountLoading"
                :change-on-select="false"
                :show-search="true"
                :field-names="{ label: 'label', value: 'value', children: 'children' }"
                @change="handleBankAccountChange"
                style="width: 100%"
                :getPopupContainer="(triggerNode) => triggerNode.parentNode"
                :popupStyle="{ minWidth: '600px', maxHeight: '400px' }"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="公司名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="companyName">
              <j-search-select-tag
                placeholder="请选择公司名称"
                v-model="model.companyName"
                dict="gh_customer,corporate_name,corporate_name,del_flag=0"
                :async="true"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="操作人员" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="operatorId">
              <j-search-select-tag
                placeholder="请选择操作人员"
                v-model="model.operatorId"
                dict="sys_user,realname,id,del_flag=0"
                :async="false"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="附件" :labelCol="labelColc" :wrapperCol="wrapperColc">
              <j-image-upload v-model="model.vouchers" text="上传" :isMultiple="true" bizPath="bankDiary"></j-image-upload>
              <div style="color: #999; margin-top: 4px">支持上传多张图片作为附件</div>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注信息" :labelCol="labelColc" :wrapperCol="wrapperColc">
              <a-textarea v-model="model.remarks" placeholder="请输入备注信息" :rows="3"></a-textarea>
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
  name: 'BankDiaryModal',
  mixins: [JeecgFormMixin],
  data() {
    return {
      title: '银行日记',
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
      bankAccountOptions: [],
      bankAccountLoading: false,
      validatorRules: {
        orderDate: [{ required: true, message: '请选择下单日期!' }],
        type: [{ required: true, message: '请选择类型!' }],
        category: [
          {
            required: true,
            validator: (rule, value, callback) => {
              if (this.model.type === 'expense' && !value) {
                callback(new Error('请选择业务类型!'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        feeDetail: [{ required: true, message: '请输入费用详情!' }],
        incomeAmount: [
          {
            required: true,
            validator: (rule, value, callback) => {
              if (this.model.type === 'income' && (!value || value <= 0)) {
                callback(new Error('请输入收入金额!'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        expenseAmount: [
          {
            required: true,
            validator: (rule, value, callback) => {
              if (this.model.type === 'expense' && (!value || value <= 0)) {
                callback(new Error('请输入支出金额!'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        bankAccountId: [{ required: true, message: '请选择银行账户!' }],
        operatorId: [{ required: true, message: '请选择操作人员!' }],
      },
      url: {
        add: '/bankDiary/ghBankDiary/add',
        edit: '/bankDiary/ghBankDiary/edit',
        queryById: '/bankDiary/ghBankDiary/queryById',
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
      
      // 加载银行账户选项
      this.loadBankAccountOptions()
      
      // 如果有银行账户ID，需要构建级联选择器的值
      if (this.model.bankAccountId) {
        this.loadBankAccountCascaderValue(this.model.bankAccountId)
      }
      
      // 设置默认操作人（新增时）
      if (!this.model.id && !this.model.operatorId) {
        const userInfo = this.$store.getters.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
        const userId = userInfo.id || userInfo.userId
        if (userId) {
          this.model.operatorId = userId
        }
      }
      
      // 如果只有ID，需要查询详情
      if (record.id && !record.orderDate) {
        getAction(this.url.queryById, { id: record.id }).then((res) => {
          if (res.success && res.result) {
            this.model = Object.assign({}, res.result)
            // 判断类型（根据收入金额和支出金额）
            if (this.model.incomeAmount && this.model.incomeAmount > 0) {
              this.model.type = 'income'
            } else if (this.model.expenseAmount && this.model.expenseAmount > 0) {
              this.model.type = 'expense'
            }
            // 加载银行账户级联值
            if (this.model.bankAccountId) {
              this.loadBankAccountCascaderValue(this.model.bankAccountId)
            }
          }
        })
      }
    },
    handleTypeChange() {
      // 切换类型时清空金额和业务类型
      if (this.model.type === 'income') {
        this.model.expenseAmount = undefined
        this.model.category = undefined
      } else if (this.model.type === 'expense') {
        this.model.incomeAmount = undefined
      }
      // 触发验证
      this.$nextTick(() => {
        this.$refs.form.validateField(['incomeAmount', 'expenseAmount', 'category'])
      })
    },
    // 加载银行账户选项（三级级联）
    loadBankAccountOptions() {
      this.bankAccountLoading = true
      return getAction('/bank/ghBankManagement/listAll').then((res) => {
        if (res.success && res.result) {
          const accounts = Array.isArray(res.result) ? res.result : []
          
          // 按收款方式分组
          const paymentMethodMap = {}
          accounts.forEach(acc => {
            if (!acc.paymentMethod || !acc.payeePerson) {
              return
            }
            
            if (!paymentMethodMap[acc.paymentMethod]) {
              paymentMethodMap[acc.paymentMethod] = {}
            }
            
            if (!paymentMethodMap[acc.paymentMethod][acc.payeePerson]) {
              paymentMethodMap[acc.paymentMethod][acc.payeePerson] = []
            }
            
            paymentMethodMap[acc.paymentMethod][acc.payeePerson].push({
              id: acc.id,
              accountNotes: acc.accountNotes || '',
              pic: acc.pic
            })
          })
          
          // 构建级联数据结构
          this.bankAccountOptions = Object.keys(paymentMethodMap).map(paymentMethod => {
            const payeePersonMap = paymentMethodMap[paymentMethod]
            const payeePersonList = Object.keys(payeePersonMap).map(payeePerson => ({
              label: payeePerson,
              value: `${paymentMethod}__${payeePerson}`,
              children: payeePersonMap[payeePerson].map(acc => ({
                label: acc.accountNotes || '（无网点名称）',
                value: acc.id,
                accountId: acc.id,
                pic: acc.pic
              }))
            }))
            
            return {
              label: paymentMethod,
              value: paymentMethod,
              children: payeePersonList
            }
          })
        } else {
          this.bankAccountOptions = []
        }
      }).catch(() => {
        this.bankAccountOptions = []
        return Promise.resolve()
      }).finally(() => {
        this.bankAccountLoading = false
      })
    },
    // 加载银行账户级联选择器的值
    loadBankAccountCascaderValue(accountId) {
      getAction('/bank/ghBankManagement/queryById', { id: accountId }).then((res) => {
        if (res.success && res.result) {
          const account = res.result
          if (account.paymentMethod && account.payeePerson) {
            this.$set(this.model, 'bankAccountCascader', [
              account.paymentMethod,
              `${account.paymentMethod}__${account.payeePerson}`,
              accountId
            ])
          }
        }
      })
    },
    // 银行账户级联选择器变化
    handleBankAccountChange(value, selectedOptions) {
      this.$set(this.model, 'bankAccountCascader', value)
      
      if (value && value.length === 3) {
        const accountId = value[2]
        this.model.bankAccountId = accountId
      } else {
        this.model.bankAccountId = undefined
      }
    },
    handleOk() {
      const that = this
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
          }).finally(() => {
            that.confirmLoading = false
          })
        }
      })
    },
    handleCancel() {
      this.visible = false
      this.model = {}
      this.bankAccountOptions = []
      this.$refs.form.resetFields()
    },
  },
}
</script>

<style scoped>
</style>