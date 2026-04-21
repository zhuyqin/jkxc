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
        :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        class="j-table-force-nowrap"
        @change="handleTableChange"
      >
        <span slot="orderStatus" slot-scope="text, record">
          <a-tag :color="getOrderStatusColor(record.orderStatus)">
            {{ getOrderStatusText(record) }}
          </a-tag>
        </span>
        <span slot="action" slot-scope="text, record">
          <a v-if="canCashierAudit(record)" @click="handleAudit(record)" style="margin-right: 8px">
            <a-icon type="check-circle" />审核
          </a>
          <a @click="handleDetail(record)" style="margin-right: 8px">
            <a-icon type="eye" />详情
          </a>
        </span>
      </a-table>
    </div>

    <!-- 出纳审核弹窗 -->
    <j-modal
      :visible.sync="auditModal.visible"
      :width="800"
      title="出纳审核"
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
import OrderModal from './modules/OrderModal'
import { filterDictTextByCache } from '@/components/dict/JDictSelectUtil'

export default {
  name: 'FdNewOrderList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    OrderModal,
  },
  data() {
    return {
      description: 'FD核算-新签订单（出纳审核）',
      url: {
        list: '/order/list',
      },
      queryParam: {
        orderNo: '',
        companyName: '',
        salesman: '',
        orderStatus: '1', // 只查询进行中的订单
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
          width: 120,
        },
        {
          title: '订单状态',
          align: 'center',
          dataIndex: 'orderStatus',
          scopedSlots: { customRender: 'orderStatus' },
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
      auditModal: {
        visible: false,
        fullscreen: false,
        switchFullscreen: false,
        orderId: '',
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
  methods: {
    getQueryParams() {
      const param = Object.assign({}, this.queryParam)
      return param
    },
    getOrderStatusColor(status) {
      // 如果状态是已取消（'3'），使用 volcano 颜色确保前景色和背景色有区别
      if (status === '3' || status === 3) {
        return 'volcano'
      }
      const colorMap = {
        '1': 'blue',
        '2': 'green',
      }
      return colorMap[status] || 'default'
    },
    getOrderStatusText(record) {
      if (record.orderStatus_dictText) {
        return record.orderStatus_dictText
      }
      if (record.orderStatus) {
        const text = filterDictTextByCache('order_status', record.orderStatus)
        if (text) {
          return text
        }
        const statusMap = {
          '1': '进行中',
          '2': '已完成',
        }
        return statusMap[record.orderStatus] || record.orderStatus
      }
      return '—'
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
    // 判断是否可以出纳审核（基于当前用户的角色ID，不依赖角色名称）
    canCashierAudit(record) {
      // 检查订单是否有审核流程
      if (!record.auditProcessId) {
        return false
      }
      
      // 如果订单的审核状态不是pending，说明已经审核完成或驳回，不显示按钮
      if (record.auditStatus !== 'pending') {
        return false
      }
      
      // 获取当前用户的所有角色ID（接口返回的是字符串数组，不是对象数组）
      if (!this.userRoles || this.userRoles.length === 0) {
        return false
      }
      // queryUserRole 返回的是 List<String>，所以 userRoles 是字符串数组
      const userRoleIds = this.userRoles.filter(id => id)
      if (userRoleIds.length === 0) {
        return false
      }
      
      // 检查订单的审核记录，看当前用户的角色是否已经审核过
      if (record.orderAudits && Array.isArray(record.orderAudits) && record.orderAudits.length > 0) {
        // 查找当前用户角色对应的审核记录
        const userRoleAudits = record.orderAudits.filter(audit => 
          audit.roleId && userRoleIds.includes(audit.roleId)
        )
        
        if (userRoleAudits.length === 0) {
          // 当前用户没有对应的审核记录，不显示按钮
          return false
        }
        
        // 检查当前用户的角色是否已经审核过（approved或rejected都算已审核）
        const hasAudited = userRoleAudits.some(audit => 
          audit.auditStatus === 'approved' || audit.auditStatus === 'rejected'
        )
        if (hasAudited) {
          // 当前用户角色已经审核过，不显示按钮
          return false
        }
        
        // 检查是否有当前用户角色的待审核记录（pending状态）
        const pendingAudit = userRoleAudits.find(audit => audit.auditStatus === 'pending')
        if (!pendingAudit) {
          return false
        }
        
        // 检查前一步是否已经全部完成（如果是第一步，直接可以审核）
        if (pendingAudit.stepOrder === 1) {
          return true
        }
        
        // 如果不是第一步，需要检查前一步是否已经全部完成
        const prevStepOrder = pendingAudit.stepOrder - 1
        const prevStepAudits = record.orderAudits.filter(audit => audit.stepOrder === prevStepOrder)
        
        // 前一步的所有审核记录都必须已通过
        const prevStepAllApproved = prevStepAudits.length > 0 && prevStepAudits.every(audit => audit.auditStatus === 'approved')
        
        return prevStepAllApproved
      }
      
      // 如果没有审核记录信息，不显示按钮（避免误显示）
      return false
    },
    handleAudit(record) {
      this.auditModal.orderId = record.id
      this.auditForm.auditStatus = 'approved'
      this.auditForm.remark = ''
      this.auditModal.visible = true
    },
    handleDetail(record) {
      getAction('/order/queryById', { id: record.id }).then((res) => {
        if (res.success && res.result) {
          this.$refs.orderModal.edit(res.result)
          this.$refs.orderModal.title = '详情'
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
          
          // 获取当前用户信息
          const userInfo = this.$store.getters.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
          const userId = userInfo.id || userInfo.userId
          
          if (!userId) {
            this.$message.error('未找到当前用户信息，无法进行审核')
            this.auditSubmitting = false
            return
          }
          
          // 获取当前用户的角色ID列表（使用queryUserRole接口，需要userid参数）
          getAction('/sys/user/queryUserRole', { userid: userId }).then((roleRes) => {
            let roleId = null
            if (roleRes.success && roleRes.result && roleRes.result.length > 0) {
              // 接口返回的是角色ID列表
              roleId = roleRes.result[0] // 使用第一个角色ID
            } else if (userInfo.roleIds && userInfo.roleIds.length > 0) {
              // 如果接口返回为空，使用用户信息中的角色ID
              roleId = userInfo.roleIds[0]
            }
            
            if (!roleId) {
              this.$message.error('未找到当前用户的角色信息，无法进行审核。请确保用户已分配角色。')
              this.auditSubmitting = false
              return
            }
            
            const params = {
              orderId: this.auditModal.orderId,
              roleId: roleId,
              auditStatus: this.auditForm.auditStatus,
              auditRemark: this.auditForm.remark,
            }
            // 调用订单审核接口
            httpAction('/order/audit', params, 'post').then((res) => {
              if (res.success) {
                this.$message.success('审核成功')
                this.handleCancelAudit()
                this.loadData()
              } else {
                this.$message.error(res.message || '审核失败')
              }
            }).catch((err) => {
              console.error('审核失败', err)
              this.$message.error('审核失败：' + (err.message || '网络错误'))
            }).finally(() => {
              this.auditSubmitting = false
            })
          }).catch((err) => {
            console.error('获取用户角色失败', err)
            // 如果获取角色失败，尝试使用用户信息中的角色
            if (userInfo.roleIds && userInfo.roleIds.length > 0) {
              const params = {
                orderId: this.auditModal.orderId,
                roleId: userInfo.roleIds[0],
                auditStatus: this.auditForm.auditStatus,
                auditRemark: this.auditForm.remark,
              }
              httpAction('/order/audit', params, 'post').then((res) => {
                if (res.success) {
                  this.$message.success('审核成功')
                  this.handleCancelAudit()
                  this.loadData()
                } else {
                  this.$message.error(res.message || '审核失败')
                }
              }).catch((err) => {
                console.error('审核失败', err)
                this.$message.error('审核失败：' + (err.message || '网络错误'))
              }).finally(() => {
                this.auditSubmitting = false
              })
            } else {
              this.$message.error('获取用户角色信息失败，请检查用户是否已分配角色')
              this.auditSubmitting = false
            }
          })
        }
      })
    },
    handleCancelAudit() {
      this.auditModal.visible = false
      this.auditForm = {
        auditStatus: 'approved',
        remark: '',
      }
      this.auditSubmitting = false
      this.$refs.auditForm && this.$refs.auditForm.resetFields()
    },
  },
}
</script>

<style scoped>
</style>

