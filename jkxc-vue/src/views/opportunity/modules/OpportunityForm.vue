<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container>
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24" v-if="model.id && model.opportunityNo">
            <a-form-model-item label="线索编号" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input v-model="model.opportunityNo" placeholder="线索编号（自动生成）" :disabled="true" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="业务人员" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="founder">
              <j-search-select-tag
                placeholder="请选择用户"
                v-model="model.founder"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
                :disabled="formDisabled"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="客户名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="corporateName">
              <a-input v-model="model.corporateName" placeholder="请输入客户名称"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="联系人员" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contacts">
              <a-input v-model="model.contacts" placeholder="请输入联系人" :disabled="formDisabled"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="联系方式" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contactInformation">
              <a-input v-model="model.contactInformation" placeholder="请输入联系方式" :disabled="formDisabled"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="线索名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="opportunityName">
              <j-category-select
                v-model="model.opportunityName"
                back="label"
                pcode="A01"
                :value="model.opportunityName"
                placeholder="请选择线索名称"
                loadTriggleChange
                :disabled="formDisabled"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="客户来源" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="opportunitySource">
              <j-dict-select-tag
                placeholder="请选择客户来源"
                v-model="model.opportunitySource"
                dictCode="opportunity_source"
                :disabled="formDisabled"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="所属区域" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="regionCascader">
              <a-cascader v-model="model.regionCascader" :options="areaOptions" placeholder="请选择 省/市/区" style="width: 100%" :disabled="formDisabled" />
              <div v-if="(!model.regionCascader || model.regionCascader.length === 0) && model.region" style="margin-top: 8px; color: #faad14;">
                历史区域：{{ model.region }}（未在省市区数据中，按原值展示）
              </div>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="当前状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="state">
              <j-dict-select-tag placeholder="请选择状态" v-model="model.state" dictCode="opportunity_status" :disabled="formDisabled" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="详细地址" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="address">
              <a-textarea v-model="model.address" rows="4" placeholder="请输入详细地址" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-textarea v-model="model.remarks" rows="3" placeholder="请输入备注" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注图片" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-image-upload v-model="model.remarkImages" text="上传" :isMultiple="true" bizPath="opportunity"></j-image-upload>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
import { httpAction } from '@/api/manage'
import areaData from 'china-area-data'

export default {
  name: 'OpportunityForm',
  props: {
    disabled: {
      type: Boolean,
      default: false,
      required: false,
    },
  },
  data() {
    return {
      model: {
        regionCascader: [], // 默认空数组，确保级联选择器与校验的响应性
      },
      areaOptions: [],
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      confirmLoading: false,
      validatorRules: {
        founder: [{ required: true, message: '请输入业务人员!' }],
        corporateName: [{ required: true, message: '请输入客户名称!' }],
        opportunityName: [{ required: true, message: '请选择线索名称!' }],
        contacts: [{ required: true, message: '请输入联系人!' }],
        contactInformation: [{ required: true, message: '请输入联系方式!' }],
        state: [{ required: true, message: '请选择当前状态!' }],
        opportunitySource: [{ required: true, message: '请选择客户来源!' }],
        regionCascader: [{
          required: true,
          message: '请选择所属区域!',
          trigger: 'change',
          validator: (rule, value, callback) => {
            if (!value || !Array.isArray(value) || value.length === 0) {
              callback(new Error('请选择所属区域!'))
            } else {
              callback()
            }
          }
        }],
        address: [{ required: true, message: '请输入详细地址!' }],
      },
      url: {
        add: '/opportunity/ghOpportunity/add',
        edit: '/opportunity/ghOpportunity/edit',
        queryById: '/opportunity/ghOpportunity/queryById',
      },
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  created() {
    this.modelDefault = JSON.parse(JSON.stringify(this.model))
    // 使用 china-area-data 生成省/市/区级联静态数据
    try {
      const provinces = areaData['86'] || {}
      this.areaOptions = Object.keys(provinces).map(pCode => ({
        value: provinces[pCode],
        label: provinces[pCode],
        children: (areaData[pCode] ? Object.keys(areaData[pCode]).map(cCode => ({
          value: areaData[pCode][cCode],
          label: areaData[pCode][cCode],
          children: (areaData[cCode] ? Object.keys(areaData[cCode]).map(aCode => ({
            value: areaData[cCode][aCode],
            label: areaData[cCode][aCode]
          })) : [])
        })) : [])
      }))
    } catch (e) {
      // 回退为空，避免阻断
      this.areaOptions = []
    }
  },
  methods: {
    add() {
      this.edit(this.modelDefault)
    },
    edit(record) {
      this.model = Object.assign({}, record)
      // 兼容老数据：如果有老的 region 串，但没有级联数据，则尝试智能拆分
      if ((!this.model.regionCascader || this.model.regionCascader.length === 0) && this.model.region) {
        const guess = this.model.region.split(/[\/-]/).filter(Boolean)
        if (guess.length) {
          this.$set(this.model, 'regionCascader', guess)
        }
      }
    },
    submitForm() {
      const that = this
      // 在验证之前，将 regionCascader 转换为 region 字符串
      if (this.model.regionCascader && this.model.regionCascader.length > 0) {
        this.model.region = this.model.regionCascader.join('/')
      } else {
        // 如果没有选择区域，清空 region 字段
        this.model.region = ''
      }
      
      this.$refs.form.validate((valid) => {
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
          httpAction(httpurl, this.model, method)
            .then((res) => {
              if (res.success) {
                that.$message.success(res.message)
                that.$emit('ok')
              } else {
                that.$message.warning(res.message)
              }
            })
            .finally(() => {
              that.confirmLoading = false
            })
        }
      })
    },
  },
}
</script>

