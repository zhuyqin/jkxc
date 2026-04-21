<template>
  <div>
    <a-spin :spinning="confirmLoading">
      <j-form-container :disabled="formDisabled">
        <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
          <a-row :gutter="24">
            <a-card title="基本信息" :bordered="false" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-model-item label="公司名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="corporateName">
                    <a-input v-model="model.corporateName" placeholder="请输入公司名称" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="联系人" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contacts">
                    <a-input v-model="model.contacts" placeholder="请输入联系人" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="联系电话" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contactInformation">
                    <a-input v-model="model.contactInformation" placeholder="请输入联系电话" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="所属区域" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="region">
                    <a-cascader v-model="model.regionCascader" :options="areaOptions" placeholder="请选择 省/市/区" style="width: 100%" />
                    <div v-if="(!model.regionCascader || model.regionCascader.length === 0) && model.region" style="margin-top: 8px; color: #faad14;">
                      历史区域：{{ model.region }}（未在省市区数据中，按原值展示）
                    </div>
                  </a-form-model-item>
                </a-col>
                <a-col :span="24">
                  <a-form-model-item label="详细地址" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="address">
                    <a-input v-model="model.address" placeholder="请输入详细地址" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
              </a-row>
            </a-card>
            <a-card title="法人信息" :bordered="false" style="margin-top: 16px" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
              <a-row :gutter="16">
                <a-col :span="24" style="margin-bottom: 20px;">
                  <a-alert
                    message="快速查询"
                    description="输入法人手机号可快速查询并填充企业信息"
                    type="info"
                    show-icon
                    style="margin-bottom: 16px;"
                  />
                  <a-form-model-item :labelCol="{xs: {span: 24}, sm: {span: 0}}" :wrapperCol="{xs: {span: 24}, sm: {span: 24}}">
                    <a-input-group compact>
                      <a-input 
                        v-model="legalPersonPhoneQuery" 
                        placeholder="请输入法人手机号" 
                        style="width: calc(100% - 120px)" 
                        @pressEnter="handleQueryClick"
                        allow-clear
                      >
                        <a-icon slot="prefix" type="mobile" />
                      </a-input>
                      <a-button type="primary" @click="handleQueryClick" :loading="queryLoading" icon="search" style="width: 120px">
                        查询企业信息
                      </a-button>
                    </a-input-group>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="法人姓名" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="legalPersonName">
                    <a-input v-model="model.legalPersonName" placeholder="请输入法人姓名" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="法人电话" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="legalPersonPhone">
                    <a-input v-model="model.legalPersonPhone" placeholder="请输入法人电话" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="税号" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="dutyParagraph">
                    <a-input v-model="model.dutyParagraph" placeholder="请输入税号" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="注册资金" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="registeredCapital">
                    <a-input v-model="model.registeredCapital" placeholder="请输入注册资金" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="24">
                  <a-form-model-item label="注册地址" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="registeredAddress">
                    <a-input v-model="model.registeredAddress" placeholder="请输入注册地址" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="24">
                  <a-form-model-item label="实际地址" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="actualAddress">
                    <a-input v-model="model.actualAddress" placeholder="请输入实际地址" allow-clear></a-input>
                  </a-form-model-item>
                </a-col>
                <a-col :span="24">
                  <a-form-model-item label="经营范围" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="businessScope">
                    <a-textarea v-model="model.businessScope" rows="4" placeholder="请输入经营范围" :maxLength="500" :auto-size="{ minRows: 4, maxRows: 6 }"></a-textarea>
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="status">
                    <j-dict-select-tag
                      type="list"
                      v-model="model.status"
                      dictCode="status"
                      placeholder="请选择状态"
                    />
                  </a-form-model-item>
                </a-col>
              </a-row>
            </a-card>
          </a-row>
        </a-form-model>
      </j-form-container>
    </a-spin>
    
    <!-- 企业选择弹窗 -->
    <a-modal
      title="选择企业"
      :visible="enterpriseSelectModal.visible"
      :width="800"
      @ok="handleSelectEnterprise"
      @cancel="handleCancelSelectEnterprise"
      okText="确定"
      cancelText="取消"
    >
      <a-table
        :columns="enterpriseSelectModal.columns"
        :dataSource="enterpriseSelectModal.dataSource"
        :pagination="false"
        :rowSelection="{ type: 'radio', selectedRowKeys: enterpriseSelectModal.selectedRowKeys, onChange: onEnterpriseSelectChange }"
        rowKey="index"
        size="small"
      >
        <template slot="corporateName" slot-scope="text">
          <a-tag color="blue">{{ text }}</a-tag>
        </template>
      </a-table>
      <div v-if="enterpriseSelectModal.dataSource.length === 0" style="text-align: center; padding: 40px 0;">
        <a-empty description="暂无数据" />
      </div>
    </a-modal>
  </div>
