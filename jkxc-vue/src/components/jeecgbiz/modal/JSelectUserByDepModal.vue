<template>
  <j-modal
    :width="modalWidth"
    :visible="visible"
    :title="title"
    switchFullscreen
    wrapClassName="j-user-select-modal"
    @ok="handleSubmit"
    @cancel="close"
    style="top:50px"
    cancelText="关闭"
  >
    <a-row :gutter="10" style="background-color: #ececec; padding: 10px; margin: -10px">
      <!-- 如果指定了roleId，不显示部门树 -->
      <a-col :md="roleId ? 24 : 6" :sm="24" v-if="!roleId">
        <a-card :bordered="false">
          <!--组织机构-->
          <a-directory-tree
            selectable
            :selectedKeys="selectedDepIds"
            :checkStrictly="true"
            :dropdownStyle="{maxHeight:'200px',overflow:'auto'}"
            :treeData="departTree"
            :expandAction="false"
            :expandedKeys.sync="expandedKeys"
            @select="onDepSelect"
          />
        </a-card>
      </a-col>
      <a-col :md="roleId ? 24 : 18" :sm="24">
        <a-card :bordered="false">
          用户账号:
          <a-input-search
            :style="{width:'150px',marginBottom:'15px'}"
            placeholder="请输入账号"
            v-model="queryParam.username"
            @search="onSearch"
          ></a-input-search>
          <a-button @click="searchReset(1)" style="margin-left: 20px" icon="redo">重置</a-button>
          <!--用户列表-->
          <a-table
            ref="table"
            :scroll="scrollTrigger"
            size="middle"
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource"
            :pagination="ipagination"
            :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange,type: getType}"
            :loading="loading"
            @change="handleTableChange">
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </j-modal>
</template>

