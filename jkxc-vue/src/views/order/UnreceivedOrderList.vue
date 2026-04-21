<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="订单编号">
              <JInput placeholder="请输入订单编号" v-model="queryParam.orderNo" type="like" />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like" />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务员">
              <j-search-select-tag
                placeholder="请输入业务员"
                v-model="queryParam.salesman"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
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

    <!-- table区域 -->
    <div>
      <a-table
        ref="table"
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
        <span slot="orderAmount" slot-scope="text">
          <span style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
        </span>
        <span slot="receivedAmount" slot-scope="text">
          <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(text) }}</span>
        </span>
        <span slot="unreceivedAmount" slot-scope="text">
          <span style="color: #faad14; font-weight: 600">¥{{ formatAmount(text) }}</span>
        </span>
        <span slot="action" slot-scope="text, record">
          <a @click="handlePayment(record)" style="margin-right: 8px">
            <a-icon type="dollar" />收款
          </a>
          <a @click="handleDetail(record)" style="margin-right: 8px">
            <a-icon type="eye" />费用详情
          </a>
        </span>
      </a-table>
    </div>

    <!-- 收款弹窗 -->
    <j-modal
      :visible.sync="paymentModal.visible"
      :width="800"
      title="收款"
      :fullscreen.sync="paymentModal.fullscreen"
      :switchFullscreen="paymentModal.switchFullscreen"
      @ok="handleConfirmPayment"
      @cancel="handleCancelPayment"
      :confirmLoading="paymentSubmitting"
    >
      <a-form-model ref="paymentForm" :model="paymentForm" :rules="paymentRules" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="订单编号">
          <a-input v-model="paymentForm.orderNo" disabled />
        </a-form-model-item>
        <a-form-model-item label="公司名称">
          <a-input v-model="paymentForm.companyName" disabled />
        </a-form-model-item>
        <a-form-model-item label="订单金额">
          <a-input v-model="paymentForm.orderAmount" disabled />
        </a-form-model-item>
        <a-form-model-item label="已收金额">
          <a-input v-model="paymentForm.receivedAmount" disabled />
        </a-form-model-item>
        <a-form-model-item label="未收金额">
          <a-input v-model="paymentForm.unreceivedAmount" disabled style="color: #faad14; font-weight: 600" />
        </a-form-model-item>
        <a-form-model-item label="收款金额" prop="amount" required>
          <a-input-number
            v-model="paymentForm.amount"
            :min="0.01"
            :max="parseFloat(paymentForm.unreceivedAmount || 0)"
            :precision="2"
            placeholder="请输入收款金额"
            style="width: 100%"
            @change="handleAmountChange"
          />
          <div style="color: #999; margin-top: 4px">最多可收：¥{{ paymentForm.unreceivedAmount || '0.00' }}</div>
        </a-form-model-item>
        <a-form-model-item label="收款时间" prop="paymentTime" required>
          <j-date v-model="paymentForm.paymentTime" :showTime="true" dateFormat="YYYY-MM-DD HH:mm:ss" placeholder="请选择收款时间" style="width: 100%" />
        </a-form-model-item>
        <a-form-model-item label="收款账户" prop="collectionAccountCascader" required>
          <a-cascader
            v-model="paymentForm.collectionAccountCascader"
            :options="collectionAccountOptions"
            placeholder="请选择收款方式/收款单位/人/网点名称"
            :loading="collectionAccountLoading"
            :change-on-select="false"
            :show-search="true"
            :field-names="{ label: 'label', value: 'value', children: 'children' }"
            @change="handleCollectionAccountChange"
            style="width: 100%"
            :getPopupContainer="(triggerNode) => triggerNode.parentNode"
            :popupStyle="{ minWidth: '600px', maxHeight: '400px' }"
          />
        </a-form-model-item>
        <a-form-model-item label="凭证附件" prop="imageVoucher" required>
          <j-image-upload v-model="paymentForm.imageVoucher" text="上传" :isMultiple="true" bizPath="payment"></j-image-upload>
          <div style="color: #999; margin-top: 4px">支持上传多张图片作为收款凭证（必填）</div>
        </a-form-model-item>
        <a-form-model-item label="备注" prop="remarks">
          <a-textarea v-model="paymentForm.remarks" :rows="4" placeholder="请输入备注" :maxLength="500" />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 费用详情弹窗 -->
    <j-modal
      :visible.sync="paymentDetailModal.visible"
      :width="1200"
      title="费用详情"
      :fullscreen.sync="paymentDetailModal.fullscreen"
      :switchFullscreen="paymentDetailModal.switchFullscreen"
    >
      <order-payment :orderId="paymentDetailModal.orderId" :companyName="paymentDetailModal.companyName"></order-payment>
    </j-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { mixinDevice } from '@/utils/mixin'
import { getAction, httpAction } from '@/api/manage'
import OrderPayment from './modules/OrderPayment'
import moment from 'moment'

