<template>
  <a-modal
    :title="title"
    :width="1000"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭"
    :maskClosable="false"
  >
    <a-spin :spinning="confirmLoading">
      <!-- 步骤表单配置弹窗 -->
      <step-form-config-modal
        ref="stepFormConfigModal"
        @ok="handleStepFormConfigOk"
      />
      
      <a-form-model ref="form" :model="model" :rules="validatorRules" :labelCol="{span: 5}" :wrapperCol="{span: 19}">
        <!-- 基本信息 -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-model-item prop="processName" required label="流程名称">
              <a-input placeholder="请输入流程名称（如：订单审批）" v-model="model.processName" :maxLength="100"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item prop="processCode" required label="流程编码">
              <a-input placeholder="请输入流程编码（唯一标识）" v-model="model.processCode" :maxLength="50"/>
            </a-form-model-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-model-item prop="status" label="状态">
              <a-radio-group v-model="model.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item prop="description" label="流程描述">
              <a-textarea v-model="model.description" placeholder="请输入流程描述（可选）" :rows="2" :maxLength="500"/>
            </a-form-model-item>
          </a-col>
        </a-row>

        <!-- 业务类型绑定配置 -->
        <a-divider style="margin: 24px 0 16px 0;">业务类型绑定</a-divider>
        <a-alert
          message="提示"
          description="业务类型必须选择到最底层节点（叶子节点），一个流程可以绑定多个业务类型。"
          type="info"
          show-icon
          style="margin-bottom: 16px;"
        />
        <a-form-model-item label="业务类型" :labelCol="{span: 5}" :wrapperCol="{span: 19}">
          <div style="margin-bottom: 8px;">
            <a-button type="dashed" @click="handleAddBusinessType" icon="plus" size="small">
              添加业务类型
            </a-button>
          </div>
          <div v-if="model.bindings && model.bindings.length > 0">
            <div v-for="(binding, index) in model.bindings" :key="index" style="margin-bottom: 12px; padding: 12px; border: 1px solid #e8e8e8; border-radius: 4px; background: #fafafa;">
              <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div style="flex: 1; margin-right: 12px;">
                  <j-category-select
                    v-model="binding.businessTypeId"
                    back="label"
                    pcode="A01"
                    placeholder="请选择业务类型（必须选择到最底层）"
                    :disabled="formDisabled"
                    loadTriggleChange
                    @change="(value) => handleBusinessTypeChange(value, binding)"
                    @select="(value) => handleBusinessTypeChange(value, binding)"
                    style="width: 100%;"
                  />
                  <div v-if="binding.businessTypeName" style="margin-top: 8px; color: #52c41a;">
                    <a-icon type="check-circle" /> 已选择：{{ binding.businessTypeName }}
                  </div>
                </div>
                <div style="flex: 0 0 200px; margin-right: 12px;">
                  <a-select
                    v-model="binding.taskType"
                    placeholder="任务类型"
                    style="width: 100%;"
                    :disabled="formDisabled"
                  >
                    <a-select-option value="once">一次性任务</a-select-option>
                    <a-select-option value="recurring">周期任务</a-select-option>
                  </a-select>
                </div>
                <div style="flex: 0 0 auto;">
                  <a-button type="danger" size="small" @click="handleRemoveBusinessType(index)" icon="delete">
                    删除
                  </a-button>
                </div>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无业务类型绑定，请点击上方按钮添加" style="padding: 20px 0" />
        </a-form-model-item>

        <!-- 审核步骤配置 -->
        <a-divider style="margin: 24px 0 16px 0;">审核步骤配置</a-divider>
        
        <div style="margin-bottom: 16px; text-align: right;">
          <a-button type="dashed" @click="handleAddStep" icon="plus">
            添加审核步骤
          </a-button>
        </div>

        <div v-if="model.steps && model.steps.length > 0">
          <div v-for="(step, index) in model.steps" :key="index" class="step-item" style="margin-bottom: 16px; padding: 16px; border: 1px solid #e8e8e8; border-radius: 4px; background: #fafafa;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
              <div style="display: flex; align-items: center;">
                <a-badge :count="index + 1" :number-style="{ backgroundColor: '#1890ff' }" style="margin-right: 8px;">
                  <span style="font-weight: 500; font-size: 14px;">步骤 {{ index + 1 }}</span>
                </a-badge>
              </div>
              <div class="step-actions">
                <a-button size="small" @click="handleMoveUp(index)" :disabled="index === 0" icon="arrow-up" style="margin-right: 4px">上移</a-button>
                <a-button size="small" @click="handleMoveDown(index)" :disabled="index === model.steps.length - 1" icon="arrow-down" style="margin-right: 4px">下移</a-button>
                <a-button size="small" type="danger" @click="handleRemoveStep(index)" icon="delete">删除</a-button>
              </div>
            </div>
            
            <a-form-model-item 
              :labelCol="{span: 5}" 
              :wrapperCol="{span: 19}"
              :label="`审核角色`"
              :required="true">
              <!-- 编辑模式下：已有步骤只显示，新添加的步骤可以编辑 -->
              <template v-if="model.id && step.isNew !== true">
                <div v-if="step.roleNames && step.roleNames.length > 0" style="padding: 8px 0;">
                  <a-tag v-for="(name, idx) in step.roleNames" :key="idx" color="blue" style="margin-right: 8px; margin-bottom: 4px; padding: 4px 12px;">
                    {{ name }}
                  </a-tag>
                </div>
                <div v-else style="color: #999; padding: 8px 0;">暂无角色</div>
                <div style="margin-top: 8px; color: #999; font-size: 12px;">
                  <a-icon type="info-circle" /> 已有步骤无法修改角色，如需修改请删除此步骤后重新添加
                </div>
              </template>
              <!-- 新增模式或编辑模式下新添加的步骤：显示选择器 -->
              <template v-else>
                <a-tree-select
                  :value="step.roleIds || []"
                  :treeData="roleTreeData"
                  :treeCheckable="true"
                  :treeCheckStrictly="true"
                  :showCheckedStrategy="'SHOW_ALL'"
                  :searchPlaceholder="'请选择审核角色（可多选）'"
                  :dropdownStyle="{maxHeight:'300px',overflow:'auto'}"
                  :multiple="true"
                  :allowClear="true"
                  style="width: 100%"
                  @change="(value) => handleRoleChange(value, step)">
                </a-tree-select>
                <div v-if="step.roleNames && step.roleNames.length > 0" style="margin-top: 8px;">
                  <a-tag v-for="(name, idx) in step.roleNames" :key="idx" color="blue" style="margin-right: 4px; margin-bottom: 4px;">
                    {{ name }}
                  </a-tag>
                </div>
              </template>
            </a-form-model-item>
            
            <a-form-model-item 
              :labelCol="{span: 5}" 
              :wrapperCol="{span: 19}"
              label="步骤描述">
              <a-input v-model="step.description" placeholder="步骤描述（可选）" :maxLength="500"/>
            </a-form-model-item>
            
            <!-- 步骤表单配置 -->
            <a-form-model-item 
              :labelCol="{span: 5}" 
              :wrapperCol="{span: 19}"
              label="表单配置">
              <a-button type="link" @click="handleConfigStepForm(step, index)" icon="setting" size="small">
                配置审核表单
              </a-button>
              <span v-if="step.formConfig" style="margin-left: 8px; color: #52c41a;">
                <a-icon type="check-circle" /> 已配置
              </span>
            </a-form-model-item>
          </div>
        </div>
        
        <a-empty v-else description="暂无审核步骤，请点击上方按钮添加" style="padding: 40px 0" />
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
  import { addAuditProcess, editAuditProcess, queryRoleTreeList, getStepForm } from '@/api/api'
  import StepFormConfigModal from './StepFormConfigModal'

  export default {
    name: 'AuditProcessModal',
    components: {
      StepFormConfigModal
    },
    data() {
      return {
        title: '操作',
        visible: false,
        model: {
          processName: '',
          processCode: '',
          status: 1,
          description: '',
          steps: [],
          bindings: [] // 业务类型绑定列表
        },
        confirmLoading: false,
        roleTreeData: [], // 角色树数据
        validatorRules: {
          processName: [{ required: true, message: '请输入流程名称!', trigger: 'blur' }],
          processCode: [{ required: true, message: '请输入流程编码!', trigger: 'blur' }]
        }
      }
    },
    created() {
      this.loadOperationRoles()
    },
    methods: {
      loadOperationRoles() {
        // 加载操作角色列表（roleType=1）
        queryRoleTreeList({roleType: 1}).then((res) => {
          if (res.success) {
            // 转换树形数据格式
            this.roleTreeData = this.convertToTreeSelectData(res.result || [])
          }
        })
      },
      // 将角色树数据转换为tree-select需要的格式
      convertToTreeSelectData(tree) {
        return tree.map(node => {
          const item = {
            title: node.title || node.roleName,
            value: node.id,
            key: node.id
          }
          if (node.children && node.children.length > 0) {
            item.children = this.convertToTreeSelectData(node.children)
          }
          return item
        })
      },
      // 根据角色ID数组获取角色名称数组
      getRoleNamesByIds(roleIds) {
        if (!roleIds || roleIds.length === 0) {
          return []
        }
        // 确保roleIds是字符串数组
        const roleIdStrings = roleIds.map(roleId => {
          if (typeof roleId === 'string') {
            return roleId
          } else if (roleId && typeof roleId === 'object') {
            return String(roleId.value || roleId.key || roleId.id || '')
          } else {
            return String(roleId || '')
          }
        }).filter(id => id && id !== '')
        
        const names = []
        const findRole = (nodes, ids) => {
          nodes.forEach(node => {
            const nodeValue = String(node.value || '')
            if (ids.includes(nodeValue)) {
              names.push(node.title || node.roleName || '')
            }
            if (node.children && node.children.length > 0) {
              findRole(node.children, ids)
            }
          })
        }
        findRole(this.roleTreeData, roleIdStrings)
        return names
      },
      handleAddStep() {
        if (!this.model.steps) {
          this.model.steps = []
        }
        // 添加新步骤时，标记为新增步骤，允许编辑角色
        // 生成唯一的临时ID，用于标识步骤（即使顺序改变也能找到）
        const tempId = `temp_step_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        this.model.steps.push({
          id: tempId, // 临时ID，用于标识步骤
          roleIds: [],
          roleNames: [],
          description: '',
          isNew: true // 标记为新添加的步骤
        })
      },
      handleRemoveStep(index) {
        this.model.steps.splice(index, 1)
      },
      handleMoveUp(index) {
        if (index > 0) {
          const temp = this.model.steps[index]
          this.$set(this.model.steps, index, this.model.steps[index - 1])
          this.$set(this.model.steps, index - 1, temp)
        }
      },
      handleMoveDown(index) {
        if (index < this.model.steps.length - 1) {
          const temp = this.model.steps[index]
          this.$set(this.model.steps, index, this.model.steps[index + 1])
          this.$set(this.model.steps, index + 1, temp)
        }
      },
      handleRoleChange(value, step) {
        // 处理tree-select返回的值，确保是数组格式
        let roleIds = []
        if (value) {
          if (Array.isArray(value)) {
            roleIds = value
          } else {
            roleIds = [value]
          }
        }
        // 使用 $set 确保响应式更新
        this.$set(step, 'roleIds', roleIds)
        // 更新角色名称数组（确保roleIds是字符串数组）
        const roleIdStrings = roleIds.map(roleId => {
          if (typeof roleId === 'string') {
            return roleId
          } else if (roleId && typeof roleId === 'object') {
            return String(roleId.value || roleId.key || roleId.id || '')
          } else {
            return String(roleId || '')
          }
        }).filter(id => id && id !== '')
        this.$set(step, 'roleNames', this.getRoleNamesByIds(roleIdStrings))
      },
      add() {
        // 新增流程
        this.edit({})
      },
      // 根据角色名称查找角色
      findRoleByName(roleName) {
        const findInTree = (nodes) => {
          for (const node of nodes) {
            if (node.title === roleName || node.roleName === roleName) {
              return node
            }
            if (node.children && node.children.length > 0) {
              const found = findInTree(node.children)
              if (found) return found
            }
          }
          return null
        }
        return findInTree(this.roleTreeData)
      },
      handleAddBusinessType() {
        if (!this.model.bindings) {
          this.model.bindings = []
        }
        this.model.bindings.push({
          businessTypeId: '',
          businessTypeName: '',
          businessTypeCode: '',
          taskType: 'once' // 默认一次性任务
        })
      },
      handleRemoveBusinessType(index) {
        this.model.bindings.splice(index, 1)
      },
      handleBusinessTypeChange(value, binding) {
        // 当业务类型选择变化时，需要验证是否是最底层节点
        // j-category-select 组件返回的 value 可能是数组（级联选择）或字符串
        // 需要取最后一个值作为业务类型ID
        let businessTypeId = value
        if (Array.isArray(value) && value.length > 0) {
          businessTypeId = value[value.length - 1]
        }
        
        if (businessTypeId) {
          binding.businessTypeId = businessTypeId
          // 通过字典查询获取业务类型名称（j-category-select 会自动处理）
          // 这里暂时不验证是否为最底层节点，由后端验证
          // 如果需要验证，可以调用相应的API
        } else {
          binding.businessTypeId = ''
          binding.businessTypeName = ''
          binding.businessTypeCode = ''
        }
      },
      handleConfigStepForm(step, stepIndex) {
        // 打开步骤表单配置弹窗
        // 这里需要传递步骤ID和表单配置数据
        // 确保步骤有唯一ID，如果没有则生成一个
        if (!step.id) {
          const tempId = `temp_step_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
          this.$set(step, 'id', tempId)
        }
        const stepId = step.id
        // 计算当前步骤的顺序（从1开始）
        const currentStepOrder = stepIndex + 1
        
        // 确保 roleIds 和 roleNames 存在
        const roleIds = step.roleIds || []
        let roleNames = step.roleNames || []
        
        // 如果 roleNames 为空但 roleIds 不为空，尝试获取角色名称
        if (roleIds.length > 0 && (!roleNames || roleNames.length === 0)) {
          const roleIdStrings = roleIds.map(roleId => {
            if (typeof roleId === 'string') {
              return roleId
            } else if (roleId && typeof roleId === 'object') {
              return String(roleId.value || roleId.key || roleId.id || '')
            } else {
              return String(roleId || '')
            }
          }).filter(id => id && id !== '')
          roleNames = this.getRoleNamesByIds(roleIdStrings)
          // 更新 step.roleNames
          this.$set(step, 'roleNames', roleNames)
        }
        
        // 调试日志
        console.log('[AuditProcessModal] handleConfigStepForm')
        console.log('[AuditProcessModal] step.roleIds:', step.roleIds)
        console.log('[AuditProcessModal] step.roleNames:', step.roleNames)
        console.log('[AuditProcessModal] roleIds:', roleIds)
        console.log('[AuditProcessModal] roleNames:', roleNames)
        
        // 传递所有步骤信息，用于生成后续步骤选项
        // 传递角色表单配置（如果有）
        this.$refs.stepFormConfigModal.open(stepId, step.formConfig, step.indicators, currentStepOrder, this.model.steps, roleIds, roleNames, step.roleFormConfigs)
      },
      handleStepFormConfigOk(stepId, formConfigOrAllConfigs, indicators, targetRoleId) {
        // 步骤表单配置保存后的回调
        // 找到对应的步骤并更新表单配置
        // 使用步骤的唯一ID来匹配，而不是索引
        const stepIndex = this.model.steps.findIndex(s => {
          // 优先匹配 id，如果没有则匹配 stepId
          return (s.id && s.id === stepId) || (s.stepId && s.stepId === stepId)
        })
        if (stepIndex >= 0) {
          // 如果formConfigOrAllConfigs是对象且包含多个角色的配置（多角色Tab模式）
          if (formConfigOrAllConfigs && typeof formConfigOrAllConfigs === 'object' && !formConfigOrAllConfigs.formName) {
            // 多角色配置模式：formConfigOrAllConfigs是 { roleKey: { formConfig, indicators, targetRoleId } }
            // 保存所有角色的配置
            this.$set(this.model.steps[stepIndex], 'roleFormConfigs', formConfigOrAllConfigs)
            // 标记已配置
            this.$set(this.model.steps[stepIndex], 'formConfig', { hasConfig: true })
          } else {
            // 单角色配置模式（兼容旧逻辑）
            this.$set(this.model.steps[stepIndex], 'formConfig', formConfigOrAllConfigs)
            this.$set(this.model.steps[stepIndex], 'indicators', indicators)
            if (targetRoleId !== undefined) {
              this.$set(this.model.steps[stepIndex], 'formTargetRoleId', targetRoleId)
            }
          }
          // 强制更新视图，确保"已配置"标识显示
          this.$forceUpdate()
        } else {
          console.warn(`未找到步骤ID为 ${stepId} 的步骤`)
        }
      },
      edit(record) {
        this.model = Object.assign({}, {
          processName: record.processName || '',
          processCode: record.processCode || '',
          status: record.status !== undefined ? record.status : 1,
          description: record.description || '',
          bindings: record.bindings ? record.bindings.map(binding => ({
            businessTypeId: binding.businessTypeId || '',
            businessTypeName: binding.businessTypeName || '',
            businessTypeCode: binding.businessTypeCode || '',
            taskType: binding.taskType || 'once'
          })) : [],
          steps: record.steps ? record.steps.map((step, index) => {
            // 兼容旧数据格式（单个角色）和新格式（多个角色）
            let stepObj = {}
            if (step.roleIds && Array.isArray(step.roleIds)) {
              stepObj = {
                id: step.id || step.stepId || null, // 保存步骤ID，用于加载表单配置
                roleIds: step.roleIds,
                roleNames: step.roleNames || [],
                description: step.description || '',
                formConfig: step.formConfig || null, // 保存表单配置
                indicators: step.indicators || null, // 保存指标配置
                roleFormConfigs: step.roleFormConfigs || null // 保存多角色表单配置（Tab模式）
              }
            } else {
              // 如果是旧格式（单个角色）
              stepObj = {
                id: step.id || step.stepId || null,
                roleIds: step.roleId ? [step.roleId] : [],
                roleNames: step.roleName ? [step.roleName] : [],
                description: step.description || '',
                formConfig: step.formConfig || null,
                indicators: step.indicators || null,
                roleFormConfigs: step.roleFormConfigs || null
              }
            }
            return stepObj
          }) : []
        })
        if (record.id) {
          this.model.id = record.id
        }
        this.visible = true
        
        // 如果有步骤ID，为每个步骤加载表单配置
        if (this.model.steps && this.model.steps.length > 0) {
          this.loadStepsFormConfig()
        }
      },
      // 加载所有步骤的表单配置
      loadStepsFormConfig() {
        if (!this.model.steps || this.model.steps.length === 0) {
          return
        }
        
        // 为每个有ID的步骤加载表单配置
        const loadPromises = this.model.steps.map((step, index) => {
          // 步骤ID可能在 id 或 stepId 字段中
          const stepId = step.id || step.stepId
          if (stepId) {
            return getStepForm({ stepId: stepId }).then((res) => {
              if (res.success && res.result) {
                const formData = res.result.form
                const indicators = res.result.indicators || []
                
                if (formData) {
                  // 更新步骤的表单配置
                  this.$set(this.model.steps[index], 'formConfig', {
                    formName: formData.formName || '',
                    formConfig: formData.formConfig || '',
                    remarkRequired: formData.remarkRequired !== undefined ? formData.remarkRequired : 1
                  })
                }
                
                if (indicators && indicators.length > 0) {
                  // 解析指标配置
                  const parsedIndicators = indicators.map(indicator => {
                    let config = {}
                    if (indicator.indicatorConfig) {
                      if (typeof indicator.indicatorConfig === 'string') {
                        try {
                          config = JSON.parse(indicator.indicatorConfig)
                        } catch (e) {
                          config = {}
                        }
                      } else {
                        config = indicator.indicatorConfig
                      }
                    }
                    return {
                      id: indicator.id,
                      indicatorType: indicator.indicatorType,
                      indicatorName: indicator.indicatorName || '',
                      config: config,
                      sortOrder: indicator.sortOrder || 0
                    }
                  })
                  this.$set(this.model.steps[index], 'indicators', parsedIndicators)
                }
              }
            }).catch(err => {
              console.error(`加载步骤 ${stepId} 的表单配置失败:`, err)
            })
          }
          return Promise.resolve()
        })
        
        // 等待所有配置加载完成
        Promise.all(loadPromises).then(() => {
          // 所有配置加载完成后，强制更新视图
          this.$forceUpdate()
        })
      },
      // 确定
      handleOk() {
        const that = this
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            // 验证步骤
            if (!that.model.steps || that.model.steps.length === 0) {
              that.$message.warning('请至少添加一个审核步骤！')
              return false
            }
            
            // 验证每个步骤都选择了角色
            for (let i = 0; i < that.model.steps.length; i++) {
              const step = that.model.steps[i]
              if (!step.roleIds || step.roleIds.length === 0) {
                that.$message.warning(`第${i + 1}个步骤未选择审核角色！`)
                return false
              }
            }
            
            // 验证业务类型绑定（可选，但如果有绑定，必须选择到最底层）
            if (that.model.bindings && that.model.bindings.length > 0) {
              for (let i = 0; i < that.model.bindings.length; i++) {
                const binding = that.model.bindings[i]
                if (!binding.businessTypeId) {
                  that.$message.warning(`第${i + 1}个业务类型绑定未选择业务类型！`)
                  return false
                }
                if (!binding.taskType) {
                  that.$message.warning(`第${i + 1}个业务类型绑定未选择任务类型！`)
                  return false
                }
              }
            }
            
            // 转换数据格式：将roleIds数组转换为后端需要的格式
            // 后端需要的是多个步骤记录，每个角色一个步骤
            // 相同stepOrder的多个角色表示这些角色需要并行审核
            const stepsForBackend = []
            // 表单配置映射：按stepOrder分组，存储表单配置
            const stepFormConfigs = {}
            
            that.model.steps.forEach((step, stepIndex) => {
              const stepOrder = stepIndex + 1
              if (step.roleIds && step.roleIds.length > 0) {
                step.roleIds.forEach((roleId, roleIndex) => {
                  // 确保roleId是字符串类型，而不是对象
                  let roleIdStr = ''
                  if (typeof roleId === 'string') {
                    roleIdStr = roleId
                  } else if (roleId && typeof roleId === 'object') {
                    // 如果是对象，提取value、key或id属性
                    roleIdStr = String(roleId.value || roleId.key || roleId.id || '')
                  } else {
                    roleIdStr = String(roleId || '')
                  }
                  
                  const roleName = step.roleNames && step.roleNames[roleIndex] ? step.roleNames[roleIndex] : ''
                  stepsForBackend.push({
                    roleId: roleIdStr, // 确保是字符串类型
                    roleName: roleName,
                    stepOrder: stepOrder, // 相同步骤的角色使用相同的stepOrder
                    description: step.description || ''
                  })
                })
              }
              
              // 保存该步骤的表单配置（按stepOrder存储，如果指定了角色则按stepOrder_roleId存储）
              // 检查是否有表单配置（formConfig对象包含formName等字段）或指标配置
              // 或者是否有roleFormConfigs（多角色Tab模式）
              if (step.roleFormConfigs && typeof step.roleFormConfigs === 'object') {
                // 多角色Tab模式：保存所有角色的配置
                for (const roleKey in step.roleFormConfigs) {
                  const roleConfig = step.roleFormConfigs[roleKey]
                  const configKey = roleKey === 'shared' ? String(stepOrder) : `${stepOrder}_${roleKey}`
                  stepFormConfigs[configKey] = {
                    formConfig: roleConfig.formConfig || null,
                    indicators: roleConfig.indicators || [],
                    targetRoleId: roleConfig.targetRoleId || null
                  }
                }
              } else if ((step.formConfig && (step.formConfig.formName || step.formConfig.formConfig)) || 
                  (step.indicators && step.indicators.length > 0)) {
                // 单角色配置模式（兼容旧逻辑）
                const configKey = step.formTargetRoleId ? `${stepOrder}_${step.formTargetRoleId}` : String(stepOrder)
                stepFormConfigs[configKey] = {
                  formConfig: step.formConfig || null,
                  indicators: step.indicators || [],
                  targetRoleId: step.formTargetRoleId || null
                }
              }
            })
            
            that.confirmLoading = true
            const submitData = {
              ...that.model,
              steps: stepsForBackend,
              stepFormConfigs: stepFormConfigs // 传递表单配置映射
            }
            
            submitData.processName = (submitData.processName || '').trim()
            submitData.processCode = (submitData.processCode || '').trim()
            
            let obj
            if (!this.model.id) {
              obj = addAuditProcess(submitData)
            } else {
              obj = editAuditProcess(submitData)
            }
            obj.then((res) => {
              if (res.success) {
                that.$message.success(res.message || '操作成功！')
                that.$emit('ok')
              } else {
                that.$message.warning(res.message || '操作失败！')
              }
            }).finally(() => {
              that.confirmLoading = false
              that.close()
            })
          } else {
            return false
          }
        })
      },
      // 关闭
      handleCancel() {
        this.close()
      },
      close() {
        this.$emit('close')
        this.visible = false
        this.$refs.form.resetFields()
        this.model = {
          processName: '',
          processCode: '',
          status: 1,
          description: '',
          steps: [],
          bindings: []
        }
      }
    }
  }
