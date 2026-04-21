<template>
  <j-modal
    :title="'表单设计器 - ' + formName"
    :width="1400"
    :visible="visible"
    :confirmLoading="confirmLoading"
    switchFullscreen
    @ok="handleOk"
    @cancel="handleCancel"
    okText="保存并创建新版本"
    cancelText="关闭"
    :maskClosable="false"
  >
    <a-spin :spinning="confirmLoading">
      <a-tabs defaultActiveKey="design" @change="handleTabChange">
        <a-tab-pane key="design" tab="设计">
          <a-row :gutter="16">
            <a-col :span="16">
              <a-card title="表单设计" :bordered="false">
                <div class="form-designer">
                  <a-form-model ref="designForm" :model="formData" :labelCol="{span: 6}" :wrapperCol="{span: 18}">
                    <a-row :gutter="16">
                      <a-col
                        v-for="(field, index) in fields"
                        :key="field.key || index"
                        :span="getFieldSpan(field, index)"
                      >
                        <div
                          class="form-field-item"
                          @click="selectField(field, index)"
                          :class="{ 'selected': selectedFieldIndex === index }"
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
                            <a-icon type="edit" @click.stop="editField(field, index)" />
                            <a-icon type="delete" @click.stop="removeField(index)" />
                            <a-icon type="copy" @click.stop="copyField(index)" />
                          </div>
                        </div>
                      </a-col>
                    </a-row>
                    <div v-if="fields.length === 0" class="empty-tip">
                      <a-empty description="从右侧拖拽字段到此处，或点击右侧按钮添加字段" />
                    </div>
                  </a-form-model>
                </div>
              </a-card>
            </a-col>
            <a-col :span="8">
              <a-card title="字段库" :bordered="false">
                <div class="field-library">
                  <a-button type="dashed" block @click="addField('input')" style="margin-bottom: 8px;">
                    <a-icon type="edit" /> 单行文本
                  </a-button>
                  <a-button type="dashed" block @click="addField('textarea')" style="margin-bottom: 8px;">
                    <a-icon type="file-text" /> 多行文本
                  </a-button>
                  <a-button type="dashed" block @click="addField('number')" style="margin-bottom: 8px;">
                    <a-icon type="number" /> 数字
                  </a-button>
                  <a-button type="dashed" block @click="addField('date')" style="margin-bottom: 8px;">
                    <a-icon type="calendar" /> 日期
                  </a-button>
                  <a-button type="dashed" block @click="addField('select')" style="margin-bottom: 8px;">
                    <a-icon type="unordered-list" /> 下拉选择
                  </a-button>
                  <a-button type="dashed" block @click="addField('radio')" style="margin-bottom: 8px;">
                    <a-icon type="check-circle" /> 单选
                  </a-button>
                  <a-button type="dashed" block @click="addField('checkbox')" style="margin-bottom: 8px;">
                    <a-icon type="check-square" /> 多选
                  </a-button>
                </div>
              </a-card>
              <a-card title="字段属性" :bordered="false" style="margin-top: 16px;" v-if="selectedField">
                <a-form-model :model="selectedField" :labelCol="{span: 8}" :wrapperCol="{span: 16}">
                  <a-form-model-item label="字段名称">
                    <a-input v-model="selectedField.name" placeholder="字段名（英文）" @change="syncSelectedFieldToFields"></a-input>
                  </a-form-model-item>
                  <a-form-model-item label="字段标签">
                    <a-input v-model="selectedField.label" placeholder="显示标签" @change="syncSelectedFieldToFields"></a-input>
                  </a-form-model-item>
                  <a-form-model-item label="占位符">
                    <a-input v-model="selectedField.placeholder" placeholder="占位符文本" @change="syncSelectedFieldToFields"></a-input>
                  </a-form-model-item>
                  <a-form-model-item label="列宽（span）">
                    <a-select v-model="selectedField.span" placeholder="选择列宽" @change="syncSelectedFieldToFields">
                      <a-select-option :value="24">整行（24）</a-select-option>
                      <a-select-option :value="12">半行（12）</a-select-option>
                      <a-select-option :value="8">1/3行（8）</a-select-option>
                      <a-select-option :value="6">1/4行（6）</a-select-option>
                    </a-select>
                  </a-form-model-item>
                  <a-form-model-item label="是否必填">
                    <a-switch v-model="selectedField.required" @change="syncSelectedFieldToFields"></a-switch>
                  </a-form-model-item>
                  <a-form-model-item label="是否禁用">
                    <a-switch v-model="selectedField.disabled" @change="syncSelectedFieldToFields"></a-switch>
                  </a-form-model-item>
                  <a-form-model-item label="默认值" v-if="selectedField.type !== 'date' && selectedField.type !== 'select' && selectedField.type !== 'radio'">
                    <a-input v-model="selectedField.defaultValue" placeholder="默认值" @change="syncSelectedFieldToFields"></a-input>
                  </a-form-model-item>
                  <!-- 日期类型配置 -->
                  <a-form-model-item label="日期类型" v-if="selectedField.type === 'date'">
                    <a-select v-model="selectedField.dateType" placeholder="选择日期类型" @change="syncSelectedFieldToFields">
                      <a-select-option value="date">日期（年月日）</a-select-option>
                      <a-select-option value="month">月份（年月）</a-select-option>
                      <a-select-option value="year">年份</a-select-option>
                      <a-select-option value="range">日期范围</a-select-option>
                    </a-select>
                  </a-form-model-item>
                  <!-- 字典配置 -->
                  <a-form-model-item label="字典编码" v-if="['select', 'radio', 'checkbox'].includes(selectedField.type)">
                    <a-input v-model="selectedField.dictCode" placeholder="输入字典编码，如：sex"></a-input>
                    <div style="margin-top: 4px; color: #999; font-size: 12px;">
                      支持字典编码或表格式：sys_user,realname,id
                    </div>
                  </a-form-model-item>
                  <!-- 选项配置（当没有字典编码时） -->
                  <a-form-model-item label="选项" v-if="['select', 'radio', 'checkbox'].includes(selectedField.type) && !selectedField.dictCode">
                    <a-textarea
                      v-model="selectedField.options"
                      placeholder="每行一个选项，格式：值:文本，如：1:选项1"
                      :rows="4"
                    ></a-textarea>
                  </a-form-model-item>
                </a-form-model>
              </a-card>
            </a-col>
          </a-row>
        </a-tab-pane>
        <a-tab-pane key="preview" tab="预览">
          <a-card title="表单预览" :bordered="false">
            <dynamic-form-renderer
              :formConfig="previewFormConfig"
              :formData="previewFormData"
              :formDisabled="false"
            />
          </a-card>
        </a-tab-pane>
      </a-tabs>
      <a-divider />
      <a-form-model-item label="版本描述" :labelCol="{span: 3}" :wrapperCol="{span: 21}">
        <a-input v-model="versionDesc" placeholder="请输入版本描述（可选）"></a-input>
      </a-form-model-item>
    </a-spin>
  </j-modal>
