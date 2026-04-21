<template>
  <div>
    <j-modal
    title="线索跟进"
    :width="800"
    :visible="visible"
    :switchFullscreen="switchFullscreen"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <div v-if="opportunityRecord.id" class="followup-lead-summary">
      <div class="followup-lead-row">
        <span class="followup-lead-label">客户名称</span>
        <span class="followup-lead-value">{{ getCorporateNameDisplay() }}</span>
      </div>
      <div class="followup-lead-row">
        <span class="followup-lead-label">联系人员</span>
        <span class="followup-lead-value">{{ getContactsDisplay() }}</span>
      </div>
      <div class="followup-lead-row">
        <span class="followup-lead-label">联系方式</span>
        <span class="followup-lead-value">{{ getContactInformationDisplay() }}</span>
      </div>
      <div class="followup-lead-row">
        <span class="followup-lead-label">线索名称</span>
        <span class="followup-lead-value">{{ opportunityNameLabel }}</span>
      </div>
      <div class="followup-lead-row">
        <span class="followup-lead-label">备注信息</span>
        <span class="followup-lead-value followup-lead-remarks">{{ getRemarksDisplay() }}</span>
      </div>
      <div class="followup-lead-row" v-if="remarkImageList.length">
        <span class="followup-lead-label">备注图片</span>
        <span class="followup-lead-value">
          <a-space :size="8" wrap>
            <span
              v-for="(img, idx) in remarkImageList"
              :key="idx"
              class="followup-remark-thumb-wrap"
              @click="openImagePreview(img)"
            >
              <img :src="toFileUrl(img)" class="followup-remark-thumb" alt="备注图片" />
            </span>
          </a-space>
        </span>
      </div>
    </div>
    <a-tabs default-active-key="1" v-model="activeTab">
      <a-tab-pane tab="跟进记录" key="1">
        <a-list
          item-layout="horizontal"
          :data-source="followupList"
          style="max-height: 400px; overflow-y: auto;"
        >
          <a-list-item slot="renderItem" slot-scope="item">
            <a-list-item-meta>
              <template slot="title">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-weight: bold">{{ item.followupPerson }}</span>
                  <span style="color: #999; font-size: 12px;">{{ item.followupWay }} | {{ item.createDate ? moment(item.createDate).format('YYYY-MM-DD HH:mm:ss') : '-' }}</span>
                </div>
              </template>
              <template slot="description">
                <div style="margin-top: 8px;">{{ item.followupContent }}</div>
                <div v-if="item.followupNextTime" style="margin-top: 4px; color: #1890ff;">
                  <a-icon type="calendar" /> 下次跟进时间：{{ moment(item.followupNextTime).format('YYYY-MM-DD') }}
                </div>
              </template>
            </a-list-item-meta>
          </a-list-item>
          <a-empty v-if="followupList.length === 0" description="暂无跟进记录" style="padding: 40px 0" />
        </a-list>
      </a-tab-pane>
      <a-tab-pane tab="创建跟进" key="2">
        <a-form-model ref="followForm" :model="followForm" :rules="followFormRules">
          <a-form-model-item label="跟进内容" :labelCol="{span: 5}" :wrapperCol="{span: 19}" prop="followupContent">
            <a-textarea v-model="followForm.followupContent" rows="4" placeholder="请输入跟进内容" />
          </a-form-model-item>
          <a-form-model-item label="跟进方式" :labelCol="{span: 5}" :wrapperCol="{span: 19}" prop="followupWay">
            <a-radio-group v-model="followForm.followupWay">
              <a-radio-button value="微信">微信</a-radio-button>
              <a-radio-button value="电话">电话</a-radio-button>
              <a-radio-button value="拜访">拜访</a-radio-button>
              <a-radio-button value="其他">其他</a-radio-button>
            </a-radio-group>
          </a-form-model-item>
          <a-form-model-item label="下次跟进时间" :labelCol="{span: 5}" :wrapperCol="{span: 19}" prop="followupNextTime">
            <a-date-picker v-model="followForm.followupNextTime" placeholder="请选择下次跟进时间" style="width: 100%" valueFormat="YYYY-MM-DD" />
          </a-form-model-item>
        </a-form-model>
      </a-tab-pane>
    </a-tabs>
    <template slot="footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-popconfirm
        v-if="opportunityRecord && opportunityRecord.id && !isConfirmedValid"
        v-has="'opportunity:confirmValid'"
        title="确认该线索有效后将发放线索奖金（仅一次），确定继续吗？"
        @confirm="handleConfirmValid"
      >
        <a-button :loading="confirmValidLoading">确认有效</a-button>
      </a-popconfirm>
      <a-button type="primary" @click="handleOk" :loading="confirmLoading">确定</a-button>
    </template>
    </j-modal>
    <a-modal :visible="previewVisible" :footer="null" @cancel="closeImagePreview" centered width="60%">
      <img :src="previewImage" alt="预览图片" style="width: 100%;" />
    </a-modal>
  </div>
