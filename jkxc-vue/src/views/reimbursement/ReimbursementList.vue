<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="报销时间">
              <j-date
                placeholder="请选择报销时间"
                v-model="queryParam.reimbursementTime"
                style="width: 100%"
                :show-time="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="报销人员">
              <j-search-select-tag
                placeholder="请选择报销人员"
                v-model="queryParam.reimbursementPerson"
                dict="sys_user,realname,realname,1=1 and del_flag = '0'"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like" allowClear></JInput>
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
      <a-button @click="handleAdd" type="primary" icon="plus">新增报销</a-button>
    </div>

    <!-- Tab页 -->
    <a-tabs v-model="activeTab" @change="handleTabChange" style="margin-top: 16px;">
      <a-tab-pane key="all" tab="全部">
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
          class="j-table-force-nowrap"
          @change="handleTableChange"
        >
          <span slot="reimbursementTime" slot-scope="text">
            {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
          </span>
          <span slot="auditFlag" slot-scope="text">
            <a-tag v-if="text === '1'" color="green">已审核</a-tag>
            <a-tag v-else-if="text === '0'" color="orange">待审核</a-tag>
            <a-tag v-else-if="text === '2'" color="red">已拒绝</a-tag>
            <span v-else>-</span>
          </span>
          <span slot="reimbursementAmount" slot-scope="text">
            <span v-if="text" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
            <span v-else>-</span>
          </span>
          <span slot="remarks" slot-scope="text, record">
            <a-input
              v-if="editingRemarksId === record.id"
              v-model="editingRemarks"
              @blur="handleRemarksBlur(record)"
              @pressEnter="handleRemarksBlur(record)"
              :autoFocus="true"
              style="width: 100%"
            />
            <span v-else @click="handleRemarksClick(record)" style="cursor: pointer; color: #1890ff;">
              {{ text || '点击编辑' }}
            </span>
          </span>
          <span slot="reimbursementBasis" slot-scope="text, record">
            <div v-if="text || record.zfVouchers" style="display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;">
              <img
                v-for="(img, index) in getImageList(text || record.zfVouchers)"
                :key="index"
                :src="getImageUrl(img)"
                alt="报销依据"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="paymentBasis" slot-scope="text, record">
            <div v-if="text || record.zfVouchers" style="display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;">
              <img
                v-for="(img, index) in getImageList(text || record.zfVouchers)"
                :key="index"
                :src="getImageUrl(img)"
                alt="支付依据"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="action" slot-scope="text, record">
            <a-dropdown>
              <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
                <a-menu-item key="edit">
                  <a-icon type="edit" />信息编辑
                </a-menu-item>
                <a-menu-item key="audit" v-has="'reimbursement:audit'">
                  <a-icon type="audit" />报销审核
                </a-menu-item>
                <a-menu-item key="delete" :disabled="!canDeleteReimburse(record)">
                  <a-icon type="delete" />删除
                </a-menu-item>
              </a-menu>
              <a-button>
                操作 <a-icon type="down" />
              </a-button>
            </a-dropdown>
          </span>
        </a-table>
      </a-tab-pane>
      <a-tab-pane key="address" tab="地址费用">
        <a-table
          ref="addressTable"
          size="middle"
          :scroll="{ x: true }"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="addressDataSource"
          :pagination="addressPagination"
          :loading="addressLoading"
          class="j-table-force-nowrap"
          @change="handleAddressTableChange"
        >
          <span slot="reimbursementTime" slot-scope="text">
            {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
          </span>
          <span slot="auditFlag" slot-scope="text">
            <a-tag v-if="text === '1'" color="green">已审核</a-tag>
            <a-tag v-else-if="text === '0'" color="orange">待审核</a-tag>
            <a-tag v-else-if="text === '2'" color="red">已拒绝</a-tag>
            <span v-else>-</span>
          </span>
          <span slot="reimbursementAmount" slot-scope="text">
            <span v-if="text" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
            <span v-else>-</span>
          </span>
          <span slot="remarks" slot-scope="text, record">
            <a-input
              v-if="editingRemarksId === record.id"
              v-model="editingRemarks"
              @blur="handleRemarksBlur(record)"
              @pressEnter="handleRemarksBlur(record)"
              :autoFocus="true"
              style="width: 100%"
            />
            <span v-else @click="handleRemarksClick(record)" style="cursor: pointer; color: #1890ff;">
              {{ text || '点击编辑' }}
            </span>
          </span>
          <span slot="reimbursementBasis" slot-scope="text, record">
            <div v-if="text || record.zfVouchers" style="display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;">
              <img
                v-for="(img, index) in getImageList(text || record.zfVouchers)"
                :key="index"
                :src="getImageUrl(img)"
                alt="报销依据"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="paymentBasis" slot-scope="text, record">
            <div v-if="text || record.zfVouchers" style="display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;">
              <img
                v-for="(img, index) in getImageList(text || record.zfVouchers)"
                :key="index"
                :src="getImageUrl(img)"
                alt="支付依据"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="action" slot-scope="text, record">
            <a-dropdown>
              <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
                <a-menu-item key="edit">
                  <a-icon type="edit" />信息编辑
                </a-menu-item>
                <a-menu-item key="audit" v-has="'reimbursement:audit'">
                  <a-icon type="audit" />报销审核
                </a-menu-item>
                <a-menu-item key="delete" :disabled="!canDeleteReimburse(record)">
                  <a-icon type="delete" />删除
                </a-menu-item>
              </a-menu>
              <a-button>
                操作 <a-icon type="down" />
              </a-button>
            </a-dropdown>
          </span>
        </a-table>
      </a-tab-pane>
      <a-tab-pane key="salary" tab="人工提成支出">
        <a-table
          ref="salaryTable"
          size="middle"
          :scroll="{ x: true }"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="salaryDataSource"
          :pagination="salaryPagination"
          :loading="salaryLoading"
          class="j-table-force-nowrap"
          @change="handleSalaryTableChange"
        >
          <span slot="reimbursementTime" slot-scope="text">
            {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
          </span>
          <span slot="auditFlag" slot-scope="text">
            <a-tag v-if="text === '1'" color="green">已审核</a-tag>
            <a-tag v-else-if="text === '0'" color="orange">待审核</a-tag>
            <a-tag v-else-if="text === '2'" color="red">已拒绝</a-tag>
            <span v-else>-</span>
          </span>
          <span slot="reimbursementAmount" slot-scope="text">
            <span v-if="text" style="color: #f5222d; font-weight: 600">¥{{ formatAmount(text) }}</span>
            <span v-else>-</span>
          </span>
          <span slot="remarks" slot-scope="text, record">
            <a-input
              v-if="editingRemarksId === record.id"
              v-model="editingRemarks"
              @blur="handleRemarksBlur(record)"
              @pressEnter="handleRemarksBlur(record)"
              :autoFocus="true"
              style="width: 100%"
            />
            <span v-else @click="handleRemarksClick(record)" style="cursor: pointer; color: #1890ff;">
              {{ text || '点击编辑' }}
            </span>
          </span>
          <span slot="reimbursementBasis" slot-scope="text, record">
            <div v-if="text || record.zfVouchers" style="display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;">
              <img
                v-for="(img, index) in getImageList(text || record.zfVouchers)"
                :key="index"
                :src="getImageUrl(img)"
                alt="报销依据"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="paymentBasis" slot-scope="text, record">
            <div v-if="text || record.zfVouchers" style="display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;">
              <img
                v-for="(img, index) in getImageList(text || record.zfVouchers)"
                :key="index"
                :src="getImageUrl(img)"
                alt="支付依据"
                style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="action" slot-scope="text, record">
            <a-dropdown>
              <a-menu slot="overlay" @click="handleActionMenuClick($event, record)">
                <a-menu-item key="edit">
                  <a-icon type="edit" />信息编辑
                </a-menu-item>
                <a-menu-item key="audit" v-has="'reimbursement:audit'">
                  <a-icon type="audit" />报销审核
                </a-menu-item>
                <a-menu-item key="delete" :disabled="!canDeleteReimburse(record)">
                  <a-icon type="delete" />删除
                </a-menu-item>
              </a-menu>
              <a-button>
                操作 <a-icon type="down" />
              </a-button>
            </a-dropdown>
          </span>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <!-- 报销表单弹窗 -->
    <reimbursement-modal ref="modalForm" @ok="modalFormOk"></reimbursement-modal>
    
    <!-- 报销审核弹窗 -->
    <reimbursement-audit-modal ref="auditModal" @ok="modalFormOk"></reimbursement-audit-modal>

    <!-- 图片预览弹窗 -->
    <a-modal :visible="previewVisible" :footer="null" @cancel="previewVisible = false" :width="800">
      <img alt="预览" style="width: 100%" :src="previewImageUrl" />
    </a-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import ReimbursementModal from './modules/ReimbursementModal'
import ReimbursementAuditModal from './modules/ReimbursementAuditModal'
import { getAction, postAction, deleteAction } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'ReimbursementList',
  mixins: [JeecgListMixin],
  components: {
    ReimbursementModal,
    ReimbursementAuditModal,
  },
  data() {
    return {
      description: '报销管理页面',
      activeTab: 'all', // 当前tab：all-全部，salary-人工提成支出
      queryParam: {
        reimbursementTime: '',
        reimbursementPerson: '',
        companyName: '',
        searchStatus: '1', // '1' 表示排除地址费用和人工提成，查询全部（默认）
      },
      // 地址费用数据
      addressDataSource: [],
      addressPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      addressLoading: false,
      // 人工提成支出数据
      salaryDataSource: [],
      salaryPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      salaryLoading: false,
      // 备注编辑相关
      editingRemarksId: null,
      editingRemarks: '',
      previewVisible: false,
      previewImageUrl: '',
      url: {
        list: '/GhReimbursement/ghReimbursement/list',
        delete: '/GhReimbursement/ghReimbursement/delete',
        deleteBatch: '/GhReimbursement/ghReimbursement/deleteBatch',
        exportXlsUrl: '/GhReimbursement/ghReimbursement/exportXls',
        importExcelUrl: '/GhReimbursement/ghReimbursement/importExcel',
      },
      columns: [
        {
          title: '报销时间',
          align: 'center',
          dataIndex: 'reimbursementTime',
          width: 120,
          scopedSlots: { customRender: 'reimbursementTime' },
        },
        {
          title: '报销人员',
          align: 'center',
          dataIndex: 'reimbursementPerson',
          width: 120,
          customRender: (text, record) => {
            return record.reimbursementPerson_dictText || text || '-'
          },
        },
        {
          title: '所属团队',
          align: 'center',
          dataIndex: 'teamId',
          width: 120,
          customRender: (text, record) => {
            return record.teamId_dictText || record.teamName || text || '-'
          },
        },
        {
          title: '报销类目',
          align: 'center',
          dataIndex: 'category',
          width: 150,
          customRender: (text, record) => {
            return record.category_dictText || text || '-'
          },
        },
        {
          title: '备注信息',
          align: 'center',
          dataIndex: 'remarks',
          width: 200,
          scopedSlots: { customRender: 'remarks' },
        },
        {
          title: '审核状态',
          align: 'center',
          dataIndex: 'auditFlag',
          width: 120,
          scopedSlots: { customRender: 'auditFlag' },
        },
        {
          title: '公司名称',
          align: 'center',
          dataIndex: 'companyName',
          width: 200,
        },
        {
          title: '报销金额',
          align: 'center',
          dataIndex: 'totalPrice',
          width: 120,
          scopedSlots: { customRender: 'reimbursementAmount' },
        },
        {
          title: '报销依据',
          align: 'center',
          dataIndex: 'reimbursementBasis',
          width: 200,
          scopedSlots: { customRender: 'reimbursementBasis' },
        },
        {
          title: '支付依据',
          align: 'center',
          dataIndex: 'paymentBasis',
          width: 200,
          scopedSlots: { customRender: 'paymentBasis' },
        },
        {
          title: '支付账号',
          align: 'center',
          dataIndex: 'paymentAccountDisplay',
          width: 250,
          customRender: (text, record) => {
            if (record.paymentAccountDisplay) {
              return record.paymentAccountDisplay
            }
            if (record.paymentBankName && record.paymentBankAccount) {
              return `${record.paymentBankName} ${record.paymentBankAccount}`
            }
            return '-'
          },
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
  methods: {
    // 新增报销
    handleAdd() {
      this.$refs.modalForm.add()
    },
    // 操作菜单点击事件
    canDeleteReimburse(record) {
      const f = record && record.auditFlag
      return f === '0' || f === undefined || f === null || f === ''
    },
    handleActionMenuClick({ key }, record) {
      switch (key) {
        case 'edit':
          this.handleEdit(record)
          break
        case 'audit':
          this.handleAudit(record)
          break
        case 'delete':
          this.handleReimbursementDelete(record)
          break
      }
    },
    handleReimbursementDelete(record) {
      if (!this.canDeleteReimburse(record)) {
        this.$message.warning('已审核或已拒绝的报销不可删除')
        return
      }
      const that = this
      this.$confirm({
        title: '确认删除',
        content: '确定删除该条报销记录？删除后不可恢复。',
        onOk() {
          return deleteAction(that.url.delete, { id: record.id }).then((res) => {
            if (res.success) {
              that.$message.success(res.message || '删除成功')
              if (that.activeTab === 'all') {
                that.reCalculatePage(1)
                that.loadData()
              } else if (that.activeTab === 'address') {
                that.loadAddressData()
              } else {
                that.loadSalaryData()
              }
            } else {
              that.$message.warning(res.message)
            }
          })
        },
      })
    },
    // 编辑报销
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
    },
    // 报销审核
    handleAudit(record) {
      this.$refs.auditModal.show(record)
    },
    // Tab切换
    handleTabChange(activeKey) {
      this.activeTab = activeKey
      if (activeKey === 'all') {
        // 全部标签页：不传递 searchStatus，查询所有数据（排除人工提成和地址费用）
        // 通过设置 searchStatus 为空或不设置，让后端查询全部数据
        this.queryParam.searchStatus = '1' // '1' 表示排除地址费用和人工提成
        this.loadData()
      } else if (activeKey === 'address') {
        this.loadAddressData()
      } else if (activeKey === 'salary') {
        this.loadSalaryData()
      }
    },
    // 加载地址费用数据
    loadAddressData() {
      this.addressLoading = true
      const params = {
        pageNo: this.addressPagination.current,
        pageSize: this.addressPagination.pageSize,
        searchStatus: '3', // 3表示只查询地址费用
      }
      // 添加查询条件
      if (this.queryParam.reimbursementTime) {
        params.reimbursementTime = this.queryParam.reimbursementTime
      }
      if (this.queryParam.reimbursementPerson) {
        params.reimbursementPerson = this.queryParam.reimbursementPerson
      }
      if (this.queryParam.companyName) {
        params.companyName = this.queryParam.companyName
      }
      getAction(this.url.list, params).then((res) => {
        if (res.success && res.result) {
          this.addressDataSource = res.result.records || []
          this.addressPagination.total = res.result.total || 0
        } else {
          this.addressDataSource = []
          this.addressPagination.total = 0
        }
      }).catch(() => {
        this.addressDataSource = []
        this.addressPagination.total = 0
      }).finally(() => {
        this.addressLoading = false
      })
    },
    // 地址费用表格分页变化
    handleAddressTableChange(pagination) {
      this.addressPagination.current = pagination.current
      this.addressPagination.pageSize = pagination.pageSize
      this.loadAddressData()
    },
    // 加载人工提成支出数据
    loadSalaryData() {
      this.salaryLoading = true
      const params = {
        pageNo: this.salaryPagination.current,
        pageSize: this.salaryPagination.pageSize,
        searchStatus: '2', // 2表示只查询人工提成
      }
      // 添加查询条件
      if (this.queryParam.reimbursementTime) {
        params.reimbursementTime = this.queryParam.reimbursementTime
      }
      if (this.queryParam.reimbursementPerson) {
        params.reimbursementPerson = this.queryParam.reimbursementPerson
      }
      if (this.queryParam.companyName) {
        params.companyName = this.queryParam.companyName
      }
      getAction(this.url.list, params).then((res) => {
        if (res.success && res.result) {
          this.salaryDataSource = res.result.records || []
          this.salaryPagination.total = res.result.total || 0
        } else {
          this.salaryDataSource = []
          this.salaryPagination.total = 0
        }
      }).catch(() => {
        this.salaryDataSource = []
        this.salaryPagination.total = 0
      }).finally(() => {
        this.salaryLoading = false
      })
    },
    // 人工提成表格分页变化
    handleSalaryTableChange(pagination) {
      this.salaryPagination.current = pagination.current
      this.salaryPagination.pageSize = pagination.pageSize
      this.loadSalaryData()
    },
    // 点击备注开始编辑
    handleRemarksClick(record) {
      this.editingRemarksId = record.id
      this.editingRemarks = record.remarks || ''
    },
    // 备注编辑完成
    handleRemarksBlur(record) {
      if (this.editingRemarksId !== record.id) {
        return
      }
      const newRemarks = this.editingRemarks || ''
      // 如果备注没有变化，直接取消编辑
      if (newRemarks === (record.remarks || '')) {
        this.editingRemarksId = null
        this.editingRemarks = ''
        return
      }
      // 调用接口更新备注
      postAction('/GhReimbursement/ghReimbursement/updateRemarks', {
        id: record.id,
        remarks: newRemarks,
      }).then((res) => {
        if (res.success) {
          this.$message.success('备注更新成功')
          record.remarks = newRemarks
          // 刷新当前tab的数据
          if (this.activeTab === 'all') {
            this.loadData()
          } else if (this.activeTab === 'salary') {
            this.loadSalaryData()
          }
        } else {
          this.$message.error(res.message || '备注更新失败')
        }
      }).catch((err) => {
        console.error('更新备注失败:', err)
        this.$message.error('备注更新失败')
      }).finally(() => {
        this.editingRemarksId = null
        this.editingRemarks = ''
      })
    },
    // 表单提交成功回调
    modalFormOk() {
      if (this.activeTab === 'all') {
        this.loadData()
      } else if (this.activeTab === 'address') {
        this.loadAddressData()
      } else if (this.activeTab === 'salary') {
        this.loadSalaryData()
      }
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getImageList(imageVoucher) {
      if (!imageVoucher) return []
      try {
        // 如果是JSON字符串，解析为数组
        if (typeof imageVoucher === 'string' && imageVoucher.startsWith('[')) {
          return JSON.parse(imageVoucher)
        }
        // 如果是逗号分隔的字符串，转换为数组
        if (typeof imageVoucher === 'string' && imageVoucher.includes(',')) {
          return imageVoucher.split(',').filter(item => item.trim())
        }
        // 如果是单个字符串，转换为数组
        return [imageVoucher]
      } catch (e) {
        return [imageVoucher]
      }
    },
    getImageUrl(imagePath) {
      if (!imagePath) return ''
      // 如果已经是完整的URL，直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      // 如果是相对路径，通过后端接口访问
      if (imagePath.startsWith('/')) {
        return imagePath
      }
      // 否则通过后端静态资源接口访问
      return '/sys/common/static/' + imagePath
    },
    previewImage(imageUrl) {
      this.previewImageUrl = imageUrl
      this.previewVisible = true
    },
    moment,
  },
}
</script>

<style scoped>
@import '~@/assets/less/common.less';

/* 确保表格文字可见 */
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
</style>

