<template>
  <a-card :bordered="false">
    <a-row :gutter="16">
      <!-- 左侧跟进时间距离天数菜单 -->
      <a-col :span="4" style="background: #fff; padding-right: 16px;">
        <div style="background: #fff; border: 1px solid #e8e8e8; border-radius: 4px; padding: 8px 0;">
          <div style="padding: 8px 16px; font-weight: 600; color: rgba(0, 0, 0, 0.85); border-bottom: 1px solid #f0f0f0; margin-bottom: 8px;">
            跟进时间
          </div>
          <a-menu
            mode="inline"
            :selectedKeys="[selectedKey]"
            @click="handleKeyMenuClick"
            style="border: none; background: #fff;"
          >
            <a-menu-item key="">
              <a-icon type="bars" />
              <span>全部 <span v-if="followupTimeCounts.total !== undefined" style="color: #999; margin-left: 4px;">({{ followupTimeCounts.total }})</span></span>
            </a-menu-item>
            <a-menu-item key="1">
              <a-icon type="clock-circle" />
              <span>3天内 <span v-if="followupTimeCounts.key1 !== undefined" style="color: #999; margin-left: 4px;">({{ followupTimeCounts.key1 }})</span></span>
            </a-menu-item>
            <a-menu-item key="2">
              <a-icon type="calendar" />
              <span>4-7天前 <span v-if="followupTimeCounts.key2 !== undefined" style="color: #999; margin-left: 4px;">({{ followupTimeCounts.key2 }})</span></span>
            </a-menu-item>
            <a-menu-item key="3">
              <a-icon type="calendar" />
              <span>8-15天前 <span v-if="followupTimeCounts.key3 !== undefined" style="color: #999; margin-left: 4px;">({{ followupTimeCounts.key3 }})</span></span>
            </a-menu-item>
            <a-menu-item key="4">
              <a-icon type="calendar" />
              <span>15天前 <span v-if="followupTimeCounts.key4 !== undefined" style="color: #999; margin-left: 4px;">({{ followupTimeCounts.key4 }})</span></span>
            </a-menu-item>
          </a-menu>
        </div>
      </a-col>
      <!-- 右侧内容区域 -->
      <a-col :span="20">
        <!-- 查询区域 -->
        <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="线索编号">
              <a-input placeholder="请输入线索编号" v-model="queryParam.opportunityNo" allowClear></a-input>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="客户名称">
              <JInput placeholder="请输入客户名称" v-model="queryParam.corporateName" type="like" allowClear></JInput>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="业务人员">
              <j-search-select-tag
                placeholder="请选择业务人员"
                v-model="queryParam.founder"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="客户来源">
              <j-dict-select-tag
                placeholder="请选择客户来源"
                v-model="queryParam.opportunitySource"
                dictCode="opportunity_source"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="当前状态">
              <j-dict-select-tag
                placeholder="请选择当前状态"
                v-model="queryParam.state"
                dictCode="opportunity_status"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator" style="margin-top: 16px;">
      <a-button @click="handleAdd" type="primary" icon="plus">新增线索</a-button>
      <a-button 
        v-if="currentTab === 'signed' && selectedRowKeys.length > 0" 
        @click="handleBatchConvertToCustomer" 
        type="primary" 
        icon="user"
        style="margin-left: 8px"
      >
        批量转客户池({{ selectedRowKeys.length }})
      </a-button>
      <a-button 
        v-if="currentTab === 'signed'" 
        v-has="'opportunity:convertAllSignedToCustomer'"
        @click="handleConvertAllSignedToCustomer" 
        type="primary" 
        icon="user-add"
        style="margin-left: 8px"
      >
        全部已签约线索转客户池
      </a-button>
      <a-button v-has="'opportunity:export'" type="primary" icon="download" @click="handleExportXls('线索管理')">导出</a-button>
      <a-upload v-has="'opportunity:import'" name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
    </div>

    <!-- Tab页区域 -->
    <a-tabs default-active-key="public" @change="handleTabChange" style="margin-top: 16px;">
      <a-tab-pane tab="公海线索" key="public"></a-tab-pane>
      <a-tab-pane tab="个人线索" key="personal"></a-tab-pane>
      <a-tab-pane tab="签约线索" key="signed"></a-tab-pane>
      <a-tab-pane tab="无效线索" key="invalid"></a-tab-pane>
    </a-tabs>

    <!-- 表格区域 -->
    <a-table
      ref="table"
      size="middle"
      :scroll="{ x: true }"
      bordered
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      :rowSelection="rowSelection"
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="state" slot-scope="text, record">
        <a-tag :color="getStateColor(record.state)" :style="getStateStyle(record.state, record.state_dictText)">
          {{ record.state_dictText || text || '-' }}
        </a-tag>
      </span>
      <span slot="confirmValid" slot-scope="text, record">
        <a-tag :color="record.confirmValid === 1 || record.confirmValid === '1' ? 'green' : 'default'">
          {{ record.confirmValid === 1 || record.confirmValid === '1' ? '有效' : '无效' }}
        </a-tag>
      </span>
      <span slot="syDay" slot-scope="text, record">
        <a-tag :color="getSyDayColor(record.syDay)" :style="getSyDayStyle(record.syDay)">
          {{ record.syDay !== null && record.syDay !== undefined ? record.syDay + '天' : '-' }}
        </a-tag>
      </span>
      <span slot="followupDays" slot-scope="text, record">
        <a-tag :color="getFollowupDaysColor(record.followupDays)" class="followup-days-tag" :style="getFollowupDaysStyle(record.followupDays)">
          {{ record.followupDays !== null && record.followupDays !== undefined ? record.followupDays + '天' : '-' }}
        </a-tag>
      </span>
      <span slot="followupRecord" slot-scope="text, record" class="followup-record-cell">
        <a-popover
          trigger="hover"
          placement="leftTop"
          :mouseEnterDelay="0.25"
          :mouseLeaveDelay="0.08"
          overlayClassName="followup-record-popover-wrap"
          @visibleChange="(v) => onFollowupPopoverVisible(v, record)"
        >
          <template slot="title">
            <span>跟进记录</span>
            <span v-if="record.corporateName" class="followup-popover-title-sub">{{ record.corporateName }}</span>
          </template>
          <div slot="content" class="followup-popover-content">
            <a-spin :spinning="followupPopoverLoading && followupPopoverOpportId === record.id">
              <template v-if="followupPopoverOpportId === record.id && !followupPopoverLoading">
                <a-list
                  v-if="followupPopoverList.length > 0"
                  item-layout="horizontal"
                  :data-source="followupPopoverList"
                  size="small"
                  class="followup-popover-list"
                >
                  <a-list-item slot="renderItem" slot-scope="item">
                    <a-list-item-meta>
                      <template slot="title">
                        <div class="followup-popover-item-head">
                          <span class="followup-popover-person">{{ item.followupPerson }}</span>
                          <span class="followup-popover-meta">{{ item.followupWay }} | {{ formatFollowupDateTime(item.createDate) }}</span>
                        </div>
                      </template>
                      <template slot="description">
                        <div class="followup-popover-desc">{{ item.followupContent }}</div>
                        <div v-if="item.followupNextTime" class="followup-popover-next">
                          <a-icon type="calendar" />
                          下次跟进：{{ formatFollowupDate(item.followupNextTime) }}
                        </div>
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </a-list>
                <a-empty v-else description="暂无跟进记录" class="followup-popover-empty" />
              </template>
            </a-spin>
          </div>
          <a-button type="link" icon="solution" size="small" @click.stop="handleFollowupRecord(record)">查看跟进</a-button>
        </a-popover>
      </span>
      <span slot="action" slot-scope="text, record">
        <a-dropdown>
          <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
            <a-menu-item key="followup">
              <a-icon type="customer-service" />线索跟进
            </a-menu-item>
            <a-menu-item v-if="currentTab === 'public'" key="assign">
              <a-icon type="user-add" />线索分配
            </a-menu-item>
            <a-menu-item v-if="currentTab === 'personal'" key="moveToPublic">
              <a-icon type="swap" />移入公海
            </a-menu-item>
            <a-menu-item v-if="!record.customerId" key="convertToCustomer">
              <a-icon type="user" />转客户池
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item key="edit">
              <a-icon type="edit" />线索编辑
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item v-has="'opportunity:delete'" key="delete">
              <a-icon type="delete" />线索删除
            </a-menu-item>
          </a-menu>
          <a-button>
            操作 <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </span>
    </a-table>

    <!-- 线索表单弹窗 -->
    <opportunity-modal ref="modalForm" @ok="modalFormOk"></opportunity-modal>
    
    <!-- 跟进记录弹窗 -->
    <followup-modal ref="followupModal" @ok="handleFollowupOk"></followup-modal>
    
    <!-- 线索分配弹窗 -->
    <assign-modal ref="assignModal" @ok="handleAssignOk"></assign-modal>
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { filterObj, processQueryParams } from '@/utils/util'
import OpportunityModal from './modules/OpportunityModal'
import FollowupModal from './modules/FollowupModal'
import AssignModal from './modules/AssignModal'
import { queryOpportunityList, deleteOpportunity } from '@/api/api'
import { getAction, getActionTimeout, postAction } from '@/api/manage'
import moment from 'moment'
import Vue from 'vue'
import { ACCESS_TOKEN, TENANT_ID } from '@/store/mutation-types'

