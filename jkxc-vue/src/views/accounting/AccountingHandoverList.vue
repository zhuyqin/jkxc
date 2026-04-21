<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="订单编号">
              <JInput placeholder="请输入订单编号" v-model="queryParam.orderNo" type="like" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like" allowClear />
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

    <!-- 标签页区域 -->
    <a-tabs defaultActiveKey="pending" @change="handleTabChange" :animated="false">
      <a-tab-pane key="pending" tab="待审核">
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
          <span slot="handoverStatus" slot-scope="text, record">
            <a-badge :status="getStatusBadgeStatus(record.handoverStatus)" :text="getStatusText(record.handoverStatus)" />
          </span>
          <span slot="action" slot-scope="text, record">
            <template v-if="canAudit(record)">
              <a-button type="primary" size="small" @click="handleAudit(record)" style="margin-right: 8px">
                <a-icon type="check-circle" />审核
              </a-button>
            </template>
            <a-button @click="handleDetail(record)" size="small">
              <a-icon type="eye" />详情
            </a-button>
          </span>
        </a-table>
      </a-tab-pane>
      
      <a-tab-pane key="approved" tab="审核通过">
        <a-table
          ref="approvedTable"
          size="middle"
          :scroll="{ x: true }"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="approvedDataSource"
          :pagination="approvedPagination"
          :loading="approvedLoading"
          class="j-table-force-nowrap"
          @change="handleApprovedTableChange"
        >
          <span slot="handoverStatus" slot-scope="text, record">
            <a-badge :status="getStatusBadgeStatus(record.handoverStatus)" :text="getStatusText(record.handoverStatus)" />
          </span>
          <span slot="action" slot-scope="text, record">
            <a-button v-if="canConvertToContract(record)" type="primary" size="small" @click="handleConvertToContract(record)" style="margin-right: 8px">
              <a-icon type="file-protect" />转为合同
            </a-button>
            <a-button @click="handleDetail(record)" size="small">
              <a-icon type="eye" />详情
            </a-button>
          </span>
        </a-table>
      </a-tab-pane>
      
      <a-tab-pane key="rejected" tab="审核驳回">
        <a-table
          ref="rejectedTable"
          size="middle"
          :scroll="{ x: true }"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="rejectedDataSource"
          :pagination="rejectedPagination"
          :loading="rejectedLoading"
          class="j-table-force-nowrap"
          @change="handleRejectedTableChange"
        >
          <span slot="handoverStatus" slot-scope="text, record">
            <a-badge :status="getStatusBadgeStatus(record.handoverStatus)" :text="getStatusText(record.handoverStatus)" />
          </span>
          <span slot="action" slot-scope="text, record">
            <a-button @click="handleDetail(record)" size="small">
              <a-icon type="eye" />详情
            </a-button>
          </span>
        </a-table>
      </a-tab-pane>
      
      <a-tab-pane key="completed" tab="已完成">
        <a-table
          ref="completedTable"
          size="middle"
          :scroll="{ x: true }"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="completedDataSource"
          :pagination="completedPagination"
          :loading="completedLoading"
          class="j-table-force-nowrap"
          @change="handleCompletedTableChange"
        >
          <span slot="handoverStatus" slot-scope="text, record">
            <a-badge :status="getStatusBadgeStatus(record.handoverStatus)" :text="getStatusText(record.handoverStatus)" />
          </span>
          <span slot="action" slot-scope="text, record">
            <a-button @click="handleDetail(record)" size="small">
              <a-icon type="eye" />详情
            </a-button>
          </span>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <!-- 审核弹窗 -->
    <j-modal
      :visible.sync="auditModal.visible"
      :width="800"
      title="代账交接审核"
      :fullscreen.sync="auditModal.fullscreen"
      :switchFullscreen="auditModal.switchFullscreen"
      @ok="handleConfirmAudit"
      @cancel="handleCancelAudit"
      :confirmLoading="auditSubmitting"
    >
      <a-form-model ref="auditForm" :model="auditForm" :rules="auditRules" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
        <a-form-model-item label="审核结果" prop="auditStatus" required>
          <a-radio-group v-model="auditForm.auditStatus">
            <a-radio value="approved">通过</a-radio>
            <a-radio value="rejected">驳回</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item label="审核备注" prop="remark">
          <a-textarea v-model="auditForm.remark" :rows="4" placeholder="请输入审核备注" :maxLength="500" />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 订单详情弹窗 -->
    <order-modal ref="orderModal" @ok="modalFormOk"></order-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { mixinDevice } from '@/utils/mixin'
