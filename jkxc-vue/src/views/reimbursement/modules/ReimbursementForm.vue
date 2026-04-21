<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row :gutter="24">
          <a-card title="基本信息" :bordered="false" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-model-item label="报销时间" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="reimbursementTime">
                  <j-date placeholder="请选择报销时间" v-model="model.reimbursementTime" style="width: 100%" :show-time="false" :disabled="formDisabled" />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="报销人员" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="reimbursementPerson">
                  <j-search-select-tag
                    placeholder="请选择报销人员"
                    v-model="model.reimbursementPerson"
                    dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                    :async="false"
                    :disabled="formDisabled"
                    @change="handlePersonChange"
                  />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="所属团队" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="model.teamName" placeholder="选择报销人员后自动带出" disabled></a-input>
                  <a-input v-model="model.teamId" type="hidden"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="报销类目" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="category">
                  <j-category-select v-model="model.category" back="value" pcode="A02" placeholder="请选择报销类目" :disabled="formDisabled" />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="公司名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="companyName">
                  <j-search-select-tag
                    placeholder="请选择公司名称"
                    v-model="model.companyName"
                    dict="gh_customer,corporate_name,corporate_name,del_flag=0"
                    :async="true"
                    :disabled="formDisabled"
                  />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="报销金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="totalPrice">
                  <a-input-number v-model="model.totalPrice" placeholder="请输入报销金额" :min="0" :precision="2" style="width: 100%" :disabled="formDisabled"></a-input-number>
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-card>

          <a-card title="报销信息" :bordered="false" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px', marginTop: '16px'}">
            <a-row :gutter="16">
              <a-col :span="24">
                <a-form-model-item label="报销依据" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="reimbursementBasis">
                  <j-image-upload v-model="model.reimbursementBasis" text="上传" :isMultiple="true" bizPath="reimbursement" :disabled="formDisabled"></j-image-upload>
                  <div style="color: #999; margin-top: 4px">支持上传多张图片作为报销依据（必填）</div>
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-card>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
import { httpAction, getAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'ReimbursementForm',
  props: {
    disabled: {
      type: Boolean,
      default: false,
      required: false,
    },
  },
  data() {
    return {
      model: {},
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
      validatorRules: {
        reimbursementTime: [{ required: true, message: '请选择报销时间!' }],
        reimbursementPerson: [{ required: true, message: '请选择报销人员!' }],
        category: [{ required: true, message: '请选择报销类目!' }],
        companyName: [{ required: false, message: '请选择公司名称!' }],
        totalPrice: [{ required: true, message: '请输入报销金额!' }],
        reimbursementBasis: [
          { 
            required: true, 
            validator: (rule, value, callback) => {
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
                callback(new Error('请上传报销依据图片'))
              } else {
                callback()
              }
            },
            trigger: 'change' 
          }
        ],
      },
      url: {
        add: '/GhReimbursement/ghReimbursement/add',
        edit: '/GhReimbursement/ghReimbursement/edit',
        queryById: '/GhReimbursement/ghReimbursement/queryById',
      },
      confirmLoading: false,
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  methods: {
    add() {
      this.edit({})
    },
    edit(record) {
      this.model = Object.assign({}, record)
      
      // 如果已有报销人员，加载团队信息
      if (record.reimbursementPerson) {
        this.loadTeamInfo(record.reimbursementPerson)
      }
    },
    // 报销人员变化，自动带出所属团队
    handlePersonChange(value) {
      if (value) {
        this.loadTeamInfo(value)
      } else {
        this.model.teamId = ''
        this.model.teamName = ''
      }
    },
    // 加载团队信息
    loadTeamInfo(realname) {
      // j-search-select-tag返回的是realname，需要通过realname查询用户
      getAction('/sys/user/list', { realname: realname, pageSize: 1 }).then((res) => {
        if (res.success && res.result && res.result.records && res.result.records.length > 0) {
          const user = res.result.records[0]
          if (user.teamId) {
            this.model.teamId = user.teamId
            // 查询团队名称
            getAction('/sys/team/queryById', { id: user.teamId }).then((teamRes) => {
              if (teamRes.success && teamRes.result) {
                this.model.teamName = teamRes.result.teamName || ''
              }
            })
          } else {
            this.model.teamId = ''
            this.model.teamName = ''
          }
        }
      }).catch(() => {
        this.model.teamId = ''
        this.model.teamName = ''
      })
    },
    submitForm() {
      const that = this
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true
          let httpurl = ''
          let method = ''
          if (!this.model.id) {
            httpurl += this.url.add
            method = 'post'
          } else {
            httpurl += this.url.edit
            method = 'put'
          }
          
          // 处理日期格式
          const formData = Object.assign({}, this.model)
          if (formData.reimbursementTime) {
            if (typeof formData.reimbursementTime === 'string') {
              formData.reimbursementTime = formData.reimbursementTime
            } else {
              formData.reimbursementTime = moment(formData.reimbursementTime).format('YYYY-MM-DD')
            }
          }
          
          httpAction(httpurl, formData, method).then((res) => {
            if (res.success) {
              that.$message.success(res.message)
              that.$emit('ok')
            } else {
              that.$message.warning(res.message)
            }
          }).finally(() => {
            that.confirmLoading = false
          })
        }
      })
    },
  },
}
</script>

<style scoped>
</style>
