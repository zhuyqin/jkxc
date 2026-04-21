<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="创建日期">
              <j-date
                placeholder="请选择创建日期"
                v-model="queryParam.createDate"
                style="width: 100%"
                :show-time="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput
                placeholder="请输入公司名称"
                v-model="queryParam.companyName"
                type="like"
                allowClear
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务类型">
              <j-dict-select-tag
                placeholder="请选择业务类型"
                v-model="queryParam.businessType"
                dictCode="business_type"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24" v-if="false">
            <a-form-item label="银行账户">
              <j-search-select-tag
                placeholder="请选择银行账户"
                v-model="queryParam.bankAccountId"
                :dict="'gh_bank_management~CONCAT_WS(\'--\',payee_person,account_notes)~id~del_flag=0'"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator" style="margin-top: 16px;">
      <a-button @click="handleAdd" type="primary" icon="plus">新增银行日记</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls" style="margin-left: 8px">导出</a-button>
    </div>

    <!-- 三级分类Tab展示 -->
    <div style="margin-top: 16px;">
      <!-- 第一级Tab：按收款方式分类 -->
      <a-tabs 
        v-model="activePaymentMethod" 
        @change="handlePaymentMethodChange"
        type="card"
        :destroyInactiveTabPane="true"
        style="margin-bottom: 16px;"
      >
        <a-tab-pane 
          v-for="paymentMethod in paymentMethodList" 
          :key="paymentMethod.value" 
          :tab="paymentMethod.label"
        >
          <!-- 第二级Tab：按收款单位/人分类 -->
          <a-tabs 
            v-model="activePayeePerson[paymentMethod.value]"
            @change="(key) => handlePayeePersonChange(paymentMethod.value, key)"
            type="card"
            :destroyInactiveTabPane="true"
            style="margin-bottom: 16px;"
          >
            <a-tab-pane 
              v-for="payeePerson in getPayeePersonList(paymentMethod.value)" 
              :key="payeePerson.value" 
              :tab="payeePerson.label"
            >
              <!-- 第三级Tab：按网点名称分类（银行账户） -->
              <a-tabs 
                v-model="activeBankAccount[payeePerson.value]"
                @change="(key) => handleBankAccountChange(key)"
                type="editable-card"
                :destroyInactiveTabPane="true"
              >
                <a-tab-pane 
                  v-for="bankAccount in getBankAccountList(paymentMethod.value, payeePerson.value)" 
                  :key="bankAccount.id" 
                  :tab="bankAccount.displayName"
                >
                  <!-- tab页仅用于选择账户；表格只渲染一份（在Tab区域下方） -->
                  <div />
                </a-tab-pane>
              </a-tabs>
            </a-tab-pane>
          </a-tabs>
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 银行日记表格（只渲染一份，避免Tab切换时创建大量Table实例导致卡顿） -->
    <a-table
      size="middle"
      :scroll="{ x: true }"
      bordered
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="createTime" slot-scope="text">
        {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
      </span>
      <span slot="incomeAmount" slot-scope="text">
        <span v-if="text && parseFloat(text) > 0" style="color: #52c41a; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="expenseAmount" slot-scope="text">
        <span v-if="text && parseFloat(text) > 0" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="balanceAmount" slot-scope="text">
        <span v-if="text" style="color: #1890ff; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="action" slot-scope="text, record">
        <a @click="handleDetail(record)">详情</a>
        <a-divider type="vertical" />
        <a @click="handleEdit(record)">编辑</a>
        <a-divider type="vertical" />
        <a v-if="record.incomeAmount && parseFloat(record.incomeAmount) > 0" @click="handleTransfer(record)">资金转移</a>
        <template v-if="record.incomeAmount && parseFloat(record.incomeAmount) > 0">
          <a-divider type="vertical" />
        </template>
        <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
          <a>删除</a>
        </a-popconfirm>
      </span>
    </a-table>

    <!-- 银行日记表单弹窗 -->
    <bank-diary-modal ref="modalForm" @ok="modalFormOk"></bank-diary-modal>
    
    <!-- 银行日记详情弹窗 -->
    <bank-diary-detail-modal ref="detailModal"></bank-diary-detail-modal>
    
    <!-- 资金转移弹窗 -->
    <j-modal
      :visible.sync="transferModal.visible"
      :width="600"
      title="资金转移"
      :confirmLoading="transferModal.loading"
      @ok="confirmTransfer"
      @cancel="handleTransferCancel"
    >
      <a-spin :spinning="transferModal.loading">
        <a-alert
          message="资金转移提示"
          description="转移后，将在源账户创建一条支出记录（扣除金额），在目标账户创建一条收入记录（增加金额）。"
          type="warning"
          show-icon
          style="margin-bottom: 16px;"
        />
        <a-form-model :model="transferModal" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-form-model-item label="源账户">
            <span>{{ transferModal.sourceAccountName || '--' }}</span>
          </a-form-model-item>
          <a-form-model-item label="转移金额">
            <span style="color: #f5222d; font-weight: 600; font-size: 16px;">
              ¥{{ formatAmount(transferModal.amount) }}
            </span>
          </a-form-model-item>
          <a-form-model-item label="目标账户" required>
            <a-cascader
              v-model="transferModal.targetAccountCascader"
              :options="transferAccountOptions"
              placeholder="请选择收款方式/收款单位/人/网点名称"
              :loading="transferAccountLoading"
              :change-on-select="false"
              :show-search="true"
              :field-names="{ label: 'label', value: 'value', children: 'children' }"
              @change="handleTransferAccountChange"
              style="width: 100%"
              :disabled="transferModal.loading"
              :getPopupContainer="(triggerNode) => triggerNode.parentNode"
              :popupStyle="{ minWidth: '600px', maxHeight: '400px' }"
            />
          </a-form-model-item>
          <a-form-model-item label="备注">
            <a-textarea
              v-model="transferModal.remarks"
              :rows="3"
              placeholder="请输入备注信息（可选）"
              :maxLength="500"
              :disabled="transferModal.loading"
            />
          </a-form-model-item>
        </a-form-model>
      </a-spin>
    </j-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import BankDiaryModal from './modules/BankDiaryModal'
import BankDiaryDetailModal from './modules/BankDiaryDetailModal'
import { getAction, postAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'BankDiaryList',
  mixins: [JeecgListMixin],
  components: {
    BankDiaryModal,
    BankDiaryDetailModal,
  },
  data() {
    return {
      description: '银行日记管理页面',
      queryParam: {
        createDate: '',
        companyName: '',
        businessType: '',
        bankAccountId: '',
      },
      // 三级分类数据结构
      bankAccountTree: {}, // {paymentMethod: {payeePerson: [bankAccounts]}}
      paymentMethodList: [], // [{label, value}]
      activePaymentMethod: null, // 当前选中的收款方式
      activePayeePerson: {}, // {paymentMethod: payeePerson}
      activeBankAccount: {}, // {payeePerson: bankAccountId}
      loadingBankAccounts: false,
      currentQueryAccountId: null, // 当前已查询的账户ID，用于判断是否需要重新查询
      isInitializing: true, // 是否正在初始化，初始化时不触发查询
      // 资金转移弹窗
      transferModal: {
        visible: false,
        loading: false,
        sourceId: '',
        sourceAccountName: '',
        amount: '',
        targetBankAccountId: '',
        targetAccountCascader: [],
        remarks: '',
      },
      // 资金转移目标账户级联选项
      transferAccountOptions: [],
      transferAccountLoading: false,
      url: {
        list: '/bankDiary/ghBankDiary/list',
        delete: '/bankDiary/ghBankDiary/delete',
        deleteBatch: '/bankDiary/ghBankDiary/deleteBatch',
        exportXlsUrl: '/bankDiary/ghBankDiary/exportXls',
        importExcelUrl: '/bankDiary/ghBankDiary/importExcel',
      },
      columns: [
        {
          title: '序号',
          align: 'center',
          dataIndex: '',
          width: 60,
          customRender: (text, record, index) => {
            return (this.ipagination.current - 1) * this.ipagination.pageSize + index + 1
          },
        },
        {
          title: '创建日期',
          align: 'center',
          dataIndex: 'createTime',
          width: 120,
          scopedSlots: { customRender: 'createTime' },
        },
        {
          title: '业务类型',
          align: 'center',
          dataIndex: 'category',
          width: 120,
          customRender: (text, record) => {
            // 如果在报销类目中，显示中文名称；否则显示原始值
            return record.category_dictText || text || '-'
          },
        },
        {
          title: '费用详情',
          align: 'center',
          dataIndex: 'feeDetail',
          width: 200,
        },
        {
          title: '收入金额',
          align: 'center',
          dataIndex: 'incomeAmount',
          width: 120,
          scopedSlots: { customRender: 'incomeAmount' },
        },
        {
          title: '支出金额',
          align: 'center',
          dataIndex: 'expenseAmount',
          width: 120,
          scopedSlots: { customRender: 'expenseAmount' },
        },
        {
          title: '结余金额',
          align: 'center',
          dataIndex: 'balanceAmount',
          width: 120,
          scopedSlots: { customRender: 'balanceAmount' },
        },
        {
          title: '银行账户',
          align: 'center',
          dataIndex: 'bankAccountId',
          width: 200,
          customRender: (text, record) => {
            return record.bankAccountName || record.bankAccountId_dictText || text || '-'
          },
        },
        {
          title: '操作人员',
          align: 'center',
          dataIndex: 'operatorId',
          width: 120,
          customRender: (text, record) => {
            return record.operatorName || record.operatorId_dictText || text || '-'
          },
        },
        {
          title: '备注信息',
          align: 'center',
          dataIndex: 'remarks',
          width: 200,
        },
        {
          title: '操作',
          align: 'center',
          dataIndex: 'action',
          fixed: 'right',
          width: 150,
          scopedSlots: { customRender: 'action' },
        },
      ],
    }
  },
  created() {
    // 重写created，不自动加载数据（等待银行账户树加载完成后再加载）
  },
  mounted() {
    // 在mounted中加载银行账户树，确保mixin已经初始化完成
    this.loadBankAccountTree()
  },
  methods: {
    // 加载银行账户三级分类树
    loadBankAccountTree() {
      this.loadingBankAccounts = true
      getAction('/bank/ghBankManagement/listAll').then((res) => {
        if (res.success && res.result) {
          const accounts = Array.isArray(res.result) ? res.result : []
          
          // 构建三级分类树结构
          const tree = {}
          accounts.forEach(acc => {
            if (!acc.paymentMethod || !acc.payeePerson) {
              return
            }
            
            if (!tree[acc.paymentMethod]) {
              tree[acc.paymentMethod] = {}
            }
            
            if (!tree[acc.paymentMethod][acc.payeePerson]) {
              tree[acc.paymentMethod][acc.payeePerson] = []
            }
            
            // 构建显示名称：优先使用网点名称，如果为空或重复则添加收款账户
            let displayName = acc.accountNotes || '（无网点名称）'
            // 如果网点名称为空，使用收款账户作为显示名称
            if (!acc.accountNotes && acc.collectionAccount) {
              displayName = acc.collectionAccount
            }
            
            tree[acc.paymentMethod][acc.payeePerson].push({
              id: acc.id,
              accountName: acc.accountNotes || '（无网点名称）',
              displayName: displayName,
              payeePerson: acc.payeePerson,
              paymentMethod: acc.paymentMethod,
              collectionAccount: acc.collectionAccount || '',
            })
          })
          
          this.bankAccountTree = tree
          
          // 构建第一级tab列表（收款方式）
          this.paymentMethodList = Object.keys(tree).map(paymentMethod => ({
            label: paymentMethod,
            value: paymentMethod
          }))
          
          // 如果资金转移账户选项未加载，现在构建（复用 bankAccountTree）
          if (this.transferAccountOptions.length === 0) {
            this.buildTransferAccountOptions()
          }
          
          // 初始化第一个收款方式（默认选中，并在初始化完成后触发查询）
          if (this.paymentMethodList.length > 0 && !this.activePaymentMethod) {
            this.isInitializing = true // 标记正在初始化
            this.$set(this, 'activePaymentMethod', this.paymentMethodList[0].value)
            this.$nextTick(() => {
              // 初始化tab选中状态
              this.initPayeePersonTabs(this.activePaymentMethod)
              // 初始化完成后，触发默认选中最后一级第一个tab的查询
              this.$nextTick(() => {
                this.isInitializing = false
                // 获取默认选中的最后一级tab（银行账户）
                const payeePersonKey = this.activePayeePerson[this.activePaymentMethod]
                if (payeePersonKey) {
                  const bankAccountId = this.activeBankAccount[payeePersonKey]
                  if (bankAccountId) {
                    // 触发查询
                    this.loadBankDiaryByAccount(String(bankAccountId))
                  }
                }
              })
            })
          } else {
            this.isInitializing = false
          }
        } else {
          this.bankAccountTree = {}
          this.paymentMethodList = []
        }
      }).catch(() => {
        this.bankAccountTree = {}
        this.paymentMethodList = []
      }).finally(() => {
        this.loadingBankAccounts = false
      })
    },
    // 获取收款单位/人列表
    getPayeePersonList(paymentMethod) {
      if (!this.bankAccountTree[paymentMethod]) {
        return []
      }
      return Object.keys(this.bankAccountTree[paymentMethod]).map(payeePerson => ({
        label: payeePerson,
        value: `${paymentMethod}__${payeePerson}`
      }))
    },
    // 获取银行账户列表
    getBankAccountList(paymentMethod, payeePersonKey) {
      const payeePerson = payeePersonKey.split('__')[1]
      if (!this.bankAccountTree[paymentMethod] || !this.bankAccountTree[paymentMethod][payeePerson]) {
        return []
      }
      return this.bankAccountTree[paymentMethod][payeePerson] || []
    },
    // 初始化收款单位/人tab（不触发查询）
    initPayeePersonTabs(paymentMethod) {
      const payeePersonList = this.getPayeePersonList(paymentMethod)
      if (payeePersonList.length > 0 && !this.activePayeePerson[paymentMethod]) {
        this.$set(this.activePayeePerson, paymentMethod, payeePersonList[0].value)
        const bankAccountList = this.getBankAccountList(paymentMethod, payeePersonList[0].value)
        // 默认选中第一个银行账户，但不触发查询
        if (bankAccountList.length > 0 && !this.activeBankAccount[payeePersonList[0].value]) {
          this.$set(this.activeBankAccount, payeePersonList[0].value, bankAccountList[0].id)
          // 不自动查询，等待用户点击最后一级tab时才查询
        }
      }
    },
    // 收款方式tab切换（切换后默认选中第一个最后一级tab并触发查询）
    handlePaymentMethodChange(paymentMethod) {
      this.activePaymentMethod = paymentMethod
      // 清空当前查询的账户ID，确保切换到最后一级tab时会触发查询
      this.currentQueryAccountId = null
      // 初始化tab选中状态
      this.initPayeePersonTabs(paymentMethod)
      // 切换后，默认选中第一个最后一级tab并触发查询
      this.$nextTick(() => {
        const payeePersonKey = this.activePayeePerson[paymentMethod]
        if (payeePersonKey) {
          const bankAccountId = this.activeBankAccount[payeePersonKey]
          if (bankAccountId) {
            // 触发查询
            this.loadBankDiaryByAccount(String(bankAccountId))
          }
        }
      })
    },
    // 收款单位/人tab切换（切换后默认选中第一个最后一级tab并触发查询）
    handlePayeePersonChange(paymentMethod, payeePersonKey) {
      this.$set(this.activePayeePerson, paymentMethod, payeePersonKey)
      // 清空当前查询的账户ID，确保切换到最后一级tab时会触发查询
      this.currentQueryAccountId = null
      const bankAccountList = this.getBankAccountList(paymentMethod, payeePersonKey)
      // 默认选中第一个银行账户
      if (bankAccountList.length > 0 && !this.activeBankAccount[payeePersonKey]) {
        this.$set(this.activeBankAccount, payeePersonKey, bankAccountList[0].id)
      }
      // 切换后，默认选中第一个最后一级tab并触发查询
      this.$nextTick(() => {
        const bankAccountId = this.activeBankAccount[payeePersonKey]
        if (bankAccountId) {
          // 触发查询
          this.loadBankDiaryByAccount(String(bankAccountId))
        }
      })
    },
    // 银行账户tab切换（最后一级，触发查询）
    handleBankAccountChange(bankAccountId) {
      // 确保传递的是字符串类型的ID
      if (!bankAccountId) {
        return
      }
      const accountIdStr = String(bankAccountId)
      
      // 如果正在初始化，不触发查询
      if (this.isInitializing) {
        return
      }
      
      // 如果切换到的账户与当前已查询的账户相同，不重复查询
      if (this.currentQueryAccountId === accountIdStr) {
        return
      }
      
      // 只有切换到最后一级tab且账户ID变化时才触发查询
      this.loadBankDiaryByAccount(accountIdStr)
    },
    // 根据银行账户ID加载银行日记
    loadBankDiaryByAccount(bankAccountId) {
      if (!bankAccountId) {
        return
      }
      // 记录当前查询的账户ID
      this.currentQueryAccountId = String(bankAccountId)
      // 重置分页到第一页
      this.ipagination.current = 1
      // 设置查询参数中的银行账户ID，清空其他可能冲突的查询条件
      this.queryParam.bankAccountId = bankAccountId
      // 重新加载数据
      this.loadData(1)
    },
    // 新增银行日记
    handleAdd() {
      this.$refs.modalForm.add()
    },
    // 编辑银行日记
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
    },
    // 查看详情
    handleDetail(record) {
      this.$refs.detailModal.show(record)
    },
    // 资金转移
    handleTransfer(record) {
      if (!record.incomeAmount || parseFloat(record.incomeAmount) <= 0) {
        this.$message.warning('该记录没有收入金额，无法转移')
        return
      }
      this.transferModal.sourceId = record.id
      this.transferModal.sourceAccountName = record.bankAccountName || record.bankAccountId_dictText || record.bankAccountId || '--'
      this.transferModal.amount = record.incomeAmount
      this.transferModal.targetBankAccountId = ''
      this.transferModal.targetAccountCascader = []
      this.transferModal.remarks = ''
      this.transferModal.loading = false
      this.transferModal.visible = true
      // 如果账户选项未加载，则加载；否则复用已有数据
      if (this.transferAccountOptions.length === 0) {
        this.loadTransferAccountOptions()
      } else {
        // 复用已有数据，直接构建级联选项
        this.buildTransferAccountOptions()
      }
    },
    // 从已有的 bankAccountTree 构建资金转移账户选项（复用数据，避免重复加载）
    buildTransferAccountOptions() {
      if (!this.bankAccountTree || Object.keys(this.bankAccountTree).length === 0) {
        // 如果 bankAccountTree 还未加载，则加载
        this.loadTransferAccountOptions()
        return
      }
      
      // 从已有的 bankAccountTree 构建级联选项
      this.transferAccountOptions = Object.keys(this.bankAccountTree).map(paymentMethod => {
        const payeePersonMap = this.bankAccountTree[paymentMethod]
        const payeePersonList = Object.keys(payeePersonMap).map(payeePerson => ({
          label: payeePerson,
          value: `${paymentMethod}__${payeePerson}`,
          children: payeePersonMap[payeePerson].map(acc => ({
            label: acc.displayName || acc.accountName || '（无网点名称）',
            value: acc.id,
            accountId: acc.id,
          }))
        }))
        
        return {
          label: paymentMethod,
          value: paymentMethod,
          children: payeePersonList
        }
      })
    },
    // 加载资金转移目标账户选项（三级级联）- 仅在 bankAccountTree 未加载时使用
    loadTransferAccountOptions() {
      // 如果 bankAccountTree 已加载，直接复用
      if (this.bankAccountTree && Object.keys(this.bankAccountTree).length > 0) {
        this.buildTransferAccountOptions()
        return
      }
      
      this.transferAccountLoading = true
      getAction('/bank/ghBankManagement/listAll').then((res) => {
        if (res.success && res.result) {
          const accounts = Array.isArray(res.result) ? res.result : []
          
          // 按收款方式分组
          const paymentMethodMap = {}
          accounts.forEach(acc => {
            // 只要求有paymentMethod和payeePerson
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
            })
          })
          
          // 构建级联数据结构
          this.transferAccountOptions = Object.keys(paymentMethodMap).map(paymentMethod => {
            const payeePersonMap = paymentMethodMap[paymentMethod]
            const payeePersonList = Object.keys(payeePersonMap).map(payeePerson => ({
              label: payeePerson,
              value: `${paymentMethod}__${payeePerson}`,
              children: payeePersonMap[payeePerson].map(acc => ({
                label: acc.accountNotes || '（无网点名称）',
                value: acc.id,
                accountId: acc.id,
              }))
            }))
            
            return {
              label: paymentMethod,
              value: paymentMethod,
              children: payeePersonList
            }
          })
        } else {
          this.transferAccountOptions = []
        }
      }).catch(() => {
        this.transferAccountOptions = []
      }).finally(() => {
        this.transferAccountLoading = false
      })
    },
    // 资金转移目标账户级联选择器变化
    handleTransferAccountChange(value, selectedOptions) {
      this.$set(this.transferModal, 'targetAccountCascader', value)
      
      // 级联选择器变化时，保存账号ID
      if (value && value.length === 3) {
        const accountId = value[2] // 第三级是账号ID
        this.transferModal.targetBankAccountId = accountId
      } else {
        this.transferModal.targetBankAccountId = ''
      }
    },
    // 确认资金转移
    confirmTransfer() {
      if (!this.transferModal.targetBankAccountId || !this.transferModal.targetAccountCascader || this.transferModal.targetAccountCascader.length !== 3) {
        this.$message.warning('请完整选择目标账户（收款方式/收款单位/人/网点名称）')
        return
      }
      
      // 确认对话框
      const that = this
      this.$confirm({
        title: '确认资金转移',
        content: `确定要将 ¥${this.formatAmount(this.transferModal.amount)} 从源账户转移到目标账户吗？`,
        okText: '确认转移',
        okType: 'primary',
        cancelText: '取消',
        onOk() {
          that.transferModal.loading = true
          postAction('/bankDiary/ghBankDiary/transfer', {
            sourceId: that.transferModal.sourceId,
            targetBankAccountId: that.transferModal.targetBankAccountId,
            remarks: that.transferModal.remarks || '',
          }).then((res) => {
            if (res.success) {
              that.$message.success('资金转移成功！')
              that.transferModal.visible = false
              that.transferModal.remarks = ''
              that.loadData()
            } else {
              that.$message.error(res.message || '资金转移失败')
            }
          }).catch((err) => {
            console.error('资金转移失败:', err)
            that.$message.error('资金转移失败：' + (err.message || '未知错误'))
          }).finally(() => {
            that.transferModal.loading = false
          })
        }
      })
    },
    // 取消资金转移
    handleTransferCancel() {
      this.transferModal.visible = false
      this.transferModal.remarks = ''
      this.transferModal.targetAccountCascader = []
      this.transferModal.targetBankAccountId = ''
      this.transferModal.loading = false
    },
    // 表单提交成功回调
    modalFormOk() {
      this.loadData()
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    moment,
  },
}
</script>

<style scoped>
@import '~@/assets/less/common.less';

/* 确保表格文字可见 */
::v-deep .ant-table {
  color: rgba(0, 0, 0, 0.85);
}

::v-deep .ant-table-tbody > tr > td {
  color: rgba(0, 0, 0, 0.85);
}

::v-deep .ant-table-thead > tr > th {
  color: rgba(0, 0, 0, 0.85);
  background: #fafafa;
}

::v-deep .ant-table-tbody > tr:hover > td {
  background: #f5f5f5;
}
</style>