</template>

<script>
import { getAction, postAction, getFileAccessHttpUrl } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'FollowupModal',
  data() {
    return {
      visible: false,
      confirmLoading: false,
      confirmValidLoading: false,
      switchFullscreen: true,
      opportunityRecord: {},
      /** 线索名称展示（分类中文名，不展示线索编号） */
      opportunityNameLabel: '—',
      followupList: [],
      activeTab: '1', // 当前选中的tab：1-跟进记录，2-创建跟进
      followForm: {
        opportId: '',
        followupContent: '',
        followupWay: '',
        followupNextTime: '',
      },
      followFormRules: {
        followupContent: [{ required: true, message: '请输入跟进内容!', trigger: 'blur' }],
        followupWay: [{ required: true, message: '请选择跟进方式!', trigger: 'change' }],
        followupNextTime: [{ required: true, message: '请选择下次跟进时间!', trigger: 'change' }],
      },
      previewVisible: false,
      previewImage: '',
      moment: moment, // 将 moment 添加到 data 中，以便模板访问
    }
  },
  computed: {
    remarkImageList() {
      const v = this.opportunityRecord && this.opportunityRecord.remarkImages
      if (!v) return []
      try {
        if (Array.isArray(v)) return v.filter(it => it && String(it).trim())
        const s = String(v).trim()
        if (!s) return []
        if (s.startsWith('[')) {
          const arr = JSON.parse(s)
          return Array.isArray(arr) ? arr.filter(it => it && String(it).trim()) : []
        }
        if (s.includes(',')) return s.split(',').map(it => it.trim()).filter(Boolean)
        return [s]
      } catch (e) {
        return String(v).split(',').map(it => it.trim()).filter(Boolean)
      }
    },
    isConfirmedValid() {
      return !!(this.opportunityRecord && (this.opportunityRecord.confirmValid === 1 || this.opportunityRecord.confirmValid === '1'))
    },
  },
  methods: {
    getCorporateNameDisplay() {
      const v = this.opportunityRecord && this.opportunityRecord.corporateName
      if (v == null || String(v).trim() === '') return '—'
      return String(v).trim()
    },
    getContactsDisplay() {
      const v = this.opportunityRecord && this.opportunityRecord.contacts
      if (v == null || String(v).trim() === '') return '—'
      return String(v).trim()
    },
    getContactInformationDisplay() {
      const v = this.opportunityRecord && this.opportunityRecord.contactInformation
      if (v == null || String(v).trim() === '') return '—'
      return String(v).trim()
    },
    getRemarksDisplay() {
      const r = this.opportunityRecord && this.opportunityRecord.remarks
      if (r == null || String(r).trim() === '') return '—'
      return String(r).trim()
    },
    show(record) {
      this.visible = true
      this.opportunityRecord = record || {}
      this.followForm.opportId = record ? record.id : ''
      this.resolveOpportunityNameLabel()
      this.loadOpportunityDetail()
      this.loadFollowupList()
      this.resetFollowForm()
    },
    loadOpportunityDetail() {
      if (!this.opportunityRecord || !this.opportunityRecord.id) return
      getAction('/opportunity/ghOpportunity/queryById', { id: this.opportunityRecord.id }).then((res) => {
        if (res && res.success && res.result) {
          this.opportunityRecord = Object.assign({}, this.opportunityRecord, res.result)
          this.resolveOpportunityNameLabel()
        }
      })
    },
    resolveOpportunityNameLabel() {
      const rec = this.opportunityRecord
      if (!rec || !rec.id) {
        this.opportunityNameLabel = '—'
        return
      }
      if (rec.opportunityName_dictText) {
        this.opportunityNameLabel = rec.opportunityName_dictText
        return
      }
      const raw = rec.opportunityName
      if (raw == null || String(raw).trim() === '') {
        this.opportunityNameLabel = '—'
        return
      }
      const sid = String(raw).trim()
      if (/[\u4e00-\u9fa5]/.test(sid)) {
        this.opportunityNameLabel = sid
        return
      }
      if (!/^[a-zA-Z0-9_-]+$/.test(sid)) {
        this.opportunityNameLabel = '—'
        return
      }
      this.opportunityNameLabel = '加载中…'
      getAction('/sys/category/loadDictItem/', { ids: sid })
        .then((res) => {
          if (this.opportunityRecord.opportunityName !== raw) return
          if (res && res.success && Array.isArray(res.result) && res.result[0]) {
            this.opportunityNameLabel = res.result[0]
          } else {
            this.opportunityNameLabel = '—'
          }
        })
        .catch(() => {
          if (this.opportunityRecord.opportunityName === raw) {
            this.opportunityNameLabel = '—'
          }
        })
    },
    loadFollowupList() {
      if (!this.opportunityRecord.id) {
        this.followupList = []
        return
      }
      getAction('/followup/ghFollowupDetail/list', { opportId: this.opportunityRecord.id, pageNo: 1, pageSize: 1000 }).then((res) => {
        if (res.success) {
          this.followupList = res.result.records || res.result || []
        } else {
          this.$message.warning(res.message || '加载跟进记录失败')
          this.followupList = []
        }
      }).catch(() => {
        this.followupList = []
      })
    },
    resetFollowForm() {
      this.followForm = {
        opportId: this.opportunityRecord.id || '',
        followupContent: '',
        followupWay: '',
        followupNextTime: '',
      }
      this.$nextTick(() => {
        if (this.$refs.followForm) {
          this.$refs.followForm.resetFields()
        }
      })
    },
    handleOk() {
      // 防止重复提交
      if (this.confirmLoading) {
        return
      }
      
      this.$refs.followForm.validate((valid) => {
        if (valid) {
          this.confirmLoading = true
          postAction('/followup/ghFollowupDetail/add', this.followForm).then((res) => {
            if (res.success) {
              this.$message.success('跟进成功！')
              // 切换到"跟进记录"tab查看新记录
              this.activeTab = '1'
              // 重新加载跟进记录列表
              this.loadFollowupList()
              this.resetFollowForm()
              // 通知父组件更新列表
              this.$emit('ok')
            } else {
              this.$message.error(res.message || '跟进失败！')
            }
          }).catch((err) => {
            this.$message.error('跟进失败：' + (err.message || '未知错误'))
          }).finally(() => {
            // 确保无论成功还是失败，都要重置loading状态
            this.confirmLoading = false
          })
        }
      })
    },
    handleConfirmValid() {
      if (!this.opportunityRecord || !this.opportunityRecord.id) return
      if (this.confirmValidLoading) return
      this.confirmValidLoading = true
      postAction('/opportunity/ghOpportunity/confirmValid', { id: this.opportunityRecord.id })
        .then((res) => {
          if (res && res.success) {
            this.$message.success(res.message || '确认有效成功！')
            // 本地更新，避免用户要关窗再进
            this.$set(this.opportunityRecord, 'confirmValid', 1)
            this.$set(this.opportunityRecord, 'confirmValidTime', moment().format('YYYY-MM-DD HH:mm:ss'))
            this.$emit('ok')
          } else {
            this.$message.error((res && res.message) || '确认有效失败！')
          }
        })
        .catch((err) => {
          this.$message.error('确认有效失败：' + ((err && err.message) || '未知错误'))
        })
        .finally(() => {
          this.confirmValidLoading = false
        })
    },
    handleCancel() {
      this.visible = false
      this.opportunityRecord = {}
      this.opportunityNameLabel = '—'
      this.followupList = []
      this.resetFollowForm()
    },
    toFileUrl(path) {
      return getFileAccessHttpUrl(path)
    },
    openImagePreview(path) {
      this.previewImage = this.toFileUrl(path)
      this.previewVisible = true
    },
    closeImagePreview() {
      this.previewVisible = false
      this.previewImage = ''
    },
  },
}
</script>

<style scoped>
.followup-lead-summary {
  margin: -8px 0 16px;
  padding: 12px 14px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
}
.followup-lead-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.followup-lead-row + .followup-lead-row {
  margin-top: 8px;
}
.followup-lead-label {
  flex: 0 0 auto;
  color: rgba(0, 0, 0, 0.45);
  min-width: 64px;
}
.followup-lead-value {
  flex: 1;
  min-width: 0;
  color: rgba(0, 0, 0, 0.85);
  word-break: break-word;
}
.followup-lead-remarks {
  white-space: pre-wrap;
}
.followup-remark-thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  display: inline-block;
}
.followup-remark-thumb-wrap {
  cursor: pointer;
  line-height: 0;
}
</style>