export default {
  name: 'OpportunityList',
  mixins: [JeecgListMixin],
  components: {
    OpportunityModal,
    FollowupModal,
    AssignModal,
  },
  data() {
    return {
      description: '线索管理页面',
      currentTab: 'public', // 当前选中的tab页：public-公海线索，personal-个人线索，signed-签约线索，invalid-无效线索
      selectedKey: '', // 当前选中的跟进时间距离天数菜单项
      // 线索名称（opportunityName）可能是 sys_category 分类ID，这里缓存“ID -> 中文名称”的映射
      opportunityNameDict: {},
      followupTimeCounts: {
        total: 0,
        key1: 0, // 3天内
        key2: 0, // 4-7天前
        key3: 0, // 8-15天前
        key4: 0, // 15天前
      },
      followupPopoverLoading: false,
      followupPopoverList: [],
      followupPopoverOpportId: null,
      queryParam: {
        opportunityNo: '',
        corporateName: '',
        founder: '',
        opportunitySource: '',
        state: '',
        ownerType: 'public', // 默认公海线索
        key: '', // 跟进时间距离天数筛选：1-3天内，2-4-7天前，3-8-15天前，4-15天前
      },
      selectedRowKeys: [], // 选中的行key数组
      selectedRows: [], // 选中的行数据数组
      // 表格行选择配置
      rowSelection: null, // 将在 created 中初始化
      url: {
        list: '/opportunity/ghOpportunity/list',
        delete: '/opportunity/ghOpportunity/delete',
        deleteBatch: '/opportunity/ghOpportunity/deleteBatch',
        exportXlsUrl: '/opportunity/ghOpportunity/exportXls',
        importExcelUrl: '/opportunity/ghOpportunity/importExcel',
        assign: '/opportunity/ghOpportunity/assign',
        moveToPublic: '/opportunity/ghOpportunity/moveToPublic',
        convertToCustomer: '/opportunity/ghOpportunity/convertToCustomer',
        batchConvertToCustomer: '/opportunity/ghOpportunity/batchConvertToCustomer',
        convertAllSignedToCustomer: '/opportunity/ghOpportunity/convertAllSignedToCustomer',
        countFollowupTime: '/opportunity/ghOpportunity/countFollowupTime',
      },
      columns: [
        {
          title: '创建时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 180,
          customRender: (text) => {
            return text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : '-'
          },
        },
        {
          title: '跟进时间',
          align: 'center',
          dataIndex: 'modiTime',
          width: 180,
          customRender: (text) => {
            return text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : '-'
          },
        },
        {
          title: '距离天数',
          align: 'center',
          dataIndex: 'syDay',
          width: 100,
          scopedSlots: { customRender: 'syDay' },
        },
        {
          title: '距离天数',
          align: 'center',
          dataIndex: 'followupDays',
          width: 100,
          scopedSlots: { customRender: 'followupDays' },
        },
        {
          title: '业务人员',
          align: 'center',
          dataIndex: 'founder',
          width: 120,
        },
        {
          title: '客户名称',
          align: 'center',
          dataIndex: 'corporateName',
          width: 200,
        },
        {
          title: '线索名称',
          align: 'center',
          dataIndex: 'opportunityName',
          width: 200,
          customRender: (text, record) => {
            // 优先使用字典/分类回填的中文名称；否则展示原字段
            return record.opportunityName_dictText || text || '-'
          },
        },
        {
          title: '线索奖金',
          align: 'center',
          dataIndex: 'clueBonus',
          width: 160,
          customRender: (text) => {
            if (text === null || text === undefined || text === '') return '-'
            const num = typeof text === 'number' ? text : parseFloat(text)
            if (isNaN(num)) return '-'
            return `¥${num.toFixed(2)}`
          },
        },
        {
          title: '是否有效',
          align: 'center',
          dataIndex: 'confirmValid',
          width: 100,
          scopedSlots: { customRender: 'confirmValid' },
        },
        {
          title: '所属区域',
          align: 'center',
          dataIndex: 'region',
          width: 150,
        },
        {
          title: '客户来源',
          align: 'center',
          dataIndex: 'opportunitySource',
          width: 120,
          customRender: (text, record) => {
            return record.opportunitySource_dictText || text || '-'
          },
        },
        {
          title: '当前状态',
          align: 'center',
          dataIndex: 'state',
          width: 120,
          scopedSlots: { customRender: 'state' },
        },
        {
          title: '跟进记录',
          align: 'center',
          dataIndex: 'followupRecord',
          width: 120,
          scopedSlots: { customRender: 'followupRecord' },
        },
        {
          title: '线索编号',
          align: 'center',
          dataIndex: 'opportunityNo',
          width: 150,
        },
        {
          title: '操作',
          align: 'center',
          dataIndex: 'action',
          fixed: 'right',
          width: 150,
          scopedSlots: { customRender: 'action' },
        },
      ],
    }
  },
  created() {
    // 初始化 rowSelection 配置，确保正确绑定 this
    const that = this
    this.rowSelection = {
      selectedRowKeys: this.selectedRowKeys,
      onChange: (selectedRowKeys, selectedRows) => {
        that.selectedRowKeys = selectedRowKeys
        that.selectedRows = selectedRows
      },
      onSelectAll: (selected, selectedRows, changeRows) => {
        that.selectedRowKeys = selected ? selectedRows.map(row => row.id) : []
        that.selectedRows = selectedRows
      },
    }
    
    // 调用mixin的created方法，加载数据
    this.loadData()
    this.initDictConfig()
  },
  watch: {
    // 监听 selectedRowKeys 变化，同步到 rowSelection
    selectedRowKeys(newVal) {
      if (this.rowSelection) {
        this.rowSelection.selectedRowKeys = newVal
      }
    },
  },
  methods: {
    // 补全查询参数：mixin 的 searchReset 会清空 queryParam，导致未传 ownerType，后端会走「默认公海超30天」逻辑，个人线索会整页为空
    getQueryParams() {
      let sqp = {}
      if (this.superQueryParams) {
        sqp.superQueryParams = encodeURI(this.superQueryParams)
        sqp.superQueryMatchType = this.superQueryMatchType
      }
      let param = Object.assign(sqp, this.queryParam, this.isorter, this.filters)
      param.field = this.getQueryField()
      param.pageNo = this.ipagination.current
      param.pageSize = this.ipagination.pageSize
      const processed = processQueryParams(param)
      const res = filterObj(processed)
      if (!res.ownerType && this.currentTab) {
        res.ownerType = this.currentTab
      }
      if (res.key == null && this.selectedKey != null && this.selectedKey !== '') {
        res.key = this.selectedKey
      }
      return res
    },
    searchReset() {
      this.queryParam = {
        opportunityNo: '',
        corporateName: '',
        founder: '',
        opportunitySource: '',
        state: '',
        ownerType: this.currentTab,
      }
      if (this.selectedKey) {
        this.queryParam.key = this.selectedKey
      }
      this.loadData(1)
      this.$nextTick(() => {
        this.loadFollowupTimeCounts()
      })
    },
    // 重写 JeecgListMixin.loadData：列表加载后，把 opportunityName（分类ID）转成展示名称
    async loadData(arg) {
      // 兼容 mixin：传参为 1 时重置到第一页
      if (arg === 1) {
        this.ipagination.current = 1
      }

      const params = this.getQueryParams() // 来自 JeecgListMixin
      this.loading = true
      try {
        const res = await getAction(this.url.list, params)
        if (res.success) {
          this.dataSource = res.result.records || res.result
          if (res.result.total) {
            this.ipagination.total = res.result.total
          } else {
            this.ipagination.total = 0
          }
          await this.fillOpportunityNameLabels()
        } else {
          this.$message.warning(res.message)
        }
      } catch (e) {
        this.$message.error('加载数据失败：' + ((e && e.message) || '未知错误'))
      } finally {
        this.loading = false
      }
    },

    /** sys_category.id：兼容数字 Snowflake / 字母数字 id，排除已含中文的展示名 */
    isLikelyCategoryId(v) {
      if (v == null || v === '') return false
      const s = String(v).trim()
      if (!s) return false
      if (/[\u4e00-\u9fa5]/.test(s)) return false
      return /^[a-zA-Z0-9_-]+$/.test(s)
    },
    // 批量查询“分类ID -> 名称”，并写入每行 record.opportunityName_dictText
    async fillOpportunityNameLabels() {
      const list = this.dataSource || []
      const ids = list.map(r => r && r.opportunityName).filter(v => this.isLikelyCategoryId(v))

      const uniqueIds = Array.from(new Set(ids))
      if (uniqueIds.length === 0) {
        return
      }

      try {
        const res = await getAction('/sys/category/loadDictItem/', { ids: uniqueIds.join(',') })
        if (res && res.success && Array.isArray(res.result)) {
          const dict = {}
          uniqueIds.forEach((id, index) => {
            const label = res.result[index]
            if (label) {
              dict[id] = label
            }
          })
          this.opportunityNameDict = dict

          list.forEach(r => {
            const id = r && r.opportunityName
            if (id && dict[id]) {
              // Vue2 新增字段需要 $set 才能保证响应式更新
              this.$set(r, 'opportunityName_dictText', dict[id])
            }
          })
        }
      } catch (e) {
        // 容错：映射失败时不影响列表展示（customRender 会回退展示原字段）
        console.warn('线索名称分类映射失败', e)
      }
    },

    // Tab页切换事件
    handleTabChange(key) {
      this.currentTab = key
      this.queryParam.ownerType = key
      this.searchQuery()
      this.loadFollowupTimeCounts()
    },
    // 获取状态颜色
    getStateColor(state) {
      const colorMap = {
        'intention_a_plus': 'red', // 意向a+
        'intention_a': 'orange', // 意向a
        'intention_b': 'blue', // 意向b
        'intention_c': 'green', // 意向c
        'invalid': 'default', // 无效客户
      }
      return colorMap[state] || 'default'
    },
    // 获取状态样式（确保背景色和文字颜色正确）
    // state: 字典值（如 intention_a_plus）
    // stateDictText: 字典文本（如 "意向A+(已签约)"）
    getStateStyle(state, stateDictText) {
      // 优先使用字典文本进行匹配，如果没有则使用字典值
      const matchText = stateDictText || state || ''
      
      // 处理 null、undefined 或空字符串
      if (!matchText) {
        return {
          backgroundColor: '#d9d9d9',
          color: 'rgba(0, 0, 0, 0.85)',
          borderColor: '#d9d9d9'
        }
      }
      
      // 转换为字符串，保留原始大小写用于匹配
      const stateStr = String(matchText).trim()
      const stateLower = stateStr.toLowerCase()
      
      // 同时检查字典值（state）
      const stateValue = state ? String(state).toLowerCase().trim() : ''
      
      const styleMap = {
        'intention_a_plus': {
          backgroundColor: '#f5222d', // 红色 - 最高优先级
          color: '#fff',
          borderColor: '#f5222d'
        },
        'intention_a': {
          backgroundColor: '#fa8c16', // 橙色
          color: '#fff',
          borderColor: '#fa8c16'
        },
        'intention_b': {
          backgroundColor: '#1890ff', // 蓝色
          color: '#fff',
          borderColor: '#1890ff'
        },
        'intention_c': {
          backgroundColor: '#52c41a', // 绿色
          color: '#fff',
          borderColor: '#52c41a'
        },
        'invalid': {
          backgroundColor: '#d9d9d9', // 灰色
          color: 'rgba(0, 0, 0, 0.85)',
          borderColor: '#d9d9d9'
        }
      }
      
      // 先尝试用字典值精确匹配（小写）
      if (stateValue && styleMap[stateValue]) {
        return styleMap[stateValue]
      }
      
      // 用字典文本匹配
      // 意向A+ (已签约)
      if (stateStr.includes('意向A+') || stateStr.includes('意向a+') || 
          stateStr.includes('A+') || stateLower.includes('a+') || 
          stateLower.includes('a_plus') || stateStr.includes('已签约') ||
          stateValue === 'intention_a_plus') {
        return styleMap['intention_a_plus']
      }
      // 意向A (强烈合作)
      if ((stateStr.includes('意向A') || stateStr.includes('意向a') || stateLower.includes('intention_a')) && 
          !stateStr.includes('+') && !stateLower.includes('plus') &&
          (stateStr.includes('强烈合作') || (!stateStr.includes('意向B') && !stateStr.includes('意向C'))) ||
          stateValue === 'intention_a') {
        return styleMap['intention_a']
      }
      // 意向B (一般意向)
      if (stateStr.includes('意向B') || stateStr.includes('意向b') || 
          stateLower.includes('intention_b') || stateStr.includes('一般意向') ||
          stateValue === 'intention_b') {
        return styleMap['intention_b']
      }
      // 意向C (先咨不急)
      if (stateStr.includes('意向C') || stateStr.includes('意向c') || 
          stateLower.includes('intention_c') || stateStr.includes('先咨不急') ||
          stateValue === 'intention_c') {
        return styleMap['intention_c']
      }
      // 无效客户
      if (stateStr.includes('无效客户') || stateStr.includes('无效') || 
          stateLower.includes('invalid') || stateValue === 'invalid') {
        return styleMap['invalid']
      }
      
      // 默认返回灰色
      return {
        backgroundColor: '#d9d9d9',
        color: 'rgba(0, 0, 0, 0.85)',
        borderColor: '#d9d9d9'
      }
    },
    // 获取距离天数颜色
    getFollowupDaysColor(followupDays) {
      if (followupDays === null || followupDays === undefined) {
        return 'default'
      }
      if (followupDays <= 3) {
        return 'green' // 3天内跟进，绿色
      } else if (followupDays <= 7) {
        return 'orange' // 4-7天，橙色
      } else if (followupDays <= 15) {
        return 'red' // 8-15天，红色
      } else {
        return 'volcano' // 15天以上，深红色
      }
    },
    getFollowupDaysStyle(followupDays) {
      if (followupDays === null || followupDays === undefined) {
        return {
          backgroundColor: '#d9d9d9',
          color: 'rgba(0, 0, 0, 0.85)',
          borderColor: '#d9d9d9'
        }
      }
      if (followupDays <= 3) {
        return {
          backgroundColor: '#52c41a',
          color: '#fff',
          borderColor: '#52c41a'
        }
      } else if (followupDays <= 7) {
        return {
          backgroundColor: '#fa8c16',
          color: '#fff',
          borderColor: '#fa8c16'
        }
      } else if (followupDays <= 15) {
        return {
          backgroundColor: '#f5222d',
          color: '#fff',
          borderColor: '#f5222d'
        }
      } else {
        return {
          backgroundColor: '#fa541c',
          color: '#fff',
          borderColor: '#fa541c'
        }
      }
    },
    getSyDayColor(syDay) {
      if (syDay === null || syDay === undefined) {
        return 'default'
      }
      if (syDay <= 3) {
        return 'green' // 3天内，绿色
      } else if (syDay <= 7) {
        return 'blue' // 4-7天，蓝色
      } else if (syDay <= 15) {
        return 'orange' // 8-15天，橙色
      } else {
        return 'red' // 15天以上，红色
      }
    },
    getSyDayStyle(syDay) {
      if (syDay === null || syDay === undefined) {
        return {
          backgroundColor: '#d9d9d9',
          color: 'rgba(0, 0, 0, 0.85)',
          borderColor: '#d9d9d9'
        }
      }
      if (syDay <= 3) {
        return {
          backgroundColor: '#52c41a',
          color: '#fff',
          borderColor: '#52c41a'
        }
      } else if (syDay <= 7) {
        return {
          backgroundColor: '#1890ff',
          color: '#fff',
          borderColor: '#1890ff'
        }
      } else if (syDay <= 15) {
        return {
          backgroundColor: '#fa8c16',
          color: '#fff',
          borderColor: '#fa8c16'
        }
      } else {
        return {
          backgroundColor: '#f5222d',
          color: '#fff',
          borderColor: '#f5222d'
        }
      }
    },
    // 左侧跟进时间距离天数菜单点击事件
    handleKeyMenuClick({ key }) {
      this.selectedKey = key
      this.queryParam.key = key || undefined
      this.searchQuery()
      this.loadFollowupTimeCounts()
    },
    onFollowupPopoverVisible(visible, record) {
      if (visible && record && record.id) {
        this.followupPopoverOpportId = record.id
        this.loadFollowupPopoverList(record.id)
      }
    },
    loadFollowupPopoverList(opportId) {
      if (!opportId) {
        this.followupPopoverList = []
        return
      }
      this.followupPopoverLoading = true
      getAction('/followup/ghFollowupDetail/list', { opportId, pageNo: 1, pageSize: 1000 })
        .then((res) => {
          if (this.followupPopoverOpportId !== opportId) {
            return
          }
          if (res.success) {
            this.followupPopoverList = res.result.records || res.result || []
          } else {
            this.followupPopoverList = []
          }
        })
        .catch(() => {
          if (this.followupPopoverOpportId === opportId) {
            this.followupPopoverList = []
          }
        })
        .finally(() => {
          if (this.followupPopoverOpportId === opportId) {
            this.followupPopoverLoading = false
          }
        })
    },
    formatFollowupDateTime(d) {
      return d ? moment(d).format('YYYY-MM-DD HH:mm:ss') : '-'
    },
    formatFollowupDate(d) {
      return d ? moment(d).format('YYYY-MM-DD') : '-'
    },
    // 查看跟进记录（完整弹窗：含新建跟进）
    handleFollowupRecord(record) {
      this.$refs.followupModal.show(record)
    },
    // 操作菜单点击事件
    handleActionMenuClick({ key }, record) {
      switch (key) {
        case 'followup':
          this.handleFollowup(record)
          break
        case 'assign':
          this.handleAssign(record)
          break
        case 'moveToPublic':
          this.handleMoveToPublic(record)
          break
        case 'convertToCustomer':
          this.handleConvertToCustomer(record)
          break
        case 'edit':
          this.handleEdit(record)
          break
        case 'delete':
          this.handleDelete(record.id)
          break
      }
    },
    // 新增线索
    handleAdd() {
      this.$refs.modalForm.add()
    },
    // 线索跟进
    handleFollowup(record) {
      this.$refs.followupModal.show(record)
    },
    // 线索分配
    handleAssign(record) {
      this.$refs.assignModal.show(record)
    },
    // 移入公海
    handleMoveToPublic(record) {
      const that = this
      this.$confirm({
        title: '确认移入公海',
        content: `确定将线索"${record.corporateName || record.opportunityNo}"移入公海吗？`,
        onOk() {
          postAction(that.url.moveToPublic, { id: record.id }).then((res) => {
            if (res.success) {
              that.$message.success('移入公海成功！')
              that.loadData()
            } else {
              that.$message.error(res.message || '移入公海失败！')
            }
          }).catch((err) => {
            that.$message.error('移入公海失败：' + (err.message || '未知错误'))
          })
        },
      })
    },
    // 转客户
    handleConvertToCustomer(record) {
      const that = this
      this.$confirm({
        title: '确认转客户池',
        content: `确定将线索"${record.corporateName || record.opportunityNo}"转入客户池吗？`,
        onOk() {
          postAction(that.url.convertToCustomer, { id: record.id }).then((res) => {
            if (res.success) {
              const msg = res.message || '转客户池成功！'
              that.$message.success(msg.replace('转客户', '转客户池'))
              that.loadData()
            } else {
              const msg = res.message || '转客户池失败！'
              that.$message.error(msg.replace('转客户', '转客户池'))
            }
          }).catch((err) => {
            that.$message.error('转客户池失败：' + (err.message || '未知错误'))
          })
        },
      })
    },
    // 批量转客户
    handleBatchConvertToCustomer() {
      if (!this.selectedRowKeys || this.selectedRowKeys.length === 0) {
        this.$message.warning('请先选择要转换的线索')
        return
      }
      
      // 筛选出状态为"意向A+(已签约)"的线索（state为"intention_a_plus"或"1"）
      const eligibleRows = this.selectedRows.filter(row => {
        const state = row.state
        return state === 'intention_a_plus' || state === '1' || state === 1
      })
      
      if (eligibleRows.length === 0) {
        this.$message.warning('所选线索中没有状态为"意向A+(已签约)"的线索，无法批量转客户池')
        return
      }
      
      // 检查是否有公司名称为空的线索
      const emptyNameRows = eligibleRows.filter(row => !row.corporateName || row.corporateName.trim() === '')
      if (emptyNameRows.length > 0) {
        this.$message.warning(`有${emptyNameRows.length}条线索的公司名称为空，无法转客户池`)
        return
      }
      
      const that = this
      const eligibleIds = eligibleRows.map(row => row.id)
      this.$confirm({
        title: '确认批量转客户池',
        content: `确定将选中的${eligibleIds.length}条"意向A+(已签约)"线索转入客户池吗？`,
        onOk() {
          postAction(that.url.batchConvertToCustomer, { ids: eligibleIds }).then((res) => {
            if (res.success) {
              const result = res.result || {}
              const successCount = result.successCount || 0
              const failCount = result.failCount || 0
              const failMessages = result.failMessages || []
              
              let message = `批量转客户池完成！成功：${successCount}条`
              if (failCount > 0) {
                message += `，失败：${failCount}条`
                if (failMessages.length > 0) {
                  message += `\n失败原因：${failMessages.join('；')}`
                }
              }
              
              that.$message.success(message)
              // 清空选择
              that.selectedRowKeys = []
              that.selectedRows = []
              // 刷新数据
              that.loadData()
            } else {
              const msg = res.message || '批量转客户池失败！'
              that.$message.error(msg.replace('转客户', '转客户池'))
            }
          }).catch((err) => {
            that.$message.error('批量转客户池失败：' + (err.message || '未知错误'))
          })
        },
      })
    },
    // 全部已签约线索转客户池
    handleConvertAllSignedToCustomer() {
      const that = this
      this.$confirm({
        title: '确认全部转客户池',
        content: '确定将所有已签约且未转客户的线索转入客户池吗？系统会自动过滤已转过的线索。',
        onOk() {
          // 显示加载提示
          const hide = that.$message.loading('正在转换中，请稍候...', 0)
          postAction(that.url.convertAllSignedToCustomer, {}).then((res) => {
            hide()
            if (res.success) {
              const result = res.result || {}
              const totalCount = result.totalCount || 0
              const successCount = result.successCount || 0
              const failCount = result.failCount || 0
              const skippedCount = result.skippedCount || 0
              const failMessages = result.failMessages || []
              
              let message = `全部转客户池完成！`
              if (totalCount > 0) {
                message += `共找到${totalCount}条已签约线索，`
              }
              message += `成功：${successCount}条`
              if (skippedCount > 0) {
                message += `，已跳过（已转换）：${skippedCount}条`
              }
              if (failCount > 0) {
                message += `，失败：${failCount}条`
                if (failMessages.length > 0 && failMessages.length <= 5) {
                  message += `\n失败原因：${failMessages.join('；')}`
                } else if (failMessages.length > 5) {
                  message += `\n失败原因（前5条）：${failMessages.slice(0, 5).join('；')}...`
                }
              }
              
              that.$message.success(message, 10) // 显示10秒
              // 清空选择
              that.selectedRowKeys = []
              that.selectedRows = []
              // 刷新数据
              that.loadData()
            } else {
              const msg = res.message || '全部转客户池失败！'
              that.$message.error(msg.replace('转客户', '转客户池'))
            }
          }).catch((err) => {
            hide()
            that.$message.error('全部转客户池失败：' + (err.message || '未知错误'))
          })
        },
      })
    },
    // 编辑线索
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
    },
    // 删除线索
    handleDelete(id) {
      const that = this
      this.$confirm({
        title: '确认删除',
        content: '确定删除这条线索吗？',
        onOk() {
          deleteOpportunity({ id: id }).then((res) => {
            if (res.success) {
              that.$message.success('删除成功！')
              that.loadData()
            } else {
              that.$message.error(res.message || '删除失败！')
            }
          })
        },
      })
    },
    // 分配成功回调
    handleAssignOk() {
      this.loadData()
    },
    // 跟进成功回调（跟进会更新 modi_time；若左侧选中「3天内/4-7天」等时段，记录会离开当前筛选，易被误以为消失）
    handleFollowupOk() {
      this.selectedKey = ''
      this.queryParam.key = undefined
      this.$message.info('跟进已保存。已切换左侧「跟进时间」为「全部」，避免线索因时段筛选暂时看不到。')
      this.loadData()
      this.$nextTick(() => {
        this.loadFollowupTimeCounts()
      })
    },
    // 表单提交成功回调
    modalFormOk() {
      this.loadData()
      this.$nextTick(() => {
        this.loadFollowupTimeCounts()
      })
    },
    // 重写handleTableChange，在表格变化后加载统计数据
    handleTableChange(pagination, filters, sorter) {
      // 调用父类的handleTableChange方法
      if (this.$options.mixins) {
        const mixin = this.$options.mixins.find(m => m.methods && m.methods.handleTableChange)
        if (mixin && mixin.methods.handleTableChange) {
          mixin.methods.handleTableChange.call(this, pagination, filters, sorter)
        }
      }
      // 加载统计数据（使用防抖，避免频繁调用）
      this.$nextTick(() => {
        this.loadFollowupTimeCounts()
      })
    },
    // 从后端加载统计各时间段的数量（全部数据）
    loadFollowupTimeCounts() {
      const that = this
      // 构建查询参数（与列表查询保持一致）
      const params = {
        ownerType: this.queryParam.ownerType || this.currentTab,
        opportunityNo: this.queryParam.opportunityNo,
        corporateName: this.queryParam.corporateName,
        founder: this.queryParam.founder,
        opportunitySource: this.queryParam.opportunitySource,
        state: this.queryParam.state,
      }
      
      // 调用后端统计接口
      // countFollowupTime 统计数据量大，单独放宽超时到 60s
      getActionTimeout(this.url.countFollowupTime, params, 60000).then((res) => {
        if (res.success && res.result) {
          that.followupTimeCounts = {
            total: res.result.total || 0,
            key1: res.result.key1 || 0, // 3天内
            key2: res.result.key2 || 0, // 4-7天前
            key3: res.result.key3 || 0, // 8-15天前
            key4: res.result.key4 || 0, // 15天前
          }
        } else {
          // 如果接口调用失败，重置为0
          that.followupTimeCounts = {
            total: 0,
            key1: 0,
            key2: 0,
            key3: 0,
            key4: 0,
          }
        }
      }).catch((err) => {
        console.error('加载跟进时间统计失败', err)
        // 出错时重置为0
        that.followupTimeCounts = {
          total: 0,
          key1: 0,
          key2: 0,
          key3: 0,
          key4: 0,
        }
      })
    },
  },
  computed: {
    //token header
    tokenHeader(){
      let head = {'X-Access-Token': Vue.ls.get(ACCESS_TOKEN)}
      let tenantid = Vue.ls.get(TENANT_ID)
      if(tenantid){
        head['tenant-id'] = tenantid
      }
      return head;
    },
    importExcelUrl: function() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    },
  },
  mounted() {
    // 初始化时设置默认选中"全部"
    this.selectedKey = ''
    // 加载统计数据
    this.$nextTick(() => {
      this.loadFollowupTimeCounts()
    })
  },
}
</script>

