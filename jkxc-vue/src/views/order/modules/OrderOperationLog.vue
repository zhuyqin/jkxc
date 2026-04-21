<template>
  <div class="operation-log-container">
    <a-spin :spinning="loading">
      <a-table
        :columns="columns"
        :dataSource="logList"
        :pagination="false"
        :loading="loading"
        rowKey="id"
        :rowClassName="getRowClassName"
      >
        <template slot="operationType" slot-scope="text">
          <a-tag :color="getOperationTypeColor(text)" style="margin: 0; padding: 2px 8px; border-radius: 4px;">
            {{ getOperationTypeText(text) }}
          </a-tag>
        </template>
        <template slot="operationDesc" slot-scope="text, record">
          <div class="operation-desc">
            <div class="desc-title">{{ text }}</div>
            <!-- 显示审核结果 -->
            <div v-if="(record.operationType === 'audit' || record.operationType === 'approve') && getAuditResult(record.remarks)" class="audit-result">
              <a-icon type="check-circle" style="margin-right: 4px; color: #52c41a;" v-if="getAuditResult(record.remarks) === '通过'" />
              <a-icon type="close-circle" style="margin-right: 4px; color: #ff4d4f;" v-else-if="getAuditResult(record.remarks) === '不通过'" />
              <a-icon type="rollback" style="margin-right: 4px; color: #faad14;" v-else />
              <span class="result-text">{{ getAuditResult(record.remarks) }}</span>
            </div>
            <!-- 显示退单/驳回原因 -->
            <div v-if="(record.operationType === 'refund' || record.operationType === 'reject') && getReasonFromRemarks(record.remarks)" class="reason-box">
              <a-icon :type="record.operationType === 'refund' ? 'rollback' : 'close-circle'" style="margin-right: 4px; color: #ff4d4f;" />
              <span class="reason-label">{{ record.operationType === 'refund' ? '退单原因' : '驳回原因' }}：</span>
              <span class="reason-text">{{ getReasonFromRemarks(record.remarks) }}</span>
            </div>
            <!-- 显示变更字段 -->
            <div v-if="record.changedFields" class="changed-fields">
              <a-icon type="edit" style="margin-right: 4px;" />
              <span>变更字段：</span>
              <a-tag v-for="(field, index) in getChangedFieldsList(record.changedFields)" 
                     :key="index" 
                     color="orange" 
                     style="margin: 2px 4px 2px 0; padding: 0 6px;">
                {{ field }}
              </a-tag>
            </div>
            <!-- 显示变更详情（只有在有实际变更内容时才显示） -->
            <div v-if="hasChangeDetails(record.remarks) && parseChangeDetails(record.remarks).length > 0" class="change-details">
              <a-icon type="info-circle" style="margin-right: 4px; color: #1890ff;" />
              <div class="change-item" v-for="(change, index) in parseChangeDetails(record.remarks)" :key="index">
                <span class="change-label">{{ change.label }}：</span>
                <template v-if="change.isUpdate">
                  <span class="change-update">{{ change.new }}</span>
                </template>
                <template v-else>
                  <span class="change-old">{{ change.old }}</span>
                  <a-icon type="arrow-right" style="margin: 0 8px; color: #999;" />
                  <span class="change-new">{{ change.new }}</span>
                </template>
              </div>
            </div>
          </div>
        </template>
        <template slot="operatorName" slot-scope="text">
          <a-icon type="user" style="margin-right: 4px; color: #999;" />
          <span>{{ text || '系统' }}</span>
        </template>
        <template slot="remarks" slot-scope="text, record">
          <div class="remarks-text">
            <template v-if="hasChangeDetails(text)">
              <!-- 如果有变更详情，提取备注部分显示 -->
              <div v-if="getRemarksText(text)">{{ getRemarksText(text) }}</div>
              <div v-else>—</div>
            </template>
            <template v-else>
              {{ text || '—' }}
            </template>
          </div>
        </template>
      </a-table>
      <a-empty v-if="!loading && (!logList || logList.length === 0)" description="暂无操作记录" style="padding: 40px 0" />
    </a-spin>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'

