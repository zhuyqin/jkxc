<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :md="6" :sm="12">
            <a-form-item label="员工姓名">
              <j-input placeholder="输入姓名模糊查询" v-model="queryParam.realname"></j-input>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="工作号码">
              <a-input placeholder="请输入工作号码" v-model="queryParam.workNo"></a-input>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="私人号码">
              <a-input placeholder="请输入私人号码" v-model="queryParam.phone"></a-input>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- table区域-begin -->
    <div>
      <a-table
        ref="table"
        bordered
        size="middle"
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :scroll="{ x: 1200 }"
        @change="handleTableChange">
      </a-table>
    </div>
    <!-- table区域-end -->
  </a-card>
</template>

<script>
  import {getAction} from '@/api/manage'
  import {JeecgListMixin} from '@/mixins/JeecgListMixin'
  import JInput from '@/components/jeecg/JInput'

  export default {
    name: "ContactList",
    mixins: [JeecgListMixin],
    components: {
      JInput
    },
    data() {
      return {
        description: '通讯名录',
        // 查询条件
        queryParam: {
          status_in: '1,2', // 默认查询在职员工（状态1-正常，2-冻结）
        },
        // 表头
        columns: [
          {
            title: '员工姓名',
            align: "center",
            width: 120,
            dataIndex: 'realname',
          },
          {
            title: '工作号码',
            align: "center",
            width: 120,
            dataIndex: 'workNo',
            customRender: (text) => {
              return text || '-';
            }
          },
          {
            title: '私人号码',
            align: "center",
            width: 120,
            dataIndex: 'phone',
            customRender: (text) => {
              return text || '-';
            }
          },
          {
            title: '紧急联系',
            align: "center",
            width: 120,
            dataIndex: 'emergencyContact',
            customRender: (text) => {
              return text || '-';
            }
          },
          {
            title: '紧急电话',
            align: "center",
            width: 120,
            dataIndex: 'emergencyPhone',
            customRender: (text) => {
              return text || '-';
            }
          },
          {
            title: '所属团队',
            align: "center",
            width: 150,
            dataIndex: 'teamName',
            customRender: (text, record) => {
              return record.teamName || record.teamId_dictText || text || '-';
            }
          },
          {
            title: '当前状态',
            align: "center",
            width: 100,
            dataIndex: 'status_dictText'
          },
        ],
        url: {
          list: "/sys/user/list",
        },
        dictOptions: {},
      }
    },
    created() {
      this.queryParam.status_in = '1,2'; // 默认查询在职员工
    },
    methods: {
      // 重写查询方法，确保使用status_in参数
      loadData(arg) {
        if (!this.url.list) {
          this.$message.error("请设置url.list属性!")
          return
        }
        // 加载数据 若传入参数1则加载第一页的内容
        if (arg === 1) {
          this.ipagination.current = 1;
        }
        // 查询条件
        var params = this.getQueryParams();
        this.loading = true;
        getAction(this.url.list, params).then((res) => {
          if (res.success) {
            this.dataSource = res.result.records;
            this.ipagination.total = res.result.total;
          }
          if (res.code === 510) {
            this.$message.warning(res.message)
          }
          this.loading = false;
        })
      },
    }
  }
</script>

<style scoped>
  @import '~@assets/less/common.less'
</style>

