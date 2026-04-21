<template>
  <j-modal
    title="客户关联"
    :width="800"
    :visible="visible"
    :switchFullscreen="switchFullscreen"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <a-form-model ref="linkForm" :model="linkForm" :rules="linkFormRules" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
      <a-form-model-item label="当前客户">
        <a-input v-model="linkForm.currentCustomer" disabled />
      </a-form-model-item>
      <a-form-model-item label="关联企业">
        <a-select
          ref="companySelect"
          placeholder="请选择要关联的公司（可多选，不能选择当前客户）"
          v-model="linkForm.relatedCompanyIds"
          mode="multiple"
          show-search
          :filter-option="false"
          :not-found-content="fetching ? undefined : null"
          @search="handleSearchCompany"
          @change="handleCompanyChange"
          allow-clear
        >
          <a-spin v-if="fetching" slot="notFoundContent" size="small" />
          <a-select-option v-for="company in companyOptions" :key="company.id" :value="company.id">
            {{ company.corporateName }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="已关联企业" v-if="relatedCompanies.length > 0">
        <a-table
          :columns="columns"
          :dataSource="relatedCompanies"
          :pagination="false"
          size="small"
          :scroll="{ y: 200 }"
        >
          <template slot="action" slot-scope="text, record">
            <a-popconfirm title="确定要取消关联吗？" @confirm="handleRemoveRelation(record.id)">
              <a-button type="link" danger size="small">取消关联</a-button>
            </a-popconfirm>
          </template>
        </a-table>
      </a-form-model-item>
    </a-form-model>
    <template slot="footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" @click="handleOk" :loading="confirmLoading">确定</a-button>
    </template>
  </j-modal>
</template>

<script>
import { putAction, getAction, deleteAction, postAction } from '@/api/manage'
import debounce from 'lodash/debounce'

export default {
  name: 'CustomerLinkModal',
  data() {
    return {
      visible: false,
      switchFullscreen: false,
      confirmLoading: false,
      customerRecord: {},
      linkForm: {
        currentCustomer: '',
        relatedCompanyIds: [],
      },
      linkFormRules: {},
      relatedCompanies: [],
      companyOptions: [], // 公司选项列表
      fetching: false, // 加载状态
      columns: [
        {
          title: '公司名称',
          dataIndex: 'corporateName',
          key: 'corporateName',
        },
        {
          title: '联系人',
          dataIndex: 'contacts',
          key: 'contacts',
        },
        {
          title: '联系电话',
          dataIndex: 'contactInformation',
          key: 'contactInformation',
        },
        {
          title: '操作',
          key: 'action',
          width: 100,
          align: 'center',
          scopedSlots: { customRender: 'action' },
        },
      ],
    }
  },
  created() {
    // 创建防抖的搜索方法
    this.handleSearchCompany = debounce(this.searchCompany, 500)
  },
  computed: {
    // 计算公司字典，排除当前客户
    companyDict() {
      const currentId = this.customerRecord.id || ''
      if (currentId) {
        return `gh_customer,corporate_name,id,del_flag=0 and id != '${currentId}'`
      }
      return 'gh_customer,corporate_name,id,del_flag=0'
    },
  },
  watch: {
    'linkForm.relatedCompanyId': {
      handler(newVal, oldVal) {
        if (newVal && newVal !== oldVal) {
          // 当选择公司ID后，自动加载公司名称
          this.loadRelatedCompanyName(newVal)
        } else if (!newVal) {
          this.linkForm.relatedCompanyName = ''
        }
      },
      immediate: false,
    },
  },
  methods: {
    show(record) {
      this.customerRecord = record
      this.visible = true
      this.linkForm.currentCustomer = record.corporateName || ''
      this.linkForm.relatedCompanyIds = []
      this.relatedCompanies = []
      this.companyOptions = []
      
      // 加载已关联的企业
      this.loadRelatedCompanies()
      // 初始加载公司列表
      this.searchCompany('')
    },
    // 搜索公司
    searchCompany(keyword) {
      this.fetching = true
      const params = {
        pageNo: 1,
        pageSize: 50,
        corporateName: keyword || ''
      }
      
      getAction('/customer/ghCustomer/list', params)
        .then((res) => {
          if (res.success && res.result && res.result.records) {
            // 过滤掉当前客户
            this.companyOptions = res.result.records.filter(item => item.id !== this.customerRecord.id)
            
            // 如果已选中的公司不在列表中，需要加载它们的信息
            if (this.linkForm.relatedCompanyIds && this.linkForm.relatedCompanyIds.length > 0) {
              const existingIds = this.companyOptions.map(c => c.id)
              const missingIds = this.linkForm.relatedCompanyIds.filter(id => !existingIds.includes(id))
              
              if (missingIds.length > 0) {
                // 将已关联的公司添加到选项中
                const relatedOptions = this.relatedCompanies.filter(c => missingIds.includes(c.id))
                this.companyOptions = [...this.companyOptions, ...relatedOptions]
              }
            }
          }
        })
        .catch((err) => {
          console.error('搜索公司失败', err)
        })
        .finally(() => {
          this.fetching = false
        })
    },
    loadRelatedCompanies() {
      if (!this.customerRecord.id) {
        return
      }
      
      getAction('/customer/ghCustomer/getRelatedCompanies', { customerId: this.customerRecord.id })
        .then((res) => {
          if (res.success && res.result) {
            this.relatedCompanies = res.result || []
            // 设置已选中的关联企业ID
            this.linkForm.relatedCompanyIds = this.relatedCompanies.map(item => item.id)
            
            // 将已关联的公司添加到选项列表中（确保它们可以显示）
            if (this.relatedCompanies.length > 0) {
              const existingIds = this.companyOptions.map(c => c.id)
              const missingCompanies = this.relatedCompanies.filter(c => !existingIds.includes(c.id))
              if (missingCompanies.length > 0) {
                this.companyOptions = [...this.companyOptions, ...missingCompanies]
              }
            }
          }
        })
        .catch((err) => {
          console.error('加载关联企业失败', err)
        })
    },
    handleCompanyChange(value) {
      if (value && Array.isArray(value)) {
        // 验证不能选择当前客户
        if (value.includes(this.customerRecord.id)) {
          this.$message.warning('不能关联当前客户自己！')
          this.linkForm.relatedCompanyIds = this.linkForm.relatedCompanyIds.filter(id => id !== this.customerRecord.id)
          return
        }
      }
    },
    handleRemoveRelation(relatedCustomerId) {
      deleteAction('/customer/ghCustomer/removeRelation', {
        customerId: this.customerRecord.id,
        relatedCustomerId: relatedCustomerId,
      })
        .then((res) => {
          if (res.success) {
            this.$message.success('取消关联成功！')
            // 从多选框中也移除该ID
            this.linkForm.relatedCompanyIds = this.linkForm.relatedCompanyIds.filter(id => id !== relatedCustomerId)
            // 重新加载关联企业列表
            this.loadRelatedCompanies()
            this.$emit('ok')
          } else {
            this.$message.error(res.message || '取消关联失败！')
          }
        })
        .catch((err) => {
          this.$message.error('取消关联失败：' + (err.message || '未知错误'))
        })
    },
    handleOk() {
      // 验证不能选择当前客户
      if (this.linkForm.relatedCompanyIds && this.linkForm.relatedCompanyIds.includes(this.customerRecord.id)) {
        this.$message.warning('不能关联当前客户自己！')
        return
      }
      
      this.submitLink()
    },
    submitLink() {
      this.confirmLoading = true
      
      // 使用批量设置接口
      const url = `/customer/ghCustomer/setRelations?customerId=${this.customerRecord.id}`
      postAction(url, this.linkForm.relatedCompanyIds || [])
        .then((res) => {
          if (res.success) {
            this.$message.success('关联成功！')
            this.handleCancel()
            this.$emit('ok')
          } else {
            this.$message.error(res.message || '关联失败！')
          }
        })
        .catch((err) => {
          this.$message.error('关联失败：' + (err.message || '未知错误'))
        })
        .finally(() => {
          this.confirmLoading = false
        })
    },
    handleCancel() {
      this.visible = false
      this.resetForm()
    },
    resetForm() {
      this.linkForm = {
        currentCustomer: '',
        relatedCompanyIds: [],
      }
      this.relatedCompanies = []
      this.companyOptions = []
      this.customerRecord = {}
      if (this.$refs.linkForm) {
        this.$refs.linkForm.resetFields()
      }
    },
  },
}
</script>

<style scoped>
</style>

