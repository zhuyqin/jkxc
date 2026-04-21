<template>
  <a-card :bordered="false">
    <a-tabs :activeKey="activeTab" @change="handleTabChange">
      <!-- 工商经理审核 -->
      <a-tab-pane key="pending_manager_audit" tab="工商经理审核">
        <business-task-table
          ref="managerAuditTable"
          :taskStatus="'pending_manager_audit'"
          :columns="managerAuditColumns"
          :operations="managerAuditOperations"
          :filterOrderNo="filterOrderNo"
          @managerAudit="showManagerAuditModal"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 公海待接收 -->
      <a-tab-pane key="public_sea" tab="公海待接收">
        <business-task-table
          ref="publicSeaTable"
          :taskStatus="'public_sea'"
          :columns="taskColumns"
          :operations="publicSeaOperations"
          :filterOrderNo="filterOrderNo"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 待本人接收 -->
      <a-tab-pane key="assigned_to_me" tab="待本人接收">
        <business-task-table
          ref="assignedToMeTable"
          :taskStatus="'assigned_to_me'"
          :columns="taskColumns"
          :operations="assignedToMeOperations"
          :filterOrderNo="filterOrderNo"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 任务 -->
      <a-tab-pane key="task" tab="任务">
        <business-task-table
          ref="taskTable"
          :taskStatus="'task'"
          :columns="taskColumns"
          :operations="taskOperations"
          :filterOrderNo="filterOrderNo"
          @updateCost="showCostModal"
          @reassign="showReassignModal"
          @exception="showExceptionModal"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 交接 -->
      <a-tab-pane key="handover" tab="交接">
        <business-task-table
          ref="handoverTable"
          :taskStatus="'handover'"
          :columns="taskColumns"
          :operations="handoverOperations"
          :filterOrderNo="filterOrderNo"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 已完单 -->
      <a-tab-pane key="completed" tab="已完单">
        <business-task-table
          ref="completedTable"
          :taskStatus="'completed'"
          :columns="taskColumns"
          :operations="completedOperations"
          :filterOrderNo="filterOrderNo"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 问题任务 -->
      <a-tab-pane key="problem_task" tab="问题任务">
        <business-task-table
          ref="problemTaskTable"
          :taskStatus="'problem_task'"
          :columns="taskColumns"
          :operations="problemTaskOperations"
          :filterOrderNo="filterOrderNo"
          @reassign="showReassignModal"
          @exception="showExceptionModal"
          @refresh="loadData"
        />
      </a-tab-pane>

      <!-- 回收站 -->
      <a-tab-pane key="recycle_bin" tab="回收站">
        <business-task-table
          ref="recycleBinTable"
          :taskStatus="'recycle_bin'"
          :columns="taskColumns"
          :operations="recycleBinOperations"
          :filterOrderNo="filterOrderNo"
          @reassign="showReassignModal"
          @refresh="loadData"
        />
      </a-tab-pane>
    </a-tabs>

    <!-- 工商经理审核弹窗 -->
    <j-modal
      :visible.sync="managerAuditModal.visible"
      :width="800"
      title="工商经理审核"
      :fullscreen.sync="managerAuditModal.fullscreen"
      :switchFullscreen="managerAuditModal.switchFullscreen"
      @ok="handleManagerAudit"
      @cancel="handleCancelManagerAudit"
      :confirmLoading="managerAuditSubmitting"
    >
      <a-form-model ref="managerAuditForm" :model="managerAuditForm" :rules="managerAuditRules" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="审核结果" prop="auditStatus" required>
          <a-radio-group v-model="managerAuditForm.auditStatus" @change="onAuditStatusChange">
            <a-radio value="approved">通过</a-radio>
            <a-radio value="rejected">驳回</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item v-if="managerAuditForm.auditStatus === 'approved'" label="分配方式" prop="assignType" required>
          <a-radio-group v-model="managerAuditForm.assignType" @change="onAssignTypeChange">
            <a-radio value="public_sea">放入公海</a-radio>
            <a-radio value="assign_user">指定工商人员</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item v-if="managerAuditForm.assignType === 'assign_user'" label="指定工商人员" prop="assignedUserId" required>
          <j-search-select-tag
            placeholder="请选择工商人员"
            v-model="managerAuditForm.assignedUserId"
            dict="sys_user,realname,id,1=1 and del_flag = '0'"
            :async="false"
          />
        </a-form-model-item>
        <a-form-model-item label="审核备注" prop="remark">
          <a-textarea v-model="managerAuditForm.remark" :rows="4" placeholder="请输入审核备注" :maxLength="500" />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 成本信息弹窗 -->
    <j-modal
      :visible.sync="costModal.visible"
      :width="600"
      title="填写成本信息"
      :fullscreen.sync="costModal.fullscreen"
      :switchFullscreen="costModal.switchFullscreen"
      @ok="handleUpdateCost"
      @cancel="handleCancelCost"
      :confirmLoading="costSubmitting"
    >
      <a-form-model ref="costForm" :model="costForm" :rules="costRules" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="成本类目" prop="costCategory" required>
          <a-input v-model="costForm.costCategory" placeholder="请输入成本类目" :maxLength="100" />
        </a-form-model-item>
        <a-form-model-item label="成本金额" prop="costAmount" required>
          <a-input-number v-model="costForm.costAmount" placeholder="请输入成本金额" :min="0" :precision="2" style="width: 100%" />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 异常任务弹窗 -->
    <j-modal
      :visible.sync="exceptionModal.visible"
      :width="600"
      :title="exceptionModal.title"
      :fullscreen.sync="exceptionModal.fullscreen"
      :switchFullscreen="exceptionModal.switchFullscreen"
      @ok="handleConvertToException"
      @cancel="handleCancelException"
      :confirmLoading="exceptionSubmitting"
    >
      <a-form-model ref="exceptionForm" :model="exceptionForm" :rules="exceptionRules" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="原因" prop="reason" required>
          <a-textarea v-model="exceptionForm.reason" :rows="4" placeholder="请输入原因" :maxLength="500" />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 转分配弹窗 -->
    <j-modal
      :visible.sync="reassignModal.visible"
      :width="600"
      title="转分配工商人员"
      :fullscreen.sync="reassignModal.fullscreen"
      :switchFullscreen="reassignModal.switchFullscreen"
      @ok="handleReassign"
      @cancel="handleCancelReassign"
      :confirmLoading="reassignSubmitting"
    >
      <a-form-model ref="reassignForm" :model="reassignForm" :rules="reassignRules" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="工商人员" prop="assignedUserId" required>
          <j-search-select-tag
            placeholder="请选择工商人员"
            v-model="reassignForm.assignedUserId"
            dict="sys_user,realname,id,1=1 and del_flag = '0'"
            :async="false"
          />
        </a-form-model-item>
      </a-form-model>
    </j-modal>
  </a-card>
