<template>
  <a-modal
    :title="title"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :destroyOnClose="true"
    cancelText="关闭">
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="报销信息" :labelCol="labelColc" :wrapperCol="wrapperColc">
              <a-descriptions :column="2" bordered size="small">
                <a-descriptions-item label="报销时间">{{ model.reimbursementTime ? moment(model.reimbursementTime).format('YYYY-MM-DD') : '-' }}</a-descriptions-item>
                <a-descriptions-item label="报销人员">{{ model.reimbursementPerson_dictText || model.reimbursementPerson || '-' }}</a-descriptions-item>
                <a-descriptions-item label="所属团队">{{ model.teamId_dictText || model.teamName || '-' }}</a-descriptions-item>
                <a-descriptions-item label="报销类目">{{ model.category_dictText || '-' }}</a-descriptions-item>
                <a-descriptions-item label="公司名称">{{ model.companyName || '-' }}</a-descriptions-item>
                <a-descriptions-item label="报销金额">¥{{ formatAmount(model.totalPrice) }}</a-descriptions-item>
                <a-descriptions-item label="报销依据" :span="2">
                  <div v-if="model.reimbursementBasis" style="display: flex; flex-wrap: wrap; gap: 8px;">
                    <img
                      v-for="(img, index) in getImageList(model.reimbursementBasis)"
                      :key="index"
                      :src="getImageUrl(img)"
                      alt="报销依据"
                      style="width: 80px; height: 80px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                      @click="previewImage(getImageUrl(img))"
                    />
                  </div>
                  <span v-else>-</span>
                </a-descriptions-item>
              </a-descriptions>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="审核状态" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="auditFlag">
              <j-dict-select-tag
                v-model="model.auditFlag"
                dictCode="audit_status"
                placeholder="请选择审核状态"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-if="model.auditFlag !== '2'">
            <a-form-model-item label="支付依据" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="paymentBasis">
              <j-image-upload v-model="model.paymentBasis" text="上传" :isMultiple="true" bizPath="reimbursement"></j-image-upload>
              <div style="color: #999; margin-top: 4px">支持上传多张图片作为支付依据（必填）</div>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-if="model.auditFlag !== '2'">
            <a-form-model-item label="支付单位" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="paymentBankAccountId">
              <a-cascader
                v-model="model.paymentAccountCascader"
                :options="paymentAccountOptions"
                placeholder="请选择收款方式/收款单位/人/网点名称"
                :loading="paymentAccountLoading"
                :change-on-select="false"
                :show-search="true"
                :field-names="{ label: 'label', value: 'value', children: 'children' }"
                @change="handlePaymentAccountChange"
                style="width: 100%"
                :getPopupContainer="(triggerNode) => triggerNode.parentNode"
                :popupStyle="{ minWidth: '600px', maxHeight: '400px' }"
              />
              <div style="color: #999; margin-top: 4px">选择支付单位（必填）</div>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="审核备注" :labelCol="labelColc" :wrapperCol="wrapperColc">
              <a-textarea v-model="model.remarks" placeholder="请输入审核备注" :rows="3"></a-textarea>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </a-spin>

    <!-- 图片预览弹窗 -->
    <a-modal :visible="previewVisible" :footer="null" @cancel="previewVisible = false" :width="800">
      <img alt="预览" style="width: 100%" :src="previewImageUrl" />
    </a-modal>
  </a-modal>
</template>

