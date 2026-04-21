<template>
  <a-card :bordered="false">
    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
    </div>

    <!-- table区域-begin -->
    <a-table
      ref="table"
      size="middle"
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      @change="handleTableChange"
    >
      <span slot="action" slot-scope="text, record">
        <a @click="handleEdit(record)">编辑</a>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
          <a>删除</a>
        </a-popconfirm>
      </span>
    </a-table>

    <opportunity-bonus-config-modal ref="modalForm" @ok="modalFormOk"></opportunity-bonus-config-modal>
  </a-card>
</template>

<script>
import { getAction } from '@/api/manage'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import OpportunityBonusConfigModal from './modules/OpportunityBonusConfigModal'
export default {
  name: 'OpportunityBonusConfigList',
  mixins: [JeecgListMixin],
  components: {
    OpportunityBonusConfigModal,
  },
  data() {
    return {
      description: '线索奖金配置管理页面',
      // 表头
      columns: [
        {
          title: '线索名称',
          align: 'center',
          dataIndex: 'opportunityName',
          width: 260,
          customRender: (text, record) => {
            return record.opportunityName_dictText || text || '-'
          },
        },
        {
          title: '奖金金额',
          align: 'center',
          dataIndex: 'bonusMoney',
          width: 160,
          customRender: (text) => {
            if (text === null || text === undefined || text === '') return '-'
            const num = typeof text === 'number' ? text : parseFloat(text)
            if (isNaN(num)) return '-'
            return `¥${num.toFixed(2)}`
          },
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: 'center',
          width: 160,
          scopedSlots: { customRender: 'action' },
        },
      ],
      url: {
        list: '/opportunity/ghOpportunityBonusConfig/list',
        add: '/opportunity/ghOpportunityBonusConfig/add',
        edit: '/opportunity/ghOpportunityBonusConfig/edit',
        delete: '/opportunity/ghOpportunityBonusConfig/delete',
      },
    }
  },
  methods: {
    async loadData(arg) {
      if (arg === 1) {
        this.ipagination.current = 1
      }
      this.loading = true
      try {
        const params = this.getQueryParams()
        const res = await getAction(this.url.list, params)
        if (res && res.success) {
          const result = res.result || {}
          this.dataSource = result.records || result
          this.ipagination.total = result.total || 0
          await this.fillOpportunityNameLabels()
        } else {
          this.$message.warning(res.message || '加载数据失败')
        }
      } catch (e) {
        this.$message.error('加载数据失败：' + ((e && e.message) || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    /** 是否为 sys_category.id（数字 Snowflake / UUID 形字符串，排除已带中文的展示文本） */
    isLikelyCategoryId(v) {
      if (v == null || v === '') return false
      const s = String(v).trim()
      if (!s) return false
      if (/[\u4e00-\u9fa5]/.test(s)) return false
      return /^[a-zA-Z0-9_-]+$/.test(s)
    },
    async fillOpportunityNameLabels() {
      const list = this.dataSource || []
      const ids = list.map((r) => r && r.opportunityName).filter((v) => this.isLikelyCategoryId(v))
      const uniqueIds = Array.from(new Set(ids))
      if (uniqueIds.length === 0) return

      try {
        const res = await getAction('/sys/category/loadDictItem/', { ids: uniqueIds.join(',') })
        if (res && res.success && Array.isArray(res.result)) {
          const dict = {}
          uniqueIds.forEach((id, index) => {
            const label = res.result[index]
            if (label) dict[id] = label
          })
          list.forEach((r) => {
            const id = r && r.opportunityName
            if (id && dict[id]) {
              this.$set(r, 'opportunityName_dictText', dict[id])
            }
          })
        }
      } catch (e) {
        // 展示兜底为原始ID
        console.warn('机会名称映射失败', e)
      }
    },
  },
}
</script>

<style scoped>
.table-operator {
  margin-bottom: 16px;
}
</style>

