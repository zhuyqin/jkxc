<template>
  <j-modal
    title="同公司批量新增订单（效果预览）"
    :visible="visible"
    :width="1200"
    :switchFullscreen="true"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <div class="batch-preview-tip">排版优化：主表只看关键字段，类型详情在右侧抽屉填写。</div>

    <a-card size="small" :bordered="false" title="公共信息（整批共享）" style="margin-bottom: 12px">
      <a-form-model :model="commonInfo" layout="inline">
        <a-form-model-item label="公司名称"><a-input v-model="commonInfo.companyName" style="width: 240px" /></a-form-model-item>
        <a-form-model-item label="联系人员"><a-input v-model="commonInfo.contacts" style="width: 180px" /></a-form-model-item>
        <a-form-model-item label="联系方式"><a-input v-model="commonInfo.contactPhone" style="width: 180px" /></a-form-model-item>
        <a-form-model-item label="业务员"><a-input v-model="commonInfo.salesman" style="width: 140px" /></a-form-model-item>
      </a-form-model>
    </a-card>

    <a-card size="small" :bordered="false" title="子订单明细">
      <div style="margin-bottom: 8px; display: flex; gap: 8px; flex-wrap: wrap">
        <a-button type="dashed" icon="plus" @click="addRow">新增一行子订单</a-button>
        <a-button icon="copy" :disabled="!editingRow" @click="applyToSameType">应用当前配置到同类型行</a-button>
      </div>
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="key" size="small" bordered>
        <template slot="businessType" slot-scope="text, record">
          <a-select v-model="record.businessType" style="width: 150px" @change="onBusinessTypeChange(record)">
            <a-select-option value="company_register">工商注册</a-select-option>
            <a-select-option value="agency_bookkeeping">代理记账</a-select-option>
            <a-select-option value="ip_service">知识产权</a-select-option>
          </a-select>
        </template>
        <template slot="summary" slot-scope="text, record">
          <span :class="record.summary ? 'summary-ok' : 'summary-empty'">{{ record.summary || '尚未填写类型字段' }}</span>
        </template>
        <template slot="amount" slot-scope="text, record">
          <a-input-number v-model="record.amount" :min="0" :precision="2" style="width: 120px" />
        </template>
        <template slot="action" slot-scope="text, record, index">
          <a-space>
            <a @click="openEditor(record)">编辑字段</a>
            <a :disabled="index===0" @click="copyPrevRow(index)">复制上一行</a>
            <a-popconfirm title="确定删除这一行吗？" @confirm="removeRow(index)">
              <a>删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      title="编辑子订单字段"
      placement="right"
      :visible="editorVisible"
      :width="620"
      @close="closeEditor"
      destroyOnClose
    >
      <a-alert
        message="按业务类型填写对应字段"
        description="字段很多时，建议使用这种抽屉模式，不在表格里平铺。"
        type="info"
        show-icon
        style="margin-bottom: 12px"
      />

      <a-form-model v-if="editingRow" layout="vertical">
        <a-form-model-item label="业务类型">
          <a-select v-model="editingRow.businessType" @change="onBusinessTypeChange(editingRow)">
            <a-select-option value="company_register">工商注册</a-select-option>
            <a-select-option value="agency_bookkeeping">代理记账</a-select-option>
            <a-select-option value="ip_service">知识产权</a-select-option>
          </a-select>
        </a-form-model-item>

        <div v-if="editingRow.businessType === 'company_register'">
          <div class="group-title">基础信息</div>
          <a-row :gutter="12">
            <a-col :span="12"><a-form-model-item label="注册地区"><a-input v-model="editingRow.dynamic.region" /></a-form-model-item></a-col>
            <a-col :span="12"><a-form-model-item label="公司类型"><a-input v-model="editingRow.dynamic.companyType" /></a-form-model-item></a-col>
            <a-col :span="12"><a-form-model-item label="注册资本(万)"><a-input-number v-model="editingRow.dynamic.registeredCapital" style="width: 100%" :min="0" /></a-form-model-item></a-col>
            <a-col :span="12"><a-form-model-item label="预计办理时长(天)"><a-input-number v-model="editingRow.dynamic.processingDays" style="width: 100%" :min="1" /></a-form-model-item></a-col>
          </a-row>
        </div>

        <div v-else-if="editingRow.businessType === 'agency_bookkeeping'">
          <div class="group-title">记账参数</div>
          <a-row :gutter="12">
            <a-col :span="12">
              <a-form-model-item label="纳税人类型">
                <a-select v-model="editingRow.dynamic.taxpayerType">
                  <a-select-option value="small">小规模</a-select-option>
                  <a-select-option value="general">一般纳税人</a-select-option>
                </a-select>
              </a-form-model-item>
            </a-col>
            <a-col :span="12"><a-form-model-item label="票量/月"><a-input-number v-model="editingRow.dynamic.ticketVolume" style="width: 100%" :min="0" /></a-form-model-item></a-col>
            <a-col :span="12">
              <a-form-model-item label="服务周期">
                <a-select v-model="editingRow.dynamic.cycle">
                  <a-select-option value="monthly">月度</a-select-option>
                  <a-select-option value="yearly">年度</a-select-option>
                </a-select>
              </a-form-model-item>
            </a-col>
            <a-col :span="12"><a-form-model-item label="起始服务月"><a-input v-model="editingRow.dynamic.startMonth" placeholder="如 2026-05" /></a-form-model-item></a-col>
          </a-row>
        </div>

        <div v-else-if="editingRow.businessType === 'ip_service'">
          <div class="group-title">知识产权参数</div>
          <a-row :gutter="12">
            <a-col :span="12"><a-form-model-item label="类型"><a-input v-model="editingRow.dynamic.ipType" placeholder="商标/专利" /></a-form-model-item></a-col>
            <a-col :span="12"><a-form-model-item label="申请阶段">
              <a-select v-model="editingRow.dynamic.stage">
                <a-select-option value="apply">申请中</a-select-option>
                <a-select-option value="review">审查中</a-select-option>
                <a-select-option value="done">已下证</a-select-option>
              </a-select>
            </a-form-model-item></a-col>
            <a-col :span="24"><a-form-model-item label="标的名称"><a-input v-model="editingRow.dynamic.subjectName" /></a-form-model-item></a-col>
          </a-row>
        </div>
      </a-form-model>

      <template slot="footer">
        <div class="drawer-footer">
          <a-button @click="closeEditor">取消</a-button>
          <a-button type="primary" @click="saveEditor">保存</a-button>
        </div>
      </template>
    </a-drawer>

    <template slot="footer">
      <a-button @click="handleCancel">关闭</a-button>
      <a-button type="primary" @click="mockSubmit">模拟提交</a-button>
    </template>
  </j-modal>
