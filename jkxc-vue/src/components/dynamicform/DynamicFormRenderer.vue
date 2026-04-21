<template>
  <div class="dynamic-form-renderer">
    <a-form-model
      ref="dynamicForm"
      :model="internalFormData"
      :rules="formRules"
      :labelCol="labelCol"
      :wrapperCol="wrapperCol"
    >
      <a-row :gutter="16">
        <a-col
          v-for="(field, index) in fields"
          :key="field.name || index"
          :span="field.span || 24"
        >
          <a-form-model-item
            :label="field.label"
            :prop="field.name"
            :required="field.required"
          >
            <!-- 字典选择组件 - select和radio -->
            <j-dict-select-tag
              v-if="field.dictCode && ['select', 'radio'].includes(field.type)"
              v-model="internalFormData[field.name]"
              :dictCode="field.dictCode"
              :placeholder="field.placeholder"
              :disabled="field.disabled || formDisabled"
              :type="field.type === 'select' ? 'list' : field.type"
              @change="handleFieldChange(field, $event)"
            />
            <!-- 字典多选组件 - checkbox -->
            <j-multi-select-tag
              v-else-if="field.dictCode && field.type === 'checkbox'"
              :value="getCheckboxValue(field.name)"
              :dictCode="field.dictCode"
              :placeholder="field.placeholder"
              :disabled="field.disabled || formDisabled"
              type="checkbox"
              @change="handleCheckboxChange(field, $event)"
            />
            <!-- 日期选择器（month/year模式需要特殊处理） -->
            <a-date-picker
              v-else-if="field.type === 'date' && (field.dateType === 'month' || field.dateType === 'year')"
              :value="getDateValue(field)"
              :placeholder="field.placeholder"
              :disabled="field.disabled || formDisabled"
              v-bind="getFieldProps(field)"
              @panelChange="handleDatePanelChange(field, $event)"
            />
            <!-- 普通组件 -->
            <component
              v-else
              :is="getFieldComponent(field.type)"
              v-model="internalFormData[field.name]"
              :placeholder="field.placeholder"
              :disabled="field.disabled || formDisabled"
              v-bind="getFieldProps(field)"
              @change="handleFieldChange(field, $event)"
            />
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>
  </div>
</template>

<script>
import moment from 'moment'

