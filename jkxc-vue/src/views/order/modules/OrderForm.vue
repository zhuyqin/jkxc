<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row :gutter="24">
          <!-- 基本信息：选择客户后自动填充 -->
          <a-card title="基本信息" :bordered="false" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-model-item label="客户名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="companyName">
                  <j-search-select-tag
                    :key="'companyName_' + (model.id || 'new')"
                    placeholder="请输入客户名称"
                    v-model="model.companyName"
                    dict="gh_customer,corporate_name,corporate_name,del_flag=0"
                    @change="companyNameChange"
                    :async="true"
                    :disabled="formDisabled"
                  >
                  </j-search-select-tag>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="所属区域" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="region">
                  <a-cascader v-model="model.regionCascader" :options="areaOptions" placeholder="请选择 省/市/区" style="width: 100%" :disabled="true" />
                  <div v-if="(!model.regionCascader || model.regionCascader.length === 0) && model.region" style="margin-top: 8px; color: #faad14;">
                    历史区域：{{ model.region }}（未在省市区数据中，按原值展示）
                  </div>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="联系人员" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contacts">
                  <a-input v-model="model.contacts" placeholder="请输入联系人" :disabled="true"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="联系方式" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contactInformation">
                  <a-input v-model="model.contactInformation" placeholder="请输入联系方式" :disabled="true"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="24">
                <a-form-model-item label="详细地址" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="address">
                  <a-textarea v-model="model.address" rows="3" placeholder="请输入详细地址" :disabled="true"></a-textarea>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="业务人员" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="salesman">
                  <j-search-select-tag
                    placeholder="请选择用户"
                    v-model="model.salesman"
                    dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                    :async="false"
                  >
                  </j-search-select-tag>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="商机来源" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="opportunitySource">
                  <j-dict-select-tag
                    placeholder="请选择商机来源"
                    v-model="model.opportunitySource"
                    dictCode="opportunity_source"
                    :disabled="formDisabled"
                  />
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-card>

          <!-- 服务项目：固定输入内容 -->
          <a-card title="服务项目" :bordered="false" style="margin-top: 16px" :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-model-item label="业务类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="businessType">
                  <j-category-select
                    v-model="model.businessType"
                    back="label"
                    pcode="A01"
                    placeholder="请选择业务类型"
                    loadTriggleChange
                    @change="handleBusinessTypeChange"
                    @select="handleBusinessTypeChange"
                  />
                  <div v-if="processBindingWarning" style="margin-top: 4px;">
                    <a-alert :message="processBindingWarning" type="warning" show-icon :closable="false" />
                  </div>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="合同金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contractAmount">
                  <a-input v-model="model.contractAmount" placeholder="请输入合同金额" type="number" @input="calculateFinalPayment"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="收款金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="receivedAmount">
                  <a-input v-model="model.receivedAmount" placeholder="已收款金额" type="number" @input="calculateFinalPayment"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="收款时间" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="collectionTime">
                  <j-date placeholder="请选择收款时间" v-model="model.collectionTime" style="width: 100%" />
                </a-form-model-item>
              </a-col>
              <a-col :span="24">
                <a-form-model-item label="收款账户" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="collectionAccountNumber">
                  <a-cascader
                    v-model="model.collectionAccountCascader"
                    :options="collectionAccountOptions"
                    placeholder="请选择收款方式/收款单位/人/网点名称"
                    :loading="collectionAccountLoading"
                    :change-on-select="false"
                    :show-search="true"
                    :field-names="{ label: 'label', value: 'value', children: 'children' }"
                    @change="handleCollectionAccountChange"
                    style="width: 100%"
                    :getPopupContainer="(triggerNode) => triggerNode.parentNode"
                    :popupStyle="{ minWidth: '600px', maxHeight: '400px' }"
                  />
                </a-form-model-item>
              </a-col>
              <a-col :span="12" v-if="qrCodeImage">
                <a-form-model-item label="收款码" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <img 
                    :src="qrCodeImage" 
                    height="100px" 
                    alt="收款码" 
                    @click="handleQrCodeClick(qrCodeImage)" 
                    style="max-width:150px;cursor: pointer;border: 1px solid #d9d9d9;border-radius: 4px;padding: 4px;"
                  />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item label="尾款金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="finalPaymentAmount">
                  <a-input v-model="model.finalPaymentAmount" placeholder="尾款金额" type="number" :readOnly="true"></a-input>
                </a-form-model-item>
              </a-col>
              <a-col :span="24">
                <a-form-model-item label="图片凭证" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="imageVoucher">
                  <j-image-upload v-model="model.imageVoucher" text="上传" :isMultiple="true" bizPath="order"></j-image-upload>
                </a-form-model-item>
              </a-col>
              <a-col :span="24">
                <a-form-model-item label="备注" :labelCol="labelColc" :wrapperCol="wrapperColc" prop="remarks">
                  <a-textarea v-model="model.remarks" rows="4" placeholder="请输入备注"></a-textarea>
                </a-form-model-item>
              </a-col>
            </a-row>  
          </a-card>

          <!-- 动态表单：之前配置的表单 -->
          <a-card 
            v-if="dynamicFormConfig" 
            title="扩展信息" 
            :bordered="false" 
            style="margin-top: 16px"
            :headStyle="{borderBottom: '1px solid #f0f0f0', paddingBottom: '12px'}"
          >
            <dynamic-form-renderer
              :formConfig="dynamicFormConfig"
              :formData="dynamicFormData"
              :formDisabled="formDisabled"
              :labelCol="labelCol"
              :wrapperCol="wrapperCol"
              @field-change="handleDynamicFieldChange"
              ref="dynamicForm"
            />
          </a-card>
        </a-row>
      </a-form-model>
    </j-form-container>
    <!-- 收款码预览弹窗 -->
    <j-modal
      :visible.sync="qrCodeModal.visible"
      :width="600"
      title="收款码预览"
      :fullscreen.sync="qrCodeModal.fullscreen"
      :switchFullscreen="qrCodeModal.switchFullscreen"
    >
      <img alt="收款码" style="width: 100%" :src="qrCodeModal.imageUrl" />
    </j-modal>
  </a-spin>
