<template>
  <div>
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="订单编号">
              <JInput placeholder="请输入订单编号" v-model="queryParam.orderNo" type="like" />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput placeholder="请输入公司名称" v-model="queryParam.companyName" type="like" />
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

    <!-- table区域 -->
    <div>
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
        <span slot="action" slot-scope="text, record">
          <template v-for="(op, index) in operations">
            <a v-if="op.key === 'audit'" :key="index" @click="handleManagerAudit(record)" style="margin-right: 8px">
              <a-icon type="check-circle" />{{ op.label }}
            </a>
            <a v-if="op.key === 'receive'" :key="index" @click="handleReceive(record)" style="margin-right: 8px">
              <a-icon type="check" />{{ op.label }}
            </a>
            <a v-if="op.key === 'updateCost'" :key="index" @click="handleUpdateCost(record)" style="margin-right: 8px">
              <a-icon type="edit" />{{ op.label }}
            </a>
            <a v-if="op.key === 'handover'" :key="index" @click="handleHandover(record)" style="margin-right: 8px">
              <a-icon type="swap" />{{ op.label }}
            </a>
            <a v-if="op.key === 'reassign'" :key="index" @click="handleReassign(record)" style="margin-right: 8px">
              <a-icon type="user-add" />{{ op.label }}
            </a>
            <a v-if="op.key === 'exception'" :key="index" @click="handleException(record)" style="margin-right: 8px">
              <a-icon type="exclamation-circle" />{{ op.label }}
            </a>
            <a-dropdown v-if="op.key === 'exceptionMenu'" :key="index" style="margin-right: 8px">
              <a-menu slot="overlay" @click="({ key }) => handleExceptionMenu(record, key)">
                <a-menu-item key="problem_task">问题任务</a-menu-item>
                <a-menu-item key="recycle_bin">回收站</a-menu-item>
                <a-menu-item key="reassign">转分配</a-menu-item>
              </a-menu>
              <a-button type="danger" size="small">
                转为异常 <a-icon type="down" />
              </a-button>
            </a-dropdown>
            <a v-if="op.key === 'confirm'" :key="index" @click="handleConfirm(record)" style="margin-right: 8px">
              <a-icon type="check-circle" />{{ op.label }}
            </a>
            <a v-if="op.key === 'recycle'" :key="index" @click="handleRecycle(record)" style="margin-right: 8px">
              <a-icon type="delete" />{{ op.label }}
            </a>
          </template>
        </span>
      </a-table>
    </div>
  </div>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { getAction, httpAction } from '@/api/manage'
import { processQueryParams } from '@/utils/util'

export default {
  name: 'BusinessTaskTable',
  mixins: [JeecgListMixin],
  props: {
    taskStatus: {
      type: String,
      required: true,
    },
    columns: {
      type: Array,
      required: true,
    },
    operations: {
      type: Array,
      default: () => [],
    },
    filterOrderNo: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      description: '工商任务列表',
      url: {
        list: '/order/businessTask/list',
      },
      queryParam: {
        taskStatus: this.taskStatus,
        orderNo: '',
        companyName: '',
      },
      // 从URL参数中获取的订单号（用于自动筛选）
      urlOrderNo: '',
    }
  },
  created() {
    // 从URL参数中获取订单号
    const query = this.$route.query
    if (query.orderNo) {
      this.urlOrderNo = query.orderNo
      this.queryParam.orderNo = query.orderNo
    }
  },
  watch: {
    taskStatus: {
      immediate: true,
      handler(newVal) {
        this.queryParam.taskStatus = newVal
        this.loadData()
      },
    },
    filterOrderNo: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.queryParam.orderNo = newVal
          this.urlOrderNo = newVal
          // 延迟加载，确保组件已初始化
          this.$nextTick(() => {
            this.loadData()
          })
        } else if (!this.urlOrderNo) {
          // 如果filterOrderNo被清空，且没有URL参数，也清空查询条件
          this.queryParam.orderNo = ''
        }
      },
    },
  },
  methods: {
    getQueryParams() {
      const param = Object.assign({}, this.queryParam)
      // 处理JInput组件的值（过滤**，去掉前后的*）
      return processQueryParams(param)
    },
    // 工商经理审核
    handleManagerAudit(record) {
      this.$emit('managerAudit', record)
    },
    // 接收任务
    handleReceive(record) {
      this.$confirm({
        title: '确认接收',
        content: `确定要接收订单 ${record.orderNo} 的任务吗？`,
        onOk: () => {
          httpAction('/order/businessTask/receive', { taskId: record.id }, 'post').then((res) => {
            if (res.success) {
              this.$message.success('接收成功')
              this.loadData()
              this.$emit('refresh')
            } else {
              this.$message.error(res.message || '接收失败')
            }
          })
        },
      })
    },
    // 填写成本
    handleUpdateCost(record) {
      this.$emit('updateCost', record)
    },
    // 去交接
    handleHandover(record) {
      this.$confirm({
        title: '确认交接',
        content: `确定要交接订单 ${record.orderNo} 的任务吗？`,
        onOk: () => {
          httpAction('/order/businessTask/handover', { taskId: record.id }, 'post').then((res) => {
            if (res.success) {
              this.$message.success('交接成功')
              this.loadData()
              this.$emit('refresh')
            } else {
              this.$message.error(res.message || '交接失败')
            }
          })
        },
      })
    },
    // 转分配
    handleReassign(record) {
      this.$emit('reassign', record)
    },
    // 转为异常
    handleException(record) {
      this.$emit('exception', record, 'problem_task')
    },
    // 异常菜单
    handleExceptionMenu(record, key) {
      if (key === 'reassign') {
        this.handleReassign(record)
      } else {
        this.$emit('exception', record, key)
      }
    },
    // 确认完成
    handleConfirm(record) {
      this.$confirm({
        title: '确认完成',
        content: `确定订单 ${record.orderNo} 的交接已完成吗？`,
        onOk: () => {
          httpAction('/order/businessTask/confirmHandover', { taskId: record.id }, 'post').then((res) => {
            if (res.success) {
              this.$message.success('确认成功')
              this.loadData()
              this.$emit('refresh')
            } else {
              this.$message.error(res.message || '确认失败')
            }
          })
        },
      })
    },
    // 回收站
    handleRecycle(record) {
      this.$emit('exception', record, 'recycle_bin')
    },
  },
}
</script>

<style scoped>
</style>

