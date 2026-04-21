/**
 * JeecgFormMixin - 表单通用 Mixin
 * 提供表单的通用方法和数据
 */
import { httpAction, getAction } from '@/api/manage'

export const JeecgFormMixin = {
  data() {
    return {
      // 表单加载状态
      confirmLoading: false,
      // 表单数据模型
      model: {},
      // 表单布局配置
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 }
      },
      // 表单验证规则
      validatorRules: {},
      // API 接口地址配置
      url: {
        add: '',
        edit: '',
        queryById: ''
      }
    }
  },
  methods: {
    /**
     * 新增方法
     * 子组件可以重写此方法
     */
    add() {
      this.edit({})
    },
    
    /**
     * 编辑方法
     * @param {Object} record - 要编辑的记录
     * 子组件可以重写此方法
     */
    edit(record) {
      this.model = Object.assign({}, record)
    },
    
    /**
     * 提交表单
     * 子组件可以重写此方法
     */
    submitForm() {
      const that = this
      if (this.$refs.form) {
        this.$refs.form.validate((valid) => {
          if (valid) {
            that.confirmLoading = true
            let httpurl = ''
            let method = ''
            
            if (!that.model.id) {
              httpurl = that.url.add
              method = 'post'
            } else {
              httpurl = that.url.edit
              method = 'put'
            }
            
            httpAction(httpurl, that.model, method)
              .then((res) => {
                if (res.success) {
                  that.$message.success(res.message)
                  that.$emit('ok')
                } else {
                  that.$message.warning(res.message)
                }
              })
              .finally(() => {
                that.confirmLoading = false
              })
          }
        })
      }
    }
  }
}

