<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="员工姓名">
              <j-search-select-tag
                placeholder="请选择员工"
                v-model="queryParam.userId"
                dict="sys_user,realname,id,del_flag=0 and status=1"
                :async="false"
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="当前职位">
              <a-input placeholder="请输入当前职位" v-model="queryParam.currentPosition" allowClear></a-input>
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
      <a-button @click="handleAdd" type="primary" icon="plus">新增工资信息</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls" style="margin-left: 8px">导出</a-button>
    </div>

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
      class="j-table-force-nowrap"
      @change="handleTableChange"
    >
      <span slot="salaryAmount" slot-scope="text, record">
        <span v-if="text && text !== '-'" style="color: #1890ff; font-weight: 600">¥{{ formatAmount(text) }}</span>
        <span v-else>-</span>
      </span>
      <span slot="action" slot-scope="text, record">
        <a @click="handleEdit(record)">编辑</a>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
          <a>删除</a>
        </a-popconfirm>
      </span>
      <template slot="footer">
        <div class="summary-footer-wrapper" v-if="dataSource && dataSource.length > 0">
          <div class="summary-footer" ref="summaryFooter">
            <table class="summary-table">
              <colgroup>
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 100px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 120px;" />
                <col style="width: 150px;" />
              </colgroup>
              <tbody>
                <tr>
                  <td style="text-align: center; font-weight: 600;">合计</td>
                  <td style="text-align: center;">-</td>
                  <td style="text-align: center;">-</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalBaseSalary) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalPerformanceSalary) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalHousingFundSubsidy) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalOtherSubsidy) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalHighTemperatureSubsidy) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalFullAttendanceBonus) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalBeforeSocial) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalCompanySocialSecurity) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalPersonalSocialSecurity) }}</td>
                  <td style="text-align: center; color: #1890ff; font-weight: 600;">¥{{ formatAmount(summaryData.totalAmount) }}</td>
                  <td style="text-align: center;">-</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </a-table>

    <!-- 工资信息表单弹窗 -->
    <salary-info-modal ref="modalForm" @ok="modalFormOk"></salary-info-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import SalaryInfoModal from './modules/SalaryInfoModal'