</template>

<script>
import { httpAction, getAction } from '@/api/manage'
import { getFormVersionByBusinessType, queryVersionById, getProcessByBusinessType } from '@/api/api'
import DynamicFormRenderer from '@/components/dynamicform/DynamicFormRenderer'
import areaData from 'china-area-data'

export default {
  name: 'OrderForm',
  components: {
    DynamicFormRenderer,
  },
  props: {
    disabled: {
      type: Boolean,
      default: false,
      required: false
    },
    normal: {
      type: Boolean,
      default: false,
      required: false
    }
  },
  data() {
    return {
      model: {},
      areaOptions: [],
      collectionAccountOptions: [], // 收款账户级联选项（三级：收款方式->收款单位/人->网点名称）
      collectionAccountLoading: false, // 收款账户加载状态
      qrCodeImage: '', // 收款码图片
      qrCodeModal: {
        visible: false,
        imageUrl: '',
        fullscreen: false,
        switchFullscreen: false,
      },
      dynamicFormConfig: null,
      dynamicFormVersionId: null,
      dynamicFormId: null, // 表单ID
      dynamicFormData: {},
      processBindingWarning: '', // 流程绑定警告信息
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 },
      },
      labelColc: {
        xs: { span: 24 },
        sm: { span: 3 },
      },
      wrapperColc: {
        xs: { span: 24 },
        sm: { span: 21 },
      },
      confirmLoading: false,
      validatorRules: {
        companyName: [{ required: true, message: '请输入客户名称!', trigger: 'blur' }],
        salesman: [{ required: true, message: '请选择业务人员!', trigger: 'change' }],
        opportunitySource: [{ required: true, message: '请选择商机来源!', trigger: 'change' }],
        businessType: [{ required: true, message: '请选择业务类型!', trigger: 'change' }],
        contractAmount: [{ required: true, message: '请输入合同金额!', trigger: 'blur' }],
        collectionTime: [{ required: true, message: '请选择收款时间!', trigger: 'change' }],
        collectionAccountNumber: [{ required: true, message: '请选择收款账户!', trigger: 'change' }],
        receivedAmount: [{ required: true, message: '请输入收款金额!', trigger: 'blur' }],
      },
      url: {
        add: '/order/add',
        edit: '/order/edit',
        queryById: '/order/queryById',
      },
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  watch: {
    // 监听业务类型变化，动态切换动态表单
    'model.businessType': {
      handler(newVal, oldVal) {
        // 只有在值真正变化时才触发（避免初始化时触发）
        if (newVal !== oldVal && newVal) {
          // 延迟一下，确保v-model已经更新
          this.$nextTick(() => {
            this.handleBusinessTypeChange(newVal);
          });
        } else if (!newVal) {
          // 如果业务类型被清空，清空动态表单
          this.dynamicFormConfig = null;
          this.dynamicFormVersionId = null;
          this.dynamicFormId = null;
          this.dynamicFormData = {};
        }
      },
      immediate: false
    },
    // 监听动态表单配置变化
    'dynamicFormConfig': {
      handler(newVal, oldVal) {
        // 配置变化时的处理逻辑（如果需要）
      },
      immediate: true
    }
  },
  created() {
    // 初始化省市区级联数据
    try {
      const provinces = areaData['86'] || {}
      this.areaOptions = Object.keys(provinces).map(pCode => ({
        value: provinces[pCode],
        label: provinces[pCode],
        children: (areaData[pCode] ? Object.keys(areaData[pCode]).map(cCode => ({
          value: areaData[pCode][cCode],
          label: areaData[pCode][cCode],
          children: (areaData[cCode] ? Object.keys(areaData[cCode]).map(aCode => ({
            value: areaData[cCode][aCode],
            label: areaData[cCode][aCode]
          })) : [])
        })) : [])
      }))
    } catch (e) {
      this.areaOptions = []
    }
    // 加载收款账户级联数据
    this.loadCollectionAccountOptions()
  },
  methods: {
    add() {
      // 新增订单时，清空动态表单相关数据，确保根据业务类型加载最新版本
      this.edit({})
      // 清空动态表单配置，等待业务类型选择后加载
      this.dynamicFormConfig = null
      this.dynamicFormVersionId = null
      this.dynamicFormId = null
      this.dynamicFormData = {}
    },
    edit(record) {
      this.model = Object.assign({}, record)
      this.visible = true
      this.qrCodeImage = '' // 清空收款码
      this.collectionAccountOptions = []
      
      // 新增订单时设置默认订单状态为"1"（进行中）
      if (!this.model.id && !this.model.orderStatus) {
        this.$set(this.model, 'orderStatus', '1')
      }
      
      // 初始化尾款金额为0（如果不存在）
      if (!this.model.finalPaymentAmount) {
        this.$set(this.model, 'finalPaymentAmount', 0)
      }
      
      // 如果传入了公司名称但没有公司ID，且没有其他客户信息，则触发一次客户信息加载
      // 如果已经传入了完整的客户信息（companyId存在），则不需要再次查询
      if (this.model.companyName && !this.model.companyId && (!this.model.contacts || !this.model.contactInformation)) {
        // 延迟触发，确保表单已经初始化
        this.$nextTick(() => {
          this.companyNameChange(this.model.companyName)
        })
      }
      
      // 计算尾款金额
      this.calculateFinalPayment()
      
      // 处理区域级联选择器
      if (this.model.region && (!this.model.regionCascader || this.model.regionCascader.length === 0)) {
        const guess = this.model.region.split(/[\/-]/).filter(Boolean)
        if (guess.length) {
          this.$set(this.model, 'regionCascader', guess)
        }
      }
      
      // 加载级联数据
      this.loadCollectionAccountOptions()
      
      // 如果有收款账号ID，反向查找级联路径并加载收款码
      if (this.model.collectionAccountNumber) {
        // 等待级联数据加载完成后再回显
        this.$nextTick(() => {
          setTimeout(() => {
            this.loadCollectionAccountCascaderValue()
            this.loadAccountQrCode(this.model.collectionAccountNumber)
          }, 500) // 延迟500ms确保级联数据已加载
        })
      }
      
      // 如果是编辑模式，加载动态表单数据
      if (record.id) {
        // 先保存动态表单数据（在加载配置之前）
        const savedFormData = record.dynamicFormData;
        const savedFormConfig = record.dynamicFormConfig; // 保存的表单配置
        
        // 编辑模式：优先使用保存的表单配置（确保显示的是创建时的表单）
        if (savedFormConfig) {
          // 优先使用保存的表单配置
          try {
            this.dynamicFormConfig = typeof savedFormConfig === 'string' 
              ? JSON.parse(savedFormConfig) 
              : savedFormConfig;
            this.dynamicFormVersionId = record.dynamicFormVersionId || record.formVersionId;
            this.dynamicFormId = record.formId;
            // 配置加载完成后，再加载数据
            this.$nextTick(() => {
              this.loadDynamicFormData(record, savedFormData);
            });
          } catch (e) {
            console.error('解析保存的表单配置失败:', e);
            // 如果解析失败，回退到通过版本ID加载
            this.loadFormConfigByVersionId(record, savedFormData);
          }
        } else if (record.dynamicFormVersionId || record.formVersionId) {
          // 如果没有保存的配置，通过版本ID加载（兼容旧数据）
          this.loadFormConfigByVersionId(record, savedFormData);
        }
      } else if (record.businessType) {
        // 新增模式：根据业务类型加载最新版本
        this.loadDynamicForm(record.businessType).then((res) => {
          this.$nextTick(() => {
            this.loadDynamicFormData(record, null);
          });
        }).catch((err) => {
          // 如果加载失败，但有保存的数据，也尝试加载
          if (savedFormData) {
            this.$nextTick(() => {
              this.loadDynamicFormData(record, savedFormData);
            });
          }
        });
      }
    },
    loadFormConfigByVersionId(record, savedFormData) {
      // 编辑/查看详情模式：使用保存的版本ID
      const versionId = record.dynamicFormVersionId || record.formVersionId;
      this.dynamicFormVersionId = versionId;
      this.dynamicFormId = record.formId;
      
      // 加载表单配置
      queryVersionById({ id: versionId }).then((res) => {
        if (res.success && res.result) {
          this.dynamicFormConfig = res.result.formConfig;
          // 配置加载完成后，再加载数据
          this.$nextTick(() => {
            this.loadDynamicFormData(record, savedFormData);
          });
        } else {
          // 配置加载失败（可能是表单已删除），但仍尝试加载已有数据
          if (savedFormData) {
            this.$nextTick(() => {
              this.loadDynamicFormData(record, savedFormData);
            });
          }
        }
      }).catch((err) => {
        // 即使异常，也尝试加载已有数据
        if (savedFormData) {
          this.$nextTick(() => {
            this.loadDynamicFormData(record, savedFormData);
          });
        }
      });
    },
    loadDynamicFormData(record, savedFormData) {
      // 加载动态表单数据
      const formDataToLoad = savedFormData || record.dynamicFormData || this.model.dynamicFormData;
      
      if (formDataToLoad) {
        let parsedData = {};
        // 如果dynamicFormData是字符串，需要解析
        if (typeof formDataToLoad === 'string') {
          try {
            parsedData = JSON.parse(formDataToLoad);
          } catch (e) {
            parsedData = {};
          }
        } else {
          parsedData = formDataToLoad;
        }
        
        // 使用$set确保响应式更新
        this.$set(this, 'dynamicFormData', parsedData);
        
        // 确保动态表单组件已渲染后再设置数据
        this.$nextTick(() => {
          if (this.$refs.dynamicForm) {
            if (this.$refs.dynamicForm.internalFormData) {
              // 直接更新内部数据
              Object.keys(parsedData).forEach(key => {
                this.$set(this.$refs.dynamicForm.internalFormData, key, parsedData[key]);
              });
            } else {
              // 如果内部数据不存在，等待一下再试
              setTimeout(() => {
                if (this.$refs.dynamicForm && this.$refs.dynamicForm.internalFormData) {
                  Object.keys(parsedData).forEach(key => {
                    this.$set(this.$refs.dynamicForm.internalFormData, key, parsedData[key]);
                  });
                }
              }, 500);
            }
          }
        });
      } else {
        this.$set(this, 'dynamicFormData', {});
      }
    },
    loadDynamicFormByVersionId(versionId) {
      // 通过版本ID加载表单配置
      // 这里需要调用接口获取版本信息
      return getAction('/dynamicform/form/queryVersionById', { id: versionId }).then((res) => {
        if (res.success && res.result) {
          this.dynamicFormConfig = res.result.formConfig;
          this.dynamicFormVersionId = res.result.id;
          this.dynamicFormId = res.result.formId;
        }
        return res;
      }).catch(() => {
        return { success: false };
      });
    },
    submitForm() {
      const that = this
      
      // 防重复提交：如果正在提交中，直接返回
      if (that.confirmLoading) {
        that.$message.warning('正在提交中，请勿重复操作')
        return
      }
      
      // 先验证动态表单（异步验证）
      const validateDynamicForm = () => {
        return new Promise((resolve) => {
          if (this.dynamicFormConfig && this.$refs.dynamicForm) {
            this.$refs.dynamicForm.validate((valid) => {
              resolve(valid);
            });
          } else {
            resolve(true); // 没有动态表单，直接通过
          }
        });
      };
      
      // 触发表单验证
      validateDynamicForm().then((dynamicFormValid) => {
        if (!dynamicFormValid) {
          this.$message.warning('请完善动态表单信息');
          return;
        }
        
        this.$refs.form.validate((valid) => {
        if (valid) {
          // 设置提交中状态，防止重复提交
          that.confirmLoading = true
          let httpurl = ''
          let method = ''
          
          // 将 regionCascader 转换为 region 字符串
          if (this.model.regionCascader && this.model.regionCascader.length > 0) {
            this.model.region = this.model.regionCascader.join('/')
          }
          
          // 获取完整的表单数据（包含动态表单数据）
          const submitData = that.getFormData();
          
          // 订单编号由后端自动生成，前端不提交contractNo
          delete submitData.contractNo;
          
          // 将合同金额映射到订单金额（如果合同金额存在）
          if (submitData.contractAmount && !submitData.orderAmount) {
            submitData.orderAmount = submitData.contractAmount;
          }
          
          // 确保动态表单数据被包含（即使没有dynamicFormVersionId，如果有动态表单配置也要保存）
          if (that.dynamicFormConfig && that.$refs.dynamicForm) {
            const dynamicData = that.$refs.dynamicForm.getFormData();
            if (dynamicData && Object.keys(dynamicData).length > 0) {
              submitData.dynamicFormData = dynamicData;
              submitData.dynamicFormVersionId = that.dynamicFormVersionId;
              submitData.formId = that.dynamicFormId;
              // 保存表单配置（用于查看详情时还原）
              if (that.dynamicFormConfig) {
                submitData.dynamicFormConfig = JSON.stringify(that.dynamicFormConfig);
              }
            }
          }
          
          // 将动态表单数据转换为JSON字符串
          if (submitData.dynamicFormData && typeof submitData.dynamicFormData === 'object') {
            submitData.dynamicFormData = JSON.stringify(submitData.dynamicFormData);
          }
          
          // 将图片凭证转换为JSON字符串（如果是数组）
          if (submitData.imageVoucher && Array.isArray(submitData.imageVoucher)) {
            submitData.imageVoucher = JSON.stringify(submitData.imageVoucher);
          }
          
          if (!this.model.id) {
            httpurl += this.url.add
            method = 'post'
          } else {
            httpurl += this.url.edit
            method = 'put'
          }
          httpAction(httpurl, submitData, method)
            .then((res) => {
              if (res.success) {
                that.$message.success(res.message)
                that.$emit('ok')
              } else {
                that.$message.warning(res.message)
              }
            })
            .catch((err) => {
              that.$message.error('操作失败')
            })
            .finally(() => {
              that.confirmLoading = false
            })
        }
      })
      })
    },
    companyNameChange(value) {
      // 客户名称变化时，加载客户信息并自动填充
      // value是客户名称（corporate_name），不是ID
      if (value) {
        // 根据客户名称查询客户列表
        getAction('/customer/ghCustomer/list', { corporateName: value, delFlag: 0, pageNo: 1, pageSize: 1 }).then((res) => {
          if (res.success && res.result && res.result.records && res.result.records.length > 0) {
            const customer = res.result.records[0]
            // 保存公司ID
            this.$set(this.model, 'companyId', customer.id)
            // 自动填充客户信息
            this.$set(this.model, 'contacts', customer.contacts || '')
            this.$set(this.model, 'contactInformation', customer.contactInformation || '')
            this.$set(this.model, 'address', customer.address || '')
            this.$set(this.model, 'region', customer.region || '')
            
            // 处理区域级联选择器
            if (customer.region) {
              const guess = customer.region.split(/[\/-]/).filter(Boolean)
              if (guess.length) {
                this.$set(this.model, 'regionCascader', guess)
              }
            }
            
            // 查询客户关联的线索，获取商机来源并自动填充（新增和编辑都支持）
            if (customer.id) {
              this.loadOpportunitySourceByCustomerId(customer.id)
            }
          }
        }).catch(() => {
          // 查询失败时不做处理，保持当前状态
        })
      } else {
        // 清空客户相关信息
        this.$set(this.model, 'companyId', '')
        this.$set(this.model, 'contacts', '')
        this.$set(this.model, 'contactInformation', '')
        this.$set(this.model, 'address', '')
        this.$set(this.model, 'region', '')
        this.$set(this.model, 'regionCascader', [])
        // 如果是新增订单，清空商机来源
        if (!this.model.id) {
          this.$set(this.model, 'opportunitySource', '')
        }
      }
    },
    // 根据客户ID查询关联的线索，获取商机来源
    loadOpportunitySourceByCustomerId(customerId) {
      if (!customerId) {
        return
      }
      // 查询该客户关联的线索（通过customerId字段）
      getAction('/opportunity/ghOpportunity/list', { 
        customerId: customerId, 
        delFlag: 0, 
        pageNo: 1, 
        pageSize: 1 
      }).then((res) => {
        if (res.success && res.result && res.result.records && res.result.records.length > 0) {
          const opportunity = res.result.records[0]
          // 如果线索有商机来源，自动填充到订单
          if (opportunity.opportunitySource) {
            this.$set(this.model, 'opportunitySource', opportunity.opportunitySource)
            console.log('已自动填充商机来源:', opportunity.opportunitySource)
          } else {
            console.log('线索未设置商机来源')
          }
        } else {
          console.log('未找到关联的线索，客户ID:', customerId)
        }
      }).catch((err) => {
        console.error('查询线索失败:', err)
        // 查询失败时不做处理，保持当前状态
      })
    },
    handleBusinessTypeChange(value, obj) {
      // 业务类型变化时，加载对应的动态表单并检查流程绑定
      // value可能是字符串（业务类型ID）或对象，需要处理
      const businessTypeId = typeof value === 'string' ? value : (value && value.value ? value.value : value)
      
      // 清空之前的警告信息
      this.processBindingWarning = ''
      
      if (businessTypeId) {
        // 检查业务类型是否绑定了审核流程
        this.checkProcessBinding(businessTypeId)
        // 加载对应的动态表单
        this.loadDynamicForm(businessTypeId);
      } else {
        this.dynamicFormConfig = null;
        this.dynamicFormVersionId = null;
        this.dynamicFormId = null;
        this.dynamicFormData = {};
      }
    },
    checkProcessBinding(businessTypeId) {
      // 检查业务类型是否绑定了审核流程
      if (!businessTypeId) {
        this.processBindingWarning = ''
        return
      }
      
      getProcessByBusinessType({ businessTypeId: businessTypeId }).then((res) => {
        if (res.success) {
          if (!res.result) {
            // 没有绑定流程，显示警告
            this.processBindingWarning = '该业务类型未配置审核流程，请在"审核流程管理"中为该业务类型绑定审核流程！'
          } else {
            // 已绑定流程，清空警告
            this.processBindingWarning = ''
          }
        } else {
          // 查询失败，清空警告（避免显示错误信息）
          this.processBindingWarning = ''
        }
      }).catch(() => {
        // 查询异常，清空警告
        this.processBindingWarning = ''
      })
    },
    loadDynamicForm(businessType) {
      // 新增订单时加载动态表单：只加载未删除的最新版本
      // 如果表单被删除或版本不存在，就不显示动态表单
      return getFormVersionByBusinessType({ businessType: businessType }).then((res) => {
        if (res.success && res.result && res.result.formConfig) {
          // 成功获取到未删除的表单版本
          this.dynamicFormConfig = res.result.formConfig;
          this.dynamicFormVersionId = res.result.id;
          this.dynamicFormId = res.result.formId; // 保存表单ID
          this.dynamicFormData = {};
        } else {
          // 表单被删除、版本不存在或未绑定表单，清空动态表单配置
          this.dynamicFormConfig = null;
          this.dynamicFormVersionId = null;
          this.dynamicFormId = null;
          this.dynamicFormData = {};
        }
        return res;
      }).catch((err) => {
        // 查询异常，清空动态表单配置
        this.dynamicFormConfig = null;
        this.dynamicFormVersionId = null;
        this.dynamicFormId = null;
        this.dynamicFormData = {};
        return { success: false };
      });
    },
    handleDynamicFieldChange(field, value) {
      // 动态表单字段变化时的处理
    },
    calculateFinalPayment() {
      // 计算尾款金额 = 合同金额 - 收款金额
      const contractAmount = parseFloat(this.model.contractAmount) || 0
      const receivedAmount = parseFloat(this.model.receivedAmount) || 0
      const finalPaymentAmount = contractAmount - receivedAmount
      this.$set(this.model, 'finalPaymentAmount', finalPaymentAmount >= 0 ? finalPaymentAmount : 0)
    },
    loadCollectionAccountOptions() {
      // 加载所有收款账号，构建三级级联数据：收款方式 -> 收款单位/人 -> 网点名称
      this.collectionAccountLoading = true
      // 使用listAll接口获取所有数据（不分页）
      return getAction('/bank/ghBankManagement/listAll').then((res) => {
        if (res.success && res.result) {
          const accounts = Array.isArray(res.result) ? res.result : []
          
          // 按收款方式分组
          const paymentMethodMap = {}
          accounts.forEach(acc => {
            // 只要求有paymentMethod和payeePerson，accountNotes可以为空（但会显示为空字符串）
            if (!acc.paymentMethod || !acc.payeePerson) {
              return // 跳过不完整的数据
            }
            
            if (!paymentMethodMap[acc.paymentMethod]) {
              paymentMethodMap[acc.paymentMethod] = {}
            }
            
            if (!paymentMethodMap[acc.paymentMethod][acc.payeePerson]) {
              paymentMethodMap[acc.paymentMethod][acc.payeePerson] = []
            }
            
            paymentMethodMap[acc.paymentMethod][acc.payeePerson].push({
              id: acc.id,
              accountNotes: acc.accountNotes || '', // 允许为空，显示为空字符串
              pic: acc.pic
            })
          })
          
          // 构建级联数据结构
          this.collectionAccountOptions = Object.keys(paymentMethodMap).map(paymentMethod => {
            const payeePersonMap = paymentMethodMap[paymentMethod]
            const payeePersonList = Object.keys(payeePersonMap).map(payeePerson => ({
              label: payeePerson,
              value: `${paymentMethod}__${payeePerson}`, // 使用分隔符组合作为value
              children: payeePersonMap[payeePerson].map(acc => ({
                label: acc.accountNotes,
                value: acc.id, // 第三级保存的是账号ID
                accountId: acc.id,
                pic: acc.pic
              }))
            }))
            
            return {
              label: paymentMethod,
              value: paymentMethod,
              children: payeePersonList
            }
          })
        } else {
          this.collectionAccountOptions = []
        }
      }).catch(() => {
        this.collectionAccountOptions = []
        return Promise.resolve()
      }).finally(() => {
        this.collectionAccountLoading = false
      })
    },
    handleCollectionAccountChange(value, selectedOptions) {
      // 级联选择器变化时，保存账号ID并加载收款码
      if (value && value.length === 3) {
        const accountId = value[2] // 第三级是账号ID
        this.model.collectionAccountNumber = accountId
        
        // 查找选中的账号信息
        if (selectedOptions && selectedOptions.length === 3) {
          const accountInfo = selectedOptions[2]
          if (accountInfo.pic) {
            this.qrCodeImage = this.getFileAccessHttpUrl(accountInfo.pic)
          } else {
            // 如果没有pic，查询详情
            getAction('/bank/ghBankManagement/queryById', { id: accountId }).then((res) => {
              if (res.success && res.result && res.result.pic) {
                this.qrCodeImage = this.getFileAccessHttpUrl(res.result.pic)
              }
            })
          }
        }
      } else {
        this.model.collectionAccountNumber = undefined
        this.qrCodeImage = ''
        this.qrCodeModal.visible = false
      }
    },
    handleQrCodeClick(imageUrl) {
      // 点击收款码预览大图
      this.qrCodeModal.imageUrl = imageUrl
      this.qrCodeModal.visible = true
    },
    getFileAccessHttpUrl(avatar) {
      // 处理图片URL
      if (!avatar) return ''
      // 如果已经是完整的URL（以http或https开头），直接返回
      if (avatar.startsWith('http')) {
        return avatar
      }
      // 如果是相对路径，且不是JSON数组格式
      if (avatar && avatar.length > 0 && avatar.indexOf('[') === -1) {
        // 使用后端返回的minio URL，后端已经返回完整URL
        // 如果后端返回的是相对路径，需要通过 /sys/common/static/ 接口访问
        if (!avatar.startsWith('/')) {
          // 相对路径，通过后端接口访问
          return '/sys/common/static/' + avatar
        }
        return avatar
      }
      return avatar
    },
    loadCollectionAccountCascaderValue() {
      // 根据收款账号ID反向查找级联路径
      if (!this.model.collectionAccountNumber) {
        return
      }
      
      // 先确保级联数据已加载
      if (this.collectionAccountOptions.length === 0) {
        this.loadCollectionAccountOptions().then(() => {
          this.findCascaderPath(this.model.collectionAccountNumber)
        })
      } else {
        this.findCascaderPath(this.model.collectionAccountNumber)
      }
    },
    findCascaderPath(accountId) {
      // 在级联数据中查找账号ID对应的路径
      for (const paymentMethodOption of this.collectionAccountOptions) {
        if (paymentMethodOption.children) {
          for (const payeePersonOption of paymentMethodOption.children) {
            if (payeePersonOption.children) {
              for (const accountOption of payeePersonOption.children) {
                // accountOption.value是账号ID，accountOption.id也是账号ID
                if (accountOption.value === accountId || accountOption.id === accountId) {
                  // 找到对应的路径
                  // 第一级：收款方式（paymentMethod）
                  // 第二级：收款单位/人（格式：paymentMethod__payeePerson）
                  // 第三级：账号ID
                  this.$set(this.model, 'collectionAccountCascader', [
                    paymentMethodOption.value,
                    payeePersonOption.value, // 这是格式化的value: paymentMethod__payeePerson
                    accountId
                  ])
                  return
                }
              }
            }
          }
        }
      }
    },
    loadAccountQrCode(accountId) {
      // 加载收款码图片
      if (!accountId) {
        this.qrCodeImage = ''
        return
      }
      
      getAction('/bank/ghBankManagement/queryById', { id: accountId }).then((res) => {
        if (res.success && res.result && res.result.pic) {
          this.qrCodeImage = this.getFileAccessHttpUrl(res.result.pic)
        } else {
          this.qrCodeImage = ''
        }
      }).catch(() => {
        this.qrCodeImage = ''
      })
    },
    getFormData() {
      const formData = { ...this.model };
      // 确保动态表单数据被包含（即使没有dynamicFormVersionId，如果有动态表单配置也要保存）
      if (this.dynamicFormConfig && this.$refs.dynamicForm) {
        const dynamicData = this.$refs.dynamicForm.getFormData();
        if (dynamicData && Object.keys(dynamicData).length > 0) {
          formData.dynamicFormData = dynamicData;
          formData.dynamicFormVersionId = this.dynamicFormVersionId;
          formData.formId = this.dynamicFormId; // 保存表单ID
        }
      } else if (this.dynamicFormVersionId && this.$refs.dynamicForm) {
        // 兼容旧逻辑：如果有版本ID但没有配置，也尝试获取数据
        formData.dynamicFormData = this.$refs.dynamicForm.getFormData();
        formData.dynamicFormVersionId = this.dynamicFormVersionId;
        formData.formId = this.dynamicFormId;
      }
      return formData;
    },
  },
}
</script>