export default {
  name: 'OrderOperationLog',
  props: {
    orderId: {
      type: String,
      default: '',
      required: true
    }
  },
  data() {
    return {
      loading: false,
      logList: [],
      columns: [
        {
          title: '操作时间',
          dataIndex: 'createTime',
          key: 'createTime',
          width: 160,
          align: 'center',
        },
        {
          title: '操作类型',
          dataIndex: 'operationType',
          key: 'operationType',
          width: 90,
          align: 'center',
          scopedSlots: { customRender: 'operationType' },
        },
        {
          title: '操作描述',
          dataIndex: 'operationDesc',
          key: 'operationDesc',
          scopedSlots: { customRender: 'operationDesc' },
        },
        {
          title: '操作人',
          dataIndex: 'operatorName',
          key: 'operatorName',
          width: 100,
          align: 'center',
          scopedSlots: { customRender: 'operatorName' },
        },
        {
          title: '备注',
          dataIndex: 'remarks',
          key: 'remarks',
          width: 180,
          scopedSlots: { customRender: 'remarks' },
        },
      ],
    }
  },
  created() {
    this.init()
  },
  watch: {
    orderId: {
      handler() {
        this.init()
      },
      immediate: true
    }
  },
  methods: {
    init() {
      if (!this.orderId) {
        return
      }
      this.loading = true
      getAction('/order/getOperationLogs', { orderId: this.orderId })
        .then((res) => {
          if (res.success) {
            this.logList = res.result || []
          }
        })
        .catch(err => {
          console.error('加载操作记录失败:', err)
          this.$message.error('加载操作记录失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    getOperationTypeText(type) {
      const typeMap = {
        'create': '创建',
        'update': '修改',
        'approve': '审批',
        'audit': '审核',
        'reject': '驳回',
        'refund': '退单',
        'confirm': '确认',
        'delete': '删除',
        'submit': '提交',
        'cancel': '取消',
        '收款': '收款',
      }
      return typeMap[type] || type
    },
    getOperationTypeColor(type) {
      const colorMap = {
        'create': 'green',
        'update': 'blue',
        'approve': 'cyan',
        'audit': 'cyan',
        'reject': 'red',
        'refund': 'orange',
        'confirm': 'green',
        'delete': 'red',
        'submit': 'orange',
        'cancel': 'default',
        '收款': 'purple',
      }
      return colorMap[type] || 'default'
    },
    getRowClassName(record, index) {
      return index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
    },
    getChangedFieldsList(changedFields) {
      if (!changedFields) return []
      return changedFields.split('、').filter(field => field.trim())
    },
    hasChangeDetails(remarks) {
      if (!remarks || remarks === '订单修改' || remarks === '订单创建') return false
      // 检查是否包含变更标记（→）且不是简单的状态描述
      if (remarks.includes('→')) {
        // 确保不是简单的状态变更描述（如"订单状态：进行中 → 退单"）
        // 这种应该通过退单原因显示，不需要显示变更详情
        if (remarks.includes('订单状态：') && (remarks.includes('退单原因：') || remarks.includes('驳回原因：'))) {
          return false
        }
        return true
      }
      // 如果包含冒号但不包含箭头，可能是其他格式的备注，不显示变更详情
      return false
    },
    parseChangeDetails(remarks) {
      if (!this.hasChangeDetails(remarks)) return []
      
      const changes = []
      // 按分号分割多个变更项
      const items = remarks.split('；').filter(item => item.trim() && item.includes('→'))
      
      items.forEach(item => {
        // 先检查是否是动态表单字段（格式：动态表单.字段名：旧值 → 新值）
        if (item.startsWith('动态表单.')) {
          const fieldMatch = item.match(/^动态表单\.(.+)：(.+?)\s*→\s*(.+)$/)
          if (fieldMatch) {
            changes.push({
              label: '动态表单.' + fieldMatch[1].trim(),
              old: fieldMatch[2].trim(),
              new: fieldMatch[3].trim()
            })
            return // 已处理，跳过后续匹配
          }
        }
        
        // 匹配格式：字段名：旧值 → 新值
        const match = item.match(/^(.+)：(.+?)\s*→\s*(.+)$/)
        if (match) {
          const label = match[1].trim()
          // 如果已经是动态表单字段，跳过（上面已处理）
          if (!label.startsWith('动态表单')) {
            changes.push({
              label: label,
              old: match[2].trim(),
              new: match[3].trim()
            })
          }
        } else if (item.includes('动态表单')) {
          // 处理动态表单变更（非字段级别的）
          if (item.includes('无 → 已填写')) {
            changes.push({
              label: '动态表单',
              old: '无',
              new: '已填写'
            })
          } else if (item.includes('已填写 → 已清空')) {
            changes.push({
              label: '动态表单',
              old: '已填写',
              new: '已清空'
            })
          } else if (item.includes('内容已更新')) {
            changes.push({
              label: '动态表单',
              old: '—',
              new: '内容已更新',
              isUpdate: true // 标记为更新类型，不显示箭头
            })
          } else {
            // 兼容旧格式
            changes.push({
              label: '动态表单',
              old: '—',
              new: '已修改'
            })
          }
        }
      })
      
      return changes
    },
    getAuditResult(remarks) {
      if (!remarks) return null
      // 从备注中提取审核结果：通过、不通过、驳回
      if (remarks.includes('结果：通过')) {
        return '通过'
      } else if (remarks.includes('结果：不通过')) {
        return '不通过'
      } else if (remarks.includes('结果：驳回')) {
        return '驳回'
      }
      return null
    },
    getRemarksText(remarks) {
      if (!remarks) return ''
      // 从备注中提取备注部分（排除变更详情）
      if (remarks.includes('备注：')) {
        const parts = remarks.split('备注：')
        if (parts.length > 1) {
          // 提取备注部分，排除变更详情
          let remarkText = parts[1]
          // 移除变更详情部分（包含 → 的部分）
          const changeIndex = remarkText.indexOf('；')
          if (changeIndex > 0) {
            remarkText = remarkText.substring(0, changeIndex)
          }
          return remarkText.trim()
        }
      }
      // 如果有驳回原因，也提取出来
      if (remarks.includes('驳回原因：')) {
        const parts = remarks.split('驳回原因：')
        if (parts.length > 1) {
          let rejectReason = parts[1]
          const changeIndex = rejectReason.indexOf('；')
          if (changeIndex > 0) {
            rejectReason = rejectReason.substring(0, changeIndex)
          }
          return rejectReason.trim()
        }
      }
      // 如果有退单原因，也提取出来
      if (remarks.includes('退单原因：')) {
        const parts = remarks.split('退单原因：')
        if (parts.length > 1) {
          let refundReason = parts[1]
          const changeIndex = refundReason.indexOf('；')
          if (changeIndex > 0) {
            refundReason = refundReason.substring(0, changeIndex)
          }
          return refundReason.trim()
        }
      }
      return ''
    },
    getReasonFromRemarks(remarks) {
      if (!remarks) return ''
      // 提取退单原因或驳回原因
      if (remarks.includes('退单原因：')) {
        const parts = remarks.split('退单原因：')
        if (parts.length > 1) {
          let reason = parts[1]
          // 移除变更详情部分（包含 → 的部分）
          const changeIndex = reason.indexOf('；')
          if (changeIndex > 0) {
            reason = reason.substring(0, changeIndex)
          }
          return reason.trim()
        }
      }
      if (remarks.includes('驳回原因：')) {
        const parts = remarks.split('驳回原因：')
        if (parts.length > 1) {
          let reason = parts[1]
          const changeIndex = reason.indexOf('；')
          if (changeIndex > 0) {
            reason = reason.substring(0, changeIndex)
          }
          return reason.trim()
        }
      }
      return ''
    },
  },
}
</script>

<style scoped>
.operation-log-container {
  padding: 0;
}

.operation-log-container >>> .ant-table {
  font-size: 14px;
}

.operation-log-container >>> .ant-table-thead > tr > th {
  background: #fafafa;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
  border-bottom: 2px solid #e8e8e8;
}

.operation-log-container >>> .table-row-even {
  background: #ffffff;
}

.operation-log-container >>> .table-row-odd {
  background: #fafafa;
}

.operation-log-container >>> .ant-table-tbody > tr:hover > td {
  background: #e6f7ff !important;
}

.operation-desc {
  padding: 2px 0;
}

.desc-title {
  font-weight: 600;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  margin-bottom: 6px;
  line-height: 1.4;
}

.changed-fields {
  margin: 6px 0;
  padding: 6px 10px;
  background: #fff7e6;
  border-left: 3px solid #ffa940;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  line-height: 1.4;
}

.changed-fields span {
  margin-right: 4px;
}

.change-details {
  margin-top: 6px;
  padding: 8px 10px;
  background: #f0f9ff;
  border-left: 3px solid #1890ff;
  border-radius: 4px;
  font-size: 12px;
}

.change-item {
  margin: 4px 0;
  display: flex;
  align-items: center;
  line-height: 1.5;
}

.change-item:first-child {
  margin-top: 0;
}

.change-item:last-child {
  margin-bottom: 0;
}

.change-label {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  min-width: 80px;
  display: inline-block;
}

.change-old {
  color: #ff4d4f;
  background: #fff1f0;
  padding: 2px 8px;
  border-radius: 3px;
  font-weight: 500;
}

.change-new {
  color: #52c41a;
  background: #f6ffed;
  padding: 2px 8px;
  border-radius: 3px;
  font-weight: 500;
}

.change-update {
  color: #1890ff;
  background: #f0f9ff;
  padding: 2px 8px;
  border-radius: 3px;
  font-weight: 500;
}

.remarks-text {
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
  line-height: 1.5;
}

.audit-result {
  margin-top: 6px;
  padding: 4px 8px;
  background: #f6ffed;
  border-left: 3px solid #52c41a;
  border-radius: 4px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
}

.audit-result .result-text {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.reason-box {
  margin-top: 6px;
  padding: 6px 10px;
  background: #fff1f0;
  border-left: 3px solid #ff4d4f;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  line-height: 1.4;
}

.reason-label {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  margin-right: 4px;
}

.reason-text {
  color: rgba(0, 0, 0, 0.65);
  flex: 1;
  word-break: break-word;
}

.operation-log-container >>> .ant-empty {
  margin: 40px 0;
}

.operation-log-container >>> .ant-table-tbody > tr > td {
  padding: 10px 12px;
  vertical-align: top;
}

.operation-log-container >>> .ant-table-thead > tr > th {
  padding: 10px 12px;
}
</style>

