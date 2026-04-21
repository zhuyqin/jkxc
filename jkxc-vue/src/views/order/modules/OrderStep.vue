<template>
  <div>
    <a-spin :spinning="loading">
      <!-- 订单基本信息 -->
      <div style="font-size: 16px; color: rgba(0, 0, 0, 0.85); font-weight: 500; margin-bottom: 20px;">
        订单信息（{{ orderInfo.companyName || '加载中...' }}）
      </div>
      <a-row style="margin-bottom: 16px" v-if="orderInfo.orderNo">
        <a-col :xs="24" :sm="12" :md="12" :lg="12" :xl="6">
          <span style="color: rgba(0, 0, 0, 0.85)">订单编号：</span>
          {{ orderInfo.orderNo }}
        </a-col>
        <a-col :xs="24" :sm="12" :md="12" :lg="12" :xl="6">
          <span style="color: rgba(0, 0, 0, 0.85)">业务员：</span>
          {{ orderInfo.salesman }}
        </a-col>
        <a-col :xs="24" :sm="24" :md="24" :lg="24" :xl="12">
          <span style="color: rgba(0, 0, 0, 0.85)">创建时间：</span>
          {{ orderInfo.createTime }}
        </a-col>
      </a-row>

      <!-- 审核流程步骤 -->
      <a-divider>审核流程</a-divider>
      <a-steps 
        :current="currentStep" 
        :status="stepStatus" 
        :direction="directionType.horizontal"
        v-if="stepList && stepList.length > 0"
      >
        <a-step 
          v-for="(step, index) in stepList" 
          :key="index"
          :status="getStepStatus(step, index)"
        >
          <template slot="title">
            <div style="fontSize: 14px; position: relative; color: black">
              {{ getStepTitle(step) }}
            </div>
          </template>
          <template slot="description">
            <div style="fontSize: 12px; color: rgba(0, 0, 0, 0.45); position: relative;">
              <div style="margin: 8px 0 4px" v-if="step.operatorName && step.operatorName !== '待处理'">
                <span style="font-weight: 500; color: rgba(0, 0, 0, 0.65);">操作人：</span>{{ step.operatorName }}
              </div>
              <div style="margin: 4px 0" v-if="step.createTime && step.createTime !== '待处理'">
                <span style="font-weight: 500; color: rgba(0, 0, 0, 0.65);">时间：</span>{{ step.createTime }}
              </div>
              <div style="margin: 4px 0" v-if="step.remarks">
                <span style="font-weight: 500; color: rgba(0, 0, 0, 0.65);">备注：</span>{{ step.remarks }}
              </div>
              <div style="margin: 4px 0; color: #999;" v-if="step.status === '0' || (step.operatorName === '待处理' && step.createTime === '待处理')">
                待处理
              </div>
            </div>
          </template>
        </a-step>
      </a-steps>
      <a-empty v-else description="暂无流程信息" style="padding: 40px 0" />

      <!-- 操作日志表格 -->
      <a-card
        :bordered="false"
        :tabList="tabList"
        :activeTabKey="activeTabKey"
        @tabChange="(key) => {this.activeTabKey = key}"
        style="margin-top: 24px"
      >
        <a-table
          v-if="activeTabKey === '1'"
          :columns="operationColumns"
          :dataSource="stepList"
          :pagination="false"
          :loading="loading"
        >
          <template slot="stepType" slot-scope="text, record">
            {{ getStepTitle(record) }}
          </template>
          <template slot="status" slot-scope="status">
            <a-badge :status="getStatusType(status)" :text="getStatusText(status)"/>
          </template>
        </a-table>
      </a-card>
    </a-spin>
  </div>
</template>

<script>
import { mixinDevice } from '@/utils/mixin.js'
import { getAction } from '@api/manage'

const directionType = {
  horizontal: 'horizontal',
  vertical: 'vertical'
}