<script>
  import { pushIfNotExist, filterObj } from '@/utils/util'
  import {queryDepartTreeList, getUserList, queryUserByDepId} from '@/api/api'
  import { getAction } from '@/api/manage'

  export default {
    name: 'JSelectUserByDepModal',
    components: {},
    props: {
      modalWidth: {
        type: Number,
        default: 1250
      },
      multi: {
        type: Boolean,
        default: true
      },
      userIds: {
        type: String,
        default: ''
      },
      store: {
        type: String,
        default: 'username'
      },
      text: {
        type: String,
        default: 'realname'
      },
      roleId: {
        type: String,
        default: null
      }
    },
    data() {
      return {
        queryParam: {
          username: "",
        },
        columns: [
          {
            title: '用户账号',
            align: 'center',
            dataIndex: 'username'
          },
          {
            title: '用户姓名',
            align: 'center',
            dataIndex: 'realname'
          },
          {
            title: '性别',
            align: 'center',
            dataIndex: 'sex',
            customRender: function (text) {
              if (text === 1) {
                return '男'
              } else if (text === 2) {
                return '女'
              } else {
                return text
              }
            }
          },
          {
            title: '手机',
            align: 'center',
            dataIndex: 'phone'
          },
          {
            title: '上级',
            align: 'center',
            dataIndex: 'directSupervisor_dictText',
            customRender: (text) => {
              return text || '-';
            }
          }
        ],
        scrollTrigger: {},
        dataSource: [],
        selectionRows: [],
        selectedRowKeys: [],
        selectUserRows: [],
        selectUserIds: [],
        ipagination: {
          current: 1,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '30'],
          showTotal: (total, range) => {
            return range[0] + '-' + range[1] + ' 共' + total + '条'
          },
          showQuickJumper: true,
          showSizeChanger: true,
          total: 0
        },
        isorter: {
          column: 'createTime',
          order: 'desc'
        },
        selectedDepIds: [],
        departTree: [],
        visible: false,
        loading: false,
        expandedKeys: [],
      }
    },
    computed: {
      // 计算属性的 getter
      getType: function () {
        return this.multi == true ? 'checkbox' : 'radio';
      },
      // 动态标题
      title: function () {
        return this.roleId ? '选择用户' : '根据部门选择用户';
      }
    },
    watch: {
      userIds: {
        immediate: true,
        handler() {
          this.initUserNames()
        }
      },
    },
    created() {
      // 该方法触发屏幕自适应
      this.resetScreenSize();
      this.loadData()
    },
    methods: {
      initUserNames() {
        if (this.userIds) {
          // 这里最后加一个 , 的原因是因为无论如何都要使用 in 查询，防止后台进行了模糊匹配，导致查询结果不准确
          let values = this.userIds.split(',') + ','
          let param = {[this.store]: values}
          getAction('/sys/user/getMultiUser', param).then((list)=>{
            this.selectionRows = []
            let selectedRowKeys = []
            let textArray = []
            if(list && list.length>0){
              for(let user of list){
                textArray.push(user[this.text])
                selectedRowKeys.push(user['id'])
                this.selectionRows.push(user)
              }
            }
            this.selectedRowKeys = selectedRowKeys
            this.$emit('initComp', textArray.join(','))
          })

        } else {
          // JSelectUserByDep组件bug issues/I16634
          this.$emit('initComp', '')
          // 前端用户选择单选无法置空的问题 #2610
          this.selectedRowKeys = []
        }
      },
      async loadData(arg) {
        if (arg === 1 && this.ipagination) {
          this.ipagination.current = 1;
        }
        let params = this.getQueryParams()//查询条件
        this.loading = true
        getAction('/sys/user/queryUserComponentData', params).then(res=>{
          if (res.success) {
            this.dataSource = res.result.records
            if (this.ipagination) {
              this.ipagination.total = res.result.total
            }
          }
        }).finally(() => {
          this.loading = false
        })
      },
      // 触发屏幕自适应
      resetScreenSize() {
        let screenWidth = document.body.clientWidth;
        if (screenWidth < 500) {
          this.scrollTrigger = {x: 800};
        } else {
          this.scrollTrigger = {};
        }
      },
      showModal() {
        console.log('[JSelectUserByDepModal] showModal被调用, roleId:', this.roleId)
        this.visible = true;
        // 如果指定了roleId，不加载部门树
        if (!this.roleId) {
          this.queryDepartTree();
        }
        this.initUserNames()
        this.loadData();
      },
      getQueryParams() {
        let param = Object.assign({}, this.queryParam, this.isorter);
        param.field = this.getQueryField();
        if (this.ipagination) {
          param.pageNo = this.ipagination.current || 1;
          param.pageSize = this.ipagination.pageSize || 10;
        } else {
          param.pageNo = 1;
          param.pageSize = 10;
        }
        param.departId = this.selectedDepIds.join(',')
        // 如果指定了roleId，添加到查询参数中
        if (this.roleId) {
          param.roleId = this.roleId
          console.log('[JSelectUserByDepModal] getQueryParams添加roleId:', this.roleId)
        }
        console.log('[JSelectUserByDepModal] getQueryParams最终参数:', param)
        return filterObj(param);
      },
      getQueryField() {
        let str = 'id,';
        if (this.columns && this.columns.length > 0) {
          for (let a = 0; a < this.columns.length; a++) {
            if (this.columns[a].dataIndex) {
              str += ',' + this.columns[a].dataIndex;
            }
          }
        }
        return str;
      },
      searchReset(num) {
        let that = this;
        that.selectedRowKeys = [];
        that.selectUserIds = [];
        that.selectedDepIds = [];
        if (num !== 0) {
          that.queryParam = {};
          that.loadData(1);
        }
      },
      close() {
        this.searchReset(0);
        this.visible = false;
      },
      handleTableChange(pagination, filters, sorter) {
        //TODO 筛选
        if (Object.keys(sorter).length > 0) {
          this.isorter.column = sorter.field;
          this.isorter.order = 'ascend' === sorter.order ? 'asc' : 'desc';
        }
        this.ipagination = pagination;
        this.loadData();
      },
      handleSubmit() {
        let that = this;
        this.getSelectUserRows();
        that.$emit('ok', that.selectUserRows);
        that.searchReset(0)
        that.close();
      },
      //获取选择用户信息
      getSelectUserRows() {
        this.selectUserRows = []
        for (let row of this.selectionRows) {
          if (this.selectedRowKeys.includes(row.id)) {
            this.selectUserRows.push(row)
          }
        }
        this.selectUserIds = this.selectUserRows.map(row => row.username).join(',')
      },
      // 点击树节点,筛选出对应的用户
      onDepSelect(selectedDepIds) {
        if (selectedDepIds[0] != null) {
          if (this.selectedDepIds[0] !== selectedDepIds[0]) {
            this.selectedDepIds = [selectedDepIds[0]];
          }
          this.loadData(1);
        }
      },
      onSelectChange(selectedRowKeys, selectionRows) {
        this.selectedRowKeys = selectedRowKeys;
        selectionRows.forEach(row => pushIfNotExist(this.selectionRows, row, 'id'))
      },
      onSearch() {
        this.loadData(1);
      },
      // 根据选择的id来查询用户信息
      initQueryUserByDepId(selectedDepIds) {
        this.loading = true
        return queryUserByDepId({id: selectedDepIds.toString()}).then((res) => {
          if (res.success) {
            this.dataSource = res.result;
            this.ipagination.total = res.result.length;
          }
        }).finally(() => {
          this.loading = false
        })
      },
      queryDepartTree() {
        // 部门管理功能已移除，返回空数据
        this.departTree = [];
        this.expandedKeys = [];
        // queryDepartTreeList().then((res) => {
        //   if (res.success) {
        //     this.departTree = res.result;
        //     // 默认展开父节点
        //     this.expandedKeys = this.departTree.map(item => item.id)
        //   }
        // })
      },
      modalFormOk() {
        this.loadData();
      }
    }
  }
</script>

<style scoped>
  .ant-table-tbody .ant-table-row td {
    padding-top: 10px;
    padding-bottom: 10px;
  }

  #components-layout-demo-custom-trigger .trigger {
    font-size: 18px;
    line-height: 64px;
    padding: 0 24px;
    cursor: pointer;
    transition: color .3s;
  }
</style>