</template>

<script>
  import { createFormVersion, getFormVersions } from '@/api/api'
  import DynamicFormRenderer from '@/components/dynamicform/DynamicFormRenderer'

  export default {
    name: 'DynamicFormDesigner',
    components: {
      DynamicFormRenderer
    },
    data () {
      return {
        visible: false,
        confirmLoading: false,
        formId: '',
        formName: '',
        formData: {},
        fields: [],
        selectedField: null,
        selectedFieldIndex: -1,
        versionDesc: '',
        fieldCounter: 0,
      }
    },
    watch: {
      'selectedField.span'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.dateType'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.name'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.label'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.placeholder'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.required'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.disabled'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.defaultValue'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.dictCode'() {
        this.syncSelectedFieldToFields();
      },
      'selectedField.options'() {
        this.syncSelectedFieldToFields();
      },
    },
    computed: {
      previewFormConfig() {
        // 确保使用最新的字段数据（包括selectedField的更新）
        const currentFields = this.fields.map((field, index) => {
          // 如果当前字段是选中的字段，使用selectedField的最新值
          if (index === this.selectedFieldIndex && this.selectedField) {
            return { ...this.selectedField };
          }
          return field;
        });
        
        const formConfig = {
          fields: currentFields.map(field => ({
            name: field.name,
            type: field.type,
            label: field.label,
            placeholder: field.placeholder,
            required: field.required,
            disabled: field.disabled,
            defaultValue: field.defaultValue,
            span: field.span || 24,
            dictCode: field.dictCode || '',
            options: field.options || '',
            dateType: field.dateType || 'date',
            format: this.getDateFormat(field),
            valueFormat: this.getDateValueFormat(field),
          })),
        };
        return JSON.stringify(formConfig);
      },
      previewFormData() {
        return { ...this.formData };
      }
    },
    methods: {
      design (record) {
        this.formId = record.id;
        this.formName = record.formName;
        this.fields = [];
        this.formData = {};
        this.selectedField = null;
        this.selectedFieldIndex = -1;
        this.versionDesc = '';
        this.visible = true;
        // 加载当前版本的表单配置
        this.loadCurrentVersion();
      },
      loadCurrentVersion() {
        // 查询该表单的所有版本
        getFormVersions({ formId: this.formId }).then((res) => {
          if (res.success && res.result && res.result.length > 0) {
            // 查找当前版本（isCurrent=1）
            const currentVersion = res.result.find(v => v.isCurrent === 1);
            if (currentVersion && currentVersion.formConfig) {
              try {
                // 解析表单配置JSON
                const formConfig = JSON.parse(currentVersion.formConfig);
                if (formConfig.fields && Array.isArray(formConfig.fields)) {
                  // 加载字段配置
                  this.fields = formConfig.fields.map((field, index) => ({
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
                    dateType: field.dateType || 'date',
                  }));
                  // 初始化表单数据
                  this.fields.forEach(field => {
                    if (field.defaultValue !== undefined && field.defaultValue !== '') {
                      this.$set(this.formData, field.name, field.defaultValue);
                    } else {
                      const defaultValue = this.getDefaultValue(field.type);
                      this.$set(this.formData, field.name, defaultValue);
                    }
                  });
                  // 更新字段计数器，避免重复
                  if (this.fields.length > 0) {
                    const maxIndex = Math.max(...this.fields.map(f => {
                      const match = f.name.match(/field_(\d+)/);
                      return match ? parseInt(match[1]) : -1;
                    }));
                    this.fieldCounter = maxIndex >= 0 ? maxIndex + 1 : this.fields.length;
                  }
                  this.$message.success('已加载当前版本表单配置');
                }
              } catch (e) {
                console.error('解析表单配置失败：', e);
                this.$message.warning('加载表单配置失败，请检查配置格式');
              }
            }
          }
        }).catch((err) => {
          console.error('加载版本信息失败：', err);
          // 加载失败不影响继续使用，只是不加载已有配置
        });
      },
      handleTabChange(key) {
        // 切换标签页时，同步更新选中字段到预览数据
        if (key === 'preview') {
          this.syncFieldToFormData();
        }
      },
      syncFieldToFormData() {
        // 同步字段配置到表单数据
        this.fields.forEach(field => {
          if (!(field.name in this.formData)) {
            if (field.defaultValue !== undefined && field.defaultValue !== '') {
              this.$set(this.formData, field.name, field.defaultValue);
            } else {
              const defaultValue = this.getDefaultValue(field.type);
              this.$set(this.formData, field.name, defaultValue);
            }
          }
        });
      },
      getDefaultValue(type) {
        const defaults = {
          input: '',
          textarea: '',
          number: null,
          date: null,
          select: undefined,
          radio: undefined,
          checkbox: [],
        };
        return defaults[type] !== undefined ? defaults[type] : '';
      },
      addField (type) {
        const fieldTypes = {
          input: { type: 'input', label: '单行文本', placeholder: '请输入' },
          textarea: { type: 'textarea', label: '多行文本', placeholder: '请输入' },
          number: { type: 'number', label: '数字', placeholder: '请输入数字' },
          date: { type: 'date', label: '日期', placeholder: '请选择日期', dateType: 'date' },
          select: { type: 'select', label: '下拉选择', placeholder: '请选择', options: '1:选项1\n2:选项2' },
          radio: { type: 'radio', label: '单选', placeholder: '', options: '1:选项1\n2:选项2' },
          checkbox: { type: 'checkbox', label: '多选', placeholder: '', options: '1:选项1\n2:选项2' },
        };
        
        const fieldConfig = fieldTypes[type];
        const fieldName = `field_${this.fieldCounter++}`;
        const field = {
          key: fieldName,
          name: fieldName,
          type: fieldConfig.type,
          label: fieldConfig.label,
          placeholder: fieldConfig.placeholder,
          required: false,
          disabled: false,
          defaultValue: '',
          span: 24, // 默认整行
          options: fieldConfig.options || '',
          dictCode: '', // 字典编码
          dateType: fieldConfig.dateType || 'date', // 日期类型
        };
        
        this.fields.push(field);
        this.formData[fieldName] = this.getDefaultValue(field.type);
        this.selectField(field, this.fields.length - 1);
      },
      selectField (field, index) {
        // 使用深拷贝，并添加响应式监听
        this.selectedField = { ...field };
        this.selectedFieldIndex = index;
        
        // 监听selectedField的变化，同步更新到fields数组
        this.$nextTick(() => {
          this.syncSelectedFieldToFields();
        });
      },
      syncSelectedFieldToFields() {
        // 当selectedField变化时，同步更新到fields数组
        if (this.selectedFieldIndex >= 0 && this.selectedFieldIndex < this.fields.length && this.selectedField) {
          // 使用Vue.set确保响应式更新
          this.$set(this.fields, this.selectedFieldIndex, { ...this.selectedField });
        }
      },
      getFieldSpan(field, index) {
        // 如果当前字段是被选中的字段，使用selectedField的最新值
        if (index === this.selectedFieldIndex && this.selectedField) {
          return this.selectedField.span || 24;
        }
        return field.span || 24;
      },
      editField (field, index) {
        this.selectField(field, index);
      },
      copyField (index) {
        const field = this.fields[index];
        const newField = {
          ...field,
          key: `field_${this.fieldCounter++}`,
          name: `${field.name}_copy`,
          label: `${field.label}_副本`,
        };
        this.fields.splice(index + 1, 0, newField);
        this.formData[newField.name] = this.getDefaultValue(newField.type);
        this.selectField(newField, index + 1);
      },
      removeField (index) {
        const field = this.fields[index];
        delete this.formData[field.name];
        this.fields.splice(index, 1);
        if (this.selectedFieldIndex === index) {
          this.selectedField = null;
          this.selectedFieldIndex = -1;
        } else if (this.selectedFieldIndex > index) {
          this.selectedFieldIndex--;
        }
      },
      getFieldComponent (type) {
        const components = {
          input: 'a-input',
          textarea: 'a-textarea',
          number: 'a-input-number',
          date: 'a-date-picker',
          select: 'a-select',
          radio: 'a-radio-group',
          checkbox: 'a-checkbox-group',
        };
        return components[type] || 'a-input';
      },
      getFieldProps (field) {
        // 如果当前字段是被选中的字段，使用selectedField的最新值
        const fieldIndex = this.fields.indexOf(field);
        const currentField = (fieldIndex === this.selectedFieldIndex && this.selectedField) 
          ? this.selectedField 
          : field;
        
        const props = {};
        if (currentField.type === 'textarea') {
          props.rows = 4;
        }
        if (currentField.type === 'number') {
          props.style = { width: '100%' };
        }
        if (currentField.type === 'date') {
          props.style = { width: '100%' };
          // 根据日期类型设置格式
          const dateType = currentField.dateType || 'date';
          if (dateType === 'month') {
            props.mode = 'month';
            props.format = 'YYYY-MM';
            props.valueFormat = 'YYYY-MM';
          } else if (dateType === 'year') {
            props.mode = 'year';
            props.format = 'YYYY';
            props.valueFormat = 'YYYY';
          } else if (dateType === 'range') {
            props.mode = 'range';
            props.format = 'YYYY-MM-DD';
            props.valueFormat = 'YYYY-MM-DD';
          } else {
            // 默认日期（年月日）
            props.format = 'YYYY-MM-DD';
            props.valueFormat = 'YYYY-MM-DD';
          }
        }
        if (['select', 'radio', 'checkbox'].includes(currentField.type) && !currentField.dictCode && currentField.options) {
          const options = [];
          currentField.options.split('\n').forEach(line => {
            const [value, text] = line.split(':');
            if (value && text) {
              options.push({ value: value.trim(), label: text.trim() });
            }
          });
          props.options = options;
        }
        return props;
      },
      handleOk () {
        if (this.fields.length === 0) {
          this.$message.warning('请至少添加一个字段');
          return;
        }
        
        // 确保使用最新的字段数据（包括selectedField的更新）
        this.syncSelectedFieldToFields();
        
        // 构建表单配置JSON
        const formConfig = {
          fields: this.fields.map(field => ({
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
            valueFormat: this.getDateValueFormat(field),
          })),
        };
        
        this.confirmLoading = true;
        createFormVersion({
          formId: this.formId,
          formConfig: JSON.stringify(formConfig),
          versionDesc: this.versionDesc,
        }).then((res) => {
          if (res.success) {
            this.$message.success('表单版本创建成功');
            this.$emit('ok');
            this.handleCancel();
          } else {
            this.$message.warning(res.message);
          }
        }).catch((err) => {
          this.$message.error('创建版本失败：' + (err.message || '未知错误'));
        }).finally(() => {
          this.confirmLoading = false;
        });
      },
      getDateFormat(field) {
        if (field.type !== 'date') return '';
        if (field.dateType === 'month') return 'YYYY-MM';
        if (field.dateType === 'year') return 'YYYY';
        if (field.dateType === 'range') return 'YYYY-MM-DD';
        return 'YYYY-MM-DD';
      },
      getDateValueFormat(field) {
        if (field.type !== 'date') return '';
        if (field.dateType === 'month') return 'YYYY-MM';
        if (field.dateType === 'year') return 'YYYY';
        if (field.dateType === 'range') return 'YYYY-MM-DD';
        return 'YYYY-MM-DD';
      },
      handleCancel () {
        this.visible = false;
        this.fields = [];
        this.formData = {};
        this.selectedField = null;
        this.selectedFieldIndex = -1;
        this.versionDesc = '';
      },
    }
  }
</script>

<style scoped>
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
  cursor: move;
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
}

.empty-tip {
  padding: 40px;
  text-align: center;
}

.field-library {
  padding: 8px;
}
</style>