export default {
  name: 'OrderStep',
  components: {},
  props: {
    id: {
      type: String,
      default: '',
      required: true
    },
    operationType: {
      type: String,
      default: 'default',
      required: false
    }
  },
  mixins: [mixinDevice],
  data() {
    return {
      loading: false,
      status: 'finish',
      directionType,
      orderInfo: {},
      currentStep: 0,
      stepStatus: 'process',
      stepList: [],
      tabList: [
        {
          key: '1',
          tab: '操作日志'
        }
      ],
      activeTabKey: '1',
      operationColumns: [
        {
          title: '步骤顺序',
          dataIndex: 'stepOrder',
          key: 'stepOrder',
          width: 80,
          align: 'center'
        },
        {
          title: '步骤类型',
          dataIndex: 'stepType',
          key: 'stepType',
          scopedSlots: { customRender: 'stepType' },
          width: 120
        },
        {
          title: '操作人',
          dataIndex: 'operatorName',
          key: 'operatorName',
          width: 120,
          customRender: (text) => text || '待处理'
        },
        {
          title: '操作状态',
          dataIndex: 'status',
          key: 'status',
          scopedSlots: { customRender: 'status' },
          width: 100,
          align: 'center'
        },
        {
          title: '操作时间',
          dataIndex: 'createTime',
          key: 'createTime',
          width: 180,
          customRender: (text) => text || '待处理'
        },
        {
          title: '备注',
          dataIndex: 'remarks',
          key: 'remarks',
          ellipsis: true
        }
      ],
    }
  },
  created() {
    this.init()
  },
  watch: {
    id: {
      handler() {
        this.init()
      },
      immediate: true
    }
  },
  methods: {
    init() {
      if (!this.id) {
        return
      }
      this.loading = true
      // 加载订单信息和审核流程
      Promise.all([
        getAction('/order/queryById', { id: this.id }),
        getAction('/sys/auditTask/getOrderAuditProgress', { orderId: this.id })
      ]).then(([orderRes, progressRes]) => {
        if (orderRes.success) {
          this.orderInfo = orderRes.result || {}
        }
        if (progressRes.success && progressRes.result) {
          const progress = progressRes.result
          // 先添加订单创建步骤
          const createStep = {
            stepType: '0',
            stepOrder: 0,
            operatorName: this.orderInfo.createBy || '系统',
            createTime: this.orderInfo.createTime || '待处理',
            remarks: '订单创建',
            status: '1', // 已完成
            roleName: ''
          }
          this.stepList = [createStep]
          
          // 如果有审核步骤，按步骤顺序添加审核步骤
          if (progress.steps && progress.steps.length > 0) {
            const auditSteps = []
            progress.steps.forEach(stepInfo => {
              const stepOrder = stepInfo.stepOrder || 0
              const stepStatus = stepInfo.status || 'pending'
              const tasks = stepInfo.tasks || []
              
              // 如果有多个任务（并行审核），展开为多个步骤项
              if (tasks.length > 0) {
                tasks.forEach(task => {
                  // 过滤掉流失审核任务（taskType='loss'），避免重复显示
                  if (task.taskType === 'loss') {
                    return
                  }
                  
                  const roleName = task.roleName || ''
                  const taskStatus = task.taskStatus || 'pending'
                  const auditResult = task.auditResult
                  
                  // 根据任务状态确定状态码
                  let status = '0' // 待处理
                  if (auditResult === 'approved' || taskStatus === 'approved') {
                    status = '1' // 已完成
                  } else if (auditResult === 'rejected' || taskStatus === 'rejected') {
                    status = '3' // 已驳回
                  } else if (taskStatus === 'pending') {
                    status = '2' // 处理中
                  }
                  
                  // 确定操作人和时间
                  let operatorName = '待处理'
                  let createTime = '待处理'
                  let remarks = ''
                  
                  if (auditResult === 'approved' || taskStatus === 'approved') {
                    operatorName = task.auditUserName || '待处理'
                    createTime = task.auditTime ? this.formatDateTime(task.auditTime) : '待处理'
                    remarks = task.auditRemark || ''
                  } else if (auditResult === 'rejected' || taskStatus === 'rejected') {
                    operatorName = task.auditUserName || '待处理'
                    createTime = task.auditTime ? this.formatDateTime(task.auditTime) : '待处理'
                    remarks = task.auditRemark || task.rejectReason || ''
                  } else if (taskStatus === 'pending') {
                    operatorName = task.assignedUserName || '待处理'
                    createTime = '待处理'
                  }
                  
                  auditSteps.push({
                    stepType: this.getStepTypeByRole(roleName),
                    stepOrder: stepOrder,
                    operatorName: operatorName,
                    createTime: createTime,
                    remarks: remarks,
                    status: status,
                    roleName: roleName,
                    isParallel: tasks.length > 1, // 标记是否为并行步骤
                    parallelIndex: tasks.indexOf(task) // 并行索引
                  })
                })
              }
            })
            
            // 按stepOrder和parallelIndex排序
            auditSteps.sort((a, b) => {
              if (a.stepOrder !== b.stepOrder) {
                return (a.stepOrder || 0) - (b.stepOrder || 0)
              }
              return (a.parallelIndex || 0) - (b.parallelIndex || 0)
            })
            this.stepList = this.stepList.concat(auditSteps)
          }
          this.updateStepStatus()
        } else {
          // 如果审核进度接口失败，回退到订单步骤
          getAction('/order/getOrderSteps', { orderId: this.id }).then((stepsRes) => {
            if (stepsRes.success && stepsRes.result) {
              this.stepList = stepsRes.result || []
            } else {
              // 如果订单步骤也没有，至少显示订单创建步骤
              this.stepList = [{
                stepType: '0',
                stepOrder: 0,
                operatorName: this.orderInfo.createBy || '系统',
                createTime: this.orderInfo.createTime || '待处理',
                remarks: '订单创建',
                status: '1',
                roleName: ''
              }]
            }
            this.updateStepStatus()
          })
        }
      }).catch(err => {
        console.error('加载订单流程信息失败:', err)
        this.$message.error('加载流程信息失败')
        this.loading = false
      }).finally(() => {
        this.loading = false
      })
    },
    updateStepStatus() {
      if (!this.stepList || this.stepList.length === 0) {
        this.currentStep = 0
        this.stepStatus = 'wait'
        return
      }
      // 找到最后一个已完成的步骤
      let lastCompletedIndex = -1
      for (let i = 0; i < this.stepList.length; i++) {
        if (this.stepList[i].status === '1') {
          lastCompletedIndex = i
        }
      }
      
      // 设置当前步骤为最后一个已完成步骤的下一个
      if (lastCompletedIndex >= 0 && lastCompletedIndex < this.stepList.length - 1) {
        this.currentStep = lastCompletedIndex + 1
        this.stepStatus = 'process'
      } else if (lastCompletedIndex === this.stepList.length - 1) {
        // 所有步骤都已完成
        this.currentStep = this.stepList.length - 1
        this.stepStatus = 'finish'
      } else {
        // 第一个步骤都未完成
        this.currentStep = 0
        this.stepStatus = 'wait'
      }
    },
    getStepStatus(step, index) {
      // 根据步骤状态返回对应的步骤状态
      if (step.status === '1') {
        // 已完成
        return 'finish'
      } else if (step.status === '3') {
        // 已驳回
        return 'error'
      } else if (step.status === '2') {
        // 处理中
        return 'process'
      } else {
        // 待处理
        return 'wait'
      }
    },
    getStepTitle(step) {
      // 如果是审核记录，使用角色名称
      if (step.roleName) {
        let title = step.roleName + '审核'
        // 如果是并行审核，在标题中标注
        if (step.isParallel) {
          title = `步骤${step.stepOrder}：${title}`
        }
        return title
      }
      const stepTypeMap = {
        '0': '订单创建',
        '1': '经理审核',
        '2': '出纳审核',
        '3': '任务分配',
        '4': '任务处理',
        '5': '任务完成',
        '6': '订单完成',
        '7': '订单驳回'
      }
      return stepTypeMap[step.stepType] || '未知步骤'
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      // 如果是字符串，直接返回
      if (typeof dateTime === 'string') {
        return dateTime
      }
      // 如果是Date对象，格式化
      if (dateTime instanceof Date) {
        const year = dateTime.getFullYear()
        const month = String(dateTime.getMonth() + 1).padStart(2, '0')
        const day = String(dateTime.getDate()).padStart(2, '0')
        const hours = String(dateTime.getHours()).padStart(2, '0')
        const minutes = String(dateTime.getMinutes()).padStart(2, '0')
        const seconds = String(dateTime.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      return String(dateTime)
    },
    getStepTypeByRole(roleName) {
      // 根据角色名称映射步骤类型
      if (roleName && roleName.includes('经理')) {
        return '1'
      } else if (roleName && roleName.includes('出纳')) {
        return '2'
      }
      return '1' // 默认经理审核
    },
    getStatusByAuditStatus(auditStatus) {
      // 将审核状态转换为步骤状态
      if (auditStatus === 'approved') {
        return '1' // 已完成
      } else if (auditStatus === 'rejected') {
        return '3' // 已驳回
      } else {
        return '0' // 待处理
      }
    },
    getStatusText(status) {
      const statusMap = {
        '0': '待处理',
        '1': '已完成',
        '2': '处理中',
        '3': '已驳回',
        '4': '已取消'
      }
      return statusMap[status] || '未知'
    },
    getStatusType(status) {
      const statusTypeMap = {
        '0': 'default',
        '1': 'success',
        '2': 'processing',
        '3': 'error',
        '4': 'warning'
      }
      return statusTypeMap[status] || 'default'
    }
  }
}
</script>

<style scoped>
</style>