</script>
<style scoped>
  /* 步骤项样式 */
  .step-item {
    transition: all 0.3s;
  }
  
  .step-item:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
  }
  
  /* 按钮组样式 */
  .step-actions {
    display: flex;
    align-items: center;
  }
  
  .step-actions .ant-btn {
    margin-right: 4px;
  }
  
  .step-actions .ant-btn:last-child {
    margin-right: 0;
  }
  
  /* 表单对齐优化 - 左对齐 */
  /deep/ .ant-form-model-item-label {
    text-align: left;
    padding-right: 8px;
    padding-left: 0;
  }
  
  /* 确保表单项整体左对齐 */
  /deep/ .ant-form-model-item {
    margin-bottom: 16px;
  }
  
  /deep/ .ant-form-model-item-control-wrapper {
    text-align: left;
  }
  
  /* 输入框左对齐 */
  /deep/ .ant-input,
  /deep/ .ant-input-group-wrapper,
  /deep/ .ant-select,
  /deep/ .ant-select-selection,
  /deep/ .ant-select-selection__rendered,
  /deep/ .ant-tree-select,
  /deep/ .ant-tree-select-selection,
  /deep/ .ant-tree-select-selection__rendered,
  /deep/ .ant-radio-group {
    text-align: left !important;
  }
  
  /deep/ .ant-input::placeholder,
  /deep/ .ant-select-selection__placeholder,
  /deep/ .ant-tree-select-selection__placeholder {
    text-align: left !important;
  }
  
  /* 确保输入框内容左对齐 */
  /deep/ .ant-input {
    text-align: left !important;
  }
  
  /deep/ .ant-textarea {
    text-align: left !important;
  }
  
  /deep/ .ant-textarea textarea {
    text-align: left !important;
  }
  
  /* 选择器内容左对齐 */
  /deep/ .ant-select-selection-selected-value,
  /deep/ .ant-tree-select-selection-selected-value {
    text-align: left !important;
  }
  
  /* 树形选择器样式 */
  /deep/ .ant-tree-select {
    width: 100%;
  }
  
  /* 标签样式 */
  /deep/ .ant-tag {
    margin-bottom: 4px;
  }
</style>