export default {
  name: 'SalaryInfoList',
  mixins: [JeecgListMixin],
  components: {
    SalaryInfoModal,
  },
  data() {
    return {
      description: '工资信息管理页面',
      queryParam: {
        userId: '',
        currentPosition: '',
      },
      url: {
        list: '/salary/ghSalaryInfo/list',
        delete: '/salary/ghSalaryInfo/delete',
        deleteBatch: '/salary/ghSalaryInfo/deleteBatch',
        exportXlsUrl: '/salary/ghSalaryInfo/exportXls',
        importExcelUrl: '/salary/ghSalaryInfo/importExcel',
      },
      columns: [
        {
          title: '员工姓名',
          align: 'center',
          dataIndex: 'userId',
          width: 120,
          customRender: (text, record) => {
            return record.userName || record.userId_dictText || text || '-'
          },
        },
        {
          title: '当前职位',
          align: 'center',
          dataIndex: 'currentPosition',
          width: 120,
        },
        {
          title: '当前星级',
          align: 'center',
          dataIndex: 'currentStarLevel',
          width: 100,
          customRender: (text, record) => {
            return record.currentStarLevel_dictText || text || '-'
          },
        },
        {
          title: '基本工资',
          align: 'center',
          dataIndex: 'baseSalary',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '绩效工资',
          align: 'center',
          dataIndex: 'performanceSalary',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '公积金补贴',
          align: 'center',
          dataIndex: 'housingFundSubsidy',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '其他补贴',
          align: 'center',
          dataIndex: 'otherSubsidy',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '高温补贴',
          align: 'center',
          dataIndex: 'highTemperatureSubsidy',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '全勤奖金',
          align: 'center',
          dataIndex: 'fullAttendanceBonus',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '合计金额',
          align: 'center',
          dataIndex: 'totalBeforeSocial',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '公司社保',
          align: 'center',
          dataIndex: 'companySocialSecurity',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '个人社保',
          align: 'center',
          dataIndex: 'personalSocialSecurity',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
        },
        {
          title: '合计',
          align: 'center',
          dataIndex: 'totalAmount',
          width: 120,
          scopedSlots: { customRender: 'salaryAmount' },
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
      summaryData: {
        totalBaseSalary: 0,
        totalPerformanceSalary: 0,
        totalHousingFundSubsidy: 0,
        totalHighTemperatureSubsidy: 0,
        totalOtherSubsidy: 0,
        totalFullAttendanceBonus: 0,
        totalBeforeSocial: 0,
        totalCompanySocialSecurity: 0,
        totalPersonalSocialSecurity: 0,
        totalAmount: 0,
      },
    }
  },
  watch: {
    // 监听数据源变化，自动计算合计
    dataSource: {
      handler(newVal) {
        this.$nextTick(() => {
          this.calculateTotals()
          setTimeout(() => {
            this.syncScroll()
            this.syncColumnWidths()
          }, 100)
        })
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    setTimeout(() => {
      this.syncScroll()
    }, 300)
  },
  methods: {
    // 新增工资信息
    handleAdd() {
      this.$refs.modalForm.add()
    },
    // 编辑工资信息
    handleEdit(record) {
      this.$refs.modalForm.edit(record)
    },
    // 表单提交成功回调
    modalFormOk() {
      this.loadData()
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    // 计算合计
    calculateTotals() {
      // 重置所有合计值
      this.summaryData.totalBaseSalary = 0
      this.summaryData.totalPerformanceSalary = 0
      this.summaryData.totalHousingFundSubsidy = 0
      this.summaryData.totalHighTemperatureSubsidy = 0
      this.summaryData.totalOtherSubsidy = 0
      this.summaryData.totalFullAttendanceBonus = 0
      this.summaryData.totalBeforeSocial = 0
      this.summaryData.totalCompanySocialSecurity = 0
      this.summaryData.totalPersonalSocialSecurity = 0
      this.summaryData.totalAmount = 0
      
      if (this.dataSource && this.dataSource.length > 0) {
        this.dataSource.forEach(record => {
          // 计算各字段的数值
          const baseSalary = parseFloat(record.baseSalary) || 0
          const performanceSalary = parseFloat(record.performanceSalary) || 0
          const housingFundSubsidy = parseFloat(record.housingFundSubsidy) || 0
          const highTemperatureSubsidy = parseFloat(record.highTemperatureSubsidy) || 0
          const otherSubsidy = parseFloat(record.otherSubsidy) || 0
          const fullAttendanceBonus = parseFloat(record.fullAttendanceBonus) || 0
          const companySocialSecurity = parseFloat(record.companySocialSecurity) || 0
          const personalSocialSecurity = parseFloat(record.personalSocialSecurity) || 0
          
          // 合计金额 = 基本工资 + 绩效工资 + 公积金补贴 + 高温补贴 + 其他补贴 + 全勤奖金
          const totalBeforeSocial = baseSalary + performanceSalary + housingFundSubsidy + highTemperatureSubsidy + otherSubsidy + fullAttendanceBonus
          
          // 合计 = 合计金额 + 公司社保 + 个人社保
          const totalAmount = totalBeforeSocial + companySocialSecurity + personalSocialSecurity
          
          // 设置计算字段
          record.totalBeforeSocial = totalBeforeSocial > 0 ? totalBeforeSocial : null
          record.totalAmount = totalAmount > 0 ? totalAmount : null
          
          // 累加合计值
          this.summaryData.totalBaseSalary += baseSalary
          this.summaryData.totalPerformanceSalary += performanceSalary
          this.summaryData.totalHousingFundSubsidy += housingFundSubsidy
          this.summaryData.totalHighTemperatureSubsidy += highTemperatureSubsidy
          this.summaryData.totalOtherSubsidy += otherSubsidy
          this.summaryData.totalFullAttendanceBonus += fullAttendanceBonus
          this.summaryData.totalBeforeSocial += totalBeforeSocial
          this.summaryData.totalCompanySocialSecurity += companySocialSecurity
          this.summaryData.totalPersonalSocialSecurity += personalSocialSecurity
          this.summaryData.totalAmount += totalAmount
        })
      }
    },
    // 同步滚动
    syncScroll() {
      this.$nextTick(() => {
        const tableBody = this.$el.querySelector('.ant-table-body')
        const summaryFooter = this.$refs.summaryFooter
        
        if (tableBody && summaryFooter) {
          // 移除之前的滚动监听
          if (this.handleTableScroll) {
            tableBody.removeEventListener('scroll', this.handleTableScroll)
          }
          
          // 添加滚动监听
          this.handleTableScroll = () => {
            summaryFooter.scrollLeft = tableBody.scrollLeft
          }
          tableBody.addEventListener('scroll', this.handleTableScroll)
          
          // 同步列宽
          this.syncColumnWidths()
        }
      })
    },
    // 同步列宽
    syncColumnWidths() {
      this.$nextTick(() => {
        const tableHeader = this.$el.querySelector('.ant-table-thead')
        const summaryTable = this.$el.querySelector('.summary-table')
        
        if (tableHeader && summaryTable) {
          const headerCells = tableHeader.querySelectorAll('th')
          const summaryCells = summaryTable.querySelectorAll('td')
          
          if (headerCells.length === summaryCells.length) {
            headerCells.forEach((headerCell, index) => {
              if (summaryCells[index]) {
                const width = headerCell.offsetWidth
                summaryCells[index].style.width = width + 'px'
                summaryCells[index].style.minWidth = width + 'px'
                summaryCells[index].style.maxWidth = width + 'px'
              }
            })
          }
        }
      })
    },
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

/* 合计行样式 */
.summary-footer-wrapper {
  margin: 0;
  overflow: hidden;
}

.summary-footer {
  margin: 0;
  background: #fafafa;
  overflow-x: auto;
  overflow-y: hidden;
  /* 隐藏滚动条但保持滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.summary-footer::-webkit-scrollbar {
  display: none; /* Chrome Safari */
}

.summary-table {
  width: 100%;
  min-width: 1630px; /* 所有列宽之和: 120+120+100+120+120+120+120+120+120+120+120+120+120+150=1630 */
  border-collapse: collapse;
  margin: 0;
  table-layout: fixed;
  background: #fafafa;
  border-spacing: 0;
}

.summary-table td {
  padding: 12px 8px;
  border: 1px solid #e8e8e8;
  border-top: none;
  height: 48px;
  box-sizing: border-box;
  vertical-align: middle;
}

.summary-table td:first-child {
  border-left: 1px solid #e8e8e8;
}

/* 确保合计行与主表格对齐 */
::v-deep .ant-table-footer {
  padding: 0;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
  overflow: hidden;
}

::v-deep .ant-table-body {
  border-bottom: 1px solid #e8e8e8;
}

/* 确保合计行跟随主表格滚动 */
::v-deep .ant-table-body-outer {
  margin-bottom: 0;
}

::v-deep .ant-table-scroll {
  position: relative;
}
</style>