<style scoped>
@import '~@/assets/less/common.less';

.followup-record-cell {
  display: inline-block;
  min-width: 88px;
}

.followup-popover-title-sub {
  color: #888;
  font-size: 12px;
  font-weight: normal;
  margin-left: 8px;
}

.followup-popover-content {
  min-width: 280px;
  max-width: 440px;
}

.followup-popover-list {
  max-height: 300px;
  overflow-y: auto;
  margin: -8px -4px 0;
}

.followup-popover-empty {
  margin: 8px 0;
  padding: 0 16px;
}

.followup-popover-item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.followup-popover-person {
  font-weight: 600;
}

.followup-popover-meta {
  color: #999;
  font-size: 12px;
}

.followup-popover-desc {
  margin-top: 6px;
  color: rgba(0, 0, 0, 0.85);
  white-space: pre-wrap;
  word-break: break-word;
}

.followup-popover-next {
  margin-top: 6px;
  color: #1890ff;
  font-size: 12px;
}

/* 修复表格文字和背景色一致的问题 */
::v-deep .ant-table {
  color: rgba(0, 0, 0, 0.85);
}

::v-deep .ant-table-tbody > tr > td {
  color: rgba(0, 0, 0, 0.85);
}

::v-deep .ant-table-thead > tr > th {
  color: rgba(0, 0, 0, 0.85);
  background: #fafafa;
}

