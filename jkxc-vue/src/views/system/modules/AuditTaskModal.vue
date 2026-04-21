<template>
  <a-modal
    :title="title"
    :width="900"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭"
    :maskClosable="false"
    :footer="null"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules" :labelCol="{span: 5}" :wrapperCol="{span: 19}">
        <!-- 任务信息 -->
        <a-card title="任务信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="订单编号">{{ taskInfo.orderNo }}</a-descriptions-item>
            <a-descriptions-item label="审核角色">
              <a-tag color="blue">{{ taskInfo.currentRoleName }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="步骤顺序">步骤 {{ taskInfo.stepOrder }}</a-descriptions-item>
            <a-descriptions-item label="任务类型">
              <a-tag :color="taskInfo.taskType === 'once' ? 'blue' : 'green'">
                {{ taskInfo.taskType === 'once' ? '一次性任务' : '周期任务' }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 审核表单 -->
        <a-card title="审核表单" :bordered="false" style="margin-bottom: 16px;">
          <!-- 如果有表单配置或指标，显示表单区域 -->
          <template v-if="formConfig || (indicators && indicators.length > 0)">
            <!-- 如果有表单配置（且有字段），显示提示信息 -->
            <div v-if="hasFormFields">
              <a-alert
                message="表单配置"
                description="表单设计器待实现，将根据配置动态渲染表单组件和特殊指标。"
                type="info"
                show-icon
              />
            </div>
            
            <!-- 特殊指标展示区域 -->
            <div v-if="indicators && indicators.length > 0" :style="{ marginTop: hasFormFields ? '16px' : '0' }">
              <a-divider v-if="hasFormFields">特殊指标</a-divider>
              <div v-for="(indicator, index) in indicators" :key="index" 
                   style="margin-bottom: 16px; padding: 16px; border: 2px dashed #1890ff; border-radius: 4px; background: #f0f9ff;">
                <!-- 下个流程审批人员选择 -->
                <template v-if="indicator.indicatorType === 'next_auditor'">
                  <div style="margin-bottom: 12px;">
                    <a-tag color="blue">{{ indicator.indicatorName || '下个流程审批人员选择' }}</a-tag>
                  </div>
                  <a-form-model-item label="选择审批人员" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                    <j-select-user-by-dep
                      v-model="model.auditData[`indicator_${indicator.id}`]"
                      :multi="false"
                      store="id"
                      text="realname"
                      :role-id="indicator.config && indicator.config.roleId ? indicator.config.roleId : null"
                      placeholder="请选择下个审批人员"
                    />
                  </a-form-model-item>
                </template>
                
                <!-- 成本填写 -->
                <template v-if="indicator.indicatorType === 'cost_input'">
                  <div style="margin-bottom: 12px;">
                    <a-tag color="green">{{ indicator.indicatorName || '成本填写' }}</a-tag>
                  </div>
                  <div v-for="(item, itemIndex) in (model.auditData[`indicator_${indicator.id}`] || [])" :key="itemIndex" 
                       style="margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #e8e8e8; border-radius: 4px;">
                    <a-form-model-item label="报销名称" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                      <a-input v-model="item.expenseName" placeholder="请输入报销名称" />
                    </a-form-model-item>
                    <a-form-model-item label="报销类目" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                      <j-category-select
                        v-model="item.categoryId"
                        back="label"
                        pcode="A02"
                        placeholder="请选择报销类目"
                      />
                    </a-form-model-item>
                    <a-form-model-item label="金额" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                      <a-input-number v-model="item.amount" :min="0" :precision="2" placeholder="请输入金额" style="width: 100%;" />
                    </a-form-model-item>
                    <a-form-model-item label="备注" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                      <a-input v-model="item.remark" placeholder="请输入备注" />
                    </a-form-model-item>
                    <div style="text-align: right;">
                      <a-button type="danger" size="small" @click="handleRemoveCostItem(indicator.id, itemIndex)" icon="delete">
                        删除
                      </a-button>
                    </div>
                  </div>
                    <a-button type="dashed" @click="handleAddCostItem(indicator.id)" icon="plus" style="width: 100%;">
                    添加报销项
                  </a-button>
                </template>
                
                <!-- 转为合同 -->
                <template v-if="indicator.indicatorType === 'convert_contract'">
                  <div style="margin-bottom: 12px;">
                    <a-tag color="orange">{{ indicator.indicatorName || '转为合同' }}</a-tag>
                  </div>
                  <a-form-model-item label="是否转为合同" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                    <a-radio-group 
                      :value="model.auditData[`indicator_${indicator.id}`] || 'no'"
                      @change="(e) => handleConvertContractChange(indicator.id, e.target.value)">
                      <a-radio value="yes">是</a-radio>
                      <a-radio value="no">否</a-radio>
                    </a-radio-group>
                    <div style="margin-top: 8px; color: #999; font-size: 12px;">
                      <a-icon type="info-circle" /> 选择"是"时，审批流程完成后将自动创建合同进入业务管理-代账管理
                    </div>
                  </a-form-model-item>
                </template>
                
                <!-- 转为地址 -->
                <template v-if="indicator.indicatorType === 'convert_address'">
                  <div style="margin-bottom: 12px;">
                    <a-tag color="purple">{{ indicator.indicatorName || '转为地址' }}</a-tag>
                  </div>
                  <a-form-model-item label="是否转为地址" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                    <a-radio-group 
                      :value="model.auditData[`indicator_${indicator.id}`] || 'no'"
                      @change="(e) => handleConvertAddressChange(indicator.id, e.target.value)">
                      <a-radio value="yes">是</a-radio>
                      <a-radio value="no">否</a-radio>
                    </a-radio-group>
                    <div style="margin-top: 8px; color: #999; font-size: 12px;">
                      <a-icon type="info-circle" /> 选择"是"时，审批流程完成后将自动创建地址进入业务管理-地址管理
                    </div>
                  </a-form-model-item>
                </template>
              </div>
            </div>
          </template>
          <a-empty v-else description="暂无表单配置" style="padding: 40px 0" />
        </a-card>

        <!-- 审核操作 -->
        <a-card title="审核操作" :bordered="false">
          <a-form-model-item label="审核备注" :labelCol="{span: 5}" :wrapperCol="{span: 19}" :required="remarkRequired">
            <a-textarea 
              v-model="model.auditRemark" 
              placeholder="请输入审核备注" 
              :rows="4"
              :maxLength="500"
            />
          </a-form-model-item>
          
          <a-form-model-item 
            v-if="showRejectReason"
            label="驳回原因" 
            :labelCol="{span: 5}" 
            :wrapperCol="{span: 19}" 
            :required="true">
            <a-textarea 
              v-model="model.rejectReason" 
              placeholder="请输入驳回原因" 
              :rows="3"
              :maxLength="500"
            />
          </a-form-model-item>

          <div style="text-align: center; margin-top: 24px;">
            <a-button type="primary" @click="handleSubmit('approved')" style="margin-right: 8px;" icon="check-circle">
              通过
            </a-button>
            <a-button @click="handleSubmit('rejected')" style="margin-right: 8px;" icon="close-circle">
              不通过
            </a-button>
            <a-button type="danger" @click="handleShowRejectReason" icon="rollback">
              驳回
            </a-button>
          </div>
        </a-card>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
  import { queryAuditTaskById, executeAudit, getStepForm } from '@/api/api'

  export default {
    name: 'AuditTaskModal',
    data() {
      return {
        title: '审核任务',
        visible: false,
        confirmLoading: false,
        taskInfo: {},
        formConfig: null,
        indicators: [],
        remarkRequired: true,
        showRejectReason: false,
        model: {
          auditRemark: '',
          rejectReason: '',
          auditData: {} // 审核数据，包含表单填写的数据和特殊指标数据
        },
        validatorRules: {
          auditRemark: [{ required: true, message: '请输入审核备注!', trigger: 'blur' }]
        }
      }
    },
    computed: {
      // 判断是否有表单字段配置
      hasFormFields() {
        if (!this.formConfig || !this.formConfig.formConfig) {
          return false
        }
        const formConfigStr = this.formConfig.formConfig
        if (formConfigStr === '{}' || formConfigStr === '{"fields":[]}') {
          return false
        }
        return true
      }
    },
    methods: {
      open(record) {
        console.log('[AuditTaskModal] open方法被调用, record:', record)
        this.taskInfo = record
        this.model = {
          auditRemark: '',
          rejectReason: '',
          auditData: {}
        }
        this.showRejectReason = false
        this.visible = true
        
        // 加载任务详情和表单配置
        this.loadTaskDetail(record.id)
      },
      loadTaskDetail(taskId) {
        console.log('[AuditTaskModal] loadTaskDetail方法被调用, taskId:', taskId)
        this.confirmLoading = true
        queryAuditTaskById({id: taskId}).then((res) => {
          console.log('[AuditTaskModal] queryAuditTaskById响应:', res)
          if (res.success && res.result) {
            this.taskInfo = res.result
            console.log('[AuditTaskModal] taskInfo.stepId:', res.result.stepId)
            
            // 加载步骤表单配置
            if (res.result.stepId) {
              this.loadStepForm(res.result.stepId)
            } else {
              console.warn('[AuditTaskModal] taskInfo中没有stepId，无法加载表单配置')
            }
          } else {
            console.warn('[AuditTaskModal] queryAuditTaskById返回失败:', res)
          }
        }).catch(err => {
          console.error('[AuditTaskModal] queryAuditTaskById异常:', err)
        }).finally(() => {
          this.confirmLoading = false
        })
      },
      loadStepForm(stepId) {
        console.log('[AuditTaskModal] loadStepForm方法被调用, stepId:', stepId)
        getStepForm({stepId: stepId}).then((res) => {
          console.log('[AuditTaskModal] getStepForm响应:', res)
          if (res.success && res.result) {
            console.log('[AuditTaskModal] getStepForm返回的原始数据:', JSON.stringify(res.result, null, 2))
            this.indicators = res.result.indicators || []
            console.log('[AuditTaskModal] 解析后的indicators:', this.indicators)
            console.log('[AuditTaskModal] indicators长度:', this.indicators.length)
            
            // 如果有表单配置，设置formConfig；如果有指标但没有表单配置，也设置一个空对象以显示指标
            if (res.result.form) {
              console.log('[AuditTaskModal] 找到表单配置:', res.result.form)
              this.formConfig = res.result.form
              // 将数字转换为Boolean：1 -> true, 0 -> false
              this.remarkRequired = this.formConfig.remarkRequired !== undefined ? !!this.formConfig.remarkRequired : true
            } else if (this.indicators && this.indicators.length > 0) {
              // 只有指标没有表单配置时，也设置formConfig为一个对象，以便显示指标区域
              console.log('[AuditTaskModal] 没有表单配置，但有指标，设置formConfig为对象')
              this.formConfig = { remarkRequired: 1 }
              this.remarkRequired = true
            } else {
              console.log('[AuditTaskModal] 没有表单配置，也没有指标，设置formConfig为null')
              this.formConfig = null
              this.remarkRequired = true
            }
            
            console.log('[AuditTaskModal] 最终的formConfig:', this.formConfig)
            console.log('[AuditTaskModal] 最终的indicators:', this.indicators)
            console.log('[AuditTaskModal] 判断条件 formConfig || (indicators && indicators.length > 0):', this.formConfig || (this.indicators && this.indicators.length > 0))
            
            // 解析indicators的config（从后端返回的indicatorConfig是JSON字符串）
            if (this.indicators && this.indicators.length > 0) {
              this.indicators.forEach(indicator => {
                // 解析indicatorConfig（如果是字符串，需要解析为对象）
                if (indicator.indicatorConfig && typeof indicator.indicatorConfig === 'string') {
                  try {
                    indicator.config = JSON.parse(indicator.indicatorConfig)
                    console.log('[AuditTaskModal] 解析indicatorConfig成功:', indicator.indicatorType, indicator.config)
                  } catch (e) {
                    console.error('[AuditTaskModal] 解析indicatorConfig失败:', e, indicator.indicatorConfig)
                    indicator.config = {}
                  }
                } else if (!indicator.config) {
                  indicator.config = {}
                }
                
                // 确保使用$set来触发响应式更新
                if (indicator.config) {
                  this.$set(indicator, 'config', indicator.config)
                }
                
                // 初始化审核数据
                if (indicator.indicatorType === 'cost_input') {
                  if (!this.model.auditData[`indicator_${indicator.id}`]) {
                    this.$set(this.model.auditData, `indicator_${indicator.id}`, [])
                  }
                } else if (indicator.indicatorType === 'next_auditor') {
                  if (!this.model.auditData[`indicator_${indicator.id}`]) {
                    this.$set(this.model.auditData, `indicator_${indicator.id}`, null)
                  }
                } else if (indicator.indicatorType === 'convert_contract') {
                  if (!this.model.auditData[`indicator_${indicator.id}`]) {
                    this.$set(this.model.auditData, `indicator_${indicator.id}`, 'no')
                  }
                } else if (indicator.indicatorType === 'convert_address') {
                  if (!this.model.auditData[`indicator_${indicator.id}`]) {
                    this.$set(this.model.auditData, `indicator_${indicator.id}`, 'no')
                  }
                }
              })
            }
          } else {
            console.warn('[AuditTaskModal] getStepForm返回失败或结果为空:', res)
            this.formConfig = null
            this.indicators = []
            this.remarkRequired = true
          }
        }).catch(err => {
          console.error('[AuditTaskModal] 加载表单配置失败:', err)
          this.formConfig = null
          this.indicators = []
          this.remarkRequired = true
        })
      },
      handleConvertContractChange(indicatorId, value) {
        this.$set(this.model.auditData, `indicator_${indicatorId}`, value)
      },
      handleConvertAddressChange(indicatorId, value) {
        this.$set(this.model.auditData, `indicator_${indicatorId}`, value)
      },
      handleAddCostItem(indicatorId) {
        if (!this.model.auditData[`indicator_${indicatorId}`]) {
          this.$set(this.model.auditData, `indicator_${indicatorId}`, [])
        }
        this.model.auditData[`indicator_${indicatorId}`].push({
          expenseName: '',
          categoryId: '',
          amount: 0,
          remark: ''
        })
      },
      handleRemoveCostItem(indicatorId, itemIndex) {
        this.model.auditData[`indicator_${indicatorId}`].splice(itemIndex, 1)
      },
      handleShowRejectReason() {
        this.showRejectReason = true
        this.$nextTick(() => {
          this.validatorRules.rejectReason = [{ required: true, message: '请输入驳回原因!', trigger: 'blur' }]
        })
      },
      handleSubmit(auditResult) {
        // 验证
        if (this.remarkRequired && !this.model.auditRemark) {
          this.$message.warning('请输入审核备注！')
          return false
        }
        
        if (auditResult === 'returned' && !this.model.rejectReason) {
          this.$message.warning('请输入驳回原因！')
          return false
        }
        
        // 处理auditData，确保next_auditor指标的数据格式正确
        const processedAuditData = { ...this.model.auditData }
        
        // 遍历所有指标，处理next_auditor类型
        if (this.indicators && this.indicators.length > 0) {
          this.indicators.forEach(indicator => {
            if (indicator.indicatorType === 'next_auditor') {
              const indicatorKey = `indicator_${indicator.id}`
              const selectedUser = processedAuditData[indicatorKey]
              
              console.log('[AuditTaskModal] 处理next_auditor指标:', indicatorKey, selectedUser)
              
              // 如果选择了用户，转换为后端期望的格式
              if (selectedUser) {
                // j-select-user-by-dep组件返回的可能是字符串（用户ID）或对象
                if (typeof selectedUser === 'string') {
                  // 如果是字符串，需要查询用户信息（这里简化处理，假设格式为 "userId,userName"）
                  // 实际上j-select-user-by-dep应该返回完整的用户对象
                  processedAuditData[indicatorKey] = {
                    userId: selectedUser,
                    userName: '', // 需要从用户列表中获取
                    nextStepId: indicator.config && indicator.config.nextStepId ? indicator.config.nextStepId : null
                  }
                } else if (typeof selectedUser === 'object' && selectedUser.key) {
                  // 如果是对象，提取userId和userName
                  processedAuditData[indicatorKey] = {
                    userId: selectedUser.key || selectedUser.id || selectedUser.userId,
                    userName: selectedUser.label || selectedUser.name || selectedUser.userName || selectedUser.realname,
                    nextStepId: indicator.config && indicator.config.nextStepId ? indicator.config.nextStepId : null
                  }
                } else if (typeof selectedUser === 'object') {
                  // 确保包含必要的字段
                  processedAuditData[indicatorKey] = {
                    userId: selectedUser.userId || selectedUser.id || selectedUser.key,
                    userName: selectedUser.userName || selectedUser.name || selectedUser.label || selectedUser.realname,
                    nextStepId: selectedUser.nextStepId || (indicator.config && indicator.config.nextStepId ? indicator.config.nextStepId : null)
                  }
                }
                
                console.log('[AuditTaskModal] 转换后的next_auditor数据:', processedAuditData[indicatorKey])
              }
            }
          })
        }
        
        // 构建提交数据
        const submitData = {
          taskId: this.taskInfo.id,
          auditResult: auditResult,
          auditData: JSON.stringify(processedAuditData),
          auditRemark: this.model.auditRemark,
          rejectReason: auditResult === 'returned' ? this.model.rejectReason : null
        }
        
        console.log('[AuditTaskModal] 提交的数据:', submitData)
        console.log('[AuditTaskModal] auditData JSON:', submitData.auditData)
        
        this.confirmLoading = true
        executeAudit(submitData).then((res) => {
          if (res.success) {
            this.$message.success('审核成功！')
            this.$emit('ok')
            this.visible = false
          } else {
            this.$message.warning(res.message || '审核失败！')
          }
        }).finally(() => {
          this.confirmLoading = false
        })
      },
      handleOk() {
        // 不在这里处理，由按钮处理
      },
      handleCancel() {
        this.visible = false
        this.$refs.form.resetFields()
        this.model = {
          auditRemark: '',
          rejectReason: '',
          auditData: {}
        }
        this.showRejectReason = false
      }
    }
  }
</script>

<style scoped>
  /* 特殊指标区域样式 */
  .indicator-item {
    border: 2px dashed #1890ff;
    border-radius: 4px;
    background: #f0f9ff;
    padding: 16px;
    margin-bottom: 16px;
  }
</style>