<script>
import { httpAction, getAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'ReimbursementAuditModal',
  data() {
    return {
      title: '报销审核',
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
      validatorRules: {
        auditFlag: [{ required: true, message: '请选择审核状态!' }],
        paymentBasis: [
          { 
            required: false, // 改为非必填，通过validator动态判断
            validator: (rule, value, callback) => {
              // 如果审核状态是"已拒绝"（值为"2"），则不需要支付依据
              if (this.model.auditFlag === '2') {
                callback()
                return
              }
              
              // 其他状态需要支付依据
              let hasValue = false
              if (value) {
                if (typeof value === 'string') {
                  const trimmed = value.trim()
                  if (trimmed && trimmed !== '[]' && trimmed !== 'null') {
                    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                      try {
                        const parsed = JSON.parse(trimmed)
                        if (Array.isArray(parsed) && parsed.length > 0) {
                          hasValue = true
                        }
                      } catch (e) {
                        // JSON解析失败
                      }
                    } else if (trimmed.includes(',')) {
                      const items = trimmed.split(',').filter(item => item.trim())
                      if (items.length > 0) {
                        hasValue = true
                      }
                    } else {
                      hasValue = true
                    }
                  }
                } else if (Array.isArray(value) && value.length > 0) {
                  hasValue = true
                }
              }
              if (!hasValue) {
                callback(new Error('请上传支付依据图片'))
              } else {
                callback()
              }
            },
            trigger: 'change' 
          }
        ],
        paymentBankAccountId: [
          { 
            required: false, // 改为非必填，通过validator动态判断
            validator: (rule, value, callback) => {
              // 如果审核状态是"已拒绝"（值为"2"），则不需要支付单位
              if (this.model.auditFlag === '2') {
                callback()
                return
              }
              
              // 其他状态需要支付单位
              if (!value) {
                callback(new Error('请选择支付单位!'))
              } else {
                callback()
              }
            },
            trigger: 'change' 
          }
        ],
      },
      paymentAccountOptions: [],
      paymentAccountLoading: false,
      previewVisible: false,
      previewImageUrl: '',
      url: {
        audit: '/GhReimbursement/ghReimbursement/audit',
        queryById: '/GhReimbursement/ghReimbursement/queryById',
      },
    }
  },
  methods: {
    show(record) {
      this.visible = true
      this.model = Object.assign({}, record)
      // 初始化级联选择器的值
      this.$set(this.model, 'paymentAccountCascader', [])
      
      // 如果审核状态为空，默认设置为"已审核"（值为"1"）
      if (!this.model.auditFlag) {
        this.model.auditFlag = '1'
      }
      
      // 如果只有ID，需要查询详情
      if (record.id && !record.reimbursementTime) {
        getAction(this.url.queryById, { id: record.id }).then((res) => {
          if (res.success && res.result) {
            this.model = Object.assign({}, res.result)
            // 初始化级联选择器的值
            this.$set(this.model, 'paymentAccountCascader', [])
            // 如果审核状态为空，默认设置为"已审核"（值为"1"）
            if (!this.model.auditFlag) {
              this.model.auditFlag = '1'
            }
            // 如果有支付单位ID，需要构建级联选择器的值
            if (this.model.paymentBankAccountId) {
              // 等待选项加载完成后再设置值
              this.loadPaymentAccountOptions().then(() => {
                this.loadPaymentAccountCascaderValue(this.model.paymentBankAccountId)
              })
            }
          }
        })
      } else {
        // 如果有支付单位ID，需要构建级联选择器的值
        if (this.model.paymentBankAccountId) {
          // 等待选项加载完成后再设置值
          this.loadPaymentAccountOptions().then(() => {
            this.loadPaymentAccountCascaderValue(this.model.paymentBankAccountId)
          })
        } else {
          // 加载支付单位选项
          this.loadPaymentAccountOptions()
        }
      }
      
      // 如果没有支付单位ID，直接加载选项
      if (!this.model.paymentBankAccountId) {
        this.loadPaymentAccountOptions()
      }
    },
    // 加载支付单位级联选择器的值
    loadPaymentAccountCascaderValue(accountId) {
      getAction('/bank/ghBankManagement/queryById', { id: accountId }).then((res) => {
        if (res.success && res.result) {
          const account = res.result
          if (account.paymentMethod && account.payeePerson) {
            this.model.paymentAccountCascader = [
              account.paymentMethod,
              `${account.paymentMethod}__${account.payeePerson}`,
              accountId
            ]
          }
        }
      })
    },
    // 加载支付单位选项（三联级）
    loadPaymentAccountOptions() {
      this.paymentAccountLoading = true
      // 使用listAll接口获取所有数据（不分页）
      return getAction('/bank/ghBankManagement/listAll').then((res) => {
        if (res.success && res.result) {
          const accounts = Array.isArray(res.result) ? res.result : []
          
          // 按收款方式分组
          const paymentMethodMap = {}
          accounts.forEach(acc => {
            // 只要求有paymentMethod和payeePerson，accountNotes可以为空（但会显示为空字符串）
            if (!acc.paymentMethod || !acc.payeePerson) {
              return // 跳过不完整的数据（后端已经过滤了delFlag和hidden）
            }
            
            if (!paymentMethodMap[acc.paymentMethod]) {
              paymentMethodMap[acc.paymentMethod] = {}
            }
            
            if (!paymentMethodMap[acc.paymentMethod][acc.payeePerson]) {
              paymentMethodMap[acc.paymentMethod][acc.payeePerson] = []
            }
            
            paymentMethodMap[acc.paymentMethod][acc.payeePerson].push({
              id: acc.id,
              accountNotes: acc.accountNotes || '', // 允许为空，显示为空字符串
              pic: acc.pic
            })
          })
          
          // 构建级联数据结构
          this.paymentAccountOptions = Object.keys(paymentMethodMap).map(paymentMethod => {
            const payeePersonMap = paymentMethodMap[paymentMethod]
            const payeePersonList = Object.keys(payeePersonMap).map(payeePerson => ({
              label: payeePerson,
              value: `${paymentMethod}__${payeePerson}`, // 使用分隔符组合作为value
              children: payeePersonMap[payeePerson].map(acc => ({
                label: acc.accountNotes || '（无网点名称）',
                value: acc.id, // 第三级保存的是账号ID
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
          this.paymentAccountOptions = []
        }
      }).catch(() => {
        this.paymentAccountOptions = []
        return Promise.resolve()
      }).finally(() => {
        this.paymentAccountLoading = false
      })
    },
    // 支付单位级联选择器变化
    handlePaymentAccountChange(value, selectedOptions) {
      // 确保级联选择器的值被正确设置（这样选择框才能显示选中的内容）
      this.$set(this.model, 'paymentAccountCascader', value)
      
      // 级联选择器变化时，保存账号ID
      if (value && value.length === 3) {
        const accountId = value[2] // 第三级是账号ID
        // 保存支付单位ID（银行账户ID）到 paymentBankAccountId
        this.model.paymentBankAccountId = accountId
      } else {
        this.model.paymentBankAccountId = undefined
      }
    },
    handleOk() {
      const that = this
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true
          // 提交数据，paymentBankAccountId 会随 model 一起提交
          httpAction(that.url.audit, that.model, 'post').then((res) => {
            if (res.success) {
              that.$message.success(res.message || '审核成功！')
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
      this.$refs.form.resetFields()
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getImageList(imageVoucher) {
      if (!imageVoucher) return []
      try {
        // 如果是JSON字符串，解析为数组
        if (typeof imageVoucher === 'string' && imageVoucher.startsWith('[')) {
          return JSON.parse(imageVoucher)
        }
        // 如果是逗号分隔的字符串，转换为数组
        if (typeof imageVoucher === 'string' && imageVoucher.includes(',')) {
          return imageVoucher.split(',').filter(item => item.trim())
        }
        // 如果是单个字符串，转换为数组
        return [imageVoucher]
      } catch (e) {
        return [imageVoucher]
      }
    },
    getImageUrl(imagePath) {
      if (!imagePath) return ''
      // 如果已经是完整的URL，直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      // 如果是相对路径，通过后端接口访问
      if (imagePath.startsWith('/')) {
        return imagePath
      }
      // 否则通过后端静态资源接口访问
      return '/sys/common/static/' + imagePath
    },
    previewImage(imageUrl) {
      this.previewImageUrl = imageUrl
      this.previewVisible = true
    },
    moment,
  },
}
</script>

<style scoped>
</style>