</template>

<script>
export default {
  name: 'BatchOrderPreviewModal',
  data() {
    return {
      visible: false,
      seq: 1,
      commonInfo: {
        companyName: '杭州示例科技有限公司',
        contacts: '张三',
        contactPhone: '13800000000',
        salesman: '王五',
      },
      rows: [],
      editorVisible: false,
      editingRow: null,
      columns: [
        { title: '序号', dataIndex: 'seq', width: 70, align: 'center' },
        { title: '业务类型', dataIndex: 'businessType', scopedSlots: { customRender: 'businessType' }, width: 170 },
        { title: '类型字段摘要', dataIndex: 'summary', scopedSlots: { customRender: 'summary' } },
        { title: '订单金额', dataIndex: 'amount', scopedSlots: { customRender: 'amount' }, width: 140, align: 'right' },
        { title: '操作', dataIndex: 'action', scopedSlots: { customRender: 'action' }, width: 150, align: 'center' },
      ],
    }
  },
  methods: {
    open() {
      this.visible = true
      if (!this.rows.length) {
        this.addRow()
      }
    },
    handleCancel() {
      this.visible = false
    },
    newRow() {
      return {
        key: `row_${Date.now()}_${this.seq}`,
        seq: this.seq++,
        businessType: '',
        amount: null,
        dynamic: {},
        summary: '',
      }
    },
    addRow() {
      const row = this.newRow()
      this.rows.push(row)
      // 新增后直接编辑，少一次点击
      this.openEditor(row)
    },
    removeRow(index) {
      this.rows.splice(index, 1)
      this.rows.forEach((r, i) => {
        r.seq = i + 1
      })
      this.seq = this.rows.length + 1
    },
    onBusinessTypeChange(record) {
      record.dynamic = {}
      record.summary = ''
    },
    openEditor(record) {
      this.editingRow = record
      this.editorVisible = true
    },
    closeEditor() {
      this.editorVisible = false
      this.editingRow = null
    },
    saveEditor() {
      if (!this.editingRow) return
      this.editingRow.summary = this.buildSummary(this.editingRow)
      this.closeEditor()
    },
    copyPrevRow(index) {
      if (index <= 0) return
      const prev = this.rows[index - 1]
      const cur = this.rows[index]
      if (!prev || !cur) return
      cur.businessType = prev.businessType
      cur.amount = prev.amount
      cur.dynamic = JSON.parse(JSON.stringify(prev.dynamic || {}))
      cur.summary = prev.summary || this.buildSummary(cur)
      this.$message.success(`已复制第${index}行配置到第${index + 1}行`)
    },
    applyToSameType() {
      if (!this.editingRow || !this.editingRow.businessType) {
        this.$message.warning('请先编辑并选择业务类型')
        return
      }
      const type = this.editingRow.businessType
      const templateDynamic = JSON.parse(JSON.stringify(this.editingRow.dynamic || {}))
      let count = 0
      this.rows.forEach((r) => {
        if (r.key === this.editingRow.key) return
        if (r.businessType === type) {
          r.dynamic = JSON.parse(JSON.stringify(templateDynamic))
          r.summary = this.buildSummary(r)
          count++
        }
      })
      this.$message.success(`已应用到 ${count} 条同类型子订单`)
    },
    buildSummary(row) {
      const d = row.dynamic || {}
      if (row.businessType === 'company_register') {
        return [d.region, d.companyType, d.registeredCapital ? `${d.registeredCapital}万` : ''].filter(Boolean).join(' | ')
      }
      if (row.businessType === 'agency_bookkeeping') {
        return [d.taxpayerType, d.ticketVolume ? `票量${d.ticketVolume}/月` : '', d.cycle].filter(Boolean).join(' | ')
      }
      if (row.businessType === 'ip_service') {
        return [d.ipType, d.subjectName, d.stage].filter(Boolean).join(' | ')
      }
      return ''
    },
    mockSubmit() {
      const invalid = this.rows.find((r) => !r.businessType || !r.amount)
      if (invalid) {
        this.$message.warning('请先补全业务类型和订单金额（演示校验）')
        return
      }
      this.$message.success(`演示通过：公司 ${this.commonInfo.companyName}，共 ${this.rows.length} 条子订单`)
    },
  },
}
</script>

<style scoped>
.batch-preview-tip {
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  color: #389e0d;
}
.summary-empty {
  color: #999;
}
.summary-ok {
  color: #262626;
}
.group-title {
  margin: 6px 0 8px;
  padding-left: 8px;
  border-left: 3px solid #1890ff;
  font-weight: 500;
}
.drawer-footer {
  text-align: right;
}
</style>
