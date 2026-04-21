<template>
  <a-card :bordered="false">
    <!-- 表格区域 -->
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
      <span slot="orderNo" slot-scope="text, record">
        <a @click="handleViewOrder(record.orderId)">{{ text || '-' }}</a>
      </span>
      <span slot="companyName" slot-scope="text">
        <a-tooltip v-if="text" :title="text">
          <span>{{ text.length > 20 ? text.substring(0, 20) + '...' : text }}</span>
        </a-tooltip>
        <span v-else>-</span>
      </span>
      <span slot="currentRoleName" slot-scope="text, record">
        <template v-if="record.roleNames && record.roleNames.length > 0">
          <a-tag v-for="(roleName, index) in record.roleNames" :key="index" color="blue" style="margin-right: 4px;">
            {{ roleName }}
          </a-tag>
        </template>
        <a-tag v-else color="blue">{{ text || '-' }}</a-tag>
      </span>
      <span slot="contractNo" slot-scope="text">
        {{ text || '-' }}
      </span>
      <span slot="stepOrder" slot-scope="text, record">
        <!-- 流失审核是反向流程，需要反转显示：第3步显示为第1步 -->
        <a-tag color="orange" style="cursor: pointer;" @click="handleViewAuditProgressByRecord(record)">
          第{{ getReversedStepOrder(text, record) }}步
        </a-tag>
      </span>
      <span slot="createTime" slot-scope="text">
        {{ text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : '-' }}
      </span>
      <span slot="action" slot-scope="text, record">
        <a-button
          type="link"
          size="small"
          @click="handleAudit(record)"
          v-if="record.taskStatus === 'pending' && canAudit(record)"
        >
          <a-icon type="audit" />审核
        </a-button>
        <a-tag
          v-else-if="record.taskStatus !== 'pending'"
          :color="record.taskStatus === 'approved' ? 'green' : 'red'"
        >
          {{ record.taskStatus === 'approved' ? '已审核' : '已驳回' }}
        </a-tag>
        <span v-else style="color: #999;">无权限</span>
      </span>
    </a-table>

    <!-- 审核弹窗 -->
    <j-modal
      :visible.sync="auditModal.visible"
      :width="800"
      title="流失审核"
      @ok="handleAuditOk"
      @cancel="handleAuditCancel"
      :confirmLoading="auditModal.loading"
    >
      <a-form-model
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        :labelCol="{ span: 5 }"
        :wrapperCol="{ span: 18 }"
      >
        <a-form-model-item label="订单编号">
          <span style="font-weight: 600">
            {{ currentRecord.orderNo || '-' }}
          </span>
        </a-form-model-item>
        <a-form-model-item label="公司名称">
          {{ currentRecord.companyName || '-' }}
        </a-form-model-item>
        <a-form-model-item label="当前审核角色">
          <template v-if="currentRecord.roleNames && currentRecord.roleNames.length > 0">
            <a-tag v-for="(roleName, index) in currentRecord.roleNames" :key="index" color="blue" style="margin-right: 4px;">
              {{ roleName }}
            </a-tag>
          </template>
          <a-tag v-else color="blue">{{ currentRecord.currentRoleName || '-' }}</a-tag>
        </a-form-model-item>
        <a-form-model-item label="审核步骤">
          <a-tag color="orange" style="cursor: pointer;" @click="handleViewAuditProgress">
            第{{ getReversedStepOrder(currentRecord.stepOrder, currentRecord) }}步
          </a-tag>
          <a-button type="link" size="small" @click="handleViewAuditProgress" style="margin-left: 8px;">
            查看审核流程
          </a-button>
        </a-form-model-item>
        <a-form-model-item label="流失原因" v-if="currentRecord && currentRecord.lossReasons">
          <div v-if="currentRecord.lossReasons.lossType">
            <a-tag :color="currentRecord.lossReasons.lossType === 'abnormal' ? 'red' : 'orange'" style="margin-bottom: 8px;">
              {{ currentRecord.lossReasons.lossType === 'abnormal' ? '非正常流失' : '正常流失' }}
            </a-tag>
          </div>
          <div v-if="currentRecord.lossReasons.normalReasons && currentRecord.lossReasons.normalReasons.length > 0" style="margin-top: 8px;">
            <span style="font-weight: 600; margin-right: 8px;">正常流失原因：</span>
            <a-tag v-for="(reason, idx) in currentRecord.lossReasons.normalReasons" :key="idx" color="blue" style="margin-bottom: 4px;">
              {{ getLossReasonText(reason, 'normal') }}
            </a-tag>
          </div>
          <div v-if="currentRecord.lossReasons.abnormalReasons && currentRecord.lossReasons.abnormalReasons.length > 0" style="margin-top: 8px;">
            <span style="font-weight: 600; margin-right: 8px;">非正常流失原因：</span>
            <a-tag v-for="(reason, idx) in currentRecord.lossReasons.abnormalReasons" :key="idx" color="red" style="margin-bottom: 4px;">
              {{ getLossReasonText(reason, 'abnormal') }}
            </a-tag>
          </div>
          <div v-if="currentRecord.lossReasons.remark" style="margin-top: 8px;">
            <span style="font-weight: 600; margin-right: 8px;">补充说明：</span>
            <span>{{ currentRecord.lossReasons.remark }}</span>
          </div>
        </a-form-model-item>
        <a-form-model-item label="审核结果" prop="auditResult" required>
          <a-radio-group v-model="auditForm.auditResult">
            <a-radio value="approved">通过</a-radio>
            <a-radio value="rejected">不通过</a-radio>
            <a-radio value="returned">驳回</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item
          label="驳回原因"
          prop="rejectReason"
          v-if="auditForm.auditResult === 'returned'"
        >
          <a-textarea
            v-model="auditForm.rejectReason"
            :rows="3"
            placeholder="请输入驳回原因"
            :maxLength="500"
          />
        </a-form-model-item>
        <a-form-model-item label="审核备注" prop="auditRemark">
          <a-textarea
            v-model="auditForm.auditRemark"
            :rows="4"
            placeholder="请输入审核备注"
            :maxLength="500"
          />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 审核流程弹窗 -->
    <a-modal
      :visible="auditProgressModal.visible"
      title="审核流程"
      :width="900"
      :footer="null"
      :zIndex="2000"
      @cancel="auditProgressModal.visible = false"
    >
      <a-spin :spinning="auditProgressModal.loading">
        <div v-if="auditProgressModal.progress && auditProgressModal.progress.steps">
          <!-- 流失原因展示 -->
          <a-card title="流失原因" :bordered="false" style="margin-bottom: 24px;" v-if="auditProgressModal.lossReasons">
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="流失类型">
                <a-tag :color="auditProgressModal.lossReasons.lossType === 'abnormal' ? 'red' : 'orange'">
                  {{ auditProgressModal.lossReasons.lossType === 'abnormal' ? '非正常流失' : '正常流失' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="正常流失原因" v-if="auditProgressModal.lossReasons.normalReasons && auditProgressModal.lossReasons.normalReasons.length > 0">
                <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                  <a-tag v-for="(reasonId, idx) in auditProgressModal.lossReasons.normalReasons" :key="idx" color="blue">
                    {{ getLossReasonText(reasonId, 'normal') }}
                  </a-tag>
                </div>
              </a-descriptions-item>
              <a-descriptions-item label="非正常流失原因" v-if="auditProgressModal.lossReasons.abnormalReasons && auditProgressModal.lossReasons.abnormalReasons.length > 0">
                <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                  <a-tag v-for="(reasonId, idx) in auditProgressModal.lossReasons.abnormalReasons" :key="idx" color="red">
                    {{ getLossReasonText(reasonId, 'abnormal') }}
                  </a-tag>
                </div>
              </a-descriptions-item>
              <a-descriptions-item label="补充说明" v-if="auditProgressModal.lossReasons.remark">
                {{ auditProgressModal.lossReasons.remark }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
          
          <!-- 审核流程时间线 -->
          <a-timeline>
            <a-timeline-item
              v-for="(step, idx) in reversedAuditSteps"
              :key="`step-${step.originalStepOrder || idx}`"
              :color="getStepColor(step)"
            >
              <div style="margin-bottom: 8px;">
                <strong>第{{ step.reversedStepOrder }}步：</strong>
                <span v-if="getStepRoleNames(step).length > 0">
                  <a-tag v-for="(roleName, idx) in getStepRoleNames(step)" :key="idx" color="blue" style="margin-right: 4px;">
                    {{ roleName }}
                  </a-tag>
                </span>
                <span v-else style="color: #999;">-</span>
              </div>
              <div v-if="step.tasks && step.tasks.length > 0">
                <div v-for="(task, taskIdx) in step.tasks" :key="`task-${task.id || taskIdx}`" style="margin-bottom: 12px; padding: 12px; background: #fafafa; border-radius: 4px; border-left: 3px solid #1890ff;">
                  <div style="display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 8px;">
                    <a-tag :color="getTaskStatusColor(task.taskStatus)" style="font-weight: 600;">
                      {{ getTaskStatusText(task.taskStatus) }}
                    </a-tag>
                    <span v-if="task.auditResult" style="font-weight: 600; color: #1890ff;">
                      审核结果：{{ task.auditResult === 'approved' ? '通过' : task.auditResult === 'rejected' ? '驳回' : task.auditResult }}
                    </span>
                    <span v-if="task.auditUserName" style="color: #666;">
                      审核人：{{ task.auditUserName }}
                    </span>
                    <span v-if="task.auditTime" style="color: #999;">
                      {{ moment(task.auditTime).format('YYYY-MM-DD HH:mm:ss') }}
                    </span>
                  </div>
                  <div v-if="task.auditRemark" style="margin-top: 8px; padding: 8px; background: #fff; border-radius: 4px; color: #666; border: 1px solid #e8e8e8;">
                    <strong style="color: #333;">审核备注：</strong>{{ task.auditRemark }}
                  </div>
                  <div v-if="task.rejectReason" style="margin-top: 8px; padding: 8px; background: #fff1f0; border-radius: 4px; color: #f5222d; border: 1px solid #ffccc7;">
                    <strong>驳回原因：</strong>{{ task.rejectReason }}
                  </div>
                </div>
              </div>
              <div v-else style="color: #999;">
                待审核
              </div>
            </a-timeline-item>
          </a-timeline>
        </div>
        <a-empty v-else description="暂无审核流程信息" style="padding: 40px 0" />
      </a-spin>
    </a-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { getAction, postAction } from '@/api/manage'
import { getPendingTasks, executeAudit, getOrderAuditProgress } from '@/api/api'
import { ajaxGetDictItems } from '@/api/api'
import moment from 'moment'

export default {
  name: 'LossAuditList',
  mixins: [JeecgListMixin],
  props: {
    // 从父组件接收查询参数
    parentQueryParam: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      description: '流失审核列表',
      moment,
      queryParam: {
        contractNo: '',
        orderNo: '',
        companyName: '',
      },
      url: {
        list: '/sys/auditTask/getPendingTasks',
      },
      columns: [
        {
          title: '订单编号',
          align: 'center',
          dataIndex: 'orderNo',
          width: 160,
          scopedSlots: { customRender: 'orderNo' },
        },
        {
          title: '公司名称',
          align: 'center',
          dataIndex: 'companyName',
          width: 200,
          scopedSlots: { customRender: 'companyName' },
        },
        {
          title: '当前审核角色',
          align: 'center',
          dataIndex: 'currentRoleName',
          width: 150,
          scopedSlots: { customRender: 'currentRoleName' },
        },
        {
          title: '审核步骤',
          align: 'center',
          dataIndex: 'stepOrder',
          width: 120,
          scopedSlots: { customRender: 'stepOrder' },
        },
        {
          title: '创建时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 180,
          scopedSlots: { customRender: 'createTime' },
        },
        {
          title: '操作',
          align: 'center',
          dataIndex: 'action',
          width: 120,
          fixed: 'right',
          scopedSlots: { customRender: 'action' },
        },
      ],
      auditModal: {
        visible: false,
        loading: false,
      },
      currentRecord: {},
      auditForm: {
        auditResult: 'approved',
        auditRemark: '',
        rejectReason: '',
      },
      auditRules: {
        auditResult: [
          { required: true, message: '请选择审核结果', trigger: 'change' },
        ],
        rejectReason: [
          { required: true, message: '请输入驳回原因', trigger: 'blur' },
        ],
      },
      // 审核流程弹窗
      auditProgressModal: {
        visible: false,
        loading: false,
        progress: null,
        lossReasons: null,
      },
      // 流失原因字典
      lossReasonOptions: [],
      lossNoNormalOptions: [],
      // 字典未加载/缺失时，兜底把英文值转中文
      lossReasonFallbackMap: {
        zero_declare: '转零申报',
        our_transfer: '我司转让',
        other_agent_cancel: '其他原因-代办注销',
        self_cancel: '自己注销',
        price_reason: '价格原因',
        service_not_satisfied: '服务需求不能满足',
        other_reason: '其他原因',
      },
      // 当前用户的角色ID列表
      userRoleIds: [],
    }
  },
  computed: {
    // 反转后的审核步骤（使用计算属性避免无限循环）
    reversedAuditSteps() {
      if (!this.auditProgressModal.progress || !this.auditProgressModal.progress.steps) {
        return []
      }
      return this.getReversedAuditSteps(this.auditProgressModal.progress.steps)
    },
  },
  created() {
    // 先加载当前用户的角色列表（在created中加载，确保在数据加载前完成）
    this.loadUserRoles()
  },
  mounted() {
    // 加载流失原因字典
    this.loadLossReasonDicts()
    // 再次尝试加载用户角色（以防created中加载失败）
    if (!this.userRoleIds || this.userRoleIds.length === 0) {
      this.loadUserRoles()
    }
  },
  methods: {
    // 重写loadData方法，使用getPendingTasks接口
    loadData(arg) {
      // 仅在明确传入 1（查询/重置）时重置第一页；翻页时保持当前页
      if (arg === 1) {
        this.ipagination.current = 1
      }
      this.loading = true
      // 合并父组件传递的查询参数
      const params = {
        pageNo: this.ipagination.current,
        pageSize: this.ipagination.pageSize,
        taskType: 'loss',
        // 仅看与自己相关的待审核任务，避免“所有人都看见全部”
        isPersonal: true,
        ...this.parentQueryParam,
      }
      // 如果有查询条件，也加上
      if (this.queryParam.contractNo) {
        params.contractNo = this.queryParam.contractNo
      }
      if (this.queryParam.orderNo) {
        params.orderNo = this.queryParam.orderNo
      }
      if (this.queryParam.companyName) {
        params.companyName = this.queryParam.companyName
      }
      
      getPendingTasks(params)
        .then((res) => {
          console.log('流失审核数据返回:', res)
          if (res.success && res.result) {
            let records = []
            // IPage对象有records和total属性
            if (res.result.records) {
              records = res.result.records || []
              this.ipagination.total = res.result.total || 0
            } else if (Array.isArray(res.result)) {
              // 如果直接返回数组
              records = res.result
              this.ipagination.total = res.result.length
            } else {
              records = []
              this.ipagination.total = 0
            }
            // 直接使用后端分页结果，保证“条数/分页/切页”一致
            this.dataSource = records
          } else {
            this.dataSource = []
            this.ipagination.total = 0
            if (res.message) {
              this.$message.warning(res.message)
            }
          }
        })
        .catch((err) => {
          console.error('流失审核查询失败:', err)
          this.$message.error('查询失败：' + (err.message || '未知错误'))
          this.dataSource = []
          this.ipagination.total = 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    // 重置查询
    searchReset() {
      this.queryParam = {
        contractNo: '',
        orderNo: '',
        companyName: '',
      }
      this.loadData(1)
    },
    // 获取反转后的步骤顺序（流失审核是反向流程）
    getReversedStepOrder(stepOrder, record) {
      // 如果有总步骤数，使用总步骤数来计算反转
      // 否则，假设最大步骤数是3（根据实际情况调整）
      const totalSteps = record.totalSteps || 3
      // 反转：第3步显示为第1步，第2步显示为第2步，第1步显示为第3步
      return totalSteps - stepOrder + 1
    },
    // 加载当前用户的角色列表
    loadUserRoles() {
      // 尝试多种方式获取用户信息
      const userInfo = this.$store.getters.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || userInfo.userId || userInfo.username
      console.log('loadUserRoles: 开始加载用户角色', { userId, userInfo })
      
      // 首先尝试从 userInfo 中直接获取角色ID（如果存在）
      if (userInfo.roleIds && Array.isArray(userInfo.roleIds) && userInfo.roleIds.length > 0) {
        this.userRoleIds = userInfo.roleIds
        console.log('loadUserRoles: 从userInfo中获取角色ID', this.userRoleIds)
        return
      }
      
      if (userId) {
        getAction('/sys/user/queryUserRole', { userid: userId }).then((res) => {
          console.log('loadUserRoles: API返回结果', res)
          if (res.success && res.result) {
            // 确保 result 是数组
            if (Array.isArray(res.result)) {
              this.userRoleIds = res.result
            } else if (typeof res.result === 'string') {
              // 如果是字符串，尝试解析
              this.userRoleIds = [res.result]
            } else {
              this.userRoleIds = []
            }
            console.log('loadUserRoles: 用户角色ID列表已设置', this.userRoleIds)
          } else {
            console.warn('loadUserRoles: API返回失败或结果为空', res)
            this.userRoleIds = []
          }
        }).catch((err) => {
          console.error('loadUserRoles: 获取用户角色失败', err)
          this.userRoleIds = []
        })
      } else {
        console.warn('loadUserRoles: 无法获取用户ID', { userInfo })
        this.userRoleIds = []
      }
    },
    // 判断是否可以审核（基于当前用户的角色ID）
    canAudit(record) {
      // 只有待审核状态才能审核
      if (record.taskStatus !== 'pending') {
        console.log('canAudit: 任务状态不是pending', record.taskStatus)
        return false
      }
      
      // 获取当前用户的所有角色ID
      if (!this.userRoleIds || this.userRoleIds.length === 0) {
        console.log('canAudit: 用户角色ID列表为空', this.userRoleIds)
        return false
      }
      
      // 检查当前用户的角色ID是否匹配任务的角色ID（支持合并后的多角色）
      // 优先使用合并后的roleIds数组，如果没有则使用单个currentRoleId
      let roleIdsToCheck = []
      if (record.roleIds && record.roleIds.length > 0) {
        // 使用合并后的角色ID数组
        roleIdsToCheck = record.roleIds
      } else {
        // 兼容单个角色ID（驼峰或下划线命名）
        const currentRoleId = record.currentRoleId || record.current_role_id
        if (currentRoleId) {
          roleIdsToCheck = [currentRoleId]
        }
      }
      
      if (roleIdsToCheck.length === 0) {
        console.log('canAudit: 任务没有角色ID', record)
        return false
      }
      
      // 将角色ID转换为字符串进行比较
      const roleIdStrs = roleIdsToCheck.map(id => String(id))
      const userRoleIdStrs = this.userRoleIds.map(id => String(id))
      
      // 检查用户的角色ID是否在任务的角色ID列表中
      const canAuditResult = userRoleIdStrs.some(userRoleId => roleIdStrs.includes(userRoleId))
      console.log('canAudit: 检查结果', {
        roleIds: roleIdStrs,
        userRoleIds: userRoleIdStrs,
        canAudit: canAuditResult,
        record: record
      })
      
      return canAuditResult
    },
    // 查看订单
    handleViewOrder(orderId) {
      // 可以打开订单详情或合同详情
      this.$message.info('查看订单功能待实现')
    },
    // 审核
    async handleAudit(record) {
      this.currentRecord = { ...record }
      // 确保流失原因字典已加载
      await this.loadLossReasonDicts()
      // 加载流失原因信息
      await this.loadLossReasons(this.currentRecord)
      console.log('handleAudit: 流失原因加载完成', this.currentRecord.lossReasons)
      this.auditForm = {
        auditResult: 'approved',
        auditRemark: '',
        rejectReason: '',
      }
      this.auditModal.visible = true
    },
    // 审核确认
    handleAuditOk() {
      this.$refs.auditFormRef.validate((valid) => {
        if (!valid) {
          return false
        }
        
        if (this.auditForm.auditResult === 'returned' && !this.auditForm.rejectReason) {
          this.$message.error('驳回时必须填写驳回原因')
          return false
        }
        
        this.auditModal.loading = true
        // 根据当前用户的角色ID，找到对应的任务ID
        let taskId = this.currentRecord.id
        if (this.currentRecord.roleIdToTaskIdMap && this.userRoleIds && this.userRoleIds.length > 0) {
          // 查找当前用户角色对应的任务ID
          for (const userRoleId of this.userRoleIds) {
            const userRoleIdStr = String(userRoleId)
            if (this.currentRecord.roleIdToTaskIdMap[userRoleIdStr]) {
              taskId = this.currentRecord.roleIdToTaskIdMap[userRoleIdStr]
              break
            }
          }
        }
        const params = {
          taskId: taskId,
          auditResult: this.auditForm.auditResult,
          auditData: '',
          auditRemark: this.auditForm.auditRemark,
          rejectReason: this.auditForm.rejectReason,
        }
        
        executeAudit(params)
          .then((res) => {
            if (res.success) {
              this.$message.success('审核成功')
              this.auditModal.visible = false
              this.loadData(1)
            } else {
              this.$message.error(res.message || '审核失败')
            }
          })
          .catch((err) => {
            this.$message.error('审核失败：' + (err.message || '未知错误'))
          })
          .finally(() => {
            this.auditModal.loading = false
          })
      })
    },
    // 审核取消
    handleAuditCancel() {
      this.auditModal.visible = false
      this.currentRecord = {}
      this.auditForm = {
        auditResult: 'approved',
        auditRemark: '',
        rejectReason: '',
      }
    },
    // 加载流失原因
    async loadLossReasons(record) {
      if (!record.orderId) {
        console.warn('loadLossReasons: 没有orderId', record)
        return
      }
      try {
        console.log('loadLossReasons: 开始加载流失原因', { orderId: record.orderId })
        // 直接根据orderId查询合同信息，后端会自动跳过tabType过滤
        const res = await getAction('/order/accountingContract/list', { 
          orderId: record.orderId,
          pageNo: 1,
          pageSize: 1
        })
        console.log('loadLossReasons: 合同查询结果', res)
        // 检查返回数据结构：可能是 result.records 或 result.list
        let contracts = []
        if (res.success && res.result) {
          if (res.result.records && Array.isArray(res.result.records)) {
            contracts = res.result.records
          } else if (res.result.list && Array.isArray(res.result.list)) {
            contracts = res.result.list
          } else if (Array.isArray(res.result)) {
            contracts = res.result
          }
        }
        console.log('loadLossReasons: 提取的合同列表', contracts)
        if (contracts.length > 0) {
          const contract = contracts[0]
          console.log('loadLossReasons: 合同信息', contract)
          if (contract.remarks && contract.remarks.includes('【流失申请】')) {
            // 解析流失原因
            const lossReasons = this.parseLossReasonsFromRemarks(contract.remarks)
            console.log('loadLossReasons: 解析后的流失原因', lossReasons)
            // 确保设置到 currentRecord
            this.$set(this.currentRecord, 'lossReasons', lossReasons)
            // 同时也设置到 record（以防万一）
            this.$set(record, 'lossReasons', lossReasons)
          } else {
            console.warn('loadLossReasons: 合同备注中没有流失申请信息', contract.remarks)
          }
        } else {
          console.warn('loadLossReasons: 未找到合同信息', res)
        }
      } catch (err) {
        console.error('loadLossReasons: 加载流失原因失败', err)
      }
    },
    // 从备注中解析流失原因
    parseLossReasonsFromRemarks(remarks) {
      const lossReasons = {
        lossType: '',
        normalReasons: [],
        abnormalReasons: [],
        remark: '',
      }
      
      if (!remarks || !remarks.includes('【流失申请】')) {
        console.log('parseLossReasonsFromRemarks: 备注中没有流失申请信息', remarks)
        return lossReasons
      }
      
      const lossIndex = remarks.indexOf('【流失申请】')
      const lossInfo = remarks.substring(lossIndex)
      console.log('parseLossReasonsFromRemarks: 提取的流失信息', lossInfo)
      
      // 解析流失类型
      if (lossInfo.includes('类型：非正常流失')) {
        lossReasons.lossType = 'abnormal'
      } else if (lossInfo.includes('类型：正常流失')) {
        lossReasons.lossType = 'normal'
      }
      
      // 解析正常流失原因ID（使用更宽松的正则，支持换行和多种分隔符）
      const normalMatch = lossInfo.match(/正常流失原因ID[：:]([^；;]+)/)
      if (normalMatch) {
        lossReasons.normalReasons = normalMatch[1].split(/[,，]/).map(s => s.trim()).filter(Boolean)
      }
      
      // 解析非正常流失原因ID（使用更宽松的正则，支持换行和多种分隔符）
      const abnormalMatch = lossInfo.match(/非正常流失原因ID[：:]([^；;]+)/)
      if (abnormalMatch) {
        lossReasons.abnormalReasons = abnormalMatch[1].split(/[,，]/).map(s => s.trim()).filter(Boolean)
      }
      
      // 解析补充说明（使用更宽松的正则，支持换行）
      const remarkMatch = lossInfo.match(/补充说明[：:](.+?)(?=；|;|$)/s)
      if (remarkMatch) {
        lossReasons.remark = remarkMatch[1].trim()
      }
      
      console.log('parseLossReasonsFromRemarks: 解析结果', lossReasons)
      return lossReasons
    },
    // 加载流失原因字典
    loadLossReasonDicts() {
      const promises = []
      if (!this.lossReasonOptions || this.lossReasonOptions.length === 0) {
        promises.push(
          ajaxGetDictItems('lossReason', null)
            .then((res) => {
              const items = (res && Array.isArray(res.result)) ? res.result : (Array.isArray(res) ? res : [])
              if (items.length > 0) {
                this.lossReasonOptions = items.map((item) => ({
                  key: item.value,
                  title: item.text,
                }))
              } else {
                this.lossReasonOptions = []
              }
            })
            .catch(() => {
              this.lossReasonOptions = []
            }),
        )
      }
      if (!this.lossNoNormalOptions || this.lossNoNormalOptions.length === 0) {
        promises.push(
          ajaxGetDictItems('lossNoNormalReason', null)
            .then((res) => {
              const items = (res && Array.isArray(res.result)) ? res.result : (Array.isArray(res) ? res : [])
              if (items.length > 0) {
                this.lossNoNormalOptions = items.map((item) => ({
                  key: item.value,
                  title: item.text,
                }))
              } else {
                this.lossNoNormalOptions = []
              }
            })
            .catch(() => {
              this.lossNoNormalOptions = []
            }),
        )
      }
      return Promise.all(promises)
    },
    // 获取流失原因文本
    getLossReasonText(reasonId, type) {
      const rid = (reasonId == null ? '' : String(reasonId)).trim()
      if (type === 'normal') {
        const reason = this.lossReasonOptions.find(item => String(item.key).trim() === rid)
        return reason ? reason.title : (this.lossReasonFallbackMap[rid] || rid)
      } else {
        const reason = this.lossNoNormalOptions.find(item => String(item.key).trim() === rid)
        return reason ? reason.title : (this.lossReasonFallbackMap[rid] || rid)
      }
    },
    // 获取反转后的审核步骤（流失审核是反向流程）
    getReversedAuditSteps(steps) {
      if (!steps || steps.length === 0) {
        return []
      }
      
      // 先找到所有正向流程的步骤（用于补充角色信息）
      const normalSteps = steps.filter(step => {
        if (step.tasks && step.tasks.length > 0) {
          // 正向流程的任务 taskType 不是 'loss' 或者是 null/undefined
          return step.tasks.some(task => !task.taskType || task.taskType !== 'loss')
        }
        return false
      })
      
      // 创建一个映射，用于快速查找正向流程步骤的角色信息（支持多个角色）
      const normalStepMap = {}
      normalSteps.forEach(step => {
        if (step.tasks && step.tasks.length > 0) {
          // 从正向流程任务中获取所有角色名称（去重）
          const roleNames = []
          step.tasks.forEach(task => {
            if (!task.taskType || task.taskType !== 'loss') {
              const roleName = task.roleName || task.currentRoleName
              if (roleName && !roleNames.includes(roleName)) {
                roleNames.push(roleName)
              }
            }
          })
          if (roleNames.length > 0) {
            normalStepMap[step.stepOrder] = roleNames
          } else if (step.roleName) {
            normalStepMap[step.stepOrder] = [step.roleName]
          }
        }
      })
      
      // 只过滤出流失审核任务（taskType='loss'），不显示之前的合同审核步骤
      // 使用深拷贝避免修改原始数据
      // 注意：即使没有任务，也要显示步骤（显示为待审核状态）
      const lossSteps = steps
        .map(step => {
          // 深拷贝步骤，避免修改原始数据
          const stepCopy = JSON.parse(JSON.stringify(step))
          if (stepCopy.tasks && stepCopy.tasks.length > 0) {
            // 只保留 taskType='loss' 的任务
            const lossTasks = stepCopy.tasks.filter(task => task.taskType === 'loss')
            if (lossTasks.length > 0) {
              // 只保留流失审核任务，过滤掉之前的合同审核任务
              stepCopy.tasks = lossTasks
              return stepCopy
            }
            // 如果没有流失审核任务，但步骤存在，也要返回（显示为待审核）
            // 从正向流程步骤中获取角色名称（可能是数组）
            stepCopy.tasks = []
            if (normalStepMap[step.stepOrder]) {
              stepCopy.roleNames = Array.isArray(normalStepMap[step.stepOrder]) 
                ? normalStepMap[step.stepOrder] 
                : [normalStepMap[step.stepOrder]]
            }
            return stepCopy
          } else {
            // 如果没有任务，也要返回步骤（显示为待审核）
            // 从正向流程步骤中获取角色名称（可能是数组）
            stepCopy.tasks = []
            if (normalStepMap[step.stepOrder]) {
              stepCopy.roleNames = Array.isArray(normalStepMap[step.stepOrder]) 
                ? normalStepMap[step.stepOrder] 
                : [normalStepMap[step.stepOrder]]
            }
            return stepCopy
          }
        })
        .filter(step => step !== null)
      
      // 如果没有找到流失审核任务，返回空数组（不显示之前的合同审核步骤）
      if (lossSteps.length === 0) {
        return []
      }
      
      // 反转步骤顺序（流失审核是反向流程）
      const sortedSteps = [...lossSteps].sort((a, b) => b.stepOrder - a.stepOrder)
      return sortedSteps.map((step, index) => {
        const reversedStep = { ...step }
        reversedStep.reversedStepOrder = index + 1 // 反转后的步骤顺序从1开始
        reversedStep.originalStepOrder = step.stepOrder // 保留原始步骤顺序
        // 设置步骤的角色名称列表（优先从流失审核任务中获取，如果没有则从正向流程步骤中获取）
        const roleNames = []
        if (reversedStep.tasks && reversedStep.tasks.length > 0) {
          // 从流失审核任务中获取所有角色名称（去重）
          reversedStep.tasks.forEach(task => {
            const roleName = task.roleName || task.currentRoleName
            if (roleName && !roleNames.includes(roleName)) {
              roleNames.push(roleName)
            }
          })
        }
        // 如果没有流失审核任务，从正向流程步骤中获取角色名称
        if (roleNames.length === 0 && normalStepMap[step.stepOrder]) {
          if (Array.isArray(normalStepMap[step.stepOrder])) {
            roleNames.push(...normalStepMap[step.stepOrder])
          } else {
            roleNames.push(normalStepMap[step.stepOrder])
          }
        }
        // 如果还是没有，使用步骤本身的角色名称
        if (roleNames.length === 0 && reversedStep.roleName) {
          roleNames.push(reversedStep.roleName)
        }
        reversedStep.roleNames = roleNames.length > 0 ? roleNames : []
        return reversedStep
      })
    },
    // 获取步骤颜色
    getStepColor(step) {
      if (!step.tasks || step.tasks.length === 0) {
        return 'blue' // 待审核
      }
      const hasApproved = step.tasks.some(task => task.taskStatus === 'approved')
      const hasRejected = step.tasks.some(task => task.taskStatus === 'rejected')
      if (hasApproved) {
        return 'green' // 已通过
      } else if (hasRejected) {
        return 'red' // 已驳回
      }
      return 'blue' // 待审核
    },
    // 获取任务状态颜色
    getTaskStatusColor(status) {
      if (status === 'approved') return 'green'
      if (status === 'rejected') return 'red'
      return 'orange'
    },
    // 获取任务状态文本
    getTaskStatusText(status) {
      if (status === 'approved') return '已通过'
      if (status === 'rejected') return '已驳回'
      return '待审核'
    },
    // 获取步骤的角色名称列表（支持多个角色）
    getStepRoleNames(step) {
      if (step.roleNames && Array.isArray(step.roleNames) && step.roleNames.length > 0) {
        return step.roleNames
      }
      // 如果步骤没有roleNames，从任务中获取所有角色名称（去重）
      const roleNames = []
      if (step.tasks && step.tasks.length > 0) {
        step.tasks.forEach(task => {
          const roleName = task.roleName || task.currentRoleName
          if (roleName && !roleNames.includes(roleName)) {
            roleNames.push(roleName)
          }
        })
      }
      // 如果还是没有，使用步骤本身的角色名称
      if (roleNames.length === 0 && step.roleName) {
        roleNames.push(step.roleName)
      }
      return roleNames
    },
    // 查看审核流程（从弹窗中）
    handleViewAuditProgress() {
      if (!this.currentRecord.orderId) {
        this.$message.warning('无法获取订单信息')
        return
      }
      // 打开审核流程弹窗
      this.showAuditProgressModal(this.currentRecord.orderId, this.currentRecord.lossReasons)
    },
    // 查看审核流程（从表格中）
    handleViewAuditProgressByRecord(record) {
      if (!record.orderId) {
        this.$message.warning('无法获取订单信息')
        return
      }
      // 先加载流失原因，然后打开审核流程弹窗
      this.loadLossReasons(record).then(() => {
        this.showAuditProgressModal(record.orderId, record.lossReasons)
      })
    },
    // 显示审核流程弹窗
    async showAuditProgressModal(orderId, lossReasons = null) {
      this.auditProgressModal.visible = true
      this.auditProgressModal.loading = true
      this.auditProgressModal.progress = null
      this.auditProgressModal.lossReasons = lossReasons
      
      try {
        // 如果没有流失原因，尝试加载
        if (!lossReasons) {
          try {
            const contractRes = await getAction('/order/accountingContract/list', { 
              orderId: orderId,
              pageNo: 1,
              pageSize: 1
            })
            if (contractRes.success && contractRes.result && contractRes.result.records && contractRes.result.records.length > 0) {
              const contract = contractRes.result.records[0]
              if (contract.remarks && contract.remarks.includes('【流失申请】')) {
                this.auditProgressModal.lossReasons = this.parseLossReasonsFromRemarks(contract.remarks)
              }
            }
          } catch (err) {
            console.error('加载流失原因失败', err)
          }
        }
        
        // 加载审核流程
        const res = await getOrderAuditProgress({ orderId })
        if (res.success && res.result && res.result.steps) {
          this.auditProgressModal.progress = res.result
        } else {
          this.auditProgressModal.progress = null
        }
      } catch (err) {
        console.error('加载审核流程失败', err)
        this.$message.error('加载审核流程失败：' + (err.message || '未知错误'))
        this.auditProgressModal.progress = null
      } finally {
        this.auditProgressModal.loading = false
      }
    },
  },
}
</script>

<style scoped>
</style>