::v-deep .ant-table-tbody > tr:hover > td {
  background: #f5f5f5;
}

::v-deep .ant-menu-item {
  color: rgba(0, 0, 0, 0.65);
}

::v-deep .ant-menu-item-selected {
  color: #1890ff;
  background-color: #e6f7ff;
}

::v-deep .ant-menu-item:hover {
  color: #1890ff;
}

/* 修复 Tag 组件文字颜色和背景色对比度问题 */
/* 强制覆盖 Ant Design Vue Tag 的内联样式 */
::v-deep .ant-table .ant-tag {
  border-color: transparent !important;
}

/* 跟进时间 Tag 的特殊样式 */
::v-deep .followup-days-tag,
::v-deep .ant-table .ant-tag[style*="background"] {
  color: #fff !important;
}

/* 确保不同颜色的 Tag 都有清晰的文字颜色和背景色 */
::v-deep .ant-tag-green,
::v-deep .ant-table .ant-tag[style*="rgb(82, 196, 26)"],
::v-deep .ant-table .ant-tag[style*="#52c41a"] {
  background-color: #52c41a !important;
  background: #52c41a !important;
  color: #fff !important;
  border-color: #52c41a !important;
}

::v-deep .ant-tag-blue,
::v-deep .ant-table .ant-tag[style*="rgb(24, 144, 255)"],
::v-deep .ant-table .ant-tag[style*="#1890ff"] {
  background-color: #1890ff !important;
  background: #1890ff !important;
  color: #fff !important;
  border-color: #1890ff !important;
}

