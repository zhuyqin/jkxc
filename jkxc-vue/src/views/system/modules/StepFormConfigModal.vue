<template>
  <a-modal
    title="配置审核表单"
    :width="1200"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="关闭"
    :maskClosable="false"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="currentRoleModel" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
        <!-- 配置模式选择（当步骤有多个角色时显示） -->
        <a-form-model-item 
          v-if="stepRoles && Array.isArray(stepRoles) && stepRoles.length > 1" 
          label="配置模式" 
          :required="true">
          <a-radio-group v-model="configMode" @change="handleConfigModeChange">
            <a-radio value="shared">共同配置（所有角色使用同一表单）</a-radio>
            <a-radio value="separate">各自配置（每个角色独立配置表单）</a-radio>
          </a-radio-group>
          <div style="margin-top: 8px; color: #999; font-size: 12px;">
            <a-icon type="info-circle" />
            <span v-if="configMode === 'shared'">选择"共同配置"时，该步骤的所有角色使用同一表单配置</span>
            <span v-else>选择"各自配置"时，必须为每个角色都配置好表单，保存时会验证所有角色是否已配置</span>
          </div>
        </a-form-model-item>
        
        <!-- 各自配置模式：显示Tab切换 -->
        <a-tabs v-if="configMode === 'separate' && stepRoles && stepRoles.length > 1" v-model="activeRoleTab" @change="handleTabChange" type="card" style="margin-bottom: 16px;">
          <a-tab-pane v-for="role in stepRoles" :key="role.roleId">
            <span slot="tab">
              <a-badge :count="getRoleConfigStatus(role.roleId)" :offset="[10, 0]">
                <span>{{ role.roleName }}</span>
              </a-badge>
            </span>
          </a-tab-pane>
        </a-tabs>
        
        <!-- 当前配置的角色提示（各自配置模式） -->
        <a-alert 
          v-if="configMode === 'separate' && stepRoles && stepRoles.length > 1" 
          :message="`正在配置：${getRoleName(activeRoleTab)}`"
          type="info"
          show-icon
          style="margin-bottom: 16px;"
        />
        
        <a-form-model-item label="表单名称">
          <a-input v-model="currentRoleModel.formName" placeholder="请输入表单名称" :maxLength="100"/>
        </a-form-model-item>
        
        <a-form-model-item label="是否必填备注">
          <a-radio-group v-model="currentRoleModel.remarkRequired">
            <a-radio :value="1">是</a-radio>
            <a-radio :value="0">否</a-radio>
          </a-radio-group>
        </a-form-model-item>
        
        <a-divider>表单组件配置</a-divider>
        <a-alert
          message="提示"
          description="可以拖拽普通表单组件和特殊指标到表单中。特殊指标包括：下个流程审批人员选择、成本填写。"
          type="info"
          show-icon
          style="margin-bottom: 16px;"
        />
        
        <!-- 表单设计器区域 -->
        <a-row :gutter="16">
          <a-col :span="16">
            <a-card title="表单设计" :bordered="false" size="small">
              <div class="form-designer">
                <a-form-model ref="designForm" :model="formData" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                  <a-row :gutter="16">
                    <a-col
                      v-for="(field, index) in formFields"
                      :key="field.key || index"
                      :span="getFieldSpan(field, index)"
                    >
                      <div
                        class="form-field-item"
                        @click="selectFormField(field, index)"
                        :class="{ 'selected': selectedFormFieldIndex === index }"
                      >
                        <a-form-model-item :label="field.label" :prop="field.name">
                          <component
                            :is="getFieldComponent(field.type)"
                            v-model="formData[field.name]"
                            :placeholder="field.placeholder"
                            :disabled="field.disabled"
                            v-bind="getFieldProps(field)"
                          />
                        </a-form-model-item>
                        <div class="field-actions">
                          <a-icon type="delete" @click.stop="removeFormField(index)" title="删除" />
                          <a-icon type="copy" @click.stop="copyFormField(index)" title="复制" />
                        </div>
                      </div>
                    </a-col>
                  </a-row>
                  <div v-if="formFields.length === 0" class="empty-tip">
                    <a-empty description="点击右侧按钮添加表单字段" :image="false" />
                  </div>
                </a-form-model>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="字段库" :bordered="false" size="small">
              <div class="field-library">
                <a-button type="dashed" block @click="addFormField('input')" style="margin-bottom: 8px;">
                  <a-icon type="edit" /> 单行文本
                </a-button>
                <a-button type="dashed" block @click="addFormField('textarea')" style="margin-bottom: 8px;">
                  <a-icon type="file-text" /> 多行文本
                </a-button>
                <a-button type="dashed" block @click="addFormField('number')" style="margin-bottom: 8px;">
                  <a-icon type="number" /> 数字
                </a-button>
                <a-button type="dashed" block @click="addFormField('date')" style="margin-bottom: 8px;">
                  <a-icon type="calendar" /> 日期
                </a-button>
                <a-button type="dashed" block @click="addFormField('select')" style="margin-bottom: 8px;">
                  <a-icon type="unordered-list" /> 下拉选择
                </a-button>
                <a-button type="dashed" block @click="addFormField('radio')" style="margin-bottom: 8px;">
                  <a-icon type="check-circle" /> 单选
                </a-button>
                <a-button type="dashed" block @click="addFormField('checkbox')" style="margin-bottom: 8px;">
                  <a-icon type="check-square" /> 多选
                </a-button>
              </div>
            </a-card>
            <a-card title="字段属性" :bordered="false" size="small" style="margin-top: 16px;" v-if="selectedFormField">
              <a-form-model :model="selectedFormField" :labelCol="{span: 8}" :wrapperCol="{span: 16}">
                <a-form-model-item label="字段名称">
                  <a-input v-model="selectedFormField.name" placeholder="字段名（英文）" @change="syncSelectedFormFieldToFields"></a-input>
                </a-form-model-item>
                <a-form-model-item label="字段标签">
                  <a-input v-model="selectedFormField.label" placeholder="显示标签" @change="syncSelectedFormFieldToFields"></a-input>
                </a-form-model-item>
                <a-form-model-item label="占位符">
                  <a-input v-model="selectedFormField.placeholder" placeholder="占位符文本" @change="syncSelectedFormFieldToFields"></a-input>
                </a-form-model-item>
                <a-form-model-item label="列宽">
                  <a-select v-model="selectedFormField.span" placeholder="选择列宽" @change="syncSelectedFormFieldToFields">
                    <a-select-option :value="24">整行（24）</a-select-option>
                    <a-select-option :value="12">半行（12）</a-select-option>
                    <a-select-option :value="8">1/3行（8）</a-select-option>
                    <a-select-option :value="6">1/4行（6）</a-select-option>
                  </a-select>
                </a-form-model-item>
                <a-form-model-item label="是否必填">
                  <a-switch v-model="selectedFormField.required" @change="syncSelectedFormFieldToFields"></a-switch>
                </a-form-model-item>
                <a-form-model-item label="是否禁用">
                  <a-switch v-model="selectedFormField.disabled" @change="syncSelectedFormFieldToFields"></a-switch>
                </a-form-model-item>
                <a-form-model-item label="默认值" v-if="selectedFormField.type !== 'date' && selectedFormField.type !== 'select' && selectedFormField.type !== 'radio' && selectedFormField.type !== 'checkbox'">
                  <a-input v-model="selectedFormField.defaultValue" placeholder="默认值" @change="syncSelectedFormFieldToFields"></a-input>
                </a-form-model-item>
                <a-form-model-item label="日期类型" v-if="selectedFormField.type === 'date'">
                  <a-select v-model="selectedFormField.dateType" placeholder="选择日期类型" @change="syncSelectedFormFieldToFields">
                    <a-select-option value="date">日期（年月日）</a-select-option>
                    <a-select-option value="month">月份（年月）</a-select-option>
                    <a-select-option value="year">年份</a-select-option>
                    <a-select-option value="range">日期范围</a-select-option>
                  </a-select>
                </a-form-model-item>
                <a-form-model-item label="字典编码" v-if="['select', 'radio', 'checkbox'].includes(selectedFormField.type)">
                  <a-input v-model="selectedFormField.dictCode" placeholder="输入字典编码" @change="syncSelectedFormFieldToFields"></a-input>
                </a-form-model-item>
                <a-form-model-item label="选项" v-if="['select', 'radio', 'checkbox'].includes(selectedFormField.type) && !selectedFormField.dictCode">
                  <a-textarea
                    v-model="selectedFormField.options"
                    placeholder="每行一个选项，格式：值:文本，如：1:选项1"
                    :rows="4"
                    @change="syncSelectedFormFieldToFields"
                  ></a-textarea>
                </a-form-model-item>
              </a-form-model>
            </a-card>
          </a-col>
        </a-row>
        
        <!-- 特殊指标配置 -->
        <a-divider>特殊指标配置</a-divider>
        <div style="margin-bottom: 16px;">
          <a-button type="dashed" @click="handleAddIndicator('next_auditor')" icon="user" style="margin-right: 8px;">
            添加：下个流程审批人员选择
          </a-button>
          <a-button type="dashed" @click="handleAddIndicator('cost_input')" icon="dollar" style="margin-right: 8px;">
            添加：成本填写
          </a-button>
          <a-button type="dashed" @click="handleAddIndicator('convert_contract')" icon="file-text">
            添加：转为合同
          </a-button>
          <a-button type="dashed" @click="handleAddIndicator('convert_address')" icon="environment" style="margin-left: 8px">
            添加：转为地址
          </a-button>
        </div>
        
        <div v-if="currentRoleModel.indicators && currentRoleModel.indicators.length > 0">
          <div v-for="(indicator, index) in currentRoleModel.indicators" :key="index" 
               style="margin-bottom: 16px; padding: 16px; border: 2px dashed #1890ff; border-radius: 4px; background: #f0f9ff;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
              <div>
                <a-tag :color="getIndicatorTagColor(indicator.indicatorType)">
                  {{ getIndicatorTypeText(indicator.indicatorType) }}
                </a-tag>
                <span style="margin-left: 8px; font-weight: 500;">{{ indicator.indicatorName || '未命名' }}</span>
              </div>
              <a-button type="danger" size="small" @click="handleRemoveIndicator(index)" icon="delete">
                删除
              </a-button>
            </div>
            
            <!-- 下个流程审批人员选择配置 -->
            <template v-if="indicator.indicatorType === 'next_auditor'">
              <a-form-model-item label="指标名称" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-input v-model="indicator.indicatorName" placeholder="请输入指标名称" :maxLength="100"/>
              </a-form-model-item>
              <a-form-model-item label="角色选择" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-tree-select
                  :value="indicator.config && indicator.config.roleId ? [indicator.config.roleId] : []"
                  :treeData="roleTreeData"
                  :searchPlaceholder="'请选择角色'"
                  :dropdownStyle="{maxHeight:'300px',overflow:'auto'}"
                  :allowClear="true"
                  style="width: 100%"
                  @change="(value) => handleIndicatorConfigChange(indicator, 'roleId', value)">
                </a-tree-select>
              </a-form-model-item>
              <a-form-model-item label="环节选择" :required="true" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-select
                  :value="getSelectedStepRoleValue(indicator)"
                  :placeholder="(availableStepOrders.length > 0 || availableStepRoleOptions.length > 0) ? '请选择下个流程环节' : '暂无后续步骤，请先添加后续步骤'"
                  :disabled="availableStepOrders.length === 0 && availableStepRoleOptions.length === 0"
                  style="width: 100%"
                  @change="(value) => handleStepRoleChange(indicator, value)">
                  <!-- 显示步骤-角色选项（当步骤有多个角色时） -->
                  <a-select-option 
                    v-for="option in availableStepRoleOptions" 
                    :key="`${option.stepOrder}_${option.roleId}`" 
                    :value="`${option.stepOrder}_${option.roleId}`">
                    步骤 {{ option.stepOrder }} - {{ option.roleName }}
                  </a-select-option>
                  <!-- 显示步骤选项（当步骤只有一个角色时） -->
                  <a-select-option v-for="stepOrder in availableStepOrders" :key="stepOrder" :value="stepOrder">
                    步骤 {{ stepOrder }}
                  </a-select-option>
                </a-select>
                <div v-if="availableStepOrders.length === 0 && availableStepRoleOptions.length === 0" style="margin-top: 4px; color: #999; font-size: 12px;">
                  <a-icon type="info-circle" /> 当前是最后一个步骤，或后续步骤尚未添加。可以先配置角色，后续步骤添加后再选择环节。
                </div>
                <div v-else-if="indicator.config && indicator.config.nextStepOrder && getStepRoles(indicator.config.nextStepOrder).length === 1" style="margin-top: 4px; color: #52c41a; font-size: 12px;">
                  <a-icon type="check-circle" /> 已选择步骤{{ indicator.config.nextStepOrder }}的唯一角色：{{ getStepRoles(indicator.config.nextStepOrder)[0].roleName }}
                </div>
              </a-form-model-item>
            </template>
            
            <!-- 成本填写配置 -->
            <template v-if="indicator.indicatorType === 'cost_input'">
              <a-form-model-item label="指标名称" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-input v-model="indicator.indicatorName" placeholder="请输入指标名称" :maxLength="100"/>
              </a-form-model-item>
              <a-form-model-item label="报销名称" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-input 
                  :value="indicator.config && indicator.config.expenseName ? indicator.config.expenseName : ''"
                  @input="(e) => handleIndicatorConfigChange(indicator, 'expenseName', e.target.value)"
                  placeholder="请输入报销名称" 
                  :maxLength="100"/>
              </a-form-model-item>
              <a-form-model-item label="报销类目" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <j-category-select
                  :value="getCategoryIdsString(indicator)"
                  @change="(value) => handleCategoryChange(indicator, value)"
                  back="label"
                  pcode="A02"
                  placeholder="请选择报销类目（可多选）"
                  :multiple="true"
                  style="width: 100%;"
                />
              </a-form-model-item>
            </template>
            
            <!-- 转为合同配置 -->
            <template v-if="indicator.indicatorType === 'convert_contract'">
              <a-form-model-item label="指标名称" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-input v-model="indicator.indicatorName" placeholder="请输入指标名称" :maxLength="100"/>
              </a-form-model-item>
              <a-form-model-item label="是否转为合同" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-radio-group 
                  :value="indicator.config && indicator.config.enabled !== undefined ? (indicator.config.enabled ? 'yes' : 'no') : 'no'"
                  @change="(e) => handleIndicatorConfigChange(indicator, 'enabled', e.target.value === 'yes')">
                  <a-radio value="yes">是</a-radio>
                  <a-radio value="no">否</a-radio>
                </a-radio-group>
                <div style="margin-top: 8px; color: #999; font-size: 12px;">
                  <a-icon type="info-circle" /> 选择"是"时，当审批流程完成且该指标值为"是"时，将自动创建合同进入业务管理-代账管理
                </div>
              </a-form-model-item>
            </template>
            
            <!-- 转为地址配置 -->
            <template v-if="indicator.indicatorType === 'convert_address'">
              <a-form-model-item label="指标名称" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-input v-model="indicator.indicatorName" placeholder="请输入指标名称" :maxLength="100"/>
              </a-form-model-item>
              <a-form-model-item label="是否转为地址" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
                <a-radio-group 
                  :value="indicator.config && indicator.config.enabled !== undefined ? (indicator.config.enabled ? 'yes' : 'no') : 'no'"
                  @change="(e) => handleIndicatorConfigChange(indicator, 'enabled', e.target.value === 'yes')">
                  <a-radio value="yes">是</a-radio>
                  <a-radio value="no">否</a-radio>
                </a-radio-group>
                <div style="margin-top: 8px; color: #999; font-size: 12px;">
                  <a-icon type="info-circle" /> 选择"是"时，当审批流程完成且该指标值为"是"时，将自动创建地址进入业务管理-地址管理
                </div>
              </a-form-model-item>
            </template>
          </div>
        </div>
        
        <a-empty v-else description="暂无特殊指标，可点击上方按钮添加" style="padding: 20px 0" />
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
  import { queryRoleTreeList } from '@/api/api'

  export default {
    name: 'StepFormConfigModal',
    data() {
      return {
        visible: false,
        confirmLoading: false,
        stepId: '',
        stepRoles: [], // 当前步骤的角色列表
        configMode: 'shared', // 配置模式：'shared'（共同配置）或 'separate'（各自配置）
        activeRoleTab: '', // 当前激活的Tab（各自配置模式下使用，为roleId）
        roleFormConfigs: {}, // 每个角色的表单配置 { roleId: { formName, formConfig, remarkRequired, indicators } }
        currentRoleModel: {
          formName: '',
          formConfig: '',
          remarkRequired: 1,
          indicators: []
        },
        roleTreeData: [],
        availableStepOrders: [], // 可用的步骤顺序（用于环节选择，当步骤只有一个角色时使用）
        availableStepRoleOptions: [], // 可用的步骤-角色选项（用于环节选择，当步骤有多个角色时展开显示）
        currentStepOrder: 0, // 当前步骤顺序
        allSteps: [], // 所有步骤信息（用于获取步骤的角色信息）
        // 表单设计器相关
        formFields: [], // 表单字段列表
        formData: {}, // 表单数据（用于设计器预览）
        selectedFormField: null, // 当前选中的字段
        selectedFormFieldIndex: -1, // 当前选中字段的索引
        formFieldCounter: 0 // 字段计数器
      }
    },
    created() {
      this.loadRoles()
    },
    methods: {
      loadRoles() {
        queryRoleTreeList({roleType: 1}).then((res) => {
          if (res.success) {
            this.roleTreeData = this.convertToTreeSelectData(res.result || [])
          }
        })
      },
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
      handleConfigModeChange(e) {
        // 配置模式切换（共同配置/各自配置）
        const mode = e.target.value
        console.log('[handleConfigModeChange] 切换配置模式:', mode)
        
        // 保存当前配置
        this.saveCurrentRoleConfig()
        
        // 切换模式
        this.configMode = mode
        
        if (mode === 'shared') {
          // 切换到共同配置模式
          this.activeRoleTab = ''
          this.loadRoleConfig('shared')
        } else {
          // 切换到各自配置模式
          // 设置第一个角色为激活状态
          if (this.stepRoles && this.stepRoles.length > 0) {
            this.activeRoleTab = this.stepRoles[0].roleId
            this.loadRoleConfig(this.activeRoleTab)
          }
        }
      },
      handleTabChange(activeKey) {
        // 保存当前角色的配置
        this.saveCurrentRoleConfig()
        // 切换到新角色
        this.activeRoleTab = activeKey
        // 加载新角色的配置
        this.loadRoleConfig(activeKey)
      },
      saveCurrentRoleConfig() {
        // 保存当前配置到roleFormConfigs
        const currentKey = this.configMode === 'shared' ? 'shared' : this.activeRoleTab
        // 同步表单字段到formConfig
        this.syncSelectedFormFieldToFields()
        const formConfigJson = JSON.stringify({
          fields: this.formFields.map(field => ({
            name: field.name,
            type: field.type,
            label: field.label,
            placeholder: field.placeholder,
            required: field.required,
            disabled: field.disabled,
            defaultValue: field.defaultValue,
            span: field.span || 24,
            dictCode: field.dictCode || '',
            options: field.dictCode ? '' : (field.options || ''),
            dateType: field.dateType || 'date',
            format: this.getDateFormat(field),
            valueFormat: this.getDateValueFormat(field)
          }))
        })
        this.$set(this.roleFormConfigs, currentKey, {
          formName: this.currentRoleModel.formName,
          formConfig: formConfigJson,
          remarkRequired: this.currentRoleModel.remarkRequired,
          indicators: JSON.parse(JSON.stringify(this.currentRoleModel.indicators || []))
        })
      },
      loadRoleConfig(roleKey) {
        // 从roleFormConfigs加载指定角色的配置
        const config = this.roleFormConfigs[roleKey] || {
          formName: '',
          formConfig: '',
          remarkRequired: 1,
          indicators: []
        }
        this.currentRoleModel = {
          formName: config.formName || '',
          formConfig: config.formConfig || '',
          remarkRequired: config.remarkRequired !== undefined ? config.remarkRequired : 1,
          indicators: JSON.parse(JSON.stringify(config.indicators || []))
        }
        // 加载表单字段配置
        this.loadFormFields(config.formConfig)
      },
      getRoleConfigStatus(roleKey) {
        // 获取角色配置状态（用于显示Badge）
        const config = this.roleFormConfigs[roleKey]
        if (!config) return 0
        
        // 检查是否有有效的表单配置
        let hasForm = false
        if (config.formConfig) {
          try {
            const formConfigObj = typeof config.formConfig === 'string' ? JSON.parse(config.formConfig) : config.formConfig
            if (formConfigObj && formConfigObj.fields && Array.isArray(formConfigObj.fields) && formConfigObj.fields.length > 0) {
              hasForm = true
            }
          } catch (e) {
            // 解析失败，检查是否是有效的非空字符串
            if (config.formConfig.trim() && config.formConfig !== '{}' && config.formConfig !== '{"fields":[]}') {
              hasForm = true
            }
          }
        }
        
        // 检查是否有指标配置
        const hasIndicators = config.indicators && Array.isArray(config.indicators) && config.indicators.length > 0
        
        // 只有在真正有配置时才返回1，否则返回0（Badge组件count为0时不显示）
        return (hasForm || hasIndicators) ? 1 : 0
      },
      handleAddIndicator(indicatorType) {
        if (!this.currentRoleModel.indicators) {
          this.currentRoleModel.indicators = []
        }
        let indicatorName = ''
        let indicatorConfig = {}
        if (indicatorType === 'next_auditor') {
          indicatorName = '下个流程审批人员选择'
          indicatorConfig = {
            roleId: '',
            roleName: '',
            nextStepOrder: null
          }
        } else if (indicatorType === 'cost_input') {
          indicatorName = '成本填写'
          indicatorConfig = {
            expenseName: '',
            categoryIds: [],
            categoryNames: []
          }
        } else if (indicatorType === 'convert_contract') {
          indicatorName = '转为合同'
          indicatorConfig = {
            enabled: false // 默认否
          }
        } else if (indicatorType === 'convert_address') {
          indicatorName = '转为地址'
          indicatorConfig = {
            enabled: false // 默认否
          }
        }
        const indicator = {
          indicatorType: indicatorType,
          indicatorName: indicatorName,
          indicatorConfig: indicatorConfig,
          sortOrder: this.currentRoleModel.indicators.length
        }
        this.currentRoleModel.indicators.push(indicator)
      },
      handleRemoveIndicator(index) {
        this.currentRoleModel.indicators.splice(index, 1)
      },
      handleStepOrderChange(indicator, stepOrder) {
        // 处理环节选择变化（旧方法，保留兼容性）
        if (!indicator.config) {
          this.$set(indicator, 'config', {
            roleId: '',
            roleName: '',
            nextStepOrder: null,
            nextStepRoleId: null
          })
        }
        this.$set(indicator.config, 'nextStepOrder', stepOrder)
        
        // 如果选择的步骤只有一个角色，自动设置
        const stepRoles = this.getStepRoles(stepOrder)
        if (stepRoles.length === 1) {
          this.$set(indicator.config, 'nextStepRoleId', stepRoles[0].roleId)
        } else {
          // 如果有多个角色，清空之前的选择，让用户重新选择
          this.$set(indicator.config, 'nextStepRoleId', null)
        }
      },
      handleStepRoleChange(indicator, value) {
        // 处理步骤-角色选择变化（新方法）
        if (!indicator.config) {
          this.$set(indicator, 'config', {
            roleId: '',
            roleName: '',
            nextStepOrder: null,
            nextStepRoleId: null
          })
        }
        
        // value格式可能是数字（stepOrder）或字符串 "stepOrder_roleId"
        const valueStr = String(value || '')
        console.log('[handleStepRoleChange] value:', value, 'valueStr:', valueStr)
        
        if (valueStr && valueStr.includes('_')) {
          // 步骤有多个角色，value格式为 "stepOrder_roleId"
          const parts = valueStr.split('_')
          const stepOrder = parseInt(parts[0])
          const roleId = parts.slice(1).join('_') // 处理 roleId 中可能包含下划线的情况
          console.log('[handleStepRoleChange] 多角色选择，stepOrder:', stepOrder, 'roleId:', roleId)
          this.$set(indicator.config, 'nextStepOrder', stepOrder)
          this.$set(indicator.config, 'nextStepRoleId', roleId)
        } else {
          // 步骤只有一个角色，value就是stepOrder
          const stepOrder = parseInt(valueStr)
          console.log('[handleStepRoleChange] 单角色选择，stepOrder:', stepOrder)
          if (!isNaN(stepOrder)) {
            this.$set(indicator.config, 'nextStepOrder', stepOrder)
            // 从 allSteps 中找到对应的步骤，获取第一个角色ID
            const step = this.allSteps.find((s, index) => {
              const sOrder = s.stepOrder || s.order || (index + 1)
              return sOrder === stepOrder
            })
            if (step && step.roleIds && step.roleIds.length > 0) {
              const firstRoleId = step.roleIds[0]
              const roleIdStr = typeof firstRoleId === 'string' 
                ? firstRoleId 
                : (firstRoleId && typeof firstRoleId === 'object' 
                  ? String(firstRoleId.value || firstRoleId.key || firstRoleId.id || '')
                  : String(firstRoleId || ''))
              this.$set(indicator.config, 'nextStepRoleId', roleIdStr)
              console.log('[handleStepRoleChange] 设置 nextStepRoleId:', roleIdStr)
            }
          }
        }
        console.log('[handleStepRoleChange] 更新后的 config:', indicator.config)
      },
      getSelectedStepRoleValue(indicator) {
        // 获取当前选中的值，用于显示在下拉框中
        if (!indicator.config || !indicator.config.nextStepOrder) {
          return null
        }
        const stepOrder = indicator.config.nextStepOrder
        
        // 直接从 allSteps 中找到对应的步骤
        const step = this.allSteps.find((s, index) => {
          const sOrder = s.stepOrder || s.order || (index + 1)
          return sOrder === stepOrder
        })
        
        if (step && step.roleIds && step.roleIds.length > 0) {
          // 确保 roleId 是字符串
          const nextStepRoleId = indicator.config.nextStepRoleId ? String(indicator.config.nextStepRoleId) : null
          
          // 将步骤中的 roleIds 也转换为字符串数组
          const stepRoleIdStrings = step.roleIds.map(roleId => {
            if (typeof roleId === 'string') {
              return roleId
            } else if (roleId && typeof roleId === 'object') {
              return String(roleId.value || roleId.key || roleId.id || '')
            } else {
              return String(roleId || '')
            }
          })
          
          if (stepRoleIdStrings.length > 1) {
            // 步骤有多个角色
            if (nextStepRoleId && stepRoleIdStrings.includes(nextStepRoleId)) {
              // 返回组合值
              return `${stepOrder}_${nextStepRoleId}`
            } else {
              // 有多个角色但还没选择具体角色
              return null
            }
          } else if (stepRoleIdStrings.length === 1) {
            // 步骤只有一个角色，返回stepOrder
            return stepOrder
          }
        }
        
        return null
      },
      getCategoryIds(indicator) {
        // 获取报销类目ID列表，过滤掉空值（返回数组，用于内部处理）
        if (!indicator.config || !indicator.config.categoryIds) {
          return []
        }
        // 过滤掉空值、null、undefined和空字符串，并确保返回的是字符串数组
        const filtered = indicator.config.categoryIds.filter(id => {
          const strId = String(id).trim()
          return strId && strId !== '' && strId !== 'null' && strId !== 'undefined'
        })
        // 如果过滤后为空，返回空数组；否则返回过滤后的数组
        return filtered.length === 0 ? [] : filtered
      },
      getCategoryIdsString(indicator) {
        // 获取报销类目ID字符串（逗号分隔），用于传递给j-category-select组件
        const ids = this.getCategoryIds(indicator)
        // 过滤掉空值后，如果为空则返回空字符串，否则返回逗号分隔的字符串
        if (ids.length === 0) {
          return ''
        }
        return ids.filter(id => {
          const strId = String(id).trim()
          return strId && strId !== '' && strId !== 'null' && strId !== 'undefined'
        }).join(',')
      },
      handleCategoryChange(indicator, value) {
        // 处理报销类目选择变化，过滤掉空值
        if (!indicator.config) {
          this.$set(indicator, 'config', {
            expenseName: '',
            categoryIds: [],
            categoryNames: []
          })
        }
        // 如果value是字符串，先转换为数组
        let valueArray = []
        if (typeof value === 'string') {
          valueArray = value ? value.split(',').filter(v => v && v.trim()) : []
        } else if (Array.isArray(value)) {
          valueArray = value.filter(id => {
            const strId = String(id).trim()
            return strId && strId !== '' && strId !== 'null' && strId !== 'undefined'
          })
        }
        // 过滤掉空值、null、undefined和空字符串
        const filteredValue = valueArray.filter(id => {
          const strId = String(id).trim()
          return strId && strId !== '' && strId !== 'null' && strId !== 'undefined'
        })
        this.$set(indicator.config, 'categoryIds', filteredValue)
        // 如果有categoryNames需要同步更新，这里暂时只更新IDs
        // categoryNames会在保存时从字典中获取
      },
      handleIndicatorConfigChange(indicator, key, value) {
        if (!indicator.config) {
          // 根据指标类型初始化 config
          if (indicator.indicatorType === 'next_auditor') {
            this.$set(indicator, 'config', {
              roleId: '',
              roleName: '',
              nextStepOrder: null,
              nextStepRoleId: null
            })
          } else if (indicator.indicatorType === 'cost_input') {
            this.$set(indicator, 'config', {
              expenseName: '',
              categoryIds: [],
              categoryNames: []
            })
          } else if (indicator.indicatorType === 'convert_contract') {
            this.$set(indicator, 'config', {
              enabled: false
            })
          } else {
            this.$set(indicator, 'config', {})
          }
        }
        this.$set(indicator.config, key, value)
        
        // 如果是角色选择，同时更新角色名称
        if (key === 'roleId' && value) {
          const role = this.findRoleById(value)
          if (role) {
            this.$set(indicator.config, 'roleName', role.title)
          }
        }
      },
      // 获取指定步骤的角色列表
      getStepRoles(stepOrder) {
        if (!this.allSteps || this.allSteps.length === 0) {
          return []
        }
        
        // 找到对应步骤顺序的步骤
        const step = this.allSteps.find((s, index) => {
          const sOrder = s.stepOrder || s.order || (index + 1)
          return sOrder === stepOrder
        })
        
        if (step && step.roleIds && step.roleIds.length > 0 && step.roleNames && step.roleNames.length > 0) {
          // 返回角色ID和名称的数组
          return step.roleIds.map((roleId, index) => ({
            roleId: roleId,
            roleName: step.roleNames[index] || ''
          }))
        }
        return []
      },
      findRoleById(roleId) {
        if (!this.roleTreeData || this.roleTreeData.length === 0) {
          return null
        }
        // 将 roleId 转换为字符串进行比较
        const roleIdStr = String(roleId || '')
        const findInTree = (nodes) => {
          for (const node of nodes) {
            // 将 node.value 也转换为字符串进行比较
            if (String(node.value || '') === roleIdStr) {
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
      open(stepId, formConfig, indicators, currentStepOrder, allSteps, stepRoleIds, stepRoleNames, roleFormConfigs) {
        this.stepId = stepId
        this.currentStepOrder = currentStepOrder || 0
        this.allSteps = allSteps || [] // 保存所有步骤信息
        
        // 设置步骤角色列表
        // 处理stepRoleIds可能是对象数组的情况（tree-select返回的格式）
        let processedRoleIds = []
        if (stepRoleIds && stepRoleIds.length > 0) {
          processedRoleIds = stepRoleIds.map(roleId => {
            if (typeof roleId === 'string') {
              return roleId
            } else if (roleId && typeof roleId === 'object') {
              return String(roleId.value || roleId.key || roleId.id || '')
            } else {
              return String(roleId || '')
            }
          }).filter(id => id && id !== '')
        }
        
        if (processedRoleIds.length > 0 && stepRoleNames && stepRoleNames.length > 0) {
          this.stepRoles = processedRoleIds.map((roleId, index) => ({
            roleId: String(roleId),
            roleName: stepRoleNames[index] || ''
          }))
        } else if (processedRoleIds.length > 0) {
          // 如果没有roleNames，尝试从roleTreeData中查找
          this.stepRoles = processedRoleIds.map(roleId => {
            const role = this.findRoleById(roleId)
            return {
              roleId: String(roleId),
              roleName: role ? (role.title || role.roleName || '') : ''
            }
          })
        } else {
          this.stepRoles = []
        }
        
        // 调试日志
        console.log('[StepFormConfigModal] open方法执行')
        console.log('[StepFormConfigModal] stepRoleIds:', stepRoleIds)
        console.log('[StepFormConfigModal] stepRoleNames:', stepRoleNames)
        console.log('[StepFormConfigModal] processedRoleIds:', processedRoleIds)
        console.log('[StepFormConfigModal] this.stepRoles:', this.stepRoles)
        console.log('[StepFormConfigModal] this.stepRoles.length:', this.stepRoles.length)
        console.log('[StepFormConfigModal] 是否显示配置模式:', this.stepRoles && Array.isArray(this.stepRoles) && this.stepRoles.length > 1)
        
        // 初始化角色表单配置
        this.roleFormConfigs = {}
        this.configMode = 'shared' // 默认共同配置
        this.activeRoleTab = ''
        
        // 使用 $set 确保响应式更新
        this.$set(this, 'stepRoles', this.stepRoles)
        
        // 如果有多个角色，初始化每个角色的配置
        if (this.stepRoles && Array.isArray(this.stepRoles) && this.stepRoles.length > 1) {
          // 如果传入了roleFormConfigs（编辑模式），使用传入的配置
          if (roleFormConfigs && typeof roleFormConfigs === 'object') {
            this.roleFormConfigs = JSON.parse(JSON.stringify(roleFormConfigs))
          } else {
            // 新建模式：初始化"全部角色共享"配置
            let parsedFormConfig = {}
            if (formConfig) {
              if (typeof formConfig === 'string') {
                try {
                  parsedFormConfig = JSON.parse(formConfig)
                } catch (e) {
                  parsedFormConfig = {}
                }
              } else {
                parsedFormConfig = formConfig
              }
            }
            
            let parsedIndicators = []
            if (indicators) {
              if (typeof indicators === 'string') {
                try {
                  parsedIndicators = JSON.parse(indicators)
                } catch (e) {
                  parsedIndicators = []
                }
              } else if (Array.isArray(indicators)) {
                parsedIndicators = JSON.parse(JSON.stringify(indicators))
              }
            }
            
            // 确保每个指标的 config 都已初始化
            parsedIndicators.forEach(indicator => {
              if (!indicator.config) {
                if (indicator.indicatorType === 'next_auditor') {
                  indicator.config = {
                    roleId: '',
                    roleName: '',
                    nextStepOrder: null,
                    nextStepRoleId: null
                  }
                } else if (indicator.indicatorType === 'cost_input') {
                  indicator.config = {
                    expenseName: '',
                    categoryIds: [],
                    categoryNames: []
                  }
                } else if (indicator.indicatorType === 'convert_contract') {
                  indicator.config = {
                    enabled: false
                  }
                }
              } else if (typeof indicator.config === 'string') {
                try {
                  indicator.config = JSON.parse(indicator.config)
                } catch (e) {
                  if (indicator.indicatorType === 'next_auditor') {
                    indicator.config = {
                      roleId: '',
                      roleName: '',
                      nextStepOrder: null,
                      nextStepRoleId: null
                    }
                  } else if (indicator.indicatorType === 'cost_input') {
                    indicator.config = {
                      expenseName: '',
                      categoryIds: [],
                      categoryNames: []
                    }
                  } else if (indicator.indicatorType === 'convert_contract') {
                    indicator.config = {
                      enabled: false
                    }
                  } else {
                    indicator.config = {}
                  }
                }
              }
              // 确保 nextStepRoleId 字段存在
              if (indicator.indicatorType === 'next_auditor' && indicator.config && !indicator.config.hasOwnProperty('nextStepRoleId')) {
                indicator.config.nextStepRoleId = null
              }
            })
            
            this.roleFormConfigs['shared'] = {
              formName: parsedFormConfig.formName || '',
              formConfig: parsedFormConfig.formConfig || '',
              remarkRequired: parsedFormConfig.remarkRequired !== undefined ? parsedFormConfig.remarkRequired : 1,
              indicators: parsedIndicators
            }
            
          }
        } else {
          // 只有一个角色或没有角色，使用原来的逻辑
          let parsedFormConfig = {}
          if (formConfig) {
            if (typeof formConfig === 'string') {
              try {
                parsedFormConfig = JSON.parse(formConfig)
              } catch (e) {
                parsedFormConfig = {}
              }
            } else {
              parsedFormConfig = formConfig
            }
          }
          
          let parsedIndicators = []
          if (indicators) {
            if (typeof indicators === 'string') {
              try {
                parsedIndicators = JSON.parse(indicators)
              } catch (e) {
                parsedIndicators = []
              }
            } else if (Array.isArray(indicators)) {
              parsedIndicators = JSON.parse(JSON.stringify(indicators))
            }
          }
          
          // 确保每个指标的 config 都已初始化
          parsedIndicators.forEach(indicator => {
            if (!indicator.config) {
              if (indicator.indicatorType === 'next_auditor') {
                indicator.config = {
                  roleId: '',
                  roleName: '',
                  nextStepOrder: null,
                  nextStepRoleId: null
                }
              } else if (indicator.indicatorType === 'cost_input') {
                indicator.config = {
                  expenseName: '',
                  categoryIds: [],
                  categoryNames: []
                }
              } else if (indicator.indicatorType === 'convert_contract') {
                indicator.config = {
                  enabled: false
                }
              }
            } else if (typeof indicator.config === 'string') {
              try {
                indicator.config = JSON.parse(indicator.config)
              } catch (e) {
                indicator.config = {}
              }
            }
            if (indicator.indicatorType === 'next_auditor' && indicator.config && !indicator.config.hasOwnProperty('nextStepRoleId')) {
              indicator.config.nextStepRoleId = null
            }
          })
          
          this.roleFormConfigs['shared'] = {
            formName: parsedFormConfig.formName || '',
            formConfig: parsedFormConfig.formConfig || '',
            remarkRequired: parsedFormConfig.remarkRequired !== undefined ? parsedFormConfig.remarkRequired : 1,
            indicators: parsedIndicators
          }
        }
        
        // 加载当前配置
        if (this.configMode === 'shared') {
          this.loadRoleConfig('shared')
        } else if (this.activeRoleTab) {
          this.loadRoleConfig(this.activeRoleTab)
        }
        
        // 生成后续步骤选项（用于环节选择）
        this.updateAvailableStepOrders(currentStepOrder, allSteps)
        
        // 如果已有配置的步骤，自动设置步骤角色（如果只有一个角色）
        if (this.currentRoleModel.indicators) {
          this.currentRoleModel.indicators.forEach(indicator => {
            if (indicator.indicatorType === 'next_auditor' && indicator.config && indicator.config.nextStepOrder) {
              const stepRoles = this.getStepRoles(indicator.config.nextStepOrder)
              if (stepRoles.length === 1 && !indicator.config.nextStepRoleId) {
                // 自动设置唯一角色
                indicator.config.nextStepRoleId = stepRoles[0].roleId
              }
            }
          })
        }
        
        this.visible = true
        
        // 强制更新视图，确保配置模式选择显示
        this.$nextTick(() => {
          this.$forceUpdate()
          console.log('[StepFormConfigModal] 视图更新后，stepRoles:', this.stepRoles)
          console.log('[StepFormConfigModal] stepRoles.length:', this.stepRoles ? this.stepRoles.length : 0)
        })
      },
      updateAvailableStepOrders(currentStepOrder, allSteps) {
        // 根据当前步骤顺序和所有步骤，生成后续步骤选项
        this.availableStepOrders = []
        this.availableStepRoleOptions = []
        
        console.log('[updateAvailableStepOrders] 开始执行')
        console.log('[updateAvailableStepOrders] currentStepOrder:', currentStepOrder)
        console.log('[updateAvailableStepOrders] allSteps:', allSteps)
        
        if (allSteps && Array.isArray(allSteps) && allSteps.length > 0) {
          // 步骤数组是按 stepOrder 排序的，所以可以直接使用索引
          // currentStepOrder 从1开始，数组索引从0开始
          const currentIndex = currentStepOrder - 1
          console.log('[updateAvailableStepOrders] currentIndex:', currentIndex, 'allSteps.length:', allSteps.length)
          
          if (currentIndex >= 0 && currentIndex < allSteps.length - 1) {
            // 生成后续步骤的选项
            for (let i = currentIndex + 1; i < allSteps.length; i++) {
              const step = allSteps[i]
              // 步骤对象应该有 stepOrder 属性，如果没有则使用索引+1
              const stepOrder = step.stepOrder || (i + 1)
              
              console.log(`[updateAvailableStepOrders] 处理步骤 ${stepOrder}:`, {
                step: JSON.parse(JSON.stringify(step)),
                roleIds: step.roleIds,
                roleNames: step.roleNames,
                roleIdsType: typeof step.roleIds,
                roleIdsIsArray: Array.isArray(step.roleIds),
                roleIdsLength: step.roleIds ? step.roleIds.length : 0,
                roleNamesLength: step.roleNames ? step.roleNames.length : 0
              })
              
              // 直接从步骤对象获取角色信息
              if (step.roleIds && Array.isArray(step.roleIds) && step.roleIds.length > 0) {
                // 如果 roleNames 为空，尝试从 roleTreeData 中获取角色名称
                let roleNames = step.roleNames || []
                if (!roleNames || roleNames.length === 0 || roleNames.every(name => !name)) {
                  // 确保 roleIds 是字符串数组
                  const roleIdStrings = step.roleIds.map(roleId => {
                    if (typeof roleId === 'string') {
                      return roleId
                    } else if (roleId && typeof roleId === 'object') {
                      return String(roleId.value || roleId.key || roleId.id || '')
                    } else {
                      return String(roleId || '')
                    }
                  })
                  
                  console.log(`[updateAvailableStepOrders] roleTreeData 长度:`, this.roleTreeData ? this.roleTreeData.length : 0)
                  console.log(`[updateAvailableStepOrders] roleTreeData 前3个:`, this.roleTreeData ? this.roleTreeData.slice(0, 3) : [])
                  
                  roleNames = roleIdStrings.map((roleIdStr, idx) => {
                    const role = this.findRoleById(roleIdStr)
                    console.log(`[updateAvailableStepOrders] 查找角色 ${roleIdStr} (索引${idx}):`, role, '原始roleId:', step.roleIds[idx])
                    // 如果从角色树中找不到，但步骤数据中有角色名称，使用步骤数据中的名称
                    if (!role && step.roleNames && step.roleNames[idx]) {
                      console.log(`[updateAvailableStepOrders] 使用步骤数据中的角色名称:`, step.roleNames[idx])
                      return step.roleNames[idx]
                    }
                    return role ? role.title : ''
                  })
                }
                
                console.log(`[updateAvailableStepOrders] 步骤 ${stepOrder} 有 ${step.roleIds.length} 个角色，角色名称:`, roleNames)
                
                if (step.roleIds.length > 1) {
                  // 步骤有多个角色，展开为多个选项
                  step.roleIds.forEach((roleId, idx) => {
                    // 确保 roleId 是字符串类型
                    let roleIdStr = ''
                    if (typeof roleId === 'string') {
                      roleIdStr = roleId
                    } else if (roleId && typeof roleId === 'object') {
                      // 如果是对象，提取 value、key 或 id 属性
                      roleIdStr = String(roleId.value || roleId.key || roleId.id || '')
                    } else {
                      roleIdStr = String(roleId || '')
                    }
                    
                    const option = {
                      stepOrder: stepOrder,
                      roleId: roleIdStr,
                      roleName: roleNames[idx] || ''
                    }
                    console.log(`[updateAvailableStepOrders] 添加角色选项:`, option)
                    this.availableStepRoleOptions.push(option)
                  })
                  console.log(`[updateAvailableStepOrders] 已添加 ${step.roleIds.length} 个角色选项`)
                } else {
                  // 步骤只有一个角色，只显示步骤编号
                  console.log(`[updateAvailableStepOrders] 步骤 ${stepOrder} 只有一个角色，添加到 availableStepOrders`)
                  this.availableStepOrders.push(stepOrder)
                }
              } else {
                // 步骤没有角色信息，也显示步骤编号（虽然这种情况不应该发生）
                console.log(`[updateAvailableStepOrders] 步骤 ${stepOrder} 没有角色信息，添加到 availableStepOrders`)
                this.availableStepOrders.push(stepOrder)
              }
            }
          } else {
            console.log('[updateAvailableStepOrders] 条件不满足，不生成后续步骤选项')
          }
        } else {
          console.log('[updateAvailableStepOrders] allSteps 为空或不是数组')
        }
        
        console.log('[updateAvailableStepOrders] 最终结果:', {
          availableStepOrders: this.availableStepOrders,
          availableStepRoleOptions: this.availableStepRoleOptions
        })
      },
      handleOk() {
        // 保存当前角色的配置
        this.saveCurrentRoleConfig()
        
        // 如果有多个角色，需要保存所有角色的配置
        if (this.stepRoles && this.stepRoles.length > 1) {
          // 验证所有角色的指标配置
          for (const roleKey in this.roleFormConfigs) {
            const config = this.roleFormConfigs[roleKey]
            if (config.indicators && config.indicators.length > 0) {
              for (let i = 0; i < config.indicators.length; i++) {
                const indicator = config.indicators[i]
                if (indicator.indicatorType === 'next_auditor') {
                  if (!indicator.config || !indicator.config.roleId) {
                    this.$message.warning(`${roleKey === 'shared' ? '全部角色共享' : this.getRoleName(roleKey)}的第${i + 1}个"下个流程审批人员选择"指标未选择角色！`)
                    this.activeRoleTab = roleKey
                    this.loadRoleConfig(roleKey)
                    return false
                  }
                  if (!indicator.config.nextStepOrder) {
                    this.$message.warning(`${roleKey === 'shared' ? '全部角色共享' : this.getRoleName(roleKey)}的第${i + 1}个"下个流程审批人员选择"指标未选择环节！`)
                    this.activeRoleTab = roleKey
                    this.loadRoleConfig(roleKey)
                    return false
                  }
                  const stepRoles = this.getStepRoles(indicator.config.nextStepOrder)
                  if (stepRoles.length > 1 && !indicator.config.nextStepRoleId) {
                    this.$message.warning(`${roleKey === 'shared' ? '全部角色共享' : this.getRoleName(roleKey)}的第${i + 1}个"下个流程审批人员选择"指标：步骤${indicator.config.nextStepOrder}有多个角色，请选择具体角色！`)
                    this.activeRoleTab = roleKey
                    this.loadRoleConfig(roleKey)
                    return false
                  }
                } else if (indicator.indicatorType === 'cost_input') {
                  if (!indicator.config || !indicator.config.expenseName || !indicator.config.expenseName.trim()) {
                    this.$message.warning(`${roleKey === 'shared' ? '全部角色共享' : this.getRoleName(roleKey)}的第${i + 1}个"成本填写"指标未填写报销名称！`)
                    this.activeRoleTab = roleKey
                    this.loadRoleConfig(roleKey)
                    return false
                  }
                }
              }
            }
          }
          
          // 触发事件，传递所有角色的配置
          const allRoleConfigs = {}
          for (const roleKey in this.roleFormConfigs) {
            const config = this.roleFormConfigs[roleKey]
            allRoleConfigs[roleKey] = {
              formConfig: {
                formName: config.formName,
                formConfig: config.formConfig,
                remarkRequired: config.remarkRequired
              },
              indicators: config.indicators || [],
              targetRoleId: roleKey === 'shared' ? null : roleKey
            }
          }
          this.$emit('ok', this.stepId, allRoleConfigs)
        } else {
          // 单个角色或没有角色，使用原来的逻辑
          // 验证指标配置
          for (let i = 0; i < this.currentRoleModel.indicators.length; i++) {
            const indicator = this.currentRoleModel.indicators[i]
            if (indicator.indicatorType === 'next_auditor') {
              if (!indicator.config || !indicator.config.roleId) {
                this.$message.warning(`第${i + 1}个"下个流程审批人员选择"指标未选择角色！`)
                return false
              }
              if (!indicator.config.nextStepOrder) {
                this.$message.warning(`第${i + 1}个"下个流程审批人员选择"指标未选择环节！`)
                return false
              }
              const stepRoles = this.getStepRoles(indicator.config.nextStepOrder)
              if (stepRoles.length > 1 && !indicator.config.nextStepRoleId) {
                this.$message.warning(`第${i + 1}个"下个流程审批人员选择"指标：步骤${indicator.config.nextStepOrder}有多个角色，请选择具体角色！`)
                return false
              }
            } else if (indicator.indicatorType === 'cost_input') {
              if (!indicator.config || !indicator.config.expenseName || !indicator.config.expenseName.trim()) {
                this.$message.warning(`第${i + 1}个"成本填写"指标未填写报销名称！`)
                return false
              }
            }
          }
          
          // 同步选中字段到字段列表
          this.syncSelectedFormFieldToFields()
          
          // 构建表单配置JSON
          const formConfigJson = JSON.stringify({
            fields: this.formFields.map(field => ({
              name: field.name,
              type: field.type,
              label: field.label,
              placeholder: field.placeholder,
              required: field.required,
              disabled: field.disabled,
              defaultValue: field.defaultValue,
              span: field.span || 24,
              dictCode: field.dictCode || '',
              options: field.dictCode ? '' : (field.options || ''),
              dateType: field.dateType || 'date',
              format: this.getDateFormat(field),
              valueFormat: this.getDateValueFormat(field)
            }))
          })
          
          // 构建提交数据
          const formConfigObj = {
            formName: this.currentRoleModel.formName,
            formConfig: formConfigJson,
            remarkRequired: this.currentRoleModel.remarkRequired
          }
          
          const indicatorsArray = this.currentRoleModel.indicators.map(indicator => ({
            id: indicator.id,
            indicatorType: indicator.indicatorType,
            indicatorName: indicator.indicatorName,
            config: indicator.config || {},
            sortOrder: indicator.sortOrder || 0
          }))
          
          this.$emit('ok', this.stepId, formConfigObj, indicatorsArray, null)
        }
        this.visible = false
      },
      getRoleName(roleId) {
        // 根据角色ID获取角色名称
        const role = this.stepRoles.find(r => r.roleId === roleId)
        return role ? role.roleName : roleId
      },
      handleClearCurrentRoleConfig() {
        // 清除当前角色的配置
        const currentKey = this.activeRoleTab
        const roleName = currentKey === 'shared' ? '全部角色共享' : this.getRoleName(currentKey)
        
        this.$confirm({
          title: '确认清除配置',
          content: `确定要清除"${roleName}"的所有表单和指标配置吗？此操作不可恢复。`,
          okText: '确定',
          okType: 'danger',
          cancelText: '取消',
          onOk: () => {
            // 重置当前角色的配置
            this.$set(this.roleFormConfigs, currentKey, {
              formName: '',
              formConfig: '',
              remarkRequired: 1,
              indicators: []
            })
            
            // 清空表单字段
            this.formFields = []
            this.formData = {}
            this.selectedFormField = null
            this.selectedFormFieldIndex = -1
            
            // 重新加载当前角色的配置（此时是空配置）
            this.loadRoleConfig(currentKey)
            
            this.$message.success(`已清除"${roleName}"的配置`)
          }
        })
      },
      handleCancel() {
        this.visible = false
        // 重置表单设计器状态
        this.formFields = []
        this.formData = {}
        this.selectedFormField = null
        this.selectedFormFieldIndex = -1
        this.formFieldCounter = 0
      },
      // ========== 表单设计器相关方法 ==========
      loadFormFields(formConfigStr) {
        this.formFields = []
        this.formData = {}
        this.selectedFormField = null
        this.selectedFormFieldIndex = -1
        this.formFieldCounter = 0
        
        if (!formConfigStr) {
          return
        }
        
        try {
          const formConfig = typeof formConfigStr === 'string' ? JSON.parse(formConfigStr) : formConfigStr
          if (formConfig && formConfig.fields && Array.isArray(formConfig.fields)) {
            this.formFields = formConfig.fields.map((field, index) => ({
              key: field.name || `field_${index}`,
              name: field.name || `field_${index}`,
              type: field.type || 'input',
              label: field.label || '',
              placeholder: field.placeholder || '',
              required: field.required || false,
              disabled: field.disabled || false,
              defaultValue: field.defaultValue || '',
              span: field.span || 24,
              dictCode: field.dictCode || '',
              options: field.options || '',
              dateType: field.dateType || 'date'
            }))
            
            // 初始化表单数据
            this.formFields.forEach(field => {
              if (field.defaultValue !== undefined && field.defaultValue !== '') {
                this.$set(this.formData, field.name, field.defaultValue)
              } else {
                const defaultValue = this.getDefaultFieldValue(field.type)
                this.$set(this.formData, field.name, defaultValue)
              }
            })
            
            // 更新字段计数器
            if (this.formFields.length > 0) {
              const maxIndex = Math.max(...this.formFields.map(f => {
                const match = f.name.match(/field_(\d+)/)
                return match ? parseInt(match[1]) : -1
              }))
              this.formFieldCounter = maxIndex >= 0 ? maxIndex + 1 : this.formFields.length
            }
          }
        } catch (e) {
          console.error('加载表单配置失败：', e)
        }
      },
      addFormField(type) {
        const fieldTypes = {
          input: { type: 'input', label: '单行文本', placeholder: '请输入' },
          textarea: { type: 'textarea', label: '多行文本', placeholder: '请输入' },
          number: { type: 'number', label: '数字', placeholder: '请输入数字' },
          date: { type: 'date', label: '日期', placeholder: '请选择日期', dateType: 'date' },
          select: { type: 'select', label: '下拉选择', placeholder: '请选择', options: '1:选项1\n2:选项2' },
          radio: { type: 'radio', label: '单选', placeholder: '', options: '1:选项1\n2:选项2' },
          checkbox: { type: 'checkbox', label: '多选', placeholder: '', options: '1:选项1\n2:选项2' }
        }
        
        const fieldConfig = fieldTypes[type]
        const fieldName = `field_${this.formFieldCounter++}`
        const field = {
          key: fieldName,
          name: fieldName,
          type: fieldConfig.type,
          label: fieldConfig.label,
          placeholder: fieldConfig.placeholder,
          required: false,
          disabled: false,
          defaultValue: '',
          span: 24,
          options: fieldConfig.options || '',
          dictCode: '',
          dateType: fieldConfig.dateType || 'date'
        }
        
        this.formFields.push(field)
        this.$set(this.formData, fieldName, this.getDefaultFieldValue(field.type))
        this.selectFormField(field, this.formFields.length - 1)
      },
      selectFormField(field, index) {
        this.selectedFormField = { ...field }
        this.selectedFormFieldIndex = index
        this.$nextTick(() => {
          this.syncSelectedFormFieldToFields()
        })
      },
      syncSelectedFormFieldToFields() {
        if (this.selectedFormFieldIndex >= 0 && this.selectedFormFieldIndex < this.formFields.length && this.selectedFormField) {
          this.$set(this.formFields, this.selectedFormFieldIndex, { ...this.selectedFormField })
        }
      },
      removeFormField(index) {
        const field = this.formFields[index]
        delete this.formData[field.name]
        this.formFields.splice(index, 1)
        if (this.selectedFormFieldIndex === index) {
          this.selectedFormField = null
          this.selectedFormFieldIndex = -1
        } else if (this.selectedFormFieldIndex > index) {
          this.selectedFormFieldIndex--
        }
      },
      copyFormField(index) {
        const field = this.formFields[index]
        const newField = {
          ...field,
          key: `field_${this.formFieldCounter++}`,
          name: `${field.name}_copy`,
          label: `${field.label}_副本`
        }
        this.formFields.splice(index + 1, 0, newField)
        this.$set(this.formData, newField.name, this.getDefaultFieldValue(newField.type))
        this.selectFormField(newField, index + 1)
      },
      getFieldSpan(field, index) {
        if (index === this.selectedFormFieldIndex && this.selectedFormField) {
          return this.selectedFormField.span || 24
        }
        return field.span || 24
      },
      getFieldComponent(type) {
        const components = {
          input: 'a-input',
          textarea: 'a-textarea',
          number: 'a-input-number',
          date: 'a-date-picker',
          select: 'a-select',
          radio: 'a-radio-group',
          checkbox: 'a-checkbox-group'
        }
        return components[type] || 'a-input'
      },
      getFieldProps(field) {
        const fieldIndex = this.formFields.indexOf(field)
        const currentField = (fieldIndex === this.selectedFormFieldIndex && this.selectedFormField) 
          ? this.selectedFormField 
          : field
        
        const props = {}
        if (currentField.type === 'textarea') {
          props.rows = 4
        }
        if (currentField.type === 'number') {
          props.style = { width: '100%' }
        }
        if (currentField.type === 'date') {
          props.style = { width: '100%' }
          const dateType = currentField.dateType || 'date'
          if (dateType === 'month') {
            props.mode = 'month'
            props.format = 'YYYY-MM'
            props.valueFormat = 'YYYY-MM'
          } else if (dateType === 'year') {
            props.mode = 'year'
            props.format = 'YYYY'
            props.valueFormat = 'YYYY'
          } else if (dateType === 'range') {
            props.mode = 'range'
            props.format = 'YYYY-MM-DD'
            props.valueFormat = 'YYYY-MM-DD'
          } else {
            props.format = 'YYYY-MM-DD'
            props.valueFormat = 'YYYY-MM-DD'
          }
        }
        // 注意：select/radio/checkbox 的选项需要通过模板中的子元素来渲染，这里暂时不处理
        return props
      },
      getDefaultFieldValue(type) {
        const defaults = {
          input: '',
          textarea: '',
          number: null,
          date: null,
          select: undefined,
          radio: undefined,
          checkbox: []
        }
        return defaults[type] !== undefined ? defaults[type] : ''
      },
      getDateFormat(field) {
        if (field.type !== 'date') return ''
        if (field.dateType === 'month') return 'YYYY-MM'
        if (field.dateType === 'year') return 'YYYY'
        if (field.dateType === 'range') return 'YYYY-MM-DD'
        return 'YYYY-MM-DD'
      },
      getDateValueFormat(field) {
        if (field.type !== 'date') return ''
        if (field.dateType === 'month') return 'YYYY-MM'
        if (field.dateType === 'year') return 'YYYY'
        if (field.dateType === 'range') return 'YYYY-MM-DD'
        return 'YYYY-MM-DD'
      },
      getIndicatorTagColor(indicatorType) {
        const colorMap = {
          'next_auditor': 'blue',
          'cost_input': 'green',
          'convert_contract': 'orange',
          'convert_address': 'purple'
        }
        return colorMap[indicatorType] || 'default'
      },
      getIndicatorTypeText(indicatorType) {
        const textMap = {
          'next_auditor': '下个流程审批人员选择',
          'cost_input': '成本填写',
          'convert_contract': '转为合同',
          'convert_address': '转为地址'
        }
        return textMap[indicatorType] || indicatorType
      }
    }
  }
</script>

<style scoped>
  /* 指标配置区域样式 */
  .indicator-item {
    border: 2px dashed #1890ff;
    border-radius: 4px;
    background: #f0f9ff;
    padding: 16px;
    margin-bottom: 16px;
  }
  
  /* 表单设计器样式 */
  .form-designer {
    min-height: 400px;
    padding: 16px;
    background: #fafafa;
  }
  
  .form-field-item {
    position: relative;
    padding: 12px;
    margin-bottom: 16px;
    background: #fff;
    border: 2px solid transparent;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .form-field-item:hover {
    border-color: #1890ff;
  }
  
  .form-field-item.selected {
    border-color: #1890ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  }
  
  .field-actions {
    position: absolute;
    top: 8px;
    right: 8px;
    display: none;
  }
  
  .form-field-item:hover .field-actions {
    display: block;
  }
  
  .field-actions i {
    margin-left: 8px;
    cursor: pointer;
    color: #1890ff;
    font-size: 16px;
  }
  
  .field-actions i:hover {
    color: #40a9ff;
  }
  
  .empty-tip {
    padding: 40px;
    text-align: center;
  }
  
  .field-library {
    padding: 8px;
  }
</style>