import { getAction, httpAction } from '@/api/manage'
import OrderModal from '@/views/order/modules/OrderModal'
import { filterDictTextByCache } from '@/components/dict/JDictSelectUtil'

export default {
  name: 'AccountingHandoverList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    OrderModal,
  },
  data() {
    return {
      description: '会计管理-代账交接',
      url: {
        list: '/order/accountingHandover/list',
      },
      queryParam: {
        orderNo: '',
        companyName: '',
        handoverStatus: 'pending', // 默认查询待审核的
      },
      activeTab: 'pending', // 当前激活的标签页
      // 审核通过的数据
      approvedDataSource: [],
      approvedPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      approvedLoading: false,
      // 审核驳回的数据
      rejectedDataSource: [],
      rejectedPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      rejectedLoading: false,
      // 已完成的数据
      completedDataSource: [],
      completedPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      completedLoading: false,
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
          title: '交接状态',
          align: 'center',
          dataIndex: 'handoverStatus',
          scopedSlots: { customRender: 'handoverStatus' },
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
          width: 200,
          scopedSlots: { customRender: 'action' },
        },
      ],
      auditModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        handoverId: '',
      },
      auditForm: {
        auditStatus: 'approved',
        remark: '',
      },
      auditRules: {
        auditStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
      },
      auditSubmitting: false, // 审核提交中状态
      userRoles: [], // 当前用户的角色列表
    }
  },
  created() {
    this.loadUserRoles()
  },
  watch: {
    // 监听数据源变化，打印调试信息
    dataSource: {
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          console.log('数据源变化，第一条记录:', newVal[0])
          console.log('第一条记录的handoverAudits:', newVal[0].handoverAudits)
        }
      },
      immediate: true
    }
  },
  methods: {
    getQueryParams() {
      const param = Object.assign({}, this.queryParam)
      return param
    },
    getStatusColor(status) {
      const colorMap = {
        'pending': 'orange',
        'approved': 'green',
        'rejected': 'red',
        'completed': 'blue',
      }
      return colorMap[status] || 'default'
    },
    getStatusBadgeStatus(status) {
      const statusMap = {
        'pending': 'processing',
        'approved': 'success',
        'rejected': 'error',
        'completed': 'default',
      }
      return statusMap[status] || 'default'
    },
    getStatusText(status) {
      const textMap = {
        'pending': '待审核',
        'approved': '审核通过',
        'rejected': '审核驳回',
        'completed': '已完成',
      }
      return textMap[status] || status
    },
    // 标签页切换
    handleTabChange(activeKey) {
      this.activeTab = activeKey
      this.queryParam.handoverStatus = activeKey
      if (activeKey === 'pending') {
        this.loadData()
      } else if (activeKey === 'approved') {
        this.loadApprovedData()
      } else if (activeKey === 'rejected') {
        this.loadRejectedData()
      } else if (activeKey === 'completed') {
        this.loadCompletedData()
      }
    },
    // 加载审核通过的数据
    loadApprovedData() {
      this.approvedLoading = true
      const params = Object.assign({}, this.queryParam, {
        handoverStatus: 'approved',
        pageNo: this.approvedPagination.current,
        pageSize: this.approvedPagination.pageSize,
      })
      getAction(this.url.list, params).then((res) => {
        if (res.success && res.result) {
          this.approvedDataSource = res.result.records || []
          this.approvedPagination.total = res.result.total || 0
        }
      }).finally(() => {
        this.approvedLoading = false
      })
    },
    // 加载审核驳回的数据
    loadRejectedData() {
      this.rejectedLoading = true
      const params = Object.assign({}, this.queryParam, {
        handoverStatus: 'rejected',
        pageNo: this.rejectedPagination.current,
        pageSize: this.rejectedPagination.pageSize,
      })
      getAction(this.url.list, params).then((res) => {
        if (res.success && res.result) {
          this.rejectedDataSource = res.result.records || []
          this.rejectedPagination.total = res.result.total || 0
        }
      }).finally(() => {
        this.rejectedLoading = false
      })
    },
    // 加载已完成的数据
    loadCompletedData() {
      this.completedLoading = true
      const params = Object.assign({}, this.queryParam, {
        handoverStatus: 'completed',
        pageNo: this.completedPagination.current,
        pageSize: this.completedPagination.pageSize,
      })
      getAction(this.url.list, params).then((res) => {
        if (res.success && res.result) {
          this.completedDataSource = res.result.records || []
          this.completedPagination.total = res.result.total || 0
        }
      }).finally(() => {
        this.completedLoading = false
      })
    },
    // 审核通过表格变化
    handleApprovedTableChange(pagination) {
      this.approvedPagination.current = pagination.current
      this.approvedPagination.pageSize = pagination.pageSize
      this.loadApprovedData()
    },
    // 审核驳回表格变化
    handleRejectedTableChange(pagination) {
      this.rejectedPagination.current = pagination.current
      this.rejectedPagination.pageSize = pagination.pageSize
      this.loadRejectedData()
    },
    // 已完成表格变化
    handleCompletedTableChange(pagination) {
      this.completedPagination.current = pagination.current
      this.completedPagination.pageSize = pagination.pageSize
      this.loadCompletedData()
    },
    // 加载当前用户的角色列表
    loadUserRoles() {
      const userInfo = this.$store.getters.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userId = userInfo.id || userInfo.userId
      if (userId) {
        getAction('/sys/user/queryUserRole', { userid: userId }).then((res) => {
          if (res.success && res.result) {
            this.userRoles = res.result
          }
        }).catch((err) => {
          console.error('获取用户角色失败:', err)
        })
      }
    },
    // 判断是否可以审核（基于当前用户的角色ID，不依赖角色名称）
    canAudit(record) {
      // 只有待审核状态才能审核
      if (record.handoverStatus !== 'pending') {
        console.log('canAudit: 状态不是pending', record.handoverStatus)
        return false
      }
      
      // 获取当前用户的所有角色ID（接口返回的是字符串数组，不是对象数组）
      if (!this.userRoles || this.userRoles.length === 0) {
        console.log('canAudit: 用户没有角色')
        return false
      }
      // queryUserRole 返回的是 List<String>，所以 userRoles 是字符串数组
      const userRoleIds = this.userRoles.filter(id => id)
      if (userRoleIds.length === 0) {
        console.log('canAudit: 用户角色ID为空')
        return false
      }
      
      // 检查代账交接的审核记录，看当前用户的角色是否已经审核过
      if (!record.handoverAudits || !Array.isArray(record.handoverAudits)) {
        console.log('canAudit: 没有审核记录或不是数组', record.handoverAudits)
        return false
      }
      
      if (record.handoverAudits.length === 0) {
        console.log('canAudit: 审核记录为空')
        return false
      }
      
      console.log('canAudit: 审核记录', record.handoverAudits, '用户角色ID', userRoleIds)
      
      // 查找当前用户角色对应的审核记录
      const userRoleAudits = record.handoverAudits.filter(audit => 
        audit.roleId && userRoleIds.includes(audit.roleId)
      )
      
      console.log('canAudit: 用户角色对应的审核记录', userRoleAudits)
      
      if (userRoleAudits.length === 0) {
        // 当前用户没有对应的审核记录，不显示按钮
        console.log('canAudit: 用户没有对应的审核记录')
        return false
      }
      
      // 检查当前用户的角色是否已经审核过（approved或rejected都算已审核）
      const hasAudited = userRoleAudits.some(audit => 
        audit.auditStatus === 'approved' || audit.auditStatus === 'rejected'
      )
      if (hasAudited) {
        // 当前用户角色已经审核过，不显示按钮
        console.log('canAudit: 用户已经审核过')
        return false
      }
      
      // 检查是否有当前用户角色的待审核记录（pending状态）
      const pendingAudit = userRoleAudits.find(audit => audit.auditStatus === 'pending')
      if (!pendingAudit) {
        console.log('canAudit: 没有待审核的记录')
        return false
      }
      
      console.log('canAudit: 待审核记录', pendingAudit)
      
      // 检查前一步是否已经全部完成（如果是第一步，直接可以审核）
      if (pendingAudit.stepOrder === 1) {
        console.log('canAudit: 第一步，可以审核')
        return true
      }
      
      // 如果不是第一步，需要检查前一步是否已经全部完成
      const prevStepOrder = pendingAudit.stepOrder - 1
      const prevStepAudits = record.handoverAudits.filter(audit => 
        audit.stepOrder === prevStepOrder
      )
      
      console.log('canAudit: 前一步审核记录', prevStepOrder, prevStepAudits)
      console.log('canAudit: 前一步审核记录详情', prevStepAudits.map(a => ({
        stepOrder: a.stepOrder,
        roleName: a.roleName,
        auditStatus: a.auditStatus
      })))
      
      // 前一步必须存在审核记录
      if (prevStepAudits.length === 0) {
        console.log('canAudit: 前一步没有审核记录，不能审核')
        return false
      }
      
      // 前一步的所有审核记录都必须已通过（approved）
      // 如果有任何一个不是approved（pending或rejected），都不能审核
      const prevStepAllApproved = prevStepAudits.every(audit => audit.auditStatus === 'approved')
      
      if (!prevStepAllApproved) {
        const notApproved = prevStepAudits.filter(audit => audit.auditStatus !== 'approved')
        console.log('canAudit: 前一步未全部完成，未完成的审核记录', notApproved.map(a => ({
          roleName: a.roleName,
          auditStatus: a.auditStatus
        })))
        console.log('canAudit: 前一步未全部完成，返回false，不显示审核按钮')
        return false  // 明确返回false，不显示审核按钮
      }
      
      console.log('canAudit: 前一步是否全部完成', prevStepAllApproved)
      console.log('canAudit: 最终返回', prevStepAllApproved)
      
      return prevStepAllApproved
    },
    // 判断是否可以转为合同
    canConvertToContract(record) {
      // 只有审核通过状态才能转为合同
      return record.handoverStatus === 'approved'
    },
    handleAudit(record) {
      this.auditModal.handoverId = record.id
      this.auditForm.auditStatus = 'approved'
      this.auditForm.remark = ''
      this.auditModal.visible = true
    },
    handleDetail(record) {
      // 跳转到订单详情
      getAction('/order/queryById', { id: record.orderId }).then((res) => {
        if (res.success && res.result) {
          this.$refs.orderModal.edit(res.result)
          this.$refs.orderModal.title = '订单详情'
          this.$refs.orderModal.disableSubmit = true
        } else {
          this.$message.error(res.message || '获取订单详情失败')
        }
      })
    },
    handleConfirmAudit() {
      // 防重复提交
      if (this.auditSubmitting) {
        this.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      this.$refs.auditForm.validate((valid) => {
        if (valid) {
          // 设置提交中状态
          this.auditSubmitting = true
          
          // 获取当前用户的角色ID列表（使用queryUserRole接口，需要userid参数）
          const userInfo = this.$store.getters.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
          const userId = userInfo.id || userInfo.userId
          if (!userId) {
            this.$message.error('未找到当前用户信息，无法进行审核')
            this.auditSubmitting = false
            return
          }
          getAction('/sys/user/queryUserRole', { userid: userId }).then((roleRes) => {
            let roleId = null
            if (roleRes.success && roleRes.result && roleRes.result.length > 0) {
              roleId = roleRes.result[0] // 使用第一个角色ID
            }
            if (!roleId) {
              this.$message.error('未找到当前用户角色信息')
              this.auditSubmitting = false
              return
            }
            
            // 调用审核接口
            const params = {
              handoverId: this.auditModal.handoverId,
              roleId: roleId,
              auditStatus: this.auditForm.auditStatus,
              auditRemark: this.auditForm.remark || '',
            }
            httpAction('/order/accountingHandover/audit', params, 'POST').then((res) => {
              if (res.success) {
                this.$message.success('审核成功')
                this.auditModal.visible = false
                // 根据当前标签页刷新数据
                if (this.activeTab === 'pending') {
                  this.loadData()
                } else if (this.activeTab === 'approved') {
                  this.loadApprovedData()
                } else if (this.activeTab === 'rejected') {
                  this.loadRejectedData()
                } else if (this.activeTab === 'completed') {
                  this.loadCompletedData()
                }
              } else {
                this.$message.error(res.message || '审核失败')
              }
            }).catch((err) => {
              this.$message.error('审核失败：' + (err.message || '未知错误'))
            }).finally(() => {
              this.auditSubmitting = false
            })
          }).catch((err) => {
            this.$message.error('获取用户角色失败：' + (err.message || '未知错误'))
            this.auditSubmitting = false
          })
        }
      })
    },
    handleCancelAudit() {
      this.auditModal.visible = false
      this.auditForm.auditStatus = 'approved'
      this.auditForm.remark = ''
    },
    handleConvertToContract(record) {
      this.$confirm({
        title: '确认转为合同',
        content: `确定要将订单 ${record.orderNo} 的代账交接转为合同吗？`,
        onOk: () => {
          httpAction('/order/accountingHandover/convertToContract', { handoverId: record.id }, 'POST').then((res) => {
            if (res.success) {
              this.$message.success('转为合同成功')
              // 刷新当前标签页数据
              if (this.activeTab === 'approved') {
                this.loadApprovedData()
              } else if (this.activeTab === 'completed') {
                this.loadCompletedData()
              }
            } else {
              this.$message.error(res.message || '转为合同失败')
            }
          }).catch((err) => {
            this.$message.error('转为合同失败：' + (err.message || '未知错误'))
          })
        },
      })
    },
    modalFormOk() {
      this.loadData()
    },
  },
}
</script>

<style scoped>
@import '~@assets/less/common.less'
</style>