export default {
  name: 'DynamicFormRenderer',
  props: {
    formConfig: {
      type: String,
      default: '{}',
    },
    formData: {
      type: Object,
      default: () => ({}),
    },
    formDisabled: {
      type: Boolean,
      default: false,
    },
    labelCol: {
      type: Object,
      default: () => ({ xs: { span: 24 }, sm: { span: 5 } }),
    },
    wrapperCol: {
      type: Object,
      default: () => ({ xs: { span: 24 }, sm: { span: 16 } }),
    },
  },
  data() {
    return {
      fields: [],
      internalFormData: {},
      formRules: {},
    }
  },
  watch: {
    formConfig: {
      immediate: true,
      handler(newVal) {
        this.parseFormConfig(newVal);
      },
    },
    formData: {
      immediate: true,
      deep: true,
      handler(newVal) {
        if (newVal && Object.keys(newVal).length > 0) {
          // 合并新数据到内部数据，保留已有值
          Object.keys(newVal).forEach(key => {
            this.$set(this.internalFormData, key, newVal[key]);
          });
        } else if (!newVal || Object.keys(newVal).length === 0) {
          // 如果新数据为空，保持内部数据不变（可能是初始化）
          // 只有在明确传入空对象时才清空
        }
      },
    },
  },
  methods: {
    parseFormConfig(configStr) {
      try {
        const config = typeof configStr === 'string' ? JSON.parse(configStr) : configStr;
        this.fields = config.fields || [];
        this.buildFormRules();
        this.initFormData();
      } catch (e) {
        console.error('解析表单配置失败:', e);
        this.fields = [];
      }
    },
    buildFormRules() {
      const rules = {};
      this.fields.forEach(field => {
        if (field.required) {
          rules[field.name] = [
            { required: true, message: `请输入${field.label}`, trigger: field.type === 'select' || field.type === 'radio' ? 'change' : 'blur' },
          ];
        }
      });
      this.formRules = rules;
    },
    initFormData() {
      this.fields.forEach(field => {
        if (!(field.name in this.internalFormData)) {
          if (field.defaultValue !== undefined && field.defaultValue !== '') {
            this.$set(this.internalFormData, field.name, field.defaultValue);
          } else {
            // 根据字段类型设置默认值
            const defaultValue = this.getDefaultValue(field);
            this.$set(this.internalFormData, field.name, defaultValue);
          }
        } else {
          // 如果已有值，需要根据字段类型进行转换
          this.normalizeFieldValue(field);
        }
      });
      this.$emit('update:formData', this.internalFormData);
    },
    normalizeFieldValue(field) {
      // 对于checkbox类型，根据是否有dictCode进行数据格式转换
      if (field.type === 'checkbox') {
        const value = this.internalFormData[field.name];
        if (field.dictCode) {
          // 字典类型：使用字符串格式（逗号分隔）
          if (Array.isArray(value)) {
            this.$set(this.internalFormData, field.name, value.join(','));
          }
        } else {
          // 非字典类型：使用数组格式
          if (typeof value === 'string' && value) {
            this.$set(this.internalFormData, field.name, value.split(',').filter(v => v));
          } else if (!value) {
            this.$set(this.internalFormData, field.name, []);
          }
        }
      }
    },
    getDefaultValue(field) {
      // checkbox根据是否有dictCode返回不同的默认值
      if (field.type === 'checkbox') {
        return field.dictCode ? '' : []; // 字典用字符串，非字典用数组
      }
      
      const defaults = {
        input: '',
        textarea: '',
        number: null,
        date: null,
        select: undefined,
        radio: undefined,
      };
      return defaults[field.type] !== undefined ? defaults[field.type] : '';
    },
    getCheckboxValue(fieldName) {
      const value = this.internalFormData[fieldName];
      if (Array.isArray(value)) {
        return value.join(',');
      }
      return value || '';
    },
    handleCheckboxChange(field, value) {
      // JMultiSelectTag返回的是逗号分隔的字符串
      this.$set(this.internalFormData, field.name, value);
      this.$emit('update:formData', this.internalFormData);
      this.handleFieldChange(field, value);
    },
    getFieldComponent(type) {
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
    getFieldProps(field) {
      const props = {};
      
      // 文本域行数
      if (field.type === 'textarea') {
        props.rows = field.rows || 4;
      }
      
      // 数字输入框
      if (field.type === 'number') {
        props.style = { width: '100%' };
        if (field.min !== undefined) props.min = field.min;
        if (field.max !== undefined) props.max = field.max;
        if (field.precision !== undefined) props.precision = field.precision;
      }
      
      // 日期选择器
      if (field.type === 'date') {
        props.style = { width: '100%' };
        const dateType = field.dateType || 'date';
        
        if (dateType === 'month') {
          props.mode = 'month';
          props.format = field.format || 'YYYY-MM';
          props.valueFormat = field.valueFormat || 'YYYY-MM';
        } else if (dateType === 'year') {
          props.mode = 'year';
          props.format = field.format || 'YYYY';
          props.valueFormat = field.valueFormat || 'YYYY';
        } else if (dateType === 'range') {
          props.mode = 'range';
          props.format = field.format || 'YYYY-MM-DD';
          props.valueFormat = field.valueFormat || 'YYYY-MM-DD';
        } else {
          // 默认日期（年月日）
          props.format = field.format || 'YYYY-MM-DD';
          props.valueFormat = field.valueFormat || 'YYYY-MM-DD';
        }
      }
      
      // 下拉选择、单选、多选（非字典）
      if (['select', 'radio', 'checkbox'].includes(field.type) && !field.dictCode) {
        if (field.options) {
          const options = [];
          if (typeof field.options === 'string') {
            // 字符串格式：每行一个选项，格式：值:文本
            field.options.split('\n').forEach(line => {
              const [value, text] = line.split(':');
              if (value && text) {
                options.push({ value: value.trim(), label: text.trim() });
              }
            });
          } else if (Array.isArray(field.options)) {
            // 数组格式
            options.push(...field.options);
          }
          
          if (field.type === 'select') {
            props.options = options;
          } else if (field.type === 'radio') {
            props.options = options;
          } else if (field.type === 'checkbox') {
            // checkbox需要特殊处理，使用a-checkbox-group
            props.options = options;
          }
        }
      }
      
      return props;
    },
    getDateValue(field) {
      const value = this.internalFormData[field.name];
      if (!value) return null;
      
      const dateType = field.dateType || 'date';
      const format = field.format || (dateType === 'month' ? 'YYYY-MM' : dateType === 'year' ? 'YYYY' : 'YYYY-MM-DD');
      
      // 使用moment解析日期值
      return moment(value, format);
    },
    handleDatePanelChange(field, value) {
      if (!value) {
        this.$set(this.internalFormData, field.name, null);
        this.$emit('update:formData', this.internalFormData);
        this.handleFieldChange(field, null);
        return;
      }
      
      const dateType = field.dateType || 'date';
      const valueFormat = field.valueFormat || (dateType === 'month' ? 'YYYY-MM' : dateType === 'year' ? 'YYYY' : 'YYYY-MM-DD');
      
      // 格式化日期值
      const formattedValue = value.format(valueFormat);
      this.$set(this.internalFormData, field.name, formattedValue);
      this.$emit('update:formData', this.internalFormData);
      this.handleFieldChange(field, formattedValue);
    },
    handleFieldChange(field, value) {
      this.$emit('field-change', field, value);
    },
    validate(callback) {
      return this.$refs.dynamicForm.validate(callback);
    },
    resetFields() {
      this.$refs.dynamicForm.resetFields();
    },
    getFormData() {
      return { ...this.internalFormData };
    },
  },
}
</script>

<style scoped>
.dynamic-form-renderer {
  padding: 16px;
}
</style>