::v-deep .ant-tag-orange,
::v-deep .ant-table .ant-tag[style*="rgb(250, 140, 22)"],
::v-deep .ant-table .ant-tag[style*="#fa8c16"] {
  background-color: #fa8c16 !important;
  background: #fa8c16 !important;
  color: #fff !important;
  border-color: #fa8c16 !important;
}

::v-deep .ant-tag-red,
::v-deep .ant-table .ant-tag[style*="rgb(245, 34, 45)"],
::v-deep .ant-table .ant-tag[style*="#f5222d"] {
  background-color: #f5222d !important;
  background: #f5222d !important;
  color: #fff !important;
  border-color: #f5222d !important;
}

::v-deep .ant-tag-volcano,
::v-deep .ant-table .ant-tag[style*="rgb(250, 84, 28)"],
::v-deep .ant-table .ant-tag[style*="#fa541c"] {
  background-color: #fa541c !important;
  background: #fa541c !important;
  color: #fff !important;
  border-color: #fa541c !important;
}

::v-deep .ant-tag-default {
  background-color: #d9d9d9 !important;
  background: #d9d9d9 !important;
  color: rgba(0, 0, 0, 0.85) !important;
  border-color: #d9d9d9 !important;
}

/* 通用规则：表格中所有非 default 的 Tag 都使用白色文字 */
::v-deep .ant-table .ant-tag:not(.ant-tag-default) {
  color: #fff !important;
}
</style>