export default {
  name: 'UnreceivedOrderList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    OrderPayment,
  },
  data() {
    return {
      description: '财务管理-未收订单',
      url: {
        list: '/order/listUnreceivedOrders',
      },
      queryParam: {
        orderNo: '',
        companyName: '',
        salesman: '',
      },
      columns: [
        {
          title: '订单编号',
          align: 'center',
          dataIndex: 'orderNo',
          fixed: 'left',
          width: 150,
        },
        {
          title: '公司名称',
          align: 'center',
          dataIndex: 'companyName',
          width: 200,
        },
        {
          title: '业务员',
          align: 'center',
          dataIndex: 'salesman',
          width: 120,
        },
        {
          title: '业务类型',
          align: 'center',
          dataIndex: 'businessType',
          customRender: (text, record) => record.businessType_dictText || text || '',
          width: 150,
        },
        {
          title: '订单金额',
          align: 'center',
          dataIndex: 'orderAmount',
          scopedSlots: { customRender: 'orderAmount' },
          width: 120,
        },
        {
          title: '已收金额',
          align: 'center',
          dataIndex: 'receivedAmount',
          scopedSlots: { customRender: 'receivedAmount' },
          width: 120,
        },
        {
          title: '未收金额',
          align: 'center',
          dataIndex: 'unreceivedAmount',
          scopedSlots: { customRender: 'unreceivedAmount' },
          width: 120,
        },
        {
          title: '创建时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 180,
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
      paymentModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
      },
      paymentDetailModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
        companyName: '',
      },
      paymentForm: {
        orderId: '',
        orderNo: '',
        companyName: '',
        orderAmount: '0.00',
        receivedAmount: '0.00',
        unreceivedAmount: '0.00',
        amount: null,
        paymentTime: moment().format('YYYY-MM-DD HH:mm:ss'),
        collectionAccountCascader: [],
        accountNumber: '',
        paymentMethod: '',
        imageVoucher: '',
        remarks: '',
      },
      paymentRules: {
        amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
        paymentTime: [{ required: true, message: '请选择收款时间', trigger: 'change' }],
        collectionAccountCascader: [
          { 
            required: true, 
            validator: (rule, value, callback) => {
              if (!value || !Array.isArray(value) || value.length !== 3) {
                callback(new Error('请选择收款账户'))
              } else {
                callback()
              }
            },
            trigger: 'change' 
          }
        ],
        imageVoucher: [
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
                callback(new Error('请上传凭证附件'))
              } else {
                callback()
              }
            },
            trigger: 'change' 
          }
        ],
      },
      paymentSubmitting: false,
      collectionAccountOptions: [], // 收款账户级联选项（三级：收款方式->收款单位/人->网点名称）
      collectionAccountLoading: false,
    }
  },
  methods: {
    getQueryParams() {
      const param = Object.assign({}, this.queryParam)
      return param
    },
    formatAmount(amount) {
      if (!amount && amount !== 0) return '0.00'
      const num = Number(amount)
      return isNaN(num) ? '0.00' : num.toFixed(2)
    },
    handlePayment(record) {
      // 获取订单金额（合同金额或订单金额）
      const orderAmount = parseFloat(record.contractAmount || record.orderAmount || 0)
      const receivedAmount = parseFloat(record.receivedAmount || 0)
      // 计算未收金额，确保不为负数
      const unreceivedAmount = Math.max(0, orderAmount - receivedAmount)
      
      this.paymentForm = {
        orderId: record.id,
        orderNo: record.orderNo || '',
        companyName: record.companyName || '',
        orderAmount: this.formatAmount(orderAmount),
        receivedAmount: this.formatAmount(receivedAmount),
        unreceivedAmount: this.formatAmount(unreceivedAmount),
        amount: null,
        paymentTime: moment().format('YYYY-MM-DD HH:mm:ss'),
        collectionAccountCascader: [],
        paymentMethod: '',
        accountNumber: '',
        imageVoucher: '',
        remarks: '',
      }
      this.paymentModal.orderId = record.id
      this.paymentModal.visible = true
      // 加载收款账户级联数据
      this.loadCollectionAccountOptions()
    },
    handleAmountChange(value) {
      // 确保收款金额不超过未收金额
      const maxAmount = parseFloat(this.paymentForm.unreceivedAmount || 0)
      if (value > maxAmount) {
        this.$message.warning(`收款金额不能超过未收金额（¥${this.paymentForm.unreceivedAmount}）`)
        this.paymentForm.amount = maxAmount
      }
      if (value < 0.01) {
        this.$message.warning('收款金额必须大于0.01')
        this.paymentForm.amount = 0.01
      }
    },
    handleConfirmPayment() {
      // 防重复提交
      if (this.paymentSubmitting) {
        this.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      this.$refs.paymentForm.validate((valid) => {
        if (valid) {
          // 验证收款金额
          if (!this.paymentForm.amount || this.paymentForm.amount <= 0) {
            this.$message.error('收款金额必须大于0')
            return
          }
          
          const maxAmount = parseFloat(this.paymentForm.unreceivedAmount || 0)
          if (this.paymentForm.amount > maxAmount) {
            this.$message.error(`收款金额不能超过未收金额（¥${this.paymentForm.unreceivedAmount}）`)
            return
          }
          
          // 验证收款账户（必填）
          if (!this.paymentForm.collectionAccountCascader || this.paymentForm.collectionAccountCascader.length !== 3) {
            this.$message.error('请选择收款账户')
            return
          }
          
          // 验证凭证附件（必填）
          let hasImageVoucher = false
          if (this.paymentForm.imageVoucher) {
            if (typeof this.paymentForm.imageVoucher === 'string') {
              // 字符串类型：可能是JSON数组字符串或逗号分隔的字符串
              const trimmed = this.paymentForm.imageVoucher.trim()
              if (trimmed && trimmed !== '[]' && trimmed !== 'null') {
                // 尝试解析为JSON数组
                if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                  try {
                    const parsed = JSON.parse(trimmed)
                    if (Array.isArray(parsed) && parsed.length > 0) {
                      hasImageVoucher = true
                    }
                  } catch (e) {
                    // JSON解析失败，检查是否为空数组字符串
                  }
                } else {
                  // 不是JSON数组，可能是逗号分隔的字符串或单个文件路径
                  if (trimmed.includes(',')) {
                    const items = trimmed.split(',').filter(item => item.trim())
                    if (items.length > 0) {
                      hasImageVoucher = true
                    }
                  } else {
                    // 单个文件路径
                    hasImageVoucher = true
                  }
                }
              }
            } else if (Array.isArray(this.paymentForm.imageVoucher)) {
              // 数组类型
              if (this.paymentForm.imageVoucher.length > 0) {
                hasImageVoucher = true
              }
            }
          }
          
          if (!hasImageVoucher) {
            this.$message.error('请上传凭证附件')
            return
          }
          
          // 设置提交中状态
          this.paymentSubmitting = true
          
          // 准备提交数据
          // 从级联选择器中提取收款方式和收款账号
          let paymentMethod = null
          let accountNumber = null
          
          if (this.paymentForm.collectionAccountCascader && this.paymentForm.collectionAccountCascader.length === 3) {
            // 三级级联：第一级是收款方式，第三级是账号ID
            paymentMethod = this.paymentForm.collectionAccountCascader[0]
            accountNumber = this.paymentForm.collectionAccountCascader[2] // 第三级是账号ID
          }
          
          const params = {
            orderId: this.paymentForm.orderId,
            amount: this.paymentForm.amount,
            paymentTime: this.paymentForm.paymentTime,
            paymentMethod: paymentMethod,
            accountNumber: accountNumber,
            imageVoucher: this.paymentForm.imageVoucher || null,
            remarks: this.paymentForm.remarks || null,
            status: '1', // 已确认
          }
          
          // 提交收款信息（后端会自动处理收款账号ID转换为收款账号字符串）
          this.submitPayment(params)
        }
      })
    },
    submitPayment(params) {
      httpAction('/order/addPayment', params, 'post').then((res) => {
        if (res.success) {
          this.$message.success('收款成功')
          this.handleCancelPayment()
          this.loadData()
        } else {
          this.$message.error(res.message || '收款失败')
        }
      }).catch((err) => {
        console.error('收款失败', err)
        this.$message.error('收款失败：' + (err.message || '网络错误'))
      }).finally(() => {
        this.paymentSubmitting = false
      })
    },
    handleCancelPayment() {
      this.paymentModal.visible = false
      this.paymentForm = {
        orderId: '',
        orderNo: '',
        companyName: '',
        orderAmount: '0.00',
        receivedAmount: '0.00',
        unreceivedAmount: '0.00',
        amount: null,
        paymentTime: moment().format('YYYY-MM-DD HH:mm:ss'),
        collectionAccountCascader: [],
        accountNumber: '',
        paymentMethod: '',
        imageVoucher: '',
        remarks: '',
      }
      this.paymentSubmitting = false
      this.$refs.paymentForm && this.$refs.paymentForm.resetFields()
    },
    loadCollectionAccountOptions() {
      // 加载所有收款账号，构建三级级联数据：收款方式 -> 收款单位/人 -> 网点名称
      this.collectionAccountLoading = true
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
          this.collectionAccountOptions = Object.keys(paymentMethodMap).map(paymentMethod => {
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
          this.collectionAccountOptions = []
        }
      }).catch(() => {
        this.collectionAccountOptions = []
        return Promise.resolve()
      }).finally(() => {
        this.collectionAccountLoading = false
      })
    },
    handleCollectionAccountChange(value, selectedOptions) {
      // 级联选择器变化时，保存账号ID和收款方式
      if (value && value.length === 3) {
        const accountId = value[2] // 第三级是账号ID
        const paymentMethod = value[0] // 第一级是收款方式
        this.paymentForm.accountNumber = accountId
        this.paymentForm.paymentMethod = paymentMethod
      } else {
        this.paymentForm.accountNumber = ''
        this.paymentForm.paymentMethod = ''
      }
    },
    handleDetail(record) {
      // 打开费用详情弹窗
      this.paymentDetailModal.orderId = record.id
      this.paymentDetailModal.companyName = record.companyName || ''
      this.paymentDetailModal.visible = true
    },
  },
}
</script>

<style scoped>
</style>