</template>

<script>
import { httpAction, getAction } from '@/api/manage'
import { queryCustomerByLegalPersonPhone } from '@/api/api'
import areaData from 'china-area-data'

export default {
  name: 'CustomerForm',
  props: {
    disabled: {
      type: Boolean,
      default: false,
      required: false,
    },
    normal: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      model: {},
      areaOptions: [],
      legalPersonPhoneQuery: '',
      queryLoading: false,
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      labelColc: {
        xs: { span: 24 },
        sm: { span: 2 },
      },
      wrapperColc: {
        xs: { span: 24 },
        sm: { span: 22 },
      },
      confirmLoading: false,
      validatorRules: {
        corporateName: [{ required: true, message: '请输入公司名称!', trigger: 'blur' }],
      },
      url: {
        add: '/customer/ghCustomer/add',
        edit: '/customer/ghCustomer/edit',
        queryById: '/customer/ghCustomer/queryById',
      },
      enterpriseSelectModal: {
        visible: false,
        dataSource: [],
        selectedRowKeys: [],
        selectedEnterprise: null,
        columns: [
          {
            title: '公司名称',
            dataIndex: 'corporateName',
            scopedSlots: { customRender: 'corporateName' },
            width: 200,
          },
          {
            title: '法人姓名',
            dataIndex: 'legalPersonName',
            width: 120,
          },
          {
            title: '法人电话',
            dataIndex: 'legalPersonPhone',
            width: 150,
          },
          {
            title: '税号',
            dataIndex: 'dutyParagraph',
            width: 150,
          },
          {
            title: '注册地址',
            dataIndex: 'registeredAddress',
            ellipsis: true,
          },
        ],
      },
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  created() {
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
  mounted() {},
  methods: {
    add() {
      this.edit({})
    },
    edit(record) {
      this.model = Object.assign({}, record)
      if (!this.model.status) {
        this.model.status = '1'
      }
      // 兼容老数据：如果有老的 region 串，但没有级联数据，则尝试智能拆分
      if ((!this.model.regionCascader || this.model.regionCascader.length === 0) && this.model.region) {
        const guess = this.model.region.split(/[\/-]/).filter(Boolean)
        if (guess.length) {
          this.$set(this.model, 'regionCascader', guess)
        }
      }
      this.visible = true
    },
    submitForm() {
      const that = this
      // 触发表单验证
      this.$refs.form.validate((valid) => {
        if (valid) {
          that.confirmLoading = true
          let httpurl = ''
          let method = ''
          
          // 将 regionCascader 转换为 region 字符串
          if (this.model.regionCascader && this.model.regionCascader.length > 0) {
            this.model.region = this.model.regionCascader.join('/')
          }
          
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
    handleQueryClick() {
      this.queryEnterpriseInfo()
    },
    queryEnterpriseInfo() {
      if (!this.legalPersonPhoneQuery || !this.legalPersonPhoneQuery.trim()) {
        this.$message.warning('请输入法人手机号')
        return
      }
      
      this.queryLoading = true
      const params = { frlxfs: this.legalPersonPhoneQuery.trim() }
      
      queryCustomerByLegalPersonPhone(params)
        .then((res) => {
          if (!res) {
            this.$message.error('接口返回数据为空')
            return
          }
          
          if (res.success && res.result) {
            // 判断返回的是单个对象还是数组
            let enterpriseList = []
            if (Array.isArray(res.result)) {
              enterpriseList = res.result
            } else {
              enterpriseList = [res.result]
            }
            
            if (enterpriseList.length === 0) {
              this.$message.warning('未查询到企业信息')
              return
            }
            
            // 如果只有一个企业，直接填充
            if (enterpriseList.length === 1) {
              this.fillEnterpriseInfo(enterpriseList[0])
              this.$message.success('企业信息查询成功，已自动填充表单')
            } else {
              // 多个企业，显示选择弹窗
              this.enterpriseSelectModal.dataSource = enterpriseList.map((item, index) => ({
                ...item,
                index: index,
              }))
              this.enterpriseSelectModal.selectedRowKeys = []
              this.enterpriseSelectModal.selectedEnterprise = null
              this.enterpriseSelectModal.visible = true
            }
          } else {
            this.$message.warning(res.message || '未查询到企业信息')
          }
        })
        .catch((err) => {
          this.$message.error('查询企业信息失败：' + (err.message || '未知错误'))
        })
        .finally(() => {
          this.queryLoading = false
        })
    },
    fillEnterpriseInfo(enterprise) {
      // 将查询到的企业信息填充到表单
      if (enterprise) {
        // 直接使用 Object.assign 合并数据，确保所有字段都被填充
        // 然后使用 Vue.set 确保响应式更新
        const newModel = Object.assign({}, this.model, {
          corporateName: enterprise.corporateName || this.model.corporateName,
          contacts: enterprise.contacts || this.model.contacts,
          contactInformation: enterprise.contactInformation || this.model.contactInformation,
          region: enterprise.region || this.model.region,
          address: enterprise.address || this.model.address,
          legalPersonName: enterprise.legalPersonName || this.model.legalPersonName,
          legalPersonPhone: enterprise.legalPersonPhone || (this.legalPersonPhoneQuery && this.legalPersonPhoneQuery.trim()) || this.model.legalPersonPhone,
          dutyParagraph: enterprise.dutyParagraph || this.model.dutyParagraph,
          registeredAddress: enterprise.registeredAddress || this.model.registeredAddress,
          actualAddress: enterprise.actualAddress || this.model.actualAddress,
          registeredCapital: enterprise.registeredCapital ? String(enterprise.registeredCapital) : this.model.registeredCapital,
          businessScope: enterprise.businessScope || this.model.businessScope,
          status: enterprise.status || this.model.status || '1',
        })
        
        // 处理区域字段：如果有 region 字符串，尝试转换为 regionCascader
        if (newModel.region && (!newModel.regionCascader || newModel.regionCascader.length === 0)) {
          const guess = newModel.region.split(/[\/-]/).filter(Boolean)
          if (guess.length) {
            newModel.regionCascader = guess
          }
        }
        
        // 使用 Vue.set 逐个更新字段，确保响应式
        Object.keys(newModel).forEach(key => {
          if (newModel[key] !== undefined && newModel[key] !== null) {
            this.$set(this.model, key, newModel[key])
          }
        })
      }
    },
    onEnterpriseSelectChange(selectedRowKeys, selectedRows) {
      this.enterpriseSelectModal.selectedRowKeys = selectedRowKeys
      if (selectedRows && selectedRows.length > 0) {
        this.enterpriseSelectModal.selectedEnterprise = selectedRows[0]
      } else {
        this.enterpriseSelectModal.selectedEnterprise = null
      }
    },
    handleSelectEnterprise() {
      if (!this.enterpriseSelectModal.selectedEnterprise) {
        this.$message.warning('请选择要填入的企业')
        return
      }
      
      // 填充选中的企业信息
      this.fillEnterpriseInfo(this.enterpriseSelectModal.selectedEnterprise)
      this.$message.success('企业信息已填充到表单')
      this.handleCancelSelectEnterprise()
    },
    handleCancelSelectEnterprise() {
      this.enterpriseSelectModal.visible = false
      this.enterpriseSelectModal.dataSource = []
      this.enterpriseSelectModal.selectedRowKeys = []
      this.enterpriseSelectModal.selectedEnterprise = null
    },
  },
}
</script>

<style scoped>
.ant-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  border-radius: 4px;
}

.ant-card-head {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.ant-form-model-item {
  margin-bottom: 16px;
}

.ant-input-group-compact {
  display: flex;
}

.ant-input-group-compact > .ant-input {
  border-radius: 4px 0 0 4px;
}

.ant-input-group-compact > .ant-btn {
  border-radius: 0 4px 4px 0;
}
</style>