</template>

<script>
import BusinessTaskTable from './modules/BusinessTaskTable'
import { getAction, httpAction } from '@/api/manage'

export default {
  name: 'BusinessTaskList',
  components: {
    BusinessTaskTable,
  },
  data() {
    return {
      activeTab: 'pending_manager_audit', // 当前激活的tab
      filterOrderNo: '', // 筛选的订单号
      // 工商经理审核列
      managerAuditColumns: [
        { title: '订单编号', align: 'center', dataIndex: 'orderNo', width: 150 },
        { 
          title: '公司名称', 
          align: 'center', 
          dataIndex: 'companyName', 
          width: 200,
          customRender: (text) => {
            if (!text) return '-';
            return text.length > 20 ? text.substring(0, 20) + '...' : text;
          },
          ellipsis: true
        },
        { title: '创建时间', align: 'center', dataIndex: 'createTime', width: 180 },
        { title: '操作', align: 'center', dataIndex: 'action', width: 150, scopedSlots: { customRender: 'action' } },
      ],
      // 任务列
      taskColumns: [
        { title: '订单编号', align: 'center', dataIndex: 'orderNo', width: 150 },
        { 
          title: '公司名称', 
          align: 'center', 
          dataIndex: 'companyName', 
          width: 200,
          customRender: (text) => {
            if (!text) return '-';
            return text.length > 20 ? text.substring(0, 20) + '...' : text;
          },
          ellipsis: true
        },
        { title: '分配人员', align: 'center', dataIndex: 'assignedUserName', width: 120 },
        { title: '接收人', align: 'center', dataIndex: 'receivedUserName', width: 120 },
        { title: '成本类目', align: 'center', dataIndex: 'costCategory', width: 150 },
        { title: '成本金额', align: 'center', dataIndex: 'costAmount', width: 120 },
        { title: '接收时间', align: 'center', dataIndex: 'receivedTime', width: 180 },
        { title: '操作', align: 'center', dataIndex: 'action', width: 200, scopedSlots: { customRender: 'action' } },
      ],
      // 工商经理审核操作
      managerAuditOperations: [
        { key: 'audit', label: '审核', type: 'primary' },
      ],
      // 公海待接收操作
      publicSeaOperations: [
        { key: 'receive', label: '接收', type: 'primary' },
      ],
      // 待本人接收操作
      assignedToMeOperations: [
        { key: 'receive', label: '接收', type: 'primary' },
      ],
      // 任务操作
      taskOperations: [
        { key: 'updateCost', label: '填写成本', type: 'default' },
        { key: 'handover', label: '去交接', type: 'primary' },
        { key: 'reassign', label: '转分配', type: 'default' },
        { key: 'exception', label: '转为异常', type: 'danger' },
      ],
      // 交接操作
      handoverOperations: [
        { key: 'confirm', label: '确认完成', type: 'primary' },
      ],
      // 已完单操作
      completedOperations: [],
      // 问题任务操作
      problemTaskOperations: [
        { key: 'reassign', label: '转分配', type: 'default' },
        { key: 'recycle', label: '回收站', type: 'danger' },
      ],
      // 回收站操作
      recycleBinOperations: [
        { key: 'reassign', label: '转分配', type: 'default' },
      ],
      // 工商经理审核弹窗
      managerAuditModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        taskId: '',
      },
      managerAuditSubmitting: false, // 经理审核提交中
      costSubmitting: false, // 成本更新提交中
      exceptionSubmitting: false, // 异常转换提交中
      reassignSubmitting: false, // 转分配提交中
      managerAuditForm: {
        auditStatus: 'approved',
        assignType: 'public_sea',
        assignedUserId: '',
        remark: '',
      },
      managerAuditRules: {
        auditStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
        assignType: [{ required: true, message: '请选择分配方式', trigger: 'change' }],
        assignedUserId: [{ required: true, message: '请选择工商人员', trigger: 'change' }],
      },
      // 成本信息弹窗
      costModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        taskId: '',
      },
      costForm: {
        costCategory: '',
        costAmount: null,
      },
      costRules: {
        costCategory: [{ required: true, message: '请输入成本类目', trigger: 'blur' }],
        costAmount: [{ required: true, message: '请输入成本金额', trigger: 'blur' }],
      },
      // 异常任务弹窗
      exceptionModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        taskId: '',
        exceptionType: '',
        title: '转为异常任务',
      },
      exceptionForm: {
        reason: '',
      },
      exceptionRules: {
        reason: [{ required: true, message: '请输入原因', trigger: 'blur' }],
      },
      // 转分配弹窗
      reassignModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        taskId: '',
      },
      reassignForm: {
        assignedUserId: '',
      },
      reassignRules: {
        assignedUserId: [{ required: true, message: '请选择工商人员', trigger: 'change' }],
      },
    }
  },
  created() {
    // 从URL参数中获取筛选条件
    const query = this.$route.query
    if (query.tab) {
      this.activeTab = query.tab
    }
    if (query.orderNo) {
      this.filterOrderNo = query.orderNo
    }
  },
  methods: {
    handleTabChange(key) {
      this.activeTab = key
      // tab切换时刷新数据
      this.$nextTick(() => {
        this.loadData()
      })
    },
    loadData() {
      // 触发所有子组件刷新
      const refs = ['managerAuditTable', 'publicSeaTable', 'assignedToMeTable', 'taskTable', 'handoverTable', 'completedTable', 'problemTaskTable', 'recycleBinTable']
      refs.forEach(ref => {
        if (this.$refs[ref]) {
          this.$refs[ref].loadData()
        }
      })
    },
    showManagerAuditModal(record) {
      this.managerAuditModal.taskId = record.id
      this.managerAuditModal.visible = true
    },
    showCostModal(record) {
      this.costModal.taskId = record.id
      this.costForm.costCategory = record.costCategory || ''
      this.costForm.costAmount = record.costAmount || null
      this.costModal.visible = true
    },
    showExceptionModal(record, exceptionType) {
      this.exceptionModal.taskId = record.id
      this.exceptionModal.exceptionType = exceptionType
      this.exceptionModal.title = exceptionType === 'problem_task' ? '转为问题任务' : '转为回收站'
      this.exceptionModal.visible = true
    },
    showReassignModal(record) {
      this.reassignModal.taskId = record.id
      this.reassignForm.assignedUserId = record.assignedUserId || ''
      this.reassignModal.visible = true
    },
    // 工商经理审核
    handleManagerAudit() {
      // 防重复提交
      if (this.managerAuditSubmitting) {
        this.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      this.$refs.managerAuditForm.validate((valid) => {
        if (valid) {
          this.managerAuditSubmitting = true
          const params = {
            taskId: this.managerAuditModal.taskId,
            auditStatus: this.managerAuditForm.auditStatus,
            assignType: this.managerAuditForm.assignType,
            assignedUserId: this.managerAuditForm.assignedUserId,
            assignedUserName: this.getUserNameById(this.managerAuditForm.assignedUserId) || '',
            remark: this.managerAuditForm.remark,
          }
          httpAction('/order/businessTask/managerAudit', params, 'post').then((res) => {
            if (res.success) {
              this.$message.success('审核成功')
              this.handleCancelManagerAudit()
              this.loadData()
            } else {
              this.$message.error(res.message || '审核失败')
            }
          }).catch((err) => {
            console.error('审核失败', err)
            this.$message.error('审核失败：' + (err.message || '网络错误'))
          }).finally(() => {
            this.managerAuditSubmitting = false
          })
        }
      })
    },
    handleCancelManagerAudit() {
      this.managerAuditModal.visible = false
      this.managerAuditForm = {
        auditStatus: 'approved',
        assignType: 'public_sea',
        assignedUserId: '',
        remark: '',
      }
      this.managerAuditSubmitting = false
      this.$refs.managerAuditForm && this.$refs.managerAuditForm.resetFields()
    },
    onAuditStatusChange(e) {
      if (e.target.value === 'rejected') {
        this.managerAuditForm.assignType = ''
        this.managerAuditForm.assignedUserId = ''
      }
    },
    onAssignTypeChange(e) {
      if (e.target.value === 'public_sea') {
        this.managerAuditForm.assignedUserId = ''
      }
    },
    // 更新成本
    handleUpdateCost() {
      // 防重复提交
      if (this.costSubmitting) {
        this.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      this.$refs.costForm.validate((valid) => {
        if (valid) {
          this.costSubmitting = true
          const params = {
            taskId: this.costModal.taskId,
            costCategory: this.costForm.costCategory,
            costAmount: this.costForm.costAmount,
          }
          httpAction('/order/businessTask/updateCost', params, 'post').then((res) => {
            if (res.success) {
              this.$message.success('更新成功')
              this.handleCancelCost()
              this.loadData()
            } else {
              this.$message.error(res.message || '更新失败')
            }
          }).catch((err) => {
            console.error('更新失败', err)
            this.$message.error('更新失败：' + (err.message || '网络错误'))
          }).finally(() => {
            this.costSubmitting = false
          })
        }
      })
    },
    handleCancelCost() {
      this.costModal.visible = false
      this.costForm = {
        costCategory: '',
        costAmount: null,
      }
      this.costSubmitting = false
      this.$refs.costForm && this.$refs.costForm.resetFields()
    },
    // 转为异常
    handleConvertToException() {
      // 防重复提交
      if (this.exceptionSubmitting) {
        this.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      this.$refs.exceptionForm.validate((valid) => {
        if (valid) {
          this.exceptionSubmitting = true
          const params = {
            taskId: this.exceptionModal.taskId,
            exceptionType: this.exceptionModal.exceptionType,
            reason: this.exceptionForm.reason,
          }
          httpAction('/order/businessTask/convertToException', params, 'post').then((res) => {
            if (res.success) {
              this.$message.success('操作成功')
              this.handleCancelException()
              this.loadData()
            } else {
              this.$message.error(res.message || '操作失败')
            }
          }).catch((err) => {
            console.error('操作失败', err)
            this.$message.error('操作失败：' + (err.message || '网络错误'))
          }).finally(() => {
            this.exceptionSubmitting = false
          })
        }
      })
    },
    handleCancelException() {
      this.exceptionModal.visible = false
      this.exceptionForm = {
        reason: '',
      }
      this.exceptionSubmitting = false
      this.$refs.exceptionForm && this.$refs.exceptionForm.resetFields()
    },
    // 转分配
    handleReassign() {
      // 防重复提交
      if (this.reassignSubmitting) {
        this.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      this.$refs.reassignForm.validate((valid) => {
        if (valid) {
          this.reassignSubmitting = true
          // 获取用户名
          const assignedUserName = this.reassignForm.assignedUserName || ''
          const params = {
            taskId: this.reassignModal.taskId,
            assignedUserId: this.reassignForm.assignedUserId,
            assignedUserName: assignedUserName,
          }
          httpAction('/order/businessTask/reassign', params, 'post').then((res) => {
            if (res.success) {
              this.$message.success('分配成功')
              this.handleCancelReassign()
              this.loadData()
            } else {
              this.$message.error(res.message || '分配失败')
            }
          }).catch((err) => {
            console.error('分配失败', err)
            this.$message.error('分配失败：' + (err.message || '网络错误'))
          }).finally(() => {
            this.reassignSubmitting = false
          })
        }
      })
    },
    handleCancelReassign() {
      this.reassignModal.visible = false
      this.reassignForm = {
        assignedUserId: '',
      }
      this.reassignSubmitting = false
      this.$refs.reassignForm && this.$refs.reassignForm.resetFields()
    },
    getUserNameById(userId) {
      // 从字典中获取用户名，这里简化处理
      return ''
    },
  },
}
</script>

<style scoped>
</style>

